package edu.unl.csce466.imgui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class ImGuiRenderer {
    private static ImGuiRenderer INSTANCE = null;

    public static ImGuiRenderer getInstance() {
        if (INSTANCE == null) INSTANCE = new ImGuiRenderer();
        return INSTANCE;
    }

    private ArrayList<ImGuiCall> preDrawCalls = new ArrayList<ImGuiCall>();
    private ArrayList<ImGuiCall> drawCalls = new ArrayList<ImGuiCall>();

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl = new ImGuiImplGl3();

    // Флаг того, что ImGui-контекст уже создан.
    // Инициализация вызывается один раз из миксина RenderSystemMixin#initRenderer
    // (в момент старта рендера Minecraft), повторные вызовы init() игнорируются.
    private boolean initialized = false;

    private ImGuiRenderer() {
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void init() {
        init(() -> {});
    }

    public void init(ImGuiCall config) {
        ImGui.createContext();
        config.execute();

        // Явно отключаем Viewports - это перекрывает любой дефолт
        ImGui.getIO().setConfigFlags(ImGui.getIO().getConfigFlags() & ~ImGuiConfigFlags.ViewportsEnable);

        // Docking оставляем (для вкладок внутри окна)
        ImGui.getIO().addConfigFlags(ImGuiConfigFlags.DockingEnable);

        // installCallbacks = true: ImGui сам ставит GLFW-коллбэки (key / char / mouse / scroll / cursorpos)
        // и автоматически вызывает предыдущие (майнкрафтовские) через встроенную цепочку.
        imGuiGlfw.init(Minecraft.getInstance().getWindow().getWindow(), true);

        // ===== Загрузка шрифта с поддержкой кириллицы =====
        loadCyrillicFont();

        try {
            initGl3Renderer("#version 410 core");
            initialized = true;
            return;
        } catch (Exception ignored) {
        }

        try {
            initGl3Renderer("#version 150 core");
            initialized = true;
            return;
        } catch (Exception ignored) {
        }

        throw new RuntimeException("Failed to initialize ImGuiImplGl3");
    }

    private void initGl3Renderer(String glslVersion) {
        try {
            try {
                imGuiGl.getClass().getMethod("init", String.class).invoke(imGuiGl, glslVersion);
                return;
            } catch (NoSuchMethodException ignored) {
            }

            try {
                imGuiGl.getClass().getMethod("init").invoke(imGuiGl);
                return;
            } catch (NoSuchMethodException ignored) {
            }

            try {
                imGuiGl.getClass().getMethod("init", String.class, boolean.class).invoke(imGuiGl, glslVersion, false);
                return;
            } catch (NoSuchMethodException ignored) {
            }

            throw new IllegalStateException("Unsupported ImGuiImplGl3 API: no compatible init() method found");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to initialize ImGuiImplGl3 via reflection", e);
        }
    }

    private void newFrameGl3Renderer() {
        try {
            imGuiGl.getClass().getMethod("newFrame").invoke(imGuiGl);
        } catch (NoSuchMethodException ignored) {
            // Старые версии не требуют newFrame для GL3
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to call ImGuiImplGl3.newFrame() via reflection", e);
        }
    }

    public void preDraw(ImGuiCall drawCall) {
        preDrawCalls.add(drawCall);
    }

    public void draw(ImGuiCall drawCall) {
        drawCalls.add(drawCall);
    }

    public void render() {
        for (ImGuiCall preDrawCall : preDrawCalls) {
            preDrawCall.execute();
        }
        preDrawCalls.clear();

        imGuiGlfw.newFrame();
        newFrameGl3Renderer();
        ImGui.newFrame();

        // Твой ImGui контент здесь (drawCalls)
        for (ImGuiCall drawCall : drawCalls) {
            drawCall.execute();
        }
        drawCalls.clear();

        ImGui.render();
        imGuiGl.renderDrawData(Objects.requireNonNull(ImGui.getDrawData()));

        // Никакого кода для Viewports - он удалён
    }

    // ================= Загрузка шрифта с кириллицей =================
    private void loadCyrillicFont() {
        ImGuiIO io = ImGui.getIO();
        System.out.println("[Ban Assistant] === Font Loading Start ===");

        boolean fontLoaded = false;
        
        // ===== Системный шрифт =====
        System.out.println("[Ban Assistant] Searching for system fonts...");
        String[] fontPaths = new String[] {
            "C:\\Windows\\Fonts\\arial.ttf",
            "C:\\Windows\\Fonts\\segoeui.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
            "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
            "/Library/Fonts/Arial.ttf",
        };

        for (String fontPath : fontPaths) {
            try {
                File f = new File(fontPath);
                if (f.exists() && f.canRead()) {
                    System.out.println("[Ban Assistant] Found: " + fontPath + " - loading...");
                    
                    // Пытаемся загрузить с явным диапазоном кириллицы (Unicode 0x0400-0x04FF)
                    // Создаём массив с диапазонами: начало, конец, 0 (конец списка)
                    short[] cyrillicRanges = new short[] {
                        0x0020, 0x007E,  // ASCII (обычные символы)
                        0x0400, 0x044F,  // Кириллица (русские буквы)
                        0
                    };
                    
                    try {
                        // Пытаемся загрузить с явным указанием диапазонов
                        io.getFonts().addFontFromFileTTF(fontPath, 16.0f, null, cyrillicRanges);
                        fontLoaded = true;
                        System.out.println("[Ban Assistant] ✓✓✓ SUCCESS: Font loaded with Cyrillic ranges!");
                        System.out.println("[Ban Assistant] Font: " + fontPath);
                        break;
                    } catch (Exception e1) {
                        System.out.println("[Ban Assistant] Method with ranges failed (" + e1.getClass().getSimpleName() + ")");
                        
                        try {
                            // Fallback: загружаем без явного указания диапазонов
                            io.getFonts().addFontFromFileTTF(fontPath, 16.0f);
                            fontLoaded = true;
                            System.out.println("[Ban Assistant] ⚠ PARTIAL: Font loaded without Cyrillic specification");
                            System.out.println("[Ban Assistant] Font: " + fontPath);
                            break;
                        } catch (Exception e2) {
                            System.out.println("[Ban Assistant] Method without ranges also failed");
                        }
                    }
                }
            } catch (Exception e) {
                // Молчим, переходим к следующему
            }
        }

        // ===== Если ничего не загрузилось, используем встроенный шрифт =====
        if (!fontLoaded) {
            System.out.println("[Ban Assistant] ✗✗✗ FAILED: No suitable font found");
            System.out.println("[Ban Assistant] ImGui will use built-in ASCII font (Cyrillic will show as ?????)");
            System.out.println("[Ban Assistant]");
            System.out.println("[Ban Assistant] === SOLUTION ===");
            System.out.println("[Ban Assistant] 1. Make sure your system has fonts with Cyrillic:");
            System.out.println("[Ban Assistant]    - Windows: Arial, Segoe UI (usually pre-installed)");
            System.out.println("[Ban Assistant]    - Linux: sudo apt install fonts-liberation fonts-dejavu");
            System.out.println("[Ban Assistant]    - macOS: Usually has Arial pre-installed");
            System.out.println("[Ban Assistant] 2. OR place Roboto-Regular.ttf in:");
            System.out.println("[Ban Assistant]    src/main/resources/assets/csce466/fonts/Roboto-Regular.ttf");
            System.out.println("[Ban Assistant]");
        } else {
            System.out.println("[Ban Assistant] === Font loaded successfully ===");
        }
    }
}

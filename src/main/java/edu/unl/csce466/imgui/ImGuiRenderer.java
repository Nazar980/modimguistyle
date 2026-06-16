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
        System.out.println("[Ban Assistant] === Font Loading Debug ===");
        System.out.println("[Ban Assistant] Available glyphRanges methods:");
        try {
            short[] cyrillic = io.getFonts().getGlyphRangesCyrillic();
            System.out.println("[Ban Assistant] ✓ getGlyphRangesCyrillic exists, size: " + cyrillic.length);
        } catch (Exception e) {
            System.out.println("[Ban Assistant] ✗ getGlyphRangesCyrillic failed: " + e.getMessage());
        }

        boolean fontLoaded = false;
        
        // ===== Попытка 1: системный шрифт напрямую (самый надёжный способ) =====
        System.out.println("[Ban Assistant] === Trying system fonts (Attempt 1) ===");
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
                System.out.println("[Ban Assistant] Checking: " + fontPath + " (exists: " + f.exists() + ")");
                if (f.exists() && f.canRead()) {
                    System.out.println("[Ban Assistant] Attempting to load: " + fontPath);
                    // Пробуем разные сигнатуры метода addFontFromFileTTF
                    try {
                        // Сигнатура 1: (String, float, ImFontConfig, short[])
                        short[] cyrillic = io.getFonts().getGlyphRangesCyrillic();
                        io.getFonts().addFontFromFileTTF(fontPath, 16.0f, null, cyrillic);
                        fontLoaded = true;
                        System.out.println("[Ban Assistant] ✓✓✓ SUCCESS: Loaded with sig1 (with cyrillic): " + fontPath);
                        break;
                    } catch (Exception e1) {
                        System.out.println("[Ban Assistant] Sig1 failed: " + e1.getClass().getSimpleName());
                        try {
                            // Сигнатура 2: (String, float)
                            io.getFonts().addFontFromFileTTF(fontPath, 16.0f);
                            fontLoaded = true;
                            System.out.println("[Ban Assistant] ✓✓ PARTIAL: Loaded with sig2 (no cyrillic specified): " + fontPath);
                            break;
                        } catch (Exception e2) {
                            System.out.println("[Ban Assistant] Sig2 also failed: " + e2.getClass().getSimpleName());
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("[Ban Assistant] Exception checking " + fontPath + ": " + e.getMessage());
            }
        }

        if (!fontLoaded) {
            System.out.println("[Ban Assistant] ✗ Could not load any system font with Cyrillic support.");
            System.out.println("[Ban Assistant] ImGui will use built-in font (ASCII only - text will show as ?????)");
            System.out.println("[Ban Assistant] SOLUTION: Make sure your system has fonts like Arial, DejaVu Sans, or Liberation Sans");
        } else {
            System.out.println("[Ban Assistant] === Font Load Successful ===");
        }
    }
}

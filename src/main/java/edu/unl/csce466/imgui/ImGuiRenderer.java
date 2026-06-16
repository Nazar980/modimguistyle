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

        // ===== Загрузка шрифта с поддержкой кириллицы (ПОСЛЕ imGuiGlfw.init!) =====
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
        System.out.println("\n\n====== [Ban Assistant] FONT LOADING DEBUG ======");

        // ===== СТРАТЕГИЯ 1: Загружаем из ассетс мода (если файл там лежит) =====
        System.out.println("[FA] Attempt 1: Load from assets (src/main/resources/assets/csce466/fonts/Roboto-Regular.ttf)");
        try {
            Minecraft mc = Minecraft.getInstance();
            ResourceLocation fontResource = new ResourceLocation("csce466", "fonts/Roboto-Regular.ttf");
            System.out.println("[FA] ResourceLocation created: " + fontResource);
            
            String tempPath = System.getProperty("java.io.tmpdir") + File.separator + "csce466_font.ttf";
            System.out.println("[FA] Temp path: " + tempPath);
            
            try (InputStream is = mc.getResourceManager().getResource(fontResource).getInputStream();
                 FileOutputStream fos = new FileOutputStream(tempPath)) {
                byte[] buffer = new byte[8192];
                int len, total = 0;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    total += len;
                }
                System.out.println("[FA] ✓ Font file extracted: " + total + " bytes");
            }
            
            // Загружаем в ImGui
            short[] cyrillic = new short[] { 0x0020, 0x007E, 0x0400, 0x044F, 0 };
            io.getFonts().addFontFromFileTTF(tempPath, 16.0f, null, cyrillic);
            System.out.println("[FA] ✓✓✓ SUCCESS: Font from assets loaded with Cyrillic!");
            System.out.println("====== [Ban Assistant] FONT READY ======\n");
            return;  // Выход, если успешно
        } catch (Exception e) {
            System.out.println("[FA] ✗ Failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

        // ===== СТРАТЕГИЯ 2: Системные шрифты =====
        System.out.println("[FA] Attempt 2: Load from system fonts");
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
                if (!f.exists()) {
                    System.out.println("[FA] X " + fontPath);
                    continue;
                }
                System.out.println("[FA] Found: " + fontPath);
                
                short[] cyrillic = new short[] { 0x0020, 0x007E, 0x0400, 0x044F, 0 };
                io.getFonts().addFontFromFileTTF(fontPath, 16.0f, null, cyrillic);
                System.out.println("[FA] ✓✓✓ SUCCESS: System font loaded with Cyrillic!");
                System.out.println("====== [Ban Assistant] FONT READY ======\n");
                return;  // Выход, если успешно
            } catch (Exception e) {
                System.out.println("[FA] ! " + fontPath + ": " + e.getClass().getSimpleName());
            }
        }

        // ===== СТРАТЕГИЯ 3: Используем встроенный шрифт ImGui (ASCII-only) =====
        System.out.println("[FA] ✗✗✗ All attempts failed. Using ImGui default font (ASCII only).");
        System.out.println("[FA] Cyrillic text will display as: ?????");
        System.out.println("[FA]");
        System.out.println("[FA] ===== HOW TO FIX =====");
        System.out.println("[FA] Option 1: Download font and place:");
        System.out.println("[FA]   src/main/resources/assets/csce466/fonts/Roboto-Regular.ttf");
        System.out.println("[FA] (Download from: https://fonts.google.com/?query=roboto)");
        System.out.println("[FA]");
        System.out.println("[FA] Option 2: Install Cyrillic fonts on your system:");
        System.out.println("[FA]   Windows: Arial/Segoe UI (pre-installed)");
        System.out.println("[FA]   Linux: sudo apt install fonts-liberation fonts-dejavu");
        System.out.println("[FA]   macOS: System already has Cyrillic fonts");
        System.out.println("====== [Ban Assistant] FONT INITIALIZATION COMPLETE ======\n");
    }
}

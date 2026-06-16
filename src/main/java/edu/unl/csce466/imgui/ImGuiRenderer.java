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
        // Это единственный нормальный способ, чтобы:
        //   1) работал текстовый ввод в полях ImGui (буквы, заглавные, Shift+<key>, кириллица, backspace, Ctrl+V),
        //   2) не ломались клавиатурные/мышиные события Minecraft когда ImGui не открыт,
        //   3) не приходилось вручную прокидывать все события через Forge-ивенты.
        imGuiGlfw.init(Minecraft.getInstance().getWindow().getWindow(), true);

        // ===== Загрузка шрифта с поддержкой кириллицы =====
        // Dear ImGui по умолчанию использует встроенный шрифт, который содержит только ASCII.
        // Для русского текста нужно явно добавить шрифт TTF с поддержкой кириллицы.
        // Ищем системный шрифт (Windows / Linux / macOS) или используем встроенный Roboto.
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

    // ================= Загрузка шрифта с кириллицей из ассетс =================
    private void loadCyrillicFont() {
        ImGuiIO io = ImGui.getIO();
        boolean fontLoaded = false;

        // ===== Вариант 1: загружаем шрифт из ассетс мода =====
        try {
            Minecraft mc = Minecraft.getInstance();
            // Ищем шрифт в папке assets/csce466/fonts/Roboto-Regular.ttf
            ResourceLocation fontResource = new ResourceLocation("csce466", "fonts/Roboto-Regular.ttf");
            
            // Прочитаем поток из ассетс и сохраним в временный файл
            String tempFontPath = System.getProperty("java.io.tmpdir") + File.separator + "imgui_font_csce466.ttf";
            try (InputStream is = mc.getResourceManager().getResource(fontResource).getInputStream();
                 FileOutputStream fos = new FileOutputStream(tempFontPath)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            }
            
            // Теперь загружаем из временного файла в ImGui
            short[] cyrillic = io.getFonts().getGlyphRangesCyrillic();
            io.getFonts().addFontFromFileTTF(tempFontPath, 16.0f, null, cyrillic);
            fontLoaded = true;
            System.out.println("[Ban Assistant] Loaded Cyrillic font from assets: " + tempFontPath);
        } catch (Exception e) {
            System.out.println("[Ban Assistant] Could not load font from assets: " + e.getMessage());
        }

        // ===== Вариант 2: если ассетс не найден, пробуем системные шрифты =====
        if (!fontLoaded) {
            String[] fontPaths = new String[] {
                "C:\\Windows\\Fonts\\arial.ttf",           // Windows
                "C:\\Windows\\Fonts\\segoeui.ttf",         // Windows (красивый)
                "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",  // Linux (DejaVu)
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",  // Linux (DejaVu)
                "/Library/Fonts/Arial.ttf",                 // macOS
            };

            for (String fontPath : fontPaths) {
                try {
                    File f = new File(fontPath);
                    if (f.exists()) {
                        short[] cyrillic = io.getFonts().getGlyphRangesCyrillic();
                        io.getFonts().addFontFromFileTTF(fontPath, 16.0f, null, cyrillic);
                        fontLoaded = true;
                        System.out.println("[Ban Assistant] Loaded Cyrillic font from system: " + fontPath);
                        break;
                    }
                } catch (Exception ignored) {
                    // Шрифт не найден, пробуем следующий
                }
            }
        }

        if (!fontLoaded) {
            System.out.println("[Ban Assistant] WARNING: Could not load any Cyrillic font. Text will show as ?????");
            System.out.println("[Ban Assistant] Make sure to add Roboto-Regular.ttf to src/main/resources/assets/csce466/fonts/");
        }
    }
}

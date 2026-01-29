package edu.unl.csce466.imgui;

import java.util.ArrayList;
import java.util.Objects;
import org.lwjgl.glfw.GLFW;
import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import net.minecraft.client.Minecraft;

public class ImGuiRenderer {
    private static ImGuiRenderer _INSTANCE = null;

    public static ImGuiRenderer getInstance() {
        if (_INSTANCE == null) _INSTANCE = new ImGuiRenderer();
        return _INSTANCE;
    }

    private ArrayList<ImGuiCall> _preDrawCalls = new ArrayList<ImGuiCall>();
    private ArrayList<ImGuiCall> _drawCalls = new ArrayList<ImGuiCall>();

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl = new ImGuiImplGl3();

    private ImGuiRenderer() {
        // Конструктор пустой
    }

    public void init() {
        init(() -> {});
    }

    public void init(ImGuiCall config) {
        ImGui.createContext();
        config.execute();

        imGuiGlfw.init(Minecraft.getInstance().getWindow().getWindow(), false);

        // Отключаем Viewports — окна теперь строго внутри Minecraft
        // ImGui.getIO().addConfigFlags(ImGuiConfigFlags.ViewportsEnable); // ← УБРАНО

        // Оставляем Docking, если хочешь вкладки внутри окна (можно убрать)
        ImGui.getIO().addConfigFlags(ImGuiConfigFlags.DockingEnable);

        try {
            initGl3Renderer("#version 410 core");
            return;
        } catch (Exception ignored) {
        }

        try {
            initGl3Renderer("#version 150 core");
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
        _preDrawCalls.add(drawCall);
    }

    public void draw(ImGuiCall drawCall) {
        _drawCalls.add(drawCall);
    }

    public void render() {
        for (ImGuiCall preDrawCall : _preDrawCalls) {
            preDrawCall.execute();
        }
        _preDrawCalls.clear();

        imGuiGlfw.newFrame();
        newFrameGl3Renderer();
        ImGui.newFrame();

        // Здесь рендер твоего ImGui контента
        for (ImGuiCall drawCall : _drawCalls) {
            drawCall.execute();
        }
        _drawCalls.clear();

        ImGui.render();
        imGuiGl.renderDrawData(Objects.requireNonNull(ImGui.getDrawData()));

        // Эта часть нужна только если Viewports включены — теперь её можно убрать
        // if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) { ... }
        // Убрано полностью
    }
}

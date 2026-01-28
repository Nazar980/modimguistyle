package edu.unl.csce466.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import edu.unl.csce466.imgui.ImGuiRenderer;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ImGuiScreen extends Screen {
    private static ImGuiScreen _INSTANCE = null;
    private boolean buttonClicked = false;
    private final ImBoolean showDemo = new ImBoolean(false);

    public static ImGuiScreen getInstance() {
        if (_INSTANCE == null) _INSTANCE = new ImGuiScreen();
        return _INSTANCE;
    }

    private ImGuiScreen() {
        super(Component.literal("ImGui"));
    }

    @Override
    public void init() {
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        ImGuiIO io = ImGui.getIO();
        Minecraft mc = Minecraft.getInstance();
        io.setMousePos((float) mc.mouseHandler.xpos(), (float) mc.mouseHandler.ypos());

        ImGuiRenderer.getInstance().draw(() -> {
            setupDarkStyle();

            ImGui.begin("ImGui Example");
            ImGui.text("Minecraft 1.19.2 + ImGui");
            if (ImGui.button("Click me")) {
                buttonClicked = true;
            }
            if (buttonClicked) {
                ImGui.text("Button clicked!");
            }
            if (ImGui.button("Toggle Demo Window")) {
                showDemo.set(!showDemo.get());
            }
            ImGui.end();

            if (showDemo.get()) {
                ImGui.showDemoWindow(showDemo);
            }
        });
    }

    private void setupDarkStyle() {
        ImGuiStyle style = ImGui.getStyle();

        style.setWindowRounding(8.0f);
        style.setFrameRounding(4.0f);
        style.setGrabRounding(4.0f);
        style.setScrollbarRounding(9.0f);
        style.setWindowPadding(12.0f, 12.0f);

        // Тёмный стиль GD Mega Hack — цвета в формате int (ImU32)
        style.setColor(ImGuiCol.WindowBg,       rgba(26, 26, 46, 242));     // #1a1a2e ~95% opacity
        style.setColor(ImGuiCol.TitleBg,        rgba(15, 15, 39, 242));
        style.setColor(ImGuiCol.TitleBgActive,  rgba(0, 212, 255, 204));    // cyan #00d4ff
        style.setColor(ImGuiCol.FrameBg,        rgba(30, 30, 60, 204));
        style.setColor(ImGuiCol.FrameBgHovered, rgba(0, 120, 255, 153));
        style.setColor(ImGuiCol.FrameBgActive,  rgba(0, 140, 255, 204));
        style.setColor(ImGuiCol.Button,         rgba(0, 80, 200, 153));
        style.setColor(ImGuiCol.ButtonHovered,  rgba(0, 120, 255, 204));
        style.setColor(ImGuiCol.ButtonActive,   rgba(0, 160, 255, 255));
        style.setColor(ImGuiCol.Text,           rgba(230, 230, 242, 255));  // светлый текст
        style.setColor(ImGuiCol.CheckMark,      rgba(0, 212, 255, 255));    // cyan чек
    }

    // Вспомогательная функция для создания ImU32 цвета
    private int rgba(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ImGui.getIO().setMouseDown(button, true);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        ImGui.getIO().setMouseDown(button, false);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        ImGui.getIO().setMouseWheel((float) delta);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

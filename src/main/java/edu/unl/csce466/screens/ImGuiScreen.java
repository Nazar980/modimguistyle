package edu.unl.csce466.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import edu.unl.csce466.imgui.ImGuiRenderer;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;           // ← Это был пропущен, теперь добавлен
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
            setupDarkStyle();  // Применяем тёмный стиль

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

        // Округления и отступы
        style.setWindowRounding(8.0f);
        style.setFrameRounding(4.0f);
        style.setGrabRounding(4.0f);
        style.setScrollbarRounding(9.0f);
        style.setWindowPadding(12.0f, 12.0f);

        // Тёмный стиль GD Mega Hack
        style.setColor(ImGuiCol.WindowBg,       new float[]{26f/255f, 26f/255f, 46f/255f, 0.95f});   // #1a1a2e
        style.setColor(ImGuiCol.TitleBg,        new float[]{15f/255f, 15f/255f, 39f/255f, 0.95f});   // #0f0f27
        style.setColor(ImGuiCol.TitleBgActive,  new float[]{0f/255f, 212f/255f, 255f/255f, 0.80f});  // #00d4ff cyan
        style.setColor(ImGuiCol.FrameBg,        new float[]{30f/255f, 30f/255f, 60f/255f, 0.80f});
        style.setColor(ImGuiCol.FrameBgHovered, new float[]{0f/255f, 120f/255f, 255f/255f, 0.60f});
        style.setColor(ImGuiCol.FrameBgActive,  new float[]{0f/255f, 140f/255f, 255f/255f, 0.80f});
        style.setColor(ImGuiCol.Button,         new float[]{0f/255f, 80f/255f, 200f/255f, 0.60f});
        style.setColor(ImGuiCol.ButtonHovered,  new float[]{0f/255f, 120f/255f, 255f/255f, 0.80f});
        style.setColor(ImGuiCol.ButtonActive,   new float[]{0f/255f, 160f/255f, 255f/255f, 1.00f});
        style.setColor(ImGuiCol.Text,           new float[]{0.9f, 0.9f, 0.95f, 1.0f});               // Светлый текст
        style.setColor(ImGuiCol.CheckMark,      new float[]{0f/255f, 212f/255f, 255f/255f, 1.0f});   // Cyan чекбокс
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

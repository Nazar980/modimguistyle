package edu.unl.csce466.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import edu.unl.csce466.imgui.ImGuiRenderer;
import imgui.ImGui;
import imgui.ImGuiIO;
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

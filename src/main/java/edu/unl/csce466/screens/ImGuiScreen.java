package edu.unl.csce466.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import edu.unl.csce466.imgui.ImGuiRenderer;
import imgui.ImGui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ImGuiScreen extends Screen {

    private static ImGuiScreen _INSTANCE = null;
    private boolean buttonClicked = false;

    public static ImGuiScreen getInstance() {
        if (_INSTANCE == null) _INSTANCE = new ImGuiScreen();
        return _INSTANCE;
    }

    private ImGuiScreen() {
        super(Component.literal("ImGui"));
    }

    public void init() {
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        ImGuiRenderer.getInstance().draw(() -> {
            ImGui.begin("ImGui Example");
            ImGui.text("Minecraft 1.19.2 + ImGui");

            if (ImGui.button("Click me")) {
                buttonClicked = true;
            }

            if (buttonClicked) {
                ImGui.text("Button clicked!");
            }

            ImGui.end();
        });
    }
}

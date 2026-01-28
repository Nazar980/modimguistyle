package edu.unl.csce466.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import edu.unl.csce466.ExampleMod;
import edu.unl.csce466.imgui.ImGuiRenderer;
import imgui.ImGui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ImGuiScreen extends Screen {

    private static ImGuiScreen _INSTANCE = null;
    private boolean _buttonClicked = false;

    public static ImGuiScreen getInstance() {
        if (_INSTANCE == null) _INSTANCE = new ImGuiScreen();
        return _INSTANCE;
    }

    private ImGuiScreen() {
        super(Component.literal("ImGui"));
    }

    public void init() {
        // можно оставить пустым или добавить что-то при необходимости
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        // фон по желанию (полупрозрачный или чёрный)
        // this.renderBackground(poseStack);

        ImGuiRenderer.getInstance().draw(() -> {
            ShowModMenu();
        });

        // Убрал дублирующееся окно "Custom Window" — было два одинаковых
        ImGuiRenderer.getInstance().draw(() -> {
            ImGui.begin("Example Window");
            ImGui.text("Hello from ImGui in Minecraft 1.19.2");

            if (ImGui.button("Click me!")) {
                _buttonClicked = true;
            }
            if (_buttonClicked) {
                ImGui.text("Button was clicked!");
            }
            ImGui.end();
        });
    }

    private void ShowModMenu() {
        ExampleMod.Zeus zeus = new ExampleMod.Zeus();

        if (ImGui.beginMenu("Level Up")) {
            if (ImGui.menuItem("Level Up!")) {
                zeus.LevelUp();
            }
            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Health")) {
            if (ImGui.menuItem("Health boost")) {
                zeus.Health();
            }
            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Diamonds")) {
            if (ImGui.menuItem("Give Diamonds")) {
                zeus.GiveDiamonds();
            }
            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Stick")) {
            if (ImGui.menuItem("Stick!")) {
                zeus.Stick();
            }
            ImGui.endMenu();
        }
    }
}

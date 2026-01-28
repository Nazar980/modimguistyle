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
            setupStyle();

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

    private void setupStyle() {
        ImGuiStyle style = ImGui.getStyle();

        // Базовые скругления и отступы
        style.setWindowRounding(6.0f);
        style.setFrameRounding(4.0f);
        style.setTabRounding(4.0f);
        style.setGrabRounding(4.0f);
        style.setScrollbarRounding(6.0f);
        style.setWindowPadding(12.0f, 12.0f);
        style.setFramePadding(6.0f, 4.0f);
        style.setItemSpacing(8.0f, 6.0f);

        // Основные цвета
        style.setColor(ImGuiCol.WindowBg,       rgba(30, 30, 35, 240));     // тёмный фон окна
        style.setColor(ImGuiCol.TitleBg,        rgba(231, 57, 102, 240));   // основной цвет заголовка (когда окно не активно)
        style.setColor(ImGuiCol.TitleBgActive,  rgba(241, 87, 132, 240));   // чуть светлее, когда окно активно/фокус
        style.setColor(ImGuiCol.TitleBgCollapsed, rgba(231, 57, 102, 180));

        // Текст — полностью белый
        style.setColor(ImGuiCol.Text,           rgba(255, 255, 255, 255));
        style.setColor(ImGuiCol.TextDisabled,   rgba(180, 180, 190, 180));

        // Табы — спокойные
        style.setColor(ImGuiCol.Tab,            rgba(42, 42, 42, 255));
        style.setColor(ImGuiCol.TabHovered,     rgba(60, 60, 65, 255));
        style.setColor(ImGuiCol.TabActive,      rgba(50, 50, 55, 255));
        style.setColor(ImGuiCol.TabUnfocused,   rgba(42, 42, 42, 200));
        style.setColor(ImGuiCol.TabUnfocusedActive, rgba(50, 50, 55, 200));

        // Кнопки и фреймы — нейтральные
        style.setColor(ImGuiCol.Button,         rgba(70, 70, 80, 200));
        style.setColor(ImGuiCol.ButtonHovered,  rgba(90, 90, 100, 220));
        style.setColor(ImGuiCol.ButtonActive,   rgba(110, 110, 120, 255));
        style.setColor(ImGuiCol.CheckMark,      rgba(231, 57, 102, 255));   // акцент на чекбоксе
        style.setColor(ImGuiCol.FrameBg,        rgba(50, 50, 55, 200));
        style.setColor(ImGuiCol.FrameBgHovered, rgba(70, 70, 80, 220));
        style.setColor(ImGuiCol.FrameBgActive,  rgba(90, 90, 100, 255));
    }

    // Вспомогательная функция для ImU32 цвета
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

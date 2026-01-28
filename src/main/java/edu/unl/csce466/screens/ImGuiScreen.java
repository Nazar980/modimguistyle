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
            setupCalmStyle();

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

    private void setupCalmStyle() {
        ImGuiStyle style = ImGui.getStyle();

        // Базовые отступы и скругления
        style.setWindowRounding(6.0f);
        style.setFrameRounding(4.0f);
        style.setTabRounding(4.0f);
        style.setGrabRounding(4.0f);
        style.setScrollbarRounding(6.0f);
        style.setWindowPadding(12.0f, 12.0f);
        style.setFramePadding(6.0f, 4.0f);
        style.setItemSpacing(8.0f, 6.0f);

        // Цвета
        style.setColor(ImGuiCol.WindowBg,       rgba(30, 30, 35, 240));     // тёмный фон окна
        style.setColor(ImGuiCol.TitleBg,        rgba(25, 25, 30, 240));
        style.setColor(ImGuiCol.TitleBgActive,  rgba(25, 25, 30, 240));

        // Табы — спокойные цвета
        style.setColor(ImGuiCol.Tab,            rgba(42, 42, 42, 255));      // обычный таб фон
        style.setColor(ImGuiCol.TabHovered,     rgba(60, 60, 65, 255));      // hovered — чуть светлее
        style.setColor(ImGuiCol.TabActive,      rgba(231, 57, 102, 255));    // активный таб — твой основной цвет
        style.setColor(ImGuiCol.TabUnfocused,   rgba(42, 42, 42, 200));
        style.setColor(ImGuiCol.TabUnfocusedActive, rgba(231, 57, 102, 200));

        // Текст — полностью белый везде
        style.setColor(ImGuiCol.Text,           rgba(255, 255, 255, 255));
        style.setColor(ImGuiCol.TextDisabled,   rgba(180, 180, 190, 180));

        // Кнопки и чекбоксы — спокойные, без неона
        style.setColor(ImGuiCol.Button,         rgba(70, 70, 80, 200));
        style.setColor(ImGuiCol.ButtonHovered,  rgba(90, 90, 100, 220));
        style.setColor(ImGuiCol.ButtonActive,   rgba(231, 57, 102, 180));
        style.setColor(ImGuiCol.CheckMark,      rgba(231, 57, 102, 255));
        style.setColor(ImGuiCol.FrameBg,        rgba(50, 50, 55, 200));
        style.setColor(ImGuiCol.FrameBgHovered, rgba(70, 70, 80, 220));
        style.setColor(ImGuiCol.FrameBgActive,  rgba(231, 57, 102, 140));
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

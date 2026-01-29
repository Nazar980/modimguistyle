package edu.unl.csce466.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import edu.unl.csce466.imgui.ImGuiRenderer;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
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

            // Центрируем окно при первом открытии
            ImGui.setNextWindowSize(420, 320, ImGuiCond.FirstUseEver);
            ImGui.setNextWindowPos(
                (io.getDisplaySizeX() - 420) * 0.5f,
                (io.getDisplaySizeY() - 320) * 0.5f,
                ImGuiCond.FirstUseEver
            );

            int flags = ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize;

            ImGui.begin("ImGui Example", flags);

            // Центрируем текст заголовка вручную
            String titleText = "ImGui Example";
            float textWidth = ImGui.calcTextSize(titleText).x;
            float titleBarWidth = ImGui.getWindowWidth() - ImGui.getStyle().getWindowPaddingX() * 2;
            float cursorX = (titleBarWidth - textWidth) * 0.5f + ImGui.getStyle().getWindowPaddingX();
            ImGui.setCursorPosX(cursorX);
            ImGui.text(titleText);

            ImGui.separator();

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

        // Убираем все скругления углов → квадратные, как в Minecraft
        style.setWindowRounding(0.0f);
        style.setFrameRounding(0.0f);
        style.setTabRounding(0.0f);
        style.setGrabRounding(0.0f);
        style.setScrollbarRounding(0.0f);
        style.setPopupRounding(0.0f);

        // Отступы
        style.setWindowPadding(10.0f, 10.0f);
        style.setFramePadding(6.0f, 4.0f);
        style.setItemSpacing(8.0f, 6.0f);

        // Цвета
        style.setColor(ImGuiCol.WindowBg,       rgba(30, 30, 35, 180));      // фон окна
        style.setColor(ImGuiCol.TitleBg,        rgba(245, 70, 130, 220));    // заголовок (розовый)
        style.setColor(ImGuiCol.TitleBgActive,  rgba(255, 90, 150, 220));    // активный заголовок
        style.setColor(ImGuiCol.TitleBgCollapsed, rgba(245, 70, 130, 160));

        style.setColor(ImGuiCol.Text,           rgba(255, 255, 255, 255));   // весь текст белый
        style.setColor(ImGuiCol.TextDisabled,   rgba(180, 180, 190, 140));

        style.setColor(ImGuiCol.Tab,            rgba(42, 42, 42, 180));
        style.setColor(ImGuiCol.TabHovered,     rgba(60, 60, 65, 200));
        style.setColor(ImGuiCol.TabActive,      rgba(50, 50, 55, 220));

        style.setColor(ImGuiCol.Button,         rgba(70, 70, 80, 160));
        style.setColor(ImGuiCol.ButtonHovered,  rgba(90, 90, 100, 180));
        style.setColor(ImGuiCol.ButtonActive,   rgba(110, 110, 120, 220));
        style.setColor(ImGuiCol.CheckMark,      rgba(245, 70, 130, 255));    // чекбоксы
        style.setColor(ImGuiCol.FrameBg,        rgba(50, 50, 55, 160));
        style.setColor(ImGuiCol.FrameBgHovered, rgba(70, 70, 80, 180));
        style.setColor(ImGuiCol.FrameBgActive,  rgba(90, 90, 100, 220));
    }

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

package edu.unl.csce466.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import edu.unl.csce466.imgui.ImGuiRenderer;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

public class ImGuiScreen extends Screen {
    private static ImGuiScreen INSTANCE = null;
    private boolean buttonClicked = false;
    private final ImBoolean showDemo = new ImBoolean(false);
    private final ImBoolean showStyleEditor = new ImBoolean(false);

    // Цвета (розовый заголовок 245,70,130,220 везде)
    private final float[] colWindowBg = {30/255f, 30/255f, 35/255f, 180/255f};
    private final float[] colTitleBg = {245/255f, 70/255f, 130/255f, 220/255f};  // <- Твой любимый
    private final float[] colTitleBgActive = {245/255f, 70/255f, 130/255f, 220/255f};
    private final float[] colTitleBgCollapsed = {245/255f, 70/255f, 130/255f, 160/255f};
    private final float[] colText = {1f, 1f, 1f, 1f};
    private final float[] colTextDisabled = {180/255f, 180/255f, 190/255f, 140/255f};
    private final float[] colTab = {42/255f, 42/255f, 42/255f, 180/255f};
    private final float[] colTabHovered = {60/255f, 60/255f, 65/255f, 200/255f};
    private final float[] colTabActive = {50/255f, 50/255f, 55/255f, 220/255f};
    private final float[] colButton = {70/255f, 70/255f, 80/255f, 160/255f};
    private final float[] colButtonHovered = {90/255f, 90/255f, 100/255f, 180/255f};
    private final float[] colButtonActive = {110/255f, 110/255f, 120/255f, 220/255f};
    private final float[] colCheckMark = {245/255f, 70/255f, 130/255f, 1f};
    private final float[] colFrameBg = {50/255f, 50/255f, 55/255f, 160/255f};
    private final float[] colFrameBgHovered = {70/255f, 70/255f, 80/255f, 180/255f};
    private final float[] colFrameBgActive = {90/255f, 90/255f, 100/255f, 220/255f};

    public static ImGuiScreen getInstance() {
        if (INSTANCE == null) INSTANCE = new ImGuiScreen();
        return INSTANCE;
    }

    private ImGuiScreen() {
        // 1.16.5: Component.literal(...) -> new StringTextComponent(...)
        super(new StringTextComponent("ImGui"));
    }

    @Override
    // 1.16.5: render принимает MatrixStack (в 1.19.2 был PoseStack)
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTick) {
        ImGuiRenderer renderer = ImGuiRenderer.getInstance();

        ImGuiIO io = ImGui.getIO();
        // Позиция мыши теперь автоматически обновляется GLFW cursor-pos callback-ом
        // (imGuiGlfw.init с installCallbacks=true). Вручную её дёргать не нужно.

        // Регистрируем содержимое меню для текущего кадра.
        // Сам рендер ImGui (newFrame + render) выполняется глобально через миксин
        // RenderSystemMixin#flipFrame в конце кадра, после всей отрисовки мира/HUD.
        renderer.draw(() -> {
            applyCurrentColors();

            ImGui.setNextWindowSize(500, 400, ImGuiCond.FirstUseEver);
            ImGui.setNextWindowPos(
                (io.getDisplaySizeX() - 500) * 0.5f,
                (io.getDisplaySizeY() - 400) * 0.5f,
                ImGuiCond.FirstUseEver
            );

            // Разрешаем закрытие, сворачивание и ресайз - убрали NoCollapse и NoResize
            int flags = ImGuiWindowFlags.None;

            ImGui.begin("GD Mega Hack", flags);  // <- Родной заголовок с кнопками x и -

            // ====== Главное меню (в стиле ImGui Demo: Menu / Examples / Tools) ======
            if (ImGui.beginMainMenuBar()) {

                // ---- Menu ----
                if (ImGui.beginMenu("Menu")) {
                    if (ImGui.menuItem("Close menu", "ESC")) {
                        // Закрытие меню по клику/ESC (так же, как крестик в title bar)
                        Minecraft.getInstance().setScreen(null);
                    }
                    ImGui.separator();
                    if (ImGui.menuItem("Exit")) {
                        Minecraft.getInstance().stop();
                    }
                    ImGui.endMenu();
                }

                // ---- Examples ----
                if (ImGui.beginMenu("Examples")) {
                    // Просто пример чекнутого/нечекнутого пункта в меню
                    ImGui.menuItem("Test button visible", "", true);
                    ImGui.menuItem("Coming soon: tabs", "", false);
                    ImGui.menuItem("Coming soon: sliders", "", false);
                    ImGui.endMenu();
                }

                // ---- Tools ----
                if (ImGui.beginMenu("Tools")) {
                    if (ImGui.menuItem("Style Color Editor", "", showStyleEditor.get())) {
                        showStyleEditor.set(!showStyleEditor.get());
                    }
                    if (ImGui.menuItem("Show ImGui Demo Window", "", showDemo.get())) {
                        showDemo.set(!showDemo.get());
                    }
                    ImGui.endMenu();
                }

                // ---- Help (чтобы справа не пусто) ----
                // Чтобы прижать Help вправо — в ImGui это делается через sameLine()+invisibleButton,
                // но для простоты пока оставим слева.
                if (ImGui.beginMenu("Help")) {
                    ImGui.text("GD Mega Hack for 1.16.5");
                    ImGui.text("ImGui rendering via imgui-java");
                    ImGui.separator();
                    ImGui.textWrapped("Press L in-game to open/close this menu.");
                    ImGui.endMenu();
                }

                ImGui.endMainMenuBar();
            }

            // ====== Основная область окна ======
            ImGui.text("Welcome to GD Mega Hack!");
            ImGui.textWrapped("Use the menu bar at the top to open the Style Color Editor, the ImGui Demo window, " +
                    "or close this menu. Toggles for cheats (Fly, KillAura, ESP, Fullbright, ...) can be added here.");
            ImGui.separator();

            ImGui.text("Test area:");
            if (ImGui.button("Click me")) {
                buttonClicked = true;
            }
            if (buttonClicked) {
                ImGui.sameLine();
                ImGui.text("Button clicked!");
            }

            ImGui.end();

            if (showStyleEditor.get()) {
                ImGui.begin("Style Color Editor", showStyleEditor, ImGuiWindowFlags.NoCollapse);
                ImGui.text("Change menu colors live (apply immediately):");
                ImGui.separator();

                ImGui.colorEdit4("WindowBg", colWindowBg);
                ImGui.colorEdit4("TitleBg", colTitleBg);
                ImGui.colorEdit4("TitleBgActive", colTitleBgActive);
                ImGui.colorEdit4("TitleBgCollapsed", colTitleBgCollapsed);
                ImGui.separator();
                ImGui.colorEdit4("Text", colText);
                ImGui.colorEdit4("TextDisabled", colTextDisabled);
                ImGui.separator();
                ImGui.colorEdit4("Tab", colTab);
                ImGui.colorEdit4("TabHovered", colTabHovered);
                ImGui.colorEdit4("TabActive", colTabActive);
                ImGui.separator();
                ImGui.colorEdit4("Button", colButton);
                ImGui.colorEdit4("ButtonHovered", colButtonHovered);
                ImGui.colorEdit4("ButtonActive", colButtonActive);
                ImGui.separator();
                ImGui.colorEdit4("CheckMark", colCheckMark);
                ImGui.colorEdit4("FrameBg", colFrameBg);
                ImGui.colorEdit4("FrameBgHovered", colFrameBgHovered);
                ImGui.colorEdit4("FrameBgActive", colFrameBgActive);

                ImGui.spacing();
                ImGui.separator();
                ImGui.spacing();

                if (ImGui.button("Reset to Default (pink title)")) {
                    resetColorsToDefault();
                }

                ImGui.end();
            }

            if (showDemo.get()) {
                ImGui.showDemoWindow(showDemo);
            }
        });
    }

    private void applyCurrentColors() {
        ImGuiStyle style = ImGui.getStyle();

        style.setColor(ImGuiCol.WindowBg, colWindowBg[0], colWindowBg[1], colWindowBg[2], colWindowBg[3]);
        style.setColor(ImGuiCol.TitleBg, colTitleBg[0], colTitleBg[1], colTitleBg[2], colTitleBg[3]);
        style.setColor(ImGuiCol.TitleBgActive, colTitleBgActive[0], colTitleBgActive[1], colTitleBgActive[2], colTitleBgActive[3]);
        style.setColor(ImGuiCol.TitleBgCollapsed, colTitleBgCollapsed[0], colTitleBgCollapsed[1], colTitleBgCollapsed[2], colTitleBgCollapsed[3]);
        style.setColor(ImGuiCol.Text, colText[0], colText[1], colText[2], colText[3]);
        style.setColor(ImGuiCol.TextDisabled, colTextDisabled[0], colTextDisabled[1], colTextDisabled[2], colTextDisabled[3]);
        style.setColor(ImGuiCol.Tab, colTab[0], colTab[1], colTab[2], colTab[3]);
        style.setColor(ImGuiCol.TabHovered, colTabHovered[0], colTabHovered[1], colTabHovered[2], colTabHovered[3]);
        style.setColor(ImGuiCol.TabActive, colTabActive[0], colTabActive[1], colTabActive[2], colTabActive[3]);
        style.setColor(ImGuiCol.Button, colButton[0], colButton[1], colButton[2], colButton[3]);
        style.setColor(ImGuiCol.ButtonHovered, colButtonHovered[0], colButtonHovered[1], colButtonHovered[2], colButtonHovered[3]);
        style.setColor(ImGuiCol.ButtonActive, colButtonActive[0], colButtonActive[1], colButtonActive[2], colButtonActive[3]);
        style.setColor(ImGuiCol.CheckMark, colCheckMark[0], colCheckMark[1], colCheckMark[2], colCheckMark[3]);
        style.setColor(ImGuiCol.FrameBg, colFrameBg[0], colFrameBg[1], colFrameBg[2], colFrameBg[3]);
        style.setColor(ImGuiCol.FrameBgHovered, colFrameBgHovered[0], colFrameBgHovered[1], colFrameBgHovered[2], colFrameBgHovered[3]);
        style.setColor(ImGuiCol.FrameBgActive, colFrameBgActive[0], colFrameBgActive[1], colFrameBgActive[2], colFrameBgActive[3]);

        style.setWindowRounding(0.0f);
        style.setFrameRounding(0.0f);
        style.setTabRounding(0.0f);
        style.setGrabRounding(0.0f);
        style.setScrollbarRounding(0.0f);
        style.setPopupRounding(0.0f);
        style.setWindowPadding(10.0f, 10.0f);
        style.setFramePadding(6.0f, 4.0f);
        style.setItemSpacing(8.0f, 6.0f);
    }

    private void resetColorsToDefault() {
        colWindowBg[0] = 30/255f; colWindowBg[1] = 30/255f; colWindowBg[2] = 35/255f; colWindowBg[3] = 180/255f;
        colTitleBg[0] = 245/255f; colTitleBg[1] = 70/255f; colTitleBg[2] = 130/255f; colTitleBg[3] = 220/255f;
        colTitleBgActive[0] = 245/255f; colTitleBgActive[1] = 70/255f; colTitleBgActive[2] = 130/255f; colTitleBgActive[3] = 220/255f;
        colTitleBgCollapsed[0] = 245/255f; colTitleBgCollapsed[1] = 70/255f; colTitleBgCollapsed[2] = 130/255f; colTitleBgCollapsed[3] = 160/255f;
        colText[0] = 1f; colText[1] = 1f; colText[2] = 1f; colText[3] = 1f;
        colTextDisabled[0] = 180/255f; colTextDisabled[1] = 180/255f; colTextDisabled[2] = 190/255f; colTextDisabled[3] = 140/255f;
        colTab[0] = 42/255f; colTab[1] = 42/255f; colTab[2] = 42/255f; colTab[3] = 180/255f;
        colTabHovered[0] = 60/255f; colTabHovered[1] = 60/255f; colTabHovered[2] = 65/255f; colTabHovered[3] = 200/255f;
        colTabActive[0] = 50/255f; colTabActive[1] = 50/255f; colTabActive[2] = 55/255f; colTabActive[3] = 220/255f;
        colButton[0] = 70/255f; colButton[1] = 70/255f; colButton[2] = 80/255f; colButton[3] = 160/255f;
        colButtonHovered[0] = 90/255f; colButtonHovered[1] = 90/255f; colButtonHovered[2] = 100/255f; colButtonHovered[3] = 180/255f;
        colButtonActive[0] = 110/255f; colButtonActive[1] = 110/255f; colButtonActive[2] = 120/255f; colButtonActive[3] = 220/255f;
        colCheckMark[0] = 245/255f; colCheckMark[1] = 70/255f; colCheckMark[2] = 130/255f; colCheckMark[3] = 1f;
        colFrameBg[0] = 50/255f; colFrameBg[1] = 50/255f; colFrameBg[2] = 55/255f; colFrameBg[3] = 160/255f;
        colFrameBgHovered[0] = 70/255f; colFrameBgHovered[1] = 70/255f; colFrameBgHovered[2] = 80/255f; colFrameBgHovered[3] = 180/255f;
        colFrameBgActive[0] = 90/255f; colFrameBgActive[1] = 90/255f; colFrameBgActive[2] = 100/255f; colFrameBgActive[3] = 220/255f;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

package edu.unl.csce466.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import edu.unl.csce466.cheat.CheatManager;
import edu.unl.csce466.imgui.ImGuiRenderer;
import imgui.ImColor;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.ImVec4;                // ← Фикс: imgui.ImVec4, а НЕ imgui.type.ImVec4
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ImGuiScreen extends Screen {
    private static ImGuiScreen _INSTANCE = null;
    private final ImBoolean windowOpen = new ImBoolean(true);  // Для кнопки закрытия окна

    public static ImGuiScreen getInstance() {
        if (_INSTANCE == null) _INSTANCE = new ImGuiScreen();
        return _INSTANCE;
    }

    private ImGuiScreen() {
        super(Component.literal("GD Mega Hack"));
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        ImGuiIO io = ImGui.getIO();
        Minecraft mc = Minecraft.getInstance();

        // Обновляем позицию мыши для ImGui (важно для кликов)
        io.setMousePos((float) mc.mouseHandler.xpos(), (float) mc.mouseHandler.ypos());

        // Рендерим всё меню через draw call
        ImGuiRenderer.getInstance().draw(this::renderMegaHackMenu);
    }

    private void renderMegaHackMenu() {
        // Применяем стиль GD Mega Hack каждый раз (чтобы не слетал)
        setupGDMegaHackStyle();

        // Окно: центрировано при первом открытии, draggable, без ресайза/коллапса
        ImGui.setNextWindowPos(ImGui.getIO().getDisplaySizeX() * 0.5f, ImGui.getIO().getDisplaySizeY() * 0.5f,
                ImGuiCond.FirstUseEver, 0.5f, 0.5f);
        ImGui.setNextWindowSize(540, 460, ImGuiCond.FirstUseEver);

        ImGui.begin("GD Mega Hack v8 - Minecraft Edition", windowOpen,
                ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.AlwaysAutoResize);

        // Neon заголовок с glow-эффектом
        ImGui.pushStyleColor(ImGuiCol.Text, 0.0f, 0.82f, 1.0f, 1.0f);  // Cyan neon #00d1ff
        ImGui.text("   GEOMETRIC DASH MEGA HACK   ");
        ImGui.text("       PORTED TO MINECRAFT 1.19.2       ");
        ImGui.popStyleColor();

        ImGui.separator();

        // Tab bar в стиле GD MH
        if (ImGui.beginTabBar("MainTabs")) {

            // Combat
            if (ImGui.beginTabItem("Combat")) {
                ImBoolean killaura = new ImBoolean(CheatManager.killAuraEnabled);
                ImGui.checkbox("KillAura", killaura);
                CheatManager.killAuraEnabled = killaura.get();

                ImFloat reach = new ImFloat(CheatManager.reachDistance);
                ImGui.sliderFloat("Reach Distance", reach, 3.0f, 8.0f, "%.1f blocks");
                CheatManager.reachDistance = reach.get();

                ImGui.endTabItem();
            }

            // Movement
            if (ImGui.beginTabItem("Movement")) {
                ImBoolean fly = new ImBoolean(CheatManager.flyEnabled);
                ImGui.checkbox("Fly", fly);
                CheatManager.flyEnabled = fly.get();

                ImFloat speed = new ImFloat(CheatManager.speedMultiplier);
                ImGui.sliderFloat("Speed Multiplier", speed, 1.0f, 15.0f, "%.1fx");
                CheatManager.speedMultiplier = speed.get();

                ImGui.endTabItem();
            }

            // Visuals
            if (ImGui.beginTabItem("Visuals")) {
                ImBoolean esp = new ImBoolean(CheatManager.espEnabled);
                ImGui.checkbox("ESP (Players & Mobs)", esp);
                CheatManager.espEnabled = esp.get();

                ImGui.checkbox("Fullbright", new ImBoolean(CheatManager.fullbrightEnabled));
                // CheatManager.fullbrightEnabled = ... (добавь в CheatManager если нужно)

                ImGui.endTabItem();
            }

            // Misc / Utils
            if (ImGui.beginTabItem("Misc")) {
                if (ImGui.button("Panic Button - Disable All")) {
                    CheatManager.disableAll();  // Вызываем метод из CheatManager
                }

                ImGui.separator();
                ImGui.textColored(0.0f, 0.82f, 1.0f, 1.0f, "Hotkeys:");
                ImGui.text("Insert - Toggle Menu");
                ImGui.text("Right Click - Test Lightning (if enabled)");

                ImGui.endTabItem();
            }

            ImGui.endTabBar();
        }

        // Футер
        ImGui.separator();
        ImGui.textDisabled("Built with ImGui-Java | Forge 1.19.2");

        ImGui.end();

        // Если юзер нажал крестик — закрываем экран
        if (!windowOpen.get()) {
            Minecraft.getInstance().setScreen(null);
        }
    }

    private void setupGDMegaHackStyle() {
        ImGuiStyle style = ImGui.getStyle();

        // Округления и padding как в GD MH
        style.setWindowRounding(10.0f);
        style.setFrameRounding(5.0f);
        style.setGrabRounding(5.0f);
        style.setScrollbarRounding(10.0f);
        style.setWindowPadding(16.0f, 16.0f);
        style.setItemSpacing(10.0f, 6.0f);

        // Основные цвета: dark space + cyan neon
        style.setColor(ImGuiCol.WindowBg, ImColor.rgbaToVec4(20, 20, 40, 240));       // #141428
        style.setColor(ImGuiCol.TitleBg, ImColor.rgbaToVec4(10, 10, 30, 240));        // #0a0a1e
        style.setColor(ImGuiCol.TitleBgActive, ImColor.rgbaToVec4(0, 100, 255, 220)); // Neon cyan active
        style.setColor(ImGuiCol.MenuBarBg, ImColor.rgbaToVec4(15, 15, 35, 240));

        style.setColor(ImGuiCol.FrameBg, ImColor.rgbaToVec4(30, 30, 60, 200));
        style.setColor(ImGuiCol.FrameBgHovered, ImColor.rgbaToVec4(0, 120, 255, 180));
        style.setColor(ImGuiCol.FrameBgActive, ImColor.rgbaToVec4(0, 140, 255, 220));

        style.setColor(ImGuiCol.Button, ImColor.rgbaToVec4(0, 80, 200, 180));
        style.setColor(ImGuiCol.ButtonHovered, ImColor.rgbaToVec4(0, 120, 255, 220));
        style.setColor(ImGuiCol.ButtonActive, ImColor.rgbaToVec4(0, 160, 255, 255));

        style.setColor(ImGuiCol.SliderGrab, ImColor.rgbaToVec4(0, 140, 255, 220));
        style.setColor(ImGuiCol.SliderGrabActive, ImColor.rgbaToVec4(0, 180, 255, 255));

        style.setColor(ImGuiCol.CheckMark, ImColor.rgbaToVec4(0, 160, 255, 255));
        style.setColor(ImGuiCol.Tab, ImColor.rgbaToVec4(30, 30, 60, 200));
        style.setColor(ImGuiCol.TabHovered, ImColor.rgbaToVec4(0, 120, 255, 220));
        style.setColor(ImGuiCol.TabActive, ImColor.rgbaToVec4(0, 100, 255, 255));

        style.setColor(ImGuiCol.ScrollbarBg, ImColor.rgbaToVec4(10, 10, 20, 180));
        style.setColor(ImGuiCol.ScrollbarGrab, ImColor.rgbaToVec4(0, 100, 255, 200));
        style.setColor(ImGuiCol.ScrollbarGrabHovered, ImColor.rgbaToVec4(0, 140, 255, 220));
    }

    // Input handlers (чтобы ImGui ловил клики/скролл)
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        ImGui.getIO().setMouseWheel((float) delta);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;  // Не паузит игру
    }
}

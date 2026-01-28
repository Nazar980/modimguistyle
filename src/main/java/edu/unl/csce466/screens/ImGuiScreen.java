package edu.unl.csce466.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import edu.unl.csce466.cheat.CheatManager;  // Импорт CheatManager (создай его, как я предлагал ранее)
import edu.unl.csce466.imgui.ImGuiRenderer;
import imgui.ImColor;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImVec4;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ImGuiScreen extends Screen {
    private static ImGuiScreen _INSTANCE = null;
    private final ImBoolean windowOpen = new ImBoolean(true);  // Для close button

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
        io.setMousePos((float) mc.mouseHandler.xpos(), (float) mc.mouseHandler.ypos());

        // Рендер ImGui контента
        ImGuiRenderer.getInstance().draw(this::renderMegaHackMenu);
    }

    private void renderMegaHackMenu() {
        // GD Mega Hack стиль: тёмный космос + neon cyan
        setupGDMegaHackStyle();

        // Главное окно: centered, draggable, no resize/collapse, с close button
        ImGui.setNextWindowPos(ImGui.getIO().getDisplaySizeX() * 0.5f, ImGui.getIO().getDisplaySizeY() * 0.5f, ImGuiCond.FirstUseEver, 0.5f, 0.5f);
        ImGui.setNextWindowSize(520, 420, ImGuiCond.FirstUseEver);

        ImGui.begin("GD Mega Hack v8 for Minecraft 1.19.2", windowOpen,
                ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.AlwaysAutoResize);

        // Neon header
        ImGui.pushStyleColor(ImGuiCol.Text, 0.0f, 0.87f, 1.0f, 1.0f);  // #00d4ff cyan
        ImGui.text("    GEOMETRIC DASH MEGA HACK PORTED TO MINECRAFT     ");
        ImGui.separator();
        ImGui.popStyleColor();

        // Tab Bar как в GD MH v7
        if (ImGui.beginTabBar("CheatTabs", ImGuiWindowFlags.None)) {
            // Combat Tab
            if (ImGui.beginTabItem("Combat")) {
                ImBoolean killAura = new ImBoolean(CheatManager.killAuraEnabled);
                ImGui.checkbox("KillAura", killAura);
                CheatManager.killAuraEnabled = killAura.get();

                ImFloat reach = new ImFloat(CheatManager.reachDistance);
                ImGui.sliderFloat("Reach", reach, 3.0f, 6.0f, "%.1f blocks");
                CheatManager.reachDistance = reach.get();

                ImGui.endTabItem();
            }

            // Movement Tab
            if (ImGui.beginTabItem("Movement")) {
                ImBoolean fly = new ImBoolean(CheatManager.flyEnabled);
                ImGui.checkbox("Fly", fly);
                CheatManager.flyEnabled = fly.get();

                ImFloat speed = new ImFloat(CheatManager.speedMultiplier);
                ImGui.sliderFloat("Speed Multiplier", speed, 1.0f, 10.0f, "%.1fx");
                CheatManager.speedMultiplier = speed.get();

                ImGui.endTabItem();
            }

            // Visuals Tab
            if (ImGui.beginTabItem("Visuals")) {
                ImBoolean esp = new ImBoolean(CheatManager.espEnabled);
                ImGui.checkbox("ESP (Players/Mobs)", esp);
                CheatManager.espEnabled = esp.get();

                // Добавь больше: Fullbright, XRay toggle и т.д.
                ImGui.endTabItem();
            }

            // Misc Tab
            if (ImGui.beginTabItem("Misc")) {
                if (ImGui.button("Panic (Disable All Cheats)")) {
                    CheatManager.flyEnabled = false;
                    CheatManager.killAuraEnabled = false;
                    CheatManager.espEnabled = false;
                    CheatManager.speedMultiplier = 2.0f;
                    CheatManager.reachDistance = 4.5f;
                }

                ImGui.separator();
                ImGui.text("Keybinds:");
                ImGui.textColored(0.0f, 0.87f, 1.0f, 1.0f, "Insert - Toggle Menu");

                ImGui.endTabItem();
            }

            ImGui.endTabBar();
        }

        // Footer info
        ImGui.separator();
        ImGui.text("Status: All cheats work in single/multiplayer (server-side hooks soon)");

        ImGui.end();

        // Close screen if window closed
        if (!windowOpen.get()) {
            Minecraft.getInstance().setScreen(null);
        }
    }

    // GD Mega Hack стиль (вызывается каждый кадр для consistency)
    private void setupGDMegaHackStyle() {
        ImGuiStyle style = ImGui.getStyle();
        style.setWindowRounding(8.0f);
        style.setFrameRounding(4.0f);
        style.setGrabRounding(4.0f);
        style.setScrollbarRounding(9.0f);
        style.setWindowPadding(15.0f, 15.0f);
        style.setItemSpacing(12.0f, 8.0f);

        // Цвета GD MH v7/v8: dark space + neon cyan gradients
        style.setWindowBg(ImColor.rgbaToVec4(26, 26, 46, 230));     // #1a1a2e
        style.setTitleBg(ImColor.rgbaToVec4(15, 15, 39, 230));      // #0f0f27
        style.setTitleBgActive(ImColor.rgbaToVec4(0, 74, 173, 200)); // Neon blue-cyan
        style.setMenuBarBg(ImColor.rgbaToVec4(18, 18, 46, 230));

        style.setScrollbarBg(ImColor.rgbaToVec4(8, 8, 16, 200));
        style.setScrollbarGrab(ImColor.rgbaToVec4(0, 96, 191, 180)); // Neon grab
        style.setScrollbarGrabHovered(ImColor.rgbaToVec4(0, 125, 255, 220));
        style.setScrollbarGrabActive(ImColor.rgbaToVec4(0, 150, 255, 255));

        style.setFrameBg(ImColor.rgbaToVec4(26, 26, 46, 200));
        style.setFrameBgHovered(ImColor.rgbaToVec4(0, 96, 191, 160));
        style.setFrameBgActive(ImColor.rgbaToVec4(0, 125, 255, 200));

        style.setSliderGrab(ImColor.rgbaToVec4(0, 112, 255, 220));
        style.setSliderGrabActive(ImColor.rgbaToVec4(0, 150, 255, 255));

        style.setButton(ImColor.rgbaToVec4(0, 74, 173, 150));
        style.setButtonHovered(ImColor.rgbaToVec4(0, 112, 255, 200));
        style.setButtonActive(ImColor.rgbaToVec4(0, 150, 255, 255));

        style.setHeader(ImColor.rgbaToVec4(0, 74, 173, 120));
        style.setHeaderHovered(ImColor.rgbaToVec4(0, 112, 255, 180));
        style.setHeaderActive(ImColor.rgbaToVec4(0, 150, 255, 240));

        style.setTab(ImColor.rgbaToVec4(26, 26, 46, 200));
        style.setTabHovered(ImColor.rgbaToVec4(0, 96, 191, 220));
        style.setTabActive(ImColor.rgbaToVec4(0, 74, 173, 255));

        style.setSeparator(ImColor.rgbaToVec4(0, 74, 173, 150));
        style.setSeparatorHovered(ImColor.rgbaToVec4(0, 112, 255, 200));
        style.setSeparatorActive(ImColor.rgbaToVec4(0, 150, 255, 255));
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        ImGui.getIO().setMouseWheel((float) delta);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;  // Не паузит игру
    }
}

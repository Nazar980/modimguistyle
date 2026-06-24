package edu.unl.csce466.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import edu.unl.csce466.cheat.DupeLogger;
import edu.unl.csce466.cheat.HitboxManager;
import edu.unl.csce466.imgui.ImGuiRenderer;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;

public class ImGuiScreen extends net.minecraft.client.gui.screen.Screen {

    private static ImGuiScreen INSTANCE = null;
    public static ImGuiScreen getInstance() {
        if (INSTANCE == null) INSTANCE = new ImGuiScreen();
        return INSTANCE;
    }

    private ImGuiScreen() {
        super(new StringTextComponent("ARZ Assistant"));
    }

    @Override public boolean isPauseScreen() { return false; }

    // Tabs
    private enum Tab { HITBOXES, DUPE }
    private Tab activeTab = Tab.HITBOXES;

    // ===== Hitbox state =====
    private final ImBoolean hbEnabled = new ImBoolean(HitboxManager.enabled);
    private final ImBoolean hbThroughWalls = new ImBoolean(HitboxManager.throughWalls);

    // ===== Dupe logger state =====
    private final ImBoolean dupeEnabled = new ImBoolean(DupeLogger.enabled);

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTick) {
        // sync GUI -> managers
        DupeLogger.enabled = dupeEnabled.get();
        HitboxManager.enabled = hbEnabled.get();
        HitboxManager.throughWalls = hbThroughWalls.get();

        ImGuiRenderer renderer = ImGuiRenderer.getInstance();
        renderer.draw(() -> {
            applyArzTheme();
            drawMainWindow();
        });
    }

    private void drawMainWindow() {
        ImGui.setNextWindowSize(860, 520, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowPos(ImGui.getIO().getDisplaySizeX() * 0.5f, ImGui.getIO().getDisplaySizeY() * 0.5f, ImGuiCond.FirstUseEver, 0.5f, 0.5f);

        int windowFlags = ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;
        String title = activeTab == Tab.HITBOXES ? "ARZ Assistant  |  Hitboxes" : "ARZ Assistant  |  Дубликация";
        if (ImGui.begin(title, windowFlags)) {

            // === Левая панель ===
            ImGui.beginChild("##sidebar", 190, 0, true);
            {
                textCentered("Меню");
                ImGui.separator();

                if (ImGui.selectable("Hitboxes", activeTab == Tab.HITBOXES)) activeTab = Tab.HITBOXES;
                if (ImGui.selectable("Дубликация", activeTab == Tab.DUPE)) activeTab = Tab.DUPE;

                float bottomY = ImGui.getWindowHeight() - 72;
                if (ImGui.getCursorPosY() < bottomY) ImGui.setCursorPosY(bottomY);

                ImGui.separator();
                if (ImGui.button("Сохранить", 172, 26)) {
                    HitboxManager.save();
                }
                if (ImGui.button("Сброс", 172, 26)) {
                    if (activeTab == Tab.HITBOXES) {
                        HitboxManager.reset();
                        hbEnabled.set(HitboxManager.enabled);
                        hbThroughWalls.set(HitboxManager.throughWalls);
                    } else {
                        DupeLogger.resetCounters();
                    }
                }
            }
            ImGui.endChild();

            ImGui.sameLine();

            // === Правая панель ===
            ImGui.beginChild("##content", 0, 0, true);
            {
                if (activeTab == Tab.HITBOXES) drawHitboxesTab();
                else drawDupeTab();
            }
            ImGui.endChild();
        }
        ImGui.end();
    }

    private void drawHitboxesTab() {
        textCentered("Hitboxes");
        ImGui.separator();
        ImGui.spacing();

        ImGui.columns(2, "hb_cols", false);
        ImGui.setColumnWidth(0, 330);

        // ---- Левая колонка: контролы ----
        {
            ImGui.checkbox("Включить Hitboxes", hbEnabled);
            ImGui.checkbox("Видно через стены", hbThroughWalls);

            ImGui.spacing(); ImGui.separator(); ImGui.spacing();

            ImGui.text("Ширина");
            ImGui.setNextItemWidth(-1);
            float[] w = { HitboxManager.width };
            if (ImGui.sliderFloat("##hb_width", w, 0.1f, 3.0f, "%.2f")) {
                HitboxManager.width = w[0];
            }
            ImGui.textDisabled(String.format("Текущая: %.2f  (ваниль 0.60)", HitboxManager.width));

            ImGui.spacing();
            ImGui.text("Высота");
            ImGui.setNextItemWidth(-1);
            float[] h = { HitboxManager.height };
            if (ImGui.sliderFloat("##hb_height", h, 0.1f, 3.5f, "%.2f")) {
                HitboxManager.height = h[0];
            }
            ImGui.textDisabled(String.format("Текущая: %.2f  (ваниль 1.80)", HitboxManager.height));

            ImGui.spacing(); ImGui.separator(); ImGui.spacing();

            ImGui.text("Цвет хитбокса");
            ImGui.setNextItemWidth(-1);
            ImGui.colorEdit4("##hb_color", HitboxManager.color);

            ImGui.spacing();
            if (ImGui.button("Сбросить размер", -1, 0)) {
                HitboxManager.width = 0.6f;
                HitboxManager.height = 1.8f;
            }

            ImGui.spacing(); ImGui.separator(); ImGui.spacing();
            ImGui.textWrapped("Хитбоксы применяются ко всем LivingEntity в мире. Работает для PvP / ботов.");
            ImGui.textDisabled("F3+B в игре тоже покажет хитбоксы.");

            ImGui.nextColumn();
        }

        // ---- Правая колонка: превью хитбокса ----
        {
            ImGui.text("Предпросмотр");
            ImGui.spacing();

            float previewH = 320f;
            float availX = ImGui.getContentRegionAvailX();
            if (availX < 50) availX = 50;

            ImGui.pushStyleColor(ImGuiCol.ChildBg, 0.10f, 0.10f, 0.10f, 0.55f);
            ImGui.beginChild("##preview_box", availX, previewH, true, ImGuiWindowFlags.NoScrollbar);
            {
                float px = ImGui.getWindowPosX();
                float py = ImGui.getWindowPosY();
                float pw = ImGui.getWindowWidth();
                float ph = ImGui.getWindowHeight();

                ImDrawList dl = ImGui.getWindowDrawList();
                dl.addRect(px, py, px + pw, py + ph, ImGui.getColorU32(0.42f, 0.42f, 0.42f, 1f), 6f);

                float boxW = 80f * (HitboxManager.width / 0.6f);
                float boxH = 220f * (HitboxManager.height / 1.8f);
                boxW = Math.max(20f, Math.min(boxW, pw - 20f));
                boxH = Math.max(30f, Math.min(boxH, ph - 60f));

                float bx0 = px + (pw - boxW) * 0.5f;
                float by1 = py + ph - 30f;
                float by0 = by1 - boxH;

                int col = ImGui.getColorU32(HitboxManager.color[0], HitboxManager.color[1], HitboxManager.color[2], HitboxManager.color[3]);
                dl.addRectFilled(bx0, by0, bx0 + boxW, by1, ImGui.getColorU32(0.15f, 0.15f, 0.15f, 0.6f), 2f);
                dl.addRect(bx0, by0, bx0 + boxW, by1, col, 2f, 0, 2.5f);

                String sizeTxt = String.format("%.2f x %.2f", HitboxManager.width, HitboxManager.height);
                float stw = ImGui.calcTextSize(sizeTxt).x;
                dl.addText(bx0 + (boxW - stw) * 0.5f, by0 - 18f, col, sizeTxt);

                String hint = "Hitbox preview";
                float htw = ImGui.calcTextSize(hint).x;
                dl.addText(px + (pw - htw) * 0.5f, py + 8f, ImGui.getColorU32(1f, 1f, 1f, 0.7f), hint);
            }
            ImGui.endChild();
            ImGui.popStyleColor();
            ImGui.textDisabled(String.format("W: %.2f  H: %.2f", HitboxManager.width, HitboxManager.height));
        }
        ImGui.columns(1);
    }

    private void drawDupeTab() {
        textCentered("Дубликация / Логгер магазина");
        ImGui.separator();
        ImGui.spacing();

        ImGui.checkbox("Логгирование", dupeEnabled);
        ImGui.sameLine();
        ImGui.textDisabled("(пишет в чат все изменения инвентаря)");

        ImGui.spacing(); ImGui.separator(); ImGui.spacing();

        ImGui.text("Статус:");
        ImGui.bulletText("Монеты: " + DupeLogger.lastCoinsStr + (DupeLogger.lastCoins >= 0 ? "  (" + DupeLogger.lastCoins + ")" : ""));
        ImGui.bulletText("Изумрудов в инвентаре: " + DupeLogger.lastEmeraldCount);

        ImGui.spacing();
        if (ImGui.button("Сбросить счётчики", 180, 0)) {
            DupeLogger.resetCounters();
        }

        ImGui.spacing(); ImGui.separator(); ImGui.spacing();

        ImGui.textWrapped("Логгер отслеживает:");
        ImGui.bulletText("Все изменения инвентаря ( +/- предметы )");
        ImGui.bulletText("Монеты из скорборда (ищет строку \"Монет: X\")");
        ImGui.bulletText("Задержку между получением изумруда и списанием монет");
        ImGui.spacing();
        ImGui.textColored(1f, 0.9f, 0.3f, 1f, "Если задержка 200-600мс -> DUP WINDOW!");
        ImGui.spacing();
        ImGui.textWrapped("Каждое событие пишется в чат с префиксом [Dupe]. Изумруды подсвечены зелёным.");
        ImGui.spacing();
        ImGui.textDisabled("Включи логгирование перед покупкой в магазине, затем смотри чат.");
    }

    // ===== Helpers =====
    private void textCentered(String text) {
        float windowWidth = ImGui.getWindowContentRegionMaxX() - ImGui.getWindowContentRegionMinX();
        float textWidth = ImGui.calcTextSize(text).x;
        ImGui.setCursorPosX(ImGui.getCursorPosX() + (windowWidth - textWidth) * 0.5f);
        ImGui.text(text);
    }

    // ===== ARZ THEME =====
    private void applyArzTheme() {
        ImGuiStyle style = ImGui.getStyle();
        style.setWindowPadding(8, 8);
        style.setWindowRounding(6.0f);
        style.setChildRounding(5.0f);
        style.setFramePadding(5, 3);
        style.setFrameRounding(3.0f);
        style.setItemSpacing(5, 4);
        style.setItemInnerSpacing(4, 4);
        style.setIndentSpacing(21);
        style.setScrollbarSize(10.0f);
        style.setScrollbarRounding(13f);
        style.setGrabMinSize(8f);
        style.setGrabRounding(1f);
        style.setWindowTitleAlign(0.5f, 0.5f);
        setCol(ImGuiCol.Text, 0.95f, 0.96f, 0.98f, 1.00f);
        setCol(ImGuiCol.TextDisabled, 0.29f, 0.29f, 0.29f, 1.00f);
        setCol(ImGuiCol.WindowBg, 0.14f, 0.14f, 0.14f, 1.00f);
        setCol(ImGuiCol.ChildBg, 0.12f, 0.12f, 0.12f, 1.00f);
        setCol(ImGuiCol.PopupBg, 0.08f, 0.08f, 0.08f, 0.94f);
        setCol(ImGuiCol.Border, 0.14f, 0.14f, 0.14f, 1.00f);
        setCol(ImGuiCol.BorderShadow, 1.00f, 1.00f, 1.00f, 0.10f);
        setCol(ImGuiCol.FrameBg, 0.22f, 0.22f, 0.22f, 1.00f);
        setCol(ImGuiCol.FrameBgHovered, 0.18f, 0.18f, 0.18f, 1.00f);
        setCol(ImGuiCol.FrameBgActive, 0.09f, 0.12f, 0.14f, 1.00f);
        setCol(ImGuiCol.TitleBg, 0.14f, 0.14f, 0.14f, 0.81f);
        setCol(ImGuiCol.TitleBgActive, 0.14f, 0.14f, 0.14f, 1.00f);
        setCol(ImGuiCol.TitleBgCollapsed, 0.00f, 0.00f, 0.00f, 0.51f);
        setCol(ImGuiCol.MenuBarBg, 0.20f, 0.20f, 0.20f, 1.00f);
        setCol(ImGuiCol.ScrollbarBg, 0.02f, 0.02f, 0.02f, 0.39f);
        setCol(ImGuiCol.ScrollbarGrab, 0.36f, 0.36f, 0.36f, 1.00f);
        setCol(ImGuiCol.ScrollbarGrabHovered, 0.18f, 0.22f, 0.25f, 1.00f);
        setCol(ImGuiCol.ScrollbarGrabActive, 0.24f, 0.24f, 0.24f, 1.00f);
        setCol(ImGuiCol.CheckMark, 1.00f, 0.28f, 0.28f, 1.00f);
        setCol(ImGuiCol.SliderGrab, 1.00f, 0.28f, 0.28f, 1.00f);
        setCol(ImGuiCol.SliderGrabActive, 1.00f, 0.28f, 0.28f, 1.00f);
        setCol(ImGuiCol.Button, 1.00f, 0.28f, 0.28f, 1.00f);
        setCol(ImGuiCol.ButtonHovered, 1.00f, 0.39f, 0.39f, 1.00f);
        setCol(ImGuiCol.ButtonActive, 1.00f, 0.21f, 0.21f, 1.00f);
        setCol(ImGuiCol.Header, 1.00f, 0.28f, 0.28f, 1.00f);
        setCol(ImGuiCol.HeaderHovered, 1.00f, 0.39f, 0.39f, 1.00f);
        setCol(ImGuiCol.HeaderActive, 1.00f, 0.21f, 0.21f, 1.00f);
        setCol(ImGuiCol.Separator, 0.42f, 0.42f, 0.42f, 1.00f);
        setCol(ImGuiCol.SeparatorHovered, 0.60f, 0.28f, 0.28f, 1.00f);
        setCol(ImGuiCol.SeparatorActive, 1.00f, 0.28f, 0.28f, 1.00f);
        setCol(ImGuiCol.ResizeGrip, 1.00f, 0.28f, 0.28f, 1.00f);
        setCol(ImGuiCol.ResizeGripHovered, 1.00f, 0.39f, 0.39f, 1.00f);
        setCol(ImGuiCol.ResizeGripActive, 1.00f, 0.19f, 0.19f, 1.00f);
        setCol(ImGuiCol.Tab, 0.22f, 0.22f, 0.22f, 1.0f);
        setCol(ImGuiCol.TabHovered, 1.00f, 0.28f, 0.28f, 1.0f);
        setCol(ImGuiCol.TabActive, 1.00f, 0.28f, 0.28f, 1.0f);
        setCol(ImGuiCol.PlotLines, 0.61f, 0.61f, 0.61f, 1.00f);
        setCol(ImGuiCol.PlotLinesHovered, 1.00f, 0.43f, 0.35f, 1.00f);
        setCol(ImGuiCol.PlotHistogram, 1.00f, 0.21f, 0.21f, 1.00f);
        setCol(ImGuiCol.PlotHistogramHovered, 1.00f, 0.18f, 0.18f, 1.00f);
        setCol(ImGuiCol.TextSelectedBg, 1.00f, 0.32f, 0.32f, 1.00f);
        setCol(ImGuiCol.ModalWindowDimBg, 0.26f, 0.26f, 0.26f, 0.60f);
        setCol(ImGuiCol.NavHighlight, 1.00f, 0.28f, 0.28f, 1.0f);
    }
    private static void setCol(int col, float r, float g, float b, float a) {
        ImGui.getStyle().setColor(col, r, g, b, a);
    }
}

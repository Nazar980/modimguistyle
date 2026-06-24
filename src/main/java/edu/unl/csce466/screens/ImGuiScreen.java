package edu.unl.csce466.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
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
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
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

    // ===== Hitbox state (зеркалим в HitboxManager) =====
    private final ImBoolean hbEnabled = new ImBoolean(HitboxManager.enabled);
    private final ImBoolean hbThroughWalls = new ImBoolean(HitboxManager.throughWalls);

    // Preview
    private float previewX = 0, previewY = 0;
    private float previewW = 280, previewH = 320;
    private float previewYaw = 25f;

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();

        // --- 1. Рендерим 3D превью игрока ПОД ImGui, чтобы оно просвечивало через прозрачный Child ---
        if (previewX != 0 && mc.player != null && mc.level != null) {
            float px = previewX + previewW * 0.5f;
            float py = previewY + previewH * 0.78f;
            int scale = 90;

            // Включаем рендер хитбоксов (F3+B эффект)
            mc.getEntityRenderDispatcher().setRenderHitBoxes(true);
            // InventoryScreen.renderEntityInInventory сам пушит матрицы
            InventoryScreen.renderEntityInInventory((int)px, (int)py, scale, -previewYaw, 0f, mc.player);
            mc.getEntityRenderDispatcher().setRenderHitBoxes(false);
        }

        // --- 2. ImGui кадр ---
        ImGuiRenderer renderer = ImGuiRenderer.getInstance();
        renderer.draw(() -> {
            applyArzTheme();
            drawMainWindow();
        });

        // Синхронизируем стейт в HitboxManager
        HitboxManager.enabled = hbEnabled.get();
        HitboxManager.throughWalls = hbThroughWalls.get();
    }

    private void drawMainWindow() {
        ImGui.setNextWindowSize(860, 520, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowPos(ImGui.getIO().getDisplaySizeX() * 0.5f, ImGui.getIO().getDisplaySizeY() * 0.5f, ImGuiCond.FirstUseEver, 0.5f, 0.5f);

        int windowFlags = ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;
        if (ImGui.begin("ARZ Assistant  |  Hitboxes", windowFlags)) {

            // === Левая панель ===
            ImGui.beginChild("##sidebar", 190, 0, true);
            {
                textCentered("Hitboxes");
                ImGui.separator();

                // Только один пункт
                ImGui.selectable("Hitboxes", true);

                // низ
                float bottomY = ImGui.getWindowHeight() - 72;
                if (ImGui.getCursorPosY() < bottomY) ImGui.setCursorPosY(bottomY);

                ImGui.separator();
                if (ImGui.button("Сохранить", 172, 26)) {
                    HitboxManager.save();
                }
                if (ImGui.button("Сброс", 172, 26)) {
                    HitboxManager.reset();
                    hbEnabled.set(HitboxManager.enabled);
                    hbThroughWalls.set(HitboxManager.throughWalls);
                }
            }
            ImGui.endChild();

            ImGui.sameLine();

            // === Правая панель ===
            ImGui.beginChild("##content", 0, 0, true);
            {
                drawHitboxesTab();
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

            // ---- Правая колонка: 3D превью ----
        {
            ImGui.text("Предпросмотр");
            ImGui.spacing();

            // Делаем Child с прозрачным фоном, чтобы было видно Minecraft entity рендер под ImGui
            ImGui.pushStyleColor(ImGuiCol.ChildBg, 0, 0, 0, 0);
            ImGui.beginChild("##preview_box", previewW, previewH, true, ImGuiWindowFlags.NoScrollbar);
            {
                // Невидимая зона для захвата мыши
                ImGui.invisibleButton("##preview_drag", previewW - 16, previewH - 16);
                boolean hovered = ImGui.isItemHovered();
                if (hovered && ImGui.isMouseDown(0)) {
                    previewYaw += ImGui.getIO().getMouseDeltaX() * 0.6f;
                }

                // Запоминаем экранные координаты этого Child для рендера entity в Screen.render()
                // imgui-java 1.86: getItemRectMin/Max без аргументов нет, используем MinX/Y / Size
                previewX = ImGui.getItemRectMinX();
                previewY = ImGui.getItemRectMinY();
                previewW = ImGui.getItemRectSizeX();
                previewH = ImGui.getItemRectSizeY();

                // Подложка / сетка
                ImDrawList dl = ImGui.getWindowDrawList();
                float x0 = previewX, y0 = previewY, x1 = x0 + previewW, y1 = y0 + previewH;
                dl.addRectFilled(x0, y0, x1, y1, ImGui.getColorU32(0.10f, 0.10f, 0.10f, 0.55f), 6f);
                dl.addRect(x0, y0, x1, y1, ImGui.getColorU32(0.42f, 0.42f, 0.42f, 1f), 6f);

                // Подпись
                String hint = hovered ? "Тяни мышью для поворота" : "3D превью игрока";
                float tw = ImGui.calcTextSize(hint).x;
                dl.addText(x0 + (previewW - tw) * 0.5f, y0 + 8, ImGui.getColorU32(1f,1f,1f,0.7f), hint);

                // Визуальный 2D бокс хитбокса поверх (в дополнение к реальному 3D хитбоксу от MC)
                float boxW = 70f * (HitboxManager.width / 0.6f);
                float boxH = 190f * (HitboxManager.height / 1.8f);
                boxW = Math.max(20, Math.min(boxW, previewW - 20));
                boxH = Math.max(30, Math.min(boxH, previewH - 60));
                float bx0 = x0 + (previewW - boxW) * 0.5f;
                float by1 = y0 + previewH - 28;
                float by0 = by1 - boxH;
                int col = ImGui.getColorU32(HitboxManager.color[0], HitboxManager.color[1], HitboxManager.color[2], HitboxManager.color[3]);
                dl.addRect(bx0, by0, bx0 + boxW, by1, col, 0f, 0, 2f);

                String sizeTxt = String.format("%.2f x %.2f", HitboxManager.width, HitboxManager.height);
                float stw = ImGui.calcTextSize(sizeTxt).x;
                dl.addText(bx0 + (boxW - stw) * 0.5f, by0 - 18, col, sizeTxt);
            }
            ImGui.endChild();
            ImGui.popStyleColor();

            ImGui.textDisabled("Крути модель ЛКМ");
        }

        ImGui.columns(1);
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

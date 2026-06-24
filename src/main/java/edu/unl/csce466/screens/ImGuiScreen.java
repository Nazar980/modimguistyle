package edu.unl.csce466.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import edu.unl.csce466.cheat.CheatManager;
import edu.unl.csce466.imgui.ImGuiRenderer;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
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

    // ===== State =====
    private enum Tab {
        SETTINGS("Настройки"),
        INFO("Информация"),
        NOTIFY("Уведомления"),
        AUTOFILL("Автозаполнение"),
        AUTOEAT("Автохавка"),
        REPO("Репозиторий"),
        // Функции
        FUN_PLAYER("Игрок"),
        FUN_VISUAL("Визуалы"),
        FUN_COMBAT("Бой"),
        FUN_MISC("Разное");

        final String label;
        Tab(String label){ this.label = label; }
    }
    private Tab activeTab = Tab.SETTINGS;

    // Настройки - колонка 1
    private final ImBoolean antiBlockedPlayer = new ImBoolean(CheatManager.flyEnabled);
    private final ImBoolean shieldControl = new ImBoolean(false);
    private final ImBoolean bunnyhop = new ImBoolean(false);
    private final ImBoolean fastRun = new ImBoolean(false);
    private final ImBoolean antiDebuff = new ImBoolean(false);
    private final ImBoolean antiStun = new ImBoolean(false);
    private final ImBoolean antiCrash = new ImBoolean(false);

    // колонка 2
    private final ImBoolean fastConnect = new ImBoolean(false);
    private final ImBoolean sbivAnim = new ImBoolean(false);
    private final ImBoolean antiAFK = new ImBoolean(false);
    private final ImBoolean adminSpec = new ImBoolean(false);

    // колонка 3
    private final ImBoolean phone = new ImBoolean(false);
    private final ImBoolean mask = new ImBoolean(false);
    private final ImBoolean armor = new ImBoolean(false);
    private final ImBoolean fisheye = new ImBoolean(false);

    // Специальные виджеты
    private final ImFloat radius = new ImFloat(12.0f);
    private final ImString vipChat1 = new ImString(128);
    private final ImString vipChat2 = new ImString(128);
    private final ImString vipChat3 = new ImString(128);

    // Сайдбар низ
    private final String[] themes = { "Тёмная", "Светлая", "ARZ Red", "Aqua" };
    private final String[] styles = { "Компактный", "Стандарт", "Большой" };
    private final ImInt themeIdx = new ImInt(2);
    private final ImInt styleIdx = new ImInt(1);

    // ===== Render =====
    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTick) {
        ImGuiRenderer renderer = ImGuiRenderer.getInstance();
        renderer.draw(() -> {
            applyArzTheme();
            drawMainWindow();
        });
    }

    private void drawMainWindow() {
        ImGui.setNextWindowSize(920, 540, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowPos(ImGui.getIO().getDisplaySizeX() * 0.5f, ImGui.getIO().getDisplaySizeY() * 0.5f, ImGuiCond.FirstUseEver, 0.5f, 0.5f);

        int windowFlags = ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;
        if (ImGui.begin("ARZ Assistant  |  by Root3287", windowFlags)) {

            // === Левая панель - Сайдбар ===
            ImGui.beginChild("##sidebar", 190, 0, true);
            {
                textCentered("Меню");
                ImGui.separator();

                if (selectableTab(Tab.SETTINGS)) activeTab = Tab.SETTINGS;
                if (selectableTab(Tab.INFO)) activeTab = Tab.INFO;
                if (selectableTab(Tab.NOTIFY)) activeTab = Tab.NOTIFY;
                if (selectableTab(Tab.AUTOFILL)) activeTab = Tab.AUTOFILL;
                if (selectableTab(Tab.AUTOEAT)) activeTab = Tab.AUTOEAT;
                if (selectableTab(Tab.REPO)) activeTab = Tab.REPO;

                ImGui.spacing(); ImGui.separator(); ImGui.spacing();
                textCenteredDisabled("Функции");
                if (selectableTab(Tab.FUN_PLAYER)) activeTab = Tab.FUN_PLAYER;
                if (selectableTab(Tab.FUN_VISUAL)) activeTab = Tab.FUN_VISUAL;
                if (selectableTab(Tab.FUN_COMBAT)) activeTab = Tab.FUN_COMBAT;
                if (selectableTab(Tab.FUN_MISC)) activeTab = Tab.FUN_MISC;

                // низ сайдбара - прижать к низу
                float bottomY = ImGui.getWindowHeight() - 110;
                if (ImGui.getCursorPosY() < bottomY) ImGui.setCursorPosY(bottomY);

                ImGui.separator();
                textCentered("Настройки меню");
                ImGui.text("Тема");
                ImGui.setNextItemWidth(-1);
                if (ImGui.combo("##theme", themeIdx, themes)) {
                    // переключение темы
                }
                ImGui.text("Стиль");
                ImGui.setNextItemWidth(-1);
                ImGui.combo("##style", styleIdx, styles);

                ImGui.spacing();
                if (ImGui.button("Сохранить", 85, 0)) {
                    // TODO: save config
                }
                ImGui.sameLine();
                if (ImGui.button("Сброс", 85, 0)) {
                    resetSettings();
                }
            }
            ImGui.endChild();

            ImGui.sameLine();

            // === Правая панель - Контент ===
            ImGui.beginChild("##content", 0, 0, true);
            {
                switch (activeTab) {
                    case SETTINGS: drawSettingsTab(); break;
                    case INFO: drawInfoTab(); break;
                    case NOTIFY: drawPlaceholder("Уведомления", "Здесь будут настройки уведомлений / тг-бота и т.д."); break;
                    case AUTOFILL: drawPlaceholder("Автозаполнение", "Шаблоны для ответов, бинды и т.п."); break;
                    case AUTOEAT: drawPlaceholder("Автохавка", "Настройки авто-хила / еды"); break;
                    case REPO: drawPlaceholder("Репозиторий", "https://github.com/...\nАвтообновление"); break;
                    case FUN_PLAYER: drawPlaceholder("Игрок", "Fly, Speed, NoClip и т.д."); break;
                    case FUN_VISUAL: drawPlaceholder("Визуалы", "ESP, Fullbright, Fisheye"); break;
                    case FUN_COMBAT: drawPlaceholder("Бой", "KillAura, Reach"); break;
                    case FUN_MISC: drawPlaceholder("Разное", "Прочие читы"); break;
                }
            }
            ImGui.endChild();
        }
        ImGui.end();
    }

    private boolean selectableTab(Tab tab) {
        boolean selected = activeTab == tab;
        return ImGui.selectable(tab.label, selected);
    }

    private void drawSettingsTab() {
        textCentered("Основные");
        ImGui.separator();

        // 3 колонки
        ImGui.columns(3, "settings_cols", false);
        // Колонка 1
        {
            checkboxRed("AntiBlockedPlayer", antiBlockedPlayer, "Блокирует отправку в деморган?");
            ImGui.checkbox("ShieldControl", shieldControl);
            checkboxWithHelp("Bunnyhop", bunnyhop, "Автоматический распрыг");
            ImGui.checkbox("FastRun", fastRun);
            ImGui.checkbox("AntiDebuff", antiDebuff);
            ImGui.checkbox("AntiStun", antiStun);
            ImGui.checkbox("AntiCrash", antiCrash);
            ImGui.nextColumn();
        }
        // Колонка 2
        {
            if (ImGui.button("Пропуск ответа...", -1, 0)) {}
            ImGui.checkbox("Fastconnect", fastConnect);
            checkboxWithHelp("Сбив /anim", sbivAnim, "Сбивает анимацию");
            ImGui.checkbox("AntiAFK", antiAFK);
            ImGui.checkbox("AdminSpec", adminSpec);

            ImGui.spacing();
            // Красная кнопка "Прослушать"
            ImGui.pushStyleColor(ImGuiCol.Button, 1.0f, 0.28f, 0.28f, 1.0f);
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 1.0f, 0.39f, 0.39f, 1.0f);
            ImGui.pushStyleColor(ImGuiCol.ButtonActive, 1.0f, 0.21f, 0.21f, 1.0f);
            if (ImGui.button("Прослушать", -1, 0)) {}
            ImGui.popStyleColor(3);

            ImGui.nextColumn();
        }
        // Колонка 3
        {
            ImGui.checkbox("Телефон", phone);
            ImGui.checkbox("Маска", mask);
            ImGui.checkbox("Бронижилет", armor);
            ImGui.checkbox("Fisheye", fisheye);
            helpMarker("Рыбий глаз - широкий FOV");
            ImGui.nextColumn();
        }
        ImGui.columns(1);

        ImGui.spacing(); ImGui.separator(); ImGui.spacing();

        // Радиус слайдер
        textCentered("Радиус действия");
        ImGui.spacing();
        ImGui.setNextItemWidth(-80);
        ImGui.sliderFloat("##radius", radius.getData(), 1.0f, 50.0f, "%.0f");
        ImGui.sameLine();
        ImGui.textDisabled(String.format("%.0f м", radius.get()));
        ImGui.sameLine();
        helpMarker("Радиус действия функций");

        ImGui.spacing(); ImGui.separator(); ImGui.spacing();

        // Вил Чат
        textCentered("Вил Чат");
        ImGui.spacing();
        ImGui.setNextItemWidth(-1);
        ImGui.inputText("##vip1", vipChat1);
        ImGui.setNextItemWidth(-1);
        ImGui.inputText("##vip2", vipChat2);
        ImGui.setNextItemWidth(-1);
        ImGui.inputText("##vip3", vipChat3);

        ImGui.spacing();
        if (ImGui.button("Отправить в VIP чат", 180, 0)) {
            sendVipChat();
        }
    }

    private void drawInfoTab() {
        textCentered("Информация");
        ImGui.separator();
        ImGui.textWrapped("ARZ Assistant - ImGui чит-меню для Minecraft 1.16.5 Forge");
        ImGui.bulletText("Открытие меню: L");
        ImGui.bulletText("ImGui-java: 1.86.10");
        ImGui.bulletText("Рендер: LWJGL3 + GL3");
        ImGui.spacing();
        ImGui.textDisabled("by Root3287 / edu.unl.csce466");
    }

    private void drawPlaceholder(String title, String desc) {
        textCentered(title);
        ImGui.separator();
        ImGui.textDisabled(desc);
    }

    // ===== Helpers =====

    private void textCentered(String text) {
        float windowWidth = ImGui.getWindowContentRegionMaxX() - ImGui.getWindowContentRegionMinX();
        float textWidth = ImGui.calcTextSize(text).x;
        ImGui.setCursorPosX(ImGui.getCursorPosX() + (windowWidth - textWidth) * 0.5f);
        ImGui.text(text);
    }

    private void textCenteredDisabled(String text) {
        float windowWidth = ImGui.getWindowContentRegionMaxX() - ImGui.getWindowContentRegionMinX();
        float textWidth = ImGui.calcTextSize(text).x;
        ImGui.setCursorPosX(ImGui.getCursorPosX() + (windowWidth - textWidth) * 0.5f);
        ImGui.textDisabled(text);
    }

    private void checkboxRed(String label, ImBoolean v, String tooltip) {
        ImGui.checkbox(label, v);
        if (tooltip != null && ImGui.isItemHovered()) {
            ImGui.setTooltip(tooltip);
        }
        // подпись красным справа, как в ARZ
        ImGui.sameLine();
        ImGui.textColored(1.0f, 0.28f, 0.28f, 1.0f, "★");
        if (tooltip != null && ImGui.isItemHovered()) {
            ImGui.setTooltip(tooltip);
        }
    }

    private void checkboxWithHelp(String label, ImBoolean v, String help) {
        ImGui.checkbox(label, v);
        ImGui.sameLine();
        helpMarker(help);
    }

    private void helpMarker(String desc) {
        ImGui.textDisabled("(?)");
        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.pushTextWrapPos(ImGui.getFontSize() * 35.0f);
            ImGui.textUnformatted(desc);
            ImGui.popTextWrapPos();
            ImGui.endTooltip();
        }
    }

    private void sendVipChat() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        // Пример отправки, замени под свой сервер
        // mc.player.sendMessage(new StringTextComponent("/vip " + vipChat1.get()), Util.NIL_UUID);
    }

    private void resetSettings() {
        antiBlockedPlayer.set(false);
        shieldControl.set(false);
        bunnyhop.set(false);
        fastRun.set(false);
        antiDebuff.set(false);
        antiStun.set(false);
        antiCrash.set(false);
        fastConnect.set(false);
        sbivAnim.set(false);
        antiAFK.set(false);
        adminSpec.set(false);
        phone.set(false);
        mask.set(false);
        armor.set(false);
        fisheye.set(false);
        radius.set(12.0f);
    }

    // ===== ARZ THEME - порт Lua -> Java =====
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

        // colors
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

        // Разделители - СВЕТЛЕЕ, чтобы было видно
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
        // доп
        setCol(ImGuiCol.NavHighlight, 1.00f, 0.28f, 0.28f, 1.0f);
    }

    private static void setCol(int col, float r, float g, float b, float a) {
        ImGui.getStyle().setColor(col, r, g, b, a);
    }
}

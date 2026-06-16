package edu.unl.csce466.screens;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import edu.unl.csce466.imgui.ImGuiRenderer;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiSelectableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class ImGuiScreen extends Screen {
    private static ImGuiScreen INSTANCE = null;

    private static final String POPUP_BAN_RULES = "SelectBanRulePopup";
    private static final String POPUP_BAN_PLAYERS = "SelectBanPlayerPopup";
    private static final String POPUP_MUTE_RULES = "SelectMuteRulePopup";
    private static final String POPUP_MUTE_PLAYERS = "SelectMutePlayerPopup";
    private static final String POPUP_CHECK_PLAYERS = "SelectCheckPlayerPopup";

    private final float[] colWindowBg = {30 / 255f, 30 / 255f, 35 / 255f, 220 / 255f};
    private final float[] colTitleBg = {245 / 255f, 70 / 255f, 130 / 255f, 220 / 255f};
    private final float[] colTitleBgActive = {245 / 255f, 70 / 255f, 130 / 255f, 220 / 255f};
    private final float[] colTitleBgCollapsed = {245 / 255f, 70 / 255f, 130 / 255f, 160 / 255f};
    private final float[] colText = {1f, 1f, 1f, 1f};
    private final float[] colTextDisabled = {180 / 255f, 180 / 255f, 190 / 255f, 140 / 255f};
    private final float[] colTab = {42 / 255f, 42 / 255f, 42 / 255f, 180 / 255f};
    private final float[] colTabHovered = {60 / 255f, 60 / 255f, 65 / 255f, 200 / 255f};
    private final float[] colTabActive = {50 / 255f, 50 / 255f, 55 / 255f, 220 / 255f};
    private final float[] colButton = {70 / 255f, 70 / 255f, 80 / 255f, 220 / 255f};
    private final float[] colButtonHovered = {90 / 255f, 90 / 255f, 100 / 255f, 230 / 255f};
    private final float[] colButtonActive = {110 / 255f, 110 / 255f, 120 / 255f, 240 / 255f};
    private final float[] colCheckMark = {245 / 255f, 70 / 255f, 130 / 255f, 1f};
    private final float[] colFrameBg = {50 / 255f, 50 / 255f, 55 / 255f, 220 / 255f};
    private final float[] colFrameBgHovered = {70 / 255f, 70 / 255f, 80 / 255f, 230 / 255f};
    private final float[] colFrameBgActive = {90 / 255f, 90 / 255f, 100 / 255f, 240 / 255f};
    private final float[] colHeader = {90 / 255f, 40 / 255f, 60 / 255f, 220 / 255f};
    private final float[] colHeaderHovered = {130 / 255f, 55 / 255f, 85 / 255f, 230 / 255f};
    private final float[] colHeaderActive = {245 / 255f, 70 / 255f, 130 / 255f, 220 / 255f};

    private static class Rule {
        final String id;
        final String desc;
        final String duration;
        final boolean permanent;

        Rule(String id, String desc, String duration, boolean permanent) {
            this.id = id;
            this.desc = desc;
            this.duration = duration;
            this.permanent = permanent;
        }

        String reason() {
            return id + " [" + desc + "]";
        }
    }

    private static class ActionState {
        Rule selectedRule = null;
        String selectedPlayerName = "";
        final ImString offlineNameBuf = new ImString("", 32);
        boolean useOffline = false;
        String statusMessage = "";
        float statusTimer = 0f;
    }

    private static class CheckState {
        String selectedPlayerName = "";
        String statusMessage = "";
        float statusTimer = 0f;
    }

    private static final List<Rule> BAN_RULES = new ArrayList<Rule>();
    private static final List<Rule> MUTE_RULES = new ArrayList<Rule>();

    static {
        BAN_RULES.add(new Rule("1.1.2", "Пиар проектов (серверов, чатов, читов и т.д.)", "12d", false));
        BAN_RULES.add(new Rule("1.5", "Использование читов", "14d", false));
        BAN_RULES.add(new Rule("1.5.1", "Тим с читером", "8d", false));
        BAN_RULES.add(new Rule("1.5.2", "Клан читеров (каждый)", "14d", false));
        BAN_RULES.add(new Rule("1.6", "Признание в использовании читов", "12d", false));
        BAN_RULES.add(new Rule("1.7", "Ник похож на ник администрации / ютуберов", "", true));
        BAN_RULES.add(new Rule("1.8", "Использование DDoS-пакетов", "28d", false));
        BAN_RULES.add(new Rule("1.8.1", "Попытка краша сервера", "", true));
        BAN_RULES.add(new Rule("1.9", "Отказ от проверки", "14d", false));
        BAN_RULES.add(new Rule("2.0", "Задерживание модератора во время проверки", "14d", false));
        BAN_RULES.add(new Rule("2.1", "Выдача себя за модерацию проекта", "20d", false));
        BAN_RULES.add(new Rule("2.5", "Больше 5 аккаунтов в бане (каждый новый аккаунт)", "14d", false));
        BAN_RULES.add(new Rule("2.6", "Обход бана", "", true));
        BAN_RULES.add(new Rule("2.7", "Покупка доната через сторонние маркетплейсы", "", true));

        MUTE_RULES.add(new Rule("1.1", "Спам (флуд)", "30m", false));
        MUTE_RULES.add(new Rule("1.1.3", "КАПС", "30m", false));
        MUTE_RULES.add(new Rule("1.2", "Массивное оскорбление", "1h", false));
        MUTE_RULES.add(new Rule("1.3", "Организация флуда в чате с помощью опроса", "4h", false));
        MUTE_RULES.add(new Rule("1.4", "Упоминание родителей", "12h", false));
        MUTE_RULES.add(new Rule("1.4.1", "Оскорбление проекта и модераторов сервера", "6h", false));
    }

    private final ActionState banState = new ActionState();
    private final ActionState muteState = new ActionState();
    private final CheckState checkState = new CheckState();

    private enum PlayerPopupContext {
        BAN,
        MUTE,
        CHECK
    }

    private PlayerPopupContext playerPopupContext = PlayerPopupContext.BAN;
    private final ImBoolean showStyleEditor = new ImBoolean(false);
    private final ImBoolean showDemo = new ImBoolean(false);

    public static ImGuiScreen getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ImGuiScreen();
        }
        return INSTANCE;
    }

    private ImGuiScreen() {
        super(new StringTextComponent("Помощник модерации"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTick) {
        ImGuiRenderer renderer = ImGuiRenderer.getInstance();
        Minecraft mc = Minecraft.getInstance();
        ImGuiIO io = ImGui.getIO();

        tickStatuses(io.getDeltaTime());

        renderer.draw(() -> {
            applyCurrentColors();

            ImGui.setNextWindowSize(760, 650, ImGuiCond.FirstUseEver);
            ImGui.setNextWindowPos(
                (io.getDisplaySizeX() - 760) * 0.5f,
                (io.getDisplaySizeY() - 650) * 0.5f,
                ImGuiCond.FirstUseEver
            );

            ImGui.begin("Помощник модерации", ImGuiWindowFlags.None);

            if (ImGui.beginMainMenuBar()) {
                if (ImGui.beginMenu("Инструменты")) {
                    if (ImGui.menuItem("Редактор цветов", "", showStyleEditor.get())) {
                        showStyleEditor.set(!showStyleEditor.get());
                    }
                    if (ImGui.menuItem("Демо-окно ImGui", "", showDemo.get())) {
                        showDemo.set(!showDemo.get());
                    }
                    ImGui.endMenu();
                }
                if (ImGui.beginMenu("Помощь")) {
                    ImGui.text("Помощник модерации для Minecraft 1.16.5");
                    ImGui.textWrapped("Выбери правило, потом игрока, потом нажми кнопку действия.");
                    ImGui.endMenu();
                }
                ImGui.endMainMenuBar();
            }

            ImGui.spacing();
            ImGui.textColored(245 / 255f, 70 / 255f, 130 / 255f, 1f, "Помощник модерации");
            ImGui.separator();

            drawBanAssistant(mc);
            drawMuteAssistant(mc);
            drawCheckAssistant(mc);

            if (showStyleEditor.get()) {
                drawStyleEditor();
            }
            if (showDemo.get()) {
                ImGui.showDemoWindow(showDemo);
            }

            ImGui.end();
        });
    }

    private void drawBanAssistant(Minecraft mc) {
        ImGui.spacing();
        ImGui.text("Помощник банов");
        ImGui.separator();

        ImGui.text("1) Правило:");
        renderSelectedRule(banState);
        if (ImGui.button("Выбрать правило...")) {
            ImGui.openPopup(POPUP_BAN_RULES);
        }
        drawBanRulesPopup();

        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();

        ImGui.text("2) Игрок:");
        drawPlayerModeControls(banState, PlayerPopupContext.BAN, POPUP_BAN_PLAYERS);
        drawBanPlayersPopup();

        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();

        ImGui.text("3) Бан:");
        drawRuleActionButton(mc, banState, "Забанить игрока", true);
        renderStatus(banState);

        ImGui.spacing();
        ImGui.separator();
    }

    private void drawMuteAssistant(Minecraft mc) {
        ImGui.spacing();
        ImGui.text("Помощник мутов");
        ImGui.separator();

        ImGui.text("1) Правило:");
        renderSelectedRule(muteState);
        if (ImGui.button("Выбрать правило...")) {
            ImGui.openPopup(POPUP_MUTE_RULES);
        }
        drawMuteRulesPopup();

        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();

        ImGui.text("2) Игрок:");
        drawPlayerModeControls(muteState, PlayerPopupContext.MUTE, POPUP_MUTE_PLAYERS);
        drawMutePlayersPopup();

        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();

        ImGui.text("3) Мут:");
        drawRuleActionButton(mc, muteState, "Выдать мут", false);
        renderStatus(muteState);

        ImGui.spacing();
        ImGui.separator();
    }

    private void drawCheckAssistant(Minecraft mc) {
        ImGui.spacing();
        ImGui.text("Проверка игроков");
        ImGui.separator();

        ImGui.text("Игрок для проверки:");
        if (checkState.selectedPlayerName.isEmpty()) {
            ImGui.textDisabled("  (игрок не выбран)");
        } else {
            ImGui.bulletText("Выбран: " + checkState.selectedPlayerName);
        }
        if (ImGui.button("Выбрать игрока...")) {
            openPlayerPopup(PlayerPopupContext.CHECK);
            ImGui.openPopup(POPUP_CHECK_PLAYERS);
        }
        drawCheckPlayersPopup();

        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();

        boolean canStart = mc.player != null && !checkState.selectedPlayerName.isEmpty();
        if (!canStart) {
            pushDisabledButtonStyle();
        }
        if (ImGui.button("Начать", -1, 36)) {
            if (canStart) {
                sendCommandAndStatus(
                    "/asuxcheat start \"" + checkState.selectedPlayerName + "\"",
                    checkState,
                    "Команда отправлена: /asuxcheat start \"" + checkState.selectedPlayerName + "\""
                );
            }
        }
        if (!canStart) {
            popDisabledButtonStyle();
            if (ImGui.isItemHovered()) {
                ImGui.setTooltip("Сначала выбери игрока для проверки.");
            }
        }

        if (ImGui.button("Остановить (отпустить)", -1, 36)) {
            sendCommandAndStatus("/asuxcheat stop", checkState, "Команда отправлена: /asuxcheat stop");
        }

        if (ImGui.button("Добавить время (2 минуты)", -1, 36)) {
            sendCommandAndStatus("/asuxcheat addtime 120", checkState, "Команда отправлена: /asuxcheat addtime 120");
        }

        if (ImGui.button("Заморозить проверку", -1, 36)) {
            sendCommandAndStatus("/asuxcheat freeze", checkState, "Команда отправлена: /asuxcheat freeze");
        }

        renderStatus(checkState);
    }

    private void drawPlayerModeControls(ActionState state, PlayerPopupContext context, String popupName) {
        if (ImGui.radioButton("Онлайн-игрок (из таб-листа)", !state.useOffline)) {
            state.useOffline = false;
        }
        if (ImGui.radioButton("Оффлайн / ник вручную", state.useOffline)) {
            state.useOffline = true;
        }

        ImGui.spacing();
        if (!state.useOffline) {
            if (state.selectedPlayerName.isEmpty()) {
                ImGui.textDisabled("  (игрок не выбран)");
            } else {
                ImGui.bulletText("Выбран: " + state.selectedPlayerName);
            }
            if (ImGui.button("Выбрать игрока...")) {
                openPlayerPopup(context);
                ImGui.openPopup(popupName);
            }
        } else {
            ImGui.text("Ник:");
            ImGui.setNextItemWidth(-1);
            ImGui.inputText("##" + popupName + "_offline", state.offlineNameBuf);
        }
    }

    private void renderSelectedRule(ActionState state) {
        if (state.selectedRule == null) {
            ImGui.textDisabled("  (правило не выбрано)");
            return;
        }

        ImGui.bulletText(state.selectedRule.id + " — " + state.selectedRule.desc);
        ImGui.bulletText("Длительность: " + (state.selectedRule.permanent ? "постоянно" : state.selectedRule.duration));
    }

    private void drawRuleActionButton(Minecraft mc, ActionState state, String buttonText, boolean isBan) {
        String targetName = state.useOffline ? state.offlineNameBuf.get().trim() : state.selectedPlayerName;
        boolean canRun = mc.player != null && state.selectedRule != null && !targetName.isEmpty();

        if (!canRun) {
            pushDisabledButtonStyle();
        }

        if (ImGui.button(buttonText, -1, 36)) {
            if (canRun) {
                String command = buildRuleCommand(state.selectedRule, targetName, isBan);
                sendChatCommand(command);
                setStatus(state, "Команда отправлена: " + command);
            }
        }

        if (!canRun) {
            popDisabledButtonStyle();
            if (ImGui.isItemHovered()) {
                ImGui.setTooltip("Сначала выбери правило и игрока.");
            }
        }

        if (canRun) {
            ImGui.spacing();
            ImGui.textWrapped("Команда для выполнения:");
            ImGui.textColored(0.4f, 0.8f, 1f, 1f, buildRuleCommand(state.selectedRule, targetName, isBan));
        }
    }

    private String buildRuleCommand(Rule rule, String targetName, boolean isBan) {
        if (rule == null) {
            return "";
        }

        if (isBan) {
            if (rule.permanent) {
                return "/ban " + targetName + " " + rule.reason();
            }
            return "/tempban " + targetName + " " + rule.duration + " " + rule.reason();
        }

        if (rule.permanent) {
            return "/mute " + targetName + " " + rule.reason();
        }
        return "/tempmute " + targetName + " " + rule.duration + " " + rule.reason();
    }

    private void sendCommandAndStatus(String command, CheckState state, String message) {
        sendChatCommand(command);
        setStatus(state, message);
    }

    private void sendChatCommand(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        mc.player.connection.sendPacket(new CChatMessagePacket(command));
    }

    private void openPlayerPopup(PlayerPopupContext context) {
        playerPopupContext = context;
    }

    private void drawBanRulesPopup() {
        ImGui.setNextWindowSize(520, 480, ImGuiCond.Appearing);
        if (ImGui.beginPopupModal(POPUP_BAN_RULES)) {
            ImGui.text("Выберите правило для бана:");
            ImGui.separator();

            if (ImGui.collapsingHeader("Основные правила", ImGuiSelectableFlags.DontClosePopups)) {
                for (Rule rule : BAN_RULES) {
                    String label = rule.id + " — " + rule.desc + (rule.permanent ? " (постоянно)" : " (" + rule.duration + ")");
                    if (ImGui.selectable(label)) {
                        banState.selectedRule = rule;
                        ImGui.closeCurrentPopup();
                    }
                }
            }

            ImGui.spacing();
            ImGui.separator();
            if (ImGui.button("Отмена", -1, 0)) {
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }
    }

    private void drawMuteRulesPopup() {
        ImGui.setNextWindowSize(520, 420, ImGuiCond.Appearing);
        if (ImGui.beginPopupModal(POPUP_MUTE_RULES)) {
            ImGui.text("Выберите правило для мута:");
            ImGui.separator();

            if (ImGui.collapsingHeader("Основные правила", ImGuiSelectableFlags.DontClosePopups)) {
                for (Rule rule : MUTE_RULES) {
                    String label = rule.id + " — " + rule.desc + " (" + rule.duration + ")";
                    if (ImGui.selectable(label)) {
                        muteState.selectedRule = rule;
                        ImGui.closeCurrentPopup();
                    }
                }
            }

            ImGui.spacing();
            ImGui.separator();
            if (ImGui.button("Отмена", -1, 0)) {
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }
    }

    private void drawBanPlayersPopup() {
        ImGui.setNextWindowSize(340, 400, ImGuiCond.Appearing);
        if (ImGui.beginPopupModal(POPUP_BAN_PLAYERS)) {
            ImGui.text("Онлайн-игроки:");
            ImGui.separator();
            drawOnlinePlayersList(name -> {
                banState.selectedPlayerName = name;
                banState.useOffline = false;
            });
            if (ImGui.button("Отмена", -1, 0)) {
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }
    }

    private void drawMutePlayersPopup() {
        ImGui.setNextWindowSize(340, 400, ImGuiCond.Appearing);
        if (ImGui.beginPopupModal(POPUP_MUTE_PLAYERS)) {
            ImGui.text("Онлайн-игроки:");
            ImGui.separator();
            drawOnlinePlayersList(name -> {
                muteState.selectedPlayerName = name;
                muteState.useOffline = false;
            });
            if (ImGui.button("Отмена", -1, 0)) {
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }
    }

    private void drawCheckPlayersPopup() {
        ImGui.setNextWindowSize(340, 400, ImGuiCond.Appearing);
        if (ImGui.beginPopupModal(POPUP_CHECK_PLAYERS)) {
            ImGui.text("Онлайн-игроки:");
            ImGui.separator();
            drawOnlinePlayersList(name -> checkState.selectedPlayerName = name);
            if (ImGui.button("Отмена", -1, 0)) {
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }
    }

    private interface PlayerSelectHandler {
        void accept(String name);
    }

    private void drawOnlinePlayersList(PlayerSelectHandler onSelect) {
        Minecraft mc = Minecraft.getInstance();
        List<NetworkPlayerInfo> players = new ArrayList<NetworkPlayerInfo>();

        if (mc.getConnection() != null) {
            Collection<NetworkPlayerInfo> online = mc.getConnection().getOnlinePlayers();
            if (online != null) {
                players.addAll(online);
                players.sort(Comparator.comparing(p -> {
                    GameProfile profile = p.getProfile();
                    String name = profile != null ? profile.getName() : "";
                    return name == null ? "" : name.toLowerCase();
                }));
            }
        }

        if (players.isEmpty()) {
            ImGui.textDisabled("(онлайн-игроки не найдены или нет подключения)");
            return;
        }

        ImGui.beginChild("playersList", 0, -40, true);
        for (NetworkPlayerInfo info : players) {
            GameProfile profile = info.getProfile();
            if (profile == null) {
                continue;
            }

            String name = profile.getName();
            if (name == null || name.isEmpty()) {
                continue;
            }

            if (ImGui.selectable(name)) {
                onSelect.accept(name);
                ImGui.closeCurrentPopup();
            }
        }
        ImGui.endChild();
    }

    private void setStatus(ActionState state, String msg) {
        state.statusMessage = msg;
        state.statusTimer = 6f;
    }

    private void setStatus(CheckState state, String msg) {
        state.statusMessage = msg;
        state.statusTimer = 6f;
    }

    private void renderStatus(ActionState state) {
        if (state.statusTimer > 0f && !state.statusMessage.isEmpty()) {
            ImGui.spacing();
            ImGui.separator();
            ImGui.textWrapped(state.statusMessage);
        }
    }

    private void renderStatus(CheckState state) {
        if (state.statusTimer > 0f && !state.statusMessage.isEmpty()) {
            ImGui.spacing();
            ImGui.separator();
            ImGui.textWrapped(state.statusMessage);
        }
    }

    private void tickStatuses(float deltaTime) {
        tickStatus(banState, deltaTime);
        tickStatus(muteState, deltaTime);
        tickStatus(checkState, deltaTime);
    }

    private void tickStatus(ActionState state, float deltaTime) {
        if (state.statusTimer > 0f) {
            state.statusTimer -= deltaTime;
            if (state.statusTimer <= 0f) {
                state.statusMessage = "";
            }
        }
    }

    private void tickStatus(CheckState state, float deltaTime) {
        if (state.statusTimer > 0f) {
            state.statusTimer -= deltaTime;
            if (state.statusTimer <= 0f) {
                state.statusMessage = "";
            }
        }
    }

    private void pushDisabledButtonStyle() {
        ImGui.pushStyleColor(ImGuiCol.Button, colButton[0] * 0.6f, colButton[1] * 0.6f, colButton[2] * 0.6f, colButton[3]);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, colButton[0] * 0.6f, colButton[1] * 0.6f, colButton[2] * 0.6f, colButton[3]);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, colButton[0] * 0.6f, colButton[1] * 0.6f, colButton[2] * 0.6f, colButton[3]);
    }

    private void popDisabledButtonStyle() {
        ImGui.popStyleColor(3);
    }

    private void drawStyleEditor() {
        ImGui.begin("Редактор цветов стиля", showStyleEditor, ImGuiWindowFlags.NoCollapse);
        ImGui.text("Меняй цвета меню прямо на лету:");
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
        ImGui.colorEdit4("Header", colHeader);
        ImGui.colorEdit4("HeaderHovered", colHeaderHovered);
        ImGui.colorEdit4("HeaderActive", colHeaderActive);
        ImGui.spacing();
        ImGui.separator();
        if (ImGui.button("Сбросить к дефолту (розовый заголовок)")) {
            resetColorsToDefault();
        }
        ImGui.end();
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
        style.setColor(ImGuiCol.Header, colHeader[0], colHeader[1], colHeader[2], colHeader[3]);
        style.setColor(ImGuiCol.HeaderHovered, colHeaderHovered[0], colHeaderHovered[1], colHeaderHovered[2], colHeaderHovered[3]);
        style.setColor(ImGuiCol.HeaderActive, colHeaderActive[0], colHeaderActive[1], colHeaderActive[2], colHeaderActive[3]);

        style.setWindowRounding(2.0f);
        style.setFrameRounding(2.0f);
        style.setTabRounding(2.0f);
        style.setGrabRounding(2.0f);
        style.setScrollbarRounding(2.0f);
        style.setPopupRounding(2.0f);
        style.setWindowPadding(10.0f, 10.0f);
        style.setFramePadding(8.0f, 5.0f);
        style.setItemSpacing(8.0f, 6.0f);
    }

    private void resetColorsToDefault() {
        colWindowBg[0] = 30 / 255f; colWindowBg[1] = 30 / 255f; colWindowBg[2] = 35 / 255f; colWindowBg[3] = 220 / 255f;
        colTitleBg[0] = 245 / 255f; colTitleBg[1] = 70 / 255f; colTitleBg[2] = 130 / 255f; colTitleBg[3] = 220 / 255f;
        colTitleBgActive[0] = 245 / 255f; colTitleBgActive[1] = 70 / 255f; colTitleBgActive[2] = 130 / 255f; colTitleBgActive[3] = 220 / 255f;
        colTitleBgCollapsed[0] = 245 / 255f; colTitleBgCollapsed[1] = 70 / 255f; colTitleBgCollapsed[2] = 130 / 255f; colTitleBgCollapsed[3] = 160 / 255f;
        colText[0] = 1f; colText[1] = 1f; colText[2] = 1f; colText[3] = 1f;
        colTextDisabled[0] = 180 / 255f; colTextDisabled[1] = 180 / 255f; colTextDisabled[2] = 190 / 255f; colTextDisabled[3] = 140 / 255f;
        colTab[0] = 42 / 255f; colTab[1] = 42 / 255f; colTab[2] = 42 / 255f; colTab[3] = 180 / 255f;
        colTabHovered[0] = 60 / 255f; colTabHovered[1] = 60 / 255f; colTabHovered[2] = 65 / 255f; colTabHovered[3] = 200 / 255f;
        colTabActive[0] = 50 / 255f; colTabActive[1] = 50 / 255f; colTabActive[2] = 55 / 255f; colTabActive[3] = 220 / 255f;
        colButton[0] = 70 / 255f; colButton[1] = 70 / 255f; colButton[2] = 80 / 255f; colButton[3] = 220 / 255f;
        colButtonHovered[0] = 90 / 255f; colButtonHovered[1] = 90 / 255f; colButtonHovered[2] = 100 / 255f; colButtonHovered[3] = 230 / 255f;
        colButtonActive[0] = 110 / 255f; colButtonActive[1] = 110 / 255f; colButtonActive[2] = 120 / 255f; colButtonActive[3] = 240 / 255f;
        colCheckMark[0] = 245 / 255f; colCheckMark[1] = 70 / 255f; colCheckMark[2] = 130 / 255f; colCheckMark[3] = 1f;
        colFrameBg[0] = 50 / 255f; colFrameBg[1] = 50 / 255f; colFrameBg[2] = 55 / 255f; colFrameBg[3] = 220 / 255f;
        colFrameBgHovered[0] = 70 / 255f; colFrameBgHovered[1] = 70 / 255f; colFrameBgHovered[2] = 80 / 255f; colFrameBgHovered[3] = 230 / 255f;
        colFrameBgActive[0] = 90 / 255f; colFrameBgActive[1] = 90 / 255f; colFrameBgActive[2] = 100 / 255f; colFrameBgActive[3] = 240 / 255f;
        colHeader[0] = 90 / 255f; colHeader[1] = 40 / 255f; colHeader[2] = 60 / 255f; colHeader[3] = 220 / 255f;
        colHeaderHovered[0] = 130 / 255f; colHeaderHovered[1] = 55 / 255f; colHeaderHovered[2] = 85 / 255f; colHeaderHovered[3] = 230 / 255f;
        colHeaderActive[0] = 245 / 255f; colHeaderActive[1] = 70 / 255f; colHeaderActive[2] = 130 / 255f; colHeaderActive[3] = 220 / 255f;
    }
}

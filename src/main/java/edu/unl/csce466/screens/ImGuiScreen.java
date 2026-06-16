package edu.unl.csce466.screens;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import edu.unl.csce466.imgui.ImGuiRenderer;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
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

    private static final String POPUP_BAN_RULES = "ВыборПравилаБан";
    private static final String POPUP_MUTE_RULES = "ВыборПравилаМут";
    private static final String POPUP_PLAYERS = "ВыборИгрока";

    private enum PlayerPickerContext {
        BAN,
        MUTE,
        CHECK
    }

    private static class Rule {
        final String id;
        final String desc;
        final String duration;
        final String command;
        final boolean permanent;

        Rule(String id, String desc, String duration, String command, boolean permanent) {
            this.id = id;
            this.desc = desc;
            this.duration = duration;
            this.command = command;
            this.permanent = permanent;
        }

        String reason() {
            return id + " [" + desc + "]";
        }

        String buildCommand(String targetName) {
            if (permanent) {
                return "/" + command + " " + targetName + " " + reason();
            }
            return "/" + command + " " + targetName + " " + duration + " " + reason();
        }
    }

    private static class AssistantState {
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

    private static final List<Rule> BAN_RULES = new ArrayList<Rule>();
    private static final List<Rule> MUTE_RULES = new ArrayList<Rule>();

    static {
        BAN_RULES.add(new Rule("1.1.2", "Пиар проектов (серверов, чатов, читов и т.д.)", "12d", "tempban", false));
        BAN_RULES.add(new Rule("1.5", "Использование читов", "14d", "tempban", false));
        BAN_RULES.add(new Rule("1.5.1", "Тим с читером", "8d", "tempban", false));
        BAN_RULES.add(new Rule("1.5.2", "Клан читеров (каждый)", "14d", "tempban", false));
        BAN_RULES.add(new Rule("1.6", "Признание в использовании читов", "12d", "tempban", false));
        BAN_RULES.add(new Rule("1.7", "Ник похож на ник администрации / ютуберов", "", "ban", true));
        BAN_RULES.add(new Rule("1.8", "Использование DDoS-пакетов", "28d", "tempban", false));
        BAN_RULES.add(new Rule("1.8.1", "Попытка краша сервера", "", "ban", true));
        BAN_RULES.add(new Rule("1.9", "Отказ от проверки", "14d", "tempban", false));
        BAN_RULES.add(new Rule("2.0", "Задерживание модератора во время проверки", "14d", "tempban", false));
        BAN_RULES.add(new Rule("2.1", "Выдача себя за модерацию проекта", "20d", "tempban", false));
        BAN_RULES.add(new Rule("2.5", "Больше 5 аккаунтов в бане (каждый новый аккаунт)", "14d", "tempban", false));
        BAN_RULES.add(new Rule("2.6", "Обход бана", "", "ban", true));
        BAN_RULES.add(new Rule("2.7", "Покупка доната через сторонние маркетплейсы", "", "ban", true));

        MUTE_RULES.add(new Rule("1.1", "Спам (флуд)", "30m", "tempmute", false));
        MUTE_RULES.add(new Rule("1.1.3", "КАПС", "30m", "tempmute", false));
        MUTE_RULES.add(new Rule("1.2", "Массивное оскорбление", "1h", "tempmute", false));
        MUTE_RULES.add(new Rule("1.3", "Организация флуда в чате с помощью опроса", "4h", "tempmute", false));
        MUTE_RULES.add(new Rule("1.4", "Упоминание родителей", "12h", "tempmute", false));
        MUTE_RULES.add(new Rule("1.4.1", "Оскорбление проекта и модераторов сервера", "6h", "tempmute", false));
    }

    private final AssistantState banState = new AssistantState();
    private final AssistantState muteState = new AssistantState();
    private final CheckState checkState = new CheckState();
    private PlayerPickerContext playerPickerContext = PlayerPickerContext.BAN;

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

        tickStatusTimers(io.getDeltaTime());

        renderer.draw(() -> {
            applyCurrentColors();

            ImGui.setNextWindowSize(760, 620, ImGuiCond.FirstUseEver);
            ImGui.setNextWindowPos(
                (io.getDisplaySizeX() - 760) * 0.5f,
                (io.getDisplaySizeY() - 620) * 0.5f,
                ImGuiCond.FirstUseEver
            );

            ImGui.begin("Помощник модерации", ImGuiWindowFlags.None);

            if (ImGui.beginTabBar("ModerationTabs")) {
                if (ImGui.beginTabItem("Помощник банов")) {
                    drawBanTab(mc);
                    ImGui.endTabItem();
                }

                if (ImGui.beginTabItem("Помощник мутов")) {
                    drawMuteTab(mc);
                    ImGui.endTabItem();
                }

                if (ImGui.beginTabItem("Проверка игроков")) {
                    drawCheckTab(mc);
                    ImGui.endTabItem();
                }

                ImGui.endTabBar();
            }
            drawRulePopup(POPUP_BAN_RULES, BAN_RULES, banState, "Выберите правило для бана:");
            drawRulePopup(POPUP_MUTE_RULES, MUTE_RULES, muteState, "Выберите правило для мута:");
            drawPlayerPopup();

            ImGui.end();
        });
    }

    private void drawBanTab(Minecraft mc) {
        drawAssistantHeader("Помощник банов");

        ImGui.text("1) Правило:");
        renderSelectedRule(banState);
        if (ImGui.button("Выбрать правило...")) {
            ImGui.openPopup(POPUP_BAN_RULES);
        }

        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();

        ImGui.text("2) Игрок:");
        drawPlayerModeControls(banState, PlayerPickerContext.BAN);

        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();

        ImGui.text("3) Действие:");
        drawRuleActionButton(mc, banState, "Забанить игрока");

        renderStatus(banState);
    }

    private void drawMuteTab(Minecraft mc) {
        drawAssistantHeader("Помощник мутов");

        ImGui.text("1) Правило:");
        renderSelectedRule(muteState);
        if (ImGui.button("Выбрать правило...")) {
            ImGui.openPopup(POPUP_MUTE_RULES);
        }

        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();

        ImGui.text("2) Игрок:");
        drawPlayerModeControls(muteState, PlayerPickerContext.MUTE);

        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();

        ImGui.text("3) Действие:");
        drawRuleActionButton(mc, muteState, "Выдать мут");

        renderStatus(muteState);
    }

    private void drawCheckTab(Minecraft mc) {
        drawAssistantHeader("Проверка игроков");

        ImGui.text("Игрок для проверки:");
        if (checkState.selectedPlayerName.isEmpty()) {
            ImGui.textDisabled("  (игрок не выбран)");
        } else {
            ImGui.bulletText("Выбран: " + checkState.selectedPlayerName);
        }
        if (ImGui.button("Выбрать игрока...")) {
            openPlayerPopup(PlayerPickerContext.CHECK);
        }

        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();

        boolean canStart = mc.player != null && !checkState.selectedPlayerName.isEmpty();
        if (!canStart) {
            pushDisabledButtonStyle();
        }
        if (ImGui.button("Начать", -1, 36)) {
            if (canStart) {
                executeCheckCommand(
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
            executeCheckCommand("/asuxcheat stop", checkState, "Команда отправлена: /asuxcheat stop");
        }

        if (ImGui.button("Добавить время (2 минуты)", -1, 36)) {
            executeCheckCommand("/asuxcheat addtime 120", checkState, "Команда отправлена: /asuxcheat addtime 120");
        }

        if (ImGui.button("Заморозить проверку", -1, 36)) {
            executeCheckCommand("/asuxcheat freeze", checkState, "Команда отправлена: /asuxcheat freeze");
        }

        renderStatus(checkState);
    }

    private void drawAssistantHeader(String title) {
        ImGui.spacing();
        ImGui.textColored(245 / 255f, 70 / 255f, 130 / 255f, 1f, title);
        ImGui.separator();
        ImGui.spacing();
    }

    private void renderSelectedRule(AssistantState state) {
        if (state.selectedRule == null) {
            ImGui.textDisabled("  (правило не выбрано)");
            return;
        }

        ImGui.bulletText(state.selectedRule.id + " — " + state.selectedRule.desc);
        if (state.selectedRule.permanent) {
            ImGui.bulletText("Длительность: постоянный бан");
        } else {
            ImGui.bulletText("Длительность: " + state.selectedRule.duration);
        }
    }

    private void drawPlayerModeControls(AssistantState state, PlayerPickerContext context) {
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
            }
        } else {
            ImGui.text("Ник:");
            ImGui.setNextItemWidth(-1);
            ImGui.inputText("##offline_name_" + context.name(), state.offlineNameBuf);
        }

        ImGui.spacing();
    }

    private void drawRuleActionButton(Minecraft mc, AssistantState state, String buttonText) {
        String targetName = state.useOffline ? state.offlineNameBuf.get().trim() : state.selectedPlayerName;
        boolean canExecute = mc.player != null && state.selectedRule != null && !targetName.isEmpty();

        if (!canExecute) {
            pushDisabledButtonStyle();
        }
        if (ImGui.button(buttonText, -1, 36)) {
            if (canExecute) {
                executeRuleCommand(state, targetName);
            }
        }
        if (!canExecute) {
            popDisabledButtonStyle();
            if (ImGui.isItemHovered()) {
                ImGui.setTooltip("Сначала выбери правило и игрока.");
            }
        }

        if (canExecute) {
            ImGui.spacing();
            ImGui.textWrapped("Команда для выполнения:");
            ImGui.textColored(0.4f, 0.8f, 1f, 1f, state.selectedRule.buildCommand(targetName));
        }
    }

    private void executeRuleCommand(AssistantState state, String targetName) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            setStatus(state, "Ошибка: локальный игрок не найден.");
            return;
        }

        String command = state.selectedRule.buildCommand(targetName);
        if (command.isEmpty()) {
            setStatus(state, "Ошибка: нечего выполнять.");
            return;
        }

        mc.player.connection.send(new CChatMessagePacket(command));
        setStatus(state, "Команда отправлена: " + command);
    }

    private void executeCheckCommand(String command, CheckState state, String successMessage) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            setStatus(state, "Ошибка: локальный игрок не найден.");
            return;
        }

        mc.player.connection.send(new CChatMessagePacket(command));
        setStatus(state, successMessage);
    }

    private void drawRulePopup(String popupName, List<Rule> rules, AssistantState state, String title) {
        ImGui.setNextWindowSize(520, 480, ImGuiCond.Appearing);
        if (ImGui.beginPopupModal(popupName)) {
            ImGui.text(title);
            ImGui.separator();

            if (rules.isEmpty()) {
                ImGui.textDisabled("(список правил пуст)");
            } else {
                ImGui.beginChild(popupName + "_list", 0, -40, true);
                for (Rule rule : rules) {
                    String label = rule.id + " — " + rule.desc;
                    if (rule.permanent) {
                        label += " (постоянно)";
                    } else {
                        label += " (" + rule.duration + ")";
                    }

                    if (ImGui.selectable(label)) {
                        state.selectedRule = rule;
                        ImGui.closeCurrentPopup();
                    }
                }
                ImGui.endChild();
            }

            if (ImGui.button("Отмена", -1, 0)) {
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }
    }

    private void drawPlayerPopup() {
        ImGui.setNextWindowSize(340, 400, ImGuiCond.Appearing);
        if (ImGui.beginPopupModal(POPUP_PLAYERS)) {
            ImGui.text(playerPopupTitle());
            ImGui.separator();

            List<NetworkPlayerInfo> players = getOnlinePlayers();

            if (players.isEmpty()) {
                ImGui.textDisabled("(онлайн-игроки не найдены или нет подключения)");
            } else {
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
                        applyPickedPlayer(name);
                        ImGui.closeCurrentPopup();
                    }
                }
                ImGui.endChild();
            }

            if (ImGui.button("Отмена", -1, 0)) {
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }
    }

    private String playerPopupTitle() {
        switch (playerPickerContext) {
            case BAN:
                return "Выберите игрока для бана:";
            case MUTE:
                return "Выберите игрока для мута:";
            case CHECK:
                return "Выберите игрока для проверки:";
            default:
                return "Выберите игрока:";
        }
    }

    private void openPlayerPopup(PlayerPickerContext context) {
        playerPickerContext = context;
        ImGui.openPopup(POPUP_PLAYERS);
    }

    private void applyPickedPlayer(String name) {
        switch (playerPickerContext) {
            case BAN:
                banState.selectedPlayerName = name;
                banState.useOffline = false;
                break;
            case MUTE:
                muteState.selectedPlayerName = name;
                muteState.useOffline = false;
                break;
            case CHECK:
                checkState.selectedPlayerName = name;
                break;
        }
    }

    private List<NetworkPlayerInfo> getOnlinePlayers() {
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

        return players;
    }

    private void setStatus(AssistantState state, String message) {
        state.statusMessage = message;
        state.statusTimer = 6f;
    }

    private void setStatus(CheckState state, String message) {
        state.statusMessage = message;
        state.statusTimer = 6f;
    }

    private void renderStatus(AssistantState state) {
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

    private void tickStatusTimers(float deltaTime) {
        tickStatus(banState, deltaTime);
        tickStatus(muteState, deltaTime);
        tickStatus(checkState, deltaTime);
    }

    private void tickStatus(AssistantState state, float deltaTime) {
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
}

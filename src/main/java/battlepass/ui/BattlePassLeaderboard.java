package battlepass.ui;

import battlepass.db_entities.BattlepassPlayer;
import battlepass.main.Battlepass;
import battlepass.utils.InventoryUtils;
import battlepass.utils.InventoryUtils.*;
import battlepass.utils.Utils;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class BattlePassLeaderboard {

    public static void openMenu(CommandSender sender) {
        List<BattlepassPlayer> playerList = Battlepass.getDatabase().getTopPlayers();
        if (playerList == null || playerList.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No players found in the leaderboard.");
            return;
        }
        if (!Battlepass.getInstance().getBattlePassConfig().leaderboardGUIEnabled()) {
            showPlayers(sender, playerList);
            return;
        }
        if (sender instanceof Player player) {

            Inventory inventory = Bukkit.createInventory(new BattlepassInvHolder(), 54, "  §8§lBattlepass Leaderboard");
            inventory = InventoryUtils.fillInventory(inventory, true, Material.BLACK_STAINED_GLASS_PANE);
            inventory = InventoryUtils.createBorder(inventory, Material.PINK_STAINED_GLASS_PANE, true, Material.YELLOW_STAINED_GLASS_PANE);
            showPlayers(inventory, playerList);
            BattlepassPlayer bpp = Battlepass.getInstance().playerDataMap.get(player.getUniqueId());
            if (bpp != null) {
                inventory.setItem(49,
                        getPlayerHead(player, bpp, Battlepass.getDatabase().getPlayerRank(player.getUniqueId())));
            }
            player.openInventory(inventory);
            InventoryUtils.playBorderAnimation(player, BorderAnimationType.RAINBOW_LOOP, null);
        } else {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
        }
    }

    private static void showPlayers(Inventory inventory, List<BattlepassPlayer> playerList) {
        List<BattlepassPlayer> sortedPlayers = new ArrayList<>(playerList);
        sortedPlayers.sort(Comparator.comparingDouble(BattlepassPlayer::getXp).reversed());

        for (int x = 0; x < Math.min(sortedPlayers.size(), 9); x++) {
            BattlepassPlayer bpp = sortedPlayers.get(x);
            OfflinePlayer player = Bukkit.getOfflinePlayer(bpp.getId());
            int pos = x + 1;
            switch (x) {
                case 0:
                    inventory.setItem(13, getPlayerHead(player, bpp, pos));
                    break;

                case 1:
                    inventory.setItem(20, getPlayerHead(player, bpp, pos));
                    break;

                case 2:
                    inventory.setItem(24, getPlayerHead(player, bpp, pos));
                    break;

                default:
                    inventory.setItem(33 + pos, getPlayerHead(player, bpp, pos));
                    break;
            }
        }
    }

    private static void showPlayers(CommandSender sender, List<BattlepassPlayer> playerList) {
        List<BattlepassPlayer> sortedPlayers = new ArrayList<>(playerList);
        sortedPlayers.sort(Comparator.comparingLong(BattlepassPlayer::getXp).reversed());
        for (int x = 0; x < sortedPlayers.size(); x++) {
            BattlepassPlayer bpp = sortedPlayers.get(x);
            UUID playerId = bpp.getId();
            long xp = bpp.getXp();
            int lvl = (int) Utils.getLvl(xp) + 1;
            int pos = x + 1;
            String playerName = Bukkit.getOfflinePlayer(playerId).getName();
            String formattedXp = Utils.getFormattedLong(xp);
            sender.sendMessage(Utils
                    .toText("&7[&f" + pos + "&7]&e " + playerName + ": Lvl " + lvl + " &f-&b " + formattedXp + " xp"));
        }
    }

    private static ItemStack getPlayerHead(OfflinePlayer player, BattlepassPlayer battlepassPlayer, int pos) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (player.isOnline())
            meta.setOwningPlayer(player);
        meta.setDisplayName("§f#" + pos + ": §e" + player.getName());
        List<String> lore = new ArrayList<>();
        long xp = battlepassPlayer.getXp();
        int lvl = (int) Utils.getLvl(xp) + 1;
        lore.add("§aLvl: §b" + lvl);
        lore.add("§eXp: §b" + Utils.getFormattedLong(xp));
        Instant lastPlayed = player.getLastPlayed() != 0 ? Instant.ofEpochMilli(player.getLastPlayed()) : Instant.now();
        Duration duration = Duration.between(lastPlayed, Instant.now());
        lore.add(player.isOnline() ? "§7Currently §aOnline"
                : "§7Last online §a" + Utils.formatDuration(duration) + " §7ago");
        meta.setLore(lore);
        head.setItemMeta(meta);
        return head;
    }
}

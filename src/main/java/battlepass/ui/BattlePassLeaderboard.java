package battlepass.ui;

import battlepass.db_entities.BattlepassPlayer;
import battlepass.main.Battlepass;
import battlepass.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BattlePassLeaderboard {

    public static void openMenu(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(Battlepass.getInstance(), () -> {
            List<BattlepassPlayer> playerList = Battlepass.getDatabase().getTopPlayers();
            if (playerList != null) {
                Inventory inventory = Bukkit.createInventory(null, 54, "  §8§lBattlepass Leaderboard");
                showPlayers(playerList, inventory);
                player.openInventory(inventory);
            }
        });
    }

    private static void showPlayers(List<BattlepassPlayer> playerList, Inventory inventory) {
        List<BattlepassPlayer> sortedPlayers = new ArrayList<>(playerList);
        sortedPlayers.sort(Comparator.comparingDouble(BattlepassPlayer::getXp).reversed());

        for (int x = 0; x < Math.min(sortedPlayers.size(), 9); x++) {
            BattlepassPlayer bpp = sortedPlayers.get(x);
            Optional<Player> onlinePlayer = Optional.ofNullable(Bukkit.getServer().getPlayer(bpp.getId()));
            int y = x+1;
            if (onlinePlayer.isPresent()) {
                inventory.setItem(13, getPlayerHead(onlinePlayer.get(), bpp, y));
            } else {
                Optional<UUID> uuid = Optional.ofNullable(bpp.getId());
                if (uuid.isPresent()) {
                    Bukkit.getScheduler().runTask(Battlepass.getInstance(), () -> {
                        Optional.ofNullable(Bukkit.getOfflinePlayer(uuid.get())).ifPresent(offlinePlayer -> {
                            inventory.setItem(13, getPlayerHead(offlinePlayer, bpp, y));
                        });
                    });
                }
            }
        }
    }

    private static ItemStack getPlayerHead(OfflinePlayer player, BattlepassPlayer battlepassPlayer, int pos) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName("§f#" + pos + ": §e" + player.getName());
        List<String> lore = new ArrayList<>();
        long xp = battlepassPlayer.getXp();
        int lvl = (int) Utils.getLvl(xp) + 1;
        lore.add("§aLvl: §b" + lvl);
        lore.add("§eXp: §b" + Utils.getFormattedLong(xp));
        Instant lastPlayed = player.getLastPlayed() != 0 ? Instant.ofEpochMilli(player.getLastPlayed()) : Instant.now();
        Duration duration = Duration.between(lastPlayed, Instant.now());
        lore.add(player.isOnline() ? "§7Currently §aOnline" : "§7Last online §a" + Utils.formatDuration(duration) + " §7ago");
        meta.setLore(lore);
        head.setItemMeta(meta);
        return head;
    }
}

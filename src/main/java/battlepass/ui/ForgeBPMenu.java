package battlepass.ui;

import java.util.Arrays;

import org.bukkit.Bukkit;

import battlepass.db_entities.BattlepassPlayer;
import battlepass.main.Battlepass;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ForgeBPMenu {
    public static void createLeaderboard() {
        for (BattlepassPlayer bpp : Battlepass.getDatabase().getTopPlayers()) {
            Button plButton = GooeyButton.builder().display(new ItemStack(Items.ALLIUM))
            .title(Bukkit.getOfflinePlayer(bpp.getId()).getName())
            .lore(Arrays.asList(
                "§7Level: §a" + bpp.getXp(),
                Bukkit.getOnlinePlayers().contains(Bukkit.getOfflinePlayer(bpp.getId())) ? "§aOnline" : "§cOffline"
            ))
            .build();
        }
        
    }

    private void openLeaderboard() {
    }
}

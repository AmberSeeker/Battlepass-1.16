package battlepass.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import battlepass.db_entities.BattlepassPlayer;
import battlepass.main.Battlepass;
import battlepass.ui.BattlePassLeaderboard;
import battlepass.utils.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BattlePassTopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (Battlepass.getInstance().getBattlePassConfig().leaderboardGUIEnabled()) {
                BattlePassLeaderboard.openMenu(player);
            } else {
                List<BattlepassPlayer> playerList = Battlepass.getInstance().getDatabase().getTopPlayers();
                if (playerList != null) {
                    sender.sendMessage(Utils.toText("&7&l=== &4&lBattlepass Leaderboard &7&l==="));
                    showPlayers(sender, playerList);
                }
            }
        }
        return true;
    }

    private void showPlayers(CommandSender sender, List<BattlepassPlayer> playerList) {
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
            sender.sendMessage(Utils.toText("&7[&f" + pos + "&7]&e " + playerName + ": Lvl " + lvl + " &f-&b " + formattedXp + " xp"));
        }
    }
}

package battlepass.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import battlepass.db_entities.BattlepassPlayer;
import battlepass.main.Battlepass;
import battlepass.utils.Utils;

public class BattlePassCheckXpCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String playerName = args[0];
            Player player = Bukkit.getPlayer(playerName);
            if (player == null) {
                sender.sendMessage(Utils.toText("&cPlayer " + playerName + " not found."));
                return true;
            }

            BattlepassPlayer battlepassPlayer = Battlepass.getInstance().playerDataMap.get(player.getUniqueId());
            if (battlepassPlayer == null) {
                sender.sendMessage(Utils.toText("&cCould not find player " + playerName + " in the database."));
                return true;
            }

            long xp = battlepassPlayer.getXp();
            double lvl = Utils.getLvl(xp) + 1.0;
            long nextXp = Utils.xpForLvl(lvl);
            sender.sendMessage(Utils.toText("&a" + playerName + " has " + xp + "/" + nextXp + " XP (Lvl " + lvl + ")"));
        } else {
            sender.sendMessage(Utils.toText("&cUsage: /checkxp <player>"));
        }
        return true;
    }
}

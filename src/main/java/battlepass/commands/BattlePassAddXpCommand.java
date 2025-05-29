package battlepass.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import battlepass.db_entities.BattlepassPlayer;
import battlepass.main.Battlepass;
import battlepass.utils.Utils;

public class BattlePassAddXpCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 2) {
            String playerName = args[0];
            int xp;
            try {
                xp = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Utils.toText("&cInvalid XP amount."));
                return true;
            }

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

            Utils.handleXP(player, battlepassPlayer, xp);
            sender.sendMessage(Utils.toText("&aAdded " + xp + " XP to " + playerName));
        } else {
            sender.sendMessage(Utils.toText("&cUsage: /addxp <player> <amount>"));
        }
        return true;
    }
}

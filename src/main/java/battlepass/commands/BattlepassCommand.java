package battlepass.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import battlepass.ui.BattlePassMenu;

public class BattlepassCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            BattlePassMenu.openMenu(player);
        } else {
            sender.sendMessage("Only players can execute this command.");
        }
        return true;
    }
}

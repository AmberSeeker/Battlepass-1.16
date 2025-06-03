package battlepass.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import battlepass.main.Battlepass;
import net.md_5.bungee.api.ChatColor;

public class BattlePassReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.GREEN + "Reloading the plugin");
        Battlepass.getInstance().loadBattlePassConfig();
        return true;
    }
}

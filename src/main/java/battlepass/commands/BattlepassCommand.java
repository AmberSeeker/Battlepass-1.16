package battlepass.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import battlepass.main.Battlepass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BattlePassCommand implements CommandExecutor, TabCompleter {

    private final Battlepass plugin;
    public BattlePassCommand(Battlepass battlepass) {
        this.plugin = battlepass;
        plugin.getCommand("battlepass").setExecutor(this);
        plugin.getCommand("battlepass").setTabCompleter(this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length == 0) {
            // Show help or main battlepass GUI
            // TODO: Implement main battlepass interface
            sender.sendMessage("BattlePass - Use /battlepass <subcommand>");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                handleReloadCommand(sender, args);
                break;
                
            case "top":
                handleTopCommand(sender, args);
                break;
                
            case "checkxp":
                handleCheckXpCommand(sender, args);
                break;
                
            case "addxp":
                handleAddXpCommand(sender, args);
                break;
                
            case "setxp":
                handleSetXpCommand(sender, args);
                break;
                
            default:
                sender.sendMessage("Unknown subcommand. Available: reload, top, checkxp, addxp, setxp");
                break;
        }
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - show all subcommands
            List<String> subCommands = Arrays.asList("reload", "top", "checkxp", "addxp", "setxp");
            String input = args[0].toLowerCase();
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            // Second argument - depends on the subcommand
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "checkxp":
                case "addxp":
                case "setxp":
                    // Show online player names
                    String input = args[1].toLowerCase();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getName().toLowerCase().startsWith(input)) {
                            completions.add(player.getName());
                        }
                    }
                    break;
                    
                case "reload":
                case "top":
                    // These commands don't take additional arguments
                    break;
            }
        } else if (args.length == 3) {
            // Third argument - only for addxp and setxp (XP amount)
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "addxp":
                case "setxp":
                    // No tab completion for XP amount (integer input)
                    // Could optionally add some example values
                    break;
            }
        }
        
        return completions;
    }
    
    private void handleReloadCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("Usage: /battlepass reload");
            return;
        }
        sender.sendMessage("Reloading BattlePass plugin...");
        Battlepass.getInstance().loadBattlePassConfig();
        sender.sendMessage("BattlePass plugin reloaded!");
    }
    
    private void handleTopCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("Usage: /battlepass top");
            return;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return;
        }
        
        Player player = (Player) sender;
        
        // TODO: Implement leaderboard GUI
        player.sendMessage("Opening leaderboard GUI...");
    }
    
    private void handleCheckXpCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("Usage: /battlepass checkxp <player_name>");
            return;
        }
        
        String targetPlayerName = args[1];
        
        // TODO: Implement XP checking functionality
        sender.sendMessage("Checking XP for player: " + targetPlayerName);
    }
    
    private void handleAddXpCommand(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage("Usage: /battlepass addxp <player_name> <xp_amount>");
            return;
        }
        
        String targetPlayerName = args[1];
        String xpAmountStr = args[2];
        
        try {
            int xpAmount = Integer.parseInt(xpAmountStr);
            
            // TODO: Implement XP adding functionality
            sender.sendMessage("Adding " + xpAmount + " XP to player: " + targetPlayerName);
            
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid XP amount. Please enter a valid integer.");
        }
    }
    
    private void handleSetXpCommand(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage("Usage: /battlepass setxp <player_name> <xp_amount>");
            return;
        }
        
        String targetPlayerName = args[1];
        String xpAmountStr = args[2];
        
        try {
            int xpAmount = Integer.parseInt(xpAmountStr);
            
            // TODO: Implement XP setting functionality
            sender.sendMessage("Setting " + targetPlayerName + "'s XP to: " + xpAmount);
            
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid XP amount. Please enter a valid integer.");
        }
    }
}
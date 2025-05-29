// CommandsBuilder.java
package battlepass.commands;

import battlepass.main.Battlepass;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.CommandExecutor;

public class CommandsBuilder {

    public static void buildCommands(Battlepass plugin) {
        registerCommand(plugin, "reload", new BattlePassReloadCommand());
        registerCommand(plugin, "setxp", new BattlePassSetXpCommand());
        registerCommand(plugin, "addxp", new BattlePassAddXpCommand());
        registerCommand(plugin, "checkxp", new BattlePassCheckXpCommand());
        registerCommand(plugin, "top", new BattlePassTopCommand());
        registerCommand(plugin, "battlepass", new BattlepassCommand()); // Main command
    }

    private static void registerCommand(Battlepass plugin, String name, Object executor) {
        PluginCommand command = plugin.getCommand(name);
        if (command == null) {
            plugin.getLogg().warn("Command " + name + " is not defined in plugin.yml");
            return;
        }
        if (executor instanceof CommandExecutor) {
            command.setExecutor((CommandExecutor) executor);
        }
        // Additional checks can be added here for TabCompleter etc.
    }
}

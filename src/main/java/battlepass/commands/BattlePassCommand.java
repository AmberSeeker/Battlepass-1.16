package battlepass.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import battlepass.config.BattlePassPermissions;
import battlepass.config.BattlePassReward;
import battlepass.db_entities.BattlepassPlayer;
import battlepass.main.Battlepass;
import battlepass.ui.BattlePassLeaderboard;
import battlepass.ui.BattlePassMenu;
import battlepass.ui.ForgeBPMenu;
import battlepass.utils.Utils;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.PLUGIN_NAME + "§cThis command can only be used by players!");
                return true;
            }

            if (!player.hasPermission(BattlePassPermissions.USE_BATTLEPASS)) {
                player.sendMessage(plugin.PLUGIN_NAME + "§cYou do not have permission to use the BattlePass.");
                return true;
            }

            player.sendMessage(plugin.PLUGIN_NAME + "§eOpening BattlePass GUI...");
            ForgeBPMenu.openMenu(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "spigui":
                // Sample command
                return true;
            case "help":
                // Show help message
                if (!(sender.hasPermission(BattlePassPermissions.USE_BATTLEPASS))) {
                    sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                if (sender.hasPermission(BattlePassPermissions.PREMIUM_PERMISSION))
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "BattlePass Commands: You have premium access!");
                else
                    sender.sendMessage(ChatColor.YELLOW +
                            "BattlePass Commands: You have basic access!\nUpgrade to premium to earn more rewards!");
                sender.sendMessage(ChatColor.GREEN + "/battlepass " + ChatColor.GRAY + "-" + ChatColor.YELLOW
                        + " Open the BattlePass GUI");
                sender.sendMessage(ChatColor.GOLD + "Subcommands:");

                sender.sendMessage(ChatColor.GREEN + "/battlepass help " + ChatColor.GRAY + "-" + ChatColor.YELLOW
                        + " Show this help message");

                if (sender.hasPermission(BattlePassPermissions.TOP)) {
                    sender.sendMessage(ChatColor.GREEN + "/battlepass top " + ChatColor.GRAY + "-" + ChatColor.YELLOW
                            + " Show the BattlePass leaderboard");
                }

                if (sender.hasPermission(BattlePassPermissions.PREMIUM_PERMISSION)) {
                    sender.sendMessage(ChatColor.GREEN + "/battlepass claimpremium " + ChatColor.GRAY + "-"
                            + ChatColor.YELLOW + " Claim your premium rewards");
                }
                if (sender.hasPermission(BattlePassPermissions.RELOAD_PERMISSION)) {
                    sender.sendMessage(ChatColor.GREEN + "/battlepass reload " + ChatColor.GRAY + "-" + ChatColor.YELLOW
                            + " Reload the BattlePass plugin");
                }
                if (sender.hasPermission(BattlePassPermissions.CHECK_XP)) {
                    sender.sendMessage(ChatColor.GREEN + "/battlepass checkxp <player_name> " + ChatColor.GRAY + "-"
                            + ChatColor.YELLOW + " Check a player's XP");
                }
                if (sender.hasPermission(BattlePassPermissions.ADD_XP)) {
                    sender.sendMessage(ChatColor.GREEN + "/battlepass addxp <player_name> <xp_amount> " + ChatColor.GRAY
                            + "-" + ChatColor.YELLOW + " Add XP to a player");
                }
                if (sender.hasPermission(BattlePassPermissions.SET_XP)) {
                    sender.sendMessage(ChatColor.GREEN + "/battlepass setxp <player_name> <xp_amount> " + ChatColor.GRAY
                            + "-" + ChatColor.YELLOW + " Set a player's XP");
                }
                return true;
            case "reload":
                if (!(sender.hasPermission(BattlePassPermissions.RELOAD_PERMISSION))) {
                    sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                handleReloadCommand(sender, args);
                break;

            case "top":
                if (!(sender.hasPermission(BattlePassPermissions.TOP))) {
                    sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                handleTopCommand(sender, args);
                break;

            case "checkxp":
                if (!(sender.hasPermission(BattlePassPermissions.CHECK_XP))) {
                    sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                handleCheckXpCommand(sender, args);
                break;

            case "addxp":
                if (!(sender.hasPermission(BattlePassPermissions.ADD_XP))) {
                    sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                handleAddXpCommand(sender, args);
                break;

            case "setxp":
                if (!(sender.hasPermission(BattlePassPermissions.SET_XP))) {
                    sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                handleSetXpCommand(sender, args);
                break;

            case "upgrade":
                if (!(sender instanceof ConsoleCommandSender)) {
                    sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "This command can only be run from the console.");
                    return true;
                }
                handlePremiumUpgradeCommand((ConsoleCommandSender) sender, args);
                break;

            case "claimpremium":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "This command can only be used by players.");
                    return true;
                }

                if (!player.hasPermission(BattlePassPermissions.PREMIUM_PERMISSION)) {
                    player.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "You do not have premium access to claim rewards.");
                    return true;
                }
                claimPremiumRewards(player);
                break;

            default:
                sender.sendMessage(plugin.PLUGIN_NAME + 
                        ChatColor.RED + "Unknown subcommand. Available: help, reload, top, checkxp, addxp, setxp");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("reload", "top", "checkxp", "addxp", "setxp", "help",
                    "claimpremium");
            String input = args[0].toLowerCase();

            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
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
                case "help":
                case "claimpremium":
                    break;
            }
        } else if (args.length == 3) {
            // Third argument - only for addxp and setxp (XP amount)
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "addxp":
                case "setxp":
                    break;
            }
        }

        return completions;
    }

    private void handleReloadCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "Usage: /battlepass reload");
            return;
        }
        sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.YELLOW + "Reloading BattlePass plugin...");
        plugin.loadBattlePassConfig();
        sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.GREEN + "BattlePass plugin reloaded!");
    }

    private void handleTopCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "Usage: /battlepass top");
            return;
        }
        BattlePassLeaderboard.openMenu(sender);
    }

    private void handleCheckXpCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "Usage: /battlepass checkxp <player_name>");
            return;
        }

        String targetPlayerName = args[1];
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "Player " + targetPlayerName + " is not online.");
            return;
        }

        BattlepassPlayer battlepassPlayer = plugin.playerDataMap.get(targetPlayer.getUniqueId());
        if (battlepassPlayer == null) {
            sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "Player data not found for " + targetPlayerName + ".");
            return;
        }

        long xp = battlepassPlayer.getXp();
        double lvl = Utils.getLvl(xp) + 1.0;
        long nextXp = Utils.xpForLvl(lvl);
        sender.sendMessage(plugin.PLUGIN_NAME + 
                Utils.toText("&a" + targetPlayerName + " has " + xp + "/" + nextXp + " XP (Lvl " + lvl + ")"));

    }

    private void handleAddXpCommand(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "Usage: /battlepass addxp <player_name> <xp_amount>");
            return;
        }

        String targetPlayerName = args[1];
        String xpAmountStr = args[2];

        try {
            int xpAmount = Integer.parseInt(xpAmountStr);
            Player player = Bukkit.getPlayer(targetPlayerName);
            if (player == null || !player.isOnline()) {
                sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "Player " + targetPlayerName + " is not online.");
                return;
            }

            BattlepassPlayer battlepassPlayer = plugin.playerDataMap.get(player.getUniqueId());
            if (battlepassPlayer == null) {
                sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "Player data not found for " + player.getName() + ".");
                return;
            }

            Utils.handleXP(player, battlepassPlayer, xpAmount);
            sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.GREEN + "Successfully added " + xpAmount + " XP to " + player.getName()
                    + "'s BattlePass.");

        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "Invalid XP amount. Please enter a valid integer.");
        }
    }

    private void handleSetXpCommand(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "Usage: /battlepass setxp <player_name> <xp_amount>");
            return;
        }

        String targetPlayerName = args[1];
        String xpAmountStr = args[2];

        try {
            int xpAmount = Integer.parseInt(xpAmountStr);
            Player player = Bukkit.getPlayer(targetPlayerName);
            if (player == null || !player.isOnline()) {
                sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "Player " + targetPlayerName + " is not online.");
                return;
            }

            BattlepassPlayer battlepassPlayer = plugin.playerDataMap.get(player.getUniqueId());
            if (battlepassPlayer == null) {
                sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "Player data not found for " + player.getName() + ".");
                return;
            }

            battlepassPlayer.setXp(xpAmount);
            sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.GREEN + "Successfully set " + player.getName() + "'s BattlePass XP to §e"
                    + xpAmount + "§a.");

        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "Invalid XP amount. Please enter a valid integer.");
        }
    }

    private void handlePremiumUpgradeCommand(ConsoleCommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /battlepass premiumupgrade <player_name>");
            return;
        }

        String playerName = args[1];
        Player player = Bukkit.getPlayer(playerName);

        if (player == null || !player.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player " + playerName + " is not online.");
            return;
        }

        if (player.hasPermission(BattlePassPermissions.PREMIUM_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "Player " + playerName + " already has premium access.");
            return;
        }

        if (plugin.getVaultPermission() == null) {
            sender.sendMessage(ChatColor.RED + "Vault permission service is not available. Please check your setup.");
            plugin.getLogger()
                    .warning(ChatColor.RED + "Vault permission service is not available. Please check your setup.");
            return;
        }
        if (!plugin.getVaultPermission().playerAdd(null, player, BattlePassPermissions.PREMIUM_PERMISSION)) {
            plugin.getLogger().warning(ChatColor.RED + "Failed to grant premium permission to " + playerName + ".");
            return;
        }
        plugin.getLogger().info(ChatColor.GREEN + "Granted premium permission to " + playerName + ".");

        BattlepassPlayer bpPlayer = plugin.playerDataMap.get(player.getUniqueId());
        if (bpPlayer == null) {
            sender.sendMessage(ChatColor.RED + "Player data not found for " + playerName + ".");
            return;
        }

        Double finalLevel = Utils.getLvl(bpPlayer.getXp());
        Long bPassRewards = plugin.premiumRewardMap.values().stream()
                .filter(reward -> reward.getRequiredLvl() <= finalLevel.intValue())
                .count();
        if (bPassRewards <= 0) {
            sender.sendMessage("No premium rewards available for " + playerName + ".");
            player.sendMessage(plugin.PLUGIN_NAME + ChatColor.YELLOW + "Your Battlepass has been upgraded to premium!");
            player.sendMessage(ChatColor.YELLOW + "Thank you for supporting the server with your purchase!");
            return;
        }

        ItemStack rewardToken = new ItemStack(Material.NAME_TAG, 1);
        ItemMeta rewardMeta = rewardToken.getItemMeta();
        rewardMeta.setDisplayName(ChatColor.GOLD + "Premium BattlePass Rewards");
        rewardMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "This item represents your premium BattlePass rewards.",
                ChatColor.YELLOW + "Player: " + ChatColor.WHITE + player.getName(),
                ChatColor.YELLOW + "Tier: " + ChatColor.WHITE + bPassRewards.toString(),
                ChatColor.GRAY
                        + "Use /battlepass claimpremium while holding it to claim your rewards upto the mentioned Tier!"));
        rewardMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        rewardMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        NamespacedKey playerKey = new NamespacedKey(plugin, "player_name");
        NamespacedKey tierKey = new NamespacedKey(plugin, "player_tier");
        rewardMeta.getPersistentDataContainer().set(playerKey, PersistentDataType.STRING, player.getName());
        rewardMeta.getPersistentDataContainer().set(tierKey, PersistentDataType.INTEGER, bPassRewards.intValue());
        rewardToken.setItemMeta(rewardMeta);
        player.getInventory().addItem(rewardToken);

        player.sendMessage(plugin.PLUGIN_NAME + 
                ChatColor.GREEN
                        + "Your Battlepass has been upgraded to premium and you have received your Premium BattlePass Rewards token! Use it to claim your rewards.");
        player.sendMessage(ChatColor.YELLOW + "Thank you for supporting the server with your purchase!");
        sender.sendMessage("Premium rewards token given to " + player.getName() + ".");
    }

    private void claimPremiumRewards(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType() != Material.NAME_TAG) {
            player.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "You must hold your Premium BattlePass Rewards token to claim rewards.");
            return;
        }
        ItemMeta meta = itemInHand.getItemMeta();
        if (meta == null) {
            player.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "Invalid item. Please hold your Premium BattlePass Rewards token.");
            return;
        }
        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey playerKey = new NamespacedKey(plugin, "player_name");
        NamespacedKey tierKey = new NamespacedKey(plugin, "player_tier");
        String storedName = data.get(playerKey, PersistentDataType.STRING);
        Integer storedTier = data.get(tierKey, PersistentDataType.INTEGER);

        if (storedName == null || storedTier == null) {
            player.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "This token is missing required information.");
            return;
        }
        if (!player.getName().equals(storedName)) {
            player.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "This Premium Rewards token does not belong to you.");
            return;
        }
        if (storedTier <= 0) {
            player.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "No premium rewards to claim.");
            return;
        }

        // Remove the token from the player's hand
        if (itemInHand.getAmount() == 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
        }
        player.updateInventory();
        // Give rewards for all tiers up to storedTier
        List<BattlePassReward> rewards = plugin.premiumRewardMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .limit(Math.min(storedTier, plugin.premiumRewardMap.size()))
                .map(Map.Entry::getValue)
                .toList();

        if (rewards.isEmpty()) {
            player.sendMessage(plugin.PLUGIN_NAME + ChatColor.RED + "No premium rewards available for you to claim.");
            return;
        }
        for (BattlePassReward reward : rewards) {
            Utils.processRewards(reward, player);
        }

        player.sendMessage(plugin.PLUGIN_NAME + ChatColor.GREEN + "You have claimed your premium rewards up to tier " + storedTier + "!");
        player.sendMessage(plugin.PLUGIN_NAME + ChatColor.YELLOW + "Thank you for supporting the server!");
    }
}

/*
 * TDOD: Add a new command for premium users to be run when a player upgrades to
 * premium.
 * The command name should be /battlepass premiumupgrade.
 * When someone buys premium, they get the battlepass.premiumupgrade and
 * battlepass.premium permission.
 * When someone buys the battlepass upgrade, run a command /battlepass
 * premiumupgrade <player_name>.
 * If they have the battlepass.premium permission, dont do anything, else give
 * them the premium rewards and check their tier, add that to an item.paper as
 * metadata,
 * and give them the item.paper with the metadata.
 * The paper item should have the following metadata:
 * - tier: the tier they are on
 * - name: the name of the player who purchased premium
 * When the player right clicks(/ or runs a command /battlepass claimpremium)
 * the paper item, it should check their name and tier and give them the rewards
 * for that tier and ones before it.
 * Cannot be used by other players, only the player who purchased premium.
 * Remove the paper item from the player's inventory after use.
 * 
 * Command Prcedure:
 * 1. Player purchases premium through the store.
 * 2. The server detects the purchase and runs the command /battlepass
 * premiumupgrade <player_name>.
 * 3. The command checks if the player has the battlepass.premium permission.
 * 4. If the player has the permission, it sends a message saying they already
 * have premium.
 * 5. If the player does not have the permission, it runs a method which adds
 * the battlepass.premium permission.
 * 6. Then it checks their level and tiers they have completed on the basic
 * pass.
 * 7. If the tier is greater than 0, it create a paper item with metadata
 * containing the tier and player name.
 * 8. The paper item is given to the player. Method over.
 * 9. The player can right click the paper item or run a command /battlepass
 * claimpremium while holding the paper to claim their premium rewards.
 * 10. The command checks the player's name and tier from the paper item
 * metadata.
 * 11. It then gives the player the rewards for that tier and all previous
 * tiers.
 * 12. After claiming the rewards, the paper item is removed from the player's
 * inventory.
 * 13. The player is sent a message thanking them for their support and
 * informing them of the rewards they received.
 * END
 * 
 * Then run the command /battlepass premiumupgrade <player_name> to get give
 * premium rewards and unlocks.
 * This command should give the player all the premium rewards and unlocks, and
 * also send them a message thanking them for their support.
 * The command should also check if the player has already purchased premium,
 * and if so, send them a message saying they already have premium. If they do
 * not have premium, it should give them the premium rewards and unlocks, and
 * send them a message thanking them for their support.
 * 
 */

package battlepass.ui;

import battlepass.config.BattlePassReward;
import battlepass.config.BattlePassText;
import battlepass.db_entities.BattlepassPlayer;
import battlepass.main.Battlepass;
import battlepass.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class BattlePassMenu {

    public static void openMenu(Player player) {
        openPage(player, 1);
    }

    public static void openPage(Player player, int page) {
        Plugin plugin = Battlepass.getInstance();

        BattlepassPlayer battlepassPlayer = Battlepass.getInstance().playerDataMap.get(player.getUniqueId());
        if (battlepassPlayer == null)
            return;

        double currentLvl = Utils.getLvl(battlepassPlayer.getXp());
        BattlePassText battlePassText = Battlepass.getInstance().battlePassText;

        Inventory inventory = Bukkit.createInventory(null, 27, Utils.toText(battlePassText.battlePassTitle));

        for (int pos = 0; pos < 18; pos++) {
            BattlePassReward reward = Battlepass.getInstance().rewardMap.get(pos + (page - 1) * 18);
            BattlePassReward premiumReward = Battlepass.getInstance().premiumRewardMap.get(pos + (page - 1) * 18);

            if (reward != null) {
                ItemStack itemStack = getItemStack(player, currentLvl, reward, false);
                inventory.setItem(reward.getPosition() - (page - 1) * 18, itemStack);
            }
            if (premiumReward != null) {
                ItemStack itemStack = getItemStack(player, currentLvl, premiumReward, true);
                inventory.setItem(premiumReward.getPosition() - (page - 1) * 18, itemStack);
            }
        }

        // Set other elements like next, back arrows, and info
        // Here you should use a GUI library or Bukkit's Inventory API to handle inventory interactions

        player.openInventory(inventory);
    }

    private static ItemStack getItemStack(Player player, double currentLvl, BattlePassReward value, boolean premium) {
        Material material = Material.matchMaterial(value.getItemType());
        boolean completed = (currentLvl >= value.getRequiredLvl());
        BattlePassText battlePassText = Battlepass.getInstance().battlePassText;

        List<String> lore = new ArrayList<>();
        if (value.getLore() != null) {
            lore = new ArrayList<>(value.getLore());
        }

        if (premium && !player.hasPermission("battlepass.premium")) {
            material = Material.BARRIER;
            lore.add("");
            lore.add(battlePassText.premiumRequiredText);
        } else if (completed) {
            material = Material.LIME_STAINED_GLASS_PANE;
            lore.add("");
            lore.add("§a§lCOMPLETED");
        } else if (currentLvl + 1.0D < value.getRequiredLvl()) {
            lore.add("");
            lore.add("§c§lLOCKED");
        }

        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(Utils.toText(value.getName()));
        meta.setLore(lore);
        itemStack.setItemMeta(meta);

        return itemStack;
    }
}

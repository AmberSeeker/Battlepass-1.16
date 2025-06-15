package battlepass.events;

import battlepass.db_entities.BattlepassPlayer;
import battlepass.main.Battlepass;
import battlepass.utils.InventoryUtils.BattlepassInvHolder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEvents implements Listener {
  
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    // BattlepassPlayer battlepassPlayer = Battlepass.getDatabase().loadPlayer(player.getUniqueId());
    if (Battlepass.getInstance().playerDataMap.get(player.getUniqueId()) == null) {
      Bukkit.getScheduler().runTaskAsynchronously(Battlepass.getInstance(), () -> {
        Battlepass.getDatabase().loadPlayer(player.getUniqueId());
      });
    }
  }
  
  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    BattlepassPlayer battlepassPlayer = Battlepass.getInstance().playerDataMap.get(player.getUniqueId());
    if (battlepassPlayer != null) {
      Bukkit.getScheduler().runTaskAsynchronously(Battlepass.getInstance(), () -> {
        Battlepass.getDatabase().savePlayer(battlepassPlayer);
      });
    } else {
      Battlepass.getInstance().getLogger().warning("Failed to save " + player.getName());
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent e) {
    if (e.getInventory().getHolder() instanceof BattlepassInvHolder) {
      e.setCancelled(true);
    }
  }
}

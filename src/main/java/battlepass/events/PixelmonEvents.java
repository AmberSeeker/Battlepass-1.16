package battlepass.events;

import battlepass.db_entities.BattlepassPlayer;
import battlepass.main.Battlepass;
import battlepass.utils.Utils;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.pixelmonmod.pixelmon.api.events.BeatWildPixelmonEvent;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.events.FishingEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PixelmonEvents {
  
  @SubscribeEvent
  public void defeatWild(BeatWildPixelmonEvent event) {
    if (event.player != null && event.wpp != null) {
      new BukkitRunnable() {
        @Override
        public void run() {
          BattlepassPlayer player = Battlepass.getInstance().playerDataMap.get(event.player.getUUID());
          int xpGained = 0;
          double baseXp = Battlepass.getInstance().battlePassXp.defeatXp;
          // Battlepass.getInstance().playerDataMap.put(uuid, battlepassPlayer);
          for (PixelmonWrapper pw : event.wpp.allPokemon) {
            if (pw != null) {
              int lvl = (pw.getPokemonLevelNum() > 100) ? 100 : pw.getPokemonLevelNum();
              xpGained += baseXp + lvl * Battlepass.getInstance().battlePassXp.defeatXpMultiplier;
            } else {
              xpGained += baseXp;
            }
          }
          Utils.handleXP((Player) event.player, player, xpGained);
        }
      }.runTaskAsynchronously(Battlepass.get());
    }
  }
  
  @SubscribeEvent
  public void onSuccessfulCapture(CaptureEvent.SuccessfulCapture event) {
    if (event.getPlayer() != null && event.getPokemon() != null) {
      new BukkitRunnable() {
        @Override
        public void run() {
          BattlepassPlayer player = Battlepass.getInstance().playerDataMap.get(event.getPlayer().getUUID());
          double baseXp = Battlepass.getInstance().battlePassXp.catchXp;
          int lvl = (event.getPokemon().getLvl().getPokemonLevel() > 100) ? 100 : event.getPokemon().getLvl().getPokemonLevel();
          int xpGained = (int) (baseXp + lvl * Battlepass.getInstance().battlePassXp.catchXpMultiplier);
          Utils.handleXP((Player) event.getPlayer(), player, xpGained);
        }
      }.runTaskAsynchronously(Battlepass.get());
    }
  }
  
  @SubscribeEvent
  public void onCatch(FishingEvent.Reel event) {
    if (event.player != null && event.optEntity.isPresent() && event.optEntity.get() instanceof PixelmonEntity) {
      new BukkitRunnable() {
        @Override
        public void run() {
          PixelmonEntity pokemon = (PixelmonEntity)event.optEntity.get();
          BattlepassPlayer player = Battlepass.getInstance().playerDataMap.get(event.player.getUUID());
          double baseXp = Battlepass.getInstance().battlePassXp.fishingXp;
          int lvl = (pokemon.getLvl().getPokemonLevel() > 100) ? 100 : pokemon.getLvl().getPokemonLevel();
          int xpGained = (int) (baseXp + lvl * Battlepass.getInstance().battlePassXp.fishingXpMultiplier);
          Utils.handleXP((Player) event.player, player, xpGained);
        }
      }.runTaskAsynchronously(Battlepass.get());
    }
  }
}

package battlepass.db_entities;

import java.util.UUID;

public class BattlepassPlayer {
  private UUID id;
  
  private long xp;

  private int prestige;
  
  public BattlepassPlayer(UUID id) {
    this.id = id;
  }
  
  public UUID getId() {
    return this.id;
  }
  
  public long getXp() {
    return this.xp;
  }
  
  public void setXp(long xp) {
    this.xp = xp;
  }

  public int getPrestige() {
    return this.prestige;
  }

  public void setPrestige(int prestige) {
    this.prestige = prestige;
  }
}

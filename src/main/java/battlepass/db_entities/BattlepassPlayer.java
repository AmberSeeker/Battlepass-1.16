package battlepass.db_entities;

import java.util.UUID;

public class BattlepassPlayer {
  private UUID id;
  
  private long xp;
  
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
}

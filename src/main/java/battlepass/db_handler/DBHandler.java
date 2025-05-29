package battlepass.db_handler;

import battlepass.db_entities.BattlepassPlayer;
import battlepass.main.Battlepass;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DBHandler {
  
  public void createTables() {
    String sql = "CREATE TABLE IF NOT EXISTS players (\n" +
            " id VARCHAR(64) PRIMARY KEY,\n" +
            " xp BIGINT DEFAULT 0\n" +
            ");";
    executeSQLStatement(sql);
  }
  
  public void savePlayer(BattlepassPlayer battlepassPlayer) {
    String sql = "INSERT INTO players (id, xp) VALUES(?, ?) ON DUPLICATE KEY UPDATE xp = ?";
    try (Connection conn = Battlepass.get().getConnection()) {
      PreparedStatement pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, battlepassPlayer.getId().toString());
      pstmt.setLong(2, battlepassPlayer.getXp());
      pstmt.setLong(3, battlepassPlayer.getXp());
      pstmt.executeUpdate();
    } catch (SQLException e) {
      Battlepass.getLogg().warn(e.getMessage());
    }
  }
  
  public void loadPlayer(UUID uuid) {
    new BukkitRunnable() {
      @Override
      public void run() {
        BattlepassPlayer battlepassPlayer = null;
        try (Connection conn = Battlepass.get().getConnection()) {
          PreparedStatement stmt = conn.prepareStatement("SELECT * FROM players where id='" + uuid.toString() + "'");
          ResultSet results = stmt.executeQuery();
          while (results.next()) {
            battlepassPlayer = new BattlepassPlayer(uuid);
            battlepassPlayer.setXp(results.getLong("xp"));
          }
          if (battlepassPlayer != null) {
            Battlepass.getInstance().playerDataMap.put(uuid, battlepassPlayer);
          } else {
            battlepassPlayer = new BattlepassPlayer(uuid);
            Battlepass.getInstance().playerDataMap.put(uuid, battlepassPlayer);
            BattlepassPlayer finalBattlepassPlayer = battlepassPlayer;
            new BukkitRunnable() {
              @Override
              public void run() {
                Battlepass.getDatabase().savePlayer(finalBattlepassPlayer);
              }
            }.runTaskAsynchronously(Battlepass.get());
          }
        } catch (SQLException e) {
          Battlepass.getLogg().warn(e.getMessage());
        }
      }
    }.runTaskAsynchronously(Battlepass.get());
  }
  
  public List<BattlepassPlayer> getTopPlayers() {
    List<BattlepassPlayer> players = new ArrayList<>();
    try (Connection conn = Battlepass.get().getConnection()) {
      PreparedStatement stmt = conn.prepareStatement("SELECT * FROM players ORDER by xp DESC LIMIT 10");
      ResultSet results = stmt.executeQuery();
      while (results.next()) {
        UUID uuid = UUID.fromString(results.getString("id"));
        BattlepassPlayer bpp = new BattlepassPlayer(uuid);
        bpp.setXp(results.getLong("xp"));
        players.add(bpp);
      }
    } catch (SQLException e) {
      Battlepass.getLogg().warn(e.getMessage());
    }
    return players;
  }
  
  private static void executeSQLStatement(String sql) {
    try (Connection conn = Battlepass.get().getConnection()) {
      Statement stmt = conn.createStatement();
      stmt.execute(sql);
    } catch (SQLException e) {
      Battlepass.getLogg().warn(e.getMessage());
    }
  }
}

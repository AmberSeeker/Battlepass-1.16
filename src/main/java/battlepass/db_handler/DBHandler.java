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
        " id TEXT PRIMARY KEY,\n" +
        " xp INTEGER DEFAULT 0\n" +
        ");";
    executeSQLStatement(sql);
  }

  public void savePlayer(BattlepassPlayer battlepassPlayer) {
    String sql = "INSERT OR REPLACE INTO players (id, xp) VALUES(?, ?)";
    try (Connection conn = Battlepass.getInstance().getConnection()) {
      PreparedStatement pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, battlepassPlayer.getId().toString());
      pstmt.setLong(2, battlepassPlayer.getXp());
      pstmt.executeUpdate();
    } catch (SQLException e) {
      Battlepass.getInstance().getLogger().warning(e.getMessage());
    }
  }

  public void loadPlayer(UUID uuid) {
    new BukkitRunnable() {
      @Override
      public void run() {
        BattlepassPlayer battlepassPlayer = null;
        try (Connection conn = Battlepass.getInstance().getConnection()) {
          PreparedStatement stmt = conn.prepareStatement("SELECT * FROM players WHERE id = ?");
          stmt.setString(1, uuid.toString());
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
            }.runTaskAsynchronously(Battlepass.getInstance());
          }
        } catch (SQLException e) {
          Battlepass.getInstance().getLogger().warning(e.getMessage());
        }
      }
    }.runTaskAsynchronously(Battlepass.getInstance());
  }

  public List<BattlepassPlayer> getTopPlayers() {
    List<BattlepassPlayer> players = new ArrayList<>();
    try (Connection conn = Battlepass.getInstance().getConnection()) {
      PreparedStatement stmt = conn.prepareStatement("SELECT * FROM players ORDER BY xp DESC LIMIT 10");
      ResultSet results = stmt.executeQuery();
      while (results.next()) {
        UUID uuid = UUID.fromString(results.getString("id"));
        BattlepassPlayer bpp = new BattlepassPlayer(uuid);
        bpp.setXp(results.getLong("xp"));
        players.add(bpp);
      }
    } catch (SQLException e) {
      Battlepass.getInstance().getLogger().warning(e.getMessage());
    }
    return players;
  }

  private static void executeSQLStatement(String sql) {
    try (Connection conn = Battlepass.getInstance().getConnection()) {
      Statement stmt = conn.createStatement();
      stmt.execute(sql);
    } catch (SQLException e) {
      Battlepass.getInstance().getLogger().warning(e.getMessage());
    }
  }

  public int getPlayerRank(UUID playerUUID) {
    int rank = -1;
    String sql = "WITH RankedPlayers AS (" +
        "    SELECT " +
        "        id, " +
        "        ROW_NUMBER() OVER (ORDER BY xp DESC) as player_rank " +
        "    FROM " +
        "        players " +
        ") " +
        "SELECT player_rank " +
        "FROM RankedPlayers " +
        "WHERE id = ?;";

    try (Connection conn = Battlepass.getInstance().getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, playerUUID.toString());

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          rank = rs.getInt("player_rank");
        }
      }
    } catch (SQLException e) {
      Battlepass.getInstance().getLogger()
          .warning("Error getting rank for player " + playerUUID + ": " + e.getMessage());
    }
    return rank;
  }

}
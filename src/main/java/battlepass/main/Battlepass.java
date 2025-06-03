package battlepass.main;

import io.izzel.arclight.api.Arclight;

import battlepass.commands.CommandsBuilder;
import battlepass.config.BattlePassConfig;
import battlepass.config.BattlePassReward;
import battlepass.config.BattlePassText;
import battlepass.config.BattlePassXp;
import battlepass.db_entities.BattlepassPlayer;
import battlepass.db_handler.DBHandler;
import battlepass.events.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import com.pixelmonmod.pixelmon.Pixelmon;
import net.milkbowl.vault.economy.Economy;
import ca.landonjw.gooeylibs2.api.UIManager;
import net.minecraftforge.fml.ModList;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Battlepass extends JavaPlugin {

    private static Battlepass instance;

    private static final String CONFIG = "battlepass.conf";

    public final Map<UUID, BattlepassPlayer> playerDataMap = new HashMap<>();

    public final BattlePassXp battlePassXp = new BattlePassXp();

    public final BattlePassText battlePassText = new BattlePassText();

    public Map<Integer, BattlePassReward> rewardMap = new HashMap<>();

    public Map<Integer, BattlePassReward> premiumRewardMap = new HashMap<>();

    private static DBHandler database;

    private File defaultConfig;

    private String dbPath;

    private Connection dataSource;

    private BattlePassConfig battlePassConfig;

    public static DBHandler getDatabase() {
        return database;
    }

    public File getDefaultConfigFile() {
        return this.defaultConfig;
    }

    public Connection getConnection() {
        try {
            if (this.dataSource == null || this.dataSource.isClosed()) {
                this.dataSource = DriverManager.getConnection(this.dbPath);
            }
            return this.dataSource;
        } catch (SQLException e) {
            getLogger().severe("Failed to get database connection: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static Battlepass getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        if (!ModList.get().isLoaded("gooeylibs2")) {
            getLogger().severe("GooeyLibs is required but not loaded! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        instance = this;
        database = new DBHandler();

        loadBattlePassConfig();

        // Initialize database
        try {
            initializeDatabase();
        } catch (Exception e) {
            getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // TODO: Change to singular /battlepass command
        CommandsBuilder.buildCommands(this);
        // new BattlePassCommand(this);
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
        Arclight.registerForgeEvent(this, Pixelmon.EVENT_BUS, new PixelmonEvents());

        getLogger().info("Battlepass plugin successfully enabled!");
    }

    private void initializeDatabase() throws SQLException {
        getLogger().info("Initializing database...");

        // Set up database path - using SQLite
        File dbFile = new File(getDataFolder(), "players.db");
        this.dbPath = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            getLogger().info("SQLite driver not found.");
        }

        // Test connection and create tables
        Connection testConnection = getConnection();
        if (testConnection != null) {
            getDatabase().createTables();
            getLogger().info("Database successfully initialized.");
        } else {
            throw new SQLException("Failed to establish database connection");
        }
    }

    public BattlePassConfig getBattlePassConfig() {
        return this.battlePassConfig;
    }

    public void loadBattlePassConfig() {
        getLogger().info("Loading plugin configuration...");
        try {
            loadConfig();
            this.battlePassConfig = new BattlePassConfig(YamlConfiguration.loadConfiguration(this.defaultConfig));
            getLogger().info("Plugin configuration successfully loaded.");
        } catch (IOException e) {
            getLogger().severe("Something went wrong during configuration load: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            getLogger().severe("Something went wrong during battlepass config parsing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadConfig() throws IOException {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File configFile = new File(getDataFolder(), CONFIG);
        if (!configFile.exists()) {
            saveResource(CONFIG, false);
        }
        this.defaultConfig = configFile;
    }

    @Override
    public void onDisable() {

        Bukkit.getServer().getOnlinePlayers().forEach(player -> {
            BattlepassPlayer bpPlayer = playerDataMap.get(player.getUniqueId());
            if (bpPlayer != null) {
                getDatabase().savePlayer(bpPlayer);
            }
        });
        // Close database connection
        if (this.dataSource != null) {
            try {
                if (!this.dataSource.isClosed()) {
                    this.dataSource.close();
                    getLogger().info("Database connection closed.");
                }
            } catch (SQLException e) {
                getLogger().severe("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }

        playerDataMap.clear();

        getLogger().info("Battlepass plugin disabled.");
    }
}

// TODO: players with buffered xp should be saved to database on disable
// TODO: handle money rewards economy plugin integration
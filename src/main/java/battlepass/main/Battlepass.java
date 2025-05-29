package battlepass.main;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.izzel.arclight.api.Arclight;
import com.pixelmonmod.pixelmon.Pixelmon;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Battlepass extends JavaPlugin {

    private static Battlepass instance;

    private static final String CONFIG = "battlepass.conf";

    private Logger logger;

    public final Map<UUID, BattlepassPlayer> playerDataMap = new HashMap<>();

    public final BattlePassXp battlePassXp = new BattlePassXp();

    public final BattlePassText battlePassText = new BattlePassText();

    public Map<Integer, BattlePassReward> rewardMap = new HashMap<>();

    public Map<Integer, BattlePassReward> premiumRewardMap = new HashMap<>();

    private static Battlepass battlepass;

    private static DBHandler database;

    private File defaultConfig;

    private Path configDir;

    private String dbPath;

    private Connection dataSource;

    private BattlePassConfig battlePassConfig;

    private static Logger log = LoggerFactory.getLogger(Battlepass.class);

    public static Battlepass get() {
        return battlepass;
    }

    public static DBHandler getDatabase() {
        return database;
    }

    public File getDefaultConfigFile() {
        return this.defaultConfig;
    }

    public Connection getConnection() {
        try {
            // Check if connection is null or closed, create new one if needed
            if (this.dataSource == null || this.dataSource.isClosed()) {
                this.dataSource = DriverManager.getConnection(this.dbPath);
            }
            return this.dataSource;
        } catch (SQLException e) {
            getLogg().error("Failed to get database connection: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static Logger getLogg() {
        return log;
    }

    public static Battlepass getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        battlepass = this;
        instance = this;
        log = getLogg();
        database = new DBHandler();
        
        // Load configuration first
        loadBattlePassConfig();
        
        // Initialize database
        try {
            initializeDatabase();
        } catch (Exception e) {
            getLogg().error("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            // Disable plugin if database initialization fails
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Build commands and register events
        CommandsBuilder.buildCommands(this);
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
        Arclight.registerForgeEvent(this, Pixelmon.EVENT_BUS, new PixelmonEvents());
        
        getLogg().info("Battlepass plugin successfully enabled!");
    }

    private void initializeDatabase() throws SQLException {
        getLogg().info("Initializing database...");
        
        // Set up database path - using SQLite which is more commonly available
        File dbFile = new File(getDataFolder(), "players.db");
        this.dbPath = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        
        // Try to load SQLite driver first, fall back to H2 if available
        boolean driverLoaded = false;
        
        // Try SQLite first (most common)
        // try {
        //     Class.forName("org.sqlite.JDBC");
        //     driverLoaded = true;
        //     getLogg().info("Using SQLite database driver.");
        // } catch (ClassNotFoundException e) {
        //     getLogg().info("SQLite driver not found, trying H2...");
        // }
        
        // Fall back to H2 if SQLite not available
        if (!driverLoaded) {
            try {
                Class.forName("org.h2.Driver");
                // Switch to H2 path format
                this.dbPath = String.format("jdbc:h2:%s/players;mode=MySQL", 
                    getDataFolder().getAbsolutePath());
                driverLoaded = true;
                getLogg().info("Using H2 database driver.");
            } catch (ClassNotFoundException e) {
                getLogg().info("H2 driver not found, trying built-in options...");
            }
        }
        
        // If neither SQLite nor H2 available, throw error
        if (!driverLoaded) {
            getLogg().error("No suitable database driver found! Please install SQLite or H2 database driver.");
            throw new SQLException("No database driver available");
        }
        
        // Test connection and create tables
        Connection testConnection = getConnection();
        if (testConnection != null) {
            database.createTables();
            getLogg().info("Database successfully initialized.");
        } else {
            throw new SQLException("Failed to establish database connection");
        }
    }

    public BattlePassConfig getBattlePassConfig() {
        return this.battlePassConfig;
    }

    public void loadBattlePassConfig() {
        getLogg().info("Loading plugin configuration...");
        try {
            loadConfig();
            this.battlePassConfig = new BattlePassConfig(YamlConfiguration.loadConfiguration(this.defaultConfig));
            getLogg().info("Plugin configuration successfully loaded.");
        } catch (IOException e) {
            getLogg().error("Something went wrong during configuration load: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            getLogg().error("Something went wrong during battlepass config parsing: " + e.getMessage());
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
        this.configDir = configFile.toPath().getParent();
    }

    @Override
    public void onDisable() {
        getLogg().info("Disabling Battlepass plugin...");
        
        // Close database connection
        if (this.dataSource != null) {
            try {
                if (!this.dataSource.isClosed()) {
                    this.dataSource.close();
                    getLogg().info("Database connection closed.");
                }
            } catch (SQLException e) {
                getLogg().error("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Clear player data map
        playerDataMap.clear();
        
        getLogg().info("Battlepass plugin disabled.");
    }
}
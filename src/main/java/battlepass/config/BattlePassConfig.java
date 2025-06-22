package battlepass.config;

import battlepass.main.Battlepass;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;

public class BattlePassConfig {
    private final FileConfiguration config;

    private final ComparableVersion CURRENT_CONFIG_VERSION = new ComparableVersion(
            Battlepass.getInstance().getDescription().getVersion());

    public BattlePassConfig(FileConfiguration config) {
        this.config = config;
        Battlepass.getInstance().rewardMap = new HashMap<>();
        Battlepass.getInstance().premiumRewardMap = new HashMap<>();
        int rewardCount = config.getConfigurationSection("main") != null
                ? config.getConfigurationSection("main").getInt("rewardCount", 27)
                : 27;

        for (int x = 1; x <= rewardCount; x++) {
            BattlePassReward battlePassReward = getBattlePassReward("" + x, false);
            BattlePassReward premiumBattlePassReward = getBattlePassReward("" + x, true);
            if (battlePassReward != null)
                Battlepass.getInstance().rewardMap.put(battlePassReward.getPosition(), battlePassReward);
            if (premiumBattlePassReward != null)
                Battlepass.getInstance().premiumRewardMap.put(premiumBattlePassReward.getPosition(),
                        premiumBattlePassReward);
        }
        configUptoDate();
        loadXPRates();
        loadText();
        loadPrestige();
    }

    private BattlePassReward getBattlePassReward(String name, boolean premium) {
        ConfigurationSection rewards = premium ? config.getConfigurationSection("premium." + name)
                : config.getConfigurationSection("rewards." + name);
        if (rewards != null && !rewards.getKeys(false).isEmpty()) {
            String displayName = rewards.getString("name");
            String itemType = rewards.getString("itemType");
            int position = rewards.getInt("position");
            int lvl = rewards.getInt("lvl") - 1;
            int money = rewards.getInt("money");
            List<String> commands = rewards.getStringList("commands");
            List<String> lore = rewards.getStringList("lore");
            return new BattlePassReward(displayName, itemType, lvl, position, money, commands, lore);
        } else {
            Battlepass.getInstance().getLogger().severe("Error when trying to load reward " + name);
            return null;
        }
    }

    private void loadXPRates() {
        ConfigurationSection xp = config.getConfigurationSection("xp");
        BattlePassXp battlePassXp = Battlepass.getInstance().battlePassXp;
        if (xp != null) {
            battlePassXp.defeatXp = xp.getDouble("defeatPokemon");
            battlePassXp.defeatXpMultiplier = xp.getDouble("defeatPokemonMultiplier");
            battlePassXp.catchXp = xp.getDouble("catchPokemon");
            battlePassXp.catchXpMultiplier = xp.getDouble("catchPokemonMultiplier");
            battlePassXp.fishingXp = xp.getDouble("fishingPokemon");
            battlePassXp.fishingXpMultiplier = xp.getDouble("fishingPokemonMultiplier");
            battlePassXp.hatchXp = xp.getDouble("hatchPokemon");
            battlePassXp.eggCyclesBonusOnHatch = xp.getBoolean("eggCyclesBonusOnHatch");
        } else {
            Battlepass.getInstance().getLogger().severe("Error when trying to load xp rates");
        }
    }

    private void loadText() {
        ConfigurationSection text = config.getConfigurationSection("text");
        BattlePassText battlePassText = Battlepass.getInstance().battlePassText;
        if (text != null) {
            battlePassText.battlePassTitle = text.getString("battlePassTitle");
            battlePassText.premiumRequiredText = text.getString("premiumRequiredText");
        } else {
            Battlepass.getInstance().getLogger().severe("Error when trying to load text");
        }
    }

    private void loadPrestige() {
        ConfigurationSection prestige = config.getConfigurationSection("prestige");
        BattlePassPrestige battlePassPrestige = Battlepass.getInstance().battlePassPrestige;
        if (prestige != null) {
            battlePassPrestige.prestigeEnabled = prestige.getBoolean("prestigeEnabled", false);
            battlePassPrestige.levelsPerPrestige = prestige.getInt("levelsPerPrestige", 100);
            battlePassPrestige.perpetualPrestige = prestige.getBoolean("perpetualPrestige", false);
            battlePassPrestige.prestigePerLevel = prestige.getInt("prestigePerLevel", 1);
            battlePassPrestige.bonusPrestigeForPremium = prestige.getBoolean("bonusPrestigeForPremium", false);
            battlePassPrestige.prestigeMultiplierForPremium = prestige.getInt("prestigeMultiplierForPremium", 1);
            battlePassPrestige.resetOnPrestige = prestige.getBoolean("resetOnPrestige", false);
        } else {
            Battlepass.getInstance().getLogger().severe("Error when trying to load prestige info");
        }
    }

    private void configUptoDate() {
        ConfigurationSection main = config.getConfigurationSection("main");
        ComparableVersion configVersion = new ComparableVersion(
                main != null ? main.getString("config-version", "invalid-version") : "invalid_version");
        if (configVersion.compareTo(CURRENT_CONFIG_VERSION) < 0) {
            Battlepass.getInstance().getLogger().warning(
                    "Your battlepass config file(battlepass.conf) is not up to date. Stuff may not work.\nCurrent Version: "
                            + configVersion.getCanonical() + ", Latest Version: " + CURRENT_CONFIG_VERSION
                            + "\nDelete it to regenerate the file or get the latest one from: https://github.com/AmberSeeker/Battlepass-1.16/blob/main/src/main/resources/battlepass.conf to compare and update.");
        }
    }

    public boolean leaderboardGUIEnabled() {
        ConfigurationSection main = config.getConfigurationSection("main");
        return main != null && main.getBoolean("leaderboardGUIEnabled");
    }

    public boolean animatedGUIEnabled() {
        ConfigurationSection main = config.getConfigurationSection("main");
        return main != null && main.getBoolean("animatedGUIEnabled", false);
    }
}

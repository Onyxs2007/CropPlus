package com.cropplus.config;

import com.cropplus.CropPlus;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager {
    
    private final CropPlus plugin;
    private FileConfiguration config;
    
    public ConfigManager(CropPlus plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    // General Settings
    public boolean isEnabled() {
        return config.getBoolean("settings.enabled", true);
    }
    
    public boolean isDebug() {
        return config.getBoolean("settings.debug", false);
    }
    
    public boolean isCheckUpdates() {
        return config.getBoolean("settings.check-updates", true);
    }
    
    // Tool Restriction Settings
    public boolean isToolRestrictionsEnabled() {
        return config.getBoolean("tool-restrictions.enabled", true);
    }
    
    public boolean isToolRestrictionsEnabled(String worldName) {
        if (!config.getBoolean("tool-restrictions.world-specific.enabled", false)) {
            return isToolRestrictionsEnabled();
        }
        return config.getBoolean("tool-restrictions.world-specific.worlds." + worldName + ".enabled", 
                                isToolRestrictionsEnabled());
    }
    
    public List<String> getAllowedTools() {
        return config.getStringList("tool-restrictions.allowed-tools");
    }
    
    public List<String> getAllowedTools(String worldName) {
        if (!config.getBoolean("tool-restrictions.world-specific.enabled", false)) {
            return getAllowedTools();
        }
        List<String> worldTools = config.getStringList("tool-restrictions.world-specific.worlds." + worldName + ".allowed-tools");
        return worldTools.isEmpty() ? getAllowedTools() : worldTools;
    }
    
    public boolean isAllowBareHands() {
        return config.getBoolean("tool-restrictions.allow-bare-hands", false);
    }
    
    public boolean isAllowBareHands(String worldName) {
        if (!config.getBoolean("tool-restrictions.world-specific.enabled", false)) {
            return isAllowBareHands();
        }
        return config.getBoolean("tool-restrictions.world-specific.worlds." + worldName + ".allow-bare-hands", 
                                isAllowBareHands());
    }
    
    // Harvesting Settings
    public boolean isHarvestingEnabled() {
        return config.getBoolean("harvesting.enabled", true);
    }
    
    public boolean isRequireSeeds() {
        return config.getBoolean("harvesting.require-seeds", true);
    }
    
    public boolean isAutoCollectEnabled() {
        return config.getBoolean("harvesting.auto-collect.enabled", true);
    }
    
    public boolean isAutoCollectEnabled(String worldName) {
        if (!config.getBoolean("harvesting.auto-collect.world-specific.enabled", false)) {
            return isAutoCollectEnabled();
        }
        return config.getBoolean("harvesting.auto-collect.world-specific.worlds." + worldName + ".auto-collect", 
                                isAutoCollectEnabled());
    }
    
    public String getDropLocation() {
        return config.getString("harvesting.auto-collect.drop-location", "CROP");
    }
    
    public String getDropLocation(String worldName) {
        if (!config.getBoolean("harvesting.auto-collect.world-specific.enabled", false)) {
            return getDropLocation();
        }
        return config.getString("harvesting.auto-collect.world-specific.worlds." + worldName + ".drop-location", 
                               getDropLocation());
    }
    
    public boolean isPlaySounds() {
        return config.getBoolean("harvesting.play-sounds", true);
    }
    
    public boolean isShowParticles() {
        return config.getBoolean("harvesting.show-particles", true);
    }
    
    // Mass Harvesting Settings
    public boolean isMassHarvestingEnabled() {
        return config.getBoolean("mass-harvesting.enabled", true);
    }
    
    public int getMaxRange() {
        return config.getInt("mass-harvesting.max-range", 5);
    }
    
    public int getDefaultRange() {
        return config.getInt("mass-harvesting.default-range", 3);
    }
    
    public boolean isRequireSneak() {
        return config.getBoolean("mass-harvesting.require-sneak", true);
    }
    
    public int getMaxCrops() {
        return config.getInt("mass-harvesting.max-crops", 50);
    }
    
    public boolean isMassHarvestToolRestrictionsEnabled() {
        return config.getBoolean("mass-harvesting.tool-restrictions.enabled", false);
    }
    
    public String getMassHarvestMinimumTier() {
        return config.getString("mass-harvesting.tool-restrictions.minimum-tier", "IRON");
    }
    
    // Cooldown Settings
    public boolean isCooldownsEnabled() {
        return config.getBoolean("cooldowns.enabled", true);
    }
    
    public double getGlobalCooldown() {
        return config.getDouble("cooldowns.global", 0.5);
    }
    
    public double getPerCropCooldown() {
        return config.getDouble("cooldowns.per-crop", 2.0);
    }
    
    public double getMassHarvestCooldown() {
        return config.getDouble("cooldowns.mass-harvest", 5.0);
    }
    
    // World Settings
    public boolean isWorldRestrictionsEnabled() {
        return config.getBoolean("worlds.enabled", false);
    }
    
    public List<String> getAllowedWorlds() {
        return config.getStringList("worlds.allowed");
    }
    
    public List<String> getDisabledWorlds() {
        return config.getStringList("worlds.disabled");
    }
    
    // Group Settings
    public boolean isGroupSettingsEnabled() {
        return config.getBoolean("groups.enabled", false);
    }
    
    public boolean isGroupToolRestrictionsEnabled(String groupName) {
        return config.getBoolean("groups.configurations." + groupName + ".tool-restrictions.enabled", false);
    }
    
    public List<String> getGroupAllowedTools(String groupName) {
        return config.getStringList("groups.configurations." + groupName + ".tool-restrictions.allowed-tools");
    }
    
    public boolean isGroupAllowBareHands(String groupName) {
        return config.getBoolean("groups.configurations." + groupName + ".tool-restrictions.allow-bare-hands", false);
    }
    
    public boolean isGroupAutoCollectEnabled(String groupName) {
        return config.getBoolean("groups.configurations." + groupName + ".auto-collect.enabled", true);
    }
    
    public String getGroupDropLocation(String groupName) {
        return config.getString("groups.configurations." + groupName + ".auto-collect.drop-location", "CROP");
    }
    
    public double getGroupCooldownMultiplier(String groupName) {
        return config.getDouble("groups.configurations." + groupName + ".cooldown-multiplier", 1.0);
    }
    
    public int getGroupRangeBonus(String groupName) {
        return config.getInt("groups.configurations." + groupName + ".range-bonus", 0);
    }
    
    // Crop Settings
    public boolean isCropEnabled(String cropType) {
        return config.getBoolean("crops." + cropType.toLowerCase() + ".enabled", true);
    }
    
    public String getSeedItem(String cropType) {
        return config.getString("crops." + cropType.toLowerCase() + ".seed-item", "");
    }
    
    public double getDropMultiplier(String cropType) {
        return config.getDouble("crops." + cropType.toLowerCase() + ".drop-multiplier", 1.0);
    }
    
    public double getToolMultiplier(String cropType, Material tool) {
        String toolName = tool.name();
        return config.getDouble("crops." + cropType.toLowerCase() + ".tool-multipliers." + toolName, 1.0);
    }
    
    // Economy Settings
    public boolean isEconomyEnabled() {
        return config.getBoolean("economy.enabled", false);
    }
    
    public double getRewardPerCrop() {
        return config.getDouble("economy.reward-per-crop", 0.1);
    }
    
    public double getMassHarvestBonus() {
        return config.getDouble("economy.mass-harvest-bonus", 1.5);
    }
    
    public double getEconomyToolMultiplier(Material tool) {
        String toolName = tool.name();
        return config.getDouble("economy.tool-multipliers." + toolName, 1.0);
    }
    
    // Messages - Fixed to handle missing keys properly
    public String getMessage(String key) {
        String message = config.getString("messages." + key);
        if (message == null) {
            plugin.getLogger().warning("Missing message key: " + key);
            return "Message key '" + key + "' not found in config!";
        }
        return message;
    }
    
    // Helper method to get raw message without prefix (for special cases)
    public String getRawMessage(String key) {
        return config.getString("messages." + key, "Message key '" + key + "' not found!");
    }
    
    // Helper methods for player-specific settings
    public String getPlayerGroup(Player player) {
        if (!isGroupSettingsEnabled()) {
            return null;
        }
        
        // Check permissions to determine group
        for (String group : config.getConfigurationSection("groups.configurations").getKeys(false)) {
            if (player.hasPermission("cropplus.group." + group)) {
                return group;
            }
        }
        return null;
    }
    
    // Backward compatibility methods
    @Deprecated
    public boolean isDropAtPlayer() {
        return !isAutoCollectEnabled() && "PLAYER".equals(getDropLocation());
    }
    
    @Deprecated
    public boolean isAutoPickup() {
        return isAutoCollectEnabled();
    }
}
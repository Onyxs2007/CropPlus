package com.cropplus;

import com.cropplus.commands.CropPlusCommand;
import com.cropplus.config.ConfigManager;
import com.cropplus.listeners.CropHarvestListener;
import com.cropplus.managers.CooldownManager;
import com.cropplus.managers.CropManager;
import com.cropplus.managers.EconomyManager;
import com.cropplus.managers.ToolManager;
import com.cropplus.utils.MessageUtil;
import com.cropplus.utils.UpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;

public final class CropPlus extends JavaPlugin {
    
    private ConfigManager configManager;
    private CropManager cropManager;
    private CooldownManager cooldownManager;
    private EconomyManager economyManager;
    private ToolManager toolManager;
    private MessageUtil messageUtil;
    private UpdateChecker updateChecker;
    
    @Override
    public void onEnable() {
        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();
        
        this.messageUtil = new MessageUtil(this);
        this.cropManager = new CropManager(this);
        this.cooldownManager = new CooldownManager(this);
        this.economyManager = new EconomyManager(this);
        this.toolManager = new ToolManager(this);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new CropHarvestListener(this), this);
        
        // Register commands
        getCommand("cropplus").setExecutor(new CropPlusCommand(this));
        
        // Check for updates
        if (configManager.isCheckUpdates()) {
            this.updateChecker = new UpdateChecker(this);
            updateChecker.checkForUpdates();
        }
        
        getLogger().info("CropPlus has been enabled!");
        getLogger().info("Tool restrictions: " + (configManager.isToolRestrictionsEnabled() ? "Enabled" : "Disabled"));
        getLogger().info("Auto-collect: " + (configManager.isAutoCollectEnabled() ? "Enabled" : "Disabled"));
        getLogger().info("Economy integration: " + (economyManager.isEconomyEnabled() ? "Enabled" : "Disabled"));
    }
    
    @Override
    public void onDisable() {
        // Clean up cooldowns
        if (cooldownManager != null) {
            cooldownManager.cleanup();
        }
        
        getLogger().info("CropPlus has been disabled!");
    }
    
    // Getters for managers
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public CropManager getCropManager() {
        return cropManager;
    }
    
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
    
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    public ToolManager getToolManager() {
        return toolManager;
    }
    
    public MessageUtil getMessageUtil() {
        return messageUtil;
    }
    
    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }
}
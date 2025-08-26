package com.cropplus.managers;

import com.cropplus.CropPlus;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {
    
    private final CropPlus plugin;
    private Economy economy;
    private boolean economyEnabled;
    
    public EconomyManager(CropPlus plugin) {
        this.plugin = plugin;
        this.economyEnabled = false;
        setupEconomy();
    }
    
    public void setupEconomy() {
        if (!plugin.getConfigManager().isEconomyEnabled()) {
            return;
        }
        
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault not found! Economy features disabled.");
            return;
        }
        
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager()
                .getRegistration(Economy.class);
        
        if (rsp == null) {
            plugin.getLogger().warning("No economy plugin found! Economy features disabled.");
            return;
        }
        
        economy = rsp.getProvider();
        economyEnabled = true;
        plugin.getLogger().info("Economy integration enabled with " + economy.getName());
    }
    
    public boolean isEconomyEnabled() {
        return economyEnabled && plugin.getConfigManager().isEconomyEnabled();
    }
    
    public void giveHarvestReward(Player player, int cropCount) {
        giveHarvestReward(player, cropCount, 1.0);
    }
    
    public void giveHarvestReward(Player player, int cropCount, double toolMultiplier) {
        if (!isEconomyEnabled()) {
            return;
        }
        
        double baseReward = plugin.getConfigManager().getRewardPerCrop();
        double totalReward = baseReward * cropCount * toolMultiplier;
        
        if (totalReward <= 0) {
            return;
        }
        
        economy.depositPlayer(player, totalReward);
        
        // Send reward message
        String message = plugin.getConfigManager().getMessage("economy-reward")
                .replace("{amount}", economy.format(totalReward));
        plugin.getMessageUtil().sendMessage(player, message);
        
        if (plugin.getConfigManager().isDebug()) {
            plugin.getLogger().info("Gave " + player.getName() + " " + 
                                  economy.format(totalReward) + " for harvesting " + cropCount + " crops");
        }
    }
    
    public Economy getEconomy() {
        return economy;
    }
}
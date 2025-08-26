package com.cropplus.managers;

import com.cropplus.CropPlus;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    
    private final CropPlus plugin;
    private final Map<UUID, Long> globalCooldowns = new HashMap<>();
    private final Map<String, Long> cropCooldowns = new HashMap<>();
    private final Map<UUID, Long> massHarvestCooldowns = new HashMap<>();
    
    public CooldownManager(CropPlus plugin) {
        this.plugin = plugin;
    }
    
    public boolean hasGlobalCooldown(Player player, double cooldownSeconds) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long cooldownTime = (long) (cooldownSeconds * 1000);
        
        if (globalCooldowns.containsKey(playerId)) {
            long lastUsed = globalCooldowns.get(playerId);
            if (currentTime - lastUsed < cooldownTime) {
                return true;
            }
        }
        
        globalCooldowns.put(playerId, currentTime);
        return false;
    }
    
    public boolean hasCropCooldown(Location location, double cooldownSeconds) {
        String locationKey = getLocationKey(location);
        long currentTime = System.currentTimeMillis();
        long cooldownTime = (long) (cooldownSeconds * 1000);
        
        if (cropCooldowns.containsKey(locationKey)) {
            long lastUsed = cropCooldowns.get(locationKey);
            if (currentTime - lastUsed < cooldownTime) {
                return true;
            }
        }
        
        cropCooldowns.put(locationKey, currentTime);
        return false;
    }
    
    public boolean hasMassHarvestCooldown(Player player, double cooldownSeconds) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long cooldownTime = (long) (cooldownSeconds * 1000);
        
        if (massHarvestCooldowns.containsKey(playerId)) {
            long lastUsed = massHarvestCooldowns.get(playerId);
            if (currentTime - lastUsed < cooldownTime) {
                return true;
            }
        }
        
        massHarvestCooldowns.put(playerId, currentTime);
        return false;
    }
    
    public long getRemainingGlobalCooldown(Player player, double cooldownSeconds) {
        UUID playerId = player.getUniqueId();
        if (!globalCooldowns.containsKey(playerId)) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long lastUsed = globalCooldowns.get(playerId);
        long cooldownTime = (long) (cooldownSeconds * 1000);
        long remaining = cooldownTime - (currentTime - lastUsed);
        
        return Math.max(0, remaining);
    }
    
    public long getRemainingMassHarvestCooldown(Player player, double cooldownSeconds) {
        UUID playerId = player.getUniqueId();
        if (!massHarvestCooldowns.containsKey(playerId)) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long lastUsed = massHarvestCooldowns.get(playerId);
        long cooldownTime = (long) (cooldownSeconds * 1000);
        long remaining = cooldownTime - (currentTime - lastUsed);
        
        return Math.max(0, remaining);
    }
    
    public void clearAllCooldowns() {
        globalCooldowns.clear();
        cropCooldowns.clear();
        massHarvestCooldowns.clear();
    }
    
    public void clearPlayerCooldowns(Player player) {
        UUID playerId = player.getUniqueId();
        globalCooldowns.remove(playerId);
        massHarvestCooldowns.remove(playerId);
    }
    
    public void cleanup() {
        // Clean up old cooldowns (older than 1 hour)
        long currentTime = System.currentTimeMillis();
        long oneHour = 60 * 60 * 1000;
        
        globalCooldowns.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > oneHour);
        
        cropCooldowns.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > oneHour);
        
        massHarvestCooldowns.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > oneHour);
        
        if (plugin.getConfigManager().isDebug()) {
            plugin.getLogger().info("Cleaned up old cooldown entries");
        }
    }
    
    private String getLocationKey(Location location) {
        return location.getWorld().getName() + "_" + 
               location.getBlockX() + "_" + 
               location.getBlockY() + "_" + 
               location.getBlockZ();
    }
}
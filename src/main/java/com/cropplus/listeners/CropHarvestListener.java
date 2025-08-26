package com.cropplus.listeners;

import com.cropplus.CropPlus;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CropHarvestListener implements Listener {
    
    private final CropPlus plugin;
    
    public CropHarvestListener(CropPlus plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-click actions
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        // Only handle main hand interactions to prevent double events
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        
        if (block == null) {
            return;
        }
        
        // Check if player is using bonemeal - if so, skip our harvesting logic entirely
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.BONE_MEAL) {
            return; // Let the normal bonemeal behavior happen
        }
        
        // Check if plugin is enabled
        if (!plugin.getConfigManager().isEnabled() || !plugin.getConfigManager().isHarvestingEnabled()) {
            return;
        }
        
        // Check if it's a supported crop
        if (!plugin.getCropManager().isSupportedCrop(block)) {
            return;
        }
        
        // Check if crop is fully grown
        if (!plugin.getCropManager().isFullyGrown(block)) {
            return;
        }
        
        // Check permissions
        if (!player.hasPermission("cropplus.use")) {
            plugin.getMessageUtil().sendMessage(player, "no-permission");
            return;
        }
        
        // Check tool restrictions
        if (!plugin.getToolManager().hasValidTool(player)) {
            sendToolRestrictionMessage(player);
            return;
        }
        
        // Check world restrictions
        if (!isWorldAllowed(player, block)) {
            plugin.getMessageUtil().sendMessage(player, "world-not-allowed");
            return;
        }
        
        // Check cooldowns
        if (!player.hasPermission("cropplus.bypass.cooldown") && hasCooldown(player, block)) {
            plugin.getMessageUtil().sendMessage(player, "cooldown-active");
            return;
        }
        
        // Check if player has seeds for replanting
        if (!plugin.getCropManager().hasRequiredSeeds(player, block)) {
            plugin.getMessageUtil().sendMessage(player, "no-seeds");
            return;
        }
        
        // Determine if this should be a mass harvest
        boolean isMassHarvest = plugin.getConfigManager().isMassHarvestingEnabled() && 
                               shouldMassHarvest(player);
        
        if (isMassHarvest) {
            handleMassHarvest(player, block);
        } else {
            handleSingleHarvest(player, block);
        }
        
        // Cancel the event to prevent normal interaction
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clear player cooldowns when they leave
        plugin.getCooldownManager().clearPlayerCooldowns(event.getPlayer());
    }
    
    private void sendToolRestrictionMessage(Player player) {
        String worldName = player.getWorld().getName();
        
        if (!plugin.getConfigManager().isAllowBareHands(worldName) && 
            player.getInventory().getItemInMainHand().getType().isAir()) {
            plugin.getMessageUtil().sendMessage(player, "bare-hands-not-allowed");
        } else {
            String requiredTool = plugin.getToolManager().getRequiredToolName(player);
            plugin.getMessageUtil().sendMessage(player, "tool-required", "{tool}", requiredTool);
        }
    }
    
    private boolean isWorldAllowed(Player player, Block block) {
        // Bypass permission check
        if (player.hasPermission("cropplus.bypass.world")) {
            return true;
        }
        
        if (!plugin.getConfigManager().isWorldRestrictionsEnabled()) {
            return true;
        }
        
        String worldName = block.getWorld().getName();
        
        // Check disabled worlds
        if (plugin.getConfigManager().getDisabledWorlds().contains(worldName)) {
            return false;
        }
        
        // Check allowed worlds (if list is not empty)
        var allowedWorlds = plugin.getConfigManager().getAllowedWorlds();
        if (!allowedWorlds.isEmpty() && !allowedWorlds.contains(worldName)) {
            return false;
        }
        
        return true;
    }
    
    private boolean hasCooldown(Player player, Block block) {
        if (!plugin.getConfigManager().isCooldownsEnabled()) {
            return false;
        }
        
        // Apply group cooldown multiplier
        double cooldownMultiplier = 1.0;
        String playerGroup = plugin.getConfigManager().getPlayerGroup(player);
        if (playerGroup != null) {
            cooldownMultiplier = plugin.getConfigManager().getGroupCooldownMultiplier(playerGroup);
        }
        
        // Check global cooldown
        double globalCooldown = plugin.getConfigManager().getGlobalCooldown() * cooldownMultiplier;
        if (globalCooldown > 0 && plugin.getCooldownManager().hasGlobalCooldown(player, globalCooldown)) {
            return true;
        }
        
        // Check per-crop cooldown
        double perCropCooldown = plugin.getConfigManager().getPerCropCooldown() * cooldownMultiplier;
        if (perCropCooldown > 0 && plugin.getCooldownManager().hasCropCooldown(block.getLocation(), perCropCooldown)) {
            return true;
        }
        
        return false;
    }
    
    private boolean shouldMassHarvest(Player player) {
        if (!plugin.getConfigManager().isMassHarvestingEnabled()) {
            return false;
        }
        
        // Check if sneak is required for mass harvest
        if (plugin.getConfigManager().isRequireSneak() && !player.isSneaking()) {
            return false;
        }
        
        // Check tool requirements for mass harvesting
        if (!plugin.getToolManager().hasValidMassHarvestTool(player)) {
            String minimumTier = plugin.getConfigManager().getMassHarvestMinimumTier();
            plugin.getMessageUtil().sendMessage(player, "tool-tier-too-low", "{tier}", minimumTier.toLowerCase());
            return false;
        }
        
        return true;
    }
    
    private void handleSingleHarvest(Player player, Block block) {
        String cropName = block.getType().name().toLowerCase().replace("_", " ");
        
        plugin.getCropManager().harvestCrop(player, block);
        
        plugin.getMessageUtil().sendMessage(player, "crop-harvested", 
                "{amount}", "1", 
                "{crop}", cropName);
        
        if (plugin.getConfigManager().isDebug()) {
            plugin.getLogger().info(player.getName() + " harvested a " + cropName + " at " + 
                                  block.getLocation().toString());
        }
    }
    
    private void handleMassHarvest(Player player, Block block) {
        // Check mass harvest cooldown with group multiplier
        double cooldownMultiplier = 1.0;
        String playerGroup = plugin.getConfigManager().getPlayerGroup(player);
        if (playerGroup != null) {
            cooldownMultiplier = plugin.getConfigManager().getGroupCooldownMultiplier(playerGroup);
        }
        
        double massHarvestCooldown = plugin.getConfigManager().getMassHarvestCooldown() * cooldownMultiplier;
        if (!player.hasPermission("cropplus.bypass.cooldown") && 
            massHarvestCooldown > 0 && 
            plugin.getCooldownManager().hasMassHarvestCooldown(player, massHarvestCooldown)) {
            plugin.getMessageUtil().sendMessage(player, "cooldown-active");
            return;
        }
        
        // Determine range with group bonus
        int range = plugin.getConfigManager().getDefaultRange();
        if (playerGroup != null) {
            range += plugin.getConfigManager().getGroupRangeBonus(playerGroup);
        }
        
        // Check if player has unlimited range permission
        if (player.hasPermission("cropplus.mass.unlimited")) {
            range = plugin.getConfigManager().getMaxRange();
            if (playerGroup != null) {
                range += plugin.getConfigManager().getGroupRangeBonus(playerGroup);
            }
        }
        
        int harvestedCount = plugin.getCropManager().massHarvest(player, block, range);
        
        if (harvestedCount > 0) {
            plugin.getMessageUtil().sendMessage(player, "mass-harvest-success", 
                    "{amount}", String.valueOf(harvestedCount));
            
            if (plugin.getConfigManager().isDebug()) {
                plugin.getLogger().info(player.getName() + " mass harvested " + harvestedCount + 
                                      " crops at " + block.getLocation().toString());
            }
        }
    }
}
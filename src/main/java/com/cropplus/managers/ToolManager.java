package com.cropplus.managers;

import com.cropplus.CropPlus;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ToolManager {
    
    private final CropPlus plugin;
    private final Set<Material> hoeTypes;
    
    public ToolManager(CropPlus plugin) {
        this.plugin = plugin;
        this.hoeTypes = Set.of(
            Material.WOODEN_HOE,
            Material.STONE_HOE,
            Material.IRON_HOE,
            Material.GOLDEN_HOE,
            Material.DIAMOND_HOE,
            Material.NETHERITE_HOE
        );
    }
    
    /**
     * Check if the player is holding a valid tool for harvesting
     */
    public boolean hasValidTool(Player player) {
        String worldName = player.getWorld().getName();
        
        // Check if tool restrictions are enabled for this world
        if (!plugin.getConfigManager().isToolRestrictionsEnabled(worldName)) {
            return true;
        }
        
        // Check group-specific settings first
        String playerGroup = plugin.getConfigManager().getPlayerGroup(player);
        if (playerGroup != null && plugin.getConfigManager().isGroupToolRestrictionsEnabled(playerGroup)) {
            return hasValidToolForGroup(player, playerGroup);
        }
        
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        
        // Check if bare hands are allowed
        if (mainHand.getType() == Material.AIR) {
            return plugin.getConfigManager().isAllowBareHands(worldName);
        }
        
        // Get allowed tools for this world
        List<String> allowedTools = plugin.getConfigManager().getAllowedTools(worldName);
        
        // Check if the tool is in the allowed list
        return isToolAllowed(mainHand.getType(), allowedTools);
    }
    
    /**
     * Check if the player has a valid tool for mass harvesting
     */
    public boolean hasValidMassHarvestTool(Player player) {
        if (!hasValidTool(player)) {
            return false;
        }
        
        if (!plugin.getConfigManager().isMassHarvestToolRestrictionsEnabled()) {
            return true;
        }
        
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        String minimumTier = plugin.getConfigManager().getMassHarvestMinimumTier();
        
        return isToolTierSufficient(mainHand.getType(), minimumTier);
    }
    
    /**
     * Get the tool multiplier for drops based on the tool being used
     */
    public double getToolMultiplier(Player player, String cropType) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        Material tool = mainHand.getType();
        
        // Get base multiplier from crop config
        double baseMultiplier = plugin.getConfigManager().getDropMultiplier(cropType);
        
        // Get tool-specific multiplier
        double toolMultiplier = plugin.getConfigManager().getToolMultiplier(cropType, tool);
        
        return baseMultiplier * toolMultiplier;
    }
    
    /**
     * Get the economy multiplier based on the tool being used
     */
    public double getEconomyToolMultiplier(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        return plugin.getConfigManager().getEconomyToolMultiplier(mainHand.getType());
    }
    
    /**
     * Get a user-friendly name for the required tool
     */
    public String getRequiredToolName(Player player) {
        String worldName = player.getWorld().getName();
        List<String> allowedTools = plugin.getConfigManager().getAllowedTools(worldName);
        
        if (allowedTools.isEmpty()) {
            return "any tool";
        }
        
        if (allowedTools.contains("ANY_HOE")) {
            return "hoe";
        }
        
        // Return the first allowed tool as an example
        String firstTool = allowedTools.get(0);
        return firstTool.toLowerCase().replace("_", " ");
    }
    
    /**
     * Check if a specific tool is allowed based on the configuration
     */
    private boolean isToolAllowed(Material tool, List<String> allowedTools) {
        if (allowedTools.isEmpty()) {
            return true; // If no restrictions, allow all tools
        }
        
        String toolName = tool.name();
        
        // Check for exact match
        if (allowedTools.contains(toolName)) {
            return true;
        }
        
        // Check for "ANY_HOE" wildcard
        if (allowedTools.contains("ANY_HOE") && hoeTypes.contains(tool)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if the tool tier is sufficient for mass harvesting
     */
    private boolean isToolTierSufficient(Material tool, String minimumTier) {
        if (!hoeTypes.contains(tool)) {
            return false; // Only hoes are considered for tier checking
        }
        
        int toolTier = getToolTier(tool);
        int requiredTier = getToolTierFromString(minimumTier);
        
        return toolTier >= requiredTier;
    }
    
    /**
     * Get the numeric tier of a tool
     */
    private int getToolTier(Material tool) {
        switch (tool) {
            case WOODEN_HOE: return 0;
            case STONE_HOE: return 1;
            case IRON_HOE: return 2;
            case GOLDEN_HOE: return 0; // Golden tools are fast but weak
            case DIAMOND_HOE: return 3;
            case NETHERITE_HOE: return 4;
            default: return -1;
        }
    }
    
    /**
     * Convert tier string to numeric value
     */
    private int getToolTierFromString(String tier) {
        switch (tier.toUpperCase()) {
            case "WOODEN": return 0;
            case "STONE": return 1;
            case "IRON": return 2;
            case "GOLDEN": return 0;
            case "DIAMOND": return 3;
            case "NETHERITE": return 4;
            default: return 0;
        }
    }
    
    /**
     * Check if player has valid tool based on their group settings
     */
    private boolean hasValidToolForGroup(Player player, String groupName) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        
        // Check if bare hands are allowed for this group
        if (mainHand.getType() == Material.AIR) {
            return plugin.getConfigManager().isGroupAllowBareHands(groupName);
        }
        
        // Get allowed tools for this group
        List<String> allowedTools = plugin.getConfigManager().getGroupAllowedTools(groupName);
        
        // If group has no tool restrictions, allow any tool
        if (allowedTools.isEmpty()) {
            return true;
        }
        
        return isToolAllowed(mainHand.getType(), allowedTools);
    }
    
    /**
     * Get all hoe types
     */
    public Set<Material> getHoeTypes() {
        return hoeTypes;
    }
    
    /**
     * Check if a material is a hoe
     */
    public boolean isHoe(Material material) {
        return hoeTypes.contains(material);
    }
}
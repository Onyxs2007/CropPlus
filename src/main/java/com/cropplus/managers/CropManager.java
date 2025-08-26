package com.cropplus.managers;

import com.cropplus.CropPlus;
import com.cropplus.models.CropInfo;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CropManager {
    
    private final CropPlus plugin;
    private final Map<Material, CropInfo> supportedCrops;
    
    public CropManager(CropPlus plugin) {
        this.plugin = plugin;
        this.supportedCrops = new HashMap<>();
        initializeSupportedCrops();
    }
    
    private void initializeSupportedCrops() {
        // Initialize supported crops with their information
        supportedCrops.put(Material.WHEAT, new CropInfo(Material.WHEAT, Material.WHEAT_SEEDS, 7));
        supportedCrops.put(Material.CARROTS, new CropInfo(Material.CARROTS, Material.CARROT, 7));
        supportedCrops.put(Material.POTATOES, new CropInfo(Material.POTATOES, Material.POTATO, 7));
        supportedCrops.put(Material.BEETROOTS, new CropInfo(Material.BEETROOTS, Material.BEETROOT_SEEDS, 3));
        supportedCrops.put(Material.NETHER_WART, new CropInfo(Material.NETHER_WART, Material.NETHER_WART, 3));
        supportedCrops.put(Material.COCOA, new CropInfo(Material.COCOA, Material.COCOA_BEANS, 2));
        supportedCrops.put(Material.SWEET_BERRY_BUSH, new CropInfo(Material.SWEET_BERRY_BUSH, Material.SWEET_BERRIES, 3));
    }
    
    public boolean isSupportedCrop(Block block) {
        Material type = block.getType();
        return supportedCrops.containsKey(type) && 
               plugin.getConfigManager().isCropEnabled(type.name().toLowerCase());
    }
    
    public boolean isFullyGrown(Block block) {
        if (!isSupportedCrop(block)) {
            return false;
        }
        
        BlockData blockData = block.getBlockData();
        CropInfo cropInfo = supportedCrops.get(block.getType());
        
        if (blockData instanceof Ageable) {
            Ageable ageable = (Ageable) blockData;
            return ageable.getAge() >= cropInfo.getMaxAge();
        }
        
        return false;
    }
    
    public boolean canHarvest(Player player, Block block) {
        // Check if crop is supported and fully grown
        if (!isSupportedCrop(block) || !isFullyGrown(block)) {
            return false;
        }
        
        // Check if player has permission
        if (!player.hasPermission("cropplus.use")) {
            return false;
        }
        
        // Check tool restrictions
        if (!plugin.getToolManager().hasValidTool(player)) {
            return false;
        }
        
        // Check world restrictions
        if (plugin.getConfigManager().isWorldRestrictionsEnabled()) {
            String worldName = block.getWorld().getName();
            List<String> allowedWorlds = plugin.getConfigManager().getAllowedWorlds();
            List<String> disabledWorlds = plugin.getConfigManager().getDisabledWorlds();
            
            if (!allowedWorlds.isEmpty() && !allowedWorlds.contains(worldName)) {
                return false;
            }
            
            if (disabledWorlds.contains(worldName)) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean hasRequiredSeeds(Player player, Block block) {
        if (!plugin.getConfigManager().isRequireSeeds()) {
            return true;
        }
        
        CropInfo cropInfo = supportedCrops.get(block.getType());
        if (cropInfo == null) {
            return false;
        }
        
        Material seedType = cropInfo.getSeedType();
        return player.getInventory().contains(seedType);
    }
    
    public void harvestCrop(Player player, Block block) {
        if (!canHarvest(player, block)) {
            return;
        }
        
        CropInfo cropInfo = supportedCrops.get(block.getType());
        if (cropInfo == null) {
            return;
        }
        
        // Get drops with tool in main hand
        Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand());
        
        // Apply tool-based multiplier
        String cropType = block.getType().name().toLowerCase();
        double multiplier = plugin.getToolManager().getToolMultiplier(player, cropType);
        if (multiplier != 1.0) {
            drops = applyDropMultiplier(drops, multiplier);
        }
        
        // Handle item collection/dropping
        handleItemCollection(player, block, drops);
        
        // Replant if seeds available
        if (hasRequiredSeeds(player, block)) {
            replantCrop(player, block, cropInfo);
        } else {
            block.setType(Material.AIR);
        }
        
        // Play effects
        playHarvestEffects(player, block);
        
        // Give economy reward with tool multiplier
        if (plugin.getEconomyManager().isEconomyEnabled()) {
            double economyMultiplier = plugin.getToolManager().getEconomyToolMultiplier(player);
            plugin.getEconomyManager().giveHarvestReward(player, 1, economyMultiplier);
        }
    }
    
    private void handleItemCollection(Player player, Block block, Collection<ItemStack> drops) {
        String worldName = player.getWorld().getName();
        
        // Check group settings first
        String playerGroup = plugin.getConfigManager().getPlayerGroup(player);
        boolean autoCollect;
        String dropLocation;
        
        if (playerGroup != null) {
            autoCollect = plugin.getConfigManager().isGroupAutoCollectEnabled(playerGroup);
            dropLocation = plugin.getConfigManager().getGroupDropLocation(playerGroup);
        } else {
            autoCollect = plugin.getConfigManager().isAutoCollectEnabled(worldName);
            dropLocation = plugin.getConfigManager().getDropLocation(worldName);
        }
        
        if (autoCollect) {
            // Try to add items to inventory
            boolean allItemsAdded = addItemsToInventory(player, drops);
            
            if (!allItemsAdded) {
                // Send message about inventory being full
                plugin.getMessageUtil().sendMessage(player, "inventory-full");
            } else {
                // Send auto-collect confirmation if configured
                if (plugin.getConfigManager().isDebug()) {
                    plugin.getMessageUtil().sendMessage(player, "auto-collected");
                }
            }
        } else {
            // Drop items based on configuration
            Location targetLocation = getDropLocation(player, block, dropLocation);
            for (ItemStack drop : drops) {
                block.getWorld().dropItemNaturally(targetLocation, drop);
            }
        }
    }
    
    private Location getDropLocation(Player player, Block block, String dropLocation) {
        switch (dropLocation.toUpperCase()) {
            case "PLAYER":
                return player.getLocation();
            case "FEET":
                return player.getLocation().add(0, -1, 0);
            case "CROP":
            default:
                return block.getLocation();
        }
    }
    
    public List<Block> getNearbyHarvestableCrops(Block centerBlock, int range) {
        List<Block> crops = new ArrayList<>();
        Location center = centerBlock.getLocation();
        
        for (int x = -range; x <= range; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -range; z <= range; z++) {
                    Block block = center.clone().add(x, y, z).getBlock();
                    if (isSupportedCrop(block) && isFullyGrown(block)) {
                        crops.add(block);
                    }
                }
            }
        }
        
        return crops;
    }
    
    public int massHarvest(Player player, Block centerBlock, int range) {
        List<Block> crops = getNearbyHarvestableCrops(centerBlock, range);
        int maxCrops = plugin.getConfigManager().getMaxCrops();
        
        if (crops.size() > maxCrops) {
            crops = crops.subList(0, maxCrops);
        }
        
        int harvested = 0;
        for (Block crop : crops) {
            if (canHarvest(player, crop) && hasRequiredSeeds(player, crop)) {
                harvestCrop(player, crop);
                harvested++;
            }
        }
        
        // Give mass harvest economy bonus with tool multiplier
        if (harvested > 1 && plugin.getEconomyManager().isEconomyEnabled()) {
            double bonus = plugin.getConfigManager().getMassHarvestBonus();
            double toolMultiplier = plugin.getToolManager().getEconomyToolMultiplier(player);
            plugin.getEconomyManager().giveHarvestReward(player, (int) (harvested * bonus), toolMultiplier);
        }
        
        return harvested;
    }
    
    private void replantCrop(Player player, Block block, CropInfo cropInfo) {
        Material seedType = cropInfo.getSeedType();
        
        // Remove one seed from inventory
        ItemStack seedItem = new ItemStack(seedType, 1);
        player.getInventory().removeItem(seedItem);
        
        // Replant the crop
        block.setType(cropInfo.getCropType());
        BlockData newBlockData = block.getBlockData();
        if (newBlockData instanceof Ageable) {
            Ageable ageable = (Ageable) newBlockData;
            ageable.setAge(0);
            block.setBlockData(ageable);
        }
    }
    
    private Collection<ItemStack> applyDropMultiplier(Collection<ItemStack> drops, double multiplier) {
        Collection<ItemStack> newDrops = new ArrayList<>();
        for (ItemStack drop : drops) {
            ItemStack newDrop = drop.clone();
            int newAmount = (int) Math.ceil(drop.getAmount() * multiplier);
            newDrop.setAmount(newAmount);
            newDrops.add(newDrop);
        }
        return newDrops;
    }
    
    private boolean addItemsToInventory(Player player, Collection<ItemStack> items) {
        boolean allItemsAdded = true;
        for (ItemStack item : items) {
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            if (!leftover.isEmpty()) {
                allItemsAdded = false;
                // Drop items that don't fit
                for (ItemStack leftoverItem : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem);
                }
            }
        }
        return allItemsAdded;
    }
    
    private void playHarvestEffects(Player player, Block block) {
        if (plugin.getConfigManager().isPlaySounds()) {
            player.playSound(block.getLocation(), Sound.BLOCK_CROP_BREAK, 0.5f, 1.0f);
        }
        
        if (plugin.getConfigManager().isShowParticles()) {
            // Use HAPPY_VILLAGER instead of VILLAGER_HAPPY (renamed in newer versions)
            block.getWorld().spawnParticle(
                Particle.HAPPY_VILLAGER, 
                block.getLocation().add(0.5, 0.5, 0.5), 
                5, 0.3, 0.3, 0.3, 0.1
            );
        }
    }
    
    public Set<Material> getSupportedCropTypes() {
        return supportedCrops.keySet();
    }
    
    public CropInfo getCropInfo(Material cropType) {
        return supportedCrops.get(cropType);
    }
}
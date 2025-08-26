package com.cropplus.models;

import org.bukkit.Material;

public class CropInfo {
    
    private final Material cropType;
    private final Material seedType;
    private final int maxAge;
    
    public CropInfo(Material cropType, Material seedType, int maxAge) {
        this.cropType = cropType;
        this.seedType = seedType;
        this.maxAge = maxAge;
    }
    
    public Material getCropType() {
        return cropType;
    }
    
    public Material getSeedType() {
        return seedType;
    }
    
    public int getMaxAge() {
        return maxAge;
    }
}
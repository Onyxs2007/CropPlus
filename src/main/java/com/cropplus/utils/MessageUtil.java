package com.cropplus.utils;

import com.cropplus.CropPlus;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MessageUtil {
    
    private final CropPlus plugin;
    
    public MessageUtil(CropPlus plugin) {
        this.plugin = plugin;
    }
    
    public void sendMessage(CommandSender sender, String messageKey) {
        String message = plugin.getConfigManager().getMessage(messageKey);
        sendRawMessage(sender, message);
    }
    
    public void sendMessage(CommandSender sender, String messageKey, String... replacements) {
        String message = plugin.getConfigManager().getMessage(messageKey);
        
        // Apply replacements in pairs (placeholder, value)
        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        
        sendRawMessage(sender, message);
    }
    
    public void sendRawMessage(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        
        String prefix = plugin.getConfigManager().getMessage("prefix");
        String fullMessage = prefix + message;
        
        // Color the message
        fullMessage = ChatColor.translateAlternateColorCodes('&', fullMessage);
        
        sender.sendMessage(fullMessage);
    }
    
    public String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public String stripColor(String message) {
        return ChatColor.stripColor(message);
    }
}
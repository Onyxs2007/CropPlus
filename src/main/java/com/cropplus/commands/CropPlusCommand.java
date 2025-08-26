package com.cropplus.commands;

import com.cropplus.CropPlus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CropPlusCommand implements CommandExecutor, TabCompleter {
    
    private final CropPlus plugin;
    
    public CropPlusCommand(CropPlus plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
            case "info":
                handleInfo(sender);
                break;
            case "toggle":
                handleToggle(sender, args);
                break;
            case "help":
            default:
                showHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("cropplus.reload")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return;
        }
        
        try {
            plugin.getConfigManager().reloadConfig();
            
            // Re-setup economy if needed
            if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
                plugin.getEconomyManager().setupEconomy();
            }
            
            plugin.getMessageUtil().sendMessage(sender, "config-reloaded");
        } catch (Exception e) {
            sender.sendMessage("§cError reloading configuration: " + e.getMessage());
            plugin.getLogger().severe("Error reloading configuration: " + e.getMessage());
        }
    }
    
    private void handleInfo(CommandSender sender) {
        String version = plugin.getDescription().getVersion();
        plugin.getMessageUtil().sendMessage(sender, "plugin-info", "{version}", version);
        
        // Additional info for admins
        if (sender.hasPermission("cropplus.admin")) {
            sender.sendMessage("§7Plugin Status:");
            sender.sendMessage("§7- Enabled: §" + (plugin.getConfigManager().isEnabled() ? "a" : "c") + plugin.getConfigManager().isEnabled());
            sender.sendMessage("§7- Harvesting: §" + (plugin.getConfigManager().isHarvestingEnabled() ? "a" : "c") + plugin.getConfigManager().isHarvestingEnabled());
            sender.sendMessage("§7- Mass Harvesting: §" + (plugin.getConfigManager().isMassHarvestingEnabled() ? "a" : "c") + plugin.getConfigManager().isMassHarvestingEnabled());
            sender.sendMessage("§7- Economy: §" + (plugin.getEconomyManager().isEconomyEnabled() ? "a" : "c") + plugin.getEconomyManager().isEconomyEnabled());
            sender.sendMessage("§7- Supported Crops: §e" + plugin.getCropManager().getSupportedCropTypes().size());
        }
    }
    
    private void handleToggle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return;
        }
        
        if (!sender.hasPermission("cropplus.admin")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 2) {
            player.sendMessage("§cUsage: /cropplus toggle <feature>");
            player.sendMessage("§cFeatures: harvesting, mass-harvesting, economy");
            return;
        }
        
        String feature = args[1].toLowerCase();
        
        // Note: This is a basic implementation. In a real plugin, you'd want to
        // modify the config file and save it, not just change runtime values.
        switch (feature) {
            case "harvesting":
                // This would require modifying the config and saving it
                player.sendMessage("§eFeature toggling requires config file modification.");
                player.sendMessage("§eUse /cropplus reload after manually editing config.yml");
                break;
            default:
                player.sendMessage("§cUnknown feature: " + feature);
                player.sendMessage("§cAvailable features: harvesting, mass-harvesting, economy");
                break;
        }
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage("§a§l=== CropPlus Commands ===");
        sender.sendMessage("§a/cropplus info §7- Show plugin information");
        
        if (sender.hasPermission("cropplus.reload")) {
            sender.sendMessage("§a/cropplus reload §7- Reload configuration");
        }
        
        if (sender.hasPermission("cropplus.admin")) {
            sender.sendMessage("§a/cropplus toggle <feature> §7- Toggle features");
        }
        
        sender.sendMessage("§7");
        sender.sendMessage("§7Right-click fully grown crops to harvest them!");
        sender.sendMessage("§7Sneak + right-click for mass harvesting!");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("info", "help");
            
            if (sender.hasPermission("cropplus.reload")) {
                subCommands = new ArrayList<>(subCommands);
                subCommands.add("reload");
            }
            
            if (sender.hasPermission("cropplus.admin")) {
                subCommands = new ArrayList<>(subCommands);
                subCommands.add("toggle");
            }
            
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            List<String> features = Arrays.asList("harvesting", "mass-harvesting", "economy");
            for (String feature : features) {
                if (feature.toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(feature);
                }
            }
        }
        
        return completions;
    }
}
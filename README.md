# Crop+  

Crop+ is a lightweight farming plugin for Paper/Spigot that makes harvesting crops faster and more enjoyable while keeping it balanced for survival servers.  

---

## 🌱 Features  

- **Right-Click Harvesting**  
  Right-click a fully grown crop to harvest it instantly.  
  Seeds are automatically replanted if available.  

- **Tool Restrictions**  
  Only works if holding a hoe (or other tools defined in the config).  
  Prevents free automation and keeps survival balanced.  

- **Auto-Collect vs. Drop**  
  Choose whether crops go directly into the player’s inventory or drop on the ground.  
  Configurable globally, per world, or per permission group.  

- **Flexible Configuration**  
  - Per-world and per-group enable/disable options.  
  - Adjustable cooldowns to prevent spam harvesting.  
  - Optional multi-block harvesting with configurable range.  
  - All settings stored in YAML configs and reloadable with `/crop reload`.  

---

## 🔑 Permissions  

- `crop.use` → Allows right-click harvesting.  
- `crop.bypass.cooldown` → Exempts player from cooldowns.  
- `crop.autocollect` → Allows auto-collect into inventory.  
- `crop.admin` → Access to admin commands like reload.  

---

## ⌨️ Commands  

- `/crop reload` → Reloads the configuration files.  
- `/crop toggle` → Enables or disables right-click harvesting for yourself.  

---

## 📦 Compatibility  

- Minecraft: **1.20+ → 1.21+**  
- Server: **Paper / Spigot**  

---

## 🛠️ Installation  

1. Download the latest release of Crop+.  
2. Place the `.jar` file into your server’s `plugins` folder.  
3. Restart or reload the server.  
4. Edit the config files as needed and run `/crop reload`.  

---

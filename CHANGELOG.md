# 📋 Changelog - Fly's Plugin

## [v1.0] - 2025-09-27

### 🎉 **New Features**

#### 🌍 **Multi-Language Support**
- **NEW:** Choose between English and German in the config
- **NEW:** Easy to edit message files for customization
- **NEW:** Edit your config/messages anytime and do `/flyreload`

#### ⚡ **Flight Speed Control**
- **NEW:** `/flyspeed` - Check your current flight speed
- **NEW:** `/flyspeed <1-10>` - Set your flight speed from slow (1) to fast (10)
- **NEW:** `/flyspeed <player> <speed>` - Set flight speed for other players

#### 🔔 **Auto-Update Notifications**
- **NEW:** Get notified when a new plugin version is available

### 🎨 **Visual & Sound Effects**

#### 🎵 **Enhanced Sound System**
- **NEW:** Unique sounds for flight activation and deactivation
- **NEW:** Sound feedback for speed changes
- **NEW:** Error sounds for invalid commands
- **NEW:** All sounds can be disabled in config

#### ✨ **Beautiful Particle Effects**
- **NEW:** Wing-like particles when enabling flight
- **NEW:** Falling particles when disabling flight
- **NEW:** Magical effects when changing flight speed
- **NEW:** All particles can be disabled in config

### 🛠️ **Server Admin Features**

#### 🌍 **World Restrictions**
- **NEW:** Allow flight only in specific worlds
- **NEW:** Block flight in certain worlds (like PvP areas)
- **NEW:** Automatic flight disabling when entering restricted worlds

#### ⚙️ **Improved Configuration**
- **IMPROVED:** Cleaner config structure
- **IMPROVED:** Separate message files for easy customization
- **IMPROVED:** Better examples and documentation

### 🎮 **User Experience Improvements**

#### 💬 **Better Messages**
- **IMPROVED:** Flight status shown in action bar (no chat spam)
- **IMPROVED:** Clearer error messages and usage instructions
- **IMPROVED:** Consistent message formatting

#### 🎯 **Smart Commands**
- **IMPROVED:** Tab completion for all commands
- **IMPROVED:** Better permission handling
- **IMPROVED:** `/flyreload` now also shows current plugin version

---

## ⚙️ **Configuration**

### Language Settings:
```yaml
language:
  language: "en"    # "en" for English, "de" for German
```

### Sound & Particle Settings:
```yaml
general:
  enable-particles: true    # Enable/disable particle effects
  enable-sounds: true       # Enable/disable sound effects
```

### World Restrictions:
```yaml
worlds:
  allowed-worlds: []        # Only allow flight in these worlds
  disabled-worlds: []       # Block flight in these worlds
```

---

## 🔐 **Permissions**

| Permission | What it does |
|------------|-------------|
| `flys.fly` | Use `/fly` command |
| `flys.fly.others` | Toggle flight for other players |
| `flys.flyspeed` | Change your own flight speed |
| `flys.flyspeed.others` | Change flight speed for others |
| `flys.flyreload` | Reload plugin configuration |
| `flys.updatenotify` | Receive update notifications |

---

## 📚 **How to Update**

1. **Download** the new plugin version
2. **Stop** your server
3. **Replace** the old plugin file
4. **Start** your server
5. **New features** will be automatically available!

*Your existing config will be automatically updated with new options.*

---

## 🎯 **Commands Overview**

- `/fly` - Toggle flight on/off
- `/fly <player>` - Toggle flight for another player
- `/flyspeed` - Check current flight speed
- `/flyspeed <1-10>` - Set flight speed (1=slow, 10=fast)
- `/flyspeed <player> <speed>` - Set speed for another player
- `/flyreload` - Reload plugin config and messages

---

## 🔗 **Download & Support**

- **Download:** [Modrinth](https://modrinth.com/plugin/flys)
- **Support:** [GitHub Issues](https://github.com/Dotta4You/Flys/issues)
- **Discord:** [JKD](https://discord.gg/PfQTqhfjgA)

---

## **Made with ❤️ by Dötchen**

*Enjoy flying! ✈️*

feat bStats*
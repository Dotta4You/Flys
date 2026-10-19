# Fly's – The Lightweight & Feature-Rich Fly Plugin ✈️  

Fly's is no longer just a simple fly toggle – it’s now a fully customizable and modern fly plugin with particles, sounds, flight speed control, world restrictions, and multi-language support!  

---

## 🚀 Features  

- **Toggle Flight:** `/fly` to enable or disable flight for yourself or others  
- **Flight Speed Control:** `/flyspeed` to check or set speeds (1–10) – works for yourself or other players  
- **World Restrictions:** Allow or block flight in specific worlds, with auto-disable on entry  
- **Multi-Language Support:** Switch between English, German, Spanish, French, Russian, and Polish – edit all messages easily  
- **Visual & Sound Effects:** Custom particles and sounds for flight toggle, speed changes, and errors (fully configurable)  
- **Timed Flight:** `/fly <player> <time>` gives flight for a limited time (e.g. `30s`, `10m`, `2h`, `1d`, `1h30m`) with a countdown warning before it ends – survives relogs
- **Combat Rule:** Optionally disable flight and block it for a cooldown after PvP damage (off by default)
- **Fall Protection:** Optionally prevent fall damage right after flight is switched off in mid-air (off by default)
- **Rank Speed Limits:** Limit flight speed per rank with `flys.maxspeed.<1-10>` (the general `max-speed` still applies)
- **Flight Restore:** Optionally give players their flight back when they rejoin (off by default)
- **PlaceholderAPI:** Placeholders for flight state, speed, remaining time and combat
- **Auto-Update Notifications:** Be informed instantly when new versions are released  
- **Smart UX:** Action bar messages instead of chat spam, tab completion for all commands, clearer errors  

---

## 🎮 Commands  

- `/fly` – Toggle flight on/off  
- `/fly <player>` – Toggle flight for another player  
- `/fly <player> <time>` – Give flight for a limited time, e.g. `10m` or `1h30m` (use your own name to give it to yourself)  
- `/flyspeed` – Show your current flight speed  
- `/flyspeed <1-10>` – Set your own flight speed  
- `/flyspeed <player> <speed>` – Set another player’s speed  
- `/flyreload` – Reload config and messages  

---

## 🔐 Permissions  

| Permission | Description |
|------------|-------------|
| `flys.fly` | Use `/fly` |
| `flys.fly.others` | Toggle flight for other players |
| `flys.fly.temp` | Use `/fly <player> <time>` |
| `flys.flyspeed` | Change your own speed |
| `flys.flyspeed.others` | Change other players’ speed |
| `flys.flyreload` | Reload plugin config |
| `flys.updatenotify` | Receive update notifications |
| `flys.bypass.combat` | Never blocked by the combat cooldown |
| `flys.maxspeed.<1-10>` | Optional: limits the maximum flight speed to that level (highest one wins) |

---

## ⚙️ Configuration  

- **Language:** Switch between English (`en`), German (`de`), Spanish (`es`), French (`fr`), Russian (`ru`), and Polish (`pl`)  
- **Particles & Sounds:** Enable/disable in config  
- **Worlds:** Define allowed or restricted worlds  
- **Extras:** the `extras` section holds fall protection, the combat rule, the timed flight limit and flight restore (new features are off by default)

---

## 🧩 PlaceholderAPI

`%flys_flying%`, `%flys_flying_status%`, `%flys_flying_symbol%`, `%flys_speed%`, `%flys_speed_percent%`, `%flys_speed_max%`, `%flys_world_allowed%`, `%flys_world_status%`, `%flys_total_flying%`, `%flys_timed%`, `%flys_time_left%`, `%flys_time_left_seconds%`, `%flys_in_combat%`, `%flys_combat_time_left%`

---

## 📥 Download & Support  

- **Download:** [Modrinth](https://modrinth.com/plugin/flys)  
- **Support & Issues:** [GitHub Issues](https://github.com/Dotta4You/Flys/issues)  
- **Community:** [Discord](https://discord.gg/PfQTqhfjgA)  

---

## 📊 Metrics & License  

- Uses **bStats** for anonymous usage statistics  
- Licensed under the [MIT License](https://opensource.org/licenses/MIT) – free to use, adapt, and share (a mention is appreciated ❤️)  

---

✨ Have fun flying with Fly's – made with ❤️ by **Dötchen**!  

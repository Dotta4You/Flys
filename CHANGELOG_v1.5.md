# ✨ Fly's v1.5 - Timed Flight & Control Update

#### The Timed Flight, Combat Rule & Rank Speed Limits Update

## 🎯 Highlights

This version brings you **timed flight** with a countdown, an optional **combat rule**, **fall protection** and **speed limits per rank**.

Everything that changes gameplay is **off by default** - with the default config, Fly's plays like before. Your config and language files are updated automatically, your own settings are never changed.

---

## 🆕 New Features

### ⏱️ Timed Flight
- **`/fly <player> <time>`** gives flight for a limited time (e.g. `30s`, `10m`, `2h`, `1d` or `1h30m`)
- Warning in the action bar before it ends: at 30s, at 10s and every second from 5s
- The time keeps running while the player is offline
- Switching flight off with `/fly` ends it early
- Permission: `flys.fly.temp`

### ⚔️ Combat Rule *(off by default)*
- Flight is disabled when a player deals or takes PvP damage
- Flying again is blocked until the cooldown has passed
- Permission `flys.bypass.combat` skips the rule

### 🪂 Fall Protection *(off by default)*
- No fall damage after flight was switched off in mid-air
- Ends with the first prevented fall damage or as soon as the player lands

### 💾 Flight Restore *(off by default)*
- Players get their flight back when they rejoin
- Fly permission, world restrictions and combat rule still apply

### 🎚️ Speed Limits per Rank
- `flys.maxspeed.<1-10>` limits the flight speed of a player (the highest level wins)
- `general.flight-speed.max-speed` stays the limit for everyone
- Players without these permissions are not affected

### 🏷️ New Placeholders
- `%flys_speed_max%` - highest speed level the player can use
- `%flys_timed%`, `%flys_time_left%` and `%flys_time_left_seconds%` - timed flight
- `%flys_in_combat%` and `%flys_combat_time_left%` - combat state

###### *All new messages are available in 🇬🇧 EN, 🇩🇪 DE, 🇪🇸 ES, 🇫🇷 FR, 🇷🇺 RU and 🇵🇱 PL.*

---

## 🔧 Improvements

- **Automatic config update**: new options and messages are added to your `config.yml` and `messages_*.json` on startup. Your own values and edited texts stay as they are, a backup is saved as `.bak` file
- A broken `config.yml` or language file is never overwritten - the plugin keeps running and logs what to fix
- Tab completion for `/flyspeed` only suggests speeds the player may use

---

## 🐛 Bug Fixes

- Flight is no longer lost after dying and respawning
- `/flyspeed` above the allowed maximum now shows an error with the maximum instead of silently using a lower value
- A `max-speed` above `1.0` in the config no longer causes errors
- When `/fly <player>` is blocked for someone else (world or combat), the message now goes to the player who used the command

---

## 💡 For Server Admins

Just update and restart - no manual config changes needed. The new options are added under `extras` in your `config.yml`:

```yaml
extras:
  fall-protection: false
  restore-flight-on-join: false
  timed-flight-max-seconds: 604800
  combat:
    disable-flight: false
    cooldown-seconds: 10
```

| Permission | Description |
|------------|-------------|
| `flys.fly.temp` | Use `/fly <player> <time>` |
| `flys.bypass.combat` | Never blocked by the combat rule |
| `flys.maxspeed.<1-10>` | Limits the maximum flight speed to that level |

Perfect for:
- Rewards and events with timed flight
- PvP servers with the combat rule
- Different flight speeds per rank

---

## 🐛 Found Issues? 

If you find bugs or have suggestions, feel free to report them on the [GitHub Issue Tracker](https://github.com/Dotta4You/Flys/issues)! 

---

**Happy flying! 🚀**

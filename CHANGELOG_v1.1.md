# 🚀 Flys v1.1 - Multi-Language Update

> **Release Date:** November 2025  
> **Type:** Feature Update + Code Optimization

---

## ✨ What's New

### 🌍 **Multi-Language Support Extended**
- 🇪🇸 **Spanish (es)** - ¡Vuelo activado!
- 🇫🇷 **French (fr)** - Vol activé!
- 🇷🇺 **Russian (ru)** - Полёт включен!
- 🇵🇱 **Polish (pl)** - Latanie włączone!

**Total: 6 languages now supported!** (🇬🇧 EN, 🇩🇪 DE, 🇪🇸 ES, 🇫🇷 FR, 🇷🇺 RU, 🇵🇱 PL)

### 📊 **Analytics & Tracking**
- **NEW:** Language usage metric for bStats
- See which languages your community uses most!

### 📝 **Translation Notes**
- Community language files now include accuracy disclaimers
- Easy to customize and improve translations

---

## 🔧 Improvements

### ⚡ **Performance & Code Quality**
- ✅ **~70 lines of code reduced** - cleaner, faster, better!
- ✅ **DRY principles implemented** - no more code duplication
- ✅ **Optimized permission checks** - helper methods for cleaner code
- ✅ **Map-based language system** - smarter file management

### ⚙️ **Configuration Simplified**
- Removed redundant `general.flight-speed.enabled` option
- Flight speed now always applied automatically when enabling flight
- Cleaner config structure

---

## 🗑️ Removed

| Item | Reason |
|------|--------|
| `general.flight-speed.enabled` | Redundant - speed is always applied |
| `flight_speed_enabled` bStats metric | Config option no longer exists |
| `getLong()` internal method | Never used |
| Duplicate permission code | Replaced with helper methods |

---

## 🛠️ Technical Details

```diff
+ Map-based language file initialization
+ Helper methods for permission validation
+ Improved code structure and maintainability
+ Better error handling in language loading
```

**Code Quality:**
- ✅ Cleaner architecture
- ✅ Better scalability for future languages
- ✅ Reduced maintenance overhead

---

## ⚠️ Important Notes

### ✅ **Fully Backward Compatible**
- No breaking changes!
- Existing configurations work without modifications
- Flight speed behavior unchanged (just simplified)

### 📦 **Migration Guide**
No migration needed! Just update and restart your server.

If you had `general.flight-speed.enabled: false` in your config:
- Remove this line (it's ignored anyway)
- Flight speed will now always apply the default speed

---

## 🎯 Quick Facts

- **Languages:** 2 → 6 ✨
- **Code Lines:** -70 📉
- **Breaking Changes:** 0 ✅
- **New Features:** 5 🎉

---

**Made with ❤️ by Dötchen**  
[GitHub](https://github.com/Dotta4You/Flys) • [Modrinth](https://modrinth.com/plugin/flys) • [Discord](https://discord.gg/PfQTqhfjgA)


# <p align="center"> 🌀 SimpleGate 🌀</p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.8--1.21-brightgreen?style=for-the-badge&logo=minecraft" />
  <img src="https://img.shields.io/badge/Spigot-Compatible-orange?style=for-the-badge" />
  <img src="https://img.shields.io/badge/License-GPL%20v3-blue?style=for-the-badge" />
  <img src="https://img.shields.io/badge/ProtocolLib-Required-red?style=for-the-badge" />
</p>

<p align="center"><i>A lightweight, modern and feature-rich portal plugin for Minecraft servers.</i></p>

---

## ✨ Description

**SimpleGate** is a clean and efficient portal plugin that allows you to create immersive gates using blocks — no commands required. It focuses on simplicity, performance, and modern behavior without unnecessary complexity.

Perfect for survival, RPG, and custom servers.

---

## 🚀 Features

- 🔮 **Custom block-based portal gates** — build your frame, right-click and it's done
- 🎨 **Portal type selector GUI** — choose between Nether, End, Ice, Water, Sculk portals and more
- ✨ **Custom particle effects per portal type** — fully configurable including hex color support
- 🌈 **Hex color support** — use `&#RRGGBB` format in all messages and GUI titles
- ❄️ **Smart damage protection** — prevents fire, lava, freeze and drowning damage near portals
- 🔥 **Lava flow protection** — lava cannot flow into or near a portal
- 🏆 **No "The End?" achievement exploit** — uses ProtocolLib to send a fake `END_GATEWAY` packet, completely avoiding the unwanted advancement trigger
- 📝 **Fully customizable messages** — every player-facing message is editable in `config.yml`
- 🌍 **Multi-world support** with world blacklist
- 🔒 **Permission-based system**
- ⚡ **Optimized teleportation** — ejects passengers, clears fire ticks, chunk-aware
- 🧠 **Lightweight & performance-friendly** — chunk-indexed gate lookup
- 🛡️ **Exploit-tested** — hardened against portal physics, fire spread, entity portals and more
- 📦 **Supports Minecraft 1.8 — 1.21.x**

---

## 📦 Dependencies

| Dependency | Required | Link |
|---|---|---|
| ProtocolLib | ✅ Yes | [SpigotMC](https://www.spigotmc.org/resources/protocollib.1997/) |

---

## 📋 Commands

| Command | Description | Permission |
|---|---|---|
| `/simplegate reload` | Reloads the configuration | `simplegate.admin` |

**Aliases:** `/sgate`, `/sg`

---

## 🔑 Permissions

| Permission | Description | Default |
|---|---|---|
| `simplegate.admin` | Access to `/simplegate reload` | op |
| `simplegate.worldbypass` | Bypass world restrictions | op |
| `simplegate.framebypass` | Bypass frame material requirements | op |

---

## 🛠️ How to Create a Gate

1. Craft or obtain the configured **creation tool** (default: `COMPASS`)
2. **Rename it** in an anvil — the name will become the gate's network ID
3. **Right-click** any block of a valid frame (default frame material: `EMERALD_BLOCK`, 2 required)
4. A **portal type selector GUI** will open — choose your portal type
5. Done! Build a second gate with the **same name** to link them together

> Two gates with the same name and same owner will teleport players between each other.

---

## ⚙️ Configuration

```yaml
Settings:
  SoundTeleportEnabled: true
  PigmanPortalSpawnEnabled: true
  RemovingCreateToolName: true
  RemovingCreateToolItem: false
  ItemRequiredToCreatePortal: "COMPASS"
  MaxPortalSize: 200
  DisabledWorlds:
    - world_nether
  BlockRequiredToCreatePortal:
    Material: EMERALD_BLOCK
    Amount: 2
  GUISettings:
    InventoryType: HOPPER   # CHEST, HOPPER or DISPENSER
    Slots: 9                # Only used for CHEST type
    Title: "&5&lSelect Portal Type"
    FillEmptySlots: false
    PortalsTypeItem:
      1:
        Material: OBSIDIAN
        MaterialContent: NETHER_PORTAL
        Slot: 0
        Name: "&5Nether Portal"
        Lore:
          - "&7Click to apply."
        # Optional particle config:
        # ParticleEffect:
        #   Type: DUST
        #   Color: "#AA00FF"

Messages:
  prefix: "&8[&dSimpleGate&8] &r"
  # All messages are customizable here
```

> 💡 **Hex color support:** You can use `&#RRGGBB` format anywhere in messages and GUI titles on servers running 1.16+.

---

## 🖼️ Portal Types (Default)

| Name | Icon | Content |
|---|---|---|
| Nether Portal | Obsidian | NETHER_PORTAL |
| End Portal | End Portal Frame | END_GATEWAY (fake, via ProtocolLib) |
| Ice Portal | Powder Snow Bucket | POWDER_SNOW |
| Water Portal | Water Bucket | WATER |
| Sculk Portal | Sculk Vein | SCULK_VEIN |

---

## 💛 Donate

If you enjoy SimpleGate and want to support its development, consider leaving a donation. It helps a lot and motivates future updates!

> ☕ **[Buy me a coffee](#)** ← *(replace with your donation link)*

---

## 📜 License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)**.

This means:
- ✅ You can use, study and modify this code freely
- ✅ Any derivative work must also be GPL v3 and free
- ❌ You cannot sell this plugin or any fork of it

See the [LICENSE](LICENSE) file for full details.

---

## 🙏 Credits & Attribution

SimpleGate is inspired by and partially based on the work of:

- **[CreativeGatez](https://github.com/marcotama/CreativeGatez)** by [marcotama](https://github.com/marcotama) — the direct base this project was ported from (Kotlin → Java) with significant additions.
- **[CreativeGates](https://github.com/MassiveCraft/CreativeGates)** by [MassiveCraft](https://github.com/MassiveCraft) — the original inspiration behind the gate concept.

SimpleGate introduces the following original features not present in either of the above projects:
- Portal type selector GUI with configurable inventory type
- Custom particle effects system with hex color support
- Smart damage protection (fire, lava, freeze, drowning) near portals
- Fake `END_GATEWAY` packet via ProtocolLib (no advancement exploit)
- Full message customization via `config.yml`
- Multi-version support up to 1.21.x
- Chunk-indexed gate lookup for performance

---

<p align="center">Made with ❤️ by <b>xEliox</b></p>

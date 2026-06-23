# Infinity Item Editor Re

## English

**Infinity Item Editor Re** is a client-side creative item editor for multiple Minecraft versions:

- **Fabric 1.20.1**
- **Forge 1.20.1**
- **NeoForge 1.21.1**
- **NeoForge 1.21.4**
- **NeoForge 1.21.10**
- **NeoForge 1.21.11**

It is an independent remake and port inspired by the old Infinity Item Editor mod. It is not an official update from the original author.

### What It Does

Infinity Item Editor Re lets you edit held items, copy items from the world, save custom items, and use helper creative tabs for special or normally unavailable items.

Current features include:

- Open the item editor with the default `U` key, including hovered player-inventory slots inside container screens.
- Edit item ID, count, custom name, lore, NBT, JSON, hide flags, unbreakable state, and common display data.
- Switch between the legacy editor layout and the newer sidebar layout.
- Copy `/give` commands, drop edited items, save items to Infinity Realm, and pick items from registry, Realm, Void, or inventory sources.
- Edit enchantments, potion effects, custom attributes, colors, signs, player heads, armor stands, fireworks, containers, banners/shields, decorated pots, spawn eggs, spawners, trial spawners, villager trades, books, command blocks, and lore.
- Use Lore Painter to draw colored tooltip art with text characters.
- Copy targeted blocks, mobs, players, and armor stands with the default `V` key.
- Save custom items into Infinity Realm with the default `G` key.
- Copy hovered item `/give` commands with `Ctrl + C` and paste `/give` item stacks into creative player-inventory slots with `Ctrl + V`.
- Collect chat hover items and equipment-packet items into the Void cache.

### Creative Tabs

The mod adds searchable creative tabs:

- **Infinity - Realm**: player-saved custom items.
- **Infinity - Unavailable**: command blocks, barrier, structure blocks, spawners, potions, enchanted books, and other items not fully exposed by normal creative tabs.
- **Infinity - Banners**: banner, shield, and pattern variants.
- **Infinity - Heads**: player heads and MHF heads.
- **Infinity - Thief**: compatibility-style helper tab based on old mod behavior.
- **Infinity - Fireworks**: firework rockets, firework stars, and presets.
- **Infinity - Void**: item data collected from chat hover items and equipment packets.

### Requirements

Use the jar that matches your Minecraft version and loader.

- Fabric and Forge 1.20.1 builds require Java 17.
- NeoForge 1.21.x builds require Java 21.

This is mainly a client-side tool. Servers normally do not need to install it, but inventory-writing and entity-equipment-copying features require creative mode permissions and may be limited by server rules.

### Status

This is still a work-in-progress remake/port. Most common editor panels, creative tabs, JSON editing, command block editing, book page editing, decorated pot/trial spawner editing, and the main shortcut workflows are implemented. The Advanced NBT Browser can browse, expand, collapse, and scroll NBT structures, but it is not yet a full graphical NBT editor.

### Credits

This project is inspired by the original Infinity Item Editor:

- Original project: <https://github.com/Ruukas97/Infinity-Item-Editor>
- Original CurseForge page: <https://www.curseforge.com/minecraft/mc-mods/infinity-item-editor>
- Related 1.16.5 project: <https://github.com/Ruukas97/Creative-Editor/>

Thanks to the original author and contributors. Please respect the original project and license.

---

## 中文

**Infinity Item Editor Re** 是一个支持多个 Minecraft 版本的客户端创造物品编辑 Mod：

- **Fabric 1.20.1**
- **Forge 1.20.1**
- **NeoForge 1.21.1**
- **NeoForge 1.21.4**
- **NeoForge 1.21.10**
- **NeoForge 1.21.11**

它复刻并移植了旧版 Infinity Item Editor 的使用体验。本项目不是原作者的官方更新版，而是独立维护的新版适配/复刻项目。

### 它能做什么

Infinity Item Editor Re 可以编辑手中的物品、从世界中复制物品、保存自定义物品，并提供多个辅助创造模式标签页。

当前功能包括：

- 默认按 `U` 打开物品编辑器，也可以在容器界面编辑鼠标悬停的玩家物品栏槽位。
- 编辑物品 ID、数量、自定义名称、Lore、NBT、JSON、隐藏标签、无法破坏状态和常用显示数据。
- 支持旧版布局和新版侧边栏布局切换。
- 复制 `/give` 命令、丢出编辑后的物品、保存到 Infinity Realm，并可从注册物品、Realm、Void 或物品栏来源选择物品。
- 编辑附魔、药水效果、自定义属性、颜色、告示牌、玩家头颅、盔甲架、烟花、容器、旗帜/盾牌、饰纹陶罐、刷怪蛋、刷怪箱、试炼刷怪笼、村民交易、书本、命令方块和 Lore。
- 使用 Lore Painter 通过字符和颜色绘制物品提示文本图案。
- 默认按 `V` 复制准星指向的方块、生物、玩家或盔甲架。
- 默认按 `G` 将自定义物品保存到 Infinity Realm。
- 在容器界面中使用 `Ctrl + C` 复制鼠标悬停物品的 `/give` 命令，并可在创造模式下用 `Ctrl + V` 粘贴到玩家物品栏槽位。
- 自动收集聊天悬浮物品和装备数据包中的物品数据到 Void 缓存。

### 创造模式标签页

本 Mod 会添加多个带搜索栏的创造模式标签页：

- **Infinity - Realm**：玩家保存的自定义物品。
- **Infinity - Unavailable**：命令方块、屏障、结构方块、刷怪笼、药水、附魔书等常规创造物品栏中不直接提供或不完整提供的物品。
- **Infinity - Banners**：旗帜、盾牌和图案变体。
- **Infinity - Heads**：玩家头颅和 MHF 系列头颅。
- **Infinity - Thief**：基于旧版行为保留的兼容辅助标签页。
- **Infinity - Fireworks**：烟花火箭、烟火之星和预设变体。
- **Infinity - Void**：从聊天悬浮物品和装备包中收集到的物品缓存。

### 运行需求

请下载与 Minecraft 版本和加载器匹配的 jar。

- Fabric 和 Forge 1.20.1 版本使用 Java 17。
- NeoForge 1.21.x 版本使用 Java 21。

这是一个主要面向客户端的工具，服务端通常不需要安装。但写入玩家物品栏、复制实体装备等功能需要创造模式权限，也可能受到服务器规则限制。

### 当前状态

本项目仍处于复刻/移植开发阶段。大部分常用编辑面板、创造标签页、JSON 编辑器、命令方块编辑器、书本页面编辑器、饰纹陶罐/试炼刷怪笼编辑和主要快捷键工作流已经实现。高级 NBT 浏览器目前支持结构浏览、展开/折叠和滚动查看，但还不是完整的图形化 NBT 编辑器。

### 致谢

本项目受旧版 Infinity Item Editor 启发：

- 原项目：<https://github.com/Ruukas97/Infinity-Item-Editor>
- 原 CurseForge 页面：<https://www.curseforge.com/minecraft/mc-mods/infinity-item-editor>
- 相关 1.16.5 项目：<https://github.com/Ruukas97/Creative-Editor/>

感谢原作者和贡献者。请尊重原项目及其许可证。

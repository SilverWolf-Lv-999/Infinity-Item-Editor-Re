# Infinity Item Editor Re

Infinity Item Editor Re 是一个面向 **Minecraft Forge 1.20.1** 的客户端物品编辑 Mod，目标是复刻并移植旧版本的 Infinity Item Editor 体验。

这个仓库不是原作者的官方更新版，而是基于旧版功能思路重新适配到新版 Minecraft 的移植/复刻项目。当前仍处于开发阶段，部分面板和细节可能还会继续补全或调整。

## 基本信息

| 项目 | 内容 |
| --- | --- |
| Minecraft | 1.20.1 |
| Mod Loader | Forge 47.x |
| 开发环境 Forge | 47.4.20 |
| Java | 17 |
| Mod ID | `infinity_item_editor_re` |
| 当前版本 | `1.20.1-0.0.1` |
| 许可证 | GNU GPL 3.0 |

## 功能

- 手持物品按 `U` 打开物品编辑器。
- 支持编辑物品 ID、数量、自定义名称、NBT，并可复制 `/give` 命令。
- 支持隐藏标签、无法破坏、附魔、药水效果、自定义属性和颜色编辑。
- 支持告示牌、玩家头颅、盔甲架、烟花、容器、旗帜/盾牌、刷怪蛋、书本和 Lore 编辑。
- 支持 Lore Painter，用字符和颜色绘制物品描述。
- 提供多个创造模式标签页：
  - `Infinity - Realm`：保存的自定义物品。
  - `Infinity - Unavailable`：命令方块、屏障、结构方块等常规创造物品栏中不直接提供的物品。
  - `Infinity - Banners`：旗帜与盾牌编辑入口。
  - `Infinity - Heads`：MHF 系列头颅。
  - `Infinity - Thief`：兼容旧版功能入口。
  - `Infinity - Fireworks`：烟花与烟火之星。
  - `Infinity - Void`：从聊天悬浮物品和装备包中收集到的物品缓存。
- 默认快捷键：
  - `U`：编辑当前手持物品，或在容器界面编辑鼠标悬停的玩家物品栏槽位。
  - `V`：复制准星指向的方块、生物、玩家或盔甲架。
  - `G`：将当前物品或鼠标悬停物品保存到 Realm。
  - `Ctrl + C`：在容器界面复制鼠标悬停物品的 `/give` 命令。
  - `Ctrl + V`：在创造模式下把剪贴板中的 `/give` 物品粘贴到鼠标悬停的玩家物品栏槽位。

## 使用说明

1. 安装 Minecraft Forge 1.20.1。
2. 下载或自行构建本 Mod 的 jar。
3. 将 jar 放入客户端 `.minecraft/mods` 目录。
4. 启动游戏后进入创造模式，手持一个物品并按 `U` 打开编辑器。

这是客户端侧工具，服务端通常不需要安装。不过写入玩家物品栏、复制实体装备等操作依赖创造模式权限；在多人服务器使用时请遵守服务器规则。

## 数据位置

Mod 会在客户端游戏目录下创建数据目录：

- `.minecraft/infinity-data/realm.nbt`：Realm 标签页保存的物品。
- `.minecraft/infinity-data/void/`：Void 缓存数据。

如果检测到旧版的 `.minecraft/infinity-data/infinity.nbt`，会尝试迁移为 `realm.nbt`。

## 配置

Forge 客户端配置文件中可以开关部分标签页和行为，主要选项包括：

- `voidTab`
- `voidAddNotification`
- `voidTabHideHeads`
- `unavailableTab`
- `bannerTab`
- `headTab`
- `thiefTab`
- `fireworkTab`

配置文件由 Forge 生成，通常位于客户端 `config` 目录。

## 从源码构建

Windows:

```powershell
.\gradlew.bat build
```

Linux / macOS:

```bash
./gradlew build
```

构建完成后，jar 会输出到：

```text
build/libs/
```

开发环境运行客户端：

```powershell
.\gradlew.bat runClient
```

## 当前状态

本项目是旧版 Mod 的 1.20.1 复刻/移植版，不保证与旧版完全一致。当前已移植大部分常用编辑面板，但高级 NBT 浏览器等功能仍可能不完整。欢迎提交 Issue 或 Pull Request 帮助补全。

## 原项目与参考

- 旧版 Infinity Item Editor：<https://github.com/Ruukas97/Infinity-Item-Editor>
- CurseForge 页面：<https://www.curseforge.com/minecraft/mc-mods/infinity-item-editor>
- 1.16.5 相关版本 Creative Editor：<https://github.com/Ruukas97/Creative-Editor/>

感谢原作者的旧版项目。本仓库仅为新版适配与复刻维护，请优先尊重原项目的署名和许可证要求。

## 许可证

本项目使用 GNU GPL 3.0 许可证，详见 [LICENSE](LICENSE)。

# Infinity Item Editor Re

Infinity Item Editor Re 是一个面向 **Minecraft Fabric / Forge / NeoForge 多版本** 的客户端物品编辑 Mod，目标是复刻并移植旧版本的 Infinity Item Editor 体验。当前 `26.2` 工作区已拆分为 `common`、`fabric`、`neoforge` 三个 Gradle 模块，并最终构建为一个可同时被 Fabric 和 NeoForge 识别的通用 Jar。

这个仓库不是原作者的官方更新版，而是基于旧版功能思路重新适配到新版 Minecraft 的移植/复刻项目。当前仍处于开发阶段，部分面板和细节可能还会继续补全或调整。

## 基本信息

| 项目 | 内容 |
| --- | --- |
| 全部分支 | Forge 1.18.2 / 1.20.1；Fabric 1.20.1 / 26.1.2 / 26.2；NeoForge 1.21.1 / 1.21.4 / 1.21.10 / 1.21.11 / 26.1.2 / 26.2 |
| 当前工作区 | Minecraft 26.2，Fabric / NeoForge 多模块 |
| 当前工作区加载器 | Fabric Loader 0.19.3、Fabric API 0.155.2+26.2、NeoForge 26.2.0.11-beta |
| Java | 当前 26.2 工作区使用 Java 25 |
| Mod ID | `infinity_item_editor_re` |
| 当前工作区版本 | `26.2.0-1.2.0-B` |
| 许可证 | GNU GPL 3.0 |

## 功能

### 物品编辑器

- 手持物品按 `U` 打开编辑器；在容器界面中，也可以对鼠标悬停的玩家物品栏槽位打开编辑器。
- 支持旧版布局和新版侧边栏布局，可在编辑器内通过 `UI` 按钮切换并保存到配置。
- 支持编辑物品 ID、数量、耐久/附加值、自定义名称和 Lore。
- 物品数量输入已放宽到 32 位有符号整数范围；实际写入能力仍取决于当前游戏模式、加载器和服务器限制。
- 支持物品选择器，可以从注册物品、Realm、Void 等列表中选择物品写入当前编辑器。
- 支持保存到当前物品栏槽位、丢出物品、复制 `/give` 命令、保存到 Realm。
- 主编辑页会显示物品 tooltip 预览，以及左侧 NBT/JSON 结构预览。

### 数据编辑

- NBT 编辑：直接编辑物品 NBT，支持校验、应用和重置。
- 组件编辑：按显示、堆叠/耐久、战斗、工具、食物、装备等类别浏览数据组件，支持搜索、预设、布尔开关、数值输入、注册表选项、RGB 控件和原始组件值编辑。
- 高级 NBT 浏览器：支持树状结构浏览、展开/折叠和滚动查看。
- JSON 编辑器：支持物品 JSON 格式化、应用、语法高亮、基础键名补全和错误位置提示。
- 隐藏标签：支持切换附魔、自定义属性、无法破坏、CanDestroy、CanPlaceOn、物品信息、染色信息、升级信息等 HideFlags。
- 无法破坏：支持直接切换 `Unbreakable` 状态。

### 属性、附魔、药水和颜色

- 附魔编辑：支持搜索附魔、显示可用/全部附魔、添加匹配附魔、批量添加、移除和清空；条目较多时可按 Mod 命名空间分组。
- 药水编辑：支持搜索药水效果，设置等级、持续时间、粒子显示状态，并添加或移除自定义药水效果；条目较多时可按 Mod 命名空间分组。
- 自定义属性：支持选择属性、数值、运算方式、装备槽位和无限值模式；属性列表来自注册表并支持按 Mod 命名空间分组。
- 颜色编辑：支持皮革装备、药水、地图等颜色数据，提供十六进制输入、RGB 滑条、随机颜色和染料色板。

### 专用物品面板

- 告示牌：编辑四行文本和第一行点击命令。
- 玩家头颅：编辑所有者、UUID、纹理值和签名，并支持随机 UUID、清除所有者数据。
- 盔甲架：切换手臂、小型、隐形、底座、标记、重力、无敌等实体标签。
- 盔甲纹饰：从注册表选择材质和图案，实时预览盔甲架、玩家或僵尸模型，可应用当前纹饰、追加多层自定义纹饰、移除最后一层或全部清空。
- 烟花火箭/烟火之星：编辑飞行时间、爆炸形状、颜色、淡出颜色、闪烁、轨迹，支持添加、移除和清空爆炸数据。
- 容器物品：编辑箱子、木桶、潜影盒等容器内的 27 个槽位，支持槽内物品 NBT 更新、从玩家物品栏副本选取物品、清空单槽和清空全部。
- 收纳袋：编辑收纳袋条目，支持从玩家物品栏、Void 和 Realm 来源追加物品，更新或清空单个条目以及清空全部。
- 旗帜/盾牌：搜索并添加图案，编辑底色和图案色，支持旗帜/盾牌互转、移除最后一层和清空图案。
- 饰纹陶罐：搜索陶片，编辑四个方向的陶片图案，支持清空单面或全部图案。
- 刷怪蛋/刷怪箱/试炼刷怪笼：搜索并应用实体类型，支持同步刷怪蛋物品，编辑常用实体标签并清除 `EntityTag`、`SpawnData` 或试炼刷怪笼生成配置。
- 村民刷怪蛋交易：支持添加、移除、清空交易，编辑买入物品、第二买入物品、售出物品、使用次数、最大次数、经验、价格倍率、特殊价格和需求。
- 书本：编辑标题、作者、世代、解析状态，支持签名/取消签名、编辑页面内容和删除页面。
- 命令方块/命令方块矿车：编辑并应用内部执行命令，可切换条件模式和红石/常开激活模式。

### Lore 和文本工具

- Lore 编辑器：支持添加、删除、移动、复制 Lore、复制完整 tooltip 文本和粘贴 Lore。
- Lore Painter：支持用方块/阴影/空白字符和颜色绘制 tooltip 图案，支持调整画布行列、预览、插入到 Lore，并临时切换 GUI 缩放辅助绘制。
- 文本格式工具：支持插入 Minecraft 格式化前缀、清除格式，并提供颜色/样式按钮辅助编辑名称、Lore、告示牌等文本。

### 世界复制、物品栏和创造标签页

- 默认快捷键：
  - `U`：编辑当前手持物品，或在容器界面编辑鼠标悬停的玩家物品栏槽位。
  - `V`：复制准星指向的方块、生物、玩家或盔甲架。
  - `G`：将当前物品或鼠标悬停物品保存到 Realm。
  - `Ctrl + C`：在容器界面复制鼠标悬停物品的 `/give` 命令。
  - `Ctrl + V`：在创造模式下把剪贴板中的 `/give` 物品粘贴到鼠标悬停的玩家物品栏槽位。
- 复制方块时会生成对应物品；按住 `Ctrl` 复制方块可带上方块实体 NBT，并保存到 Realm。
- 复制生物、玩家或盔甲架时，会把目标装备复制到玩家对应装备槽位。
- 自动收集聊天悬浮物品和服务器装备包中的物品数据到 Void 缓存。
- 提供多个创造模式标签页；NeoForge 版本带搜索栏，Fabric 版本使用原版创造标签页 API：
  - `Infinity - Realm`：保存的自定义物品。
  - `Infinity - Unavailable`：命令方块、屏障、结构方块、刷怪笼、药水、附魔书等常规创造物品栏中不直接提供或不完整提供的物品。
  - `Infinity - Banners`：旗帜、盾牌和图案变体。
  - `Infinity - Heads`：玩家头颅和 MHF 系列头颅。
  - `Infinity - Thief`：聊天悬浮物品等兼容旧版行为的辅助入口。
  - `Infinity - Fireworks`：烟花火箭、烟火之星和预设变体。
  - `Infinity - Void`：从聊天悬浮物品和装备包中收集到的物品缓存。

## 使用说明

1. 安装目标 Minecraft 版本对应的 Fabric、Forge 或 NeoForge；当前 26.2 工作区支持 Fabric 和 NeoForge。
2. 当前 26.2 版本只需下载或构建同一个通用 jar；Fabric 和 NeoForge 均使用该文件。
3. 将 jar 放入客户端 `.minecraft/mods` 目录。
4. 启动游戏后进入创造模式，手持一个物品并按 `U` 打开编辑器。

Fabric 端必须同时安装 Fabric API；当前构建使用 Fabric API `0.155.2+26.2`。

这是客户端侧工具，服务端通常不需要安装。不过写入玩家物品栏、复制实体装备等操作依赖创造模式权限；在多人服务器使用时请遵守服务器规则。

## 数据位置

Mod 会在客户端游戏目录下创建数据目录：

- `.minecraft/infinity-data/realm.nbt`：Realm 标签页保存的物品。
- `.minecraft/infinity-data/void/`：Void 缓存数据。

如果检测到旧版的 `.minecraft/infinity-data/infinity.nbt`，会尝试迁移为 `realm.nbt`。

## 配置

客户端配置文件中可以开关部分标签页和行为。主要选项包括：

- `itemGuiMode`：物品编辑器界面模式，默认 `LEGACY` 旧界面；可设为 `SIDEBAR` 使用新版侧边栏，也可以在编辑器内点击 `UI` 按钮即时切换并保存。 编辑器会根据窗口可用空间自动等比例缩放布局，调整窗口大小后控件和鼠标区域会同步更新。
- `voidTab`
- `voidAddNotification`
- `voidTabHideHeads`
- `unavailableTab`
- `bannerTab`
- `headTab`
- `thiefTab`
- `fireworkTab`

当前工作区的配置文件为 `.minecraft/config/infinity_item_editor_re.properties`，Fabric 与 NeoForge 共用相同格式。

## 从源码构建

不同 Minecraft/Loader 版本请使用对应分支或源码目录构建。本工作区对应 Minecraft 26.2，使用 Java 25，可同时构建 Fabric 和 NeoForge 版本。

Windows:

```powershell
.\gradlew.bat build
```

Linux / macOS:

```bash
./gradlew build
```

构建完成后，最终分发的通用 jar 会输出到：

```text
build/libs/infinity_item_editor_re-26.2.0-1.2.0-B.jar
```

该 Jar 同时包含 `fabric.mod.json`、`META-INF/neoforge.mods.toml` 和两个平台入口类。Fabric 与 NeoForge 会分别读取自己的元数据并启动对应入口。`fabric/build/libs/` 与 `neoforge/build/libs/` 中的文件只是通用 Jar 的平台构建输入，不需要单独分发。

开发环境运行客户端：

```powershell
.\gradlew.bat :fabric:runClient
.\gradlew.bat :neoforge:runClient
```

项目模块：

```text
common/    平台无关的编辑器、数据和资源
fabric/    Fabric 加载器入口与事件注册
neoforge/  NeoForge 加载器入口与事件注册
build/     最终的 Fabric / NeoForge 通用 Jar
```

## 当前状态

本工作区面向 Minecraft 26.2，并从同一多模块项目构建 Fabric / NeoForge 通用 Jar。当前版本已经包含分类化数据组件编辑、按 Mod 分组的注册表内容、盔甲纹饰、独立收纳袋条目编辑器、饰纹陶罐、试炼刷怪笼、JSON/命令方块/书本编辑、创造标签页和主要快捷键工作流。高级 NBT 浏览器目前支持结构浏览、展开/折叠和滚动查看，但还不是完整的图形化 NBT 编辑器。

## 原项目与参考

- 旧版 Infinity Item Editor：<https://github.com/Ruukas97/Infinity-Item-Editor>
- CurseForge 页面：<https://www.curseforge.com/minecraft/mc-mods/infinity-item-editor>
- 1.16.5 相关版本 Creative Editor：<https://github.com/Ruukas97/Creative-Editor/>

感谢原作者的旧版项目。本仓库仅为新版适配与复刻维护，请优先尊重原项目的署名和许可证要求。

## 许可证

本项目使用 GNU GPL 3.0 许可证，详见 [LICENSE](LICENSE)。

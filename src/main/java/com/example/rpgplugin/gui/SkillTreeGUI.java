package com.example.rpgplugin.gui;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.gui.service.SkillTreeService;
import com.example.rpgplugin.gui.service.SkillTreeService.SkillAcquireResult;
import com.example.rpgplugin.gui.service.SkillTreeService.SkillRefundResult;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.SkillNode;
import com.example.rpgplugin.skill.SkillTree;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * スキル振り分けGUI
 * <p>
 * スキルポイントを消費してスキルを習得・強化するGUIを提供する。
 * YAMLから自動的にGUIを構成し、親子関係を表示する。
 * </p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: GUIの表示とイベント処理に専念</li>
 *   <li>ビジネスロジックはSkillTreeServiceに委譲</li>
 * </ul>
 */
public class SkillTreeGUI {

    private final RPGPlugin plugin;
    private final Player player;
    private final UUID playerUuid;
    private final String classId;
    private final SkillTree skillTree;
    private final SkillTreeService service;

    // GUIサイズ（行数 - 1行につき9スロット）
    private static final int INVENTORY_ROWS = 6;
    private static final int INVENTORY_SIZE = INVENTORY_ROWS * 9;

    // アイテム配置定数
    private static final int SKILL_DISPLAY_START = 9; // スキル表示開始位置（2行目から）
    private static final int SKILL_DISPLAY_END = 44;  // スキル表示終了位置（5行目まで）
    private static final int INFO_ITEM_SLOT = 49;     // 情報アイテム位置（最下行中央）

    // GUI識別用タイトル
    private static final Component INVENTORY_TITLE = Component.text("スキルツリー");
    private static final String INVENTORY_TITLE_PLAIN = "スキルツリー"; // 互換性のため残す

    // プレイヤーごとのGUIインスタンス管理
    private static final Map<UUID, SkillTreeGUI> openGuis = new HashMap<>();

    /**
     * コンストラクタ
     *
     * @param plugin  プラグインインスタンス
     * @param player  対象プレイヤー
     * @param classId クラスID
     */
    public SkillTreeGUI(RPGPlugin plugin, Player player, String classId) {
        this.plugin = plugin;
        this.player = player;
        this.playerUuid = player.getUniqueId();
        this.classId = classId != null ? classId : getDefaultClassId();
        this.service = new SkillTreeService(plugin);

        SkillManager skillManager = plugin.getSkillManager();
        this.skillTree = skillManager.getTreeRegistry().getTree(this.classId);

        if (this.skillTree == null) {
            player.sendMessage(Component.text("スキルツリーが見つかりません: " + this.classId, NamedTextColor.RED));
        }
    }

    /**
     * デフォルトのクラスIDを取得します
     *
     * @return デフォルトクラスID
     */
    private String getDefaultClassId() {
        // プレイヤーの現在のクラスを使用、なければ"warrior"
        RPGPlayer rpgPlayer = plugin.getPlayerManager().getRPGPlayer(player.getUniqueId());
        if (rpgPlayer != null && rpgPlayer.getClassId() != null) {
            return rpgPlayer.getClassId();
        }
        return "warrior";
    }

    /**
     * GUIを開きます
     */
    public void open() {
        if (skillTree == null) {
            player.sendMessage(Component.text("スキルツリーが利用できません", NamedTextColor.RED));
            return;
        }

        Inventory inventory = createInventory();
        player.openInventory(inventory);

        // GUIインスタンスを登録
        openGuis.put(playerUuid, this);
    }

    /**
     * インベントリを作成して初期化します
     *
     * @return 初期化済みインベントリ
     */
    private Inventory createInventory() {
        Inventory inventory = plugin.getServer().createInventory(
            null,
            INVENTORY_SIZE,
            INVENTORY_TITLE
        );

        // 装飾アイテムを設置
        setupDecoration(inventory);

        // スキルアイテムを配置
        setupSkillItems(inventory);

        // 情報アイテムを配置
        setupInfoItem(inventory);

        return inventory;
    }

    /**
     * 装飾アイテムを設定します
     *
     * @param inventory インベントリ
     */
    private void setupDecoration(Inventory inventory) {
        // 枠（ガラスパン）
        ItemStack borderItem = createItem(
            Material.LIGHT_GRAY_STAINED_GLASS_PANE,
            " ",
            null
        );

        // 上枠
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, borderItem);
        }
        // 下枠
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, borderItem);
        }

        // スキルポイント表示
        ItemStack skillPointItem = createSkillPointDisplay();
        inventory.setItem(4, skillPointItem);
    }

    /**
     * スキルポイント表示アイテムを作成します
     *
     * @return スキルポイント表示アイテム
     */
    private ItemStack createSkillPointDisplay() {
        int skillPoints = service.getAvailableSkillPoints(player, classId);

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("<yellow>残りスキルポイント: <green>" + skillPoints);
        lore.add("");
        lore.add("<gray>左クリック: スキル習得/レベルアップ");
        lore.add("<gray>右クリック: スキル解除/レベルダウン");

        return createItem(
            Material.ENCHANTED_BOOK,
            "<gold>スキルポイント",
            lore
        );
    }

    /**
     * スキルアイテムを配置します
     *
     * @param inventory インベントリ
     */
    private void setupSkillItems(Inventory inventory) {
        if (skillTree == null) {
            return;
        }

        List<SkillNode> allNodes = new ArrayList<>(skillTree.getAllNodes().values());

        // 親スキルがないものを先頭に、それ以外を親子順にソート
        allNodes.sort(new SkillNodeComparator(skillTree));

        int slot = SKILL_DISPLAY_START;
        for (SkillNode node : allNodes) {
            if (slot > SKILL_DISPLAY_END) {
                break; // 空きがない場合は終了
            }

            ItemStack skillItem = createSkillItem(node);
            inventory.setItem(slot, skillItem);

            slot++;
        }
    }

    /**
     * スキルアイテムを作成します
     *
     * @param node スキルノード
     * @return スキルアイテム
     */
    private ItemStack createSkillItem(SkillNode node) {
        Skill skill = node.getSkill();
        String skillId = skill.getId();
        int currentLevel = service.getSkillLevel(player, skillId);
        int maxLevel = skill.getMaxLevel();
        int cost = skillTree.getCost(skillId);

        Material iconMaterial = getIconMaterial(skill);
        String displayName = skill.getColoredDisplayName();

        List<String> lore = new ArrayList<>();

        // レベル表示
        if (currentLevel == 0) {
            lore.add("<gray>未習得");
            lore.add("");
        } else {
            lore.add("<green>レベル: <gold>" + currentLevel + "<gray> / " + maxLevel);
            lore.add("");
        }

        // コスト表示
        if (currentLevel < maxLevel) {
            lore.add("<yellow>習得コスト: <aqua>" + cost + " SP");
        } else {
            lore.add("<gray>最大レベルに達しています");
        }

        // 説明
        lore.add("");
        for (String line : skill.getDescription()) {
            lore.add("<white>" + line);
        }

        // 前提スキル表示
        if (!node.isRoot()) {
            String parentId = skillTree.getParentSkillId(skillId);
            if (parentId != null && !"none".equalsIgnoreCase(parentId)) {
                lore.add("");
                Skill parentSkill = plugin.getSkillManager().getSkill(parentId);
                String parentName = parentSkill != null ? parentSkill.getDisplayName() : parentId;

                int parentLevel = service.getSkillLevel(player, parentId);
                if (parentLevel > 0) {
                    lore.add("<green>前提: " + parentName + " (習得済み)");
                } else {
                    lore.add("<red>前提: " + parentName + " (未習得)");
                }
            }
        }

        // 習得可能状態
        lore.add("");
        if (service.canAcquireSkill(player, skillId, classId)) {
            lore.add("<green>▶ 習得可能");
        } else {
            lore.add("<red>✖ 習得条件を満たしていません");
        }

        return createItem(iconMaterial, displayName, lore);
    }

    /**
     * 情報アイテムを配置します
     *
     * @param inventory インベントリ
     */
    private void setupInfoItem(Inventory inventory) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("<yellow>現在のクラス: <white>" + classId);
        lore.add("");
        lore.add("<gray>クリックでGUIを閉じる");

        ItemStack infoItem = createItem(
            Material.BARRIER,
            "<red>閉じる",
            lore
        );

        inventory.setItem(INFO_ITEM_SLOT, infoItem);
    }

    /**
     * アイテムを作成します
     *
     * @param material   マテリアル
     * @param name       表示名
     * @param lore       説明文
     * @return アイテムスタック
     */
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(name));
            if (lore != null && !lore.isEmpty()) {
                List<Component> loreComponents = new ArrayList<>();
                for (String line : lore) {
                    loreComponents.add(MiniMessage.miniMessage().deserialize(line));
                }
                meta.lore(loreComponents);
            }
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * スキルのアイコンマテリアルを取得します
     *
     * @param skill スキル
     * @return マテリアル
     */
    private Material getIconMaterial(Skill skill) {
        String iconMaterialStr = skill.getIconMaterial();
        if (iconMaterialStr != null && !iconMaterialStr.isEmpty()) {
            Material iconMaterial = Material.matchMaterial(iconMaterialStr);
            if (iconMaterial != null) {
                return iconMaterial;
            }
        }

        // デフォルトアイコン（ターゲットが必要なスキルは武器、それ以外は本）
        if (skill.getSkillTarget() != null) {
            return Material.DIAMOND_SWORD;
        } else {
            return Material.ENCHANTED_BOOK;
        }
    }

    /**
     * スキルを習得します
     *
     * @param skillId スキルID
     * @return 成功した場合はtrue
     */
    public boolean acquireSkill(String skillId) {
        SkillAcquireResult result = service.acquireSkill(player, skillId, classId);
        result.sendMessageTo(player);
        if (result.isSuccess()) {
            refreshGUI();
        }
        return result.isSuccess();
    }

    /**
     * スキルを解除します
     *
     * @param skillId スキルID
     * @return 成功した場合はtrue
     */
    public boolean refundSkill(String skillId) {
        SkillRefundResult result = service.refundSkill(player, skillId, classId);
        result.sendMessageTo(player);
        if (result.isSuccess()) {
            refreshGUI();
        }
        return result.isSuccess();
    }

    /**
     * GUIをリフレッシュします
     */
    public void refreshGUI() {
        if (player.getOpenInventory() != null) {
            Inventory topInventory = player.getOpenInventory().getTopInventory();
            String title = PlainTextComponentSerializer.plainText().serialize(player.getOpenInventory().title());
            if (topInventory != null && INVENTORY_TITLE_PLAIN.equals(title)) {
                // インベントリを再構成
                topInventory.clear();
                setupDecoration(topInventory);
                setupSkillItems(topInventory);
                setupInfoItem(topInventory);
            }
        }
    }

    /**
     * プレイヤーのGUIを閉じます
     */
    public void close() {
        openGuis.remove(playerUuid);
    }

    /**
     * プレイヤーのGUIインスタンスを取得します
     *
     * @param player プレイヤー
     * @return GUIインスタンス（存在しない場合はnull）
     */
    public static SkillTreeGUI getOpenGUI(Player player) {
        return openGuis.get(player.getUniqueId());
    }

    /**
     * GUIがスキルスロットかチェックします
     *
     * @param slot スロット番号
     * @return スキルスロットの場合はtrue
     */
    public static boolean isSkillSlot(int slot) {
        return slot >= SKILL_DISPLAY_START && slot <= SKILL_DISPLAY_END;
    }

    /**
     * スロットからスキルIDを取得します
     *
     * @param slot スロット番号
     * @return スキルID（見つからない場合はnull）
     */
    public String getSkillIdFromSlot(int slot) {
        if (!isSkillSlot(slot) || skillTree == null) {
            return null;
        }

        List<SkillNode> allNodes = new ArrayList<>(skillTree.getAllNodes().values());
        allNodes.sort(new SkillNodeComparator(skillTree));

        int index = slot - SKILL_DISPLAY_START;
        if (index >= 0 && index < allNodes.size()) {
            return allNodes.get(index).getSkill().getId();
        }

        return null;
    }

    /**
     * スキルノードの比較クラス
     * <p>
     * 親子関係を考慮してソートを行う
     * </p>
     */
    private static class SkillNodeComparator implements Comparator<SkillNode> {
        private final SkillTree skillTree;

        public SkillNodeComparator(SkillTree skillTree) {
            this.skillTree = skillTree;
        }

        @Override
        public int compare(SkillNode n1, SkillNode n2) {
            // ルートノードを優先
            boolean n1Root = n1.isRoot();
            boolean n2Root = n2.isRoot();

            if (n1Root && !n2Root) {
                return -1;
            }
            if (!n1Root && n2Root) {
                return 1;
            }

            // 親子関係を考慮
            String id1 = n1.getSkill().getId();
            String id2 = n2.getSkill().getId();

            String parent1 = skillTree.getParentSkillId(id1);
            String parent2 = skillTree.getParentSkillId(id2);

            // n1がn2の親の場合
            if (id2.equals(parent1)) {
                return -1;
            }
            // n2がn1の親の場合
            if (id1.equals(parent2)) {
                return 1;
            }

            // スキルIDで辞書順
            return id1.compareTo(id2);
        }
    }
}

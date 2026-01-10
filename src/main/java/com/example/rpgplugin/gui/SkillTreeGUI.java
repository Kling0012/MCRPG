package com.example.rpgplugin.gui;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.SkillNode;
import com.example.rpgplugin.skill.SkillTree;
import org.bukkit.ChatColor;
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
 */
public class SkillTreeGUI {

    private final RPGPlugin plugin;
    private final Player player;
    private final UUID playerUuid;
    private final String classId;
    private final SkillTree skillTree;

    // GUIサイズ（行数 - 1行につき9スロット）
    private static final int INVENTORY_ROWS = 6;
    private static final int INVENTORY_SIZE = INVENTORY_ROWS * 9;

    // アイテム配置定数
    private static final int SKILL_DISPLAY_START = 9; // スキル表示開始位置（2行目から）
    private static final int SKILL_DISPLAY_END = 44;  // スキル表示終了位置（5行目まで）
    private static final int INFO_ITEM_SLOT = 49;     // 情報アイテム位置（最下行中央）

    // GUI識別用タイトル
    private static final String INVENTORY_TITLE = "スキルツリー";

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

        SkillManager skillManager = plugin.getSkillManager();
        this.skillTree = skillManager.getTree(this.classId);

        if (this.skillTree == null) {
            player.sendMessage(ChatColor.RED + "スキルツリーが見つかりません: " + this.classId);
        }
    }

    /**
     * デフォルトのクラスIDを取得します
     *
     * @return デフォルトクラスID
     */
    private String getDefaultClassId() {
        // プレイヤーの現在のクラスを使用、なければ"warrior"
        RPGPlayer rpgPlayer = plugin.getPlayerManager().getRPGPlayer(player);
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
            player.sendMessage(ChatColor.RED + "スキルツリーが利用できません");
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
        int skillPoints = getAvailableSkillPoints();

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.YELLOW + "残りスキルポイント: " + ChatColor.GREEN + skillPoints);
        lore.add("");
        lore.add(ChatColor.GRAY + "左クリック: スキル習得/レベルアップ");
        lore.add(ChatColor.GRAY + "右クリック: スキル解除/レベルダウン");

        return createItem(
            Material.ENCHANTED_BOOK,
            ChatColor.GOLD + "スキルポイント",
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

        List<SkillNode> allNodes = new ArrayList<>(skillTree.getAllNodes());

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
        int currentLevel = getCurrentSkillLevel(skillId);
        int maxLevel = skill.getMaxLevel();
        int cost = skillTree.getCost(skillId);

        Material iconMaterial = getIconMaterial(skill);
        String displayName = skill.getColoredDisplayName();

        List<String> lore = new ArrayList<>();

        // レベル表示
        if (currentLevel == 0) {
            lore.add(ChatColor.GRAY + "未習得");
            lore.add("");
        } else {
            lore.add(ChatColor.GREEN + "レベル: " + ChatColor.GOLD + currentLevel + ChatColor.GRAY + " / " + maxLevel);
            lore.add("");
        }

        // コスト表示
        if (currentLevel < maxLevel) {
            lore.add(ChatColor.YELLOW + "習得コスト: " + ChatColor.AQUA + cost + " SP");
        } else {
            lore.add(ChatColor.GRAY + "最大レベルに達しています");
        }

        // 説明
        lore.add("");
        lore.add(ChatColor.WHITE + skill.getDescription());

        // 前提スキル表示
        if (!node.isRoot()) {
            String parentId = skillTree.getParentSkillId(skillId);
            if (parentId != null && !"none".equalsIgnoreCase(parentId)) {
                lore.add("");
                Skill parentSkill = plugin.getSkillManager().getSkill(parentId);
                String parentName = parentSkill != null ? parentSkill.getDisplayName() : parentId;

                int parentLevel = getCurrentSkillLevel(parentId);
                if (parentLevel > 0) {
                    lore.add(ChatColor.GREEN + "前提: " + parentName + " (習得済み)");
                } else {
                    lore.add(ChatColor.RED + "前提: " + parentName + " (未習得)");
                }
            }
        }

        // 習得可能状態
        lore.add("");
        if (canAcquireSkill(node)) {
            lore.add(ChatColor.GREEN + "▶ 習得可能");
        } else {
            lore.add(ChatColor.RED + "✖ 習得条件を満たしていません");
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
        lore.add(ChatColor.YELLOW + "現在のクラス: " + ChatColor.WHITE + classId);
        lore.add("");
        lore.add(ChatColor.GRAY + "クリックでGUIを閉じる");

        ItemStack infoItem = createItem(
            Material.BARRIER,
            ChatColor.RED + "閉じる",
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
            meta.setDisplayName(name);
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
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
        Material iconMaterial = skill.getIconMaterial();
        if (iconMaterial != null) {
            return iconMaterial;
        }

        // デフォルトアイコン
        if (skill.isActive()) {
            return Material.DIAMOND_SWORD;
        } else {
            return Material.ENCHANTED_BOOK;
        }
    }

    /**
     * 現在のスキルレベルを取得します
     *
     * @param skillId スキルID
     * @return スキルレベル
     */
    private int getCurrentSkillLevel(String skillId) {
        SkillManager skillManager = plugin.getSkillManager();
        return skillManager.getSkillLevel(player, skillId);
    }

    /**
     * 利用可能なスキルポイントを取得します
     *
     * @return スキルポイント
     */
    private int getAvailableSkillPoints() {
        // TODO: プレイヤーデータにスキルポイントを追加する必要がある
        // 現状はプレイヤーレベルに基づいて計算
        int playerLevel = player.getLevel();
        SkillManager skillManager = plugin.getSkillManager();

        // 習得済みスキルの総コストを計算
        int usedPoints = 0;
        for (Skill skill : skillManager.getSkillsForClass(classId)) {
            int level = getCurrentSkillLevel(skill.getId());
            if (level > 0) {
                int cost = skillTree.getCost(skill.getId());
                usedPoints += cost * level;
            }
        }

        // 基礎スキルポイント（レベル * 1 + 5）
        int basePoints = playerLevel + 5;

        return Math.max(0, basePoints - usedPoints);
    }

    /**
     * スキルを習得可能かチェックします
     *
     * @param node スキルノード
     * @return 習得可能な場合はtrue
     */
    private boolean canAcquireSkill(SkillNode node) {
        Skill skill = node.getSkill();
        String skillId = skill.getId();
        int currentLevel = getCurrentSkillLevel(skillId);

        // 最大レベルチェック
        if (currentLevel >= skill.getMaxLevel()) {
            return false;
        }

        // 親スキルチェック
        if (!node.isRoot()) {
            String parentId = skillTree.getParentSkillId(skillId);
            if (parentId != null && !"none".equalsIgnoreCase(parentId)) {
                int parentLevel = getCurrentSkillLevel(parentId);
                if (parentLevel == 0) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * スキルを習得します
     *
     * @param skillId スキルID
     * @return 成功した場合はtrue
     */
    public boolean acquireSkill(String skillId) {
        SkillManager skillManager = plugin.getSkillManager();
        Skill skill = skillManager.getSkill(skillId);

        if (skill == null) {
            player.sendMessage(ChatColor.RED + "スキルが見つかりません: " + skillId);
            return false;
        }

        int currentLevel = getCurrentSkillLevel(skillId);
        int availablePoints = getAvailableSkillPoints();
        int cost = skillTree.getCost(skillId);

        // スキルポイントチェック
        if (availablePoints < cost) {
            player.sendMessage(ChatColor.RED + "スキルポイントが足りません（必要: " + cost + "、所持: " + availablePoints + "）");
            return false;
        }

        // 習得条件チェック
        SkillNode node = skillTree.getNode(skillId);
        if (node != null && !canAcquireSkill(node)) {
            player.sendMessage(ChatColor.RED + "習得条件を満たしていません");
            return false;
        }

        // 習得処理
        if (currentLevel == 0) {
            // 新規習得
            if (skillManager.acquireSkill(player, skillId, 1)) {
                player.sendMessage(ChatColor.GREEN + "スキルを習得しました: " + skill.getColoredDisplayName());
                refreshGUI();
                return true;
            }
        } else {
            // レベルアップ
            if (currentLevel >= skill.getMaxLevel()) {
                player.sendMessage(ChatColor.RED + "既に最大レベルに達しています");
                return false;
            }
            if (skillManager.upgradeSkill(player, skillId)) {
                refreshGUI();
                return true;
            }
        }

        return false;
    }

    /**
     * スキルを解除します
     *
     * @param skillId スキルID
     * @return 成功した場合はtrue
     */
    public boolean refundSkill(String skillId) {
        SkillManager skillManager = plugin.getSkillManager();
        Skill skill = skillManager.getSkill(skillId);

        if (skill == null) {
            player.sendMessage(ChatColor.RED + "スキルが見つかりません: " + skillId);
            return false;
        }

        int currentLevel = getCurrentSkillLevel(skillId);

        if (currentLevel == 0) {
            player.sendMessage(ChatColor.RED + "このスキルは習得していません");
            return false;
        }

        // 子スキルチェック
        SkillNode node = skillTree.getNode(skillId);
        if (node != null && !node.isLeaf()) {
            for (SkillNode child : node.getChildren()) {
                if (getCurrentSkillLevel(child.getSkill().getId()) > 0) {
                    player.sendMessage(ChatColor.RED + "子スキルを先に解除してください");
                    return false;
                }
            }
        }

        // 解除処理
        if (currentLevel == 1) {
            // 完全に削除
            skillManager.getPlayerSkillData(player).removeSkill(skillId);
            player.sendMessage(ChatColor.YELLOW + "スキルを解除しました: " + skill.getColoredDisplayName());
        } else {
            // レベルダウン
            skillManager.getPlayerSkillData(player).setSkillLevel(skillId, currentLevel - 1);
            player.sendMessage(ChatColor.YELLOW + "スキルレベルを下げました: " + skill.getColoredDisplayName() + " Lv." + (currentLevel - 1));
        }

        refreshGUI();
        return true;
    }

    /**
     * GUIをリフレッシュします
     */
    public void refreshGUI() {
        if (player.getOpenInventory() != null) {
            Inventory topInventory = player.getOpenInventory().getTopInventory();
            if (topInventory != null && INVENTORY_TITLE.equals(topInventory.getTitle())) {
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

        List<SkillNode> allNodes = new ArrayList<>(skillTree.getAllNodes());
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

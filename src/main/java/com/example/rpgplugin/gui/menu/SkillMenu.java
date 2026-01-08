package com.example.rpgplugin.gui.menu;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.SkillNode;
import com.example.rpgplugin.skill.SkillTree;
import com.example.rpgplugin.stats.StatManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * スキルツリーGUI
 *
 * <p>スキルツリーの表示とスキル習得操作を提供します。</p>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: GUIの表示と操作に専念</li>
 *   <li>DRY: GUIアイテム作成ロジックを再利用</li>
 *   <li>KISS: シンプルなGUI構造</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class SkillMenu {

    private final RPGPlugin plugin;
    private final Player player;
    private final RPGPlayer rpgPlayer;
    private final SkillManager skillManager;
    private final SkillTree skillTree;
    private final Inventory inventory;

    private static final int INVENTORY_SIZE = 54;
    private static final String INVENTORY_TITLE = "スキルツリー";

    private static final int INFO_ITEM_SLOT = 4;
    private static final int POINTS_ITEM_SLOT = 49;
    private static final int BACK_SLOT = 45;
    private static final int CLOSE_SLOT = 53;

    /** スロットとスキルのマッピング（クリック時のスキル特定に使用） */
    private final java.util.Map<Integer, Skill> slotToSkillMap = new java.util.HashMap<>();

    /** プレイヤーのクラスID（キャッシュ用） */
    private final String playerClassId;

    /** 自動更新有効フラグ */
    private final boolean autoRefreshEnabled;

    /**
     * コンストラクタ（Phase14: 自動更新対応版）
     *
     * <p>SkillTreeRegistryからスキルツリーを自動取得します。</p>
     *
     * @param plugin プラグインインスタンス
     * @param player プレイヤー
     * @param autoRefresh 自動更新を有効にするか
     */
    public SkillMenu(RPGPlugin plugin, Player player, boolean autoRefresh) {
        this.plugin = plugin;
        this.player = player;
        this.skillManager = plugin.getSkillManager();
        this.playerClassId = skillManager.getPlayerManager().getRPGPlayer(player.getUniqueId()).getClassId();
        this.skillTree = skillManager.getSkillTree(playerClassId);
        this.autoRefreshEnabled = autoRefresh;

        PlayerManager playerManager = plugin.getPlayerManager();
        this.rpgPlayer = playerManager.getRPGPlayer(player.getUniqueId());

        this.inventory = Bukkit.createInventory(null, INVENTORY_SIZE, INVENTORY_TITLE);

        // 自動更新リスナーを登録
        if (autoRefreshEnabled) {
            skillManager.getTreeRegistry().addListener(this::onTreeUpdated);
        }

        initializeItems();
    }

    /**
     * コンストラクタ（自動更新有効版）
     *
     * @param plugin プラグインインスタンス
     * @param player プレイヤー
     */
    public SkillMenu(RPGPlugin plugin, Player player) {
        this(plugin, player, true);
    }

    /**
     * スキルツリー更新時のコールバック
     *
     * @param classId 更新されたクラスID
     */
    private void onTreeUpdated(String classId) {
        if (classId.equals(playerClassId) && player.getOpenInventory().getTopInventory().equals(inventory)) {
            // GUIをリフレッシュ
            refreshGUI();
        }
    }

    /**
     * GUIアイテムを初期化します
     */
    private void initializeItems() {
        // 背景を埋める
        fillBackground();

        // 情報アイテム
        inventory.setItem(INFO_ITEM_SLOT, createInfoItem());

        // スキルポイント表示
        inventory.setItem(POINTS_ITEM_SLOT, createPointsDisplayItem());

        // スキルツリーを表示
        displaySkillTree();

        // 戻るボタン
        inventory.setItem(BACK_SLOT, createBackButton());

        // 閉じるボタン
        inventory.setItem(CLOSE_SLOT, createCloseButton());
    }

    /**
     * 背景を埋めます
     */
    private void fillBackground() {
        ItemStack backgroundItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = backgroundItem.getItemMeta();
        meta.setDisplayName(" ");
        backgroundItem.setItemMeta(meta);

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, backgroundItem);
            }
        }
    }

    /**
     * 情報アイテムを作成します
     *
     * @return 情報アイテム
     */
    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + "スキルツリー");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "クラス: " + ChatColor.WHITE + rpgPlayer.getClassId());
        lore.add("");
        lore.add(ChatColor.YELLOW + "クリックでスキルを習得・強化");
        lore.add(ChatColor.YELLOW + "右クリックで詳細表示");

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * スキルポイント表示アイテムを作成します
     *
     * @return スキルポイント表示アイテム
     */
    private ItemStack createPointsDisplayItem() {
        SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(player);
        int points = data.getSkillPoints();

        Material material = points > 0 ? Material.EMERALD : Material.REDSTONE;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + "スキルポイント: " + ChatColor.WHITE + points);

        List<String> lore = new ArrayList<>();
        if (points > 0) {
            lore.add(ChatColor.GREEN + "使用可能なポイントがあります");
        } else {
            lore.add(ChatColor.RED + "使用可能なポイントがありません");
            lore.add(ChatColor.GRAY + "レベルアップでポイントを獲得");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * スキルツリーを表示します
     */
    private void displaySkillTree() {
        int startSlot = 9;
        int currentSlot = startSlot;

        for (SkillNode node : skillTree.getRootNodes()) {
            displaySkillNode(node, currentSlot);
            currentSlot += 2;
        }
    }

    /**
 * スキルノードを表示します
 *
 * <p>スロットとスキルのマッピングを維持し、クリック時の識別を可能にします。</p>
 *
 * @param node スキルノード
 * @param slot スロット位置
 */
private void displaySkillNode(SkillNode node, int slot) {
    if (slot >= INVENTORY_SIZE || slot < 0) {
        return;
    }

    Skill skill = node.getSkill();
    SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(player);
    int level = data.getSkillLevel(skill.getId());

    boolean isAcquired = level > 0;
    boolean canAcquire = skillManager.hasSkill(player, skill.getId()) || checkCanAcquire(skill);

    ItemStack item = createSkillItem(skill, level, isAcquired, canAcquire);
    inventory.setItem(slot, item);

    // スロットとスキルのマッピングを保存
    slotToSkillMap.put(slot, skill);

    // 子ノードを表示
    int childSlot = slot + 9;
    for (SkillNode child : node.getChildren()) {
        displaySkillNode(child, childSlot);
        childSlot++;
    }
}

    /**
     * スキルアイテムを作成します
     *
     * @param skill スキル
     * @param level 習得レベル
     * @param isAcquired 習得済みか
     * @param canAcquire 習得可能か
     * @return スキルアイテム
     */
    private ItemStack createSkillItem(Skill skill, int level, boolean isAcquired, boolean canAcquire) {
        Material material = getSkillMaterial(skill, isAcquired, canAcquire);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        // 表示名
        String status = isAcquired ? ChatColor.GREEN + "[習得済み]" :
                        canAcquire ? ChatColor.YELLOW + "[習得可能]" :
                        ChatColor.RED + "[条件未達]";
        meta.setDisplayName(status + " " + skill.getColoredDisplayName() + ChatColor.GRAY + " Lv." + level);

        // 説明文
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "タイプ: " + skill.getType().getColoredName());

        if (skill.getCooldown() > 0) {
            lore.add(ChatColor.GRAY + "クールダウン: " + ChatColor.WHITE + skill.getCooldown() + "秒");
        }

        if (skill.getManaCost() > 0) {
            lore.add(ChatColor.GRAY + "消費MP: " + ChatColor.WHITE + skill.getManaCost());
        }

        lore.add("");

        // スキル説明
        for (String line : skill.getDescription()) {
            lore.add(ChatColor.WHITE + line);
        }

        lore.add("");

        if (isAcquired) {
            lore.add(ChatColor.GREEN + "左クリック: 強化 (" + (level + 1) + "/" + skill.getMaxLevel() + ")");
        } else if (canAcquire) {
            int cost = skillTree.getCost(skill.getId());
            lore.add(ChatColor.YELLOW + "左クリック: 習得 (コスト: " + cost + "ポイント)");
        } else {
            lore.add(ChatColor.RED + "習得条件を満たしていません");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * スキルの素材を取得します
     *
     * @param skill スキル
     * @param isAcquired 習得済みか
     * @param canAcquire 習得可能か
     * @return 素材
     */
    private Material getSkillMaterial(Skill skill, boolean isAcquired, boolean canAcquire) {
        if (isAcquired) {
            return Material.ENCHANTED_BOOK;
        } else if (canAcquire) {
            return Material.BOOK;
        } else {
            return Material.BARRIER;
        }
    }

    /**
     * 戻るボタンを作成します
     *
     * @return 戻るボタン
     */
    private ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "メニューに戻る");
        item.setItemMeta(meta);
        return item;
    }

    /**
     * 閉じるボタンを作成します
     *
     * @return 閉じるボタン
     */
    private ItemStack createCloseButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "閉じる");
        item.setItemMeta(meta);
        return item;
    }

    /**
 * 習得可能かチェックします
 *
 * <p>以下の条件をチェックします:</p>
 * <ul>
 *   <li>スキルポイントが十分にあるか</li>
 *   <li>前提スキルを習得しているか</li>
 *   <li>レベル要件を満たしているか</li>
 *   <li>クラス要件を満たしているか</li>
 * </ul>
 *
 * @param skill スキル
 * @return 習得可能な場合はtrue
 */
private boolean checkCanAcquire(Skill skill) {
    SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(player);
    
    // 1. スキルポイントチェック
    int cost = skillTree.getCost(skill.getId());
    if (data.getSkillPoints() < cost) {
        return false;
    }
    
    // 2. 前提スキルチェック
    Skill.SkillTreeConfig treeConfig = skill.getSkillTree();
    if (treeConfig != null) {
        String parentSkillId = treeConfig.getParent();
        if (parentSkillId != null && !"none".equalsIgnoreCase(parentSkillId)) {
            // 親スキルが習得されているかチェック
            if (!data.hasSkill(parentSkillId)) {
                return false;
            }
        }
        
        // 3. レベル要件・ステータス要件チェック
        int playerLevel = rpgPlayer.getLevel();
        java.util.Map<com.example.rpgplugin.stats.Stat, Double> stats = new java.util.HashMap<>();
        
        // 全ステータス値を取得
        for (com.example.rpgplugin.stats.Stat stat : com.example.rpgplugin.stats.Stat.values()) {
            stats.put(stat, (double) rpgPlayer.getFinalStat(stat));
        }
        
        // SkillTreeの習得要件チェック
        if (!skillTree.canAcquire(player, skill.getId(), playerLevel, stats)) {
            return false;
        }
    }
    
    // 4. クラス要件チェック
    List<String> availableClasses = skill.getAvailableClasses();
    if (!availableClasses.isEmpty() && !availableClasses.contains(rpgPlayer.getClassId())) {
        return false;
    }
    
    return true;
}

    /**
 * クリックイベントを処理します
 *
 * @param slot クリックされたスロット
 * @return 処理した場合はtrue
 */
public boolean handleClick(int slot) {
    // 閉じるボタン
    if (slot == CLOSE_SLOT) {
        player.closeInventory();
        return true;
    }

    // 戻るボタン
    if (slot == BACK_SLOT) {
        // ステータスメニューを開く
        StatManager statManager = plugin.getStatManager();
        PlayerManager playerManager = plugin.getPlayerManager();
        
        if (statManager != null && playerManager != null) {
            StatMenu statMenu = new StatMenu(player, statManager, playerManager);
            statMenu.open();
        }
        
        player.closeInventory();
        return true;
    }

    // スキルスロット
    ItemStack clickedItem = inventory.getItem(slot);
    if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
        String displayName = clickedItem.getItemMeta().getDisplayName();

        // スキルアイテムかチェック
        if (displayName.contains("[習得済み]") || displayName.contains("[習得可能]")) {
            return handleSkillClick(slot, clickedItem);
        }
    }

    return false;
}

    /**
 * スキルクリックを処理します
 *
 * <p>習得済みスキルの場合は強化、未習得スキルの場合は習得を試みます。</p>
 *
 * @param slot スロット
 * @param item アイテム
 * @return 処理した場合はtrue
 */
private boolean handleSkillClick(int slot, ItemStack item) {
    Skill skill = slotToSkillMap.get(slot);
    if (skill == null) {
        return false;
    }
    
    SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(player);
    int level = data.getSkillLevel(skill.getId());
    
    if (level > 0) {
        // 強化処理
        return upgradeSkill(skill);
    } else {
        // 習得処理
        return acquireSkill(skill);
    }
}

    /**
     * スキルを習得します
     *
     * @param skill 習得するスキル
     * @return 成功した場合はtrue
     */
    private boolean acquireSkill(Skill skill) {
        // 習得条件を再チェック
        if (!checkCanAcquire(skill)) {
            player.sendMessage(ChatColor.RED + "習得条件を満たしていません: " + skill.getColoredDisplayName());
            return true;
        }
        
        SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(player);
        int cost = skillTree.getCost(skill.getId());
        
        // スキルポイントを消費
        if (!data.useSkillPoint()) {
            player.sendMessage(ChatColor.RED + "スキルポイントが不足しています");
            return true;
        }
        
        // スキルを習得
        boolean success = skillManager.acquireSkill(player, skill.getId(), 1);
        if (success) {
            player.sendMessage(ChatColor.GREEN + "スキルを習得しました: " + skill.getColoredDisplayName() + ChatColor.GREEN + " (コスト: " + cost + "ポイント)");
        }
        
        return success;
    }

    /**
     * スキルを強化します
     *
     * @param skill 強化するスキル
     * @return 成功した場合はtrue
     */
    private boolean upgradeSkill(Skill skill) {
        SkillManager.PlayerSkillData data = skillManager.getPlayerSkillData(player);
        int currentLevel = data.getSkillLevel(skill.getId());
        
        // 最大レベルチェック
        if (currentLevel >= skill.getMaxLevel()) {
            player.sendMessage(ChatColor.RED + "既に最大レベルに達しています: " + skill.getColoredDisplayName());
            return true;
        }
        
        // 強化を実行
        boolean success = skillManager.upgradeSkill(player, skill.getId());
        
        return success;
    }

    /**
     * GUIをリフレッシュします
     */
    public void refreshGUI() {
        inventory.clear();
        initializeItems();
    }

    /**
     * インベントリを取得します
     *
     * @return インベントリ
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * プレイヤーを取得します
     *
     * @return プレイヤー
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * GUIを開きます
     */
    public void open() {
        player.openInventory(inventory);
    }
}

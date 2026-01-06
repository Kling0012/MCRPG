package com.example.rpgplugin.mythicmobs.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * MythicMobsドロップ設定管理クラス
 *
 * <p>YAMLファイルからモブのドロップ設定を読み込み、管理します。</p>
 *
 * <p>設定ファイル形式:</p>
 * <pre>
 * mobs:
 *   SkeletonKing:
 *     mob_id: "SkeletonKing"
 *     drops:
 *       - item: DIAMOND_SWORD
 *         amount: 1
 *         chance: 0.3
 *         exclusive: true
 *         expiration_minutes: 2
 * </pre>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
public class MobDropConfig {

    private final Logger logger;

    /**
     * ドロップアイテム設定
     */
    public static class DropItem {
        private final Material material;
        private final int amount;
        private final double chance;
        private final boolean exclusive;
        private final int expirationSeconds;

        /**
         * コンストラクタ
         *
         * @param material アイテムマテリアル
         * @param amount 数量
         * @param chance ドロップ率（0.0-1.0）
         * @param exclusive 独占ドロップかどうか
         * @param expirationSeconds 独占有効期限（秒）
         */
        public DropItem(Material material, int amount, double chance, boolean exclusive, int expirationSeconds) {
            this.material = material;
            this.amount = amount;
            this.chance = chance;
            this.exclusive = exclusive;
            this.expirationSeconds = expirationSeconds;
        }

        /**
         * アイテムマテリアルを取得します
         *
         * @return マテリアル
         */
        public Material getMaterial() {
            return material;
        }

        /**
         * 数量を取得します
         *
         * @return 数量
         */
        public int getAmount() {
            return amount;
        }

        /**
         * ドロップ率を取得します
         *
         * @return ドロップ率（0.0-1.0）
         */
        public double getChance() {
            return chance;
        }

        /**
         * 独占ドロップかどうかを取得します
         *
         * @return 独占ドロップの場合はtrue
         */
        public boolean isExclusive() {
            return exclusive;
        }

        /**
         * 独占有効期限（秒）を取得します
         *
         * @return 期限（秒）
         */
        public int getExpirationSeconds() {
            return expirationSeconds;
        }

        /**
         * ドロップ判定を行います
         *
         * @return ドロップする場合はtrue
         */
        public boolean shouldDrop() {
            return Math.random() < chance;
        }

        /**
         * ItemStackを作成します
         *
         * @return ItemStack
         */
        public ItemStack createItemStack() {
            return new ItemStack(material, amount);
        }
    }

    /**
     * モブドロップ設定
     */
    public static class MobDrop {
        private final String mobId;
        private final List<DropItem> drops;

        /**
         * コンストラクタ
         *
         * @param mobId モブID
         * @param drops ドロップアイテムリスト
         */
        public MobDrop(String mobId, List<DropItem> drops) {
            this.mobId = mobId;
            this.drops = drops;
        }

        /**
         * モブIDを取得します
         *
         * @return モブID
         */
        public String getMobId() {
            return mobId;
        }

        /**
         * ドロップアイテムリストを取得します
         *
         * @return ドロップアイテムリスト
         */
        public List<DropItem> getDrops() {
            return drops;
        }

        /**
         * ドロップ処理を実行し、ドロップしたアイテムのリストを返します
         *
         * @return ドロップしたアイテムリスト
         */
        public List<DropItem> processDrops() {
            List<DropItem> droppedItems = new ArrayList<>();

            for (DropItem drop : drops) {
                if (drop.shouldDrop()) {
                    droppedItems.add(drop);
                }
            }

            return droppedItems;
        }
    }

    /**
     * コンストラクタ
     *
     * @param logger ロガー
     */
    public MobDropConfig(Logger logger) {
        this.logger = logger;
    }

    /**
     * 設定セクションからモブドロップ設定を読み込みます
     *
     * @param section 設定セクション
     * @return モブドロップ設定のリスト
     */
    public List<MobDrop> loadFromConfig(ConfigurationSection section) {
        List<MobDrop> mobDrops = new ArrayList<>();

        if (section == null) {
            logger.warning("Mob drop configuration section is null");
            return mobDrops;
        }

        ConfigurationSection mobsSection = section.getConfigurationSection("mobs");
        if (mobsSection == null) {
            logger.warning("No 'mobs' section found in drop configuration");
            return mobDrops;
        }

        for (String mobKey : mobsSection.getKeys(false)) {
            try {
                ConfigurationSection mobSection = mobsSection.getConfigurationSection(mobKey);
                if (mobSection == null) {
                    continue;
                }

                String mobId = mobSection.getString("mob_id", mobKey);
                List<DropItem> drops = loadDrops(mobSection);

                if (!drops.isEmpty()) {
                    MobDrop mobDrop = new MobDrop(mobId, drops);
                    mobDrops.add(mobDrop);
                    logger.fine("Loaded drop config for mob: " + mobId + " (" + drops.size() + " drops)");
                }
            } catch (Exception e) {
                logger.warning("Failed to load drop config for mob: " + mobKey + " - " + e.getMessage());
            }
        }

        logger.info("Loaded " + mobDrops.size() + " mob drop configurations");
        return mobDrops;
    }

    /**
     * ドロップアイテム設定を読み込みます
     *
     * @param mobSection モブ設定セクション
     * @return ドロップアイテムリスト
     */
    private List<DropItem> loadDrops(ConfigurationSection mobSection) {
        List<DropItem> drops = new ArrayList<>();

        List<?> dropsList = mobSection.getList("drops");
        if (dropsList == null) {
            return drops;
        }

        for (Object dropObj : dropsList) {
            if (!(dropObj instanceof ConfigurationSection)) {
                continue;
            }

            ConfigurationSection dropSection = (ConfigurationSection) dropObj;

            try {
                String materialName = dropSection.getString("item");
                int amount = dropSection.getInt("amount", 1);
                double chance = dropSection.getDouble("chance", 1.0);
                boolean exclusive = dropSection.getBoolean("exclusive", false);
                int expirationMinutes = dropSection.getInt("expiration_minutes", 2);

                Material material = Material.matchMaterial(materialName);
                if (material == null) {
                    logger.warning("Invalid material: " + materialName);
                    continue;
                }

                int expirationSeconds = exclusive ? expirationMinutes * 60 : 0;
                DropItem dropItem = new DropItem(material, amount, chance, exclusive, expirationSeconds);
                drops.add(dropItem);

            } catch (Exception e) {
                logger.warning("Failed to load drop item: " + e.getMessage());
            }
        }

        return drops;
    }
}

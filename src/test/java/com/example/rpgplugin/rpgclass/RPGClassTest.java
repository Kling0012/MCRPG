package com.example.rpgplugin.rpgclass;

import com.example.rpgplugin.rpgclass.growth.StatGrowth;
import com.example.rpgplugin.rpgclass.requirements.ClassRequirement;
import org.bukkit.Material;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RPGClassのテストクラス
 *
 * <p>注意: 移行期間中、非推奨の {@code getAvailableSkills()} を
 * テストで意図的に使用します。</p>
 */
@SuppressWarnings("deprecation")
@DisplayName("RPGClass Tests")
class RPGClassTest {

    @Nested
    @DisplayName("Builder Pattern")
    class BuilderTests {

        @Test
        @DisplayName("最小限の設定でビルドできる")
        void build_MinimalConfig_Success() {
            RPGClass result = new RPGClass.Builder("warrior")
                    .build();

            assertEquals("warrior", result.getId());
            assertEquals("warrior", result.getName());
            assertEquals(1, result.getRank());
            assertEquals(50, result.getMaxLevel());
        }

        @Test
        @DisplayName("全てのフィールドを設定してビルドできる")
        void build_FullConfig_Success() {
            List<String> description = Arrays.asList("戦士クラス", "近接戦闘専門");
            StatGrowth growth = new StatGrowth.Builder()
                    .setManualPoints(5)
                    .build();

            RPGClass result = new RPGClass.Builder("warrior")
                    .setName("勇敢なる戦士")
                    .setDisplayName("勇者")
                    .setDescription(description)
                    .setRank(2)
                    .setMaxLevel(60)
                    .setIcon(Material.DIAMOND_SWORD)
                    .setStatGrowth(growth)
                    .setNextRankClassId("warrior_advanced")
                    .setManaRegen(1.5)
                    .build();

            assertEquals("warrior", result.getId());
            assertEquals("勇敢なる戦士", result.getName());
            assertEquals("勇者", result.getDisplayName());
            assertEquals(2, result.getRank());
            assertEquals(60, result.getMaxLevel());
            assertEquals(Material.DIAMOND_SWORD, result.getIcon());
            assertEquals(growth, result.getStatGrowth());
            assertTrue(result.getNextRankClassId().isPresent());
            assertEquals("warrior_advanced", result.getNextRankClassId().get());
            assertEquals(1.5, result.getManaRegen(), 0.01);
        }

        @Test
        @DisplayName("スキルを追加できる")
        void build_AddAvailableSkills_Success() {
            RPGClass result = new RPGClass.Builder("mage")
                    .addAvailableSkill("fireball")
                    .addAvailableSkill("ice_spell")
                    .build();

            assertEquals(2, result.getAvailableSkills().size());
            assertTrue(result.getAvailableSkills().contains("fireball"));
            assertTrue(result.getAvailableSkills().contains("ice_spell"));
        }

        @Test
        @DisplayName("次ランクの要件を追加できる")
        void build_AddNextRankRequirement_Success() {
            ClassRequirement mockReq = new ClassRequirement() {
                @Override
                public String getType() {
                    return "test";
                }

                @Override
                public String getDescription() {
                    return "Test requirement";
                }

                @Override
                public boolean check(org.bukkit.entity.Player player) {
                    return true;
                }
            };

            RPGClass result = new RPGClass.Builder("warrior")
                    .setNextRankClassId("warrior_advanced")
                    .addNextRankRequirement(mockReq)
                    .build();

            assertEquals(1, result.getNextRankRequirements().size());
        }

        @Test
        @DisplayName("分岐ランクを追加できる")
        void build_AddAlternativeRank_Success() {
            List<ClassRequirement> requirements1 = new ArrayList<>();
            List<ClassRequirement> requirements2 = new ArrayList<>();

            RPGClass result = new RPGClass.Builder("warrior")
                    .addAlternativeRank("berserker", requirements1)
                    .addAlternativeRank("guardian", requirements2)
                    .build();

            assertTrue(result.getAlternativeRanks().containsKey("berserker"));
            assertTrue(result.getAlternativeRanks().containsKey("guardian"));
        }

        @Test
        @DisplayName("パッシブボーナスを追加できる")
        void build_AddPassiveBonus_Success() {
            org.bukkit.configuration.ConfigurationSection section = createMockSection("stat_bonus", "10");

            RPGClass.PassiveBonus bonus = RPGClass.PassiveBonus.parse(section);

            RPGClass result = new RPGClass.Builder("warrior")
                    .addPassiveBonus(bonus)
                    .build();

            assertEquals(1, result.getPassiveBonuses().size());
        }

        @Test
        @DisplayName("経験値減衰を設定できる")
        void build_SetExpDiminish_Success() {
            org.bukkit.configuration.ConfigurationSection section = createMockDiminishSection("30", "0.5");

            RPGClass.ExpDiminish diminish = RPGClass.ExpDiminish.parse(section);

            RPGClass result = new RPGClass.Builder("mage")
                    .setExpDiminish(diminish)
                    .build();

            assertNotNull(result.getExpDiminish());
            assertEquals(30, result.getExpDiminish().getStartLevel());
            assertEquals(0.5, result.getExpDiminish().getReductionRate(), 0.01);
        }
    }

    @Nested
    @DisplayName("PassiveBonus")
    class PassiveBonusTests {

        @Test
        @DisplayName("typeとvalueをパースできる")
        void parse_BasicValues_Success() {
            org.bukkit.configuration.ConfigurationSection section = createMockSection("stat_bonus", "10");

            RPGClass.PassiveBonus result = RPGClass.PassiveBonus.parse(section);

            assertEquals("stat_bonus", result.getType());
            assertEquals(10.0, result.getValue(), 0.01);
        }

        @Test
        @DisplayName("valueが省略された場合はデフォルト値を使用")
        void parse_MissingValue_UsesDefault() {
            org.bukkit.configuration.ConfigurationSection section = createMockSection("custom_bonus", null);

            RPGClass.PassiveBonus result = RPGClass.PassiveBonus.parse(section);

            assertEquals("custom_bonus", result.getType());
            assertEquals(0.0, result.getValue(), 0.01);
        }

        @Test
        @DisplayName("typeが省略された場合はunknownを使用")
        void parse_MissingType_UsesDefault() {
            org.bukkit.configuration.file.YamlConfiguration config =
                    new org.bukkit.configuration.file.YamlConfiguration();
            config.set("value", 15);

            RPGClass.PassiveBonus result = RPGClass.PassiveBonus.parse(config);

            assertEquals("unknown", result.getType());
            assertEquals(15.0, result.getValue(), 0.01);
        }

        @Test
        @DisplayName("小数のvalueをパースできる")
        void parse_DecimalValue_Success() {
            org.bukkit.configuration.ConfigurationSection section = createMockSection("damage_reduction", "0.15");

            RPGClass.PassiveBonus result = RPGClass.PassiveBonus.parse(section);

            assertEquals("damage_reduction", result.getType());
            assertEquals(0.15, result.getValue(), 0.01);
        }

        @Test
        @DisplayName("Getterメソッドが正しく動作する")
        void passiveBonus_Getters_WorkCorrectly() {
            org.bukkit.configuration.ConfigurationSection section = createMockSection("mana_bonus", "50");

            RPGClass.PassiveBonus result = RPGClass.PassiveBonus.parse(section);

            assertEquals("mana_bonus", result.getType());
            assertEquals(50.0, result.getValue(), 0.01);
        }
    }

    @Nested
    @DisplayName("ExpDiminish")
    class ExpDiminishTests {

        @Test
        @DisplayName("start_levelとreduction_rateをパースできる")
        void parse_BasicValues_Success() {
            org.bukkit.configuration.ConfigurationSection section = createMockDiminishSection("25", "0.3");

            RPGClass.ExpDiminish result = RPGClass.ExpDiminish.parse(section);

            assertEquals(25, result.getStartLevel());
            assertEquals(0.3, result.getReductionRate(), 0.01);
        }

        @Test
        @DisplayName("start_levelが省略された場合はデフォルト値を使用")
        void parse_MissingStartLevel_UsesDefault() {
            org.bukkit.configuration.file.YamlConfiguration config =
                    new org.bukkit.configuration.file.YamlConfiguration();
            config.set("reduction_rate", "0.5");

            RPGClass.ExpDiminish result = RPGClass.ExpDiminish.parse(config);

            assertEquals(30, result.getStartLevel());
            assertEquals(0.5, result.getReductionRate(), 0.01);
        }

        @Test
        @DisplayName("reduction_rateが省略された場合はデフォルト値を使用")
        void parse_MissingReductionRate_UsesDefault() {
            org.bukkit.configuration.file.YamlConfiguration config =
                    new org.bukkit.configuration.file.YamlConfiguration();
            config.set("start_level", 20);

            RPGClass.ExpDiminish result = RPGClass.ExpDiminish.parse(config);

            assertEquals(20, result.getStartLevel());
            assertEquals(0.5, result.getReductionRate(), 0.01);
        }

        @Test
        @DisplayName("全て省略された場合はデフォルト値を使用")
        void parse_AllMissing_UsesDefaults() {
            org.bukkit.configuration.file.YamlConfiguration config =
                    new org.bukkit.configuration.file.YamlConfiguration();

            RPGClass.ExpDiminish result = RPGClass.ExpDiminish.parse(config);

            assertEquals(30, result.getStartLevel());
            assertEquals(0.5, result.getReductionRate(), 0.01);
        }

        @Test
        @DisplayName("applyExpで経験値減少を計算できる")
        void applyExp_CalculatesCorrectly() {
            org.bukkit.configuration.ConfigurationSection section = createMockDiminishSection("30", "0.5");

            RPGClass.ExpDiminish result = RPGClass.ExpDiminish.parse(section);

            // レベル30未満では減少なし
            assertEquals(100, result.applyExp(100, 25));

            // レベル30以上では50%減少
            assertEquals(50, result.applyExp(100, 30));
            assertEquals(50, result.applyExp(100, 35));
        }

        @Test
        @DisplayName("Getterメソッドが正しく動作する")
        void expDiminish_Getters_WorkCorrectly() {
            org.bukkit.configuration.ConfigurationSection section = createMockDiminishSection("40", "0.7");

            RPGClass.ExpDiminish result = RPGClass.ExpDiminish.parse(section);

            assertEquals(40, result.getStartLevel());
            assertEquals(0.7, result.getReductionRate(), 0.01);
        }
    }

    @Nested
    @DisplayName("Getter Methods")
    class GetterMethodTests {

        @Test
        @DisplayName("全てのGetterが正しく動作する")
        void getters_AllFields_WorkCorrectly() {
            List<String> description = Arrays.asList("説明文");
            StatGrowth growth = new StatGrowth.Builder().setManualPoints(3).build();

            RPGClass rpgClass = new RPGClass.Builder("test_class")
                    .setName("テストクラス")
                    .setDisplayName("テスト")
                    .setDescription(description)
                    .setRank(3)
                    .setMaxLevel(70)
                    .setIcon(Material.IRON_SWORD)
                    .setStatGrowth(growth)
                    .setNextRankClassId("test_advanced")
                    .setManaRegen(2.0)
                    .build();

            assertEquals("test_class", rpgClass.getId());
            assertEquals("テストクラス", rpgClass.getName());
            assertEquals("テスト", rpgClass.getDisplayName());
            assertEquals(description, rpgClass.getDescription());
            assertEquals(3, rpgClass.getRank());
            assertEquals(70, rpgClass.getMaxLevel());
            assertEquals(Material.IRON_SWORD, rpgClass.getIcon());
            assertEquals(growth, rpgClass.getStatGrowth());
            assertTrue(rpgClass.getNextRankClassId().isPresent());
            assertEquals("test_advanced", rpgClass.getNextRankClassId().get());
            assertEquals(2.0, rpgClass.getManaRegen(), 0.01);
        }

        @Test
        @DisplayName("デフォルト値が正しく設定される")
        void getters_DefaultValues_Success() {
            RPGClass rpgClass = new RPGClass.Builder("minimal")
                    .build();

            assertEquals("minimal", rpgClass.getId());
            assertEquals("minimal", rpgClass.getName());
            assertEquals("minimal", rpgClass.getDisplayName());
            assertEquals(1, rpgClass.getRank());
            assertEquals(50, rpgClass.getMaxLevel());
            assertEquals(Material.DIAMOND_SWORD, rpgClass.getIcon());
            assertEquals(1.0, rpgClass.getManaRegen(), 0.01);
            assertTrue(rpgClass.getAvailableSkills().isEmpty());
            assertTrue(rpgClass.getPassiveBonuses().isEmpty());
            assertTrue(rpgClass.getAlternativeRanks().isEmpty());
            assertTrue(rpgClass.getNextRankRequirements().isEmpty());
        }

        @Test
        @DisplayName("hasNextRankが正しく動作する")
        void hasNextRank_WorksCorrectly() {
            RPGClass withNextRank = new RPGClass.Builder("test1")
                    .setNextRankClassId("test_advanced")
                    .build();
            assertTrue(withNextRank.hasNextRank());

            RPGClass withoutNextRank = new RPGClass.Builder("test2")
                    .build();
            assertFalse(withoutNextRank.hasNextRank());
        }

        @Test
        @DisplayName("hasAlternativeRanksが正しく動作する")
        void hasAlternativeRanks_WorksCorrectly() {
            RPGClass withAlternatives = new RPGClass.Builder("test1")
                    .addAlternativeRank("alt1", new ArrayList<>())
                    .build();
            assertTrue(withAlternatives.hasAlternativeRanks());

            RPGClass withoutAlternatives = new RPGClass.Builder("test2")
                    .build();
            assertFalse(withoutAlternatives.hasAlternativeRanks());
        }
    }

    @Nested
    @DisplayName("Collection Methods")
    class CollectionMethodTests {

        @Test
        @DisplayName("availableSkillsのModifiableを返す")
        void getAvailableSkills_ReturnsModifiableList() {
            RPGClass rpgClass = new RPGClass.Builder("test")
                    .addAvailableSkill("skill1")
                    .build();

            List<String> skills = rpgClass.getAvailableSkills();
            assertEquals(1, skills.size());

            // 返されるリストは新しいArrayListなので、変更しても元のオブジェクトには影響しない
            skills.add("skill2");
            assertEquals(2, skills.size()); // 取得したリスト自体は変更可能
            assertEquals(1, rpgClass.getAvailableSkills().size()); // 元のリストは変更なし
        }

        @Test
        @DisplayName("passiveBonusesのModifiableを返す")
        void getPassiveBonuses_ReturnsModifiableList() {
            org.bukkit.configuration.ConfigurationSection section = createMockSection("stat_bonus", "5");
            RPGClass.PassiveBonus bonus = RPGClass.PassiveBonus.parse(section);

            RPGClass rpgClass = new RPGClass.Builder("test")
                    .addPassiveBonus(bonus)
                    .build();

            List<RPGClass.PassiveBonus> bonuses = rpgClass.getPassiveBonuses();
            assertEquals(1, bonuses.size());
        }

        @Test
        @DisplayName("alternativeRanksのModifiableを返す")
        void getAlternativeRanks_ReturnsModifiableMap() {
            RPGClass rpgClass = new RPGClass.Builder("test")
                    .addAlternativeRank("alt1", new ArrayList<>())
                    .build();

            Map<String, List<ClassRequirement>> alternatives = rpgClass.getAlternativeRanks();
            assertEquals(1, alternatives.size());
        }
    }

    // ===== ヘルパーメソッド =====

    private org.bukkit.configuration.ConfigurationSection createMockSection(
            String type, String value) {
        org.bukkit.configuration.file.YamlConfiguration config =
                new org.bukkit.configuration.file.YamlConfiguration();
        config.set("type", type);
        if (value != null) {
            // 文字列を数値として変換
            try {
                config.set("value", Double.parseDouble(value));
            } catch (NumberFormatException e) {
                config.set("value", value);
            }
        }
        return config;
    }

    private org.bukkit.configuration.ConfigurationSection createMockDiminishSection(
            String startLevel, String reductionRate) {
        org.bukkit.configuration.file.YamlConfiguration config =
                new org.bukkit.configuration.file.YamlConfiguration();
        if (startLevel != null) {
            config.set("start_level", Integer.parseInt(startLevel));
        }
        if (reductionRate != null) {
            config.set("reduction_rate", Double.parseDouble(reductionRate));
        }
        return config;
    }
}

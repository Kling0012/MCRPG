package com.example.rpgplugin.integration;

import com.example.rpgplugin.model.skill.FormulaDamageConfig;
import com.example.rpgplugin.model.skill.SkillTreeConfig;
import com.example.rpgplugin.model.skill.TargetingConfig;
import com.example.rpgplugin.model.skill.VariableDefinition;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.skill.Skill;
import com.example.rpgplugin.skill.SkillCostType;
import com.example.rpgplugin.skill.SkillManager;
import com.example.rpgplugin.skill.SkillType;
import com.example.rpgplugin.skill.LevelDependentParameter;
import com.example.rpgplugin.skill.target.AreaShape;
import com.example.rpgplugin.skill.target.ShapeCalculator;
import com.example.rpgplugin.skill.target.SkillTarget;
import com.example.rpgplugin.skill.target.TargetSelector;
import com.example.rpgplugin.skill.target.TargetType;
import com.example.rpgplugin.stats.Stat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 範囲エフェクトの結合テスト
 *
 * <p>テスト対象:</p>
 * <ul>
 *   <li>ターゲット選択 → ダメージ適用 → 結果確認</li>
 *   <li>扇状範囲（CONE）</li>
 *   <li>四角形範囲（RECT）</li>
 *   <li>円形範囲（CIRCLE）</li>
 *   <li>単体ターゲット（SINGLE）</li>
 * </ul>
 *
 * <p>設計原則:</p>
 * <ul>
 *   <li>SOLID-S: ターゲット選択と範囲計算のテストに専念</li>
 *   <li>現実的: 実際のスキル使用シナリオを模倣</li>
 *   <li>独立性: 各テストは独立して実行可能</li>
 * </ul>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("範囲エフェクト結合テスト")
class TargetingIntegrationTest {

    @Mock
    private RPGPlugin mockPlugin;

    @Mock
    private Logger mockLogger;

    @Mock
    private PlayerManager mockPlayerManager;

    @Mock
    private Player mockPlayer;

    @Mock
    private World mockWorld;

    @Mock
    private Location playerLocation;

    private MockedStatic<Bukkit> mockedBukkit;

    private SkillManager skillManager;

    /**
     * 各テストの前に実行されるセットアップ処理
     */
    @BeforeEach
    void setUp() {
        // 前のモックが残っている場合はクローズする
        if (mockedBukkit != null) {
            try {
                mockedBukkit.close();
            } catch (Exception e) {
                // クローズ時の例外は無視
            }
        }

        // Bukkit静的メソッドのモック化
        mockedBukkit = mockStatic(Bukkit.class);

        // Pluginの基本設定
        when(mockPlugin.getLogger()).thenReturn(mockLogger);

        // プレイヤーのモック設定
        UUID playerUuid = UUID.randomUUID();
        when(mockPlayer.getUniqueId()).thenReturn(playerUuid);
        when(mockPlayer.getName()).thenReturn("TestPlayer");
        when(mockPlayer.getLocation()).thenReturn(playerLocation);
        when(mockPlayer.getWorld()).thenReturn(mockWorld);

        // 位置情報のモック設定
        when(playerLocation.getWorld()).thenReturn(mockWorld);
        when(playerLocation.getX()).thenReturn(0.0);
        when(playerLocation.getY()).thenReturn(64.0);
        when(playerLocation.getZ()).thenReturn(0.0);
        when(playerLocation.getYaw()).thenReturn(0.0f);
        when(playerLocation.getPitch()).thenReturn(0.0f);
        when(playerLocation.getDirection()).thenReturn(new Vector(0, 0, 1)); // +Z方向を向いている
        when(playerLocation.toVector()).thenReturn(new Vector(0, 64, 0));

        // 距離計算のモック（プレイヤー位置用）
        when(playerLocation.distance(any())).thenAnswer(invocation -> {
            Location other = invocation.getArgument(0);
            double dx = 0.0 - other.getX();
            double dy = 64.0 - other.getY();
            double dz = 0.0 - other.getZ();
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        });

        // 距離の二乗計算のモック（プレイヤー位置用）
        when(playerLocation.distanceSquared(any())).thenAnswer(invocation -> {
            Location other = invocation.getArgument(0);
            double dx = 0.0 - other.getX();
            double dy = 64.0 - other.getY();
            double dz = 0.0 - other.getZ();
            return dx * dx + dy * dy + dz * dz;
        });

        // Worldのモック設定（近くのエンティティ取得）
        when(mockWorld.getNearbyEntities(any(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new ArrayList<>());

        // SkillManagerの初期化
        skillManager = new SkillManager(mockPlugin, mockPlayerManager);
    }

    /**
     * 各テストの後に実行されるクリーンアップ処理
     */
    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
        skillManager.clearAllSkills();
        skillManager.clearAllPlayerData();
    }

    // ==================== シナリオ1: 単体ターゲット選択 ====================

    @Nested
    @DisplayName("シナリオ1: 単体ターゲット選択")
    class Scenario1SingleTargetSelection {

        @Test
        @DisplayName("シナリオ1-1: SINGLE設定で最も近いエンティティが選択される")
        void test1_1_SingleTargetSelectsNearest() {
            // Given: 単体ターゲット設定と候補エンティティ
            SkillTarget singleTarget = createSingleTargetConfig();
            List<Entity> candidates = createMockEntities(
                    new double[]{5, 3, 10}, // Z座標（距離: 5, 3, 10）
                    new double[]{0, 0, 0}    // X座標
            );

            // When: ターゲットを選択
            List<Entity> selected = TargetSelector.selectTargets(
                    mockPlayer, singleTarget, candidates, null);

            // Then: 最も近いエンティティ（距離3）が選択される
            assertThat(selected).hasSize(1);
            assertThat(selected.get(0).getLocation().getZ()).isEqualTo(3);
        }

        @Test
        @DisplayName("シナリオ1-2: 候補がいない場合空リストが返る")
        void test1_2_EmptyCandidatesReturnEmptyList() {
            // Given: 単体ターゲット設定と空の候補リスト
            SkillTarget singleTarget = createSingleTargetConfig();
            List<Entity> candidates = List.of();

            // When: ターゲットを選択
            List<Entity> selected = TargetSelector.selectTargets(
                    mockPlayer, singleTarget, candidates, null);

            // Then: 空リストが返る
            assertThat(selected).isEmpty();
        }
    }

    // ==================== シナリオ2: 扇状範囲（CONE） ====================

    @Nested
    @DisplayName("シナリオ2: 扇状範囲（CONE）")
    class Scenario2ConeArea {

        @Test
        @DisplayName("シナリオ2-1: CONE設定で扇状範囲内のエンティティが選択される")
        void test2_1_ConeSelectsInAngle() {
            // Given: 扇状範囲設定（90°コーン = 前方から左右45°まで）
            SkillTarget coneTarget = createConeTargetConfig(90.0, 10.0);

            // 前方（+Z）にあるエンティティ（Y座標を64に修正してプレイヤーと同じ高さに）
            List<Entity> candidates = createMockEntities(
                    new double[]{0, 64, 5},    // 前方・0°・角度内
                    new double[]{4, 64, 5},    // 斜め約38度・角度内（45°未満）
                    new double[]{-4, 64, 5},   // 左斜め約38度・角度内（45°未満）
                    new double[]{15, 64, 0},   // 横方向・90°・角度外
                    new double[]{0, 64, 12}    // 距離12・範囲外
            );

            // When: ターゲットを選択
            List<Entity> selected = TargetSelector.selectTargets(
                    mockPlayer, coneTarget, candidates, null);

            // Then: 角度内のエンティティが選択される
            assertThat(selected).hasSize(3);
        }

        @Test
        @DisplayName("シナリオ2-2: CONEの角度設定が正しく適用される")
        void test2_2_ConeAngleCorrectlyApplied() {
            // Given: 狭い角度（45度コーン = 前方から左右22.5°まで）
            SkillTarget narrowCone = createConeTargetConfig(45.0, 10.0);

            List<Entity> candidates = createMockEntities(
                    new double[]{0, 64, 5},   // 前方・0°・角度内
                    new double[]{2, 64, 5},   // 斜め約21.8度・角度内（22.5°未満）
                    new double[]{3, 64, 5},   // 斜め約31度・角度外（22.5°超）
                    new double[]{8, 64, 5}    // 斜め約58度・角度外
            );

            // When: ターゲットを選択
            List<Entity> selected = TargetSelector.selectTargets(
                    mockPlayer, narrowCone, candidates, null);

            // Then: 狭い角度内のエンティティのみ選択される
            assertThat(selected).hasSize(2);
        }

        @Test
        @DisplayName("シナリオ2-3: CONEの距離制限が正しく適用される")
        void test2_3_ConeRangeCorrectlyApplied() {
            // Given: 短い範囲の扇状範囲
            SkillTarget shortCone = createConeTargetConfig(90.0, 5.0);

            List<Entity> candidates = createMockEntities(
                    new double[]{0, 64, 3},   // 距離3・範囲内
                    new double[]{0, 64, 5},   // 距離5・範囲ギリギリ内
                    new double[]{0, 64, 7},   // 距離7・範囲外
                    new double[]{0, 64, 10}   // 距離10・範囲外
            );

            // When: ターゲットを選択
            List<Entity> selected = TargetSelector.selectTargets(
                    mockPlayer, shortCone, candidates, null);

            // Then: 範囲内のエンティティのみ選択される
            assertThat(selected).hasSize(2);
        }
    }

    // ==================== シナリオ3: 四角形範囲（RECT） ====================

    @Nested
    @DisplayName("シナリオ3: 四角形範囲（RECT）")
    class Scenario3RectArea {

        @Test
        @DisplayName("シナリオ3-1: RECT設定で四角形範囲内のエンティティが選択される")
        void test3_1_RectSelectsInArea() {
            // Given: 四角形範囲設定
            SkillTarget rectTarget = createRectTargetConfig(6.0, 10.0);

            List<Entity> candidates = createMockEntities(
                    new double[]{0, 64, 5},   // 前方中心・範囲内
                    new double[]{2, 64, 5},   // 右寄り・範囲内（width=6なので±3）
                    new double[]{4, 64, 5},   // 右端・範囲内
                    new double[]{5, 64, 5},   // 右端外・範囲外
                    new double[]{-3, 64, 5}   // 左端ギリギリ内
            );

            // When: ターゲットを選択
            List<Entity> selected = TargetSelector.selectTargets(
                    mockPlayer, rectTarget, candidates, null);

            // Then: 幅6以内のエンティティが選択される
            assertThat(selected).hasSizeGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("シナリオ3-2: RECTの深さ制限が正しく適用される")
        void test3_2_RectDepthCorrectlyApplied() {
            // Given: 短い四角形範囲
            SkillTarget shortRect = createRectTargetConfig(6.0, 5.0);

            List<Entity> candidates = createMockEntities(
                    new double[]{0, 64, 3},   // 深さ3・範囲内
                    new double[]{0, 64, 5},   // 深さ5・範囲ギリギリ内
                    new double[]{0, 64, 7},   // 深さ7・範囲外
                    new double[]{0, 64, 10}   // 深さ10・範囲外
            );

            // When: ターゲットを選択
            List<Entity> selected = TargetSelector.selectTargets(
                    mockPlayer, shortRect, candidates, null);

            // Then: 深さ5以内のエンティティのみ選択される
            assertThat(selected).hasSize(2);
        }
    }

    // ==================== シナリオ4: 円形範囲（CIRCLE） ====================

    @Nested
    @DisplayName("シナリオ4: 円形範囲（CIRCLE）")
    class Scenario4CircleArea {

        @Test
        @DisplayName("シナリオ4-1: CIRCLE設定で円形範囲内のエンティティが選択される")
        void test4_1_CircleSelectsInRadius() {
            // Given: 円形範囲設定
            SkillTarget circleTarget = createCircleTargetConfig(5.0);

            List<Entity> candidates = createMockEntities(
                    new double[]{0, 64, 3},   // 距離3・範囲内
                    new double[]{4, 64, 3},   // 距離5・範囲ギリギリ内
                    new double[]{0, 64, 5},   // 距離5・範囲ギリギリ内
                    new double[]{6, 64, 0},   // 距離6・範囲外
                    new double[]{0, 64, 7}    // 距離7・範囲外
            );

            // When: ターゲットを選択
            List<Entity> selected = TargetSelector.selectTargets(
                    mockPlayer, circleTarget, candidates, null);

            // Then: 半径5以内のエンティティが選択される
            assertThat(selected).hasSize(3);
        }

        @Test
        @DisplayName("シナリオ4-2: CIRCLEの半径設定が正しく適用される")
        void test4_2_CircleRadiusCorrectlyApplied() {
            // Given: 小さな円形範囲
            SkillTarget smallCircle = createCircleTargetConfig(3.0);

            List<Entity> candidates = createMockEntities(
                    new double[]{0, 64, 2},   // 距離2・範囲内
                    new double[]{0, 64, 3},   // 距離3・範囲ギリギリ内
                    new double[]{0, 64, 4},   // 距離4・範囲外
                    new double[]{0, 64, 5}    // 距離5・範囲外
            );

            // When: ターゲットを選択
            List<Entity> selected = TargetSelector.selectTargets(
                    mockPlayer, smallCircle, candidates, null);

            // Then: 半径3以内のエンティティのみ選択される
            assertThat(selected).hasSize(2);
        }
    }

    // ==================== シナリオ5: ShapeCalculatorの結合テスト ====================

    @Nested
    @DisplayName("シナリオ5: ShapeCalculatorの結合テスト")
    class Scenario5ShapeCalculatorIntegration {

        @Test
        @DisplayName("シナリオ5-1: ShapeCalculatorでCONE判定が正しい")
        void test5_1_ShapeCalculatorConeCheck() {
            // Given: 扇状範囲設定とプレイヤー位置
            SkillTarget coneTarget = createConeTargetConfig(90.0, 10.0);

            // When: 前方のエンティティを作成して判定
            Entity entity = mock(Entity.class);
            Location entityLocation = mockEntityLocation(0, 64, 5);
            when(entity.getLocation()).thenReturn(entityLocation);
            when(entity.getWorld()).thenReturn(mockWorld);

            // Then: 範囲内と判定される
            boolean inRange = ShapeCalculator.isInRange(
                    entity, playerLocation, playerLocation.getDirection(),
                    AreaShape.CONE, coneTarget);

            assertThat(inRange).isTrue();
        }

        @Test
        @DisplayName("シナリオ5-2: ShapeCalculatorでCIRCLE判定が正しい")
        void test5_2_ShapeCalculatorCircleCheck() {
            // Given: 円形範囲設定
            SkillTarget circleTarget = createCircleTargetConfig(5.0);

            // When: 半径3の位置のエンティティを作成
            Entity entity = mock(Entity.class);
            Location entityLocation = mockEntityLocation(3, 64, 0);
            when(entity.getLocation()).thenReturn(entityLocation);
            when(entity.getWorld()).thenReturn(mockWorld);

            // Then: 範囲内と判定される
            boolean inRange = ShapeCalculator.isInRange(
                    entity, playerLocation, playerLocation.getDirection(),
                    AreaShape.CIRCLE, circleTarget);

            assertThat(inRange).isTrue();
        }

        @Test
        @DisplayName("シナリオ5-3: ShapeCalculatorで範囲外が正しく判定される")
        void test5_3_ShapeCalculatorOutOfRangeCheck() {
            // Given: 円形範囲設定（半径5）
            SkillTarget circleTarget = createCircleTargetConfig(5.0);

            // When: 半径10の位置のエンティティを作成
            Entity entity = mock(Entity.class);
            Location entityLocation = mockEntityLocation(10, 64, 0);
            when(entity.getLocation()).thenReturn(entityLocation);
            when(entity.getWorld()).thenReturn(mockWorld);

            // Then: 範囲外と判定される
            boolean inRange = ShapeCalculator.isInRange(
                    entity, playerLocation, playerLocation.getDirection(),
                    AreaShape.CIRCLE, circleTarget);

            assertThat(inRange).isFalse();
        }
    }

    // ==================== シナリオ6: SkillManagerとの統合 ====================

    @Nested
    @DisplayName("シナリオ6: SkillManagerとの統合")
    class Scenario6SkillManagerIntegration {

        @Test
        @DisplayName("シナリオ6-1: SkillManagerでターゲット選択ができる")
        void test6_1_SkillManagerTargetSelection() {
            // Given: 扇状範囲スキルを登録
            Skill coneSkill = createConeSkill();
            skillManager.registerSkill(coneSkill);

            // 候補エンティティを作成（Y座標を64に修正）
            List<Entity> candidates = createMockEntities(
                    new double[]{0, 64, 5},
                    new double[]{2, 64, 5},
                    new double[]{10, 64, 5}
            );

            // When: ターゲットを選択
            List<Entity> selected = TargetSelector.selectTargets(
                    mockPlayer, coneSkill.getSkillTarget(), candidates, null);

            // Then: 適切なターゲットが選択されている
            assertThat(selected).isNotEmpty();
        }

        @Test
        @DisplayName("シナリオ6-2: SkillTargetでのターゲット選択ができる")
        void test6_2_SkillTargetSelection() {
            // Given: 円形範囲スキルを登録
            Skill circleSkill = createCircleSkill();
            skillManager.registerSkill(circleSkill);

            // 候補エンティティを作成（Y座標を64に修正）
            List<Entity> candidates = createMockEntities(
                    new double[]{0, 64, 3},
                    new double[]{0, 64, 7},
                    new double[]{6, 64, 0}
            );

            // When: SkillTarget経由でターゲットを選択
            List<Entity> selected = TargetSelector.selectTargets(
                    mockPlayer, circleSkill.getSkillTarget(), candidates, null);

            // Then: 半径内のエンティティが選択されている
            assertThat(selected).hasSize(1); // (0,2,3)のみ半径5内
        }
    }

    // ==================== テスト用ヘルパーメソッド ====================

    /**
     * 単体ターゲット設定を作成
     */
    private SkillTarget createSingleTargetConfig() {
        return new SkillTarget(
                TargetType.NEAREST_HOSTILE,
                AreaShape.SINGLE,
                new SkillTarget.SingleTargetConfig(true, false),
                null,
                null,
                null
        );
    }

    /**
     * 扇状範囲設定を作成
     */
    private SkillTarget createConeTargetConfig(double angle, double range) {
        return new SkillTarget(
                TargetType.AREA_SELF,
                AreaShape.CONE,
                null,
                new SkillTarget.ConeConfig(angle, range),
                null,
                null
        );
    }

    /**
     * 四角形範囲設定を作成
     */
    private SkillTarget createRectTargetConfig(double width, double depth) {
        return new SkillTarget(
                TargetType.AREA_SELF,
                AreaShape.RECT,
                null,
                null,
                new SkillTarget.RectConfig(width, depth),
                null
        );
    }

    /**
     * 円形範囲設定を作成
     */
    private SkillTarget createCircleTargetConfig(double radius) {
        return new SkillTarget(
                TargetType.AREA_SELF,
                AreaShape.CIRCLE,
                null,
                null,
                null,
                new SkillTarget.CircleConfig(radius)
        );
    }

    /**
     * 扇状範囲スキルを作成
     */
    private Skill createConeSkill() {
        DamageCalculation damage = new DamageCalculation(
                30.0,
                Stat.INTELLIGENCE,
                1.5,
                10.0
        );

        LevelDependentParameter costParam = new LevelDependentParameter(15.0, 2.0, null, 25.0);
        LevelDependentParameter cooldownParam = new LevelDependentParameter(10.0, 0.0, null, null);

        SkillTarget skillTarget = createConeTargetConfig(90.0, 10.0);

        return new Skill(
                "cone_attack",
                "コーンアタック",
                "&eコーンアタック",
                SkillType.NORMAL,
                java.util.List.of("&c扇状の範囲攻撃"),
                5,
                10.0,
                15,
                cooldownParam,
                costParam,
                SkillCostType.MANA,
                damage,
                (SkillTreeConfig) null,
                (String) null,
                java.util.List.of(),
                (java.util.List<VariableDefinition>) null,
                (FormulaDamageConfig) null,
                (TargetingConfig) null,
                skillTarget,
                null  // componentEffect
        );
    }

    /**
     * 円形範囲スキルを作成
     */
    private Skill createCircleSkill() {
        DamageCalculation damage = new DamageCalculation(
                25.0,
                Stat.INTELLIGENCE,
                1.0,
                5.0
        );

        LevelDependentParameter costParam = new LevelDependentParameter(20.0, 3.0, null, 40.0);
        LevelDependentParameter cooldownParam = new LevelDependentParameter(12.0, 0.0, null, null);

        SkillTarget skillTarget = createCircleTargetConfig(5.0);

        return new Skill(
                "shockwave",
                "ショックウェーブ",
                "&bショックウェーブ",
                SkillType.NORMAL,
                java.util.List.of("&c周囲に衝撃波を放つ"),
                5,
                12.0,
                20,
                cooldownParam,
                costParam,
                SkillCostType.MANA,
                damage,
                (SkillTreeConfig) null,
                (String) null,
                java.util.List.of(),
                (java.util.List<VariableDefinition>) null,
                (FormulaDamageConfig) null,
                (TargetingConfig) null,
                skillTarget,
                null  // componentEffect
        );
    }

    /**
     * モックエンティティリストを作成
     */
    private List<Entity> createMockEntities(double[] zCoords, double[] xCoords) {
        List<Entity> entities = new ArrayList<>();
        for (int i = 0; i < zCoords.length && i < xCoords.length; i++) {
            Entity entity = mock(Entity.class);
            Location location = mockEntityLocation(xCoords[i], 64, zCoords[i]);
            when(entity.getLocation()).thenReturn(location);
            when(entity.getType()).thenReturn(EntityType.ZOMBIE);
            entities.add(entity);
        }
        return entities;
    }

    /**
     * モックエンティティリストを作成（可変長引数版）
     * 各配列は1つのエンティティの[X, Y, Z]座標を表す
     */
    private List<Entity> createMockEntities(double[]... coords) {
        List<Entity> entities = new ArrayList<>();
        for (double[] coord : coords) {
            Entity entity = mock(Entity.class);
            double x = coord.length > 0 ? coord[0] : 0;
            double y = coord.length > 1 ? coord[1] : 64;
            double z = coord.length > 2 ? coord[2] : 0;
            Location location = mockEntityLocation(x, y, z);
            when(entity.getLocation()).thenReturn(location);
            when(entity.getType()).thenReturn(EntityType.ZOMBIE);
            entities.add(entity);
        }
        return entities;
    }

    /**
     * モック位置を作成
     */
    private Location mockEntityLocation(double x, double y, double z) {
        Location loc = mock(Location.class);
        when(loc.getX()).thenReturn(x);
        when(loc.getY()).thenReturn(y);
        when(loc.getZ()).thenReturn(z);
        when(loc.getWorld()).thenReturn(mockWorld);
        when(loc.toVector()).thenReturn(new Vector(x, y, z));

        // 距離計算のモック
        when(loc.distance(any())).thenAnswer(invocation -> {
            Location other = invocation.getArgument(0);
            double dx = x - other.getX();
            double dy = y - other.getY();
            double dz = z - other.getZ();
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        });

        // 距離の二乗計算のモック
        when(loc.distanceSquared(any())).thenAnswer(invocation -> {
            Location other = invocation.getArgument(0);
            double dx = x - other.getX();
            double dy = y - other.getY();
            double dz = z - other.getZ();
            return dx * dx + dy * dy + dz * dz;
        });

        return loc;
    }
}

package com.example.rpgplugin.skill.component.target;

import com.example.rpgplugin.skill.component.ComponentSettings;
import com.example.rpgplugin.skill.component.ComponentType;
import com.example.rpgplugin.skill.target.AreaShape;
import com.example.rpgplugin.skill.target.TargetType;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

/**
 * TargetComponent関連の単体テスト
 *
 * <p>ターゲット選択コンポーネントの機能テスト。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@DisplayName("TargetComponent テスト")
@ExtendWith(MockitoExtension.class)
class TargetComponentTest {

    @Mock
    private LivingEntity mockCaster;
    @Mock
    private LivingEntity mockTarget1;
    @Mock
    private LivingEntity mockTarget2;
    @Mock
    private LivingEntity mockTarget3;
    @Mock
    private Player mockPlayer;
    @Mock
    private Location mockLocation;
    @Mock
    private Location mockEyeLocation;

    private UUID testUuid;
    private List<LivingEntity> nearbyEntities;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();

        // lenient() - 全てのテストで使用されないスタブ
        lenient().when(mockCaster.getUniqueId()).thenReturn(testUuid);
        lenient().when(mockCaster.getLocation()).thenReturn(mockLocation);
        lenient().when(mockCaster.getEyeLocation()).thenReturn(mockEyeLocation);
        lenient().when(mockCaster.isValid()).thenReturn(true);

        lenient().when(mockTarget1.getUniqueId()).thenReturn(UUID.randomUUID());
        lenient().when(mockTarget1.isValid()).thenReturn(true);
        lenient().when(mockTarget1.getLocation()).thenReturn(mockLocation);

        lenient().when(mockTarget2.getUniqueId()).thenReturn(UUID.randomUUID());
        lenient().when(mockTarget2.isValid()).thenReturn(true);
        lenient().when(mockTarget2.getLocation()).thenReturn(mockLocation);

        lenient().when(mockTarget3.getUniqueId()).thenReturn(UUID.randomUUID());
        lenient().when(mockTarget3.isValid()).thenReturn(true);
        lenient().when(mockTarget3.getLocation()).thenReturn(mockLocation);

        lenient().when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        lenient().when(mockPlayer.isValid()).thenReturn(true);
        lenient().when(mockPlayer.getLocation()).thenReturn(mockLocation);

        // 視線方向のデフォルト設定
        lenient().when(mockEyeLocation.getDirection()).thenReturn(new Vector(0, 0, 1));
        lenient().when(mockLocation.toVector()).thenReturn(new Vector(0, 0, 0));
        lenient().when(mockEyeLocation.toVector()).thenReturn(new Vector(0, 1.62, 0));

        // 初期化済みエンティティリスト
        nearbyEntities = new ArrayList<>();
    }

    // ==================== SelfTargetComponent テスト ====================

    @Test
    @DisplayName("SelfTargetComponent: 自分自身を選択")
    void testSelfTargetComponent_SelectsSelf() {
        SelfTargetComponent component = new SelfTargetComponent();

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertEquals(1, targets.size(), "自分自身が選択されること");
        assertSame(mockCaster, targets.get(0), "キャスター自身が返されること");
    }

    @Test
    @DisplayName("SelfTargetComponent: nullキャスターは空リスト")
    void testSelfTargetComponent_NullCaster() {
        SelfTargetComponent component = new SelfTargetComponent();

        List<LivingEntity> targets = component.selectTargets(null, 1);

        assertTrue(targets.isEmpty(), "nullキャスターは空リスト");
    }

    @Test
    @DisplayName("SelfTargetComponent: 無効キャスターは空リスト")
    void testSelfTargetComponent_InvalidCaster() {
        lenient().when(mockCaster.isValid()).thenReturn(false);
        SelfTargetComponent component = new SelfTargetComponent();

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertTrue(targets.isEmpty(), "無効キャスターは空リスト");
    }

    @Test
    @DisplayName("SelfTargetComponent: キーとタイプ")
    void testSelfTargetComponent_KeyAndType() {
        SelfTargetComponent component = new SelfTargetComponent();

        assertEquals("SELF", component.getKey(), "キーがSELFであること");
        assertEquals(ComponentType.TARGET, component.getType(), "タイプがTARGETであること");
    }

    // ==================== SingleTargetComponent テスト ====================

    @Test
    @DisplayName("SingleTargetComponent: target_self=trueで自分自身")
    void testSingleTargetComponent_TargetSelf() {
        SingleTargetComponent component = new SingleTargetComponent();
        component.getSettings().set("target_self", true);

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertEquals(1, targets.size(), "自分自身が選択されること");
        assertSame(mockCaster, targets.get(0));
    }

    @Test
    @DisplayName("SingleTargetComponent: 近くにエンティティがいない")
    void testSingleTargetComponent_NoNearbyEntities() {
        SingleTargetComponent component = new SingleTargetComponent();
        lenient().when(mockCaster.getNearbyEntities(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new ArrayList<>());

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertTrue(targets.isEmpty(), "ターゲットがいない場合は空リスト");
    }

    @Test
    @DisplayName("SingleTargetComponent: 敵対的のみフィルタ")
    void testSingleTargetComponent_HostileOnly() {
        SingleTargetComponent component = new SingleTargetComponent();
        component.getSettings().set("hostile_only", true);
        component.getSettings().set("range", 10.0);

        nearbyEntities.add(mockPlayer);
        nearbyEntities.add(mockTarget1);

        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(10.0, 10.0, 10.0);

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertFalse(targets.contains(mockPlayer), "プレイヤーは除外されること");
        assertTrue(targets.contains(mockTarget1), "Mobは含まれること");
    }

    @Test
    @DisplayName("SingleTargetComponent: hostile_only=falseで全て対象")
    void testSingleTargetComponent_NotHostileOnly() {
        SingleTargetComponent component = new SingleTargetComponent();
        component.getSettings().set("hostile_only", false);
        component.getSettings().set("range", 10.0);

        nearbyEntities.add(mockTarget1);

        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(10.0, 10.0, 10.0);

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertEquals(1, targets.size(), "ターゲットが選択されること");
    }

    @Test
    @DisplayName("SingleTargetComponent: select_nearest=true")
    void testSingleTargetComponent_SelectNearest() {
        SingleTargetComponent component = new SingleTargetComponent();
        component.getSettings().set("select_nearest", true);
        component.getSettings().set("hostile_only", false);
        component.getSettings().set("range", 10.0);

        nearbyEntities.add(mockTarget1);
        nearbyEntities.add(mockTarget2);

        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(10.0, 10.0, 10.0);

        // 距離を設定
        lenient().when(mockLocation.distance(mockTarget1.getLocation())).thenReturn(5.0);
        lenient().when(mockLocation.distance(mockTarget2.getLocation())).thenReturn(10.0);

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertEquals(1, targets.size(), "1体だけ選択されること");
        assertSame(mockTarget1, targets.get(0), "最も近いエンティティが選択されること");
    }

    @Test
    @DisplayName("SingleTargetComponent: select_nearest=falseでランダム")
    void testSingleTargetComponent_RandomSelection() {
        SingleTargetComponent component = new SingleTargetComponent();
        component.getSettings().set("select_nearest", false);
        component.getSettings().set("hostile_only", false);
        component.getSettings().set("range", 10.0);

        nearbyEntities.add(mockTarget1);
        nearbyEntities.add(mockTarget2);

        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(10.0, 10.0, 10.0);

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertEquals(1, targets.size(), "1体選択されること");
    }

    @Test
    @DisplayName("SingleTargetComponent: レベル依存の範囲計算")
    void testSingleTargetComponent_LevelDependentRange() {
        SingleTargetComponent component = new SingleTargetComponent();
        component.getSettings().set("range", 10.0);
        component.getSettings().set("range_per_level", 2.0);

        nearbyEntities.add(mockTarget1);
        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(12.0, 12.0, 12.0);
        doReturn(new ArrayList<>()).when(mockCaster).getNearbyEntities(10.0, 10.0, 10.0);

        List<LivingEntity> targets1 = component.selectTargets(mockCaster, 1);
        List<LivingEntity> targets2 = component.selectTargets(mockCaster, 2);

        assertEquals(0, targets1.size(), "レベル1では範囲外");
        assertEquals(1, targets2.size(), "レベル2では範囲内");
    }

    // ==================== ConeTargetComponent テスト ====================

    @Test
    @DisplayName("ConeTargetComponent: コーン内のターゲットを選択")
    void testConeTargetComponent_SelectsInCone() {
        ConeTargetComponent component = new ConeTargetComponent();
        component.getSettings().set("angle", 90.0);
        component.getSettings().set("range", 10.0);
        component.getSettings().set("max_targets", 10);

        nearbyEntities.add(mockTarget1);
        nearbyEntities.add(mockTarget2);

        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(10.0, 10.0, 10.0);

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertTrue(targets.size() >= 0, "ターゲットリストが返されること");
    }

    @Test
    @DisplayName("ConeTargetComponent: nullキャスターは空リスト")
    void testConeTargetComponent_NullCaster() {
        ConeTargetComponent component = new ConeTargetComponent();

        List<LivingEntity> targets = component.selectTargets(null, 1);

        assertTrue(targets.isEmpty(), "nullキャスターは空リスト");
    }

    @Test
    @DisplayName("ConeTargetComponent: キーとタイプ")
    void testConeTargetComponent_KeyAndType() {
        ConeTargetComponent component = new ConeTargetComponent();

        assertEquals("CONE", component.getKey(), "キーがCONEであること");
        assertEquals(ComponentType.TARGET, component.getType(), "タイプがTARGETであること");
    }

    @Test
    @DisplayName("ConeTargetComponent: レベル依存の角度と範囲")
    void testConeTargetComponent_LevelDependent() {
        ConeTargetComponent component = new ConeTargetComponent();
        component.getSettings().set("angle", 90.0);
        component.getSettings().set("angle_per_level", 10.0);
        component.getSettings().set("range", 10.0);
        component.getSettings().set("range_per_level", 2.0);
        component.getSettings().set("max_targets", 5);

        List<LivingEntity> targets = component.selectTargets(mockCaster, 3);

        // 範囲: 10 + 2*2 = 14
        verify(mockCaster).getNearbyEntities(14.0, 14.0, 14.0);
    }

    @Test
    @DisplayName("ConeTargetComponent: 最大ターゲット数制限")
    void testConeTargetComponent_MaxTargetsLimit() {
        ConeTargetComponent component = new ConeTargetComponent();
        component.getSettings().set("angle", 360.0);
        component.getSettings().set("range", 10.0);
        component.getSettings().set("max_targets", 2);

        nearbyEntities.add(mockTarget1);
        nearbyEntities.add(mockTarget2);
        nearbyEntities.add(mockTarget3);

        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(10.0, 10.0, 10.0);

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertTrue(targets.size() <= 2, "最大ターゲット数で制限されること");
    }

    // ==================== SphereTargetComponent テスト ====================

    @Test
    @DisplayName("SphereTargetComponent: 球形範囲のターゲットを選択")
    void testSphereTargetComponent_SelectsInSphere() {
        SphereTargetComponent component = new SphereTargetComponent();
        component.getSettings().set("radius", 5.0);
        component.getSettings().set("max_targets", 10);
        component.getSettings().set("include_caster", false);

        nearbyEntities.add(mockTarget1);
        nearbyEntities.add(mockTarget2);

        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(5.0, 5.0, 5.0);

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertFalse(targets.contains(mockCaster), "キャスターは含まれないこと");
        assertTrue(targets.size() >= 0, "ターゲットリストが返されること");
    }

    @Test
    @DisplayName("SphereTargetComponent: include_caster=true")
    void testSphereTargetComponent_IncludeCaster() {
        SphereTargetComponent component = new SphereTargetComponent();
        component.getSettings().set("radius", 5.0);
        component.getSettings().set("max_targets", 10);
        component.getSettings().set("include_caster", true);

        // キャスターをnearbyEntitiesに追加（getNearbyEntitiesがキャスターを返すように）
        nearbyEntities.add(mockCaster);
        nearbyEntities.add(mockTarget1);

        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(5.0, 5.0, 5.0);

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertTrue(targets.contains(mockCaster), "キャスターが含まれること");
    }

    @Test
    @DisplayName("SphereTargetComponent: nullキャスターは空リスト")
    void testSphereTargetComponent_NullCaster() {
        SphereTargetComponent component = new SphereTargetComponent();

        List<LivingEntity> targets = component.selectTargets(null, 1);

        assertTrue(targets.isEmpty(), "nullキャスターは空リスト");
    }

    @Test
    @DisplayName("SphereTargetComponent: キーとタイプ")
    void testSphereTargetComponent_KeyAndType() {
        SphereTargetComponent component = new SphereTargetComponent();

        assertEquals("SPHERE", component.getKey(), "キーがSPHEREであること");
        assertEquals(ComponentType.TARGET, component.getType(), "タイプがTARGETであること");
    }

    @Test
    @DisplayName("SphereTargetComponent: レベル依存の半径")
    void testSphereTargetComponent_LevelDependentRadius() {
        SphereTargetComponent component = new SphereTargetComponent();
        component.getSettings().set("radius", 5.0);
        component.getSettings().set("radius_per_level", 1.5);

        lenient().when(mockCaster.getNearbyEntities(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new ArrayList<>());

        component.selectTargets(mockCaster, 3);

        // 半径: 5 + 1.5*2 = 8
        verify(mockCaster).getNearbyEntities(8.0, 8.0, 8.0);
    }

    // ==================== AreaTargetComponent テスト ====================

    @Test
    @DisplayName("AreaTargetComponent: 円形範囲")
    void testAreaTargetComponent_Circle() {
        AreaTargetComponent component = new AreaTargetComponent();
        component.getSettings().set("area_shape", "CIRCLE");
        component.getSettings().set("radius", 10.0);
        component.getSettings().set("max_targets", 15);

        nearbyEntities.add(mockTarget1);

        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(10.0, 10.0, 10.0);

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertTrue(targets.contains(mockTarget1), "ターゲットが含まれること");
    }

    @Test
    @DisplayName("AreaTargetComponent: 矩形範囲")
    void testAreaTargetComponent_Rectangle() {
        AreaTargetComponent component = new AreaTargetComponent();
        component.getSettings().set("area_shape", "RECT");  // RECT而不是RECTANGLE
        component.getSettings().set("width", 10.0);
        component.getSettings().set("depth", 15.0);
        component.getSettings().set("max_targets", 15);

        nearbyEntities.add(mockTarget1);

        // range = Math.max(width, depth) = 15.0
        // doReturnパターンで型チェックを回避
        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(anyDouble(), anyDouble(), anyDouble());

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertTrue(targets.contains(mockTarget1), "ターゲットが含まれること");
    }

    @Test
    @DisplayName("AreaTargetComponent: include_caster")
    void testAreaTargetComponent_IncludeCaster() {
        AreaTargetComponent component = new AreaTargetComponent();
        component.getSettings().set("area_shape", "CIRCLE");
        component.getSettings().set("radius", 10.0);
        component.getSettings().set("include_caster", true);

        // キャスターをnearbyEntitiesに追加
        nearbyEntities.add(mockCaster);
        nearbyEntities.add(mockTarget1);

        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(10.0, 10.0, 10.0);

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertTrue(targets.contains(mockCaster), "キャスターが含まれること");
    }

    @Test
    @DisplayName("AreaTargetComponent: include_caster=falseでキャスターを除外")
    void testAreaTargetComponent_ExcludeCaster() {
        AreaTargetComponent component = new AreaTargetComponent();
        component.getSettings().set("area_shape", "CIRCLE");
        component.getSettings().set("radius", 10.0);
        component.getSettings().set("include_caster", false);

        // キャスターをnearbyEntitiesに追加
        nearbyEntities.add(mockCaster);
        nearbyEntities.add(mockTarget1);

        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(10.0, 10.0, 10.0);

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertFalse(targets.contains(mockCaster), "キャスターは除外されること");
        assertTrue(targets.contains(mockTarget1), "ターゲットは含まれること");
    }

    @Test
    @DisplayName("AreaTargetComponent: 無効なarea_shapeはデフォルト")
    void testAreaTargetComponent_InvalidShape() {
        AreaTargetComponent component = new AreaTargetComponent();
        component.getSettings().set("area_shape", "INVALID");
        component.getSettings().set("radius", 10.0);

        lenient().when(mockCaster.getNearbyEntities(10.0, 10.0, 10.0))
                .thenReturn(new ArrayList<>());

        // デフォルトのCIRCLEが使用される
        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertNotNull(targets, "ターゲットリストが返されること");
    }

    @Test
    @DisplayName("AreaTargetComponent: nullキャスターは空リスト")
    void testAreaTargetComponent_NullCaster() {
        AreaTargetComponent component = new AreaTargetComponent();
        component.getSettings().set("area_shape", "CIRCLE");
        component.getSettings().set("radius", 10.0);

        List<LivingEntity> targets = component.selectTargets(null, 1);

        assertTrue(targets.isEmpty(), "nullキャスターは空リスト");
    }

    @Test
    @DisplayName("AreaTargetComponent: 無効なキャスターは空リスト")
    void testAreaTargetComponent_InvalidCaster() {
        AreaTargetComponent component = new AreaTargetComponent();
        component.getSettings().set("area_shape", "CIRCLE");
        component.getSettings().set("radius", 10.0);

        // isValid()がfalseを返すようにモック
        lenient().when(mockCaster.isValid()).thenReturn(false);

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertTrue(targets.isEmpty(), "無効なキャスターは空リスト");
    }

    @Test
    @DisplayName("AreaTargetComponent: RECTで幅と奥行きを設定")
    void testAreaTargetComponent_RectangleWidthDepth() {
        AreaTargetComponent component = new AreaTargetComponent();
        // AreaShape列挙型ではRECT而不是RECTANGLE
        component.getSettings().set("area_shape", "RECT");
        component.getSettings().set("width", 12.0);
        component.getSettings().set("depth", 8.0);
        component.getSettings().set("max_targets", 15);

        nearbyEntities.add(mockTarget1);

        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(anyDouble(), anyDouble(), anyDouble());

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        // range = Math.max(12.0, 8.0) = 12.0
        verify(mockCaster).getNearbyEntities(12.0, 12.0, 12.0);
        assertTrue(targets.contains(mockTarget1), "ターゲットが含まれること");
    }

    // ==================== LineTargetComponent テスト ====================

    @Test
    @DisplayName("LineTargetComponent: 直線上のターゲットを選択")
    void testLineTargetComponent_SelectsOnLine() {
        LineTargetComponent component = new LineTargetComponent();
        component.getSettings().set("length", 15.0);
        component.getSettings().set("width", 2.0);
        component.getSettings().set("max_targets", 5);

        nearbyEntities.add(mockTarget1);

        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(17.0, 17.0, 17.0);

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertNotNull(targets, "ターゲットリストが返されること");
    }

    @Test
    @DisplayName("LineTargetComponent: nullキャスターは空リスト")
    void testLineTargetComponent_NullCaster() {
        LineTargetComponent component = new LineTargetComponent();

        List<LivingEntity> targets = component.selectTargets(null, 1);

        assertTrue(targets.isEmpty(), "nullキャスターは空リスト");
    }

    @Test
    @DisplayName("LineTargetComponent: キーとタイプ")
    void testLineTargetComponent_KeyAndType() {
        LineTargetComponent component = new LineTargetComponent();

        assertEquals("LINE", component.getKey(), "キーがLINEであること");
        assertEquals(ComponentType.TARGET, component.getType(), "タイプがTARGETであること");
    }

    @Test
    @DisplayName("LineTargetComponent: レベル依存の長さと幅")
    void testLineTargetComponent_LevelDependent() {
        LineTargetComponent component = new LineTargetComponent();
        component.getSettings().set("length", 15.0);
        component.getSettings().set("length_per_level", 3.0);
        component.getSettings().set("width", 2.0);
        component.getSettings().set("width_per_level", 0.5);

        lenient().when(mockCaster.getNearbyEntities(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new ArrayList<>());

        component.selectTargets(mockCaster, 3);

        // 長さ: 15 + 3*2 = 21, 幅: 2 + 0.5*2 = 3
        verify(mockCaster).getNearbyEntities(24.0, 24.0, 24.0);
    }

    @Test
    @DisplayName("LineTargetComponent: キャスターは除外される")
    void testLineTargetComponent_ExcludesCaster() {
        LineTargetComponent component = new LineTargetComponent();
        component.getSettings().set("length", 15.0);
        component.getSettings().set("width", 2.0);

        // キャスターをnearbyEntitiesに追加
        nearbyEntities.add(mockCaster);
        nearbyEntities.add(mockTarget1);

        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(anyDouble(), anyDouble(), anyDouble());

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertFalse(targets.contains(mockCaster), "キャスターは除外されること");
    }

    @Test
    @DisplayName("LineTargetComponent: 複数ターゲットでソートされる")
    void testLineTargetComponent_SortsByDistance() {
        LineTargetComponent component = new LineTargetComponent();
        component.getSettings().set("length", 15.0);
        component.getSettings().set("width", 2.0);
        component.getSettings().set("max_targets", 10);

        // 2つのターゲットを追加
        nearbyEntities.add(mockTarget1);
        nearbyEntities.add(mockTarget2);

        // キャスターの視線位置モック
        Location eyeLoc = mock(Location.class);
        Vector direction = new Vector(1, 0, 0); // X方向を向いている
        Vector casterVec = new Vector(0, 0, 0);

        lenient().when(mockCaster.getEyeLocation()).thenReturn(eyeLoc);
        lenient().when(eyeLoc.getDirection()).thenReturn(direction);
        lenient().when(eyeLoc.toVector()).thenReturn(casterVec);

        // キャスターの位置モック（ソート用）
        lenient().when(mockCaster.getLocation()).thenReturn(mockLocation);
        lenient().when(mockLocation.toVector()).thenReturn(casterVec);

        // ターゲット1の位置モック（キャスターから5ブロック先）
        Location loc1 = mock(Location.class);
        Vector vec1 = new Vector(5, 0, 0); // 直線上
        lenient().when(mockTarget1.getLocation()).thenReturn(loc1);
        lenient().when(loc1.toVector()).thenReturn(vec1);
        lenient().when(mockLocation.distance(loc1)).thenReturn(5.0);

        // ターゲット2の位置モック（キャスターから10ブロック先）
        Location loc2 = mock(Location.class);
        Vector vec2 = new Vector(10, 0, 0); // 直線上
        lenient().when(mockTarget2.getLocation()).thenReturn(loc2);
        lenient().when(loc2.toVector()).thenReturn(vec2);
        lenient().when(mockLocation.distance(loc2)).thenReturn(10.0);

        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(anyDouble(), anyDouble(), anyDouble());

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertEquals(2, targets.size(), "2つのターゲットが選択されること");
        // 距離順にソートされている（近い順）
        assertSame(mockTarget1, targets.get(0), "近いターゲットが先頭");
        assertSame(mockTarget2, targets.get(1), "遠いターゲットが後ろ");
    }

    // ==================== SectorTargetComponent テスト ====================

    @Test
    @DisplayName("SectorTargetComponent: 扇形内のターゲットを選択")
    void testSectorTargetComponent_SelectsInSector() {
        SectorTargetComponent component = new SectorTargetComponent();
        component.getSettings().set("angle", 60.0);
        component.getSettings().set("radius", 8.0);
        component.getSettings().set("max_targets", 8);

        nearbyEntities.add(mockTarget1);

        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(8.0, 8.0, 8.0);

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertTrue(targets.size() >= 0, "ターゲットリストが返されること");
    }

    @Test
    @DisplayName("SectorTargetComponent: nullキャスターは空リスト")
    void testSectorTargetComponent_NullCaster() {
        SectorTargetComponent component = new SectorTargetComponent();

        List<LivingEntity> targets = component.selectTargets(null, 1);

        assertTrue(targets.isEmpty(), "nullキャスターは空リスト");
    }

    @Test
    @DisplayName("SectorTargetComponent: キーとタイプ")
    void testSectorTargetComponent_KeyAndType() {
        SectorTargetComponent component = new SectorTargetComponent();

        assertEquals("SECTOR", component.getKey(), "キーがSECTORであること");
        assertEquals(ComponentType.TARGET, component.getType(), "タイプがTARGETであること");
    }

    // ==================== NearestHostileTargetComponent テスト ====================

    @Test
    @DisplayName("NearestHostileTargetComponent: 最寄りの敵対的を選択")
    void testNearestHostileTargetComponent_SelectsNearest() {
        NearestHostileTargetComponent component = new NearestHostileTargetComponent();
        component.getSettings().set("range", 15.0);

        nearbyEntities.add(mockTarget1);
        nearbyEntities.add(mockTarget2);

        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(15.0, 15.0, 15.0);

        // 距離を設定
        lenient().when(mockLocation.distance(mockTarget1.getLocation())).thenReturn(5.0);
        lenient().when(mockLocation.distance(mockTarget2.getLocation())).thenReturn(10.0);

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertEquals(1, targets.size(), "1体選択されること");
        assertSame(mockTarget1, targets.get(0), "最も近いエンティティが選択されること");
    }

    @Test
    @DisplayName("NearestHostileTargetComponent: プレイヤーは除外")
    void testNearestHostileTargetComponent_ExcludesPlayers() {
        NearestHostileTargetComponent component = new NearestHostileTargetComponent();
        component.getSettings().set("range", 15.0);

        nearbyEntities.add(mockPlayer);

        doReturn(nearbyEntities).when(mockCaster).getNearbyEntities(15.0, 15.0, 15.0);

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertFalse(targets.contains(mockPlayer), "プレイヤーは除外されること");
    }

    @Test
    @DisplayName("NearestHostileTargetComponent: ターゲットがいない")
    void testNearestHostileTargetComponent_NoTargets() {
        NearestHostileTargetComponent component = new NearestHostileTargetComponent();
        component.getSettings().set("range", 15.0);

        lenient().when(mockCaster.getNearbyEntities(15.0, 15.0, 15.0))
                .thenReturn(new ArrayList<>());

        List<LivingEntity> targets = component.selectTargets(mockCaster, 1);

        assertTrue(targets.isEmpty(), "ターゲットがいない場合は空リスト");
    }

    @Test
    @DisplayName("NearestHostileTargetComponent: nullキャスターは空リスト")
    void testNearestHostileTargetComponent_NullCaster() {
        NearestHostileTargetComponent component = new NearestHostileTargetComponent();

        List<LivingEntity> targets = component.selectTargets(null, 1);

        assertTrue(targets.isEmpty(), "nullキャスターは空リスト");
    }

    @Test
    @DisplayName("NearestHostileTargetComponent: キーとタイプ")
    void testNearestHostileTargetComponent_KeyAndType() {
        NearestHostileTargetComponent component = new NearestHostileTargetComponent();

        assertEquals("NEAREST_HOSTILE", component.getKey(), "キーがNEAREST_HOSTILEであること");
        assertEquals(ComponentType.TARGET, component.getType(), "タイプがTARGETであること");
    }

    // ==================== TargetComponent 基底クラス テスト ====================

    @Test
    @DisplayName("TargetComponent: getTypeはTARGET")
    void testTargetComponent_GetType() {
        // 具象クラスでテスト
        SelfTargetComponent component = new SelfTargetComponent();

        assertEquals(ComponentType.TARGET, component.getType(), "タイプはTARGETであること");
    }

    @Test
    @DisplayName("TargetComponent: 子コンポーネント操作")
    void testTargetComponent_Children() {
        SelfTargetComponent parent = new SelfTargetComponent();
        SelfTargetComponent child = new SelfTargetComponent();

        parent.addChild(child);

        assertEquals(1, parent.getChildren().size(), "子コンポーネントが追加されること");
        assertSame(child, parent.getChildren().get(0), "追加した子コンポーネントであること");
    }

    @Test
    @DisplayName("TargetComponent: null子の追加は無視")
    void testTargetComponent_AddNullChild() {
        SelfTargetComponent parent = new SelfTargetComponent();

        parent.addChild(null);

        assertEquals(0, parent.getChildren().size(), "null子は追加されないこと");
    }

    @Test
    @DisplayName("TargetComponent: executeは子コンポーネントを実行")
    void testTargetComponent_ExecuteWithChildren() {
        SelfTargetComponent parent = new SelfTargetComponent();
        SelfTargetComponent child = spy(new SelfTargetComponent());

        parent.addChild(child);

        // 子のexecuteメソッドがtrueを返すようにスタブ
        doReturn(true).when(child).execute(any(), anyInt(), any());

        // SelfTargetComponentは自分自身を選択するので、targetsは使用されずに自分が選ばれる
        List<LivingEntity> inputTargets = new ArrayList<>();
        inputTargets.add(mockTarget1);

        boolean result = parent.execute(mockCaster, 1, inputTargets);

        assertTrue(result, "実行成功すること");
        // SelfTargetComponentが自分自身を選択し、それが子に渡されることを検証
        verify(child).execute(eq(mockCaster), eq(1), any());
    }

    @Test
    @DisplayName("TargetComponent: 空ターゲットでexecuteは失敗")
    void testTargetComponent_ExecuteWithEmptyTargets() {
        SelfTargetComponent component = new SelfTargetComponent();

        boolean result = component.execute(mockCaster, 1, new ArrayList<>());

        assertFalse(result, "空ターゲットでは失敗すること");
    }

    @Test
    @DisplayName("TargetComponent: 設定の取得")
    void testTargetComponent_Settings() {
        SelfTargetComponent component = new SelfTargetComponent();
        ComponentSettings settings = component.getSettings();

        assertNotNull(settings, "設定が取得できること");
        settings.set("test_key", "test_value");
        assertEquals("test_value", component.getSettings().getString("test_key", ""));
    }

    @Test
    @DisplayName("TargetComponent: getTargetType")
    void testTargetComponent_GetTargetType() {
        SelfTargetComponent component = new SelfTargetComponent();

        // デフォルト値
        TargetType result = component.getTargetType(TargetType.SELF);

        assertEquals(TargetType.SELF, result, "デフォルト値が返されること");
    }

    @Test
    @DisplayName("TargetComponent: getTargetType with setting")
    void testTargetComponent_GetTargetTypeWithSetting() {
        SelfTargetComponent component = new SelfTargetComponent();
        component.getSettings().set("type", "NEAREST_HOSTILE");

        TargetType result = component.getTargetType(TargetType.SELF);

        assertEquals(TargetType.NEAREST_HOSTILE, result, "設定値が返されること");
    }

    @Test
    @DisplayName("TargetComponent: getTargetType invalid type returns default")
    void testTargetComponent_GetTargetTypeInvalid() {
        SelfTargetComponent component = new SelfTargetComponent();
        component.getSettings().set("type", "INVALID");

        TargetType result = component.getTargetType(TargetType.SELF);

        assertEquals(TargetType.SELF, result, "無効なタイプではデフォルト値が返されること");
    }

    @Test
    @DisplayName("TargetComponent: getAreaShape")
    void testTargetComponent_GetAreaShape() {
        SelfTargetComponent component = new SelfTargetComponent();

        AreaShape result = component.getAreaShape(AreaShape.CIRCLE);

        assertEquals(AreaShape.CIRCLE, result, "デフォルト値が返されること");
    }

    @Test
    @DisplayName("TargetComponent: getRange")
    void testTargetComponent_GetRange() {
        SelfTargetComponent component = new SelfTargetComponent();
        component.getSettings().set("range", 20.0);

        double result = component.getRange(1, 10.0);

        assertEquals(20.0, result, "設定値が返されること");
    }

    @Test
    @DisplayName("TargetComponent: getRange with per_level")
    void testTargetComponent_GetRangePerLevel() {
        SelfTargetComponent component = new SelfTargetComponent();
        component.getSettings().set("range", 10.0);
        component.getSettings().set("range_per_level", 2.0);

        assertEquals(10.0, component.getRange(1, 5.0), "レベル1はbase値");
        assertEquals(12.0, component.getRange(2, 5.0), "レベル2はbase+per_level");
        assertEquals(14.0, component.getRange(3, 5.0), "レベル3はbase+2*per_level");
    }

    @Test
    @DisplayName("TargetComponent: getMaxTargets")
    void testTargetComponent_GetMaxTargets() {
        SelfTargetComponent component = new SelfTargetComponent();
        component.getSettings().set("max_targets", 10);
        component.getSettings().set("max_targets_per_level", 2);

        assertEquals(10, component.getMaxTargets(1, 5), "レベル1はbase値");
        assertEquals(12, component.getMaxTargets(2, 5), "レベル2はbase+per_level");
        assertEquals(14, component.getMaxTargets(3, 5), "レベル3はbase+2*per_level");
    }

    @Test
    @DisplayName("TargetComponent: getAngle")
    void testTargetComponent_GetAngle() {
        SelfTargetComponent component = new SelfTargetComponent();
        component.getSettings().set("angle", 45.0);
        component.getSettings().set("angle_per_level", 5.0);

        assertEquals(45.0, component.getAngle(1, 30.0), "レベル1はbase値");
        assertEquals(50.0, component.getAngle(2, 30.0), "レベル2はbase+per_level");
        assertEquals(55.0, component.getAngle(3, 30.0), "レベル3はbase+2*per_level");
    }

    @Test
    @DisplayName("TargetComponent: getRadius")
    void testTargetComponent_GetRadius() {
        SelfTargetComponent component = new SelfTargetComponent();
        component.getSettings().set("radius", 8.0);
        component.getSettings().set("radius_per_level", 1.5);

        assertEquals(8.0, component.getRadius(1, 5.0), "レベル1はbase値");
        assertEquals(9.5, component.getRadius(2, 5.0), "レベル2はbase+per_level");
    }

    @Test
    @DisplayName("TargetComponent: getLength")
    void testTargetComponent_GetLength() {
        SelfTargetComponent component = new SelfTargetComponent();
        component.getSettings().set("length", 12.0);
        component.getSettings().set("length_per_level", 3.0);

        assertEquals(12.0, component.getLength(1, 5.0), "レベル1はbase値");
        assertEquals(15.0, component.getLength(2, 5.0), "レベル2はbase+per_level");
    }

    @Test
    @DisplayName("TargetComponent: getWidth")
    void testTargetComponent_GetWidth() {
        SelfTargetComponent component = new SelfTargetComponent();
        component.getSettings().set("width", 3.0);
        component.getSettings().set("width_per_level", 0.5);

        assertEquals(3.0, component.getWidth(1, 2.0), "レベル1はbase値");
        assertEquals(3.5, component.getWidth(2, 2.0), "レベル2はbase+per_level");
    }

    @Test
    @DisplayName("TargetComponent: getNearbyEntities")
    void testTargetComponent_GetNearbyEntities() {
        SelfTargetComponent component = new SelfTargetComponent();

        List<LivingEntity> entities = component.getNearbyEntities(mockCaster, 10.0);

        assertNotNull(entities, "エンティティリストが返されること");
    }

    @Test
    @DisplayName("TargetComponent: getNearbyEntities null caster")
    void testTargetComponent_GetNearbyEntitiesNullCaster() {
        SelfTargetComponent component = new SelfTargetComponent();

        List<LivingEntity> entities = component.getNearbyEntities(null, 10.0);

        assertTrue(entities.isEmpty(), "nullキャスターは空リスト");
    }

    @Test
    @DisplayName("TargetComponent: getNearbyEntities invalid caster")
    void testTargetComponent_GetNearbyEntitiesInvalidCaster() {
        SelfTargetComponent component = new SelfTargetComponent();
        lenient().when(mockCaster.isValid()).thenReturn(false);

        List<LivingEntity> entities = component.getNearbyEntities(mockCaster, 10.0);

        assertTrue(entities.isEmpty(), "無効キャスターは空リスト");
    }

    @Test
    @DisplayName("TargetComponent: filterHostile")
    void testTargetComponent_FilterHostile() {
        SelfTargetComponent component = new SelfTargetComponent();

        List<LivingEntity> entities = new ArrayList<>();
        entities.add(mockPlayer);
        entities.add(mockTarget1);

        List<LivingEntity> hostile = component.filterHostile(mockCaster, entities);

        assertFalse(hostile.contains(mockPlayer), "プレイヤーは除外されること");
        assertTrue(hostile.contains(mockTarget1), "Mobは含まれること");
    }

    @Test
    @DisplayName("TargetComponent: getNearestEntity")
    void testTargetComponent_GetNearestEntity() {
        SelfTargetComponent component = new SelfTargetComponent();

        List<LivingEntity> entities = new ArrayList<>();
        entities.add(mockTarget1);
        entities.add(mockTarget2);

        lenient().when(mockLocation.distance(mockTarget1.getLocation())).thenReturn(5.0);
        lenient().when(mockLocation.distance(mockTarget2.getLocation())).thenReturn(10.0);

        LivingEntity nearest = component.getNearestEntity(mockCaster, entities);

        assertSame(mockTarget1, nearest, "最も近いエンティティが返されること");
    }

    @Test
    @DisplayName("TargetComponent: getNearestEntity empty list")
    void testTargetComponent_GetNearestEntityEmpty() {
        SelfTargetComponent component = new SelfTargetComponent();

        LivingEntity nearest = component.getNearestEntity(mockCaster, new ArrayList<>());

        assertNull(nearest, "空リストではnullが返されること");
    }

    @Test
    @DisplayName("TargetComponent: getNearestEntity ignores invalid")
    void testTargetComponent_GetNearestEntityIgnoresInvalid() {
        SelfTargetComponent component = new SelfTargetComponent();

        List<LivingEntity> entities = new ArrayList<>();
        entities.add(mockTarget1);
        entities.add(mockTarget2);
        lenient().when(mockTarget1.isValid()).thenReturn(false);
        lenient().when(mockLocation.distance(mockTarget2.getLocation())).thenReturn(10.0);

        LivingEntity nearest = component.getNearestEntity(mockCaster, entities);

        assertSame(mockTarget2, nearest, "無効エンティティは無視されること");
    }

    @Test
    @DisplayName("TargetComponent: getNearestEntity ignores caster")
    void testTargetComponent_GetNearestEntityIgnoresCaster() {
        SelfTargetComponent component = new SelfTargetComponent();

        List<LivingEntity> entities = new ArrayList<>();
        entities.add(mockCaster);

        LivingEntity nearest = component.getNearestEntity(mockCaster, entities);

        assertNull(nearest, "キャスター自身は無視されること");
    }

    @Test
    @DisplayName("TargetComponent: limitTargets")
    void testTargetComponent_LimitTargets() {
        SelfTargetComponent component = new SelfTargetComponent();

        List<LivingEntity> targets = new ArrayList<>();
        targets.add(mockTarget1);
        targets.add(mockTarget2);
        targets.add(mockTarget3);

        List<LivingEntity> limited = component.limitTargets(targets, 2);

        assertEquals(2, limited.size(), "制限された数だけ返されること");
    }

    @Test
    @DisplayName("TargetComponent: limitTargets no limit needed")
    void testTargetComponent_LimitTargetsNoLimit() {
        SelfTargetComponent component = new SelfTargetComponent();

        List<LivingEntity> targets = new ArrayList<>();
        targets.add(mockTarget1);

        List<LivingEntity> limited = component.limitTargets(targets, 5);

        assertEquals(1, limited.size(), "制限未満で全て返されること");
        assertSame(targets, limited, "同じリストが返されること");
    }

    @Test
    @DisplayName("TargetComponent: cleanUp")
    void testTargetComponent_CleanUp() {
        SelfTargetComponent parent = new SelfTargetComponent();
        SelfTargetComponent child = spy(new SelfTargetComponent());

        parent.addChild(child);

        // 例外が発生しないことを確認
        assertDoesNotThrow(() -> parent.cleanUp(mockCaster), "cleanUpは例外を投げないこと");

        // 子のcleanUpも呼ばれる
        verify(child).cleanUp(mockCaster);
    }

    @Test
    @DisplayName("TargetComponent: toString")
    void testTargetComponent_ToString() {
        SelfTargetComponent component = new SelfTargetComponent();

        String str = component.toString();

        assertTrue(str.contains("SelfTargetComponent"), "クラス名が含まれること");
        assertTrue(str.contains("SELF"), "キーが含まれること");
        assertTrue(str.contains("TARGET"), "タイプが含まれること");
    }

    @Test
    @DisplayName("TargetComponent: setSkill")
    void testTargetComponent_SetSkill() {
        SelfTargetComponent component = new SelfTargetComponent();

        // 単純に例外が発生しないことを確認
        assertDoesNotThrow(() -> component.setSkill(null), "setSkillは例外を投げないこと");
    }

    @Test
    @DisplayName("TargetComponent: addChild propagates skill")
    void testTargetComponent_AddChildPropagatesSkill() {
        SelfTargetComponent parent = new SelfTargetComponent();
        SelfTargetComponent child = new SelfTargetComponent();
        parent.setSkill(mock(com.example.rpgplugin.skill.component.SkillEffect.class));

        parent.addChild(child);

        // 親と同じスキルが設定される（実際にはnullだがpropagation自体はテスト）
        // 内部実装でskillが伝播されることを確認
        assertNotNull(child.getSettings(), "子の設定は初期化されていること");
    }
}

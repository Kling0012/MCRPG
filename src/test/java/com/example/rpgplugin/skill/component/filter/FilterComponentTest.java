package com.example.rpgplugin.skill.component.filter;

import com.example.rpgplugin.skill.component.ComponentType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * フィルターコンポーネント テストクラス
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("フィルターコンポーネント テスト")
class FilterComponentTest {

    // ==================== EntityTypeFilter テスト ====================

    @Nested
    @DisplayName("EntityTypeFilter: エンティティタイプフィルター")
    class EntityTypeFilterTests {

        @Mock
        private Player mockCaster;

        @Mock
        private Player mockPlayer;

        @Mock
        private LivingEntity mockMob;

        private EntityTypeFilter filter;

        @BeforeEach
        void setUp() {
            filter = new EntityTypeFilter();
        }

        @Test
        @DisplayName("getType: FILTERを返す")
        void testGetType() {
            assertEquals(ComponentType.FILTER, filter.getType());
        }

        @Test
        @DisplayName("setFilterType/getFilterType: フィルタータイプの設定と取得")
        void testSetGetFilterType() {
            filter.setFilterType(EntityTypeFilter.EntityType.PLAYER);
            assertEquals(EntityTypeFilter.EntityType.PLAYER, filter.getFilterType());

            filter.setFilterType(EntityTypeFilter.EntityType.MOB);
            assertEquals(EntityTypeFilter.EntityType.MOB, filter.getFilterType());
        }

        @Test
        @DisplayName("test: SELFフィルター - 発動者のみ通す")
        void testSelf_SameEntity() {
            filter.setFilterType(EntityTypeFilter.EntityType.SELF);

            assertTrue(filter.test(mockCaster, 1, mockCaster));
            assertFalse(filter.test(mockCaster, 1, mockPlayer));
            assertFalse(filter.test(mockCaster, 1, mockMob));
        }

        @Test
        @DisplayName("test: PLAYERフィルター - プレイヤーのみ通す")
        void testPlayer() {
            filter.setFilterType(EntityTypeFilter.EntityType.PLAYER);

            assertTrue(filter.test(mockCaster, 1, mockPlayer));
            assertFalse(filter.test(mockCaster, 1, mockMob));
        }

        @Test
        @DisplayName("test: MOBフィルター - Mobのみ通す")
        void testMob() {
            filter.setFilterType(EntityTypeFilter.EntityType.MOB);

            assertTrue(filter.test(mockCaster, 1, mockMob));
            assertFalse(filter.test(mockCaster, 1, mockPlayer));
        }

        @Test
        @DisplayName("test: ALLフィルター - 全て通す")
        void testAll() {
            filter.setFilterType(EntityTypeFilter.EntityType.ALL);

            assertTrue(filter.test(mockCaster, 1, mockPlayer));
            assertTrue(filter.test(mockCaster, 1, mockMob));
            assertTrue(filter.test(mockCaster, 1, mockCaster));
        }

        @Test
        @DisplayName("test: nullフィルタータイプの場合はALLとして扱う")
        void testNullFilterType() {
            filter.setFilterType(null);

            assertTrue(filter.test(mockCaster, 1, mockPlayer));
            assertTrue(filter.test(mockCaster, 1, mockMob));
        }

        @Test
        @DisplayName("execute: フィルタリングして子コンポーネントを実行")
        void testExecute() {
            List<LivingEntity> targets = new ArrayList<>();
            targets.add(mockPlayer);
            targets.add(mockMob);

            filter.setFilterType(EntityTypeFilter.EntityType.PLAYER);

            // PLAYERフィルターなのでプレイヤーのみ通過
            // 子コンポーネントがないのでexecuteChildrenはfalseを返す
            // FilterComponent.executeは「filteredが空ではない && executeChildrenがtrue」を返す
            // 子がない場合はfalseになる
            assertFalse(filter.execute(mockCaster, 1, targets));
        }

        @Test
        @DisplayName("test: フィルタリングの直接テスト")
        void testFiltering_Direct() {
            List<LivingEntity> targets = new ArrayList<>();
            targets.add(mockPlayer);
            targets.add(mockMob);

            filter.setFilterType(EntityTypeFilter.EntityType.PLAYER);

            // PLAYERフィルター: プレイヤーはtrue、Mobはfalse
            assertTrue(filter.test(mockCaster, 1, mockPlayer));
            assertFalse(filter.test(mockCaster, 1, mockMob));
        }

        @Test
        @DisplayName("execute: フィルタ結果が空の場合はfalse")
        void testExecute_EmptyFiltered() {
            List<LivingEntity> targets = new ArrayList<>();
            targets.add(mockMob); // MOBのみ

            filter.setFilterType(EntityTypeFilter.EntityType.PLAYER);

            // PLAYERフィルターなのでMOBは通過しない
            assertFalse(filter.execute(mockCaster, 1, targets));
        }

        @Test
        @DisplayName("toString: フィルター情報を含む文字列を返す")
        void testToString() {
            filter.setFilterType(EntityTypeFilter.EntityType.PLAYER);

            String result = filter.toString();

            assertTrue(result.contains("EntityTypeFilter"));
            assertTrue(result.contains("filterType=PLAYER"));
        }

        @Test
        @DisplayName("execute: 空のターゲットリストの場合はfalse")
        void testExecute_EmptyTargets() {
            List<LivingEntity> targets = new ArrayList<>();

            assertFalse(filter.execute(mockCaster, 1, targets));
        }
    }

    // ==================== EntityType EntityType 列挙型テスト ====================

    @Nested
    @DisplayName("EntityType 列挙型")
    class EntityTypeEnumTests {

        @Test
        @DisplayName("getId: 正しいIDを返す")
        void testGetId() {
            assertEquals("self", EntityTypeFilter.EntityType.SELF.getId());
            assertEquals("player", EntityTypeFilter.EntityType.PLAYER.getId());
            assertEquals("mob", EntityTypeFilter.EntityType.MOB.getId());
            assertEquals("all", EntityTypeFilter.EntityType.ALL.getId());
        }

        @Test
        @DisplayName("getDisplayName: 正しい表示名を返す")
        void testGetDisplayName() {
            assertEquals("発動者のみ", EntityTypeFilter.EntityType.SELF.getDisplayName());
            assertEquals("プレイヤーのみ", EntityTypeFilter.EntityType.PLAYER.getDisplayName());
            assertEquals("Mobのみ", EntityTypeFilter.EntityType.MOB.getDisplayName());
            assertEquals("全て", EntityTypeFilter.EntityType.ALL.getDisplayName());
        }

        @Test
        @DisplayName("fromId: 正しいIDからEntityTypeを取得")
        void testFromId_Valid() {
            assertEquals(EntityTypeFilter.EntityType.SELF, EntityTypeFilter.EntityType.fromId("self"));
            assertEquals(EntityTypeFilter.EntityType.PLAYER, EntityTypeFilter.EntityType.fromId("player"));
            assertEquals(EntityTypeFilter.EntityType.MOB, EntityTypeFilter.EntityType.fromId("mob"));
            assertEquals(EntityTypeFilter.EntityType.ALL, EntityTypeFilter.EntityType.fromId("all"));
        }

        @Test
        @DisplayName("fromId: 大文字小文字を区別しない")
        void testFromId_CaseInsensitive() {
            assertEquals(EntityTypeFilter.EntityType.PLAYER, EntityTypeFilter.EntityType.fromId("PLAYER"));
            assertEquals(EntityTypeFilter.EntityType.PLAYER, EntityTypeFilter.EntityType.fromId("Player"));
            assertEquals(EntityTypeFilter.EntityType.MOB, EntityTypeFilter.EntityType.fromId("MOB"));
        }

        @Test
        @DisplayName("fromId: 無効なIDの場合はALLを返す")
        void testFromId_Invalid() {
            assertEquals(EntityTypeFilter.EntityType.ALL, EntityTypeFilter.EntityType.fromId("invalid"));
            assertEquals(EntityTypeFilter.EntityType.ALL, EntityTypeFilter.EntityType.fromId(""));
        }

        @Test
        @DisplayName("fromId: nullの場合はALLを返す")
        void testFromId_Null() {
            assertEquals(EntityTypeFilter.EntityType.ALL, EntityTypeFilter.EntityType.fromId(null));
        }

        @Test
        @DisplayName("values: 全ての列挙値を取得")
        void testValues() {
            EntityTypeFilter.EntityType[] values = EntityTypeFilter.EntityType.values();
            assertEquals(4, values.length);
        }
    }

    // ==================== TargetGroupFilterComponent テスト ====================

    @Nested
    @DisplayName("TargetGroupFilterComponent: ターゲットグループフィルター")
    class TargetGroupFilterComponentTests {

        @Mock
        private Player mockCaster;

        @Mock
        private Player mockPlayer;

        @Mock
        private Player mockAlly;

        @Mock
        private LivingEntity mockMob;

        @Mock
        private LivingEntity mockEnemyMob;

        private TargetGroupFilterComponent filter;

        @BeforeEach
        void setUp() {
            filter = new TargetGroupFilterComponent();
        }

        @Test
        @DisplayName("getType: FILTERを返す")
        void testGetType() {
            assertEquals(ComponentType.FILTER, filter.getType());
        }

        @Test
        @DisplayName("setGroupFilter/getGroupFilter: グループフィルターの設定と取得")
        void testSetGetGroupFilter() {
            filter.setGroupFilter(com.example.rpgplugin.skill.target.TargetGroupFilter.ALLY);
            assertEquals(com.example.rpgplugin.skill.target.TargetGroupFilter.ALLY, filter.getGroupFilter());

            filter.setGroupFilter(com.example.rpgplugin.skill.target.TargetGroupFilter.BOTH);
            assertEquals(com.example.rpgplugin.skill.target.TargetGroupFilter.BOTH, filter.getGroupFilter());
        }

        @Test
        @DisplayName("test: ENEMYフィルター - 敵のみ通す（デフォルト）")
        void testEnemy() {
            // デフォルトは"enemy"なので設定不要

            // Mobは敵、自分は除外
            assertTrue(filter.test(mockCaster, 1, mockMob));
            assertFalse(filter.test(mockCaster, 1, mockCaster)); // 自分は除外
            assertFalse(filter.test(mockCaster, 1, mockPlayer)); // プレイヤーは除外
        }

        @Test
        @DisplayName("test: ALLYフィルター - 味方のみ通す")
        void testAlly() {
            // settingsに値を設定
            filter.getSettings().set("group", "ally");

            // プレイヤーか自分は味方
            assertTrue(filter.test(mockCaster, 1, mockPlayer));
            assertTrue(filter.test(mockCaster, 1, mockCaster));
            assertFalse(filter.test(mockCaster, 1, mockMob));
        }

        @Test
        @DisplayName("test: BOTHフィルター - 全て通す")
        void testBoth() {
            // settingsに値を設定
            filter.getSettings().set("group", "both");

            assertTrue(filter.test(mockCaster, 1, mockPlayer));
            assertTrue(filter.test(mockCaster, 1, mockMob));
            assertTrue(filter.test(mockCaster, 1, mockCaster));
        }

        @Test
        @DisplayName("toString: フィルター情報を含む文字列を返す")
        void testToString() {
            filter.setGroupFilter(com.example.rpgplugin.skill.target.TargetGroupFilter.ENEMY);

            String result = filter.toString();

            assertTrue(result.contains("TargetGroupFilterComponent"));
            assertTrue(result.contains("groupFilter=ENEMY"));
        }
    }
}

package com.example.rpgplugin.skill.component.cost;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.skill.component.ComponentType;
import com.example.rpgplugin.skill.component.EffectComponent;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * コストコンポーネントのテストクラス
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("コストコンポーネント テスト")
class CostComponentTest {

    @Mock
    private LivingEntity mockCaster;

    @Mock
    private Player mockPlayer;

    @Mock
    private RPGPlugin mockPlugin;

    @Mock
    private PlayerManager mockPlayerManager;

    @Mock
    private RPGPlayer mockRpgPlayer;

    @Mock
    private PlayerInventory mockInventory;

    private List<LivingEntity> targets;

    @BeforeEach
    void setUp() {
        targets = new ArrayList<>();
        when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        when(mockPlayer.getInventory()).thenReturn(mockInventory);
        when(mockCaster.isValid()).thenReturn(true);
    }

    // ==================== CostComponent 基底クラス テスト ====================

    @Nested
    @DisplayName("CostComponent: 基底クラス")
    class CostComponentTests {

        private class TestCostComponent extends CostComponent {
            public TestCostComponent() {
                super("TEST_COST");
            }

            @Override
            protected boolean consumeCost(LivingEntity caster, int level) {
                return true;
            }
        }

        @Test
        @DisplayName("getTypeはCOST")
        void testGetType() {
            TestCostComponent component = new TestCostComponent();

            assertEquals(ComponentType.COST, component.getType());
        }

        @Test
        @DisplayName("executeはコスト消費後に子を実行")
        void testExecute_WithChildren() {
            TestCostComponent component = new TestCostComponent();
            EffectComponent child = mock(EffectComponent.class);
            component.addChild(child);

            when(child.execute(any(), anyInt(), any())).thenReturn(true);

            // targetsが空だとexecuteChildrenがfalseを返すためターゲットを追加
            targets.add(mockCaster);

            boolean result = component.execute(mockCaster, 1, targets);

            assertTrue(result);
            verify(child).execute(eq(mockCaster), eq(1), eq(targets));
        }

        @Test
        @DisplayName("executeはコスト消費失敗時にfalse")
        void testExecute_CostConsumeFails() {
            TestCostComponent component = new TestCostComponent() {
                @Override
                protected boolean consumeCost(LivingEntity caster, int level) {
                    return false;
                }
            };
            EffectComponent child = mock(EffectComponent.class);
            component.addChild(child);

            boolean result = component.execute(mockCaster, 1, targets);

            assertFalse(result);
            verify(child, never()).execute(any(), anyInt(), any());
        }

        @Test
        @DisplayName("calculateCost: 基本計算")
        void testCalculateCost_Basic() {
            TestCostComponent component = new TestCostComponent();
            component.getSettings().set("base", 10.0);
            component.getSettings().set("per_level", 2.0);

            assertEquals(10.0, component.calculateCost(1), "レベル1はbaseのみ");
            assertEquals(12.0, component.calculateCost(2), "レベル2はbase+per_level");
            assertEquals(14.0, component.calculateCost(3), "レベル3はbase+2*per_level");
        }

        @Test
        @DisplayName("calculateCost: 最小値制限")
        void testCalculateCost_MinConstraint() {
            TestCostComponent component = new TestCostComponent();
            component.getSettings().set("base", -10.0);
            component.getSettings().set("min", 5.0);

            assertEquals(5.0, component.calculateCost(1), "最小値が適用される");
        }

        @Test
        @DisplayName("calculateCost: 最大値制限")
        void testCalculateCost_MaxConstraint() {
            TestCostComponent component = new TestCostComponent();
            component.getSettings().set("base", 100.0);
            component.getSettings().set("max", 50.0);

            assertEquals(50.0, component.calculateCost(1), "最大値が適用される");
        }

        @Test
        @DisplayName("calculateCost: デフォルト値")
        void testCalculateCost_Defaults() {
            TestCostComponent component = new TestCostComponent();

            assertEquals(0.0, component.calculateCost(1), "デフォルトは0");
        }

        @Test
        @DisplayName("getCostType: デフォルトはMANA")
        void testGetCostType_Default() {
            TestCostComponent component = new TestCostComponent();

            assertEquals(com.example.rpgplugin.skill.SkillCostType.MANA, component.getCostType());
        }
    }

    // ==================== ManaCostComponent テスト ====================

    @Nested
    @DisplayName("ManaCostComponent: マナ消費")
    class ManaCostComponentTests {

        @Test
        @DisplayName("プレイヤー以外は成功")
        void testNonPlayer_Succeeds() {
            ManaCostComponent component = new ManaCostComponent();

            assertTrue(component.consumeCost(mockCaster, 1));
        }

        @Test
        @DisplayName("nullコストは成功")
        void testZeroCost_Succeeds() {
            ManaCostComponent component = new ManaCostComponent();
            component.getSettings().set("base", 0.0);

            assertTrue(component.consumeCost(mockPlayer, 1));
        }

        @Test
        @DisplayName("plugin=nullは成功（テスト環境）")
        void testNullPlugin_Succeeds() {
            ManaCostComponent component = new ManaCostComponent(null);
            component.getSettings().set("base", 10.0);

            assertTrue(component.consumeCost(mockPlayer, 1));
        }

        @Test
        @DisplayName("正常にマナ消費")
        void testConsumeMana_Success() {
            ManaCostComponent component = new ManaCostComponent(mockPlugin);
            component.getSettings().set("base", 10.0);

            when(mockPlugin.getPlayerManager()).thenReturn(mockPlayerManager);
            when(mockPlayerManager.getRPGPlayer(any(UUID.class))).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.hasMana(10)).thenReturn(true);
            when(mockRpgPlayer.consumeMana(10)).thenReturn(true);

            assertTrue(component.consumeCost(mockPlayer, 1));
            verify(mockRpgPlayer).consumeMana(10);
        }

        @Test
        @DisplayName("マナ不足は失敗")
        void testInsufficientMana_Fails() {
            ManaCostComponent component = new ManaCostComponent(mockPlugin);
            component.getSettings().set("base", 10.0);

            when(mockPlugin.getPlayerManager()).thenReturn(mockPlayerManager);
            when(mockPlayerManager.getRPGPlayer(any(UUID.class))).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.hasMana(10)).thenReturn(false);

            assertFalse(component.consumeCost(mockPlayer, 1));
            verify(mockRpgPlayer, never()).consumeMana(anyInt());
        }

        @Test
        @DisplayName("RPGPlayerがnullは失敗")
        void testNullRpgPlayer_Fails() {
            ManaCostComponent component = new ManaCostComponent(mockPlugin);
            component.getSettings().set("base", 10.0);

            when(mockPlugin.getPlayerManager()).thenReturn(mockPlayerManager);
            when(mockPlayerManager.getRPGPlayer(any(UUID.class))).thenReturn(null);

            assertFalse(component.consumeCost(mockPlayer, 1));
        }

        @Test
        @DisplayName("レベル依存コスト計算")
        void testLevelDependentCost() {
            ManaCostComponent component = new ManaCostComponent(mockPlugin);
            component.getSettings().set("base", 10.0);
            component.getSettings().set("per_level", 5.0);

            when(mockPlugin.getPlayerManager()).thenReturn(mockPlayerManager);
            when(mockPlayerManager.getRPGPlayer(any(UUID.class))).thenReturn(mockRpgPlayer);
            when(mockRpgPlayer.hasMana(anyInt())).thenReturn(true);
            when(mockRpgPlayer.consumeMana(anyInt())).thenReturn(true);

            component.consumeCost(mockPlayer, 3);

            verify(mockRpgPlayer).consumeMana(20); // 10 + 5*2 = 20
        }

        @Test
        @DisplayName("getCostTypeはMANA")
        void testGetCostType() {
            ManaCostComponent component = new ManaCostComponent();

            assertEquals(com.example.rpgplugin.skill.SkillCostType.MANA, component.getCostType());
        }
    }

    // ==================== StaminaCostComponent テスト ====================

    @Nested
    @DisplayName("StaminaCostComponent: スタミナ消費")
    class StaminaCostComponentTests {

        @Test
        @DisplayName("プレイヤー以外は成功")
        void testNonPlayer_Succeeds() {
            StaminaCostComponent component = new StaminaCostComponent();

            assertTrue(component.consumeCost(mockCaster, 1));
        }

        @Test
        @DisplayName("nullコストは成功")
        void testZeroCost_Succeeds() {
            StaminaCostComponent component = new StaminaCostComponent();
            component.getSettings().set("base", 0.0);

            assertTrue(component.consumeCost(mockPlayer, 1));
        }

        @Test
        @DisplayName("正のコストでも成功（未実装）")
        void testPositiveCost_Succeeds() {
            StaminaCostComponent component = new StaminaCostComponent();
            component.getSettings().set("base", 10.0);

            assertTrue(component.consumeCost(mockPlayer, 1));
        }

        @Test
        @DisplayName("getCostTypeはMANA（暫定）")
        void testGetCostType() {
            StaminaCostComponent component = new StaminaCostComponent();

            assertEquals(com.example.rpgplugin.skill.SkillCostType.MANA, component.getCostType());
        }
    }

    // ==================== HpCostComponent テスト ====================

    @Nested
    @DisplayName("HpCostComponent: HP消費")
    class HpCostComponentTests {

        @Test
        @DisplayName("nullキャスターは失敗")
        void testNullCaster_Fails() {
            HpCostComponent component = new HpCostComponent();

            assertFalse(component.consumeCost(null, 1));
        }

        @Test
        @DisplayName("無効なキャスターは失敗")
        void testInvalidCaster_Fails() {
            HpCostComponent component = new HpCostComponent();
            when(mockCaster.isValid()).thenReturn(false);

            assertFalse(component.consumeCost(mockCaster, 1));
        }

        @Test
        @DisplayName("nullコストは成功")
        void testZeroCost_Succeeds() {
            HpCostComponent component = new HpCostComponent();
            component.getSettings().set("base", 0.0);

            assertTrue(component.consumeCost(mockCaster, 1));
        }

        @Test
        @DisplayName("正常にHP消費")
        void testConsumeHp_Success() {
            HpCostComponent component = new HpCostComponent();
            component.getSettings().set("base", 5.0);
            when(mockCaster.getHealth()).thenReturn(20.0);

            assertTrue(component.consumeCost(mockCaster, 1));
            verify(mockCaster).setHealth(15.0);
        }

        @Test
        @DisplayName("死亡許可なしでHP不足は失敗")
        void testInsufficientHp_NoDeath_Fails() {
            HpCostComponent component = new HpCostComponent();
            component.getSettings().set("base", 10.0);
            when(mockCaster.getHealth()).thenReturn(5.0);

            assertFalse(component.consumeCost(mockCaster, 1));
            verify(mockCaster, never()).setHealth(anyDouble());
        }

        @Test
        @DisplayName("死亡許可でHP消費")
        void testConsumeHp_AllowDeath() {
            HpCostComponent component = new HpCostComponent();
            component.getSettings().set("base", 10.0);
            component.getSettings().set("allow_death", true);
            when(mockCaster.getHealth()).thenReturn(5.0);

            assertTrue(component.consumeCost(mockCaster, 1));
            verify(mockCaster).setHealth(0.0);
        }

        @Test
        @DisplayName("HP消費は0以下にならない")
        void testHpDoesNotGoNegative() {
            HpCostComponent component = new HpCostComponent();
            component.getSettings().set("base", 100.0);
            component.getSettings().set("allow_death", true); // 死亡許可でHP0まで消費
            when(mockCaster.getHealth()).thenReturn(50.0);

            assertTrue(component.consumeCost(mockCaster, 1));
            verify(mockCaster).setHealth(0.0);
        }

        @Test
        @DisplayName("getCostTypeはHP")
        void testGetCostType() {
            HpCostComponent component = new HpCostComponent();

            assertEquals(com.example.rpgplugin.skill.SkillCostType.HP, component.getCostType());
        }

        @Test
        @DisplayName("レベル依存コスト計算")
        void testLevelDependentCost() {
            HpCostComponent component = new HpCostComponent();
            component.getSettings().set("base", 5.0);
            component.getSettings().set("per_level", 2.0);
            when(mockCaster.getHealth()).thenReturn(20.0);

            component.consumeCost(mockCaster, 3);

            // 5 + 2*2 = 9
            verify(mockCaster).setHealth(11.0);
        }

        @Test
        @DisplayName("最小値制限")
        void testMinConstraint() {
            HpCostComponent component = new HpCostComponent();
            component.getSettings().set("base", -5.0);
            component.getSettings().set("min", 1.0);
            when(mockCaster.getHealth()).thenReturn(20.0);

            component.consumeCost(mockCaster, 1);

            verify(mockCaster).setHealth(19.0);
        }

        @Test
        @DisplayName("最大値制限")
        void testMaxConstraint() {
            HpCostComponent component = new HpCostComponent();
            component.getSettings().set("base", 30.0);
            component.getSettings().set("max", 10.0);
            when(mockCaster.getHealth()).thenReturn(20.0);

            component.consumeCost(mockCaster, 1);

            verify(mockCaster).setHealth(10.0);
        }
    }

    // ==================== ItemCostComponent テスト ====================

    @Nested
    @DisplayName("ItemCostComponent: アイテム消費")
    class ItemCostComponentTests {

        @Test
        @DisplayName("プレイヤー以外は成功")
        void testNonPlayer_Succeeds() {
            ItemCostComponent component = new ItemCostComponent();

            assertTrue(component.consumeCost(mockCaster, 1));
        }

        @Test
        @DisplayName("無効なアイテムは失敗")
        void testInvalidItem_Fails() {
            ItemCostComponent component = new ItemCostComponent();
            component.getSettings().set("item", "INVALID_ITEM");

            assertFalse(component.consumeCost(mockPlayer, 1));
        }

        @Test
        @DisplayName("null数量は成功")
        void testZeroAmount_Succeeds() {
            ItemCostComponent component = new ItemCostComponent();
            component.getSettings().set("item", "DIAMOND");
            component.getSettings().set("amount", 0);

            assertTrue(component.consumeCost(mockPlayer, 1));
        }

        @Test
        @DisplayName("アイテム不足は失敗")
        void testInsufficientItem_Fails() {
            ItemCostComponent component = new ItemCostComponent();
            component.getSettings().set("item", "DIAMOND");
            component.getSettings().set("amount", 10);
            when(mockInventory.getContents()).thenReturn(new ItemStack[0]);

            assertFalse(component.consumeCost(mockPlayer, 1));
        }

        @Test
        @DisplayName("check_onlyは消費せず成功")
        void testCheckOnly_Succeeds() {
            ItemCostComponent component = new ItemCostComponent();
            component.getSettings().set("item", "DIAMOND");
            component.getSettings().set("amount", 1);
            component.getSettings().set("check_only", true);

            ItemStack diamond = new ItemStack(Material.DIAMOND, 5);
            when(mockInventory.getContents()).thenReturn(new ItemStack[]{diamond});

            assertTrue(component.consumeCost(mockPlayer, 1));
            assertEquals(5, diamond.getAmount(), "アイテムは消費されない");
        }

        @Test
        @DisplayName("正常にアイテム消費")
        void testConsumeItem_Success() {
            ItemCostComponent component = new ItemCostComponent();
            component.getSettings().set("item", "DIAMOND");
            component.getSettings().set("amount", 3);

            ItemStack diamond = new ItemStack(Material.DIAMOND, 5);
            ItemStack[] contents = new ItemStack[]{diamond, null};
            when(mockInventory.getContents()).thenReturn(contents);
            when(mockInventory.getItem(anyInt())).thenAnswer(invocation -> {
                int index = invocation.getArgument(0);
                return contents[index];
            });
            doAnswer(invocation -> {
                int index = invocation.getArgument(0);
                ItemStack item = invocation.getArgument(1);
                contents[index] = item;
                return null;
            }).when(mockInventory).setItem(anyInt(), any());

            assertTrue(component.consumeCost(mockPlayer, 1));
            assertEquals(2, diamond.getAmount(), "アイテムが消費される");
        }

        @Test
        @DisplayName("複数スタックから消費")
        void testConsumeFromMultipleStacks() {
            ItemCostComponent component = new ItemCostComponent();
            component.getSettings().set("item", "DIAMOND");
            component.getSettings().set("amount", 7);

            ItemStack stack1 = new ItemStack(Material.DIAMOND, 5);
            ItemStack stack2 = new ItemStack(Material.DIAMOND, 5);
            ItemStack[] contents = new ItemStack[]{stack1, stack2};
            when(mockInventory.getContents()).thenReturn(contents);
            when(mockInventory.getItem(anyInt())).thenAnswer(invocation -> {
                int index = invocation.getArgument(0);
                return index < contents.length ? contents[index] : null;
            });
            doAnswer(invocation -> {
                int index = invocation.getArgument(0);
                ItemStack item = invocation.getArgument(1);
                if (index < contents.length) {
                    contents[index] = item;
                }
                return null;
            }).when(mockInventory).setItem(anyInt(), any());

            assertTrue(component.consumeCost(mockPlayer, 1));
            // 1つ目のスタックは完全消費でsetItem(0, null)が呼ばれる
            verify(mockInventory).setItem(eq(0), isNull());
            // stack2はsetAmountで2個残る
            assertEquals(3, stack2.getAmount(), "2つ目のスタックは残り");
        }

        @Test
        @DisplayName("スタック全体を消費")
        void testConsumeEntireStack() {
            ItemCostComponent component = new ItemCostComponent();
            component.getSettings().set("item", "DIAMOND");
            component.getSettings().set("amount", 5);

            ItemStack diamond = new ItemStack(Material.DIAMOND, 5);
            ItemStack[] contents = new ItemStack[]{diamond};
            when(mockInventory.getContents()).thenReturn(contents);
            when(mockInventory.getItem(anyInt())).thenAnswer(invocation -> {
                int index = invocation.getArgument(0);
                return index < contents.length ? contents[index] : null;
            });
            doAnswer(invocation -> {
                int index = invocation.getArgument(0);
                ItemStack item = invocation.getArgument(1);
                if (index < contents.length) {
                    contents[index] = item;
                }
                return null;
            }).when(mockInventory).setItem(anyInt(), any());

            assertTrue(component.consumeCost(mockPlayer, 1));
            verify(mockInventory).setItem(eq(0), isNull());
        }

        @Test
        @DisplayName("レベル依存数量計算")
        void testLevelDependentAmount() {
            ItemCostComponent component = new ItemCostComponent();
            component.getSettings().set("item", "DIAMOND");
            component.getSettings().set("amount", 5);
            component.getSettings().set("amount_per_level", 2);

            ItemStack diamond = new ItemStack(Material.DIAMOND, 20);
            ItemStack[] contents = new ItemStack[]{diamond};
            when(mockInventory.getContents()).thenReturn(contents);
            when(mockInventory.getItem(anyInt())).thenAnswer(invocation -> {
                int index = invocation.getArgument(0);
                return index < contents.length ? contents[index] : null;
            });
            doAnswer(invocation -> {
                int index = invocation.getArgument(0);
                ItemStack item = invocation.getArgument(1);
                if (index < contents.length) {
                    contents[index] = item;
                }
                return null;
            }).when(mockInventory).setItem(anyInt(), any());

            component.consumeCost(mockPlayer, 3);

            assertEquals(11, diamond.getAmount()); // 5 + 2*2 = 9消費
        }

        @Test
        @DisplayName("getRequiredItem: アイテムスタックを取得")
        void testGetRequiredItem() {
            ItemCostComponent component = new ItemCostComponent();
            component.getSettings().set("item", "DIAMOND");
            component.getSettings().set("amount", 5);

            ItemStack result = component.getRequiredItem(1);

            assertNotNull(result);
            assertEquals(Material.DIAMOND, result.getType());
            assertEquals(5, result.getAmount());
        }

        @Test
        @DisplayName("getRequiredItem: 無効なアイテムはnull")
        void testGetRequiredItem_InvalidItem() {
            ItemCostComponent component = new ItemCostComponent();
            component.getSettings().set("item", "INVALID");

            ItemStack result = component.getRequiredItem(1);

            assertNull(result);
        }

        @Test
        @DisplayName("getRequiredItem: 最小1")
        void testGetRequiredItem_MinOne() {
            ItemCostComponent component = new ItemCostComponent();
            component.getSettings().set("item", "DIAMOND");
            component.getSettings().set("amount", -5);

            ItemStack result = component.getRequiredItem(1);

            assertEquals(1, result.getAmount());
        }

        @Test
        @DisplayName("デフォルトはDIAMOND")
        void testDefaultItem() {
            ItemCostComponent component = new ItemCostComponent();
            // item設定なし

            ItemStack result = component.getRequiredItem(1);

            assertEquals(Material.DIAMOND, result.getType());
        }

        @Test
        @DisplayName("インベントリのnullアイテムはスキップ")
        void testNullItemsSkipped() {
            ItemCostComponent component = new ItemCostComponent();
            component.getSettings().set("item", "DIAMOND");
            component.getSettings().set("amount", 1);

            ItemStack diamond = new ItemStack(Material.DIAMOND, 5);
            ItemStack[] contents = new ItemStack[]{null, diamond, null};
            when(mockInventory.getContents()).thenReturn(contents);
            when(mockInventory.getItem(anyInt())).thenAnswer(invocation -> {
                int index = invocation.getArgument(0);
                return index < contents.length ? contents[index] : null;
            });
            doAnswer(invocation -> {
                int index = invocation.getArgument(0);
                ItemStack item = invocation.getArgument(1);
                if (index < contents.length) {
                    contents[index] = item;
                }
                return null;
            }).when(mockInventory).setItem(anyInt(), any());

            assertTrue(component.consumeCost(mockPlayer, 1));
        }

        @Test
        @DisplayName("異なるアイテムはスキップ")
        void testDifferentItemsSkipped() {
            ItemCostComponent component = new ItemCostComponent();
            component.getSettings().set("item", "DIAMOND");
            component.getSettings().set("amount", 1);

            ItemStack iron = new ItemStack(Material.IRON_INGOT, 10);
            ItemStack diamond = new ItemStack(Material.DIAMOND, 5);
            ItemStack[] contents = new ItemStack[]{iron, diamond};
            when(mockInventory.getContents()).thenReturn(contents);
            when(mockInventory.getItem(anyInt())).thenAnswer(invocation -> {
                int index = invocation.getArgument(0);
                return index < contents.length ? contents[index] : null;
            });
            doAnswer(invocation -> {
                int index = invocation.getArgument(0);
                ItemStack item = invocation.getArgument(1);
                if (index < contents.length) {
                    contents[index] = item;
                }
                return null;
            }).when(mockInventory).setItem(anyInt(), any());

            assertTrue(component.consumeCost(mockPlayer, 1));
            assertEquals(10, iron.getAmount(), "異種アイテムは消費されない");
            assertEquals(4, diamond.getAmount());
        }
    }
}

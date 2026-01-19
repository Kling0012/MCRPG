package com.example.rpgplugin.rpgclass.requirements;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ItemRequirementのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ItemRequirement Tests")
class ItemRequirementTest {

    @Mock
    private Player mockPlayer;

    @Mock
    private PlayerInventory mockInventory;

    @Mock
    private ConfigurationSection mockConfig;

    @BeforeEach
    void setUp() {
        when(mockPlayer.getInventory()).thenReturn(mockInventory);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("コンストラクタ - 正しい値")
        void constructorWithValidValues() {
            ItemRequirement requirement = new ItemRequirement("Test Item", 5, false);
            assertEquals("Test Item", requirement.getItemName());
            assertEquals(5, requirement.getRequiredAmount());
            assertFalse(requirement.isConsumeOnUse());
        }

        @Test
        @DisplayName("コンストラクタ - 消費フラグtrue")
        void constructorWithConsumeTrue() {
            ItemRequirement requirement = new ItemRequirement("Consumable", 1, true);
            assertEquals("Consumable", requirement.getItemName());
            assertEquals(1, requirement.getRequiredAmount());
            assertTrue(requirement.isConsumeOnUse());
        }

        @Test
        @DisplayName("コンストラクタ - 0は1に調整される")
        void constructorWithZeroAdjustsToOne() {
            ItemRequirement requirement = new ItemRequirement("Test", 0, false);
            assertEquals(1, requirement.getRequiredAmount());
        }

        @Test
        @DisplayName("コンストラクタ - 負の値は1に調整される")
        void constructorWithNegativeAdjustsToOne() {
            ItemRequirement requirement = new ItemRequirement("Test", -10, true);
            assertEquals(1, requirement.getRequiredAmount());
        }

        @Test
        @DisplayName("コンストラクタ - 大きな値")
        void constructorWithLargeValue() {
            ItemRequirement requirement = new ItemRequirement("Rare Item", 999, false);
            assertEquals(999, requirement.getRequiredAmount());
        }
    }

    @Nested
    @DisplayName("check() Tests")
    class CheckTests {

        @Test
        @DisplayName("check - nullプレイヤーはfalse")
        void checkWithNullPlayer() {
            ItemRequirement requirement = new ItemRequirement("Test Item", 1, false);
            assertFalse(requirement.check(null));
        }

        @Test
        @DisplayName("check - 空インベントリはfalse")
        void checkWithEmptyInventory() {
            when(mockInventory.getContents()).thenReturn(new ItemStack[0]);
            ItemRequirement requirement = new ItemRequirement("Test Item", 1, false);
            assertFalse(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - nullアイテムのみのインベントリ")
        void checkWithNullItemsOnly() {
            ItemStack[] items = {null, null, null};
            when(mockInventory.getContents()).thenReturn(items);
            ItemRequirement requirement = new ItemRequirement("Test Item", 1, false);
            assertFalse(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - アイテムなし（metaなし）")
        void checkWithItemWithoutMeta() {
            ItemStack mockItem = mock(ItemStack.class);
            when(mockItem.hasItemMeta()).thenReturn(false);

            ItemStack[] items = {mockItem};
            when(mockInventory.getContents()).thenReturn(items);
            ItemRequirement requirement = new ItemRequirement("Test Item", 1, false);
            assertFalse(requirement.check(mockPlayer));
        }
    }

    @Nested
    @DisplayName("getDescription() Tests")
    class GetDescriptionTests {

        @Test
        @DisplayName("getDescription - 非消費アイテム")
        void getDescriptionWithNonConsumable() {
            ItemRequirement requirement = new ItemRequirement("Test Item", 5, false);
            assertEquals("所持:Test Item x5", requirement.getDescription());
        }

        @Test
        @DisplayName("getDescription - 消費アイテム")
        void getDescriptionWithConsumable() {
            ItemRequirement requirement = new ItemRequirement("Consumable", 1, true);
            assertEquals("消費:Consumable x1", requirement.getDescription());
        }

        @Test
        @DisplayName("getDescription - 大きな個数")
        void getDescriptionWithLargeAmount() {
            ItemRequirement requirement = new ItemRequirement("Rare Item", 999, false);
            assertEquals("所持:Rare Item x999", requirement.getDescription());
        }

        @Test
        @DisplayName("getDescription - 1個")
        void getDescriptionWithOne() {
            ItemRequirement requirement = new ItemRequirement("Single Item", 1, true);
            assertEquals("消費:Single Item x1", requirement.getDescription());
        }
    }

    @Nested
    @DisplayName("getType() Tests")
    class GetTypeTests {

        @Test
        @DisplayName("getType - itemを返す")
        void getTypeReturnsItem() {
            ItemRequirement requirement = new ItemRequirement("Test", 1, false);
            assertEquals("item", requirement.getType());
        }
    }

    @Nested
    @DisplayName("Getter Methods Tests")
    class GetterMethodsTests {

        @Test
        @DisplayName("getItemName - 正しく返す")
        void getItemNameReturnsCorrect() {
            ItemRequirement requirement = new ItemRequirement("Special Item", 10, false);
            assertEquals("Special Item", requirement.getItemName());
        }

        @Test
        @DisplayName("getRequiredAmount - 正しく返す")
        void getRequiredAmountReturnsCorrect() {
            ItemRequirement requirement = new ItemRequirement("Test", 25, true);
            assertEquals(25, requirement.getRequiredAmount());
        }

        @Test
        @DisplayName("isConsumeOnUse - true")
        void isConsumeOnUseReturnsTrue() {
            ItemRequirement requirement = new ItemRequirement("Test", 1, true);
            assertTrue(requirement.isConsumeOnUse());
        }

        @Test
        @DisplayName("isConsumeOnUse - false")
        void isConsumeOnUseReturnsFalse() {
            ItemRequirement requirement = new ItemRequirement("Test", 1, false);
            assertFalse(requirement.isConsumeOnUse());
        }
    }

    @Nested
    @DisplayName("parse() Tests")
    class ParseTests {

        @Test
        @DisplayName("parse - 正常パース")
        void parseWithValidValues() {
            when(mockConfig.getString("item", "UNKNOWN")).thenReturn("Test Item");
            when(mockConfig.getInt("amount", 1)).thenReturn(5);
            when(mockConfig.getBoolean("consume", false)).thenReturn(true);

            ItemRequirement requirement = ItemRequirement.parse(mockConfig);
            assertEquals("Test Item", requirement.getItemName());
            assertEquals(5, requirement.getRequiredAmount());
            assertTrue(requirement.isConsumeOnUse());
        }

        @Test
        @DisplayName("parse - デフォルト値")
        void parseWithDefaults() {
            when(mockConfig.getString("item", "UNKNOWN")).thenReturn("UNKNOWN");
            when(mockConfig.getInt("amount", 1)).thenReturn(1);
            when(mockConfig.getBoolean("consume", false)).thenReturn(false);

            ItemRequirement requirement = ItemRequirement.parse(mockConfig);
            assertEquals("UNKNOWN", requirement.getItemName());
            assertEquals(1, requirement.getRequiredAmount());
            assertFalse(requirement.isConsumeOnUse());
        }

        @Test
        @DisplayName("parse - 消費フラグなし")
        void parseWithoutConsumeFlag() {
            when(mockConfig.getString("item", "UNKNOWN")).thenReturn("Item");
            when(mockConfig.getInt("amount", 1)).thenReturn(10);
            when(mockConfig.getBoolean("consume", false)).thenReturn(false);

            ItemRequirement requirement = ItemRequirement.parse(mockConfig);
            assertEquals("Item", requirement.getItemName());
            assertEquals(10, requirement.getRequiredAmount());
            assertFalse(requirement.isConsumeOnUse());
        }

        @Test
        @DisplayName("parse - 0は1に調整される")
        void parseWithZeroAdjustsToOne() {
            when(mockConfig.getString("item", "UNKNOWN")).thenReturn("Item");
            when(mockConfig.getInt("amount", 1)).thenReturn(0);
            when(mockConfig.getBoolean("consume", false)).thenReturn(false);

            ItemRequirement requirement = ItemRequirement.parse(mockConfig);
            assertEquals(1, requirement.getRequiredAmount());
        }

        @Test
        @DisplayName("parse - 負の値は1に調整される")
        void parseWithNegativeAdjustsToOne() {
            when(mockConfig.getString("item", "UNKNOWN")).thenReturn("Item");
            when(mockConfig.getInt("amount", 1)).thenReturn(-5);
            when(mockConfig.getBoolean("consume", false)).thenReturn(true);

            ItemRequirement requirement = ItemRequirement.parse(mockConfig);
            assertEquals(1, requirement.getRequiredAmount());
            assertTrue(requirement.isConsumeOnUse());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("複数インスタンス - 別々の設定")
        void multipleInstancesHaveDifferentSettings() {
            ItemRequirement req1 = new ItemRequirement("Item1", 1, false);
            ItemRequirement req2 = new ItemRequirement("Item2", 10, true);
            ItemRequirement req3 = new ItemRequirement("Item3", 100, false);

            assertEquals("Item1", req1.getItemName());
            assertEquals(1, req1.getRequiredAmount());
            assertFalse(req1.isConsumeOnUse());

            assertEquals("Item2", req2.getItemName());
            assertEquals(10, req2.getRequiredAmount());
            assertTrue(req2.isConsumeOnUse());

            assertEquals("Item3", req3.getItemName());
            assertEquals(100, req3.getRequiredAmount());
            assertFalse(req3.isConsumeOnUse());
        }

        @Test
        @DisplayName("空文字列アイテム名")
        void emptyItemName() {
            ItemRequirement requirement = new ItemRequirement("", 1, false);
            assertEquals("", requirement.getItemName());
            assertEquals("所持: x1", requirement.getDescription());
        }
    }

    @Nested
    @DisplayName("check() - Item Counting Logic Tests")
    class ItemCountingTests {

        @Mock
        private ItemMeta mockItemMeta;

        @Test
        @DisplayName("check - 一致するアイテムが1つ（個数1）")
        void checkWithSingleMatchingItem() {
            ItemStack mockItem = mock(ItemStack.class);
            when(mockItem.hasItemMeta()).thenReturn(true);
            when(mockItem.getItemMeta()).thenReturn(mockItemMeta);
            when(mockItemMeta.hasDisplayName()).thenReturn(true);
            when(mockItemMeta.displayName()).thenReturn(Component.text("Test Item"));
            when(mockItem.getAmount()).thenReturn(1);

            ItemStack[] items = {mockItem};
            when(mockInventory.getContents()).thenReturn(items);

            ItemRequirement requirement = new ItemRequirement("Test Item", 1, false);
            assertTrue(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - 一致するアイテムが個数不足")
        void checkWithInsufficientItemCount() {
            ItemStack mockItem = mock(ItemStack.class);
            when(mockItem.hasItemMeta()).thenReturn(true);
            when(mockItem.getItemMeta()).thenReturn(mockItemMeta);
            when(mockItemMeta.hasDisplayName()).thenReturn(true);
            when(mockItemMeta.displayName()).thenReturn(Component.text("Test Item"));
            when(mockItem.getAmount()).thenReturn(2);

            ItemStack[] items = {mockItem};
            when(mockInventory.getContents()).thenReturn(items);

            ItemRequirement requirement = new ItemRequirement("Test Item", 5, false);
            assertFalse(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - 複数の一致するアイテムで個数を累積")
        void checkWithMultipleMatchingItemsAccumulatingCount() {
            ItemStack mockItem1 = mock(ItemStack.class);
            ItemStack mockItem2 = mock(ItemStack.class);
            ItemStack mockItem3 = mock(ItemStack.class);

            when(mockItem1.hasItemMeta()).thenReturn(true);
            when(mockItem1.getItemMeta()).thenReturn(mockItemMeta);
            when(mockItem1.getAmount()).thenReturn(3);

            when(mockItem2.hasItemMeta()).thenReturn(true);
            when(mockItem2.getItemMeta()).thenReturn(mockItemMeta);
            when(mockItem2.getAmount()).thenReturn(2);

            when(mockItem3.hasItemMeta()).thenReturn(true);
            when(mockItem3.getItemMeta()).thenReturn(mockItemMeta);
            when(mockItem3.getAmount()).thenReturn(1);

            when(mockItemMeta.hasDisplayName()).thenReturn(true);
            when(mockItemMeta.displayName()).thenReturn(Component.text("Test Item"));

            ItemStack[] items = {mockItem1, mockItem2, mockItem3};
            when(mockInventory.getContents()).thenReturn(items);

            ItemRequirement requirement = new ItemRequirement("Test Item", 5, false);
            assertTrue(requirement.check(mockPlayer)); // 3 + 2 + 1 = 6 >= 5
        }

        @Test
        @DisplayName("check - 一致しないアイテム名")
        void checkWithNonMatchingItemName() {
            ItemStack mockItem = mock(ItemStack.class);
            when(mockItem.hasItemMeta()).thenReturn(true);
            when(mockItem.getItemMeta()).thenReturn(mockItemMeta);
            when(mockItemMeta.hasDisplayName()).thenReturn(true);
            when(mockItemMeta.displayName()).thenReturn(Component.text("Different Item"));

            ItemStack[] items = {mockItem};
            when(mockInventory.getContents()).thenReturn(items);

            ItemRequirement requirement = new ItemRequirement("Test Item", 1, false);
            assertFalse(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - displayNameなしのアイテム")
        void checkWithItemNoDisplayName() {
            ItemStack mockItem = mock(ItemStack.class);
            when(mockItem.hasItemMeta()).thenReturn(true);
            when(mockItem.getItemMeta()).thenReturn(mockItemMeta);
            when(mockItemMeta.hasDisplayName()).thenReturn(false);

            ItemStack[] items = {mockItem};
            when(mockInventory.getContents()).thenReturn(items);

            ItemRequirement requirement = new ItemRequirement("Test Item", 1, false);
            assertFalse(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - 混合アイテム（一致しないものを含む）")
        void checkWithMixedItems() {
            ItemStack matchingItem = mock(ItemStack.class);
            ItemStack nonMatchingItem = mock(ItemStack.class);
            ItemStack nullItem = null;

            ItemMeta matchingMeta = mock(ItemMeta.class);
            ItemMeta nonMatchingMeta = mock(ItemMeta.class);

            when(matchingItem.hasItemMeta()).thenReturn(true);
            when(matchingItem.getItemMeta()).thenReturn(matchingMeta);
            when(matchingItem.getAmount()).thenReturn(5);
            when(matchingMeta.hasDisplayName()).thenReturn(true);
            when(matchingMeta.displayName()).thenReturn(Component.text("Target Item"));

            when(nonMatchingItem.hasItemMeta()).thenReturn(true);
            when(nonMatchingItem.getItemMeta()).thenReturn(nonMatchingMeta);
            when(nonMatchingMeta.hasDisplayName()).thenReturn(true);
            when(nonMatchingMeta.displayName()).thenReturn(Component.text("Other Item"));

            ItemStack[] items = {matchingItem, nonMatchingItem, nullItem};
            when(mockInventory.getContents()).thenReturn(items);

            ItemRequirement requirement = new ItemRequirement("Target Item", 5, false);
            assertTrue(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - 大きな個数のアイテム")
        void checkWithLargeItemAmount() {
            ItemStack mockItem = mock(ItemStack.class);
            when(mockItem.hasItemMeta()).thenReturn(true);
            when(mockItem.getItemMeta()).thenReturn(mockItemMeta);
            when(mockItemMeta.hasDisplayName()).thenReturn(true);
            when(mockItemMeta.displayName()).thenReturn(Component.text("Stackable Item"));
            when(mockItem.getAmount()).thenReturn(64);

            ItemStack[] items = {mockItem};
            when(mockInventory.getContents()).thenReturn(items);

            ItemRequirement requirement = new ItemRequirement("Stackable Item", 50, false);
            assertTrue(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - 必要個数ちょうど")
        void checkWithExactRequiredAmount() {
            ItemStack mockItem = mock(ItemStack.class);
            when(mockItem.hasItemMeta()).thenReturn(true);
            when(mockItem.getItemMeta()).thenReturn(mockItemMeta);
            when(mockItemMeta.hasDisplayName()).thenReturn(true);
            when(mockItemMeta.displayName()).thenReturn(Component.text("Exact Item"));
            when(mockItem.getAmount()).thenReturn(10);

            ItemStack[] items = {mockItem};
            when(mockInventory.getContents()).thenReturn(items);

            ItemRequirement requirement = new ItemRequirement("Exact Item", 10, false);
            assertTrue(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - 必要個数より1つ少ない")
        void checkWithOneLessThanRequired() {
            ItemStack mockItem = mock(ItemStack.class);
            when(mockItem.hasItemMeta()).thenReturn(true);
            when(mockItem.getItemMeta()).thenReturn(mockItemMeta);
            when(mockItemMeta.hasDisplayName()).thenReturn(true);
            when(mockItemMeta.displayName()).thenReturn(Component.text("Almost Item"));
            when(mockItem.getAmount()).thenReturn(9);

            ItemStack[] items = {mockItem};
            when(mockInventory.getContents()).thenReturn(items);

            ItemRequirement requirement = new ItemRequirement("Almost Item", 10, false);
            assertFalse(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - 空文字列displayName")
        void checkWithEmptyDisplayName() {
            ItemStack mockItem = mock(ItemStack.class);
            when(mockItem.hasItemMeta()).thenReturn(true);
            when(mockItem.getItemMeta()).thenReturn(mockItemMeta);
            when(mockItemMeta.hasDisplayName()).thenReturn(true);
            when(mockItemMeta.displayName()).thenReturn(Component.text(""));
            when(mockItem.getAmount()).thenReturn(10);

            ItemStack[] items = {mockItem};
            when(mockInventory.getContents()).thenReturn(items);

            ItemRequirement requirement = new ItemRequirement("", 5, false);
            assertTrue(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - 日本語アイテム名")
        void checkWithJapaneseItemName() {
            ItemStack mockItem = mock(ItemStack.class);
            when(mockItem.hasItemMeta()).thenReturn(true);
            when(mockItem.getItemMeta()).thenReturn(mockItemMeta);
            when(mockItemMeta.hasDisplayName()).thenReturn(true);
            when(mockItemMeta.displayName()).thenReturn(Component.text("魔法の水晶"));
            when(mockItem.getAmount()).thenReturn(3);

            ItemStack[] items = {mockItem};
            when(mockInventory.getContents()).thenReturn(items);

            ItemRequirement requirement = new ItemRequirement("魔法の水晶", 3, false);
            assertTrue(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - 複数スタックで合計達成")
        void checkWithMultipleStacksReachingTotal() {
            ItemStack stack1 = mock(ItemStack.class);
            ItemStack stack2 = mock(ItemStack.class);
            ItemStack stack3 = mock(ItemStack.class);

            when(stack1.hasItemMeta()).thenReturn(true);
            when(stack1.getItemMeta()).thenReturn(mockItemMeta);
            when(stack1.getAmount()).thenReturn(64);

            when(stack2.hasItemMeta()).thenReturn(true);
            when(stack2.getItemMeta()).thenReturn(mockItemMeta);
            when(stack2.getAmount()).thenReturn(64);

            when(stack3.hasItemMeta()).thenReturn(true);
            when(stack3.getItemMeta()).thenReturn(mockItemMeta);
            when(stack3.getAmount()).thenReturn(32);

            when(mockItemMeta.hasDisplayName()).thenReturn(true);
            when(mockItemMeta.displayName()).thenReturn(Component.text("Resource"));

            ItemStack[] items = {stack1, stack2, stack3};
            when(mockInventory.getContents()).thenReturn(items);

            ItemRequirement requirement = new ItemRequirement("Resource", 150, false);
            assertTrue(requirement.check(mockPlayer)); // 64 + 64 + 32 = 160 >= 150
        }
    }

    @Nested
    @DisplayName("check() - Edge Cases with Null and Meta")
    class NullAndMetaEdgeCases {

        @Mock
        private ItemMeta mockItemMeta;

        @Test
        @DisplayName("check - インベントリにnullと有効なアイテムが混在")
        void checkWithMixedNullAndValidItems() {
            ItemStack validItem = mock(ItemStack.class);
            when(validItem.hasItemMeta()).thenReturn(true);
            when(validItem.getItemMeta()).thenReturn(mockItemMeta);
            when(mockItemMeta.hasDisplayName()).thenReturn(true);
            when(mockItemMeta.displayName()).thenReturn(Component.text("Valid Item"));
            when(validItem.getAmount()).thenReturn(5);

            ItemStack[] items = {null, validItem, null, null};
            when(mockInventory.getContents()).thenReturn(items);

            ItemRequirement requirement = new ItemRequirement("Valid Item", 5, false);
            assertTrue(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - displayNameがnullの場合")
        void checkWithNullDisplayName() {
            ItemStack mockItem = mock(ItemStack.class);
            when(mockItem.hasItemMeta()).thenReturn(true);
            when(mockItem.getItemMeta()).thenReturn(mockItemMeta);
            when(mockItemMeta.hasDisplayName()).thenReturn(false);

            ItemStack[] items = {mockItem};
            when(mockInventory.getContents()).thenReturn(items);

            ItemRequirement requirement = new ItemRequirement("Any Item", 1, false);
            assertFalse(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - hasItemMetaがfalseの場合")
        void checkWithHasItemMetaFalse() {
            ItemStack mockItem = mock(ItemStack.class);
            when(mockItem.hasItemMeta()).thenReturn(false);

            ItemStack[] items = {mockItem};
            when(mockInventory.getContents()).thenReturn(items);

            ItemRequirement requirement = new ItemRequirement("Test Item", 1, false);
            assertFalse(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - getItemMetaがnullを返す")
        void checkWithGetItemMetaReturnsNull() {
            ItemStack mockItem = mock(ItemStack.class);
            when(mockItem.hasItemMeta()).thenReturn(true);
            when(mockItem.getItemMeta()).thenReturn(null);

            ItemStack[] items = {mockItem};
            when(mockInventory.getContents()).thenReturn(items);

            ItemRequirement requirement = new ItemRequirement("Test Item", 1, false);
            // 本番コードにnullチェックがないため、NullPointerExceptionがスローされる
            assertThrows(NullPointerException.class, () -> requirement.check(mockPlayer));
        }
    }
}

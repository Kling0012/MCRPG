package com.example.rpgplugin.rpgclass.requirements;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
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
 * QuestRequirementの単体テスト
 *
 * カバレッジ向上を目的とした包括的なテストスイート
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("QuestRequirement 単体テスト")
class QuestRequirementTest {

    @Mock
    private Player mockPlayer;

    @Mock
    private ConfigurationSection mockConfig;

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("コンストラクタ - 全引数指定")
        void constructorWithAllArguments() {
            QuestRequirement requirement = new QuestRequirement("quest_001", "テストクエスト", "BetonQuest");

            assertEquals("quest_001", requirement.getQuestId());
            assertEquals("BetonQuest", requirement.getExternalPlugin());
            assertEquals("クエスト「テストクエスト」完了", requirement.getDescription());
        }

        @Test
        @DisplayName("コンストラクタ - questNameがnullの場合はquestIdを使用")
        void constructorWithNullQuestName() {
            QuestRequirement requirement = new QuestRequirement("quest_002", null, null);

            assertEquals("quest_002", requirement.getQuestId());
            assertNull(requirement.getExternalPlugin());
            assertEquals("クエスト「quest_002」完了", requirement.getDescription()); // questIdが使用される
        }

        @Test
        @DisplayName("コンストラクタ - 外部プラグインなし")
        void constructorWithoutExternalPlugin() {
            QuestRequirement requirement = new QuestRequirement("quest_003", "内部クエスト", null);

            assertEquals("quest_003", requirement.getQuestId());
            assertNull(requirement.getExternalPlugin());
            assertEquals("クエスト「内部クエスト」完了", requirement.getDescription());
        }

        @Test
        @DisplayName("コンストラクタ - 空文字列のプラグイン名")
        void constructorWithEmptyPlugin() {
            QuestRequirement requirement = new QuestRequirement("quest_004", "クエスト", "");

            assertEquals("quest_004", requirement.getQuestId());
            assertEquals("", requirement.getExternalPlugin());
        }

        @Test
        @DisplayName("コンストラクタ - BetonQuest指定")
        void constructorWithBetonQuest() {
            QuestRequirement requirement = new QuestRequirement("bq_hero", "英雄の道", "BetonQuest");

            assertEquals("bq_hero", requirement.getQuestId());
            assertEquals("BetonQuest", requirement.getExternalPlugin());
        }

        @Test
        @DisplayName("コンストラクタ - Questsプラグイン指定")
        void constructorWithQuestsPlugin() {
            QuestRequirement requirement = new QuestRequirement("qs_adventure", "冒険", "Quests");

            assertEquals("qs_adventure", requirement.getQuestId());
            assertEquals("Quests", requirement.getExternalPlugin());
        }
    }

    @Nested
    @DisplayName("check() Tests")
    class CheckTests {

        @Test
        @DisplayName("check - nullプレイヤーはfalse")
        void checkWithNullPlayer() {
            QuestRequirement requirement = new QuestRequirement("quest_001", "テスト", null);

            assertFalse(requirement.check(null));
        }

        @Test
        @DisplayName("check - 外部プラグインなし（内部管理）はfalse")
        void checkWithInternalManagement() {
            QuestRequirement requirement = new QuestRequirement("quest_001", "テスト", null);

            // 内部管理は未実装なので常にfalse
            assertFalse(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - BetonQuest指定（クラスなしでfalse）")
        void checkWithBetonQuestWhenClassNotAvailable() {
            QuestRequirement requirement = new QuestRequirement("bq_quest", "クエスト", "BetonQuest");

            // BetonQuestクラスが存在しない環境ではfalse
            assertFalse(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - Questsプラグイン指定（未実装でfalse）")
        void checkWithQuestsPlugin() {
            QuestRequirement requirement = new QuestRequirement("qs_quest", "クエスト", "Quests");

            // Questsプラグイン連携は未実装
            assertFalse(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - 不明なプラグイン指定")
        void checkWithUnknownPlugin() {
            QuestRequirement requirement = new QuestRequirement("quest", "クエスト", "UnknownPlugin");

            assertFalse(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - 大文字小文字を区別しないBetonQuest")
        void checkWithBetonQuestCaseInsensitive() {
            QuestRequirement requirement = new QuestRequirement("quest", "クエスト", "betonquest");

            // 小文字でもBetonQuestとして認識される（ただしクラスがないのでfalse）
            assertFalse(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("check - 大文字小文字を区別しないQuests")
        void checkWithQuestsCaseInsensitive() {
            QuestRequirement requirement = new QuestRequirement("quest", "クエスト", "QUESTS");

            // 大文字でもQuestsとして認識される
            assertFalse(requirement.check(mockPlayer));
        }
    }

    @Nested
    @DisplayName("getDescription() Tests")
    class GetDescriptionTests {

        @Test
        @DisplayName("getDescription - クエスト名を含む説明")
        void getDescriptionReturnsQuestName() {
            QuestRequirement requirement = new QuestRequirement("quest_001", "勇者の試練", null);

            assertEquals("クエスト「勇者の試練」完了", requirement.getDescription());
        }

        @Test
        @DisplayName("getDescription - questNameがnullの場合はIDを表示")
        void getDescriptionWithNullName() {
            QuestRequirement requirement = new QuestRequirement("quest_002", null, null);

            assertEquals("クエスト「quest_002」完了", requirement.getDescription());
        }

        @Test
        @DisplayName("getDescription - 外部プラグイン指定")
        void getDescriptionWithExternalPlugin() {
            QuestRequirement requirement = new QuestRequirement("bq_001", "BetonQuestクエスト", "BetonQuest");

            assertEquals("クエスト「BetonQuestクエスト」完了", requirement.getDescription());
        }

        @Test
        @DisplayName("getDescription - 空文字列クエスト名")
        void getDescriptionWithEmptyName() {
            QuestRequirement requirement = new QuestRequirement("quest", "", null);

            assertEquals("クエスト「」完了", requirement.getDescription());
        }

        @Test
        @DisplayName("getDescription - 特殊文字を含むクエスト名")
        void getDescriptionWithSpecialCharacters() {
            QuestRequirement requirement = new QuestRequirement("quest", "クエスト: 【禁断の地】", null);

            assertEquals("クエスト「クエスト: 【禁断の地】」完了", requirement.getDescription());
        }
    }

    @Nested
    @DisplayName("getType() Tests")
    class GetTypeTests {

        @Test
        @DisplayName("getType - questを返す")
        void getTypeReturnsQuest() {
            QuestRequirement requirement = new QuestRequirement("quest", "クエスト", null);

            assertEquals("quest", requirement.getType());
        }

        @Test
        @DisplayName("getType - 外部プラグイン指定でもquestを返す")
        void getTypeReturnsQuestWithExternalPlugin() {
            QuestRequirement requirement = new QuestRequirement("quest", "クエスト", "BetonQuest");

            assertEquals("quest", requirement.getType());
        }
    }

    @Nested
    @DisplayName("Getter Methods Tests")
    class GetterMethodsTests {

        @Test
        @DisplayName("getQuestId - 正しく返す")
        void getQuestIdReturnsCorrect() {
            QuestRequirement requirement = new QuestRequirement("my_quest_id", "名前", "Plugin");

            assertEquals("my_quest_id", requirement.getQuestId());
        }

        @Test
        @DisplayName("getExternalPlugin - nullを返す（内部管理）")
        void getExternalPluginReturnsNullForInternal() {
            QuestRequirement requirement = new QuestRequirement("quest", "名前", null);

            assertNull(requirement.getExternalPlugin());
        }

        @Test
        @DisplayName("getExternalPlugin - プラグイン名を返す")
        void getExternalPluginReturnsPluginName() {
            QuestRequirement requirement = new QuestRequirement("quest", "名前", "BetonQuest");

            assertEquals("BetonQuest", requirement.getExternalPlugin());
        }

        @Test
        @DisplayName("getExternalPlugin - 空文字列を返す")
        void getExternalPluginReturnsEmptyString() {
            QuestRequirement requirement = new QuestRequirement("quest", "名前", "");

            assertEquals("", requirement.getExternalPlugin());
        }
    }

    @Nested
    @DisplayName("parse() Tests")
    class ParseTests {

        @Test
        @DisplayName("parse - 全プロパティ指定")
        void parseWithAllProperties() {
            when(mockConfig.getString("quest_id", "unknown")).thenReturn("quest_001");
            when(mockConfig.getString("quest_name", null)).thenReturn("テストクエスト");
            when(mockConfig.getString("plugin", null)).thenReturn("BetonQuest");

            QuestRequirement requirement = QuestRequirement.parse(mockConfig);

            assertEquals("quest_001", requirement.getQuestId());
            assertEquals("BetonQuest", requirement.getExternalPlugin());
            assertEquals("クエスト「テストクエスト」完了", requirement.getDescription());
        }

        @Test
        @DisplayName("parse - デフォルト値（quest_idのみ）")
        void parseWithDefaults() {
            when(mockConfig.getString("quest_id", "unknown")).thenReturn("quest_default");
            when(mockConfig.getString("quest_name", null)).thenReturn(null);
            when(mockConfig.getString("plugin", null)).thenReturn(null);

            QuestRequirement requirement = QuestRequirement.parse(mockConfig);

            assertEquals("quest_default", requirement.getQuestId());
            assertNull(requirement.getExternalPlugin());
            assertEquals("クエスト「quest_default」完了", requirement.getDescription()); // IDが使用される
        }

        @Test
        @DisplayName("parse - quest_nameなし")
        void parseWithoutQuestName() {
            when(mockConfig.getString("quest_id", "unknown")).thenReturn("quest_002");
            when(mockConfig.getString("quest_name", null)).thenReturn(null);
            when(mockConfig.getString("plugin", null)).thenReturn("Quests");

            QuestRequirement requirement = QuestRequirement.parse(mockConfig);

            assertEquals("quest_002", requirement.getQuestId());
            assertEquals("Quests", requirement.getExternalPlugin());
        }

        @Test
        @DisplayName("parse - pluginなし")
        void parseWithoutPlugin() {
            when(mockConfig.getString("quest_id", "unknown")).thenReturn("quest_003");
            when(mockConfig.getString("quest_name", null)).thenReturn("内部クエスト");
            when(mockConfig.getString("plugin", null)).thenReturn(null);

            QuestRequirement requirement = QuestRequirement.parse(mockConfig);

            assertEquals("quest_003", requirement.getQuestId());
            assertNull(requirement.getExternalPlugin());
            assertEquals("クエスト「内部クエスト」完了", requirement.getDescription());
        }

        @Test
        @DisplayName("parse - 空文字列プラグイン名")
        void parseWithEmptyPlugin() {
            when(mockConfig.getString("quest_id", "unknown")).thenReturn("quest_004");
            when(mockConfig.getString("quest_name", null)).thenReturn("クエスト");
            when(mockConfig.getString("plugin", null)).thenReturn("");

            QuestRequirement requirement = QuestRequirement.parse(mockConfig);

            assertEquals("quest_004", requirement.getQuestId());
            assertEquals("", requirement.getExternalPlugin());
        }

        @Test
        @DisplayName("parse - quest_idデフォルト値")
        void parseWithDefaultQuestId() {
            when(mockConfig.getString("quest_id", "unknown")).thenReturn("unknown");
            when(mockConfig.getString("quest_name", null)).thenReturn(null);
            when(mockConfig.getString("plugin", null)).thenReturn(null);

            QuestRequirement requirement = QuestRequirement.parse(mockConfig);

            assertEquals("unknown", requirement.getQuestId());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("複数インスタンス - 別々の設定")
        void multipleInstancesHaveDifferentSettings() {
            QuestRequirement req1 = new QuestRequirement("q1", "クエスト1", null);
            QuestRequirement req2 = new QuestRequirement("q2", "クエスト2", "BetonQuest");
            QuestRequirement req3 = new QuestRequirement("q3", null, "Quests");

            assertEquals("q1", req1.getQuestId());
            assertNull(req1.getExternalPlugin());
            assertEquals("クエスト「クエスト1」完了", req1.getDescription());

            assertEquals("q2", req2.getQuestId());
            assertEquals("BetonQuest", req2.getExternalPlugin());
            assertEquals("クエスト「クエスト2」完了", req2.getDescription());

            assertEquals("q3", req3.getQuestId());
            assertEquals("Quests", req3.getExternalPlugin());
            assertEquals("クエスト「q3」完了", req3.getDescription()); // nullはIDに置き換え
        }

        @Test
        @DisplayName("長いクエスト名")
        void longQuestName() {
            String longName = "非常に長いクエスト名が含まれている場合のテストケースを確認するための文字列です";
            QuestRequirement requirement = new QuestRequirement("long_quest", longName, null);

            assertTrue(requirement.getDescription().contains(longName));
        }

        @Test
        @DisplayName("特殊文字を含むクエストID")
        void questIdWithSpecialCharacters() {
            QuestRequirement requirement = new QuestRequirement("quest-01_test", "クエスト", null);

            assertEquals("quest-01_test", requirement.getQuestId());
        }

        @Test
        @DisplayName("日本語クエスト名")
        void japaneseQuestName() {
            QuestRequirement requirement = new QuestRequirement("jp_quest", "勇者たちの旅路", null);

            assertEquals("クエスト「勇者たちの旅路」完了", requirement.getDescription());
        }

        @Test
        @DisplayName("check() - 有効なプレイヤーでも外部プラグインなしはfalse")
        void checkWithValidPlayerButNoExternalPlugin() {
            QuestRequirement requirement = new QuestRequirement("internal_quest", "内部クエスト", null);

            // 有効なPlayerを渡しても、内部管理は未実装なのでfalse
            assertFalse(requirement.check(mockPlayer));
        }
    }

    @Nested
    @DisplayName("External Plugin Integration Tests")
    class ExternalPluginTests {

        @Test
        @DisplayName("外部プラグインチェック - BetonQuest（例外時false）")
        void externalPluginCheckBetonQuestReturnsFalseOnException() {
            QuestRequirement requirement = new QuestRequirement("bq", "クエスト", "BetonQuest");

            // BetonQuestクラスが存在しないのでfalse
            assertFalse(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("外部プラグインチェック - Quests（常にfalse）")
        void externalPluginCheckQuestsAlwaysFalse() {
            QuestRequirement requirement = new QuestRequirement("qs", "クエスト", "Quests");

            // Questsプラグイン連携は未実装
            assertFalse(requirement.check(mockPlayer));
        }

        @Test
        @DisplayName("外部プラグインチェック - 不明なプラグイン")
        void externalPluginCheckUnknownPluginReturnsFalse() {
            QuestRequirement requirement = new QuestRequirement("quest", "クエスト", "MysteriousPlugin");

            assertFalse(requirement.check(mockPlayer));
        }
    }
}

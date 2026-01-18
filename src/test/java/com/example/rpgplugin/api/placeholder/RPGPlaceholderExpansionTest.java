package com.example.rpgplugin.api.placeholder;

import com.example.rpgplugin.RPGPlugin;
import com.example.rpgplugin.api.RPGPluginAPI;
import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.player.PlayerManager;
import com.example.rpgplugin.rpgclass.RPGClass;
import com.example.rpgplugin.rpgclass.ClassManager;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.storage.models.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * RPGPlaceholderExpansionのテストクラス
 *
 * <p>PlaceholderAPI拡張機能の各種メソッドとプレースホルダーの評価を検証します。</p>
 *
 * <p>テストカバレッジ目標: 90%+</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RPGプレースホルダー拡張機能のテスト")
class RPGPlaceholderExpansionTest {

    @Mock
    private RPGPlugin plugin;

    @Mock
    private RPGPluginAPI api;

    @Mock
    private Player player;

    @Mock
    private RPGPlayer rpgPlayer;

    @Mock
    private RPGClass rpgClass;

    private RPGPlaceholderExpansion expansion;
    private final UUID testUUID = UUID.randomUUID();

    /**
     * テスト前のセットアップ
     */
    @BeforeEach
    void setUp() {
        // PluginMetaのモック設定
        io.papermc.paper.plugin.configuration.PluginMeta pluginMeta = mock(io.papermc.paper.plugin.configuration.PluginMeta.class);
        lenient().when(plugin.getPluginMeta()).thenReturn(pluginMeta);
        lenient().when(pluginMeta.getAuthors()).thenReturn(List.of("RPGPlugin Team"));

        // PlayerManagerとClassManagerのモック設定
        PlayerManager playerManager = mock(PlayerManager.class);
        lenient().when(plugin.getPlayerManager()).thenReturn(playerManager);

        ClassManager classManager = mock(ClassManager.class);
        lenient().when(plugin.getClassManager()).thenReturn(classManager);

        // APIのモック設定（lenientを使用して不要な警告を回避）
        lenient().when(plugin.getAPI()).thenReturn(api);

        // プレイヤーの基本情報設定
        lenient().when(player.getUniqueId()).thenReturn(testUUID);

        // プレイヤーデータの作成
        PlayerData playerData = new PlayerData(testUUID, "TestPlayer");
        playerData.setMaxHealth(100);
        playerData.setMaxMana(200);
        playerData.setCurrentMana(150);

        // RPGPlayerのモック設定（テストで必要なもののみ）
        lenient().when(rpgPlayer.getPlayerData()).thenReturn(playerData);
        lenient().when(rpgPlayer.getAvailablePoints()).thenReturn(5);
        lenient().when(playerManager.getRPGPlayer(testUUID)).thenReturn(rpgPlayer);

        // RPGClassのモック設定（テストで必要なもののみ）
        lenient().when(rpgClass.getDisplayName()).thenReturn("Warrior");
        lenient().when(rpgClass.getRank()).thenReturn(2);
        lenient().when(rpgClass.toString()).thenReturn("Warrior");
        lenient().when(classManager.getPlayerClass(player)).thenReturn(Optional.of(rpgClass));

        // RPGPlaceholderExpansionの初期化
        expansion = new RPGPlaceholderExpansion(plugin);
    }

    // ========== メタデータ取得メソッドのテスト ==========

    @Test
    @DisplayName("getIdentifier()は'rpg'を返す")
    void getIdentifierShouldReturnRpg() {
        assertEquals("rpg", expansion.getIdentifier());
    }

    @Test
    @DisplayName("getAuthor()は正しい作者名を返す")
    void getAuthorShouldReturnCorrectAuthor() {
        assertEquals("RPGPlugin Team", expansion.getAuthor());
    }

    @Test
    @DisplayName("getVersion()は正しいバージョンを返す")
    void getVersionShouldReturnCorrectVersion() {
        assertEquals("1.0.0", expansion.getVersion());
    }

    @Test
    @DisplayName("persist()はtrueを返す")
    void persistShouldReturnTrue() {
        assertTrue(expansion.persist());
    }

    // ========== プレースホルダー評価のテスト ==========

    @Test
    @DisplayName("レベルプレースホルダー '%rpg_level%' は正しいレベル値を返す")
    void levelPlaceholderShouldReturnCorrectLevel() {
        // 準備
        lenient().when(api.getLevel(player)).thenReturn(10);

        // 実行
        String result = expansion.onPlaceholderRequest(player, "level");

        // 検証
        assertEquals("10", result);
    }

    @Test
    @DisplayName("クラスIDプレースホルダー '%rpg_class%' はクラスIDを返す")
    void classIdPlaceholderShouldReturnClassId() {
        // 準備
        lenient().when(api.getClassId(player)).thenReturn("warrior");

        // 実行
        String result = expansion.onPlaceholderRequest(player, "class");

        // 検証
        assertEquals("warrior", result);
    }

    @Test
    @DisplayName("クラス名プレースホルダー '%rpg_class_name%' はクラス表示名を返す")
    void classNamePlaceholderShouldReturnClassName() {
        // 準備
        lenient().when(plugin.getPlayerManager().getRPGPlayer(testUUID)).thenReturn(rpgPlayer);
        lenient().when(plugin.getClassManager().getPlayerClass(player)).thenReturn(Optional.of(rpgClass));

        // 実行
        String result = expansion.onPlaceholderRequest(player, "class_name");

        // 検証
        assertEquals("Warrior", result);
    }

    @Test
    @DisplayName("クラスランクプレースホルダー '%rpg_class_rank%' はクラスランクを返す")
    void classRankPlaceholderShouldReturnClassRank() {
        // 準備
        lenient().when(plugin.getPlayerManager().getRPGPlayer(testUUID)).thenReturn(rpgPlayer);
        lenient().when(plugin.getClassManager().getPlayerClass(player)).thenReturn(Optional.of(rpgClass));

        // 実行
        String result = expansion.onPlaceholderRequest(player, "class_rank");

        // 検証
        assertEquals("2", result);
    }

    @Test
    @DisplayName("スキルポイントプレースホルダー '%rpg_skill_points%' は利用可能ポイントを返す")
    void skillPointsPlaceholderShouldReturnAvailablePoints() {
        // 準備
        lenient().when(plugin.getPlayerManager().getRPGPlayer(testUUID)).thenReturn(rpgPlayer);

        // 実行
        String result = expansion.onPlaceholderRequest(player, "skill_points");

        // 検証
        assertEquals("5", result);
    }

    @Test
    @DisplayName("最大HPプレースホルダー '%rpg_max_hp%' は最大HPを返す")
    void maxHpPlaceholderShouldReturnMaxHealth() {
        // 準備
        lenient().when(plugin.getPlayerManager().getRPGPlayer(testUUID)).thenReturn(rpgPlayer);

        // 実行
        String result = expansion.onPlaceholderRequest(player, "max_hp");

        // 検証
        assertEquals("100", result);
    }

    @Test
    @DisplayName("最大MPプレースホルダー '%rpg_max_mana%' は最大MPを返す")
    void maxManaPlaceholderShouldReturnMaxMana() {
        // 準備
        lenient().when(plugin.getPlayerManager().getRPGPlayer(testUUID)).thenReturn(rpgPlayer);

        // 実行
        String result = expansion.onPlaceholderRequest(player, "max_mana");

        // 検証
        assertEquals("200", result);
    }

    @Test
    @DisplayName("現在MPプレースホルダー '%rpg_mana%' は現在MPを返す")
    void manaPlaceholderShouldReturnCurrentMana() {
        // 準備
        lenient().when(plugin.getPlayerManager().getRPGPlayer(testUUID)).thenReturn(rpgPlayer);

        // 実行
        String result = expansion.onPlaceholderRequest(player, "mana");

        // 検証
        assertEquals("150", result);
    }

    @Test
    @DisplayName("ステータスマルチプレースホルダー '%rpg_stats%' はすべてのステータスを返す")
    void statsPlaceholderShouldReturnAllStats() {
        // 準備
        lenient().when(plugin.getPlayerManager().getRPGPlayer(testUUID)).thenReturn(rpgPlayer);
        lenient().when(api.getStat(player, Stat.STRENGTH)).thenReturn(15);
        lenient().when(api.getStat(player, Stat.INTELLIGENCE)).thenReturn(10);
        lenient().when(api.getStat(player, Stat.SPIRIT)).thenReturn(8);
        lenient().when(api.getStat(player, Stat.VITALITY)).thenReturn(12);
        lenient().when(api.getStat(player, Stat.DEXTERITY)).thenReturn(14);

        // 実行
        String result = expansion.onPlaceholderRequest(player, "stats");

        // 検証
        assertEquals("STR:15 INT:10 SPI:8 VIT:12 DEX:14", result);
    }

    // ========== ステータスプレースホルダーのテスト ==========

    @Test
    @DisplayName("STRステータスプレースホルダー '%rpg_stat_STR%' はSTR値を返す")
    void strStatPlaceholderShouldReturnStrValue() {
        // 準備
        lenient().when(api.getStat(player, Stat.STRENGTH)).thenReturn(25);

        // 実行
        String result = expansion.onPlaceholderRequest(player, "stat_STR");

        // 検証
        assertEquals("25", result);
    }

    @Test
    @DisplayName("INTステータスプレースホルダー '%rpg_stat_INT%' はINT値を返す")
    void intStatPlaceholderShouldReturnIntValue() {
        // 準備
        lenient().when(api.getStat(player, Stat.INTELLIGENCE)).thenReturn(18);

        // 実行
        String result = expansion.onPlaceholderRequest(player, "stat_INT");

        // 検証
        assertEquals("18", result);
    }

    @Test
    @DisplayName("SPIステータスプレースホルダー '%rpg_stat_SPI%' はSPI値を返す")
    void spiStatPlaceholderShouldReturnSpiValue() {
        // 準備
        lenient().when(api.getStat(player, Stat.SPIRIT)).thenReturn(22);

        // 実行
        String result = expansion.onPlaceholderRequest(player, "stat_SPI");

        // 検証
        assertEquals("22", result);
    }

    @Test
    @DisplayName("VITステータスプレースホルダー '%rpg_stat_VIT%' はVIT値を返す")
    void vitStatPlaceholderShouldReturnVitValue() {
        // 準備
        lenient().when(api.getStat(player, Stat.VITALITY)).thenReturn(30);

        // 実行
        String result = expansion.onPlaceholderRequest(player, "stat_VIT");

        // 検証
        assertEquals("30", result);
    }

    @Test
    @DisplayName("DEXステータスプレースホルダー '%rpg_stat_DEX%' はDEX値を返す")
    void dexStatPlaceholderShouldReturnDexValue() {
        // 準備
        lenient().when(api.getStat(player, Stat.DEXTERITY)).thenReturn(20);

        // 実行
        String result = expansion.onPlaceholderRequest(player, "stat_DEX");

        // 検証
        assertEquals("20", result);
    }

    @Test
    @DisplayName("不明なステータスプレースホルダーは'0'を返す")
    void unknownStatPlaceholderShouldReturnZero() {
        // 実行
        String result = expansion.onPlaceholderRequest(player, "stat_UNKNOWN");

        // 検証
        assertEquals("0", result);
    }

    // ========== スキル関連プレースホルダーのテスト ==========

    @Test
    @DisplayName("スキルレベルプレースホルダー '%rpg_skill_level_fireball%' はスキルレベルを返す")
    void skillLevelPlaceholderShouldReturnSkillLevel() {
        // 準備
        lenient().when(api.hasSkill(player, "fireball")).thenReturn(true);
        lenient().when(api.getSkillLevel(player, "fireball")).thenReturn(5);

        // 実行
        String result = expansion.onPlaceholderRequest(player, "skill_level_fireball");

        // 検証
        assertEquals("5", result);
    }

    @Test
    @DisplayName("存在しないスキルレベルプレースホルダーは'0'を返す")
    void nonExistentSkillLevelPlaceholderShouldReturnZero() {
        // 準備
        lenient().when(api.hasSkill(player, "nonexistent")).thenReturn(false);

        // 実行
        String result = expansion.onPlaceholderRequest(player, "skill_level_nonexistent");

        // 検証
        assertEquals("0", result);
    }

    // ========== 辺境値テスト ==========

    @Test
    @DisplayName("最大HPが0の場合はデフォルト値を返す")
    void zeroMaxHealthShouldReturnDefault() {
        // 準備
        PlayerData playerData = new PlayerData(testUUID, "TestPlayer");
        playerData.setMaxHealth(0);
        lenient().when(plugin.getPlayerManager().getRPGPlayer(testUUID)).thenReturn(rpgPlayer);
        lenient().when(rpgPlayer.getPlayerData()).thenReturn(playerData);

        // 実行
        String result = expansion.onPlaceholderRequest(player, "max_hp");

        // 検証
        assertEquals("20", result);
    }

    @Test
    @DisplayName("最大MPが0の場合はデフォルト値を返す")
    void zeroMaxManaShouldReturnDefault() {
        // 準備
        PlayerData playerData = new PlayerData(testUUID, "TestPlayer");
        playerData.setMaxMana(0);
        lenient().when(plugin.getPlayerManager().getRPGPlayer(testUUID)).thenReturn(rpgPlayer);
        lenient().when(rpgPlayer.getPlayerData()).thenReturn(playerData);

        // 実行
        String result = expansion.onPlaceholderRequest(player, "max_mana");

        // 検証
        assertEquals("100", result);
    }

    // ========== 異常系テスト ==========

    @Test
    @DisplayName("nullプレイヤーの場合は空文字列を返す")
    void nullPlayerShouldReturnEmptyString() {
        // 実行
        String result = expansion.onPlaceholderRequest(null, "level");

        // 検証
        assertEquals("", result);
    }

    @Test
    @DisplayName("nullクラスIDの場合は'None'を返す")
    void nullClassIdShouldReturnNone() {
        // 準備
        lenient().when(api.getClassId(player)).thenReturn(null);

        // 実行
        String result = expansion.onPlaceholderRequest(player, "class");

        // 検証
        assertEquals("None", result);
    }

    @Test
    @DisplayName("存在しないRPGPlayerの場合はデフォルト値を返す")
    void nonExistentRPGPlayerShouldReturnDefault() {
        // 準備
        lenient().when(plugin.getPlayerManager().getRPGPlayer(testUUID)).thenReturn(null);

        // 実行
        String result = expansion.onPlaceholderRequest(player, "skill_points");

        // 検証
        assertEquals("0", result);
    }

    @Test
    @DisplayName("存在しないクラスの場合は'None'を返す")
    void nonExistentClassShouldReturnNone() {
        // 準備
        lenient().when(plugin.getPlayerManager().getRPGPlayer(testUUID)).thenReturn(rpgPlayer);
        lenient().when(plugin.getClassManager().getPlayerClass(player)).thenReturn(Optional.empty());

        // 実行
        String result = expansion.onPlaceholderRequest(player, "class_name");

        // 検証
        assertEquals("None", result);
    }

    @Test
    @DisplayName("不明なプレースホルダーはnullを返す")
    void unknownPlaceholderShouldReturnNull() {
        // 実行
        String result = expansion.onPlaceholderRequest(player, "unknown_placeholder");

        // 検証
        assertNull(result);
    }

    @Test
    @DisplayName("nullパラメータの場合はnullを返す")
    void nullParamsShouldReturnNull() {
        // 実行
        String result = expansion.onPlaceholderRequest(player, null);

        // 検証
        assertNull(result);
    }

    // ========== 統合テスト ==========

    @Test
    @DisplayName("完全なステータスプレースホルダーの変換テスト")
    void comprehensiveStatConversionTest() {
        // 準備
        lenient().when(api.getStat(player, Stat.STRENGTH)).thenReturn(50);
        lenient().when(api.getStat(player, Stat.INTELLIGENCE)).thenReturn(40);
        lenient().when(api.getStat(player, Stat.SPIRIT)).thenReturn(30);
        lenient().when(api.getStat(player, Stat.VITALITY)).thenReturn(60);
        lenient().when(api.getStat(player, Stat.DEXTERITY)).thenReturn(45);

        // 実行と検証
        assertEquals("50", expansion.onPlaceholderRequest(player, "stat_STR"));
        assertEquals("40", expansion.onPlaceholderRequest(player, "stat_intelligence")); // 大文字小文字不区別
        assertEquals("30", expansion.onPlaceholderRequest(player, "stat_spirit"));
        assertEquals("60", expansion.onPlaceholderRequest(player, "stat_VIT"));
        assertEquals("45", expansion.onPlaceholderRequest(player, "stat_dexterity"));
    }

    @Test
    @DisplayName("プレースホルダーのエッジケースの包括的テスト")
    void placeholderEdgeCasesComprehensiveTest() {
        // nullプレイヤーのテスト
        assertEquals("", expansion.onPlaceholderRequest(null, "level"));
        assertEquals("", expansion.onPlaceholderRequest(null, "class"));
        assertEquals("", expansion.onPlaceholderRequest(null, "stat_STR"));

        // 空パラメータのテスト
        assertNull(expansion.onPlaceholderRequest(player, ""));

        // 不明なプレースホルダーのテスト
        assertNull(expansion.onPlaceholderRequest(player, "completely_unknown"));

        // ステータスパスの不完全なテスト
        assertEquals("0", expansion.onPlaceholderRequest(player, "stat_")); // 不完全なstat_
        assertEquals("0", expansion.onPlaceholderRequest(player, "stat")); // statのみ
    }
}
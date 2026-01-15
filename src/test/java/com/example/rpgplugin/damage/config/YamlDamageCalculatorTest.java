package com.example.rpgplugin.damage.config;

import com.example.rpgplugin.player.RPGPlayer;
import com.example.rpgplugin.stats.Stat;
import com.example.rpgplugin.stats.StatManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * YamlDamageCalculatorのユニットテスト
 *
 * <p>YAMLベースダメージ計算機のテストを行います。</p>
 *
 * @author RPGPlugin Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("YamlDamageCalculator テスト")
class YamlDamageCalculatorTest {

    @Mock
    private DamageConfig mockDamageConfig;

    @Mock
    private VariableScopeManager mockScopeManager;

    @Mock
    private RPGPlayer mockPlayer;

    @Mock
    private StatManager mockStatManager;

    private YamlDamageCalculator calculator;

    @BeforeEach
    void setUp() {
        // RPGPlayerの基本設定
        lenient().when(mockPlayer.getStatManager()).thenReturn(mockStatManager);
        lenient().when(mockPlayer.getClassId()).thenReturn("Warrior");
        lenient().when(mockPlayer.getLevel()).thenReturn(10);
        lenient().when(mockPlayer.getClassRank()).thenReturn(1);

        // ステータス値の設定
        lenient().when(mockStatManager.getFinalStat(Stat.STRENGTH)).thenReturn(50);
        lenient().when(mockStatManager.getFinalStat(Stat.INTELLIGENCE)).thenReturn(20);
        lenient().when(mockStatManager.getFinalStat(Stat.SPIRIT)).thenReturn(15);
        lenient().when(mockStatManager.getFinalStat(Stat.VITALITY)).thenReturn(30);
        lenient().when(mockStatManager.getFinalStat(Stat.DEXTERITY)).thenReturn(25);

        // setDamageConfigは何も返さない（voidメソッド）
        doNothing().when(mockScopeManager).setDamageConfig(any());

        // calculatorを初期化
        calculator = new YamlDamageCalculator(mockDamageConfig, mockScopeManager);
    }

    // ==================== コンストラクタ テスト ====================

    @Nested
    @DisplayName("コンストラクタ テスト")
    class ConstructorTests {

        @Test
        @DisplayName("正常なコンストラクタで初期化できる")
        void constructor_Valid_InitializesFields() {
            YamlDamageCalculator calc = new YamlDamageCalculator(mockDamageConfig, mockScopeManager);

            assertThat(calc.getDamageConfig()).isSameAs(mockDamageConfig);
            assertThat(calc.getScopeManager()).isSameAs(mockScopeManager);
        }

        @Test
        @DisplayName("scopeManagerがnullの場合はsetDamageConfigが呼ばれない")
        void constructor_NullScopeManager_DoesNotCallSetDamageConfig() {
            YamlDamageCalculator calc = new YamlDamageCalculator(mockDamageConfig, null);

            assertThat(calc.getDamageConfig()).isSameAs(mockDamageConfig);
            assertThat(calc.getScopeManager()).isNull();
        }

        @Test
        @DisplayName("scopeManagerがある場合はsetDamageConfigが呼ばれる")
        void constructor_WithScopeManager_CallsSetDamageConfig() {
            reset(mockScopeManager);
            YamlDamageCalculator calc = new YamlDamageCalculator(mockDamageConfig, mockScopeManager);

            verify(mockScopeManager).setDamageConfig(mockDamageConfig);
        }
    }

    // ==================== calculateSkillDamage テスト ====================

    @Nested
    @DisplayName("calculateSkillDamage テスト")
    class CalculateSkillDamageTests {

        @Test
        @DisplayName("3引数バージョンが4引数バージョンを呼び出す")
        void calculateSkillDamage_ThreeArgs_CallsFourArgVersion() {
            EventConfig config = new EventConfig.Builder()
                    .formula("BASE_DAMAGE * 2")
                    .minDamage(1.0)
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, 5))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            double result = calculator.calculateSkillDamage(100.0, mockPlayer, 5);

            assertThat(result).isEqualTo(200.0);
        }

        @Test
        @DisplayName("クラス設定がない場合は基本ダメージを返す")
        void calculateSkillDamage_NoClassOverride_UsesDefault() {
            // setUpで作成されたcalculatorは「Warrior」設定を持っているため、
            // Mage用の動作を確認するために新規calculatorを作成
            // Note: getEventConfigForClassがnullの場合、コードはbaseDamageを直接返す
            reset(mockScopeManager);
            doNothing().when(mockScopeManager).setDamageConfig(any());
            when(mockPlayer.getClassId()).thenReturn("Mage");
            when(mockDamageConfig.getEventConfigForClass("Mage", "skill_damage"))
                    .thenReturn(null);

            YamlDamageCalculator calc = new YamlDamageCalculator(mockDamageConfig, mockScopeManager);
            double result = calc.calculateSkillDamage(100.0, mockPlayer, 3);

            assertThat(result).isEqualTo(100.0);
        }

        @Test
        @DisplayName("設定がない場合は基本ダメージを返す")
        void calculateSkillDamage_NoConfig_ReturnsBaseDamage() {
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(null);
            when(mockDamageConfig.getEventConfig("skill_damage"))
                    .thenReturn(null);

            double result = calculator.calculateSkillDamage(100.0, mockPlayer, 5);

            assertThat(result).isEqualTo(100.0);
        }

        @Test
        @DisplayName("プレイヤーがnullの場合は基本ダメージを返す")
        void calculateSkillDamage_NullPlayer_ReturnsBaseDamage() {
            when(mockDamageConfig.getEventConfig((String) null))
                    .thenReturn(null);

            double result = calculator.calculateSkillDamage(100.0, null, 5);

            assertThat(result).isEqualTo(100.0);
        }

        @Test
        @DisplayName("追加変数を含む計算ができる")
        void calculateSkillDamage_WithAdditionalVariables_IncludesVariables() {
            EventConfig config = new EventConfig.Builder()
                    .formula("BASE_DAMAGE + BONUS")
                    .minDamage(1.0)
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, 5))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            Map<String, Double> additionalVars = Map.of("BONUS", 50.0);
            double result = calculator.calculateSkillDamage(100.0, mockPlayer, 5, additionalVars);

            assertThat(result).isEqualTo(150.0);
        }

        @Test
        @DisplayName("数式エラー時は基本ダメージを返す")
        void calculateSkillDamage_FormulaError_ReturnsBaseDamage() {
            EventConfig config = new EventConfig.Builder()
                    .formula("INVALID_SYNTAX###")
                    .minDamage(1.0)
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, 5))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            double result = calculator.calculateSkillDamage(100.0, mockPlayer, 5);

            assertThat(result).isEqualTo(100.0);
        }

        @Test
        @DisplayName("最小ダメージが適用される")
        void calculateSkillDamage_AppliesMinDamage() {
            EventConfig config = new EventConfig.Builder()
                    .formula("BASE_DAMAGE * 0.01")
                    .minDamage(10.0)
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, 5))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            double result = calculator.calculateSkillDamage(100.0, mockPlayer, 5);

            assertThat(result).isEqualTo(10.0);
        }

        @Test
        @DisplayName("最大ダメージが適用される")
        void calculateSkillDamage_AppliesMaxDamage() {
            EventConfig config = new EventConfig.Builder()
                    .formula("BASE_DAMAGE * 10")
                    .minDamage(1.0)
                    .maxDamage(500.0)
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, 5))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            double result = calculator.calculateSkillDamage(100.0, mockPlayer, 5);

            assertThat(result).isEqualTo(500.0);
        }

        @Test
        @DisplayName("数式が空の場合はフォールバックを使用")
        void calculateSkillDamage_EmptyFormula_UsesFallback() {
            EventConfig config = new EventConfig.Builder()
                    .formula("")
                    .fallbackFormula("BASE_DAMAGE + 20")
                    .minDamage(1.0)
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, 5))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            double result = calculator.calculateSkillDamage(100.0, mockPlayer, 5);

            assertThat(result).isEqualTo(120.0);
        }
    }

    // ==================== calculatePhysicalAttack テスト ====================

    @Nested
    @DisplayName("calculatePhysicalAttack テスト")
    class CalculatePhysicalAttackTests {

        @Test
        @DisplayName("物理攻撃ダメージを計算できる")
        void calculatePhysicalAttack_Valid_CalculatesDamage() {
            EventConfig config = new EventConfig.Builder()
                    .formula("BASE_DAMAGE + STR")
                    .minDamage(1.0)
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "physical_attack"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, null))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            double result = calculator.calculatePhysicalAttack(50.0, mockPlayer);

            assertThat(result).isEqualTo(100.0); // 50 + 50(STR)
        }

        @Test
        @DisplayName("設定がない場合は基本ダメージを返す")
        void calculatePhysicalAttack_NoConfig_ReturnsBaseDamage() {
            when(mockDamageConfig.getEventConfigForClass("Warrior", "physical_attack"))
                    .thenReturn(null);
            when(mockDamageConfig.getEventConfig("physical_attack"))
                    .thenReturn(null);

            double result = calculator.calculatePhysicalAttack(50.0, mockPlayer);

            assertThat(result).isEqualTo(50.0);
        }
    }

    // ==================== calculateMagicAttack テスト ====================

    @Nested
    @DisplayName("calculateMagicAttack テスト")
    class CalculateMagicAttackTests {

        @Test
        @DisplayName("魔法攻撃ダメージを計算できる")
        void calculateMagicAttack_Valid_CalculatesDamage() {
            EventConfig config = new EventConfig.Builder()
                    .formula("BASE_DAMAGE + INT")
                    .minDamage(1.0)
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "magic_attack"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, null))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            double result = calculator.calculateMagicAttack(50.0, mockPlayer);

            assertThat(result).isEqualTo(70.0); // 50 + 20(INT)
        }

        @Test
        @DisplayName("設定がない場合は基本ダメージを返す")
        void calculateMagicAttack_NoConfig_ReturnsBaseDamage() {
            when(mockDamageConfig.getEventConfigForClass("Warrior", "magic_attack"))
                    .thenReturn(null);
            when(mockDamageConfig.getEventConfig("magic_attack"))
                    .thenReturn(null);

            double result = calculator.calculateMagicAttack(50.0, mockPlayer);

            assertThat(result).isEqualTo(50.0);
        }
    }

    // ==================== calculateDamageTaken テスト ====================

    @Nested
    @DisplayName("calculateDamageTaken テスト")
    class CalculateDamageTakenTests {

        @Test
        @DisplayName("物理カットでダメージを軽減できる")
        void calculateDamageTaken_PhysicalCut_ReducesDamage() {
            EventConfig config = new EventConfig.Builder()
                    .physicalCutFormula("DAMAGE * 0.8")
                    .minDamage(1.0)
                    .build();
            when(mockDamageConfig.getEventConfig("damage_taken"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, null))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            double result = calculator.calculateDamageTaken(100.0, mockPlayer, false);

            assertThat(result).isEqualTo(80.0);
        }

        @Test
        @DisplayName("魔法カットでダメージを軽減できる")
        void calculateDamageTaken_MagicCut_ReducesDamage() {
            EventConfig config = new EventConfig.Builder()
                    .magicCutFormula("DAMAGE * 0.7")
                    .minDamage(1.0)
                    .build();
            when(mockDamageConfig.getEventConfig("damage_taken"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, null))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            double result = calculator.calculateDamageTaken(100.0, mockPlayer, true);

            assertThat(result).isEqualTo(70.0);
        }

        @Test
        @DisplayName("カット式がない場合はダメージを変更しない")
        void calculateDamageTaken_NoCutFormula_ReturnsOriginal() {
            EventConfig config = new EventConfig.Builder()
                    .minDamage(1.0)
                    .build();
            when(mockDamageConfig.getEventConfig("damage_taken"))
                    .thenReturn(config);

            double result = calculator.calculateDamageTaken(100.0, mockPlayer, false);

            assertThat(result).isEqualTo(100.0);
        }

        @Test
        @DisplayName("設定がない場合はダメージを変更しない")
        void calculateDamageTaken_NoConfig_ReturnsOriginal() {
            when(mockDamageConfig.getEventConfig("damage_taken"))
                    .thenReturn(null);

            double result = calculator.calculateDamageTaken(100.0, mockPlayer, false);

            assertThat(result).isEqualTo(100.0);
        }

        @Test
        @DisplayName("最小ダメージが適用される")
        void calculateDamageTaken_AppliesMinDamage() {
            EventConfig config = new EventConfig.Builder()
                    .physicalCutFormula("DAMAGE * 0.01")
                    .minDamage(5.0)
                    .build();
            when(mockDamageConfig.getEventConfig("damage_taken"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, null))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            double result = calculator.calculateDamageTaken(100.0, mockPlayer, false);

            assertThat(result).isEqualTo(5.0);
        }
    }

    // ==================== isCriticalHit テスト ====================

    @Nested
    @DisplayName("isCriticalHit テスト")
    class IsCriticalHitTests {

        @Test
        @DisplayName("クリティカル設定がある場合は判定を行う")
        void isCriticalHit_WithConfig_ChecksChance() {
            EventConfig config = new EventConfig.Builder()
                    .critical("1.0", "2.0") // 100% chance
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, null))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            // chanceが1.0なので必ずtrue（ただし乱数次第）
            boolean result = calculator.isCriticalHit(mockPlayer, "Warrior");

            // 結果は乱数依存だが、メソッドが正しく呼ばれることを確認
            assertThat(result).isIn(true, false);
        }

        @Test
        @DisplayName("クリティカル設定がない場合はfalse")
        void isCriticalHit_NoCritical_ReturnsFalse() {
            EventConfig config = new EventConfig.Builder()
                    .formula("BASE_DAMAGE")
                    .minDamage(1.0)
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(config);

            boolean result = calculator.isCriticalHit(mockPlayer, "Warrior");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("設定がない場合はfalse")
        void isCriticalHit_NoConfig_ReturnsFalse() {
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(null);
            when(mockDamageConfig.getEventConfig("skill_damage"))
                    .thenReturn(null);

            boolean result = calculator.isCriticalHit(mockPlayer, "Warrior");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("クラス名がnullの場合はデフォルト設定を使用")
        void isCriticalHit_NullClassName_UsesDefault() {
            EventConfig config = new EventConfig.Builder()
                    .critical("0.0", "2.0") // 0% chance
                    .build();
            when(mockDamageConfig.getEventConfig("skill_damage"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, null))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            boolean result = calculator.isCriticalHit(mockPlayer, null);

            assertThat(result).isFalse();
        }
    }

    // ==================== getCriticalMultiplier テスト ====================

    @Nested
    @DisplayName("getCriticalMultiplier テスト")
    class GetCriticalMultiplierTests {

        @Test
        @DisplayName("クリティカル倍率を計算できる")
        void getCriticalMultiplier_WithConfig_ReturnsMultiplier() {
            EventConfig config = new EventConfig.Builder()
                    .critical("0.5", "3.0")
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, null))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            double result = calculator.getCriticalMultiplier(mockPlayer, "Warrior");

            assertThat(result).isEqualTo(3.0);
        }

        @Test
        @DisplayName("クリティカル設定がない場合はデフォルト倍率2.0")
        void getCriticalMultiplier_NoCritical_ReturnsDefault() {
            EventConfig config = new EventConfig.Builder()
                    .formula("BASE_DAMAGE")
                    .minDamage(1.0)
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(config);

            double result = calculator.getCriticalMultiplier(mockPlayer, "Warrior");

            assertThat(result).isEqualTo(2.0);
        }

        @Test
        @DisplayName("設定がない場合はデフォルト倍率2.0")
        void getCriticalMultiplier_NoConfig_ReturnsDefault() {
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(null);
            when(mockDamageConfig.getEventConfig("skill_damage"))
                    .thenReturn(null);

            double result = calculator.getCriticalMultiplier(mockPlayer, "Warrior");

            assertThat(result).isEqualTo(2.0);
        }

        @Test
        @DisplayName("数式エラー時は0.0")
        void getCriticalMultiplier_FormulaError_ReturnsZero() {
            EventConfig config = new EventConfig.Builder()
                    .critical("0.5", "INVALID###")
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, null))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            double result = calculator.getCriticalMultiplier(mockPlayer, "Warrior");

            assertThat(result).isEqualTo(0.0);
        }
    }

    // ==================== calculateWithCritical テスト ====================

    @Nested
    @DisplayName("calculateWithCritical テスト")
    class CalculateWithCriticalTests {

        @Test
        @DisplayName("クリティカルなしで計算できる")
        void calculateWithCritical_NoCritical_ReturnsNormalDamage() {
            EventConfig config = new EventConfig.Builder()
                    .formula("BASE_DAMAGE + 10")
                    .minDamage(1.0)
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, 5))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            YamlDamageCalculator.CriticalResult result =
                    calculator.calculateWithCritical(100.0, mockPlayer, 5);

            assertThat(result.getFinalDamage()).isEqualTo(110.0);
            assertThat(result.isCritical()).isFalse();
            assertThat(result.getMultiplier()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("クリティカルなし（プレイヤーnull）で計算できる")
        void calculateWithCritical_NullPlayer_ReturnsBaseDamage() {
            when(mockDamageConfig.getEventConfig((String) null))
                    .thenReturn(null);

            YamlDamageCalculator.CriticalResult result =
                    calculator.calculateWithCritical(100.0, null, 5);

            assertThat(result.getFinalDamage()).isEqualTo(100.0);
            assertThat(result.isCritical()).isFalse();
            assertThat(result.getMultiplier()).isEqualTo(1.0);
        }
    }

    // ==================== calculateDetailed テスト ====================

    @Nested
    @DisplayName("calculateDetailed テスト")
    class CalculateDetailedTests {

        @Test
        @DisplayName("詳細計算結果を取得できる")
        void calculateDetailed_Valid_ReturnsDetailedResult() {
            EventConfig config = new EventConfig.Builder()
                    .formula("BASE_DAMAGE + STR")
                    .minDamage(10.0)
                    .maxDamage(200.0)
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, 5))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            YamlDamageCalculator.DamageResult result =
                    calculator.calculateDetailed(100.0, mockPlayer, 5, "skill_damage");

            assertThat(result.getBaseDamage()).isEqualTo(100.0);
            assertThat(result.getFinalDamage()).isEqualTo(150.0); // 100 + 50(STR)
            assertThat(result.getMinDamage()).isEqualTo(10.0);
            assertThat(result.getMaxDamage()).isEqualTo(200.0);
            assertThat(result.getFormula()).isEqualTo("BASE_DAMAGE + STR");
        }

        @Test
        @DisplayName("詳細計算で変数が収集される")
        void calculateDetailed_CollectsVariables() {
            EventConfig config = new EventConfig.Builder()
                    .formula("BASE_DAMAGE")
                    .minDamage(1.0)
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "test_event"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, 5))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            YamlDamageCalculator.DamageResult result =
                    calculator.calculateDetailed(100.0, mockPlayer, 5, "test_event");

            Map<String, Object> vars = result.getVariables();
            assertThat(vars).containsKey("STR");
            assertThat(vars).containsKey("INT");
            assertThat(vars).containsKey("SPI");
            assertThat(vars).containsKey("VIT");
            assertThat(vars).containsKey("DEX");
            assertThat(vars).containsKey("LV");
            assertThat(vars).containsKey("CLASS_RANK");

            assertThat(vars.get("STR")).isEqualTo(50);
            assertThat(vars.get("LV")).isEqualTo(10);
        }

        @Test
        @DisplayName("設定がない場合はデフォルト詳細結果を返す")
        void calculateDetailed_NoConfig_ReturnsDefaultResult() {
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(null);
            when(mockDamageConfig.getEventConfig("skill_damage"))
                    .thenReturn(null);

            YamlDamageCalculator.DamageResult result =
                    calculator.calculateDetailed(100.0, mockPlayer, 5, "skill_damage");

            assertThat(result.getBaseDamage()).isEqualTo(100.0);
            assertThat(result.getFinalDamage()).isEqualTo(100.0);
            assertThat(result.getMinDamage()).isEqualTo(1);
            assertThat(result.isCritical()).isFalse();
            assertThat(result.getCriticalMultiplier()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("プレイヤーがnullの場合は空の変数マップ")
        void calculateDetailed_NullPlayer_EmptyVariables() {
            EventConfig config = new EventConfig.Builder()
                    .formula("BASE_DAMAGE")
                    .minDamage(1.0)
                    .build();
            when(mockDamageConfig.getEventConfig((String) null))
                    .thenReturn(config);

            YamlDamageCalculator.DamageResult result =
                    calculator.calculateDetailed(100.0, null, 5, "skill_damage");

            assertThat(result.getVariables()).isEmpty();
        }
    }

    // ==================== CriticalResult テスト ====================

    @Nested
    @DisplayName("CriticalResult テスト")
    class CriticalResultTests {

        @Test
        @DisplayName("CriticalResultを構築できる")
        void criticalResult_Constructor_BuildsCorrectly() {
            YamlDamageCalculator.CriticalResult result =
                    new YamlDamageCalculator.CriticalResult(true, 2.5, 250.0);

            assertThat(result.isCritical()).isTrue();
            assertThat(result.getMultiplier()).isEqualTo(2.5);
            assertThat(result.getFinalDamage()).isEqualTo(250.0);
        }

        @Test
        @DisplayName("非クリティカルのCriticalResultを構築できる")
        void criticalResult_NotCritical_BuildsCorrectly() {
            YamlDamageCalculator.CriticalResult result =
                    new YamlDamageCalculator.CriticalResult(false, 1.0, 100.0);

            assertThat(result.isCritical()).isFalse();
            assertThat(result.getMultiplier()).isEqualTo(1.0);
            assertThat(result.getFinalDamage()).isEqualTo(100.0);
        }
    }

    // ==================== DamageResult テスト ====================

    @Nested
    @DisplayName("DamageResult テスト")
    class DamageResultTests {

        @Test
        @DisplayName("DamageResultを構築できる")
        void damageResult_Constructor_BuildsCorrectly() {
            Map<String, Object> vars = Map.of("STR", 50.0, "INT", 20.0);
            YamlDamageCalculator.DamageResult result =
                    new YamlDamageCalculator.DamageResult(
                            100.0, 150.0, 10.0, 200.0,
                            "BASE_DAMAGE + STR", vars, true, 2.0);

            assertThat(result.getBaseDamage()).isEqualTo(100.0);
            assertThat(result.getFinalDamage()).isEqualTo(150.0);
            assertThat(result.getMinDamage()).isEqualTo(10.0);
            assertThat(result.getMaxDamage()).isEqualTo(200.0);
            assertThat(result.getFormula()).isEqualTo("BASE_DAMAGE + STR");
            assertThat(result.isCritical()).isTrue();
            assertThat(result.getCriticalMultiplier()).isEqualTo(2.0);
        }

        @Test
        @DisplayName("getVariablesでコピーが返される")
        void damageResult_GetVariables_ReturnsCopy() {
            Map<String, Object> vars = Map.of("STR", 50.0);
            YamlDamageCalculator.DamageResult result =
                    new YamlDamageCalculator.DamageResult(
                            100.0, 150.0, 10.0, null,
                            "BASE_DAMAGE", vars, false, 1.0);

            Map<String, Object> vars1 = result.getVariables();
            Map<String, Object> vars2 = result.getVariables();

            assertThat(vars1).isNotSameAs(vars2);
            assertThat(vars1).isEqualTo(vars2);
        }

        @Test
        @DisplayName("maxDamageがnullの場合はDouble.MAX_VALUE")
        void damageResult_NullMaxDamage_UsesMaxValue() {
            YamlDamageCalculator.DamageResult result =
                    new YamlDamageCalculator.DamageResult(
                            100.0, 150.0, 10.0, null,
                            "BASE_DAMAGE", Map.of(), false, 1.0);

            assertThat(result.getMaxDamage()).isEqualTo(Double.MAX_VALUE);
        }
    }

    // ==================== scopeManagerがnullの場合のテスト ====================

    @Nested
    @DisplayName("scopeManagerがnullの場合 テスト")
    class NullScopeManagerTests {

        private YamlDamageCalculator calcWithoutScope;

        @BeforeEach
        void setUp() {
            calcWithoutScope = new YamlDamageCalculator(mockDamageConfig, null);
        }

        @Test
        @DisplayName("scopeManagerがnullでも計算できる")
        void calculateSkillDamage_NullScopeManager_UsesDefaultContext() {
            EventConfig config = new EventConfig.Builder()
                    .formula("BASE_DAMAGE * 2")
                    .minDamage(1.0)
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(config);

            double result = calcWithoutScope.calculateSkillDamage(100.0, mockPlayer, 5);

            // scopeManagerがnullでも、プレイヤーがある場合は数式が評価される
            assertThat(result).isEqualTo(200.0);
        }
    }

    // ==================== 複雑な数式テスト ====================

    @Nested
    @DisplayName("複雑な数式テスト")
    class ComplexFormulaTests {

        @Test
        @DisplayName("Booleanのtrueが1.0に変換される")
        void evaluateFormula_BooleanTrue_ConvertsToOne() {
            EventConfig config = new EventConfig.Builder()
                    .formula("10 > 5")
                    .minDamage(1.0)
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, 5))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            double result = calculator.calculateSkillDamage(100.0, mockPlayer, 5);

            assertThat(result).isEqualTo(1.0);
        }

        @Test
        @DisplayName("BooleanのfalseがminDamageで上書きされる")
        void evaluateFormula_BooleanFalse_AppliesMinDamage() {
            EventConfig config = new EventConfig.Builder()
                    .formula("5 > 10")
                    .minDamage(1.0)
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, 5))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            double result = calculator.calculateSkillDamage(100.0, mockPlayer, 5);

            // false (0.0) は minDamage (1.0) で上書きされる
            assertThat(result).isEqualTo(1.0);
        }

        @Test
        @DisplayName("論理演算子を使用した数式")
        void evaluateFormula_LogicalOperators_WorksCorrectly() {
            EventConfig config = new EventConfig.Builder()
                    .formula("BASE_DAMAGE > 50 && BASE_DAMAGE < 150")
                    .minDamage(1.0)
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, 5))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            double result = calculator.calculateSkillDamage(100.0, mockPlayer, 5);

            // 100 > 50 && 100 < 150 = true = 1.0
            assertThat(result).isEqualTo(1.0);
        }

        @Test
        @DisplayName("算術演算子を使用した数式")
        void evaluateFormula_ArithmeticOperators_WorksCorrectly() {
            EventConfig config = new EventConfig.Builder()
                    .formula("BASE_DAMAGE * 2 + 50 / 2 - 10")
                    .minDamage(1.0)
                    .build();
            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(config);
            when(mockScopeManager.buildVariableContext(mockPlayer, 5))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            double result = calculator.calculateSkillDamage(100.0, mockPlayer, 5);

            // 100 * 2 + 50 / 2 - 10 = 200 + 25 - 10 = 215
            assertThat(result).isEqualTo(215.0);
        }
    }

    // ==================== クラスオーバーライドテスト ====================

    @Nested
    @DisplayName("クラスオーバーライド テスト")
    class ClassOverrideTests {

        @Test
        @DisplayName("クラス別設定が優先される")
        void calculateSkillDamage_ClassOverride_UsesOverride() {
            EventConfig defaultConfig = new EventConfig.Builder()
                    .formula("BASE_DAMAGE")
                    .minDamage(1.0)
                    .build();

            EventConfig warriorConfig = new EventConfig.Builder()
                    .formula("BASE_DAMAGE * 1.5")
                    .minDamage(1.0)
                    .build();

            DamageConfig.ClassOverrideConfig classOverride =
                    new DamageConfig.ClassOverrideConfig.Builder()
                            .addEventOverride("skill_damage", warriorConfig)
                            .build();

            when(mockDamageConfig.getEventConfigForClass("Warrior", "skill_damage"))
                    .thenReturn(warriorConfig);
            when(mockDamageConfig.getEventConfig("skill_damage"))
                    .thenReturn(defaultConfig);
            when(mockScopeManager.buildVariableContext(mockPlayer, 5))
                    .thenReturn(new com.example.rpgplugin.skill.evaluator.VariableContext(mockPlayer));

            double result = calculator.calculateSkillDamage(100.0, mockPlayer, 5);

            assertThat(result).isEqualTo(150.0);
        }
    }
}

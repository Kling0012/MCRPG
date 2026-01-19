package com.example.rpgplugin.skill.component.mechanic;

import com.example.rpgplugin.skill.component.ComponentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpeedMechanicのテストクラス
 *
 * <p>注：SpeedMechanicはBukkit APIのPotionEffectTypeに依存しており、
 * Mockitoでの完全なモック化は制限されています。このテストでは：</p>
 * <ul>
 *   <li>基本機能テスト（コンストラクタ、getType）</li>
 *   <li>設定管理のテスト</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpeedMechanic Tests")
class SpeedMechanicTest {

    private SpeedMechanic mechanic;

    @BeforeEach
    void setUp() {
        mechanic = new SpeedMechanic();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("コンストラクタ - キーはspeed")
        void constructorKeyIsSpeed() {
            assertEquals("speed", mechanic.getKey());
        }
    }

    @Nested
    @DisplayName("getType() Tests")
    class GetTypeTests {

        @Test
        @DisplayName("getType - MECHANIC を返す")
        void getTypeReturnsMechanic() {
            assertEquals(ComponentType.MECHANIC, mechanic.getType());
        }
    }

    @Nested
    @DisplayName("Settings Tests")
    class SettingsTests {

        @Test
        @DisplayName("getSettings - nullでない")
        void getSettingsNotNull() {
            assertNotNull(mechanic.getSettings());
        }

        @Test
        @DisplayName("複数インスタンス - 別々の設定を持つ")
        void multipleInstancesHaveSeparateSettings() {
            SpeedMechanic mechanic1 = new SpeedMechanic();
            SpeedMechanic mechanic2 = new SpeedMechanic();

            mechanic1.getSettings().set("duration", 5.0);
            mechanic2.getSettings().set("duration", 10.0);

            assertNotEquals(mechanic1.getSettings().getDouble("duration", 0),
                    mechanic2.getSettings().getDouble("duration", 0),
                    "Settings should not be shared between instances");
        }

        @Test
        @DisplayName("設定値の取得と設定")
        void settingsGetSet() {
            mechanic.getSettings().set("duration", 10.0);
            mechanic.getSettings().set("amplifier", 2);
            mechanic.getSettings().set("ambient", false);

            assertEquals(10.0, mechanic.getSettings().getDouble("duration", 0));
            assertEquals(2, mechanic.getSettings().getInt("amplifier", 0));
            assertFalse(mechanic.getSettings().getBoolean("ambient", true));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("getChildren - 空リスト")
        void getChildrenReturnsEmpty() {
            assertTrue(mechanic.getChildren().isEmpty());
        }
    }
}

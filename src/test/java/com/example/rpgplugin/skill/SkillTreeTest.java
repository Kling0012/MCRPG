package com.example.rpgplugin.skill;

import com.example.rpgplugin.model.skill.SkillTreeConfig;
import com.example.rpgplugin.model.skill.UnlockRequirement;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SkillTreeのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SkillTree: スキルツリー")
class SkillTreeTest {

    @Mock
    private Skill mockSkill;

    @Mock
    private Skill mockSkill2;

    @Mock
    private SkillTreeConfig mockTreeConfig;

    @Mock
    private SkillTreeConfig mockTreeConfig2;

    @Mock
    private Player mockPlayer;

    @Mock
    private com.example.rpgplugin.stats.Stat mockStat;

    private SkillTree tree;

    @BeforeEach
    void setUp() {
        tree = new SkillTree("warrior");

        // mockSkillの基本設定
        when(mockSkill.getId()).thenReturn("testSkill");
        when(mockSkill.getSkillTree()).thenReturn(mockTreeConfig);

        // mockSkill2の基本設定
        when(mockSkill2.getId()).thenReturn("testSkill2");
        when(mockSkill2.getSkillTree()).thenReturn(mockTreeConfig2);
    }

    // ========== コンストラクタ テスト ==========

    @Nested
    @DisplayName("コンストラクタ: 初期化")
    class ConstructorTests {

        @Test
        @DisplayName("test: クラスIDを設定して初期化")
        void testConstructorWithClassId() {
            SkillTree warriorTree = new SkillTree("warrior");

            assertEquals("warrior", warriorTree.getClassId());
            assertEquals(0, warriorTree.getNodeCount());
        }

        @Test
        @DisplayName("test: 空のノードマップで初期化")
        void testConstructorInitializesEmptyNodes() {
            SkillTree newTree = new SkillTree("mage");

            assertEquals(0, newTree.getAllNodes().size());
            assertEquals(0, newTree.getRootNodes().size());
        }
    }

    // ========== addNode() テスト ==========

    @Nested
    @DisplayName("addNode: ノードの追加")
    class AddNodeTests {

        @Test
        @DisplayName("test: ルートノードを追加")
        void testAddRootNode() {
            SkillNode rootNode = new SkillNode(mockSkill, null);

            tree.addNode(rootNode);

            assertEquals(1, tree.getNodeCount());
            assertSame(rootNode, tree.getNode("testSkill"));
            assertEquals(1, tree.getRootNodes().size());
        }

        @Test
        @DisplayName("test: 子ノードを追加")
        void testAddChildNode() {
            SkillNode rootNode = new SkillNode(mockSkill, null);
            SkillNode childNode = new SkillNode(mockSkill2, rootNode);

            tree.addNode(rootNode);
            tree.addNode(childNode);

            assertEquals(2, tree.getNodeCount());
            assertSame(childNode, tree.getNode("testSkill2"));
            assertEquals(1, tree.getRootNodes().size()); // ルートは1つ
        }

        @Test
        @DisplayName("test: 複数のルートノードを追加")
        void testAddMultipleRootNodes() {
            SkillNode root1 = new SkillNode(mockSkill, null);
            SkillNode root2 = new SkillNode(mockSkill2, null);

            tree.addNode(root1);
            tree.addNode(root2);

            assertEquals(2, tree.getNodeCount());
            assertEquals(2, tree.getRootNodes().size());
        }

        @Test
        @DisplayName("test: ノード追加で親子関係が自動構築される")
        void testAddNodeBuildsParentChildRelationship() {
            SkillNode rootNode = new SkillNode(mockSkill, null);
            SkillNode childNode = new SkillNode(mockSkill2, rootNode);

            tree.addNode(rootNode);
            tree.addNode(childNode);

            List<SkillNode> children = tree.getChildren("testSkill");
            assertEquals(1, children.size());
            assertTrue(children.contains(childNode));
        }

        @Test
        @DisplayName("test: 同じノードを再追加しても上書きされる")
        void testAddSameNodeAgain() {
            SkillNode node = new SkillNode(mockSkill, null);

            tree.addNode(node);
            tree.addNode(node); // 同じノードを再度追加

            assertEquals(1, tree.getNodeCount());
        }
    }

    // ========== getNode() テスト ==========

    @Nested
    @DisplayName("getNode: ノードの取得")
    class GetNodeTests {

        @Test
        @DisplayName("test: 登録済みノードを取得")
        void testGetRegisteredNode() {
            SkillNode node = new SkillNode(mockSkill, null);
            tree.addNode(node);

            SkillNode result = tree.getNode("testSkill");

            assertSame(node, result);
        }

        @Test
        @DisplayName("test: 未登録ノードはnull")
        void testGetUnregisteredNode() {
            SkillNode result = tree.getNode("nonexistent");

            assertNull(result);
        }

        @Test
        @DisplayName("test: 空のツリーから取得")
        void testGetNodeFromEmptyTree() {
            assertNull(tree.getNode("anySkill"));
        }
    }

    // ========== getAllNodes() テスト ==========

    @Nested
    @DisplayName("getAllNodes: 全ノード取得")
    class GetAllNodesTests {

        @Test
        @DisplayName("test: 全ノードのマップを取得")
        void testGetAllNodes() {
            SkillNode node1 = new SkillNode(mockSkill, null);
            SkillNode node2 = new SkillNode(mockSkill2, null);

            tree.addNode(node1);
            tree.addNode(node2);

            Map<String, SkillNode> allNodes = tree.getAllNodes();

            assertEquals(2, allNodes.size());
            assertTrue(allNodes.containsKey("testSkill"));
            assertTrue(allNodes.containsKey("testSkill2"));
        }

        @Test
        @DisplayName("test: 返されたマップはコピー")
        void testGetAllNodesReturnsCopy() {
            SkillNode node = new SkillNode(mockSkill, null);
            tree.addNode(node);

            Map<String, SkillNode> nodes1 = tree.getAllNodes();
            Map<String, SkillNode> nodes2 = tree.getAllNodes();

            // 別のインスタンス
            assertNotSame(nodes1, nodes2);
            // 内容は同じ
            assertEquals(nodes1, nodes2);
        }

        @Test
        @DisplayName("test: 返されたマップへの変更は元に影響しない")
        void testGetAllNodesCopyIsIndependent() {
            SkillNode node = new SkillNode(mockSkill, null);
            tree.addNode(node);

            Map<String, SkillNode> nodes = tree.getAllNodes();
            nodes.clear(); // コピーをクリア

            assertEquals(1, tree.getNodeCount()); // 元は影響を受けない
        }

        @Test
        @DisplayName("test: 空のツリーでは空のマップ")
        void testGetAllNodesEmpty() {
            Map<String, SkillNode> allNodes = tree.getAllNodes();

            assertNotNull(allNodes);
            assertTrue(allNodes.isEmpty());
        }
    }

    // ========== getRootNodes() テスト ==========

    @Nested
    @DisplayName("getRootNodes: ルートノード取得")
    class GetRootNodesTests {

        @Test
        @DisplayName("test: ルートノードのリストを取得")
        void testGetRootNodes() {
            SkillNode root1 = new SkillNode(mockSkill, null);
            SkillNode root2 = new SkillNode(mockSkill2, null);

            tree.addNode(root1);
            tree.addNode(root2);

            List<SkillNode> roots = tree.getRootNodes();

            assertEquals(2, roots.size());
            assertTrue(roots.contains(root1));
            assertTrue(roots.contains(root2));
        }

        @Test
        @DisplayName("test: 返されたリストはコピー")
        void testGetRootNodesReturnsCopy() {
            SkillNode root = new SkillNode(mockSkill, null);
            tree.addNode(root);

            List<SkillNode> roots1 = tree.getRootNodes();
            List<SkillNode> roots2 = tree.getRootNodes();

            // 別のインスタンス
            assertNotSame(roots1, roots2);
            // 内容は同じ
            assertEquals(roots1, roots2);
        }

        @Test
        @DisplayName("test: 子ノードはルートに含まれない")
        void testChildNodesNotInRoots() {
            SkillNode root = new SkillNode(mockSkill, null);
            SkillNode child = new SkillNode(mockSkill2, root);

            tree.addNode(root);
            tree.addNode(child);

            List<SkillNode> roots = tree.getRootNodes();

            assertEquals(1, roots.size());
            assertTrue(roots.contains(root));
            assertFalse(roots.contains(child));
        }
    }

    // ========== getClassId() テスト ==========

    @Nested
    @DisplayName("getClassId: クラスID取得")
    class GetClassIdTests {

        @Test
        @DisplayName("test: クラスIDを取得")
        void testGetClassId() {
            assertEquals("warrior", tree.getClassId());
        }

        @Test
        @DisplayName("test: 異なるクラスIDのツリー")
        void testDifferentClassId() {
            SkillTree mageTree = new SkillTree("mage");
            assertEquals("mage", mageTree.getClassId());
        }
    }

    // ========== canAcquire() テスト ==========

    @Nested
    @DisplayName("canAcquire: 習得可能チェック")
    class CanAcquireTests {

        @Test
        @DisplayName("test: 存在しないスキルはfalse")
        void testCanAcquireNonExistentSkill() {
            Map<com.example.rpgplugin.stats.Stat, Double> stats = new HashMap<>();

            boolean result = tree.canAcquire(mockPlayer, "nonexistent", 10, stats);

            assertFalse(result);
        }

        @Test
        @DisplayName("test: SkillTreeConfigがnullなら習得可能")
        void testCanAcquireWithNullConfig() {
            when(mockSkill.getSkillTree()).thenReturn(null);
            SkillNode node = new SkillNode(mockSkill, null);
            tree.addNode(node);

            Map<com.example.rpgplugin.stats.Stat, Double> stats = new HashMap<>();

            boolean result = tree.canAcquire(mockPlayer, "testSkill", 10, stats);

            assertTrue(result);
        }

        @Test
        @DisplayName("test: 親がnoneの場合は習得可能")
        void testCanAcquireWithNoneParent() {
            when(mockTreeConfig.getParent()).thenReturn("none");
            when(mockTreeConfig.getUnlockRequirements()).thenReturn(new ArrayList<>());

            SkillNode node = new SkillNode(mockSkill, null);
            tree.addNode(node);

            Map<com.example.rpgplugin.stats.Stat, Double> stats = new HashMap<>();

            boolean result = tree.canAcquire(mockPlayer, "testSkill", 10, stats);

            assertTrue(result);
        }

        @Test
        @DisplayName("test: 親スキルが存在しない場合はfalse")
        void testCanAcquireWithMissingParent() {
            when(mockTreeConfig.getParent()).thenReturn("missingParent");
            when(mockTreeConfig.getUnlockRequirements()).thenReturn(new ArrayList<>());

            SkillNode node = new SkillNode(mockSkill, null);
            tree.addNode(node);

            Map<com.example.rpgplugin.stats.Stat, Double> stats = new HashMap<>();

            boolean result = tree.canAcquire(mockPlayer, "testSkill", 10, stats);

            assertFalse(result);
        }

        @Test
        @DisplayName("test: レベル要件を満たしている")
        void testCanAcquireMeetsLevelRequirement() {
            UnlockRequirement levelReq = mock(UnlockRequirement.class);
            when(levelReq.getType()).thenReturn("level");
            when(levelReq.getValue()).thenReturn(10.0);

            List<UnlockRequirement> reqs = new ArrayList<>();
            reqs.add(levelReq);

            when(mockTreeConfig.getParent()).thenReturn("none");
            when(mockTreeConfig.getUnlockRequirements()).thenReturn(reqs);

            SkillNode node = new SkillNode(mockSkill, null);
            tree.addNode(node);

            Map<com.example.rpgplugin.stats.Stat, Double> stats = new HashMap<>();

            // レベル10以上で習得可能
            assertTrue(tree.canAcquire(mockPlayer, "testSkill", 10, stats));
            assertTrue(tree.canAcquire(mockPlayer, "testSkill", 15, stats));
            assertFalse(tree.canAcquire(mockPlayer, "testSkill", 5, stats));
        }

        @Test
        @DisplayName("test: ステータス要件を満たしている")
        void testCanAcquireMeetsStatRequirement() {
            UnlockRequirement statReq = mock(UnlockRequirement.class);
            when(statReq.getType()).thenReturn("stat");
            when(statReq.getValue()).thenReturn(50.0);
            when(statReq.getStat()).thenReturn(mockStat);

            List<UnlockRequirement> reqs = new ArrayList<>();
            reqs.add(statReq);

            when(mockTreeConfig.getParent()).thenReturn("none");
            when(mockTreeConfig.getUnlockRequirements()).thenReturn(reqs);

            SkillNode node = new SkillNode(mockSkill, null);
            tree.addNode(node);

            Map<com.example.rpgplugin.stats.Stat, Double> stats = new HashMap<>();
            stats.put(mockStat, 60.0);

            assertTrue(tree.canAcquire(mockPlayer, "testSkill", 10, stats));
        }

        @Test
        @DisplayName("test: ステータス要件を満たしていない")
        void testCanAcquireFailsStatRequirement() {
            UnlockRequirement statReq = mock(UnlockRequirement.class);
            when(statReq.getType()).thenReturn("stat");
            when(statReq.getValue()).thenReturn(50.0);
            when(statReq.getStat()).thenReturn(mockStat);

            List<UnlockRequirement> reqs = new ArrayList<>();
            reqs.add(statReq);

            when(mockTreeConfig.getParent()).thenReturn("none");
            when(mockTreeConfig.getUnlockRequirements()).thenReturn(reqs);

            SkillNode node = new SkillNode(mockSkill, null);
            tree.addNode(node);

            Map<com.example.rpgplugin.stats.Stat, Double> stats = new HashMap<>();
            stats.put(mockStat, 30.0); // 50未満

            assertFalse(tree.canAcquire(mockPlayer, "testSkill", 10, stats));
        }

        @Test
        @DisplayName("test: ステータスがnullの場合はfalse")
        void testCanAcquireWithNullStat() {
            UnlockRequirement statReq = mock(UnlockRequirement.class);
            when(statReq.getType()).thenReturn("stat");
            when(statReq.getValue()).thenReturn(50.0);
            when(statReq.getStat()).thenReturn(null);

            List<UnlockRequirement> reqs = new ArrayList<>();
            reqs.add(statReq);

            when(mockTreeConfig.getParent()).thenReturn("none");
            when(mockTreeConfig.getUnlockRequirements()).thenReturn(reqs);

            SkillNode node = new SkillNode(mockSkill, null);
            tree.addNode(node);

            Map<com.example.rpgplugin.stats.Stat, Double> stats = new HashMap<>();

            assertFalse(tree.canAcquire(mockPlayer, "testSkill", 10, stats));
        }

        @Test
        @DisplayName("test: 無効な要件タイプはfalse")
        void testCanAcquireWithInvalidRequirementType() {
            UnlockRequirement invalidReq = mock(UnlockRequirement.class);
            when(invalidReq.getType()).thenReturn("invalid");
            when(invalidReq.getValue()).thenReturn(10.0);

            List<UnlockRequirement> reqs = new ArrayList<>();
            reqs.add(invalidReq);

            when(mockTreeConfig.getParent()).thenReturn("none");
            when(mockTreeConfig.getUnlockRequirements()).thenReturn(reqs);

            SkillNode node = new SkillNode(mockSkill, null);
            tree.addNode(node);

            Map<com.example.rpgplugin.stats.Stat, Double> stats = new HashMap<>();

            assertFalse(tree.canAcquire(mockPlayer, "testSkill", 10, stats));
        }

        @Test
        @DisplayName("test: 大文字小文字を区別しない要件タイプ")
        void testCanAcquireCaseInsensitiveRequirementType() {
            UnlockRequirement levelReq = mock(UnlockRequirement.class);
            when(levelReq.getType()).thenReturn("LEVEL"); // 大文字
            when(levelReq.getValue()).thenReturn(10.0);

            List<UnlockRequirement> reqs = new ArrayList<>();
            reqs.add(levelReq);

            when(mockTreeConfig.getParent()).thenReturn("none");
            when(mockTreeConfig.getUnlockRequirements()).thenReturn(reqs);

            SkillNode node = new SkillNode(mockSkill, null);
            tree.addNode(node);

            Map<com.example.rpgplugin.stats.Stat, Double> stats = new HashMap<>();

            assertTrue(tree.canAcquire(mockPlayer, "testSkill", 10, stats));
        }

        @Test
        @DisplayName("test: 複数の要件をすべて満たす")
        void testCanAcquireMeetsMultipleRequirements() {
            UnlockRequirement levelReq = mock(UnlockRequirement.class);
            when(levelReq.getType()).thenReturn("level");
            when(levelReq.getValue()).thenReturn(10.0);

            UnlockRequirement statReq = mock(UnlockRequirement.class);
            when(statReq.getType()).thenReturn("stat");
            when(statReq.getValue()).thenReturn(50.0);
            when(statReq.getStat()).thenReturn(mockStat);

            List<UnlockRequirement> reqs = new ArrayList<>();
            reqs.add(levelReq);
            reqs.add(statReq);

            when(mockTreeConfig.getParent()).thenReturn("none");
            when(mockTreeConfig.getUnlockRequirements()).thenReturn(reqs);

            SkillNode node = new SkillNode(mockSkill, null);
            tree.addNode(node);

            Map<com.example.rpgplugin.stats.Stat, Double> stats = new HashMap<>();
            stats.put(mockStat, 60.0);

            assertTrue(tree.canAcquire(mockPlayer, "testSkill", 10, stats));
        }

        @Test
        @DisplayName("test: いずれかの要件を満たさないとfalse")
        void testCanAcquireFailsOneRequirement() {
            UnlockRequirement levelReq = mock(UnlockRequirement.class);
            when(levelReq.getType()).thenReturn("level");
            when(levelReq.getValue()).thenReturn(10.0);

            UnlockRequirement statReq = mock(UnlockRequirement.class);
            when(statReq.getType()).thenReturn("stat");
            when(statReq.getValue()).thenReturn(50.0);
            when(statReq.getStat()).thenReturn(mockStat);

            List<UnlockRequirement> reqs = new ArrayList<>();
            reqs.add(levelReq);
            reqs.add(statReq);

            when(mockTreeConfig.getParent()).thenReturn("none");
            when(mockTreeConfig.getUnlockRequirements()).thenReturn(reqs);

            SkillNode node = new SkillNode(mockSkill, null);
            tree.addNode(node);

            Map<com.example.rpgplugin.stats.Stat, Double> stats = new HashMap<>();
            stats.put(mockStat, 30.0); // ステータス要件未満

            assertFalse(tree.canAcquire(mockPlayer, "testSkill", 10, stats));
        }

        @Test
        @DisplayName("test: ステータス未設定時はデフォルト0を使用")
        void testCanAcquireUsesDefaultStatValue() {
            UnlockRequirement statReq = mock(UnlockRequirement.class);
            when(statReq.getType()).thenReturn("stat");
            when(statReq.getValue()).thenReturn(10.0);
            when(statReq.getStat()).thenReturn(mockStat);

            List<UnlockRequirement> reqs = new ArrayList<>();
            reqs.add(statReq);

            when(mockTreeConfig.getParent()).thenReturn("none");
            when(mockTreeConfig.getUnlockRequirements()).thenReturn(reqs);

            SkillNode node = new SkillNode(mockSkill, null);
            tree.addNode(node);

            Map<com.example.rpgplugin.stats.Stat, Double> stats = new HashMap<>();
            // mockStatを含まない → デフォルト0.0

            assertFalse(tree.canAcquire(mockPlayer, "testSkill", 10, stats));
        }
    }

    // ========== getCost() テスト ==========

    @Nested
    @DisplayName("getCost: コスト取得")
    class GetCostTests {

        @Test
        @DisplayName("test: 設定されたコストを取得")
        void testGetCost() {
            when(mockTreeConfig.getCost()).thenReturn(5);

            SkillNode node = new SkillNode(mockSkill, null);
            tree.addNode(node);

            assertEquals(5, tree.getCost("testSkill"));
        }

        @Test
        @DisplayName("test: コンフィグがnullならデフォルトコスト1")
        void testGetCostWithNullConfig() {
            when(mockSkill.getSkillTree()).thenReturn(null);

            SkillNode node = new SkillNode(mockSkill, null);
            tree.addNode(node);

            assertEquals(1, tree.getCost("testSkill"));
        }

        @Test
        @DisplayName("test: 未登録スキルはコスト0")
        void testGetCostNonExistentSkill() {
            assertEquals(0, tree.getCost("nonexistent"));
        }
    }

    // ========== getParentSkillId() テスト ==========

    @Nested
    @DisplayName("getParentSkillId: 親スキルID取得")
    class GetParentSkillIdTests {

        @Test
        @DisplayName("test: 親スキルIDを取得")
        void testGetParentSkillId() {
            // 親スキルのモック
            Skill parentSkill = mock(Skill.class);
            when(parentSkill.getId()).thenReturn("parentSkill");
            when(parentSkill.getSkillTree()).thenReturn(null);

            // 子スキルの設定
            when(mockTreeConfig.getParent()).thenReturn("parentSkill");

            // ルートノードと子ノードを作成
            SkillNode parentNode = new SkillNode(parentSkill, null);
            SkillNode childNode = new SkillNode(mockSkill, parentNode);

            tree.addNode(parentNode);
            tree.addNode(childNode);

            assertEquals("parentSkill", tree.getParentSkillId("testSkill"));
        }

        @Test
        @DisplayName("test: ルートノードはnull")
        void testGetParentSkillIdForRoot() {
            SkillNode rootNode = new SkillNode(mockSkill, null);
            tree.addNode(rootNode);

            assertNull(tree.getParentSkillId("testSkill"));
        }

        @Test
        @DisplayName("test: parentがnoneならnull")
        void testGetParentSkillIdWithNoneParent() {
            // 親スキルのモック
            Skill parentSkill = mock(Skill.class);
            when(parentSkill.getId()).thenReturn("parentSkill");
            when(parentSkill.getSkillTree()).thenReturn(null);

            // 子スキルの設定
            when(mockTreeConfig.getParent()).thenReturn("none");

            // ルートノードと子ノードを作成
            SkillNode parentNode = new SkillNode(parentSkill, null);
            SkillNode childNode = new SkillNode(mockSkill, parentNode);

            tree.addNode(parentNode);
            tree.addNode(childNode);

            assertNull(tree.getParentSkillId("testSkill"));
        }

        @Test
        @DisplayName("test: NONE（大文字）でもnull")
        void testGetParentSkillIdWithNoneUpperCase() {
            // 親スキルのモック
            Skill parentSkill = mock(Skill.class);
            when(parentSkill.getId()).thenReturn("parentSkill");
            when(parentSkill.getSkillTree()).thenReturn(null);

            // 子スキルの設定
            when(mockTreeConfig.getParent()).thenReturn("NONE");

            // ルートノードと子ノードを作成
            SkillNode parentNode = new SkillNode(parentSkill, null);
            SkillNode childNode = new SkillNode(mockSkill, parentNode);

            tree.addNode(parentNode);
            tree.addNode(childNode);

            assertNull(tree.getParentSkillId("testSkill"));
        }

        @Test
        @DisplayName("test: コンフィグがnullならnull")
        void testGetParentSkillIdWithNullConfig() {
            // 親スキルのモック
            Skill parentSkill = mock(Skill.class);
            when(parentSkill.getId()).thenReturn("parentSkill");
            when(parentSkill.getSkillTree()).thenReturn(null);

            // 子スキルの設定
            when(mockSkill.getSkillTree()).thenReturn(null);

            // ルートノードと子ノードを作成
            SkillNode parentNode = new SkillNode(parentSkill, null);
            SkillNode childNode = new SkillNode(mockSkill, parentNode);

            tree.addNode(parentNode);
            tree.addNode(childNode);

            assertNull(tree.getParentSkillId("testSkill"));
        }

        @Test
        @DisplayName("test: 未登録スキルはnull")
        void testGetParentSkillIdNonExistentSkill() {
            assertNull(tree.getParentSkillId("nonexistent"));
        }
    }

    // ========== getChildren() テスト ==========

    @Nested
    @DisplayName("getChildren: 子スキル取得")
    class GetChildrenTests {

        @Test
        @DisplayName("test: 子スキルのリストを取得")
        void testGetChildren() {
            // 別々のスキルIDを使用
            Skill childSkill1 = mock(Skill.class);
            when(childSkill1.getId()).thenReturn("childSkill1");
            Skill childSkill2 = mock(Skill.class);
            when(childSkill2.getId()).thenReturn("childSkill2");

            SkillNode root = new SkillNode(mockSkill, null);
            SkillNode child1 = new SkillNode(childSkill1, root);
            SkillNode child2 = new SkillNode(childSkill2, root);

            tree.addNode(root);
            tree.addNode(child1);
            tree.addNode(child2);

            List<SkillNode> children = tree.getChildren("testSkill");

            assertEquals(2, children.size());
            assertTrue(children.contains(child1));
            assertTrue(children.contains(child2));
        }

        @Test
        @DisplayName("test: 未登録スキルは空のリスト")
        void testGetChildrenNonExistentSkill() {
            List<SkillNode> children = tree.getChildren("nonexistent");

            assertNotNull(children);
            assertTrue(children.isEmpty());
        }

        @Test
        @DisplayName("test: リーフノードは空のリスト")
        void testGetChildrenForLeaf() {
            SkillNode leaf = new SkillNode(mockSkill, null);
            tree.addNode(leaf);

            List<SkillNode> children = tree.getChildren("testSkill");

            assertNotNull(children);
            assertTrue(children.isEmpty());
        }
    }

    // ========== isRootSkill() テスト ==========

    @Nested
    @DisplayName("isRootSkill: ルートスキル判定")
    class IsRootSkillTests {

        @Test
        @DisplayName("test: ルートスキルはtrue")
        void testIsRootSkillTrue() {
            SkillNode root = new SkillNode(mockSkill, null);
            tree.addNode(root);

            assertTrue(tree.isRootSkill("testSkill"));
        }

        @Test
        @DisplayName("test: 子スキルはfalse")
        void testIsRootSkillFalse() {
            SkillNode root = new SkillNode(mockSkill, null);
            SkillNode child = new SkillNode(mockSkill2, root);

            tree.addNode(root);
            tree.addNode(child);

            assertFalse(tree.isRootSkill("testSkill2"));
        }

        @Test
        @DisplayName("test: 未登録スキルはfalse")
        void testIsRootSkillNonExistent() {
            assertFalse(tree.isRootSkill("nonexistent"));
        }
    }

    // ========== getMaxDepth() テスト ==========

    @Nested
    @DisplayName("getMaxDepth: 最大深さ取得")
    class GetMaxDepthTests {

        @Test
        @DisplayName("test: 空のツリーは深さ0")
        void testGetMaxDepthEmpty() {
            assertEquals(0, tree.getMaxDepth());
        }

        @Test
        @DisplayName("test: ルートのみは深さ0")
        void testGetMaxDepthRootOnly() {
            SkillNode root = new SkillNode(mockSkill, null);
            tree.addNode(root);

            assertEquals(0, tree.getMaxDepth());
        }

        @Test
        @DisplayName("test: 1レベルの子は深さ1")
        void testGetMaxDepthWithChild() {
            SkillNode root = new SkillNode(mockSkill, null);
            SkillNode child = new SkillNode(mockSkill2, root);

            tree.addNode(root);
            tree.addNode(child);

            assertEquals(1, tree.getMaxDepth());
        }

        @Test
        @DisplayName("test: 複雑なツリーの最大深さ")
        void testGetMaxDepthComplexTree() {
            //       root (depth 0)
            //      / | \
            //     a  b  c (depth 1)
            //        /\
            //       d  e (depth 2)

            SkillNode root = new SkillNode(mockSkill, null);
            SkillNode nodeA = new SkillNode(mockSkill2, root);
            SkillNode nodeB = new SkillNode(mockSkill, root);
            SkillNode nodeC = new SkillNode(mockSkill2, root);
            SkillNode nodeD = new SkillNode(mockSkill, nodeB);
            SkillNode nodeE = new SkillNode(mockSkill2, nodeB);

            tree.addNode(root);
            tree.addNode(nodeA);
            tree.addNode(nodeB);
            tree.addNode(nodeC);
            tree.addNode(nodeD);
            tree.addNode(nodeE);

            assertEquals(2, tree.getMaxDepth());
        }
    }

    // ========== getNodeCount() テスト ==========

    @Nested
    @DisplayName("getNodeCount: ノード数取得")
    class GetNodeCountTests {

        @Test
        @DisplayName("test: 空のツリーは0")
        void testGetNodeCountEmpty() {
            assertEquals(0, tree.getNodeCount());
        }

        @Test
        @DisplayName("test: ノードを追加すると増加")
        void testGetNodeCountIncrements() {
            assertEquals(0, tree.getNodeCount());

            SkillNode node1 = new SkillNode(mockSkill, null);
            tree.addNode(node1);
            assertEquals(1, tree.getNodeCount());

            SkillNode node2 = new SkillNode(mockSkill2, null);
            tree.addNode(node2);
            assertEquals(2, tree.getNodeCount());
        }
    }

    // ========== toString() テスト ==========

    @Nested
    @DisplayName("toString: 文字列表現")
    class ToStringTests {

        @Test
        @DisplayName("test: toStringはクラスIDを含む")
        void testToStringContainsClassId() {
            String result = tree.toString();

            assertTrue(result.contains("warrior"));
        }

        @Test
        @DisplayName("test: toStringはノード数を含む")
        void testToStringContainsNodeCount() {
            SkillNode node = new SkillNode(mockSkill, null);
            tree.addNode(node);

            String result = tree.toString();

            assertTrue(result.contains("nodeCount=1"));
        }

        @Test
        @DisplayName("test: toStringはルート数を含む")
        void testToStringContainsRootCount() {
            SkillNode node = new SkillNode(mockSkill, null);
            tree.addNode(node);

            String result = tree.toString();

            assertTrue(result.contains("rootCount=1"));
        }
    }

    // ========== 複雑なツリー構造のテスト ==========

    @Nested
    @DisplayName("複雑なツリー構造")
    class ComplexTreeTests {

        @Test
        @DisplayName("test: マルチブランチツリーの構築")
        void testMultiBranchTree() {
            //    root1    root2
            //     / \       |
            //    a   b      c
            //        |
            //        d

            SkillNode root1 = new SkillNode(mockSkill, null);
            SkillNode root2 = new SkillNode(mockSkill2, null);

            // 子ノード用に追加のモックスキルを作成
            Skill skillA = mock(Skill.class);
            when(skillA.getId()).thenReturn("skillA");
            Skill skillB = mock(Skill.class);
            when(skillB.getId()).thenReturn("skillB");
            Skill skillC = mock(Skill.class);
            when(skillC.getId()).thenReturn("skillC");
            Skill skillD = mock(Skill.class);
            when(skillD.getId()).thenReturn("skillD");

            SkillNode nodeA = new SkillNode(skillA, root1);
            SkillNode nodeB = new SkillNode(skillB, root1);
            SkillNode nodeC = new SkillNode(skillC, root2);
            SkillNode nodeD = new SkillNode(skillD, nodeB);

            tree.addNode(root1);
            tree.addNode(root2);
            tree.addNode(nodeA);
            tree.addNode(nodeB);
            tree.addNode(nodeC);
            tree.addNode(nodeD);

            // 全ノード数
            assertEquals(6, tree.getNodeCount());

            // ルートノード数
            assertEquals(2, tree.getRootNodes().size());

            // 最大深さ
            assertEquals(2, tree.getMaxDepth());

            // 深さ確認
            assertEquals(0, root1.getDepth());
            assertEquals(0, root2.getDepth());
            assertEquals(1, nodeA.getDepth());
            assertEquals(1, nodeB.getDepth());
            assertEquals(1, nodeC.getDepth());
            assertEquals(2, nodeD.getDepth());
        }
    }
}

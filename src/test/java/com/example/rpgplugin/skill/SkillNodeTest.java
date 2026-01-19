package com.example.rpgplugin.skill;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SkillNodeのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SkillNode: スキルツリーノード")
class SkillNodeTest {

    @Mock
    private Skill mockSkill;

    @Test
    @DisplayName("test: コンストラクタでルートノードを作成")
    void testConstructorWithRoot() {
        SkillNode node = new SkillNode(mockSkill, null);

        assertSame(mockSkill, node.getSkill());
        assertNull(node.getParent());
        assertTrue(node.isRoot());
        assertTrue(node.isLeaf());
        assertEquals(0, node.getChildren().size());
        assertEquals(0, node.getDepth());
    }

    @Test
    @DisplayName("test: コンストラクタで子ノードを作成")
    void testConstructorWithParent() {
        SkillNode parent = new SkillNode(mockSkill, null);
        SkillNode child = new SkillNode(mockSkill, parent);

        assertSame(mockSkill, child.getSkill());
        assertSame(parent, child.getParent());
        assertFalse(child.isRoot());
        assertEquals(1, child.getDepth());
    }

    @Nested
    @DisplayName("addChild: 子ノードの追加")
    class AddChildTests {

        @Test
        @DisplayName("test: 子ノードを追加")
        void testAddChild() {
            SkillNode parent = new SkillNode(mockSkill, null);
            SkillNode child = new SkillNode(mockSkill, parent);

            parent.addChild(child);

            assertEquals(1, parent.getChildren().size());
            assertTrue(parent.getChildren().contains(child));
            assertFalse(parent.isLeaf());
        }

        @Test
        @DisplayName("test: 複数の子ノードを追加")
        void testAddMultipleChildren() {
            SkillNode parent = new SkillNode(mockSkill, null);
            SkillNode child1 = new SkillNode(mockSkill, parent);
            SkillNode child2 = new SkillNode(mockSkill, parent);
            SkillNode child3 = new SkillNode(mockSkill, parent);

            parent.addChild(child1);
            parent.addChild(child2);
            parent.addChild(child3);

            assertEquals(3, parent.getChildren().size());
            assertTrue(parent.getChildren().contains(child1));
            assertTrue(parent.getChildren().contains(child2));
            assertTrue(parent.getChildren().contains(child3));
        }

        @Test
        @DisplayName("test: 同じ子ノードを追加しても重複しない")
        void testAddChildDuplicate() {
            SkillNode parent = new SkillNode(mockSkill, null);
            SkillNode child = new SkillNode(mockSkill, parent);

            parent.addChild(child);
            parent.addChild(child); // 同じ子を再度追加

            assertEquals(1, parent.getChildren().size());
        }

        @Test
        @DisplayName("test: getChildrenはコピーを返す")
        void testGetChildrenReturnsCopy() {
            SkillNode parent = new SkillNode(mockSkill, null);
            SkillNode child = new SkillNode(mockSkill, parent);

            parent.addChild(child);
            List<SkillNode> children1 = parent.getChildren();
            List<SkillNode> children2 = parent.getChildren();

            // 別のインスタンスであることを確認
            assertNotSame(children1, children2);
            assertEquals(children1, children2);
        }
    }

    @Nested
    @DisplayName("isRoot/isLeaf: ノード種別の判定")
    class NodeKindTests {

        @Test
        @DisplayName("test: ルートノードはisRootがtrue")
        void testIsRootTrue() {
            SkillNode node = new SkillNode(mockSkill, null);
            assertTrue(node.isRoot());
        }

        @Test
        @DisplayName("test: 子ノードはisRootがfalse")
        void testIsRootFalse() {
            SkillNode parent = new SkillNode(mockSkill, null);
            SkillNode child = new SkillNode(mockSkill, parent);
            assertFalse(child.isRoot());
        }

        @Test
        @DisplayName("test: 子がないノードはisLeafがtrue")
        void testIsLeafTrue() {
            SkillNode node = new SkillNode(mockSkill, null);
            assertTrue(node.isLeaf());
        }

        @Test
        @DisplayName("test: 子があるノードはisLeafがfalse")
        void testIsLeafFalse() {
            SkillNode parent = new SkillNode(mockSkill, null);
            SkillNode child = new SkillNode(mockSkill, parent);
            parent.addChild(child);
            assertFalse(parent.isLeaf());
        }
    }

    @Nested
    @DisplayName("getDepth: 深さの取得")
    class DepthTests {

        @Test
        @DisplayName("test: ルートノードの深さは0")
        void testRootDepth() {
            SkillNode node = new SkillNode(mockSkill, null);
            assertEquals(0, node.getDepth());
        }

        @Test
        @DisplayName("test: 直接の子ノードの深さは1")
        void testChildDepth() {
            SkillNode parent = new SkillNode(mockSkill, null);
            SkillNode child = new SkillNode(mockSkill, parent);
            assertEquals(1, child.getDepth());
        }

        @Test
        @DisplayName("test: 孫ノードの深さは2")
        void testGrandchildDepth() {
            SkillNode root = new SkillNode(mockSkill, null);
            SkillNode parent = new SkillNode(mockSkill, root);
            SkillNode child = new SkillNode(mockSkill, parent);

            assertEquals(0, root.getDepth());
            assertEquals(1, parent.getDepth());
            assertEquals(2, child.getDepth());
        }
    }

    @Nested
    @DisplayName("toString: 文字列表現")
    class ToStringTests {

        @Test
        @DisplayName("test: toStringはスキルIDと子ノード数を含む")
        void testToString() {
            when(mockSkill.getId()).thenReturn("testSkill");

            SkillNode node = new SkillNode(mockSkill, null);
            String result = node.toString();

            assertTrue(result.contains("testSkill"));
            assertTrue(result.contains("children=0"));
        }

        @Test
        @DisplayName("test: 子ノードがある場合のtoString")
        void testToStringWithChildren() {
            when(mockSkill.getId()).thenReturn("parentSkill");

            SkillNode parent = new SkillNode(mockSkill, null);
            SkillNode child = new SkillNode(mockSkill, parent);
            parent.addChild(child);

            String result = parent.toString();
            assertTrue(result.contains("children=1"));
        }
    }

    @Nested
    @DisplayName("ツリー構造の構築")
    class TreeStructureTests {

        @Test
        @DisplayName("test: 複雑なツリー構造を構築")
        void testComplexTree() {
            //       root
            //      / | \
            //     a  b  c
            //        /\
            //       d  e

            SkillNode root = new SkillNode(mockSkill, null);
            SkillNode nodeA = new SkillNode(mockSkill, root);
            SkillNode nodeB = new SkillNode(mockSkill, root);
            SkillNode nodeC = new SkillNode(mockSkill, root);
            SkillNode nodeD = new SkillNode(mockSkill, nodeB);
            SkillNode nodeE = new SkillNode(mockSkill, nodeB);

            root.addChild(nodeA);
            root.addChild(nodeB);
            root.addChild(nodeC);
            nodeB.addChild(nodeD);
            nodeB.addChild(nodeE);

            // 深さを確認
            assertEquals(0, root.getDepth());
            assertEquals(1, nodeA.getDepth());
            assertEquals(1, nodeB.getDepth());
            assertEquals(1, nodeC.getDepth());
            assertEquals(2, nodeD.getDepth());
            assertEquals(2, nodeE.getDepth());

            // ルート/リーフを確認
            assertTrue(root.isRoot());
            assertFalse(nodeA.isRoot());
            assertTrue(nodeA.isLeaf());
            assertTrue(nodeC.isLeaf());
            assertTrue(nodeD.isLeaf());
            assertTrue(nodeE.isLeaf());
            assertFalse(nodeB.isLeaf());

            // 子ノード数を確認
            assertEquals(3, root.getChildren().size());
            assertEquals(2, nodeB.getChildren().size());
            assertEquals(0, nodeA.getChildren().size());
        }
    }
}

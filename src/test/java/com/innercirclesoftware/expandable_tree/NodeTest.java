package com.innercirclesoftware.expandable_tree;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NodeTest {

    /**
     * Size: 11
     * <p>
     * <p>child0
     * <p>--child0_0
     * <p>--child0_1
     * <p>--child0_2
     * <p>--child0_3
     * <p>--child0_4
     * <p>----child0_4_0
     * <p>----child0_4_1
     * <p>--child0_5
     * <p>child1
     * <p>child2
     */
    private Node<String> root;
    /**
     * Child 0
     */
    private Node<String> child0;
    /**
     * Child 1
     */
    private Node<String> child0_0;
    /**
     * Child 2
     */
    private Node<String> child0_1;
    /**
     * Child 3
     */
    private Node<String> child0_2;
    /**
     * Child 4
     */
    private Node<String> child0_3;
    /**
     * Child 5
     */
    private Node<String> child0_4;
    /**
     * Child 6
     */
    private Node<String> child0_4_0;
    /**
     * Child 7
     */
    private Node<String> child0_4_1;
    /**
     * Child 8
     */
    private Node<String> child0_5;
    /**
     * Child 9
     */
    private Node<String> child1;
    /**
     * Child 10
     */
    private Node<String> child2;

    @Before
    public void setUp() {
        root = new Node<>();
        child0 = new Node<>("child0");
        child0_0 = new Node<>("child0_0");
        child0_1 = new Node<>("child0_1");
        child0_2 = new Node<>("child0_2");
        child0_3 = new Node<>("child0_3");
        child0_4 = new Node<>("child0_4");
        child0_4_0 = new Node<>("child0_4_0");
        child0_4_1 = new Node<>("child0_4_1");
        child0_5 = new Node<>("child0_5");
        child1 = new Node<>("child1");
        child2 = new Node<>("child2");

        child0.add(child0_0);
        child0.add(child0_1);
        child0.add(child0_2);
        child0.add(child0_3);
        child0.add(child0_4);
        child0.add(child0_5);
        child0_4.add(child0_4_0);
        child0_4.add(child0_4_1);
        root.add(child0);
        root.add(child1);
        root.add(child2);
    }

    @Test
    public void getParent() {
        assertNull(root.getParent());

        assertEquals(root, child0.getParent());
        assertEquals(root, child1.getParent());
        assertEquals(root, child2.getParent());

        assertEquals(child0, child0_0.getParent());
        assertEquals(child0, child0_1.getParent());
        assertEquals(child0, child0_2.getParent());
        assertEquals(child0, child0_3.getParent());
        assertEquals(child0, child0_4.getParent());
        assertEquals(child0, child0_5.getParent());

        assertEquals(child0_4, child0_4_0.getParent());
        assertEquals(child0_4, child0_4_1.getParent());

        Node<String> parent = new Node<>("parent");
        parent.add(root);
        assertEquals(parent, child0.getParent());
        assertEquals(parent, child1.getParent());
        assertEquals(parent, child2.getParent());
    }

    @Test
    public void add() {
        Node<String> child3 = new Node<>("child3");
        root.add(child3); //add to the end
        assertEquals(child3, root.get(11));

        Node<String> child4 = new Node<>("child4");
        root.add(child4, 3);
        assertEquals(child4, root.get(11));
        assertEquals(child3, root.get(12));


        Node<String> child5 = new Node<>("child5");
        Node<String> child6 = new Node<>("child6");
        Node<String> child7 = new Node<>("child7");
        Node<String> nodeToMerge = new Node<>();
        nodeToMerge.add(child5);
        nodeToMerge.add(child6);
        nodeToMerge.add(child7);

        root.add(nodeToMerge);
        assertEquals(root, child5.getParent());
        assertEquals(root, child6.getParent());
        assertEquals(root, child7.getParent());
        assertEquals(child5, root.get(13));
        assertEquals(child6, root.get(14));
        assertEquals(child7, root.get(15));
    }

    @Test
    public void remove() {
        assertEquals(0, root.remove(child0));
        assertEquals(2, root.size());
        assertNull(child0.getParent());
        assertEquals(child1, root.get(0));
        assertEquals(child2, root.get(1));
    }

    @Test
    public void getData() {
        //getData() never returns null, unless it's the root node
        try {
            root.getData();
            //should have thrown NPE
            fail();
        } catch (NullPointerException ignored) {
        }
        assertEquals("child0", child0.getData());
        assertEquals("child0_0", child0_0.getData());
    }

    @Test
    public void get() {
        assertEquals(child0, root.get(0));
        assertEquals(child0_0, root.get(1));
        assertEquals(child0_1, root.get(2));
        assertEquals(child0_2, root.get(3));
        assertEquals(child0_3, root.get(4));
        assertEquals(child0_4, root.get(5));
        assertEquals(child0_4_0, root.get(6));
        assertEquals(child0_4_1, root.get(7));
        assertEquals(child0_5, root.get(8));
        assertEquals(child1, root.get(9));
        assertEquals(child2, root.get(10));

        child0_4.setExpanded(false);
        assertEquals(child0, root.get(0));
        assertEquals(child0_0, root.get(1));
        assertEquals(child0_1, root.get(2));
        assertEquals(child0_2, root.get(3));
        assertEquals(child0_3, root.get(4));
        assertEquals(child0_4, root.get(5));
        assertEquals(child0_5, root.get(6));
        assertEquals(child1, root.get(7));
        assertEquals(child2, root.get(8));

        child0_4.setExpanded(true);
        child0_4_0.setExpanded(false);
        assertEquals(child0_4_0, root.get(6));
    }

    @Test
    public void size() {
        assertEquals(11, root.size());

        child0_4_0.setExpanded(false);
        assertEquals(11, root.size());

        child0_4_1.setExpanded(false);
        assertEquals(11, root.size());

        child0_5.setExpanded(false);
        assertEquals(11, root.size());

        child0_4.setExpanded(false);
        assertEquals(9, root.size());

        child0_4_1.setExpanded(true);
        assertEquals(9, root.size());

        child0_4_0.setExpanded(true);
        child0_4_1.setExpanded(true);
        child0_4.setExpanded(true);
        child0_5.setExpanded(true);
        assertEquals(11, root.size());
        child0_4.setExpanded(false);
        assertEquals(9, root.size());
        child0_4.add("1");
        assertEquals(9, root.size());


        root.setExpanded(false);
        assertEquals("Not expanded root with children: \n" + root.toString(), 0, root.size());

        //no children
        root.setExpanded(true);
        root.clear();
        assertEquals("Empty root: \n" + root.toString(), 0, root.size());

        child0.clear();
        root.add(child0);
        assertEquals("Root with 1 direct child: \n" + root.toString(), 1, root.size());

        child0_0.clear();
        child0.add(child0_0);
        assertEquals("Root with 1 direct child, holding 1 other child: \n" + root.toString(), 2, root.size());
    }

    @Test
    public void isExpanded() {
        assertTrue(root.isExpanded());
        assertTrue(child0.isExpanded());
        assertTrue(child0_1.isExpanded());
        assertTrue(child0_2.isExpanded());
        assertTrue(child0_3.isExpanded());
        assertTrue(child0_4.isExpanded());
        assertTrue(child0_4_0.isExpanded());
        assertTrue(child0_4_1.isExpanded());
        assertTrue(child0_5.isExpanded());
        assertTrue(child1.isExpanded());
        assertTrue(child2.isExpanded());

        child0_4_0.setExpanded(false);
        assertFalse(child0_4_0.isExpanded());
        child0_4_0.setExpanded(true);
        assertTrue(child0_4_0.isExpanded());

        child0_4.setExpanded(false);
        assertFalse(child0_4_0.isExpanded());
        assertFalse(child0_4_1.isExpanded());
        assertFalse(child0_4.isExpanded());
    }

    @Test
    public void setExpanded() {
        assertEquals(root.isExpanded(), root.setExpanded(root.isExpanded()));
        assertNotEquals(root.isExpanded(), root.setExpanded(!root.isExpanded()));
        assertTrue(root.setExpanded(true));
        assertTrue(root.isExpanded());
        assertFalse(root.setExpanded(false));
        assertFalse(root.isExpanded());
    }

    @Test
    public void toggleExpanded() {
        boolean wasExpanded = root.isExpanded();
        assertNotEquals(wasExpanded, root.toggleExpanded());
        assertNotEquals(wasExpanded, root.isExpanded());
    }

    @Test
    public void clear() {
        root.clear();
        assertEquals(0, root.size());
        assertNull(child0.getParent());
        assertNull(child1.getParent());
        assertNull(child2.getParent());
    }

    @Test
    public void childCount() {
        assertEquals(11, root.childCount());

        child0_4.setExpanded(false);
        assertEquals(2, child0_4.childCount());

        assertEquals(0, child2.childCount());

        root.clear();
        assertEquals(0, root.childCount());
    }

    @Test
    public void replaceWith() {
        Node<String> newChild2 = new Node<>("newChild2");
        assertTrue(child2.replaceWith(newChild2));
        assertNull(child2.getParent());
        assertEquals(root, newChild2.getParent());
        assertEquals(newChild2, root.get(10));

        Node<String> newChild0_4 = new Node<>("newChild0_4");
        assertTrue(child0_4.replaceWith(newChild0_4));
        assertEquals(9, root.size());

        assertFalse(root.replaceWith(newChild0_4));
    }

    @Test
    public void iterator() {
        Iterator<Node<String>> rootIterator = root.iterator();

        assertTrue(rootIterator.hasNext());
        assertEquals(child0, rootIterator.next());

        assertTrue(rootIterator.hasNext());
        assertEquals(child0_0, rootIterator.next());

        assertTrue(rootIterator.hasNext());
        assertEquals(child0_1, rootIterator.next());

        assertTrue(rootIterator.hasNext());
        assertEquals(child0_2, rootIterator.next());

        assertTrue(rootIterator.hasNext());
        assertEquals(child0_3, rootIterator.next());

        assertTrue(rootIterator.hasNext());
        assertEquals(child0_4, rootIterator.next());

        assertTrue(rootIterator.hasNext());
        assertEquals(child0_4_0, rootIterator.next());

        assertTrue(rootIterator.hasNext());
        assertEquals(child0_4_1, rootIterator.next());

        assertTrue(rootIterator.hasNext());
        assertEquals(child0_5, rootIterator.next());

        assertTrue(rootIterator.hasNext());
        assertEquals(child1, rootIterator.next());

        assertTrue(rootIterator.hasNext());
        assertEquals(child2, rootIterator.next());

        assertFalse(rootIterator.hasNext());

        Iterator<Node<String>> child0_4_iterator = child0_4.iterator();
        assertTrue(child0_4_iterator.hasNext());
        assertEquals(child0_4, child0_4_iterator.next());

        assertTrue(child0_4_iterator.hasNext());
        assertEquals(child0_4_0, child0_4_iterator.next());

        assertTrue(child0_4_iterator.hasNext());
        assertEquals(child0_4_1, child0_4_iterator.next());

        assertFalse(child0_4_iterator.hasNext());

        rootIterator = root.iterator();
        root.setExpanded(false);
        assertFalse(rootIterator.hasNext());
    }

    @Test
    public void hasExpandableChildren() {
        assertFalse(root.hasExpandableChildren());
        child0.setExpanded(false);
        assertTrue(root.hasExpandableChildren());
        child0.setExpanded(true);
        assertFalse(root.hasExpandableChildren());
        child1.setExpanded(false);
        assertFalse(root.hasExpandableChildren());
    }

    @Test
    public void hasCollapsibleChildren() {
        assertTrue(root.hasCollapsibleChildren());
        child0.setExpanded(false);
        assertFalse(root.hasCollapsibleChildren());
    }

    @Test
    public void directChildCount() {
        assertEquals(3, root.directChildCount());
    }

    @Test
    public void getDirectChild() {
        assertEquals(child0, root.getDirectChild(0));
        assertEquals(child1, root.getDirectChild(1));
        assertEquals(child2, root.getDirectChild(2));
    }

    @Test
    public void test_getWithInvalidPosition_throwsExceptionWithSamePosition() {
        test_throwsExceptionWithGivenPosition(11);
        test_throwsExceptionWithGivenPosition(12);
        test_throwsExceptionWithGivenPosition(-1);
        test_throwsExceptionWithGivenPosition(100);
    }

    private void test_throwsExceptionWithGivenPosition(int position) {
        try {
            root.get(position);
            fail();
        } catch (Node.NodeIndexOutOfBoundsException exception) {
            assertEquals(position, exception.getPosition());
            return;
        }

        fail();
    }

    @Test
    public void test_ConstructorWithListOfChildren() {
        List<Node<? extends String>> childrenNodes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Node<String> child = new Node<>("child " + i);
            childrenNodes.add(child);
        }

        Node<String> rootWithChildren = new Node<>(childrenNodes);
        assertEquals(childrenNodes, rootWithChildren.getChildren());
        for (Node<? extends String> child : childrenNodes) {
            assertEquals(rootWithChildren, child.getParent());
        }
    }

    @Test
    public void test_ConstructorWrappingOtherNode() {
        Node<String> toWrap = new Node<>("we want to wrap this in a root");
        assertFalse(toWrap.isRoot());

        Node<String> wrapped = new Node<>(toWrap);
        assertTrue(wrapped.isRoot());
    }

    @Test
    public void test_addingChildWhichIsARoot_insertsTheChildrenInsteadOfTheRoot() {
        Node<String> root = new Node<>();

        Node<String> rootToAddAsChild = new Node<>();
        rootToAddAsChild.add("1");
        rootToAddAsChild.add("2");

        root.add(rootToAddAsChild);

        assertEquals(2, root.directChildCount());
        assertEquals("1", root.getDirectChild(0).getData());
        assertEquals("2", root.getDirectChild(1).getData());
    }

    @Test
    public void testNotEqualsComparingToNull() {
        assertNotEquals(new Node<String>(), null);
        assertNotEquals(new Node<>(""), null);
    }

    @Test
    public void testNotEqualForDataOfDifferentClasses() {
    }

    @Test
    public void testNotEqualsForDifferentData() {
        assertNotEquals(new Node<>(""), new Node<>(0));
        assertNotEquals(new Node<>("first"), new Node<>("second"));
    }

    @Test
    public void testNotEqualsWhenOneMissingRoot() {
        Node inRoot = new Node<>(new Node<>(""));
        Node notInRoot = new Node<>("");
        assertNotEquals(inRoot, notInRoot);
    }

    @Test
    public void testEqualsByReference() {
        Node<String> equalsByReference = new Node<>("data");
        assertEquals(equalsByReference, equalsByReference);
    }

    @Test
    public void testEqualsSimple() {
        assertEquals(new Node<>("data"), new Node<>("data"));
        assertEquals(new Node<>(new Node<>("child")), new Node<>(new Node<>("child")));
    }

    @Test
    public void testEqualsComplex() {
        Node<String> complex = createComplexNode();
        Node<String> complexCopy = createComplexNode();
        assertNotSame(complex, complexCopy); //make sure createComplex() didn't return the same objects

        assertEquals(complex, complexCopy);
    }

    private Node<String> createComplexNode() {
        Node<String> equalsComplex = new Node<String>();
        equalsComplex.add("child 1").add("child 1.1").add("child 1.1.1");
        equalsComplex.add("child 2");
        return equalsComplex;
    }
}
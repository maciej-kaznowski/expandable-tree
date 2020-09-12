package com.innercirclesoftware.expandable_tree

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NodesKtTest {

    @Test
    fun `map on root node should produce another root node`() {
        val root = Node<Any>()
        val mapped = root.map { "My Data" }

        assertTrue(mapped.isRoot)
    }

    @Test
    fun `map on node with data maps data`() {
        val nodeWithData = Node("Original")
        val mapped = nodeWithData.map { string -> string.repeat(2) }
        assertEquals("OriginalOriginal", mapped.data)
    }

    @Test
    fun `mapping node with data to different type`() {
        val nodeWithData = Node("Original")
        val mapped = nodeWithData.map { 1 }
        assertEquals(1, mapped.data)
    }

    @Test
    fun `mapping node with children correctly maps data`() {
        val children = (0 until 100).map { Node(it) }
        val root = Node(children)
        val mapped = root.map { int -> "Int $int" }
        assertTrue(mapped.isRoot)
        assertFalse(mapped.hasParent())
        mapped.children.forEachIndexed { index, child ->
            assertFalse(child.isRoot)
            assertEquals(mapped, child.parent)
            assertEquals("Int $index", child.data)
        }
    }

    @Test
    fun `wrapIfRoot shouldn't wrap if it is a root`() {
        val root = Node<Any>()
        assertTrue(root.isRoot)
        assertEquals(root, root.wrapIfNotRoot())
    }

    @Test
    fun `wrapIfRoot should wrap if it isn't a root `() {
        val notRoot = Node("")
        assertFalse(notRoot.isRoot)

        val wrappedInRoot = notRoot.wrapIfNotRoot()
        assertTrue(wrappedInRoot.isRoot)
        assertNotEquals(wrappedInRoot, notRoot)
        assertEquals(wrappedInRoot, notRoot.parent)
    }

    @Test
    fun `flatten() for just root tree returns empty list`() {
        assertEquals(emptyList<String>(), Node<String>().flatten())
    }

    @Test
    fun `flatten() for complex tree returns correct depths`() {
        val node = Node<String>().apply {
            add("child 1").also { child1 ->
                child1.add("child 1.1").also { child1_1 ->
                    child1_1.add("child 1.1.1")
                }
                child1.add("child 1.2")
                child1.add("child 1.3").also { child1_3 ->
                    child1_3.add("child 1.3.1")
                    child1_3.add("child 1.3.2")
                }
            }
            add("child 2")
        }

        val actual: List<FlattenedNode<out String>> = node.flatten()
        val expected: List<FlattenedNode<out String>> = listOf(
                "child 1" to 0,
                "child 1.1" to 1,
                "child 1.1.1" to 2,
                "child 1.2" to 1,
                "child 1.3" to 1,
                "child 1.3.1" to 2,
                "child 1.3.2" to 2,
                "child 2" to 0
        ).asDepthList()

        assertEquals(expected, actual)
    }
}
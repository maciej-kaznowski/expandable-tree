package com.innercirclesoftware.expandable_tree

fun <From, To> Node<From>.map(mapper: (From) -> To): Node<To> {
    val mappedData: To? = if (isRoot.not()) mapper(this.data) else null
    val mappedNode: Node<To> = mappedData?.let { Node(it) } ?: Node()
    val mappedChildren: List<Node<To>> = this.children.map { node -> node.map(mapper) }
    mappedNode.add(Node(mappedChildren))
    return mappedNode
}

fun <What> Node<What>.wrapIfNotRoot(): Node<What> {
    if (isRoot) return this

    return Node(this)
}

operator fun <What> Node<What>.plusAssign(child: Node<What>) {
    this.add(child)
}

fun <What> Node<What>.depth(): Int {
    val parent = this.parent
    return when {
        isRoot -> 0
        parent != null && parent.isRoot.not() -> parent.depth() + 1
        parent != null && parent.isRoot -> 0
        else -> 0
    }
}

fun <What> Node<What>.flatten(): List<FlattenedNode<out What>> {
    if (this.isRoot) {
        //no data  so not interested in it concat map the children
        return children.map { it.flatten() }.flatten()
    }

    val element = FlattenedNode(data, depth())

    if (this.isExpanded.not()) {
        return listOf(element)
    }

    return listOf(element) + this.children.map { child -> child.flatten() }.flatten()
}

data class FlattenedNode<What>(val item: What, val depth: Int)

fun <What> List<Pair<What, Int>>.asDepthList(): List<FlattenedNode<What>> {
    return map { (what, depth) -> FlattenedNode(what, depth) }
}

fun <T> Node<T>.isLeaf(): Boolean {
    return !isRoot && children.isEmpty()
}

fun <T> Node<out T>.getParents(): Sequence<Node<out Any>> {
    var lastParent = parent
    return generateSequence {
        return@generateSequence lastParent?.also {
            lastParent = it.parent
        }
    }
}

fun <T : Any> Node<T>.add(child: T, childConsumer: Node<T>.() -> Unit) {
    add(Node(child).apply(childConsumer))
}
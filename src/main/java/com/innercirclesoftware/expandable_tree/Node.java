package com.innercirclesoftware.expandable_tree;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class Node<T> implements Iterable<Node<T>> {

    //TODO we can probably remove this entirely -> when adding/removing we can determine the size by taking into account if the Node was/is a root
    //also have to account for expanded/collapsed
    private static final int SIZE_INVALIDATED = -1;

    @NotNull
    private final List<Node<? extends T>> children;
    @Nullable
    private Node<? super T> parent = null; //will be null when it's the top most node
    @Nullable
    private T data; //will be null when it's the top most node
    private boolean expanded = true;
    private int size;

    public Node() {
        this.data = null;
        this.size = 0;
        this.children = new ArrayList<>();
    }

    public Node(@NotNull List<Node<? extends T>> children) {
        //make sure all the children aren't part of another tree
        for (Node<? extends T> child : children) {
            if (child.hasParent()) {
                String msg = String.format("Cannot attach child %s as it is already attached to %s", child, child.getParent());
                throw new IllegalArgumentException(msg);
            }

            if (child.isRoot()) {
                //we can't wrap a root with another root
                String msg = String.format("Cannot wrap a root in another root. child=%s", child);
                throw new IllegalArgumentException(msg);
            }
        }

        this.data = null;
        this.size = SIZE_INVALIDATED;
        this.children = new ArrayList<>(children);
        for (Node<? extends T> child : this.children) {
            child.parent = this;
            child.size = SIZE_INVALIDATED;
        }
    }

    @SuppressWarnings("CopyConstructorMissesField") //we're not copying - we're wrapping the child in another Node
    public Node(@NotNull Node<? extends T> child) {
        if (child.hasParent()) {
            String msg = String.format("Cannot attach child %s as it is already attached to %s", child, child.getParent());
            throw new IllegalArgumentException(msg);
        }

        if (child.isRoot()) {
            //we can't wrap a root with another root
            //well, technically we could if we essentially copied it
            String msg = String.format("Cannot wrap a root in another root. child=%s", child);
            throw new IllegalArgumentException(msg);
        }

        this.data = null;
        this.size = SIZE_INVALIDATED;
        this.children = new ArrayList<>();
        this.children.add(child);
        child.parent = this;
        child.size = SIZE_INVALIDATED;
    }

    public Node(@NotNull T data) {
        this.data = requireNonNull(data);
        this.size = SIZE_INVALIDATED;
        this.children = new ArrayList<>();
    }

    //used to avoid recursion in equals where we take parents children and then compare the children's parents
    private static boolean equalsExcludeChildren(@Nullable Node a, @Nullable Node<?> b) {
        if (a == b) return true;
        if (a == null || b == null) return false;

        return a.expanded == b.expanded && Objects.equals(a.data, b.data) && equalsExcludeChildren(a.parent, b.parent);
    }

    private static int hashExcludingChildren(@Nullable Node node) {
        if (node == null) return 0;

        return Objects.hash(node.expanded, node.data, hashExcludingChildren(node.parent));
    }

    @Nullable
    public Node<? super T> getParent() {
        return this.parent;
    }

    /**
     * Add to the end of the children data as a node and return the created node.
     *
     * @param data the non-null data which we will create a child from
     * @return the created child, added as a child to this node
     */
    @NotNull
    public Node<T> add(@NotNull T data) {
        Node<T> toAdd = new Node<>(requireNonNull(data));
        this.add(toAdd);
        return toAdd;
    }

    public void add(@NotNull Node<? extends T> child) {
        add(requireNonNull(child), children.size()); //add to the end of the list
    }

    /**
     * Adding a node will remove the reference to the current parent: so if you do someList.add(node) then node;s parent will now be someList
     */
    public void add(@NotNull Node<? extends T> node, int index) {
        requireNonNull(node);

        if (node.isRoot()) {
            for (int i = 0; i < node.children.size(); i++) {
                Node<? extends T> child = node.children.get(i);
                child.parent = this;
                child.size = SIZE_INVALIDATED;
                this.children.add(index + i, child);
            }
        } else {
            node.parent = this;
            node.size = SIZE_INVALIDATED;
            this.children.add(index, node);
        }
        invalidateSize();
    }

    /**
     * @return true if the node doesn't hold any data
     */
    public boolean isRoot() {
        return data == null;
    }

    /**
     * @return true if this node has a parent where this node is a child of that parent
     */
    public boolean hasParent() {
        return parent != null;
    }

    private void remove(int index) {
        Node<? extends T> toRemove = children.get(index);
        toRemove.parent = null;
        toRemove.size = SIZE_INVALIDATED;
        children.remove(index);
        invalidateSize();
    }

    /**
     * @param child the root element that should be removed
     * @return the index of the removed root
     */
    public int remove(@NotNull Node<? extends T> child) {
        int index = children.indexOf(requireNonNull(child));
        remove(index);
        return index;
    }

    @NotNull
    public T getData() {
        if (isRoot()) {
            String msg = String.format("Root Node %s does not have data", this.toString());
            throw new NullPointerException(msg);
        }

        return requireNonNull(data);
    }

    public void setData(@NotNull T data) {
        this.data = requireNonNull(data);
    }

    @NotNull
    public Node<T> get(int position) {
        if (position < 0) {
            throw new NodeIndexOutOfBoundsException(this, position);
        }

        try {
            if (parent == null) {
                int currentProgress = 0;
                for (int i = 0; i < children.size(); i++) {
                    Node<? extends T> child = children.get(i);
                    int rootSize = child.size();
                    if (position < currentProgress + rootSize) {
                        //root has the given position, find it
                        return (Node<T>) child.get(position - currentProgress);
                    }
                    currentProgress += rootSize;
                }
            } else {
                if (position == 0) return this;
                int currentPos = 1;
                for (int i = 0; i < children.size(); i++) {
                    Node<? extends T> child = children.get(i);
                    if (position < currentPos + child.size()) {
                        //specified child has it
                        return (Node<T>) child.get(position - currentPos);
                    }
                    currentPos += child.size();
                }
            }
        } catch (NodeIndexOutOfBoundsException cause) {
            NodeIndexOutOfBoundsException exception = new NodeIndexOutOfBoundsException(this, position);
            exception.initCause(cause);
            throw exception;
        }

        if (position >= size()) {
            throw new NodeIndexOutOfBoundsException(this, position);
        }

        //should never get to this point
        throw new RuntimeException("Error finding node for position " + position + ", size is " + size());
    }

    public int size() {
        if (size != SIZE_INVALIDATED) return size;

        if (expanded) {
            //expanded
            if (isExpanded()) {
                size = parent == null ? 0 : 1;
                for (Node<? extends T> child : children) size += child.size();
                return size;
            } else {
                return size = 0;
            }
        } else {
            //not expanded
            if (parent == null) return size = 0; //root node, not expanded
            if (parent.isExpanded()) return size = 1; //child node with expanded parents
            else return size = 0;//child with non-expanded parent
        }
    }

    public boolean isExpanded() {
        if (!expanded) return false; //if it's set to not expanded then immediately return false
        if (parent == null) return true; //root node
        return parent.isExpanded(); //check if parent is expanded
    }

    public boolean setExpanded(boolean expanded) {
        if (this.expanded == expanded) return expanded;
        this.expanded = expanded;
        invalidateSize();
        return expanded;
    }

    /**
     * @return Toggle expanded. Returns true if expanded, false if not expanded
     */
    public boolean toggleExpanded() {
        return setExpanded(!isExpanded());
    }

    private void invalidateSize() {
        this.size = SIZE_INVALIDATED;
        if (this.parent != null) this.parent.invalidateSize();
    }

    public void clear() {
        invalidateSize();
        for (int i = 0; i < children.size(); i++) {
            Node<? extends T> node = children.get(i);
            node.parent = null;
            node.invalidateSize();
        }
        children.clear();
    }

    public int childCount() {
        int count = 0;
        for (Node<? extends T> child : children) count += 1 + child.childCount();
        return count;
    }

    /**
     * Replace this nodes place in the tree with the given node. Only possible when
     * this instance has a parent. If it doesn't have a parent, then it's a root node (could be detached) and then the given node should be used instead
     *
     * @param node The node which should take this nodes place in the tree with
     * @return True if the node we replaced with is part of the tree, false otherwise when
     */
    public boolean replaceWith(@NotNull Node<T> node) {
        requireNonNull(node);

        Node<? super T> parent = getParent();
        if (parent == null) return false; //if this node doesn't have a parent, then we can't replace it
        int index = parent.indexOf(this);
        parent.remove(index);
        parent.add(node, index);
        return true;
    }

    private int indexOf(@NotNull Node<? extends T> item) {
        return children.indexOf(item);
    }

    @NotNull
    @Override
    public Iterator<Node<T>> iterator() {
        return new Iterator<Node<T>>() {

            int position = 0;

            @Override
            public boolean hasNext() {
                return position < size();
            }

            @Override
            public Node<T> next() {
                Node<T> node = get(position);
                position++;
                return node;
            }
        };
    }

    @Override
    @NotNull
    public String toString() {
        return toString(data -> {
            final String valueOf = String.valueOf(data);
            return valueOf.replaceAll("\n", "\\n");
        });
    }

    /**
     * A convenience method which allows the data to have a custom toString
     */
    @NotNull
    public String toString(@NotNull final Function<T, String> dataToString) {
        requireNonNull(dataToString);
        return toString(this, 0, dataToString);
    }

    @NotNull
    private String toString(@NotNull Node<? extends T> node, int level, @NotNull final Function<T, String> dataToString) {
        StringBuilder builder = new StringBuilder();
        if (level == 0) builder.append("\n");
        for (int i = 0; i < level; i++) builder.append("-");

        if (isExpanded()) {
            if (node.getParent() == null) {
                builder.append("ROOT_NODE");
            } else {
                final T data = node.getData();
                final String dataStr = dataToString.apply(data);
                builder.append(dataStr);
            }
        } else {
            builder.append("NOT_EXPANDED");
        }
        builder.append("\n");
        for (int i = 0; i < node.children.size(); i++) {
            Node<? extends T> child = node.children.get(i);
            builder.append(toString(child, level + 1, dataToString));
        }
        return builder.toString();
    }

    public boolean hasExpandableChildren() {
        for (int i = 0; i < children.size(); i++) {
            if (!children.get(i).expanded && children.get(i).childCount() != 0) return true;
        }
        return false;
    }

    public boolean hasCollapsibleChildren() {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).expanded && children.get(i).childCount() != 0) return true;
        }
        return false;
    }

    public int directChildCount() {
        return children.size();
    }

    @NotNull
    public Node<? extends T> getDirectChild(int i) {
        return children.get(i);
    }

    @NotNull
    public List<Node<? extends T>> getChildren() {
        return children;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node<?> node = (Node<?>) o;
        return expanded == node.expanded &&
                size() == node.size() && //size is cached -> calculate
                children.equals(node.children) &&
                Node.equalsExcludeChildren(parent, node.parent) &&
                Objects.equals(data, node.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(children, Node.hashExcludingChildren(parent), data, expanded, size);
    }

    public static class NodeIndexOutOfBoundsException extends IndexOutOfBoundsException {

        private final int position;
        @NotNull
        private final Node node;

        NodeIndexOutOfBoundsException(@NotNull Node node, int position) {
            super(String.format("Invalid index position=%s, size() = %s\n node=%s", position, node.size(), node.toString()));
            this.node = node;
            this.position = position;
        }

        public int getPosition() {
            return position;
        }

        @NotNull
        public Node getNode() {
            if (getCause() instanceof NodeIndexOutOfBoundsException) {
                return ((NodeIndexOutOfBoundsException) getCause()).getNode();
            }

            return node;
        }
    }
}
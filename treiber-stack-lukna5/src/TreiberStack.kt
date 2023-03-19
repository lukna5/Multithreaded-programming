package mpp.stack

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import java.util.EmptyStackException
class TreiberStack<E> {
    private val top = atomic<Node<E>?>(null)

    /**
     * Adds the specified element [x] to the stack.
     */
    fun push(x: E) {
        while (true){
            var head = top.value
            var head1 = Node(x, head)
            if (top.compareAndSet(head, head1)) {
                return
            }
        }
    }

    /**
     * Retrieves the first element from the stack
     * and returns it; returns `null` if the stack
     * is empty.
     */
    fun pop(): E? {
        if (top.value == null){
            return null
        }
        while (true) {
            var head = top.value
            if (head != null) {
                if (top.compareAndSet(head, head.next)) {
                    return head.x
                }
            }
            else return null
        }
    }
}

private class Node<E>(val x: E, val next: Node<E>?)

private const val ELIMINATION_ARRAY_SIZE = 2 // DO NOT CHANGE IT
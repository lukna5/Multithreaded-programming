import java.util.concurrent.atomic.*

class Solution(val env: Environment) : Lock<Solution.Node> {
    // todo: необходимые поля (val, используем AtomicReference)
    private val tail = AtomicReference<Node>(null)

    override fun lock(): Node {
        val my = Node() // сделали узел
        my.locked.set(true)
        val tail1 = tail.getAndSet(my) ?: return my
        tail1.run {
            next.value = my
            while (my.locked.value) {
                env.park()
            }

        }

        return my // вернули узел
    }

    override fun unlock(node: Node) {
        var nodeNext = node.next.value
        if (nodeNext == null) {
            if (tail.compareAndSet(node, null)){
                return
            }
            while (node.next.value == null);
        }
        nodeNext = node.next.value
        nodeNext?.run {
            locked.value = false
            env.unpark(thread)
        }
    }

    data class Node (
        val next: AtomicReference<Node?> = AtomicReference<Node?>(null),
        val locked: AtomicReference<Boolean> = AtomicReference(false),
        val thread: Thread = Thread.currentThread() // запоминаем поток, которые создал узел
    )
}
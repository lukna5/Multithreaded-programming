import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * An element is transferred from sender to receiver only when [send] and [receive]
 * invocations meet in time (rendezvous), so [send] suspends until another coroutine
 * invokes [receive] and [receive] suspends until another coroutine invokes [send].
 */
class SynchronousQueue<E> {
    /**
     * Sends the specified [element] to this channel, suspending if there is no waiting
     * [receive] invocation on this channel.
     */
    private val dummy = Node()
    private var head: AtomicReference<Node> = AtomicReference(dummy)
    private val tail: AtomicReference<Node> = AtomicReference(dummy)
    private fun ifBadRetro(retro: Int?) : Boolean{
        return retro != 777
    }
    suspend fun send(element: E) {
        while (true) {
            val newNode = Node(2, element)
            val h = head.get()
            val t = tail.get()
            if ((t == h || t.type.get() == 2)) {
                val courRes = suspendCoroutine<Any?> sc@{ cont ->
                    newNode.f.set(cont)
                    val RETRO = 777
                    if (t != null && !t.next.compareAndSet(null, newNode)) {
                        tail.compareAndSet(t, t.next.get())
                        cont.resume(RETRO)
                        return@sc
                    }
                    tail.compareAndSet(t, newNode)
                }
                if (courRes is Int? && ifBadRetro(courRes as Int?)){
                    return
                }
            } else {
                if (!alternativeSend(h, t, element)){
                    return
                }
            }
        }
    }

    private fun alternativeSend(h: Node, t: Node, x: E): Boolean{
        val alt = alternative(h, t, x)
        return alt is Boolean && alt
    }
    private fun alternativeReceive(h: Node, t: Node, x: E?): Any{
        return alternative(h, t, x)
    }
    private fun alternative(h: Node, t: Node, x: E?): Any{
        val next = h.next.get()
        if (t != this.tail.get() || h != this.head.get() || next == null) {
            return true
        }
        if (head.compareAndSet(h, next)) {
            if (x != null) {
                next.x.compareAndSet(null, x)
            }
            next.f.get()?.resume(null)
            return h.next.get()?.x?.get()!!
        }

        return true
    }
    /**
     * Retrieves and removes an element from this channel if there is a waiting [send] invocation on it,
     * suspends the caller if this channel is empty.
     */
    suspend fun receive(): E {
        while (true){
            val h = head.get()
            val t = tail.get()
            if (t == this.tail.get() && (t == h || t.type.get() == 1)) {
                val node = Node(null, 1)
                val courRes = suspendCoroutine<Any?> sc@ { cont ->
                    node.f.set(cont)
                    val RETRO = 777
                    if (t != null && !t.next.compareAndSet(null, node)) {
                        tail.compareAndSet(t, t.next.get())
                        cont.resume(RETRO)
                        return@sc
                    }
                    this.tail.compareAndSet(t, node)
                }
                if (courRes is Int? && ifBadRetro(courRes as Int?)){
                    return node.x.get()!!
                }
            } else {
                val alt = alternativeReceive(h, t , null)
                if (alt is Boolean && alt as Boolean){
                    continue
                }
                return alt as E
            }
        }
    }
    private inner class Node(
        type: Int = 3,
        x: E? = null
    ){
        constructor(x: E?, type: Int) : this(type, x)


        val f: AtomicReference<Continuation<Any?>?> = AtomicReference(null)
        //type == 1 -> Receiver, type == 2 -> Sender
        val type: AtomicReference<Int> = AtomicReference(type)
        val x: AtomicReference<E?> = AtomicReference(x)
        val next: AtomicReference<Node?> = AtomicReference(null)

    }
}

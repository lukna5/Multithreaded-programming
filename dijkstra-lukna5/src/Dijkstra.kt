package dijkstra

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.ReentrantLock
import java.util.*
import java.util.concurrent.Phaser
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.Lock
import kotlin.Comparator
import kotlin.concurrent.thread

private val NODE_DISTANCE_COMPARATOR = Comparator<Node> { o1, o2 -> Integer.compare(o1!!.distance, o2!!.distance) }

// Returns `Integer.MAX_VALUE` if a path has not been found.
fun shortestPathParallel(start: Node) {
    val workers = Runtime.getRuntime().availableProcessors()
    // The distance to the start node is `0`
    start.distance = 0
    // Create a priority (by distance) queue and add the start node into it
    val q = MyCoolMultiQueue(workers, NODE_DISTANCE_COMPARATOR) // TODO replace me with a multi-queue based PQ!
    q.add(start)
    val active = AtomicInteger()
    active.incrementAndGet()
    // Run worker threads and wait until the total work is done
    val onFinish = Phaser(workers + 1) // `arrive()` should be invoked at the end by each worker

    repeat(workers) {
        thread {
            while (true) {
                // TODO Write the required algorithm here,
                // TODO break from this loop when there is no more node to process.
                // TODO Be careful, "empty queue" != "all nodes are processed".
                val cur = q.poll()
                if (cur == null){
                    if (q.size.compareAndSet(0, 0)){
                        break
                    }
                    continue
                }
                for (e in cur.outgoingEdges) {
                    while (true){
                        val dist1 = e.to.distance
                        val dist2 = cur.distance + e.weight
                        if (dist1 > dist2) {
                            if (e.to.casDistance(dist1, dist2)) {
                                q.add(e.to)
                                active.incrementAndGet()
                                break
                            }
                        } else {
                            break
                        }
                    }
                }
                q.size.decrementAndGet()
            }
            onFinish.arrive()
        }
    }
    onFinish.arriveAndAwaitAdvance()
}

private class MyCoolMultiQueue<E>(val workers: Int, private var comparator: Comparator<E>) {
    val random = Random(0)
    val size = atomic(0)
    private val queue = Collections.nCopies(workers, PriorQueue(comparator))

    fun add(element: E) {
        val ind = random.nextInt(workers)
        synchronized(queue[ind].queue) {
            queue[ind].queue.add(element)
        }
        size.incrementAndGet()
    }

    fun poll(): E?{
        while (true) {
            val ind1 = random.nextInt(workers)
            val ind2 = random.nextInt(workers)
            val q1 = queue[ind1]
            val q2 = queue[ind2]
            synchronized (q1.queue) {
                synchronized (q2.queue) {
                    val peekQ1 = q1.queue.peek()
                    val peekQ2 = q2.queue.peek()
                    if (peekQ1 == null && peekQ2 == null) {
                        return null
                    }
                    if (peekQ1 != null && peekQ2 == null){
                        return q1.queue.poll()
                    }
                    if (peekQ1 == null){
                        return q2.queue.poll()
                    }
                    return when {
                        else -> if (comparator.compare(peekQ1, peekQ2) > 0) {
                            q2.queue.poll()
                        } else {
                            q1.queue.poll()
                        }
                    }

                }

            }
        }
    }


    fun unlock(ind: Int){
        queue[ind].locker.unlock()
    }
}
class PriorQueue<T>(comparator: Comparator<T>) {
    val locker: Lock = ReentrantLock()
    val queue: PriorityQueue<T> = PriorityQueue(comparator)
}
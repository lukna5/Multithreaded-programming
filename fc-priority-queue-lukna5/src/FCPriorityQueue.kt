import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.atomicArrayOfNulls
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class FCPriorityQueue<E : Comparable<E>> {
    private val q = PriorityQueue<E>()
    private val lock = atomic(false)
    private val threads = Runtime.getRuntime().availableProcessors()
    private val size = threads
    private val threadsCount = atomic(0)
    private val operations = atomicArrayOfNulls<Any?>(threads)
    /**
     * Retrieves the element with the highest priority
     * and returns it as the result of this function;
     * returns `null` if the queue is empty.
     */
    fun poll(): E? {
        return lockOperation(2, null)
    }

    /**
     * Returns the element with the highest priority
     * or `null` if the queue is empty.
     */
    fun peek(): E? {
        return lockOperation(1, null)
    }

    /**
     * Adds the specified element to the queue.
     */
    fun add(element: E) {
        lockOperation(3, element)
    }
    class Operat<E>(val op: () -> E?) {
        fun calc(): Res<E> {
            return Res(op())
        }
    }

    class Res<E>(val res: E?)

    // type 1 -> peek, 2 -> poll, 3 -> add
    fun lockOperation(type: Int, element: E?): E? {
        var oper: Operat<E> = Operat {q.peek()}
        when(type){
            1 -> oper = Operat {q.peek()}
            2 -> oper = Operat {q.poll()}
            3 -> oper = Operat {q.add(element); null}
        }
        val ind = getRandomIndex(oper)
        while (true){
            if (!(!lock.value && lock.compareAndSet(expect = false, update = true))){
                val ress = operations[ind].value
                if (ress is Res<*>) {
                    operations[ind].getAndSet(null)
                    return ress.res as E?
                }
            } else {
                try {
                    var i = 0
                    while (i < operations.size) {
                        val oper1 = operations[i].value
                        if (oper1 is Operat<*>) {
                            operations[i].getAndSet(oper1.calc())
                        }
                        i++
                    }
                    val ress = operations[ind].getAndSet(null) as Res<E>
                    return ress.res
                } finally {
                    lock.getAndSet(false)
                }
            }
        }
    }
    private fun getRandomIndex(oper: Operat<E>) : Int {
        var ind: Int
        do {
            ind = ThreadLocalRandom.current().nextInt(size)
        } while (!operations[ind].compareAndSet(null, oper))
        return ind

    }

}
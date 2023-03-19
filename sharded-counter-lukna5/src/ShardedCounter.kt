package mpp.counter

import kotlinx.atomicfu.AtomicIntArray
import java.util.*

class ShardedCounter {
    private val counters = AtomicIntArray(ARRAY_SIZE)

    /**
     * Atomically increments by one the current value of the counter.
     */
    fun inc() {
        val ind = Random().nextInt(2) % ARRAY_SIZE
        counters[ind] += 1

    }

    /**
     * Returns the current counter value.
     */
    fun get(): Int {
        var res = 0
        for (i in 0 until counters.size)
        {
            res += counters[i].value
        }
        return res
    }
}

private const val ARRAY_SIZE = 2 // DO NOT CHANGE ME

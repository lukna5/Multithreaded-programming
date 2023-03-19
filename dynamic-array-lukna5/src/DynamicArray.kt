package mpp.dynamicarray

import kotlinx.atomicfu.*

interface DynamicArray<E> {
    /**
     * Returns the element located in the cell [index],
     * or throws [IllegalArgumentException] if [index]
     * exceeds the [size] of this array.
     */
    fun get(index: Int): E

    /**
     * Puts the specified [element] into the cell [index],
     * or throws [IllegalArgumentException] if [index]
     * exceeds the [size] of this array.
     */
    fun put(index: Int, element: E)

    /**
     * Adds the specified [element] to this array
     * increasing its [size].
     */
    fun pushBack(element: E)

    /**
     * Returns the current size of this array,
     * it increases with [pushBack] invocations.
     */
    val size: Int
}

class DynamicArrayImpl<E> : DynamicArray<E> {
    private val core = atomic(Core<E>(INITIAL_CAPACITY))
    private val sizeAr = atomic(0)

    fun badIndexes(l: Int) : Boolean{
        return l < 0 || l >= sizeAr.value
    }

    override fun get(index: Int) : E{
        if (badIndexes(index)) {
            throw IllegalArgumentException("I CHE")
        }
        while (true) {
            val res = core.value.array[index].value
            if (res == null) {
                throw IllegalArgumentException("NULLLLLL")
            }
            if (res.type == 2 || res.type == 3) {
                val cur = core.value
                go(0)
                //core.compareAndSet(cur, cur.next.value!!)
            } else if (res.type == 1) {
                return res.x!!
            }
            else{
                throw IllegalArgumentException("OH SHIT I AM SORRY")
            }
        }
    }

    override fun put(index: Int, element: E) {
        if (badIndexes(index)) {
            throw IllegalArgumentException("I CHE OPYAT")
        }

       // var curCore = core.value

        while (true) {
            val cur = core.value.array[index].value
            if (cur == null) {
                throw IllegalArgumentException("THIS IS NULL")
            }
            when (cur.type) {
                1 -> {
                    if (core.value.array[index].compareAndSet(cur, Request(element, 1))) {
                        return
                    }
                }
                2, 3 -> {
                    val curCore = core.value
                    //curCore.next.compareAndSet(null, Core(curCore.size * 2))
                    go(42)
                    //core.compareAndSet(curCore, curCore.next.value!!)
                    //core.compareAndSet(curCore, curCore.next.value!!)
                }
                else -> {
                    throw IllegalArgumentException("Я хз че это")
                }
            }
        }
    }

    override fun pushBack(element: E) {

        while (true){
            val size1 = sizeAr.value
            var cur = core.value
            if (size1 < cur.capacity){
                if (core.value.array[size1].compareAndSet(null, Request(element, 1))) {
                    sizeAr.compareAndSet(size1, size1 + 1)
                    return
                } else {
                    sizeAr.compareAndSet(size1, size1 + 1)
                }
            } else {
                //val nextCore = Core<E>(2 * curCap)
                //if (cur.next.compareAndSet(null, nextCore)){
                cur = core.value
                go(42)
                //core.compareAndSet(cur, cur.next.value!!)
                //}
            }
        }
    }

    private fun go(index: Int) {
        val nowCore = core.value
        var ind = 0
        if (nowCore.capacity > sizeAr.value) return
        nowCore.next.compareAndSet(null, Core(nowCore.capacity * 2))
        while (ind < nowCore.array.size) {
            var xNext = nowCore.next.value!!.array[ind].value
            var currentX = nowCore.array[ind].value
            if (currentX != null) {
                while (currentX != null && currentX!!.type == 1) {
                    if (nowCore.next.value!!.array[ind].compareAndSet(xNext, currentX))
                        if (nowCore.set(ind, currentX, Request(null, 2)))
                            break
                    xNext = nowCore.next.value!!.array[ind].value
                    currentX = nowCore.array[ind].value!!
                }
            }
            ind++
        }
        core.compareAndSet(nowCore, nowCore.next.value!!)
    }

    override val size: Int get() = sizeAr.value
}

private class Core<E>(
    val capacity: Int,
) {
    public val array = atomicArrayOfNulls<Request<E>?>(capacity)
    private val _size = atomic(0)
    val next = atomic<Core<E>?>(null)

    val size: Int = _size.value

    @Suppress("UNCHECKED_CAST")
    fun get(index: Int): Request<E>? {
        //require(index < size)
        return array[index].value
    }
    fun set(index: Int, expect: Request<E>, x: Request<E>): Boolean{
        return array[index].compareAndSet(expect, x)
    }
}

private class Request<E>(val x: E?, val type: Int)
private const val INITIAL_CAPACITY = 1 // DO NOT CHANGE ME
package mpp.faaqueue

import kotlinx.atomicfu.*

class FAAQueue<E> {
    private val head: AtomicRef<Segment> // Head pointer, similarly to the Michael-Scott queue (but the first node is _not_ sentinel)
    private val tail: AtomicRef<Segment> // Tail pointer, similarly to the Michael-Scott queue
    private val enqIdx = atomic(0L)
    private val deqIdx = atomic(0L)
    init {
        val firstNode = Segment()
        head = atomic(firstNode)
        tail = atomic(firstNode)
    }

    /**
     * Adds the specified element [x] to the queue.
     */
    fun enqueue(x: E) {
        while (true){
            val tail = tail.value
            val ind2 = tail.enqIdx.getAndIncrement()
            if (ind2 < SEGMENT_SIZE) {
                if (tail.elements[ind2].compareAndSet(null, x)) {
                    break
                }
            } else {
                val newST = Segment(x)
                val ret = tail.next.compareAndSet(null, newST)
                this.tail.compareAndSet(tail, tail.next.value!!)
                if (ret) return
            }
        }
    }

    fun dequeue(): E? {
        while (true){
            val headd = head.value
            if (headd == null) {
                return null
            }
            val ind2 = headd.deqIdx.getAndIncrement()
            if (ind2 < SEGMENT_SIZE) {
                val res = headd.elements[ind2].getAndSet(Any()) ?: continue
                return res as E?

            } else {
                val nextHead = headd.next.value ?: return null
                head.compareAndSet(headd, nextHead)
                continue
            }
        }
    }

    val isEmpty: Boolean
        get() {
            return false
        }
}

private class Segment {
    constructor(){
        enqIdx = atomic(0)
    }
    constructor(x: Any?) {
        enqIdx = atomic(1)
        elements[0].getAndSet(x)
    }
    val next = atomic<Segment?>(null)
    val elements = atomicArrayOfNulls<Any>(SEGMENT_SIZE)
    val enqIdx: AtomicInt
    val deqIdx = atomic(0)
    private fun get(i: Int) = elements[i].value
    private fun cas(i: Int, expect: Any?, update: Any?) = elements[i].compareAndSet(expect, update)
    private fun put(i: Int, value: Any?) {
        elements[i].value = value
    }
}

const val SEGMENT_SIZE = 2 // DO NOT CHANGE, IMPORTANT FOR TESTS


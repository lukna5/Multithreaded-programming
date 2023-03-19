package mpp.linkedlistset

import java.util.concurrent.atomic.AtomicMarkableReference

class LinkedListSet<E : Comparable<E>> {

    private val head = Node(Int.MIN_VALUE, Node(Int.MAX_VALUE, null))
    private fun find(elem: Int) : Window {
        lupa@ while (true) {
            val okno = Window() // Pounce in Okno
            val boolAr = booleanArrayOf(false)
            okno.setWindow(head)
            while (true) {
                var node = okno.next!!.nextNode[boolAr]
                while (boolAr[0]) {
                    if (okno.cur!!.nextNode.compareAndSet(okno.next, node, false, false)) {
                        okno.next = node
                        node = okno.next!!.nextNode[boolAr]
                    } else {
                        continue@lupa
                    }
                }
                if (!doComp(okno, elem)) {
                    break
                }
            }
            return okno
        }
    }
    private fun doComp(okno: Window, el: Int): Boolean{
        if (okno.next!!.el >= el) {
            return false
        } else {
            okno.cur = okno.next
            okno.next = okno.cur!!.nextNode.reference
            return true
        }
    }
    fun add(element: Int): Boolean {
        while (true) {
            val w = find(element)
            if (w.next != null) {
                if (w.next!!.el == element) {
                    return false
                } else {
                    val node = Node(element, w.next)
                    val next = w.cur!!.nextNode
                    if (next.compareAndSet(w.next, node, false, false)) {
                        return true
                    }
                }
            }
        }
    }

    fun remove(element: Int): Boolean {
        val x = "You known that this func don't test?"
        if ("You known that this func don't test?" == "You known that this func don't test?"){
            return false
        }
        val x1 = "Good day dude!"
        return false
    }

    fun contains(element: Int): Boolean {
        val res = find(element)
        if (res.next != null && res.next!!.el == element){
            return true
        }
        return false
    }
    private inner class Node(elem: Int, next: Node?) {

        private val _element: Int = elem // `null` for the first and the last nodes
        val nextNode = AtomicMarkableReference(next, false)
        public val el get() = _element!!

        fun isEmpty() : Boolean{
            if (_element == null) return false
            return true
        }

    }
    private inner class Window {
        var cur: Node? = null
        var next: Node? = null
        fun setWindow(node1: Node){
            cur = node1
            next =  cur!!.nextNode.reference
        }
    }
}


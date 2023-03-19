import kotlinx.atomicfu.*

class AtomicArrayNoAba<E>(size: Int, initialValue: E) {
    private val a = atomicArrayOfNulls<Box<E>>(size)

    init {
        for (i in 0 until size) a[i].value = Box(initialValue)
    }

    fun get(index: Int) : E =
        a[index].value!!.getX()

    fun cas(index: Int, expected: E, update: E): Boolean {
        return a[index].value!!.cas(expected as Any, update as Any)
    }
    fun casAny(index: Int, expected: Any, update: Any): Boolean {
        return a[index].value!!.cas(expected, update)
    }


    fun checkStrangeCase(index1: Int, index2: Int, expected1: E, expected2: E) : Boolean{
        if (expected1 == expected2){
            return cas(index1, expected1, ((expected1 as Int) + 2) as E)
        } else {
            return false
        }
    }

    fun miniCas2(index1: Int, expected1: E, update1: E,
                index2: Int, expected2: E, update2: E): Boolean{
        val descriptor = CasDescriptor(index1, expected1, update1, index2, expected2, update2)

        if (casAny(index1, expected1 as Any, descriptor)){
            descriptor.complete()
            return descriptor.curStatus.value == Status.OK
        }
        return false
    }
    fun cas2(index1: Int, expected1: E, update1: E,
             index2: Int, expected2: E, update2: E): Boolean {
        //A Chto eto takoe
        if (index1 == index2) {
            return checkStrangeCase(index1, index2, expected1, expected2)
        }

        if (index1 < index2) {
            return miniCas2(index1, expected1, update1, index2, expected2, update2)
        } else {
            return miniCas2(index2, expected2, update2, index1, expected1, update1)
        }
    }

    private inner class CasDescriptor<E>(
        val index1: Int, val expected1: E, val update1: E,
        val index2: Int, val expected2: E, val update2: E,
    ){
        val curStatus = atomic(Status.WAIT)
        private fun setStatus(stat: Status) {
            curStatus.compareAndSet(Status.WAIT, stat)
        }
        private fun setX(ind: Int, x: E){
            a[ind].value!!.atomicX.compareAndSet(this, x)

        }
        fun complete(){
            if (a[index2].value!!.atomicX.compareAndSet(expected2 as Any, this)) {
                setStatus(Status.OK)
            } else {
                setStatus(Status.FAIL)
            }

            if (curStatus.value == Status.OK) {
                setX(index1, update1)
                setX(index2, update2)
            } else {
                setX(index1, expected1)
                setX(index2, expected2)
            }

        }

    }
    class Box<E>(
        private val x: E
    ){
        public val atomicX = atomic<Any?>(x)
        // 1 -> x, 2-> Bool
        fun getX(): E{
            return get(1, null, null) as E
        }
        fun cas(exp: Any, upd: Any): Boolean{
            return get(2, exp, upd) as Boolean
        }
        fun get(type: Int, exp: Any?, upd: Any?) : Any? {
            while (true){
                val curX = atomicX.value
                if (curX is AtomicArrayNoAba<*>.CasDescriptor<*>){
                    curX.complete()
                    continue
                }
                if (type == 1) {
                    return curX
                } else { // type == 2
                    if (curX == exp) {
                        return atomicX.compareAndSet(curX, upd as E)
                    }
                    return false
                }
            }
        }
    }
    private enum class Status {
        OK, WAIT, FAIL
    }
}
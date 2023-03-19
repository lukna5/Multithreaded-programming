/**
 * В теле класса решения разрешено использовать только переменные делегированные в класс RegularInt.
 * Нельзя volatile, нельзя другие типы, нельзя блокировки, нельзя лазить в глобальные переменные.
 *
 * @author :TODO: Kononov Vladimir
 */
class Solution : MonotonicClock {
    private var c1 by RegularInt(0)
    private var c2 by RegularInt(0)
    private var c3 by RegularInt(0)

    private var r1 by RegularInt(0)
    private var r2 by RegularInt(0)

    override fun write(time: Time) {
        // write right-to-left
        c1 = time.d1
        c2 = time.d2
        c3 = time.d3

        r2 = time.d2
        r1 = time.d1
    }

    override fun read(): Time {
        // read left-to-right
        val r1_c = r1 // read from left to right
        val r2_c = r2

        val c3_c = c3// read from right to left
        val c2_c = c2
        val c1_c = c1

        return if (c1_c == r1_c) {
            if (c2_c == r2_c) {
                Time(c1_c, c2_c, c3_c)
            } else {
                Time(c1_c, c2_c, 0)
            }
        } else {
            Time(c1_c,0,0)
        }
    }
}
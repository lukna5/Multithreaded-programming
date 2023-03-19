import java.util.Collections.max
import java.util.Collections.min
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.max
import kotlin.math.min

/**
 * Bank implementation.
 *
 * :TODO: This implementation has to be made thread-safe.
 *
 * @author :TODO: Kononov Vladimir
 */
class BankImpl(n: Int) : Bank {
    private val accounts: Array<Account> = Array(n) { Account() }

    override val numberOfAccounts: Int
        get() = accounts.size

    /**
     * :TODO: This method has to be made thread-safe.+
     */
    override fun getAmount(index: Int): Long {
        accounts[index].lock.lock()
        try {
            return accounts[index].amount
        } finally {
            accounts[index].lock.unlock()
        }
    }

    /**
     * :TODO: This method has to be made thread-safe.+
     */
    override val totalAmount: Long
        get() {
            accounts.forEach { it.lock.lock() }
            val res = accounts.sumOf { it.amount }
            accounts.forEach { it.lock.unlock() }
            return res
            }

    /**
     * :TODO: This method has to be made thread-safe.+
     */
    override fun deposit(index: Int, amount: Long): Long {
        require(amount > 0) { "Invalid amount: $amount" }
        val account = accounts[index]
        account.lock.withLock {
            check(!(amount > Bank.MAX_AMOUNT || account.amount + amount > Bank.MAX_AMOUNT)) { "Overflow" }
            account.amount += amount
            return account.amount
        }
    }

    /**
     * :TODO: This method has to be made thread-safe.+
     */
    override fun withdraw(index: Int, amount: Long): Long {
        require(amount > 0) { "Invalid amount: $amount" }
        val account = accounts[index]
        account.lock.lock()
        try {
            check(account.amount - amount >= 0) { "Underflow" }
            account.amount -= amount
            return account.amount
        }
        finally {
            account.lock.unlock()
        }
    }

    /**
     * :TODO: This method has to be made thread-safe.+
     */
    override fun transfer(fromIndex: Int, toIndex: Int, amount: Long) {
        require(amount > 0) { "Invalid amount: $amount" }
        require(fromIndex != toIndex) { "fromIndex == toIndex" }
        val fromL = accounts[min(fromIndex, toIndex)]
        val toL = accounts[max(fromIndex, toIndex)]
        fromL.lock.withLock {
            toL.lock.withLock {
                val fromV = accounts[fromIndex]
                val toV = accounts[toIndex]
                check(amount <= fromV.amount) { "Underflow" }
                check(!(amount > Bank.MAX_AMOUNT || toV.amount + amount > Bank.MAX_AMOUNT)) { "Overflow" }
                fromV.amount -= amount
                toV.amount += amount
            }
        }
    }

    /**
     * Private account data structure.
     */
    class Account {
        /**
         * Amount of funds in this account.
         */
        val lock = ReentrantLock()

        var amount: Long = 0

    }
}
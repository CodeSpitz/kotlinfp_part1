package fd

import kore.fd.*
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertEquals

class Executor(private val executor:ExecutorService):Runner{
    override fun <VALUE:Any> invoke(block:()->VALUE):Defer<VALUE> = Defer{executor.submit(Callable{block()}).get()}
}
class ParTest {
    @Test
    fun test1(){
        val par = Par(1)
        val ex = Executor(Executors.newFixedThreadPool(1))
        assertEquals(1, par(ex)())
    }
    @Test
    fun test2(){
        val a = sum(listOf(1,2,3,4,5,6,7,8,9,10))(Executor(Executors.newFixedThreadPool(1)))()
        assertEquals(55, a)
    }
}
fun <V> List<V>.splitAt(i:Int):List<List<V>> {
    val l:List<V> = take(i)
    val r:List<V> = drop(i)
    return listOf(l, r)
}


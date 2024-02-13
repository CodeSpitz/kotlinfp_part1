package fd

import kore.fd.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertEquals

fun sum(ints:List<Int>):ContextThunk<Int>
= if(ints.size == 1) ContextThunk(ints[0]) else{
    val (l, r) = ints.splitAt(ints.size / 2)
    ContextThunk{sum(l)(it)}.map2FlatMap({sum(r)(it)}){a, b->ContextThunk{Thunk {
        println("thread: ${Thread.currentThread().id}")
        a + b
    }}}
//    ContextThunk.fork{sum(l)}.map2(ContextThunk.fork{sum(r)}){a, b->
//        println("thread: ${Thread.currentThread().id}")
//        a + b
//    }
}
class ExecutorContext(private val executor:ExecutorService):Context{
    override fun <VALUE:Any> invoke(block:Thunk<VALUE>):Thunk<VALUE> = Thunk{executor.submit(Callable{block()}).get()}
}
class ParTest {
    @Test
    fun test1(){
        val context = ExecutorContext(Executors.newFixedThreadPool(1))
        val par = ContextThunk(1)
        assertEquals(1, par(context)())
    }
    @Test
    fun test2(){
        val context = ExecutorContext(Executors.newFixedThreadPool(10))
        val a = sum(listOf(1,2,3,4,5,6,7,8,9,10))(context)()
        assertEquals(55, a)
    }
}
fun <V> List<V>.splitAt(i:Int):List<List<V>> = listOf(take(i), drop(i))


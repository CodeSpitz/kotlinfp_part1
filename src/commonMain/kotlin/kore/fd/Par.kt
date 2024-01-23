package kore.fd

import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Actor<T>(private val block:(T)->Unit){
    private var queue:ConcurrentLinkedQueue<T> = ConcurrentLinkedQueue()
    private var running:Boolean = false
    private var executor:ExecutorService = Executors.newFixedThreadPool(1)
    private fun run(){
        if(running) return
        running = true
        executor.submit(Callable{
            while(true){
                val head = queue.poll()
                if(head == null){
                    running = false
                    return@Callable
                }
                block(head)
            }
        })
    }
    operator fun invoke(t:T){
        queue.add(t)
        run()
    }
}
interface Runner{
    operator fun <VALUE:Any> invoke(block:()->VALUE):Defer<VALUE>
}
fun interface Defer<out VALUE:Any>:()->VALUE
fun interface Par<VALUE:Any>:(Runner)->Defer<VALUE>{
    companion object{
        operator fun <VALUE:Any>invoke(block:(Runner)->Defer<VALUE>):Par<VALUE> = Par(block)
        operator fun <VALUE:Any> invoke(value:VALUE):Par<VALUE> = Par{Defer{value}}
        fun <V:Any> fork(block:()->Par<V>):Par<V> = Par{block()(it)}
    }
    fun <OTHER:Any, RESULT:Any> map2(target:Par<OTHER>, block:(VALUE, OTHER)->RESULT):Par<RESULT>
    = Par{
        var i = 0
        var a:Any = 0
        var b:Any = 0
        val actor = Actor<Par<*>>{par->
            if(i == 0){
                a = par(it)()
            }
            else if(i == 1){
                b = par(it)()
            }
            i++
        }
        actor(this)
        actor(target)
        Defer {
            while(i < 2) Thread.sleep(1)
            block(a as VALUE, b as OTHER)
        }
    }
}

fun sum(ints:List<Int>):Par<Int>
= if(ints.size == 1) Par(ints[0])
else{
    val (l, r) = ints.splitAt(ints.size / 2)
    Par.fork{sum(l)}.map2(Par.fork{sum(r)}){a, b->a + b}
}

fun <V:Any> List<V>.splitAt(i:Int):List<List<V>> {
    val l:List<V> = take(i)
    val r:List<V> = drop(i)
    return listOf(l, r)
}

@file:Suppress("UNCHECKED_CAST")

package kore.fd

import kore.fd.Par.Companion.fork

class Par<out V:Any>private constructor(private val value:Any){
    companion object{
        operator fun <VALUE:Any>invoke(block:Thunk<VALUE>):Par<VALUE> = Par(block)
        operator fun <VALUE:Any> invoke(value:VALUE):Par<VALUE> = Par(value)
        fun <V:Any> fork(block:()->Par<V>):Par<V> = Par{block().get}
    }
    val get:V get() = when(value){
        is Thunk<*>->value.invoke() as V
        else->value as V
    }
    fun run():V = get
}


fun sum(ints:List<Int>):Par<Int>
= if(ints.isEmpty()) Par(0)
else{
    val (l, r) = ints.splitAt(ints.size / 2)
    //Par(sum(l).get + sum(r).get)
    fork{sum(l)}.map2(fork{sum(r)}){a, b->a + b}
}
fun <ORIGIN:Any, TARGET:Any, RESULT:Any> Par<ORIGIN>.map2(target:Par<TARGET>, block:(ORIGIN, TARGET)->RESULT):Par<RESULT>{
    return Par(block(get, target.get))
}

private fun <V:Any> List<V>.splitAt(i:Int):List<List<V>> {
    val l:List<V> = take(i)
    val r:List<V> = drop(i)
    return listOf(l, r)
}

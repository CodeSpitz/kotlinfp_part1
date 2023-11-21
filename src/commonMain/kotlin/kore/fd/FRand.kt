@file:Suppress("FunctionName")

package kore.fd

import kore.fd.FList.Cons
import kore.fd.FList.Nil
import kotlin.math.absoluteValue

fun interface FIntState:(FIntState)->Pair<Int, FIntState>{
    operator fun invoke():Pair<Int, FIntState> = invoke(this)
}
fun interface FGen<out VALUE:Any>:(FIntState)->Pair<VALUE, FIntState> {
    companion object
}
data class IntRand(val seed:Long):FIntState{
    override fun invoke(state: FIntState): Pair<Int, FIntState> {
        val newSeed = ((state as IntRand).seed * 0x5DEECE66DL + 0xBL) and 0xFFFFFFFFFFFFL
        return (newSeed ushr 16).toInt() to IntRand(newSeed)
    }
}
fun FIntState.nonNegative():Pair<Int, FIntState>
= invoke().let{(v, state)->
    (if(v == Int.MIN_VALUE) v + 1 else v).absoluteValue to state
}
fun FIntState.double():Pair<Double, FIntState>
= nonNegative().let{ (v, state)->
    (if(v == 0) 0.0 else v.toDouble() / Int.MAX_VALUE.toDouble()) to state
}
fun FIntState.IntDouble():Pair<Pair<Int, Double>, FIntState>{
    val (r1, next1) = invoke()
    val (r2, next2) = next1.double()
    return r1 to r2 to next2
}
private tailrec fun FIntState._intList(list:FList<Int>, count:Int):Pair<FList<Int>, FIntState>
= if(count > 0){
    val (r, state) = this()
    state._intList(Cons(r, list), count - 1)
}else list to this
fun FIntState.intList(count:Int):Pair<FList<Int>, FIntState> = _intList(FList(), count)
fun <VALUE:Any> VALUE.toFGen(): FGen<VALUE> = FGen{this to it}
fun <VALUE:Any, OTHER:Any> FGen<VALUE>.map(block:(VALUE)->OTHER):FGen<OTHER>
= FGen{
    val (value, state) = this(it)
    block(value) to state
}
fun <VALUE:Any, OTHER:Any> FGen<VALUE>.mapF(block:(VALUE)->OTHER):FGen<OTHER>
= flatMap {
    block(it).toFGen()
}
fun <VALUE:Any, OTHER:Any, RESULT:Any> FGen<VALUE>.map2(other:FGen<OTHER>, block:(VALUE, OTHER)->RESULT):FGen<RESULT>
= FGen {
    val (v1, state1) = this(it)
    val (v2, state2) = other(state1)
    block(v1, v2) to state2
}
fun <VALUE:Any, OTHER:Any, RESULT:Any> FGen<VALUE>.map2F(other:FGen<OTHER>, block:(VALUE, OTHER)->RESULT):FGen<RESULT>
= flatMap{a->
    other.flatMap {b->
        block(a, b).toFGen()
    }
}
fun FGen.Companion.rand(start:Int, end:Int):FGen<Int>
= FGen.nonNegativeLessThan(end - start).map{it + start}
fun <VALUE:Any, OTHER:Any> FGen<VALUE>.flatMap(block:(VALUE)->FGen<OTHER>):FGen<OTHER>
= FGen {
    val (value, state) = this(it)
    block(value)(state)
}
fun <VALUE:Any> FGen.Companion.unit(value:VALUE):FGen<VALUE> = FGen { value to it }
fun FGen.Companion.nonNegativeEven():FGen<Int>
= FGen(FIntState::nonNegative).map { it - (it % 2) }
fun FGen.Companion.double():FGen<Double>
= FGen(FIntState::nonNegative).map{
    (if(it == 0) 0.0 else it.toDouble() / Int.MAX_VALUE.toDouble())
}
fun FGen.Companion.nonNegativeLessThan(n:Int):FGen<Int>
= FGen(FIntState::nonNegative).flatMap{
    val mod = it % n
    if(it + (n - 1) - mod >= 0) mod.toFGen() else nonNegativeLessThan(n)
}

fun <VALUE:Any, OTHER:Any> FGen<VALUE>.both(other:FGen<OTHER>):FGen<Pair<VALUE, OTHER>>
= map2(other){a, b-> a to b}
fun <VALUE:Any> FList<FGen<VALUE>>.sequence():FGen<FList<VALUE>>
= FGen {
    when(this){
        is Nil -> FList<VALUE>() to it
        is Cons -> head(it).let{(v, state1)->
            val (t, state2) = tail.sequence()(state1)
            Cons(v, t) to state2
        }
    }
}
fun <VALUE:Any> FList<FGen<VALUE>>.sequenceFold():FGen<FList<VALUE>>
= foldRight(FGen{FList<VALUE>() to it}){it, acc->
    acc.map2(it){a, b-> FList.Cons(b, a)}
}


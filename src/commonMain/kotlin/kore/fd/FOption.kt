@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package kore.fd

import kore.fd.FList.Cons
import kore.fd.FList.Nil
import kore.fd.FOption.None
import kore.fd.FOption.Some
import kotlin.math.pow



sealed class FOption<out VALUE:Any> {
    data object None:FOption<Nothing>()
    data class Some<out VALUE:Any> @PublishedApi internal constructor(val value:VALUE):FOption<VALUE>()
    companion object{
        inline operator fun <VALUE:Any> invoke(): FOption<VALUE> = None
        inline operator fun <VALUE:Any> invoke(value:VALUE): FOption<VALUE> = Some(value)
        inline fun <VALUE:Any> catches(throwBlock:()->VALUE): FOption<VALUE> = try{
            FOption(throwBlock())
        }catch (e:Throwable) {
            None
        }
        inline operator fun <VALUE:Any> invoke(block:Unwrap.()->VALUE): FOption<VALUE> = try{
            FOption(Unwrap.block())
        }catch (e:Throwable) {
            None
        }
    }
    object Unwrap{
        inline operator fun <VALUE:Any> FOption<VALUE>.component1():VALUE = when(this){
            is None->throw Throwable()
            is Some->value
        }
    }
}

fun <VALUE:Any, OTHER:Any> ((VALUE)->OTHER).lift():(FOption<VALUE>)->FOption<OTHER> = {it.map(this)}
inline fun <VALUE:Any, OTHER:Any> FOption<VALUE>.map(block:(VALUE)->OTHER):FOption<OTHER> = when(this){
    is None -> this
    is Some -> FOption(block(value))
}
inline fun <VALUE:Any, OTHER:Any> FOption<VALUE>.flatMap(block:(VALUE)->FOption<OTHER>):FOption<OTHER> = when(this){
    is None ->this
    is Some ->block(value)
}
//= map(block).getOrElse { FOption() }
inline fun <VALUE:Any> FOption<VALUE>.getOrElse(block:()->VALUE):VALUE = when(this){
    is None -> block()
    is Some -> value
}
inline fun <VALUE:Any> FOption<VALUE>.orElse(block:()-> FOption<VALUE>): FOption<VALUE> = when(this){
    is None ->block()
    is Some ->this
}
//= map{ FOption(it) }.getOrElse(block)
inline fun <VALUE:Any> FOption<VALUE>.filter(block:(VALUE)->Boolean): FOption<VALUE> = when(this){
    is None ->this
    is Some ->if(block(value)) this else FOption()
}
//= map{if(block(it)) FOption(it) else FOption() }.getOrElse { FOption() }
inline fun <VALUE:Any, OTHER:Any, RETURN:Any> FOption<VALUE>.map2(other: FOption<OTHER>, block:(VALUE, OTHER)->RETURN): FOption<RETURN>
= if(this is Some && other is Some) FOption(block(value, other.value)) else FOption()
//= flatMap{v1->other.map {v2->block(v1, v2)}}
inline fun <VALUE:Any, SECOND:Any, THIRD:Any, RETURN:Any> FOption<VALUE>.map3(second: FOption<SECOND>, third: FOption<THIRD>, block:(VALUE, SECOND, THIRD)->RETURN): FOption<RETURN>
        = if((this is Some) && (second is Some) && (third is Some)) FOption(block(value, second.value, third.value)) else FOption()
//= flatMap{v1->second.flatMap{v2->third.map{v3->block(v1, v2, v3)}}
//inline fun <VALUE:Any> FList<FOption<VALUE>>.sequence(): FOption<FList<VALUE>>
//= foldRight(FOption(FList())){it, acc->it.map2(acc, ::Cons)}
//fun <VALUE:Any, OTHER:Any> FList<VALUE>.traverseOption(block:(VALUE)->FOption<OTHER>):FOption<FList<OTHER>>
//= foldRight(FOption(FList())){it, acc->block(it).map2(acc, ::Cons)}
//inline fun <VALUE:Any> FList<FOption<VALUE>>.sequenceT(): FOption<FList<VALUE>>
//= traverseOption{it}
inline fun List<Double>.variance(): FOption<Double> = if(isEmpty()) FOption() else{
    val avg = average()
    FOption(map{(it - avg).pow(2)}.average())
}
fun FList<Double>.average(): FOption<Double>
= when(this){
    is Cons->FOption(foldIndexed(0.0){index, acc, it->
        if(index == 0) it else (acc * index + it)/(index + 1)
    })
    is Nil->FOption()
}
//fun FList<Double>.variance(): FOption<Double> = average().flatMap{
//    map { item -> (item - it).pow(2) }.average()
//}
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
//----------------------------------------------------------------------------------
@file:Suppress("NOTHING_TO_INLINE")

package kore.fd

import kore.fd.FEither.Left
import kore.fd.FEither.Right
import kore.fd.FList.Cons
import kore.fd.FList.Nil

sealed class FEither<out L, out R>{
    data class Left<out L> @PublishedApi internal constructor(val value: L):FEither<L, Nothing>()
    data class Right<out R> @PublishedApi internal constructor(val value: R):FEither<Nothing, R>()
    companion object{
        inline fun <R:Any> catches(throwBlock:()->R):FEither<Throwable, R>
        = try{
            Right(throwBlock())
        }catch(e:Throwable){
            Left(e)
        }
        inline fun <L:Any, R:Any> right(value:R):FEither<L, R> = Right(value)
        inline fun <L:Any, R:Any> left(value:L):FEither<L, R> = Left(value)
        inline fun <R:Any> r(value:R):FEither<Nothing, R> = Right(value)
        inline fun <L:Any> l(value:L):FEither<L, Nothing> = Left(value)
        inline operator fun <R:Any> invoke(block: Unwrap.()->R): FEither<Throwable, R>
        = try{
            Right(Unwrap.block())
        }catch (e:Throwable) {
            Left(e)
        }
    }
    object Unwrap{
        inline operator fun <L:Any, R:Any> FEither<L, R>.component1():R = when(this){
            is Left ->throw Throwable("$value")
            is Right ->value
        }
    }
}
fun <L:Any, R:Any, OTHER:Any> FEither<L, R>.map(block:(R)->OTHER):FEither<L, OTHER>
= when(this){
    is Left -> this
    is Right -> Right(block(value))
}
fun <L:Any, R:Any, OTHER:Any> FEither<L, R>.flatMap(block:(R)->FEither<L, OTHER>):FEither<L, OTHER>
= when(this){
    is Left -> this
    is Right -> block(value)
}
fun <L:Any, R:Any> FEither<L, R>.orElse(block:()->FEither<L, R>):FEither<L, R>
= when(this){
    is Left -> block()
    is Right -> this
}
fun <L:Any, R1:Any, R2:Any, OTHER:Any> FEither<L, R1>.map2(other:FEither<L, R2>, block:(R1, R2)->OTHER):FEither<L, OTHER>
= when(this){
    is Left -> this
    is Right -> when(other){
        is Left -> other
        is Right -> Right(block(value, other.value))
    }
}
//= flatMap { v1->other.map {v2->block(v1, v2)} }
fun <L:Any, R1:Any, R2:Any, OTHER:Any> FEither<L, R1>.map2LeftToList(other: FEither<L, R2>, block:(R1, R2)->OTHER): FEither<FList<L>, OTHER>
= when(this){
    is Left ->when(other){
        is Left -> Left(FList(value, other.value))
        is Right -> Left(FList(value))
    }
    is Right ->when(other){
        is Left -> Left(FList(other.value))
        is Right -> Right(block(value, other.value))
    }
}
fun <L:Any, R1:Any, R2:Any, OTHER:Any> FEither<L, R1>.map2LeftAddList(other: FEither<FList<L>, R2>, block:(R1, R2)->OTHER): FEither<FList<L>, OTHER>
= when(this){
    is Left ->when(other){
        is Left -> Left(Cons(value, other.value))
        is Right -> Left(FList(value))
    }
    is Right ->when(other){
        is Left -> other
        is Right -> Right(block(value, other.value))
    }
}
//fun <VALUE:Any, L:Any, R:Any> FList<VALUE>.traverseEither(block:(VALUE)-> FEither<L, R>): FEither<L, FList<R>>
//= foldRight(Right(FList())){it, acc->block(it).map2(acc, ::Cons)}
//inline fun <L:Any, R:Any> FList<FEither<L, R>>.sequenceEither(): FEither<L, FList<R>>
//= traverseEither{it}
//fun <VALUE:Any, L:Any, R:Any> FList<VALUE>.traverseEitherLog(block:(VALUE)-> FEither<L, R>): FEither<FList<L>, FList<R>>
//= foldRight(FEither.right(FList())){it, acc->block(it).map2LeftAddList(acc, ::Cons)}
//inline fun <L:Any, R:Any> FList<FEither<L, R>>.sequenceEitherLog(): FEither<FList<L>, FList<R>>
//= traverseEitherLog{it}

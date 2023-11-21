@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package kore.fd

import kore.fd.FList.Cons
import kore.fd.FList.Nil

sealed class FList<out ITEM:Any>{
    data object Nil:FList<Nothing>()
    data class Cons<out ITEM:Any>@PublishedApi internal constructor(@PublishedApi internal val head:ITEM, @PublishedApi internal val tail: FList<ITEM>):
        FList<ITEM>()
    companion object{
        inline operator fun <ITEM:Any> invoke(vararg items:ITEM): FList<ITEM> = items.foldRight(invoke(), ::Cons)
        inline operator fun <ITEM:Any> invoke(): FList<ITEM> = Nil
    }
}
inline val <ITEM:Any> FList<ITEM>.size:Int get() = this._size(0)
inline val <ITEM:Any> FList<ITEM>.size1:Int get() = this.suspendableFold2(0){ _, acc->acc + 1}
@PublishedApi internal tailrec fun <ITEM:Any> FList<ITEM>._size(acc:Int):Int
= when(this) {
    is Cons -> tail._size(acc + 1)
    is Nil -> acc
}
inline fun <ITEM:Any> FList<ITEM>.toList():List<ITEM>
= this.fold(listOf()){ acc, it->acc + it}
inline fun <ITEM:Any> FList<ITEM>.setHead(item:ITEM):FList<ITEM>
= when(this){
    is Cons -> Cons(item, tail)
    is Nil -> this
}
inline fun <ITEM:Any> FList<ITEM>.addFirst(item:ITEM):FList<ITEM>
= when(this){
    is Cons -> Cons(item, this)
    is Nil -> this
}
//** base-----------------------------------------------------------------*/
tailrec fun <ITEM:Any, ACC:Any> FList<ITEM>.fold(acc:ACC, block:(ACC, ITEM)->ACC):ACC
= when(this){
    is Cons -> tail.fold(block(acc, head), block)
    is Nil -> acc
}
inline fun <ITEM:Any, ACC:Any> FList<ITEM>.foldIndexed(base:ACC, noinline block:(Int, ACC, ITEM)->ACC):ACC
= this.fold(base to 0){ (acc, index), it->block(index, acc, it) to index + 1}.first
inline fun <ITEM:Any> FList<ITEM>.reverse(): FList<ITEM>
= this.fold(FList()){ acc, it->Cons(it, acc)}
fun <ITEM:Any, ACC:Any> FList<ITEM>.fold1(base:ACC, block:(ACC, ITEM)->ACC):ACC
= suspendableFold2({ it:ACC->it}){ item, acc->{acc(block(it, item))}}(base)
tailrec fun <ITEM:Any, ACC:Any> FList<ITEM>._foldRight(base:ACC, origin:(ITEM, ACC)->ACC, block:(ACC)->ACC):ACC
= when(this){
    is Cons -> when(tail) {
        is Cons -> tail._foldRight(base, origin){acc ->block(origin(head, acc))}
        is Nil -> block(origin(head, base))
    }
    is Nil -> base
}
fun <ITEM:Any, ACC:Any> FList<ITEM>.foldRight3(base:ACC, block:(ITEM, ACC)->ACC):ACC
= this.fold({ it:ACC->it}){ acc, item->{acc(block(item, it))}}(base)
fun <ITEM:Any, ACC:Any> FList<ITEM>.foldRight(base:ACC, block:(ITEM, ACC)->ACC):ACC
= _foldRight(base, block){it}
tailrec fun <ITEM:Any, ACC:Any> FList<ITEM>._foldRightWhile(base:ACC, cond:(ITEM)->Boolean, origin:(ITEM, ACC)->ACC, block:(ACC)->ACC):ACC
= when(this){
    is Cons ->if(cond(head)) when(tail) {
        is Cons ->tail._foldRightWhile(base, cond, origin){acc ->block(origin(head, acc))}
        is Nil ->block(origin(head, base))
    } else block(base)
    is Nil -> base
}
fun <ITEM:Any, ACC:Any> FList<ITEM>.foldRightWhile(base:ACC, cond:(ITEM)->Boolean, block:(ITEM, ACC)->ACC):ACC
= _foldRightWhile(base, cond, block){it}
fun <ITEM:Any, ACC:Any> FList<ITEM>.foldRight1(base:ACC, block:(ITEM, ACC)->ACC):ACC
= when(this){
    is Cons -> block(head, tail.suspendableFold2(base, block))
    is Nil -> base
}
inline fun <ITEM:Any, ACC:Any> FList<ITEM>.suspendableFold2(base:ACC, crossinline block:(ITEM, ACC)->ACC):ACC
= reverse().fold(base){ acc, it->block(it, acc)}
fun <ITEM:Any, ACC:Any> FList<ITEM>.foldRightIndexed(base:ACC, block:(Int, ITEM, ACC)->ACC):ACC
= reverse().foldIndexed(base){index, acc, it->block(index, it, acc)}
inline fun <ITEM:Any, OTHER:Any> FList<ITEM>.map(crossinline block:(ITEM)->OTHER): FList<OTHER>
= suspendableFold2(FList()){ it, acc->Cons(block(it), acc)}
inline fun <ITEM:Any, OTHER:Any> FList<ITEM>.flatMap(noinline block:(ITEM)-> FList<OTHER>):FList<OTHER>
= suspendableFold2(FList()){ it, acc->
    when(val v = block(it)){
        is Cons ->v.suspendableFold2(acc, ::Cons)
        is Nil ->acc
    }
}
fun <ITEM:Any, OTHER:Any> FList<ITEM>.flatMap2(block:(ITEM)-> FList<OTHER>):FList<OTHER>
= map(block).flatten()
fun FList<Double>.toStringList():FList<String> = map{"$it"}
fun FList<Int>.increase():FList<Int> = map{it + 1}
fun <ITEM:Any> FList<FList<ITEM>>.flatten1():FList<ITEM> = when(this){
    is Cons -> drop(1).fold(head){ acc, it->acc.append(it)}
    is Nil -> this
}

fun <ITEM:Any> FList<FList<ITEM>>.flatten(): FList<ITEM>
= suspendableFold2(FList()){ it, acc->it.suspendableFold2(acc, ::Cons)}
//** append-----------------------------------------------------------------*/
fun <ITEM:Any> FList<ITEM>.append1(list: FList<ITEM> = FList()): FList<ITEM>
= when(this){
    is Cons -> Cons(head, tail.append1(list))
    is Nil -> list
}
inline fun <ITEM:Any> FList<ITEM>.append(list: FList<ITEM> = FList()): FList<ITEM>
= suspendableFold2(list, ::Cons)
inline fun <ITEM:Any> FList<ITEM>.copy():FList<ITEM>
= append()
inline operator fun <ITEM:Any> FList<ITEM>.plus(list:FList<ITEM>):FList<ITEM>
= append(list)
//** drop----------------------------------------------------------*/
tailrec fun <ITEM:Any> FList<ITEM>.drop(n:Int = 1): FList<ITEM>
= if(n > 0 && this is Cons) tail.drop(n - 1) else this
tailrec fun <ITEM:Any> FList<ITEM>.dropWhile(block:(ITEM)->Boolean): FList<ITEM>
= if(this is Cons && block(head)) tail.dropWhile(block) else this
@PublishedApi internal tailrec fun <ITEM:Any> FList<ITEM>._dropWhileIndexed(index:Int, block:(Int, ITEM)->Boolean): FList<ITEM>
= if(this is Cons && block(index, head)) tail._dropWhileIndexed(index + 1, block) else this
inline fun <ITEM:Any> FList<ITEM>.dropWhileIndexed(noinline block:(Int, ITEM)->Boolean):FList<ITEM>
= _dropWhileIndexed(0, block)
//** dropLast----------------------------------------------------------*/
@PublishedApi internal tailrec fun <ITEM:Any> FList<ITEM>._dropLastWhileIndexed(index:Int, block:(Int, ITEM)->Boolean):FList<ITEM>
= when(this){
    is Cons -> if(!block(index, head)) reverse() else tail._dropLastWhileIndexed(index + 1, block)
    is Nil -> reverse()
}
inline fun <ITEM:Any> FList<ITEM>.dropLastWhileIndexed(noinline block:(Int, ITEM)->Boolean):FList<ITEM>
= reverse()._dropLastWhileIndexed(0, block)
inline fun <ITEM:Any> FList<ITEM>.dropLastWhile(noinline block:(ITEM)->Boolean):FList<ITEM>
= reverse()._dropLastWhileIndexed(0){_,it->block(it)}
fun <ITEM:Any> FList<ITEM>.dropLastOne():FList<ITEM>
= when(this){
    is Cons->if(tail is Nil) Nil else Cons(head, tail.dropLastOne())
    is Nil->this
}
inline fun <ITEM:Any> FList<ITEM>.dropLast(n:Int = 1):FList<ITEM>
= reverse()._dropLastWhileIndexed(0){index,_->index < n}
fun <ITEM:Any> FList<ITEM>.dropLast1(n:Int = 1):FList<ITEM>
= when(this){
    is Cons->if(n > 0) dropLastOne().dropLast(n - 1) else this
    is Nil->this
}
//** utils-----------------------------------------------------------------*/
fun <ITEM:Any> FList<ITEM>.filter(block:(ITEM)->Boolean): FList<ITEM>
= suspendableFold2(FList()){ it, acc->if(block(it)) Cons(it, acc) else acc}
fun <ITEM:Any> FList<ITEM>.filter1(block:(ITEM)->Boolean): FList<ITEM>
= flatMap{if(block(it)) FList(it) else Nil}
tailrec fun <ITEM:Any> FList<ITEM>.sliceFrom(item:ITEM): FList<ITEM>
= if(this is Cons && head != item) tail.sliceFrom(item) else this
tailrec fun <ITEM:Any> FList<ITEM>.slice(from:Int): FList<ITEM>
= if(this is Cons && from > 0) tail.slice(from - 1) else this
inline fun <ITEM:Any> FList<ITEM>.slice(from:Int, to:Int): FList<ITEM>
= slice(from).dropLast(to - from)
tailrec fun <ITEM:Any> FList<ITEM>.startsWith(target:FList<ITEM>):Boolean
= when(this) {
    is Cons ->{
        when(target){
            is Cons -> if(head == target.head) tail.startsWith(target.tail) else false
            is Nil -> true
        }
    }
    is Nil -> target is Nil
}
fun <ITEM:Any> FList<ITEM>.startsWith1(target:FList<ITEM>):Boolean
= this.fold(true to target){ (acc, other), it->
    when(other){
        is Cons -> if(acc && it == other.head) true to other.tail else false to FList()
        is Nil -> acc to other
    }
}.first
tailrec operator fun <ITEM:Any> FList<ITEM>.contains(target:ITEM):Boolean
= when(this) {
    is Cons -> if(head == target) true else target in tail
    is Nil -> false
}
tailrec operator fun <ITEM:Any> FList<ITEM>.contains(target: FList<ITEM>):Boolean
        = when(this) {
    is Cons -> when (target) {
        is Cons -> if(startsWith(target)) true else target in tail
        is Nil -> false
    }
    is Nil -> target is Nil
}
inline fun <ITEM:Any, OTHER:Any, RESULT:Any> FList<ITEM>.zipWith(other: FList<OTHER>, noinline block:(ITEM, OTHER)->RESULT): FList<RESULT>
= this.fold(FList<RESULT>() to other){ (acc, other), it->
    when(other){
        is Cons -> Cons(block(it, other.head), acc) to other.tail
        is Nil -> acc to other
    }
}.first.reverse()
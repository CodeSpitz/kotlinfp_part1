@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package kore.fd

import kore.fd.FStream.Cons
import kore.fd.FStream.Empty

fun <VALUE:Any> lazyIf(
    cond:()->Boolean,
    onTrue:()->VALUE,
    onFalse:()->VALUE
):()->VALUE
= {if(cond()) onTrue() else onFalse()}

fun maybeTwice2(b:Boolean, i:()->Int):Int{
    val j by lazy(i)
    return if(b) j + j else 0
}
fun maybeTwice3(b:Boolean, i:()->Int):Int{
    val j = i()
    return if(b) j + j else 0
}
sealed class FStream<out ITEM:Any> {
    data object Empty:FStream<Nothing>()
    class Cons<out ITEM:Any>@PublishedApi internal constructor(h:()->ITEM, t:()->FStream<ITEM>):FStream<ITEM>(){
        private var memoHead:@UnsafeVariance ITEM? = null
        val head:()->ITEM = {memoHead ?: h().also { memoHead = it }}
        private var memoTail:FStream<@UnsafeVariance ITEM>? = null
        val tail:()->FStream<ITEM> = {memoTail ?: t().also { memoTail = it }}
    }
    companion object{
        @PublishedApi internal val emptyTail:()->FStream<Nothing> = {Empty}
        inline operator fun <ITEM:Any> invoke(noinline head:()->ITEM):FStream<ITEM>
        = Cons(head, emptyTail)
        inline operator fun <ITEM:Any> invoke(noinline head:()->ITEM, noinline tail:()-> FStream<ITEM>):FStream<ITEM>
        = Cons(head, tail)
        inline operator fun <ITEM:Any> invoke(): FStream<ITEM> = Empty
        operator fun <ITEM:Any> invoke(vararg items:ITEM): FStream<ITEM>
//        = unfold(items to 0){(items, n)->
//            if(items.isEmpty() || items.size <= n) null else ({items[n]} to {items to n + 1})
//        }
        = invoke({items[0]}){invoke(*items.sliceArray(1..items.size))}
    }
}
//** base-----------------------------------------------------------------*/
fun <ITEM:Any, OTHER:Any> FStream<ITEM>.fold(
    empty:()->OTHER,
    cons:(head:()->ITEM, next:()->OTHER)->OTHER
):OTHER
= if(this is Cons) cons(head){tail().fold(empty, cons)} else empty()
fun <ITEM:Any, STATE:Any, OTHER:Any> FStream<ITEM>.foldState(
    state: STATE,
    nextState:(STATE)->STATE,
    empty:(state:STATE)->OTHER,
    cons:(head:()->ITEM, state:STATE, next:()->OTHER)->OTHER
):OTHER
= if(this is Cons) cons(head, state){tail().foldState(nextState(state), nextState, empty, cons)} else empty(state)
fun <ITEM:Any, STATE:Any> FStream.Companion.unfold(
    state:STATE,
    block:(STATE)->Pair<()->ITEM, ()->STATE>?
):FStream<ITEM>
= block(state)?.let{(item, nextState)->FStream(item){unfold(nextState(), block)}} ?: Empty
fun <ITEM:Any, STATE:Any> FStream.Companion.unfoldOption(
    state:STATE,
    block:(STATE)->FOption<Pair<()->ITEM, ()->STATE>>
):FStream<ITEM>
= block(state).map{(item, nextState)->FStream(item){unfoldOption(nextState(), block)}}.getOrElse{Empty}
//= when(val v = block(state)){
//    is FOption.Some -> v.value.let{(item, nextState)->
//        invoke(item){unfoldOption(nextState(), block)}
//    }
//    is FOption.None -> Empty
//}
//----------------------------------------------------------------------------
fun <ITEM:Any> FStream.Companion.constant(item:ITEM):FStream<ITEM> = FStream({item}){constant(item)}
fun <ITEM:Any> FStream.Companion.constant2(item:ITEM):FStream<ITEM> = unfoldOption(item){FOption({it} to {it})}
fun <ITEM:Any> FStream.Companion.constant3(item:ITEM):FStream<ITEM> = unfold(item){{it} to {it}}
fun FStream.Companion.increaseFrom(item:Int):FStream<Int> = FStream({item}){increaseFrom(item + 1)}
fun FStream.Companion.increaseFrom2(item:Int):FStream<Int> = unfoldOption(item){FOption({it} to {it + 1})}
fun FStream.Companion.increaseFrom3(item:Int):FStream<Int> = unfold(item){{it} to {it + 1}}
private fun _fib(prevprev:Int, prev:Int):FStream<Int> = FStream({prevprev + prev}){ _fib(prev, prevprev + prev)}
fun FStream.Companion.fib():FStream<Int> = FStream({0}){FStream({1}){ _fib(0, 1)}}
fun FStream.Companion.fib2():FStream<Int> = FStream({0}){FStream({1}){unfoldOption(0 to 1){ (prevprev, prev)->FOption({prevprev + prev} to {prev to prevprev + prev})}}}
fun FStream.Companion.fib3():FStream<Int> = FStream({0}){FStream({1}){unfold(0 to 1){(prevprev, prev)->{prevprev + prev} to {prev to prevprev + prev}}}}
//----------------------------------------------------------------------------
fun <ITEM:Any> FStream<ITEM>.headOption():FOption<ITEM>
= fold({FOption()}){it, _->FOption(it())}
//= if(this is Cons) FOption(head()) else FOption()
fun <ITEM:Any, OTHER:Any> FStream<ITEM>.map(block:(ITEM)->OTHER):FStream<OTHER>
= fold({FStream()}){head, next-> FStream({block(head())}, next)}
fun <ITEM:Any, OTHER:Any> FStream<ITEM>.mapUnfold(block:(ITEM)->OTHER):FStream<OTHER>
= FStream.unfold(this){if(it is Cons) Pair({block(it.head())}, it.tail) else null}
fun <ITEM:Any, OTHER:Any> FStream<ITEM>.mapUnfoldOption(block:(ITEM)->OTHER):FStream<OTHER>
= FStream.unfoldOption(this){
    when(it){
        is Cons -> FOption({block(it.head())} to it.tail)
        is Empty -> FOption()
    }
}
inline fun <ITEM:Any, OTHER:Any> FStream<ITEM>.flatMap(noinline block:(ITEM)->FStream<OTHER>):FStream<OTHER>
= fold({FStream()}){head, next->block(head()).append(next)}
fun <ITEM:Any> FStream<ITEM>.filter(block:(ITEM)->Boolean):FStream<ITEM>
= fold({FStream()}){head, next->if(block(head())) FStream(head, next) else next()}
//** append-----------------------------------------------------------------*/
fun <ITEM:Any> FStream<ITEM>.append(other:()->FStream<ITEM>):FStream<ITEM>
= fold(other){head, next-> FStream(head, next)}
fun <ITEM:Any> FStream<ITEM>.appendUnfold(other:()->FStream<ITEM>):FStream<ITEM>
= FStream.unfold(this to other){(origin, other)->
    when(origin){
        is Cons ->origin.head to {origin.tail() to other}
        is Empty -> when(val v = other()){
            is Cons -> v.head to {origin to v.tail}
            is Empty -> null
        }
    }
}
inline operator fun <ITEM:Any> FStream<ITEM>.plus(stream:FStream<ITEM>):FStream<ITEM> = append{stream}
//** ----------------------------------------
fun <ITEM:Any> FStream<ITEM>.toList():List<ITEM>
= fold({listOf<ITEM>()}){head, next->next() + head()}.reversed()
fun <ITEM:Any> FStream<ITEM>.toFList():FList<ITEM>
= fold({FList()}){head, next->FList.Cons(head(), next())}
fun <ITEM:Any> FStream<ITEM>.take(n:Int):FStream<ITEM>
= if(this is Cons && n > 0) FStream(head){tail().take(n - 1)} else FStream()
fun <ITEM:Any> FStream<ITEM>.takeFold(n:Int):FStream<ITEM>
= foldState(
    n,
    {it - 1},
    {FStream()}
){head, state, next ->
    if(state > 0) FStream(head, next) else FStream()
}
fun <ITEM:Any> FStream<ITEM>.takeUnfold(n:Int):FStream<ITEM>
= FStream.unfold(this to n){(stream, n)->
    if(stream is Cons && n > 0) stream.head to {stream.tail() to n - 1} else null
}
fun <ITEM:Any> FStream<ITEM>.takeWhile(block:(ITEM)->Boolean):FStream<ITEM>
= fold({FStream()}){head, next->if(block(head())) FStream(head, next) else FStream()}
fun <ITEM:Any> FStream<ITEM>.takeWhileUnfold(block:(ITEM)->Boolean): FStream<ITEM>
= FStream.unfold(this){if(it is Cons && block(it.head())) it.head to it.tail else null}
fun <ITEM:Any, OTHER:Any, RESULT:Any> FStream<ITEM>.zipWith(other:FStream<OTHER>, block:(ITEM, OTHER)->RESULT):FStream<RESULT>
= FStream.unfold(this to other){(origin, other)->
    if(origin is Cons && other is Cons) Pair({block(origin.head(), other.head())}){origin.tail() to other.tail()}
    else null
}
fun <ITEM:Any, OTHER:Any, RESULT:Any> FStream<ITEM>.zipWithFold(other:FStream<OTHER>, block:(ITEM, OTHER)->RESULT):FStream<RESULT>
= foldState(
    other,
    {
        if(it is Cons) it.tail() else FStream()
    },
    {FStream()}
){head, state, next ->
    if(state is Cons) FStream({block(head(), state.head())}, next) else FStream()
}
fun <ITEM:Any, OTHER:Any> FStream<ITEM>.zipAll(other:FStream<OTHER>):FStream<Pair<FOption<ITEM>, FOption<OTHER>>>
= FStream.unfold(this to other){(origin, other)->
    if(origin is Empty && other is Empty) null
    else Pair(
        {Pair(
            if(origin is Cons) FOption(origin.head()) else FOption(),
            if(other is Cons) FOption(other.head()) else FOption()
        )},
        {Pair(
            if(origin is Cons) origin.tail() else FStream(),
            if(other is Cons) other.tail() else FStream()
        )}
    )
}
tailrec fun <ITEM:Any> FStream<ITEM>.startsWith(target:FStream<ITEM>):Boolean = when(this){
    is Cons -> when(target){
        is Cons -> if(head() == target.head()) tail().startsWith(target.tail()) else false
        is Empty -> true
    }
    is Empty -> target is Empty
}
fun <ITEM:Any> FStream<ITEM>.startsWith2(target:FStream<ITEM>):Boolean
= zipAll(target).takeWhile{(a, b)->
    a is FOption.Some && b is FOption.Some
}.all{it.first == it.second}
tailrec fun <ITEM:Any> FStream<ITEM>.drop(n:Int):FStream<ITEM>
= if(this is Cons && n > 0) tail().drop(n - 1) else this
fun <ITEM:Any> FStream<ITEM>.dropFold(n:Int):FStream<ITEM>
= foldState(n, {it - 1}, {FStream()}){head, state, next ->
    if(state <= 0) FStream(head, next) else next()
}
tailrec operator fun <ITEM:Any> FStream<ITEM>.contains(block:(ITEM)->Boolean):Boolean = when(this){
    is Empty -> false
    is Cons->if(block(head())) true else tail().contains(block)
}
tailrec operator fun <ITEM:Any> FStream<ITEM>.contains(item:ITEM):Boolean
= when(this){
    is Empty -> false
    is Cons->if(item == head()) true else tail().contains(item)
}
fun <ITEM:Any> FStream<ITEM>.any(block:(ITEM)->Boolean):Boolean
= fold({false}){it, acc->block(it()) || acc()}
fun <ITEM:Any> FStream<ITEM>.all(block:(ITEM)->Boolean):Boolean
= fold({true}){it, acc->block(it()) && acc()}
fun <ITEM:Any> FStream<ITEM>.tails():FStream<FStream<ITEM>>
= FStream.unfold(this){if(it is Cons) Pair({it}, it.tail) else null} + FStream{FStream()}
fun <ITEM:Any> FStream<ITEM>.tailsFold():FStream<FStream<ITEM>>
= foldState(
    {if(this is Cons) tail() else Empty},
    {{
        it().let{if(it is Cons) it.tail() else FStream()}
    }},
    {FStream{FStream()}}
){head, state, next ->
    FStream({FStream(head, state)}, next)
}
fun <ITEM:Any> FStream<ITEM>.tailsScan():FStream<FStream<ITEM>>
= scan(FStream()){ it, acc->FStream(it, acc)}
fun <ITEM:Any, ACC:Any> FStream<ITEM>.scan(acc:ACC, block:(()->ITEM, ()->ACC)->ACC):FStream<ACC>
= FStream.unfold(this){
    if(it is Cons) Pair({it.fold({acc}){head, next->block(head, next)}}, it.tail)
    else null
} + FStream{acc}
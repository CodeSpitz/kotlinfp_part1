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
inline val <ITEM:Any> FList<ITEM>.size1:Int get() = this.foldRight(0){_, acc->acc + 1}
@PublishedApi internal tailrec fun <ITEM:Any> FList<ITEM>._size(acc:Int):Int
= when(this) {
    is Cons -> tail._size(acc + 1)
    is Nil -> acc
}
inline fun <ITEM:Any> FList<ITEM>.toList():List<ITEM>
= fold(listOf()){acc, it->acc + it}
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
= fold(base to 0){(acc, index), it->block(index, acc, it) to index + 1}.first
inline fun <ITEM:Any> FList<ITEM>.reverse(): FList<ITEM>
= fold(FList()){acc, it->Cons(it, acc)}
fun <ITEM:Any, ACC:Any> FList<ITEM>.fold1(base:ACC, block:(ACC, ITEM)->ACC):ACC
= foldRight({it:ACC->it}){item, acc->{acc(block(it, item))}}(base)
tailrec fun <ITEM:Any, ACC:Any> FList<ITEM>._foldRight(base:ACC, origin:(ITEM, ACC)->ACC, block:(ACC)->ACC):ACC
= when(this){
    is Cons -> when(tail) {
        is Cons -> tail._foldRight(base, origin){acc ->block(origin(head, acc))}
        is Nil -> block(origin(head, base))
    }
    is Nil -> base
}
fun <ITEM:Any, ACC:Any> FList<ITEM>.foldRight3(base:ACC, block:(ITEM, ACC)->ACC):ACC
= fold({it:ACC->it}){acc, item->{acc(block(item, it))}}(base)
fun <ITEM:Any, ACC:Any> FList<ITEM>.foldRight2(base:ACC, block:(ITEM, ACC)->ACC):ACC
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
    is Cons -> block(head, tail.foldRight(base, block))
    is Nil -> base
}
inline fun <ITEM:Any, ACC:Any> FList<ITEM>.foldRight(base:ACC, crossinline block:(ITEM, ACC)->ACC):ACC
= reverse().fold(base){ acc, it->block(it, acc)}
fun <ITEM:Any, ACC:Any> FList<ITEM>.foldRightIndexed(base:ACC, block:(Int, ITEM, ACC)->ACC):ACC
= reverse().foldIndexed(base){index, acc, it->block(index, it, acc)}
inline fun <ITEM:Any, OTHER:Any> FList<ITEM>.map(crossinline block:(ITEM)->OTHER): FList<OTHER>
= foldRight(FList()){ it, acc->Cons(block(it), acc)}
inline fun <ITEM:Any, OTHER:Any> FList<ITEM>.flatMap(noinline block:(ITEM)-> FList<OTHER>):FList<OTHER>
= foldRight(FList()){it, acc->
    when(val v = block(it)){
        is Cons ->v.foldRight(acc, ::Cons)
        is Nil ->acc
    }
}
fun <ITEM:Any, OTHER:Any> FList<ITEM>.flatMap2(block:(ITEM)-> FList<OTHER>):FList<OTHER>
= map(block).flatten()
fun FList<Double>.toStringList():FList<String> = map{"$it"}
fun FList<Int>.increase():FList<Int> = map{it + 1}
fun <ITEM:Any> FList<FList<ITEM>>.flatten1():FList<ITEM> = when(this){
    is Cons -> drop(1).fold(head){acc, it->acc.append(it)}
    is Nil -> this
}

fun <ITEM:Any> FList<FList<ITEM>>.flatten(): FList<ITEM>
= foldRight(FList()){it, acc->it.foldRight(acc, ::Cons)}
//** append-----------------------------------------------------------------*/
fun <ITEM:Any> FList<ITEM>.append1(list: FList<ITEM> = FList()): FList<ITEM>
= when(this){
    is Cons -> Cons(head, tail.append1(list))
    is Nil -> list
}
inline fun <ITEM:Any> FList<ITEM>.append(list: FList<ITEM> = FList()): FList<ITEM>
= foldRight(list, ::Cons)
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
= foldRight(FList()){it, acc->if(block(it)) Cons(it, acc) else acc}
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
= fold(true to target){(acc, other), it->
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
= fold(FList<RESULT>() to other){(acc, other), it->
    when(other){
        is Cons -> Cons(block(it, other.head), acc) to other.tail
        is Nil -> acc to other
    }
}.first.reverse()
//---------------------------------------------------------------------------------------------
package fd

import kore.fd.*
import kore.fd.FList
import org.junit.Test
import kotlin.test.assertEquals

class ListTest{
    @Test
    fun test1(){
        val list = FList(1, 2, 3)
        val nil = FList<Int>()
        assertEquals(list.size, 3)
        assertEquals(nil.size, 0)
        assertEquals(list.toList(), listOf(1, 2, 3))
        assertEquals(list.setHead(5).toString(), "Cons(head=5, tail=Cons(head=2, tail=Cons(head=3, tail=Nil)))")
        assertEquals(nil.setHead(5).toString(), "Nil")
        assertEquals(list.addFirst(4).toString(), "Cons(head=4, tail=Cons(head=1, tail=Cons(head=2, tail=Cons(head=3, tail=Nil))))")
        assertEquals(nil.addFirst(4).toString(), "Nil")
        assertEquals(list.fold(""){acc,it->acc + it}, "123")
        assertEquals(list.fold1(""){acc,it->acc + it}, "123")
        assertEquals(list.foldIndexed(""){index, acc,it->"$acc$index$it"}, "011223")
        assertEquals(list.foldRight1("@") { it, acc -> acc + it }, "@321")
        assertEquals(list.foldRight1(1.0) { it, acc -> acc * it }, 6.0)
        assertEquals(list.foldRight2("@") { it, acc -> acc + it }, "@321")
        assertEquals(list.foldRight2(1.0) { it, acc -> acc * it }, 6.0)
        assertEquals(FList(1,2,0,3,4).foldRightWhile(0,{it != 0}) { it, acc -> acc + it }, 3)
        assertEquals(list.foldRight("@") { it, acc -> acc + it }, "@321")
        assertEquals(list.foldRight(1.0) { it, acc -> acc * it }, 6.0)
        assertEquals(list.foldRight3("@") { it, acc -> acc + it }, "@321")
        assertEquals(list.foldRight3(1.0) { it, acc -> acc * it }, 6.0)
        assertEquals(list.foldRightIndexed(""){index, it, acc->"$acc$index$it"}, "031221")
        assertEquals(list.reverse().toList(), listOf(3,2,1))
        assertEquals(FList(1,2,3).reverse().reverse().toList(), listOf(1,2,3))
        assertEquals(list.map{"${it*2}"}.fold(""){acc, it->"$acc$it"}, "246")
        assertEquals(list.flatMap{ if(it != 2) FList(it) else FList() }.toList(), listOf(1,3))
        assertEquals(FList(FList(1,2), FList(3,4)).flatten().toList(), listOf(1,2,3,4))
        assertEquals(list.append1().toList(), listOf(1,2,3))
        assertEquals(list.append1(FList(4, 5)).toList(), listOf(1,2,3,4,5))
        assertEquals(list.append().toList(), listOf(1,2,3))
        assertEquals(list.append(FList(4, 5)).toList(), listOf(1,2,3,4,5))
        assertEquals((list + FList.Nil).toList(), listOf(1,2,3))
        assertEquals((list + FList(4,5)).toList(), listOf(1,2,3,4,5))
        assertEquals(list.copy().toList(), listOf(1,2,3))
        assertEquals(list.drop(2).toString(), "Cons(head=3, tail=Nil)")
        assertEquals(list.dropWhile { it < 2 }.toString(), "Cons(head=2, tail=Cons(head=3, tail=Nil))")
        assertEquals(list.dropWhileIndexed { index, it -> index < 1 }.toString(), "Cons(head=2, tail=Cons(head=3, tail=Nil))")
        assertEquals(list.dropLast().toList(), listOf(1,2))
        assertEquals(list.dropLast(2).toList(), listOf(1))
        assertEquals(list.dropLastWhile { it > 2 }.toList(), listOf(1,2))
        assertEquals(nil.dropLast().toList(), listOf())
        assertEquals(nil.dropLast(2).toList(), listOf())
        assertEquals(nil.dropLastWhile { it > 2 }.toList(), listOf())
        assertEquals(list.dropLastWhileIndexed { index, it ->index < 1}.toList(), listOf(1, 2))
        assertEquals(list.filter{it != 2}.toList(), listOf(1,3))
        assertEquals(FList(1, 2, 3, 4).filter{it % 2 == 0}.toList(), listOf(2,4))
        assertEquals(FList(1, 2, 3, 4).filter1{it % 2 == 0}.toList(), listOf(2,4))
        assertEquals(FList(1, 2, 3, 4).sliceFrom(3).toList(), listOf(3,4))
        assertEquals(FList(1, 2, 3, 4).slice(1).toList(), listOf(2,3,4))
        assertEquals(FList(1, 2, 3, 4).slice(1,2).toList(), listOf(2,3))
        assertEquals(nil.sliceFrom(3).toList(), listOf())
        assertEquals(nil.slice(1).toList(), listOf())
        assertEquals(nil.slice(1,2).toList(), listOf())
        assertEquals(list.startsWith(FList(2,3)), false)
        assertEquals(list.startsWith(FList(1,2,3)), true)
        assertEquals(list.startsWith(FList(1,3)), false)
        assertEquals(FList(1,2,3).startsWith(FList(1,2)), true)
        assertEquals(list.startsWith1(FList(2,3)), false)
        assertEquals(list.startsWith1(FList(1,2,3)), true)
        assertEquals(list.startsWith1(FList(1,3)), false)
        assertEquals(FList(1,2,3).startsWith1(FList(1,2)), true)
        assertEquals(list.startsWith(FList()), true)
        assertEquals(nil.startsWith(FList()), true)
        assertEquals(nil.startsWith(FList(1,2)), false)
        assertEquals(FList(1,2) in list, true)
        assertEquals(FList(2,3) in list, true)
        assertEquals(FList(1,2,3) in list, true)
        assertEquals(FList(1,3) in list, false)
        assertEquals(FList() in list, false)
        assertEquals(FList(1,3) in nil, false)
        assertEquals(FList() in nil, true)
        assertEquals(1 in list, true)
        assertEquals(2 in list, true)
        assertEquals(3 in list, true)
        assertEquals(4 in list, false)
        assertEquals(1 in nil, false)
        assertEquals(FList(1, 2, 3).zipWith(FList(1,1,1)){ a, b->a + b}.toList(), listOf(2,3,4))
        assertEquals(FList(1, 2).zipWith(FList(1,1,1)){ a, b->a + b}.toList(), listOf(2,3))
        assertEquals(FList(1, 2, 3).zipWith(FList(1,1)){ a, b->a + b}.toList(), listOf(2,3))
        assertEquals(FList(1.0,2.0,3.0).average().getOrElse { 0.0 }, 2.0)
        listOf(1.0).average()

    }

}
//--------------------------------------------------------------------------------------------
package kore.fd

import kore.fd.FTree.Branch
import kore.fd.FTree.Leaf

sealed class FTree<out ITEM:Any> {
    companion object{
        operator fun <ITEM:Any> invoke(left: FTree<ITEM>, right: FTree<ITEM>): FTree<ITEM> = Branch(left, right)
        operator fun <ITEM:Any> invoke(value:ITEM): FTree<ITEM> = Leaf(value)
    }
    data class Leaf<ITEM:Any>@PublishedApi internal constructor(@PublishedApi internal val item:ITEM): FTree<ITEM>()
    data class Branch<ITEM:Any>@PublishedApi internal constructor(@PublishedApi internal val left: FTree<ITEM>, @PublishedApi internal val right: FTree<ITEM>): FTree<ITEM>()

}
val <ITEM:Any> FTree<ITEM>.size:Int get() = fold({1}){ l, r->1 + l + r}
val <ITEM:Any> FTree<ITEM>.depth:Int get() = fold({1}){ l, r->1 + if(l > r) l else r}
val <ITEM> FTree<ITEM>.max:ITEM where ITEM:Comparable<ITEM>, ITEM:Number get() = fold({it}){ l, r->if(l > r) l else r}
fun <ITEM:Any> FTree<ITEM>.setLeft(tree: FTree<ITEM>): FTree<ITEM> = when(this){
    is Leaf -> Branch(tree, this)
    is Branch -> Branch(tree, right)
}
fun <ITEM:Any> FTree<ITEM>.setRight(tree: FTree<ITEM>): FTree<ITEM> = when(this){
    is Leaf -> Branch(this, tree)
    is Branch -> Branch(left, tree)
}
fun <ITEM:Any, OTHER:Any> FTree<ITEM>.fold(leafBlock:(ITEM)->OTHER, branchBlock:(l:OTHER, r:OTHER)->OTHER):OTHER = when(this){
    is Leaf -> leafBlock(item)
    is Branch -> branchBlock(left.fold(leafBlock, branchBlock), right.fold(leafBlock, branchBlock))
}
fun <ITEM:Any, OTHER:Any> FTree<ITEM>.map(block:(ITEM)->OTHER): FTree<OTHER> = fold({FTree(block(it))}){ l, r->FTree(l, r)}
operator fun <ITEM:Any> FTree<ITEM>.contains(item:ITEM):Boolean = fold({it == item}){ l, r-> l || r}
//--------------------------------------------------------------------------------------------
package fd

import kore.fd.*
import kotlin.test.Test
import kotlin.test.assertEquals
class TreeTest {
    @Test
    fun test1(){
        val tree2 = FTree(FTree(FTree(1), FTree(FTree(3), FTree(4))), FTree(5))
        assertEquals(tree2.depth, 4)
        assertEquals(tree2.size, 7)
        assertEquals(tree2.max, 5)
        assertEquals("${tree2.map{it*2}}", "Branch(left=Branch(left=Leaf(item=2), right=Branch(left=Leaf(item=6), right=Leaf(item=8))), right=Leaf(item=10))")
        assertEquals(1 in tree2, true)
        assertEquals(3 in tree2, true)
        assertEquals(4 in tree2, true)
        assertEquals(5 in tree2, true)
        assertEquals(2 in tree2, false)
        assertEquals( 5 in tree2, true)
    }
}

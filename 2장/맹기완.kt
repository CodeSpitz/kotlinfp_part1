package kotlinFP

import kotlin.test.Test
import kotlin.test.assertEquals

class a {
    fun test2(){
        assertEquals(fib(0), 0)
        assertEquals(fib(1), 1)
        assertEquals(fib(2), 1)
        assertEquals(fib(3), 2)
        assertEquals(fib(4), 3)
        assertEquals(fib(5), 5)
    }
    @Test
    fun testIsSorted(){
        assertEquals(arrayListOf(1,2,3,4).isSorted(Int::less), true)
    }
    @Test
    fun testCurry(){
        val c1 = curry{a:Int, b:Int->Int
            a + b
        }
        assertEquals(c1(2)(5), 7)
        assertEquals(c1(1)(3), 4)
        val c11 = uncurry(c1)
        assertEquals(c11(2,5), 7)
        val block = {a:Int, b:Int->Int
            a + b
        }
        assertEquals(block.curry(2)(5), 7)
        assertEquals(block.curry(1)(3), 4)
    }
    @Test
    fun testCompose(){
        val f1 = compose({it:Int->it*2}, {it:Int->it+5})
        val f2 = {it:Int->it+5} + { it*2 }
        assertEquals(f1(2), 14)
        assertEquals(f2(2), 14)
    }
}
fun <P1, P2, RETURN> uncurry(block:(P1)->(P2)->RETURN):(P1, P2)->RETURN = {p1, p2->block(p1)(p2)}
fun <P, RESULT, RETURN> compose(result:(RESULT)->RETURN, block:(P)->RESULT):(P)->RETURN = {p->result(block(p))}
operator fun <P, RESULT, RETURN> ((P)->RESULT).plus(block:(RESULT)->RETURN):(P)->RETURN = {p->block(this(p))}


fun <P1, P2, RETURN> curry(block:(P1, P2)->RETURN):(P1)->((P2)->RETURN) = {p1->{p2->block(p1, p2)}}
fun <P1, P2, RETURN> ((P1, P2)->RETURN).curry(p1:P1):(P2)->RETURN = {p2->this(p1, p2)}
fun  ((Int, Int)->Int).curry(p1:Int):(Int)->Int = {p2->
    println("aa")
    this(p1, p2)
}

//fun fib(i: Int): Int {
//    tailrec fun go(offset: Int, a: Int, b: Int): Int =
//            if (offset < i) go(offset + 1, b, a + b) else a + b
//    return if (i <= 1) i else go(2, 0, 1)
//}
internal tailrec fun _fib(curr:Int, limit:Int, pprev:Int, prev:Int):Int = if(curr == limit) pprev + prev else _fib(curr + 1, limit, prev, pprev + prev)
fun fib(i:Int): Int = when(i) {
    in 0..1 -> i
    else -> _fib(2, i, 0, 1)
}

inline val <ITEM:Any> List<ITEM>.head: ITEM get() = first()
inline val <ITEM:Any> List<ITEM>.tail: List<ITEM> get() = drop(1)

inline fun <NUM:Comparable<NUM>> NUM.above(b:NUM):Boolean = this > b
inline fun <NUM:Comparable<NUM>> NUM.more(b:NUM):Boolean = this >= b
inline fun <NUM:Comparable<NUM>> NUM.under(b:NUM):Boolean = this < b
inline fun <NUM:Comparable<NUM>> NUM.less(b:NUM):Boolean = this <= b
internal tailrec fun <ITEM:Any> _isSorted(head:ITEM, tail:List<ITEM>, check:ITEM.(ITEM)->Boolean):Boolean{
    val tailHead = tail.head
    val tailTail = tail.tail

    return when{
        !check(head, tailHead)-> false
        tailTail.isEmpty()-> true
        else->_isSorted(tailHead, tailTail, check)
    }

}
fun <ITEM:Any> List<ITEM>.isSorted(check:ITEM.(ITEM)->Boolean):Boolean = when(size){
    in 0..1->true
    else->_isSorted(head, tail, check)
}

package fd

import kore.fd.*
import kotlin.test.Test
import kotlin.test.assertEquals

class StreamTest {
    @Test
    fun test0(){
        val f = lazyIf({true}, {3}, {5})
        assertEquals(f(), 3)
    }
    @Test
    fun test1(){
        assertEquals(FStream(1,2,3,4).headOption().getOrElse { 10 }, 1)
        assertEquals(FStream({1}, { FStream() }).toFList().toString(), "Cons(head=1, tail=Nil)")
        assertEquals(FStream(1,2,3,4).toFList().toString(), "Cons(head=1, tail=Cons(head=2, tail=Cons(head=3, tail=Cons(head=4, tail=Nil))))")
        assertEquals(FStream(1,2,3,4).take(2).toFList().toString(), "Cons(head=1, tail=Cons(head=2, tail=Nil))")
        assertEquals(FStream(1,2,3,4).drop(2).toFList().toString(), "Cons(head=3, tail=Cons(head=4, tail=Nil))")
        assertEquals(FStream(1,2,3,4).dropFold(2).toFList().toString(), "Cons(head=3, tail=Cons(head=4, tail=Nil))")
        assertEquals(FStream(1,2,3,4).toList(), listOf(1,2,3,4))
        assertEquals(FStream(1,2,3,4).take(2).toList(), listOf(1,2))
        assertEquals(FStream(1,2,3,4).drop(2).toList(), listOf(3,4))

        assertEquals(FStream(1,2,3,4).takeWhile{it<3}.toList(), listOf(1,2))

        assertEquals(FStream(1,2,3,4).takeWhileUnfold{it<3}.toList(), listOf(1,2))

        assertEquals(FStream(1,2,3,4).any{it == 2}, true)
        assertEquals(FStream(1,2,3,4).any{it == 5}, false)
        assertEquals(FStream(1,2,3,4).all{it < 5}, true)
        assertEquals(FStream(1,2,3,4).all{it < 4}, false)

        assertEquals(FStream(1,2,3,4).map{"-$it"}.toList(), listOf("-1", "-2", "-3", "-4"))
        assertEquals(FStream(1,2,3,4).mapUnfold{"-$it"}.toList(), listOf("-1", "-2", "-3", "-4"))
        assertEquals(FStream(1,2,3,4).mapUnfoldOption{"-$it"}.toList(), listOf("-1", "-2", "-3", "-4"))
        assertEquals(FStream(1,2,3,4).filter{it<3}.toList(), listOf(1,2))
        assertEquals(FStream(1,2).append{ FStream(3,4) }.toList(), listOf(1,2,3,4))
        assertEquals(FStream(1,2).appendUnfold{ FStream(3,4) }.toList(), listOf(1,2,3,4))
        assertEquals((FStream(1,2) + FStream(3,4)).toList(), listOf(1,2,3,4))
        assertEquals(FStream(1,2,3,4).flatMap{FStream("-$it")}.toList(), listOf("-1", "-2", "-3", "-4"))
        assertEquals(FStream(1,2,3,4).flatMap{if(it< 3) FStream("-$it") else FStream()}.toList(), listOf("-1", "-2"))
        assertEquals(FStream(1,2,3,4).flatMap{if(it % 2 == 0) FStream("-$it") else FStream()}.toList(), listOf("-2", "-4"))


        assertEquals({it == 1} in FStream(1,2), true)
        assertEquals({it == 5} in FStream(1,2), false)
        assertEquals(2 in FStream(1,2), true)
        assertEquals(7 in FStream(1,2), false)
        assertEquals(FStream.constant(1).take(2).map { it * 2 }.toList(), listOf(2,2))
        assertEquals(FStream.increaseFrom(1).take(3).toList(), listOf(1,2,3))
        assertEquals(FStream.fib().take(7).toList(), listOf(0,1,1,2,3,5,8))
        assertEquals(FStream.unfoldOption(1){FOption({ it * 2 } to { it + 1 })}.take(3).toList(), listOf(2,4,6))
        assertEquals(FStream.increaseFrom2(1).take(3).toList(), listOf(1,2,3))
        assertEquals(FStream.constant2(1).take(2).map { it * 2 }.toList(), listOf(2,2))
        assertEquals(FStream.fib2().take(7).toList(), listOf(0,1,1,2,3,5,8))
        assertEquals(FStream.increaseFrom3(1).take(3).toList(), listOf(1,2,3))
        assertEquals(FStream.constant3(1).take(2).map { it * 2 }.toList(), listOf(2,2))
        assertEquals(FStream.fib3().take(7).toList(), listOf(0,1,1,2,3,5,8))

        println("-------2")
        assertEquals(FStream.fib2().takeFold(7).toList(), listOf(0,1,1,2,3,5,8))
        assertEquals(FStream.fib2().takeUnfold(7).toList(), listOf(0,1,1,2,3,5,8))
        println("-------3")
        assertEquals(FStream(1,2,3,4).takeWhileUnfold{it<3}.toList(), listOf(1,2))

        assertEquals(FStream(1,2,3,4).zipWith(FStream("1","2")){a,b->
            "$a$b"
        }.toList(), listOf("11","22"))
        assertEquals(FStream(1,2,3,4).zipWithFold(FStream("1","2")){a,b->
            "$a$b"
        }.toList(), listOf("11","22"))
        assertEquals(FStream(1,2,3,4).zipAll(FStream("1","2")).toList().map {(a,b)->
            a.getOrElse {0} to b.getOrElse { "0" }
        }, listOf(1 to "1",2 to "2", 3 to "0", 4 to "0"))
        assertEquals(FStream(1,2,3,4).startsWith(FStream(1,2,3)),true)
        assertEquals(FStream(1,2,3,4).startsWith2(FStream(1,2,3)),true)
        assertEquals(FStream(1,2,3).tails().toList().map{it.toList()}, listOf(listOf(1,2,3), listOf(2,3), listOf(3), listOf()))
        assertEquals(FStream(1,2,3).tailsFold().toList().map{it.toList()}, listOf(listOf(1,2,3), listOf(2,3), listOf(3), listOf()))
        assertEquals(FStream(1,2,3).tailsScan().toList().map{it.toList()}, listOf(listOf(1,2,3), listOf(2,3), listOf(3), listOf()))
        assertEquals(FStream(1,2,3).scan(0){ a, b->a()+b()}.toList(), listOf(6,5,3,0))
        assertEquals(FStream(1,2,3).scan(FStream()){ a, b->FStream(a, b)}.toList().map{it.toList()},
            listOf(listOf(1,2,3), listOf(2,3), listOf(3), listOf())
        )
    }
}
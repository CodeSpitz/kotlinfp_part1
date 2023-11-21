package fd

import kore.fd.*
import kotlin.test.Test
import kotlin.test.assertEquals

class RandTest {
    @Test
    fun test1(){
        val rand1 = IntRand(42)
        val (r1, rand2) = rand1()
        assertEquals(r1, 16159453)
        val (list, rand3) = rand1.intList(3)
        assertEquals(list.toList()[2], 16159453)
        assertEquals(list.size, 3)
        assertEquals(rand1.nonNegative().first > 0, true)
        val state = 10.toFGen()(rand1)
        assertEquals(state.first, 10)
        assertEquals(state.second().first, 16159453)
        val map = 10.toFGen().map { "$it" }(rand1)
        assertEquals(map.first, "10")
        val even = FGen.nonNegativeEven()(rand1)
        assertEquals(even.first, 16159452)
        val double = FGen.double()(rand1)
        assertEquals(double.first, 0.007524831689672932)
        assertEquals(list.size, 3)
        assertEquals(rand1.nonNegative().first > 0, true)
    }
    @Test
    fun test2(){
        val rand1 = IntRand(42)
        assertEquals( 54.toFGen().map { "${it+5}" }(rand1).first, "59")
        assertEquals( FGen { 54 to it }.map { "${it+5}" }(rand1), FGen {"59" to it}(rand1))
        assertEquals( 54.toFGen().mapF { "${it+5}" }(rand1).first, "59")
        assertEquals( FGen { 54 to it }.mapF { "${it+5}" }(rand1), FGen {"59" to it}(rand1))
        assertEquals(
            FList(FGen { it() }, FGen { it() }, FGen { it() }).sequence()(rand1),
            FGen {
                val (a, s1) = it()
                val (b, s2) = s1()
                val (c, s3) = s2()
                FList(a, b, c) to s3
            }(rand1)
        )
        assertEquals(
            FList(FGen { 1 to it }, FGen { 2 to it }, FGen { 3 to it }).sequenceFold()(rand1),
            FGen {
                FList(1, 2, 3) to it
            }(rand1)
        )
    }
    @Test
    fun test3(){
        val i1 = FState.run{
            val (x) = 41.toState<IntRandState, Int>()
            val (y) = "52".toState<IntRandState, String>()
            "$x$y"
        }
        assertEquals(i1(IntRandState(42)).first, "4152")
        val i2 = FState.run{
            val (x) = FState{a:Int->
                a to a + 1
            }
            val (y) = FState{a:Int->
                a * 2 to a + 1
            }
            "$x-$y"
        }
        assertEquals(i2(1), "1-4" to 3)
    }
    @Test
    fun test4(){
        val (v, machine) = simulator(FList(
            Input.Coin,
            Input.Turn,
            Input.Coin,
            Input.Turn
        ))(Input.Machine(true, 5, 0))
        val (coin, candy) = v
        assertEquals(coin, 2)
        assertEquals(candy, 3)
        assertEquals(machine, Input.Machine(true, 3, 2))
    }
}
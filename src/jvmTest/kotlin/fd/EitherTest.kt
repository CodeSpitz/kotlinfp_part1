//@file:Suppress("UNCHECKED_CAST")
//
//package fd
//
//import kore.fd.*
//import kore.fd.FEither.Left
//import kore.fd.FEither.Right
//import kotlin.test.Test
//import kotlin.test.assertEquals
//
//class EitherTest {
//    @Test
//    fun test1(){
//        assertEquals(FEither.r(3).orElse{ FEither.r(2)}, FEither.r(3))
//        assertEquals(FEither.l(3).orElse{ FEither.r(2)}, FEither.r(2))
//        assertEquals(FEither.l(3).map{5}, FEither.l(3))
//        assertEquals(FEither.r(3).map{5}, FEither.r(5))
//        assertEquals(FEither.l(3).flatMap{ FEither.r(5)}, FEither.l(3))
//        assertEquals(FEither.r(3).flatMap{ FEither.r(5)}, FEither.r(5))
//        assertEquals(FEither{
//            val (a) = FEither.r(5)
//            val (b) = FEither.r(6)
//            a + b
//        }, FEither.r(11))
//        assertEquals(FList(1, 2, 3).traverseEither{ FEither.r(it)}, FEither.r(FList(1, 2, 3)))
//        assertEquals(FList(FEither.r(1), FEither.r(2), FEither.r(3)).sequenceEither(), FEither.r(FList(1,2,3)))
//        assertEquals(FList(FEither.r(1), FEither.l(2), FEither.r(3)).sequenceEither(), FEither.left(2))
//        assertEquals(FEither.l(1).map2LeftToList<Int, Int, Int, Int>(FEither.l(2)){ a, b->3}, FEither.l(FList(1,2)))
//        assertEquals(FList(1, 2, 3).traverseEitherLog{ FEither.right(it)}, FEither.right(FList(1, 2, 3)))
//        assertEquals(FList(1, Left("2"), Left("3")).traverseEitherLog{
//            when(it){
//                is Left<*> -> Left(it.value)
//                is Right<*>-> Right(it.value)
//                else-> FEither.right(it)
//            } as FEither<String, Int>
//        }, FEither.left(FList("2","3")))
//        assertEquals(FList(Left("1"), 2, Left("3")).traverseEitherLog{
//            when(it){
//                is Left<*> -> Left(it.value)
//                is Right<*>-> Right(it.value)
//                else-> FEither.right(it)
//            } as FEither<String, Int>
//        }, FEither.left(FList("1","3")))
//        assertEquals(
//            FList(Left("1"), FEither.right(2), Left("3")).sequenceEitherLog(),
//            FEither.left(FList("1", "3"))
//        )
//        assertEquals(
//            FList(FEither.right("1"), Left("2"), Left("3")).sequenceEitherLog(),
//            FEither.left(FList("2", "3"))
//        )
//        assertEquals(
//            FList(FEither.r("1"), FEither.r("2"), FEither.r("3")).sequenceEitherLog(),
//            FEither.r(FList("1","2","3"))
//        )
//    }
//}
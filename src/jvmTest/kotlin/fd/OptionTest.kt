//package fd
//
//import kore.fd.*
//import kotlin.test.Test
//import kotlin.test.assertEquals
//class OptionTest {
//    @Test
//    fun test1(){
//        val option = FOption(3)
//        assertEquals(option.map{it *2}.getOrElse { 0 }, 6)
//        assertEquals(option.flatMap { FOption(it * 2) }.getOrElse { 0 }, 6)
//        assertEquals(option.filter { it > 1}.getOrElse { 0 }, 3)
//        assertEquals(listOf(1.0,2.0,3.0,4.0).variance().getOrElse { 0.0 }, 1.25)
//        assertEquals(option.map2(FOption(2)){ a, b->a + b}.getOrElse { 0 }, 5)
//        assertEquals(FList(FOption(1),FOption(3)).sequence().toString(), "Some(value=Cons(head=1, tail=Cons(head=3, tail=Nil)))")
//        assertEquals(FList(FOption(1), FOption(), FOption(3)).sequence().toString(), "None")
//        assertEquals(FList(FOption(1), FOption(), FOption(3)).sequenceT().toString(), "None")
//        assertEquals(FList(FOption(1),FOption(3)).sequenceT().toString(), "Some(value=Cons(head=1, tail=Cons(head=3, tail=Nil)))")
//        assertEquals(FOption {
//            val (a) = FOption(3)
//            val (b) = FOption(5)
//            a + b
//        }.getOrElse { 0 }, 8)
//    }
//}
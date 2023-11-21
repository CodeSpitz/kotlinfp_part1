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
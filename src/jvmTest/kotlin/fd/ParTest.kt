package fd

import kore.fd.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ParTest {
    @Test
    fun test1(){
        val par = Par{1}
        assertEquals(1, par.get)
    }
}
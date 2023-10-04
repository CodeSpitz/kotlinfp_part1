@file:Suppress("UNCHECKED_CAST")

package kotlinFP

import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

interface CreditCard
class Charge<PRODUCT:Product>(val cc:CreditCard, val qty:Int, val product: KClass<PRODUCT>)
class Receipt<PRODUCT:Product>(val qty:Int, val product: KClass<PRODUCT>)
interface Product{
    val price:Double
    val factory:()->Product
}
class Cafe{
    @PublishedApi internal val factories:HashMap<KClass<*>, ()->Product> = hashMapOf()
    fun addFactory(vararg products:Product){
        products.forEach {
            factories[it::class] = it.factory
        }
    }
    fun <PRODUCT:Product> getProduct(product:KClass<PRODUCT>):PRODUCT = factories[product]!!() as PRODUCT
    fun <PRODUCT: Product> buy(cc: CreditCard, qty:Int, product:KClass<PRODUCT>): Charge<PRODUCT>?
    = if(qty == 0) null else Charge(cc, qty, product)
    fun <PRODUCT: Product> payment(vararg charges: Charge<PRODUCT>):Receipt<PRODUCT>?{
        if(charges.isEmpty()) return null
        val totalQty = charges.sumOf { it.qty }
        //결제 부수효과
        val isPaidOK = true
        return if(isPaidOK) Receipt(totalQty, charges.first().product) else null
    }
    inline fun <reified PRODUCT: Product> receive(receipt: Receipt<PRODUCT>):Array<PRODUCT>?{
        factories[receipt.product] ?: return null
        return Array(receipt.qty){getProduct(receipt.product)}
    }
}
class Americano: Product {
    override val price:Double get() = 1000.0
    override val factory:()->Americano get() = ::Americano
}
class CafeLatte: Product{
    override val price:Double get() = 2000.0
    override val factory:()->CafeLatte get() = ::CafeLatte
}
class CafeTest{
    @Test
    fun test1(){
        val cafe: Cafe = Cafe().also {
            it.addFactory(Americano(), CafeLatte())
        }
        val myCard = object :CreditCard{}
        val americano = cafe.buy(myCard, 2, Americano::class)?.let{charge->
            cafe.payment(charge)?.let{receipt->
                cafe.receive(receipt)
            }
        }
        assertEquals(americano?.size, 2)
        assertEquals(americano?.get(0)?.price, 1000.0)
    }
}

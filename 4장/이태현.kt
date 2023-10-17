import org.junit.Assert
import kotlin.math.pow
import FList.Cons
import FList.Nil
import Option.Some
import Option.None
import Either.Left
import Either.Right

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
@PublishedApi internal tailrec fun <ITEM:Any> FList<ITEM>._size(acc:Int):Int
        = when(this) {
    is Cons -> tail._size(acc + 1)
    is Nil -> acc
}
tailrec fun <ITEM:Any, ACC:Any> FList<ITEM>.fold(acc:ACC, block:(ACC, ITEM)->ACC):ACC
        = when(this){
    is Cons -> tail.fold(block(acc, head), block)
    is Nil -> acc
}
inline fun FList<Double>.sum() = fold(
    0.0,
    { acc, item -> acc + item }
)
tailrec operator fun <ITEM:Any> FList<ITEM>.contains(target:ITEM):Boolean
        = when(this) {
    is Cons -> if(head == target) true else target in tail
    is Nil -> false
}
inline fun <ITEM:Any> FList<ITEM>.reverse(): FList<ITEM> = fold(FList()){acc, it->Cons(it, acc)}
inline fun <ITEM:Any, ACC:Any> FList<ITEM>.foldRight(base:ACC, crossinline block:(ITEM, ACC)->ACC):ACC =
    reverse().fold(base){ acc, it->block(it, acc)}
inline fun <ITEM:Any, OTHER:Any> FList<ITEM>.map(crossinline block:(ITEM)->OTHER): FList<OTHER> =
    foldRight(FList()){ it, acc->Cons(block(it), acc)}

sealed class Option<out ITEM:Any>{
    data object None: Option<Nothing>()
    data class Some<out ITEM:Any>@PublishedApi internal constructor(@PublishedApi internal val get:ITEM):
        Option<ITEM>()
    companion object{
        inline operator fun <ITEM:Any> invoke(): Option<ITEM> = None
        inline operator fun <ITEM:Any> invoke(item:ITEM): Option<ITEM> = Some(item)
    }
}

// 리스트 4.2
inline fun FList<Double>.mean(): Option<Double> =
    if (this.size == 0) None
    else Some(this.sum() / this.size)

// 연습문제 4.1
inline fun <ITEM:Any, OTHER:Any> Option<ITEM>.map(f: (ITEM) -> OTHER): Option<OTHER> =
    when (this) {
        is Some -> Some(f(get))
        is None -> None
    }
inline fun <ITEM:Any, OTHER:Any> Option<ITEM>.flatMap(f: (ITEM) -> Option<OTHER>): Option<OTHER> =
    when (this) {
        is Some -> f(get)
        is None -> None
    }
inline fun <ITEM:Any> Option<ITEM>.getOrElse(default: () -> ITEM): ITEM =
    when (this) {
        is Some -> get
        is None -> default()
    }
inline fun <ITEM:Any> Option<ITEM>.orElse(default: () -> Option<ITEM>): Option<ITEM> =
    when (this) {
        is Some -> this
        is None -> default()
    }
inline fun <ITEM:Any> Option<ITEM>.filter(f: (ITEM) -> Boolean): Option<ITEM> =
    when (this) {
        is Some -> if (f(get)) this else None
        is None -> this
    }

// 연습문제 4.2
inline fun FList<Double>.variance(): Option<Double> =
    if (this.size == 0) None
    else Some(this.fold(0.0, { acc, item -> acc + (item - this.mean().getOrElse { 0.0 }).pow(2) }) / this.size)

// 연습문제 4.3
inline fun <ITEM:Any, OTHER:Any, TARGET:Any> Option<ITEM>.map2(other: Option<OTHER>, f: (ITEM, OTHER) -> TARGET): Option<TARGET> =
    this.flatMap { item ->
        other.map { other ->
            f(item, other)
        }
    }

// 연습문제 4.4
//inline fun <ITEM:Any> FList<Option<ITEM>>.sequence(): Option<FList<ITEM>> =
//    this.foldRight(
//        Some(Nil),
//        { oa1: Option<ITEM>, oa2: Option<FList<ITEM>> ->
//            oa1.map2(oa2) { a1: ITEM, a2: FList<ITEM> ->
//                Cons(a1, a2)
//            }
//
//        }
//    )

// 연습문제 4.5
fun <ITEM:Any, OTHER:Any> FList<ITEM>.traverse(f: (ITEM) -> Option<OTHER>): Option<FList<OTHER>> =
    when (this) {
        is Nil -> Some(Nil)
        is Cons ->
            f(this.head).map2(this.tail.traverse(f)) { b, xb ->
                Cons(b, xb)
            }
    }
fun <ITEM:Any> FList<Option<ITEM>>.sequence(): Option<FList<ITEM>> = this.traverse { it }

// 리스트 4.5
sealed class Either<out ERR, out ITEM> {
    data class Left<out ERR>(val value: ERR): Either<ERR, Nothing>()
    data class Right<out ITEM>(val value: ITEM): Either<Nothing, ITEM>()

    companion object{
        inline operator fun <ERR:Error> invoke(err:ERR): Either<ERR, Nothing> = Left(err)
        inline operator fun <ITEM:Any> invoke(item:ITEM): Either<Nothing, ITEM> = Right(item)
    }
}

// 연습문제 4.6
fun <ERR, ITEM, OTHER> Either<ERR, ITEM>.map(f: (ITEM) -> OTHER): Either<ERR, OTHER> =
    when (this) {
        is Left -> this
        is Right -> Right(f(value))
    }

fun <ERR, ITEM, OTHER> Either<ERR, ITEM>.flatMap(f: (ITEM) -> Either<ERR, OTHER>): Either<ERR, OTHER> =
    when (this) {
        is Left -> this
        is Right -> f(value)
    }

fun <ERR, ITEM> Either<ERR, ITEM>.orElse(f: () -> Either<ERR, ITEM>): Either<ERR, ITEM> =
    when (this) {
        is Left -> f()
        is Right -> this
    }

fun <ERR, ITEM, OTHER, TARGET> Either<ERR, ITEM>.map2(
    other: Either<ERR, OTHER>,
    f: (ITEM, OTHER) -> TARGET
): Either<ERR, TARGET> =
    this.flatMap { item ->
        other.map { other ->
            f(item, other)
        }
    }

// 연습문제 4.7
//fun <ERR:Error, ITEM:Any, OTHER:Any> FList<ITEM>.traverse(
//    f: (ITEM) -> Either<ERR, OTHER>
//): Either<ERR, FList<OTHER>> =
//    when (this) {
//        is Nil -> Right(Nil)
//        is Cons ->
//            f(head).map2(tail.traverse(f)) { b, xb -> Cons(b, xb) }
//    }

//fun <ERR:Error, ITEM:Any> FList<Either<ERR, ITEM>>.sequence(): Either<ERR, FList<ITEM>> = this.traverse { it }

fun main(args: Array<String>) {
    println("Chapter 4 Hello World!")

    // 연습문제 4.1
    val practice4_1: Option<Int> = Option(1)
    Assert.assertEquals(practice4_1.map { it.toString() }, Option<String>("1"))
    Assert.assertEquals(practice4_1.map { it.toDouble() }, Option<Double>(1.0))
    Assert.assertEquals(practice4_1.flatMap { Option<String>(it.toString()) }, Option<String>("1"))
    Assert.assertEquals(practice4_1.flatMap { Option<Double>(it.toDouble()) }, Option<Double>(1.0))
    Assert.assertEquals(practice4_1.getOrElse { None }, 1)
    Assert.assertEquals(practice4_1.orElse { None }, Option<Int>(1))
    Assert.assertEquals(practice4_1.filter { it % 2 == 1 }, Option<Int>(1))
    Assert.assertEquals(practice4_1.filter { it % 2 == 0 }, None)

    // 연습문제 4.2
    val practice4_2: FList<Double> = FList(1.0, 2.0, 3.0)
    Assert.assertEquals(practice4_2.variance(), Option<Double>(2.0 / 3.0))

    // 연습문제 4.3
    val practice4_3: Option<Int> = Option(1)
    Assert.assertEquals(practice4_3.map2(Option<Double>(1.0), { a, b -> a.toString() + b.toString() }), Option<String>("11.0"))

    // 연습문제 4.6
    val practice4_6_1: Right<Int> = Right(1)
    Assert.assertEquals(practice4_6_1.map { a -> a.toString() }, Right<String>("1"))
    val practice4_6_2: Right<Int> = Right(1)
    Assert.assertEquals(practice4_6_2.flatMap { a -> Right(a.toString()) }, Right<String>("1"))
    val practice4_6_3: Left<String> = Left("Fail")
    Assert.assertEquals(practice4_6_3.orElse { Right(0) }, Right(0))
    val practice4_6_4: Right<Int> = Right(1)
    Assert.assertEquals(practice4_6_4.map2(Right(2.0)) { a, b -> a.toString() + b.toString() }, Right<String>("12.0"))
}
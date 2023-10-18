import kotlin.math.pow

sealed class Option<out ITEM>
data class Some<out ITEM>(val get: ITEM) : Option<ITEM>()
data object None : Option<Nothing>()

fun main() {
    val aa = listOf(Some(1), Some(2), Some(3))
    for (i in aa) {
        i.filter { it == 2 }
    }

    val someValue: Option<Int> = Some(5)
    val result1 = someValue.flatMap { Some(it * 2) }
}

// 연습문제 4.1
fun <ITEM, OTHER> Option<ITEM>.map(f: (ITEM) -> OTHER): Option<OTHER> =
    when(this) {
        is Some -> Some(f(get))
        else -> None
    }
fun <ITEM, OTHER> Option<ITEM>.flatMap(f: (ITEM) -> Option<OTHER>): Option<OTHER> =
    when(this) {
        is Some -> f(get)
        else -> None
    }
fun <ITEM> Option<ITEM>.getOrElse(default: () -> ITEM): ITEM =
    when(this) {
        is Some -> get
        else -> default()
    }
fun <ITEM> Option<ITEM>.orElse(ob: () -> Option<ITEM>): Option<ITEM> =
    when(this) {
        is Some -> this
        else -> ob()
    }
fun <ITEM> Option<ITEM>.filter(f: (ITEM) -> Boolean): Option<ITEM> =
    when(this) {
        is Some -> if(f(get)) this else None
        else -> None
    }


// 연습문제 4.2
fun List<Double>.mean(): Option<Double> =
    if (isEmpty()) None else Some(sum() / size())

fun List<Double>.variance(): Option<Double> =
    mean().flatMap { m -> map { x -> (x - m).pow(2) }.mean() }


// 연습문제 4.3
fun <ITEM, OTHER, RESULT> map2(a: Option<ITEM>, b: Option<OTHER>, f: (ITEM, OTHER) -> RESULT): Option<RESULT> =
    a.flatMap { aItem -> b.map { bItem -> f(aItem, bItem) } }

// 연습문제 4.4
fun <ITEM, RESULT> List<ITEM>.foldRight(acc: RESULT, f: (ITEM, RESULT) -> RESULT): RESULT =
    when (this) {
        is Nil -> acc
        is Cons -> f(head, tail.foldRight(acc, f))
    }
fun <ITEM> List<Option<ITEM>>.sequence(): Option<List<ITEM>> =
    foldRight(Some(Nil)) { oa1: Option<ITEM>, oa2: Option<List<ITEM>> ->
        map2(oa1, oa2) { a1: ITEM, a2: List<ITEM> -> Cons(a1, a2) }
    }


// 연습문제 4.5
fun <ITEM, OTHER> traverse( xa: List<ITEM>, f: (ITEM) -> Option<OTHER> ): Option<List<OTHER>> =
    when (xa) {
        is Nil -> Some(Nil)
        is Cons -> map2(f(xa.head),
            traverse(xa.tail, f)) {
                b, xb -> Cons(b, xb)
        }
    }

fun <ITEM> sequence(xs: List<Option<ITEM>>): Option<List<ITEM>> = traverse(xs) { it }

// 연습문제 4.6
sealed class Either<out ERROR, out ITEM>
data class Left<out ERROR>(val value: ERROR) : Either<ERROR, Nothing>()
data class Right<out ITEM>(val value: ITEM) : Either<Nothing, ITEM>()
fun <ERROR, ITEM, RESULT> Either<ERROR, ITEM>.map(f: (ITEM) -> RESULT): Either<ERROR, RESULT> =
    when(this){
        is Right ->  Right(f(value))
        is Left -> Left(value)
    }

fun <ERROR, ITEM, RESULT> Either<ERROR, ITEM>.flatMap(f: (ITEM) -> Either<ERROR, RESULT>): Either<ERROR, RESULT> =
    when(this){
        is Right ->  f(value)
        is Left -> Left(value)
    }


fun <ERROR, ITEM> Either<ERROR, ITEM>.orElse(f: () -> Either<ERROR, ITEM>): Either<ERROR, ITEM> =
    when(this){
        is Right ->  this
        is Left -> f()
    }

fun <ERROR, ITEM, OTHER, RESULT> Either<ERROR, ITEM>.map2(be: Either<ERROR, OTHER>, f: (ITEM, OTHER) -> RESULT ): Either<ERROR, RESULT> =
    when {
        this is Left -> Left(this.value)
        be is Left -> Left(be.value)
        else -> Right(f((this as Right).value, (be as Right).value))
    }


// 연습문제 4.7
sealed class List<out ITEM> {
    companion object {
        fun <ITEM> of(vararg aa: ITEM): List<ITEM> {
            val tail = aa.sliceArray(1 until aa.size)
            return if (aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
        }
    }
}

// 연습문제 4.8
fun <ERROR, ITEM, OTHER> List<ITEM>.traverse(f: (ITEM) -> Either<ERROR, OTHER> ): Either<ERROR, List<OTHER>> =
    when (this) {
        is Nil -> Right(Nil)
        is Cons -> f(head).map2(tail.traverse(f)) { b, xb ->
            Cons(b, xb)
        }
    }

fun <ERROR, ITEM> List<Either<ERROR, ITEM>>.sequence(): Either<ERROR, List<ITEM>> =
    traverse{ it }

data class Name(val value: String)
data class Age(val value: Int)
data class Person(val name: Name, val age: Age)

fun mkName(name: String): Either<String, Name> =
    if (name.isBlank()) Left("Name is empty.") else Right(Name(name))

fun mkAge(age: Int): Either<String, Age> =
    if (age < 0) Left("Age is out of range.") else Right(Age(age))

fun mkPerson(name: String, age: Int): Either<String, Person> =
    mkName(name).map2(mkAge(age)) { n, a -> Person(n, a) }

// 현재는 map2는 하나의 오류만 보고 가능하다. 두개의 오류 보고하는 방법은? 타입을 바꿔야 하나?
// 바꿜을 때 orElse, sequence, traverse 어떻게 바뀌나?


data object Nil : List<Nothing>()

data class Cons<out ITEM>(val head: ITEM, val tail: List<ITEM>) : List<ITEM>()

fun <ITEM> empty(): List<ITEM> = Nil
fun <ITEM> List<ITEM>.isEmpty(): Boolean =
    when(this) {
        is Nil -> true
        else -> false
    }


fun <ITEM> List<ITEM>.size(): Int =
    when(this) {
        is Nil -> 0
        is Cons -> 1 + tail.size()
    }

fun List<Double>.sum(): Double =
    foldLeft(0.0){x, y -> x + y }

fun <ITEM, ITEM2> List<ITEM>.map(f: (ITEM) -> ITEM2): List<ITEM2> =
    when(this) {
        is Nil -> Nil
        is Cons -> Cons(f(head), tail.map(f))
    }

tailrec fun <ITEM, ACC> List<ITEM>.foldLeft(acc: ACC, f: (ACC, ITEM) -> ACC): ACC =
    when(this){
        is Nil -> acc
        is Cons -> tail.foldLeft(f(acc, this.head), f)
    }

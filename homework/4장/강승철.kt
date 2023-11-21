package com.example.fp_practice_with_kotlin.chapter4

import org.junit.*
import kotlin.math.pow

import com.example.fp_practice_with_kotlin.chapter3.*
import com.example.fp_practice_with_kotlin.chapter3.FList.*

import com.example.fp_practice_with_kotlin.chapter4.FOption.*
import com.example.fp_practice_with_kotlin.chapter4.Either.*

sealed class FOption<out VALUE: Any> {
    data object None: FOption<Nothing>()
    data class Some<out VALUE: Any>@PublishedApi internal constructor(@PublishedApi internal val value: VALUE): FOption<VALUE>()
    companion object{
        inline operator fun <VALUE: Any> invoke(value: VALUE): FOption<VALUE> = Some(value)
        inline operator fun <VALUE: Any> invoke(): FOption<VALUE> = None
    }
}

// ===================================
// ✏️[연습문제 4.1]
inline fun <VALUE: Any, TO: Any> FOption<VALUE>.map(noinline block: (VALUE) -> TO): FOption<TO> =
    if (this is Some) FOption(block(value)) else FOption()
inline fun <VALUE: Any> FOption<VALUE>.getOfElse(noinline block: () -> VALUE): VALUE =
    if (this is Some) value else block()
inline fun <VALUE: Any, TO: Any> FOption<VALUE>.flatMap(noinline block: (VALUE) -> FOption<TO>): FOption<TO> =
    map(block).getOfElse { FOption() }
inline fun <VALUE: Any> FOption<VALUE>.orElse(noinline block: () -> FOption<VALUE>): FOption<VALUE> =
     map { FOption(it) }.getOfElse { block() }
inline fun <VALUE: Any> FOption<VALUE>.filter(noinline block: (VALUE) -> Boolean): FOption<VALUE> =
//    map { if (block(it)) FOption(it) else FOption() }.getOfElse { FOption() }     // <-- 결국 flatmap 내부 구현과 동일
    flatMap { if (block(it)) FOption(it) else FOption() }



data class Employee(
    val name: String,
    val department: String,
    val manager: FOption<String>
)
fun lookupByName(name: String): FOption<Employee> = TODO()
fun timDepartment(): FOption<String> =
    lookupByName("Tim").map { it.department }


// ===================================
// ✏️[연습문제 4.2]
fun <ITEM> FList<ITEM>._sum(block: (ITEM, ITEM) -> ITEM): FOption<ITEM> where ITEM: Number =
    when (this) {
        is Cons -> FOption(drop().fold(head) { acc, item -> block(acc, item) })
        is Nil -> FOption()
    }
inline fun FList<Double>.sum(): FOption<Double> = _sum { accu, curr -> accu + curr }
inline fun FList<Double>.variance(): FOption<Double> =
    sum()
        .map { it / size }
        .map { mean ->
            fold(0.0 to mean) { (acc, mean), curr ->
                acc + (curr - mean).pow(2) to mean
            }.first
        }
        .map { it / size }

class Chapter4_2 {
    fun variance(data: List<Double>): Double {
        // 데이터의 개수 확인
        if (data.isEmpty()) { return 0.0 }

        // 평균 계산
        val mean = data.sum() / data.size

        // 각 데이터 포인트에서 평균을 뺀 값을 제곱하고, 그 결과를 모두 더함
        val squaredDifferencesSum = data.sumByDouble { (it - mean).pow(2) }

        // 제곱된 차이의 합을 데이터 포인트의 개수로 나눔
        return squaredDifferencesSum / data.size
    }

    @Test
    fun main() {
        val data1 = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        val list1: FList<Double> = FList(*data1.toTypedArray())
        Assert.assertEquals(variance(data1), list1.variance().getOfElse { 0.0 }, 0.001)

        val data2 = emptyList<Double>()
        val list2: FList<Double> = FList(*data2.toTypedArray())
        Assert.assertEquals(variance(data2), list2.variance().getOfElse { 0.0 }, 0.001)
    }

}

// ===================================
// ✏️[연습문제 4.3]
inline fun <VALUE: Any, OTHER: Any, TO: Any> FOption<VALUE>.combine(
    other: FOption<OTHER>,
    noinline block: (VALUE, OTHER) -> TO
): FOption<TO> =
    flatMap { value -> other.map { block(value, it) } }
class Chapter4_3 {
    @Test
    fun main() {
        val d1: FOption<Double> = FOption(3.4)
        val i1: FOption<Int> = FOption(100)
        val r1: FOption<String> = d1.combine(i1) { a, b -> "${a * b.toDouble()}" }
        Assert.assertEquals("340.0", r1.getOfElse { "0" }, )
    }
}

// ===================================
// ✏️[연습문제 4.4]
inline fun <ITEM: Any> FList<FOption<ITEM>>.sequence(): FOption<FList<ITEM>> =
    traverse { it }
//inline fun <ITEM: Any, TO: Any> FList<ITEM>.traverse(crossinline block: (ITEM) -> FOption<TO>): FOption<FList<TO>> =
//    foldRight(FOption(FList())) { curr, acc ->
//        block(curr).combine(acc) { head, tail -> Cons(head, tail) }
//    }
class Chapter4_4 {
    @Test
    fun main() {
        val target = FList(
            FOption(1.0),
            FOption(2.0),
            FOption(3.0),
        )
        Assert.assertEquals(
            FOption(FList(1.0, 2.0, 3.0)),
            target.sequence()
        )
        val target2 = FList(
            FOption(1.0),
            FOption(2.0),
            FOption(),
            FOption(3.0),
        )
        Assert.assertEquals(
            FOption<FList<Double>>(),
            target2.sequence()
        )
    }
}

// ===================================
// ✏️[연습문제 4.5]
inline fun <ITEM: Any, TO: Any> FList<ITEM>.traverse(crossinline block: (ITEM) -> FOption<TO>): FOption<FList<TO>> =
    foldRight(FOption(FList())) { curr, acc ->
        block(curr).combine(acc) { head, tail -> Cons(head, tail) }
    }
class Chapter4_5 {
    @Test
    fun main() {
        val target = FList(1.0, 2.0, 3.0,)
        Assert.assertEquals(
            FOption(FList("1.0", "2.0", "3.0")),
            target.traverse { Some("${it}") }
        )
        Assert.assertEquals(
            target.map { FOption("${it}") }.sequence(),
            target.traverse { Some("${it}") }
        )
    }
}

/* ================================================== */

sealed class Either<out LEFT: Any, out RIGHT: Any> {
    data class Left<out VALUE: Any>@PublishedApi internal constructor(@PublishedApi internal val value: VALUE): Either<VALUE, Nothing>()
    data class Right<out VALUE: Any>@PublishedApi internal constructor(@PublishedApi internal val value: VALUE): Either<Nothing, VALUE>()
    companion object{
        inline fun <VALUE: Any> right(value: VALUE) = Right(value)
        inline fun <VALUE: Any> left(value: VALUE) = Left(value)
    }
}

// ===================================
// ✏️[연습문제 4.6]

inline fun <LEFT: Any, RIGHT: Any, TO: Any> Either<LEFT, RIGHT>.map(block: (RIGHT) -> TO): Either<LEFT, TO> =
    when (this) {
        is Left -> Either.left(value)
        is Right -> Either.right(block(value))
    }
inline fun <LEFT: Any, RIGHT: Any, TO: Any> Either<LEFT, RIGHT>.flatMap(block: (RIGHT) -> Either<LEFT, TO>): Either<LEFT, TO> =
    when (this) {
        is Left -> Either.left(value)
        is Right -> block(value)
    }
inline fun <LEFT: Any, RIGHT: Any> Either<LEFT, RIGHT>.orElse(block: () -> Either<LEFT, RIGHT>): Either<LEFT, RIGHT> =
    when (this) {
        is Left -> block()
        is Right -> this
    }
inline fun <LEFT: Any, RIGHT: Any, OTHER: Any, TO: Any> Either<LEFT, RIGHT>.combine(
    other: Either<LEFT, OTHER>,
    block: (RIGHT, OTHER) -> TO
): Either<LEFT, TO> =
    flatMap { right -> other.map { block(right, it) } }

// ===================================
// ✏️[연습문제 4.7]
inline fun <ITEM: Any, LEFT: Any, RIGHT: Any> FList<ITEM>.traverse2(crossinline block: (ITEM) -> Either<LEFT, RIGHT>): Either<LEFT, FList<RIGHT>> =
    foldRight(Either.right(FList())) { curr, acc ->
        block(curr).combine(acc) { head, tail -> Cons(head, tail) }
    }
inline fun <ITEM, LEFT, RIGHT> FList<ITEM>.sequence2(): Either<LEFT ,FList<RIGHT>> where ITEM: Either<LEFT, RIGHT>, LEFT: Any, RIGHT: Any =
    traverse2 { it }



data class Name(val value: String)
data class Age(val value: Int)
data class Person(val name: Name, val age: Age)

fun mkName(name: String): Either<String, Name> =
    if (name.isBlank()) Either.left("Name is empty.")
    else Either.right(Name(name))

fun mkAge(age: Int): Either<String, Age> =
    if (age < 0) Either.left("Age is out of range.")
    else Either.right(Age(age))

fun mkPerson(name: String, age: Int): Either<String, Person> =
    mkName(name).combine(mkAge(age)) { name, age -> Person(name, age) }

// ===================================
// ✏️[연습문제 4.8]
val <LEFT: Any, RIGHT: Any> Either<LEFT, RIGHT>.partial: Either<FList<LEFT>, RIGHT> get() =
    when (this) {
        is Left -> Left(FList(value))
        is Right -> Right(value)
    }
fun <LEFT: Any, RIGHT: Any, OTHER: Any, TO: Any> Either<FList<LEFT>, RIGHT>.partialCombine(
    other: Either<LEFT, OTHER>,
    block: (RIGHT, OTHER) -> TO
): Either<FList<LEFT>, TO> =
    when (this) {
        is Left -> when (other) {
            is Left -> Left(this.value.append(FList(other.value)))
            is Right -> Left(this.value)
        }
        is Right -> when (other) {
            is Left -> Left(FList(other.value))
            is Right -> Right(block(this.value, other.value))
        }
    }

fun mkPerson2(name: String, age: Int): Either<FList<String>, Person> =
    mkName(name)
        .partial
        .partialCombine(mkAge(age)) { name, age -> Person(name, age) }

class Chapter4_8 {
    @Test
    fun main() {
        val ret = mkPerson2("", -1)
        when (ret) {
            is Left -> println(ret.value.fold("") { acc, curr -> acc + " " + curr  })
            is Right -> println(ret.value)
        }
    }
}
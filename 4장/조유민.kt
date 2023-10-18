package codespitz_study_kotlinfunctionalprogramming.`4장`

import arrow.core.Either
import arrow.core.Either.Right
import arrow.core.Either.Left
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.doubles.plusOrMinus
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import io.kotest.matchers.shouldBe
import kotlin.math.pow

// 연습문제 1번
fun <A, B> Option<A>.map(f: (A) -> B): Option<B> =
    when (this) {
        is Some -> Some(f(value))
        None -> None
    }

fun <A, B> Option<A>.flatMap(f: (A) -> Option<B>): Option<B> =
    when (this) {
        is Some -> f(value)
        None -> None
    }

fun <A> Option<A>.getOrElse(default: () -> A): A =
    when (this) {
        is Some -> value
        None -> default()
    }

fun <A> Option<A>.orElse(ob: () -> Option<A>): Option<A> =
    when (this) {
        is Some -> this
        None -> ob()
    }

fun <A> Option<A>.filter(f: (A) -> Boolean): Option<A> =
    when (this) {
        is Some -> if (f(value)) this else None
        None -> None
    }

fun <A, B> Option<A>.flatMap_2(
    f: (A) -> Option<B>
): Option<B> =
    when (this) {
        is Some -> f(value)
        None -> None
    }

fun <A> Option<A>.orElse_2(
    ob: () -> Option<A>
): Option<A> =
    when (this) {
        is Some -> this
        None -> ob()
    }

fun <A> Option<A>.filter_2(
    f: (A) -> Boolean
): Option<A> =
    when (this) {
        is Some -> if (f(value)) this else None
        None -> None
    }

class Exercise1 : WordSpec ({
    val none: Option<Int> = None
    val some: Option<Int> = Some(10)

    "option map" should {
        "transform an option of some value" {
            some.map { it * 2 } shouldBe Some(20)
        }
        "pass over an option of none" {
            none.map { it * 10 } shouldBe None
        }
    }

    "option flatMap" should {
        """apply a function yielding an option to an
            option of some value""" {
            some.flatMap { a ->
                Some(a.toString())
            } shouldBe Some("10")

            some.flatMap_2 { a ->
                Some(a.toString())
            } shouldBe Some("10")
        }
        "pass over an option of none" {
            none.flatMap { a ->
                Some(a.toString())
            } shouldBe None

            none.flatMap_2 { a ->
                Some(a.toString())
            } shouldBe None
        }
    }

    "option getOrElse" should {
        "extract the value of some option" {
            some.getOrElse { 0 } shouldBe 10
        }
        "return a default value if the option is none" {
            none.getOrElse { 10 } shouldBe 10
        }
    }

    "option orElse" should {
        "return the option if the option is some" {
            some.orElse { Some(20) } shouldBe some
            some.orElse_2 { Some(20) } shouldBe some
        }
        "return a default option if the option is none" {
            none.orElse { Some(20) } shouldBe Some(20)
            none.orElse_2 { Some(20) } shouldBe Some(20)
        }
    }

    "option filter" should {
        "return some option if the predicate is met" {
            some.filter { it > 0 } shouldBe some
            some.filter_2 { it > 0 } shouldBe some
        }
        "return a none option if the predicate is not met" {
            some.filter { it < 0 } shouldBe None
            some.filter_2 { it < 0 } shouldBe None
        }
    }
})

// 연습문제 2번
fun mean(xs: List<Double>): Option<Double> =
    if (xs.isEmpty()) None
    else Some(xs.sum() / xs.size)

fun variance(xs: List<Double>): Option<Double> =
    mean(xs).flatMap { mean ->
        val squaredDifferences = xs.map { x -> (x - mean).pow(2) }
        mean(squaredDifferences)
    }

class Exercise2 : WordSpec ({
    "variance" should {
        "determine the variance of a list of numbers" {
            val ls =
                listOf(1.0, 1.1, 1.0, 3.0, 0.9, 0.4)
            variance(ls).getOrElse { 0.0 } shouldBe
                    (0.675).plusOrMinus(0.005)
        }
    }
})

// 연습문제 3번
fun <A, B, C> map2(a: Option<A>, b: Option<B>, f: (A, B) -> C): Option<C> =
    a.flatMap { aValue ->
        b.map { bValue ->
            f(aValue, bValue)
        }
    }

class Exercise3 : WordSpec ({

    "map2" should {

        val a = Some(5)
        val b = Some(20)
        val none: Option<Int> = None

        "combine two option values using a binary function" {
            map2(a, b) { aa, bb ->
                aa * bb
            } shouldBe Some(100)
        }

        "return none if either option is not defined" {
            map2(a, none) { aa, bb ->
                aa * bb
            } shouldBe None

            map2(none, b) { aa, bb ->
                aa * bb
            } shouldBe None
        }
    }
})

// 연습문제 4번
fun <A> sequence(xs: List<Option<A>>): Option<List<A>> =
    xs.foldRight(Some(emptyList())) { oa, acc ->
        when (oa) {
            is Some -> acc.map { list -> listOf(oa.value) + list }
            is None -> None
        }
    }

class Exercise4 : WordSpec ({

    "sequence" should {
        "turn a list of some options into an option of list" {
            val lo =
                listOf(Some(10), Some(20), Some(30))
            sequence(lo) shouldBe Some(listOf(10, 20, 30))
        }
        "turn a list of options containing none into a none" {
            val lo =
                listOf(Some(10), None, Some(30))
            sequence(lo) shouldBe None
        }
    }
})

// 연습문제 5번
fun <A, B> traverse(
    xa: List<A>,
    f: (A) -> Option<B>
): Option<List<B>> =
    xa.foldRight(Some(emptyList())) { a, acc ->
        acc.flatMap { listB ->
            f(a).map { b ->
                listOf(b) + listB
            }
        }
    }

fun <A> sequence2(xs: List<Option<A>>): Option<List<A>> =
    traverse(xs) { it }

fun <A> catches(a: () -> A): Option<A> =
    try {
        Some(a())
    } catch (e: Throwable) {
        None
    }

class Exercise5 : WordSpec ({
    "traverse" should {
        """return some option of a transformed list if all
            transformations succeed""" {
            val xa = listOf(1, 2, 3, 4, 5)
            traverse(xa) { a: Int ->
                catches { a.toString() }
            } shouldBe Some(
                listOf("1", "2", "3", "4", "5")
            )
        }

        "return a none option if any transformations fail" {
            val xa = listOf("1", "2", "x", "4")
            traverse(xa) { a ->
                catches { a.toInt() }
            } shouldBe None
        }
    }

    "sequence" should {
        "!turn a list of some options into an option of list" {
            val lo =
                listOf(Some(10), Some(20), Some(30))
            sequence2(lo) shouldBe Some(listOf(10, 20, 30))
        }

        "turn a list of options containing a none into a none" {
            val lo =
                listOf(Some(10), None, Some(30))
            sequence2(lo) shouldBe None
        }
    }
})

// 연습문제 6번
fun <E, A, B> Either<E, A>.map(f: (A) -> B): Either<E, B> =
    when (this) {
        is Right -> Right(f(this.value))
        is Left -> this
    }

fun <E, A, B> Either<E, A>.flatMap(f: (A) -> Either<E, B>): Either<E, B> =
    when (this) {
        is Right -> f(this.value)
        is Left -> this
    }

fun <E, A> Either<E, A>.orElse(f: () -> Either<E, A>): Either<E, A> =
    when (this) {
        is Right -> this
        is Left -> f()
    }

fun <E, A, B, C> map2(
    ae: Either<E, A>,
    be: Either<E, B>,
    f: (A, B) -> C
): Either<E, C> =
    ae.flatMap { a ->
        be.map { b ->
            f(a, b)
        }
    }

class Exercise6 : WordSpec ({
    val right: Either<Throwable, Int> = Right(1)
    val left: Either<Throwable, Int> = Left(Throwable("boom"))

    "either map" should {
        "transform a right value" {
            right.map { it.toString() } shouldBe Right("1")
        }
        "pass over a left value" {
            left.map { it.toString() } shouldBe left
        }
    }

    "either orElse" should {
        "return the either if it is right" {
            right.orElse { left } shouldBe right
        }
        "pass the default value if either is left" {
            left.orElse { right } shouldBe right
        }
    }

    "either flatMap" should {
        "apply a function yielding an either to a right either" {
            right.flatMap { a ->
                Right(a.toString())
            } shouldBe Right("1")
        }
        "pass on the left value" {
            left.flatMap { a ->
                Right(a.toString())
            } shouldBe left
        }
    }

    "either map2" should {
        val right1: Right<Int> = Right(3)
        val right2: Right<Int> = Right(2)
        val left1: Either<Throwable, Int> =
            Left(IllegalArgumentException("boom"))
        val left2: Either<Throwable, Int> =
            Left(IllegalStateException("pow"))

        "combine two either right values using a binary function" {
            map2(
                right1,
                right2
            ) { a, b -> (a * b).toString() } shouldBe Right("6")
        }
        "return left if either is left" {
            map2(
                right1,
                left1
            ) { a, b -> (a * b).toString() } shouldBe left1
        }
        "return the first left if both are left" {
            map2(
                left1,
                left2
            ) { a, b -> (a * b).toString() } shouldBe left1
        }
    }
})

// 연습문제 7번
fun <E, A, B> traverse3(
    xs: List<A>,
    f: (A) -> Either<E, B>
): Either<E, List<B>> =
    xs.fold(Right(emptyList())) { acc, a ->
        acc.flatMap { list ->
            f(a).map { b ->
                list + b
            }
        }
    }

fun <E, A> sequence3(es: List<Either<E, A>>): Either<E, List<A>> =
    traverse3(es) { it }

fun <A> catches3(a: () -> A): Either<String, A> =
    try {
        Right(a())
    } catch (e: Exception) {
        Left(e.message ?: "An error occurred")
    }

class Exercise7 : WordSpec ({
    "traverse" should {
        """return a right either of a transformed list if all
            transformations succeed""" {
            val xa = listOf("1", "2", "3", "4", "5")

            traverse3(xa) { a ->
                catches3 { Integer.parseInt(a) }
            } shouldBe Right(listOf(1, 2, 3, 4, 5))
        }

        "return a left either if any transformations fail" {
            val xa = listOf("1", "2", "x", "4", "5")

            traverse3(xa) { a ->
                catches3 { Integer.parseInt(a) }
            } shouldBe Left(
                """For input string: "x""""
            )
        }
    }
    "sequence" should {
        "turn a list of right eithers into a right either of list" {
            val xe: List<Either<String, Int>> =
                listOf(Right(1), Right(2), Right(3))

            sequence3(xe) shouldBe Right(listOf(1, 2, 3))
        }

        """convert a list containing any left eithers into a
            left either""" {
            val xe: List<Either<String, Int>> =
                listOf(Right(1), Left("boom"), Right(3))

            sequence3(xe) shouldBe Left("boom")
        }
    }
})

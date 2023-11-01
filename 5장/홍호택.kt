package study.fp

import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Chapter5Test {
    @Test
    fun test5Dot1() {
        val aa = of(1,2,3,4)
        val listAA = aa.toList()
        assertEquals(listAA, List.of(1,2,3,4))
    }

    @Test
    fun test5Dot2() {
        val aa = of(1,2,3,4)
        assertEquals(aa.take(2).toList(), List.of(1,2))
        assertEquals(aa.drop(2).toList(), List.of(3,4))
    }

    @Test
    fun test5Dot3() {
        val aa = of(1,2,3,4)
        assertEquals(aa.takeWhile { it < 3 }.toList(), List.of(1,2))
        assertEquals(aa.takeWhile { it > 2 }.toList(), List.of(3,4))
    }

    @Test
    fun test5Dot4() {
        val aa = of(1,2,3,4)
        assertEquals(aa.forAll { it < 3 }, false)
    }

    @Test
    fun test5Dot5() {
        val aa = of(1,2,3,4)
        assertEquals(aa.exists2 { it == 3 }, true)
        assertEquals(aa.takeWhile2 { it < 3 }.toList(), List.of(1,2))
        assertEquals(aa.takeWhile2 { it > 2 }.toList(), List.of(3,4))

    }
}

sealed class Option<out ITEM>{
    fun isEmpty():Boolean {
        return true
    }
}
data class Some<out ITEM>(val get: ITEM) : Option<ITEM>()
data object None : Option<Nothing>()
fun <ITEM> emptyO(): Option<ITEM> = None

fun <ITEM, OTHER> Option<ITEM>.map(f: (ITEM) -> OTHER): Option<OTHER> =
    when(this) {
        is Some -> Some(f(get))
        else -> None
    }
fun <ITEM> Option<ITEM>.getOrElse(default: () -> ITEM): ITEM =
    when(this) {
        is Some -> get
        else -> default()
    }

sealed class Stream<out ITEM>{
    companion object {
        fun <ITEM> cons(a: () -> ITEM, b: () -> Stream<ITEM>): Stream<ITEM> =
            cons(a, b)
    }

}
data class Cons<out ITEM>(
    val head: () -> ITEM,
    val tail: () -> Stream<ITEM>
) : Stream<ITEM>()
data object Empty : Stream<Nothing>()

sealed class List<out ITEM> {
    companion object {
        fun <ITEM> of(vararg aa: ITEM): List<ITEM> {
            val tail = aa.sliceArray(1..< aa.size)
            return if (aa.isEmpty()) Nil else ConsL(aa[0], of(*tail))
        }
    }
}

data object Nil : List<Nothing>()
fun <ITEM> emptyL(): List<ITEM> = Nil

data class ConsL<out ITEM>(val head: ITEM, val tail: List<ITEM>): List<ITEM>()
tailrec fun <ITEM, RESULT>  List<ITEM>.foldLeft(acc: RESULT, f: (ITEM, RESULT) -> RESULT) : RESULT =
    when (this) {
        is Nil -> acc
        is ConsL -> tail.foldLeft(f(head,acc), f)
    }
fun <ITEM> List<ITEM>.reverse() = foldLeft(emptyL<ITEM>()) { item, acc -> ConsL(item, acc)}


fun <ITEM> Stream<ITEM>.headOption(): Option<ITEM> =
    when (this) {
        is Empty -> None
        is Cons -> Some(head())
    }

fun <ITEM> cons(hd: () -> ITEM, tl: () -> Stream<ITEM>): Stream<ITEM> {
    val head: ITEM by lazy(hd)
    val tail: Stream<ITEM> by lazy(tl)
    return Cons({ head }, { tail })
}
fun <ITEM> empty(): Stream<ITEM> = Empty
fun <ITEM> of(vararg xs: ITEM): Stream<ITEM> =
    if (xs.isEmpty()) empty()
    else cons({ xs[0] }
    ) { of(*xs.sliceArray(1 until xs.size)) }

// 연습문제 5-1
fun <ITEM> Stream<ITEM>.toList(): List<ITEM> {
        tailrec fun <ITEM> Stream<ITEM>.go(acc:List<ITEM>):List<ITEM> =
            when(this) {
                is Empty -> acc
                is Cons -> tail().go(ConsL(head(), acc))
            }

        return go(List.of<ITEM>()).reverse()
}

// 연습문제 5-2
fun <ITEM> Stream<ITEM>.take(stopIndex: Int): Stream<ITEM> {
    tailrec fun Stream<ITEM>.go(index: Int, acc: Stream<ITEM>) : Stream<ITEM> =
        if(index == stopIndex)
            acc
        else
            when(this) {
                is Empty -> acc
                is Cons -> tail().go(index + 1, cons({ head() }, { acc }))
            }

    return go(0, Empty).reverse()
}

fun <ITEM> Stream<ITEM>.drop(stopIndex: Int): Stream<ITEM> {
    tailrec fun Stream<ITEM>.go(index: Int, acc: Stream<ITEM>) : Stream<ITEM> =
        when(this) {
            is Empty -> acc
            is Cons -> tail().go(index + 1, if(index >= stopIndex) cons({ head() }, { acc }) else acc)
        }

    return go(0, Empty).reverse()
}

fun <ITEM> Stream<ITEM>.reverse(): Stream<ITEM> {
    tailrec fun Stream<ITEM>.go(acc: Stream<ITEM>) : Stream<ITEM> =
        when(this) {
            is Empty -> acc
            is Cons -> tail().go(cons({ head() }, { acc }))
        }
    return go(Empty)
}

// 연습문제 5.3
fun <ITEM> Stream<ITEM>.takeWhile(p: (ITEM) -> Boolean): Stream<ITEM> =
    when (this) {
        is Empty -> empty()
        is Cons ->
            if (p(head()))
                cons(head) { tail().takeWhile(p) }
            else empty()
    }


// 연습문제 5.4
fun <ITEM> Stream<ITEM>.forAll(p: (ITEM) -> Boolean): Boolean =
    when(this) {
        is Empty -> true
        is Cons -> if(p(head())) tail().forAll(p) else false
    }


// 연습문제 5.4
fun <ITEM> Stream<ITEM>.takeWhile2(p: (ITEM) -> Boolean): Stream<ITEM> =
    foldRight( {empty()}, { item, f -> if(p(item)) cons({ item }, f) else f()})

fun <ITEM> Stream<ITEM>.exists(p: (ITEM) -> Boolean): Boolean =
                when (this) {
                    is Cons -> p(this.head()) || this.tail().exists(p)
                    else -> false
                }
fun <ITEM> Stream<ITEM>.exists2(p: (ITEM) -> Boolean): Boolean =
    foldRight({false}, { item, f -> p(item) || f()})

fun <ITEM, RESULT> Stream<ITEM>.foldRight(
    z: () -> RESULT,
    f: (ITEM, () -> RESULT) -> RESULT
): RESULT =
    when (this) {
        is Cons -> f(this.head()) {
            tail().foldRight(z, f)
        }
        is Empty -> z()
    }


// 연습문제 5.5
fun <ITEM> Stream<ITEM>.takeWhile3(p: (ITEM) -> Boolean): Stream<ITEM> = foldRight({ empty() },
    { h, t -> if (p(h)) cons({ h }, t) else t() })

// 연습문제 5.6
fun <ITEM> Stream<ITEM>.headOption2(): Option<ITEM> =
    this.foldRight(
        { emptyO() },
        { a, _ -> Some(a) }
    )

// 연습문제 5.7
fun <ITEM, RESULT> Stream<ITEM>.map(f: (ITEM) -> RESULT): Stream<RESULT> =
    this.foldRight(
        { empty<RESULT>() },
        { h, t -> cons({ f(h) }, t) })
fun <ITEM> Stream<ITEM>.filter(f: (ITEM) -> Boolean): Stream<ITEM> =
    this.foldRight(
        { empty<ITEM>() },
        { h, t -> if (f(h)) cons({ h }, t) else t() })
fun <ITEM> Stream<ITEM>.append(sa: () -> Stream<ITEM>): Stream<ITEM> = foldRight(sa) { h, t -> cons({ h }, t) }
fun <ITEM, RESULT> Stream<ITEM>.flatMap(f: (ITEM) -> Stream<RESULT>): Stream<RESULT> =
    foldRight(
        { empty<RESULT>() },
        { h, t -> f(h).append(t) })

// 연습문제 5.8
fun <ITEM> constant(a: ITEM): Stream<ITEM> =
    Stream.cons({ a }, { constant(a) })

// 연습문제 5.9
fun from(n: Int): Stream<Int> =
    cons({ n }, { from(n + 1) })

// 연습문제 5.10
fun fibs(): Stream<Int> {
    fun go(curr: Int, nxt: Int): Stream<Int> =
        cons({ curr }, { go(nxt, curr + nxt) })
    return go(0, 1)
}

// 연습문제 5.11
fun <ITEM, OTHER> unfold(z: OTHER, f: (OTHER) -> Option<Pair<ITEM, OTHER>>): Stream<ITEM> =
    f(z).map { pair ->
    cons({ pair.first },
        { unfold(pair.second, f) })
}.getOrElse {
    empty()
}

// 연습문제 5.12
fun fibs1(): Stream<Int> =
    unfold(0 to 1) { (curr, next) ->
        Some(curr to (next to (curr + next)))
    }

fun from2(n: Int): Stream<Int> =
    unfold(n) { a -> Some(a to (a + 1)) }

fun <ITEM> constant2(n: ITEM): Stream<ITEM> =
    unfold(n) { a -> Some(a to a) }

fun ones(): Stream<Int> =
    unfold(1) { Some(1 to 1) }

// 연습문제 5.13
fun <ITEM, RESULT> Stream<ITEM>.map2(f: (ITEM) -> RESULT): Stream<RESULT> =
    unfold(this) { s: Stream<ITEM> ->
        when (s) {
            is Cons -> Some(f(s.head()) to s.tail())
            else -> None
        } }
fun <ITEM> Stream<ITEM>.take2(n: Int): Stream<ITEM> =
    unfold(this) { s: Stream<ITEM> ->
        when (s) {
            is Cons ->
                if (n > 0)
                    Some(s.head() to s.tail().take(n - 1))
                else None
            else -> None
        }
    }
fun <ITEM> Stream<ITEM>.takeWhile4(p: (ITEM) -> Boolean): Stream<ITEM> = unfold(this
) { s: Stream<ITEM> ->
    when (s) {
        is Cons ->
            if (p(s.head()))
                Some(s.head() to s.tail())
            else None

        else -> None
    }
}

fun <ITEM, OTHER, C> Stream<ITEM>.zipWith(
    that: Stream<OTHER>,
    f: (ITEM, OTHER) -> C
): Stream<C> =
    unfold(this to that) { (ths: Stream<ITEM>, tht: Stream<OTHER>) -> when (ths) {
        is Cons ->
            when (tht) {
                is Cons ->
                    Some(
                        Pair(
                            f(ths.head(), tht.head()),
                            ths.tail() to tht.tail()
                        ) )
                else -> None }
        else -> None }
    }
fun <ITEM, OTHER> Stream<ITEM>.zipAll(
    that: Stream<OTHER>
): Stream<Pair<Option<ITEM>, Option<OTHER>>> =
    unfold(this to that) { (ths, tht) ->
        when (ths) {
            is Cons -> when (tht) {
                is Cons ->
                    Some(
                        Pair(
                            Some(ths.head()) to Some(tht.head()),
                            ths.tail() to tht.tail()
                        ) )
                else -> Some(
                    Pair(
                        Some(ths.head()) to None,
                        ths.tail() to empty<OTHER>()
                    ) )
            }
            else -> when (tht) {
                is Cons ->
                    Some(
                        Pair(
                            None to Some(tht.head()),
                            empty<ITEM>() to tht.tail()
                        ) )
                else -> None }
        }
    }


// 연습문제 5.14
fun <ITEM> Stream<ITEM>.startsWith(that: Stream<ITEM>): Boolean = this.zipAll(that)
    .takeWhile { !it.second.isEmpty() }
    .forAll { it.first == it.second }


// 연습문제 5.15
//fun <ITEM> Stream<ITEM>.tails(): Stream<ITEM> =
//    unfold(this) { s: Stream<ITEM> ->
//        when (s) {
//            is Cons -> cons({ s to s.tail() }, {Empty})
//            else -> Empty
//        }
//    }

// 연습문제 5.16
//fun <ITEM, OTHER> Stream<ITEM>.scanRight(z: OTHER, f: (ITEM, () -> OTHER) -> OTHER): Stream<OTHER> =
//    foldRight({ z to of(z) },
//        { a: ITEM, p0: () -> Pair<OTHER, Stream<OTHER>> ->
//            val p1: Pair<OTHER, Stream<OTHER>> by lazy { p0() }
//            val b2: OTHER = f(a) {p1.first}
//            Pair(b2, cons({ b2 }, { p1.second }))
//        }
//    ).second

package codespitz_study_kotlinfunctionalprogramming.`5장`

import java.util.Collections.reverse

sealed class Stream<out A: Any> {
    companion object {
        fun <A: Any> cons(hd: ()-> A, tl: ()-> Stream<A>): Stream<A> {
            val head: A by lazy(hd)
            val tail: Stream<A> by lazy(tl)
            return Cons({head}, {tail})
        }
    }
}

data class Cons<out A: Any>(
    val head: ()-> A,
    val tail: ()-> Stream<A>
): Stream<A>()

object Empty: Stream<Nothing>()

fun <A: Any> Stream<A>.headOption(): Option<A>
        = when(this) {
    is Empty-> Option.None
    is Cons-> Option.Some(head())
}

fun <A: Any> cons(hd: ()-> A, tl: ()-> Stream<A>): Stream<A> {
    val head: A by lazy(hd)
    val tail: Stream<A> by lazy(tl)
    return Cons({head}, {tail})
}

fun <A: Any> empty(): Stream<A> = Empty
fun <A: Any> of(vararg xs: A): Stream<A>
        = if(xs.isEmpty()) empty()
else cons({xs[0]}, {of(*xs.sliceArray(1 until xs.size))})

fun <A: Any> Stream<A>.toListUnsafe(): FList<A>
        = when(this) {
    is Empty -> FList.Nil
    is Cons -> ConsL(head(), tail().toListUnsafe())
}
fun <A: Any> Stream<A>.toList(): FList<A> {
    tailrec fun go(xs: Stream<A>, acc: FList<A>)
            = when(xs) {
        is Empty -> acc
        is Cons -> go(xs.tail(), ConsL(xs.head(), acc))
    }
    return reverse(go(this, NilL))
}

fun <ITEM: Any> Stream<ITEM>.take(n: Int): Stream<ITEM> {
    fun go(xs: Stream<ITEM>, n: Int): Stream<ITEM>
            = when(xs) {
        is Empty -> empty()
        is Cons ->
            if(n == 0) empty()
            else cons(xs.head, {go(xs.tail(), n - 1)})
    }

    return go(this, n)
}
fun <ITEM: Any> Stream<ITEM>.drop(n: Int): Stream<ITEM> {
    tailrec fun go(xs: Stream<ITEM>, n: Int): Stream<ITEM>
            = when(xs) {
        is Empty -> empty()
        is Cons ->
            if(n == 0) xs
            else go(xs.tail(), n - 1)
    }

    return go(this, n)
}

fun <ITEM: Any> Stream<ITEM>.takeWhile(p: (ITEM)-> Boolean): Stream<ITEM>
        = when(this) {
    is Empty -> empty()
    is Cons ->
        if(p(this.head()))
            cons(head, {this.tail().takeWhile(p)})
        else empty()
}

fun <A: Any> Stream<A>.exists(p: (A)-> Boolean): Boolean
        = when(this) {
    is Cons-> p(head()) || tail().exists(p)
    else -> false
}
fun <A:Any, B: Any> Stream<A>.foldRight(
    z: () -> B,
    f: (A, ()-> B)-> B
): B
        = when(this) {
    is Cons -> f(head()) {
        tail().foldRight(z, f)
    }
    is Empty -> z()
}
fun <A: Any> Stream<A>.exists2(p: (A)-> Boolean): Boolean
        = foldRight({false}, {a, b -> p(a) || b()})

fun <A: Any> Stream<A>.forAll(p: (A) -> Boolean): Boolean
        = foldRight({true}, {a, b -> p(a) && b()})

fun <A: Any> Stream<A>.takeWhile3(p: (A) -> Boolean): Stream<A>
        = foldRight({empty()}, {h, t -> if(p(h)) cons({h}, t) else t()})
fun <A: Any> Stream<A>.headOption3(): Option<A>
        = foldRight({Option.none()}, {a, _ -> Option(a)})
fun <A: Any, B: Any> Stream<A>.map3(f: (A) -> B): Stream<B>
        = foldRight({empty<B>()}, {h, t -> cons({f(h)}, t)})
fun <A: Any> Stream<A>.filter3(f: (A)-> Boolean): Stream<A>
        = foldRight(
    {empty()},
    {h, t -> if(f(h)) cons({h}, t) else t()}
)
fun <A: Any> Stream<A>.append3(sa: () -> Stream<A>): Stream<A>
        = foldRight(sa) {h, t -> cons({h}, t)}
fun <A: Any, B: Any> Stream<A>.flatMap(f: (A) -> Stream<B>): Stream<B>
        = foldRight(
    {empty<B>()},
    {h, t -> f(h).append3(t)}
)

fun <A: Any> constant(a: A): Stream<A>
        = Stream.cons({a}, {constant(a)})

fun from(n: Int): Stream<Int> = cons({n}, {from(n + 1)})


fun fibs(): Stream<Int> {
    fun go(curr: Int, nxt: Int): Stream<Int>
            = cons({curr}, {go(nxt, curr + nxt)})
    return go(0, 1)
}

fun <A: Any, S: Any> unfold(z: S, f: (S)-> Option<Pair<A, S>>): Stream<A>
        = f(z).map { pair ->
    cons({pair.first}, {unfold(pair.second, f)})
}.getOrElse {
    empty()
}

fun fibs12(): Stream<Int>
        = unfold(0 to 1) {(curr, next) ->
    Option.Some(curr to (next to (curr + next)))
}
fun from12(n: Int): Stream<Int>
        = unfold(n, {a -> Option.Some(a to (a + 1))})
fun <A: Any> constant12(n: A): Stream<A>
        = unfold(n, {a -> Option.Some(a to a)})
fun ones12(): Stream<Int>
        = unfold(1, {Option.Some(1 to 1)})

fun <A: Any, B: Any> Stream<A>.map13(f: (A) -> B): Stream<B>
        = unfold(this) {s: Stream<A> ->
    when(s) {
        is Cons -> Option.Some(f(s.head()) to s.tail())
        else -> Option.None
    }
}

fun <A: Any> Stream<A>.take13(n: Int): Stream<A>
        = unfold(this) {s: Stream<A> ->
    when(s) {
        is Cons ->
            if(n > 0)
                Option.Some(s.head() to s.tail().take(n - 1))
            else Option.None
        else -> Option.None
    }
}

fun <A: Any> Stream<A>.takeWhile13(p: (A) -> Boolean): Stream<A>
        = unfold(this) {s: Stream<A> ->
    when(s) {
        is Cons ->
            if(p(s.head()))
                Option.Some(s.head() to s.tail())
            else Option.None
        else -> Option.None
    }
}

fun <A: Any, B: Any, C: Any> Stream<A>.zipWith13(
    that: Stream<B>,
    f: (A, B) -> C
): Stream<C>
        = unfold(this to that) {(ths: Stream<A>, tht: Stream<B>) ->
    when(ths) {
        is Cons ->
            when(tht) {
                is Cons ->
                    Option.Some(
                        Pair(
                            f(ths.head(), tht.head()),
                            ths.tail() to tht.tail())
                    )
                else -> Option.None
            }
        else -> Option.None
    }
}

fun <A: Any, B: Any> Stream<A>.zipAll(
    that: Stream<B>
): Stream<Pair<Option<A>, Option<B>>>
        = unfold(this to that) {(ths, tht) ->
    when(ths) {
        is Cons -> when(tht) {
            is Cons -> Option.Some(
                Pair(
                    Option.Some(ths.head()) to Option.Some(tht.head()),
                    ths.tail() to tht.tail()
                )
            )
            else -> Option.Some(
                Pair(
                    Option.Some(ths.head()) to Option.None,
                    ths.tail() to empty()
                )
            )
        }
        else -> when(tht) {
            is Cons -> Option.Some(
                Pair(
                    Option.None to Option.Some(tht.head()),
                    empty<A>() to tht.tail()
                )
            )
            else -> Option.None
        }
    }
}

fun <A: Any> Stream<A>.startWith(that: Stream<A>): Boolean
        = zipAll(that)
    .takeWhile { !it.second.isEmpty() }
    .forAll { it.first == it.second }

fun <A: Any> Stream<A>.tails(): Stream<Stream<A>>
        = unfold(this) {s: Stream<A> ->
    when(s) {
        is Cons -> Option.Some(s to s.tail())
        else -> Option.None
    }
}
fun <A: Any, B: Any> Stream<A>.scanRight(z: B, f: (A, () -> B) -> B): Stream<B>
        = foldRight({z to of(z)}) {a: A, p0: () -> Pair<B, Stream<B>> ->
    val p1: Pair<B, Stream<B>> by lazy {p0()}
    val b2: B = f(a) {p1.first}
    Pair<B, Stream<B>>(b2, cons({b2}, {p1.second}))
}.second


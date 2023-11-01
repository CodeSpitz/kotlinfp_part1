import Stream.Cons
import Stream.Empty
import sun.jvm.hotspot.opto.Block

fun <A> lazyIf(
    cond: Boolean,
    onTrue: () -> A,
    onFalse: () -> A
): A = if (cond) onTrue() else onFalse();

sealed class Stream<out ITEM: Any> {
    data object Empty: Stream<Nothing>()
    data class Cons<out ITEM: Any>@PublishedApi internal constructor(
        @PublishedApi internal val head: () -> ITEM,
        @PublishedApi internal val tail: () -> Stream<ITEM>
    ): Stream<ITEM>()

    companion object {
        inline operator fun <ITEM: Any> invoke(vararg items: ITEM): Stream<ITEM> = items.foldRight(invoke()) {
                it, acc -> Cons({ it }) { acc }
        }
        inline operator fun <ITEM: Any> invoke(noinline hd: () -> ITEM, noinline tl: () -> Stream<ITEM>): Stream<ITEM> {
            val head: ITEM by lazy(hd)
            val tail: Stream<ITEM> by lazy(tl)
            return Cons({ head }, { tail })
        }
        inline operator fun <ITEM: Any> invoke(): Stream<ITEM> = Empty
    }
}

fun <ITEM: Any> Stream<ITEM>.headOption(): Option<ITEM> =
    when(this) {
        is Cons -> Option(head())
        is Empty -> Option()
    }

tailrec fun <ITEM: Any> Stream<ITEM>.toListUnsafe(): FList<ITEM> =
    when(this) {
        is Cons -> FList(head(), tail().toListUnsafe())
        is Empty -> FList()
    }

fun <ITEM: Any> Stream<ITEM>.toList(): FList<ITEM> {
    tailrec fun go(list: FList<ITEM>, stream: Stream<ITEM>): FList<ITEM> =
        when(stream) {
            is Cons -> go(FList(stream.head(), list), stream.tail())
            is Empty -> list
        }
    return go(FList(), this).reverse()
}

fun <ITEM: Any> Stream<ITEM>.take(n: Int): Stream<ITEM> =
    when (this) {
        is Cons -> when {
            n > 0 -> Stream(head) { tail().take(n - 1) }
            else -> Stream()
        }
        is Empty -> this
    }



fun <ITEM: Any> Stream<ITEM>.drop(n: Int): Stream<ITEM> =
    when (this) {
        is Cons -> when {
            n > 0 -> tail().drop(n - 1)
            else -> this
        }
        is Empty -> this
    }

fun <ITEM: Any> Stream<ITEM>.takeWhile(p: (ITEM) -> Boolean): Stream<ITEM> =
    when (this) {
        is Cons -> when {
            p(head()) -> Stream(head) { tail().takeWhile(p) }
            else -> Stream()
        }
        is Empty -> this
    }

fun <ITEM: Any> Stream<ITEM>.exists(p: (ITEM) -> Boolean): Boolean =
    when (this) {
        is Cons -> p(head()) || tail().exists(p)
        is Empty -> false
    }

fun <ITEM: Any, ACC: Any> Stream<ITEM>.foldRight(acc: () -> ACC, block: (ITEM, () -> ACC) -> ACC): ACC =
    when (this) {
        is Cons -> block(this.head()) { tail().foldRight(acc, block) }
        is Empty -> acc()
    }

fun <ITEM: Any> Stream<ITEM>.exists2(p: (ITEM) -> Boolean): Boolean =
    foldRight({ false }) { it, acc -> acc() || p(it) }

fun <ITEM: Any> Stream<ITEM>.forAll(p: (ITEM) -> Boolean): Boolean =
    when (this) {
        is Cons -> if(p(head())) tail().forAll(p) else false
        is Empty -> false
    }

fun <ITEM: Any> Stream<ITEM>.takeWhile2(p: (ITEM) -> Boolean): Stream<ITEM> =
    foldRight({ Stream() }) { it, acc -> if(p(it)) Stream({ it }, acc) else acc() }

fun <ITEM:Any> Stream<ITEM>.headOption2(): Option<ITEM> =
    foldRight({ Option() }) { it, _ -> Option(it)}

fun <ITEM: Any, OTHER: Any> Stream<ITEM>.map(block: (ITEM) -> OTHER): Stream<OTHER> =
    foldRight({Stream<OTHER>()}) { it, acc -> Stream({block(it)}, acc)}

fun <ITEM: Any> Stream<ITEM>.filter(f: (ITEM) -> Boolean): Stream<ITEM> =
    foldRight({Stream()}) { it, acc -> if(f(it)) Stream({ it }, acc) else acc() }

fun <ITEM: Any> Stream<ITEM>.append(stream: () -> Stream<ITEM>): Stream<ITEM> =
    foldRight(stream) { it, acc -> Stream({ it }, acc) }

fun <ITEM: Any, OTHER: Any> Stream<ITEM>.flatMap(block: (ITEM) -> Stream<OTHER>): Stream<OTHER> =
    foldRight({ Stream<OTHER>() }) { it, acc -> block(it).append(acc) }

fun <ITEM: Any> Stream<ITEM>.find(p: (ITEM) -> Boolean): Option<ITEM> =
    filter(p).headOption2()

fun ones(): Stream<Int> = Stream({ 1 }) { ones() }

fun <ITEM: Any> constant(v: ITEM): Stream<ITEM> =
    Stream({ v }) { constant(v) }

fun from(n: Int): Stream<Int> =
    Stream({ n }) { from(n + 1) }

fun fibs(): Stream<Int> {
    fun go(n1: Int, n2: Int): Stream<Int> =
        Stream({ n1 }) { go(n2, n1 + n2) }
    return go(0, 1)
}

fun <ITEM: Any, ACC: Any> unfold(stream: ACC, block: (ACC) -> Option<Pair<ITEM, ACC>>): Stream<ITEM> =
    block(stream).map { pair ->
        Stream({ pair.first }, { unfold(pair.second, block)})
    }.getOrElse { Stream() }

fun fibs2(): Stream<Int> =
    unfold(0 to 1) { (n1, n2) -> Option(n1 to (n2 to (n1 + n2)))}

fun from2(n: Int): Stream<Int> = unfold(n) { Option(it to (it + 1))}

fun <ITEM: Any> constant2(n: ITEM): Stream<ITEM> =
    unfold(n) { Option(it to it) }

fun ones2(): Stream<Int> =
    unfold(1) { Option(1 to 1) }

fun ones3(): Stream<Int> = constant2(1)

fun <ITEM: Any, OTHER: Any> Stream<ITEM>.map2(block: (ITEM) -> OTHER): Stream<OTHER> =
    unfold(this) {
            s -> when (s) {
        is Cons -> Option(block(s.head()) to s.tail())
        is Empty -> Option()
    }
    }

fun <ITEM: Any> Stream<ITEM>.take2(n: Int): Stream<ITEM> =
    unfold(this) {
            s: Stream<ITEM> -> when (s) {
        is Cons -> when {
            n > 0 -> Option(s.head() to s.tail().take(n - 1))
            else -> Option()
        }
        is Empty -> Option()
    }
    }

fun <ITEM: Any> Stream<ITEM>.takeWhile3(p: (ITEM) -> Boolean): Stream<ITEM> =
    unfold(this) {
            s: Stream<ITEM> -> when (s) {
        is Cons -> when {
            p(s.head()) -> Option(s.head() to s.tail())
            else -> Option()
        }
        is Empty -> Option()
    }
    }

fun <ITEM: Any, OTHER: Any, RESULT: Any> Stream<ITEM>.zipWith(other: Stream<OTHER>, block: (ITEM, OTHER) -> RESULT): Stream<RESULT> =
    unfold(this to other) {
            (it: Stream<ITEM>, other: Stream<OTHER>) ->
        when(it) {
            is Cons ->
                when(other) {
                    is Cons -> Option(Pair(block(it.head(), other.head()), it.tail() to other.tail()))
                    is Empty -> Option(Pair(Option(it.head()) to Option(), it.tail() to other.tail()))
                }
            is Empty -> Option()
        }



        fun <ITEM: Any> Stream<ITEM>.startsWith(other: Stream<ITEM>): Boolean =
            this.zipAll(other)
                .takeWhile3 { !it.second.isEmpty() }
                .forAll {it.first == it.second}
    }

fun <ITEM: Any> Stream<ITEM>.tails(): Stream<Stream<ITEM>> =
    unfold(this) {
            s: Stream<ITEM> -> when(s) {
        is Cons -> Option(s to s.tail())
        is Empty -> Option()
    }
    }

fun <ITEM: Any, OTHER: Any> Stream<ITEM>.scanRight(base: OTHER, block: (ITEM, () -> OTHER) -> OTHER): Stream<OTHER> =
    foldRight({base to Stream(base)}) {
            it: ITEM, p0: () -> Pair<OTHER, Stream<OTHER>> ->
        val p1: Pair<OTHER, Stream<OTHER>> by lazy { p0() }
        val b2: OTHER = block(it) {p1.first}
        Pair<OTHER, Stream<OTHER>>(b2, Stream({b2}) { p1.second})
    }.second
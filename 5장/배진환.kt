package fp.study

sealed interface FStream<out E : Any> {
    companion object {
        fun <E : Any> of(vararg xs: E): FStream<E> = when {
            xs.isEmpty() -> Empty
            else -> FStream({ xs[0] }) {
                of(*xs.sliceArray(1 until xs.size))
            }
        }

        inline operator fun <E : Any> invoke(
            noinline head: () -> E,
            noinline tail: () -> FStream<E>
        ): FStream<E> {
            val lazyHead by lazy(head)
            return SCons({ lazyHead }, tail)
        }

        inline operator fun <E : Any> invoke(): FStream<E> = FStream()
    }
}

@PublishedApi
internal data class SCons<out E : Any>(
    val head: () -> E,
    val tail: () -> FStream<E>
) : FStream<E>

@PublishedApi
internal data object Empty : FStream<Nothing>

inline fun <E : Any> FStream<E>.headOption(): FOption<E> = when (this) {
    is SCons -> Some(head())
    is Empty -> None
}

// 1
private tailrec fun <E : Any> FStream<E>._toFList(l: FList<E> = FList()): FList<E> =
    when (this) {
        is SCons -> _toFList(FList(head(), l))
        is Empty -> l.reverse()
    }

fun <E : Any> FStream<E>.toFList(): FList<E> = _toFList()

// 2
fun <E : Any> FStream<E>.take(n: Int): FStream<E> = when (this) {
    is SCons -> if (n > 0) FStream(head) { tail().take(n - 1) } else FStream()
    is Empty -> this
}

tailrec fun <E : Any> FStream<E>.drop(n: Int): FStream<E> = when (this) {
    is SCons -> if (n > 0) this else tail().drop(n - 1)
    is Empty -> this
}

// 3
fun <E : Any> FStream<E>.takeWhile(block: (E) -> Boolean): FStream<E> =
    when (this) {
        is SCons -> if (block(head())) FStream(head) {
            tail().takeWhile(
                block
            )
        } else FStream()

        is Empty -> this
    }

fun <VALUE : Any, ACC : Any> FStream<VALUE>.foldRight(
    init: () -> ACC,
    block: (VALUE, () -> ACC) -> ACC
): ACC = when (this) {
    is SCons -> block(head()) { tail().foldRight(init, block) }
    is Empty -> init()
}

inline fun <E : Any> FStream<E>.exist2(crossinline block: (E) -> Boolean): Boolean =
    foldRight({ false }) { e, acc ->
        block(e) || acc()
    }

// 4
inline fun <E : Any> FStream<E>.all(crossinline block: (E) -> Boolean): Boolean =
    foldRight({ true }) { e, acc ->
        block(e) && acc()
    }

// 5
inline fun <E : Any> FStream<E>.takeWhile2(crossinline block: (E) -> Boolean): FStream<E> =
    foldRight({ FStream() }) { curr, acc ->
        if (block(curr)) FStream({ curr }) { acc() }
        else FStream()
    }

// 6
inline fun <E : Any> FStream<E>.headOption2(): FOption<E> =
    foldRight<E, FOption<E>>({ None }) { curr, _ ->
        Some(curr)
    }

// 7
inline fun <E : Any, E2 : Any> FStream<E>.map(crossinline block: (E) -> E2): FStream<E2> =
    foldRight({ FStream<E2>() }) { e, acc ->
        FStream({ block(e) }) { acc() }
    }

inline fun <E : Any> FStream<E>.filter(crossinline block: (E) -> Boolean): FStream<E> =
    foldRight({ FStream() }) { curr, acc ->
        if (block(curr)) FStream({ curr }) { acc() }
        else acc()
    }

inline fun <E : Any> FStream<E>.append(other: FStream<E>): FStream<E> =
    foldRight({ other }) { curr, acc ->
        FStream({ curr }) {
            acc()
        }
    }

fun <E : Any> FStream<E>.find(block: (E) -> Boolean): FOption<E> =
    filter(block).headOption2()

fun ones(): FStream<Int> = FStream({ 1 }) { ones() }

// 8
fun <E : Any> FStream<E>.constant(block: () -> E): FStream<E> =
    FStream(block) { constant(block) }

//9
fun from(n: Int): FStream<Int> =
    FStream({ n }) { if (n > 0) from(n - 1) else FStream() }

// 10
fun fibs(): FStream<Int> = _fibs(0, 1)

private fun _fibs(curr: Int, next: Int): FStream<Int> =
    FStream({ curr }, { _fibs(next, next + curr) })

// 11
fun <E : Any, S : Any> unfold(
    init: S,
    block: (S) -> FOption<Pair<E, S>>
): FStream<E> =
    block(init).map { (e, s) ->
        FStream({ e }) { unfold(s, block) }
    }.getOrElse { FStream() }

// 12
fun ones2(): FStream<Int> = unfold(1) { FOption(it to it) }
fun <E : Any> constant2(block: () -> E): FStream<E> =
    unfold(block()) { FOption(it to it) }

fun from2(n: Int): FStream<Int> =
    unfold(n) { FOption(it to it + 1) }

fun fibs2(): FStream<Int> =
    unfold(0 to 1) { FOption(it.first to (it.second to it.first + it.second)) }

// 13
fun <E1 : Any, E2 : Any, E3 : Any> FStream<E1>.zipWith(
    other: FStream<E2>,
    zip: (E1, E2) -> E3
): FStream<E3> =
    when (this) {
        is SCons -> when (other) {
            is SCons -> FStream({ zip(head(), other.head()) }) {
                tail().zipWith(other.tail(), zip)
            }

            is Empty -> FStream()
        }

        is Empty -> FStream()
    }

fun <E1 : Any, E2 : Any, E3 : Any> FStream<E1>.zipWith2(
    other: FStream<E2>,
    zip: (E1, E2) -> E3
): FStream<E3> =
    unfold(this to other) { (stream1: FStream<E1>, stream2: FStream<E2>) ->
        when (stream1) {
            is SCons -> when (stream2) {
                is SCons -> FOption(
                    Pair(
                        zip(stream1.head(), stream2.head()),
                        stream1.tail() to stream2.tail()
                    )
                )

                is Empty -> FOption()
            }

            is Empty -> FOption()
        }
    }

fun <E1 : Any, E2 : Any> FStream<E1>.zipAll(
    other: FStream<E2>
): FStream<Pair<FOption<E1>, FOption<E2>>> =
    unfold(this to other) { (target, other) ->
        when (target) {
            is SCons -> when (other) {
                is SCons -> FOption(
                    Pair(
                        FOption(target.head()) to FOption(other.head()),
                        target.tail() to other.tail()
                    )
                )

                is Empty -> FOption(
                    Pair(
                        Pair(FOption(target.head()), FOption()),
                        target.tail() to other
                    )
                )
            }

            is Empty -> when (other) {
                is SCons -> FOption(
                    Pair(
                        Pair(FOption(), FOption(other.head())),
                        target to other.tail()
                    )
                )

                is Empty -> FOption()
            }
        }
    }

// 14
fun <E : Any> FStream<E>.startsWith(other: FStream<E>): Boolean =
    zipAll(other).takeWhile { !it.second.isEmpty() }
        .all { it.first != it.second }

// 15
fun <E : Any> FStream<E>.tails(): FStream<FStream<E>> =
    unfold(this) {
        when (it) {
            is SCons -> FOption(it to it.tail())
            is Empty -> FOption()
        }
    }

//
fun <E : Any, ACC : Any> FStream<E>.scanRight(
    init: ACC,
    block: (E, ACC) -> ACC
): FStream<ACC> =
    foldRight({ init to FStream.of(init) }) { currE: E, acc ->
        val (currResult, currStream) = acc()
        block(currE, currResult).let{
            it to FStream({ it }, { currStream })
        }
    }.second
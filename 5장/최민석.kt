package study.fp

sealed class Stream<out A> {

    companion object {
        fun <A> cons(hd: () -> A, tl: () -> Stream<A>): Stream<A> {
            val head: A by lazy(hd)
            val tail: Stream<A> by lazy(tl)
            return ConsS({ head}, { tail })
        }

        fun <A> empty(): Stream<A> = EmptyS

        fun <A> of(vararg xs: A): Stream<A> =
            if(xs.isEmpty()) empty() else cons({ xs[0] }, { of(*xs.sliceArray(1 until xs.size)) })
    }
}

data class ConsS<out A>(
    val head: () -> A,
    val tail: () -> Stream<A>
): Stream<A>()

object EmptyS: Stream<Nothing>()


// 5.1
tailrec fun <A, B> Stream<A>.foldLeft(z: B, f: (B, A) -> B): B = when(this) {
    is ConsS -> tail().foldLeft(f(z, head()), f)
    EmptyS -> z
}

fun <A> Stream<A>.reverse() = foldLeft(Stream.of<A>()) { acc, a -> ConsS( { a }, { acc })}

fun <A> Stream<A>.toList1(): ListE<A> {
    return when(this) {
        is ConsS -> {
            Cons(head(), tail().toList1())
        }
        EmptyS -> Nil
    }
}

fun <A> Stream<A>.toList2(): ListE<A> {
    return reverse(foldLeft(z = Nil as ListE<A>) { acc, a ->
        println("toList2::$a")
        Cons(a, acc)}
    )
}

// 5.2
fun <A> Stream<A>.take(n: Int): Stream<A> {
    val tmp = foldLeft(n to Stream.of<A>()) { (num, stream), a ->
        val left = num - 1
        if (left >= 0 ) num-1 to ConsS({a} , { stream })
        else num to stream
    }

    return tmp.second.reverse()
}


tailrec fun <A> Stream<A>.drop(n: Int): Stream<A> = when(this){
    is ConsS -> {
        if (n > 0) tail().drop(n-1)
        else this
    }
    EmptyS -> EmptyS
}

// 5.3
fun <A> Stream<A>.takeWhile(p: (A) -> Boolean): Stream<A> = foldLeft<A, Stream<A>>(z = Stream.of()) { acc, a ->
    if (p(a)) Stream.cons({ a }, { acc })
    else acc
}.reverse()

// 5.4
fun <A> Stream<A>.forAll(p: (A) -> Boolean): Boolean = foldLeft(true) { acc, a ->
    acc || p(a)
}


fun <A, B> Stream<A>.foldRight(
    z: B,
    f: (A, () -> B) -> B
): B = when(this) {
    is ConsS -> f(this.head()) {
        tail().foldRight(z, f)
    }
    EmptyS -> z
}


// 5.5
fun <A> Stream<A>.takeWhileRight(p: (A) -> Boolean): Stream<A> = foldRight(z = Stream.of()) { a, acc ->
    if (p(a)) {
        Stream.cons({ a }, acc )
    } else Stream.empty()
}

// 5.6
fun <A> Stream<A>.headOption(): Option<A> = foldRight<A, Option<A>>( Option() ) { a, acc ->
    Option(a)
}

// 5.7
fun <A, B> Stream<A>.map(f: (A) -> B): Stream<B> = foldRight(z = Stream.of()) { a, acc ->
    println("map::$a")
    Stream.cons( { f(a) } , acc)
}

fun <A> Stream<A>.filter(f: (A) -> Boolean): Stream<A> = foldRight(z = Stream.of()) { a, acc ->
    println("filter::$a")

    if (f(a)) {
        Stream.cons( { a }, acc )
    }
    else acc()
}

fun <A> Stream<A>.append(newItem: () -> A): Stream<A> = foldRight(z = Stream.of(newItem())) { a, acc  ->
    Stream.cons( { a }, acc)
}

fun <A, B> Stream<A>.flatMap(f: (A) -> Stream<B>): Stream<B> = foldLeft(z = Stream.of()) { acc, a ->
    when(val item = f(a)) {
        is ConsS -> acc.append {
            item.head()
        }
        EmptyS -> acc
    }
}


// 5.8
fun <A> constant(a: A): Stream<A> = Stream.cons({ a }, { constant(a) })

// 5.9
fun from(n: Int): Stream<Int> = Stream.cons({n}, { from(n+1) })

// 5.10
fun fibs(): Stream<Int> {

    fun acc(state: Int, next: Int): Stream<Int> {
        return Stream.cons( { state }, {
            acc (state, state + next)
        } )
    }
    return acc(0, 1)
}

// 5.11
fun <A, S> unfold(z: S, f: (S) -> Option<Pair<A, S>>): Stream<A> {
    return when(val result = f(z)) {
        None -> Stream.of<A>()
        is Some -> {
            val (value, state) = result.data
            Stream.cons({ value }, { unfold(state, f) })
        }
    }
}


// 5.12
fun <A> constantUnfold(a: A) = unfold(a) { a -> Option(a to a)}
fun fromUnfold(n: Int): Stream<Int> = unfold(n) { n -> Option(n to n+1) }
fun fibsUnfold(): Stream<Int> = unfold(0 to 1) { (curr, next) ->
    Option(curr to  (curr to (curr + next)))
}

// 5.13
fun <A, B> Stream<A>.mapUnfold(f: (A) -> B): Stream<B> {
    return unfold(this) { s ->
        when(s) {
            is ConsS -> Option(f(s.head()) to s.tail())
            EmptyS -> Option()
        }
    }
}

fun <A> Stream<A>.takeUnfold(n: Int): Stream<A> = unfold(this) { s ->
    when(s) {
        is ConsS -> {
            if (n > 0) Option(s.head() to s.tail().takeUnfold(n-1))
            else Option()
        }
        EmptyS -> Option()
    }
}


fun <A> Stream<A>.takeWhileUnfold(p: (A) -> Boolean): Stream<A> = unfold(this) { s ->
    when(s) {
        is ConsS -> {
            if (p(s.head())) Option(s.head() to s.tail())
            else Option()
        }
        EmptyS -> Option()
    }
}

fun <A, B, C> Stream<A>.zipWith(
    that: Stream<B>,
    f: (A, B) -> C
): Stream<C> = unfold(this to that) { (ths, tht) ->
    when(ths) {
        is ConsS -> {
            when(tht) {
                is ConsS -> {
                    Option(f(ths.head(), tht.head()) to (ths.tail() to tht.tail()))
                }
                EmptyS -> Option()
            }
        }
        EmptyS -> Option()
    }
}

fun <A, B> Stream<A>.zipAll(that: Stream<B>): Stream<Pair<Option<A>, Option<B>>> = unfold(this to that) { (ths, tht) ->
    when(ths) {
        is ConsS -> {
            when(tht) {
                is ConsS -> {
                    Option((Option(ths.head()) to Option(tht.head())) to (ths.tail() to tht.tail()))
                }
                EmptyS -> Option((Option(ths.head()) to Option()) to (ths.tail() to EmptyS))
            }
        }
        EmptyS -> {
            when(tht) {
                is ConsS -> {
                    Option((Option() to Option(tht.head())) to (EmptyS to tht.tail()))
                }
                EmptyS -> Option()
            }
        }
    }
}

// 5.14
fun <A> Stream<A>.startsWith(that: Stream<A>): Boolean = that.zipWith(this) { tht, ths ->
    tht == ths
}.foldRight(true) { a, acc -> a && acc() }


// 5.15
fun <A> Stream<A>.tails(): Stream<Stream<A>> = unfold(this) { s ->
    when(s) {
        is ConsS -> {
            Option(s to s.tail())
        }
        EmptyS -> Option()
    }

}

// 5.16
fun <A, B> Stream<A>.scanRight(z: B, f: (A, () -> B) -> B): Stream<B> = foldRight(z to Stream.of(z)) { a: A, p0: () -> Pair<B, Stream<B>> ->
    val p1: Pair<B, Stream<B>> by lazy { p0() }
    val b2 = f(a) { p1.first }
    b2 to Stream.cons({ b2 }, { p1.second })
}.second



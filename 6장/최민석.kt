package study.fp

interface RNG  {
    fun nextInt(): Pair<Int, RNG>
}

// 6.1
fun nonNegativeInt(rng: RNG): Pair<Int, RNG> {
    val (n, rng) = rng.nextInt()

    val adjustedN = if (n < 0) -(n+1) else n

    return adjustedN to rng
}

// 6.2
fun double(rng: RNG): Pair<Double, RNG> {
    val (n, rng) = rng.nextInt()
    return ((n - 1) / Int.MAX_VALUE).toDouble() to rng
}

// 6.3
fun intDouble(rng: RNG): Pair<Pair<Int, Double>, RNG> {
    val (intN, rng2) = rng.nextInt()
    val (doubleN, rng3) = double(rng)

    return (intN to doubleN) to rng3
}

fun doubleInt(rng: RNG): Pair<Pair<Double, Int>, RNG> {
    val (doubleN, rng2) = double(rng)
    val (intN, rng3) = rng.nextInt()

    return (doubleN to intN) to rng3
}

fun double3(rng: RNG): Pair<Triple<Double, Double, Double>, RNG> {
    val (doubleN1, rng2) = double(rng)
    val (doubleN2, rng3) = double(rng2)
    val (doubleN3, rng4) = double(rng3)

    return Triple(doubleN1, doubleN2, doubleN3) to rng4
}

fun ints(count: Int, rng: RNG): Pair<ListE<Int>, RNG> {
    return if (count > 0) {
        val (i, r1) = rng.nextInt()
        val (xs, r2) = ints(count -1, r1)
        Cons(i, xs) to r2
    } else Nil to rng
}

typealias Rand<A> = (RNG) -> Pair<A, RNG>
fun <A,B> map(s: Rand<A>, f: (A) -> B): Rand<B> = { rng ->
    val (a, rng2) = s(rng)
    f(a) to rng2
}

// 6.5
fun doubleR(): Rand<Double> = map({ rng -> rng.nextInt() }) { i ->
        ((i - 1) / Int.MAX_VALUE).toDouble()
    }

// 6.6
fun <A, B, C> map2(
    ra: Rand<A>,
    rb: Rand<B>,
    f: (A, B) -> C
): Rand<C> = { rng ->
    val (a, rng2) = ra(rng)
    val (b, rng3) = rb(rng2)
    f(a, b) to rng3
}

// 6.7
fun <A> sequence(fs: ListE<Rand<A>>): Rand<ListE<A>> = { rng ->
    foldRight(fs, Nil as ListE<A> to rng) { f, (list, nextRng) ->
        val (nextItem, nRng) = f(nextRng)

        Int.MAX_VALUE
        Cons(nextItem, list) to nRng
    }
}

// 6.8
fun <A, B> flatMap(f: Rand<A>, g: (A) -> Rand<B>): Rand<B> = { rng ->
    val (a, nextRng) = f(rng)
    g(a)(nextRng)
}

fun nonNegativeIntLestThan(n: Int): Rand<Int> = flatMap(::nonNegativeInt) { i ->
    val mod = i % n
    if (i + n - 1 - mod >= 0) {
        { rng ->  mod to rng}
    }
    else nonNegativeIntLestThan(n)
}


// 6.9
fun <A,B> mapF(s: Rand<A>, f: (A) -> B): Rand<B> = flatMap(s) { a ->
    { fng -> f(a) to fng}
}


// 6.10
data class State<S, out A>(val run: (S) -> Pair<A, S>) {

    companion object {
        fun <S, A> unit(item: A): State<S, A> = State { state ->
            item to state
        }

        fun <S, A, B, C> map2(
            ra: State<S, A>,
            rb: State<S, B>,
            f: (A, B) -> C
        ): State<S, C> = State { state ->
            val (a, state1) = ra.run(state)
            val (b, state2) = rb.run(state1)
            f(a, b) to state2
        }

    }

    fun <B> map(f: (A) -> B): State<S, B> = State { state ->
        val (item, newState) = run(state)
        f(item) to newState
    }

    fun <B> flatMap( f: (A) -> State<S, B>): State<S, B> = State { state ->
        val (item, newState) = run(state)
        f(item).run(newState)
    }

}

// 6.11
sealed class Input
object Coin: Input()
object Turn: Input()

data class Machine(
    val locked: Boolean,
    val candies: Int,
    val coins: Int,
)

fun simulateMachine1(input: Input): State<Machine, Pair<Int, Int>> = State { machine ->
    if (machine.candies == 0) Pair(machine.coins, 0) to machine
    else when(input) {
        Coin -> {
            val newMachine = if (machine.locked) {
                machine.copy(locked = false, coins = machine.coins + 1)
            } else {
                machine
            }

            Pair(newMachine.coins, newMachine.candies) to newMachine
        }
        Turn -> {
            val newMachine = if (machine.locked.not()) machine.copy(locked = true, candies = machine.candies - 1)
            else machine
            Pair(newMachine.coins, newMachine.candies) to newMachine
        }
    }
}

fun simulateMachine(inputs: ListE<Input>): State<Machine, Pair<Int, Int>> = State { machine ->
    foldLeft(inputs, Pair(machine.coins, machine.candies) to machine) { acc, input ->
        val (_, m) = acc
        simulateMachine1(input).run(m)
    }
}



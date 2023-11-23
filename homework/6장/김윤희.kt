// 6.1
fun nonNegativeInt(rng: RNG): Pair<Int, RNG> {
    val (value, newRng) = rng.nextInt()
    if (value == Int.MIN_VALUE) return Int.MAX_VALUE to newRng
    return if (value < 0) 0 to newRng else value to newRng
}

// 6.2
fun double(rng: RNG): Pair<Double, RNG> {
    val (value, newRng) = nonNegativeInt(rng)
    return if (value > 1) {
        (Int.MAX_VALUE - 1).toDouble() / Int.MAX_VALUE.toDouble() to newRng
    } else {
        value.toDouble() to newRng
    }
}

// 6.3
fun intDouble(rng: RNG): Pair<Pair<Int, Double>, RNG> {
    val (intVal, newRng) = nonNegativeInt(rng)
    val (doubleVal, newRng2) = double(newRng)
    return (intVal to doubleVal) to newRng2
}

fun doubleInt(rng: RNG): Pair<Pair<Double, Int>, RNG> {
    val (doubleVal, newRng) = double(rng)
    val (intVal, newRng2) = nonNegativeInt(newRng)
    return (doubleVal to intVal) to newRng2
}

fun double3(rng: RNG): Pair<Triple<Double, Double, Double>, RNG> {
    val (doubleVal, newRng) = double(rng)
    val (doubleVal2, newRng2) = double(newRng)
    val (doubleVal3, newRng3) = double(newRng2)
    return Triple(doubleVal, doubleVal2, doubleVal3) to newRng3
}

// 6.4
fun ints(count: Int, rng: RNG): Pair<List<Int>, RNG> {
    if (count == 0) return (List.empty<Int>() to rng)

    val (intVal, newRng) = nonNegativeInt(rng)
    val (intList, newRng2) = ints(count - 1, newRng)

    return Cons(intVal, intList) to newRng2
}

// 6.5
fun doubleR(): Rand<Double> =
    map(::nonNegativeInt) { it / (Int.MAX_VALUE.toDouble() + 1) }

// 6.6
fun <A, B, C> map2(
    ra: Rand<A>,
    rb: Rand<B>,
    f: (A, B) -> C
): Rand<C> = { rng ->
    val (a, newRng) = ra(rng)
    val (b, newRng2) = rb(newRng)
    f(a, b) to newRng2
}

// 6.7
fun <A> sequence(fs: List<Rand<A>>): Rand<List<A>> =
    when (fs) {
        is Nil -> unit(Nil)
        is Cons -> map2(fs.head, sequence(fs.tail)) { a, b -> Cons(a, b) }
    }

fun <A> sequence2(fs: List<Rand<A>>): Rand<List<A>> =
    foldRight(fs, unit(List.empty())) { a, listA -> map2(a, listA) { x, y -> Cons(x, y) } }

fun ints2(count: Int, rng: RNG): Pair<List<Int>, RNG> {
    val nonNegativeIntFunc: Rand<Int> = map(::nonNegativeInt) { it }

    fun ints2Func(count: Int): List<Rand<Int>> {
        if (count == 0) return List.empty()
        return Cons(nonNegativeIntFunc, ints2Func(count - 1))
    }

    return sequence(ints2Func(count))(rng)
}

// 6.8
fun <A, B> flatMap(f: Rand<A>, g: (A) -> Rand<B>): Rand<B> = { rng ->
    val (a, newRng) = f(rng)
    g(a)(newRng)
}

fun nonNegativeIntLessThan(n: Int): Rand<Int> =
    flatMap(::nonNegativeInt) { intValue ->
        val mod = intValue % n
        if (intValue + (n - 1) - mod >= 0) unit(mod) else nonNegativeIntLessThan(n)
    }

// 6.9
fun <A, B> mapF(ra: Rand<A>, f: (A) -> B): Rand<B> =
    flatMap(ra) { a -> unit(f(a)) }

fun <A, B, C> map2F(
    ra: Rand<A>,
    rb: Rand<B>,
    f: (A, B) -> C
): Rand<C> =
    flatMap(ra) { a -> flatMap(rb) { b -> unit(f(a, b)) } }

// 6.10
data class State<S, out A>(val run: (S) -> Pair<A, S>) {

    companion object {
        fun <S, A> unit(a: A): State<S, A> = State { s -> a to s }

        fun <S, A, B, C> map2(
            ra: State<S, A>,
            rb: State<S, B>,
            f: (A, B) -> C
        ): State<S, C> =
            State { s ->
                val (a, newRng) = ra.run(s)
                val (b, newRng2) = rb.run(newRng)
                f(a, b) to newRng2
            }

        fun <S, A> sequence(fs: List<State<S, A>>): State<S, List<A>> =
            foldRight(fs, unit(List.empty())) { a, listA -> map2(a, listA) { x, y -> Cons(x, y) } }
    }

    fun <B> map(f: (A) -> B): State<S, B> = flatMap { a -> unit(f(a)) }

    fun <B> flatMap(f: (A) -> State<S, B>): State<S, B> = State { s ->
        val (a, s2) = this.run(s)
        f(a).run(s2)
    }
}

// 6.11
val update: (Input) -> (Machine) -> Machine =
    { input ->
        { machine ->
            if (machine.candies == 0) machine
            else {
                when (input) {
                    is Coin ->
                        if (!machine.locked) machine
                        else Machine(false, machine.candies, machine.coins + 1)

                    is Turn ->
                        if (machine.locked) machine
                        else Machine(true, machine.candies - 1, machine.coins)
                }
            }
        }
    }

fun simulateMachine(
    inputs: List<Input>
): State<Machine, Tuple2<Int, Int>> =
    State.fx(Id.monad()) {
        inputs
            .map(update)
            .map(StateApi::modify)
            .stateSequential()
            .bind()
        val s = StateApi.get<Machine>().bind()
        Tuple2(s.candies, s.coins)
    }

// 4.1
sealed class Option<out A> {
    fun <B> map(f: (A) -> B): Option<B> = when (this) {
        is Some -> Some(f(this.value))
        None -> None
    }

    fun <B> flatMap(f: (A) -> Option<B>): Option<B> = when (this) {
        is Some -> f(this.value)
        None -> None
    }

    fun getOrElse(default: () -> A): A = when (this) {
        is Some -> this.value
        None -> default()
    }

    fun orElse(ob: () -> Option<@UnsafeVariance A>): Option<A> = when (this) {
        is Some -> this
        None -> ob()
    }

    fun filter(f: (A) -> Boolean): Option<A> = when (this) {
        is Some -> if (f(this.value)) this else None
        None -> None
    }
}

data class Some<out A>(val value: A) : Option<A>()
object None : Option<Nothing>()

// 4.2
fun variance(xs: List<Double>): Option<Double> {
    val mean = mean(xs)
    return mean.flatMap { m ->
        val squaredDifferences = xs.map { x ->
            (x - m).pow(2)
        }
        val variance = squaredDifferences.sum() / xs.size()
        Some(variance)
    }
}

// 4.3
fun <A, B, C> map2(a: Option<A>, b: Option<B>, f: (A, B) -> C): Option<C> {
    return when (a) {
        is Some -> {
            when (b) {
                is Some -> Some(f(a.get(), b.get()))
                is None -> None
            }
        }
        is None -> None
    }
}

// 4.4
fun <A> sequence(xs: List<Option<A>>): Option<List<A>> {
    val elements = xs.map { option ->
        when (option) {
            is Some -> option.get()
            is None -> return None
        }
    }
    return Some(List.of(*elements.toTypedArray()))
}

// 4.5
fun <A, B> traverse(xa: List<A>, f: (A) -> Option<B>): Option<List<B>> {
    val transformedList = xa.map { a -> f(a) }
    return if (transformedList.all { it is Some }) {
        Some(List.of(*transformedList.map { (it as Some).get() }.toTypedArray()))
    } else {
        None
    }
}

// 4.6
sealed class Either<out E, out A> {
    // Implement the Either class as you have it in your code.

    fun <B> map(f: (A) -> B): Either<E, B> =
        when (this) {
            is Right -> Right(f(this.value))
            is Left -> this
        }

    fun <B> flatMap(f: (A) -> Either<E, B>): Either<E, B> =
        when (this) {
            is Right -> f(this.value)
            is Left -> this
        }

    fun orElse(f: () -> Either<E, A>): Either<E, A> =
        when (this) {
            is Right -> this
            is Left -> f()
        }

    companion object {
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
    }
}

class Right<out A>(val value: A) : Either<Nothing, A>()
class Left<out E>(val value: E) : Either<E, Nothing>()

// 4.7
fun <E, A, B> traverse(xs: List<A>, f: (A) -> Either<E, B>): Either<E, List<B>> {
    val transformedList = xs.map { a ->
        f(a)
    }

    val error = transformedList.find { it is Left } as? Left
    return error?.value ?: Right(List.of(*transformedList.map { (it as Right).value }.toTypedArray()))
}

// 4.8
sealed class Partial<out A, out B>

data class Failures<out A>(val get: List<A>) : Partial<A, Nothing>()
data class Success<out B>(val get: B) : Partial<Nothing, B>()

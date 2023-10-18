// ex1
fun <A, B> Option<A>.map(f: (A) -> B): Option<B> = when (this) {
  is None -> this
  is Some -> Some(f(this.get))
}

fun <A, B> Option<A>.flatMap(f: (A) -> Option<B>): Option<B> = when (this) {
  is None -> None
  is Some -> f(this.get)
}

fun <A> Option<A>.getOrElse(default: () -> A): A = when (this) {
  is None -> default()
  is Some -> this.get
}

fun <A> Option<A>.orElse(ob: () -> Option<A>): Option<A> = when (this) {
  is None -> ob()
  is Some -> this
}

fun <A> Option<A>.filter(f: (A) -> Boolean): Option<A> = when (this) {
  is None -> None
  is Some -> if (f(this.get)) this else None
}

// ex2
fun mean(xs: List<Double>): Option<Double> =
    if (xs.isEmpty()) None
    else Some(xs.sum() / xs.size())

fun variance(xs: List<Double>): Option<Double> = mean(xs).flatMap { m ->
    mean(
        map(xs) { x -> (x - m).pow(2) }
    )
}

// ex3
fun <A, B, C> map2(a: Option<A>, b: Option<B>, f: (A, B) -> C): Option<C> =
    a.flatMap { aa ->
        b.map { bb ->
            f(aa, bb)
        }
    }

// ex4
fun <A> sequence(
    xs: List<Option<A>>
): Option<List<A>> =
    xs.foldRight(Some(Nil)) { oa1: Option<A>, oa2: Option<List<A>> ->
        map2(oa1, oa2) { a1: A, a2: List<A> ->
            Cons(a1, a2)
        }
    }

// ex5
fun <A, B> traverse(
  xa: List<A>,
  f: (A) -> Option<B>
): Option<List<B>> =
  when (xa) {
      is Cons ->
          map2(f(xa.head), traverse(xa.tail, f)) { b, xb ->
              Cons(b, xb)
          }
      is Nil -> Some(Nil)
  }

fun <A> sequence(xs: List<Option<A>>): Option<List<A>> =
  traverse(xs) { it }

fun <A> catches(a: () -> A): Option<A> =
  try {
      Some(a())
  } catch (e: Throwable) {
      None
  }

// ex6
sealed class Either<out E, out A>

data class Left<out E>(val value: E) : Either<E, Nothing>()

data class Right<out A>(val value: A) : Either<Nothing, A>()

fun <E, A, B> Either<E, A>.map(f: (A) -> B): Either<E, B> =
    when (this) {
        is Left -> this
        is Right -> Right(f(this.value))
    }

fun <E, A> Either<E, A>.orElse(f: () -> Either<E, A>): Either<E, A> =
    when (this) {
        is Left -> f()
        is Right -> this
    }

fun <E, A, B> Either<E, A>.flatMap(f: (A) -> Either<E, B>): Either<E, B> =
    when (this) {
        is Left -> this
        is Right -> f(this.value)
    }

fun <E, A, B, C> map2(
    ae: Either<E, A>,
    be: Either<E, B>,
    f: (A, B) -> C
): Either<E, C> =
    ae.flatMap { a -> be.map { b -> f(a, b) } }

// ex7
fun <E, A, B> traverse(
        xs: List<A>,
        f: (A) -> Either<E, B>
    ): Either<E, List<B>> =
        when (xs) {
            is Nil -> Right(Nil)
            is Cons ->
                map2(f(xs.head), traverse(xs.tail, f)) { b, xb ->
                    Cons(b, xb)
                }
        }

    fun <E, A> sequence(es: List<Either<E, A>>): Either<E, List<A>> =
        traverse(es) { it }

    fun <A> catches(a: () -> A): Either<String, A> =
        try {
            Right(a())
        } catch (e: Throwable) {
            Left(e.message!!)
        }

// ex8
sealed class Partial<out A, out B>

data class Failures<out A>(val get: List<A>) : Partial<A, Nothing>()

data class Success<out B>(val get: B) : Partial<Nothing, B>()

/////////////////////////// 1


fun <A, B> Option<A>.map(f: (A) -> B): Option<B>  {
    return when(this) {
        is None -> None
        is Some -> Some(f(this.get))
    }
}



fun <A, B> Option<A>.flatMap(f: (A) -> Option<B>): Option<B>  {
    return when(this) {
        is None -> None
        is Some -> f(this.get)
    }
}


fun <A> Option<A>.getOrElse(default: () -> A): A {
    return when(this) {
        is None -> default()
        is Some -> this.get
    }
}

fun <A> Option<A>.orElse(ob: () -> Option<A>): Option<A> {
    return when(this) {
        is None -> ob()
        is Some -> this
    }
}


fun <A> Option<A>.filter(f: (A) -> Boolean): Option<A> {
    return when(this) {
        is None -> None
        is Some -> when(f(this.get)) {
            true -> this
            false -> None
        }
    }
}




/////////////////// 2


fun variance(xs: List<Double>): Option<Double> =
        mean(xs).flatMap { m ->
            mean(xs.map { x -> (x -m).pow(2)})
        }


//////////////////// 3

fun <A, B, C> map2(oa: Option<A>, ob: Option<B>, f: (A, B) -> C): Option<C> =
        oa.flatMap { a: A->
            ob.map {  b: B ->
                f(a, b)
            }
        }


/////////////////////// 4


fun <A> sequence(xs: List<Option<A>>): Option<List<A>> =
        xs.foldRight(Some(Nil)) { oi1: Option<A>, oi2: Option<List<A>> ->
            map2(oi1, oi2) { i1: A, i2: List<A> -> Cons(i1, i2)}
        }



///////////////////////// 5

fun <A, B> traverse(
        xa: List<A>,
        f: (A) -> Option<B>
): Option<List<B>> =
        when(xa) {
            is Nil -> Some(Nil)
            is Cons -> map2(f(xa.head), traverse(xa.tail, f)) {head, tail ->
                Cons(head, tail)
            }
        }


/////////////////////////// 6

fun <A, B> traverse(
        xa: List<A>,
        f: (A) -> Option<B>
): Option<List<B>> =
        when(xa) {
            is Nil -> Some(Nil)
            is Cons -> map2(f(xa.head), traverse(xa.tail, f)) {head, tail ->
                Cons(head, tail)
            }
        }

///////////////////////////// 7
fun <E, A, B> traverse(
        xs: List<A>,
        f: (A) -> Either<E, B>
): Either<E, List<B>> =
        when(xs) {
            is Nil -> Right(Nil)
            is Cons -> map2(f(xs.head), traverse(xs.tail, f)) { head, tail -> Cons(head, tail) }
        }


fun <E, A> sequence(es: List<Either<E, A>>): Either<E, List<A>> = traverse(es) { it }
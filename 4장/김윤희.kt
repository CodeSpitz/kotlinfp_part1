// 4.1
fun <A, B> Option<A>.map(f: (A) -> B): Option<B> =
    when (this) {
        is None -> None
        is Some -> Some(f(this.get))
    }  // Option이 None이 아닌 경우 f를 적용해 A 타입 값을 B 타입으로 변환함

fun <A, B> Option<A>.flatMap(f: (A) -> Option<B>): Option<B> =
    when (this) {
        is None -> None
        is Some -> f(this.get)
    } // Option이 None이 아닌 경우, 실패할 수도 있는 f를 적용해 A 타입 값을 B 타입으로 변환함

fun <A> Option<A>.getOrElse(default: () -> A): A =
    when (this) {
        is None -> default()
        is Some -> this.get
    } // Option이 None인 경우 디폴트 값을 반환함

fun <A> Option<A>.orElse(ob: () -> Option<A>): Option<A> =
    this.map(::Some).getOrElse { ob() } // Option이 None인 경우 디폴트 옵션을 반환함

fun <A> Option<A>.filter(f: (A) -> Boolean): Option<A> =
    this.flatMap { a -> if (f(a)) Some(a) else None } // 술어 f를 만족하지 않으면 Some을 None으로 변환함

// 4.2
fun variance(xs: List<Double>): Option<Double> =
    mean(xs).flatMap { m ->
        mean(xs.map { x ->
            (x - m).pow(2)
        })
    }

// 4.3
fun <A, B, C> map2(optionA: Option<A>, optionB: Option<B>, f: (A, B) -> C): Option<C> =
    optionA.flatMap { a ->
        optionB.map { b ->
            f(a, b)
        }
    }

// 4.4
fun <A> sequence(xs: List<Option<A>>): Option<List<A>> =
    foldRight(xs, Some(List.empty())) { optionA: Option<A>, optionB: Option<List<A>> ->
        map2(optionA, optionB) { a: A, b: List<A> -> Cons(a, b) }
    }

// 4.5
fun <A, B> traverse(
    xa: List<A>,
    f: (A) -> Option<B>
): Option<List<B>> =
    foldRight(xa, Some(List.empty())) { a: A, optionB: Option<List<B>> ->
        map2(f(a), optionB) { b: B, listB: List<B> -> Cons(b, listB) }
    }

fun <A> sequence(xs: List<Option<A>>): Option<List<A>> =
    traverse(xs) { it }

// 4.6
fun <E, A, B> Either<E, A>.map(f: (A) -> B): Either<E, B> =
    when (this) {
        is Left -> this
        is Right -> Right(f(this.value))
    }

fun <E, A, B> Either<E, A>.flatMap(f: (A) -> Either<E, B>): Either<E, B> =
    when (this) {
        is Left -> this
        is Right -> f(this.value)
    }

fun <E, A> Either<E, A>.orElse(f: () -> Either<E, A>): Either<E, A> =
    when (this) {
        is Left -> f()
        is Right -> this
    }

fun <E, A, B, C> map2(
    ae: Either<E, A>,
    be: Either<E, B>,
    f: (A, B) -> C
): Either<E, C> =
    ae.flatMap { a -> be.map { b -> f(a, b) } }

// 4.7
fun <E, A, B> traverse(
    xs: List<A>,
    f: (A) -> Either<E, B>
): Either<E, List<B>> =
    foldRight(xs, Right(List.empty())) { a: A, eitherB: Either<E, List<B>> ->
        map2(f(a), eitherB) { b: B, listB: List<B> -> Cons(b, listB) }
    }

fun <E, A> sequence(es: List<Either<E, A>>): Either<E, List<A>> =
    traverse(es) { it }

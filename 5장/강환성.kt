import java.util.Collections.reverse

sealed class StreamC5<out A: Any> {
    companion object {
        fun <A: Any> cons(hd: ()-> A, tl: ()-> StreamC5<A>): StreamC5<A> {
            val head: A by lazy(hd)
            val tail: StreamC5<A> by lazy(tl)
            return ConsC5({head}, {tail})
        }
    }
}

data class ConsC5<out A: Any>(
    val head: ()-> A,
    val tail: ()-> StreamC5<A>
): StreamC5<A>()

object EmptyC5: StreamC5<Nothing>()

fun <A: Any> StreamC5<A>.headOption(): Option<A>
= when(this) {
    is EmptyC5-> Option.None
    is ConsC5-> Option.Some(head())
}

fun <A: Any> cons(hd: ()-> A, tl: ()-> StreamC5<A>): StreamC5<A> {
    val head: A by lazy(hd)
    val tail: StreamC5<A> by lazy(tl)
    return ConsC5({head}, {tail})
}

fun <A: Any> empty(): StreamC5<A> = EmptyC5
fun <A: Any> of(vararg xs: A): StreamC5<A>
= if(xs.isEmpty()) empty()
    else cons({xs[0]}, {of(*xs.sliceArray(1 until xs.size))})

/*
연습문제 5.1
Stream을 List로 변환하는 함수를 작성하라. 이 함수는 스트림의 모든 값을 강제 계산해
REPL에서 결과를 관찰할 수 있게 해준다. 스트림을 3장에서 개발한 단일 연결 List로 변환
해도 된다. 그리고 이 함수나 다른 도우미 함수를 Stream의 확장 메서드로 작성해도 좋다.

이 함수를 구현할 때 스택 안전성을 고려하라. List에서 구현한 다른 메서드를 사용하거나
꼬리 재귀 제거를 고려하라.
*/
// 교제
fun <A: Any> StreamC5<A>.toListUnsafe(): FList<A>
= when(this) {
    is EmptyC5 -> FList.Nil
    is ConsC5 -> ConsL(head(), tail().toListUnsafe())
}
fun <A: Any> StreamC5<A>.toList(): FList<A> {
    tailrec fun go(xs: StreamC5<A>, acc: FList<A>)
    = when(xs) {
        is EmptyC5 -> acc
        is ConsC5 -> go(xs.tail(), ConsL(xs.head(), acc))
    }
    return reverse(go(this, NilL))
}

/*
연습문제 5.2
Stream의 맨 앞에서 원소를 n개 반환하는 take(n)과 맨 앞에서 원소를 n개 건너뛴 나머지
스트림을 돌려주는 drop(n)을 작성하라
 */
fun <ITEM: Any> StreamC5<ITEM>.take(n: Int): StreamC5<ITEM> {
    fun go(xs: StreamC5<ITEM>, n: Int): StreamC5<ITEM>
    = when(xs) {
        is EmptyC5 -> empty()
        is ConsC5 ->
            if(n == 0) empty()
            else cons(xs.head, {go(xs.tail(), n - 1)})
    }

    return go(this, n)
}
fun <ITEM: Any> StreamC5<ITEM>.drop(n: Int): StreamC5<ITEM> {
    tailrec fun go(xs: StreamC5<ITEM>, n: Int): StreamC5<ITEM>
    = when(xs) {
        is EmptyC5 -> empty()
        is ConsC5 ->
            if(n == 0) xs
            else go(xs.tail(), n - 1)
    }

    return go(this, n)
}

/*
연습문제 5.3
주어진 술어와 일치하는 모든 접두사(맨 앞부터 조건을 만족하는 연속된 원소들)를
돌려주는 takeWhile을 작성하라.

REPL에서 스트림을 관찰하기 위해 take와 toList를 함께 사용할 수 있다.
예를 들어 Stream.of(1, 2, 3).take(2).toList()를 출력해보라.
단위 테스트에서 단언식을 작성할 때도 이 기법이 유용하다.
 */
// == 교제 ==
fun <ITEM: Any> StreamC5<ITEM>.takeWhile(p: (ITEM)-> Boolean): StreamC5<ITEM>
= when(this) {
    is EmptyC5 -> empty()
    is ConsC5 ->
        if(p(this.head()))
            cons(head, {this.tail().takeWhile(p)})
        else empty()
}

// == 교제 ==
fun <A: Any> StreamC5<A>.exists(p: (A)-> Boolean): Boolean
= when(this) {
    is ConsC5-> p(head()) || tail().exists(p)
    else -> false
}
fun <A:Any, B: Any> StreamC5<A>.foldRight(
    z: () -> B,
    f: (A, ()-> B)-> B
): B
= when(this) {
    is ConsC5 -> f(head()) {
        tail().foldRight(z, f)
    }
    is EmptyC5 -> z()
}
fun <A: Any> StreamC5<A>.exists2(p: (A)-> Boolean): Boolean
= foldRight({false}, {a, b -> p(a) || b()})

/*
연습문제 5.4
Stream의 모든 원소가 주어진 술어를 만족하는지 검사하는 forAll을 구현하라.
여러분의 구현은 술어를 만족하지 않는 값을 만나자마자
순회를 최대한 빨리 중단해야만 한다.
 */
// 교제
fun <A: Any> StreamC5<A>.forAll(p: (A) -> Boolean): Boolean
= foldRight({true}, {a, b -> p(a) && b()})

/*
연습문제 5.5
foldRight를 사용해 takeWhile을 구현하라
 */
// 교제
fun <A: Any> StreamC5<A>.takeWhile3(p: (A) -> Boolean): StreamC5<A>
= foldRight({empty()}, {h, t -> if(p(h)) cons({h}, t) else t()})
/*
연습문제 5.6
어려움: foldRight를 사용해 headOption을 구현하라.
 */
// 교제
fun <A: Any> StreamC5<A>.headOption3(): Option<A>
= foldRight({Option.none()}, {a, _ -> Option(a)})
/*
연습문제 5.7
foldRight를 사용해 map, filter, append를 구현하라.
append 메서드는 인자에 대해 엄격하지 않아야만 한다.
필요하면 앞에서 정의한 함수를 사용할지 고려해보라.
 */
// 교제
fun <A: Any, B: Any> StreamC5<A>.map3(f: (A) -> B): StreamC5<B>
= foldRight({empty<B>()}, {h, t -> cons({f(h)}, t)})
fun <A: Any> StreamC5<A>.filter3(f: (A)-> Boolean): StreamC5<A>
= foldRight(
    {empty()},
    {h, t -> if(f(h)) cons({h}, t) else t()}
)
fun <A: Any> StreamC5<A>.append3(sa: () -> StreamC5<A>): StreamC5<A>
= foldRight(sa) {h, t -> cons({h}, t)}
fun <A: Any, B: Any> StreamC5<A>.flatMap(f: (A) -> StreamC5<B>): StreamC5<B>
= foldRight(
    {empty<B>()},
    {h, t -> f(h).append3(t)}
)

/*
연습문제 5.8
ones를 약간 일반화해서 정해진 값으로 이뤄진 무한 Stream을 동려주는
constant 함수를 작성하라.
 */
// == 교제 ==
fun <A: Any> constant(a: A): StreamC5<A>
= StreamC5.cons({a}, {constant(a)})

/*
연습문제 5.9
n부터 시작해서 n+1, n+2 등을 차례로 내놓는 무한한 정수 스트림을
만들어내는 함수를 작성하라(코틀린에서 Int 타입은 32비트 부호가 있는 정수이므로
이 스트림은 약 40억 개의 정수를 주기적으로 반복하면서 양수와 음수를 오간다).
 */
// == 교제 ==
fun from(n: Int): StreamC5<Int> = cons({n}, {from(n + 1)})

/*
연습문제 5.10
0, 1, 1, 2, 3, 5, 9 처럼 변하는 무한한 피보나치 수열을 만들어내는 fibs 함수를 작성하라.
 */
// == 교제 ==
fun fibs(): StreamC5<Int> {
    fun go(curr: Int, nxt: Int): StreamC5<Int>
    = cons({curr}, {go(nxt, curr + nxt)})
    return go(0, 1)
}

/*
연습문제 5.11
unfold 라는 더 일반적인 스트림 구성 함수를 작성하라.
이 함수는 초기 상태를 첫 번째 인자로 받고, 현재 상태로 부터 다음 상태와
스트림상의 다음 값을 만들어내는 함수를 두 번째 인자로 받는다.
 */
fun <A: Any, S: Any> unfold(z: S, f: (S)-> Option<Pair<A, S>>): StreamC5<A>
= f(z).map { pair ->
    cons({pair.first}, {unfold(pair.second, f)})
}.getOrElse {
    empty()
}

/*
연습문제 5.12
unfold를 사용해 fibs, from, constant, ones를 구현하라.
재귀적 버전에서 fun ones(): Stream<Int> = Stream.cons({1}, {ones()})로
공유를 사용했던 것과 달리 unfold를 사용해 constant와 ones를 정의하면 공유를 쓰지 않게 된다.
재귀 정의는 순회를 하는 동안에도 스트림에 대한 참조를 유지하기 때문에 메모리를 상수로 소비하지만,
unfold 기반 구현은 그렇지 않다. 공유 유지는 극히 미묘하며 타입을 통해 추적하기 어려우므로,
스트림을 사용해 프로그래밍을 할 때 일반적으로 의존하는 특성은 아니다.
예를 들어 단순히 xs.map {x -> x}를 호출해도 공유가 깨진다.
 */
// == 교제 ==
fun fibs12(): StreamC5<Int>
= unfold(0 to 1) {(curr, next) ->
    Option.Some(curr to (next to (curr + next)))
}
fun from12(n: Int): StreamC5<Int>
= unfold(n, {a -> Option.Some(a to (a + 1))})
fun <A: Any> constant12(n: A): StreamC5<A>
= unfold(n, {a -> Option.Some(a to a)})
fun ones12(): StreamC5<Int>
= unfold(1, {Option.Some(1 to 1)})

/*
연습문제 5.13
unfold를 사용해 map, take, takeWhile, zipWith(3장 참고), zipAll을 구현하라.
zipAll 함수는 두 스트림 중 한쪽에 원소가 남아 있는 한 순회를 계속해야 하며,
각 스트림을 소진했는지 여부를 표현하기 위해 Option을 사용한다.
 */
fun <A: Any, B: Any> StreamC5<A>.map13(f: (A) -> B): StreamC5<B>
= unfold(this) {s: StreamC5<A> ->
    when(s) {
        is ConsC5 -> Option.Some(f(s.head()) to s.tail())
        else -> Option.None
    }
}

fun <A: Any> StreamC5<A>.take13(n: Int): StreamC5<A>
= unfold(this) {s: StreamC5<A> ->
    when(s) {
        is ConsC5 ->
            if(n > 0)
                Option.Some(s.head() to s.tail().take(n - 1))
            else Option.None
        else -> Option.None
    }
}

fun <A: Any> StreamC5<A>.takeWhile13(p: (A) -> Boolean): StreamC5<A>
= unfold(this) {s: StreamC5<A> ->
    when(s) {
        is ConsC5 ->
            if(p(s.head()))
                Option.Some(s.head() to s.tail())
            else Option.None
        else -> Option.None
    }
}

fun <A: Any, B: Any, C: Any> StreamC5<A>.zipWith13(
    that: StreamC5<B>,
    f: (A, B) -> C
): StreamC5<C>
= unfold(this to that) {(ths: StreamC5<A>, tht: StreamC5<B>) ->
    when(ths) {
        is ConsC5 ->
            when(tht) {
                is ConsC5 ->
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

fun <A: Any, B: Any> StreamC5<A>.zipAll(
    that: StreamC5<B>
): StreamC5<Pair<Option<A>, Option<B>>>
= unfold(this to that) {(ths, tht) ->
    when(ths) {
        is ConsC5 -> when(tht) {
            is ConsC5 -> Option.Some(
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
            is ConsC5 -> Option.Some(
                Pair(
                    Option.None to Option.Some(tht.head()),
                    empty<A>() to tht.tail()
                )
            )
            else -> Option.None
        }
    }
}

/*
연습문제 5.14
어려움: 이전에 작성한 함수를 사용해 startWith를 구현하라.
이 함수는 어떤 Stream이 다른 Stream의 접수사인지 여부를 검사해야 한다.
예를 들어 Stream(1, 2, 3).startWith(Stream(1, 2))는 true 다.

팁: 이 문제는 이번 장의 앞부분에서 unfold를 사용해 개발한 함수만을 사용해 구현할 수 있다.
 */
fun <A: Any> StreamC5<A>.startWith(that: StreamC5<A>): Boolean
= zipAll(that)
    .takeWhile { !it.second.isEmpty() }
    .forAll { it.first == it.second }

/*
연습문제 5.15
unfold를 사용해 tails를 구현하라. tails는 주어진 Stream의 모든 접미사를 돌려준다.
이때 원래 Stream과 똑같은 스트림을 가장 먼저 돌려준다. 예를 들어 Stream.of(1, 2, 3)에 대해
tails는 Stream.of(Stream.of(1,2,3)), Stream.of(2,3), Stream.of(3), Stream.empty())를
반환 한다.
 */
fun <A: Any> StreamC5<A>.tails(): StreamC5<StreamC5<A>>
= unfold(this) {s: StreamC5<A> ->
    when(s) {
        is ConsC5 -> Option.Some(s to s.tail())
        else -> Option.None
    }
}

/*
연습문제 5.16
어려움/선택적: tails를 일반화해서 scanRight를 만들라. scanRight는 foldRight와 마찬가지로
중간 결과로 이뤄진 스트림을 반환한다. 예를 들면 다음과 같다.
>>> Stream.of(1, 2, 3).scanRight(0, {a, b -> a+b}).toList()

이 예제는 List.of(1+2+3+0, 2+3+0, 3+0, 0)이라는 식과 같다. 여러분의 함수는 중간 결과를
재사용해서 원소가 n개인 Stream을 순회하는 데 걸린 시간이 n에 선형적으로 비례해야만 한다.
unfold를 사용해 이 함수를 구현할 수 있을까? 구현할 수 있다면 어떻게 구현할 수 있고,
구현할 수 없다면 왜 구현할 수 없을까? 여러분이 지금까지 작성한 다른 함수를 사용해
이 함수를 구현할 수 있을까?
 */
fun <A: Any, B: Any> StreamC5<A>.scanRight(z: B, f: (A, () -> B) -> B): StreamC5<B>
= foldRight({z to of(z)}) {a: A, p0: () -> Pair<B, StreamC5<B>> ->
    val p1: Pair<B, StreamC5<B>> by lazy {p0()}
    val b2: B = f(a) {p1.first}
    Pair<B, StreamC5<B>>(b2, cons({b2}, {p1.second}))
}.second

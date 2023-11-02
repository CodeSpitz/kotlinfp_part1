/*
출석체크용 과제제출. 부록B 참고 
 */


/*
연습문제 5.1
스트림관찰을 위한 도우미 함수1

Stream을 List로 변환하는 함수 : 스트림의 모든 값을 강제 계산해 REPL에서 결과 관찰
- 스트림 > 단일 연결 List(3장)로 변환가능
- stream의 확장 메서드 작성가능
* 스택 안전성 고려
* List에서 구현한 다른 메서드를 사용하거나 꼬리 재귀 제거 고려

fun <A> Stream<A>.toList(): List<A> =
    S0LUTI0N_HERE()
*/

/*안전미흡! 나이브한 해법은 스택 오버플로 발생.*/
fun <A> Stream<A>.toListUnsafe(): List<A> = when (this) {
    is Empty -> NilL
    is Cons -> ConsL(this.head(), this.tail().toListUnsafe())
}

/*tailrec을 reverse와 함께 사용 : 더 안전한 구현 가능*/
fun <A> Stream<A>.toList(): List<A> {
    tailrec fun go(xs: Stream<A>, acc: List<A>): List<A> = when (xs) {
        is Empty -> acc
        is Cons -> go(xs.tail(), ConsL(xs.head(), acc))
    }
    return reverse(go(this, NilL))
}



/*
연습문제 5.2
스트림관찰을 위한 도우미 함수2

drop(n) 작성
 - Stream 맨 앞 원소 n개 반환 : take(n)
 - 맨 앞 원소 n개 건너뛴 나머지 스트림 반환

fun <A> Stream<A>.take(n: Int): Stream<A> =
    SOLUTION_HERE()
fun <A> Stream<A>.drop(n: Int): Stream<A> =
    SOLUTION_HERE()
*/

fun <A> Stream<A>.take(n: Int): Stream<A> {
    fun go(xs: Stream<A>, n: Int): Stream<A> = when (xs) {
        is Empty -> empty()
        is Cons ->
            if (n == 0) empty()
            else cons(xs.head, { go(xs.tail(), n - 1) })
    }
    return go(this, n)
}
fun <A> Stream<A>.drop(n: Int): Stream<A> {
    tailrec fun go(xs: Stream<A>, n: Int): Stream<A> = when (xs) {
        is Empty -> empty()
        is Cons ->
            if (n == 0) xs
            else go(xs.tail(), n - 1)
    }
    return go(this, n)
}



/*
연습문제 5.3
스트림관찰을 위한 도우미 함수3

주어진 술어와 일치히는 모든 접두사(맨 앞부터 조건을 만족하는 연속된 원소들)를 돌려주는 takeWhile 작성
REPL에서 스트림을 관찰하기 위해 take와 toList 함께 사용 가능
예)Stream.of(1, 2, 3).take(2) .toList() 출력
 단위 테스트에서 단언식[assertion expression]을 작성할 때 - 유용
*/

fun <A> Stream<A>.takeWhile(p: (A) -> Boolean): Stream<A> =
    when (this) {
        is Empty -> empty()
        is Cons ->
            if (p(this.head()))
                cons(this.head, { this.tail().takeWhile(p) })
            else empty()
    }

/*
연습문제 5.4

Stream의 모든 원소가 술어를 만족히는지 검사하는 forAll 구현
 - 구현은 술어를 만족하지 않는 값을 만나자마자 순회를 최대한 빨리 중단

fun <A> Stream<A>.forAll(p: (A) -> Boolean): Boolean =
    SOLUTION_HERE()
*/

fun <A> Stream<A>.forAll(p: (A) -> Boolean): Boolean =
    foldRight({ true }, { a, b -> p(a) && b() })



/*
연습문제 5.5

foldRight로 takeWhile 구현
*/

fun <A> Stream<A>.takeWhile(p: (A) -> Boolean): Stream<A> =
    foldRight({ empty() },
        { h, t -> if (p(h)) cons({ h }, t) else t() })


/*
연습문제 5.6

어려움: foldRight로 headOption 구현
*/

fun <A> Stream<A>.headOption(): Option<A> =
    this.foldRight(
        { Option.empty() },
        { a, _ -> Some(a) }
    )


/*
연습문제 5.7

foldRight로 map, filter, append 구현
 - 인자에 대해 관대할것
 - 필요시 선행 함수 사용 고려
*/

fun <A, B> Stream<A>.map(f: (A) -> B): Stream<B> =
    this.foldRight(
        { empty<B>() },
        { h, t -> cons({ f(h) }, t) })

fun <A> Stream<A>.filter(f: (A) -> Boolean): Stream<A> =
    this.foldRight(
        { empty<A>() },
        { h, t -> if (f(h)) cons({ h }, t) else t() })

fun <A> Stream<A>.append(sa: () -> Stream<A>): Stream<A> =
    foldRight(sa) { h, t -> cons({ h }, t) }

fun <A, B> Stream<A>.flatMap(f: (A) -> Stream<B>): Stream<B> =
    foldRight(
        { empty<B>() },
        { h, t -> f(h).append(t) })


/*
연습문제 5.8

ones를 약간 일반화 - 정해진 값으로 이뤄진 무한 Stream을 돌려주는 constant 함수 작성

fun <A> constant(a: A): Stream<A> =
    SOLUTION_HERE()
*/

fun <A> constant(a: A): Stream<A> =
    Stream.cons({ a }, { constant(a) })



/*
연습문제 5.9

n부터 시작해서 n + 1, n + 2 등 차례로 내놓는 무한한 정수 스트림 생성 함수 작성
 - 코틀린에서 Int 타입은 32비트 부호가 있는 정수
   약 40억 개의 양수와 음수를 오가며 정수 반복

fun from(n: Int): Stream<Int> =
    SOLUTION_HERE()
*/

fun from(n: Int): Stream<Int> =
    cons({ n }, { from(n + 1) })



/*
연습문제 5.10

fibs 함수 작성
 - 0, 1, 1, 2, 3, 5, 9처럼 변하는 무한 피보나치수열 생성

fun fibs(): Stream<Int> =
    SOLUTION_HERE()
*/

fun fibs(): Stream<Int> {
    fun go(curr: Int, nxt: Int): Stream<Int> =
        cons({ curr }, { go(nxt, curr + nxt) })
    return go(0, 1)
}


/*
연습문제 5.11

unfold라는 더 일반적인 스트림 구성 함수 작성
 - 첫 번째 인자 : 초기 상태
 - 두 번째 인자 : 현재 상태로부터 다음 상태와 스트림상의 다음 값을 만들어내는 함수

fun <A, S> unfold(z: S, f: (S) -> Option<Pair<A, S>>): Stream<A> =
    SOLUTION_HERE()
*/

fun <A, S> unfold(z: S, f: (S) -> Option<Pair<A, S>>): Stream<A> =
    f(z).map { pair ->
        cons({ pair.first },
            { unfold(pair.second, f) })
    }.getOrElse {
        empty()
    }

/*
연습문제 5.12

unfold로 fibs, from, constant, ones 구현
 - (재귀적 버전에서 fun ones(): Stream<Int> = Stream.cons({ 1 }, { ones() })로 공유를 사용햇던 것과 달리)
 - unfold로 constant와 ones 정의 : 공유 미사용
   (재귀 정의 : 순회하는 동안에도 스트림에 대한 참조 유지 : 메모리를 상수로 소비)
 - unfold 기반 구현 : 메모리를 상수로 소비 안함
   . 공유유지 : 극히미묘, 타입을 통해 추적하기 어려움 - 스트림을 사용한 프로그래밍 시, 일반적으로 의존하는 특성은 아님
예) 단순히 xs.map { x -> x } 호출도 공유가 깨짐
*/

fun fibs(): Stream<Int> =
    Stream.unfold(0 to 1, { (curr, next) ->
        Some(curr to (next to (curr + next)))
    })

fun from(n: Int): Stream<Int> =
    Stream.unfold(n, { a -> Some(a to (a + 1)) })

fun <A> constant(n: A): Stream<A> =
    Stream.unfold(n, { a -> Some(a to a) })

fun ones(): Stream<Int> =
    Stream.unfold(1, { Some(1 to 1) })


/*
연습문제 5.13

unfold 사용 map, take, takeWhile, zipWith(3장 참고), zipAll 구현
 - zipAll 함수 : 두 스트림 중 한쪽에 원소가 남아 있는 한 순회 계속
 - 각 스트림 소진 여부 표현 : Option 사용

fun <A, B> Stream<A>.map(f: (A) -> B): Stream<B> =
    SOLUTION_HERE()
fun <A> Stream<A>.take(n: Int): Stream<A> =
    SOLUTION_HERE()
fun <A> Stream<A>.takeWhile(p: (A) -> Boolean): Stream<A> =
    SOLUTION_HERE()
fun <A, B, C> Stream<A>.zipWith(
    that: Stream<B>,
    f: (A, B) -> C
): Stream<C> =
    SOLUTION_HERE()
fun <A, B> Stream<A>.zipAll(
    that: Stream<B>
): Stream<Pair<Option<A>, Option<B>>> =
    SOLUTION_HERE()
*/

fun <A, B> Stream<A>.map(f: (A) -> B): Stream<B> =
    Stream.unfold(this) { s: Stream<A> ->
        when (s) {
            is Cons -> Some(f(s.head()) to s.tail())
            else -> None
        }
    }

fun <A> Stream<A>.take(n: Int): Stream<A> =
    Stream.unfold(this) { s: Stream<A> ->
        when (s) {
            is Cons ->
                if (n > 0)
                    Some(s.head() to s.tail().take(n - 1))
                else None
            else -> None
        }
    }

fun <A> Stream<A>.takeWhile(p: (A) -> Boolean): Stream<A> =
    Stream.unfold(this,
        { s: Stream<A> ->
            when (s) {
                is Cons ->
                    if (p(s.head()))
                        Some(s.head() to s.tail())
                    else None
                else -> None
            }
        })

fun <A, B, C> Stream<A>.zipWith(
    that: Stream<B>,
    f: (A, B) -> C
): Stream<C> =
    Stream.unfold(this to that) { (ths: Stream<A>, tht: Stream<B>) ->
        when (ths) {
            is Cons ->
                when (tht) {
                    is Cons ->
                        Some(
                            Pair(
                                f(ths.head(), tht.head()),
                                ths.tail() to tht.tail()
                            )
                        )
                    else -> None
                }
            else -> None
        }
    }

fun <A, B> Stream<A>.zipAll(
    that: Stream<B>
): Stream<Pair<Option<A>, Option<B>>> =
    Stream.unfold(this to that) { (ths, tht) ->
        when (ths) {
            is Cons -> when (tht) {
                is Cons ->
                    Some(
                        Pair(
                            Some(ths.head()) to Some(tht.head()),
                            ths.tail() to tht.tail()
                        )
                    )
                else ->
                    Some(
                        Pair(
                            Some(ths.head()) to None,
                            ths.tail() to Stream.empty<B>()
                        )
                    )
            }
            else -> when (tht) {
                is Cons ->
                    Some(
                        Pair(
                            None to Some(tht.head()),
                            Stream.empty<A>() to tht.tail()
                        )
                    )
                else -> None
            }
        }
    }


/*
연습문제 5.14


startsWith 구현 - 어려움: 이전 작성 함수 사용
 - 어떤 Stream이 다른 Stream의 접두사인지 여부 검사
예) Stream(1, 2, 3).startsWith(Stream(1, 2)) : true

fun <A> Stream<A>.startsWith(that: Stream<A>): Boolean =
    SOLIT「ION_HERE()

팁 : unfold로 개발한 함수만 사용 구현 가능
*/

fun <A> Stream<A>.startsWith(that: Stream<A>): Boolean =
    this.zipAll(that)
        .takeWhile { !it.second.isEmpty() }
        .forAll { it.first == it.second }


/*
연습문제 5.15

unfold로 tails 구현
 - tails : Stream의 모든 접미사 응답.
 - 원래 Stream과 같은 스트림을 가장 먼저 회신
예) Stream.of(1, 2, 3)에 대해 tails는
 - Stream.of(Stream.of(1, 2, 3), Stream.of(2, 3), Stream.of(3), Stream.empty()) 반환

fun <A> Stream<A>.tails(): Stream<Stream<A>> =
    SOLUTION_HERE()
*/

fun <A> Stream<A>.tails(): Stream<Stream<A>> =
    Stream.unfold(this) { s: Stream<A> ->
        when (s) {
            is Cons ->
                Some(s to s.tail())
            else -> None
        }
    }


/*
연습문제 5.16

여러움/선택적 : tails 일반화 > scanRight 작성
 - scanRight : foldRight와 마찬가지로 중간 결과로 이뤄진 스트림 반환
예)
>>> Stream.of(1, 2, 3).scanRight(0, { a, b -> a + b }).toList()

res1: chapter3.List<kotlin.Int> =
    Cons(head=6,tail=Cons(head=5,tail=Cons(head=3,tail=Cons(head=0,tail=Nil))))

-> List.of(l+2+3+0, 2+3+0, 3+0, 0)
 중간결과를 재사용해서 원소가 n개인 Stream을 순회하는 데 걸린 시간이 n에 선형적으로 비례해 한다.
-> unfold로 구현 가능?
*/

fun <A, B> Stream<A>.scanRight(z: B, f: (A, () -> B) -> B): Stream<B> =
    foldRight({ z to Stream.of(z) },
        { a: A, p0: () -> Pair<B, Stream<B>> ->
            val p1: Pair<B, Stream<B>> by lazy { p0() }
            val b2: B = f(a) { p1.first }
            Pair<B, Stream<B>>(b2, cons({ b2 }, { p1.second }))
        }).second

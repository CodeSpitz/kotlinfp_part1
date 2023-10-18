/*
출석체크용 과제제출. 부록B 참고 
 */

/*
연문제.4.1
Option에 대한 모든 함수 구현
각 함수 구현 시 의미와 어떤 상황에서 사용할지, 언제 사용할지 검토
- 매칭 사용( map과 getOrElse 이외의 모든 함수 매칭 없이 구현)
- map과 flatMap의 경우 타입 시그니처만 구현 결정
- getOrElse는 Option에 따라, Some인 경우 결과 반환, None인 경우 디폴트값 반환
- orElse :
 > 첫 번째 Option의 값이 정의된 경우(Some), 그 Option 반환
 > 그렇지 않은 경우, 두 번째 Option 반환
*/
fun <A, B> Option<A>.map(f: (A) -> B): Option<B> =
    when (this) {
        is None -> None
        is Some -> Some(f(this.get))
    }

fun <A> Option<A>.getOrElse(default: () -> A): A =
    when (this) {
        is None -> default()
        is Some -> this.get
    }

fun <A, B> Option<A>.flatMap(f: (A) -> Option<B>): Option<B> =
    this.map(f).getOrElse { None }

fun <A> Option<A>.orElse(ob: () -> Option<A>): Option<A> =
    this.map { Some(it) }.getOrElse { ob() }

fun <A> Option<A>.filter(f: (A) -> Boolean): Option<A> =
    this.flatMap { a -> if (f(a)) Some(a) else None }

/*다른 접근 방법*/

fun <A, B> Option<A>.flatMap_2(f: (A) -> Option<B>)： Option<B> =
    when (this) {
        is None -> None
        is Some -> f(this.get)
    }

fun <A> Option<A>.orElse_2(ob: () -> Option<A>): Option<A> =
    when (this) {
        is None -> ob()
        is Some -> this
    }

fun <A> Option<A>.filter_2(f: (A) -> Boolean): Option<A> =
    when (this) {
        is None -> None
        is Some ->
            if (f(this.get)) this
            else None
    }


/*
연습문제 4.2
flatMap 사용 variance 함수 구현
시퀀스 평균 m - 분산은 시퀀스의 원소를 x라 할 때 x-m을 제곱한 값의 평균
> (x - m).pow(2)
(리스트 4.2에서 만든) mean 메서드로 함수 구현
fun variance(xs: List<Double>): Option<Double> =
    S0LUTI0N_HERE()

리스트 4.2의 mean 메서드를 사용한다.
 */
fun mean(xs: List<Double>): Option<Double> =
    if (xs.isEmpty()) None
    else Some(xs.sum() / xs.size())

fun variance(xs: List<Double>): Option<Double> =
    mean(xs).flatMap { m ->
        mean(xs.map { x ->
            (x - m).pow(2)
        })
    }

/*
연습문제 4.3
map2 작성 :두 Option 값을 이항 함수를 통해 조합하는 제네릭 함수
 - 두 Option 중 하나라도 None이면 반환값도 None

[map2의 시그니처]
fun <A, B, C> map2(a: Option<A>, b: Option<B>, f: (A, B) -> C): Option<C> =
    SOLUTION_HERE()
*/

fun <A, B, C> map2(
    oa: Option<A>,
    ob: Option<B>,
    f: (A, B) -> C
): Option<C> =
    oa.flatMap { a ->
        ob.map { b ->
            f(a, b)
        }
    }

/*
연습문제 4.4
원소가 Option인 리스트를 원소가 리스트인 Option으로 합쳐주는 sequence 함수 작성
 - 반환되는 Option의 원소는 원래 리스트에서 Some인 값들만 모은 리스트
 - 원래 리스트안에 None이 단 하나라도 있으면 결과값이 None
 - 그렇지 않으면 모든 정상 값이 모인 리스트가들어 있는 Some이 결과값

 [시그니처]
 fun <A> sequence(xs: List<Option<A>>): Option<List<A>> =
    SOLUTION_HERE()

객체지향 스타일로 함수를 작성히는 게 적합하지 않다고 확실히 알 수 있는경우
이 함수는 List의 메서드가 돼서는 안 되고 Option의 메서드가 될 수도 없다.
(List는 Option에 대해 알 필요가 없어야만 함)
이 함수를 Option의 동반 객체 안에 넣어야 함
*/

fun <A> sequence(
    xs: List<Option<A>>
): Option<List<A>> =
    xs.foldRight(Some(Nil),
        { oa1: Option<A>, oa2: Option<List<A>> ->
            map2(oa1, oa2) { a1: A, a2: List<A> ->
                Cons(a1, a2)
            }
        })

/*
연습문제 4.5
traverse 함수 구현
(map을 한 다음에 sequence를 하면 간단 하지만,)
리스트를 단 한번만 순회하는 더 효율적인 구현 시도
 - sequence를 traverse를 사용해 구현하라.

fun <A, B> traverse(
    xa: List<A>,
    f: (A) -> Option<B>
): Option<List<B>> =

    SOLUTION_HERE()
*/

fun <A, B> traverse(
    xa: List<A>,
    f: (A) -> Option<B>
): Option<List<B>> =
    when (xa) {
        is Nil -> Some(Nil)
        is Cons ->
            map2(f(xa.head), traverse(xa.tail, f)) { b, xb ->
                Cons(b, xb)
            }
    }

fun <A> sequence(xs: List<Option<A>>): Option<List<A>> =
    traverse(xs) { it }



/*
연습문제 4.6
Right 값에 대해 활용할 수 있는 map, flatMap, orElse, map2 구현

fun <E, A, B> Either<E, A>.map(f: (A) -> B): Either<E, B> =
    SOLUTION_HERE()

fun <E, A, B> Either<E, A>.flatMap(f: (A) -> Either<E, B>): Either<E, B> =
    SOLUTION_HERE()

fun <E, A> Either<E, A>.orElse(f: () -> Either<E, A>): Either<E, A> =
    SOLUTION_HERE()

fun <E, A, B, C> map2(
    ae: Either<E, A>,
    be: Either<E, B>,
    f: (A, B) -> C
): Either<E, C> =
    SOLUTION_HERE()
*/

fun <E, A, B> Either<E, A>.map(f: (A) -> B): Either<E, B> =
    when (this) {
        is Left -> this
        is Right -> Right(f(this.value))
    }

fun <E, A> Either<E, A>.orElse(f: () -> Either<E, A>): ERher<E, A> =
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


/*
연습문제 4.7
Either에 대한 sequence와 traverse를 구현
 - 두 함수는 오류가 생긴 경우 최초로 발생한오류를 반환
*/
fun <E, A, B> traverse(
    xs: List<A>>
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

/*
연습문제 4.8
(리스트 4.8 기준) 이름과 나이가 모두 잘못되더라도 map2가 오류를 하나만 보고
두 오류를 모두 보고하게 하려면?
 - map2나 mkPerson의 시그니처?
 - 새로운 데이터 타입? (Either보다 이 추가 요구 사항을 더 잘 다룰 수 있는 추가 구조를 포함하는)
 > 새로운 데이터 타입에 대해 orElse, traverse, sequence는 어떻게 다르게 동작해야 할까?

Option과 Either의 변종
여러 오류를 누적시키고 싶은 경우, 실패를 표현하는 데이터 생성자에서 오류 리스트를 유지하게 해주는 새 데이터 타입 사용

애로우 라이브러리 : Validated 타입
 - 가능한 한 오류를 누적시키는 방식의 map, map2, sequence 등 구현 가능
(flatMap에서는 오류를 누적시킬 수 없다. 왜?).
실패 값을 리스트에 누적시킬 필요가 없으며, 그 대신 사용자가 제공하는 이항함수를 사용해 값 누적
map2나 sequence 등의 도우미 함수를 다른 방식으로 구현
 - Either<List<E>,_>를 직접 사용해 오류를 누적
*/
sealed class Partial<out A, out B>

data class Failures<out A>(val get: List<A>) : Partial<A, Nothing>()
data class Success<out B>(val get: B) : Partial<Nothing, B>()

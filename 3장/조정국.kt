package ex03

/*
연습문제 3.1
List의 첫 번째 원소를 제거히는 tail 함수 구현
 - 상수 시간[constant time]에 실행 종료
 - List가 Nil일 때 선택할 수 있는 여러 가지 처리 방법 검토

fun <A> tail(xs: List<A>): List<A> =
    SOLUTION_HERE()
*/

fun <A> tail(xs: List<A>): List<A> =
    when (xs) {
        is Cons -> xs.tail
        is Nil ->
            throw IllegalStateException("Nil cannot have a 'tail'")
    }

/*
연습문제 3.2
 - 연습문제 3.1과 같은 아이디어 사용
 - List의 첫 원소를 다른 값으로 대치하는 setHead함수 작성

 fun <A> setHead(xs: List<A>, x: A): List<A> =
    S0LUTI0N_HERE()

*/

fun <A> setHead(xs： List<A>, x: A)： List<A> =
    when (xs) {
        is Nil ->
        throw IllegalStateException(
            "Cannot replace 'head' of a Nil list"
        )
        is Cons -> Cons(x, xs.tail)
    }


/*
연습문제 3.3
tail을 더 일반화하여 drop 함수 작성
 - drop은 리스트 맨 앞부터 n개 원소를 제거
 - 삭제할 원소의 개수에 비례해 시간이 걸린다
 (따라서 전체 List를 복사할 필요 없다)

 fun <A> drop(l: List<A>, n: Int): List<A> =
    SOLUTION_HERE()

*/

tailrec fun <A> drop(l: List<A>, n: Int): List<A> =
    if (n == 0) 1
    else when (l) {
        is Cons -> drop(l.tail, n - 1)
        is Nil -> throw IllegalStateException(
            "Cannot drop more elements than in list"
        )
    }




/*
연습문제 3.4
dropWhile 구현
 - List 맨 앞부터 주어진 술어를 만족(술어 함수가 true를 반환)하는 연속적인 원소 삭제
  (이 함수는 주어진 술어를 만족하는 접두사를 List에서 제거)

  fun <A> dropWhile(l: List<A>, f: (A) -> Boolean): List<A> =
    SOLUTION_HERE()

*/
tailrec fun <A> dropWhile(l: List<A>으 f: (A) -> Boolean): List<A> =
    when (1) {
        is Cons ->
            if (f(l.head)) dropWhile(l.tail, f) else 1
        is Nil -> 1
    }

/*
연습문제 3.5
코드가 리스트를 연결하는 코드의 경우처럼 항상 제대로 작동하는 것은 아님.
어떤 List에서 마지막 원소를 제외한 나머지 모든 원소로 이뤄진 （순서는 동일한）
새 List를 반환하는 init 함수를 정의
 - 예를 들어 List（1, 2, 3, 4）에 대해 init은 List（l, 2, 3）을 돌려줘야 함.
이 함수를 tail처럼 상수 시간에 구현할 수 없는 이유는 무엇일까?

fun <A> init(l: List<A>): List<A> =
    SOLUTION_HERE()

*/
fun <A> init(l: List<A>): List<A> =
    when (1) {
        is Cons ->
            if (l.tail == Nil) Nil
            else Cons(l.head, init(l.tail))
        is Nil ->
            throw IllegalStateException("Cannot init Nil list")
    }



/*
연습문제 3.6
foldRight로 구현된 product가 리스트 원소로 0.0을 만나면 재귀를 즉시 중단하고 결과를 돌려줄 수 있는가?
즉시 결과를 돌려줄 수 있거나 돌려줄 수 없는 이유?
긴 리스트에 대해 쇼트 서킷을 제공할 수 있으면 어떤 장점이 있을 생각
5장에서는 이질문이 내포하고 있는 의미를 더 자세히 살펴본다
*/

불가능하지 않다!
 - f 함수를 호출하기 전에 함수에 전달할 인자를 평가
 - foldRight의 경우 : 리스트를 맨 마지막까지 순회한다는 뜻
 - 이른 중단을 지원하려면 엄격하지 않은 평가가 필요
 > 5장에서 다룬다

/*
연습문제 3.7
Nil과 Cons를 foldRight에 넘길 때 각각 어떤 일이 벌어지는지 확인.
(Nil as List:<Int> 라고 타입 명시
- 그렇지 않으면 코틀린이 foldRight의 B 타입 파라미터를 List<Nothing>으로 추론한다.)

foldRight(
    Cons(1, Cons(2, Cons(3, Nil))),
    Nil as List<Int>,
    { x, y -> Cons(x, y) }
)

결과가 foldRight와 List 데이터 생성자 사이에 존재하는 관계를 어떻게 보여주는지, 왜그럴지?
이 문맥에서 단순히 Nil을 전딜하는 것 : A의 타입 정보가 부족하기 때문에 충분하지 않음
 - 필요성 : Nil as List<Int>
번잡스러운 표현의 대안 : 동반 객체 안에 이를 회피하기 위한 편의 메서드 추가 가능 - 향후 사용패턴

fun <A> empty(): List<A> = Nil

*/

fun <A, B> foldRight(xs: List<A>, z: B, f: (A, B) -> B): B =
    when (xs) {
        is Nil -> z
        is Cons -> f(xs.head, foldRight(xs.tail, z, f))
    }
val f = { x: Int, y: List<Int> -> Cons(x, y) }
val z = Nil as List<Int>

val trace = {
    foldRight(List.of(1, 2, 3), z, f)
    Cons(1, foldRight(List.of(2, 3), z, f))
    Cons(1, Cons(2, foldRight(List.of(3), z, f)))
    Cons(1, Cons(2, Cons(3, foldRight(List.empty(), z, f))))
    Cons(1, Cons(2, Cons(3, Nil)))
}



/*
연습문제 3.8
foldRight를 사용해 리스트 길이 계산

fun <A> length(xs: List<A>): Int =
    SOLUTION_HERE()

*/
fun <A> length(xs: List<A>): Int =
    foldRight(xs, 0, { _, acc -> 1 + acc })

/*
연습문제 3.9
foldRight는 꼬리 재귀가 아니므로 리스트가 긴 경우 StackOverflowError 발생.
(스택 안전[stack-safe]하지 않다)
 - 스택 안전하지 않은지 확인
 - 다른 리스트 재귀 함수 foldLeft를 2장에서 설명한 기법을 사용해 꼬리 재귀로 작성하라.
 > foldLeft의 시그니처

 tailrec fun <A, B> foldLeft(xs: List<A>, z: B, f: (B, A) -> B): B =
    SOLUTION_HERE()

*/

tailrec fun <A, B> foldLeft(xs: List<A>, z: B, f： (B, A) -> B): B =
when (xs) {
    is Nil -> z
    is Cons -> foldLeft(xs.tail, f(z, xs.head), f)
}

/*
연습문제 3.10
foldLeft를 사용해 sum, product, 리스트 길이 계산 함수 작성
*/

fun sumL(xs: List<Int>): Int =
    foldLeft(xs, 0, { x, y -> x + y })
fun productL(xs: List<Double>): Double =
    foldLeft(xs, 1.0, { x, y -> x * y })
fun <A> lengthL(xs: List<A>): Int =
    foldLeft(xs, 0, { acc, _ -> acc + 1 })

/*
연습문제 3.11
리스트 순서를 뒤집은 새 리스트 반환
 - 함수() 를 작성하라.
 - List(1, 2, 3) > List(3, 2, 1)을 반환
이 함수를 접기 연산을 사용해 작성할 수 있는지 검토
(foldRight와 foldLeft를 합쳐서 접기 연산이라고 한다)
*/

fun <A> reverse(xs: List<A>): List<A> =
    foldLeft(xs, List.empty(), { t: List<A>, h: A -> Cons(h, t) })

/*
연습문제 3.12
어려움: foldLeft를 foldRight를 사용해 작성할 수 있는가?
반대로 foldRight를 foldLeft를 사용해 작성할 수 있는가?
foldRight를 foldLeft로 구현 : foldRight를 꼬리 재귀로 구현할 수 있기 때문에 유용
 - 큰 리스트를 스택 오버플로를 일으키지 않고 foldRight로 처리할 수 있다는 뜻
*/

fun <A, B> foldLeftR(xs: List<A>, z: B, f: (B, A) -> B): B =
    foldRight(
        xs,
        { b: B -> b },
        { a, g ->
            { b ->
                g(f(b, a))
            }
        })(z)
fun <A, B> foldRightL(xs： List<A>, z： B, f: (A, B) -> B): B =
    foldLeft(xs,
        { b: B -> b },
        { g, a ->
            { b ->
                g(f(a, b))
            }
        })(z)

/*
연습문제 3.13
append를 foldLeft나 foldRight를 사용해 구현
*/
fun <A> append(a1: List<A>, a2: List<A>): List<A> =
    foldRight(a1, a2, { x, y -> Cons(X, y) })


/*
연습문제 3.14
어려움: 리스트가 원소인 리스트를 단일 리스트로 연결해주는 함수 작성
 - 이 함수의 실행 시간은 모든 리스트의 길이 합계에 선형으로 비례해야 한다.
 - 지금까지 정의한 함수를 활용하라.
*/

fun <A> concat(xxs: List<List<A>>): List<A> =
    foldRight(
            xxs,
            List.empty(),
            { xs1: List<A>, xs2: List<A> ->
            foldRight(xs1, xs2, { a, ls -> Cons(a, ls) })
        })
fun <A> concat2(xxs: List<List<A>>): List<A> =
    foldRight(
        xxs,
        List.empty(),
        { xs1, xs2 ->
            append(xs1, xs2)
        })

/*
연습문제 3.15
정수로 이뤄진 리스트를 각 원소에 1을 더한 리스트로 변환하는 함수 작성
 - 이 함수는 순수 함수이면서 새 List 반환해야 한다
*/
fun increment(xs: List<Int>): List<Int> =
    foldRight(
        xs,
        List.empty(),
        { i: Int, ls ->
            Cons(i + 1, ls)
        })

/*
연습문제 3.16
List<Double>의 각 원소를 String으로 변환하는 함수 작성
d.toString()을 사용하면 Double 타입인 d를 String으로 변경
*/
fun doubleToString(xs: List<Double>): List<String> =
    foldRight(
        xs,
        List, empty(),
        { d, ds ->
            Cons(d.toString(), ds)
        })


/*
연습문제 3.17
리스트의 모든 원소를 변경하되 리스트 구조는 그대로 유지하는 map 함수 작성
 - map의 시그니처 참고 (표준 리이브러리에서 map과 flatMap은 List의 메서드)

 fun <A, B> map(xs: List<A>, f: (A) -> B): List<B> =
    SOLUTION_HERE()

*/

fun <A, B> map(xs: List<A>, f: (A) -> B): List<B> =
    foldRightL(xs, List.empty()) { a: A, xa: List<B> ->
        Cons(f(a), xa)
    }

/*
연습문제 3.18
리스트에서 주어진 술어를 만족하지 않는 원소 제거
 - filter 함수 작성
 - 함수를 사용해 List<Int>에서 홀수를 모두 제거

fun <A> filter(xs: List<A>, f: (A) -> Boolean): List<A> =
    SOLUTION_HERE()

*/
fun <A> filter(xs: List<A>, f： (A) -> Boolean): List<A> =
    foldRight(
        xs,
        List,empty(),
        {
            a, ls ->
            if (f(a)) Cons(a, ls)
            else ls
        })


/*
연습문제 3.19
map처럼 작동하지만 인자로 (단일 값이 아니라) 리스트를 반환하는 함수를 받는 flatMap 함수 작성
 - 인자로 전달된 함수가 만들어낸 모든 리스트의 원소가 (순서대로) 최종 리스트 안에 삽입
 - flatMap의 시그니처
 예) flatMap(List.of(1, 2, 3), { i -> List.of(i, i) })
  > 결과 : List(1, 1, 2, 2, 3, 3)

fun <A, B> flatMap(xa: List<A>, f: (A) -> List<B>): List<B> =
SOLUTION_HERE()

*/

fun <A, B> flatMap(xa: List<A>, f: (A) -> List<B>): List<B> =
    foldRight(
        xa,
        List.empty(),
        { a, lb ->
            append(f(a), lb)
        })
fun <A, B> flatMap2(xa: List<A>, f: (A) -> List<B>): List<B> =
    foldRight(
        xa,
        List.empty(),
        { a, xb ->
            foldRight(f(a), xb, { b, lb -> Cons(b, lb) })
        })

/*
연습문제 3.20
flatMap을 사용해 filter 구현
*/

fun <A> filter2(xa: List<A>, f: (A) -> Boolean): List<A> =
    flatMap(xa) { a ->
        if (f(a)) List.of(a) else List.empty()
    }


/*
연습문제 3.21
두 리스트를 받아서 서로 같은 위치(인덱스)에 있는 원소들을 더한 값으로 이뤄진 새 리스트를 돌려주는 함수 작성
예) List<1, 2, 3)과 List(4, 5, 6)
 > 결과 : List(5, 7, 9)
*/

fun add(xa: List<Int>, xb: List<Int>): List<Int> =
    when (xa) {
        is Nil -> Nil
        is Cons -> when (xb) {
            is Nil -> Nil
            is Cons ->
                Cons(xa.head + xb.head, add(xa.tall, xb.tail))
        }
    }


/*
연습문제 3.22
연습문제 3.21 에서 작성한 함수를 일반화
정수 타입(리스트의 원소 타입)이나 덧셈(대응하는원소에 작용히는 연산)에 한정되지 않고 다양한 처리가 가능하게 개선.
 - 일반화한 함수이름 : zipWith
*/
fun <A> zipWith(xa: List<A>, xb: List<A>, f: (A, A) -> A)： List<A> =
    when (xa) {
        is Nil -> Nil
        is Cons -> when (xb) {
            is Nil -> Nil
            is Cons -> Cons(
                f(xa.head, xb.head),
                zipWith(xa.tail, xb.tail, f)
            )
        }
    }

/*
연습문제 3,23
어려움: 어떤 List가 다른 List를 부분열로 포함하는지 검사하는 hasSubsequence 구현
예) List(1, 2, 3, 4)의 부분 시퀀스 : Llst(1, 2), List(2, 3), List(4) 등
 - 효율적인 동시에 간결한 순수 함수형 해법 : 쉽지 않음.
 - 5장에서 재시도.
힌트: 코틀린에서 두 값 x와 y가 동등한지 비교 : x == y를 쓴다

tailrec fun <A> hasSubsequence(xs: List<A>, sub: List<A>): Boolean =
    SOLUTION_HERE()

*/

tailrec fun <A> startsWith(l1: List<A>, l2: List<A>): Boolean =
    when (l1) {
        is Nil -> l2 == Nil
        is Cons -> when (l2) {
            is Nil -> true
            is Cons ->
                if (l1.head == l2.head)
                    startsWith(l1.tail, l2.tail)
                else false
        }
    }
tailrec fun <A> hasSubsequence(xs: List<A>, sub: List<A>): Boolean =
    when (xs) {
        is Nil -> false
        is Cons ->
            if (startsWith(xs, sub))
                true
            else hasSubsequence(xs.tail, sub)
    }



/*
연습문제 3.24
트리 안에 들어 있는 노드(잎[leaf]과 가지[branch]를 모두 포함)의 개수 반환
 - size 함수 작성
*/
fun <A> size(tree: Tree<A>): Int =
    when (tree) {
        is Leaf -> 1
        is Branch -> 1 + size(tree.left) + size(tree.right)
    }

/*
연습문제 3.25
Tree<Int>에서 가장 큰 원소를 돌려주는 maximum 함수 작성
 - 힌트: 코틀린은 두 값의 최댓값을 결정해주는 maxOf라는 내장 함수 제공
예)x와 y 중 최대값 : maxOf（x, y）
*/

fun <A> maximum(tree: Tree<A>): Int =
    when (tree) {
        is Leaf -> tree.value
        is Branch -> maxOf(maximum(tree.left), maximum(tree.right))
    }

/*
연습문제 3.26
트리 뿌리root에서 각 잎까지의 경로 중 가장 길이가 긴(간선[edge]의 수가 경로의 길이) 값을 돌려주는 depth 함수 작성
*/
fun depth(tree: Tree<Int>): Int =
    when (tree) {
        is Leaf -> 0
        is Branch -> 1 + maxOf(depth(tree.left), depth(tree.right))
    }

/*
연습문제 3.27
List에 정의했던 map과 대응하는 map 함수를 정의
 - map은 트리의 모든 원소를 주어진 함수를 사용해 변환한 새 함수를 반환
*/

fun <A, B> map(tree: Tree<A>, f: (A) -> B): Tree<B> =
when (tree) {
    is Leaf -> Leaf(f(tree.value))
    is Branch -> Branch(
        map(tree.left, f),
        map(tree.right, f)
    )
}

/*
연습문제 3.28
Tree에서 size, maximum, depth, map을 일반화해 이 함수들의 유사한 점을 추상화한 새로운 fold 함수 작성
 - 더 일반적인 이 fold 함수를 사용해 size, maximum, depth, map을 재구현
 - List의 오른쪽/왼쪽 폴드와 여기서 정의한 fold 사이에 유사점을 찾아보자

fun <A, B> fold(ta: Tree<A>, l: (A) -> B, b: (B, B) -> B): B =
    SOLUTION_HERE()

fun <A> sizeF(ta: Tree<A>): Int =
    SOLUTION_HERE()

fun maximumF(ta: Tree<Int>): Int =
    SOLUTION_HERE()

fun <A> depthF(ta: Tree<A>): Int =
    SOLUTION_HERE()

fun <A, B> mapF(ta: Tree<A>, f: (A) -> B): Tree<B> =
    SOLUTION_HERE()

*/
fun <A, B> fold(ta: Tree<A>, l: (A) -> B, b: (B, B) -> B): B =
    when (ta) {
        is Leaf -> l(ta.value)
        is Branch -> b(fold(ta.left, l, b), fold(ta.right, l, b))
    }
fun <A> sizeF(ta: Tree<A>): Int =
    fold(ta, { 1 }, { b1, b2 -> 1 + b1 + b2 })
fun maximumF(ta: Tree<Int>): Int =
    fold(ta, { a -> a }, { b1, b2 -> maxOf(b1, b2) })
fun <A> depthF(ta: Tree<A>): Int =
    fold(ta, { 0 }, { b1, b2 -> 1 + maxOf(b1, b2) })
fun <A, B> mapF(ta: Tree<A>, f： (A) -> B): Tree<B> =
    fold(ta, { a: A -> Leaf(f(a)) },
        { b1: Tree<B>, b2: Tree<B> -> Branch(b1, b2) })

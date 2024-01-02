package khs

// ========== 3장 연습문제 ==========

import khs.ListF.Nil
import khs.ListF.Cons
sealed class ListF<out ITEM: Any> {
    companion object {
        fun <ITEM: Any> of(vararg aa: ITEM): ListF<ITEM> {
            val tail = aa.sliceArray(1..< aa.size)
            return if(aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
        }
        fun <ITEM: Any> empty():ListF<ITEM> = Nil
        fun <ITEM:Any> cons(head :ITEM, tail :ListF<ITEM>): ListF<ITEM> =Cons(head, tail)
    }
    data object Nil: ListF<Nothing>()
    data class Cons<out ITEM: Any>(val head: ITEM, val tail: ListF<ITEM>): ListF<ITEM>()
}

/**
 * 연습문제 3.1
 * List의 첫 번째 원소를 제거하는 tail 함수를 구현하라.
 * 이 함수는 상수 시산에 실행이 끝나야 한다.
 * List가 Nil일 때 선택할 수 있는 여러 가지 처리 방법을 생각해보라.
 * 다음 장에서 이 경우(함수가 정상 작동하지 못하는 경우)를 다시 살펴본다.
 */
fun <ITEM: Any> ListF<ITEM>.tail(): ListF<ITEM>
= when(this) {
    is Nil-> Nil
    is Cons-> tail
}

/**
 * 연습문제 3.2
 * 연습문제 3.1과 같은 아이디어를 사용해 List의 첫 원소를 다른 값으로 대치하는 setHead함수를 작성하라.
 */
fun <ITEM: Any> ListF<ITEM>.setHead(item: ITEM): ListF<ITEM>
= when(this) {
    is Nil-> Nil
    is Cons-> Cons(item, tail)
}

/**
 * 연습문제 3.3
 * tail을 더 일반화해서 drop 함수를 작성하라.
 * drop은 리스트 맨 앞부터 n개 원소를 제거한다.
 * 이 함수는 삭제할 원소의 개수에 비례해 시간이 걸리다는
 * 사실(따라서 여러분이 전체 List를 복사할 필요가 없다는 사실)을 알아두라.
 */
fun <ITEM: Any> ListF<ITEM>.drop(n: Int): ListF<ITEM>
= when(this) {
    is Nil-> Nil
    is Cons-> goDrop(this, n)
}
private tailrec fun <ITEM: Any> goDrop(list: ListF<ITEM>, n: Int): ListF<ITEM>
= if(n==0) list else goDrop(list.tail(), n-1)

/**
 * 연습문제 3.4
 * dropWhile을 구현하라. 이 함수는 List의 맨 앞에서 부터 주어진 술어를
 * 만족(술어 함수가 true를 반환)하는 연속적인 원소를 삭제한다.
 * (다른 말로 하면, 이 함수는 주어진 술어를 만족하는 접두사를 List에서 제거한다.)
 */
fun <ITEM: Any> ListF<ITEM>.dropWhile(block: (ITEM)-> Boolean): ListF<ITEM>
= when(this) {
    is Nil-> Nil
    is Cons-> {
        if(block(head)) tail.dropWhile(block) else this
    }
}

/**
 * 연습문제 3.5
 * 코드가 리스트를 연결하는 코드의 경우처럼 항상 제대로 작동하는 것은 아니다.
 * 어떤 List에서 마지막 원소를 제외한 나머지 모든 원소로 이뤄진(순서는 동일한)
 * 새 List를 반환하는 init 함수를 정의하라.
 * 예를 들어 List(1,2,3,4)에 대해 init은 List(1,2,3)을 돌려줘야 한다.
 * 이 함수를 tail처럼 상수 시간에 구현할 수 없는 이유는 무엇일까?
 */
fun <ITEM: Any> ListF<ITEM>.init(): ListF<ITEM>
= when(this) {
    is Cons-> if(tail == Nil) Nil else Cons(head, tail.init())
    is Nil-> Nil
}
// tail이 Nil일때 까지 순회 해야 한다.

/**
 * 연습문제 3.6
 * foldRigth로 구현된 product가 리스트 원소로 0.0을 만나면 재귀를 즉시 중단하고 결과를 돌려줄 수 있는가?
 * 즉시 결과를 돌려줄 수 있거나 돌려줄 수 없는 이유는 무엇인가?
 * 긴 리스트에 대해 쇼트 서킷을 제공할 수 있으면 어떤 장점이 있을지 생각해보라.
 * 5장에서는 이 질문이 내포하고 있는 의미를 더 자세히 살펴본다.
 */
// 리스트 3.10 쇼트 서킷을 제거함으로써 product 정규화 하기
fun ListF<Int>.sum(): Int = when(this) {
    is Cons-> head + tail.sum()
    is Nil-> 0
}
fun ListF<Double>.product(): Double = when(this) {
    is Cons-> head * tail.product()
    is Nil-> 1.0
}
// 리스트 3.11 foldRight를 사용해 product와 sum 일반화하기
fun <ITEM:Any, OTHER:Any> ListF<ITEM>.foldRight(acc:OTHER, block:(it:ITEM, acc:OTHER)-> OTHER): OTHER
= when(this) {
    is Cons-> block(head, tail.foldRight(acc, block))
    is Nil-> acc
}
fun ListF<Int>.sumFoldRight(): Int = foldRight(0) { it, acc -> it + acc }
fun ListF<Double>.productFoldRight(): Double = foldRight(1.0) { it, acc -> it * acc }

/**
 * 연습문제 3.7
 * 다음과 같이 Nil과 Cons를 foldRight에 넘길 때 각가 어떤 일이 벌어지는지 살펴보라.
 * (여기서는 Nil as List<Int>라고 타입을 명시해야 한다.
 * 그렇지 않으면 코틀린이 foldRight의 B 타입 파라미터를 List<Nothing>으로 추론한다.)
 */
fun ex0307(list:ListF<Int>): ListF<Int>
= list.foldRight(Nil as ListF<Int>) {it, acc -> Cons(it, acc)}
//fun <ITEM: Any> empty(): ListF<ITEM> = Nil

/**
 * 연습문제 3.8
 * foldRight를 사용해 리스트 길이를 계산하라.
 */
fun <ITEM:Any> ListF<ITEM>.length(): Int = foldRight(0) {_, acc -> acc+1}

/**
 * 연습문제 3.9
 * 우리가 구현한 foldRight는 꼬리 재귀가 아니므로 리스트가 긴 경우 StackOverflowError를 발생시킨다
 * (이를 스택 안전 하지 않다고 말한다). 정말 우리 구현이 스택 안전하지 않은지 확인하라.
 * 그 후 다른 리스트 재귀 함수 foldLeft를 2장에서 설명한 기법을 사용해 꼬리 재귀로 작성하라.
 * 다음은 foldLeft의 시그니처다.
 */
fun <ITEM:Any, OTHER:Any> ListF<ITEM>.foldLeft(acc:OTHER, block:(OTHER, ITEM)-> OTHER): OTHER
= when(this) {
    is Cons-> tail.foldLeft(block(acc, head), block)
    is Nil-> acc
}

/**
 * 연습문제 3.10
 * foldLeft를 사용해 sum, product 리스트 길이 계산 함수를 작성하라.
 */
fun ListF<Int>.sumFoldLeft():Int = foldLeft(0) {acc, it -> acc + it}
fun ListF<Double>.productFoldLeft():Double = foldLeft(1.0) {acc, it -> acc * it}
fun <ITEM:Any> ListF<ITEM>.lengthFoldLeft():Int = foldLeft(0) {acc, _ -> acc + 1}

/**
 * 연습문제 3.11
 * 리스트 순서를 뒤집은 새 리스트를 반환하는 함수(List(1,2,3)이 주어지면 List(3,2,1)을 반환)를 작성하라.
 * 이 함수를 접기 연산(foldRight와 foldLeft를 합쳐서 접기 연산이라고 한다)을 사용해 작성할 수 있는지 살펴보라.
 */
fun <ITEM:Any> ListF<ITEM>.reverse():ListF<ITEM> = foldLeft(ListF.empty()) { acc, it -> Cons(it, acc)}

/**
 * 연습문제 3.12
 * foldLeft를 foldRight를 사용해 작성할 수 있는가?
 * 반대로 foldRight를 foldLeft를 사용해 작성할 수 있는가?
 * foldRight를 foldLeft로 구현하면 foldRight를 꼬리 재귀로 구현 할 수 있기 때문에 유용하다.
 * 이 말은 큰 리스트를 스택 오버플로를 일으키지 않고 foldRight로 처리할 수 있다는 뜻이다.
 */

fun <ITEM:Any, OTHER:Any> ListF<ITEM>.foldLeftR(other:OTHER, block:(OTHER, ITEM)-> OTHER):OTHER
= reverse().foldRight(other) { it, acc -> block(acc, it)}
fun <ITEM:Any, OTHER:Any> ListF<ITEM>.foldRightL(other:OTHER, block:(ITEM, OTHER)-> OTHER):OTHER
= reverse().foldLeft(other) {acc, it -> block(it, acc)}

/**
 * 연습문제 3.13
 * append를 foldLeft나 foldRight를 사용해 구현하라.
 */
fun <ITEM:Any> ListF<ITEM>.append(list:ListF<ITEM>):ListF<ITEM>
= when(this) {
    is Cons-> Cons(head, tail.append(list))
    is Nil-> list
}

/**
 * 연습문제 3.14
 * 리스트가 원소인 리스트를 단일 리스트로 연결해주는 함수를 작성하라.
 * 이 함수의 실행 시간은 모든 리스트의 길이 합계에 선형으로 비례해야 한다.
 * 이 책에서 지금까지 정의한 함수를 활용하라.
 */
fun <ITEM:Any> ListF<ListF<ITEM>>.flatten():ListF<ITEM>
= foldRight(ListF.empty()) {it, acc ->
    it.foldRight(acc) {it2, acc2 -> Cons(it2, acc2)}
}

/**
 * 연습문제 3.15
 * 정수로 이뤄진 리스트를 각 원소에 1을 더한 리스트로 변환하는 함수를 작성하라.
 * 이 함수는 순수 함수이면서 새 List를 반환해야 한다.
 */
fun ListF<Int>.intPlusOne(): ListF<Int>
= foldRight(ListF.empty()) {it, acc -> Cons(it+1, acc)}
//= when(this) {
//    is Cons-> Cons(head+1, tail.intPlusOne())
//    is Nil-> Nil
//}

/**
 * 연습문제 3.16
 * List<Double>의 각 원소를 String으로 변환하는 함수를 작성하라.
 * d.toString()을 사용하면 Double 타입인 d를 String으로 바꿀 수 있다.
 */
fun ListF<Double>.doubleToString(): ListF<String>
= foldRight(ListF.empty()) {it, acc -> Cons(it.toString(), acc)}
//= when(this) {
//    is Cons-> Cons(head.toString(), tail.doubleToString())
//    is Nil-> Nil
//}

/**
 * 연습문제 3.17
 * 리스트의 모든 원소를 변경하되 리스트 구조는 그대로 유지하는 map 함수를 작성하라.
 * 다음은 map의 시그니처다.(표준 라이브러리에서 map과 flatMap은 List의 메서드다.)
 */
fun <ITEM:Any, OTHER:Any> ListF<ITEM>.map(block:(ITEM)-> OTHER): ListF<OTHER>
= foldRight(ListF.empty()) {it, acc -> Cons(block(it), acc)}

/**
 * 연습문제 3.18
 * 리스트에 주어진 술어를 만족하지 않는 원소를 제거해주는 filter 함수를 작성하라.
 * 이 함수를 사용해 List<Int>에서 홀수를 모두 제거하라.
 */
fun <ITEM:Any> ListF<ITEM>.filter(block:(ITEM)-> Boolean): ListF<ITEM>
= foldRight(ListF.empty()) {it, acc -> if(block(it)) Cons(it, acc) else acc}

/**
 * 연습문제 3.19
 * map처럼 작동하지만 인자로(단일 값이 아니라) 리스트를 반환하는 함수를 받는 flatMap 함수를 작성하라.
 * 이때 인자로 전달된 함수가 만들어낸 모든 리스트의 원소가 (순서대로) 최종 리스트 안에 삽입돼야 한다.
 * 다음은 flatMap의 시그니처다.
 */
fun <ITEM:Any, OTHER:Any> ListF<ITEM>.flatMap(block:(ITEM)-> ListF<OTHER>): ListF<OTHER>
= foldRight(ListF.empty()) {it, acc ->
    val other= block(it)
    if(other==Nil) acc else other.foldRight(acc, ::Cons)
}

/**
 * 연습문제 3.20
 * flatMap을 사용해 filter를 구현하라.
 */
fun <ITEM:Any> ListF<ITEM>.flatMapFilter(block:(ITEM)-> Boolean): ListF<ITEM>
= flatMap { if(block(it)) Cons(it, Nil) else Nil }

/**
 * 연습문제 3.21
 * 두 리스트를 받아서 서로 같은 위치(인덱스)에 있는 원소들을 더한 값으로 이뤄진 새 리스트를 돌려주는 함수를 작성하라.
 * 예를 들어 List(1,2,3)과 List(4,5,6)은 List(5,7,9)가 된다.
 */
fun ListF<Int>.zip(list:ListF<Int>): ListF<Int>
= when(this) {
    is Cons-> when(list) {
        is Cons-> Cons(head+list.head, tail.zip(list.tail))
        is Nil-> Nil
    }
    is Nil-> Nil
}

/**
 * 연습문제 3.22
 * 연습문제 3.21에서 작성한 함수를 일반화해 정수 타입(리스트의 원소 타입)이나 덧셈(대응하는 원소에 작용하는 연산)에
 * 한정되지 않고 다양한 처리가 가능하게 하라. 이 일반화한 함수에는 zipWith라는 이름을 붙여라.
 */
fun <ITEM:Any> ListF<ITEM>.zipWith(list: ListF<ITEM>, block:(ITEM, ITEM)-> ITEM): ListF<ITEM>
= when(this) {
    is Cons-> when(list) {
        is Cons-> Cons(block(head, list.head), tail.zipWith(list.tail, block))
        is Nil-> Nil
    }
    is Nil-> Nil
}

/**
 * 연습문제 3.23
 * 어떤 List가 다른 List를 부분열로 포함하는지 검사하는 hasSubsequence를 구현하라.
 * 예를 들어 List(1,2,3,4)의 부분 시퀀스는 List(1,2), List(2,3), List(4)등이 있다.
 * 효율적인 동시에 간결한 순수 함수형 해법을 찾아내기는 어려울 수도 있다.
 * 그래도 괜찮다. 가장 자연스럽게 떠오르는 아이디어로 함수를 구현하라.
 * 5장에서 이 함수 구현을 다시 살펴보면, 바라건대 이를 더 개선할 수 있을 것이다.
 * 힌트: 코틀린에서 두 값 x와 y가 동등한지 비교하려면 x == y를 쓴다.
 */
tailrec fun <ITEM:Any> ListF<ITEM>.hasSubsequence(sub: ListF<ITEM>): Boolean
= when(this) {
    is Cons-> if(startsWith(this, sub)) true else tail.hasSubsequence(sub)
    is Nil-> false
}
tailrec fun <ITEM:Any> startsWith(target:ListF<ITEM>, sub:ListF<ITEM>): Boolean
= when(target) {
    is Cons-> when(sub) {
        is Cons-> if(target.head == sub.head) startsWith(target.tail, sub.tail) else false
        is Nil-> true
    }
    is Nil-> sub == Nil
}


package khs

import khs.TreeF.Leaf
import khs.TreeF.Branch

sealed class TreeF<out ITEM:Any> {
    companion object {
        fun <ITEM:Any> of(v:ITEM)= Leaf(v)
        fun <ITEM:Any> of(l:TreeF<ITEM>, r:TreeF<ITEM>)= Branch(l,r)
    }
    data class Leaf<ITEM:Any>(val value:ITEM): TreeF<ITEM>()
    data class Branch<ITEM:Any>(val left:TreeF<ITEM>, val right:TreeF<ITEM>): TreeF<ITEM>()
}

/**
 * 연습문제 3.24
 * 트리 안에 들어 있는 노드(잎과 가지를 모두 포함)의 개수를 반환하는 size 함수를 작성하라.
 */
fun <ITEM:Any> TreeF<ITEM>.size(): Int
= when(this) {
    is Leaf-> 1
    is Branch-> 1+ left.size()+ right.size()
}

/**
 * 연습문제 3.25
 * Tree<Int>에서 가장 큰 원소를 돌려주는 maximum 함수를 작성하라.
 * 힌트: 코틀린은 두 값의 최댓값을 결정해주는 maxOf라는 내장 함수를 제공한다.
 * 예를 들어 x와 y중 최댓값은 maxOf(x, y)이다.
 */
fun TreeF<Int>.maximum(): Int
= when(this) {
    is Leaf-> value
    is Branch-> maxOf(left.maximum(), right.maximum())
}

/**
 * 연습문제 3.26
 * 트리 뿌리에서 각 잎까지의 경로 중 가장 길이가 긴(간선의 수가 경로의 길이임) 값을 돌려주는 depth함수를 작성하라.
 */
fun <ITEM:Any> TreeF<ITEM>.depth(): Int
= when(this) {
    is Leaf-> 0
    is Branch-> 1+ maxOf(left.depth(), right.depth())
}

/**
 * 연습문제 3.27
 * List에 정의했던 map과 대응하는 map 함수를 정의하라.
 * 이 map은 트리의 모든 원소를 주어진 함수를 사용해 변환한 새 함수를 반환한다.
 */
fun <ITEM:Any, OTHER:Any> TreeF<ITEM>.map(block:(ITEM)-> OTHER): TreeF<OTHER>
= when(this) {
    is Leaf-> Leaf(block(value))
    is Branch-> Branch(left.map(block), right.map(block))
}

/**
 * 연습문제 3.28
 * Tree에서 size, maximum, depth, map을 일반화해 이 함수들의 유사한 점을 추상화한 새로운 fold 함수를 작성하라.
 * 그리고 더 일반적인 이 fold 함수를 사용해 size, maximum, depth, map을 재구현하라.
 * List의 오른쪽/왼쪽 폴드와 여기서 정의한 fold 사이에 유사점을 찾아낼 수 있는가
 */
fun <ITEM:Any, OTHER:Any> TreeF<ITEM>.fold(leaf:(ITEM)->OTHER, branch:(OTHER,OTHER)->OTHER): OTHER
= when(this) {
    is Leaf-> leaf(value)
    is Branch-> branch(left.fold(leaf, branch), right.fold(leaf, branch))
}
fun <ITEM:Any> TreeF<ITEM>.sizeF(): Int = fold({1}) {l, r -> 1+ l+ r}
fun TreeF<Int>.maximumF(): Int = fold({it}) {l, r -> maxOf(l, r)}
fun TreeF<Int>.depthF(): Int = fold({0}) {l, r -> 1+ maxOf(l, r)}
fun <ITEM:Any, OTHER:Any> TreeF<ITEM>.mapF(block:(ITEM)->OTHER): TreeF<OTHER>
= fold({Leaf(block(it))}) {l:TreeF<OTHER>, r:TreeF<OTHER> -> Branch(l, r)}



// ========== 3장 테스트 ==========

package khs

import kotlin.test.Test
import kotlin.test.assertEquals

class Chapter3Test {
    @Test
    fun listTest() {
        val intList = ListF.of(1, 2, 3, 4)
        val doubleList = ListF.of(1.0,2.0,3.0,4.0)

        println("=====> list: $intList")
        assertEquals(intList.tail(), ListF.of(2,3,4))
        assertEquals(intList.setHead(10), ListF.of(10,2,3,4))
        assertEquals(intList.drop(2), ListF.of(3,4))
        assertEquals(intList.dropWhile { it<=2 }, ListF.of(3,4))
        assertEquals(intList.init(), ListF.of(1,2,3))
        assertEquals(intList.sum(), 10)
        assertEquals(doubleList.product(), 24.0)
        assertEquals(intList.foldRight(0) { it, acc -> it + acc}, 10)
        assertEquals(intList.sumFoldRight(), 10)
        assertEquals(doubleList.productFoldRight(), 24.0)
        assertEquals(intList.length(), 4)
        assertEquals(intList.foldLeft(0) { acc, it -> it + acc}, 10)
        assertEquals(intList.sumFoldLeft(), 10)
        assertEquals(doubleList.productFoldLeft(), 24.0)
        assertEquals(intList.lengthFoldLeft(), 4)
        assertEquals(intList.reverse(), ListF.of(4,3,2,1))
        assertEquals(intList.foldRightL(0) { it, acc -> it + acc}, 10)
        assertEquals(intList.foldLeftR(0) {acc, it -> it + acc}, 10)
        assertEquals(intList.append(ListF.of(10,20)), ListF.of(1,2,3,4,10,20))
        assertEquals(ListF.of(ListF.of(1,2), ListF.of(3,4)).flatten(), ListF.of(1,2,3,4))
        assertEquals(intList.intPlusOne(), ListF.of(2,3,4,5))
        assertEquals(doubleList.doubleToString(), ListF.of("1.0","2.0","3.0","4.0"))
        assertEquals(intList.map {"$it"}, ListF.of("1","2","3","4"))
        assertEquals(intList.filter {it%2 == 1}, ListF.of(1,3))
        assertEquals(intList.flatMap {if(it%2 == 1) ListF.of(it) else ListF.empty()}, ListF.of(1,3))
        assertEquals(intList.flatMapFilter {it%2 == 1}, ListF.of(1,3))
        assertEquals(intList.zip(ListF.of(1,2,3,4)), ListF.of(2,4,6,8))
        assertEquals(intList.zipWith(ListF.of(1,2,3,4)) {a, b -> a+b}, ListF.of(2,4,6,8))
        assertEquals(intList.hasSubsequence(ListF.of(1,2)), true)
    }

    @Test
    fun treeTest() {
        val tree= TreeF.of(TreeF.of(1), TreeF.of(TreeF.of(TreeF.of(2), TreeF.of(22)), TreeF.of(3)))
        println("=====> tree: $tree")

        assertEquals(tree.size(), 7)
        assertEquals(tree.maximum(), 22)
        assertEquals(tree.depth(), 3)
        assertEquals(tree.map{"$it"}, TreeF.of(TreeF.of("1"), TreeF.of(TreeF.of(TreeF.of("2"), TreeF.of("22")), TreeF.of("3"))))
        assertEquals(tree.fold({"$it"}) {l, r -> l+r}, "12223")
        assertEquals(tree.sizeF(), 7)
        assertEquals(tree.maximumF(), 22)
        assertEquals(tree.depthF(), 3)
        assertEquals(tree.mapF{"$it"}, TreeF.of(TreeF.of("1"), TreeF.of(TreeF.of(TreeF.of("2"), TreeF.of("22")), TreeF.of("3"))))
    }
}

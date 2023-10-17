sealed class List<out ITEM>
object Nil : List<Nothing>()
data claspackage repo.lec3


sealed class List<out ITEM> {
    companion object {
        fun <ITEM> of(vararg aa: ITEM): List<ITEM> {
            val tail = aa.sliceArray(1 until aa.size)
            return if (aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
        }
    }
}
data object Nil : List<Nothing>()

data class Cons<out ITEM>(val head: ITEM, val tail: List<ITEM>) : List<ITEM>()

fun <ITEM> empty(): List<ITEM> = Nil
sealed class Tree<out ITEM>
data class Leaf<ITEM>(val value: ITEM) : Tree<ITEM>()
data class Branch<ITEM>(val left: Tree<ITEM>, val right: Tree<ITEM>) : Tree<ITEM>()
fun main() {
    val ints = List.of(1, 2, 3, 4)
    println("연습문제 3.1: List 첫 번째 원소 제거하는 tail함수?")
    println(ints.tail())

    println("연습문제 3.2: List 첫 번째 원소 대체하기?")
    println(ints.setHead(5))

    println("연습문제 3.3: 정해진 숫자만큼의 List의 앞 원소들을 제거한다.?")
    println(ints.drop(2))

    println("연습문제 3.4: List의 조건에 만족하는 원소를 제거한다.?")
    println(ints.dropWhile { it > 2 })

    println("연습문제 3.5: List의 마지막 원소를 제거한다.?")
    println(ints.init())

    println("연습문제 3.7: foldRight로 표시해 본다.")
    val aa = Cons(1, Cons(2, Cons(3, Nil))).foldRight(
        Nil as List<Int>
    ) { x, y -> Cons(x, y) }
    println(aa)

    println("연습문제 3.8: List의 size? ${ints.size()}")
    println("연습문제 3.9: foldLeft? ${ints.foldLeft(0) { x, y -> x + y }}")
    println("연습문제 3.10: foldLeft 사용한 sum? ${ints.sum3()}")
    println("연습문제 3.10: foldLeft 사용한 product? ${List.of(1.0,2.0,3.0).product3()}")
    println("연습문제 3.11: List를 reverse 하면? ${ints.revers()}")

    val xs = List.of(List.of(1,2,3), List.of(4,5), List.of(6,7), )
    println("연습문제 3.14: Lists를 list로 만들라? ${xs.listsToList()}")

    println("연습문제 3.15: List의 각 원소에 1을 더한 새로운 리스트는? ${ints.addOneToEachElement()}")

    val xs2 = List.of(1.0,2.0,3.0,4.0)
    println("연습문제 3.16: double list를 문자열 리스트로 변경해라? ${xs2.doubleListToStringList()}")

    println("연습문제 3.17: map으로 List의 각 원소에 1을 더한 새로운 리스트는? ${ints.map{ it + 1}}")
    println("연습문제 3.18: List 에 filter 기능 적용? ${ints.filter{ it > 1}}")
    println("연습문제 3.19: 인자가 List 인 flapMap을 구현하라? ${ints.flatMap{ List.of(it, it)}}")
    println("연습문제 3.20: flapMap을 사용한 List 에 filter 기능 적용? ${ints.filter2{ it > 1}}")

    val list1 = Cons(1, Cons(2, Cons(3, Nil)))
    val list2 = Cons(4, Cons(5, Cons(6, Cons(6, Nil))))
    println("연습문제 3.21: 두 List 원소들의 합의 List를 만들기 ? ${list1.addItemOfLists(list2)}")
    println("연습문제 3.22: 두 List 원소들의 합의 List를 만들기 ? ${list1.zipWith(list2){a, b-> a+b}}")  // 원소의 정소 타입 외 다양한 타입도 가능한지 ?
    println("연습문제 3.23: subList가 List에 포함되는가?${list1.hasSubsequence(list2)}")

    val tree = Branch(
        Branch(Leaf(1), Leaf(2)),
        Branch(Leaf(3), Leaf(4))
    )
    println("연습문제 3.24: tree의 size?${tree.size()}")
    println("연습문제 3.25: tree의 maximum value?${tree.maximum()}")
    println("연습문제 3.26: tree의 depth?${tree.depth()}")
    println("연습문제 3.27: tree의 원소들의 값에 + 1 한다.?${tree.map{it +1}}")

    println("연습문제 3.28: tree의 size?${tree.sizeF()}")
    println("연습문제 3.28: tree의 maximum value?${tree.maximumF()}")
    println("연습문제 3.28: tree의 depth?${tree.depthF()}")
    //println("연습문제 3.28: tree의 원소들의 값에 + 1 한다.?${tree.mapF{it +1}}")

}

// 연습문제 3.1
fun <ITEM> List<ITEM>.tail(): List<ITEM> =
    when (this) {
        is Nil -> Nil
        is Cons -> tail
    }

// 연습문제 3.2
fun <ITEM> List<ITEM>.setHead(x:ITEM): List<ITEM> =
    when (this) {
        is Nil -> Nil
        is Cons -> Cons(x, tail)
    }

// 연습문제 3.3
fun <ITEM> List<ITEM>.drop(n: Int): List<ITEM> =
    when {
        n <= 0 -> this
        this is Cons -> tail.drop(n - 1)
        else -> this
    }

// 연습문제 3.4
fun <ITEM> List<ITEM>.dropWhile(f: (ITEM) -> Boolean): List<ITEM> =
    when (this) {
        is Nil -> Nil
        is Cons -> if(f(head)) tail.dropWhile(f)  else this
    }

// 연습문제 3.5
fun <ITEM> List<ITEM>.init(): List<ITEM> =
    when (this) {
        is Nil -> Nil
        is Cons -> if (tail is Nil) Nil else Cons(head, tail.init())
    }
// 연습문제 3.6
// 설명하라

// 연습문제 3.7
fun <ITEM, RESULT> List<ITEM>.foldRight(acc: RESULT, f: (ITEM, RESULT) -> RESULT): RESULT =
    when (this) {
        is Nil -> acc
        is Cons -> f(head, tail.foldRight(acc, f))
    }

// 연습문제 3.8
fun <ITEM> List<ITEM>.size(): Int =
    when(this) {
        is Nil -> 0
        is Cons -> 1 + tail.size()
    }

// 연습문제 3.9
tailrec fun <ITEM, ACC> List<ITEM>.foldLeft(acc: ACC, f: (ACC, ITEM) -> ACC): ACC =
    when(this){
        is Nil -> acc
        is Cons -> tail.foldLeft(f(acc, this.head), f)
    }

// 연습문제 3.10
fun List<Int>.sum3(): Int =
    foldLeft(0){x, y -> x + y }

fun List<Double>.product3(): Double =
    foldLeft( 1.0) { x, y -> x * y }

// 연습문제 3.11
fun <ITEM> List<ITEM>.revers(): List<ITEM> =
    foldLeft(empty()){ list, item -> Cons(item, list) }

// 연습문제 3.12
// 작업중

// 연습문제 3.13 append
fun <ITEM> List<ITEM>.append(a: List<ITEM>): List<ITEM> =
    foldRight(a){ x, y -> Cons(x, y) })

// 연습문제 3.14 List<List<itme>> -> List<item>
fun <ITEM> List<List<ITEM>>.listsToList(): List<ITEM> =
    when(this){
        is Nil -> Nil
        is Cons -> head.append(tail.listsToList())
    }


// 연습문제 3.15
fun List<Int>.addOneToEachElement(): List<Int> =
    when(this){
        is Nil -> Nil
        is Cons -> Cons(head + 1, tail.addOneToEachElement())
    }

// 연습문제 3.16
fun List<Double>.doubleListToStringList(): List<String> =
    when(this){
        is Nil -> Nil
        is Cons -> Cons(head.toString(), tail.doubleListToStringList())
    }



// 연습문제 3.17
fun <ITEM, ITEM2> List<ITEM>.map(f: (ITEM) -> ITEM2): List<ITEM2> =
    when(this) {
        is Nil -> Nil
        is Cons -> Cons(f(head), tail.map(f))
    }

// 연습문제 3.18
fun <ITEM> List<ITEM>.filter(f: (ITEM) -> Boolean): List<ITEM> =
    when (this) {
        is Nil -> this
        is Cons -> if (f(head)) Cons(head, tail.filter(f)) else tail.filter(f)
    }

// 연습문제 3.19
fun <ITEM, RESULT> List<ITEM>.flatMap(f: (ITEM) -> List<RESULT>): List<RESULT> =
    when (this) {
        is Nil -> Nil // 빈 리스트일 경우 빈 리스트를 반환합니다.
        is Cons -> f(head).append2(tail.flatMap(f)) // Cons일 경우 flatMap을 재귀적으로 호출합니다.
    }

// 연습문제 3.20
fun <ITEM> List<ITEM>.filter2(f: (ITEM) -> Boolean): List<ITEM> =
    flatMap {if (f(it)) Cons(it, Nil) else Nil}

// 연습문제 3.21
fun List<Int>.addItemOfLists(list2: List<Int>): List<Int> =
    when(this){
        is Nil -> Nil
        is Cons -> when(list2) {
            is Nil -> Nil
            is Cons -> Cons(head + list2.head, this.tail.addItemOfLists(list2.tail))
        }
    }

// 연습문제 3.22
fun <ITEM> List<ITEM>.zipWith(list2: List<ITEM>, f: (a:ITEM, b:ITEM)->ITEM): List<ITEM> =
    when(this){
        is Nil -> Nil
        is Cons -> when(list2) {
            is Nil -> Nil
            is Cons -> Cons(f(head, list2.head), this.tail.zipWith(list2.tail, f))
        }
    }


// 연습문제 3.23
tailrec fun <ITEM> List<ITEM>.hasSubsequence(sub: List<ITEM>): Boolean =
    when {
        sub is Nil -> true
        this is Nil || sub.size() > this.size() -> false
        this.startsWith(sub) -> true
        else -> this.tail().hasSubsequence(sub)
    }

fun <ITEM> List<ITEM>.startsWith(sub: List<ITEM>): Boolean =
    when {
        sub is Nil -> true
        this is Nil -> false
        (this is Cons && sub is Cons) && (this.head == sub.head) ->
            this.tail().startsWith(sub.tail())
        else -> false
    }


// 연습문제 3.24
fun <ITEM> Tree<ITEM>.size(): Int =
    when (this) {
        is Leaf -> 1 // Leaf 노드는 1개의 노드로 카운트합니다.
        is Branch -> left.size() + right.size() + 1 // Branch 노드는 자식 노드들의 합에 1을 더합니다.
    }

// 연습문제 3.25
fun Tree<Int>.maximum(): Int =
    when (this) {
        is Leaf -> value
        is Branch -> maxOf(left.maximum(), right.maximum())
    }

// 연습문제 3.26
fun Tree<Int>.depth(): Int =
    when (this) {
        is Leaf -> 0
        is Branch -> {
            val leftDepth = left.depth()
            val rightDepth = right.depth()
            1 + maxOf(leftDepth, rightDepth)
        }
    }

// 연습문제 3.27
fun <ITEM, RESULT> Tree<ITEM>.map(f: (ITEM) -> RESULT): Tree<RESULT> =
    when (this) {
        is Leaf -> Leaf(f(value))
        is Branch -> Branch(left.map(f), right.map(f))
    }

// 연습문제 3.28
fun <ITEM, RESULT> Tree<ITEM>.fold(l: (ITEM) -> RESULT, b: (RESULT, RESULT) -> RESULT): RESULT =
    when (this) {
        is Leaf -> l(value)
        is Branch -> b(left.fold(l, b), right.fold(l, b))
    }

fun <ITEM> Tree<ITEM>.sizeF(): Int =
    fold({ _-> 1}){ leftSize, rightSize -> 1 + leftSize + rightSize}

fun Tree<Int>.maximumF(): Int =
    fold({it}){ leftValue, rightValue -> maxOf(leftValue, rightValue)}

fun Tree<Int>.depthF(): Int =
    fold({_->0}){ leftDepth, rightDepth -> 1 + maxOf(leftDepth, rightDepth)}

//fun <ITEM, RESULT> Tree<ITEM>.mapF(f: (ITEM) -> RESULT): Tree<RESULT> =
//    fold({ Leaf(f(it)) }){ leftMapped, rightMapped -> Branch(leftMapped, rightMapped) }s Cons<out ITEM>(val head: ITEM, val tail: List<ITEM>) : List<ITEM>()
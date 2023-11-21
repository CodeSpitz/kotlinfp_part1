package kore.fd

import kore.fd.FTree.Branch
import kore.fd.FTree.Leaf

sealed class FTree<out ITEM:Any> {
    companion object{
        operator fun <ITEM:Any> invoke(left: FTree<ITEM>, right: FTree<ITEM>): FTree<ITEM> = Branch(left, right)
        operator fun <ITEM:Any> invoke(value:ITEM): FTree<ITEM> = Leaf(value)
    }
    data class Leaf<ITEM:Any>@PublishedApi internal constructor(@PublishedApi internal val item:ITEM): FTree<ITEM>()
    data class Branch<ITEM:Any>@PublishedApi internal constructor(@PublishedApi internal val left: FTree<ITEM>, @PublishedApi internal val right: FTree<ITEM>): FTree<ITEM>()

}
val <ITEM:Any> FTree<ITEM>.size:Int get() = fold({1}){ l, r->1 + l + r}
val <ITEM:Any> FTree<ITEM>.depth:Int get() = fold({1}){ l, r->1 + if(l > r) l else r}
val <ITEM> FTree<ITEM>.max:ITEM where ITEM:Comparable<ITEM>, ITEM:Number get() = fold({it}){ l, r->if(l > r) l else r}
fun <ITEM:Any> FTree<ITEM>.setLeft(tree: FTree<ITEM>): FTree<ITEM> = when(this){
    is Leaf -> Branch(tree, this)
    is Branch -> Branch(tree, right)
}
fun <ITEM:Any> FTree<ITEM>.setRight(tree: FTree<ITEM>): FTree<ITEM> = when(this){
    is Leaf -> Branch(this, tree)
    is Branch -> Branch(left, tree)
}
fun <ITEM:Any, OTHER:Any> FTree<ITEM>.fold(leafBlock:(ITEM)->OTHER, branchBlock:(l:OTHER, r:OTHER)->OTHER):OTHER = when(this){
    is Leaf -> leafBlock(item)
    is Branch -> branchBlock(left.fold(leafBlock, branchBlock), right.fold(leafBlock, branchBlock))
}
fun <ITEM:Any, OTHER:Any> FTree<ITEM>.map(block:(ITEM)->OTHER): FTree<OTHER> =
    fold({FTree(block(it))}){ l, r->FTree(l, r)}
operator fun <ITEM:Any> FTree<ITEM>.contains(item:ITEM):Boolean = fold({it == item}){ l, r-> l || r}

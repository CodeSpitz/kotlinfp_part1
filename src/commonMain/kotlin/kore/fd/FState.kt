package kore.fd

import kore.fd.FList.Cons

fun interface FState<STATE:Any, out VALUE:Any>:(STATE)->Pair<VALUE, STATE>{
    companion object{
        fun <STATE:Any, VALUE:Any> run(block:Run<STATE, VALUE>.()->VALUE): FState<STATE, VALUE>
        = FState {
            val run = Run<STATE, VALUE>(it)
            run.block() to run.state
        }
    }
    class Run<STATE:Any, out VALUE:Any>(var state:STATE){
        operator fun <OTHER:Any> FState<STATE, OTHER>.component1():OTHER{
            val (v, s) = this(state)
            state = s
            return v
        }
    }
    fun <OTHER:Any> flatMap(block:(VALUE)->FState<STATE, OTHER>): FState<STATE, OTHER>
    = FState {
        val (value, state) = this(it)
        block(value)(state)
    }
    fun <OTHER:Any> map(block:(VALUE)->OTHER): FState<STATE, OTHER>
    = flatMap{
        block(it).toState()
    }
    fun <OTHER:Any, RESULT:Any> map2(other: FState<STATE, OTHER>, block:(VALUE, OTHER)->RESULT): FState<STATE, RESULT>
    = flatMap{a->
        other.flatMap {b->
            block(a, b).toState()
        }
    }
}
fun <STATE:Any> FState.Companion.stateToValue():FState<STATE, STATE> = FState{it to it}
fun <STATE:Any, VALUE:Any> VALUE.toState():FState<STATE, VALUE> = FState {this to it}
fun <VALUE:Any> VALUE.toUnitState():FState<VALUE, VALUE> = toState()
fun <STATE:Any, VALUE:Any> FList<FState<STATE, VALUE>>.sequence(): FState<STATE, FList<VALUE>>
= foldRight(FList<VALUE>().toState()){ it, acc->
    acc.map2(it){a, b-> Cons(b, a) }
}
data class IntRandState(val seed:Long):FState<IntRandState, Int>{
    override fun invoke(state: IntRandState): Pair<Int, IntRandState> {
        val newSeed = ((state as IntRand).seed * 0x5DEECE66DL + 0xBL) and 0xFFFFFFFFFFFFL
        return (newSeed ushr 16).toInt() to IntRandState(newSeed)
    }
    operator fun invoke():Pair<Int, IntRandState> = invoke(this)
}

sealed interface Input{
    data object Coin:Input
    data object Turn:Input
    data class Machine(val locked:Boolean, val candies:Int, val coins:Int)
}
fun simulator(inputs:FList<Input>):FState<Input.Machine, Pair<Int, Int>>
= FState {machine->
    if(inputs is Cons) simulator(inputs.tail)(
        if(machine.candies <= 0) machine
        else when(inputs.head){
            is Input.Coin -> if(machine.locked) {
                    if (machine.candies > 0) Input.Machine(false, machine.candies, machine.coins + 1)
                    else machine
                }else machine
            is Input.Turn -> if(!machine.locked) Input.Machine(true, machine.candies - 1, machine.coins)
                else machine
        }
    ) else machine.coins to machine.candies to machine
}
package kore.fd

import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Actor<ITEM:Any, RESULT:Any>(private val block:(ITEM)->RESULT){
    private var isAlive:Boolean = true
    private var isRunning:Boolean = false
    private var executor:ExecutorService = Executors.newFixedThreadPool(1)
    private val result:MutableList<RESULT> = mutableListOf()
    private var queue:ConcurrentLinkedQueue<ITEM> = ConcurrentLinkedQueue()
    private fun run(){
        if(!isAlive || isRunning) return
        isRunning = true
        executor.submit{
            while(!Thread.interrupted() && isAlive){
                val head = queue.poll()
                if(head != null) result.add(block(head))
                else {
                    isRunning = false
                    break
                }
                Thread.sleep(1)
            }
        }
    }
    fun stop(){
        executor.shutdownNow()
        isAlive = false
        isRunning = false
    }
    operator fun invoke(vararg items:ITEM){
        if(!isAlive) throw IllegalStateException("Actor is not alive")
        items.forEach{queue.add(it)}
        run()
    }
    fun result():List<RESULT>{
        while(isRunning) Thread.sleep(1)
        return result
    }
}
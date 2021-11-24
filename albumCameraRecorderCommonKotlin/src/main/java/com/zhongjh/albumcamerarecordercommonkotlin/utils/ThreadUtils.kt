//package com.zhongjh.albumcamerarecordercommonkotlin.utils
//
//import android.os.Handler
//import android.os.Looper
//import androidx.annotation.IntRange
//import java.util.*
//import java.util.concurrent.*
//
///**
// * <pre>
// * author: Blankj
// * blog  : http://blankj.com
// * time  : 2018/05/08
// * desc  : utils about thread
// * update: zhongjh 优化代码
// * update time: zhongjh 转化kotlin 2021/11/23
//</pre> *
// * @author Blankj
// */
//object ThreadUtils {
//
//    val handler = Handler(Looper.getMainLooper())
//    private val TYPE_PRIORITY_POOLS: MutableMap<Int, MutableMap<Int, ExecutorService?>> = HashMap()
//
//    fun isMainThread(): Boolean {
//        return Looper.myLooper() == Looper.getMainLooper()
//    }
//
//    /**
//     * 执行ui线程
//     * @param runnable 事件
//     */
//    fun runOnUiThread(runnable: Runnable) {
//        if (Looper.myLooper() == Looper.getMainLooper()) {
//            // 如果当前线程就是ui线程直接执行事件
//            runnable.run()
//        } else {
//            handler.post(runnable)
//        }
//    }
//
//    /**
//     * 延时执行ui线程
//     * @param runnable 事件
//     * @param delayMillis 时间毫秒
//     */
//    fun runOnUiThreadDelayed(runnable: Runnable, delayMillis: Long) {
//        handler.postDelayed(runnable, delayMillis)
//    }
//
//    /**
//     * 返回一个可以复用并且有固定数量的线程池
//     * 使用ThreadFactory创建新线程
//     *
//     * @param size 线程池的大小
//     * @return 返回一个固定的线程池
//     */
//    fun getFixedPool(@IntRange(from = 1) size: Int): ExecutorService {
//        return getPoolByTypeAndPriority(size)
//    }
//
//    private fun getPoolByTypeAndPriority(type: Int): ExecutorService {
//        return getPoolByTypeAndPriority(type, Thread.NORM_PRIORITY)
//    }
//
//    private fun getPoolByTypeAndPriority(type: Int, priority: Int): ExecutorService {
//        // 同步锁
//        synchronized(TYPE_PRIORITY_POOLS) {
//            var pool: ExecutorService
//            var priorityPools = TYPE_PRIORITY_POOLS[type]
//            if (priorityPools == null) {
//                // 创建 ConcurrentHashMap
//                priorityPools = ConcurrentHashMap()
//                pool = ThreadPoolExecutor4Util.createPool(type, priority)
//            } else {
//
//            }
//
//        }
//    }
//
//    internal class ThreadPoolExecutor4Util(corePoolSize: Int, maximumPoolSize: Int,
//                                           keepAliveTime: Long, unit: TimeUnit?,
//                                           workQueue: LinkedBlockingQueue4Util,
//                                           threadFactory: ThreadFactory?) : ThreadPoolExecutor(corePoolSize, maximumPoolSize,
//            keepAliveTime, unit,
//            workQueue,
//            threadFactory
//    )
//
//    /**
//     * LinkedBlockingQueue是一个单向链表实现的阻塞队列,先进先出的顺序
//     */
//    private class LinkedBlockingQueue4Util : LinkedBlockingQueue<Runnable> {
//
//        /**
//         * 使用
//         */
//        @Volatile
//        private var mPool: ThreadPoolExecutor4Util? = null
//        private var mCapacity = Int.MAX_VALUE
//
//        internal constructor() : super() {}
//
//        internal constructor(isAddSubThreadFirstThenAddQueue: Boolean) : super() {
//            if (isAddSubThreadFirstThenAddQueue) {
//                mCapacity = 0
//            }
//        }
//
//        internal constructor(capacity: Int) : super() {
//            mCapacity = capacity
//        }
//
//        override fun offer(runnable: Runnable?): Boolean {
//            if (mCapacity <= size &&
//                    mPool != null && mPool!!.poolSize < mPool!!.maximumPoolSize) {
//                // create a non-core thread
//                return false
//            }
//            return super.offer(runnable)
//        }
//    }
//
//}
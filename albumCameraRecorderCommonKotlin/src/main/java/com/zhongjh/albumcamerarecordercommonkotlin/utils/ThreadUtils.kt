package com.zhongjh.albumcamerarecordercommonkotlin.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.IntRange
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2018/05/08
 * desc  : utils about thread
 * update: zhongjh 优化代码
 * update time: zhongjh 转化kotlin 2021/11/23
</pre> *
 * @author Blankj
 */
object ThreadUtils {

    val handler = Handler(Looper.getMainLooper())
    private val TYPE_PRIORITY_POOLS: MutableMap<Int, MutableMap<Int, ExecutorService?>> = HashMap()
    private val TASK_POOL_MAP: MutableMap<BaseTask<*>, ExecutorService?> = ConcurrentHashMap()
    /**
     * 稳定的定时器任务
     */
    private val mExecutorService: ScheduledExecutorService = ScheduledThreadPoolExecutor(1, ThreadFactory { target: Runnable? -> Thread(target) })
    private const val TYPE_SINGLE: Int = -1
    private const val TYPE_CACHED: Int = -2
    private const val TYPE_IO: Int = -4
    private const val TYPE_CPU: Int = -8

    fun isMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    /**
     * 执行ui线程
     * @param runnable 事件
     */
    fun runOnUiThread(runnable: Runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // 如果当前线程就是ui线程直接执行事件
            runnable.run()
        } else {
            handler.post(runnable)
        }
    }

    /**
     * 延时执行ui线程
     * @param runnable 事件
     * @param delayMillis 时间毫秒
     */
    fun runOnUiThreadDelayed(runnable: Runnable, delayMillis: Long) {
        handler.postDelayed(runnable, delayMillis)
    }

    /**
     * 返回一个可以复用并且有固定数量的线程池
     * 使用ThreadFactory创建新线程
     *
     * @param size 线程池的大小
     * @return 返回一个固定的线程池
     */
    fun getFixedPool(@IntRange(from = 1) size: Int): ExecutorService {
        return getPoolByTypeAndPriority(size)
    }

    private fun <T> execute(pool: ExecutorService?, baseTask: BaseTask<T>) {
        execute(pool, baseTask, 0, 0, null)
    }

    /**
     *
     * @param pool ExecutorService 线程池
     * @param delay 延时
     * @param period 间隔时间
     * @param unit 时间单位
     *
     */
    private fun <T> execute(pool: ExecutorService?, baseTask: BaseTask<T>,
                            delay: Long, period: Long, unit: TimeUnit?) {
        synchronized(TASK_POOL_MAP) {
            if (TASK_POOL_MAP[baseTask] != null) {
                Log.e("ThreadUtils", "Task can only be executed once.")
                return
            }
            TASK_POOL_MAP.put(baseTask, pool)
        }
        // 如果间隔时间=0
        if (period == 0L) {
            // 如果延时=0
            if (delay == 0L) {
                // 直接执行
                pool!!.execute(baseTask)
            } else {
                // 使用mExecutorService执行延时的任务
                val timerTask: TimerTask = object : TimerTask() {
                    override fun run() {
                        pool!!.execute(baseTask)
                    }
                }
                mExecutorService.schedule(timerTask, unit!!.toMillis(delay), TimeUnit.MILLISECONDS)
            }
        } else {
            // 设置循环定时任务
            baseTask.setSchedule(true)
            val timerTask: TimerTask = object : TimerTask() {
                override fun run() {
                    pool!!.execute(baseTask)
                }
            }
            // 使用mExecutorService执行延时的任务
            mExecutorService.scheduleAtFixedRate(timerTask, unit!!.toMillis(delay), unit.toMillis(period), TimeUnit.MILLISECONDS)
        }
    }

    private fun getPoolByTypeAndPriority(type: Int): ExecutorService {
        return getPoolByTypeAndPriority(type, Thread.NORM_PRIORITY)
    }

    private fun getPoolByTypeAndPriority(type: Int, priority: Int): ExecutorService {
        // 同步锁
        synchronized(TYPE_PRIORITY_POOLS) {
            var pool: ExecutorService
            var priorityPools = TYPE_PRIORITY_POOLS[type]
            if (priorityPools == null) {
                // 创建 ConcurrentHashMap
                priorityPools = ConcurrentHashMap()
                pool = ThreadPoolExecutor4Util.createPool(type, priority)
            } else {

            }

        }
    }

    /**
     * 创建线程池
     */
    internal class ThreadPoolExecutor4Util private constructor(corePoolSize: Int, maximumPoolSize: Int,
                                                               keepAliveTime: Long, unit: TimeUnit?,
                                                               workQueue: LinkedBlockingQueue4Util,
                                                               threadFactory: ThreadFactory?) : ThreadPoolExecutor(corePoolSize, maximumPoolSize,
            keepAliveTime, unit,
            workQueue,
            threadFactory
    ) {
        /**
         * 原子操作Integer类
         */
        private val mSubmittedCount = AtomicInteger()
        private val mWorkQueue: LinkedBlockingQueue4Util

        init {
            workQueue.mPool = this
            mWorkQueue = workQueue
        }

        /**
         * 返回的是 原值 - 1
         */
        override fun afterExecute(r: Runnable, t: Throwable) {
            mSubmittedCount.decrementAndGet()
            super.afterExecute(r, t)
        }

        /**
         * 执行
         */
        override fun execute(command: Runnable) {
            // 如果被中断就直接返回
            if (this.isShutdown) {
                return
            }
            // 原值 + 1
            mSubmittedCount.incrementAndGet()
            try {
                super.execute(command)
            } catch (ignore: RejectedExecutionException) {
                Log.e("ThreadUtils", "This will not happen!")
                // 如果出现RejectedExecutionException异常，就把command插入到队列的尾部。但是这个异常不会出现，因为要么调用shutdown()，或者线程池的线程数量已经达到了maximumPoolSize的时候
                mWorkQueue.offer(command)
            } catch (t: Throwable) {
                // 别的异常 原值 - 1
                mSubmittedCount.decrementAndGet()
            }
        }

        companion object {
            fun createPool(type: Int, priority: Int): ExecutorService {
                return when (type) {
                    TYPE_SINGLE -> {
                        ThreadPoolExecutor4Util(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                LinkedBlockingQueue4Util(),
                                UtilsThreadFactory("single", priority))
                    }
                }
            }
        }
    }

    /**
     * LinkedBlockingQueue是一个单向链表实现的阻塞队列,先进先出的顺序
     */
    private class LinkedBlockingQueue4Util : LinkedBlockingQueue<Runnable> {

        /**
         * 使用
         */
        @Volatile
        private var mPool: ThreadPoolExecutor4Util? = null
        private var mCapacity = Int.MAX_VALUE

        internal constructor() : super() {}

        internal constructor(isAddSubThreadFirstThenAddQueue: Boolean) : super() {
            if (isAddSubThreadFirstThenAddQueue) {
                mCapacity = 0
            }
        }

        internal constructor(capacity: Int) : super() {
            mCapacity = capacity
        }

        override fun offer(runnable: Runnable?): Boolean {
            if (mCapacity <= size &&
                    mPool != null && mPool!!.poolSize < mPool!!.maximumPoolSize) {
                // create a non-core thread
                return false
            }
            return super.offer(runnable)
        }
    }

    /**
     * 线程工厂
     * @param prefix: 前缀，类型
     * @param priority: 线程在轮询中的优先级。
     */
    internal class UtilsThreadFactory @JvmOverloads constructor(prefix: String, priority: Int, isDaemon: Boolean = false) : ThreadFactory {
        private val namePrefix: String
        private val priority: Int
        private val isDaemon: Boolean

        /**
         * 原子类计算
         */
        private val atomicState = AtomicLong()

        init {
            namePrefix = prefix + "-pool-" +
                    POOL_NUMBER.getAndIncrement() +
                    "-thread-"
            this.priority = priority
            this.isDaemon = isDaemon
        }

        companion object {
            private val POOL_NUMBER = AtomicInteger(1)
            private const val serialVersionUID = -9209200509960368598L
        }

        /**
         * 创建一个线程
         */
        override fun newThread(r: Runnable?): Thread {
            val t: Thread = object : Thread(r, namePrefix + atomicState.andIncrement) {
                override fun run() {
                    try {
                        super.run()
                    } catch (t: Throwable) {
                        Log.e("ThreadUtils", "Request threw uncaught throwable", t)
                    }
                }
            }
            t.isDaemon = isDaemon
            t.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { t, e -> println(e) }
            t.priority = priority
            return t
        }
    }

    abstract class BaseSimpleBaseTask<T> : BaseTask<T>() {

    }

    /**
     * 抽象基类BaseTask，继承于Runnable
     */
    abstract class BaseTask<T> : Runnable {
        companion object {
            private const val NEW = 0
            private const val RUNNING = 1
            private const val EXCEPTIONAL = 2
            private const val COMPLETING = 3
            private const val CANCELLED = 4
            private const val INTERRUPTED = 5
            private const val TIMEOUT = 6
        }

        private val state = AtomicInteger(NEW)

        /**
         * 是否定期任务
         */
        @Volatile
        private var isSchedule = false

        /**
         * 该任务的线程
         */
        @Volatile
        private var runner: Thread? = null
        private var mExecutorService: ScheduledExecutorService? = null
        private var mTimeoutMillis: Long = 0
        private var mTimeoutListener: OnTimeoutListener? = null
        private var deliver: Executor? = null

        val isCanceled: Boolean
            get() = state.get() >= CANCELLED

        val isDone: Boolean
            get() = state.get() > RUNNING

        /**
         * 线程方法
         * @return 实体
         * @throws Throwable 异常
         */
        @Throws(Throwable::class)
        abstract fun doInBackground(): T

        /**
         * 成功
         * @param result 实体
         */
        abstract fun onSuccess(result: T)

        /**
         * 取消
         */
        abstract fun onCancel()

        /**
         * 失败
         * @param t 异常
         */
        abstract fun onFail(t: Throwable?)

        override fun run() {
            // 如果是个循环任务,就意味着每次定时循环的时候，第二次会用到旧的任务
            if (isSchedule) {
                // 判断线程是否为null
                if (runner == null) {
                    // 如果当前状态是 NEW(新线程) 并且改成 RUNNING(线程运行中)，就返回True,否则False
                    if (!state.compareAndSet(NEW, RUNNING)) {
                        // 当前状态不是NEW，直接返回
                        return
                    }
                    // 创建线程
                    runner = Thread.currentThread()
                    if (mTimeoutListener != null) {
                        // 计划任务不支持超时。
                        Log.w("ThreadUtils", "Scheduled task doesn't support timeout.")
                    }
                } else {
                    if (state.get() != RUNNING) {
                        // 如果当前状态不是 RUNNING(线程运行中) 直接返回
                        return
                    }
                }
            } else {
                // 如果当前状态是 NEW(新线程) 并且改成 RUNNING(线程运行中)，就返回True,否则False
                if (!state.compareAndSet(NEW, RUNNING)) {
                    // 当前状态不是NEW，直接返回
                    return
                }
                // 创建线程
                runner = Thread.currentThread()
                if (mTimeoutListener != null) {
                    mExecutorService = ScheduledThreadPoolExecutor(1, ThreadFactory { target: Runnable? -> Thread(target) })
                    (mExecutorService as ScheduledThreadPoolExecutor).schedule(object : TimerTask() {
                        override fun run() {
                            if (!isDone && mTimeoutListener != null) {
                                // 如果时间结束还没完成就是超时了
                                timeout()

                            }
                        }
                    }, mTimeoutMillis, TimeUnit.MILLISECONDS)
                }
            }
        }


        private fun timeout() {
            synchronized(state) {
                // 如果步骤已经执行到RUNNING后面了就直接返回
                if (state.get() > RUNNING) {
                    return
                }

            }
        }

        /**
         * 设置是否定期执行
         */
        fun setSchedule(isSchedule: Boolean) {
            this.isSchedule = isSchedule
        }

        interface OnTimeoutListener {
            /**
             * 超时
             */
            fun onTimeout()
        }

    }
}
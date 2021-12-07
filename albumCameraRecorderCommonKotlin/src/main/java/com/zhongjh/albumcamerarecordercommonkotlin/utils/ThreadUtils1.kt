package com.zhongjh.albumcamerarecordercommonkotlin.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.CallSuper
import androidx.annotation.IntRange
import com.zhongjh.albumcamerarecordercommonkotlin.utils.ThreadUtils1.TYPE_SINGLE
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2018/05/08
 * desc  : utils about thread
 * update: zhongjh 优化代码
</pre> *
 * @author Blankj
 */
object ThreadUtils1 {
    val mainHandler = Handler(Looper.getMainLooper())
    private val TYPE_PRIORITY_POOLS: MutableMap<Int, MutableMap<Int, ExecutorService?>> = HashMap()
    private val TASK_POOL_MAP: MutableMap<BaseTask<*>, ExecutorService?> = ConcurrentHashMap()
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    private val mExecutorService: ScheduledExecutorService = ScheduledThreadPoolExecutor(1, ThreadFactory { target: Runnable? -> Thread(target) })
    private const val TYPE_SINGLE: Int = -1
    private const val TYPE_CACHED: Int = -2
    private const val TYPE_IO: Int = -4
    private const val TYPE_CPU: Int = -8
    private var sDeliver: Executor? = null

    /**
     * Return whether the thread is the main thread.
     * 返回该线程是否是主线程。
     *
     * @return `true`: yes<br></br>`false`: no
     */
    val isMainThread: Boolean
        get() = Looper.myLooper() == Looper.getMainLooper()

    fun runOnUiThread(runnable: Runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run()
        } else {
            mainHandler.post(runnable)
        }
    }

    fun runOnUiThreadDelayed(runnable: Runnable?, delayMillis: Long) {
        mainHandler.postDelayed(runnable!!, delayMillis)
    }

    /**
     * Return a thread pool that reuses a fixed number of threads
     * operating off a shared unbounded queue, using the provided
     * ThreadFactory to create new threads when needed.
     *
     * @param size The size of thread in the pool.
     * @return a fixed thread pool
     */
    fun getFixedPool(@IntRange(from = 1) size: Int): ExecutorService? {
        return getPoolByTypeAndPriority(size)
    }

    /**
     * Return a thread pool that reuses a fixed number of threads
     * operating off a shared unbounded queue, using the provided
     * ThreadFactory to create new threads when needed.
     *
     * @param size     The size of thread in the pool.
     * @param priority The priority of thread in the poll.
     * @return a fixed thread pool
     */
    fun getFixedPool(@IntRange(from = 1) size: Int,
                     @IntRange(from = 1, to = 10) priority: Int): ExecutorService? {
        return getPoolByTypeAndPriority(size, priority)
    }

    /**
     * Return a thread pool that uses a single worker thread operating
     * off an unbounded queue, and uses the provided ThreadFactory to
     * create a new thread when needed.
     *
     * @return a single thread pool
     */
    val singlePool: ExecutorService?
        get() = getPoolByTypeAndPriority(TYPE_SINGLE.toInt())

    /**
     * Return a thread pool that uses a single worker thread operating
     * off an unbounded queue, and uses the provided ThreadFactory to
     * create a new thread when needed.
     *
     * @param priority The priority of thread in the poll.
     * @return a single thread pool
     */
    fun getSinglePool(@IntRange(from = 1, to = 10) priority: Int): ExecutorService? {
        return getPoolByTypeAndPriority(TYPE_SINGLE.toInt(), priority)
    }

    /**
     * Return a thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available.
     *
     * @return a cached thread pool
     */
    val cachedPool: ExecutorService?
        get() = getPoolByTypeAndPriority(TYPE_CACHED.toInt())

    /**
     * Return a thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available.
     *
     * @param priority The priority of thread in the poll.
     * @return a cached thread pool
     */
    fun getCachedPool(@IntRange(from = 1, to = 10) priority: Int): ExecutorService? {
        return getPoolByTypeAndPriority(TYPE_CACHED.toInt(), priority)
    }

    /**
     * Return a thread pool that creates (2 * CPU_COUNT + 1) threads
     * operating off a queue which size is 128.
     *
     * @return a IO thread pool
     */
    val ioPool: ExecutorService?
        get() = getPoolByTypeAndPriority(TYPE_IO.toInt())

    /**
     * Return a thread pool that creates (2 * CPU_COUNT + 1) threads
     * operating off a queue which size is 128.
     *
     * @param priority The priority of thread in the poll.
     * @return a IO thread pool
     */
    fun getIoPool(@IntRange(from = 1, to = 10) priority: Int): ExecutorService? {
        return getPoolByTypeAndPriority(TYPE_IO.toInt(), priority)
    }

    /**
     * Return a thread pool that creates (CPU_COUNT + 1) threads
     * operating off a queue which size is 128 and the maximum
     * number of threads equals (2 * CPU_COUNT + 1).
     *
     * @return a cpu thread pool for
     */
    val cpuPool: ExecutorService?
        get() = getPoolByTypeAndPriority(TYPE_CPU.toInt())

    /**
     * Return a thread pool that creates (CPU_COUNT + 1) threads
     * operating off a queue which size is 128 and the maximum
     * number of threads equals (2 * CPU_COUNT + 1).
     *
     * @param priority The priority of thread in the poll.
     * @return a cpu thread pool for
     */
    fun getCpuPool(@IntRange(from = 1, to = 10) priority: Int): ExecutorService? {
        return getPoolByTypeAndPriority(TYPE_CPU.toInt(), priority)
    }

    /**
     * Executes the given task in a fixed thread pool.
     *
     * @param size The size of thread in the fixed thread pool.
     * @param baseTask The task to execute.
     * @param <T>  The type of the task's result.
    </T> */
    fun <T> executeByFixed(@IntRange(from = 1) size: Int, baseTask: BaseTask<T>) {
        execute(getPoolByTypeAndPriority(size), baseTask)
    }

    /**
     * Executes the given task in a fixed thread pool.
     *
     * @param size     The size of thread in the fixed thread pool.
     * @param baseTask     The task to execute.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByFixed(@IntRange(from = 1) size: Int,
                           baseTask: BaseTask<T>,
                           @IntRange(from = 1, to = 10) priority: Int) {
        execute(getPoolByTypeAndPriority(size, priority), baseTask)
    }

    /**
     * Executes the given task in a fixed thread pool after the given delay.
     *
     * @param size  The size of thread in the fixed thread pool.
     * @param baseTask  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
    </T> */
    fun <T> executeByFixedWithDelay(@IntRange(from = 1) size: Int,
                                    baseTask: BaseTask<T>,
                                    delay: Long,
                                    unit: TimeUnit) {
        executeWithDelay(getPoolByTypeAndPriority(size), baseTask, delay, unit)
    }

    /**
     * Executes the given task in a fixed thread pool after the given delay.
     *
     * @param size     The size of thread in the fixed thread pool.
     * @param baseTask     The task to execute.
     * @param delay    The time from now to delay execution.
     * @param unit     The time unit of the delay parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByFixedWithDelay(@IntRange(from = 1) size: Int,
                                    baseTask: BaseTask<T>,
                                    delay: Long,
                                    unit: TimeUnit,
                                    @IntRange(from = 1, to = 10) priority: Int) {
        executeWithDelay(getPoolByTypeAndPriority(size, priority), baseTask, delay, unit)
    }

    /**
     * Executes the given task in a fixed thread pool at fix rate.
     *
     * @param size   The size of thread in the fixed thread pool.
     * @param baseTask   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
    </T> */
    fun <T> executeByFixedAtFixRate(@IntRange(from = 1) size: Int,
                                    baseTask: BaseTask<T>,
                                    period: Long,
                                    unit: TimeUnit) {
        executeAtFixedRate(getPoolByTypeAndPriority(size), baseTask, 0, period, unit)
    }

    /**
     * Executes the given task in a fixed thread pool at fix rate.
     *
     * @param size     The size of thread in the fixed thread pool.
     * @param baseTask     The task to execute.
     * @param period   The period between successive executions.
     * @param unit     The time unit of the period parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByFixedAtFixRate(@IntRange(from = 1) size: Int,
                                    baseTask: BaseTask<T>,
                                    period: Long,
                                    unit: TimeUnit,
                                    @IntRange(from = 1, to = 10) priority: Int) {
        executeAtFixedRate(getPoolByTypeAndPriority(size, priority), baseTask, 0, period, unit)
    }

    /**
     * Executes the given task in a fixed thread pool at fix rate.
     *
     * @param size         The size of thread in the fixed thread pool.
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeByFixedAtFixRate(@IntRange(from = 1) size: Int,
                                    baseTask: BaseTask<T>,
                                    initialDelay: Long,
                                    period: Long,
                                    unit: TimeUnit) {
        executeAtFixedRate(getPoolByTypeAndPriority(size), baseTask, initialDelay, period, unit)
    }

    /**
     * Executes the given task in a fixed thread pool at fix rate.
     *
     * @param size         The size of thread in the fixed thread pool.
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param priority     The priority of thread in the poll.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeByFixedAtFixRate(@IntRange(from = 1) size: Int,
                                    baseTask: BaseTask<T>,
                                    initialDelay: Long,
                                    period: Long,
                                    unit: TimeUnit,
                                    @IntRange(from = 1, to = 10) priority: Int) {
        executeAtFixedRate(getPoolByTypeAndPriority(size, priority), baseTask, initialDelay, period, unit)
    }

    /**
     * Executes the given task in a single thread pool.
     *
     * @param baseTask The task to execute.
     * @param <T>  The type of the task's result.
    </T> */
    fun <T> executeBySingle(baseTask: BaseTask<T>) {
        execute(getPoolByTypeAndPriority(TYPE_SINGLE.toInt()), baseTask)
    }

    /**
     * Executes the given task in a single thread pool.
     *
     * @param baseTask     The task to execute.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeBySingle(baseTask: BaseTask<T>,
                            @IntRange(from = 1, to = 10) priority: Int) {
        execute(getPoolByTypeAndPriority(TYPE_SINGLE.toInt(), priority), baseTask)
    }

    /**
     * Executes the given task in a single thread pool after the given delay.
     *
     * @param baseTask  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
    </T> */
    fun <T> executeBySingleWithDelay(baseTask: BaseTask<T>,
                                     delay: Long,
                                     unit: TimeUnit) {
        executeWithDelay(getPoolByTypeAndPriority(TYPE_SINGLE.toInt()), baseTask, delay, unit)
    }

    /**
     * Executes the given task in a single thread pool after the given delay.
     *
     * @param baseTask     The task to execute.
     * @param delay    The time from now to delay execution.
     * @param unit     The time unit of the delay parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeBySingleWithDelay(baseTask: BaseTask<T>,
                                     delay: Long,
                                     unit: TimeUnit,
                                     @IntRange(from = 1, to = 10) priority: Int) {
        executeWithDelay(getPoolByTypeAndPriority(TYPE_SINGLE.toInt(), priority), baseTask, delay, unit)
    }

    /**
     * Executes the given task in a single thread pool at fix rate.
     *
     * @param baseTask   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
    </T> */
    fun <T> executeBySingleAtFixRate(baseTask: BaseTask<T>,
                                     period: Long,
                                     unit: TimeUnit) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_SINGLE.toInt()), baseTask, 0, period, unit)
    }

    /**
     * Executes the given task in a single thread pool at fix rate.
     *
     * @param baseTask     The task to execute.
     * @param period   The period between successive executions.
     * @param unit     The time unit of the period parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeBySingleAtFixRate(baseTask: BaseTask<T>,
                                     period: Long,
                                     unit: TimeUnit,
                                     @IntRange(from = 1, to = 10) priority: Int) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_SINGLE.toInt(), priority), baseTask, 0, period, unit)
    }

    /**
     * Executes the given task in a single thread pool at fix rate.
     *
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeBySingleAtFixRate(baseTask: BaseTask<T>,
                                     initialDelay: Long,
                                     period: Long,
                                     unit: TimeUnit) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_SINGLE.toInt()), baseTask, initialDelay, period, unit)
    }

    /**
     * Executes the given task in a single thread pool at fix rate.
     *
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param priority     The priority of thread in the poll.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeBySingleAtFixRate(baseTask: BaseTask<T>,
                                     initialDelay: Long,
                                     period: Long,
                                     unit: TimeUnit,
                                     @IntRange(from = 1, to = 10) priority: Int) {
        executeAtFixedRate(
                getPoolByTypeAndPriority(TYPE_SINGLE.toInt(), priority), baseTask, initialDelay, period, unit
        )
    }

    /**
     * Executes the given task in a cached thread pool.
     *
     * @param baseTask The task to execute.
     * @param <T>  The type of the task's result.
    </T> */
    fun <T> executeByCached(baseTask: BaseTask<T>) {
        execute(getPoolByTypeAndPriority(TYPE_CACHED.toInt()), baseTask)
    }

    /**
     * Executes the given task in a cached thread pool.
     *
     * @param baseTask     The task to execute.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByCached(baseTask: BaseTask<T>,
                            @IntRange(from = 1, to = 10) priority: Int) {
        execute(getPoolByTypeAndPriority(TYPE_CACHED.toInt(), priority), baseTask)
    }

    /**
     * Executes the given task in a cached thread pool after the given delay.
     *
     * @param baseTask  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
    </T> */
    fun <T> executeByCachedWithDelay(baseTask: BaseTask<T>,
                                     delay: Long,
                                     unit: TimeUnit) {
        executeWithDelay(getPoolByTypeAndPriority(TYPE_CACHED.toInt()), baseTask, delay, unit)
    }

    /**
     * Executes the given task in a cached thread pool after the given delay.
     *
     * @param baseTask     The task to execute.
     * @param delay    The time from now to delay execution.
     * @param unit     The time unit of the delay parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByCachedWithDelay(baseTask: BaseTask<T>,
                                     delay: Long,
                                     unit: TimeUnit,
                                     @IntRange(from = 1, to = 10) priority: Int) {
        executeWithDelay(getPoolByTypeAndPriority(TYPE_CACHED.toInt(), priority), baseTask, delay, unit)
    }

    /**
     * Executes the given task in a cached thread pool at fix rate.
     *
     * @param baseTask   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
    </T> */
    fun <T> executeByCachedAtFixRate(baseTask: BaseTask<T>,
                                     period: Long,
                                     unit: TimeUnit) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_CACHED.toInt()), baseTask, 0, period, unit)
    }

    /**
     * Executes the given task in a cached thread pool at fix rate.
     *
     * @param baseTask     The task to execute.
     * @param period   The period between successive executions.
     * @param unit     The time unit of the period parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByCachedAtFixRate(baseTask: BaseTask<T>,
                                     period: Long,
                                     unit: TimeUnit,
                                     @IntRange(from = 1, to = 10) priority: Int) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_CACHED.toInt(), priority), baseTask, 0, period, unit)
    }

    /**
     * Executes the given task in a cached thread pool at fix rate.
     *
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeByCachedAtFixRate(baseTask: BaseTask<T>,
                                     initialDelay: Long,
                                     period: Long,
                                     unit: TimeUnit) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_CACHED.toInt()), baseTask, initialDelay, period, unit)
    }

    /**
     * Executes the given task in a cached thread pool at fix rate.
     *
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param priority     The priority of thread in the poll.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeByCachedAtFixRate(baseTask: BaseTask<T>,
                                     initialDelay: Long,
                                     period: Long,
                                     unit: TimeUnit,
                                     @IntRange(from = 1, to = 10) priority: Int) {
        executeAtFixedRate(
                getPoolByTypeAndPriority(TYPE_CACHED.toInt(), priority), baseTask, initialDelay, period, unit
        )
    }

    /**
     * 在IO线程池中执行给定的任务。
     *
     * @param baseTask The task to execute.
     * @param <T>  The type of the task's result.
    </T> */
    fun <T> executeByIo(baseTask: BaseTask<T>) {
        execute(getPoolByTypeAndPriority(TYPE_IO.toInt()), baseTask)
    }

    /**
     * Executes the given task in an IO thread pool.
     *
     * @param baseTask     The task to execute.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByIo(baseTask: BaseTask<T>,
                        @IntRange(from = 1, to = 10) priority: Int) {
        execute(getPoolByTypeAndPriority(TYPE_IO.toInt(), priority), baseTask)
    }

    /**
     * Executes the given task in an IO thread pool after the given delay.
     *
     * @param baseTask  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
    </T> */
    fun <T> executeByIoWithDelay(baseTask: BaseTask<T>,
                                 delay: Long,
                                 unit: TimeUnit) {
        executeWithDelay(getPoolByTypeAndPriority(TYPE_IO.toInt()), baseTask, delay, unit)
    }

    /**
     * Executes the given task in an IO thread pool after the given delay.
     *
     * @param baseTask     The task to execute.
     * @param delay    The time from now to delay execution.
     * @param unit     The time unit of the delay parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByIoWithDelay(baseTask: BaseTask<T>,
                                 delay: Long,
                                 unit: TimeUnit,
                                 @IntRange(from = 1, to = 10) priority: Int) {
        executeWithDelay(getPoolByTypeAndPriority(TYPE_IO.toInt(), priority), baseTask, delay, unit)
    }

    /**
     * Executes the given task in an IO thread pool at fix rate.
     *
     * @param baseTask   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
    </T> */
    fun <T> executeByIoAtFixRate(baseTask: BaseTask<T>,
                                 period: Long,
                                 unit: TimeUnit) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_IO.toInt()), baseTask, 0, period, unit)
    }

    /**
     * Executes the given task in an IO thread pool at fix rate.
     *
     * @param baseTask     The task to execute.
     * @param period   The period between successive executions.
     * @param unit     The time unit of the period parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByIoAtFixRate(baseTask: BaseTask<T>,
                                 period: Long,
                                 unit: TimeUnit,
                                 @IntRange(from = 1, to = 10) priority: Int) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_IO.toInt(), priority), baseTask, 0, period, unit)
    }

    /**
     * Executes the given task in an IO thread pool at fix rate.
     *
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeByIoAtFixRate(baseTask: BaseTask<T>,
                                 initialDelay: Long,
                                 period: Long,
                                 unit: TimeUnit) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_IO.toInt()), baseTask, initialDelay, period, unit)
    }

    /**
     * Executes the given task in an IO thread pool at fix rate.
     *
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param priority     The priority of thread in the poll.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeByIoAtFixRate(baseTask: BaseTask<T>,
                                 initialDelay: Long,
                                 period: Long,
                                 unit: TimeUnit,
                                 @IntRange(from = 1, to = 10) priority: Int) {
        executeAtFixedRate(
                getPoolByTypeAndPriority(TYPE_IO.toInt(), priority), baseTask, initialDelay, period, unit
        )
    }

    /**
     * Executes the given task in a cpu thread pool.
     *
     * @param baseTask The task to execute.
     * @param <T>  The type of the task's result.
    </T> */
    fun <T> executeByCpu(baseTask: BaseTask<T>) {
        execute(getPoolByTypeAndPriority(TYPE_CPU.toInt()), baseTask)
    }

    /**
     * Executes the given task in a cpu thread pool.
     *
     * @param baseTask     The task to execute.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByCpu(baseTask: BaseTask<T>,
                         @IntRange(from = 1, to = 10) priority: Int) {
        execute(getPoolByTypeAndPriority(TYPE_CPU.toInt(), priority), baseTask)
    }

    /**
     * Executes the given task in a cpu thread pool after the given delay.
     *
     * @param baseTask  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
    </T> */
    fun <T> executeByCpuWithDelay(baseTask: BaseTask<T>,
                                  delay: Long,
                                  unit: TimeUnit) {
        executeWithDelay(getPoolByTypeAndPriority(TYPE_CPU.toInt()), baseTask, delay, unit)
    }

    /**
     * Executes the given task in a cpu thread pool after the given delay.
     *
     * @param baseTask     The task to execute.
     * @param delay    The time from now to delay execution.
     * @param unit     The time unit of the delay parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByCpuWithDelay(baseTask: BaseTask<T>,
                                  delay: Long,
                                  unit: TimeUnit,
                                  @IntRange(from = 1, to = 10) priority: Int) {
        executeWithDelay(getPoolByTypeAndPriority(TYPE_CPU.toInt(), priority), baseTask, delay, unit)
    }

    /**
     * Executes the given task in a cpu thread pool at fix rate.
     *
     * @param baseTask   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
    </T> */
    fun <T> executeByCpuAtFixRate(baseTask: BaseTask<T>,
                                  period: Long,
                                  unit: TimeUnit) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_CPU.toInt()), baseTask, 0, period, unit)
    }

    /**
     * Executes the given task in a cpu thread pool at fix rate.
     *
     * @param baseTask     The task to execute.
     * @param period   The period between successive executions.
     * @param unit     The time unit of the period parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByCpuAtFixRate(baseTask: BaseTask<T>,
                                  period: Long,
                                  unit: TimeUnit,
                                  @IntRange(from = 1, to = 10) priority: Int) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_CPU.toInt(), priority), baseTask, 0, period, unit)
    }

    /**
     * Executes the given task in a cpu thread pool at fix rate.
     *
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeByCpuAtFixRate(baseTask: BaseTask<T>,
                                  initialDelay: Long,
                                  period: Long,
                                  unit: TimeUnit) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_CPU.toInt()), baseTask, initialDelay, period, unit)
    }

    /**
     * Executes the given task in a cpu thread pool at fix rate.
     *
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param priority     The priority of thread in the poll.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeByCpuAtFixRate(baseTask: BaseTask<T>,
                                  initialDelay: Long,
                                  period: Long,
                                  unit: TimeUnit,
                                  @IntRange(from = 1, to = 10) priority: Int) {
        executeAtFixedRate(
                getPoolByTypeAndPriority(TYPE_CPU.toInt(), priority), baseTask, initialDelay, period, unit
        )
    }

    /**
     * Executes the given task in a custom thread pool.
     *
     * @param pool The custom thread pool.
     * @param baseTask The task to execute.
     * @param <T>  The type of the task's result.
    </T> */
    fun <T> executeByCustom(pool: ExecutorService?, baseTask: BaseTask<T>) {
        execute(pool, baseTask)
    }

    /**
     * Executes the given task in a custom thread pool after the given delay.
     *
     * @param pool  The custom thread pool.
     * @param baseTask  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
    </T> */
    fun <T> executeByCustomWithDelay(pool: ExecutorService?,
                                     baseTask: BaseTask<T>,
                                     delay: Long,
                                     unit: TimeUnit) {
        executeWithDelay(pool, baseTask, delay, unit)
    }

    /**
     * Executes the given task in a custom thread pool at fix rate.
     *
     * @param pool   The custom thread pool.
     * @param baseTask   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
    </T> */
    fun <T> executeByCustomAtFixRate(pool: ExecutorService?,
                                     baseTask: BaseTask<T>,
                                     period: Long,
                                     unit: TimeUnit) {
        executeAtFixedRate(pool, baseTask, 0, period, unit)
    }

    /**
     * Executes the given task in a custom thread pool at fix rate.
     *
     * @param pool         The custom thread pool.
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeByCustomAtFixRate(pool: ExecutorService?,
                                     baseTask: BaseTask<T>,
                                     initialDelay: Long,
                                     period: Long,
                                     unit: TimeUnit) {
        executeAtFixedRate(pool, baseTask, initialDelay, period, unit)
    }

    /**
     * Cancel the given task.
     *
     * @param baseTask The task to cancel.
     */
    fun cancel(baseTask: BaseTask<*>?) {
        if (baseTask == null) {
            return
        }
        baseTask.cancel()
    }

    /**
     * Cancel the given tasks.
     *
     * @param baseTasks The tasks to cancel.
     */
    fun cancel(vararg baseTasks: BaseTask<*>?) {
        if (baseTasks == null || baseTasks.size == 0) {
            return
        }
        for (baseTask in baseTasks) {
            if (baseTask == null) {
                continue
            }
            baseTask.cancel()
        }
    }

    /**
     * Cancel the given tasks.
     *
     * @param baseTasks The tasks to cancel.
     */
    fun cancel(baseTasks: List<BaseTask<*>?>?) {
        if (baseTasks == null || baseTasks.size == 0) {
            return
        }
        for (baseTask in baseTasks) {
            if (baseTask == null) {
                continue
            }
            baseTask.cancel()
        }
    }

    /**
     * Cancel the tasks in pool.
     *
     * @param executorService The pool.
     */
    fun cancel(executorService: ExecutorService) {
        if (executorService is ThreadPoolExecutor4Util) {
            for ((key, value) in TASK_POOL_MAP) {
                if (value === executorService) {
                    cancel(key)
                }
            }
        } else {
            Log.e("ThreadUtils", "The executorService is not ThreadUtils's pool.")
        }
    }

    /**
     * Set the deliver.
     *
     * @param deliver The deliver.
     */
    fun setDeliver(deliver: Executor?) {
        sDeliver = deliver
    }

    private fun <T> execute(pool: ExecutorService?, baseTask: BaseTask<T>) {
        execute(pool, baseTask, 0, 0, null)
    }

    private fun <T> executeWithDelay(pool: ExecutorService?,
                                     baseTask: BaseTask<T>,
                                     delay: Long,
                                     unit: TimeUnit) {
        execute(pool, baseTask, delay, 0, unit)
    }

    private fun <T> executeAtFixedRate(pool: ExecutorService?,
                                       baseTask: BaseTask<T>,
                                       delay: Long,
                                       period: Long,
                                       unit: TimeUnit) {
        execute(pool, baseTask, delay, period, unit)
    }

    /**
     *
     * @param pool ExecutorService 线程池
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
        if (period == 0L) {
            if (delay == 0L) {
                pool!!.execute(baseTask)
            } else {
                val timerTask: TimerTask = object : TimerTask() {
                    override fun run() {
                        pool!!.execute(baseTask)
                    }
                }
                mExecutorService.schedule(timerTask, unit!!.toMillis(delay), TimeUnit.MILLISECONDS)
            }
        } else {
            baseTask.setSchedule(true)
            val timerTask: TimerTask = object : TimerTask() {
                override fun run() {
                    pool!!.execute(baseTask)
                }
            }
            mExecutorService.scheduleAtFixedRate(timerTask, unit!!.toMillis(delay), unit.toMillis(period), TimeUnit.MILLISECONDS)
        }
    }

    private fun getPoolByTypeAndPriority(type: Int): ExecutorService? {
        return getPoolByTypeAndPriority(type, Thread.NORM_PRIORITY)
    }

    private fun getPoolByTypeAndPriority(type: Int, priority: Int): ExecutorService {
        synchronized(TYPE_PRIORITY_POOLS) {
            var pool: ExecutorService
            var priorityPools = TYPE_PRIORITY_POOLS[type]
            if (priorityPools == null) {
                priorityPools = ConcurrentHashMap()
                pool = ThreadPoolExecutor4Util.createPool(type, priority)
                priorityPools[priority] = pool
                TYPE_PRIORITY_POOLS[type] = priorityPools
            } else {
                if (priorityPools[priority] == null) {
                    pool = ThreadPoolExecutor4Util.createPool(type, priority)
                    priorityPools[priority] = pool
                } else {
                    pool = priorityPools[priority]!!
                }
            }
            return pool
        }
    }

    private val globalDeliver: Executor?
        private get() {
            if (sDeliver == null) {
                sDeliver = Executor { command -> runOnUiThread(command) }
            }
            return sDeliver
        }

    internal class ThreadPoolExecutor4Util private constructor(corePoolSize: Int, maximumPoolSize: Int,
                                                               keepAliveTime: Long, unit: TimeUnit?,
                                                               workQueue: LinkedBlockingQueue4Util,
                                                               threadFactory: ThreadFactory?) : ThreadPoolExecutor(corePoolSize, maximumPoolSize,
            keepAliveTime, unit,
            workQueue,
            threadFactory
    ) {
        private val mSubmittedCount = AtomicInteger()
        private val mWorkQueue: LinkedBlockingQueue4Util
        private val submittedCount: Int
            private get() = mSubmittedCount.get()

        override fun afterExecute(r: Runnable, t: Throwable) {
            mSubmittedCount.decrementAndGet()
            super.afterExecute(r, t)
        }

        override fun execute(command: Runnable) {
            if (this.isShutdown) {
                return
            }
            mSubmittedCount.incrementAndGet()
            try {
                super.execute(command)
            } catch (ignore: RejectedExecutionException) {
                Log.e("ThreadUtils", "This will not happen!")
                mWorkQueue.offer(command)
            } catch (t: Throwable) {
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
                                UtilsThreadFactory("single", priority)
                        )
                    }
                    TYPE_CACHED -> {
                        ThreadPoolExecutor4Util(0, 128,
                                60L, TimeUnit.SECONDS,
                                LinkedBlockingQueue4Util(true),
                                UtilsThreadFactory("cached", priority)
                        )
                    }
                    TYPE_IO -> {
                        ThreadPoolExecutor4Util(2 * CPU_COUNT + 1, 2 * CPU_COUNT + 1,
                                30, TimeUnit.SECONDS,
                                LinkedBlockingQueue4Util(),
                                UtilsThreadFactory("io", priority)
                        )
                    }
                    TYPE_CPU -> {
                        ThreadPoolExecutor4Util(CPU_COUNT + 1, 2 * CPU_COUNT + 1,
                                30, TimeUnit.SECONDS,
                                LinkedBlockingQueue4Util(true),
                                UtilsThreadFactory("cpu", priority)
                        )
                    }
                    else -> ThreadPoolExecutor4Util(type, type,
                            0L, TimeUnit.MILLISECONDS,
                            LinkedBlockingQueue4Util(),
                            UtilsThreadFactory("fixed($type)", priority)
                    )
                }
            }
        }

        init {
            workQueue.mPool = this
            mWorkQueue = workQueue
        }
    }

    private class LinkedBlockingQueue4Util : LinkedBlockingQueue<Runnable> {
        @Volatile
        var mPool: ThreadPoolExecutor4Util? = null
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

        override fun offer(runnable: Runnable): Boolean {
            return if (mCapacity <= size && mPool != null && mPool!!.poolSize < mPool!!.maximumPoolSize) {
                // create a non-core thread
                false
            } else super.offer(runnable)
        }
    }

    internal class UtilsThreadFactory @JvmOverloads constructor(prefix: String, priority: Int, isDaemon: Boolean = false) :  ThreadFactory {
        private val namePrefix: String
        private val priority: Int
        private val isDaemon: Boolean
        private val atomicState = AtomicLong()
        override fun newThread(r: Runnable): Thread {
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

        companion object {
            private val POOL_NUMBER = AtomicInteger(1)
            private const val serialVersionUID = -9209200509960368598L
        }

        init {
            namePrefix = prefix + "-pool-" +
                    POOL_NUMBER.getAndIncrement() +
                    "-thread-"
            this.priority = priority
            this.isDaemon = isDaemon
        }
    }

    abstract class BaseSimpleBaseTask<T> : BaseTask<T>() {
        override fun onCancel() {
            Log.e("ThreadUtils", "onCancel: " + Thread.currentThread())
        }

        override fun onFail(t: Throwable?) {
            Log.e("ThreadUtils", "onFail: ", t)
        }
    }

    abstract class BaseTask<T> : Runnable {
        private val state = AtomicInteger(NEW)

        @Volatile
        private var isSchedule = false

        @Volatile
        private var runner: Thread? = null
        private var mExecutorService: ScheduledExecutorService? = null
        private var mTimeoutMillis: Long = 0
        private var mTimeoutListener: OnTimeoutListener? = null
        private var deliver: Executor? = null

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
            if (isSchedule) {
                if (runner == null) {
                    if (!state.compareAndSet(NEW, RUNNING)) {
                        return
                    }
                    runner = Thread.currentThread()
                    if (mTimeoutListener != null) {
                        Log.w("ThreadUtils", "Scheduled task doesn't support timeout.")
                    }
                } else {
                    if (state.get() != RUNNING) {
                        return
                    }
                }
            } else {
                if (!state.compareAndSet(NEW, RUNNING)) {
                    return
                }
                runner = Thread.currentThread()
                if (mTimeoutListener != null) {
                    mExecutorService = ScheduledThreadPoolExecutor(1, ThreadFactory { target: Runnable? -> Thread(target) })
                    (mExecutorService as ScheduledThreadPoolExecutor).schedule(object : TimerTask() {
                        override fun run() {
                            if (!isDone && mTimeoutListener != null) {
                                timeout()
                                mTimeoutListener!!.onTimeout()
                            }
                        }
                    }, mTimeoutMillis, TimeUnit.MILLISECONDS)
                }
            }
            try {
                val result = doInBackground()
                if (isSchedule) {
                    if (state.get() != RUNNING) {
                        return
                    }
                    getDeliver()!!.execute { onSuccess(result) }
                } else {
                    if (!state.compareAndSet(RUNNING, COMPLETING)) {
                        return
                    }
                    getDeliver()!!.execute {
                        onSuccess(result)
                        onDone()
                    }
                }
            } catch (ignore: InterruptedException) {
                state.compareAndSet(CANCELLED, INTERRUPTED)
            } catch (throwable: Throwable) {
                if (!state.compareAndSet(RUNNING, EXCEPTIONAL)) {
                    return
                }
                getDeliver()!!.execute {
                    onFail(throwable)
                    onDone()
                }
            }
        }

        @JvmOverloads
        fun cancel(mayInterruptIfRunning: Boolean = true) {
            synchronized(state) {
                if (state.get() > RUNNING) {
                    return
                }
                state.set(CANCELLED)
            }
            if (mayInterruptIfRunning) {
                if (runner != null) {
                    runner!!.interrupt()
                }
            }
            getDeliver()!!.execute {
                onCancel()
                onDone()
            }
        }

        private fun timeout() {
            synchronized(state) {
                if (state.get() > RUNNING) {
                    return
                }
                state.set(TIMEOUT)
            }
            if (runner != null) {
                runner!!.interrupt()
            }
            onDone()
        }

        val isCanceled: Boolean
            get() = state.get() >= CANCELLED

        val isDone: Boolean
            get() = state.get() > RUNNING

        fun setDeliver(deliver: Executor?): BaseTask<T> {
            this.deliver = deliver
            return this
        }

        /**
         * Scheduled task doesn't support timeout.
         */
        fun setTimeout(timeoutMillis: Long, listener: OnTimeoutListener?): BaseTask<T> {
            mTimeoutMillis = timeoutMillis
            mTimeoutListener = listener
            return this
        }

        fun setSchedule(isSchedule: Boolean) {
            this.isSchedule = isSchedule
        }

        private fun getDeliver(): Executor? {
            return if (deliver == null) {
                globalDeliver
            } else deliver
        }

        @CallSuper
        protected fun onDone() {
            TASK_POOL_MAP.remove(this)
            if (mExecutorService != null) {
                mExecutorService!!.shutdownNow()
                mExecutorService = null
                mTimeoutListener = null
            }
        }

        interface OnTimeoutListener {
            /**
             * 超时
             */
            fun onTimeout()
        }

        override fun hashCode(): Int {
            return super.hashCode()
        }

        override fun equals(obj: Any?): Boolean {
            return super.equals(obj)
        }

        companion object {
            private const val NEW = 0
            private const val RUNNING = 1
            private const val EXCEPTIONAL = 2
            private const val COMPLETING = 3
            private const val CANCELLED = 4
            private const val INTERRUPTED = 5
            private const val TIMEOUT = 6
        }
    }

    class SyncValue<T> {
        private val mLatch = CountDownLatch(1)
        private val mFlag = AtomicBoolean()
        private var mValue: T? = null
        fun setValue(value: T) {
            if (mFlag.compareAndSet(false, true)) {
                mValue = value
                mLatch.countDown()
            }
        }

        val value: T?
            get() {
                if (!mFlag.get()) {
                    try {
                        mLatch.await()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                return mValue
            }
    }
}
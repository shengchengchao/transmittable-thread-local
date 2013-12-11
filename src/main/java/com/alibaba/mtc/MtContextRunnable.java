package com.alibaba.mtc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * {@link MtContextRunnable} decorate {@link Runnable}, so as to get @{@link MtContextThreadLocal}
 * and transmit it to the time of {@link Runnable} execution, needed when use {@link Runnable} to thread pool.
 * <p/>
 * Use factory method {@link #get(Runnable)} to create instance.
 *
 * @author ding.lid
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @since 0.9.0
 */
public final class MtContextRunnable implements Runnable {
    private final Map<MtContextThreadLocal<?>, Object> copied;
    private final Runnable runnable;

    private MtContextRunnable(Runnable runnable) {
        copied = MtContextThreadLocal.copy();
        this.runnable = runnable;
    }

    /**
     * wrap method {@link Runnable#run()}.
     */
    @Override
    public void run() {
        Map<MtContextThreadLocal<?>, Object> backup = MtContextThreadLocal.backupAndSet(copied);
        try {
            runnable.run();
        } finally {
            MtContextThreadLocal.restore(backup);
        }
        // FIXME add attribute so as to release copied after run
    }

    public Runnable getRunnable() {
        return runnable;
    }

    /**
     * Factory method, wrapper input {@link Runnable} to {@link MtContextRunnable}.
     * <p/>
     * This method is idempotent.
     *
     * @param runnable input {@link Runnable}
     * @return Wrapped {@link Runnable}
     */
    public static MtContextRunnable get(Runnable runnable) {
        if (null == runnable) {
            return null;
        }

        if (runnable instanceof MtContextRunnable) { // avoid redundant decoration, and ensure idempotency
            return (MtContextRunnable) runnable;
        }
        return new MtContextRunnable(runnable);
    }

    /**
     * wrapper input {@link Runnable} Collection to {@link MtContextRunnable} Collection.
     */
    public static List<MtContextRunnable> gets(Collection<? extends Runnable> tasks) {
        if (null == tasks) {
            return null;
        }
        List<MtContextRunnable> copy = new ArrayList<MtContextRunnable>();
        for (Runnable task : tasks) {
            copy.add(MtContextRunnable.get(task));
        }
        return copy;
    }
}

package dev.westernpine.lib.object;

import dev.westernpine.bettertry.Try;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class Scheduler {

    private Timer timer;
    private ExecutorService executor;

    public Scheduler() {
        timer = new Timer();
        executor = Executors.newCachedThreadPool();
    }

    public static void loop(Supplier<Boolean> condition, Callable<Boolean> callable) {
        while (condition.get())
            if (Try.of(callable::call).getUnchecked())
                break;
    }

    public synchronized void shutdown() {
        executor.shutdown();
        timer.cancel();
    }

    public synchronized List<Runnable> shutdownNow() {
        List<Runnable> leftovers = executor.shutdownNow();
        timer.cancel();
        return leftovers;
    }

    /*
     * Sync
     */

    public synchronized void run(Runnable task) {
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, 0);
    }

    public synchronized void runLater(Runnable task, Long delay) {
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, delay);
    }

    public synchronized void runLater(Runnable task, Date start) {
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, start);
    }

    public synchronized void runLaterRepeating(Runnable task, Long delay, Long period) {
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, delay, period);
    }

    public synchronized void runLaterRepeating(Runnable task, Date start, Long period) {
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, start, period);
    }

    /*
     * Sync Callable
     */

    public synchronized <T> CompletableFuture<T> call(Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    future.complete(callable.call());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0);
        return future;
    }

    public synchronized <T> CompletableFuture<T> callLater(Callable<T> callable, Long delay) {
        CompletableFuture<T> future = new CompletableFuture<>();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    future.complete(callable.call());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, delay);
        return future;
    }

    public synchronized <T> CompletableFuture<T> callLater(Callable<T> callable, Date start) {
        CompletableFuture<T> future = new CompletableFuture<>();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    future.complete(callable.call());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, start);
        return future;
    }

    /*
     * Async
     */

    public synchronized void runAsync(Runnable task) {
        this.executor.submit(task);
    }

    public synchronized void runLaterAsync(Runnable task, Long delay) {
        this.executor.submit(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            task.run();
        });
    }

    public synchronized void runLaterAsync(Runnable task, Date start) {
        this.executor.submit(() -> {
            while (start.getTime() < new Date().getTime()) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            task.run();
        });
    }

    public synchronized void runLaterRepeatingAsync(Runnable task, Long delay, Long period) {
        this.executor.submit(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (true) {
                task.run();
                try {
                    Thread.sleep(period);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public synchronized void runLaterRepeatingAsync(Runnable task, Date start, Long period) {
        this.executor.submit(() -> {
            while (start.getTime() < new Date().getTime()) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (true) {
                task.run();
                try {
                    Thread.sleep(period);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*
     * Async Callable
     */

    public synchronized <T> Future<T> callAsync(Callable<T> callable) {
        return this.executor.submit(callable);
    }

    public synchronized <T> Future<T> callLaterAsync(Callable<T> callable, Long delay) {
        return this.executor.submit(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return callable.call();
        });
    }

    public synchronized <T> Future<T> callLaterAsync(Callable<T> callable, Date start) {
        return this.executor.submit(() -> {
            while (start.getTime() < new Date().getTime()) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return callable.call();
        });
    }

}

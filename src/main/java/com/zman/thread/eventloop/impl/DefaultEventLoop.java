package com.zman.thread.eventloop.impl;

import com.zman.thread.eventloop.EventLoop;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.*;


public class DefaultEventLoop implements EventLoop, Runnable {

    /**
     * 每个队列包含的最大任务个数
     */
    private int maxQueueSize = 1000;

    /**
     * 立即执行类型的任务队列
     */
    private final BlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<>(maxQueueSize+1);

    /**
     * 延迟支持类型的任务队列
     */
    private final PriorityQueue<ScheduledFutureTask> scheduledTaskQueue = new PriorityQueue<>(16);

    /**
     * 是否停止了event loop
     */
    private boolean stop = false;

    /**
     * event loop的执行器
     */
    private Executor executor;

    /**
     * 队列满了返回异常Future
     */
    private static final FutureTask QUEUE_FUTURE_FUTURE = new FutureTask<>(() -> {
        throw new RuntimeException("queue is full");
    });

    private static final Runnable WAKEUP_TASK = () -> { /* Do nothing. */ };


    public DefaultEventLoop(String eventloopName) {
        QUEUE_FUTURE_FUTURE.run();
        executor = Executors.newSingleThreadExecutor(r -> new Thread(r, eventloopName));
        executor.execute(this);
    }

    public void run() {
        do {
            Runnable task = takeTask();

            if (task != null) {
                task.run();
                updateLastExecutionTime();
            }

        } while (!confirmShutdown());
    }

    private boolean confirmShutdown() {
        return stop;
    }

    /**
     * 提交任务
     *
     * @param taskType 事件类型，用于区分不同类型的任务，用于做任务调度
     * @param task     任务
     * @return future
     */
    @Override
    public <T> Future<T> submit(String taskType, Callable<T> task) {
        FutureTask<T> futureTask = new FutureTask<>(task);
        boolean success = taskQueue.offer(futureTask);
        if(success){
            return futureTask;
        }else{
            return QUEUE_FUTURE_FUTURE;
        }
    }

    /**
     * 提交任务
     *
     * @param taskType 事件类型，用于区分不同类型的任务，用于做任务调度
     * @param task     任务
     * @param timeout  时间
     * @param timeUnit 单位
     * @return future
     */
    @Override
    public <T> Future<T> submit(String taskType, Callable<T> task, long timeout, TimeUnit timeUnit) {
        ScheduledFutureTask<T> scheduledFutureTask = new ScheduledFutureTask<>(task, timeout, timeUnit);
        if(scheduledTaskQueue.size()<maxQueueSize){
            scheduledTaskQueue.offer(scheduledFutureTask);

            // eventloop刚启动时，taskQueue和scheduledTaskQueue都没有任务，此时eventloop会await(taskQueue.take)
            // 需要再scheduled已经加入到scheduledTaskQueue后再将WAKEUP_TASK加入到taskQueue，否则WAKEUP_TASK可能很快执行完，
            // 结果eventloop又回到await状态
            if(taskQueue.size()==0){
                taskQueue.offer(WAKEUP_TASK);
            }

            return scheduledFutureTask;
        }else{
            return QUEUE_FUTURE_FUTURE;
        }
    }

    public Future<String> shutdown() {
        return submit(TaskType.COMPUTE.name(), () -> {
            stop = true;
            return "success";
        });
    }

    /**
     * @return 一个任务，或者null
     */
    private Runnable takeTask() {
        Runnable task = null;
        try {
            ScheduledFutureTask scheduledTask = scheduledTaskQueue.peek();
            if (scheduledTask != null) {
                long delayNano = scheduledTask.getDelay(TimeUnit.NANOSECONDS);
                if (delayNano > 0) {
                    task = taskQueue.poll(delayNano, TimeUnit.NANOSECONDS);
                } else {
                    task = scheduledTaskQueue.poll();
                }
            } else {
                task = taskQueue.take();
            }
        } catch (InterruptedException e) {
            // ignore
        }

        return task;
    }


    /**
     * 记录上一个task执行完成的时间
     */
    private void updateLastExecutionTime() {
        // todo
    }


}

package com.zman.thread.eventloop.impl;

import com.zman.thread.eventloop.EventLoop;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;


public class DefaultEventLoop implements EventLoop, Runnable{

    /**
     * 任务容器：每中任务类型拥有独立的队列，用于任务优先级调度
     */
    private final Map<String,BlockingQueue<Runnable>> taskContainer = new HashMap<>();

    /**
     * 是否停止了event loop
     */
    private boolean stop = false;

    /**
     * event loop的执行器
     */
    private Executor executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "event-loop-thread"));


    public DefaultEventLoop(){
        executor.execute(this);
    }

    public void run(){
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
        BlockingQueue<Runnable> queue = taskContainer.computeIfAbsent(taskType, _v -> new LinkedBlockingQueue<>(1000));
        FutureTask<T> futureTask = new FutureTask<>(task);
        boolean success = queue.offer(futureTask);
        if( !success ){
            FutureTask<T> queueFullException = new FutureTask<>(()->{throw new RuntimeException("queue is full");});
            queueFullException.run();
            return queueFullException;
        }else {
            return futureTask;
        }
    }

    public Future<String> shutdown(){
        return submit(TaskType.COMPUTE.name(), ()->{
            stop = true;
            return "success";
        });
    }

    /**
     * @return 一个任务，或者null
     */
    private Runnable takeTask() {
        Runnable futureTask = null;
        try {
            Iterator<BlockingQueue<Runnable>> iterator = taskContainer.values().iterator();
            while( iterator.hasNext() ){
                BlockingQueue<Runnable> futureTasks = iterator.next();
                if( !futureTasks.isEmpty() ){
                    futureTask = futureTasks.take();
                    break;
                }
            }

            if( futureTask == null ){
                Thread.sleep(10);   // 防止cpu空转
            }
        } catch (InterruptedException e) {
            // ignore
        }

        return futureTask;
    }


    /**
     * 记录上一个task执行完成的时间
     */
    private void updateLastExecutionTime() {
        // todo
    }

}

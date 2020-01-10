package com.zman.thread.eventloop;

import com.zman.thread.eventloop.exception.QueueExceedException;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * eventloop一旦创建就会开始运行loop，无需start，直接可以submit任务
 */
public interface EventLoop {

    /**
     * 设置队列中包含的最大任务个数
     * @param maxQueueSize 任务个数
     */
    void setMaxQueueSize(int maxQueueSize);

    /**
     * 提交任务
     * @param taskType 事件类型，用于区分不同类型的任务，用于做任务调度
     * @param task     任务
     * @param <T>      返回值的泛型
     * @return  future
     * @throws QueueExceedException 队列满了
     */
    <T> Future<T> submit(String taskType, Callable<T> task) throws QueueExceedException;

    /**
     * 提交任务
     * @param task  任务
     * @throws QueueExceedException 队列满了
     */
    void submit(Runnable task) throws QueueExceedException;

    /**
     * 提交任务
     * @param taskType  事件类型，用于区分不同类型的任务，用于做任务调度
     * @param task      任务
     * @param timeout   时间
     * @param timeUnit  单位
     * @param <T>       返回值的泛型
     * @return  future
     * @throws QueueExceedException 队列满了
     */
    <T> Future<T> submit(String taskType, Callable<T> task, long timeout, TimeUnit timeUnit) throws QueueExceedException;

    /**
     * 提交任务
     * @param task      任务
     * @param timeout   时间
     * @param timeUnit  单位
     * @throws QueueExceedException 队列满了
     */
    void submit(Runnable task, long timeout, TimeUnit timeUnit) throws QueueExceedException;


    /**
     * 停止event loop，尽快停止event loop，Queue中未执行的任务将不被执行了。
     * @return future
     */
    Future<Void> shutdown();
}

package com.zman.thread.eventloop;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * eventloop一旦创建就会开始运行loop，无需start，直接可以submit任务
 */
public interface EventLoop {

    /**
     * 提交任务
     * @param taskType 事件类型，用于区分不同类型的任务，用于做任务调度
     * @param task     任务
     * @param <T>      返回值的泛型
     * @return  future
     */
    <T> Future<T> submit(String taskType, Callable<T> task);


    /**
     * 停止event loop
     * @return future
     */
    Future<?> shutdown();
}

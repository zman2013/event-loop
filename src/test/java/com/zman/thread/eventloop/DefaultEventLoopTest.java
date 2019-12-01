package com.zman.thread.eventloop;

import com.zman.thread.eventloop.impl.DefaultEventLoop;
import com.zman.thread.eventloop.impl.TaskType;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DefaultEventLoopTest {

    @Test
    public void test() throws ExecutionException, InterruptedException {
        EventLoop eventLoop = new DefaultEventLoop();
        Future<Long> future = eventLoop.submit(TaskType.COMPUTE.name(), ()-> 1+1L);
        Assert.assertEquals(2L, future.get().longValue());
    }

}

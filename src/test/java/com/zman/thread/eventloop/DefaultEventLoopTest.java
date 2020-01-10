package com.zman.thread.eventloop;

import com.zman.thread.eventloop.exception.QueueExceedException;
import com.zman.thread.eventloop.impl.DefaultEventLoop;
import com.zman.thread.eventloop.impl.TaskType;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultEventLoopTest {

    @Test
    public void test() throws ExecutionException, InterruptedException, QueueExceedException {
        EventLoop eventLoop = new DefaultEventLoop("event-loop");
        Future<Long> future = eventLoop.submit(TaskType.COMPUTE.name(), ()-> 1+1L);
        Assert.assertEquals(2L, future.get().longValue());
    }

    @Test(expected = TimeoutException.class)
    public void testScheduledTask() throws ExecutionException, InterruptedException, TimeoutException, QueueExceedException {
        EventLoop eventLoop = new DefaultEventLoop("event-loop");
        Future<Long> future = eventLoop.submit(TaskType.COMPUTE.name(), ()-> 1+1L, 10, TimeUnit.MINUTES);
        Assert.assertEquals(2L, future.get(1, TimeUnit.MILLISECONDS).longValue());
    }

    @Test
    public void testScheduledTaskWithImmediateTask() throws ExecutionException, InterruptedException, TimeoutException, QueueExceedException {
        EventLoop eventLoop = new DefaultEventLoop("event-loop");

        Future<Long> future2 = eventLoop.submit(TaskType.COMPUTE.name(), ()-> 1+1L, 10, TimeUnit.MILLISECONDS);
        Future<Long> future = eventLoop.submit(TaskType.COMPUTE.name(), ()-> 1+1L);
        Assert.assertEquals(2L, future.get().longValue());
        Assert.assertEquals(2L, future2.get(100, TimeUnit.MILLISECONDS).longValue());
    }

    @Test
    public void testScheduledTaskNoImmediateTask() throws ExecutionException, InterruptedException, TimeoutException, QueueExceedException {
        EventLoop eventLoop = new DefaultEventLoop("event-loop");

        Future<Long> future = eventLoop.submit(TaskType.COMPUTE.name(), ()-> 1+1L, 10, TimeUnit.MILLISECONDS);
        Assert.assertEquals(2L, future.get(100, TimeUnit.MILLISECONDS).longValue());
    }

    @Test
    public void testTaskQueueFull() throws InterruptedException, QueueExceedException {
        EventLoop eventLoop = new DefaultEventLoop("event-loop");
        eventLoop.shutdown();

        QueueExceedException e = null;
        for( int i = 0; i < 1001; i ++) {
            try{
                eventLoop.submit(TaskType.COMPUTE.name(), () -> 1 + 1L, 10, TimeUnit.MINUTES);
            }catch (QueueExceedException e1){
                e = e1;
            }
        }
        Assert.assertNotNull(e);

        e = null;
        for( int i = 0; i < 1001; i ++) {
            try{
                eventLoop.submit(TaskType.COMPUTE.name(), () -> 1 + 1L);
            }catch (QueueExceedException e2){
                e = e2;
            }
        }
        Assert.assertNotNull(e);
    }


    @Test
    public void testBothTypeTask() throws InterruptedException, QueueExceedException {
        EventLoop eventLoop = new DefaultEventLoop("event-loop");
        AtomicInteger c = new AtomicInteger(0);

        eventLoop.submit("", ()-> {
                    for (int i = 0; i < 1000; i++) {
                        eventLoop.submit(TaskType.COMPUTE.name(), c::incrementAndGet, 3, TimeUnit.NANOSECONDS);
                        eventLoop.submit("", c::incrementAndGet);
                    }
                    return null;
                });

        Thread.sleep(1000);

        Assert.assertEquals(2000, c.get());
    }


    @Test
    public void testScheduleTaskNotReady() throws InterruptedException, QueueExceedException {
        EventLoop eventLoop = new DefaultEventLoop("event-loop");
        AtomicInteger c = new AtomicInteger(0);

        eventLoop.submit("", ()-> {
            for (int i = 0; i < 1000; i++) {
                eventLoop.submit(TaskType.COMPUTE.name(), c::incrementAndGet, 3, TimeUnit.MINUTES);
                eventLoop.submit("", c::incrementAndGet);
            }
            return null;
        });

        Thread.sleep(1000);

        Assert.assertEquals(1000, c.get());
    }


    @Test
    public void testSubmitRunnable() throws QueueExceedException, InterruptedException {
        EventLoop eventLoop = new DefaultEventLoop("event-loop");
        AtomicInteger c = new AtomicInteger(0);
        eventLoop.submit(()->c.incrementAndGet());
        eventLoop.submit(()->c.incrementAndGet(),1,TimeUnit.NANOSECONDS);

        Thread.sleep(1);

        Assert.assertEquals(2, c.get());
    }

}

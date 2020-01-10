[![Travis Build](https://api.travis-ci.org/zman2013/event-loop.svg?branch=master)](https://api.travis-ci.org/zman2013/event-loop.svg?branch=master)
[![Coverage Status](https://coveralls.io/repos/github/zman2013/event-loop/badge.svg?branch=master)](https://coveralls.io/github/zman2013/event-loop?branch=master)


# event-loop
a pure lightweight event-loop based on a single thread

## Dependency
```xml
<dependency>
    <groupId>com.zmannotes</groupId>
    <artifactId>event-loop</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Interface
```java
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
     * 提交任务
     * @param taskType  事件类型，用于区分不同类型的任务，用于做任务调度
     * @param task      任务
     * @param timeout   时间
     * @param timeUnit  单位
     * @param <T>       返回值的泛型
     * @return  future
     */
    <T> Future<T> submit(String taskType, Callable<T> task, long timeout, TimeUnit timeUnit);

    /**
     * 停止event loop
     * @return future
     */
    Future<?> shutdown();
}

```

## Example
```java
EventLoop eventLoop = new DefaultEventLoop("event-loop");
Future<Long> future = eventLoop.submit(TaskType.COMPUTE.name(), ()-> 1+1L);
Assert.assertEquals(2L, future.get().longValue());

Future<Long> future2 = eventLoop.submit(TaskType.COMPUTE.name(), ()-> 1+1L, 10, TimeUnit.MILLISECONDS);
Assert.assertEquals(2L, future2.get(100, TimeUnit.MILLISECONDS).longValue());
```
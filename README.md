# event-loop
a pure lightweight event-loop based on a single thread

## Dependency
```xml
<dependency>
    <groupId>com.zmannotes</groupId>
    <artifactId>event-loop</artifactId>
    <version>0.0.1</version>
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
     * 停止event loop
     * @return future
     */
    Future<?> shutdown();
}

```
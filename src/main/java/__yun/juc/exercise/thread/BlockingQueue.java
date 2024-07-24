package __yun.juc.exercise.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: __yun
 * @Date: 2024/07/24/8:16
 * @Description:
 */
@Slf4j
public class BlockingQueue<T> {
    // 容量
    private int capacity;
    // 队列
    private Deque<T> queue = new ArrayDeque<>();
    // 锁
    private ReentrantLock lock = new ReentrantLock();
    // 生产者条件变量
    private Condition producerWaitSet = lock.newCondition();
    // 消费者条件变量
    private Condition comsumerWaitSet = lock.newCondition();
    public T get(Long timeout, TimeUnit timeUnit){
        lock.lock();
        try {
            long nanos = timeUnit.toNanos(timeout);
            while (queue.isEmpty()){
                if (nanos <= 0){
                    return null;
                }
                nanos = producerWaitSet.awaitNanos(nanos);
            }
            T first = queue.removeFirst();
            comsumerWaitSet.signal();
            return first;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
    // 生产者
    public T get() {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                producerWaitSet.await();
            }
            T first = queue.removeFirst();
            comsumerWaitSet.signal();
            return first;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
    public boolean put(T t,Long timeout, TimeUnit timeUnit){
        lock.lock();
        try {
            long nanos = timeUnit.toNanos(timeout);
            while (queue.size() == capacity) {
                if (nanos <= 0){
                    return false;
                }
                nanos = comsumerWaitSet.awaitNanos(nanos);
            }
            queue.addLast(t);
            producerWaitSet.signal();
            return true;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
    public void put(T t){
        lock.lock();
        try {
            while (queue.size() == capacity) {
                comsumerWaitSet.await();
            }
            queue.addLast(t);
            producerWaitSet.signal();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
    public BlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    public void tryPut(T task, ThreadPool.RejectPolicy<T> rejectPolicy) {
        lock.lock();
        try {
            if (queue.size() < capacity){
                log.info("添加任务到队列..");
                queue.addLast(task);
                producerWaitSet.signal();
            }else {
                rejectPolicy.reject(this,task);
            }
        } finally {
            lock.unlock();
        }
    }
}

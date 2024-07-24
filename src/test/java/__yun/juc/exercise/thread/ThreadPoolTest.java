package __yun.juc.exercise.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: __yun
 * @Date: 2024/07/24/9:00
 * @Description:
 */
@Slf4j
class ThreadPoolTest {

    public static void main(String[] args) {
        ThreadPool threadPool = new ThreadPool(1, TimeUnit.SECONDS, 10, 2,(queue,task)->{
            log.info("执行拒绝策略..");
            queue.put(task);
        });
        for (int i = 0; i < 5; i++) {
            int j = i;
            threadPool.execute(()->{
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                log.info("任务执行:{}",j);
            });
        }

    }
}
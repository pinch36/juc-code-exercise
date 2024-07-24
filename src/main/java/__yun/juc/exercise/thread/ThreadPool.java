package __yun.juc.exercise.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: __yun
 * @Date: 2024/07/24/8:27
 * @Description:
 */
@Slf4j
public class ThreadPool {
    // 任务队列
    BlockingQueue<Runnable> queue;  // 任务队列
    // 线程池
    HashSet<Worker> workers = new HashSet<Worker>();
    // 线程池大小
    private int coreSize;
    // 时间度量
    private TimeUnit timeUnit;
    // 超时时间
    private long timeout;
    // 任务容量
    private int capacity;
    public void execute(Runnable task){
        // 懒加载
        synchronized (workers) {
            if (workers.size() < coreSize){
                Worker worker = new Worker(task);
                log.info("创建线程{}",worker);
                workers.add(worker);
                worker.start();
            }else{
                log.info("加入任务队列{}..",task);
                queue.put(task);
            }
        }
    }
    public ThreadPool(int coreSize, TimeUnit timeUnit, long timeout,int capacity) {
        this.coreSize = coreSize;
        this.timeUnit = timeUnit;
        this.timeout = timeout;
        this.capacity = capacity;
        queue = new BlockingQueue<Runnable>(capacity);
        log.info("线程池构造..");
    }

    private class Worker extends Thread{
        private Runnable task;

        public Worker(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
//            while (task != null || (task = queue.get()) != null){
            while (task != null || (task = queue.get(10L,TimeUnit.SECONDS)) != null){
                try {
                    log.info("线程{}执行..",this);
                    task.run();
                }catch (Exception e){
                    e.printStackTrace();
                }finally{
                    task = null;
                }
            }
            synchronized (workers) {
                log.info("线程删除{}..",this);
                workers.remove(this);
            }
        }
    }
}

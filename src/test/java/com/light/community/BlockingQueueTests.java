package com.light.community;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author light
 * @Description  阻塞队列测试
 * @create 2023-05-16 19:19
 */
public class BlockingQueueTests {
	public static void main(String[] args) {
		ArrayBlockingQueue blockingQueue = new ArrayBlockingQueue<>(10);
		new Thread(new Producer(blockingQueue)).start();
		new Thread(new Consumer(blockingQueue)).start();
		new Thread(new Consumer(blockingQueue)).start();
		new Thread(new Consumer(blockingQueue)).start();
	}
}

class Producer implements Runnable{

	private BlockingQueue<Integer> queue;
	public  Producer(BlockingQueue<Integer> queue){
		this.queue=queue;
	}
	@Override
	public void run() {
		try {
			for(int i=1;i<=100;i++){
				Thread.sleep(20);
				queue.put(i); //生产
				System.out.println(Thread.currentThread().getName()+" 生产："+queue.size());
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}

class Consumer implements Runnable{

	private BlockingQueue<Integer> queue;
	public  Consumer(BlockingQueue<Integer> queue){
		this.queue=queue;
	}
	@Override
	public void run() {
		try {
			while (true){
				Thread.sleep(new Random().nextInt(1000));
				queue.take(); //消费
				System.out.println(Thread.currentThread().getName()+" 消费："+queue.size());
			}

		}catch (Exception e){
			e.printStackTrace();
		}
	}
}

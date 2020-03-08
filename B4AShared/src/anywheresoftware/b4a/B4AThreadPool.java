
/*
 * Copyright 2010 - 2020 Anywhere Software (www.b4x.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 package anywheresoftware.b4a;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class B4AThreadPool {
	//not synchronized.
	private final WeakHashMap<Object, ConcurrentHashMap<Integer, Future<?>>> futures = new WeakHashMap<Object, ConcurrentHashMap<Integer, Future<?>>>();
	private final ConcurrentLinkedQueue<QueuedTask> queueOfTasks = new ConcurrentLinkedQueue<QueuedTask>();
	private ThreadPoolExecutor pool;
	private static final int THREADS_SPARE = 5;
	public B4AThreadPool() {
		pool = new ThreadPoolExecutor(0, 50,
				60L, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>()) {
			@Override
			protected void afterExecute(Runnable r, Throwable t) {
				for (int i = 0;i < 1;i++) {
					QueuedTask qt = queueOfTasks.poll();
					if (qt != null) {
						BA.handler.post(qt);
					}
				}
			}
		};
		pool.setThreadFactory(new MyThreadFactory());
	}
	private static class MyThreadFactory implements ThreadFactory {
		private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

		@Override
		public Thread newThread(Runnable r) {
			Thread t = defaultFactory.newThread(r);
			t.setDaemon(true);
			return t;
		}
	}

	public void submit(Runnable task, Object container, int taskId) {
		if (pool.getActiveCount() > pool.getMaximumPoolSize() - THREADS_SPARE) {
			queueOfTasks.add(new QueuedTask(task, container, taskId));
		}
		else {
			
			submitToPool(task, container, taskId);
		}

	}
	class QueuedTask implements Runnable {
		final Runnable task;
		final Object container;
		final int taskId;
		public QueuedTask(Runnable task, Object container, int taskId) {
			this.task = task;
			this.container = container;
			this.taskId = taskId;
		}
		public void run() {
			if (pool.getActiveCount() > pool.getMaximumPoolSize() - THREADS_SPARE) {
				BA.handler.postDelayed(this, 50);
			}
			else {
				submitToPool(task, container, taskId);
			}
		}
	}
	private void submitToPool(Runnable task, Object container, int taskId) {
		Future<?> f;
		try {
			f = pool.submit(task);
		} catch (RejectedExecutionException ree) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			submitToPool(task, container, taskId);
			return;
		}
		ConcurrentHashMap<Integer, Future<?>> map;
		synchronized (futures) {
			map = futures.get(container);
			if (map == null) {
				map = new ConcurrentHashMap<Integer, Future<?>>();
				futures.put(container, map);
			}
		}
		//clean finished tasks
		for (Iterator<Future<?>> it = map.values().iterator();it.hasNext();) {
			Future<?> fit = it.next();
			if (fit.isDone())
				it.remove();
		}
		map.put(taskId, f);
	}
	public boolean isRunning(Object container, int taskId) {
		ConcurrentHashMap<Integer, Future<?>> map = futures.get(container);
		if (map == null)
			return false;
		Future<?> f = map.get(taskId);
		if (f == null)
			return false;
		return !f.isDone();
	}
	public void markTaskAsFinished(Object container, int taskId) {
		ConcurrentHashMap<Integer, Future<?>> map = futures.get(container);
		if (map == null)
			return;
		map.remove(taskId);
	}

}

/*-
 * Copyright © 2018 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.daq.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link ThreadFactory} implementation to help creation of {@link ExecutorService}s. Instances should not be
 * created directly but should use the {@link Threads} builder instead.<p>
 *
 * Intended to replace use of
 * <pre>
 * ExecutorService service = Executors.newSingleThreadExecutor((r) -> {
 *     Thread t = new Thread(r, "name of thread");
 *     t.setDaemon(true);
 *     return t;
 * });
 * </pre>
 * with
 * <pre>
 * ExecutorService service = Executors.newSingleThreadExecutor(
 *         Threads.daemon().named("name of thread").factory()
 * );
 * </pre>
 * @since 9.8
 * @see ThreadFactory
 * @see ExecutorService
 */
public class GdaThreadFactoryBuilder {

	/** Counter give automatically named factories unique names */
	private static final AtomicInteger factoryCounter = new AtomicInteger();

	/** The template to use when group name is not set directly */
	private static final String GROUP_NAME_TEMPLATE = "GdaThreadFactoryGroup-%d";

	/** Default {@link UncaughtExceptionHandler} - just logs the error and the thread it came from */
	public static final UncaughtExceptionHandler DEFAULT_EXCEPTION_HANDLER = new GdaExceptionHandler();

	/** Threads are given a unique name based on a given name and an id */
	private final AtomicInteger threadCounter = new AtomicInteger();

	/** The class loader used by the created threads */
	private ClassLoader loader;

	/** Daemon status of created threads */
	private boolean daemon = true;

	/** The name of created threads - threads will have a unique (per thread factory) id appended */
	private String name = "GdaThreadFactory-%d";

	/** The priority of created threads @see Thread#setPriority(int) */
	private int priority = Thread.NORM_PRIORITY;

	/** The name for the group created threads are part of */
	private String group;

	/** The handler for uncaught exceptions thrown in created threads
	 * @see Thread#setUncaughtExceptionHandler(UncaughtExceptionHandler)
	 */
	private UncaughtExceptionHandler handler = DEFAULT_EXCEPTION_HANDLER;

	private GdaThreadFactoryBuilder() {
	}

	/**
	 * Set name for created threads to have. Names are built using {@link String#format(String, Object...)} and
	 * the given name and format objects.
	 *
	 * @param nameFormat Name format to use for created threads
	 * @param args Optional arguments used to format name
	 * @return GdaThreadFactory
	 *
	 * @see Thread#setName(String)
	 * @see String#format(String, Object...)
	 */
	public GdaThreadFactoryBuilder named(String nameFormat, Object... args) {
		name = String.format(nameFormat, args) + "-%d";
		return this;
	}

	/**
	 * Set the priority for created threads to have.
	 *
	 * @param priority Priority of created threads.
	 * @return GdaThreadFactory
	 *
	 * @see Thread#setPriority(int)
	 */
	public GdaThreadFactoryBuilder priority(int priority) {
		this.priority = priority;
		return this;
	}

	/**
	 * Set the {@link UncaughtExceptionHandler} for created threads
	 *
	 * @param handler Handler for uncaught exceptions
	 * @return GdaThreadFactory
	 *
	 * @see UncaughtExceptionHandler
	 * @see Thread#setUncaughtExceptionHandler(UncaughtExceptionHandler)
	 */
	public GdaThreadFactoryBuilder uncaughtExceptionHandler(UncaughtExceptionHandler handler) {
		this.handler = handler;
		return this;
	}

	/**
	 * Set this ThreadFactory to create user threads to allow threads to keep process alive.
	 * Threads are created as daemon threads otherwise.
	 *
	 * @return GdaThreadFactory
	 *
	 * @see Thread#setDaemon(boolean)
	 */
	public GdaThreadFactoryBuilder user() {
		daemon = false;
		return this;
	}

	/**
	 * Set the classloader for created threads to use
	 *
	 * @param loader ClassLoader for created threads
	 * @return GdaThreadFactory
	 *
	 * @see ClassLoader
	 * @see Thread#setContextClassLoader(ClassLoader)
	 */
	public GdaThreadFactoryBuilder classLoader(ClassLoader loader) {
		this.loader = loader;
		return this;
	}

	/**
	 * Set the name for the {@link ThreadGroup} shared by all created threads
	 *
	 * @param group name
	 * @return GdaThreadFactory
	 *
	 * @see Thread#Thread(ThreadGroup, Runnable)
	 * @see Thread#getThreadGroup()
	 */
	public GdaThreadFactoryBuilder group(String group) {
		this.group = group;
		return this;
	}

	/**
	 * Build the ThreadFactory with the given configuration
	 *
	 * @return ThreadFactory that creates threads configured with the setting of this builder
	 */
	public ThreadFactory factory() {
		if (group == null) {
			group = String.format(GROUP_NAME_TEMPLATE, factoryCounter.getAndIncrement());
		}
		ThreadGroup tGroup = new ThreadGroup(group);
		return r -> {
			Thread t = new Thread(tGroup, r, String.format(name, threadCounter.getAndIncrement()));
			if (loader == null) {
				loader = Thread.currentThread().getContextClassLoader();
			}
			t.setContextClassLoader(loader);
			t.setDaemon(daemon);
			t.setPriority(priority);
			t.setUncaughtExceptionHandler(handler);
			return t;
		};
	}

	/**
	 * GdaThreadFactory builder to allow static use of GdaThreadFactory. Threads are daemon by default.<p>
	 * Expected use would be with {@link ExecutorService}s<p>
	 * <pre>
	 * ExecutorService threadPool = Executors.newFixedThreadPool(3,
	 *         Threads.daemon().named("pool_threads").priority(Thread.NORM_PRIORITY)
	 * );
	 * </pre>
	 *
	 * @since 9.8
	 */
	public static final class Threads {

		private Threads() {
			// Prevent instances
		}

		/**
		 * Create a Thread builder set to build daemon threads. This is the default.
		 * @return A GdaThreadFactoryBuilder
		 *
		 * @see Thread#setDaemon(boolean)
		 */
		public static GdaThreadFactoryBuilder daemon() {
			return new GdaThreadFactoryBuilder();
		}

		/**
		 * Create a Thread builder set to build user threads. By default threads are created as daemon threads.
		 * @return A GdaThreadFactoryBuilder
		 *
		 * @see Thread#setDaemon(boolean)
		 */
		public static GdaThreadFactoryBuilder user() {
			return new GdaThreadFactoryBuilder().user();
		}

		/**
		 * Create a Thread builder set to build named threads. Threads are named using
		 * {@link String#format(String, Object...)} with the given arguments.
		 * @param nameFormat Format string used with String.format to name threads
		 * @param args Objects to use with string name
		 * @return A GdaThreadFactoryBuilder
		 *
		 * @see Thread#setName(String)
		 */
		public static GdaThreadFactoryBuilder named(String nameFormat, Object... args) {
			return new GdaThreadFactoryBuilder().named(nameFormat, args);
		}

		/**
		 * Create a Thread builder set to build threads with the given priority.
		 * @param priority The priority to run the created threads with
		 * @return A GdaThreadFactory
		 *
		 * @see Thread#setPriority(int)
		 */
		public static GdaThreadFactoryBuilder priority(int priority) {
			return new GdaThreadFactoryBuilder().priority(priority);
		}

		/**
		 * Create a Thread builder set to build threads with the given {@link UncaughtExceptionHandler}.
		 *
		 * @param handler
		 * @return A GdaThreadFactory
		 *
		 * @see Thread#setUncaughtExceptionHandler(UncaughtExceptionHandler)
		 */
		public static GdaThreadFactoryBuilder uncaughtExceptionHandler(UncaughtExceptionHandler handler) {
			return new GdaThreadFactoryBuilder().uncaughtExceptionHandler(handler);
		}

		/**
		 * Create a Thread builder set to build threads with the given {@link ClassLoader}.
		 *
		 * @param loader ClassLoader for created threads to use
		 * @return A GdaThreadFactory
		 *
		 * @see Thread#setContextClassLoader(ClassLoader)
		 */
		public static GdaThreadFactoryBuilder classLoader(ClassLoader loader) {
			return new GdaThreadFactoryBuilder().classLoader(loader);
		}

		/**
		 * Create a ThreadFactory builder to build threads in a {@link ThreadGroup} with the given name
		 *
		 * @param groupName The name of the {@link ThreadGroup} shared by Threads created by this factory
		 * @return {@link GdaThreadFactoryBuilder}
		 *
		 * @see Thread#getThreadGroup()
		 */
		public static GdaThreadFactoryBuilder group(String groupName) {
			return new GdaThreadFactoryBuilder().group(groupName);
		}

		/**
		 * Create a ThreadFactory with default settings.
		 *
		 * @return A default ThreadFactory
		 */
		public static ThreadFactory factory() {
			return new GdaThreadFactoryBuilder().factory();
		}
	}

	private static final class GdaExceptionHandler implements UncaughtExceptionHandler {
		private static final Logger logger = LoggerFactory.getLogger(GdaExceptionHandler.class);

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			logger.error("Unhandled exception from thread: {}", t.getName(), e);
		}

	}
}

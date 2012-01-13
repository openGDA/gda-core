/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.util;

import java.util.Collection;
import java.util.HashSet;

/**
 * This class is used to ensure that in GDA all 'uk.ac.gda.util.ThreadManager.getThread(' lines are
 * transferred to ThreadManager.createNew(...) lines.
 * 
 * This ensures that all GDA threads have a name as some were previously not named.
 * 
 * Later this central point for Threads might be upgraded to use ThreadGroups or 
 * to use ExecutorService.
 */
public class ThreadManager {

	protected static Collection<String> takenNames = new HashSet<String>(89);
	
	/**
	 * @return a thread automatically named from the calling class name.
	 */
	public static Thread getThread() {
		final Thread thread = new Thread();
		createUniqueNameFromStack(thread);
		return thread;
	}
	
	/**
	 * @param target 
	 * @return a thread automatically named from the calling class name.
	 */
	public static Thread getThread(Runnable target) {
		final Thread thread = new Thread(target);
		createUniqueNameFromStack(thread);
		return thread;
	}
	
	/**
	 * Names a thread from the calling stack.
	 * @param thread
	 */
	public static void createUniqueNameFromStack(final Thread thread) {
		final StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		String call = null;
		for (int i = 1; i < stack.length; i++) {
			final String name = stack[i].getClassName();
			if (!name.equals(ThreadManager.class.getName())) {
				call = name;
				break;
			}
		}
		
		if (call!=null) thread.setName(ThreadManager.getUniqueName(call));
	}

	protected static String getUniqueName(final String call) {
		int count = 1;
		String name = "GDA Thread "+call+"_"+count;
		while(takenNames.contains(name)) {
			++count;
			name = "GDA Thread "+call+"_"+count;
		}
		takenNames.add(name);
		if (takenNames.size()>100) takenNames.clear();
		return name;
	}

	/**
	 * @param target 
	 * @param name 
	 * @return a thread made by the uk.ac.gda.util.ThreadManager.getThread(target, name) constructor.
	 */
	public static Thread getThread(Runnable target, String name) {
        return new Thread(target, name);
	}
	
	/**
	 * @param name 
	 * @return a thread made by the uk.ac.gda.util.ThreadManager.getThread(name) constructor.
	 */
	public static Thread getThread(final String name) {
        return new Thread(name);
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ThreadNameStackTester t = new ThreadNameStackTester();
		t.create();
	}
	
	private static class ThreadNameStackTester {
		void create() {
			final Thread blank = ThreadManager.getThread();
			final Thread run   = ThreadManager.getThread(new Runnable() {
				@Override
				public void run() {
					System.out.println("Hello");
				}
			});
			final Thread named1 = ThreadManager.getThread(new Runnable() {
				@Override
				public void run() {
					System.out.println("you");
				}
			}, "Name1");
			final Thread named2 = ThreadManager.getThread("Name2");

			System.out.println(blank.getName());
			System.out.println(run.getName());
			System.out.println(named1.getName());
			System.out.println(named2.getName());
		}
	}
}

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

package org.eclipse.scanning.test.malcolm.real;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * <p>A custom {@link Answer} that collects the argument of the method it is used for.
 * This answer only currently works with methods that take a single argument.
 * This class uses a supplied copy function to clone the argument, so that
 * we can verify the argument as it was when the method was called, not when
 * we do the verification.</p>
 *
 * <p>Note: it would have been nice to create something like {@link ArgumentCaptor}
 * (probably by extending it) that works exactly the same as that, but also
 * copying the argument, but this was not possible as it would require using internal
 * package of the mockito plug-in jar.</p>
 *
 * @param <T>
 */
public class BeanCollectingAnswer<T> implements Answer<Void> {

	private final Class<T> argClass;

	private final Function<T, T> copyFunction;

	private List<T> arguments = new LinkedList<>();

	public static <T> BeanCollectingAnswer<T> forClass(
			Class<T> argClass, Function<T, T> copyFunction) {
		return new BeanCollectingAnswer<>(argClass, copyFunction);
	}

	private BeanCollectingAnswer(Class<T> argClass, Function<T, T> copyFunction) {
		this.argClass = argClass;
		this.copyFunction = copyFunction;
	}

	@Override
	public Void answer(InvocationOnMock invocation) throws Throwable {
		final Object[] args = invocation.getArguments();
		if (args.length == 0) {
			throw new AssertionError("The method was called with no arguments");
		}
		if (args.length > 1) {
			throw new AssertionError("The method was called with more than one argument");
		}

		@SuppressWarnings("unchecked")
		final T argument = (T) invocation.getArguments()[0];
		if (!argClass.isInstance(argument)) {
			throw new ClassCastException("The argument is not of the expected type " + argClass.getName() + ": " + argument);
		}
		final T copy = copyFunction.apply(argument);
		arguments.add(copy);
		return null;
	}

	/**
	 * Returns the collected value of the argument, and clears this object for next use.
	 * Use this method if the method was called exactly once since the previous use.
	 * @return
	 * @throws AssertionError if this method
	 */
	public T getValue() throws AssertionError {
		if (arguments.isEmpty()) {
			throw new AssertionError("The method was not invoked");
		}
		if (arguments.size() > 1) {
			throw new AssertionError("The method was invoked more than once");
		}

		return getAllValues().get(0);
	}

	/**
	 * Returns all collected values and clears for next use. Use this method if the method was
	 * called multiple times since the previous use.
	 * @return
	 */
	public List<T> getAllValues() {
		final List<T> arguments = this.arguments;
		this.arguments = new LinkedList<>();
		return arguments;
	}

}

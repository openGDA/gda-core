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

package org.eclipse.scanning.test.util;

import java.util.function.Consumer;

/**
 * A utility class holding some methods for making lambda easier to work with
 * exception. More methods can be added to this class as required.
 */
public final class LambdaUtils {

	private LambdaUtils() {
		// private constructor to prevent instantiation
	}

	/**
	 * An interface
	 * @param <T>
	 */
	@FunctionalInterface
	public interface ExceptionThrowingConsumer<T> {
		void accept(T t) throws Exception;
	}

	/**
	 * Returns a {@link Consumer} that wraps an {@link ExceptionThrowingConsumer}.
	 * by wrapping then in a call to this method, a lambda expression that would otherwise
	 * match the {@link Consumer} functional interface can be passed to a
	 * method that takes a {@link Consumer}, such as {@link Iterable#forEach(Consumer)}. For example:
	 * <p>
	 * <pre>
	 *    List<String> strings = Arrays.toList("one", "two, "three");
	 *    strings.forEach(str -> str.process
	 * </pre>
	 * Note that the method the consumer is passed to may now throw a {@link RuntimeException}
	 * wrapping the original exception.
	 *
	 * @param consumer
	 * @return
	 */
	public static <T> Consumer<T> wrap(ExceptionThrowingConsumer<T> consumer) {
		return t -> {
			try {
				consumer.accept(t);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}


}

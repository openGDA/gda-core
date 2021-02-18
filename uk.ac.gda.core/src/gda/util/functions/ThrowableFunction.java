/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.util.functions;

import java.util.function.Function;

/**
 * Represents a function that accepts one argument and produces a result,
 * while supporting checked exception throws in the function.
 *
 * It provides method to {@link #unchecked(ThrowableFunction)} the checked exception,
 * i.e. convert the checked exception into runtime exception to work around the limitation
 * of current lambda, stream functions.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @param <E> the checked exception
 *
 */
@FunctionalInterface
public interface ThrowableFunction<T, R, E extends Throwable> {
	R apply(T t) throws E;

	static <T, R, E extends Throwable> Function<T,R> unchecked(ThrowableFunction<T, R, E> f) {
		return t -> {
			try {
				return f.apply(t);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};
	}
}


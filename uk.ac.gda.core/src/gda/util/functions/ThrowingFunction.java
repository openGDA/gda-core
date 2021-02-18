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

import java.util.Objects;
import java.util.function.Function;

/**
 * A {@link Function} interface that supports checked exception throws.
 *
 * It converts the checked exception into runtime exception to work around the limitation
 * of current lambda, stream functions.
 *
 * As a work around, it should only be used in place that the checked exception is not required to be handled.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> extends Function<T, R> {
	@Override
	default R apply(T t) {
		try {
			return applyThrows(t);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	R applyThrows(T t) throws Exception;

	default <V> ThrowingFunction<T, V> andThen(ThrowingFunction<? super R, ? extends V> after) {
		Objects.requireNonNull(after);
		try {
			return (T t) -> after.apply(apply(t));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	default <V> ThrowingFunction<V, R> compose(ThrowingFunction<? super V, ? extends T> before) {
		Objects.requireNonNull(before);
		try {
			return (V v) -> apply(before.apply(v));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}

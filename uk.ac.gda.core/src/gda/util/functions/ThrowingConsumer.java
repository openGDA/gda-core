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
import java.util.function.Consumer;

/**
 * A {@link Consumer} functional interface that supports checked exception throws.
 * It converts the checked exception into runtime exception to work around the limitation
 * of current lambda, stream functions.
 *
 * As a work around, it should only be used in place that the checked exception is not required to be handled.
 *
 * @param <T> the type of the input to the operation
 *
 */
@FunctionalInterface
public interface ThrowingConsumer<T> extends Consumer<T> {
	@Override
	default void accept(T t) {
		try {
			acceptThrows(t);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
	/**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void acceptThrows(T t) throws Exception;

    default ThrowingConsumer<T> andThen(ThrowingConsumer<? super T> after) {
		Objects.requireNonNull(after);
		try {
			return (T t) -> { accept(t); after.accept(t); };
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}

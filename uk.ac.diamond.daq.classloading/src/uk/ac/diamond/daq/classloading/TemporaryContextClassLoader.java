/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.classloading;

import static java.util.Objects.requireNonNull;

/**
 * Temporary context to allow code block to be run with an alternative class loader.
 * </br>
 * Intended to be used in a try-with-resources block
 * <pre>
 * try (var tccl = new TemporaryContextClassLoader(newClassLoader)) {
 *     // Code requiring new class loader
 * }
 * </pre>
 * is equivalent to
 * <pre>
 * var previous = Thread.currentThread().getContextClassLoader();
 * try {
 *     Thread.currentThread().setContextClassLoader(newClassLoader);
 *     // Code requiring new class loader
 * } finally {
 *     Thread.currentThread().setContextClassLoader(previous);
 * }
 * </pre>
 */
public class TemporaryContextClassLoader implements AutoCloseable {

	private final ClassLoader previous;

	public TemporaryContextClassLoader(ClassLoader newClassLoader) {
		requireNonNull(newClassLoader, "New class loader must not be null");
		previous = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(newClassLoader);
	}

	/**
	 * Revert the thread context class loader to the one being used before this
	 * context was created
	 */
	@Override
	public void close() {
		Thread.currentThread().setContextClassLoader(previous);
	}
}

/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package gda.jython;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.python.core.PySystemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for registering builtins.
 * <br />
 * These cannot be part of {@link GdaBuiltin} due to the {@code @Untraversable} annotation
 * in its superclass.
 */
public final class GdaBuiltinManager {

	private static final Logger logger = LoggerFactory.getLogger(GdaBuiltinManager.class);

	private GdaBuiltinManager() {

	}

	/** Create builtin from <name, List<Methods>> map entry. Mainly to be used in Map.foreach method */
	private static GdaBuiltin builtinsForGroup(Map.Entry<String, List<Method>> methodGroup) {
		logger.debug("Creating builtin function for {} from {} method(s)",
				methodGroup.getKey(),
				methodGroup.getValue().size());
		return new GdaBuiltin(methodGroup.getKey(), methodGroup.getValue());
	}

	/**
	 * Create a Python Callable for each static method in the given class
	 * and register them as builtins with the current PySystemState.
	 * @param clazz with static/annotated methods
	 */
	public static void registerBuiltinsFrom(Class<?> clazz) {
		logger.debug("Registering builtin functions from {}", clazz.getCanonicalName());
		builtinMethodsFrom(clazz)
				.forEach(m -> PySystemState.getDefaultBuiltins().__setitem__(m.getName(), m));
	}

	/**
	 * Create a builtin wrapper around all annotated static methods in the given class
	 * @param clazz with annotated static methods
	 * @return a collection of builtin functions
	 */
	public static Collection<GdaBuiltin> builtinMethodsFrom(Class<?> clazz) {
		return Stream.of(clazz.getMethods())
				.filter(m -> isStatic(m.getModifiers()))
				.filter(m -> m.isAnnotationPresent(GdaJythonBuiltin.class))
				.collect(groupingBy(Method::getName))
				.entrySet()
				.stream()
				.map(GdaBuiltinManager::builtinsForGroup)
				.collect(toList());
	}
}

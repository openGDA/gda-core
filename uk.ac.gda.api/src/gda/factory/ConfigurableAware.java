/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.factory;

/**
 * An additional interface in the Configurable system that allows other beans to be aware
 * of the configuration process. Some actions required at start up need to be able to guarantee that all
 * configurables have been configured. This is not possible during an objects own configure as it cannot
 * control the order in which it will be called.
 * <p>
 * All methods default to no-ops to allow classes to only implement the required hooks.
 */
public interface ConfigurableAware {

	/**
	 * Called before the configuration process starts when no beans have been configured.<p>
	 * Order in which implementations of this method are called is undetermined.<p>
	 * @throws FactoryException if thrown, considered a fatal configuration exception
	 */
	@SuppressWarnings("unused") // exception not thrown in no-op default
	default void preConfigure() throws FactoryException {}

	/**
	 * Called before each bean is configured.
	 * @param bean about to be configured.
	 */
	@SuppressWarnings("unused") //bean intended to be used by implementations
	default void preConfigureBean(Configurable bean) {}

	/**
	 * Called after each bean is configured.
	 * @param bean that has just been configured.
	 * @param error any exception raised during configure - null if successful
	 */
	@SuppressWarnings("unused") //bean intended to be used by implementations
	default void postConfigureBean(Configurable bean, Exception error) {}

	/**
	 * Called after the configuration process is complete.<p>
	 * When this is called, all beans are guaranteed to have been configured
	 * although the order in which implementations of this method are called is
	 * undetermined
	 * @throws FactoryException if thrown, considered a fatal configuration exception
	 */
	@SuppressWarnings("unused") // exception not thrown in no-op default
	default void postConfigure() throws FactoryException {}
}

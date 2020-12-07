/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api;

/**
 *
 * This is supposed to fit any object which takes a model and
 * can notify the user if the a given model is valid or not.
 * For instance IRunnableDevice, IPointGenerator.
 *
 * IMPORTANT: A model should not be an IValidator. Models should be
 * maintained as vanilla as possible.
 *
 * @author Matthew Gerring
 *
 */
public interface IValidator<T> {

	/**
	 * The validation server will set itself on any validator incase that validator
	 * want to validate sub-parts of a complex model.
	 *
	 * @param vservice
	 */
	default void setService(IValidatorService vservice) {
		// do nothing by default, implementations should override
	}

	/**
	 * If the given model is considered "invalid", this method may either throw a ModelValidationException explaining
	 * why it is considered invalid or return a modified version of the model that does pass validation. If the model is
	 * valid, it is returned. A model should be considered invalid if its parameters would cause the generator
	 * implementation to hang or crash.
	 *
	 * @param model
	 *            - a model of type T to validate
	 * @return a valid model
	 * @throws ModelValdiationException
	 *             if model invalid
	 */

	T validate(T model);

}

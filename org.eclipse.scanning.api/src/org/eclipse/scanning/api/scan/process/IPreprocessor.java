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
package org.eclipse.scanning.api.scan.process;

import org.eclipse.scanning.api.event.scan.ScanRequest;

/**
 *
 * <p>
 * A preprocessor which can make changes to a {@link ScanRequest} before it is processed.
 * For example this may transform the scan request according to the particular hardware of a beamline.
 * <p>
 * Preprocessors can be registered as OSGi services (perhaps in Spring config files, using the OSGiServiceRegister
 * class) and will then be used by the ScanServlet to process scan requests before they are run.
 *
 * @author Matthew Gerring
 *
 */
public interface IPreprocessor {

	/**
	 * Preprocessor name.
	 *
	 * @return name
	 */
	String getName();

	/**
	 * Runs the preprocessor on the given {@link ScanRequest}. This may modify the {@link ScanRequest}
	 * in place or return an entirely new {@link ScanRequest} object.
	 *
	 * @param req The request sent by the user interface
	 * @return the processed {@link ScanRequest}
	 */
	<T> ScanRequest preprocess(ScanRequest req) throws ProcessingException;
}

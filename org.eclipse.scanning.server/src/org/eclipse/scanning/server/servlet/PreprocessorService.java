/*-
 *******************************************************************************
 * Copyright (c) 2011, 2023 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.server.servlet;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.scanning.api.scan.process.IPreprocessor;

import uk.ac.diamond.osgi.services.ServiceProvider;


/**
 * An OSGi service to aggregate the configured {@link IPreprocessor}s.
 * This class is an OSGi component. It should be accessed via the {@link ServiceProvider},
 * i.e. {@code ServiceProvider#getService(PreprocessorService.class).getPreprocessors()}
 */
public class PreprocessorService {

	private Set<IPreprocessor> preprocessors = new LinkedHashSet<>();

	public synchronized void addPreprocessor(IPreprocessor preprocessor) {
		preprocessors.add(preprocessor);
	}

	public synchronized void removePreprocessor(IPreprocessor preprocessor) {
		preprocessors.remove(preprocessor);
	}

	public Set<IPreprocessor> getPreprocessors() {
		return preprocessors;
	}
}

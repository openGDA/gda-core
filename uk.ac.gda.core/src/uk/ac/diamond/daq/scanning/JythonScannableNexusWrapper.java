/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.scanning;

import org.eclipse.dawnsci.nexus.NXobject;

import gda.device.Scannable;
import gda.jython.InterfaceProvider;

public class JythonScannableNexusWrapper<N extends NXobject> extends ScannableNexusWrapper<N> {

	private String scannableName;

	public JythonScannableNexusWrapper(String scannableName) {
		this.scannableName = scannableName;
	}

	/**
	 * Set the scannable name to that given. This should only be used to set up the
	 * {@link JythonScannableNexusWrapper}, e.g. from Spring.
	 * @param scannableName
	 */
	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	/**
	 * Returns the Jython scannable for the name set in this wrapper
	 * @throws NullPointerException if no such scannable exists
	 */
	@Override
	public Scannable getScannable() {
		try {
			Object jythonObj = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(scannableName);
			if (jythonObj instanceof Scannable) {
				return (Scannable) jythonObj;
			}
		} catch (Exception e) {
			// JythonServerFacade.getCurrentInstance() never actually throws an exception
		}

		throw new NullPointerException("No such scannable in the jython namespace: " + scannableName);
	}

	/**
	 * Overriden to throw {@link UnsupportedOperationException}. As a {@link JythonScannableNexusWrapper}
	 * gets the scannable with a given name each time it is required, use {@link #setScannableName(String)} instead.
	 * @param scannable
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public void setScannable(Scannable scannable) {
		throw new UnsupportedOperationException("Use setScannableName() instead");
	}

	/**
	 * Jython scannables cannot be renamed, as we cannot update the name in the jython namespace
	 */
	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException("Cannot rename a jython scannable: " + scannableName);
	}

}

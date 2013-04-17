/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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
package uk.ac.diamond.tomography.reconstruction.views;

import org.eclipse.jface.viewers.ISelection;

public class ParametersSelection implements ISelection {

	private double centreOfRotation = 0;
	private final String nexusFileFullPath;

	public ParametersSelection(String nexusFileFullPath, double centreOfRotation) {
		this.nexusFileFullPath = nexusFileFullPath;
		this.centreOfRotation = centreOfRotation;
	}

	public String getNexusFileFullPath() {
		return nexusFileFullPath;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	public double getCentreOfRotation() {
		return centreOfRotation;
	}

}
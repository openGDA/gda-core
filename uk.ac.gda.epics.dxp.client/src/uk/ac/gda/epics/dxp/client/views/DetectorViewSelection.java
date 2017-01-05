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

package uk.ac.gda.epics.dxp.client.views;

import org.eclipse.jface.viewers.ISelection;

import uk.ac.gda.epics.dxp.client.BeamlineHutch;
import uk.ac.gda.epics.dxp.client.BeamlineHutch.Collimator;

public class DetectorViewSelection implements ISelection {

	private BeamlineHutch activeHutch;

	private BeamlineHutch.Collimator activeCollimator;

	public DetectorViewSelection(BeamlineHutch activeHutch, Collimator activeCollimator) {
		this.activeHutch = activeHutch;
		this.activeCollimator = activeCollimator;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	public Collimator getActiveCollimator() {
		return activeCollimator;
	}

	public BeamlineHutch getActiveHutch() {
		return activeHutch;
	}

}

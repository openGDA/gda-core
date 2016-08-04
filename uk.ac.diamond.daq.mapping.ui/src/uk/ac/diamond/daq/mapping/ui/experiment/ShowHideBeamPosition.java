/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class ShowHideBeamPosition extends AbstractHandler {

	private static BeamPositionPlotter beamPositionPlotter;

	public synchronized void setBeamPositionPlotter(BeamPositionPlotter beamPositionPlotter) {
		ShowHideBeamPosition.beamPositionPlotter = beamPositionPlotter;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		beamPositionPlotter.init();
		beamPositionPlotter.toggleShowBeamPosition();
		return null;
	}
}

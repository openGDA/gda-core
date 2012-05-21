/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;
import uk.ac.gda.exafs.ui.PlottingPerspective;
import uk.ac.gda.exafs.ui.data.ScanObjectManager;

public class SwitchToPlotPerspectiveHandler extends AbstractHandler {

	private static Logger logger = LoggerFactory.getLogger(SwitchToPlotPerspectiveHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String perspectiveID = PlottingPerspective.ID;
		try {
			IScanParameters scObj = ScanObjectManager.getCurrentScan();
			if (scObj != null && scObj instanceof MicroFocusScanParameters)
				perspectiveID = "uk.ac.gda.microfocus.ui.MicroFocusPerspective";
			PlatformUI.getWorkbench().showPerspective(perspectiveID,
					PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		} catch (WorkbenchException e) {
			logger.error("Cannot change perspective to " + perspectiveID, e);
		} catch (Exception e) {
			logger.error("Cannot change perspective to " + perspectiveID, e);
		}
		return null;
	}

}

/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.client.liveplot;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class HideOldHandler extends AbstractHandler {

	public static final String ID = "uk.ac.gda.client.xyPlotHideOldCommand";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart view = HandlerUtil.getActivePartChecked(event);
		if(view instanceof LivePlotView){
			LivePlotView livePlotView = (LivePlotView)view;
			//boolean enabled = false;
			boolean enabled=!livePlotView.getHideOldestScan();
			livePlotView.setHideOldestScan(enabled);
		}
	return null;
	}

}

/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.client.livecontrol;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToggleLayoutControl extends AbstractHandler  {

	private static final Logger logger = LoggerFactory.getLogger(ToggleLayoutControl.class);


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		LiveControlsView liveControlsView = getLiveControlsView(event);
		List<LiveControl> controls = liveControlsView.getDisplayedControlSet().getControls();

		controls.stream()
			.filter(ScannablePositionerControl.class::isInstance)
			.map(ScannablePositionerControl.class::cast)
			.forEach(ScannablePositionerControl::toggleLayoutControl);

		// Toggle positioners in the LiveControlGroups
		controls.stream()
			.filter(LiveControlGroup.class::isInstance)
			.map(LiveControlGroup.class::cast)
			.forEach(LiveControlGroup::toggleLayoutControl);

		controls.stream()
		.filter(JythonScriptListControl.class::isInstance)
		.map(JythonScriptListControl.class::cast)
		.forEach(JythonScriptListControl::toggleLayoutControl);

		IWorkbenchPage page = liveControlsView.getViewSite().getPage();
		page.hideView(liveControlsView);
		try {
			page.showView(LiveControlsView.ID, liveControlsView.getDisplayedControlSet().getName(), IWorkbenchPage.VIEW_ACTIVATE);
		} catch (PartInitException e) {
			logger.error("show view {} is failed.", LiveControlsView.ID, e);
		}

		return null;
	}

	private LiveControlsView getLiveControlsView(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (!(part instanceof LiveControlsView)) {
			throw new ExecutionException("Active part should be " + LiveControlsView.class.getName());
		}
		return (LiveControlsView) part;
	}
}

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

package uk.ac.gda.client.microfocus.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import uk.ac.gda.client.microfocus.views.ExafsSelectionView;
import uk.ac.gda.client.microfocus.views.scan.MicroFocusElementListView;

public class AddPointToExafsSelectionViewHandler extends AbstractHandler {

	public static String ID = "uk.ac.gda.client.microfocus.addpointtoexafsselectionviewhandler";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		MicroFocusElementListView mfElements = (MicroFocusElementListView) PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().findView(MicroFocusElementListView.ID);

		Double[] xyz = mfElements.getLastXYZSelection();

		ExafsSelectionView selectionView = (ExafsSelectionView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().findView(ExafsSelectionView.ID);
		selectionView.setSelectedPoint(xyz);

		return null;
	}

}

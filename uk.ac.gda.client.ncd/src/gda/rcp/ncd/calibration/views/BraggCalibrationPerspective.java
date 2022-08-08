/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.rcp.ncd.calibration.views;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class BraggCalibrationPerspective implements IPerspectiveFactory {
	private static final String SCAN_DATA = "uk.ac.gda.client.ncd.edgescandata";
	private static final String EDGE_SELECTION = "uk.ac.gda.client.ncd.edgeselection";
	private static final String CALIBRATION = "uk.ac.gda.client.ncd.calibration";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		layout.addView(CALIBRATION, IPageLayout.BOTTOM, 0.5f, IPageLayout.ID_EDITOR_AREA);
		layout.addView(EDGE_SELECTION, IPageLayout.TOP, 0.5f, CALIBRATION);
		layout.addView(SCAN_DATA, IPageLayout.RIGHT, 0.3f, EDGE_SELECTION);
	}

}

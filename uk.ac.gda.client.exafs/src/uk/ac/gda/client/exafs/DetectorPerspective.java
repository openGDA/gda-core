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

package uk.ac.gda.client.exafs;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class DetectorPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		
		String editorArea=layout.getEditorArea();
		
		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.4f, editorArea);
		topLeft.addView("uk.ac.gda.client.exafs.stanford.amplifiers");
		topLeft.addView("uk.ac.gda.beamline.b18.mythencontrol");
		topLeft.addView("uk.ac.gda.devices.mythen.visualisation.views.MythenDataControlView");
		
		layout.addStandaloneView("uk.ac.gda.client.simplescanview", true, IPageLayout.BOTTOM, 0.5f, "topLeft");

		IFolderLayout topRight = layout.createFolder("topRight", IPageLayout.RIGHT, 0.6f, editorArea);
		topRight.addView("uk.ac.gda.exafs.ui.views.scalersmonitor");
		layout.addStandaloneView("gda.rcp.jythonterminalview", true, IPageLayout.BOTTOM, 0.5f, "topRight");
		
		
		layout.setEditorAreaVisible(false);

	}

}

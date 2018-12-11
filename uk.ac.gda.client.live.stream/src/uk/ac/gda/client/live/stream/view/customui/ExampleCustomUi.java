/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.view.customui;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class ExampleCustomUi extends AbstractLiveStreamViewCustomUi {

	@Override
	public void createUi(Composite composite) {
		// Draw a test widget
		Text text = new Text(composite, SWT.NONE);
		text.setText("My live stream custom UI");
		text.pack();

		interactWithThePlot();

		addAToolbarButton();
	}

	protected void interactWithThePlot() {
		// Example of interacting with the plot. Add a region to the plot
		try {
			IRegion box = getPlottingSystem().createRegion("Test Region", RegionType.BOX);

			// Set the size of the ROI
			IROI roi = new RectangularROI(50, 80, 45);
			// Set the position of the ROI
			roi.setPoint(100, 150);

			// Set the IROI on the IRegion (its a strange API..)
			box.setROI(roi);

			getPlottingSystem().addRegion(box);
		} catch (Exception e) {
			// Do something here in a non-example
		}
	}

	private void addAToolbarButton() {
		getActionBars().getToolBarManager().add(new Action() {
			@Override
			public void run() {
				System.out.println("My button was pressed!");
			}

			@Override
			public String getText() {
				return "Example Button";
			}
		});

		getActionBars().updateActionBars();
	}

}

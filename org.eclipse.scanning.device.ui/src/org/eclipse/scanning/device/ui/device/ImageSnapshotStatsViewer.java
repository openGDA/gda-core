/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package org.eclipse.scanning.device.ui.device;

import java.text.DecimalFormat;

import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Pulls relevant information from IDataset
 * pertaining to a detector snapshot as an image
 * and creates widgets for displaying that information
 */
public class ImageSnapshotStatsViewer implements SnapshotStatsViewer {

	private DecimalFormat oneDecimalPlace = new DecimalFormat("##########0.0#");
	private Text avgIntensity;
	private Text stdDeviation;
	private Text maxIntensity;
	private Text maxLocation;
	private Text minIntensity;
	private Text minLocation;
	private Text saturatedPixels;

	/**
	 * Draw widgets ready to display stats from a detector snapshot (image)
	 * @param parent
	 */
	public ImageSnapshotStatsViewer(Composite parent) {
		//Prepare a common layout
		GridDataFactory gd = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false);

		(new Label(parent, SWT.NONE)).setText("Average intensity");
		avgIntensity = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		gd.applyTo(avgIntensity);

		(new Label(parent, SWT.NONE)).setText("Standard deviation");
		stdDeviation = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		gd.applyTo(stdDeviation);

		(new Label(parent, SWT.NONE)).setText("Maximum intensity");
		maxIntensity = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		gd.applyTo(maxIntensity);

		(new Label(parent, SWT.NONE)).setText("Location");
		maxLocation = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		gd.applyTo(maxLocation);

		(new Label(parent, SWT.NONE)).setText("Minimum intensity");
		minIntensity = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		gd.applyTo(minIntensity);

		(new Label(parent, SWT.NONE)).setText("Location");
		minLocation = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		gd.applyTo(minLocation);

		(new Label(parent, SWT.NONE)).setText("Saturated pixels");
		saturatedPixels = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		saturatedPixels.setToolTipText("Number of pixels with intensities higher than 5 times the mean");
		gd.applyTo(saturatedPixels);
	}

	@Override
	public void update(IDataset dataset, double exposure) {

		SnapshotStatsCalculator stats = new SnapshotStatsCalculator();

		double mean = stats.calculateMean(dataset);
		avgIntensity.setText(oneDecimalPlace.format(mean));
		stdDeviation.setText(oneDecimalPlace.format(stats.calculateStdDev(dataset)));
		maxIntensity.setText(oneDecimalPlace.format(stats.findMaximumIntensity(dataset)));
		int[] maxLoc = stats.findMaximumPosition(dataset);
		maxLocation.setText(maxLoc[1] + ", " + maxLoc[0]);
		minIntensity.setText(oneDecimalPlace.format(stats.findMinimumIntensity(dataset)));
		int[] minLoc = stats.findMinimumPosition(dataset);
		minLocation.setText(minLoc[1] + ", " + minLoc[0]);
		saturatedPixels.setText(String.valueOf(stats.countBadPoints(dataset, 5*mean)));
	}

}

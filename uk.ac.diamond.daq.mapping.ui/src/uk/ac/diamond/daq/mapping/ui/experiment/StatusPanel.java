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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

class StatusPanel extends Composite {

	private Label statusLabel;
	private String message;
	private PathInfo pathInfo;
	private MappingExperimentView mappingExperimentView;

	StatusPanel(Composite parent, int style, MappingExperimentView mappingExperimentView) {
		super(parent, style);
		this.mappingExperimentView = mappingExperimentView;

		GridLayoutFactory.fillDefaults().applyTo(this);

		statusLabel = new Label(parent, SWT.NONE);
		statusLabel.setText(" \n "); // to make sure height is allocated correctly
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(statusLabel);

		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(separator);

		updateStatusLabel();
	}

	void updateStatusLabel() {
		String firstLine, secondLine;
		if (message != null && message.length() > 0) {
			firstLine = message;
			secondLine = "";
		} else {
			firstLine = "Map points: ";
			secondLine = "Smallest steps: ";
			if (pathInfo != null) {
				firstLine += pathInfo.getFormattedPointCount();
				if (pathInfo.pointCount > PathInfoCalculatorJob.MAX_POINTS_IN_ROI) {
					firstLine += String.format(" (Only displaying the first %,d points)",
							PathInfoCalculatorJob.MAX_POINTS_IN_ROI);
				}
				double totalTime = mappingExperimentView.getPointExposureTime() * pathInfo.pointCount;
				firstLine += String.format("    Total exposure time: %02.0f:%02.0f:%02.0f",
						Math.floor(totalTime / 3600.0),
						Math.floor((totalTime % 3600.0) / 60.0),
						totalTime % 60.0);

				secondLine += String.format("X = %s;  Y = %s;  Absolute = %s",
						pathInfo.getFormattedSmallestXStep(),
						pathInfo.getFormattedSmallestYStep(),
						pathInfo.getFormattedSmallestAbsStep());

			} else {
				firstLine += "unknown";
				secondLine += "unknown";
			}
		}
		String text = firstLine + "\n" + secondLine;
		statusLabel.setText(text);
		statusLabel.setToolTipText(text);
	}

	void setPathInfo(PathInfo pathInfo) {
		this.pathInfo = pathInfo;
		this.message = null;
		updateStatusLabel();
	}

	void setMessage(String message) {
		this.message = message;
		updateStatusLabel();
	}
}
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

package uk.ac.diamond.daq.mapping.ui.region;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;
import uk.ac.diamond.daq.mapping.ui.NumberAndUnitsComposite;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractRegionAndPathComposite;

public class RectangleRegionComposite extends AbstractRegionAndPathComposite {

	public RectangleRegionComposite(Composite parent, RectangularMappingRegion region) {
		super(parent, SWT.NONE);

		// X Start
		Label xStartLabel = new Label(this, SWT.NONE);
		xStartLabel.setText(getFastAxisName() + " Start");
		NumberAndUnitsComposite xStart = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(xStart);
		bindNumberUnits(xStart, "xStart", region);

		// X Stop
		Label xStopLabel = new Label(this, SWT.NONE);
		xStopLabel.setText(getFastAxisName() + " Stop");
		NumberAndUnitsComposite xStop = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(xStop);
		bindNumberUnits(xStop, "xStop", region);

		// Y Start
		Label yStartLabel = new Label(this, SWT.NONE);
		yStartLabel.setText(getSlowAxisName() + " Start");
		NumberAndUnitsComposite yStart = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(yStart);
		bindNumberUnits(yStart, "yStart", region);

		// Y Stop
		Label yStopLabel = new Label(this, SWT.NONE);
		yStopLabel.setText(getSlowAxisName() + " Stop");
		NumberAndUnitsComposite yStop = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(yStop);
		bindNumberUnits(yStop, "yStop", region);
	}

}

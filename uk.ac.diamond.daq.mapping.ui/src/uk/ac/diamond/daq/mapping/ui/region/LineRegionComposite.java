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

import uk.ac.diamond.daq.mapping.region.LineMappingRegion;
import uk.ac.diamond.daq.mapping.ui.NumberAndUnitsComposite;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractRegionAndPathComposite;

public class LineRegionComposite extends AbstractRegionAndPathComposite {

	public LineRegionComposite(Composite parent, LineMappingRegion region) {
		super(parent, SWT.NONE);

		(new Label(this, SWT.NONE)).setText("X Start:");
		NumberAndUnitsComposite xStart = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(xStart);
		bindNumberUnits(xStart, "xStart", region);

		(new Label(this, SWT.NONE)).setText("Y Start:");
		NumberAndUnitsComposite yStart = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(yStart);
		bindNumberUnits(yStart, "yStart", region);

		(new Label(this, SWT.NONE)).setText("X Stop:");
		NumberAndUnitsComposite xStop = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(xStop);
		bindNumberUnits(xStop, "xStop", region);

		(new Label(this, SWT.NONE)).setText("Y Stop:");
		NumberAndUnitsComposite yStop = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(yStop);
		bindNumberUnits(yStop, "yStop", region);
	}

}

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

import uk.ac.diamond.daq.mapping.region.CircularMappingRegion;
import uk.ac.diamond.daq.mapping.ui.NumberAndUnitsComposite;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractRegionAndPathComposite;

public class CircleRegionComposite extends AbstractRegionAndPathComposite {

	public CircleRegionComposite(Composite parent, CircularMappingRegion region) {
		super(parent, SWT.NONE);

		// X Centre
		Label xCentreLabel = new Label(this, SWT.NONE);
		xCentreLabel.setText("X Centre:");
		NumberAndUnitsComposite xCentre = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(xCentre);
		bindNumberUnits(xCentre, "xCentre", region);

		// Y Centre
		Label yCentreLabel = new Label(this, SWT.NONE);
		yCentreLabel.setText("Y Centre:");
		NumberAndUnitsComposite yCentre = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(yCentre);
		bindNumberUnits(yCentre, "yCentre", region);

		// Radius
		Label radiusLabel = new Label(this, SWT.NONE);
		radiusLabel.setText("Radius:");
		NumberAndUnitsComposite radius = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(radius);
		bindNumberUnits(radius, "radius", region);
	}

}

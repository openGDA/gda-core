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

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.mapping.region.CentredRectangleMappingRegion;
import uk.ac.diamond.daq.mapping.ui.NumberAndUnitsComposite;
import uk.ac.diamond.daq.mapping.ui.NumberUnitsWidgetProperty;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractRegionPathComposite;

public class CentredRectangleRegionComposite extends AbstractRegionPathComposite {

	public CentredRectangleRegionComposite(Composite parent, CentredRectangleMappingRegion region) {
		super(parent, SWT.NONE);

		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(this);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

		// X Start
		Label xStartLabel = new Label(this, SWT.NONE);
		xStartLabel.setText("X Centre:");
		NumberAndUnitsComposite xCentre = new NumberAndUnitsComposite(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(xCentre);

		final NumberUnitsWidgetProperty nuwp = new NumberUnitsWidgetProperty(); // Can be reused for all bindings
		IObservableValue xStartTarget = nuwp.observe(xCentre);
		IObservableValue xStartModel = BeanProperties.value("xCentre").observe(region);
		dbc.bindValue(xStartTarget, xStartModel);

		// X Stop
		Label xStopLabel = new Label(this, SWT.NONE);
		xStopLabel.setText("X Range:");
		NumberAndUnitsComposite xRange = new NumberAndUnitsComposite(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(xRange);

		IObservableValue xStopTarget = nuwp.observe(xRange);
		IObservableValue xStopModel = BeanProperties.value("xRange").observe(region);
		dbc.bindValue(xStopTarget, xStopModel);

		// Y Start
		Label yStartLabel = new Label(this, SWT.NONE);
		yStartLabel.setText("Y Centre:");
		NumberAndUnitsComposite yCentre = new NumberAndUnitsComposite(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(yCentre);

		IObservableValue yStartTarget = nuwp.observe(yCentre);
		IObservableValue yStartModel = BeanProperties.value("yCentre").observe(region);
		dbc.bindValue(yStartTarget, yStartModel);

		// Y Stop
		Label yStopLabel = new Label(this, SWT.NONE);
		yStopLabel.setText("Y Reange:");
		NumberAndUnitsComposite yRange = new NumberAndUnitsComposite(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(yRange);

		IObservableValue yStopTarget = nuwp.observe(yRange);
		IObservableValue yStopModel = BeanProperties.value("yRange").observe(region);
		dbc.bindValue(yStopTarget, yStopModel);
	}

}

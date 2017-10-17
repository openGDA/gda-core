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

package uk.ac.diamond.daq.mapping.ui.path;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.mapping.ui.NumberAndUnitsComposite;
import uk.ac.diamond.daq.mapping.ui.NumberUnitsWidgetProperty;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractRegionPathComposite;

public class RasterPathComposite extends AbstractRegionPathComposite {

	public RasterPathComposite(Composite parent, RasterModel path) {
		super(parent, SWT.NONE);

		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(this);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

		// X Step
		Label xStepLabel = new Label(this, SWT.NONE);
		xStepLabel.setText("X Step:");
		NumberAndUnitsComposite xStep = new NumberAndUnitsComposite(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(xStep);

		final NumberUnitsWidgetProperty nuwp = new NumberUnitsWidgetProperty(); // Can be reused for all bindings
		IObservableValue xStepTarget = nuwp.observe(xStep);
		IObservableValue xStepModel = BeanProperties.value("fastAxisStep").observe(path);
		dbc.bindValue(xStepTarget, xStepModel);

		// Y Step
		Label yStepLabel = new Label(this, SWT.NONE);
		yStepLabel.setText("Y Step:");
		NumberAndUnitsComposite yStep = new NumberAndUnitsComposite(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(yStep);

		IObservableValue yStepTarget = nuwp.observe(yStep);
		IObservableValue yStepModel = BeanProperties.value("slowAxisStep").observe(path);
		dbc.bindValue(yStepTarget, yStepModel);

		// Snake
		Label snakeLabel = new Label(this, SWT.NONE);
		snakeLabel.setText("Snake:");
		Button snake = new Button(this, SWT.CHECK);

		IObservableValue snakeTarget = WidgetProperties.selection().observe(snake);
		IObservableValue snakeModel = BeanProperties.value("snake").observe(path);
		dbc.bindValue(snakeTarget, snakeModel);
	}

}

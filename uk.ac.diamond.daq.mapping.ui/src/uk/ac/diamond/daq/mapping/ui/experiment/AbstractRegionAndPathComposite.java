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

package uk.ac.diamond.daq.mapping.ui.experiment;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.ui.NumberUnitsWidgetProperty;

public abstract class AbstractRegionAndPathComposite extends Composite {

	protected final DataBindingContext dbc = new DataBindingContext();
	protected final GridDataFactory gdControls = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);


	public AbstractRegionAndPathComposite(Composite parent, int style) {
		super(parent, style);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(this);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);
	}

	protected void bindTextBox(Text widget, String modelProperty, IScanPathModel path) {
		IObservableValue target = WidgetProperties.text(SWT.Modify).observe(widget);
		IObservableValue model = BeanProperties.value(modelProperty).observe(path);
		dbc.bindValue(target, model);
	}

	protected void bindSelection(Widget widget, String modelProperty, IScanPathModel path)  {
		IObservableValue target = WidgetProperties.selection().observe(widget);
		IObservableValue model = BeanProperties.value(modelProperty).observe(path);
		dbc.bindValue(target, model);
	}

	protected void bindNumberUnits(Widget widget, String modelProperty, IMappingScanRegionShape region) {
		IObservableValue target = (new NumberUnitsWidgetProperty()).observe(widget);
		IObservableValue model = BeanProperties.value(modelProperty).observe(region);
		dbc.bindValue(target, model);
	}

}
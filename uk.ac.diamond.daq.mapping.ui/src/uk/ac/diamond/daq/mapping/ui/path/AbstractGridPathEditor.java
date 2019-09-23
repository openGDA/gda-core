/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scanning.api.points.models.AbstractGridModel;
import org.eclipse.scanning.api.points.models.AbstractGridModel.Orientation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Abstract superclass for grid-like paths (subclasses of {@link AbstractGridModel}),
 *
 */
public class AbstractGridPathEditor extends AbstractPathEditor {

	/**
	 * Creates controls for options common to grid paths:<ul>
	 * <li>alternating - whether subsequent scans change direction in the innermost axis;</li>
	 * <li>continuous - whether to scan the innermost axis continuously (for malcolm scans only);</li>
	 * <li>orientation - whether to treat the 2nd axis as the fast axis</li>
	 *
	 * @param parent composite to draw the controls on
	 */

	protected void makeCommonGridOptionsControls(Composite parent) {
		makeCommonOptionsControls(parent);
		makeOrientationControl(parent);
	}

	/**
	 * If the path edited by this editor can have vertical or horizontal orientation, this method will draw the
	 * control to toggle this property.
	 * @param parent composite to draw control on
	 */
	@SuppressWarnings("unchecked")
	private void makeOrientationControl(Composite parent) {
		Label orientationLabel = new Label(parent, SWT.NONE);
		orientationLabel.setText("Orientation");
		ComboViewer oriantationSelector = new ComboViewer(parent);
		oriantationSelector.add(Orientation.HORIZONTAL);
		oriantationSelector.add(Orientation.VERTICAL);
		oriantationSelector.setSelection(new StructuredSelection(Orientation.HORIZONTAL));

		IViewerObservableValue inWidget = ViewerProperties.singleSelection().observe(oriantationSelector);
		IObservableValue<String> inModel = BeanProperties.value("orientation").observe(getModel());
		binder.bind(inWidget, inModel);
	}

}

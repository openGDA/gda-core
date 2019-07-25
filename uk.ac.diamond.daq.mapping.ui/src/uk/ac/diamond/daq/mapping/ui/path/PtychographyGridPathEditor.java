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

package uk.ac.diamond.daq.mapping.ui.path;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.points.models.AbstractOverlapModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PtychographyGridPathEditor extends AbstractPathEditor {

	private static final Logger logger = LoggerFactory.getLogger(PtychographyGridPathEditor.class);

	@Override
	public Composite createEditorPart(Composite parent) {

		final Composite composite = super.createEditorPart(parent);

		new Label(composite, SWT.NONE).setText("Beam overlap");

		Composite overlapControls = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(overlapControls);
		GridLayoutFactory.fillDefaults().spacing(0, 1).applyTo(overlapControls);

		Text overlapText = new Text(overlapControls, SWT.BORDER);
		grabHorizontalSpace.applyTo(overlapText);
		binder.bind(overlapText, "overlap", getModel());

		Slider overlapSlider = new Slider(overlapControls, SWT.HORIZONTAL);
		overlapSlider.setMaximum(100); // not inclusive
		overlapSlider.setMinimum(0);
		overlapSlider.setIncrement(5);
		overlapSlider.setSelection(50);

		overlapSlider.addListener(SWT.Selection, event -> overlapText.setText(Double.toString(overlapSlider.getSelection() / 100.0)));
		overlapText.addListener(SWT.Modify, event -> overlapSlider.setSelection((int) (Double.parseDouble(overlapText.getText()) * 100)));

		grabHorizontalSpace.applyTo(overlapSlider);

		try {
			Object[] beamDimensions = getBeamDimensions();
			AbstractOverlapModel overlapModel = (AbstractOverlapModel) getModel();
			overlapModel.setBeamSize(beamDimensions);
			overlapText.setText(String.valueOf(overlapModel.getOverlap()));

			String beamDimensionsHint = "Beam dimensions = " + beamDimensions[0] + " x " + beamDimensions[1];
			composite.setToolTipText(beamDimensionsHint);

			// Show 'invalid' icon on text if overlap is outside valid range
			ControlDecorationSupport.create(new MultiValidator() {

				IObservableValue observable = binder.getObservableValue(overlapText);

				@Override
				protected IStatus validate() {
					double range = Double.parseDouble((String) observable.getValue());
					return range >= 0 && range < 1 ? ValidationStatus.ok() :
						ValidationStatus.error("0 <= overlap < 1");
				}
			}, SWT.LEFT);

		} catch (ScanningException e) {
			logger.error("Error getting beam dimensions", e);
			overlapText.setEnabled(false);
			overlapSlider.setEnabled(false);
			skipACell(composite);
			Label errorLabel = new Label(composite, SWT.NONE);
			errorLabel.setText("Beam dimensions not set!");
			errorLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
			showErrorDialog(composite.getShell());
		}

		new Label(composite, SWT.NONE).setText("Random offset");
		Text offsetText = new Text(composite, SWT.BORDER);
		offsetText.setToolTipText("% of step size");
		grabHorizontalSpace.applyTo(offsetText);
		binder.bind(offsetText, "randomOffset", getModel());

		makeContinuousControl(composite);

		return composite;
	}

	/**
	 * This method adds a blank label to a composite in order to skip a cell in a GridLayout
	 * @param composite
	 */
	@SuppressWarnings("unused")
	private void skipACell(Composite composite) {
		new Label(composite, SWT.NONE);
	}

	private int showErrorDialog(Shell shell) {
		MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		dialog.setText("Beam dimensions configuration not found");

		String message = "In order to specify scan paths in terms of beam overlap,\n";
		message += "GDA must have a BeamDimensions configuration.\n\n";
		message += "Please contact GDA support.";

		dialog.setMessage(message);
		return dialog.open();
	}

}

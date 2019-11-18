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

import org.eclipse.core.databinding.Binding;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.ui.experiment.AbstractModelEditor;

/**
 * Parent class for all path editors used in RegionAndPathSection.
 */
public abstract class AbstractPathEditor extends AbstractModelEditor<IScanPathModel> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractPathEditor.class);
	private Button continuous;
	private Label continuousLabel;
	private Binding continuousBinding;

	/**
	 * Creates controls for options common to all paths:<ul>
	 * <li>alternating - whether subsequent scans change direction in the innermost axis - only effectively valid with an outer scannable;</li>
	 * <li>continuous - whether to scan the innermost axis continuously (for malcolm scans only);</li>
	 *
	 * @param parent composite to draw the controls on
	 */
	protected void makeCommonOptionsControls(Composite parent) {
		makeAlternatingControl(parent);
		makeContinuousControl(parent);
	}

	/**
	 * If the path edited by this editor can be continuous (Malcolm-driven), this method will draw the controls for consistency.
	 * @param parent composite to draw control on
	 */
	private void makeContinuousControl(Composite parent) {
		continuousLabel = new Label(parent, SWT.NONE);
		continuousLabel.setText("Continuous");
		continuous = new Button(parent, SWT.CHECK);
		continuousBinding = binder.bind(continuous, "continuous", getModel());
	}

	/**
	 * If the path edited by this editor is alternating, this method will draw the controls for consistency.
	 * @param parent composite to draw control on
	 */
	private void makeAlternatingControl(Composite parent) {
		Label alternatingLabel = new Label(parent, SWT.NONE);
		alternatingLabel.setText("Alternating");
		Button alternatingButton = new Button(parent, SWT.CHECK);
		binder.bind(alternatingButton, "alternating", getModel());
	}

	/**
	 * Triggered when a device selection is changed - Only Malcolm devices can drive continuous scans
	 * @param enabled true if device is a Malcolm device, otherwise false
	 */
	public void setContinuousEnabled(boolean enabled) {
		if (continuousLabel != null) {
			continuousLabel.setEnabled(enabled);
			continuous.setEnabled(enabled);

			if (enabled) {
				// control is enabled: update it from model
				continuousBinding.updateModelToTarget();
			} else {

				/* Control is disabled: uncheck button but don't update model
				 *
				 * Q: Why?
				 * A: We want to cache the continuous state for the next time
				 *    the control becomes enabled
				 */
				continuous.setSelection(false);
			}
		}
	}

	protected Object[] getBeamDimensions() throws ScanningException {
		try {
			return (Object[]) getScannableDeviceService().getScannable("beamDimensions").getPosition();
		} catch (EventException e) {
			logger.error("Could not get scannable device service", e);
			throw new ScanningException("Could not get scannable device service", e);
		}
	}

}

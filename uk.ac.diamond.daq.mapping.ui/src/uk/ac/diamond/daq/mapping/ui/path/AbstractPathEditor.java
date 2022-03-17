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

import static java.util.Collections.unmodifiableSet;

import java.util.EnumSet;
import java.util.Set;

import org.eclipse.core.databinding.Binding;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.points.models.IBoundsToFit;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.ui.experiment.AbstractRegionPathModelEditor;

/**
 * Parent class for all path editors used in RegionAndPathSection.
 */
public abstract class AbstractPathEditor extends AbstractRegionPathModelEditor<IScanPathModel> {

	/**
	 * An option for which controls can be created. Subclasses can
	 * Note: this pattern emulates extensible enums.
	 */
	public interface PathOption { }

	/**
	 * Common options that this class can add controls for.
	 */
	public enum CommonPathOption implements PathOption {
		ALTERNATING, CONTINUOUS, BOUNDING_FIT_CAPABLE
	}

	private static final Logger logger = LoggerFactory.getLogger(AbstractPathEditor.class);

	private static final Set<PathOption> ALL_COMMON_OPTIONS = unmodifiableSet(EnumSet.allOf(CommonPathOption.class));

	private Set<PathOption> optionsToDisplay = ALL_COMMON_OPTIONS;
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
		if (shouldDisplayOption(CommonPathOption.ALTERNATING)) {
			makeAlternatingControl(parent);
		}
		if (shouldDisplayOption(CommonPathOption.CONTINUOUS)) {
			makeContinuousControl(parent);
		}
		if (shouldDisplayOption(CommonPathOption.BOUNDING_FIT_CAPABLE)) {
			makeBoundsFitControl(parent);
		}
	}

	protected boolean shouldDisplayOption(PathOption option) {
		return optionsToDisplay.contains(option) &&
			(option != CommonPathOption.BOUNDING_FIT_CAPABLE || isBoundingFitCapable());
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

	private void makeBoundsFitControl(Composite parent) {
		Label boundsFitLabel = new Label(parent, SWT.NONE);
		boundsFitLabel.setText("Half-step buffer");
		Button boundsButton = new Button(parent, SWT.CHECK);
		boundsButton.setToolTipText("Should the model step in half a step from all edges, to allow continuous motion to never leave the region?");
		Binding boundsBind = binder.bind(boundsButton, "boundsToFit", getModel());
		boundsButton.setSelection(Boolean.getBoolean("gda.mapping.boundsToFit"));
		// Default could be True or False
		boundsBind.updateTargetToModel();
	}

	private boolean isBoundingFitCapable() {
		if (getModel() instanceof IBoundsToFit) {
			return ((IBoundsToFit) getModel()).isBoundsToFit();
		}
		return false;
	}

	public Set<PathOption> getOptionsToDisplay() {
		return optionsToDisplay;
	}

	public void setOptionsToDisplay(Set<PathOption> optionsToDisplay) {
		this.optionsToDisplay = optionsToDisplay;
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

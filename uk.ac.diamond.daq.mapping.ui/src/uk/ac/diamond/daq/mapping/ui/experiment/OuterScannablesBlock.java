/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static uk.ac.gda.ui.tool.ClientMessages.OUTER_SCANNABLES_CONFIGURE_TP;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.ScannableMotion;
import gda.observable.IObserver;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.impl.MappingScanDefinition;
import uk.ac.diamond.daq.mapping.impl.ScanPathModelWrapper;
import uk.ac.diamond.daq.mapping.ui.Activator;

/**
 * A section for configuring the outer scannables of a scan, e.g. temperature.
 * <p>
 * When only a subset of the configured scannables could be used as the outer axis in a scan,
 * they should be given as the {@code permittedOuterScannables} in the {@link MappingScanDefinition}.
 * <br>
 * If this list is not configured, then the user will be able to display and
 * configure any available scannable that implements {@link ScannableMotion} or a derived interface.
 */
public class OuterScannablesBlock {

	@FunctionalInterface
	public interface ScannablesChangedListener {

		/**
		 * Called when the list of scannables models has changed, i.e. models have been changed or removed.
		 * Note that this method is not called when the state a model in the set of scannables changed.
		 * Also note that this list is not live.
		 * @param scannables new list of {@link IScanModelWrapper}s for the outer scannables
		 */
		void scannablesChanged(List<IScanModelWrapper<IAxialModel>> scannables);
	}

	@FunctionalInterface
	public interface PointsChangedListener {
		void pointsChanged();
	}

	private static final Logger logger = LoggerFactory.getLogger(OuterScannablesBlock.class);

	private DataBindingContext dataBindingContext;

	/**
	 * Overall composite for section
	 */
	private Composite sectionComposite;

	/**
	 * Composite to hold list of scannables<br>
	 * The user can choose which scannables to show in this list: see comment on {@link #outerScannables}.
	 */
	private Composite scannablesComposite;

	/**
	 * Widgets (one for each scannable) to allow entry & editing of the scan path (start/stop/step values)
	 */
	private Set<ScanPathEditor> scanPathEditors = new HashSet<>();

	/**
	 * Observer for events from {@link #scanPathEditors}
	 */
	private final IObserver scanPathObserver = this::handleScanPathUpdate;

	/**
	 * The outer scannables shown.
	 */
	private List<IScanModelWrapper<IAxialModel>> outerScannables = new ArrayList<>();

	/**
	 * Names of the scannables that the user can choose
	 */
	private Set<String> availableScannableNames = null;

	private ScannablesChangedListener scannablesChangedListener = null;

	private PointsChangedListener pointsChangeListener = null;

	/**
	 * Set a listener to the scannables being added and removed from this block. This can be used to relayout
	 * the containing view as the space required by the block may have changed.
	 * @param scannablesChangedListener
	 */
	public void setScannablesChangeListener(ScannablesChangedListener scannablesChangedListener) {
		this.scannablesChangedListener = scannablesChangedListener;
	}

	/**
	 * Set a listener to the points being made. This can be used to update any feedback about the number of points
	 * in the scan, estimated duration, etc.
	 * @param pointsChangedListener
	 */
	public void setPointsChangedListener(PointsChangedListener pointsChangedListener) {
		this.pointsChangeListener = pointsChangedListener;
	}

	/**
	 * Sets the outer scananbles. Note this list is copied rather than updated, so
	 * clients should add a {@link ScannablesChangedListener} and call
	 * @param outerScannables
	 */
	public void setOuterScannables(List<IScanModelWrapper<IAxialModel>> outerScannables) {
		this.outerScannables = new ArrayList<>(outerScannables);
	}

	public void setAvailableScannableNames(Set<String> availableScannableNames) {
		this.availableScannableNames = availableScannableNames;
	}

	public List<IScanModelWrapper<IAxialModel>> getOuterScannables() {
		return this.outerScannables;
	}

	public void createControls(Composite parent) {
		sectionComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sectionComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(sectionComposite);

		final Label otherScanAxesLabel = new Label(sectionComposite, SWT.NONE);
		otherScanAxesLabel.setText("Other Scan Axes");
		GridDataFactory.fillDefaults().applyTo(otherScanAxesLabel);

		// button to add a new scannable
		final Button addScannablesButton = new Button(sectionComposite, SWT.PUSH);
		addScannablesButton.setImage(Activator.getImage("icons/plus.png"));
		addScannablesButton.setToolTipText(getMessage(OUTER_SCANNABLES_CONFIGURE_TP));
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(addScannablesButton);
		addScannablesButton.addListener(SWT.Selection, event -> addScannables());

		createScannableControls();
	}

	/**
	 * Create a control (checkbox + scan path specification) for each scannable chosen to be displayed
	 */
	private void createScannableControls() {
		removeOldBindings();
		disposeScanPathEditors();

		// Ensure scannables are shown in alphabetical order (case insensitive)
		outerScannables.sort(comparing(IScanModelWrapper::getName, CASE_INSENSITIVE_ORDER));

		if (scannablesComposite != null) {
			scannablesComposite.dispose();
		}
		dataBindingContext = new DataBindingContext();
		final Map<String, Binding> checkBoxBindings = new HashMap<>();

		scannablesComposite = new Composite(sectionComposite, SWT.NONE);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(scannablesComposite);
		GridLayoutFactory.swtDefaults().numColumns(4).margins(0, 0).applyTo(scannablesComposite);

		// Create a control for each scannable to be shown
		for (IScanModelWrapper<IAxialModel> scannableAxisParameters : outerScannables) {
			final String scannableName = scannableAxisParameters.getName();

			// Create checkbox and bind to "includeInScan" in the model
			final Button checkBox = new Button(scannablesComposite, SWT.CHECK);
			checkBox.setText(scannableName);
			final IObservableValue<Boolean> checkBoxValue = WidgetProperties.buttonSelection().observe(checkBox);
			final IObservableValue<Boolean> activeValue = PojoProperties.value("includeInScan", Boolean.class).observe(scannableAxisParameters);
			final Binding checkBoxBinding = dataBindingContext.bindValue(checkBoxValue, activeValue);
			checkBoxBindings.put(scannableName, checkBoxBinding);

			// Create control to display/edit scan path definition
			final ScanPathEditor scanPathEditor = new ScanPathEditor(scannablesComposite, SWT.NONE, scannableAxisParameters);
			GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(scanPathEditor);
			scanPathEditor.addIObserver(scanPathObserver);
			scanPathEditors.add(scanPathEditor);

			// Button to delete scannable
			final Button deleteScannableButton = new Button(scannablesComposite, SWT.NONE);
			deleteScannableButton.setImage(Activator.getImage("icons/cross.png"));
			deleteScannableButton.setToolTipText("Delete scannable");
			deleteScannableButton.addListener(SWT.Selection, event -> deleteScannable(scannableAxisParameters));

			// when the include in scan checkbox is changed we need to revalidate the model
			// as this determines the severity of the validation status. We also call for the
			// scan path to be recalculated in case any change to the preview is needed
			checkBoxBinding.getModel().addChangeListener(evt -> {
				scanPathEditor.revalidate();
				updatePoints();
			});

			scanPathEditor.revalidate();
		}

		if (scannablesChangedListener != null) {
			scannablesChangedListener.scannablesChanged(outerScannables);
		}
	}

	private void disposeScanPathEditors() {
		for (ScanPathEditor scanPathEditor : scanPathEditors) {
			scanPathEditor.dispose();
		}
		scanPathEditors.clear();
	}

	@SuppressWarnings("unused")
	private void handleScanPathUpdate(Object source, Object arg) {
		updatePoints();
	}

	private void updatePoints() {
		if (pointsChangeListener != null) {
			pointsChangeListener.pointsChanged();
		}
	}

	private void deleteScannable(IScanModelWrapper<IAxialModel> scannable) {
		if (MessageDialog.openQuestion(getShell(), "Confirm deletion", String.format("Do you want to delete %s?", scannable.getName()))) {
			outerScannables.remove(scannable);
			updateControls();
		}
	}

	private void addScannables() {
		// Get the scannables to show in the dialog: exclude the scannables that are already shown.
		final Set<String> scannablesShownNames = outerScannables.stream()
				.map(IScanModelWrapper::getName)
				.collect(toSet());

		final List<String> scannablesToChooseNames = availableScannableNames.stream()
				.filter(scannable -> !scannablesShownNames.contains(scannable))
				.collect(toList());

		if (scannablesToChooseNames.isEmpty()) {
			MessageDialog.openError(getShell(), "No scannables to add",	"There are no more scannables available to add");
			return;
		}

		final String titleText = "Select scannable(s) to add";
		final String headerText = "Select one or more scannables";
		final MultiSelectDialog dialog = new MultiSelectDialog(getShell(), titleText, headerText, scannablesToChooseNames);
		if (dialog.open() == Window.OK) {
			final List<String> scannablesNamesToAdd = dialog.getSelected();
			for (String scannableName : scannablesNamesToAdd) {
				addScannableInternal(scannableName, false);
			}
			updateControls();
		}
	}

	public void updateControls() {
		createScannableControls();
		updatePoints();
	}

	public void dispose() {
		disposeScanPathEditors();
	}

	/**
	 * Get the wrapper for the named scannable.
	 * <p>
	 * If the scannable is not currently shown in this section
	 *
	 * @param scannableName
	 *            name of the scannable
	 * @return optional wrapper for the scannable
	 */
	private Optional<IScanModelWrapper<IAxialModel>> getScannableWrapper(String scannableName) {
		return outerScannables.stream()
				.filter(item -> item.getName().equals(scannableName))
				.findFirst();
	}

	/**
	 * Make a scannable visible in this section (if it is not already shown) and set the flag to say whether it should
	 * be included in a scan
	 *
	 * @param scannableName name of the scannable
	 * @param includeInScan <code>true</code> if the scannable is to be included in scans, <code>false</code> otherwise
	 */
	public void addScannable(String scannableName, boolean includeInScan) {
		addScannableInternal(scannableName, includeInScan);

	}

	/**
	 * Adds a scannable without updating the UI or notifying listeners.
	 * @param scannableName
	 * @param includeInScan
	 */
	private void addScannableInternal(String scannableName, boolean includeInScan) {
		final Optional<IScanModelWrapper<IAxialModel>> optWrapper = getScannableWrapper(scannableName);
		if (optWrapper.isPresent()) {
			optWrapper.get().setIncludeInScan(includeInScan);
			return;
		}

		if (availableScannableNames.contains(scannableName)) {
			outerScannables.add(new ScanPathModelWrapper<>(scannableName, null, includeInScan));
		} else {
			final String message = String.format("Cannot add %s as outer scannable: not one of the permitted scannables", scannableName);
			final Status status = new Status(IStatus.WARNING, "uk.ac.diamond.daq.mapping.ui", "Scannable configuration");
			ErrorDialog.openError(getShell(), "Configuration error", message, status);
			logger.warn(message);
		}
	}

	private Shell getShell() {
		return sectionComposite.getShell();
	}

	private void removeOldBindings() {
		if (dataBindingContext == null) {
			return;
		}

		// copy the bindings to prevent concurrent modification exception
		final List<Binding> bindings = new ArrayList<>(dataBindingContext.getBindings());
		for (Binding binding : bindings) {
			dataBindingContext.removeBinding(binding);
			binding.dispose();
		}
	}

}

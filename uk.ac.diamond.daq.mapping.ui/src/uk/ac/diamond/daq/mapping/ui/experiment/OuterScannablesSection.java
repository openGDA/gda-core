/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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
import static uk.ac.gda.ui.tool.ClientMessages.OUTER_SCANNABLES_CONFIGURE_TP;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.ScannableMotion;
import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.impl.MappingScanDefinition;
import uk.ac.diamond.daq.mapping.impl.ScanPathModelWrapper;

/**
 * A section for configuring the outer scannables of a scan, e.g. temperature.
 * <p>
 * When only a subset of the configured scannables could be used as the outer axis in a scan,
 * they should be given as the {@code permittedOuterScannables} in the {@link MappingScanDefinition}.
 * <br>
 * If this list is not configured, then the user will be able to display and
 * configure any available scannable that implements {@link ScannableMotion} or a derived interface.
 */
public class OuterScannablesSection extends AbstractMappingSection {

	private static final Logger logger = LoggerFactory.getLogger(OuterScannablesSection.class);

	/**
	 * Overall composite for section
	 */
	private Composite sectionComposite;

	/**
	 * Composite to hold list of scannables<br>
	 * The user can choose which scannables to show in this list: see comment on {@link #scannablesToShow}.
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
	 * The outer scannables to show in the view.<br>
	 * Initially empty, the user can add and remove scannables to/from the list.<br>
	 * The choice will be saved when the client is closed and restored when opened, unless the client is reset.
	 */
	private List<IScanModelWrapper<IScanPointGeneratorModel>> scannablesToShow;

	/**
	 * Names of the scannables that the user can choose
	 * <p>
	 * This will be either:<br>
	 * <li>the {@code permittedOuterScannables} configured</li>
	 * or, if no default scannables are configured:
	 * <li>all {@link ScannableMotion} devices configured for the beamline</li>
	 */
	private Set<String> availableScannables;

	@Override
	public void initialize(MappingExperimentView mappingView) {
		super.initialize(mappingView);

		availableScannables = new HashSet<>(getMappingBean().getScanDefinition().getPermittedOuterScannables());
		if (availableScannables.isEmpty()) {
			try {
				availableScannables.addAll(Finder.getFindablesOfType(ScannableMotion.class).keySet());
			} catch (Exception e) {
				logger.error("Exception getting list of scannables", e);
			}
		} else {
			// Ensure that the default scannable(s) are in the list, even if not explicitly set as "permitted"
			availableScannables.addAll(getMappingBean().getScanDefinition().getDefaultOuterScannables());
		}
	}

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		sectionComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sectionComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(sectionComposite);

		final Label otherScanAxesLabel = new Label(sectionComposite, SWT.NONE);
		otherScanAxesLabel.setText("Other Scan Axes");
		GridDataFactory.fillDefaults().applyTo(otherScanAxesLabel);

		// button to add a new scannable
		final Button btnAdd = new Button(sectionComposite, SWT.PUSH);
		btnAdd.setImage(getImage("icons/plus.png"));
		btnAdd.setToolTipText(getMessage(OUTER_SCANNABLES_CONFIGURE_TP));
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(btnAdd);
		btnAdd.addListener(SWT.Selection, event -> addScannables());

		createScannableControls();
	}

	/**
	 * Create a control (checkbox + scan path specification) for each scannable chosen to be displayed
	 */
	private void createScannableControls() {
		removeOldBindings();
		disposeScanPathEditors();

		scannablesToShow = new ArrayList<>(getMappingBean().getScanDefinition().getOuterScannables());

		// Ensure scannables are shown in alphabetical order (case insensitive)
		scannablesToShow.sort(comparing(IScanModelWrapper<IScanPointGeneratorModel>::getName, CASE_INSENSITIVE_ORDER));

		if (scannablesComposite != null) {
			scannablesComposite.dispose();
		}
		dataBindingContext = new DataBindingContext();
		final Map<String, Binding> checkBoxBindings = new HashMap<>();

		scannablesComposite = new Composite(sectionComposite, SWT.NONE);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(scannablesComposite);
		GridLayoutFactory.swtDefaults().numColumns(4).margins(0, 0).applyTo(scannablesComposite);

		// Create a control for each scannable to be shown
		for (IScanModelWrapper<IScanPointGeneratorModel> scannableAxisParameters : scannablesToShow) {
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
			deleteScannableButton.setImage(getImage("icons/cross.png"));
			deleteScannableButton.setToolTipText("Delete scannable");
			deleteScannableButton.addListener(SWT.Selection, event -> deleteScannable(scannableAxisParameters));

			// when the include in scan checkbox is changed we need to revalidate the model
			// as this determines the severity of the validation status. We also call for the
			// scan path to be recalculated in case any change to the preview is needed
			checkBoxBinding.getModel().addChangeListener(evt -> {
				scanPathEditor.revalidate();
				updatePoints();
			});
		}

		// Keep the mapping bean in sync with the GUI
		getMappingBean().getScanDefinition().setOuterScannables(scannablesToShow);
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
		getService(RegionAndPathController.class).updatePoints();
	}

	private void deleteScannable(IScanModelWrapper<IScanPointGeneratorModel> scannable) {
		if (MessageDialog.openQuestion(getShell(), "Confirm deletion", String.format("Do you want to delete %s?", scannable.getName()))) {
			scannablesToShow.remove(scannable);
			updateControls();
		}
	}

	private void addScannables() {
		// Get the scannables to show in the dialog: exclude the scannables that are already shown.
		final List<String> scannablesShown = extractScannableNames(scannablesToShow);
		final List<String> scannablesToChoose = availableScannables.stream()
				.filter(scannable -> !scannablesShown.contains(scannable))
				.collect(Collectors.toList());

		if (scannablesToChoose.isEmpty()) {
			MessageDialog.openError(getShell(), "No scannables to add",	"There are no more scannables available to add");
			return;
		}

		final String titleText = "Select scannable(s) to add";
		final String headerText = "Select one or more scannables";
		final MultiSelectDialog dialog = new MultiSelectDialog(getShell(), titleText, headerText, scannablesToChoose);
		if (dialog.open() == Window.OK) {
			final List<String> scannablesSelected = dialog.getSelected();
			if (!scannablesSelected.isEmpty()) {
				showScannables(scannablesSelected, false);
				updateControls();
			}
		}
	}

	private static List<String> extractScannableNames(Collection<IScanModelWrapper<IScanPointGeneratorModel>> scannables) {
		return scannables.stream()
				.map(IScanModelWrapper<IScanPointGeneratorModel>::getName)
				.collect(Collectors.toList());
	}

	@Override
	public void updateControls() {
		createScannableControls();
		relayoutMappingView();
		updateStatusLabel();
	}

	@Override
	public void dispose() {
		disposeScanPathEditors();
		super.dispose();
	}

	/**
	 * Get the wrapper for the named scannable.
	 * <p>
	 * If the scannable is not currently shown in this section, this will return <code>null</code>
	 *
	 * @param scannableName
	 *            name of the scannable
	 * @return wrapper for the scannable or <code>null</code>
	 */
	private IScanModelWrapper<IScanPointGeneratorModel> getScannableWrapper(String scannableName) {
		return scannablesToShow.stream()
				.filter(item -> item.getName().equals(scannableName))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Make a scannable visible in this section (if it is not already shown) and set the flag to say whether it should
	 * be included in a scan
	 *
	 * @param scannableName
	 *            name of the scannable
	 * @param includeInScan
	 *            <code>true</code> if the scannable is to be included in scans, <code>false</code> otherwise
	 */
	public void showScannable(String scannableName, boolean includeInScan) {
		final IScanModelWrapper<IScanPointGeneratorModel> wrapper = getScannableWrapper(scannableName);
		if (wrapper != null) {
			wrapper.setIncludeInScan(includeInScan);
			return;
		}

		if (availableScannables.contains(scannableName)) {
			scannablesToShow.add(new ScanPathModelWrapper(scannableName, null, includeInScan));
		} else {
			final String message = String.format("Cannot add %s as outer scannable: not one of the permitted scannables", scannableName);
			final Status status = new Status(IStatus.WARNING, "uk.ac.diamond.daq.mapping.ui", "Scannable configuration");
			ErrorDialog.openError(getShell(), "Configuration error", message, status);
			logger.warn(message);
		}
	}

	/**
	 * Make a collection of scannables visible in this section.
	 * <p>
	 * See {@link #showScannable(String, boolean)}
	 *
	 * @param scannableNames
	 *            names of the scannable
	 * @param includeInScan
	 *            <code>true</code> if the scannables are to be included in scans, <code>false</code> otherwise
	 */
	public void showScannables(Collection<String> scannableNames, boolean includeInScan) {
		for (String scannableName : scannableNames) {
			showScannable(scannableName, includeInScan);
		}
	}
}

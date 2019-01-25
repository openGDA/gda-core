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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.scanning.api.points.models.IScanPathModel;
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
 * they should be given as the {@code defaultOuterScannables} in the {@link MappingScanDefinition}.
 * <br>
 * If this list is not configured, then the user will be able to display and
 * configure any available scannable that implements {@link ScannableMotion} or a derived interface.
 */
class OuterScannablesSection extends AbstractMappingSection {

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
	private List<IScanModelWrapper<IScanPathModel>> scannablesToShow;

	/**
	 * Names of the scannables that the user can choose
	 * <p>
	 * This list will be either:<br>
	 * <li>the {@code defaultOuterScannables} configured</li>
	 * or, if no default scannables are configured:
	 * <li>all {@link ScannableMotion} configured for the beamline</li>
	 */
	private List<String> availableScannables;

	@Override
	public void initialize(MappingExperimentView mappingView) {
		super.initialize(mappingView);
		List<String> defaultScannables = getMappingBean().getScanDefinition().getDefaultOuterScannables();
		if (defaultScannables == null || defaultScannables.isEmpty()) {
			try {
				availableScannables = new ArrayList<>(Finder.getInstance().getFindablesOfType(ScannableMotion.class).keySet());
			} catch (Exception e) {
				logger.error("Exception getting list of scannables", e);
				availableScannables = Collections.emptyList();
			}
		} else {
			availableScannables = new ArrayList<>(defaultScannables);
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
		btnAdd.setImage(MappingExperimentUtils.getImage("icons/plus.png"));
		btnAdd.setToolTipText("Add scannables");
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
		scannablesToShow.sort(comparing(IScanModelWrapper<IScanPathModel>::getName, CASE_INSENSITIVE_ORDER));

		if (scannablesComposite != null) {
			scannablesComposite.dispose();
		}
		dataBindingContext = new DataBindingContext();
		final Map<String, Binding> checkBoxBindings = new HashMap<>();

		scannablesComposite = new Composite(sectionComposite, SWT.NONE);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(scannablesComposite);
		GridLayoutFactory.swtDefaults().numColumns(4).margins(0, 0).applyTo(scannablesComposite);

		// Create a control for each scannable to be shown
		for (IScanModelWrapper<IScanPathModel> scannableAxisParameters : scannablesToShow) {
			final String scannableName = scannableAxisParameters.getName();

			// Create checkbox and bind to "includeInScan" in the model
			final Button checkBox = new Button(scannablesComposite, SWT.CHECK);
			checkBox.setText(scannableName);
			final IObservableValue<?> checkBoxValue = WidgetProperties.selection().observe(checkBox);
			@SuppressWarnings("unchecked")
			final IObservableValue<?> activeValue = PojoProperties.value("includeInScan").observe(scannableAxisParameters);
			final Binding checkBoxBinding = dataBindingContext.bindValue(checkBoxValue, activeValue);
			checkBoxBindings.put(scannableName, checkBoxBinding);

			// Create control to display/edit scan path definition
			final ScanPathEditor scanPathEditor = new ScanPathEditor(scannablesComposite, SWT.NONE, scannableAxisParameters);
			GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(scanPathEditor);
			scanPathEditor.addIObserver(scanPathObserver);
			scanPathEditors.add(scanPathEditor);

			// Button to delete scannable
			final Button deleteScannableButton = new Button(scannablesComposite, SWT.NONE);
			deleteScannableButton.setImage(MappingExperimentUtils.getImage("icons/cross.png"));
			deleteScannableButton.setToolTipText("Delete scannable");
			deleteScannableButton.addListener(SWT.Selection, event -> deleteScannable(scannableAxisParameters));

			// when the include in scan checkbox is changed we need to revalidate the model
			// as this determines the severity of the validation status.
			checkBoxBinding.getModel().addChangeListener(evt -> {
				scanPathEditor.revalidate();
				updateStatusLabel();
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
		updateStatusLabel();
	}

	private void deleteScannable(IScanModelWrapper<IScanPathModel> scannable) {
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
				for (String scannable : scannablesSelected) {
					scannablesToShow.add(new ScanPathModelWrapper(scannable, null, false));
				}
				updateControls();
			}
		}
	}

	private static List<String> extractScannableNames(Collection<IScanModelWrapper<IScanPathModel>> scannables) {
		return scannables.stream()
				.map(IScanModelWrapper<IScanPathModel>::getName)
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
}

/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.tomo;

import static uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoScanSetupView.ANGLE_1_LABEL;
import static uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoScanSetupView.ANGLE_2_LABEL;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.ui.experiment.ScanPathEditor;

class TomoPathSection extends AbstractTomoViewSection {

	private Composite sectionComposite;
	private Composite pathControlsComposite;

	private ScanPathEditor angle1PathEditor;
	private ScanPathEditor angle2PathEditor;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);

		sectionComposite = createComposite(parent, 1, true);
		createSectionLabel(sectionComposite);

		createScanPathEditors();
	}

	private void createScanPathEditors() {
		if (pathControlsComposite != null) pathControlsComposite.dispose();
		if (angle1PathEditor != null) angle1PathEditor.dispose();
		if (angle2PathEditor != null) angle2PathEditor.dispose();

		pathControlsComposite = createComposite(sectionComposite, 2, false);
		angle1PathEditor = createScanPathEditor(pathControlsComposite, ANGLE_1_LABEL, getBean().getAngle1Model());
		angle2PathEditor = createScanPathEditor(pathControlsComposite, ANGLE_2_LABEL, getBean().getAngle2Model());
		createRestoreDefaultsButton(pathControlsComposite);
	}

	private void createSectionLabel(final Composite parent) {
		final Label sectionLabel = new Label(parent, SWT.NONE);
		sectionLabel.setText("Tomography Setup");
	}

	private ScanPathEditor createScanPathEditor(final Composite parent, String axisLabel,
			final IScanModelWrapper<IAxialModel> angleParams) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(axisLabel);

		final ScanPathEditor scanPathEditor = new ScanPathEditor(parent, SWT.NONE, angleParams);
		scanPathEditor.addIObserver(this::handleScanPathUpdate);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(scanPathEditor);
		return scanPathEditor;
	}

	@SuppressWarnings("unused")
	private void handleScanPathUpdate(Object source, Object arg) {
		getView().updatePoints();
	}

	private void createRestoreDefaultsButton(Composite parent) {
		final Button restoreDefaultsButton = new Button(parent, SWT.PUSH);
		restoreDefaultsButton.setText("Restore Defaults"); // TODO DAQ-4034 should label be "Calculate steps"
		GridDataFactory.swtDefaults().span(2, 1).align(SWT.TRAIL, SWT.CENTER).applyTo(restoreDefaultsButton);
		restoreDefaultsButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> setTomographyDefaults()));
	}

	private void setTomographyDefaults() {
		// TODO DAQ-4034 implement restoring / calculating defaults
		MessageDialog.openInformation(getShell(), "Tensor Tomography Scan", "Restore defaults pressed");
	}

	@Override
	public void updateControls() {
		createScanPathEditors();
		getView().relayout();
	}

}

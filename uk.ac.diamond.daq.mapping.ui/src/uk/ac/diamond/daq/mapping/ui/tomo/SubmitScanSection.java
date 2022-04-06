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

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.TensorTomoScanBean;
import uk.ac.diamond.daq.mapping.ui.Activator;
import uk.ac.diamond.daq.mapping.ui.MappingUIConstants;
import uk.ac.diamond.daq.mapping.ui.experiment.copyscan.CopyScanConfig;
import uk.ac.diamond.daq.mapping.ui.experiment.copyscan.CopyScanWizard;
import uk.ac.diamond.daq.mapping.ui.experiment.copyscan.CopyScanWizardDialog;

class SubmitScanSection extends AbstractTomoViewSection  {

	private static final String[] TOMO_FILE_FILTER_NAMES = new String[] { "Tomo Scan Files (*.tomo)", "All Files (*.*)" };
	private static final String[] TOMO_FILE_FILTER_EXTENSIONS = new String[] { "*.tomo", "*.*" };

	private static final Logger logger = LoggerFactory.getLogger(SubmitScanSection.class);

	private CopyScanConfig copyScanConfig = null;

	protected SubmitScanSection(TensorTomoScanSetupView tomoView) {
		super(tomoView);
	}

	@Override
	public void createControls(Composite parent) {
		// TODO reuse code from mapping SubmitScanSection
		createSeparator(parent);

		final Composite sectionComposite = createComposite(parent, 2, true);
		createSubmitSection(sectionComposite);
		createLoadSaveSection(sectionComposite);
	}

	private void createLoadSaveSection(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.RIGHT, SWT.CENTER).applyTo(composite);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(composite);

		final Button copyScanCommandButton = new Button(composite, SWT.PUSH);
		copyScanCommandButton.setImage(Activator.getImage("icons/copy.png"));
		copyScanCommandButton.setToolTipText("Copy the scan command to the system clipboard");
		copyScanCommandButton.addSelectionListener(widgetSelectedAdapter(e -> copyScanToClipboard()));

		// TODO use multi-function button to also load from nexus file or persistence service?
		final Button loadScanButton = new Button(composite, SWT.PUSH);
		loadScanButton.setImage(Activator.getImage("icons/open.png"));
		loadScanButton.setToolTipText("Load a scan from a .tomo file");
		loadScanButton.addSelectionListener(widgetSelectedAdapter(e -> loadScan()));

		final  Button saveScanButton = new Button(composite, SWT.PUSH);
		saveScanButton.setImage(Activator.getImage("icons/save.png"));
		saveScanButton.setToolTipText("Save a scan to a .tomo file");
		saveScanButton.addSelectionListener(widgetSelectedAdapter(e -> saveScan()));
	}

	private void copyScanToClipboard() {
		final CopyScanWizard copyScanWizard = new CopyScanWizard(tomoView.createScanBean(), getCopyScanConfig());
		new CopyScanWizardDialog(getShell(), copyScanWizard).open();
	}

	private CopyScanConfig getCopyScanConfig() {
		if (copyScanConfig == null) {
			copyScanConfig = new CopyScanConfig();
		}
		return copyScanConfig;
	}

	private void loadScan() {
		final String filename = chooseFileName(SWT.OPEN);
		if (filename == null) return;

		try {
			final byte[] bytes = Files.readAllBytes(Paths.get(filename));
			final String json = new String(bytes, StandardCharsets.UTF_8);
			final IMarshallerService marshaller = getService(IMarshallerService.class);
			final TensorTomoScanBean tomoBean = marshaller.unmarshal(json, TensorTomoScanBean.class);
			tomoView.setTomoBean(tomoBean);
			tomoView.refreshView();
		} catch (Exception e) {
			final String errorMessage = "Could not load a tomography scan from file: " + filename;
			logger.error(errorMessage, e);
			ErrorDialog.openError(getShell(), "Load Tomo Scan", errorMessage,
					new Status(IStatus.ERROR, MappingUIConstants.PLUGIN_ID, errorMessage, e));
		}
	}

	private void saveScan() {
		final String filename = chooseFileName(SWT.SAVE);
		if (filename == null) return;

		// TODO capture stage info snapshot? Proabably not necessary as malcolm device determines this on I22
		// or should we do it anyway, in case this view is used by other beamlines?
		final IMarshallerService marshaller = getService(IMarshallerService.class);
		try {
			final String json = marshaller.marshal(getTomoBean());
			Files.write(Paths.get(filename), json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		} catch (Exception e) {
			final String errorMessage = "Could not save the tomo scan to file: " + filename;
			logger.error(errorMessage, e);
			ErrorDialog.openError(getShell(), "Save Tomo Scan", errorMessage,
					new Status(IStatus.ERROR, MappingUIConstants.PLUGIN_ID, errorMessage, e));
		}
	}

	private String chooseFileName(int fileDialogStyle) {
		final FileDialog dialog = new FileDialog(getShell(), fileDialogStyle);
		dialog.setFilterNames(TOMO_FILE_FILTER_NAMES);
		dialog.setFilterExtensions(TOMO_FILE_FILTER_EXTENSIONS);
		dialog.setFilterPath(getVisitConfigDir());
		dialog.setOverwrite(true);

		return dialog.open();
	}

	private void createSubmitSection(Composite parent) {
		final Button submitScanButton = new Button(parent, SWT.PUSH);
		submitScanButton.setText("Queue Scan");
		submitScanButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> submitScan()));
	}

	private void submitScan() {
		tomoView.submitScan();
	}

	private String getVisitConfigDir() {
		return getService(IFilePathService.class).getVisitConfigDir();
	}

}

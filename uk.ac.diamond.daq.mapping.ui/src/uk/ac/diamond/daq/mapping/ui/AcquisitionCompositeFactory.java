/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui;

import java.util.Arrays;
import java.util.function.Supplier;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.ui.controller.AcquisitionUiReloader;
import uk.ac.diamond.daq.mapping.ui.experiment.controller.ExperimentScanningAcquisitionController;
import uk.ac.gda.api.acquisition.AcquisitionKeys;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.AcquisitionControllerException;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.Reloadable;
import uk.ac.gda.ui.tool.controller.AcquisitionController;


public abstract class AcquisitionCompositeFactory implements CompositeFactory {

	private static final Logger logger = LoggerFactory.getLogger(AcquisitionCompositeFactory.class);

	private final Supplier<Composite> buttonsCompositeSupplier;
	private CompositeFactory scanControls;
	private AcquisitionController<ScanningAcquisition> acquisitionController;

	protected AcquisitionCompositeFactory(Supplier<Composite> buttonsCompositeSupplier) {
		this.buttonsCompositeSupplier = buttonsCompositeSupplier;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		try {
			getAcquisitionController().initialise(getKey());
		} catch (AcquisitionControllerException e) {
			logger.error("Error initialising beam selector acquisition", e);
			var errorComposite = new Composite(parent, SWT.NONE);
			GridLayoutFactory.swtDefaults().applyTo(errorComposite);
			new Label(errorComposite, SWT.NONE).setText("Beam selector scans unavailable (see error log)");
			return errorComposite;
		}

		var controls = getScanControls().createComposite(parent, style);
		var buttonsComposite = buttonsCompositeSupplier.get();
		Arrays.asList(buttonsComposite.getChildren()).forEach(Control::dispose);
		getButtonControlsFactory().createComposite(buttonsComposite, SWT.NONE);
		if (scanControls instanceof Reloadable reloadableScanControls) {
			var loadListener = new AcquisitionUiReloader(getKey(), reloadableScanControls);
			SpringApplicationContextFacade.addApplicationListener(loadListener);
			controls.addDisposeListener(dispose -> SpringApplicationContextFacade.removeApplicationListener(loadListener));
		}
		return controls;
	}

	protected abstract AcquisitionKeys getKey();

	protected abstract Supplier<CompositeFactory> createScanControls();

	private CompositeFactory getScanControls() {
		if (scanControls == null) {
			scanControls = createScanControls().get();
		}
		return scanControls;
	}

	protected abstract CompositeFactory getButtonControlsFactory();

	/**
	 * The human friendly name to identify the acquisition in the GUI
	 */
	protected abstract ClientMessages getName();

	public void createNewAcquisition() {
		boolean confirmed = UIHelper.showConfirm("Create new configuration? The existing one will be discarded");
		if (confirmed) {
			try {
				getAcquisitionController().newScanningAcquisition(getKey());
			} catch (AcquisitionControllerException e) {
				handleControllerException(e);
			}
			if (scanControls instanceof Reloadable reloadableScanControls) {
				reloadableScanControls.reload();
			}
		}
	}

	public void saveAcquisition() {
		try {
			getAcquisitionController().saveAcquisitionConfiguration();
		} catch (AcquisitionControllerException e) {
			handleControllerException(e);
		}
	}

	public void submitAcquisition() {
		try {
			getAcquisitionController().runAcquisition();
		} catch (AcquisitionControllerException e) {
			handleControllerException(e);
		}
	}

	private AcquisitionController<ScanningAcquisition> getAcquisitionController() {
		if (acquisitionController == null) {
			acquisitionController = SpringApplicationContextFacade.getBean(ExperimentScanningAcquisitionController.class);
		}
		return acquisitionController;
	}

	private void handleControllerException(AcquisitionControllerException e) {
		Throwable root = e.getCause();
		UIHelper.showError(e.getMessage(), root == null ? "Unknown" : root.getMessage());
	}
}
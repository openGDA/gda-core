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

package uk.ac.gda.tomography.scan.editor.view.configuration.radiography;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.Arrays;
import java.util.function.Supplier;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.diamond.daq.mapping.ui.controller.ScanningAcquisitionController;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceLoadEvent;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.composites.AcquisitionCompositeButtonGroupFactoryBuilder;
import uk.ac.gda.client.exception.AcquisitionControllerException;
import uk.ac.gda.client.properties.acquisition.AcquisitionKeys;
import uk.ac.gda.client.properties.acquisition.AcquisitionPropertyType;
import uk.ac.gda.client.properties.acquisition.AcquisitionSubType;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.document.ScanningAcquisitionTemporaryHelper;
import uk.ac.gda.ui.tool.selectable.NamedCompositeFactory;

/**
 * This is the base for the radiography configuration.
 *
 * <p>
 * Listening to {@link AcquisitionConfigurationResourceLoadEvent} refresh the view when a new configuration is loaded.
 * </p>
 *
 * @author Maurizio Nagni
 */
public class RadiographyComposite implements NamedCompositeFactory {

	private static final Logger logger = LoggerFactory.getLogger(RadiographyComposite.class);
	private static final AcquisitionKeys key = new AcquisitionKeys(AcquisitionPropertyType.TOMOGRAPHY, AcquisitionSubType.STANDARD, AcquisitionTemplateType.STATIC_POINT);

	private final Supplier<Composite> buttonsCompositeSupplier;

	private ScanningAcquisitionController acquisitionController;

	private RadiographyScanControls scanControls;

	private final LoadListener loadListener;

	public RadiographyComposite(Supplier<Composite> buttonsCompositeSupplier) {
		this.buttonsCompositeSupplier = buttonsCompositeSupplier;
		this.loadListener = new LoadListener();
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		try {
			getAcquisitionController().initialise(key);
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
		buttonsComposite.layout(true, true);
		SpringApplicationContextFacade.addApplicationListener(loadListener);
		controls.addDisposeListener(dispose -> SpringApplicationContextFacade.removeApplicationListener(loadListener));
		return controls;
	}

	private CompositeFactory getScanControls() {
		if (scanControls == null) {
			this.scanControls = new RadiographyScanControls();
		}
		return scanControls;
	}

	@Override
	public ClientMessages getName() {
		return ClientMessages.RADIOGRAPHY;
	}

	@Override
	public ClientMessages getTooltip() {
		return ClientMessages.RADIOGRAPHY_TP;
	}

	private CompositeFactory getButtonControlsFactory() {
		return getAcquistionButtonGroupFactoryBuilder().build();
	}

	private AcquisitionCompositeButtonGroupFactoryBuilder getAcquistionButtonGroupFactoryBuilder() {
		var acquisitionButtonGroup = new AcquisitionCompositeButtonGroupFactoryBuilder();
		acquisitionButtonGroup.addNewSelectionListener(widgetSelectedAdapter(event -> {
			createNewAcquisition();
			scanControls.reload();
		}));
		acquisitionButtonGroup.addSaveSelectionListener(widgetSelectedAdapter(event -> getScanningAcquisitionTemporaryHelper().saveAcquisition()));
		acquisitionButtonGroup.addRunSelectionListener(widgetSelectedAdapter(event -> getScanningAcquisitionTemporaryHelper().runAcquisition()));
		return acquisitionButtonGroup;
	}

	private void createNewAcquisition() {
		boolean confirmed = UIHelper.showConfirm("Create new configuration? The existing one will be discarded");
		if (confirmed) {
			try {
				getAcquisitionController().newScanningAcquisition(key);
			} catch (AcquisitionControllerException e) {
				logger.error("Could not create new beam selector acquisition", e);
			}
		}
	}

	private ScanningAcquisitionController getAcquisitionController() {
		if (acquisitionController == null) {
			acquisitionController = SpringApplicationContextFacade.getBean(ScanningAcquisitionController.class);
		}
		return acquisitionController;
	}

	private class LoadListener implements ApplicationListener<AcquisitionConfigurationResourceLoadEvent> {

		@Override
		public void onApplicationEvent(AcquisitionConfigurationResourceLoadEvent event) {
			if (!(event.getSource() instanceof ScanningAcquisitionController)) {
				return;
			}
			if (!AcquisitionPropertyType.TOMOGRAPHY.equals(((ScanningAcquisitionController)event.getSource()).getAcquisitionKeys().getPropertyType())) {
				return;
			}
			PlatformUI.getWorkbench().getDisplay().asyncExec(scanControls::reload);
		}
	}

	private ScanningAcquisitionTemporaryHelper getScanningAcquisitionTemporaryHelper() {
		return SpringApplicationContextFacade.getBean(ScanningAcquisitionTemporaryHelper.class);
	}
}
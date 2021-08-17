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
import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.addDisposableApplicationListener;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.diamond.daq.mapping.api.document.helper.ScanpathDocumentHelper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.ui.controller.ScanningAcquisitionController;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceLoadEvent;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.composites.AcquisitionCompositeButtonGroupFactoryBuilder;
import uk.ac.gda.client.properties.acquisition.AcquisitionPropertyType;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.Reloadable;
import uk.ac.gda.ui.tool.document.DocumentFactory;
import uk.ac.gda.ui.tool.document.ScanningAcquisitionTemporaryHelper;
import uk.ac.gda.ui.tool.selectable.ButtonControlledCompositeTemplate;
import uk.ac.gda.ui.tool.selectable.NamedCompositeFactory;

/**
 * This Composite allows to edit a {@link ScanningParameters} object.
 *
 * @author Maurizio Nagni
 */
public class RadiographyButtonControlledCompositeFactory implements NamedCompositeFactory, ButtonControlledCompositeTemplate {

	private static final Logger logger = LoggerFactory.getLogger(RadiographyButtonControlledCompositeFactory.class);

	private final Supplier<Composite> controlButtonsContainerSupplier;

	private CompositeFactory acquistionConfigurationFactory;
	private ScanpathDocumentHelper scanpathDocumentHelper;

	public RadiographyButtonControlledCompositeFactory(Supplier<Composite> controlButtonsContainerSupplier) {
		this.controlButtonsContainerSupplier = controlButtonsContainerSupplier;
		try {
			this.scanpathDocumentHelper = new ScanpathDocumentHelper(this::getScanningParameters);
		} catch (NoSuchElementException e) {
			UIHelper.showWarning("Tomography cannot be instantiated normally", e);
		}
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		addDisposableApplicationListener(this, new LoadListener());
		acquistionConfigurationFactory = null;
		return createButtonControlledComposite(parent, style);
	}

	@Override
	public ClientMessages getName() {
		return ClientMessages.RADIOGRAPHY;
	}

	@Override
	public ClientMessages getTooltip() {
		return ClientMessages.RADIOGRAPHY_TP;
	}

	@Override
	public CompositeFactory getControlledCompositeFactory() {
		return Optional.ofNullable(acquistionConfigurationFactory)
				.orElseGet(this::createAcquistionConfigurationFactory);
	}

	private CompositeFactory createAcquistionConfigurationFactory() {
		this.acquistionConfigurationFactory = new RadiographyConfigurationLayoutFactory();
		setAcquisitionTemplateType(AcquisitionTemplateType.STATIC_POINT);
		return this.acquistionConfigurationFactory;
	}

	private void setAcquisitionTemplateType(AcquisitionTemplateType acquisitionTemplateType) {
		getDocumentFactory()
			.buildScanpathBuilder(AcquisitionPropertyType.TOMOGRAPHY, acquisitionTemplateType)
			.ifPresent(scanpathDocumentHelper::updateScanPathDocument);
	}

	@Override
	public CompositeFactory getButtonControlsFactory() {
		return getAcquistionButtonGroupFacoryBuilder().build();
	}

	@Override
	public Supplier<Composite> getButtonControlsContainerSupplier() {
		return controlButtonsContainerSupplier;
	}

	private AcquisitionCompositeButtonGroupFactoryBuilder getAcquistionButtonGroupFacoryBuilder() {
		var acquisitionButtonGroup = new AcquisitionCompositeButtonGroupFactoryBuilder();
		acquisitionButtonGroup.addNewSelectionListener(widgetSelectedAdapter(event -> getScanningAcquisitionTemporaryHelper().newAcquisition()));
		acquisitionButtonGroup.addSaveSelectionListener(widgetSelectedAdapter(event -> getScanningAcquisitionTemporaryHelper().saveAcquisition()));
		acquisitionButtonGroup.addRunSelectionListener(widgetSelectedAdapter(event -> getScanningAcquisitionTemporaryHelper().runAcquisition()));
		return acquisitionButtonGroup;
	}

	private class LoadListener implements ApplicationListener<AcquisitionConfigurationResourceLoadEvent> {

		@Override
		public void onApplicationEvent(AcquisitionConfigurationResourceLoadEvent event) {
			if (!(event.getSource() instanceof ScanningAcquisitionController)) {
				return;
			}
			if (!AcquisitionPropertyType.TOMOGRAPHY.equals(((ScanningAcquisitionController)event.getSource()).getAcquisitionType())) {
				return;
			}
			if (getControlledCompositeFactory() instanceof Reloadable) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(((Reloadable)getControlledCompositeFactory())::reload);
			}
		}
	}

	// ------------ UTILS ----
	private ScanningParameters getScanningParameters() {
		return getScanningAcquisitionTemporaryHelper()
				.getScanningParameters()
				.orElseThrow();
	}

	private DocumentFactory getDocumentFactory() {
		return SpringApplicationContextFacade.getBean(DocumentFactory.class);
	}

	private ScanningAcquisitionTemporaryHelper getScanningAcquisitionTemporaryHelper() {
		return SpringApplicationContextFacade.getBean(ScanningAcquisitionTemporaryHelper.class);
	}
}
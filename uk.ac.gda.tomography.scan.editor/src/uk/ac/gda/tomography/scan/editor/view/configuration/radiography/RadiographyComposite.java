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

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.ui.AcquisitionCompositeFactory;
import uk.ac.gda.api.acquisition.AcquisitionKeys;
import uk.ac.gda.api.acquisition.AcquisitionPropertyType;
import uk.ac.gda.api.acquisition.AcquisitionSubType;
import uk.ac.gda.api.acquisition.TrajectoryShape;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceLoadEvent;
import uk.ac.gda.client.composites.AcquisitionCompositeButtonGroupFactoryBuilder;
import uk.ac.gda.ui.tool.ClientMessages;

/**
 * This is the base for the radiography configuration.
 *
 * <p>
 * Listening to {@link AcquisitionConfigurationResourceLoadEvent} refresh the view when a new configuration is loaded.
 * </p>
 *
 * @author Maurizio Nagni
 */
public class RadiographyComposite extends AcquisitionCompositeFactory {

	private static final AcquisitionKeys key = new AcquisitionKeys(AcquisitionPropertyType.TOMOGRAPHY, AcquisitionSubType.RADIOGRAPHY, TrajectoryShape.STATIC_POINT);

	public RadiographyComposite(Supplier<Composite> buttonsCompositeSupplier) {
		super(buttonsCompositeSupplier);
	}

	@Override
	protected Supplier<CompositeFactory> createScanControls() {
		return RadiographyScanControls::new;
	}

	@Override
	public ClientMessages getName() {
		return ClientMessages.RADIOGRAPHY;
	}

	@Override
	protected AcquisitionKeys getKey() {
		return key;
	}

	@Override
	protected CompositeFactory getButtonControlsFactory() {
		return getAcquistionButtonGroupFactoryBuilder().build();
	}

	private AcquisitionCompositeButtonGroupFactoryBuilder getAcquistionButtonGroupFactoryBuilder() {
		var acquisitionButtonGroup = new AcquisitionCompositeButtonGroupFactoryBuilder();
		acquisitionButtonGroup.addSaveSelectionListener(widgetSelectedAdapter(event -> saveAcquisition()));
		acquisitionButtonGroup.addRunSelectionListener(widgetSelectedAdapter(event -> submitAcquisition()));
		return acquisitionButtonGroup;
	}
}
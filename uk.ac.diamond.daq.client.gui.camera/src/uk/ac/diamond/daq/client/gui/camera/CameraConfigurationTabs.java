/* Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.swt.widgets.Composite;

import gda.rcp.views.CompositeFactory;
import gda.rcp.views.TabCompositeFactory;
import gda.rcp.views.TabCompositeFactoryImpl;
import gda.rcp.views.TabFolderBuilder;
import uk.ac.diamond.daq.client.gui.camera.absorption.AbsorptionComposite;
import uk.ac.diamond.daq.client.gui.camera.liveview.CameraImageComposite;
import uk.ac.diamond.daq.client.gui.camera.positioning.CameraPositioningComposite;
import uk.ac.diamond.daq.client.gui.camera.roi.SensorSelectionComposite;
import uk.ac.diamond.daq.client.gui.camera.settings.CameraSettingsComposite;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

public class CameraConfigurationTabs implements CompositeFactory {

	private CameraConfigurationProperties camera;
	private CameraImageComposite cameraImageComposite;

	public CameraConfigurationTabs(CameraConfigurationProperties camera, CameraImageComposite cameraImageComposite) {
		this.camera = camera;
		this.cameraImageComposite = cameraImageComposite;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		TabFolderBuilder builder = new TabFolderBuilder();
		builder.addTab(createSettingsCompositeFactory());
		builder.addTab(createPositioningCompositeFactory());
		builder.addTab(createAbsorptionCompositeFactory());
		builder.addTab(createROICompositeFactory());

		return builder.build().createComposite(parent, style);
	}

	private final TabCompositeFactory createSettingsCompositeFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		CompositeFactory cf = new CameraSettingsComposite();
		group.setCompositeFactory(cf);
		group.setLabel(ClientMessagesUtility.getMessage(ClientMessages.SETTINGS));
		return group;
	}

	private final TabCompositeFactory createPositioningCompositeFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		CompositeFactory cf = new CameraPositioningComposite(camera);
		group.setCompositeFactory(cf);
		group.setLabel(ClientMessagesUtility.getMessage(ClientMessages.POSITIONS));
		return group;
	}

	private final TabCompositeFactory createAbsorptionCompositeFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		group.setCompositeFactory(new AbsorptionComposite(cameraImageComposite));
		group.setLabel(ClientMessagesUtility.getMessage(ClientMessages.ABSORPTION));
		group.setTooltip(ClientMessagesUtility.getMessage(ClientMessages.ABSORPTION_TP));
		return group;
	}

	private final TabCompositeFactory createROICompositeFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		CompositeFactory cf = new SensorSelectionComposite();
		group.setCompositeFactory(cf);
		group.setLabel(ClientMessagesUtility.getMessage(ClientMessages.ROI));
		return group;
	}

}
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

package uk.ac.gda.tomography.ui.mode;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.IScannableMotor;
import gda.rcp.views.StageCompositeDefinition;
import gda.rcp.views.StageCompositeFactory;
import gda.rcp.views.TabCompositeFactory;
import gda.rcp.views.TabCompositeFactoryImpl;
import gda.rcp.views.TabFolderCompositeFactory;
import uk.ac.gda.tomography.scan.editor.view.TomographyMessages;
import uk.ac.gda.tomography.ui.tool.TomographyMessagesUtility;
import uk.ac.gda.tomography.ui.tool.TomographySWTElements;

public abstract class TomographyBaseMode implements TomographyMode {

	private boolean loaded = false;

	private Composite stageControls;

	// this works for any number of elements:
	private final Map<ModeDevices, String> devicesMap = new EnumMap<>(ModeDevices.class);
	private final Map<ModeDevices, IScannableMotor> motors = new EnumMap<>(ModeDevices.class);

	private static final Logger logger = LoggerFactory.getLogger(TomographyBaseMode.class);

	@Override
	public Map<ModeDevices, IScannableMotor> getMotors() throws IncompleteModeException {
		return loadMotors();
	}

	public Map<ModeDevices, String> getDevicesMap() {
		return Collections.unmodifiableMap(devicesMap);
	}

	public Composite getUI(Composite parent) {
		return getStageControls(parent);
	}

	void addToDevicesMap(ModeDevices device, String propertyName) {
		devicesMap.put(device, propertyName);
	}

	private Map<ModeDevices, IScannableMotor> loadMotors() throws IncompleteModeException {
		if (loaded) {
			return motors;
		}
		populateDevicesMap();
		for (Entry<ModeDevices, String> entry : getDevicesMap().entrySet()) {
			motors.put(entry.getKey(), ModeHelper.getMotor(getBeanId(entry)));
		}
		loaded = true;
		return motors;
	}

	private String getBeanId(Entry<ModeDevices, String> entry) throws IncompleteModeException {
		if (Objects.isNull(LocalProperties.get(entry.getValue(), null))) {
			throw new IncompleteModeException(String.format("Cannot find property beanId:%s for device:%s", entry.getValue(), entry.getKey()));
		}
		return LocalProperties.get(entry.getValue());
	}

	private Composite getStageControls(Composite parent) {
		if (Objects.isNull(stageControls) || stageControls.isDisposed()) {
			stageControls = TomographySWTElements.createComposite(parent, SWT.NONE, 4);
			createStageControls(SWT.NONE, SWT.BORDER);
			stageControls.pack();
		}
		return stageControls;
	}

	private Composite getStageControls() {
		return stageControls;
	}

	protected final TabCompositeFactory createStageMotorsCompositeFactory(StageCompositeDefinition[] motors, TomographyMessages message) {
		TabCompositeFactoryImpl group  = new TabCompositeFactoryImpl();
		StageCompositeFactory scf = new StageCompositeFactory();
		group.setCompositeFactory(scf);
		group.setLabel(TomographyMessagesUtility.getMessage(message));
		scf.setStageCompositeDefinitions(motors);
		return group;
	}

	protected StageCompositeDefinition doMotor(ModeDevices device, TomographyMessages label) {
		StageCompositeDefinition scd = new StageCompositeDefinition();
		try {
			scd.setScannable(getMotors().get(device));
		} catch (IncompleteModeException e) {
			logger.error("TODO put description of error here", e);
		}
		scd.setStepSize(1);
		scd.setDecimalPlaces(0);
		scd.setLabel(TomographyMessagesUtility.getMessage(label));
		return scd;
	}

	protected final void createStageControls(int labelStyle, int textStyle) {
		TabFolderCompositeFactory motorTabs = new TabFolderCompositeFactory();
		motorTabs.setFactories(getTabsFactories());
		motorTabs.createComposite(getStageControls(), SWT.NONE);
	}

	protected abstract void populateDevicesMap();

	protected abstract TabCompositeFactory[] getTabsFactories();
}

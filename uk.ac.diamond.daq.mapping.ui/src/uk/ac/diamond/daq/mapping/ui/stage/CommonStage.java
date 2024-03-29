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

package uk.ac.diamond.daq.mapping.ui.stage;

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createComposite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.IScannableMotor;
import gda.rcp.views.StageCompositeDefinition;
import gda.rcp.views.StageCompositeFactory;
import gda.rcp.views.TabCompositeFactory;
import gda.rcp.views.TabCompositeFactoryImpl;
import gda.rcp.views.TabFolderBuilder;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.Stage;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.StageDevice;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.spring.FinderService;


/**
 * Implements common methods for a {@link StageDescription}
 *
 * @author Maurizio Nagni
 */
public abstract class CommonStage implements StageDescription {

	private Composite stageControls;
	private Stage stage;

	private final Map<StageDevice, String> devicesMap = new EnumMap<>(StageDevice.class);
	private final Map<StageDevice, IScannableMotor> motors = new EnumMap<>(StageDevice.class);
	private final Map<String, String> metadata = new HashMap<>();

	private static final Logger logger = LoggerFactory.getLogger(CommonStage.class);

	protected CommonStage(Stage stage) {
		super();
		this.stage = stage;
	}

	private Map<StageDevice, IScannableMotor> getMotors() {
		if (motors.isEmpty()) {
			loadDevices();
		}
		return Collections.unmodifiableMap(motors);
	}

	@Override
	public Stage getStage() {
		return stage;
	}

	@Override
	public Map<String, String> getMetadata() {
		if (metadata.isEmpty()) {
			loadDevices();
		}
		return metadata;
	}

	@Override
	public Set<DevicePosition<Double>> getMotorsPosition() {
		Set<DevicePosition<Double>> positions = new HashSet<>();
		Map<StageDevice, IScannableMotor> intMotors = getMotors();
		if (intMotors == null) {
			return Collections.unmodifiableSet(positions);
		}
		for (Entry<StageDevice, IScannableMotor> entry : intMotors.entrySet()) {
			try {
				Double position = (Double) entry.getValue().getPosition();
				positions.add(new DevicePosition<>(entry.getKey(), position));
			} catch (DeviceException e) {
				logger.error("Cannot get the position of {}", entry.getKey());
			}
		}
		return Collections.unmodifiableSet(positions);
	}

	public Map<StageDevice, String> getDevicesMap() {
		if (devicesMap.isEmpty()) {
			populateDevicesMap();
		}
		return Collections.unmodifiableMap(devicesMap);
	}

	public Composite getUI(Composite parent) {
		return getStageControls(parent);
	}

	void addToDevicesMap(StageDevice device, String propertyName) {
		devicesMap.put(device, propertyName);
	}

	private void loadDevices() {
		for (Entry<StageDevice, String> entry : getDevicesMap().entrySet()) {
			if (entry.getKey().name().startsWith("MOTOR")) {
				mapMotor(entry);
			}
			if (entry.getKey().name().startsWith("MALCOLM")) {
				loadMalcolm(entry);
			}
		}
	}

	private void mapMotor(Entry<StageDevice, String> entry) {
			loadMotor(entry.getValue()).ifPresent(c -> motors.put(entry.getKey(), c));
	}

	public static final Optional<IScannableMotor> loadMotor(String entry) {
		try {
			return getIScannableMotor(getBeanId(entry));
		} catch (LoadException e) {
			var errMsg = String.format("Cannot load motor %s", entry);
			UIHelper.showError(errMsg, e);
			logger.error(errMsg, e);
		}
		return Optional.empty();
	}

	private void loadMalcolm(Entry<StageDevice, String> entry) {
		try {
			metadata.put(entry.getKey().name(), getBeanId(entry));
		} catch (LoadException e) {
			var errMsg = String.format("Cannot load Malcolm %s", entry.getKey());
			UIHelper.showError(errMsg, e);
			logger.error(errMsg, e);
		}
	}

	private String getBeanId(Entry<StageDevice, String> entry) throws LoadException {
		return getBeanId(entry.getValue());
	}

	private static final String getBeanId(String entry) throws LoadException {
		return Optional.ofNullable(LocalProperties.get(entry)).orElseThrow(LoadException::new);
	}

	private Composite getStageControls(Composite parent) {
		if (Objects.isNull(stageControls) || stageControls.isDisposed()) {
			stageControls = createComposite(parent, SWT.NONE, 4);
			createClientGridDataFactory().grab(true, false).applyTo(stageControls);
			createStageControls();
			stageControls.pack();
			stageControls.getShell().layout(true, true);
		}
		return stageControls;
	}

	private Composite getStageControls() {
		return stageControls;
	}

	protected final TabCompositeFactory createStageMotorsCompositeFactory(StageCompositeDefinition[] motors, ClientMessages message) {
		var group = new TabCompositeFactoryImpl();
		var scf = new StageCompositeFactory();
		group.setCompositeFactory(scf);
		group.setLabel(ClientMessagesUtility.getMessage(message));
		scf.setStageCompositeDefinitions(motors);
		return group;
	}

	protected StageCompositeDefinition createMotorElement(StageDevice device) throws StageException {
		var scd = new StageCompositeDefinition();
		scd.setScannable(getMotors().get(device));
		if (Objects.isNull(scd.getScannable())) {
			throw new StageException(String.format("Device %s not found", device));
		}
		scd.setStepSize(1);
		scd.setDecimalPlaces(3);
		return scd;
	}

	protected final void createStageControls() {
		var builder = new TabFolderBuilder();
		Arrays.stream(getTabsFactories()).forEach(builder::addTab);
		builder.build().createComposite(getStageControls(), SWT.NONE);
	}

	protected class TabCompositionBuilder {
		private List<TabCompositeFactory> tabComposite = new ArrayList<>();

		public TabCompositionBuilder assemble(StageCompositeDefinition[] stageComposite, ClientMessages label) {
			if (Objects.nonNull(stageComposite)) {
				tabComposite.add(createStageMotorsCompositeFactory(stageComposite, label));
			}
			return this;
		}

		public TabCompositeFactory[] build() {
			return tabComposite.toArray(new TabCompositeFactory[0]);
		}
	}

	protected class StageCompositeDefinitionBuilder {
		private List<StageCompositeDefinition> composite = new ArrayList<>();

		public StageCompositeDefinitionBuilder assemble(StageDevice device, ClientMessages label) {
			StageCompositeDefinition scd;
			try {
				scd = createMotorElement(device);
				scd.setLabel(ClientMessagesUtility.getMessage(label));
			} catch (StageException e) {
				return this;
			}
			composite.add(scd);
			return this;
		}

		public StageCompositeDefinition[] build() {
			return composite.toArray(new StageCompositeDefinition[0]);
		}
	}

	/**
	 * Using {@link #addToDevicesMap} defines the devices in the stage
	 */
	protected abstract void populateDevicesMap();

	/**
	 * Defines an array of factories each of which creates a tab describing a set of stage features. Usually a single tab is enough to contains all the stage
	 * motors, but additional tabs may be created to describe further consistent, stage specific, sets of properties as temperature control or features as time
	 * controller operations.
	 *
	 * @return an array of tabcomposites
	 */
	protected abstract TabCompositeFactory[] getTabsFactories();

	private static class LoadException extends GDAClientException {

	}

	private static Optional<IScannableMotor> getIScannableMotor(String findableMotor) {
		return SpringApplicationContextFacade.getBean(FinderService.class)
				.getFindableObject(findableMotor, IScannableMotor.class);
	}
}

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

package uk.ac.diamond.daq.mapping.ui.services;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBeanProvider;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.impl.DetectorModelWrapper;
import uk.ac.diamond.daq.mapping.impl.MappingExperimentBean;
import uk.ac.diamond.daq.mapping.ui.experiment.RegionAndPathController;
import uk.ac.diamond.daq.mapping.ui.experiment.ScanManagementController;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.spring.ClientRemoteServices;

/**
 * Centralise all services requests by the mapping package. The goal is to reduce multiple implementation to access the
 * same service.
 *
 * @author Maurizio Nagni
 */
public class MappingServices {

	private static final Logger logger = LoggerFactory.getLogger(MappingServices.class);

	private MappingServices() {
	}

	/**
	 * @return a plotting system instance
	 * @deprecated use instead {@link ClientRemoteServices#getIPlottingService()}
	 */
	@Deprecated
	public static IPlottingService getPlottingService() {
		return getClientRemoteServices().getIPlottingService();
	}

	/**
	 * @return a Scan Management instance
	 * @deprecated use instead {@link MappingRemoteServices#getScanManagementController()}
	 */
	@Deprecated
	public static ScanManagementController getScanManagementController() {
		return getMappingRemoteServices().getScanManagementController();
	}

	/**
	 * @return a Region nd Path controller instance
	 * @deprecated use instead {@link MappingRemoteServices#getRegionAndPathController()}
	 */
	@Deprecated
	public static RegionAndPathController getRegionAndPathController() {
		return getMappingRemoteServices().getRegionAndPathController();
	}

	public static Optional<IRunnableDeviceService> getIRunnableDeviceService() {
		return Optional.of(getClientRemoteServices().getIRunnableDeviceService());
	}

	public static IMappingExperimentBeanProvider getMappingBeanProvider() {
		return getWorkbench().getService(IMappingExperimentBeanProvider.class);
	}

	/**
	 * Updates the {@link MappingExperimentBean#getDetectorParameters()} communicating directly with the server
	 *
	 * @return the map of the discovered detectors
	 */
	public static Map<String, IScanModelWrapper<IDetectorModel>> updateDetectorParameters() {
		return internalUpdateDetectorParameters();
	}

	private static Collection<DeviceInformation<?>> getDevicesInformation(DeviceRole role) {
		IRunnableDeviceService rs = getIRunnableDeviceService().orElse(null);
		if (rs != null) {
			try {
				return rs.getDeviceInformation(role);
			} catch (ScanningException e) {
				logger.error("Error getting device informations", e);
			}
		}
		return Collections.emptyList();
	}

	private static IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	private static Map<String, IScanModelWrapper<IDetectorModel>> internalUpdateDetectorParameters() {
		logger.info("{} updateDetectorParameters", MappingServices.class);
		// a function to convert DeviceInformations to IDetectorModelWrappers
		final Function<DeviceInformation<?>, IScanModelWrapper<IDetectorModel>> malcolmInfoToWrapper = info -> {
			final DetectorModelWrapper<IDetectorModel> wrapper = new DetectorModelWrapper<>(info.getLabel(),
					(IDetectorModel) info.getModel(), false);
			wrapper.setShownByDefault(info.isShownByDefault());
			return wrapper;
		};

		// wraps DeviceInformation objects into DetectorModelWrappers
		Collection<DeviceInformation<?>> devices = Arrays.stream(DeviceRole.values())
				.map(MappingServices::getDevicesInformation).flatMap(Collection::stream).collect(Collectors.toList());

		// creates a map of detector models
		final Map<String, IScanModelWrapper<IDetectorModel>> params = devices.stream()
				.filter(p -> IDetectorModel.class.isInstance(p.getModel()))
				.map(malcolmInfoToWrapper::apply)
				.collect(toMap(IScanModelWrapper<IDetectorModel>::getName, identity()));

		// extracts the detector names
		final Set<String> deviceNames = params.values().stream()
				.map(IScanModelWrapper<IDetectorModel>::getModel).map(IDetectorModel::getName)
				.collect(Collectors.toSet());

		// a predicate to filter out malcolm devices which no longer exist
		final Predicate<IScanModelWrapper<IDetectorModel>> nonExistantMalcolmFilter = wrapper -> !(wrapper
				.getModel() instanceof IMalcolmModel) || deviceNames.contains(wrapper.getModel().getName());

		// creates an IScanModelWrapper map
		final Map<String, IScanModelWrapper<IDetectorModel>> detectorParamsByName = getMappingBeanProvider()
				.getMappingExperimentBean().getDetectorParameters().stream().filter(nonExistantMalcolmFilter)
				.collect(toMap(IScanModelWrapper<IDetectorModel>::getName, // key by name
						identity(), // the value is the wrapper itself
						(v1, v2) -> v1, // merge function not used as there should be no duplicate keys
						LinkedHashMap::new)); // create a linked hash map to maintain the order

		// merge in the wrappers for the malcolm devices. The merge function here keeps the original
		// wrapper if the mapping bean already contained one for a device with this name
		params.forEach((name, parameters) -> detectorParamsByName.merge(name, parameters, (v1, v2) -> v1));

		// convert to a list and set this as the detector parameters in the bean
		final List<IScanModelWrapper<IDetectorModel>> detectorParamList = new ArrayList<>(
				detectorParamsByName.values());
		getMappingBeanProvider().getMappingExperimentBean().setDetectorParameters(detectorParamList);

		return detectorParamsByName;
	}

	private static MappingRemoteServices getMappingRemoteServices() {
		return SpringApplicationContextFacade.getBean(MappingRemoteServices.class);
	}

	private static ClientRemoteServices getClientRemoteServices() {
		return SpringApplicationContextFacade.getBean(ClientRemoteServices.class);
	}
}

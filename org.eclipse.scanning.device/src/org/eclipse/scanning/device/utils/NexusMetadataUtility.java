/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package org.eclipse.scanning.device.utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.INexusDeviceDecorator;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.ServiceHolder;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusEntryBuilder;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.device.SimpleNexusDevice;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.scanning.device.AbstractMetadataField;
import org.eclipse.scanning.device.AbstractNexusMetadataDevice;
import org.eclipse.scanning.device.INexusMetadataDevice;
import org.eclipse.scanning.device.LinkedField;
import org.eclipse.scanning.device.NexusMetadataAppender;
import org.eclipse.scanning.device.NexusMetadataDevice;
import org.eclipse.scanning.device.ScalarField;
import org.eclipse.scanning.device.ScannableField;
import org.eclipse.scanning.device.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.Scannable;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;

/**
 * a utility class to support dynamic metadata creation, add, remove and display in Jython terminal during runtime by users.
 *
 * @author Fajin Yuan
 * @since 9.22
 */
public enum NexusMetadataUtility {

	INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(NexusMetadataUtility.class);
	/**
	 * map of user created instances of {@link AbstractNexusMetadataDevice}s using device name as key
	 */
	private final Map<String, AbstractNexusMetadataDevice<NXobject>> userAddedNexusMetadataDevices = new HashMap<>();
	/**
	 * set of user added fields to metadata devices
	 */
	private final Set<ImmutablePair<String, String>> userAddedFields = new HashSet<>();

	/**
	 * add a {@link ScalarField} to the specified metadata device. If the named device is not existed yet a new metadata device
	 * will be created with nexus type default to {@link NexusConstants#COLLECTION}
	 *
	 * @param deviceName
	 *            - the name of metadata device to add
	 * @param fieldName
	 *            - the field name to be added to the device
	 * @param fieldValue
	 *            - the field value to be added to the device
	 * @param unit
	 *            - the unit for the field
	 */
	public void addScalar(String deviceName, String fieldName, Object fieldValue, String unit) {
		final INexusMetadataDevice<NXobject> nxMetadataDevice = getNexusMetadataDeviceOrAppender(deviceName)
				.orElseGet(() -> createNexusMetadataDevice(deviceName, NexusConstants.COLLECTION));
		nxMetadataDevice.setScalarField(fieldName, fieldValue, unit);
		userAddedFields.add(new ImmutablePair<>(deviceName, fieldName));
	}

	/**
	 * add a {@link ScannableField} to the specified metadata device. If the named device is not existed yet a new metadata device
	 * will be created with nexus type default to {@link NexusConstants#COLLECTION}
	 *
	 * @param deviceName
	 *            - the name of metadata device to add
	 * @param scannable
	 *			  - the scannable object to be added to the device
	 */
	public void addScannable(String deviceName, Scannable scannable) {
		if (deviceName.equals(scannable.getName())) {
			throw new IllegalArgumentException(MessageFormat.format("Name of metadata device {0} cannot be same as the name of given scannable {1} ", deviceName, scannable.getName()));
		}
		Set<String> allScannableNames = InterfaceProvider.getJythonNamespace().getAllNamesForType(Scannable.class);
		if (allScannableNames.contains(deviceName)) {
			throw new IllegalArgumentException(MessageFormat.format("Name of metadata device {0} cannot be same as name of another scannable in GDA", deviceName));
		}
		final INexusMetadataDevice<NXobject> nxMetadataDevice = getNexusMetadataDeviceOrAppender(deviceName)
				.orElseGet(() -> createNexusMetadataDevice(deviceName, NexusConstants.COLLECTION));
		final String scannable_name = scannable.getName();
		final String field_name = scannable_name.startsWith(deviceName)
				? StringUtils.removeStart(scannable_name, deviceName)
				: scannable_name;
		nxMetadataDevice.addField(new ScannableField(field_name, scannable_name));
		userAddedFields.add(new ImmutablePair<>(deviceName, field_name));
	}

	/**
	 * add a {@link LinkedField} to the specified metadata device. If the named device is not existed yet a new metadata device
	 * will be created with nexus type default to {@link NexusConstants#COLLECTION}
	 *
	 * @param deviceName
	 *            - the name of metadata device to add
	 * @param fieldName
	 *            - the field name to be added to the device
	 * @param fileName
	 *            - the external file name, if null or empty, the link will be internal
	 * @param linkPath
	 *            - the link path value to be added to the device
	 */
	public void addLink(String deviceName, String fieldName, String fileName, String linkPath) {
		final INexusMetadataDevice<NXobject> nxMetadataDevice = getNexusMetadataDeviceOrAppender(deviceName)
				.orElseGet(() -> createNexusMetadataDevice(deviceName, NexusConstants.COLLECTION));
		if (StringUtils.isNotBlank(fileName)) {
			nxMetadataDevice.setExternalLinkedField(fieldName, fileName, linkPath);
		} else {
			nxMetadataDevice.setLinkedField(fieldName, linkPath);
		}
		userAddedFields.add(new ImmutablePair<>(deviceName, fieldName));
	}

	/**
	 * remove user added field from a specified device. If the device is empty i.e. had no child node, the device will
	 * be removed as well
	 *
	 * @param deviceName
	 *            - the name of the metadata device
	 * @param fieldName
	 *            - the filed name of the metadata device
	 */
	public void remove(String deviceName, String fieldName) {
		final INexusMetadataDevice<NXobject> nxMetadataDevice = getNexusMetadataDeviceOrAppender(deviceName)
				.orElseThrow();
		// remove field name from local cache
		if (userAddedFields.remove(new ImmutablePair<>(deviceName, fieldName))) {
			// Only allow user added field to be removed
			nxMetadataDevice.removeNode(fieldName);
		} else {
			InterfaceProvider.getTerminalPrinter().print("Field '" + fieldName + "' in device '" + deviceName
					+ "' is not user added metadata so it cannot be removed!");
		}
		removeEmptyDevice(deviceName, nxMetadataDevice);
	}

	private void removeEmptyDevice(String deviceName, final INexusMetadataDevice<NXobject> nxMetadataDevice) {
		if (userAddedNexusMetadataDevices.containsKey(deviceName)
				&& nxMetadataDevice instanceof AbstractNexusMetadataDevice
				&& !((AbstractNexusMetadataDevice<NXobject>) nxMetadataDevice).hasChildNodes()) {
			// only allow user added metadata group to be removed
			Services.getCommonBeamlineDevicesConfiguration().removeAdditionalDeviceName(deviceName);
			ServiceHolder.getNexusDeviceService().unregister(nxMetadataDevice);
			userAddedNexusMetadataDevices.remove(deviceName);
		}
	}

	/**
	 * remove all user added devices and fields
	 */
	public void clear() {
		userAddedFields.clear();
		userAddedNexusMetadataDevices.clear();
	}

	/**
	 * disable the specified metadata device, i.e. the metadata for this device will not be collected.
	 *
	 * @param deviceNames the names of the metadata devices
	 */
	public void disable(String... deviceNames) {
		final var commonBeamlineDevicesConfiguration = Services.getCommonBeamlineDevicesConfiguration();
		for (String deviceName : deviceNames) {
			if (commonBeamlineDevicesConfiguration.isMandatoryDeviceName(deviceName)) {
				InterfaceProvider.getTerminalPrinter().print(
						MessageFormat.format("Cannot disable metadata device \"{0}\".", deviceName));
			} else {
				commonBeamlineDevicesConfiguration.disableDevices(deviceNames);
			}
		}
	}

	/**
	 * enable the specified metadata device, i.e. the metadata for this device will be collected.
	 *
	 * @param deviceNames the names of the metadata devices
	 */
	public void enable(String... deviceNames) {
		final var commonBeamlineDevicesConfiguration = Services.getCommonBeamlineDevicesConfiguration();
		for (String deviceName : deviceNames) {
			if (commonBeamlineDevicesConfiguration.getDisabledDeviceNames().contains(deviceName)) {
				commonBeamlineDevicesConfiguration.enableDevice(deviceName);
			} else {
				InterfaceProvider.getTerminalPrinter().print(
						MessageFormat.format("Cannot enable metadata device \"{0}\", as it is not disabled", deviceName));
			}
		}
	}

	/**
	 * display the metadata for the specified device on Jython Terminal. this can be used to display context sensitive
	 * metadata device.
	 *
	 * @param deviceName
	 *            - the name of the metadata device to display
	 * @param showValue
	 *            - boolean to specify if value of the filed to be show or not
	 * @throws NexusException
	 *             - nexus provider not found exception
	 */
	public void display(String deviceName, boolean showValue) throws NexusException {
		final INexusMetadataDevice<NXobject> nxMetadataDevice = getNexusMetadataDeviceOrAppender(deviceName)
				.orElseThrow();
		final NexusObjectProvider<NXobject> nexusProvider = nxMetadataDevice.getNexusProvider(null);
		final NXobject nexusObject = nexusProvider.getNexusObject();
		final StringJoiner nexusNodePath = new StringJoiner("").add(getNexusNodePath(nexusProvider, deviceName));
		final StringJoiner message = new StringJoiner(" :: ").add(deviceName)
				.add(new StringJoiner("", "\t(", ")").add(nexusNodePath.toString()).toString());
		InterfaceProvider.getTerminalPrinter().print(message.toString());
		final Map<String, DataNode> dataNode = nexusObject.getDataNodeMap();
		prettyPrint(dataNode, 1, showValue);
		final Map<String, GroupNode> groupNodeMap = nexusObject.getGroupNodeMap();
		prettyPrintGroup(groupNodeMap, 1, showValue, nexusNodePath);
	}

	/**
	 * returns the path within the nexus file where the nexus object for the given nexusPovider would be added by
	 * DefaultNexusEntryBuilder
	 * <p>
	 * The implementation here is closely related to how the Default Diamond Nexus File Structure at
	 * https://confluence.diamond.ac.uk/x/uYVzBg are implemented in {@link DefaultNexusEntryBuilder#addDefaultGroups()}
	 * and {@link DefaultNexusEntryBuilder#addGroupToNexusTree()}.
	 *
	 * @param nexusProvider
	 * @param deviceName
	 * @return
	 * @throws NexusException
	 */
	private String getNexusNodePath(NexusObjectProvider<NXobject> nexusProvider, String deviceName)
			throws NexusException {
		// default groups in DefaultNexusEntryBuilder have only NXentry, NXinstrument, and NXsample at the writing of
		// this class
		final List<NXobject> defaultGroups = new ArrayList<>(Arrays.asList(NexusNodeFactory.createNXentry(),
				NexusNodeFactory.createNXinstrument(), NexusNodeFactory.createNXsample()));
		final NexusBaseClass category = nexusProvider.getCategory();
		String path = null;
		if (category == null) {
			NXobject nexusObject = nexusProvider.getNexusObject();
			for (final NXobject group : defaultGroups) {
				if (group.canAddChild(nexusObject)) {
					path = findPathForCategory(group.getNexusBaseClass(), deviceName);
					break;
				}
			}
			if (path == null) {
				throw new NexusException(
						"Cannot find a parent group that accepts a " + nexusObject.getNexusBaseClass());
			}
		} else {
			path = findPathForCategory(category, deviceName);
		}
		return path;
	}

	/**
	 * returns the path where a nexus object of a given category would be added within the entry by
	 * {@link DefaultNexusEntryBuilder}.
	 *
	 * @param category
	 * @param deviceName
	 * @return
	 * @throws NexusException
	 */
	private String findPathForCategory(NexusBaseClass category, String deviceName) throws NexusException {
		final String entryName = getEntryName();
		// handle the default groups defined in DefaultNexusEntryBuilder
		if (category == NexusBaseClass.NX_ENTRY) {
			return Node.SEPARATOR + entryName + Node.SEPARATOR + deviceName;
		} else if (category == NexusBaseClass.NX_INSTRUMENT) {
			return Node.SEPARATOR + entryName + Node.SEPARATOR + "instrument" + Node.SEPARATOR + deviceName;
		} else if (category == NexusBaseClass.NX_SAMPLE) {
			return Node.SEPARATOR + entryName + Node.SEPARATOR + "sample" + Node.SEPARATOR + deviceName;
		} else {
			throw new NexusException("No path found for category " + category);
		}
	}

	private String getEntryName() {
		String entryName = System.getProperty("org.eclipse.scanning.nexusEntryName");
		if (entryName == null) {
			entryName = System.getProperty("GDA/gda.nexus.entryName", "entry");
		}
		return entryName; // will be null if neither property set, uses default- "entry" name
	}

	private void prettyPrintGroup(Map<String, GroupNode> groupNodeMap, final int depth, boolean showValue,
			StringJoiner path) {
		final String indent = new String(new char[depth]).replace('\0', '\t');
		final int depth4datanode = depth + 1;
		groupNodeMap.entrySet().stream().forEach(
				entry -> printGroup(depth, showValue, path, indent, depth4datanode, entry.getKey(), entry.getValue()));
	}

	private int printGroup(int depth, boolean showValue, StringJoiner path2, final String indent, int depth4datanode,
			final String name, final GroupNode value) {
		final var path = new StringJoiner(Node.SEPARATOR, "\t(", ")").add(path2.toString()).add(name);
		final var message = new StringJoiner(" :: ", indent, "").add(name).add(path.toString());
		InterfaceProvider.getTerminalPrinter().print(message.toString());
		if (!value.getGroupNodeMap().isEmpty()) {
			prettyPrintGroup(value.getGroupNodeMap(), ++depth, showValue, path);
		}
		prettyPrint(value.getDataNodeMap(), depth4datanode, showValue);
		return depth;
	}

	private void prettyPrint(Map<String, DataNode> dataNode, int depth, boolean showValue) {
		final String indent = new String(new char[depth]).replace('\0', '\t');
		if (!showValue) {
			dataNode.entrySet().stream()
					.forEach(entry -> InterfaceProvider.getTerminalPrinter().print(indent + entry.getKey()));
		} else {
			dataNode.entrySet().stream().forEach(entry -> InterfaceProvider.getTerminalPrinter()
					.print(nameValueString(indent, entry.getKey(), entry.getValue())));
		}
	}

	private String nameValueString(final String indent, final String name, final DataNode value) {
		final var message = new StringJoiner(" : ", indent, "").add(name);
		try {
			if (value.getDataset().getRank() > 0) {
				// fix I10-583 to support scannable that returns mixed data type elements in array
				message.add(DatasetUtils.sliceAndConvertLazyDataset(value.getDataset()).toString(true));
			} else {
				var string = value.getDataset().getSlice().getString();
				if (value.getAttributeNames().contains(AbstractMetadataField.ATTRIBUTE_NAME_UNITS)) {
					string = new StringJoiner(" ").add(string)
							.add(value.getAttribute(AbstractMetadataField.ATTRIBUTE_NAME_UNITS).getFirstElement())
							.toString();
				}
				message.add(string);
			}
		} catch (DatasetException e) {
			String message2 = e.getMessage();
			logger.warn("Field '{}' : {}", name, message2, e);
			// metadata collection should not make data collection to fail thus just return a description of the
			// problem encountered.
			message.add(message2);
		}
		return message.toString();
	}

	/**
	 * display all metadata to be collected in Jython Terminal except thos from context sensitive Metadata Appenders.
	 *
	 * @param showValue
	 *            - boolean to specify if filed value to be shown or not
	 * @throws NexusException
	 *             - nexus provider not found exception
	 */
	public void list(boolean showValue) throws NexusException {
		final var commonBeamlineDevicesConfiguration = Services.getCommonBeamlineDevicesConfiguration();
		final Set<String> commonDeviceNames = commonBeamlineDevicesConfiguration.getCommonDeviceNames();

		for (String deviceName : commonDeviceNames) {
			display(deviceName, showValue);
		}
	}

	public Optional<INexusMetadataDevice<NXobject>> getNexusMetadataDeviceOrAppender(String name) {
		final INexusDeviceService nexusDeviceService = ServiceHolder.getNexusDeviceService();
		if (nexusDeviceService.hasNexusDevice(name)) {
			return getNexusMetadataDevice(name, nexusDeviceService);
		} else if (nexusDeviceService.hasDecorator(name)) {
			return getNexusMetadataAppender(name, nexusDeviceService);
		} else {
			logger.debug("Cannot find {} from Nexus Device Service as Metadata Device or Decorator", name);
			return Optional.empty();
		}
	}

	private Optional<INexusMetadataDevice<NXobject>> getNexusMetadataDevice(String name,
			final INexusDeviceService nexusDeviceService) {
		try {
			final INexusDevice<NXobject> nexusDevice = nexusDeviceService.getNexusDevice(name);
			if (nexusDevice instanceof INexusMetadataDevice) {
				return Optional.ofNullable((INexusMetadataDevice<NXobject>) nexusDevice);
			} else {
				throw new IllegalStateException("Device '" + name
						+ "' retrieved from service is not a Nexus Metadata Device! Please use another name.");
			}
		} catch (NexusException e) {
			logger.error("Cannot find '{}' from Nexus Device Service as Metadata Device", name, e);
			return Optional.empty();
		}
	}

	private Optional<INexusMetadataDevice<NXobject>> getNexusMetadataAppender(String name,
			final INexusDeviceService nexusDeviceService) {
		try {
			INexusDeviceDecorator<NXobject> nexusDecorator = nexusDeviceService.getDecorator(name);
			if (nexusDecorator instanceof NexusMetadataAppender) {
				final Findable find = Finder.find(name);
				// Since we don't yet have the nexus object that would be appended when the nexus file is actually
				// created, we have to use a dummy nexus object, then we can see what nodes the appender would append to
				// it.
				NXobject nexusObject;
				if (find instanceof Detector) {
					nexusObject = NexusNodeFactory.createNXdetector();
				} else if (find instanceof Scannable scannable) {
					if (scannable.getInputNames().length == 1) {
						nexusObject = NexusNodeFactory.createNXpositioner();
					} else if (scannable.getInputNames().length == 0) {
						nexusObject = NexusNodeFactory.createNXsensor();
					} else {
						nexusObject = NexusNodeFactory.createNXcollection();
					}
				} else {
					// the default case
					nexusObject = NexusNodeFactory.createNXcollection();
				}
				final NexusObjectProvider<NXobject> nexusObjectProvider = new NexusObjectWrapper<>(
						nexusDecorator.getName(), nexusObject);
				final INexusDevice<NXobject> nexusDevice = new SimpleNexusDevice<>(nexusObjectProvider);
				nexusDecorator.setDecorated(nexusDevice);
				return Optional.ofNullable((NexusMetadataAppender<NXobject>) nexusDecorator);
			} else {
				throw new IllegalStateException("Decorator '" + name
						+ "' retrieved from service is not a Nexus Metadata Appender! Please use another name.");
			}
		} catch (NexusException e1) {
			logger.error("Cannot find '{}' from Nexus Device Service as Metadata Appender", name, e1);
			return Optional.empty();
		}
	}

	/**
	 * create a new {@link NexusMetadataDevice}
	 *
	 * @param name
	 * @param nexusClass
	 * @return an instance of {@link NexusMetadataDevice}
	 */
	public NexusMetadataDevice<NXobject> createNexusMetadataDevice(String name, String nexusClass) {
		final NexusMetadataDevice<NXobject> nexusMetadataDevice = new NexusMetadataDevice<>();
		nexusMetadataDevice.setName(name);
		nexusMetadataDevice.setNexusClass(nexusClass);
		// must register dynamically create INexusDevice so it can be accessed by data writer later
		ServiceHolder.getNexusDeviceService().register(nexusMetadataDevice);
		Services.getCommonBeamlineDevicesConfiguration().getAdditionalDeviceNames().add(name);
		userAddedNexusMetadataDevices.put(name, nexusMetadataDevice);
		return nexusMetadataDevice;
	}

	public Set<ImmutablePair<String, String>> getUserAddedFields() {
		return userAddedFields;
	}

	/**
	 * return a list node path for all field of all {@link INexusMetadataDevice}
	 *
	 * @return list of node paths
	 */
	public List<String> getFieldNodePathsFromAllNexusMetadataDevices() {
		final var commonBeamlineDevicesConfiguration = Services.getCommonBeamlineDevicesConfiguration();
		ThrowingFunction<String, List<String>> f = this::getNexusMetadataDeviceNodePaths;
		return commonBeamlineDevicesConfiguration.getCommonDeviceNames().stream()
				.map(f::apply).flatMap(List::stream)
				.toList();
	}

	private List<String> getNexusMetadataDeviceNodePaths(String deviceName) throws NexusException {
		List<String> deviceNodePaths = new ArrayList<>();
		final INexusDeviceService nexusDeviceService = ServiceHolder.getNexusDeviceService();
		final INexusDevice<NXobject> nexusDevice = nexusDeviceService.getNexusDevice(deviceName);
		INexusMetadataDevice<NXobject> nxMetadataDevice;
		if (nexusDevice instanceof INexusMetadataDevice) {
			nxMetadataDevice = (INexusMetadataDevice<NXobject>) nexusDevice;
			final NexusObjectProvider<NXobject> nexusProvider = nxMetadataDevice.getNexusProvider(null);
			final NXobject nexusObject = nexusProvider.getNexusObject();
			final String nexusNodePath = getNexusNodePath(nexusProvider, deviceName);
			deviceNodePaths.addAll(nexusObject.getDataNodeMap().entrySet().stream().map(e -> nexusNodePath + Node.SEPARATOR + e.getKey()).toList());
			deviceNodePaths.addAll(nexusObject.getGroupNodeMap().entrySet().stream().map(e -> processGroupNode(e.getKey(), e.getValue(), nexusNodePath))
			.flatMap(List::stream).toList());
		}
		return deviceNodePaths;
	}

	private List<String> processGroupNode(String name, GroupNode group, String nexusNodePath) {
		final String newNodePath = nexusNodePath + Node.SEPARATOR + name;
		if (group.getGroupNodeMap().isEmpty()) {
			return group.getDataNodeMap().entrySet().stream()
					.map(e -> newNodePath + Node.SEPARATOR + e.getKey()).toList();
		}
		return group.getGroupNodeMap().entrySet().stream().map(e -> processGroupNode(e.getKey(), e.getValue(), newNodePath))
				.flatMap(List::stream).toList();
	}

	/**
	 * A {@link Function} interface that converts the checked exception into runtime exception to work around
	 * the limitation of current lambda, stream functions.
	 *
	 * As a work around, it should only be used in place that the checked exception is not required to be handled.
	 *
	 * @param <T> the type of the input to the function
	 * @param <R> the type of the result of the function
	 */
	@FunctionalInterface
	private interface ThrowingFunction<T, R> extends Function<T, R> {
		@Override
		default R apply(T t) {
			try {
				return applyThrows(t);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		R applyThrows(T t) throws Exception;
	}
}

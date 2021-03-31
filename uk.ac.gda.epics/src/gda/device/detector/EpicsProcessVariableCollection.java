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

package gda.device.detector;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.detector.addetector.filewriter.MultipleHDF5PluginsPerDetectorFileWriter;
import gda.device.detector.addetector.filewriter.MultipleImagesPerHDF5FileWriter;
import gda.epics.connection.EpicsController;
import gda.factory.FindableBase;
import gda.jython.InterfaceProvider;
import gda.util.functions.ThrowingFunction;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

/**
 * This class is designed to collect additional data from a specified set of EPICS Processing Variables given in {@link #getName2PVNestedMap()},
 * {@link #getName2PVSimpleMap()}, and/or {@link #getFieldsToAppend()} with configurable GDA name for each PV, String constant and Spring SPEL Expression are
 * also supported in {@link #getFieldsToAppend()}.
 * <p>
 * These maps can be set using {@link #setName2PVNestedMap(Map)}, {@link #setName2PVSimpleMap(Map)}, and {@link #setFieldsToAppend(Map)}, respectively.
 * <p>
 * <ul>
 * <li>{@link #setName2PVNestedMap(Map)} supports map of maps which are used to generate {@link INexusTree} node with children nodes in
 * {@link #createPVCollectionNode(String)}.</li>
 * <li>{@link #getName2PVSimpleMap()} supports simple map which is used to generate {@link INexusTree} node with only fields, no children node in
 * {@link #createPVCollectionNode(String)}.</li>
 * <li>{@link #setFieldsToAppend(Map)} supports a multiple valued map and 3 different {@link InputType} ({@link InputType#CONSTANT}, {@link InputType#PV},
 * {@link InputType#EXPRESSION}), which is used to generate a map of configurable GDA name with its value for fields only.</li>
 * </ul>
 * <p>
 * <b>Important to know:</b>
 * <p>
 * Object of this class tolerant with any PV connection failure so it will not block data collection from others. Instead of throwing {@link RuntimeException}
 * when connection fails, it just returns value of "Not Available" in the data file as well as makes an ERROR log entry in the log file.
 * <p>
 * <b>Usage:</b>
 * <p>
 * Instance of this class can be injected into file writer {@link MultipleImagesPerHDF5FileWriter} or
 * {@link MultipleHDF5PluginsPerDetectorFileWriter} to contribute to the data collection. The original requirements are to capture active
 * detector setting parameters during data collection with the detector.
 *
 * @author Fajin Yuan
 * @since 23 March 2021
 * @version 9.21
 */
public class EpicsProcessVariableCollection extends FindableBase {

	private static final Logger logger = LoggerFactory.getLogger(EpicsProcessVariableCollection.class);
	private static final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	public enum InputType {

		CONSTANT {

			@Override
			public String apply(String s1) {
				return s1;
			}

		},

		PV {

			@Override
			public String apply(String s1) {
				try {
					Optional<Channel> option = getChannel(s1);
					if (option.isPresent()) {
						return EPICS_CONTROLLER.caget(option.get());
					}
				} catch (TimeoutException | CAException e) {
					logger.error("Failed to get data from {}", s1, e);
				} catch (InterruptedException e) {
					logger.error("Interrupted while getting data from {}", s1, e);
					Thread.currentThread().interrupt();
				}
				return ""; // failure get new value just return nothing - empty
			}

		},

		EXPRESSION {
			private transient ExpressionParser parser = new SpelExpressionParser();
			private transient StandardEvaluationContext context = new StandardEvaluationContext();

			@Override
			public String apply(String s1) {
				Expression exp = parser.parseExpression(s1);
				return exp.getValue(context, String.class);
			}

		};

		public abstract String apply(String s1);
	}

	/**
	 * Spring bean configurable map which maps user names to EPICS processing variable names
	 */
	private Map<String, String> name2PVSimpleMap = new HashMap<>();
	/**
	 * Spring bean property to configure if instance of this class use nested map or not
	 */
	private boolean nestedMap = false;
	/**
	 * Spring bean configurable map which maps group names to another map specifies user name as key and EPICS PV name as value
	 */
	private Map<String, Map<String, String>> name2PVNestedMap = new HashMap<>();
	/**
	 * a cached instance which maps EPICS PV to CA Channel created
	 */
	private static Map<String, Channel> channelMap = new HashMap<>();
	/**
	 * Spring bean configurable multi-valued map to be added as fields to existing node.
	 */
	private Map<String, List<ImmutablePair<InputType, String>>> fieldsToAppend = new HashMap<>();

	/**
	 * add specified filed with specified {@link InputType} and String value
	 *
	 * @param name
	 *            - field name, i.e. map key
	 * @param type
	 *            - {@link InputType} of corresponding value
	 * @param value
	 *            - the String of corresponding {@link InputType}
	 */
	public void addField(String name, InputType type, String value) {
		if (fieldsToAppend.containsKey(name)) {
			fieldsToAppend.get(name).add(new ImmutablePair<>(type, value));
		} else {
			fieldsToAppend.put(name, Arrays.asList(new ImmutablePair<>(type, value)));
		}
	}

	/**
	 * remove a specified field from the map
	 *
	 * @param name
	 *            - field name
	 * @return the value of the field being removed
	 */
	public List<ImmutablePair<InputType, String>> removeField(String name) {
		return fieldsToAppend.remove(name);
	}

	/**
	 * remove a specific {@link InputType} and String value pair from the specified name field
	 *
	 * @param name
	 *            - field name, i.e. map key
	 * @param type
	 *            - {@link InputType} of corresponding value
	 * @param value
	 *            - the String of corresponding {@link InputType}
	 */
	public void removeElementFromNamedField(String name, InputType type, String value) {
		fieldsToAppend.get(name).removeIf(pair -> pair.getLeft() == type && pair.getRight().equalsIgnoreCase(value));
	}

	/**
	 * add a Key value pair to the simple map returned by {@link #getName2PVSimpleMap()}
	 *
	 * @param name
	 *            - GDA name as key
	 * @param pvName
	 *            - PV name from which to get the value
	 */
	public void addToSimpleMap(String name, String pvName) {
		name2PVSimpleMap.put(name, pvName);
	}

	/**
	 * remove an item from the simple map returned by {@link #getName2PVSimpleMap()}
	 *
	 * @param name
	 *            - GDA name as key removed from map
	 * @return - the PV name removed from map
	 */
	public String removeFromSimpleMap(String name) {
		return name2PVSimpleMap.remove(name);
	}

	/**
	 * add a key value pair to a specified group in the nested map returned by {@link #getName2PVNestedMap()}
	 *
	 * @param group
	 *            - the key to the inner map
	 * @param name
	 *            - the GDA name as key of the inner map
	 * @param pvName
	 *            - the PV name as value of the inner map
	 */
	public void addToNestedMap(String group, String name, String pvName) {
		if (name2PVNestedMap.containsKey(group)) {
			name2PVNestedMap.get(group).put(name, pvName);
		} else {
			Map<String, String> innerMap = new HashMap<>();
			innerMap.put(name, pvName);
			name2PVNestedMap.put(group, innerMap);
		}
	}

	/**
	 * remove an item from the specified group of the nested map
	 *
	 * @param group
	 *            - key to the inner map
	 * @param name
	 *            - key of the inner map to be removed
	 * @return - the PB name removed from the inner map
	 */
	public String removeFromNestedMap(String group, String name) {
		if (name2PVNestedMap.containsKey(group)) {
			return name2PVNestedMap.get(group).remove(name);
		} else {
			throw new IllegalArgumentException("group '" + group + "' does not exist in this map!");
		}
	}

	/**
	 * remove a whole group from the nested map
	 *
	 * @param group
	 *            - group to be removed
	 * @return - the map of the removed group
	 */
	public Map<String, String> removeGroupFromNestedMap(String group) {
		return name2PVNestedMap.remove(group);
	}

	/**
	 * display simple map content on Jython Terminal.
	 */
	public void displaySimpleMap() {
		Optional.ofNullable(name2PVSimpleMap).orElseThrow().forEach((k, v) -> print(k + " : " + v));
	}

	/**
	 * display nested map content on Jython Terminal.
	 */
	public void displayNestedMap() {
		Optional.of(name2PVNestedMap).orElseThrow().forEach(this::print);
	}

	private void print(String k, Map<String, String> v) {
		print(k);
		v.forEach((x, y) -> print("\t" + x + " : " + y));
	}

	/**
	 * display fields to append on Jython Terminal.
	 */
	public void displayFieldsToAppend() {
		Optional.ofNullable(fieldsToAppend).orElseThrow().forEach(this::print);
	}

	private void print(String k, List<ImmutablePair<InputType, String>> v) {
		v.forEach(e -> print(k + " : InputType = " + e.getLeft() + ", value = " + e.getRight()));
	}

	/**
	 * create fields in a name-value map which can be added to other Nexus node directly. The data source comes from {@link #getFieldsToAppend()} which can be
	 * set with {@link #setFieldsToAppend(Map)}
	 *
	 * @return a map of name , value
	 */
	public Map<String, String> createFieldsToAppend() {
		return Optional.ofNullable(getFieldsToAppend()).orElseGet(Collections::emptyMap) // Null-Safe
				.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> processPairs(e.getValue())));
	}

	private String calculate(String s1, InputType operator) {
		return operator.apply(s1);
	}

	/**
	 * join the results of list together, if the result is blank, i.e. empty or just spaces, return constant "Not Available"
	 *
	 * @param pairs
	 * @return String
	 */
	private String processPairs(List<ImmutablePair<InputType, String>> pairs) {
		String value = Optional.ofNullable(pairs).map(Collection::stream).orElseGet(Stream::empty) // Null-Safe Stream
				.map(e -> calculate(e.getRight(), e.getLeft())).collect(Collectors.joining(", "));
		if (value.isBlank()) {
			return "Not Available";
		}
		return value;
	}

	/**
	 * create an {@link INexusTree} node as NXcollection from PV map specified in {@link #getName2PVNestedMap()} or {@link #getName2PVSimpleMap()}
	 *
	 * @param name
	 *            the name of the tree node
	 * @return an {@link INexusTree} node
	 */
	public INexusTree createPVCollectionNode(String name) {
		INexusTree node = new NexusTreeNode(name, NexusExtractor.NXCollectionClassName, null);
		if (isNestedMap()) {
			Optional.ofNullable(getName2PVNestedMap()).orElseGet(Collections::emptyMap) // Null-safe
					.entrySet().stream().map(e -> fillGroup(e.getValue(), createGroup(e.getKey()))).forEach(node::addChildNode);
		} else {
			Optional.ofNullable(getName2PVSimpleMap()).orElseGet(Collections::emptyMap) // Null-safe
					.entrySet().stream().map(this::createData).forEach(node::addChildNode);
		}
		return node;
	}

	private INexusTree createGroup(String name) {
		return new NexusTreeNode(name, NexusExtractor.NXCollectionClassName, null);
	}

	/**
	 * fill the specified group with data from map source
	 *
	 * @param map
	 * @param group
	 * @return
	 */
	private INexusTree fillGroup(Map<String, String> map, INexusTree group) {
		map.entrySet().stream().map(this::createData).forEach(group::addChildNode);
		return group;
	}

	/**
	 * build the data node and retrieve values from PVs, if PV access failed, return 'Not Available' instead.
	 *
	 * @param entry
	 * @return
	 */
	private INexusTree createData(Entry<String, String> entry) {
		Optional<Channel> option = getChannel(entry.getValue());
		NexusGroupData groupData = new NexusGroupData("Not Available");
		if (option.isPresent()) { // only collect data from PV when channel is present, otherwise return 'Not Available'
			Channel channel = option.get();
			try {
				if (channel.getFieldType().isDOUBLE()) {
					groupData = new NexusGroupData(EPICS_CONTROLLER.cagetDouble(channel));
				} else if (channel.getFieldType().isFLOAT()) {
					groupData = new NexusGroupData(EPICS_CONTROLLER.cagetFloat(channel));
				} else if (channel.getFieldType().isINT()) {
					groupData = new NexusGroupData(EPICS_CONTROLLER.cagetInt(channel));
				} else if (channel.getFieldType().isSHORT()) {
					groupData = new NexusGroupData(EPICS_CONTROLLER.cagetShort(channel));
				} else if (channel.getFieldType().isBYTE()) {
					groupData = new NexusGroupData(new String(EPICS_CONTROLLER.cagetByteArray(channel), StandardCharsets.UTF_8));
				} else if (channel.getFieldType().isENUM()) {
					groupData = new NexusGroupData(EPICS_CONTROLLER.cagetLabel(channel));
				} else {
					groupData = new NexusGroupData(EPICS_CONTROLLER.cagetString(channel));
				}
			} catch (TimeoutException | CAException e) {
				logger.error("Failed to get value from {}", entry.getValue(), e);
			} catch (InterruptedException e) {
				logger.error("Interrupted when getting value from {}", entry.getValue(), e);
				Thread.currentThread().interrupt();
			}
		}
		return new NexusTreeNode(entry.getKey(), NexusExtractor.SDSClassName, null, groupData);
	}

	/**
	 * Lazy initialize channels and store them in a map for retrieval later. Intentionally designed to cope with channel creation failure. This way it will not
	 * block data collection from other PVs.
	 *
	 * @param pv
	 * @return channel - Optional<Channel>
	 */
	private static Optional<Channel> getChannel(String pv) {
		ThrowingFunction<String, Channel> f = EPICS_CONTROLLER::createChannel;
		Channel channel = null;
		try {
			channel = channelMap.computeIfAbsent(pv, f);
			logger.trace("Created channel for PV: {}", pv);
		} catch (RuntimeException e) {
			logger.error("Error create CA Channel for {}", pv, e);
		}
		return Optional.ofNullable(channel);
	}

	public Map<String, Map<String, String>> getName2PVNestedMap() {
		return name2PVNestedMap;
	}

	/**
	 * set a nested map
	 *
	 * @param name2pvNestedMap
	 *            - map of maps
	 */
	public void setName2PVNestedMap(Map<String, Map<String, String>> name2pvNestedMap) {
		name2PVNestedMap = name2pvNestedMap;
	}

	public Map<String, String> getName2PVSimpleMap() {
		return name2PVSimpleMap;
	}

	/**
	 * set simple map
	 *
	 * @param name2pvSimpleMap
	 *            - map of GDA name to PV
	 */
	public void setName2PVSimpleMap(Map<String, String> name2pvSimpleMap) {
		name2PVSimpleMap = name2pvSimpleMap;
	}

	public boolean isNestedMap() {
		return nestedMap;
	}

	public void setNestedMap(boolean nestedMap) {
		this.nestedMap = nestedMap;
	}

	public Map<String, List<ImmutablePair<InputType, String>>> getFieldsToAppend() {
		return fieldsToAppend;
	}

	/**
	 * set a multi-valued map
	 *
	 * @param fieldsToAppend
	 */
	public void setFieldsToAppend(Map<String, List<ImmutablePair<InputType, String>>> fieldsToAppend) {
		this.fieldsToAppend = fieldsToAppend;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fieldsToAppend == null) ? 0 : fieldsToAppend.hashCode());
		result = prime * result + ((name2PVNestedMap == null) ? 0 : name2PVNestedMap.hashCode());
		result = prime * result + ((name2PVSimpleMap == null) ? 0 : name2PVSimpleMap.hashCode());
		result = prime * result + (nestedMap ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EpicsProcessVariableCollection other = (EpicsProcessVariableCollection) obj;
		if (fieldsToAppend == null) {
			if (other.fieldsToAppend != null)
				return false;
		} else if (!fieldsToAppend.equals(other.fieldsToAppend))
			return false;
		if (name2PVNestedMap == null) {
			if (other.name2PVNestedMap != null)
				return false;
		} else if (!name2PVNestedMap.equals(other.name2PVNestedMap))
			return false;
		if (name2PVSimpleMap == null) {
			if (other.name2PVSimpleMap != null)
				return false;
		} else if (!name2PVSimpleMap.equals(other.name2PVSimpleMap))
			return false;
		if (nestedMap != other.nestedMap)
			return false;

		return true;
	}

	private void print(String msg) {
		InterfaceProvider.getTerminalPrinter().print(msg);
	}

}

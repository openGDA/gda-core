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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.SymbolicNode;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * specify extra data items to be collected in a scan.
 *
 * These data items can be set in maps defined in Spring beans using following methods:
 * <p>
 * <ul>
 * <li>{@link #setName2PVNestedMap(Map)} - this supports nested map in which data from PVs are further grouped into children nodes under this group before adding into containing node.</li>
 * <li>{@link #setName2PVSimpleMap(Map)} - this supports simple map in which data from all PVs are grouped together in this group before adding into containing node.</li>
 * <li>{@link #setFieldsToAppend(Map)} - this supports a multiple valued map in which data from different type items are added directly to the containing node.
 * <li>{@link #setName2PairMap(Map)} - this supports a simple map in which data from different type items are grouped together before adding into containing node.
 * </ul>
 * All of these maps are optional, that is if it is not empty, data from PVs within the map will be collected, otherwise nothing will be collected in data file.
 * <p>
 * For the last 2 methods, there are 4 different {@link InputType} of items can be specified in Spring beans - ({@link InputType#CONSTANT}, {@link InputType#PV}, {@link InputType#LINK}, {@link InputType#EXPRESSION}).
 * <p>
 * <b>Important to know:</b>
 * <p>
 * Objects of this class tolerant with any PV access failure so it will not block data collection from others. Instead of throwing exception
 * when PV access fails, it returns the error message of the PV access problem as well as makes ERROR log entry in the log file.
 * <p>
 * <b>Usage:</b>
 * <p>
 * Instance of this class can be injected into file writer {@link MultipleImagesPerHDF5FileWriter} or {@link MultipleHDF5PluginsPerDetectorFileWriter} to
 * contribute to the data collection. The original requirements are to capture active detector setting parameters during data collection with the detector.
 *
 * @author Fajin Yuan
 * @since 23 March 2021
 * @version 9.21
 */
public class EpicsProcessVariableCollection extends FindableBase {

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
				return getChannel(s1).map(t -> {
					try {
						return EPICS_CONTROLLER.caget(t);
					} catch (TimeoutException | CAException e) {
						logger.error("Failed to get data from {}", s1, e);
						return e.getMessage();
					} catch (InterruptedException e) {
						logger.error("Interrupted while getting data from {}", s1, e);
						Thread.currentThread().interrupt();
						return e.getMessage();
					}
				}).orElse(String.format("Error create CA Channel for %s", s1));
			}
		},

		LINK {
			@Override
			public String apply(String link) {
				return link;
			}
		},

		EXPRESSION {
			private transient ExpressionParser parser = new SpelExpressionParser();
			private transient StandardEvaluationContext context = new StandardEvaluationContext();

			@Override
			public String apply(String s1) {
				var exp = parser.parseExpression(s1);
				return exp.getValue(context, String.class);
			}

		};

		public abstract String apply(String s1);
	}

	private static final Logger logger = LoggerFactory.getLogger(EpicsProcessVariableCollection.class);
	private static final String NOT_AVAILABLE = "Not Available";
	private static final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	/**
	 * a cached instance which maps EPICS PV to CA Channel created
	 */
	private static final Map<String, Channel> channelMap = new HashMap<>();

	/**
	 * Spring bean configurable map which maps user names to EPICS processing variable names
	 */
	private Map<String, String> name2PVSimpleMap = new HashMap<>();
	/**
	 * Spring bean configurable map which maps group names to another map specifies user name as key and EPICS PV name as value
	 */
	private Map<String, Map<String, String>> name2PVNestedMap = new HashMap<>();
	/**
	 * Spring bean configurable multi-valued map to be added as fields to existing node.
	 */
	private Map<String, List<ImmutablePair<InputType, String>>> fieldsToAppend = new HashMap<>();
	/**
	 * Spring bean configurable map which supports different {@link InputType}s.
	 */
	private Map<String, ImmutablePair<InputType, String>> name2PairMap = new HashMap<>();
	private String expectedFullFileName;

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
	 * add a Key value pair to the simple map
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
	 * remove an item from the simple map
	 *
	 * @param name
	 *            - GDA name as key removed from map
	 * @return - the PV name removed from map
	 */
	public String removeFromSimpleMap(String name) {
		return name2PVSimpleMap.remove(name);
	}

	/**
	 * add a key value pair to a specified group in the nested map
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
	 * return an {@link Optional} of a {@link Map} containing name-value maps which can be added to other Nexus node directly.
	 *
	 * @return a map of name , value
	 */
	public Optional<Map<String, String>> createFieldsToAppend() {
		return Optional.ofNullable(fieldsToAppend).map(e -> e.entrySet().stream().collect(Collectors.toMap(Entry::getKey, i -> processPairs(i.getValue()))));
	}

	public Optional<Map<String, DataNode>> createDataNodeToAppend() {
		return Optional.ofNullable(fieldsToAppend).map(e -> e.entrySet().stream().collect(Collectors.toMap(Entry::getKey, i -> createDataNode(i.getValue()))));
	}

	private DataNode createDataNode(List<ImmutablePair<InputType, String>> value) {
		var node = NexusNodeFactory.createDataNode();
		node.setDataset(new NexusGroupData(processPairs(value)).toDataset());
		return node;
	}

	private String calculate(String s1, InputType operator) {
		return operator.apply(s1);
	}

	/**
	 * return an {@link Optional} of an {@link INexusTree} which can be added to other Nexus node.
	 *
	 * @return a map of name , value
	 */
	public Optional<INexusTree> createNexusTreeFromName2PairMap(String name) {
		return createGroupNodeFromName2PairMap().map(e -> translate(name, e));
	}

	/**
	 * join the results of list together, if the result is blank, i.e. empty or just spaces, return constant "Not Available"
	 *
	 * @param pairs
	 * @return String
	 */
	private String processPairs(List<ImmutablePair<InputType, String>> pairs) {
		String value = Optional.ofNullable(pairs).map(Collection::stream).orElseGet(Stream::empty).map(e -> calculate(e.getRight(), e.getLeft()))
				.collect(Collectors.joining(", "));
		if (value.isBlank()) {
			return NOT_AVAILABLE;
		}
		return value;
	}

	/**
	 * create an {@Optional} of an {@link INexusTree} node as NXcollection from PV map specified in {@link #setName2PVNestedMap(Map)}
	 *
	 * @param name
	 *            the name of the tree node
	 * @return an {@link Optional} of an {@link INexusTree} node
	 */
	public Optional<INexusTree> createNexusTreeFromName2PVNestedMap(String name) {
		return createGroupNodeFromName2PVNestedMap().map(e -> translate(name, e));
	}

	/**
	 * create an {@Optional} of an {@link INexusTree} node as NXcollection from PV map specified in {@link #setName2PVSimpleMap(Map)}
	 *
	 * @param name
	 *            the name of the tree node
	 * @return an {@link Optional} of an {@link INexusTree} node
	 */
	public Optional<INexusTree> createNexusTreeFromName2PVSimpleMap(String name) {
		return createGroupNodeFromName2PVSimpleMap().map(e -> translate(name, e));
	}

	private INexusTree translate(String groupName, GroupNode node) {
		INexusTree tnode = new NexusTreeNode(groupName, NexusExtractor.NXCollectionClassName, null);
		Map<String, GroupNode> groupNodeMap = node.getGroupNodeMap();
		if (groupNodeMap.isEmpty()) {
			for (Entry<String, DataNode> data : node.getDataNodeMap().entrySet()) {
				try {
					tnode.addChildNode(new NexusTreeNode(data.getKey(), NexusExtractor.SDSClassName, null,
							NexusGroupData.createFromDataset(data.getValue().getDataset().getSlice(null, null, null))));
				} catch (DatasetException e) {
					logger.error("Cannot getSlice from dataset for {}", data.getKey(), e);
				}
			}
			for (String name : node.getSymbolicNodeNames()) {
				SymbolicNode symbolicNode = node.getSymbolicNode(name);
				tnode.addChildNode(new NexusTreeNode(name, NexusExtractor.ExternalSDSLink, null,
								new NexusGroupData("nxfile://"+symbolicNode.getSourceURI().getPath()+symbolicNode.getPath().replaceFirst("/entry", "#entry"))));
			}
		} else {
			for (Entry<String, GroupNode> group : groupNodeMap.entrySet()) {
				tnode.addChildNode(translate(group.getKey(), group.getValue()));
			}
		}
		return tnode;
	}

	/**
	 * return an {@link Optional} of an {@link INexusTree} which can be added to other Nexus node. The data source comes from {@link #setName2PairMap(Map)}
	 * which can be set with {@link #setName2PairMap(Map)}
	 *
	 * @return a map of name , value
	 */
	public Optional<GroupNode> createGroupNodeFromName2PairMap() {
		return Optional.of(name2PairMap).map(e -> createGroupNode2(e.entrySet()));
	}

	private GroupNode createGroupNode2(Set<Entry<String, ImmutablePair<InputType, String>>> entrySet) {
		var group = NexusNodeFactory.createNXobjectForClass(NexusBaseClass.NX_COLLECTION);
		entrySet.stream().map(e -> new ImmutablePair<>(e.getKey(), createNode(e.getValue()))).forEach(e -> group.addNode(e.getLeft(), e.getRight()));
		return group;
	}

	private Node createNode(ImmutablePair<InputType, String> pair) {
		Node node;
		if (pair.getLeft() == InputType.LINK) {
			try {
				node = NexusNodeFactory.createSymbolicNode(new URI(expectedFullFileName), pair.getRight());
				logger.debug("External entry path = {}, source URI = {}", ((SymbolicNode)node).getPath(), ((SymbolicNode)node).getSourceURI());
			} catch (URISyntaxException e) {
				logger.error("URI {} is not valid.", expectedFullFileName, e);
				// return error message instead throw exception so data collection will not stop
				node = NexusNodeFactory.createDataNode();
				((DataNode) node).setDataset(new NexusGroupData(e.getMessage()).toDataset());
			}
		} else {
			node = NexusNodeFactory.createDataNode();
			((DataNode) node).setDataset(new NexusGroupData(calculate(pair.getRight(), pair.getLeft())).toDataset());
		}
		return node;
	}

	/**
	 * create an {@link GroupNode} as NXcollection from PV map specified in {@link #setName2PVNestedMap(Map)} or {@link #setName2PVSimpleMap(Map)}. This is used
	 * by new NexusScanDataWriter.
	 *
	 * @return a group node
	 */
	public Optional<GroupNode> createGroupNodeFromName2PVNestedMap() {
		return Optional.of(name2PVNestedMap).map(e -> createGroupNode(e.entrySet()));
	}

	private GroupNode createGroupNode(Set<Map.Entry<String, Map<String, String>>> set) {
		var group = NexusNodeFactory.createNXobjectForClass(NexusBaseClass.NX_COLLECTION);
		set.stream().map(e -> new ImmutablePair<>(e.getKey(), createGroupNode(e.getValue()))).forEach(e -> group.addGroupNode(e.getLeft(), e.getRight()));
		return group;
	}

	public Optional<GroupNode> createGroupNodeFromName2PVSimpleMap() {
		return Optional.of(name2PVSimpleMap).map(this::createGroupNode);
	}

	private GroupNode createGroupNode(Map<String, String> map) {
		var group = NexusNodeFactory.createNXobjectForClass(NexusBaseClass.NX_COLLECTION);
		map.entrySet().stream().map(e -> new ImmutablePair<>(e.getKey(), createDataNode(e.getValue())))
				.forEach(e -> group.addDataNode(e.getLeft(), e.getRight()));
		return group;
	}

	private DataNode createDataNode(String value) {
		var dataNode = NexusNodeFactory.createDataNode();
		dataNode.setDataset(getChannel(value).map(this::getDataset).orElseGet(()->DatasetFactory.createFromObject(NOT_AVAILABLE)));
		return dataNode;
	}

	private IDataset getDataset(Channel channel) {
		try {
			Object data = EPICS_CONTROLLER.getValue(channel);
			return DatasetFactory.createFromObject(data, 1); //need to set shape to 1 otherwise single number will has shape of 0 which cause ArrayIndexOutOfBound in ArrayDescriptor class.
		} catch (TimeoutException | CAException e) {
			logger.error("Failed to get value from {}", channel.getName(), e);
		} catch (InterruptedException e) {
			logger.error("Interrupted when getting value from {}", channel.getName(), e);
			Thread.currentThread().interrupt();
		}
		return null;
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
			logger.error("Error create Channel Access for {}", pv, e);
		}
		return Optional.ofNullable(channel);
	}

	/**
	 * set a nested map
	 *
	 * @param name2pvNestedMap
	 *            - map of maps
	 */
	public void setName2PVNestedMap(Map<String, Map<String, String>> name2pvNestedMap) {
		name2PVNestedMap = Objects.requireNonNull(name2pvNestedMap);
	}

	/**
	 * set simple map
	 *
	 * @param name2pvSimpleMap
	 *            - map of GDA name to PV
	 */
	public void setName2PVSimpleMap(Map<String, String> name2pvSimpleMap) {
		name2PVSimpleMap = Objects.requireNonNull(name2pvSimpleMap);
	}

	/**
	 * set a multi-valued map
	 *
	 * @param fieldsToAppend
	 */
	public void setFieldsToAppend(Map<String, List<ImmutablePair<InputType, String>>> fieldsToAppend) {
		this.fieldsToAppend = Objects.requireNonNull(fieldsToAppend);
	}

	private void print(String msg) {
		InterfaceProvider.getTerminalPrinter().print(msg);
	}

	public void setName2PairMap(Map<String, ImmutablePair<InputType, String>> name2PairMap) {
		this.name2PairMap = Objects.requireNonNull(name2PairMap);
	}

	@Override
	public int hashCode() {
		final var prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(fieldsToAppend, name2PVNestedMap, name2PVSimpleMap, name2PairMap);
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
		return Objects.equals(fieldsToAppend, other.fieldsToAppend) && Objects.equals(name2PVNestedMap, other.name2PVNestedMap)
				&& Objects.equals(name2PVSimpleMap, other.name2PVSimpleMap) && Objects.equals(name2PairMap, other.name2PairMap);
	}

	public void setHDF5Filename(String expectedFullFileName) {
		this.expectedFullFileName = Objects.requireNonNull(expectedFullFileName);
	}
}

package gda.data.nexus.scan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.analysis.tree.impl.TreeFileImpl;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.dawnsci.nexus.impl.NXentryImpl;
import org.eclipse.dawnsci.nexus.impl.NXrootImpl;
import org.eclipse.dawnsci.nexus.impl.NexusNodeFactory;
import org.nexusformat.NexusException;

/**
 * A builder that can build a NeXus file, given a list of {@link NexusDevice}s.
 */
public class DefaultNexusFileBuilder implements NexusFileBuilder {

	private NexusNodeFactory nxObjectFactory = new NexusNodeFactory();

	private List<NexusDevice<?>> nexusDevices = null;

	private Map<NexusDevice<?>, GroupNode> deviceBaseClassInstances;

	private String filePath;

	private NXroot rootNode;

	private TreeFileImpl treeFile;

	private Map<String, String> dataLinks = new HashMap<>();

	public DefaultNexusFileBuilder() {
	}

	@Override
	public void setNexusDevices(List<NexusDevice<?>> nexusDevices) {
		this.nexusDevices = nexusDevices;
		this.deviceBaseClassInstances = new HashMap<>(nexusDevices.size());
	}

	public List<NexusDevice<?>> getNexusDevices() {
		return nexusDevices;
	}

	@Override
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public TreeFile buildNexusFile() throws Exception {
		// check that the builder has been initialised with the file path and nexus devices
		if (filePath == null) {
			throw new IllegalStateException("Nexus path not set");
		}

		List<NexusDevice<?>> nexusDevices = getNexusDevices();
		if (nexusDevices == null || nexusDevices.isEmpty()) {
			throw new IllegalStateException("No nexus devices set");
		}

		// build the nexus tree
		rootNode = createNexusTree();

		// add the root node to a new TreeFile object
		treeFile = nxObjectFactory.createTreeFile(filePath);
		treeFile.setGroupNode(rootNode);

		return treeFile;
	}

	/**
	 * Returns the built nexus file as a {@link TreeFile}.
	 *
	 * @return built nexus file
	 */
	public TreeFile getNexusTree() {
		return treeFile;
	}

	/**
	 * Returns the NXsample group within the NXentry.
	 *
	 * @return NXsample group
	 */
	public NXsample getSample() {
		// TODO: only gets sample from first entry
		return rootNode.getEntry().getSample();
	}

	/**
	 * Returns the NXinstrument group within the NXentry.
	 *
	 * @return NXinstrument group
	 */
	public NXinstrument getInstrument() {
		// TODO: only gets sample from first entry
		return rootNode.getEntry().getInstrument();
	}

	/**
	 * Creates a link from the NXdata node within an entry to the data node with the given path relative to the entry
	 *
	 * @param name
	 * @param path
	 */
	public void addDataLink(String name, String path) {
		dataLinks.put(name, path);
	}

	protected NXroot createNexusTree() throws Exception {
		NXrootImpl root = nxObjectFactory.createNXroot();
		root.setAttributeFile_name(filePath);

		NXentryImpl entry = nxObjectFactory.createNXentry();
		root.setEntry(entry);

		createSkeletonEntry(entry);
		addBaseClassesInstancesForDevices(entry);
		configureApplicationDefinitions(entry);
		configureDataGroup(entry);
		performAdditionalActions(entry);
		// TODO other standard groups?

		return root;
	}

	private void configureApplicationDefinitions(final NXentryImpl entry) {
		for (NexusDevice<?> device : getNexusDevices()) {

		}
	}

	/**
	 * @param entry
	 * @throws NexusException
	 */
	protected void configureDataGroup(NXentryImpl entry) throws NexusException {
		NXdata data = nxObjectFactory.createNXdata();
		entry.setData(data);

		for (Map.Entry<String, String> dataLinkEntry : dataLinks.entrySet()) {
			final String name = dataLinkEntry.getKey();
			final String relativePath = dataLinkEntry.getValue(); // path relative to entry
			NodeLink nodeLink = entry.findNodeLink(dataLinkEntry.getValue());
			if (nodeLink == null) {
				throw new NexusException("No node exists within entry with relative path: " + relativePath);
			}
			if (!nodeLink.isDestinationData()) {
				throw new NexusException("The node at the given relative path is not a data node: " + relativePath);
			}

			data.addDataNode(name, (DataNode) nodeLink.getDestination());
		}
	}

	/**
	 * Perform additional actions to build the NeXus entry.
	 * Subclasses should override this to perform add any additional child groups
	 * or data nodes as required.
	 * @param entry
	 */
	@SuppressWarnings("unused")
	protected void performAdditionalActions(NXentryImpl entry) {
		// default implementation does nothing
	}

	protected void addBaseClassesInstancesForDevices(NXentryImpl entry) throws Exception {
		final NXinstrument instrument = entry.getInstrument();
		final NXsample sample = entry.getSample();

		for (NexusDevice<?> nexusDevice : nexusDevices) {
			final String deviceName = nexusDevice.getName();
			if (deviceName == null || deviceName.isEmpty()) {
				throw new NullPointerException("Device name must be specified");
			}
			NXobject baseClassInstance = nexusDevice.createBaseClassInstance(nxObjectFactory);
			deviceBaseClassInstances.put(nexusDevice, baseClassInstance);
			switch (nexusDevice.getDeviceType()) {
			case INSTRUMENT:
				instrument.addGroupNode(deviceName, baseClassInstance);
				break;
			case SAMPLE:
				sample.addGroupNode(deviceName, baseClassInstance);
				break;
			default:
				throw new IllegalArgumentException("Unknown device type " + nexusDevice.getDeviceType());
			}
		}
	}

	protected NXinstrument createSkeletonEntry(NXentryImpl entry) {
		NXinstrument instrument = nxObjectFactory.createNXinstrument();
		entry.setInstrument(instrument);

		NXsample sample = nxObjectFactory.createNXsample();
		entry.setSample(sample);

		return instrument;
	}

}

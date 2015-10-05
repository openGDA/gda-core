package gda.data.nexus.scan;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.analysis.tree.impl.TreeFileImpl;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.dawnsci.nexus.impl.NXentryImpl;
import org.eclipse.dawnsci.nexus.impl.NXobjectFactory;
import org.eclipse.dawnsci.nexus.impl.NXrootImpl;

public class DefaultNexusFileBuilder implements NexusFileBuilder {

	private NXobjectFactory nxObjectFactory = new NXobjectFactory();

	private List<NexusDevice<?>> nexusDevices = null;

	private String filePath;

	private NXroot rootNode;

	private TreeFileImpl treeFile;

	public DefaultNexusFileBuilder() {
	}

	@Override
	public TreeFile buildNexusFile() {
		if (filePath == null) {
			throw new IllegalStateException("Nexus path not set");
		}
		if (nexusDevices == null || nexusDevices.isEmpty()) {
			throw new IllegalStateException("No nexus devices set");
		}

		treeFile = nxObjectFactory.createTreeFile(filePath);
		rootNode = createNexusTree();
		treeFile.setGroupNode(rootNode);

		return treeFile;
	}

	public TreeFile getNexusTree() {
		return treeFile;
	}

	public NXsample getSample() {
		// TODO: only gets sample from first entry
		return rootNode.getEntry().getSample();
	}

	public NXinstrument getInstrument() {
		// TODO: only gets sample from first entry
		return rootNode.getEntry().getInstrument();
	}

	protected NXroot createNexusTree() {
		NXrootImpl root = nxObjectFactory.createNXroot();
		root.setAttributeFile_name(filePath);
		// TODO set other attributes: nexus version, hdf5 version, etc
//		root.setAttributeFile_time(

		NXentryImpl entry = nxObjectFactory.createNXentry();
		root.setEntry(entry);

		createSkeletonEntry(entry);
		addBaseClassesInstancesForDevices(entry);
		performAdditionalActions(entry);

		// TODO data group?
		// TODO other standard groups?

		return root;
	}

	/**
	 * Perform additional actions to build the NeXus entry.
	 * Subclasses should override this to perform add any additional child groups
	 * or data nodes as required.
	 * @param entry
	 */
	protected void performAdditionalActions(NXentry entry) {
		// default implementation does nothing
	}

	protected void addBaseClassesInstancesForDevices(NXentryImpl entry) {
		final NXinstrument instrument = entry.getInstrument();
		final NXsample sample = entry.getSample();

		for (NexusDevice<?> nexusDevice : nexusDevices) {
			final String deviceName = nexusDevice.getName();
			if (deviceName == null || deviceName.isEmpty()) {
				throw new NullPointerException("Device name must be specified");
			}
			NXobject baseClassInstance = nexusDevice.createBaseClassInstance(nxObjectFactory);
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

		NXdata data = nxObjectFactory.createNXdata();
		entry.setData(data);
		// TODO how to get the data:

		return instrument;
	}

	@Override
	public void setNexusDevices(List<NexusDevice<?>> nexusDevices) {
		this.nexusDevices = nexusDevices;
	}

	@Override
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}


}

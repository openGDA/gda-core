package gda.data.nexus.scan;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.tree.TreeFile;

/**
 * A NeXus file builder knows how to build a NeXus file for a given scan.
 */
public interface NexusFileBuilder {

	/**
	 * Add the NeXus devices to the builder
	 *
	 * @param devices
	 */
	public void setNexusDevices(List<NexusDevice<?>> devices);

	/**
	 * Set the path of the NeXus file.
	 * @param filepath
	 */
	public void setFilePath(String filepath);

	/**
	 * Build the NeXus file.
	 *
	 * @return nexus file
	 * @throws Exception
	 */
	public TreeFile buildNexusFile() throws Exception;

}

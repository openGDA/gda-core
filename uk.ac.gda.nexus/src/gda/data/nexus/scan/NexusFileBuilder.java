package gda.data.nexus.scan;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.tree.TreeFile;

/**
 * A nexus file builder knows how to build a nexus file for a given scan.
 */
public interface NexusFileBuilder {

	public void setNexusDevices(List<NexusDevice<?>> devices);

	public void setFilePath(String filepath);

	public TreeFile buildNexusFile();

}

package gda.data.scan.datawriter.scannablewriter;

import gda.data.scan.datawriter.SelfCreatingLink;

import java.util.Collection;

import org.nexusformat.NeXusFileInterface;
import org.nexusformat.NexusException;

/**
 * This interface allows writing of a single input or extra element of a scannable
 */
public interface ComponentWriter {
	
	public Collection<SelfCreatingLink> makeComponent(NeXusFileInterface file, int[] dim, String path,
			String scannableName, String componentName, Object pos, String unit) throws NexusException;

	public void writeComponent(NeXusFileInterface file, int[] start, String path, String scannableName,
			String componentName, Object pos) throws NexusException;
}
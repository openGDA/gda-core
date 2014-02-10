package gda.data.scan.datawriter.scannablewriter;

import gda.data.scan.datawriter.SelfCreatingLink;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.Vector;

import org.nexusformat.NeXusFileInterface;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultComponentWriter implements ComponentWriter {
	private static final Logger logger = LoggerFactory.getLogger(DefaultComponentWriter.class);

	private int levels = 0;
	
	public DefaultComponentWriter() { }

	/**
	 * Set the file into the position to write the data
	 * 
	 * @param file
	 * @return name of trailing component
	 * @throws NexusException
	 */
	protected String enterLocation(NeXusFileInterface file, String path) throws NexusException {
		levels = 0;
		StringTokenizer st = new StringTokenizer(path, "/");
		while (st.hasMoreTokens()) {
			String[] split = st.nextToken().split(":");
			String name = split[0];
			if (split.length == 1) {
				// no class, write data
				return name;
			}
			String clazz = split[1];
			try {
				file.makegroup(name, clazz);
			} catch (NexusException ne) {
				// ignore, it might be there already
			}
			file.opengroup(name, clazz);
			levels++;
		}

		throw new IllegalArgumentException(
				"configured path is not well formed (suspect it has no trailing component)");
	}

	protected void leaveLocation(NeXusFileInterface file) throws NexusException {
		for (int i = 0; i < levels; i++) {
			file.closegroup();
		}
	}

	protected int[] nulldimfordim(int[] dim) {
		int[] mdim = new int[dim.length];
		for (int i = 0; i < mdim.length; i++) {
			mdim[i] = 0;
		}
		return mdim;
	}

	protected int[] slabsizedimfordim(int[] dim) {
		int[] mdim = new int[dim.length];
		for (int i = 0; i < mdim.length; i++) {
			mdim[i] = 1;
		}
		return mdim;
	}

	protected int[] makedatadimfordim(int[] dim) {
		int[] mdim = new int[dim.length];
		for (int i = 0; i < mdim.length; i++) {
			mdim[i] = -1;
		}
		return mdim;
	}
	
	protected int[] putslabdimfordim(int[] dim) {
		return dim;
	}

	protected Object getComponentSlab(Object pos) {
		Object poso = pos;
		return new double[] { (Double) poso };
	}

	@Override
	public Collection<SelfCreatingLink> makeComponent(NeXusFileInterface file, int[] dim, String path,
			String scannableName, String componentName, Object pos, String unit) throws NexusException {
		Vector<SelfCreatingLink> sclc = new Vector<SelfCreatingLink>();

		String name = enterLocation(file, path);

		int[] makedatadim = makedatadimfordim(dim);
		file.makedata(name, NexusFile.NX_FLOAT64, makedatadim.length, makedatadim);
		file.opendata(name);
		file.putattr("local_name", String.format("%s.%s", scannableName, componentName).getBytes(),
				NexusFile.NX_CHAR);

		String axislist = "1";
		for (int j = 2; j <= dim.length; j++) {
			axislist = axislist + String.format(",%d", j);
		}
		file.putattr("axis", axislist.getBytes(), NexusFile.NX_CHAR);
		if (unit != null && !unit.isEmpty())
			file.putattr("units", unit.getBytes(Charset.forName("UTF-8")), NexusFile.NX_CHAR);
		addCustomAttributes(file, scannableName, componentName);
		file.putslab(getComponentSlab(pos), nulldimfordim(dim), slabsizedimfordim(dim));

		sclc.add(new SelfCreatingLink(file.getdataID()));
		file.closedata();

		leaveLocation(file);

		return sclc;
	}

	/**
	 * To allow overriding classes to add random attributes if required
	 * the file will have the data set "open" ready to receive putattr calls
	 * @param file nexus file
	 * @param scannableName name of Scannable
	 * @param componentName extra or input name being written
	 * @throws NexusException 
	 */
	@SuppressWarnings("unused")
	protected void addCustomAttributes(NeXusFileInterface file, String scannableName, String componentName) throws NexusException { }

	@Override
	public void writeComponent(NeXusFileInterface file, int[] start, String path, String scannableName,
			String componentName, Object pos) throws NexusException {
		String name = enterLocation(file, path);

		file.opendata(name);
		try {
			file.putslab(getComponentSlab(pos), putslabdimfordim(start), slabsizedimfordim(start));
		} catch (Exception e) {
			logger.error("error converting scannable data", e);
		}
		file.closedata();

		leaveLocation(file);
	}
}
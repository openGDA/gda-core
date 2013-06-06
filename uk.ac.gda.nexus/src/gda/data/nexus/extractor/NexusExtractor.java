/*-
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

package gda.data.nexus.extractor;

import gda.data.nexus.NexusFileWrapper;
import gda.data.nexus.extractor.INexusTreeProcessor.RESPONSE;
import gda.data.nexus.tree.INexusSourceProvider;
import gda.data.nexus.tree.INexusTree;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.nexusformat.NeXusFileInterface;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
/**
 * see the package-info.java file for information on this class
 */
final public class NexusExtractor implements INexusDataGetter {
	private static final Logger logger = LoggerFactory.getLogger(NexusExtractor.class);

	public final static String NXDataClassName = "NXdata";
	public final static String NXEntryClassName = "NXentry";
	public final static String NXInstrumentClassName = "NXinstrument";
	public final static String NXPositionerClassName = "NXpositioner";
	public final static String NXNoteClassName = "NXnote";
	public final static String NXDetectorClassName = "NXdetector";
	public final static String NXMonitorClassName = "NXmonitor";
	public final static String NXCollectionClassName = "NXcollection";
	public final static String SDSClassName = "SDS";
	public final static String AttrClassName = "Attr";
	public static final String ExternalSDSLink = "ExternalSDSLink";
	public final static int DATAWANTED_NX_CHAR = 1;
	public final static int DATAWANTED_NX_FLOAT64 = 2;
	public final static String topClass = "";
	public final static String topName = "";


	private Group currentGroupBeingProcessed = null;

	private NeXusFileInterface file = null;

	final String fileName;

	private INexusTreeProcessor loopProcessor;

	/**
	 * @param dimensions
	 *            should use calcTotalLengthLong as total size may be too big for int
	 * @return the size of a buffer needed to hold the data in an SDS block
	 */
	public static int calcTotalLength(int[] dimensions) {
		int totalLength = 1;
		for (int dimension : dimensions) {
			totalLength *= dimension;
		}
		return totalLength;
	}

	public static long calcTotalLengthLong(int[] dimensions) {
		long totalLength = 1;
		for (int dimension : dimensions) {
			totalLength *= dimension;
		}
		return totalLength;
	}

	/**
	 * @param fileName
	 */
	public NexusExtractor(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @param attrName
	 *            name of attribute e.g. target
	 * @return value of target attribute if present else null
	 * @throws NexusException
	 * @throws NexusExtractorException
	 */
	@Override
	@SuppressWarnings("unchecked")
	public NexusGroupData getAttributeOfCurrentProcessedGroup(String attrName) throws NexusException,
			NexusExtractorException {
		NexusGroupData val = null;
		// try to open the attribute
		try {
			file.opendata(currentGroupBeingProcessed.name);
			Map<String, org.nexusformat.AttributeEntry> dir = file.attrdir();
			Set<Entry<String, org.nexusformat.AttributeEntry>> set = dir.entrySet();
			for (Entry<String, org.nexusformat.AttributeEntry> entry : set) {
				Attr attr = new Attr(entry.getKey(), entry.getValue());
				if (attr.getName().equals(attrName)) {
					Group currentGroupBeingProcessed_tmp = currentGroupBeingProcessed;
					currentGroupBeingProcessed = attr;
					val = getDataForCurrentProcessedGroup(attr.getName(), attr.getNXclass(), true);
					currentGroupBeingProcessed = currentGroupBeingProcessed_tmp;
					break;
				}
			}
		} finally {
			file.closedata();
		}
		return val;
	}

	/**
	 * @param name
	 * @param nxClass
	 * @param getData
	 * @return @see gda.data.nexus.extractor.NexusTreeBuilder
	 * @throws NexusException
	 * @throws NexusExtractorException
	 */
	@Override
	public NexusGroupData getDataForCurrentProcessedGroup(String name, String nxClass, boolean getData)
			throws NexusException, NexusExtractorException {
		if (currentGroupBeingProcessed == null || !currentGroupBeingProcessed.getName().equals(name)
				|| !currentGroupBeingProcessed.getNXclass().equals(nxClass)) {
			throw new NexusExtractorException("getDataForCurrentProcessedGroup being called out of sequence");
		}
		boolean isAttr = currentGroupBeingProcessed instanceof Attr;
		int type = 0;
		int length = 0;
		if (isAttr) {
			type = ((Attr) currentGroupBeingProcessed).entry.type;
			length = ((Attr) currentGroupBeingProcessed).entry.length;
		}
		return getNexusGroupData(file, currentGroupBeingProcessed.name, name, getData, isAttr, type, length, null, null);
	}

	static NexusGroupData getNexusGroupData(NeXusFileInterface file, String currentGroupName, String attrName,
			boolean getData, boolean isAttr, int AttrType, int AttrLength, int[] startPos, int[] dims)
			throws NexusException, NexusExtractorException {

		if (!isAttr) {
			file.opendata(currentGroupName);
		}

		try {
			int[] infoDims = new int[10];
			int[] infoArgs = new int[10];
			if (isAttr) {
				infoArgs[0] = 1;
				infoArgs[1] = AttrType;
				infoDims[0] = AttrLength;
				/*
				 * add on 1 extra byte for the null in the attribute string hdfdump shows that we simply put the
				 * attribute using the code: file.putattr(name, (String)value.getBytes(), NexusFile.NX_CHAR); but the
				 * attribute length does not account the byte for the null
				 */

				if (infoArgs[1] == NexusFile.NX_CHAR) {
					infoDims[0] += 1;
				}
			} else {
				file.getinfo(infoDims, infoArgs);
			}

			int rank = infoArgs[0];
			int[] dimensions = new int[rank];
			for (int i = 0; i < rank; i++) {
				dimensions[i] = infoDims[i];
			}
			int type = infoArgs[1];

			int lengthToSend = 0;

			long totalLength = calcTotalLengthLong(dimensions);
			/* if a particular slab is request then check for sanity and change dimensions and length to that value */
			if (!isAttr && startPos != null && dims != null) {
				/* check sanity */
				if (startPos.length != dims.length || startPos.length != dimensions.length) {
					throw new NexusExtractorException("startPos or dimensions of different rank than dataset");
				}
				// use new dimensions
				dimensions = dims;
				long lengthRequested = calcTotalLengthLong(dimensions);
				if (lengthRequested > totalLength) {
					throw new NexusExtractorException("requested chunk extends over dataset boundaries");
				}
				totalLength = lengthRequested;
			}

			if (!getData || totalLength <= 0) {
				return new NexusGroupData(dimensions, type, null);
			}

			if (totalLength > Integer.MAX_VALUE) {
				throw new NexusExtractorException("data size requested too big for java arrays");
			}

			lengthToSend = (int) totalLength;

			Serializable data = null;
			
			switch (type) {
			case NexusFile.NX_CHAR:
			case NexusFile.NX_INT8:
			case NexusFile.NX_UINT8:
				data = new byte[lengthToSend];
				break;
			case NexusFile.NX_INT16:
			case NexusFile.NX_UINT16:
				data = new short[lengthToSend];
				break;
			case NexusFile.NX_INT32:
			case NexusFile.NX_UINT32:
				data = new int[lengthToSend];
				break;
			case NexusFile.NX_INT64:
			case NexusFile.NX_UINT64:
				data = new long[lengthToSend];
				break;
			case NexusFile.NX_FLOAT32:
				data = new float[lengthToSend];
				break;
			case NexusFile.NX_FLOAT64:
				data = new double[lengthToSend];
				break;
			}
			
			if (data == null) {
				throw new NexusExtractorException("Unable to getdata for " + currentGroupName + " as type "
						+ Integer.toString(type) + " is not supported");
			}
			
			try {
				if (isAttr) {
					int arg2[] = new int[2];
					arg2[0] = lengthToSend;
					arg2[1] = type;
					file.getattr(attrName, data, arg2);
					// remove the 1 extra bytes added on for the null in the attribute string
					if (infoArgs[1] == NexusFile.NX_CHAR) {
						dimensions[0] -= 1;
						data = Arrays.copyOf((byte[]) data, dimensions[0]);
					}
				} else {
					if (startPos != null && dims != null) {
						file.getslab(startPos, dimensions, data);
					} else {
						file.getdata(data);
					}
				}
				return new NexusGroupData(dimensions, type, data);
			} catch (NexusException ex) {
				logger.error("Error gettting data. totalLength=" + totalLength + " type = " + type);
				throw ex;
			}
		} finally {
			if (!isAttr) {
				file.closedata();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private RESPONSE loop(Group group, final IMonitor mon) throws NexusException, NexusExtractorException {

		if (mon != null) {
			if (mon.isCancelled()) {
				return RESPONSE.NO_MORE;
			}
		}

		// by default loop into the children on the current item
		RESPONSE response = RESPONSE.GO_INTO;
		if (group.name.equals("UNKNOWN") && group.NXclass.equals("UNKNOWN")) {
			// if unexpected value skip over
			response = RESPONSE.SKIP_OVER;
		} else {
			// set pointer to current group that is used if getDataForCurrentProcessedGroup is called
			currentGroupBeingProcessed = group;
			// tell processor of new item and get response to indicate the interest in it and its children
			response = loopProcessor.beginElement(group.name, group.NXclass, this);
			// clear pointer to current group as getDataForCurrentProcessedGroup should only be called during processing
			// of beginElement
			currentGroupBeingProcessed = null;
			if (response == RESPONSE.NO_MORE || response == RESPONSE.SKIP_OVER || response == RESPONSE.SDS_ATTR) {
				// If the processor has dealt with this item and no-more items are needed or the current item is to be
				// skipped then
				// simply escape. This is also required if the item is an SDS which can have attributes and the
				// processor wishes to be
				// passed the attributes
				if (response == RESPONSE.SDS_ATTR) {
					// SDS element is open so now iterate over attributes and then send endElement
					// The processor does not allow the attribute to be a parent so simply send endElement after all
					// have been processed

					file.opendata(group.name);
					try {
						Map<String, org.nexusformat.AttributeEntry> dir = file.attrdir();
						Set<Entry<String, org.nexusformat.AttributeEntry>> set = dir.entrySet();
						for (Entry<String, org.nexusformat.AttributeEntry> entry : set) {
							RESPONSE response2 = loop(new Attr(entry.getKey(), entry.getValue()), mon);
							if (response2 == RESPONSE.NO_MORE) {
								response = RESPONSE.NO_MORE;
								break;
							}
						}
					} finally {
						// signal to the processor that the SDS_ATTR have finished
						loopProcessor.endElement(); // e
						file.closedata();
					}
				}
				// always return skip which causes processing of current item to stop, To stop processing all together
				// the processor can send
				// NO_MORE for all future beginElements.
				return RESPONSE.SKIP_OVER;
			}
		}
		// if top group which was added artificially by this processing and is not in the file or there is no dataset
		if (!(group.name.isEmpty() && group.NXclass.isEmpty()) && !group.containsSDS()) {
			file.opengroup(group.name, group.NXclass);
		}
		try {
			// We need to get the sets of groups and attr after first opending the file - otherwise we can get a Nexus
			// invalid type exception
			Map<String, String> groupdir = file.groupdir();
			Map<String, org.nexusformat.AttributeEntry> attrdir = file.attrdir();
			{
				// iterate over all elements of the node
				Set<Entry<String, String>> set = groupdir.entrySet();
				for (Entry<String, String> entry : set) {
					RESPONSE response2 = loop(new Group(entry.getKey(), entry.getValue()), mon);
					if (response2 == RESPONSE.NO_MORE) {
						response = RESPONSE.NO_MORE;
						break;
					}
				}
			}
			{
				// iterate over all attributes of the node.
				Set<Entry<String, org.nexusformat.AttributeEntry>> set = attrdir.entrySet();
				for (Entry<String, org.nexusformat.AttributeEntry> entry : set) {
					RESPONSE response2 = loop(new Attr(entry.getKey(), entry.getValue()), mon);
					if (response2 == RESPONSE.NO_MORE) {
						response = RESPONSE.NO_MORE;
						break;
					}
				}
			}
		} catch (NexusException e) {
			logger.info(e.getMessage() + " " + group.toString());
			throw e;
		} finally {
			if (!(group.name.isEmpty() && group.NXclass.isEmpty()) && !group.containsSDS()) {
				file.closegroup();
			}
			currentGroupBeingProcessed = null;
		}
		// tell processor that end of current node has been reached.
		loopProcessor.endElement();
		return response;
	}

	/**
	 * @param loopProcessor
	 * @param debug
	 *            - if true the file access is instrumented
	 * @throws NexusException
	 * @throws NexusExtractorException
	 */
	public void runLoop(INexusTreeProcessor loopProcessor, boolean debug) throws NexusException,
			NexusExtractorException {
		runLoop(loopProcessor, debug, null);
	}

	/**
	 * @param loopProcessor
	 * @param debug
	 *            - if true the file access is instrumented
	 * @param mon
	 * @throws NexusException
	 * @throws NexusExtractorException
	 */
	public void runLoop(INexusTreeProcessor loopProcessor, boolean debug, final IMonitor mon) throws NexusException,
			NexusExtractorException {
		this.loopProcessor = loopProcessor;
		file = new NexusFile(fileName, NexusFile.NXACC_READ);
		if (debug) {
			file = new NexusFileWrapper(file);
		}
		try {
			loop(new Group(topName, topClass), mon);
		} finally {
			if (file != null) {
				file.close();
			}
			currentGroupBeingProcessed = null;
		}
	}

	@Override
	public URL getSourceId() throws MalformedURLException {
		return new URL("file:" + new File(fileName).getAbsolutePath());
	}

	/**
	 * @param source
	 *            source of data
	 * @param nodePathWithClasses
	 * @param startPos
	 *            start position of the slab (can be null)
	 * @param dims
	 *            slab dimensions (can be null)
	 * @param debug
	 *            true if the file api is to be logged
	 * @return NexusGroupData
	 * @throws NexusExtractorException
	 * @throws NexusException
	 */
	static synchronized public NexusGroupData getNexusGroupData(URL source, String nodePathWithClasses, int[] startPos,
			int[] dims, boolean debug) throws NexusException, NexusExtractorException {
		return new SimpleExtractor(source, nodePathWithClasses, startPos, dims, debug).getData();
	}

	/**
	 * @param node
	 * @param readIfNull
	 * @return the NexusGroupData from a node - reading from the file if possible and requested
	 * @throws NexusExtractorException
	 * @throws NexusException
	 */
	static public NexusGroupData getNexusGroupDataWithBuffer(INexusTree node, boolean readIfNull)
			throws NexusException, NexusExtractorException {
		if ((node.getData() == null || node.getData().getBuffer() == null) && readIfNull) {
			INexusTree top = node;
			while (top.getParentNode() != null) {
				top = top.getParentNode();
			}
			if (top instanceof INexusSourceProvider) {
				URL source = ((INexusSourceProvider) top).getSource();
				return NexusExtractor.getNexusGroupData(source, node.getNodePathWithClasses(), null, null,
						logger.isDebugEnabled());
			}
		}
		return node.getData();
	}

	/**
	 * @param node
	 * @param startPos
	 *            start position of the slab
	 * @param dims
	 *            slab dimensions
	 * @return the NexusGroupData from a node - reading from the file if possible and requested
	 * @throws NexusExtractorException
	 * @throws NexusException
	 */
	static public NexusGroupData getNexusGroupDataSlab(INexusTree node, int[] startPos, int[] dims)
			throws NexusExtractorException, NexusException {
		INexusTree top = node;
		while (top.getParentNode() != null) {
			top = top.getParentNode();
		}
		if (!(top instanceof INexusSourceProvider)) {
			throw new NexusExtractorException("top node is not INexusSourceProvider");
		}
		URL source = ((INexusSourceProvider) top).getSource();
		return NexusExtractor.getNexusGroupData(source, node.getNodePathWithClasses(), startPos, dims,
				logger.isDebugEnabled());
	}
}

class Group {
	final String name;
	final String NXclass;

	Group(String name, String NXclass) {
		this.name = name;
		this.NXclass = NXclass;
	}

	static Group getInstance(Group source) {
		return new Group(source.name, source.NXclass);
	}

	String getName() {
		return name;
	}

	String getNXclass() {
		return NXclass;
	}

	boolean containsSDS() {
		return getNXclass().equals(NexusExtractor.SDSClassName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((NXclass == null) ? 0 : NXclass.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Group)) {
			return false;
		}
		Group other = (Group) o;
		return name.equals(other.name) && NXclass.equals(other.NXclass);
	}

	@Override
	public String toString() {
		return name + "." + NXclass;
	}
}

class Attr extends Group {
	org.nexusformat.AttributeEntry entry;

	Attr(String name, org.nexusformat.AttributeEntry entry) {
		super(name, NexusExtractor.AttrClassName);
		this.entry = entry;
	}
}

class SimpleExtractor {

	URL source;
	String nodePathWithClasses;
	boolean debug;
	int[] startPos, dims;

	SimpleExtractor(URL source, String nodePathWithClasses, int[] startPos, int[] dims, boolean debug) {
		this.debug = debug;
		this.source = source;
		this.startPos = startPos;
		this.dims = dims;
		this.nodePathWithClasses = nodePathWithClasses;
	}

	/**
	 * @param file
	 * @param nodePathWithClasses
	 * @return node with given path
	 * @throws NexusException
	 * @throws NexusExtractorException
	 */
	@SuppressWarnings("unchecked")
	public NexusGroupData getData(NeXusFileInterface file, String nodePathWithClasses) throws NexusException,
			NexusExtractorException {
		String[] nodeIds = nodePathWithClasses.split("/", 3);
		if (!(nodeIds[0].equals("") && nodeIds[1].equals("")) && !(nodeIds[1].equals(NexusExtractor.SDSClassName))
				&& !(nodeIds[1].equals(NexusExtractor.AttrClassName))) {
			file.opengroup(nodeIds[0], nodeIds[1]);
		}
		if (nodeIds.length > 2 && !nodeIds[2].isEmpty()) {
			if (nodeIds[1].equals(NexusExtractor.SDSClassName)) {
				file.opendata(nodeIds[0]);
			}
			return getData(file, nodeIds[2]);
		}
		boolean isAttr = nodeIds[1].equals(NexusExtractor.AttrClassName);
		String attrName = nodeIds[0];
		int attrType = 0;
		int attrLength = 0;
		if (isAttr) {
			Map<String, org.nexusformat.AttributeEntry> dir = file.attrdir();
			Set<Entry<String, org.nexusformat.AttributeEntry>> set = dir.entrySet();
			for (Entry<String, org.nexusformat.AttributeEntry> entry : set) {
				if (entry.getKey().equals(attrName)) {
					org.nexusformat.AttributeEntry attrEntry = entry.getValue();
					attrType = attrEntry.type;
					attrLength = attrEntry.length;
					break;
				}
			}
		}
		return NexusExtractor.getNexusGroupData(file, nodeIds[0], attrName, true, isAttr, attrType, attrLength,
				startPos, dims);
	}

	protected final NexusGroupData getData() throws NexusException, NexusExtractorException {
		NeXusFileInterface file = new NexusFile(source.getPath(), NexusFile.NXACC_READ);
		if (debug) {
			file = new NexusFileWrapper(file);
		}
		try {
			return getData(file, nodePathWithClasses);
		} finally {
			file.close();
		}
	}
}
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

import gda.data.nexus.extractor.INexusTreeProcessor.RESPONSE;
import gda.data.nexus.tree.INexusSourceProvider;
import gda.data.nexus.tree.INexusTree;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.hdf5.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private NexusFile file = null;

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
	public NexusGroupData getAttributeOfCurrentProcessedGroup(String attrName) throws NexusException,
			NexusExtractorException {
		NexusGroupData val = null;
		// try to open the attribute
		DataNode d = file.getData((GroupNode) currentGroupBeingProcessed.parent, currentGroupBeingProcessed.name);
		Attribute a = d.getAttribute(attrName);
		if (a != null) {
			Group currentGroupBeingProcessed_tmp = currentGroupBeingProcessed;
			currentGroupBeingProcessed = new Attr(d, attrName, a);
			val = NexusGroupData.createFromDataset(a.getValue());
			currentGroupBeingProcessed = currentGroupBeingProcessed_tmp;
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
		if (currentGroupBeingProcessed instanceof Attr) {
			Attribute a = ((Attr) currentGroupBeingProcessed).entry;
			return NexusGroupData.createFromDataset(a.getValue());
		}
		DataNode d = file.getData((GroupNode) currentGroupBeingProcessed.parent, name);
		ILazyDataset l = d.getDataset();
		int[] shape = l.getShape();
		NexusGroupData n;
		if (getData) {
			n = NexusGroupData.createFromDataset(l.getSlice());
		} else {
			n = new NexusGroupData(shape, l.elementClass());
			if (d.isString()) {
				n.setMaxStringLength(d.getMaxStringLength());
			}
		}
		return n;
	}

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
					DataNode d = file.getData((GroupNode) group.parent, group.name);
					Iterator<? extends Attribute> it = d.getAttributeIterator();
					while (it.hasNext()) {
						Attribute a = it.next();
						RESPONSE response2 = loop(new Attr(d, a.getName(), a), mon);
						if (response2 == RESPONSE.NO_MORE) {
							response = RESPONSE.NO_MORE;
							break;
						}
					}
					// signal to the processor that the SDS_ATTR have finished
					loopProcessor.endElement(); // e
				}
				// always return skip which causes processing of current item to stop, To stop processing all together
				// the processor can send
				// NO_MORE for all future beginElements.
				return RESPONSE.SKIP_OVER;
			}
		}
		GroupNode c;
		if (group.name.equals(topName) && group.NXclass.equals(topClass)) {
			c = file.getGroup(Tree.ROOT, false);
		} else {
			c = file.getGroup((GroupNode) group.parent, group.name, group.NXclass, false);
		}
		try {
			// We need to get the sets of groups and attr after first opending the file - otherwise we can get a Nexus
			// invalid type exception
			Iterator<String> it = c.getNodeNameIterator();
			while (it.hasNext()) {
				String n = it.next();
				NodeLink l = c.getNodeLink(n);
				Node nn = l.getDestination();
				Attribute a = nn.getAttribute(NexusFile.NXCLASS);
				String nc = a == null ? "" : a.getFirstElement();
				if (nc.isEmpty() && l.isDestinationData()) {
					nc = SDSClassName;
				}
				RESPONSE response2 = loop(new Group(c, n, nc), mon);
				if (response2 == RESPONSE.NO_MORE) {
					response = RESPONSE.NO_MORE;
					break;
				}
			}
			Iterator<? extends Attribute> ait = c.getAttributeIterator();
			while (ait.hasNext()) {
				Attribute a = ait.next();
				if (NexusFile.NXCLASS.equals(a.getName()))
						continue; // skip NX_class

				RESPONSE response2 = loop(new Attr(c, a.getName(), a), mon);
				if (response2 == RESPONSE.NO_MORE) {
					response = RESPONSE.NO_MORE;
					break;
				}
			}
		} catch (NexusException e) {
			logger.info(e.getMessage() + " " + group.toString());
			throw e;
		} catch (Throwable e) {
			logger.info(e.getMessage() + " " + group.toString());
			throw e;
		} finally {
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
		try {
			file = NexusUtils.openNexusFileReadOnly(fileName);
			file.setDebug(debug);
			loop(new Group(null, topName, topClass), mon);
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
	 * @throws NexusException
	 */
	static synchronized public NexusGroupData getNexusGroupData(URL source, String nodePathWithClasses, int[] startPos,
 int[] dims, boolean debug)
			throws NexusException {
		return new SimpleExtractor(source, nodePathWithClasses, startPos, dims, debug).getData();
	}

	/**
	 * @param node
	 * @param readIfNull
	 * @return the NexusGroupData from a node - reading from the file if possible and requested
	 * @throws NexusException
	 */
	static public NexusGroupData getNexusGroupDataWithBuffer(INexusTree node, boolean readIfNull) throws NexusException {
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
	final Node parent;

	Group(Node parent, String name, String NXclass) {
		this.parent = parent;
		this.name = name;
		this.NXclass = NXclass;
	}

	static Group getInstance(Group source) {
		return new Group(source.parent, source.name, source.NXclass);
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
	Attribute entry;

	Attr(Node parent, String name, Attribute entry) {
		super(parent, name, NexusExtractor.AttrClassName);
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
		if (startPos != null && dims != null && startPos.length != dims.length) {
			throw new IllegalArgumentException("Non-null start position and dimensions must have same length");
		}
		this.nodePathWithClasses = nodePathWithClasses;
	}

	private String createAugmentedPath(String nodePathWithClasses) {
		while (nodePathWithClasses.startsWith("/"))
			nodePathWithClasses = nodePathWithClasses.substring(1);
		return split(new StringBuilder(), nodePathWithClasses).toString();
	}

	private StringBuilder split(StringBuilder path, String bits) {
		String[] nodes = bits.split("/", 3);
		path.append(Node.SEPARATOR);
		path.append(nodes[0]);
		path.append(NexusFile.NXCLASS_SEPARATOR);
		path.append(nodes[1]);
		if (nodes.length == 2 || nodes[2].isEmpty())
			return path;
		return split(path, nodes[2]);
	}

	/**
	 * @param file
	 * @param nodePathWithClasses
	 * @return node with given path
	 * @throws NexusException
	 */
	public NexusGroupData getData(NexusFile file, String nodePathWithClasses) throws NexusException {
		String augmentedPath = createAugmentedPath(nodePathWithClasses);
		String attrName = null;
		if (augmentedPath.endsWith(NexusExtractor.AttrClassName)) {
			int i = augmentedPath.lastIndexOf(Node.SEPARATOR);
			attrName = augmentedPath.substring(i + 1, augmentedPath.lastIndexOf(NexusFile.NXCLASS_SEPARATOR));
			augmentedPath = augmentedPath.substring(0, i);
		}

		DataNode data = file.getData(augmentedPath);
		if (attrName != null) {
			Attribute a = data.getAttribute(attrName);
			return NexusGroupData.createFromDataset(a.getValue());
		}

		ILazyDataset lazy = data.getDataset();
		SliceND slice = null;
		if (startPos != null || dims != null) {
			int[] start = startPos == null ? new int[lazy.getRank()] : startPos;
			if (start.length != lazy.getRank()) {
				throw new IllegalArgumentException("Start position must have length equal to dataset rank");
			}
			int[] stop;
			if (dims == null) {
				stop = lazy.getShape();
			} else {
				stop = new int[dims.length];
				for (int i = 0; i < dims.length; i++) {
					stop[i] = dims[i] + start[i];
				}
			}
			slice = SliceND.createSlice(lazy, start, stop);
		} else {
			slice = SliceND.createSlice(lazy, null, null);
		}

		return NexusGroupData.createFromDataset(lazy.getSlice(slice));
	}

	protected final NexusGroupData getData() throws NexusException {
		try (NexusFile file = NexusUtils.openNexusFileReadOnly(source.getPath())) {
			file.setDebug(debug);
			return getData(file, nodePathWithClasses);
		}
	}
}

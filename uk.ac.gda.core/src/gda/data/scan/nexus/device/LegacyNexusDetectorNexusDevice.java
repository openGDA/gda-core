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

package gda.data.scan.nexus.device;

import java.io.File;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.SymbolicNode;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.InterfaceUtils;
import org.eclipse.january.dataset.SliceND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.data.scan.datawriter.NexusDataWriter;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.NexusDetector;

/**
 * An instance of this class wraps a {@link NexusDetector} to implement {@link INexusDevice},
 *
 * This code is derived from {@link NexusDataWriter}.makeNexusDetector and changed as little as possible.
 * The chief difference with this code and NexusDataWriter is that this class creates a Nexus structure
 * purely in memory, whereas NexusDataWriter writes to disk.
 *
 * @deprecated this class has been replaced by {@link NexusDetectorNexusDevice}. This class is being
 * 	  kept for now as it is as similar to NexusDataWriter.makeNexusDetector as possible, so we can
 *    be reasonably sure that it works
 */
@Deprecated
public class LegacyNexusDetectorNexusDevice extends AbstractDetectorNexusDeviceAdapter {

	private static final Logger logger = LoggerFactory.getLogger(LegacyNexusDetectorNexusDevice.class);

	private int[] scanDimensions = null;
	private String nexusFileUrl = null;
	private int[] dataDimPrefix = null;// an array of same length as scanDimensions, filled with 1s, for convenience

	private int[] dataStartPosPrefix = null; // set at each point

	private NXdetector detectorGroup = null;

	private List<String> primaryFieldNames = null;

	private Set<String> externalFileNames = null;
	private Map<String, Integer> externalDatasetRanks = null;

	public LegacyNexusDetectorNexusDevice(NexusDetector detector) {
		super(detector);
	}

	private INexusTree getDetectorNexusSubTree() throws NexusException {
		try {
			final NexusTreeProvider treeProvider = ((NexusDetector) getDetector()).readout();
			final INexusTree nexusTree = treeProvider.getNexusTree();
			if (nexusTree.getNumberOfChildNodes() != 1) {
				// At present we assume that the tree has a single entry, most detectors do this.
				logger.error("Nexus tree for detector {} has more than one sub-tree. Only the first will be processed.", getDetector().getName());
			}

			final INexusTree detectorSubTree = nexusTree.getChildNode(0);
			if (!detectorSubTree.getName().equals(getDetector().getName())) {
				logger.warn("Detector subtree {} has different name to detector {}", nexusTree.getName(), getDetector().getName());
			}
			if (!detectorSubTree.getNxClass().equals(NexusExtractor.NXDetectorClassName)) {
				// can only handle NXdetector trees. See NexusDataWriter.makeNexusDetectorGroups
				throw new NexusException("Nxclass attribute for detector " + getName() + "must be " + NexusExtractor.NXDetectorClassName + ", was: " + detectorSubTree.getNxClass());
			}

			return detectorSubTree;
		} catch (DeviceException e) {
			throw new NexusException("Cannot get NexusTree for detector: " + getName());
		}
	}

	@Override
	protected void writeDataFields(NexusScanInfo info, NXdetector detGroup) throws NexusException {
		this.detectorGroup = detGroup;
		nexusFileUrl = info.getFilePath();
		scanDimensions = info.getShape();
		dataDimPrefix = new int[scanDimensions.length];
		Arrays.fill(dataDimPrefix, 1);
		primaryFieldNames = new ArrayList<>();
		externalDatasetRanks = new HashMap<>();
		externalFileNames = new HashSet<>();

		final INexusTree detTree = getDetectorNexusSubTree();
		for (INexusTree subTree : detTree) {
			writeHere(detGroup, subTree, true, false);
		}
	}

	@Override
	protected void writeMetaDataFields(NXdetector detGroup, Detector detector) throws DeviceException {
		// set the local name
		detGroup.setLocal_nameScalar(detector.getName());

		// TODO: localName is the only field set by NexusDataWriter for NexusDetectors (other than any set the INexusTree)
		// in particular id, type and description are not set - uncommenting the line below would add these fields
//		super.writeMetaDataFields(detGroup, detector);
	}

	@Override
	protected String getPrimaryDataFieldName() {
		return primaryFieldNames.isEmpty() ? null : primaryFieldNames.get(0);
	}

	@Override
	protected void configureNexusWrapper(NexusObjectWrapper<NXdetector> nexusWrapper, NexusScanInfo info)
			throws NexusException {
		super.configureNexusWrapper(nexusWrapper, info);
		// the first primary field is added above, the rest are added as 'additional' primary data fields
		// note: these are the fields that NXdata groups are created for
		primaryFieldNames.stream().skip(1).forEach(nexusWrapper::addAdditionalPrimaryDataFieldName);
		externalFileNames.forEach(nexusWrapper::addExternalFileName);
		externalDatasetRanks.forEach(nexusWrapper::setExternalDatasetRank);
	}

	private GroupNode writeHere(GroupNode group, INexusTree tree, boolean makeData, boolean attrOnly) throws NexusException {
		// TODO: this method is huge and is seriously is need of refactoring, it is copied from NexusDataWriter
		// and should be refactored (or rewritten) with care only once adequately test
		if (!tree.isPointDependent() && !makeData) {
			return null;
		}

		GroupNode newGroup = null;
		String name = tree.getName();
		String nxClass = tree.getNxClass();
		boolean loopNodes = true;
		boolean attrBelowThisOnly = attrOnly;
		boolean nxClassIsSDS = isSDS(nxClass, tree.getParentNode() != null);
		boolean nxClassIsAttr = nxClass.equals(NexusExtractor.AttrClassName);
		boolean nxClassIsExternalSDS = nxClass.equals(NexusExtractor.ExternalSDSLink);
		if (nxClassIsExternalSDS) {
			if (makeData) {
				// link to an external SDS file (whatever that is)
				NexusGroupData data = tree.getData();

				/**
				 * Create a link of the format "nxfile://" + path to external file relative to nxs file + "#" + address
				 *
				 * The buffer in data contains "nxfile://" + abs path to external file + "#" + address
				 *
				 * so we need to replace the abs path with the relative path
				 */
				String link = ((String[]) data.getBuffer())[0];
				// link is of format nxfile:// + filepath + # + address
				String[] linkParts = link.split("nxfile://");
				if (linkParts.length != 2) {
					throw new NexusException("Invalid format for external link " + StringUtils.quote(link));
				}
				String[] parts = linkParts[1].split("#");
				if (parts.length != 2) {
					throw new NexusException("Invalid format for external link " + StringUtils.quote(link));
				}
				Path absExtPath = Paths.get(parts[0]).toAbsolutePath();
				String address = parts[1];
				File f = absExtPath.toFile();
				if (!f.exists()) {
					logger.warn("file {} does not exist at time of adding link", absExtPath);
				}
				Path nxsFile = Paths.get(nexusFileUrl).toAbsolutePath();
				Path nxsParent = nxsFile.getParent();
				Path relativize = nxsParent.relativize(absExtPath);
				String relativePath = relativize.toString();
				addExternalLink(group, name, relativePath, address);
//				links.add(new ExternalNXlink(name, relativeLink)); // note: changed from NexusDataWriter
				if (data.isDetectorEntryData && group == detectorGroup) { // only an immediate child node of the detector group can be a primary field
					if (data.externalDataRank < 0) {
						// in order to correctly build an NXdata for the field, we need to know its rank, which we can calculate from the dimensions field.
						logger.error("No rank set for field {}. This field will not be set as a primary field {}", name);
					} else {
						externalFileNames.add(relativePath);
						externalDatasetRanks.put(name, data.externalDataRank + scanDimensions.length);
						primaryFieldNames.add(name);
					}
				}
			}
			return null;
		}
		if (nxClassIsAttr) {
			if (makeData) {
				// create an attribute for the current node
				NexusGroupData data = tree.getData();
				if (data != null && data.getBuffer() != null) {
					INexusTree parent = tree.getParentNode();
					Node node;
					if (isSDS(parent.getNxClass(), parent.getParentNode() != null)) {
//						node = file.getData(group, parent.getName()); // note: changed from NexusDataWriter
						node = group.getDataNode(parent.getName());
					} else {
						node = group;
					}

					if ("axis".equals(name) || "label".equals(name)) {
						Integer axisno = getIntfromBuffer(data.getBuffer());
//						axisno += thisPoint.getScanDimensions().length;
						axisno += scanDimensions.length; // note: change from NexusDataWriter
//						NexusUtils.writeStringAttribute(file, node, name, axisno.toString());
						node.addAttribute(TreeFactory.createAttribute(name, axisno.toString())); // note: changed from NexusDataWriter
					} else {
						if (data.isChar()) {
//							NexusUtils.writeStringAttribute(file, node, name, (String) data.getFirstValue());
							node.addAttribute(TreeFactory.createAttribute(name, data.getFirstValue())); // note: changed from NexusDataWriter
						} else {
//							NexusUtils.writeAttribute(file, node, name, data.toDataset());
							node.addAttribute(TreeFactory.createAttribute(name, data.toDataset())); // note: changed from NexusDataWriter
						}
					}
				}
			}
			return null;
		}
		if (attrOnly) {
			// we're only allowed to create attributes at this point, so return here
			return null;
		}

		if (!name.isEmpty() && !nxClass.isEmpty()) {
			// write the data as a group, creating the group first if not SDS
			if (!nxClassIsSDS) {
				// create/get a GroupNode, note: mutually exclusive with tree.getData != null belowsDataWriter
//				group = file.getGroup(group, name, nxClass, true); // note: changed from NexusDataWriter
//				newGroup = group;
				if (makeData) {
					newGroup = NexusNodeFactory.createNXobjectForClass(nxClass);
					group.addGroupNode(name, newGroup);
					group = newGroup;
				} else {
					newGroup = group.getGroupNode(name);
					group = newGroup;
				}
			}

			NexusGroupData sds = tree.getData(); // note SDS stands for Scientific Data Set(?)
			if (sds != null) { // create a DataNode containing a dataset, note: mutually exclusive with nxClassIsSDS above
				if (!tree.isPointDependent()) {
					final DataNode data = NexusNodeFactory.createDataNode(); // moved and rewritten from NexusDataWRiter
					data.setDataset(sds.toDataset());
					group.addDataNode(name, data);
				} else {
					ILazyWriteableDataset lazy = sds.toLazyDataset();
					int[] sdims = lazy.getShape();
					lazy.setName(name);
					if (makeData) {
						DataNode data;
						int[] dataDimMake = generateDataDim(tree.isPointDependent(),
								tree.isPointDependent() ? scanDimensions : null, sdims);
						lazy.setMaxShape(dataDimMake);

						int[] dimensions;
						boolean requiresChunking = false;
						if (sdims.length == 1 && sdims[0] == 1) {
							// zero-dim data (single value per point), so dimensions are scan dimensions
							dimensions = tree.isPointDependent() ? scanDimensions : new int[] { 1 };
							requiresChunking = tree.isPointDependent();
						} else {
							requiresChunking = true;
							if (!tree.isPointDependent()) {
								dimensions = Arrays.copyOf(dataDimMake, dataDimMake.length);
							} else {
								dimensions = Arrays.copyOf(scanDimensions, scanDimensions.length + sdims.length);
								System.arraycopy(sdims, 0, dimensions, scanDimensions.length, sdims.length);
							}
						}
						if (requiresChunking) {
							int[] specifiedChunkDims;
							if (!tree.isPointDependent()) {
								// not point dependent so use dataset chunking
								if (sds.chunkDimensions != null) {
									specifiedChunkDims = sds.chunkDimensions.clone();
								} else { // No chunking set on the dataset so fill with -1 to estimate automatically
									specifiedChunkDims = new int[sds.dimensions.length];
									Arrays.fill(specifiedChunkDims, -1);
								}
							} else if (!(sdims.length == 1 && sdims[0] == 1)) {
								// point dependent, non-zero-dim data
								// extend chunk to include scan dimensions
								specifiedChunkDims = new int[dimensions.length];
								Arrays.fill(specifiedChunkDims, -1);
								if (sds.chunkDimensions != null) {
									System.arraycopy(sds.chunkDimensions, 0, specifiedChunkDims, scanDimensions.length, sds.chunkDimensions.length);
								}
							} else {
								// zero-dim, point dependent data
								// chunk rank matches scan rank (dimensions have been reduced to scan dimensions)
								specifiedChunkDims = new int[scanDimensions.length];
								Arrays.fill(specifiedChunkDims, -1);
							}
							int dataByteSize = InterfaceUtils.getItemBytes(1, sds.getInterface());
							if (dataByteSize <= 0) {
								// TODO: Fix for string types, particularly fixed length strings
								dataByteSize = 4;
							}
							int[] chunk = NexusUtils.estimateChunking(dimensions, dataByteSize, specifiedChunkDims);
							int[] maxshape = lazy.getMaxShape();
							for (int i = 0; i < maxshape.length; i++) {
								// chunk length in a given dimension should not exceed the upper bound of the dataset
								chunk[i] = maxshape[i] > 0 && maxshape[i] < chunk[i] ? maxshape[i] : chunk[i];
							}
							lazy.setChunking(chunk);
						}
						lazy.setFillValue(getFillValue(InterfaceUtils.getElementClass(sds.getInterface())));
						// data = file.createData(group, lazy, compression); // note: changed from NexusDataWriter
						data = NexusNodeFactory.createDataNode();
						data.setDataset(lazy);
						group.addDataNode(name, data);
						if (sds.isDetectorEntryData && group == detectorGroup) {
							// only an immediate child node of the detector group can be a primary field
	//						links.add(new SelfCreatingLink(data)); // note: changed from NexusDataWriter
							primaryFieldNames.add(name);
						}

						attrBelowThisOnly = true;
					} else {
						if (sdims.length == 1 && sdims[0] == 1) {
							sdims = null; // fix single item writing
						}
						int[] dataDim = generateDataDim(false, dataDimPrefix, sdims);
						int[] dataStartPos = generateDataStartPos(dataStartPosPrefix, sdims);
						int[] dataStop = generateDataStop(dataStartPos, sdims);

						// DataNode d = file.getData(group, name); // note: changed from NexusDataWriter
						DataNode d = group.getDataNode(name);

						lazy = d.getWriteableDataset();
						IDataset ds = sds.toDataset(); // TODO: copied from NexusDataWriter, but do we need to do this?
						ds.setShape(dataDim);

						try {
							lazy.setSlice(null, ds, SliceND.createSlice(lazy, dataStartPos, dataStop));
						} catch (Exception e) {
							logger.error("Problem setting slice on lazy dataset: {}", lazy, e);
							throw new NexusException("Problem setting slice on lazy dataset", e);
						}

						// Close data - do not add children as attributes added for first point only
						loopNodes = false;
					}
				}
			}
		} else {
			logger.warn("Name or class is empty:");
		}
		if (loopNodes) {
			for (INexusTree branch : tree) {
				writeHere(group, branch, makeData, attrBelowThisOnly);
			}
		}

		return newGroup;
	}

	@Override
	public void writePosition(Object data, SliceND scanSlice) throws NexusException {
		dataStartPosPrefix = scanSlice.getStart();
		final INexusTree detTree = getDetectorNexusSubTree();
		for (INexusTree subTree : detTree) {
			writeHere(detectorGroup, subTree, false, false);
		}
	}

	@Override
	public void scanEnd() throws NexusException {
		// clear all cached state
		scanDimensions = null;
		nexusFileUrl = null;
		dataDimPrefix = null;
		dataStartPosPrefix = null;
		detectorGroup = null;
		primaryFieldNames = null;
		externalDatasetRanks = null;
		externalFileNames = null;
	}

	private boolean isSDS(String className, boolean hasParent) {
		return (NexusExtractor.SDSClassName.equals(className) || (className.isEmpty() && hasParent));
	}

	private void addExternalLink(GroupNode group, String name, String externalFilePath, String nodePath) throws NexusException {
		try {
			if (!nodePath.startsWith(Tree.ROOT)) {
				nodePath = Tree.ROOT + nodePath;
			}
			final URI uri = new URI(externalFilePath);
			final SymbolicNode linkNode = NexusNodeFactory.createSymbolicNode(uri, nodePath);
			group.addSymbolicNode(name, linkNode);
		} catch (URISyntaxException e) {
			throw new NexusException("External file path '" + externalFilePath + "' cannot be converted to a URI");
		}
	}

	private static int getIntfromBuffer(Object buf) {
		if (buf instanceof Object[])
			buf = ((Object[]) buf)[0];
		if (buf instanceof Number)
			return ((Number) buf).intValue();
		if (buf.getClass().isArray()) {
			int len = ArrayUtils.getLength(buf);
			if (len == 1) {
				Object object = Array.get(buf, 0);
				return getIntfromBuffer(object);
			}
		}
		return Integer.parseInt(buf.toString());
	}

	/**
	 * calculate dimensionality of data to be written
	 *
	 * @param make
	 *            if true calculate for pre-allocation (first Dim UNLIMITED)
	 * @param dataDimPrefix
	 *            set to null if not point dependent
	 * @param dataDimensions
	 * @return dimensions
	 */
	protected static int[] generateDataDim(boolean make, int[] dataDimPrefix, int[] dataDimensions) {
		int[] dataDim = null;
		if (dataDimPrefix != null) {
			// do not attempt to add dataDimensions if not set or indicates single point
			int dataDimensionToAdd = dataDimensions != null && (dataDimensions.length > 1 || dataDimensions[0] > 1)
					? dataDimensions.length
					: 0;

			if (make) {
				dataDim = new int[dataDimPrefix.length + dataDimensionToAdd];
				Arrays.fill(dataDim, ILazyWriteableDataset.UNLIMITED);
			} else {
				dataDim = Arrays.copyOf(dataDimPrefix, dataDimPrefix.length + dataDimensionToAdd);
			}
			if (dataDimensionToAdd > 0 && dataDimensions != null) {
				for (int i = dataDimPrefix.length; i < dataDimPrefix.length + dataDimensionToAdd; i++) {
					dataDim[i] = dataDimensions[i - dataDimPrefix.length];
				}
			}
		} else if (dataDimensions != null) {
			dataDim = Arrays.copyOf(dataDimensions, dataDimensions.length);
		}

		return dataDim;
	}
	protected static final int[] generateDataStartPos(int[] dataStartPosPrefix, int[] dataDimensions) {
		int[] dataStartPos = null;
		if (dataStartPosPrefix != null) {
			// Do not add to the dimensions if we are dealing with a single points
			int dataDimensionToAdd = dataDimensions != null && (dataDimensions.length > 1 || dataDimensions[0] > 1)
					? dataDimensions.length
					: 0;
			dataStartPos = Arrays.copyOf(dataStartPosPrefix, dataStartPosPrefix.length + dataDimensionToAdd);
		} else if (dataDimensions != null) {
			dataStartPos = new int[dataDimensions.length];
		}
		return dataStartPos;
	}

	protected static int[] generateDataStop(int[] dataStartPos, int[] dataDimensions) {
		int[] dataStop = dataStartPos.clone();
		if (dataDimensions == null) {
			for (int i = 0; i < dataStop.length; i++) {
				dataStop[i]++;
			}
			return dataStop;
		}
		int prefix = dataStop.length - dataDimensions.length;
		if (prefix > 0) {
			for (int i = 0; i < prefix; i++) {
				dataStop[i]++;
			}
		}
		for (int i = 0; i < dataDimensions.length; i++) {
			dataStop[i + prefix] += dataDimensions[i];
		}
		return dataStop;
	}

	protected static final int[] generateStartPosPrefix(int currentPoint, int[] scanDimensions) {
		if (scanDimensions.length == 1) {
			return new int[] { currentPoint };
		}
		int[] scanNumbers = new int[scanDimensions.length];
		int[] totals = new int[scanDimensions.length];
		totals[scanDimensions.length - 1] = 1;
		for (int i = scanDimensions.length - 2; i > -1; i--) {
			totals[i] = scanDimensions[i + 1] * totals[i + 1];
		}
		if (currentPoint > totals[0] * scanDimensions[0]) {
			throw new IllegalArgumentException("currentPoint is larger than expected from reported scan dimensions");
		}

		int remainder = currentPoint;
		for (int j = 0; j < scanDimensions.length - 1; j++) {
			scanNumbers[j] = remainder / totals[j];
			remainder = remainder - scanNumbers[j] * totals[j];
		}
		scanNumbers[scanDimensions.length - 1] = remainder;
		return scanNumbers;
	}

}

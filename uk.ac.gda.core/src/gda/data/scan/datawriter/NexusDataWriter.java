/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.data.scan.datawriter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.DTypeUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.python.core.PyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.data.nexus.INeXusInfoWriteable;
import gda.data.nexus.NexusFileFactory;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeAppender;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.data.scan.datawriter.scannablewriter.ScannableWriter;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.NexusDetector;
import gda.device.scannable.ScannableUtils;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.scan.IScanDataPoint;

/**
 * DataWriter that outputs NeXus files and optionally a SRS/Text file as well.
 */
public class NexusDataWriter extends DataWriterBase implements DataWriter {
	private static final Logger logger = LoggerFactory.getLogger(NexusDataWriter.class);

	/**
	 * Property to control the level of instrumentation of the nexus api
	 */
	public static final String GDA_NEXUS_INSTRUMENT_API = "gda.nexus.instrumentApi";

	public static final String GDA_NEXUS_METADATAPROVIDER_NAME = "gda.nexus.metadata.provider.name";

	/**
	 * Property specifying whether SRS data files should be written in addition to NeXus files. Default is {@code true}.
	 */
	public static final String GDA_NEXUS_CREATE_SRS = "gda.nexus.createSRS";

	/**
	 * Boolean property specifying whether nxs/dat filenames should be prefixed with the beamline name; if {@code true},
	 * files will be named (e.g.) {@code "i23-999.nxs"} instead of just {@code "999.nxs"}
	 */
	public static final String GDA_NEXUS_BEAMLINE_PREFIX = "gda.nexus.beamlinePrefix";

	/**
	 * Property to enable swmr writing for the nexus scan file.
	 * SWMR mode will be enabled before the first scan point is written into the file, after the initial structure is set up
	 * The structure of the file cannot change after we enter SWMR mode.
	 */
	public static final String GDA_NEXUS_SWMR = "gda.nexus.writeSwmr";

	static final int MAX_DATAFILENAME = 255;

	/** Are we going to write an SRS file as well ? */
	private static boolean createSrsFileByDefault = true;
	private boolean createSrsFile = createSrsFileByDefault;

	// beamline name
	protected String beamline = null;

	// Directory to write data to
	protected String dataDir = null;

	// file name with no extension
	protected String fileBaseName = null;
	protected String fileBaseUrl = null;

	// file names
	private String nexusFileNameTemplate = null;

	protected String nexusFileName = null;

	// Fully qualified filenames
	protected String nexusFileUrl = null;

	// Relative filenames
	protected String nexusRelativeUrl = null;

	// NeXus entry name
	protected String entryName = "entry1";

	protected NexusFile file;

	protected SrsDataFile srsFile;

	/**
	 * The current run number.
	 */
	protected int scanNumber = -1;

	int getScanNumber() throws Exception {
		configureScanNumber(-1); // ensure it has been configured
		return scanNumber;
	}

	protected Collection<SelfCreatingLink> scannableID;

	boolean firstData = true;

	protected int scanPointNumber = -1;

	IScanDataPoint thisPoint;

	protected Metadata metadata = null;

	private boolean setupPropertiesDone = false;

	private boolean fileNumberConfigured = false;

	/**
	 * Constructor. This attempts to read the java.property which defines the beamline name.
	 */
	public NexusDataWriter() {
		super();
		// Check to see if we want to create a text/SRS file as well.
		// in constructor instead of setupProperties as srsFile is required at an earlier stage
		try {
			createSrsFile = LocalProperties.check(GDA_NEXUS_CREATE_SRS, createSrsFileByDefault);
			if (createSrsFile) {
				logger.info("NexusDataWriter is configured to also create SRS data files");
				srsFile = new SrsDataFile();
			}
		}
		catch (InstantiationException ex) {
			throw new RuntimeException("Could not instantiate SrsFile", ex);
		}
	}

	public NexusDataWriter(int fileNumber) {
		this();
		scanNumber = fileNumber;
	}

	protected void setupProperties() throws InstantiationException {
		if (setupPropertiesDone)
			return;
		metadata = GDAMetadataProvider.getInstance();

		try {
			beamline = metadata.getMetadataValue("instrument", "gda.instrument", null);
		} catch (DeviceException e1) {
		}

		// If the beamline name isn't set then default to 'base'.
		if (beamline == null) {
			// If the beamline name is not set then use 'base'
			beamline = "base";
		}

		// Check to see if the data directory has been defined.
		dataDir = PathConstructor.createFromDefaultProperty();
		if (dataDir == null) {
			// this java property is compulsory - stop the scan
			throw new InstantiationException("cannot work out data directory - cannot create a new data file.");
		}

		if (beforeScanMetaData== null) {
			String metaDataProviderName = LocalProperties.get(GDA_NEXUS_METADATAPROVIDER_NAME);
			if( StringUtils.hasLength(metaDataProviderName)){
				NexusTreeAppender metaDataProvider = Finder.getInstance().find(metaDataProviderName);
				InterfaceProvider.getTerminalPrinter().print("Getting meta data before scan");
				beforeScanMetaData = new NexusTreeNode("before_scan", NexusExtractor.NXCollectionClassName, null);
				metaDataProvider.appendToTopNode(beforeScanMetaData);
			}
		}
		setupPropertiesDone = true;
	}

	public INexusTree getBeforeScanMetaData() {
		return beforeScanMetaData;
	}

	public void setBeforeScanMetaData(INexusTree beforeScanMetaData) {
		this.beforeScanMetaData = beforeScanMetaData;
	}

	@Override
	public synchronized void configureScanNumber(int _scanNumber) throws Exception {
		if (!fileNumberConfigured) {
			if (_scanNumber > 0) {
				// the scan or other datawriter has set the id
				scanNumber = _scanNumber;
			} else {
				if (scanNumber <= 0) {
					setupProperties();
					// not set in a constructor so get from num tracker
					try {
						NumTracker runNumber = new NumTracker(beamline);
						// Get the next run number
						scanNumber = runNumber.incrementNumber();
					} catch (IOException e) {
						logger.error("ERROR: Could not instantiate NumTracker in NexusDataWriter().", e);
						throw new InstantiationException(
								"ERROR: Could not instantiate NumTracker in NexusDataWriter()." + e.getMessage());
					}
				}
			}
			//needs to use the same scan number
			if (createSrsFile) {
				srsFile.configureScanNumber(scanNumber);
			}
			fileNumberConfigured = true;
		}
	}

	static final int[] generateStartPosPrefix(int currentPoint, int[] scanDimensions) {
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

	static final int[] generateDataStartPos(int[] dataStartPosPrefix, int[] dataDimensions) {
		int[] dataStartPos = null;
		if (dataStartPosPrefix != null) {
			// Do not add to the dimensions if we are dealing with a single points
			int dataDimensionToAdd = dataDimensions != null && (dataDimensions.length > 1 || dataDimensions[0] > 1) ?
					dataDimensions.length : 0;
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
			int dataDimensionToAdd = dataDimensions != null && (dataDimensions.length > 1 || dataDimensions[0] > 1) ? dataDimensions.length : 0;

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

	/*
	 * The dimensions of the scan {@link Scan#getDimensions()}
	 */
	int[] scanDimensions;
	/*
	 * Fields present for convenience The location within data at which data is to be written - not taking into account
	 * the dimensions of the data itself The length of this array should match the length of scanDimensions and the
	 * values are calculated from the getCurrentPointNumber() method of the ScanDataPoint. For a 2d scan of 4 x 3 e.g.
	 * scan simpleScannable1, 0., 3, 1., simpleScannable2, 0., 2, 1. the values of scanPointNumbers will be [0,0],
	 * [0,1], [0,2], [1,0], [1,1], [1,2], [2,0], [2,1], [2,2], [3,0], [3,1], [3,2]
	 */
	int[] dataStartPosPrefix;
	/*
	 * Fields present for convenience The dimensions of the to be written - not taking into account the dimensions of
	 * the data itself The length of this array should match the length of scanDimensions and the values are all 1 but
	 * for the first which is ILazyWriteableDataset.UNLIMITED.
	 */
	int[] dataDimPrefix;

	private INexusTree beforeScanMetaData;

	// Performance instrumentation logging the total time spent writing
	private long totalWritingTime;

	@Override
	public void addData(IScanDataPoint dataPoint) throws Exception {
		// Some performance instrumentation
		long startTime = System.nanoTime(); // Get the time starting to write the point
		// If this is the start of a new scan reset the performance instrumentation
		if (scanPointNumber == -1) {
			totalWritingTime = 0;
		}

		thisPoint = dataPoint;
		scanPointNumber++;

		// Some debug messages
		logger.debug("Adding SDP with UUID: {}", dataPoint.getUniqueName());
		if (firstData) {
			logger.debug("Scan Parameters...");
			logger.debug("Scan Command : {}", dataPoint.getCommand());
			logger.debug("Instrument Name : {}", dataPoint.getInstrument());
			logger.debug("Number of Points : {}", dataPoint.getNumberOfPoints());
		}
		logger.debug("Current Point : {}", dataPoint.getCurrentPointNumber());

		if (scanPointNumber != dataPoint.getCurrentPointNumber()) {
			logger.warn("The DataWriter(" + scanPointNumber + ") and the DataPoint("
					+ dataPoint.getCurrentPointNumber() + ") disagree about the point number!");
		}
		dataStartPosPrefix = generateStartPosPrefix(thisPoint.getCurrentPointNumber(), thisPoint.getScanDimensions());

		try {
			if (firstData) {
				scanDimensions = dataPoint.getScanDimensions();
				dataDimPrefix = new int[scanDimensions.length];
				Arrays.fill(dataDimPrefix, 1);
				this.prepareFileAndStructure();
				if (file instanceof NexusFileHDF5 && LocalProperties.check(GDA_NEXUS_SWMR, false)) {
					((NexusFileHDF5) file).activateSwmrMode();
				}
				firstData = false;
			}
		} finally {
			firstData = false;
		}
		dataPoint.setCurrentFilename(getCurrentFileName());

		try {
			if (createSrsFile) {
				try {
					srsFile.addData(dataPoint);
				} catch (Exception ex) {
					String error = "Exception whilst writing Srs File";
					logger.error(error, ex);
					terminalPrinter.print(error);
				}
			}

			for (Scannable scannable : thisPoint.getScannables()) {
				checkForThreadInterrupt();
				writeScannable(scannable);
			}

			for (Detector detector : thisPoint.getDetectors()) {
				checkForThreadInterrupt();
				writeDetector(detector);
			}

			file.flush();

		} catch (Exception ex) {
			String error = "Exception occurred writing the nexus file. The nexus file is not being written correctly or has not been written.";
			logger.error(error, ex);
			terminalPrinter.print(error);
			throw ex;
		} finally {
			// Even if there was an exception we call super
			// that way the ascii file is still written.
//			try {
				super.addData(this, dataPoint);
//			} catch (Exception e) {
//				logger.error("exception received from DataWriterBase.addData(...)", e);
//			}
		}

		// Finished addData do performance instrumentations
		long finishTime = System.nanoTime();
		totalWritingTime += finishTime - startTime;
	}

	/*
	 * Both the Log4J and Nexus libraries absorb Thread interrupts. As the GDA Scanning Mechanism is dependent on using
	 * this mechanism and assumes that any Thread interruptions will be not be absorbed but will be propagated then we
	 * need to explicitly check for this in this class where some operations can take a finite amount of time and so
	 * when a scan is aborted the interruption can be absorbed and not propagated.
	 * <p>
	 * This check is made in other parts of the scanning mechanism, but should also be performed here.
	 *
	 * @throws InterruptedException
	 */
	private void checkForThreadInterrupt() throws InterruptedException {
		if (Thread.interrupted()) {
			Thread.currentThread().interrupt();
			throw new InterruptedException();
		}
	}

	private void writeGenericDetector(String detectorName, int[] dataDimensions, Object newData) throws NexusException {

		// Navigate to correct location in the file.
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, "instrument", NexusExtractor.NXInstrumentClassName);
		NexusUtils.addToAugmentPath(path, detectorName, NexusExtractor.NXDetectorClassName);
		GroupNode g = file.getGroup(path.toString(), false);

		if (newData instanceof INexusTree) {
			INexusTree detectorData = (INexusTree) newData;
			for (INexusTree subTree : detectorData) {
				writeHere(file, g, subTree, false, false, null);
			}
		} else {

			int[] startPos = generateDataStartPos(dataStartPosPrefix, dataDimensions);
			int[] dimArray = generateDataDim(false, dataDimPrefix, dataDimensions);
			int[] stop = generateDataStop(startPos, dataDimensions);

			// Open data array.
			DataNode d = file.getData(g, "data");
			ILazyWriteableDataset lazy;
			if (d == null) {
				lazy = NexusUtils.createLazyWriteableDataset("data", Dataset.FLOAT64, dimArray, null ,null);
				lazy.setFillValue(getFillValue(Dataset.FLOAT64));
				d = file.createData(g, lazy);
			} else {
				lazy = d.getWriteableDataset();
			}
			Dataset ds = DatasetFactory.createFromObject(newData).reshape(dimArray);
			try {
				lazy.setSlice(null, ds, SliceND.createSlice(lazy, startPos, stop));
			} catch (Exception e) {
				throw new NexusException(e.getMessage());
			}
		}
	}

	private void writeDetector(Detector detector) throws DeviceException, NexusException {

		if (detector.createsOwnFiles()) {
			writeFileCreatorDetector(detector.getName(), extractFileName(detector.getName()),
					detector.getDataDimensions());
		} else {
			if (detector instanceof NexusDetector) {
				writeNexusDetector((NexusDetector) detector);
			} else if (detector.getExtraNames() != null && detector.getExtraNames().length > 0) {
				double[] data = extractDoubleData(detector.getName());
				if (data != null) {
					writeCounterTimer(detector, data);
				} else {
					writeCounterTimer(detector);
				}
			} else {
				writeGenericDetector(detector.getName(), detector.getDataDimensions(),
						extractDoubleData(detector.getName()));
			}
		}
	}

	private static int getIntfromBuffer(Object buf) {
		if (buf instanceof Object[])
			buf = ((Object[]) buf)[0];
		if (buf instanceof Number)
			return ((Number) buf).intValue();
		if( buf.getClass().isArray()){
			int len = ArrayUtils.getLength(buf);
			if( len ==1){
				 Object object = Array.get(buf, 0);
				 return getIntfromBuffer(object);
			}
		}
		return Integer.parseInt(buf.toString());
	}

	void writeHere(NexusFile file, GroupNode group, INexusTree tree, boolean makeData, boolean attrOnly,
			List<SelfCreatingLink> links) throws NexusException {
		if (!tree.isPointDependent() && !makeData) {
			return;
		}
		String name = tree.getName();
		String nxClass = tree.getNxClass();
		boolean loopNodes = true;
		boolean attrBelowThisOnly = attrOnly;
		boolean nxClassIsSDS = nxClass.equals(NexusExtractor.SDSClassName);
		boolean nxClassIsAttr = nxClass.equals(NexusExtractor.AttrClassName);
		boolean nxClassIsExternalSDS = nxClass.equals(NexusExtractor.ExternalSDSLink);
		if (nxClassIsExternalSDS) {
			if (makeData) {
				NexusGroupData data = tree.getData();
				try {
					/**
					 * Create a link of the format
					 * "nxfile://" + path to external file relative to nxs file + "#" + address
					 *
					 * The buffer in data contains
					 * "nxfile://" + abs path to external file + "#" + address
					 *
					 * so we need to replace the abs path with the relative path
					 */
					String link = ((String[]) data.getBuffer())[0];
					//link is of format nxfile:// + filepath + # + address
					String[] linkParts = link.split("nxfile://");
					if( linkParts.length!=2){
						throw new NexusException("Invalid format for external link " + StringUtils.quote(link));
					}
					String[] parts = linkParts[1].split("#");
					if( parts.length!=2){
						throw new NexusException("Invalid format for external link " + StringUtils.quote(link));
					}
					Path absExtPath = Paths.get(parts[0]);
					String address=parts[1];
					File f = absExtPath.toFile();
					if (!f.exists())
						logger.warn("file " + absExtPath + " does not exist at time of adding link");
					Path nxsFile = Paths.get(nexusFileUrl);
					Path nxsParent = nxsFile.getParent();
					Path relativize = nxsParent.relativize(absExtPath);
					String relativeLink = "nxfile://" + relativize + "#" + address;
					String path = file.getPath(group);
					file.linkExternal(new URI(relativeLink), path + name, false);
					links.add(new ExternalNXlink(name, relativeLink));
				} catch (URISyntaxException e) {
					throw new NexusException(
							"supported encoding in creating string for external linking -- this should never happen");
				}
			}
			return;
		}
		if (nxClassIsAttr) {
			if (makeData) {
				NexusGroupData data = tree.getData();
				if (data != null && data.getBuffer() != null) {
					INexusTree parent = tree.getParentNode();
					Node node;
					if (parent.getNxClass().equals(NexusExtractor.SDSClassName)) {
						node = file.getData(group, parent.getName());
					} else {
						node = file.getGroup(group, parent.getName(), parent.getNxClass(), false);
					}
					if ("axis".equals(name) || "label".equals(name)) {
						Integer axisno = getIntfromBuffer(data.getBuffer());
						axisno += thisPoint.getScanDimensions().length;
						NexusUtils.writeStringAttribute(file, node, name, axisno.toString());
					} else {
						if (data.isChar()) {
							NexusUtils.writeStringAttribute(file, node, name, (String) data.getFirstValue());
						} else {
							NexusUtils.writeAttribute(file, node, name, data.toDataset());
						}
					}
				}
			}
			return;
		}
		if (attrOnly) {
			return;
		}
		if (!name.isEmpty() && !nxClass.isEmpty()) {
			if (!nxClassIsSDS) {
				group = file.getGroup(group, name, nxClass, true);
			}

			NexusGroupData sds = tree.getData();
			if (sds != null) {
				ILazyWriteableDataset lazy = sds.toLazyDataset();
				int[] sdims = lazy.getShape();
				lazy.setName(name);
				if (makeData) {
					DataNode data;
					int[] dataDimMake = generateDataDim(tree.isPointDependent(),
							tree.isPointDependent() ? scanDimensions : null, sdims);
					lazy.setMaxShape(dataDimMake);

					int[] dimensions;
					int compression = NexusFile.COMPRESSION_NONE;
					boolean requiresChunking = false;
					if (sdims.length == 1 && sdims[0] == 1) {
						// zero-dim data (single value per point), so dimensions are scan dimensions
						dimensions = tree.isPointDependent() ? scanDimensions : new int[] { 1 };
						requiresChunking = tree.isPointDependent();
						// do not compress such simple data by default
						compression = sds.compressionType != null ? sds.compressionType : NexusFile.COMPRESSION_NONE;
					} else {
						requiresChunking = true;
						if (!tree.isPointDependent()) {
							dimensions = Arrays.copyOf(dataDimMake, dataDimMake.length);
						} else {
							dimensions = Arrays.copyOf(scanDimensions, scanDimensions.length + sdims.length);
							System.arraycopy(sdims, 0, dimensions, scanDimensions.length, sdims.length);
						}
						compression = sds.compressionType != null ? sds.compressionType : NexusFile.COMPRESSION_LZW_L1;
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
						int dataByteSize = DTypeUtils.getItemBytes(sds.getDType());
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
					// TODO: only enable compression if the chunk size makes it worthwhile
					lazy.setFillValue(getFillValue(sds.getDType()));
					data = file.createData(group, lazy, compression);

					if (!tree.isPointDependent()) {
						int[] dataStartPos = generateDataStartPos(null, sdims);
						int[] dataStop = generateDataStop(dataStartPos, sdims);
						IDataset ds = sds.toDataset();
						try {
							lazy.setSlice(null, ds, SliceND.createSlice(lazy, dataStartPos, dataStop));
						} catch (Exception e) {
							logger.error("Problem setting slice on lazy dataset: {}", lazy, e);
							throw new NexusException("Problem setting slice on lazy dataset", e);
						}
					}
					if (links != null && sds.isDetectorEntryData) {
						links.add(new SelfCreatingLink(data));
					}

					attrBelowThisOnly = true;
				} else {
					if (sdims.length == 1 && sdims[0] == 1) {
						sdims = null; // fix single item writing
					}
					int[] dataDim = generateDataDim(false, dataDimPrefix, sdims);
					int[] dataStartPos = generateDataStartPos(dataStartPosPrefix, sdims);
					int[] dataStop = generateDataStop(dataStartPos, sdims);

					DataNode d = file.getData(group, name);

					lazy = d.getWriteableDataset();
					IDataset ds = sds.toDataset();
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
		} else {
			logger.warn("Name or class is empty:");
		}
		if (loopNodes) {
			for (INexusTree branch : tree) {
				writeHere(file, group, branch, makeData, attrBelowThisOnly, links);
			}
		}
	}

	private void writeNexusDetector(NexusDetector detector) throws NexusException {
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		GroupNode pg = file.getGroup(path.toString(), false);
		NexusUtils.addToAugmentPath(path, "instrument", NexusExtractor.NXInstrumentClassName);
		GroupNode g = file.getGroup(path.toString(), false);
		INexusTree tree = extractNexusTree(detector.getName());
		for (INexusTree subTree : tree) {
			if (subTree.getNxClass().equals(NexusExtractor.NXDetectorClassName))
				writeHere(file, g, subTree, false, false, null);
			else if (subTree.getNxClass().equals(NexusExtractor.NXMonitorClassName)) {
				writeHere(file, pg, subTree, false, false, null);
			}
		}
	}

	private Object extractDetectorObject(String detectorName) {
		int index = thisPoint.getDetectorNames().indexOf(detectorName);
		return thisPoint.getDetectorData().get(index);
	}

	private double[] extractDoubleData(String detectorName) {
		Object object = extractDetectorObject(detectorName);
		return extractDoubleData(detectorName, object);
	}

	private String extractFileName(String detectorName) {
		return (String) extractDetectorObject(detectorName);
	}

	private INexusTree extractNexusTree(String detectorName) {
		return ((NexusTreeProvider) extractDetectorObject(detectorName)).getNexusTree();
	}

	/**
	 * @param detectorName
	 * @param object
	 * @return the data read from the detector
	 * @throws NumberFormatException
	 */
	private double[] extractDoubleData(String detectorName, Object object) throws NumberFormatException {
		double[] data = null;
		if (object instanceof double[]) {
			data = (double[]) object;
		} else if (object instanceof PyList) {
			// coerce PyList into double array.
			int length = ((PyList) object).__len__();
			data = new double[length];
			for (int i = 0; i < length; i++) {
				data[i] = Double.valueOf(((PyList) object).__getitem__(i).toString());
			}
		} else if (object instanceof int[]) {
			int[] idata = (int[]) object;
			data = new double[idata.length];
			for (int i = 0; i < data.length; i++) {
				data[i] = idata[i];
			}
		} else if (object instanceof long[]) {
			long[] ldata = (long[]) object;
			data = new double[ldata.length];
			for (int i = 0; i < data.length; i++) {
				data[i] = ldata[i];
			}
		} else if (object instanceof String[]) {
			String[] sdata = (String[]) object;
			data = new double[sdata.length];
			for (int i = 0; i < data.length; i++) {
				data[i] = Double.valueOf(sdata[i]);
			}
		} else if (object instanceof Number[]) {
			Number[] ldata = (Number[]) object;
			data = new double[ldata.length];
			for (int i = 0; i < data.length; i++) {
				data[i] = ldata[i].doubleValue();
			}
		} else if (object instanceof Double) {
			data = new double[] { (Double) object };
		} else if (object instanceof Integer) {
			data = new double[] { (Integer) object };
		} else if (object instanceof Long) {
			data = new double[] { (Long) object };
		} else {
			logger.error("cannot handle data of type " + object.getClass().getName() + " from detector: "
					+ detectorName + ". NO DATA WILL BE WRITTEN TO NEXUS FILE!");
		}
		return data;
	}

	private Double[] extractDoublePositions(String scannableName) {
		int index = thisPoint.getScannableNames().indexOf(scannableName);

		if (index > -1) {
			Object position = thisPoint.getPositions().get(index);
			if (position != null) {
				return ScannableUtils.objectToArray(position);
			}
		}
		return null;
	}

	/**
	 * Perform any tasks that should be done at the end of a scan and close the file.
	 *
	 * @throws Exception
	 */
	@Override
	public void completeCollection() throws Exception {
		releaseFile();
		super.completeCollection();
		int numberOfPoints = scanPointNumber + 1;
		// Log the performance info. Convert ns into ms, and report per point to make comparable
		logger.info("Writting {} points to NeXus took an average of {} ms per point", numberOfPoints,
				(totalWritingTime / 1.0E6) / numberOfPoints);
	}

	/**
	 * Releases the file handle.
	 */
	public void releaseFile() {
		try {
			if (file != null) {
				file.flush();
				file.close();
			}
			if (createSrsFile) {
				srsFile.releaseFile();
			}

		} catch (NexusException ne) {
			String error = "NeXusException occurred when closing file: ";
			logger.error(error, ne);
			terminalPrinter.print(error);
			terminalPrinter.print(ne.getMessage());
		} catch (Throwable et) {
			String error = "Error occurred when closing data file(s): ";
			logger.error(error, et);
			terminalPrinter.print(error);
			terminalPrinter.print(et.getMessage());
		} finally {
			file = null;
		}
	}

	@Override
	public String getCurrentFileName() {
		return nexusFileUrl;
	}

	public Object getData() {
		return file;
	}

	private void prepareFileAndStructure() throws Exception {
		setupProperties();
		createNextFile();
		makeMetadata();
		makeScannablesAndMonitors();
		makeDetectors();
	}

	private void makeMetadata() {
		try {
			GroupNode g = file.getGroup(NexusUtils.createAugmentPath(entryName, NexusExtractor.NXEntryClassName), true);

			NexusUtils.writeString(file, g, "scan_command", thisPoint.getCommand());
			String scanid = "";
			try {
				scanid = metadata.getMetadataValue(GDAMetadataProvider.SCAN_IDENTIFIER);
			} catch (DeviceException e) {
				// do nothing
			}
			NexusUtils.writeString(file, g, "scan_identifier", scanid.isEmpty() ? thisPoint.getUniqueName() : scanid);
			NexusUtils.writeIntegerArray(file, g, "scan_dimensions", thisPoint.getScanDimensions());
			NexusUtils.writeString(file, g, "title", metadata.getMetadataValue("title"));
			createCustomMetaData(g);
		} catch (Exception e) {
			logger.info("error writing less important scan information", e);
		} finally {
		}
	}

	/**
	 * Override to provide additional meta data, if required. Does nothing otherwise.
	 * @param g
	 *
	 * @throws NexusException
	 */
	// allow inheriting classes to throw this exception
	protected void createCustomMetaData(GroupNode g) throws NexusException {

		if( beforeScanMetaData != null ){
			writeHere(file, g, beforeScanMetaData, true, false, null);
		}
	}

	protected String getGroupClassFor(@SuppressWarnings("unused") Scannable s) {
		String groupName = "NXpositioner";
		return groupName;
	}

	/**
	 * Create NXpositioner/NXmonitor class for each Scannable.
	 */
	private void makeScannablesAndMonitors() {
		scannableID = new Vector<SelfCreatingLink>();
		Collection<Scannable> scannablesAndMonitors = new Vector<Scannable>();
		scannablesAndMonitors.addAll(thisPoint.getScannables());
		scannablesAndMonitors = makeConfiguredScannablesAndMonitors(scannablesAndMonitors);
		makeScannablesAndMonitors(scannablesAndMonitors);
	}

	/**
	 * this is run when processing the first ScanDataPoint
	 * the file is in the root node
	 * we add all the one off metadata here
	 */
	protected Collection<Scannable> makeConfiguredScannablesAndMonitors(Collection<Scannable> scannablesAndMonitors) {
		Set<String> metadatascannablestowrite = new HashSet<String>(metadatascannables);

		for (Detector det : thisPoint.getDetectors()) {
			logger.info("found detector named: "+det.getName());
			String detname = det.getName();
			if (metadataScannablesPerDetector.containsKey(detname)) {
				HashSet<String> metasPerDet = metadataScannablesPerDetector.get(detname);
				if (metasPerDet != null && !metasPerDet.isEmpty()) {
					metadatascannablestowrite.addAll(metasPerDet);
				}
			}
		}

		try {
			GroupNode g = file.getGroup(NexusUtils.createAugmentPath(entryName, NexusExtractor.NXEntryClassName), false);

			Set<Scannable> wehavewritten = new HashSet<Scannable>();
			for (Scannable scannable : scannablesAndMonitors) {
				String scannableName = scannable.getName();
				if (weKnowTheLocationFor(scannableName)) {
					wehavewritten.add(scannable);
					ScannableWriter writer = locationmap.get(scannableName);
					Collection<String> prerequisites = writer.getPrerequisiteScannableNames();
					if (prerequisites != null)
						metadatascannablestowrite.addAll(prerequisites);
					scannableID.addAll(writer.makeScannable(file, g, scannable, getSDPositionFor(scannableName), generateDataDim(false, scanDimensions, null)));
				}
			}

			Set<String> aux = new HashSet<String>();
			do { // add dependencies of metadata scannables
				aux.clear();
				for (String s: metadatascannablestowrite) {
					if (weKnowTheLocationFor(s)) {
						Collection<String> prerequisites = locationmap.get(s).getPrerequisiteScannableNames();
						if (prerequisites != null)
							aux.addAll(prerequisites);
					}
				}
			} while (metadatascannablestowrite.addAll(aux));

			// remove the ones in the scan, as they are not metadata
			for (Scannable scannable : scannablesAndMonitors) {
				metadatascannablestowrite.remove(scannable.getName());
			}
			// only use default writing for the ones we haven't written yet
			scannablesAndMonitors.removeAll(wehavewritten);

			makeMetadataScannables(g, metadatascannablestowrite);
		} catch (NexusException e) {
			// FIXME NexusDataWriter should allow exceptions to be thrown
			logger.error("TODO put description of error here", e);
		}
		return scannablesAndMonitors;
	}

	protected void makeScannablesAndMonitors(Collection<Scannable> scannablesAndMonitors) {

		String axislist = "1";
		for (int j = 2; j <= thisPoint.getScanDimensions().length; j++) {
			axislist = axislist + String.format(",%d", j);
		}

		try {
			StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
			NexusUtils.addToAugmentPath(path, "instrument", NexusExtractor.NXInstrumentClassName);

			int[] dataDim = generateDataDim(true, scanDimensions, null);

			int inputnameindex = 0;
			int extranameindex = 0;
			for (Scannable scannable : scannablesAndMonitors) {

				String[] inputNames = scannable.getInputNames();
				String[] extraNames = scannable.getExtraNames();

				String groupName = getGroupClassFor(scannable);
				StringBuilder p = NexusUtils.addToAugmentPath(new StringBuilder(path), scannable.getName(), groupName);
				GroupNode g = file.getGroup(p.toString(), true);

				// Check to see if the scannable will write its own info into NeXus
				if (scannable instanceof INeXusInfoWriteable) {
					((INeXusInfoWriteable) scannable).writeNeXusInformation(file, g);
				}

				// loop over input names...
				for (String element : inputNames) {
					// Create the data array (with an unlimited scan
					// dimension)
					int[] chunking = NexusUtils.estimateChunking(scanDimensions, 8);
					ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset(element, Dataset.FLOAT64, dataDim, null, chunking);
					lazy.setFillValue(getFillValue(Dataset.FLOAT64));
					DataNode data = file.createData(g, lazy);

					// Get a link ID to this data set.
					NexusUtils.writeStringAttribute(file, data, "local_name", String.format("%s.%s", scannable.getName(), element));

					// assign axes
					if (thisPoint.getScanDimensions().length > 0) {
						// TODO
						// in all likelihood this will not give the right axis assignment
						// for scannables with multiple input names
						// this is not solvable given the current data in SDP

						if ((thisPoint.getScanDimensions().length) > inputnameindex) {
							NexusUtils.writeStringAttribute(file, data, "label", String.format("%d", inputnameindex + 1));
							NexusUtils.writeStringAttribute(file, data, "primary", "1");
						}
						NexusUtils.writeStringAttribute(file, data, "axis", axislist);
					}

					scannableID.add(new SelfCreatingLink(data));
					inputnameindex++;
				}

				for (String element : extraNames) {

					// Create the data array (with an unlimited scan
					// dimension)
					ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset(element, Dataset.FLOAT64, dataDim, null, null);
					lazy.setFillValue(getFillValue(Dataset.FLOAT64));
					DataNode data = file.createData(g, lazy);

					// Get a link ID to this data set.
					NexusUtils.writeStringAttribute(file, data, "local_name", String.format("%s.%s", scannable.getName(), element));

					if (thisPoint.getDetectorNames().isEmpty() && extranameindex == 0) {
						NexusUtils.writeStringAttribute(file, data, "signal", "1");
					}

					scannableID.add(new SelfCreatingLink(data));
					extranameindex++;
				}
			}

		} catch (NexusException e) {
			String error = "NeXus file creation failed during makeScannables: ";
			logger.error(error + e.getMessage(), e);
			terminalPrinter.print(error);
			terminalPrinter.print(e.getMessage());
		}
	}

	private void makeFallbackNXData() throws NexusException {
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, "default", NexusExtractor.NXDataClassName);
		GroupNode group = file.getGroup(path.toString(), true);
		makeAxesLinks(group);
	}

	protected void makeAxesLinks(GroupNode group) {
		// Make links to all scannables.
		for (SelfCreatingLink id : scannableID) {
			try {
				id.create(file, group);
			} catch (NexusException e) {
				logger.warn("Error in makeLink (reported to NX group) for " + id.toString() + "with error"
						+ e.getMessage());
			}
		}
	}

	/**
	 * Create NXdetector class for each Detector.
	 */
	private void makeDetectors() {
		try {
			Vector<Detector> detectors = thisPoint.getDetectors();

			if (detectors.size() == 0) {
				makeFallbackNXData();
				return;
			}

			// create an NXdetector for each detector...
			for (Detector detector : detectors) {
				try {
					makeDetectorEntry(detector);
				} catch(Exception e){
					throw new DeviceException("Error making detector entry for detector " + detector.getName(),e);
				}
			}
		} catch (NexusException e) {
			String error = "NeXus file creation failed during makeDetectors: ";
			logger.error(error + e.getMessage(), e);
			terminalPrinter.print(error);
			terminalPrinter.print(e.getMessage());
		} catch (DeviceException de) {
			String error = "DeviceException during NeXus file creation: ";
			logger.error(error + de.getMessage(), de);
			terminalPrinter.print(error);
			terminalPrinter.print(de.getMessage());
		}
	}

	private void makeDetectorEntry(Detector detector) throws DeviceException, NexusException {
		logger.debug("Making NXdetector for " + detector.getName() + " in NeXus file.");

		if (detector instanceof NexusDetector) {
			logger.debug("Creating NexusTree entry in NeXus file.");
			INexusTree detTree = extractNexusTree(detector.getName());
			for (INexusTree det : detTree) {
				if (det.getNxClass().equals(NexusExtractor.NXDetectorClassName)) {
					makeGenericDetector(det.getName(), null, Dataset.FLOAT64, detector, det);
				} else if (det.getNxClass().equals(NexusExtractor.NXMonitorClassName)) {
					// FIXME -- if this doesn't explode I am truly surprised
					GroupNode group = file.getGroup(NexusUtils.createAugmentPath(entryName, NexusExtractor.NXEntryClassName), false);
					writeHere(file, group, det, firstData, false, null);
				}
			}
		} else if (detector.createsOwnFiles()) {
			logger.debug("Creating File Creator Detector entry in NeXus file.");
			makeFileCreatorDetector(detector.getName(), detector.getDataDimensions(), detector);
		} else if (detector.getExtraNames().length > 0) {
			logger.debug("Creating Detector entry in NeXus file.");
			makeCounterTimer(detector);
		} else {
			logger.debug("Creating Generic Detector entry in NeXus file.");
			makeGenericDetector(detector.getName(), detector.getDataDimensions(), Dataset.FLOAT64, detector, null);
		}
	}

	private void writeFileCreatorDetector(String detectorName, String dataFileName,
			@SuppressWarnings("unused") int[] detectorDataDimensions) throws NexusException {

		if (dataFileName.length() > 255) {
			logger.error("The detector (" + detectorName + ") returned a file name (of length " + dataFileName.length()
					+ ") which is greater than the max allowed length (" + MAX_DATAFILENAME + ").");
		}

		// Navigate to correct location in the file.
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, "instrument", NexusExtractor.NXInstrumentClassName);
		NexusUtils.addToAugmentPath(path, detectorName, NexusExtractor.NXDetectorClassName);
		NexusUtils.addToAugmentPath(path, "data_file", NexusExtractor.NXNoteClassName);
		GroupNode group = file.getGroup(path.toString(), false);

		logger.debug("Filename received from detector: " + dataFileName);

		// Now lets construct the relative (to the nexus data file) path to the file.
		if (dataFileName.startsWith(dataDir)) {
			dataFileName = dataFileName.substring(dataDir.length());
			// Check for a leading '/'
			if (dataFileName.startsWith("/") == false) {
				dataFileName = "/" + dataFileName;
			}
			// Make the path relative.
			dataFileName = "." + dataFileName;
		}

		// Set all the start positions to be zero (except for the first
		// dimension which is the scan point)
		int[] dataDim = generateDataDim(false, dataDimPrefix, null);
		int[] dataStartPos = generateDataStartPos(dataStartPosPrefix, null);
		int[] dataStop = generateDataStop(dataStartPos, null);

		DataNode data = file.getData(group, "file_name");
		Dataset fileName = DatasetFactory.createFromObject(dataFileName).reshape(dataDim);
		ILazyWriteableDataset lazy = data.getWriteableDataset();
		try {
			lazy.setSlice(null, fileName, SliceND.createSlice(lazy, dataStartPos, dataStop));
		} catch (Exception e) {
			logger.error("Could not write file_name", e);
			throw new NexusException("Could not write file_name", e);
		}
		NexusUtils.writeIntegerAttribute(file, group, "data_filename", 1);
	}

	private void makeFileCreatorDetector(String detectorName, @SuppressWarnings("unused") int[] dataDimensions,
			Object detector) throws NexusException {
		// Navigate to the relevant section in file...
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, "instrument", NexusExtractor.NXInstrumentClassName);
		NexusUtils.addToAugmentPath(path, detectorName, NexusExtractor.NXDetectorClassName);
		GroupNode group = file.getGroup(path.toString(), true);

		// Metadata items
		NexusUtils.writeString(file, group, "description", "Generic GDA Detector - External Files");
		NexusUtils.writeString(file, group, "type", "Detector");

		// Check to see if the detector will write its own info into NeXus
		if (detector instanceof INeXusInfoWriteable) {
			((INeXusInfoWriteable) detector).writeNeXusInformation(file, group);
		}

		GroupNode g = file.getGroup(group, "data_file", NexusExtractor.NXNoteClassName, true);

		int[] dataDim = generateDataDim(true, scanDimensions, null);

		ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset("file_name", Dataset.STRING, dataDim, null, null);
		int dataByteSize = 8; // vlen strings are sizeof(char*), need to handle fixed length case
		int[] chunk = NexusUtils.estimateChunking(scanDimensions, dataByteSize);
		lazy.setChunking(chunk);
		file.createData(g, lazy);

		// FIXME this data group does not contain detector data,
		// we should not create this
		// if this is the only detector the fallback group would be better
		// Make and open NXdata
		path.setLength(0);
		NexusUtils.addToAugmentPath(path, entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, detectorName, NexusExtractor.NXDataClassName);
		group = file.getGroup(path.toString(), true);

		makeAxesLinks(group);
	}

	/**
	 * Creates an NXdetector for a generic detector (ie one without a special create routine).
	 */
	private void makeGenericDetector(String detectorName, int[] dataDimensions, int dtype, Detector detector,
			INexusTree detectorData) throws NexusException {
		// Navigate to the relevant section in file...
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, "instrument", NexusExtractor.NXInstrumentClassName);
		NexusUtils.addToAugmentPath(path, detectorName, NexusExtractor.NXDetectorClassName);
		// Create NXdetector
		GroupNode group = file.getGroup(path.toString(), true);

		// Metadata items
		try {
			if (!(detector instanceof NexusDetector)) {
				String detDescription = detector.getDescription();
				String detType = detector.getDetectorType();
				String detId = detector.getDetectorID();
				if (detDescription != null && detDescription.length() > 0) {
					NexusUtils.writeString(file, group, "description", detDescription);
				}
				if (detType != null && detType.length() > 0) {
					NexusUtils.writeString(file, group, "type", detType);
				}
				if (detId != null && detId.length() > 0) {
					NexusUtils.writeString(file, group, "id", detId);
				}
			} else {
				NexusUtils.writeString(file, group, "local_name", detectorName);
			}
		} catch (DeviceException e) {
			e.printStackTrace();
		}

		// Check to see if the detector will write its own info into NeXus
		if (detector instanceof INeXusInfoWriteable) {
			((INeXusInfoWriteable) detector).writeNeXusInformation(file, group);
		}

		List<SelfCreatingLink> links = new Vector<SelfCreatingLink>();
		if (detectorData != null) {
			for (INexusTree subTree : detectorData) {
				writeHere(file, group, subTree, true, false, links);
			}
		} else if (detector.getExtraNames().length > 0) {
			// Detectors with multiple extra names can act like counter-timers

			int[] dataDim = generateDataDim(true, scanDimensions, null);
			boolean first = true;
			for (String extra : detector.getExtraNames()) {

				ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset(extra, Dataset.FLOAT64, dataDim, null, null);
				lazy.setFillValue(getFillValue(Dataset.FLOAT64));
				DataNode data = file.createData(group, lazy);

				NexusUtils.writeStringAttribute(file, data, "local_name", String.format("%s.%s", detectorName, extra));

				// Get a link ID to this data set
				SelfCreatingLink detectorID = new SelfCreatingLink(data);

				path.setLength(0);
				NexusUtils.addToAugmentPath(path, entryName, NexusExtractor.NXEntryClassName);
				NexusUtils.addToAugmentPath(path, detector.getName(), NexusExtractor.NXDataClassName);
				// If this is the first channel then we need to create (and
				// open) the NXdata item and link to the scannables.
				GroupNode g = file.getGroup(path.toString(), first);
				if (first) {
					first = false;
					makeAxesLinks(g);
				}

				// Make a link to the data array
				detectorID.create(file, g);
			}

		} else {
			// even make data area for detectors that first create their own files
			int[] dataDim = generateDataDim(true, scanDimensions, dataDimensions);

			// make the data array to store the data...
			ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset("data", dtype, dataDim, null, null);
			lazy.setFillValue(getFillValue(dtype));
			DataNode data = file.createData(group, lazy);
			NexusUtils.writeStringAttribute(file, data, "local_name", String.format("%s.%s", detectorName, detectorName));

			// Get a link ID to this data set.
			links.add(new SelfCreatingLink(data));
		}

		// Make and open NXdata
		path.setLength(0);
		NexusUtils.addToAugmentPath(path, entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, detectorName, NexusExtractor.NXDataClassName);
		group = file.getGroup(path.toString(), true);

		// Make a link to the data array
		for (SelfCreatingLink link : links) {
			link.create(file, group);
		}

		makeAxesLinks(group);
	}

	/**
	 * Creates an NXdetector for a CounterTimer.
	 */
	private void makeCounterTimer(Detector detector) throws NexusException, DeviceException {
		SelfCreatingLink detectorID;

		// Navigate to the relevant section in file...
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, "instrument", NexusExtractor.NXInstrumentClassName);
		NexusUtils.addToAugmentPath(path, detector.getName(), NexusExtractor.NXDetectorClassName);
		GroupNode group = file.getGroup(path.toString(), true);

		// Metadata items
		String description = detector.getDescription();
		String type = detector.getDetectorType();
		String id = detector.getDetectorID();

		if (description != null && description.length() > 0) {
			NexusUtils.writeString(file, group, "description", detector.getDescription());
		}
		if (type != null && type.length() > 0) {
			NexusUtils.writeString(file, group, "type", detector.getDetectorType());
		}
		if (id != null && id.length() > 0) {
			NexusUtils.writeString(file, group, "id", detector.getDetectorID());
		}

		// Check to see if the detector will write its own info into NeXus
		if (detector instanceof INeXusInfoWriteable) {
			((INeXusInfoWriteable) detector).writeNeXusInformation(file, group);
		}
		int[] dataDim = generateDataDim(true, scanDimensions, null);

		final String[] extraNames = detector.getExtraNames();
		for (int j = 0; j < extraNames.length; j++) {

			// this can fail if the list of names contains duplicates
			ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset(extraNames[j], Dataset.FLOAT64, dataDim, null, null);
			lazy.setChunking(NexusUtils.estimateChunking(scanDimensions, 8));
			DataNode data = file.createData(group, lazy);

			// Get a link ID to this data set
			detectorID = new SelfCreatingLink(data);
			NexusUtils.writeStringAttribute(file, data, "local_name", String.format("%s.%s", detector.getName(), extraNames[j]));

			path.setLength(0);
			NexusUtils.addToAugmentPath(path, entryName, NexusExtractor.NXEntryClassName);
			NexusUtils.addToAugmentPath(path, detector.getName(), NexusExtractor.NXDataClassName);
			GroupNode g = file.getGroup(path.toString(), j == 0);
			if (j == 0) {
				// If this is the first channel then we need to create (and
				// open) the NXdata item and link to the scannables.
				makeAxesLinks(g);
			}

			// Make a link to the data array
			detectorID.create(file, g);
		}
	}

	/**
	 * wrap the dreaded static so behaviour can be customised
	 *
	 * @return reference to file
	 * @throws Exception
	 */
	protected NexusFile createFile() throws Exception {
		return NexusFileFactory.createFile(nexusFileUrl, LocalProperties.check(GDA_NEXUS_INSTRUMENT_API), LocalProperties.check(GDA_NEXUS_SWMR));
	}

	/**
	 * Create the next file. First increment the file number and then try and get a NeXus file handle from
	 * {@link NexusFileFactory}.
	 * @throws Exception
	 */
	public void createNextFile() throws Exception {
		try {
			if (file != null) {
				try {
					file.close();
				} catch (Throwable et) {
					String error = "Error closing NeXus file.";
					logger.error(error, et);
					terminalPrinter.print(error);
					terminalPrinter.print(et.getMessage());
				}
			}

			// set the entry name
			// this.entryName = "scan_" + run;
			this.entryName = "entry1";

			// construct filename
			if (nexusFileNameTemplate != null) {
				nexusFileName = String.format(nexusFileNameTemplate, scanNumber);
			} else if (LocalProperties.check(GDA_NEXUS_BEAMLINE_PREFIX)) {
				nexusFileName = beamline + "-" + scanNumber + ".nxs";
			} else {
				nexusFileName = Long.toString(scanNumber) + ".nxs";
			}

			if (!dataDir.endsWith(File.separator)) {
				dataDir += File.separator;
			}

			nexusFileUrl = dataDir + nexusFileName;

			// Check to see if the file(s) already exists!
			if (new File(nexusFileUrl).exists()) {
				throw new Exception("The file " + nexusFileUrl + " already exists.");
			}

			// create nexus file and return handle
			file = createFile();

			// If we have been return a null file reference then there was
			// some problem creating the file.
			if (file == null) {
				throw new Exception();
			}

			// Print informational message to console.
			String msg = "Writing data to file (NeXus): " + nexusFileUrl;
			logger.info(msg);
			terminalPrinter.print(msg);
			if (createSrsFile) {
				msg = "Also creating file (txt): " + srsFile.fileUrl;
				logger.info(msg);
				terminalPrinter.print(msg);
			}
		} catch (Error ex) {
			String error = "Failed to create file (" + nexusFileUrl;
			if (createSrsFile) {
				error += " or " + srsFile.fileUrl;
			}
			error += ")";
			error += ". Nexus binary library was not found. Inform Data Acquisition.";
			logger.error(error, ex);
			if (terminalPrinter != null){
				terminalPrinter.print(error);
				terminalPrinter.print(ex.getMessage());
			}
			throw ex;
		} catch (Exception ex) {
				String error = "Failed to create file (" + nexusFileUrl;
				if (createSrsFile) {
					error += " or " + srsFile.fileUrl;
				}
				error += ")";
				logger.error(error, ex);
				if (terminalPrinter != null){
					terminalPrinter.print(error);
					terminalPrinter.print(ex.getMessage());
				}
				throw ex;
			}

	}

	/**
	 * Returns the full path of the folder which data files are written to.
	 *
	 * @return the full path of the folder which data files are written
	 */
	public String getDataDir() {
		return dataDir;
	}

	/**
	 * Not used in this implementation.
	 */
	@Override
	public void setHeader(String header) {
	}

	protected void writeScannable(Scannable scannable) throws NexusException {
		if (!weKnowTheLocationFor(scannable.getName())) {
			writePlainDoubleScannable(scannable);
		} else {
			GroupNode group = file.getGroup(NexusUtils.createAugmentPath(entryName, NexusExtractor.NXEntryClassName), false);
			locationmap.get(scannable.getName()).writeScannable(file, group, scannable, getSDPositionFor(scannable.getName()), generateDataStartPos(dataStartPosPrefix, null));
		}
	}

	/**
	 * Writes the data for a given scannable to an existing NXpositioner.
	 * @throws NexusException
	 */
	protected void writePlainDoubleScannable(Scannable scannable) throws NexusException {
		int[] startPos = generateDataStartPos(dataStartPosPrefix, null);
		int[] stop = generateDataStop(startPos, null);
		int[] dimArray = generateDataDim(false, dataDimPrefix, null);

		// Get inputNames and positions
		String[] inputNames = scannable.getInputNames();
		String[] extraNames = scannable.getExtraNames();
		Double[] positions = extractDoublePositions(scannable.getName());

		logger.debug("Writing data for scannable (" + scannable.getName() + ") to NeXus file.");

		// Navigate to correct location in the file.
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, "instrument", NexusExtractor.NXInstrumentClassName);
		NexusUtils.addToAugmentPath(path, scannable.getName(), getGroupClassFor(scannable));
		GroupNode group = file.getGroup(path.toString(), false);

		// Loop over inputNames...
		for (int i = 0; i < inputNames.length; i++) {
			// Open data item
			DataNode data = file.getData(group, inputNames[i]);
			try {
				ILazyWriteableDataset lazy = data.getWriteableDataset();
				lazy.setSlice(null, DatasetFactory.createFromObject(positions[i]).reshape(dimArray), SliceND.createSlice(lazy, startPos, stop));
			} catch (Exception e) {
				throw new NexusException(e.getMessage());
			}
		}

		// and now over extraNames...
		for (int i = 0; i < extraNames.length; i++) {
			// Open data item
			DataNode data = file.getData(group, extraNames[i]);
			ILazyWriteableDataset lazy = data.getWriteableDataset();
			try {
				lazy.setSlice(null, DatasetFactory.createFromObject(positions[inputNames.length + i]), SliceND.createSlice(lazy, startPos, stop));
			} catch (Exception e) {
				throw new NexusException(e.getMessage());
			}
		}
	}

	private void writeCounterTimer(Detector detector, double newData[]) throws NexusException {
		int[] startPos = generateDataStartPos(dataStartPosPrefix, null);
		int[] stop = generateDataStop(startPos, null);
		int[] dimArray = generateDataDim(false, dataDimPrefix, null);

		logger.debug("Writing data for Detector (" + detector.getName() + ") to NeXus file.");

		// Navigate to correct location in the file.
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, "instrument", NexusExtractor.NXInstrumentClassName);
		NexusUtils.addToAugmentPath(path, detector.getName(), NexusExtractor.NXDetectorClassName);
		GroupNode group = file.getGroup(path.toString(), true);

		for (int j = 0; j < detector.getExtraNames().length; j++) {
			DataNode data = file.getData(group, detector.getExtraNames()[j]);
			try {
				ILazyWriteableDataset lazy = data.getWriteableDataset();
				lazy.setSlice(null, DatasetFactory.createFromObject(newData[j]).reshape(dimArray), SliceND.createSlice(lazy, startPos, stop));
			} catch (Exception e) {
				throw new NexusException(e.getMessage());
			}
		}
	}

	private void writeCounterTimer(Detector detector) throws NexusException {
		double[] newData = extractDoubleData(detector.getName());
		writeCounterTimer(detector, newData);
	}

	@Override
	public int getCurrentScanIdentifier() {
		try {
			return getScanNumber();
		} catch (Exception e) {
			logger.error("Error getting scanIdentifier", e);
		}
		return -1;
	}

	public void setNexusFileNameTemplate(String nexusFileNameTemplate) throws Exception {
		this.nexusFileNameTemplate = nexusFileNameTemplate;
		// We calculate some probable paths now so that the probable
		// file name and path are known should the intended file path
		// be queried before being written. (NOTE this is happening in
		// XasAsciiNexusDataWriter currently.
		this.nexusFileName = String.format(nexusFileNameTemplate, getScanNumber());
		this.nexusFileUrl = dataDir + nexusFileName;
	}

	public String getNexusFileNameTemplate(){
		return this.nexusFileNameTemplate;
	}

	@Override
	public void addDataWriterExtender(final IDataWriterExtender e) {
		super.addDataWriterExtender(e);
		if (this.createSrsFile) {
			logger.warn("NexusDataWriter no longer disables the creation of SRS data files when a DataWriterExtender is added: "+e.toString());
		}
		/* This line prevents SRS data files from being written on most beamlines, where a DataWriterExtender is added for the FileRegistrar
		this.createSrsFile = false;
		*/
	}

	public boolean isFirstData() {
		return firstData;
	}

	private static Set<String> metadatascannables = new HashSet<String>();

	private static Map<String, ScannableWriter> locationmap = new HashMap<String, ScannableWriter>();

	private static Map<String, HashSet<String>> metadataScannablesPerDetector = new HashMap<String, HashSet<String>>();

	private boolean weKnowTheLocationFor(String scannableName) {
		return locationmap.containsKey(scannableName);
	}

	private Object getSDPositionFor(String scannableName) {
		int index = thisPoint.getScannableNames().indexOf(scannableName);
		return thisPoint.getPositions().get(index);
	}

	private void makeMetadataScannableFallback(GroupNode group, Scannable scannable, Object position) throws NexusException {
		String[] inputNames = scannable.getInputNames();
		String[] extraNames = scannable.getExtraNames();
		// FIXME ideally this would work for non-doubles as well
		// FIXME this needs to bring in the units
		Double[] positions = ScannableUtils.objectToArray(position);

		logger.debug("Writing data for scannable (" + scannable.getName() + ") to NeXus file.");

		// Navigate to correct location in the file.
		String nxDirName = "before_scan";
		String nxClass = "NXcollection";
		// Navigate to correct location in the file.
		GroupNode g = file.getGroup(NexusUtils.addToAugmentPath(NexusUtils.addToAugmentPath(new StringBuilder(file.getPath(group)), nxDirName, nxClass),
				scannable.getName(), nxClass).toString(), true);

		for (int i = 0; i < inputNames.length; i++) {
			NexusUtils.writeDouble(file, g, inputNames[i], positions[i]);
		}
		for (int i = 0; i < extraNames.length; i++) { // TODO check if position index is correct here
			NexusUtils.writeDouble(file, g, extraNames[i], positions[i]);
		}
	}

	private void makeMetadataScannables(GroupNode group, Set<String> metadatascannablestowrite) throws NexusException {
		for(String scannableName: metadatascannablestowrite) {
			try {
				Scannable scannable = (Scannable) InterfaceProvider.getJythonNamespace().getFromJythonNamespace(scannableName);
				Object position = scannable.getPosition();
				if (weKnowTheLocationFor(scannableName)) {
					locationmap.get(scannableName).makeScannable(file, group, scannable, position, new int[] {1});
				} else {
					makeMetadataScannableFallback(group, scannable, position);
					// put in default location (NXcollection with name metadata)
				}
			} catch (NexusException e) {
				throw e;
			} catch (Exception e) {
				logger.error("error getting "+scannableName+" from namespace or reading position from it.", e);
			}
		}
	}

	public static Set<String> getMetadatascannables() {
		return metadatascannables;
	}

	public static void setMetadatascannables(Set<String> metadatascannables) {
		if (metadatascannables == null)
			NexusDataWriter.metadatascannables = new HashSet<String>();
		else
			NexusDataWriter.metadatascannables = metadatascannables;
	}

	public static Map<String, ScannableWriter> getLocationmap() {
		return locationmap;
	}

	public static void setLocationmap(Map<String, ScannableWriter> locationmap) {
		if (locationmap == null)
			NexusDataWriter.locationmap = new HashMap<String, ScannableWriter>();
		else
			NexusDataWriter.locationmap = locationmap;
	}

	public static Map<String, HashSet<String>> getMetadataScannablesPerDetector() {
		return metadataScannablesPerDetector;
	}

	public static void setMetadataScannablesPerDetector(Map<String, HashSet<String>> metadataScannablesPerDetector) {
		if (metadataScannablesPerDetector == null) {
			NexusDataWriter.metadataScannablesPerDetector = new HashMap<String, HashSet<String>>();
		} else {
			NexusDataWriter.metadataScannablesPerDetector = metadataScannablesPerDetector;
		}
	}

	public String getNexusFileName() {
		return nexusFileName;
	}

	private static Object getFillValue(int dtype) {
		switch (dtype) {
		case Dataset.FLOAT64: {
			String floatFill = LocalProperties.get("gda.nexus.floatfillvalue", "nan");
			return floatFill.equalsIgnoreCase("nan") ? Double.NaN : Double.parseDouble(floatFill);
		}
		case Dataset.FLOAT32: {
			String floatFill = LocalProperties.get("gda.nexus.floatfillvalue", "nan");
			return floatFill.equalsIgnoreCase("nan") ? Float.NaN : Float.parseFloat(floatFill);
		}
		case Dataset.INT8:
			return (byte) 0;
		case Dataset.INT16:
			return (short) 0;
		case Dataset.INT32:
			return 0;
		case Dataset.INT64:
			return (long) 0;
		default:
			return null;
		}
	}
}

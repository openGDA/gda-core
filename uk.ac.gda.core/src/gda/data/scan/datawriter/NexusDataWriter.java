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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toCollection;
import static org.eclipse.dawnsci.nexus.NexusConstants.DATA_AXES;
import static org.eclipse.dawnsci.nexus.NexusConstants.DATA_INDICES_SUFFIX;
import static org.eclipse.dawnsci.nexus.NexusConstants.DATA_SIGNAL;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.appender.INexusFileAppender;
import org.eclipse.dawnsci.nexus.appender.INexusFileAppenderService;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.template.NexusTemplate;
import org.eclipse.dawnsci.nexus.template.NexusTemplateService;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.InterfaceUtils;
import org.eclipse.january.dataset.SliceND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.ServiceHolder;
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
import gda.data.scan.datawriter.scannablewriter.SingleScannableWriter;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.NexusDetector;
import gda.device.scannable.ScannableUtils;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.scan.IScanDataPoint;
import gda.scan.Scan;
import gda.scan.ScanDataPoint;
import gda.util.QuantityFactory;
import gda.util.TypeConverters;
import uk.ac.diamond.daq.api.messaging.messages.SwmrStatus;

/**
 * DataWriter that outputs NeXus files and optionally a SRS/Text file as well.
 */
public class NexusDataWriter extends DataWriterBase implements INexusDataWriter {

	public static final String GROUP_NAME_INSTRUMENT = "instrument";
	public static final String GROUP_NAME_MEASUREMENT = "measurement";

	private static final Logger logger = LoggerFactory.getLogger(NexusDataWriter.class);

	private static final int[] SINGLE_SHAPE = new int[] { 1 };
	private static final int[] SCALAR_SHAPE = new int[0];

	// Always format with 3 decimal places of Millis, prevent truncating by default formatters being unreadable by DateDatasetImpl
	private static final DateTimeFormatter MILLISECOND_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

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

	/** Maximum length of filenames that can be linked from Nexus files */
	private static final int MAX_DATAFILENAME = 255;

	/** Default SRS writing */
	public static final boolean CREATE_SRS_FILE_BY_DEFAULT = true;

	/** Property that if enabled writes a measurement group that contains the data printed to the console during the scan */
	public static final String GDA_NEXUS_CREATE_MEASUREMENT_GROUP = "gda.nexus.writeMeasurementGroup";

	/** Property that if enabled create a link called <code>positioners</code> to the <code>before_scan</code> */
	private static final String GDA_NEXUS_LINK_POSITIONERS_GROUP = "gda.nexus.linkPositionersGroup";

	/** Property to determine whether the nexus title needs to be prettified*/
	private static final String GDA_NEXUS_PRETTIFY_TITLE = "gda.nexus.prettifyTitle";

	/** Are we going to write an SRS file as well? */
	private boolean createSrsFile = CREATE_SRS_FILE_BY_DEFAULT;

	// beamline name
	protected String beamline = null;

	/** Directory to write data to */
	protected String dataDir = null;

	/** file name with no extension */
	protected String fileBaseName = null;
	protected String fileBaseUrl = null;

	// file names
	private String nexusFileNameTemplate = null;

	private String nexusFileName = null;

	// Fully qualified filenames
	private String nexusFileUrl = null;

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

	private final ZonedDateTime startTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

	private List<SelfCreatingLink> axesDataNodes;

	private boolean firstData = true;

	private boolean writingMeasurementGroup = false;

	private int scanPointNumber = -1;

	private IScanDataPoint thisPoint;

	private Metadata metadata = null;

	private boolean fileNumberConfigured = false;

	/**
	 * Constructor. This attempts to read the java.property which defines the beamline name.
	 */
	public NexusDataWriter() {
		super();

		setupProperties();

		// Check to see if we want to create a text/SRS file as well.
		if (createSrsFile) {
			logger.info("NexusDataWriter is configured to also create SRS data files");
			srsFile = new SrsDataFile();
		}
	}

	public NexusDataWriter(int fileNumber) {
		this();
		configureScanNumber(fileNumber);
	}

	private void setupProperties() {
		logger.debug("Setting up properties...");

		metadata = GDAMetadataProvider.getInstance();

		beamline = metadata.getMetadataValue(GROUP_NAME_INSTRUMENT, LocalProperties.GDA_INSTRUMENT, "base"); // 'base' is the default beamline line

		// Check to see if the data directory has been defined.
		dataDir = InterfaceProvider.getPathConstructor().createFromDefaultProperty();
		if (dataDir == null) {
			// this java property is compulsory - stop the scan
			throw new IllegalStateException("cannot work out data directory - cannot create a new data file.");
		}

		if (beforeScanMetaData == null) {
			String metaDataProviderName = LocalProperties.get(GDA_NEXUS_METADATAPROVIDER_NAME);
			if (StringUtils.hasLength(metaDataProviderName)) {
				NexusTreeAppender metaDataProvider = Finder.find(metaDataProviderName);
				InterfaceProvider.getTerminalPrinter().print("Getting meta data before scan");
				beforeScanMetaData = new NexusTreeNode("before_scan", NexusExtractor.NXCollectionClassName, null);
				metaDataProvider.appendToTopNode(beforeScanMetaData);
			}
		}

		createSrsFile = LocalProperties.check(GDA_NEXUS_CREATE_SRS, CREATE_SRS_FILE_BY_DEFAULT);

		if (LocalProperties.check(GDA_NEXUS_SWMR, false)) {
			swmrStatus = SwmrStatus.ENABLED;
		}
	}

	/**
	 * Creates a link called <code>positioners</code> to the <code>before_scan</code> entry.
	 *
	 * @param beforeScanGroupPath
	 */
	private void createPositionersGroupLink() {
		String beforeScanGroupPath = '/' + entryName + "/before_scan";
		String positionersPath = '/' + entryName + "/positioners";
		try {
			file.link(beforeScanGroupPath, positionersPath);
		} catch (NexusException e) {
			logger.error("Failed to create positioners link '{}' to '{}", positionersPath, beforeScanGroupPath, e);
		}
	}

	@Override
	public void setBeforeScanMetaData(INexusTree beforeScanMetaData) {
		this.beforeScanMetaData = beforeScanMetaData;
	}

	@Override
	public synchronized void configureScanNumber(int scanNumber) {
		if (!fileNumberConfigured) {
			logger.debug("Configuring file number");
			if (scanNumber > 0) {
				// the scan or other datawriter has set the id
				this.scanNumber = scanNumber;
			} else if (this.scanNumber <= 0) {
				// not set in a constructor so get from num tracker
				try {
					NumTracker runNumber = new NumTracker(beamline);
					// Get the next run number
					this.scanNumber = runNumber.incrementNumber();
				} catch (IOException e) {
					logger.error("Could not instantiate NumTracker", e);
					throw new IllegalStateException("Could not instantiate NumTracker in NexusDataWriter()", e);
				}
			}
			// needs to use the same scan number
			if (createSrsFile) {
				try {
					srsFile.configureScanNumber(this.scanNumber);
				} catch (Exception e) {
					logger.error("Failed to configure SRS file number", e);
					throw new IllegalStateException(e);
				}
			}
			fileNumberConfigured = true;
			logger.debug("Scan number set to '{}'", this.scanNumber);
		}

		// Now we know the scan number construct the file name
		constructFileName();
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

	/**
	 * The dimensions of the scan {@link Scan#getDimension()}
	 */
	private int[] scanDimensions;

	/**
	 * Fields present for convenience The location within data at which data is to be written - not taking into account
	 * the dimensions of the data itself The length of this array should match the length of scanDimensions and the
	 * values are calculated from the getCurrentPointNumber() method of the ScanDataPoint. For a 2d scan of 4 x 3 e.g.
	 * scan simpleScannable1, 0., 3, 1., simpleScannable2, 0., 2, 1. the values of scanPointNumbers will be [0,0],
	 * [0,1], [0,2], [1,0], [1,1], [1,2], [2,0], [2,1], [2,2], [3,0], [3,1], [3,2]
	 */
	private int[] dataStartPosPrefix;

	/**
	 * Fields present for convenience The dimensions of the to be written - not taking into account the dimensions of
	 * the data itself The length of this array should match the length of scanDimensions and the values are all 1 but
	 * for the first which is ILazyWriteableDataset.UNLIMITED.
	 */
	private int[] dataDimPrefix;

	private INexusTree beforeScanMetaData;

	/** Performance instrumentation logging the total time spent writing */
	private long totalWritingTime;

	/** Flag to indicate if SWMR is disabled, enabled or active */
	private SwmrStatus swmrStatus = SwmrStatus.DISABLED;

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
			logger.warn("The DataWriter({}) and the DataPoint({}) disagree about the point number!", scanPointNumber,
					dataPoint.getCurrentPointNumber());
		}
		dataStartPosPrefix = generateStartPosPrefix(thisPoint.getCurrentPointNumber(), thisPoint.getScanDimensions());

		try {
			if (firstData) {
				scanDimensions = dataPoint.getScanDimensions();
				dataDimPrefix = new int[scanDimensions.length];
				Arrays.fill(dataDimPrefix, 1);
				prepareFileAndStructure();
				if (file instanceof NexusFileHDF5 && LocalProperties.check(GDA_NEXUS_SWMR, false)) {
					((NexusFileHDF5) file).activateSwmrMode();
					swmrStatus  = SwmrStatus.ACTIVE;
				}
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

			if (writingMeasurementGroup) {
				writeMeasurementGroup();
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
			super.addData(this, dataPoint);
		}

		// Finished addData do performance instrumentations
		long finishTime = System.nanoTime();
		totalWritingTime += finishTime - startTime;
	}

	private void writeMeasurementGroup() {
		try {
			final GroupNode measurementGroup = getMeasurementGroup();

			final String[] headers = getHeaders();
			final Double[] data = thisPoint.getAllValuesAsDoubles();

			final int[] startPos = generateDataStartPos(dataStartPosPrefix, null);
			final int[] stop = generateDataStop(startPos, null);
			IntStream.range(0, headers.length).filter(i -> data[i] != null).forEach(i -> {
				try {
					// note we assume that the same fields are null for each point
					final DataNode dataNode = file.getData(measurementGroup, headers[i]);
					final ILazyWriteableDataset dataset = dataNode.getWriteableDataset();
					dataset.setSlice(null, DatasetFactory.createFromObject(data[i]),
							SliceND.createSlice(dataset, startPos, stop));
				} catch (DatasetException | NexusException e) {
					logger.error("Error writing measurement group entry: {}", header, e);
				}
			});

		}
		catch (IllegalStateException | NexusException e) {
			logger.error("Not writing measurement data group", e);
		}
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

	private void writeGenericDetector(String detectorName, int[] dataDimensions, double[] newData) throws NexusException {

		// Navigate to correct location in the file.
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, GROUP_NAME_INSTRUMENT, NexusExtractor.NXInstrumentClassName);
		NexusUtils.addToAugmentPath(path, detectorName, NexusExtractor.NXDetectorClassName);
		GroupNode g = file.getGroup(path.toString(), false);

		int[] startPos = generateDataStartPos(dataStartPosPrefix, dataDimensions);
		int[] dimArray = generateDataDim(false, dataDimPrefix, dataDimensions);
		int[] stop = generateDataStop(startPos, dataDimensions);

		// Open data array.
		DataNode d = file.getData(g, "data");
		ILazyWriteableDataset lazy;
		if (d == null) {
			lazy = NexusUtils.createLazyWriteableDataset("data", Double.class, dimArray, null, null);
			lazy.setFillValue(getFillValue(Double.class));
			file.createData(g, lazy);
		} else {
			lazy = d.getWriteableDataset();
		}
		Dataset ds = DatasetFactory.createFromObject(newData).reshape(dimArray);
		try {
			lazy.setSlice(null, ds, SliceND.createSlice(lazy, startPos, stop));
		} catch (DatasetException e) {
			throw new NexusException("Error writing data from " + detectorName, e);
		}
	}

	private void writeDetector(Detector detector) throws DeviceException, NexusException {
		if (detector instanceof NexusDetector) {
			writeNexusDetector((NexusDetector) detector);
		} else if (detector.createsOwnFiles()) {
			writeFileCreatorDetector(detector.getName(), extractFileName(detector.getName()));
		} else if (detector.getExtraNames() != null && detector.getExtraNames().length > 0) {
			writeCounterTimer(detector);
		} else {
			writeGenericDetector(detector.getName(), detector.getDataDimensions(),
					extractDoubleData(detector.getName()));
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
	 * Writes the data encapsulated in the given {@link INexusTree} node into the given {@link NexusFile} within the
	 * given {@link GroupNode}. If {@code makeData} is <code>true</code>, the data structure is created, otherwise it
	 * is assumed to already exist and is written to for the current point.
	 * <p>
	 * If the return value is not <code>null</code>, it is the group node created for the given sub-tree.
	 *
	 * @param file the nexus file to write to
	 * @param group the parent group to write to
	 * @param tree the tree containing the data to write
	 * @param makeData <code>true</code> to create the nexus nodes, <code>false</code> otherwise
	 * @param attrOnly
	 * @param links
	 * @return the group node created, if any, <code>null</code> otherwise
	 * @throws NexusException
	 */
	private GroupNode writeHere(NexusFile file, GroupNode group, INexusTree tree, boolean makeData, boolean attrOnly,
			List<SelfCreatingLink> links) throws NexusException {
		// TODO: this method is huge and is seriously is need of refactoring
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
				Path absExtPath = getReal(Paths.get(parts[0]));
				String address = parts[1];
				File f = absExtPath.toFile();
				if (!f.exists()) {
					logger.warn("file {} does not exist at time of adding link", absExtPath);
				}
				Path nxsFile = getReal(Paths.get(nexusFileUrl));
				Path nxsParent = nxsFile.getParent();
				Path relativize = nxsParent.relativize(absExtPath);
				String relativeLink = "nxfile://" + relativize + "#" + address;
				String path = file.getPath(group);
				try {
					URI relativeLinkUri = new URI(relativeLink);
					file.linkExternal(relativeLinkUri, path + name, false);
				} catch (URISyntaxException e) {
					throw new NexusException("Could not create URI from: " + relativeLink, e);
				}
				links.add(new ExternalNXlink(name, relativeLink));

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
						node = file.getData(group, parent.getName());
					} else {
						node = group;
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
			return null;
		}
		if (attrOnly) {
			// we're only allowed to create attributes at this point, so return here
			return null;
		}

		if (!name.isEmpty() && !nxClass.isEmpty()) {
			// write the data as a group, creating the group first if not SDS
			if (!nxClassIsSDS) {
				group = file.getGroup(group, name, nxClass, true);
				newGroup = group;
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
					int compression;
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
					// TODO: only enable compression if the chunk size makes it worthwhile
					lazy.setFillValue(getFillValue(InterfaceUtils.getElementClass(sds.getInterface())));
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

		return newGroup;
	}

	private boolean isSDS(String className, boolean hasParent) {
		return (NexusExtractor.SDSClassName.equals(className) || (className.isEmpty() && hasParent));
	}

	private void writeNexusDetector(NexusDetector detector) throws NexusException {
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		GroupNode pg = file.getGroup(path.toString(), false);
		NexusUtils.addToAugmentPath(path, GROUP_NAME_INSTRUMENT, NexusExtractor.NXInstrumentClassName);
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
		final Object object = extractDetectorObject(detectorName);
		return TypeConverters.toDoubleArray(object);
	}

	private String extractFileName(String detectorName) {
		return (String) extractDetectorObject(detectorName);
	}

	private INexusTree extractNexusTree(String detectorName) {
		return ((NexusTreeProvider) extractDetectorObject(detectorName)).getNexusTree();
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
		logger.debug("completeCollection() called with file={}, entryName={}", file, entryName);
		final ZonedDateTime finishTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		if (file != null) {
			// In some error conditions, this can be called twice with 'file' being null the second time
			GroupNode g = file.getGroup(NexusUtils.createAugmentPath(entryName, NexusExtractor.NXEntryClassName), false);
			ILazyWriteableDataset endTime = file.getData(g, NXentry.NX_END_TIME).getWriteableDataset();
			endTime.setSlice(
					null,
					DatasetFactory.createFromObject(MILLISECOND_DATE_FORMAT.format(finishTime)), new SliceND(SCALAR_SHAPE));
			applyTemplates();
		}
		releaseFile();
		super.completeCollection();
		int numberOfPoints = scanPointNumber + 1;
		// Log the performance info. Convert ns into ms, and report per point to make comparable
		logger.info("Writing {} points to NeXus took an average of {} ms per point", numberOfPoints,
				(totalWritingTime / 1.0E6) / numberOfPoints);
	}

	private void applyTemplates() throws NexusException {
		final NexusTemplateService templateService = ServiceHolder.getNexusTemplateService();
		if (templateService != null) {
			for (String templateFilePath : getConfiguration().getNexusTemplateFiles()) {
				Path filePath = Paths.get(templateFilePath);
				if (!filePath.isAbsolute()) {
					// if the file path is relative, resolve it relative to gda.var
					final String gdaVar = InterfaceProvider.getPathConstructor().createFromProperty(LocalProperties.GDA_VAR_DIR);
					templateFilePath = Paths.get(gdaVar).resolve(filePath).toString();
				}
				final NexusTemplate template = templateService.loadTemplate(templateFilePath);
				template.apply(file);
			}
		}
	}

	/**
	 * Releases the file handle.
	 */
	protected void releaseFile() {
		try {
			if (file != null) {
				file.flush();
				file.close();
			}
			if (createSrsFile) {
				srsFile.releaseFile();
			}

		} catch (Exception e) {
			String error = "Error occurred when closing data file(s): ";
			logger.error(error, e);
			terminalPrinter.print(error);
			terminalPrinter.print(e.getMessage());
		} finally {
			file = null;
		}
	}

	@Override
	public String getCurrentFileName() {
		return nexusFileUrl;
	}

	private void prepareFileAndStructure() throws Exception {
		createNextFile();
		makeMetadata();
		makeScannables();
		makeDetectors();
		if (LocalProperties.check(GDA_NEXUS_CREATE_MEASUREMENT_GROUP, false)) {
			makeMeasurementGroup();
		}
		if(LocalProperties.check(GDA_NEXUS_LINK_POSITIONERS_GROUP, false)) {
			createPositionersGroupLink();
		}
	}

	/** Make a measurement group and data set then add it to the nexus file.
	 *
	 * @throws NexusException
	 */
	private void makeMeasurementGroup() throws NexusException {
		logger.debug("Making 'measurement' group");
		try {
			final GroupNode measurementGroup = getMeasurementGroup();

			final String[] headers = getHeaders();
			final Double[] firstPointData = thisPoint.getAllValuesAsDoubles();

			final int[] dataDimensions = generateDataDim(true, scanDimensions, null);
			final int[] chunking = NexusUtils.estimateChunking(scanDimensions, 8);
			final int[] dataIndices = IntStream.range(0, scanDimensions.length).toArray();

			String lastHeader = null; // header of the last field with non-null data
			for (int fieldIndex = 0; fieldIndex < headers.length; fieldIndex++) {
				if (firstPointData[fieldIndex] == null) continue; // don't write null valued fields (these will be from detectors)

				final String header = headers[fieldIndex];
				final ILazyWriteableDataset lazyWriteableDataset = NexusUtils.createLazyWriteableDataset(
						header, Double.class, dataDimensions, null, chunking);
				lazyWriteableDataset.setFillValue(getFillValue(Double.class));
				file.createData(measurementGroup, lazyWriteableDataset); // Makes the dataset
				NexusUtils.writeAttribute(file, measurementGroup, header + DATA_INDICES_SUFFIX, dataIndices);
				lastHeader = header;
			}

			// Add NXData metadata
			NexusUtils.writeAttribute(file, measurementGroup, DATA_SIGNAL, lastHeader);
			// This should be 1D as we are not writing complex detector data here just values

			// calculate the axes headers, assuming the first field for each scannable is the axes field
			final String[] axesHeaders = thisPoint.getScannables().stream()
					.limit(scanDimensions.length)
					.map(scn -> scn.getInputNames()[0])
					.toArray(String[]::new);
			NexusUtils.writeAttribute(file, measurementGroup, DATA_AXES, axesHeaders);

			// Set to true if creating the group succeeds
			writingMeasurementGroup = true;
		} catch (IllegalStateException e) {
			logger.error("Not writing measurement data group", e);
		}
	}

	/**
	 * @return An NXCollection group node to hold the measurement
	 *
	 * @throws NexusException
	 */
	private GroupNode getMeasurementGroup() throws NexusException {
		final StringBuilder groupPath = new StringBuilder();
		NexusUtils.addToAugmentPath(groupPath, entryName, NexusExtractor.NXEntryClassName); // /entry1/
		NexusUtils.addToAugmentPath(groupPath, GROUP_NAME_MEASUREMENT, NexusExtractor.NXDataClassName);// /entry1/measurement/
		return file.getGroup(groupPath.toString(), true); // Makes the group if it doesn't exist
	}

	/**
	 * @return An ordered list of combined position and detector headers
	 */
	private String[] getHeaders() {
		final List<String> positionHeaders = thisPoint.getPositionHeader();
		final List<String> detectorHeaders = thisPoint.getDetectorHeader();

		// Create a set first to detect duplicates - use a LinkedHashSet to maintain insertion order
		final Set<String> headersSet = Stream.concat(positionHeaders.stream(), detectorHeaders.stream()).collect(toCollection(LinkedHashSet::new));
		if (headersSet.size() != positionHeaders.size() + detectorHeaders.size()) {
			throw new IllegalStateException("Duplicates in position and detector headers: " + positionHeaders + " & " + detectorHeaders);
		}

		return headersSet.toArray(String[]::new); // return as array
	}

	private void makeMetadata() {
		try {
			GroupNode g = file.getGroup(NexusUtils.createAugmentPath(entryName, NexusExtractor.NXEntryClassName), true);

			NexusUtils.writeString(file, g, "scan_command", thisPoint.getCommand());
			String scanid = metadata.getMetadataValue(GDAMetadataProvider.SCAN_IDENTIFIER);
			NexusUtils.writeString(file, g, "scan_identifier", scanid.isEmpty() ? thisPoint.getUniqueName() : scanid);
			NexusUtils.writeIntegerArray(file, g, "scan_dimensions", thisPoint.getScanDimensions());
			if (!g.containsNode("title")) {
				String title = metadata.getMetadataValue("title");
				if(title == null || title.isEmpty()) {
					// If no title is set use the scan command as the title (DAQ-1861)
					title = createTitle(thisPoint.getCommand());
				}
				NexusUtils.writeString(file, g, "title", title);
			}
			NexusUtils.writeString(file, g, NXentry.NX_START_TIME,  MILLISECOND_DATE_FORMAT.format(startTime));
			ILazyWriteableDataset endTime = NexusUtils.createLazyWriteableDataset(
					NXentry.NX_END_TIME, String.class, SCALAR_SHAPE, null, null);
			file.createData(g, endTime);
			createCustomMetaData(g);
		} catch (Exception e) {
			logger.warn("error writing less important scan information", e);
		}
	}

	/*
	 * @param command
	 * 			- The command being run
	 * This will create a sanitized version of the command to use as the title
	 */
	private String createTitle(String command) {
		if (LocalProperties.check(GDA_NEXUS_PRETTIFY_TITLE)) {
			String newTitle = List.of(command.replaceAll("[^a-zA-Z]", " ").split(" "))
					.stream().filter(word -> !word.isEmpty()).distinct().collect(Collectors.joining(" "));
			return newTitle.trim();
		}
		return command;
	}

	/**
	 * Override to provide additional meta data, if required. Does nothing otherwise.
	 *
	 * @param g
	 *
	 * @throws NexusException
	 */
	// allow inheriting classes to throw this exception
	protected void createCustomMetaData(GroupNode g) throws NexusException {

		if (beforeScanMetaData != null) {
			writeHere(file, g, beforeScanMetaData, true, false, null);
		}

		getConfiguration().getMetadata().entrySet().stream().forEach(e -> {
			try {
				String data = metadata.getMetadataValue(e.getKey());
				String aPath = file.getPath(g) + e.getValue();
				final String name = NexusUtils.getName(aPath);
				if (name != null && !name.isEmpty()) {
					aPath = aPath.substring(0, aPath.lastIndexOf(name));
				}
				if (data != null && !data.isEmpty()) {
					NexusUtils.writeString(
							file,
							file.getGroup(aPath, true),
							name,
							data);
					logger.debug("Wrote {} to '{}'", e.getKey(), aPath);
				} else {
					logger.trace("Not writing '{}' as metadata not set", e.getKey());
				}
			} catch (NexusException | IllegalArgumentException e1) {
				logger.error("Could not write entry for {}", e.getKey(), e1);
			}
		});
	}

	protected String getGroupClassFor(@SuppressWarnings("unused") Scannable s) {
		return "NXpositioner";
	}

	/**
	 * Writes configured scannables (including monitors), and calculates and writes metadata scannables.
	 * The list of scannables and monitors to write at each point of the scan are retrieved by calling
	 * {@link ScanDataPoint#getScannables()}.
	 * <p>
	 * <ul>
	 * <li>calls {@link #makeConfiguredScannables(Collection)} to create the nexus objects
	 * (nodes and datasets) for the list of scannables and monitors passed in that have
	 * are configured, i.e. they have an entry in the locationMap, whose value is the {@link ScannableWriter}
	 * that will create those datasets and is configured with the paths to write to;</li>
	 * <li>calls {@link #makeDefaultScannables(Collection)} for the scannables that do not have
	 * an entry in the location map, to create their nexus objects is a default way;<li>
	 * <li>calls {@link #makeMetadataScannables(Set)} to calculate and write the metadata scannables
	 * i.e. scannables whose datasets have a single value written at the start of the scan.
	 * </ul>
	 * <p>
	 * This method is run as part of {@code prepareFileAndStructure()}, called when the first {@link ScanDataPoint} is written.
	 * <p>
	 *
	 * @param scannablesAndMonitors set of scannables and monitors whose value is recorded at each point of the scan
	 * @throws NexusException
	 */
	private void makeScannables() {
		// group the scannables and monitors in the scan by whether they have a ScannableWriter configured
		final Map<Boolean, List<Scannable>> scannables = thisPoint.getScannables().stream()
				.collect(partitioningBy(this::hasConfiguredWriter));

		axesDataNodes = new ArrayList<>();
		try {
			makeConfiguredScannables(scannables.getOrDefault(true, emptyList()));
			// only use default writing for the scannables that don't have a ScannableWriter configured
			makeDefaultScannables(scannables.getOrDefault(false, emptyList()));

			makeMetadataScannables();
		} catch (NexusException e) {
			logger.error("Error making configured scannables and monitors", e);
		}
	}

	private void makeMetadataScannables() throws NexusException {
		final MetadataScannableCalculator metadataScannableCalculator = new MetadataScannableCalculator(
				thisPoint.getDetectorNames(), thisPoint.getScannableNames());
		final Set<String> metadataScannableNames = metadataScannableCalculator.calculateMetadataScannableNames();
		makeMetadataScannables(metadataScannableNames);
	}

	/**
	 * Creates the nexus groups and dataset for scannables that have an entry configured in the
	 * location map. The entry is created by the {@link ScannableWriter} that is the value of that
	 * entry. If the writer is a {@link SingleScannableWriter} (as is normally the case) this writes
	 * each element of the position array of the {@link Scannable} to the path within the
	 * array returned by {@link SingleScannableWriter#getPaths()} with the corresponding index.
	 * @param scannablesAndMonitors scannable and monitors to write
	 * @throws NexusException
	 */
	private void makeConfiguredScannables(Collection<Scannable> scannablesAndMonitors) throws NexusException {
		if (scannablesAndMonitors.isEmpty()) return;

		final String firstScannableName = thisPoint.getScannableNames().get(0);
		final GroupNode group = file.getGroup(NexusUtils.createAugmentPath(entryName, NexusExtractor.NXEntryClassName), false);
		for (Scannable scannable : scannablesAndMonitors) {
			final String scannableName = scannable.getName();
			final Optional<ScannableWriter> optWriter = getWriterForScannable(scannableName);
			if (!optWriter.isPresent()) throw new NexusException("No writer found for scannable: " + scannableName); // not possible as already checked
			final ScannableWriter writer = optWriter.get();
			axesDataNodes.addAll(writer.makeScannable(file, group, scannable, getSDPositionFor(scannableName),
					generateDataDim(false, scanDimensions, null), scannableName.equals(firstScannableName)));
		}
	}

	/**
	 * Writes scannables (including monitors) that do not have a configured {@link ScannableWriter}
	 * in a default way. Essentially a {@link NXpositioner} is created within the {@link NXinstrument},
	 * and a dataset is created inside that group for each name in
	 * both {@link Scannable#getInputNames()} and {@link Scannable#getExtraNames()}.
	 *
	 * @param scannables
	 */
	private void makeDefaultScannables(Collection<Scannable> scannables) {
		final StringBuilder axislistBuilder = new StringBuilder("1");
		for (int j = 2; j <= thisPoint.getScanDimensions().length; j++) {
			axislistBuilder.append(String.format(",%d", j));
		}
		final String axislist = axislistBuilder.toString();

		try {
			final StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
			NexusUtils.addToAugmentPath(path, GROUP_NAME_INSTRUMENT, NexusExtractor.NXInstrumentClassName);
			final int[] dataDim = generateDataDim(true, scanDimensions, null);

			int inputNameIndex = 0;
			int extraNameIndex = 0;
			for (Scannable scannable : scannables) {
				final String[] inputNames = scannable.getInputNames();
				final String[] extraNames = scannable.getExtraNames();

				final String groupName = getGroupClassFor(scannable);
				final StringBuilder groupPath = NexusUtils.addToAugmentPath(new StringBuilder(path), scannable.getName(), groupName);
				final GroupNode groupNode = file.getGroup(groupPath.toString(), true);

				// Check to see if the scannable will write its own info into NeXus
				if (scannable instanceof INeXusInfoWriteable) {
					((INeXusInfoWriteable) scannable).writeNeXusInformation(file, groupNode);
				}

				// loop over input names...
				for (String element : inputNames) {
					// Create the data array (with an unlimited scan dimension)
					final int[] chunking = NexusUtils.estimateChunking(scanDimensions, 8);
					final ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset(element, Double.class, dataDim, null, chunking);
					lazy.setFillValue(getFillValue(Double.class));
					final DataNode data = file.createData(groupNode, lazy);

					// Get a link ID to this data set.
					NexusUtils.writeStringAttribute(file, data, "local_name", String.format("%s.%s", scannable.getName(), element));

					// assign axes
					if (thisPoint.getScanDimensions().length > 0) {
						// TODO
						// in all likelihood this will not give the right axis assignment
						// for scannables with multiple input names
						// this is not solvable given the current data in SDP

						if ((thisPoint.getScanDimensions().length) > inputNameIndex) {
							NexusUtils.writeStringAttribute(file, data, "label", String.format("%d", inputNameIndex + 1));
							NexusUtils.writeStringAttribute(file, data, "primary", "1");
						}
						NexusUtils.writeStringAttribute(file, data, "axis", axislist);
					}

					axesDataNodes.add(new SelfCreatingLink(data));
					inputNameIndex++;
				}

				for (String element : extraNames) {
					// Create the data array (with an unlimited scan dimension)
					final ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset(element, Double.class, dataDim, null, null);
					lazy.setFillValue(getFillValue(Double.class));
					final DataNode data = file.createData(groupNode, lazy);

					// Get a link ID to this data set.
					NexusUtils.writeStringAttribute(file, data, "local_name", String.format("%s.%s", scannable.getName(), element));

					if (thisPoint.getDetectorNames().isEmpty() && extraNameIndex == 0) {
						NexusUtils.writeStringAttribute(file, data, "signal", "1");
					}

					axesDataNodes.add(new SelfCreatingLink(data));
					extraNameIndex++;
				}

				addDeviceMetadata(scannable.getName(), groupNode);
			}

		} catch (NexusException e) {
			final String error = "NeXus file creation failed during makeScannables";
			logger.error(error, e);
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
		for (SelfCreatingLink id : axesDataNodes) {
			try {
				id.create(file, group);
			} catch (NexusException e) {
				logger.warn("Error in makeLink (reported to NX group) for {} with error", id, e);
			}
		}
	}

	/**
	 * Create NXdetector class for each Detector.
	 */
	private void makeDetectors() {
		try {
			final Collection<Detector> detectors = thisPoint.getDetectors();

			if (detectors.isEmpty()) {
				makeFallbackNXData();
				return;
			}

			// create an NXdetector for each detector...
			for (Detector detector : detectors) {
				try {
					makeDetectorEntry(detector);
				} catch (Exception e) {
					throw new DeviceException("Error making detector entry for detector " + detector.getName(), e);
				}
			}
		} catch (NexusException e) {
			String error = "NeXus file creation failed during makeDetectors: ";
			logger.error(error, e);
			terminalPrinter.print(error);
			terminalPrinter.print(e.getMessage());
		} catch (DeviceException de) {
			String error = "DeviceException during NeXus file creation: ";
			logger.error(error, de);
			terminalPrinter.print(error);
			terminalPrinter.print(de.getMessage());
		}
	}

	private void makeDetectorEntry(Detector detector) throws DeviceException, NexusException {
		logger.debug("Making NXdetector for {} in NeXus file.", detector.getName());

		final GroupNode detectorGroup;
		if (detector instanceof NexusDetector) {
			makeNexusDetectorGroups(detector); // a nexus detector may make multiple groups as some subclasses can control multiple actual detectors
			detectorGroup = null;
		} else if (detector.createsOwnFiles()) {
			detectorGroup = makeFileCreatorDetector(detector);
		} else if (detector.getExtraNames().length > 0) {
			detectorGroup = makeCounterTimer(detector);
		} else {
			detectorGroup = makeGenericDetector(detector);
		}

		if (detectorGroup != null) {
			addDeviceMetadata(detector.getName(), detectorGroup);
		}
	}

	private void makeNexusDetectorGroups(Detector detector) throws NexusException {
		logger.debug("Creating NexusTree entry in NeXus file.");
		INexusTree detTree = extractNexusTree(detector.getName());
		for (INexusTree det : detTree) {
			GroupNode deviceGroup = null;
			if (det.getNxClass().equals(NexusExtractor.NXDetectorClassName)) {
				deviceGroup = makeNexusDetector(detector, det);
			} else if (det.getNxClass().equals(NexusExtractor.NXMonitorClassName)) {
				// FIXME -- if this doesn't explode I am truly surprised
				GroupNode entryGroup = file.getGroup(NexusUtils.createAugmentPath(entryName, NexusExtractor.NXEntryClassName), false);
				deviceGroup = writeHere(file, entryGroup, det, firstData, false, null);
			}
			if (deviceGroup != null) {
				addDeviceMetadata(det.getName(), deviceGroup);
			}
		}
	}

	private void writeFileCreatorDetector(String detectorName, String dataFileName) throws NexusException {

		if (dataFileName.length() > MAX_DATAFILENAME) {
			logger.error(
					"The detector ({}) returned a file name (of length {}) which is greater than the max allowed length ({}).",
					detectorName.length(), dataFileName.length(), MAX_DATAFILENAME);
		}

		// Navigate to correct location in the file.
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, GROUP_NAME_INSTRUMENT, NexusExtractor.NXInstrumentClassName);
		NexusUtils.addToAugmentPath(path, detectorName, NexusExtractor.NXDetectorClassName);
		NexusUtils.addToAugmentPath(path, "data_file", NexusExtractor.NXNoteClassName);
		GroupNode group = file.getGroup(path.toString(), false);

		logger.debug("Filename received from detector: {}", dataFileName);

		// Now lets construct the relative (to the nexus data file) path to the file.
		if (dataFileName.startsWith(dataDir)) {
			dataFileName = dataFileName.substring(dataDir.length());
			// Check for a leading '/'
			if (!dataFileName.startsWith("/")) {
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

	private GroupNode makeFileCreatorDetector(Detector detector) throws NexusException {
		logger.debug("Creating File Creator Detector entry for {} in NeXus file.", detector.getName());

		// Navigate to the relevant section in file...
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, GROUP_NAME_INSTRUMENT, NexusExtractor.NXInstrumentClassName);
		NexusUtils.addToAugmentPath(path, detector.getName(), NexusExtractor.NXDetectorClassName);
		final GroupNode detectorGroup = file.getGroup(path.toString(), true);

		// Metadata items
		NexusUtils.writeString(file, detectorGroup, "description", "Generic GDA Detector - External Files");
		NexusUtils.writeString(file, detectorGroup, "type", "Detector");

		// Check to see if the detector will write its own info into NeXus
		if (detector instanceof INeXusInfoWriteable) {
			((INeXusInfoWriteable) detector).writeNeXusInformation(file, detectorGroup);
		}

		final GroupNode dataFileGroup = file.getGroup(detectorGroup, "data_file", NexusExtractor.NXNoteClassName, true);

		int[] dataDim = generateDataDim(true, scanDimensions, null);

		ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset("file_name", String.class, dataDim, null, null);
		int dataByteSize = 8; // vlen strings are sizeof(char*), need to handle fixed length case
		int[] chunk = NexusUtils.estimateChunking(scanDimensions, dataByteSize);
		lazy.setChunking(chunk);
		file.createData(dataFileGroup, lazy);

		// FIXME this data group does not contain detector data,
		// we should not create this
		// if this is the only detector the fallback group would be better
		// Make and open NXdata
		path.setLength(0);
		NexusUtils.addToAugmentPath(path, entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, detector.getName(), NexusExtractor.NXDataClassName);
		final GroupNode dataGroup = file.getGroup(path.toString(), true);

		makeAxesLinks(dataGroup);

		return detectorGroup;
	}

	/**
	 * Creates an NXdetector group for a generic detector (i.e. one that isn't dealt with by special case methods)
	 */
	private GroupNode makeGenericDetector(Detector detector) throws NexusException, DeviceException {
		logger.debug("Creating Generic Detector entry for {} in NeXus file.", detector.getName());
		final String detectorName = detector.getName();
		final int[] dataDimensions = detector.getDataDimensions();

		// Navigate to the relevant section in file...
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, GROUP_NAME_INSTRUMENT, NexusExtractor.NXInstrumentClassName);
		NexusUtils.addToAugmentPath(path, detectorName, NexusExtractor.NXDetectorClassName);
		// Create NXdetector
		final GroupNode detectorGroup = file.getGroup(path.toString(), true);
		// add metadata according to a metadata appender, if one is registered

		// Metadata items
		try {
			String detDescription = detector.getDescription();
			String detType = detector.getDetectorType();
			String detId = detector.getDetectorID();
			if (detDescription != null && detDescription.length() > 0) {
				NexusUtils.writeString(file, detectorGroup, "description", detDescription);
			}
			if (detType != null && detType.length() > 0) {
				NexusUtils.writeString(file, detectorGroup, "type", detType);
			}
			if (detId != null && detId.length() > 0) {
				NexusUtils.writeString(file, detectorGroup, "id", detId);
			}
		} catch (DeviceException e) {
			logger.error("Error writing detector metadata", e);
		}

		// Check to see if the detector will write its own info into NeXus
		if (detector instanceof INeXusInfoWriteable) {
			((INeXusInfoWriteable) detector).writeNeXusInformation(file, detectorGroup);
		}

		List<SelfCreatingLink> links = new ArrayList<>();
		// even make data area for detectors that first create their own files
		int[] dataDim = generateDataDim(true, scanDimensions, dataDimensions);

		// make the data array to store the data...
		ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset("data", Double.class, dataDim, null, null);
		lazy.setFillValue(getFillValue(Double.class));
		DataNode data = file.createData(detectorGroup, lazy);
		NexusUtils.writeStringAttribute(file, data, "local_name", String.format("%s.%s", detectorName, detectorName));

		// Get a link ID to this data set.
		links.add(new SelfCreatingLink(data));

		// Make and open NXdata
		path.setLength(0);
		NexusUtils.addToAugmentPath(path, entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, detectorName, NexusExtractor.NXDataClassName);
		final GroupNode dataGroup = file.getGroup(path.toString(), true);

		// Make a link to the data array
		for (SelfCreatingLink link : links) {
			link.create(file, dataGroup);
		}

		makeAxesLinks(dataGroup);

		return detectorGroup;
	}

	private GroupNode makeNexusDetector(Detector detector, INexusTree detectorData) throws NexusException {
		final String detectorName = detectorData.getName();

		// Navigate to the relevant section in file...
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, GROUP_NAME_INSTRUMENT, NexusExtractor.NXInstrumentClassName);
		NexusUtils.addToAugmentPath(path, detectorName, NexusExtractor.NXDetectorClassName);
		// Create NXdetector
		final GroupNode detectorGroup = file.getGroup(path.toString(), true);
		// add metadata according to a metadata appender, if one is registered

		// Metadata items
		NexusUtils.writeString(file, detectorGroup, "local_name", detectorName);

		// Check to see if the detector will write its own info into NeXus
		if (detector instanceof INeXusInfoWriteable) {
			((INeXusInfoWriteable) detector).writeNeXusInformation(file, detectorGroup);
		}

		List<SelfCreatingLink> links = new ArrayList<>();
		for (INexusTree subTree : detectorData) {
			writeHere(file, detectorGroup, subTree, true, false, links);
		}

		// Make and open NXdata
		path.setLength(0);
		NexusUtils.addToAugmentPath(path, entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, detectorName, NexusExtractor.NXDataClassName);
		final GroupNode dataGroup = file.getGroup(path.toString(), true);

		// Make a link to the data array
		for (SelfCreatingLink link : links) {
			link.create(file, dataGroup);
		}

		makeAxesLinks(dataGroup);

		return detectorGroup;
	}

	/**
	 * Creates an NXdetector for a CounterTimer.
	 */
	private GroupNode makeCounterTimer(Detector detector) throws NexusException, DeviceException {
		logger.debug("Creating Detector entry for {} in NeXus file.", detector.getName());
		SelfCreatingLink detectorID;

		// Navigate to the relevant section in file...
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, GROUP_NAME_INSTRUMENT, NexusExtractor.NXInstrumentClassName);
		NexusUtils.addToAugmentPath(path, detector.getName(), NexusExtractor.NXDetectorClassName);
		final GroupNode detectorGroup = file.getGroup(path.toString(), true);

		// Metadata items
		String description = detector.getDescription();
		String type = detector.getDetectorType();
		String id = detector.getDetectorID();

		if (description != null && description.length() > 0) {
			NexusUtils.writeString(file, detectorGroup, "description", detector.getDescription());
		}
		if (type != null && type.length() > 0) {
			NexusUtils.writeString(file, detectorGroup, "type", detector.getDetectorType());
		}
		if (id != null && id.length() > 0) {
			NexusUtils.writeString(file, detectorGroup, "id", detector.getDetectorID());
		}

		// Check to see if the detector will write its own info into NeXus
		if (detector instanceof INeXusInfoWriteable) {
			((INeXusInfoWriteable) detector).writeNeXusInformation(file, detectorGroup);
		}
		int[] dataDim = generateDataDim(true, scanDimensions, null);

		final String[] extraNames = detector.getExtraNames();
		for (int j = 0; j < extraNames.length; j++) {

			// this can fail if the list of names contains duplicates
			ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset(extraNames[j], Double.class, dataDim, null, null);
			lazy.setChunking(NexusUtils.estimateChunking(scanDimensions, 8));
			DataNode data = file.createData(detectorGroup, lazy);

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

		return detectorGroup;
	}

	/**
	 * Use the {@link INexusFileAppenderService} to add metadata to the given group according to
	 * any {@link INexusFileAppender} registered with that service for the given device name.
	 * @param deviceName device name
	 * @param group group to append to
	 * @throws NexusException
	 */
	private void addDeviceMetadata(String deviceName, GroupNode group) throws NexusException {
		final INexusFileAppenderService appenderService = ServiceHolder.getNexusFileAppenderService();
		if (appenderService != null) { // the service should exist in GDA, not all Junit tests will need it
			appenderService.appendMetadata(deviceName, file, group);
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
	protected void createNextFile() throws Exception {
		try {
			if (file != null) {
				try {
					file.close();
				} catch (Exception et) {
					String error = "Error closing NeXus file.";
					logger.error(error, et);
					terminalPrinter.print(error);
					terminalPrinter.print(et.getMessage());
				}
			}

			// set the entry name
			this.entryName = "entry1";

			constructFileName();

			// Check to see if the file(s) already exists!
			if (new File(nexusFileUrl).exists()) {
				throw new IOException("The file " + nexusFileUrl + " already exists.");
			}

			// create nexus file and return handle
			file = createFile();

			// If we have been return a null file reference then there was
			// some problem creating the file.
			if (file == null) {
				throw new IOException("Could not create file: " + nexusFileUrl);
			}

			// Print informational message to console.
			String msg = "Writing data to file (NeXus): " + nexusFileUrl;
			logger.info(msg);
			terminalPrinter.print(msg);
		} catch (Exception ex) {
			String error = "Failed to create file (" + nexusFileUrl;
			if (createSrsFile) {
				error += " or " + srsFile.fileUrl;
			}
			error += ").";
			logger.error(error, ex);
			if (terminalPrinter != null) {
				terminalPrinter.print(error);
				terminalPrinter.print(ex.getMessage());
			}
			throw ex;
		}

	}

	private void constructFileName() {
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
		logger.debug("Nexus file to be written to '{}'", nexusFileUrl);
	}

	@Override
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
		final Optional<ScannableWriter> optScannableWriter = getWriterForScannable(scannable.getName());
		if (optScannableWriter.isPresent()) {
			GroupNode group = file.getGroup(NexusUtils.createAugmentPath(entryName, NexusExtractor.NXEntryClassName), false);
			optScannableWriter.get().writeScannable(file, group, scannable, getSDPositionFor(scannable.getName()), generateDataStartPos(dataStartPosPrefix, null));
		} else {
			writePlainDoubleScannable(scannable);
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

		logger.debug("Writing data for scannable ({}) to NeXus file.", scannable.getName());

		// Navigate to correct location in the file.
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, GROUP_NAME_INSTRUMENT, NexusExtractor.NXInstrumentClassName);
		NexusUtils.addToAugmentPath(path, scannable.getName(), getGroupClassFor(scannable));
		GroupNode group = file.getGroup(path.toString(), false);

		// Loop over inputNames...
		for (int i = 0; i < inputNames.length; i++) {
			// Open data item
			DataNode data = file.getData(group, inputNames[i]);
			try {
				ILazyWriteableDataset lazy = data.getWriteableDataset();
				lazy.setSlice(null, DatasetFactory.createFromObject(positions[i]).reshape(dimArray), SliceND.createSlice(lazy, startPos, stop));
			} catch (DatasetException e) {
				throw new NexusException("Error writing " + inputNames[i], e);
			}
		}

		// and now over extraNames...
		for (int i = 0; i < extraNames.length; i++) {
			// Open data item
			DataNode data = file.getData(group, extraNames[i]);
			ILazyWriteableDataset lazy = data.getWriteableDataset();
			try {
				lazy.setSlice(null, DatasetFactory.createFromObject(positions[inputNames.length + i]), SliceND.createSlice(lazy, startPos, stop));
			} catch (DatasetException e) {
				throw new NexusException("Error writing " + extraNames[i], e);
			}
		}
	}

	private void writeCounterTimer(Detector detector) throws NexusException {
		final double[] newData = extractDoubleData(detector.getName());
		final int[] startPos = generateDataStartPos(dataStartPosPrefix, null);
		final int[] stop = generateDataStop(startPos, null);
		final int[] dimArray = generateDataDim(false, dataDimPrefix, null);

		logger.debug("Writing data for Detector ({}) to NeXus file.", detector.getName());

		// Navigate to correct location in the file.
		final StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, GROUP_NAME_INSTRUMENT, NexusExtractor.NXInstrumentClassName);
		NexusUtils.addToAugmentPath(path, detector.getName(), NexusExtractor.NXDetectorClassName);
		final GroupNode group = file.getGroup(path.toString(), true);

		for (int j = 0; j < detector.getExtraNames().length; j++) {
			final DataNode data = file.getData(group, detector.getExtraNames()[j]);
			try {
				final ILazyWriteableDataset lazy = data.getWriteableDataset();
				lazy.setSlice(null, DatasetFactory.createFromObject(newData[j]).reshape(dimArray), SliceND.createSlice(lazy, startPos, stop));
			} catch (Exception e) {
				throw new NexusException("Error writing data for " + detector.getName(), e);
			}
		}
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

	@Override
	public void setNexusFileNameTemplate(String nexusFileNameTemplate) {
		this.nexusFileNameTemplate = nexusFileNameTemplate;
		// We calculate some probable paths now so that the probable
		// file name and path are known should the intended file path
		// be queried before being written. (NOTE this is happening in
		// XasAsciiNexusDataWriter currently.
		this.nexusFileName = String.format(nexusFileNameTemplate, getScanNumber());
		this.nexusFileUrl = dataDir + nexusFileName;
	}

	protected String getNexusFileNameTemplate() {
		return this.nexusFileNameTemplate;
	}

	private boolean hasConfiguredWriter(Scannable scannable) {
		return getWriterForScannable(scannable.getName()).isPresent();
	}

	private Optional<ScannableWriter> getWriterForScannable(String scannableName) {
		return Optional.ofNullable(getConfiguration().getLocationMap().get(scannableName));
	}

	private Object getSDPositionFor(String scannableName) {
		int index = thisPoint.getScannableNames().indexOf(scannableName);
		return thisPoint.getPositions().get(index);
	}

	private void makeMetadataScannableFallback(GroupNode group, Scannable scannable, Object position)
			throws NexusException {

		String[] allNames = (String[]) ArrayUtils.addAll(scannable.getInputNames(), scannable.getExtraNames());

		logger.debug("Writing data for scannable ({}) to NeXus file.", scannable.getName());

		// Navigate to correct location in the file.
		final String nxDirName = "before_scan";
		final String nxClass = NexusExtractor.NXCollectionClassName;
		// Navigate to correct location in the file.
		final String augmentedPath = NexusUtils.addToAugmentPath(
				NexusUtils.addToAugmentPath(new StringBuilder(file.getPath(group)), nxDirName, nxClass),
				scannable.getName(), nxClass).toString();
		logger.debug("Writing data for scannable ({}) to NeXus file at {}.", scannable.getName(), augmentedPath);
		final GroupNode groupNode = file.getGroup(augmentedPath, true);

		// handle String value that cannot be converted to Quantity
		if (position instanceof String && QuantityFactory.createFromString((String) position) == null) {
			// If position is a single String then just have one name regardless whether input or extra
			NexusUtils.writeString(file, groupNode, allNames[0], (String) position);
		} else if (position instanceof String[]) {
			final String[] positions = (String[]) position;
			for (int i = 0; i < allNames.length; i++) {
				NexusUtils.writeString(file, groupNode, allNames[i], positions[i]);
			}
		} else if (position.getClass().isArray()) {
			// handle a scannable that returns an array (single type, mixed or primitive)
			for (int i = 0; i < allNames.length; i++) {
				NexusUtils.write(file, groupNode, allNames[i], Array.get(position, i));
			}
		} else if (position instanceof Iterable<?>) {
			final Iterator<?> positions = ((Iterable<?>) position).iterator();
		    for (int i = 0; i < allNames.length; i++) {
		        NexusUtils.write(file, groupNode, allNames[i], positions.next());
		    }
		} else {
			// FIXME this needs to bring in the units
			final Double[] positions = ScannableUtils.objectToArray(position);

			for (int i = 0; i < allNames.length; i++) {
				NexusUtils.writeDouble(file, groupNode, allNames[i], positions[i]);
			}
		}

		addDeviceMetadata(scannable.getName(), groupNode);
	}

	private void makeMetadataScannables(Set<String> metadataScannableNames) throws NexusException {
		final GroupNode group = file.getGroup(NexusUtils.createAugmentPath(entryName, NexusExtractor.NXEntryClassName),
				false);
		for (String scannableName : metadataScannableNames) {
			try {
				final Scannable scannable = (Scannable) InterfaceProvider.getJythonNamespace()
						.getFromJythonNamespace(scannableName);
				if (scannable == null) {
					// see if there is a nexus device registered with the nexus device service with the given name. This
					// allows custom metadata to be added without having to create a scannable.
					if (ServiceHolder.getNexusDeviceService().hasNexusDevice(scannableName)) {
						final INexusDevice<? extends NXobject> nexusDevice = ServiceHolder.getNexusDeviceService()
								.getNexusDevice(scannableName);

						writeNexusDevice(group, nexusDevice);
					} else {
						logger.error("No such scannable or nexus device '{}'. It will not be written", scannableName);
					}
				} else {
					makeMetadataScannable(group, scannableName, scannable);
				}
			} catch (DeviceException e) {
				logger.error("Error writing '{}' to NeXus file.", scannableName, e);
			}
		}
	}

	private void makeMetadataScannable(final GroupNode entry, String scannableName, final Scannable scannable)
			throws DeviceException, NexusException {
		logger.debug("Getting scannable '{}' data for writing to NeXus file.", scannable.getName());
		final Object position = scannable.getPosition();
		final Optional<ScannableWriter> optScannableWriter = getWriterForScannable(scannableName);
		if (optScannableWriter.isPresent()) {
			optScannableWriter.get().makeScannable(file, entry, scannable, position, SINGLE_SHAPE, false);
		} else {
			// put in default location (NXcollection with name metadata)
			makeMetadataScannableFallback(entry, scannable, position);
		}
	}

	private <N extends NXobject> void writeNexusDevice(GroupNode group, INexusDevice<N> nexusDevice) throws NexusException {
		final NexusObjectProvider<N> nexusObjectProvider = nexusDevice.getNexusProvider(null);
		final String name = nexusObjectProvider.getName();
		final N nexusObject = nexusObjectProvider.getNexusObject();
		file.addNode(group, name, nexusObject);
	}

	private static NexusDataWriterConfiguration getConfiguration() {
		return ServiceHolder.getNexusDataWriterConfiguration();
	}

	/**
	 * Clears the {@link NexusDataWriterConfiguration}. This method should be called in the
	 * {@code @After} or {@code AfterClass} method of tests that set the configuration.
	 */
	public static void clearConfiguration() {
		getConfiguration().clear();
	}

	@Override
	public String getNexusFileName() {
		return nexusFileName;
	}

	int getScanNumber() {
		configureScanNumber(-1); // ensure it has been configured
		return scanNumber;
	}

	private static Object getFillValue(Class<?> clazz) {

		if (Double.class.equals(clazz)) {
			String floatFill = LocalProperties.get("gda.nexus.floatfillvalue", "nan");
			return floatFill.equalsIgnoreCase("nan") ? Double.NaN : Double.parseDouble(floatFill);
		}
		if (Float.class.equals(clazz)) {
			String floatFill = LocalProperties.get("gda.nexus.floatfillvalue", "nan");
			return floatFill.equalsIgnoreCase("nan") ? Float.NaN : Float.parseFloat(floatFill);
		}
		Object[] objs = NexusUtils.getFillValue(clazz);
		if (objs != null) {
			return objs[0];
		}
			return null;
	}

	/**
	 * Remove symlinks and parent directory links from path if possible eg
	 *
	 * <pre>
	 * getReal(Paths.get("/path/to/directory/sub/../abc.nxs"))
	 * </pre>
	 *
	 * returns {@link Path} to
	 *
	 * <pre>
	 * /path/to/directory/abc.nxs
	 * </pre>
	 *
	 * @param path
	 *            {@link Path} the original path possibly containing symlinks/..
	 * @return the {@link Path} made as real as it can be, if the path doesn't exist or can't be read, return the
	 *         original path
	 */
	private static Path getReal(Path path) {
		if (path == null) {
			return null;
		}
		try {
			return path.toRealPath();
		} catch (IOException ioe) {
			logger.warn("Could not make {} into real path", path);
			return path;
		}
	}

	@Override
	public SwmrStatus getSwmrStatus() {
		return swmrStatus;
	}

}

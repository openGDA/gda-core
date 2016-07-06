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

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.IMetadataEntry;
import gda.data.metadata.Metadata;
import gda.data.nexus.INeXusInfoWriteable;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.NexusDetector;
import gda.device.scannable.ScannableUtils;
import gda.scan.IScanDataPoint;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.python.core.PyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataWriter that outputs NeXus files and optionally a SRS/Text file as well.
 */
public class SixdNexusDataWriter extends DataWriterBase implements DataWriter {

	/**
	 * Property to control the level of instrumentation of the nexus api
	 */
	public static final String GDA_NEXUS_INSTRUMENT_API = "gda.nexus.instrumentApi";

	/**
	 * Property specifying whether SRS data files should be written in
	 * addition to NeXus files. Default is {@code true}.
	 */
	public static final String GDA_NEXUS_CREATE_SRS = "gda.nexus.createSRS";

	/**
	 * Boolean property specifying whether nxs/dat filenames should be prefixed
	 * with the beamline name; if {@code true}, files will be named (e.g.)
	 * {@code "i23-999.nxs"} instead of just {@code "999.nxs"}
	 */
	public static final String GDA_NEXUS_BEAMLINE_PREFIX = "gda.nexus.beamlinePrefix";

	private static final Logger logger = LoggerFactory.getLogger(SixdNexusDataWriter.class);

	static final int MAX_DATAFILENAME = 255;

	/** Are we going to write an SRS file as well ? */
	private boolean createSrsFile = false;

	// beamline name
	private String beamline = null;

	// Directory to write data to
	protected String dataDir = null;

	// file name with no extension
	protected String fileBaseName = null;
	protected String fileBaseUrl = null;

	// file names
	private String nexusFileNameTemplate = null;
	private String txtFileNameTemplate = null;
	protected String nexusFileName = null;
	protected String txtFileName = null;

	// Fully qualified filenames
	protected String nexusFileUrl = null;
	protected String txtFileUrl = null;

	// Relative filenames
	protected String nexusRelativeUrl = null;
	protected String txtRelativeUrl = null;

	// NeXus entry name
	protected String entryName = "entry1";

	/** File Handle for NeXus file */
	protected NexusFile file;
	/** File Handle for text file */
	protected FileWriter txtfile;

	/**
	 * The current run number.
	 */
	protected int scanNumber;

	int getScanNumber() throws Exception{
		configureScanNumber(-1); //ensure it has been configured
		return scanNumber;
	}

	protected Vector<SelfCreatingLink> scannableID;

	boolean firstData = true;

	protected int scanPointNumber = -1;

	IScanDataPoint thisPoint;

	protected Metadata metadata = null;

	private boolean setupPropertiesDone=false;

	private boolean fileNumberConfigured=false;

	/**
	 * Constructor. This attempts to read the java.property which defines the beamline name.
	 */
	public SixdNexusDataWriter(){
		super();
	}

	public SixdNexusDataWriter(int fileNumber) {
		scanNumber = fileNumber;
	}

	protected void setupProperties() throws InstantiationException {
		if( setupPropertiesDone)
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

		// Check to see if we want to create a text/SRS file as well.
		createSrsFile = LocalProperties.check(GDA_NEXUS_CREATE_SRS, true);

		setupPropertiesDone = true;

	}

	@Override
	public void configureScanNumber(int _scanNumber) throws Exception {
		if( !fileNumberConfigured){
			if( _scanNumber <= 0){
				//the scan or other datawriter has set the id
				scanNumber = _scanNumber;
			} else {
				if( scanNumber <= 0){
					setupProperties();
					//not set in a constructor so get from num tracker
					try {
						NumTracker runNumber = new NumTracker(beamline);
						// Get the next run number
						scanNumber = runNumber.incrementNumber();
					} catch (IOException e) {
						logger.error("ERROR: Could not instantiate NumTracker in NexusDataWriter().", e);
						throw new InstantiationException("ERROR: Could not instantiate NumTracker in NexusDataWriter()." + e.getMessage());
					}
				}
			}
			fileNumberConfigured = true;
		}
	}

	/**
	 * calculate dimensionality of data to be written
	 * @param make if true calculate for pre-allocation (first Dim NX_UNLIMITED)
	 * @param dataDimPrefix set to null if not point dependent
	 * @param dataDimensions
	 * @return dimensions
	 */
	// TODO decide whether this is same as one in NDW
	static private int[] generateDataDim(boolean make, int[] dataDimPrefix, int[] dataDimensions) {
		int[] dataDim = null;
		if (dataDimPrefix != null) {
			//do not attempt to add dataDimensions if not set or indicates single point
			int dataDimensionToAdd = dataDimensions != null && (dataDimensions.length>1 || dataDimensions[0]>1) ?
					dataDimensions.length : 0;

			dataDim = Arrays.copyOf(dataDimPrefix, dataDimPrefix.length + dataDimensionToAdd);
			if( dataDimensionToAdd > 0 && dataDimensions != null){
				for (int i = dataDimPrefix.length; i < dataDimPrefix.length + dataDimensionToAdd; i++) {
					dataDim[i] = dataDimensions[i - dataDimPrefix.length];
				}
			}
		} else if (dataDimensions != null) {
			dataDim = Arrays.copyOf(dataDimensions, dataDimensions.length);
		}
		if (make && dataDim != null) {
			dataDim[0] = ILazyWriteableDataset.UNLIMITED;
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

	@Override
	public void addData(IScanDataPoint dataPoint) throws Exception {
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
		dataStartPosPrefix = NexusDataWriter.generateStartPosPrefix(thisPoint.getCurrentPointNumber(), thisPoint.getScanDimensions());

		try {
			if (firstData) {
				scanDimensions = dataPoint.getScanDimensions();
				dataDimPrefix = new int[scanDimensions.length];
				Arrays.fill(dataDimPrefix, 1);
				this.prepareForCollection();
				firstData = false;
			}
		} finally {
			firstData = false;
		}
		dataPoint.setCurrentFilename(getCurrentFileName());

		try {
			if (createSrsFile) {
				txtfile.write(dataPoint.toFormattedString() + "\n");
				txtfile.flush();
			}

			for (Scannable scannable : thisPoint.getScannables()) {
				writeScannable(scannable);
			}

			for (Detector detector : thisPoint.getDetectors()) {
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
			try {
				super.addData(this, dataPoint);
			} catch (Exception e) {
				logger.error("exception received from DataWriterBase.addData(...)", e);
			}
		}
	}

	private void writeDetector(Detector detector) throws NexusException {
		if (detector instanceof NexusDetector) {
			writeNexusDetector((NexusDetector) detector);
		} else { // Non Nexus detector
			writeCommonDetector(detector);
/*
			if (detector.createsOwnFiles()) {
				writeFileCreatorDetector(detector.getName(), extractFileName(detector.getName()), detector.getDataDimensions());
				}
			else{
				writeCounterTimer(detector);
				writeGenericDetector(detector.getName(), detector.getDataDimensions(), extractDoubleData(detector.getName()));

			}
*/
		}

	}

	private static int getIntfromBuffer(Object buf) {
		if (buf instanceof Object[])
			buf = ((Object[]) buf)[0];
		if (buf instanceof Number)
			return ((Number) buf).intValue();
		return Integer.parseInt(buf.toString());
	}

	void writeHere(NexusFile file, GroupNode group, INexusTree tree, boolean makeData, boolean attrOnly, List<SelfCreatingLink> links)
			throws NexusException {
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
					String filePath = new String((byte[]) data.getBuffer(), "UTF-8");
					File f = new File(filePath);
					if ( ! f.exists())
						logger.warn("file " + filePath + " does not exist at time of adding link");
					file.linkExternal(new URI(filePath), NexusUtils.addToAugmentPath(file.getPath(group), name, nxClass), false);
					links.add(new ExternalNXlink(name, filePath));
				} catch (UnsupportedEncodingException | URISyntaxException e) {
					throw new NexusException("supported encoding in creating string for external linking -- this should never happen");
				}
			}
			return;
		}
		if (nxClassIsAttr) {
			if (makeData) {
				NexusGroupData data = tree.getData();
				if (data != null && data.getBuffer() != null) {
					DataNode node = file.getData(group, name);
					if ("axis".equals(name) || "label".equals(name)) {
						Integer axisno = getIntfromBuffer(data.getBuffer());
						axisno += thisPoint.getScanDimensions().length;
						NexusUtils.writeStringAttribute(file, node, name, axisno.toString());
					} else {
						NexusUtils.writeAttribute(file, node, name, data.toDataset());
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
				if (makeData) {
					int[] dataDimMake = generateDataDim(tree.isPointDependent(),
							tree.isPointDependent() ? scanDimensions : null, sdims);
					// make the data array to store the data...
					lazy.setMaxShape(dataDimMake);
					DataNode data = file.createData(group, lazy);

					// FIXME put a break point here and not make it crash

					if (!tree.isPointDependent()) {
						int[] dataDim = generateDataDim(false, null, sdims);
						int[] dataStartPos = NexusDataWriter.generateDataStartPos(null, sdims);
						int[] dataStop = NexusDataWriter.generateDataStop(dataStartPos, sdims);
						IDataset ds = sds.toDataset();
						ds.setShape(dataDim);
						try {
							lazy.setSlice(null, ds, SliceND.createSlice(lazy, dataStartPos, dataStop));
						} catch (Exception e) {
							logger.error("Problem setting slice on data node: {}", lazy, e);
							throw new NexusException("Problem setting slice on data node", e);
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
					int[] dataStartPos = NexusDataWriter.generateDataStartPos(dataStartPosPrefix, sdims);
					int[] dataStop = NexusDataWriter.generateDataStop(dataStartPos, sdims);

					// Open data array.
					DataNode data = file.getData(group, name);

					lazy = data.getWriteableDataset();
					IDataset ds = sds.toDataset();
					ds.setShape(dataDim);
					try {
						lazy.setSlice(null, ds, SliceND.createSlice(lazy, dataStartPos, dataStop));
					} catch (Exception e) {
						logger.error("Problem setting slice on data node", e);
						throw new NexusException(e.getMessage());
					}

					// Close data - do not add children as attributes added for first point only
					loopNodes = false;
				}
			}
		}
		if (loopNodes) {
			for (INexusTree branch : tree) {
				writeHere(file, group, branch, makeData, attrBelowThisOnly, links);
			}
		}
	}

	private void writeNexusDetector(NexusDetector detector) throws NexusException {
		GroupNode group = file.getGroup(NexusUtils.createAugmentPath(entryName, NexusExtractor.NXEntryClassName), false);
		INexusTree tree = extractNexusTree(detector.getName());
		for (INexusTree subTree : tree) {
			writeHere(file, group, subTree, false, false, null);
		}
	}

	private Object extractDetectorObject(String detectorName) {
		int index = thisPoint.getDetectorNames().indexOf(detectorName);
		Object object = thisPoint.getDetectorData().get(index);
		return object;
	}

	private boolean isNumberParsable(Object obj){
		boolean result=false;
		try{
			((Number)obj).doubleValue();
			result=true;
		}catch(NumberFormatException e){
			System.out.println("None parseable object found: " + obj.toString());
			result=false;
		}catch(ClassCastException e){
			System.out.println("ClassCastException found: " + obj.toString());
			result=false;
		}catch(Exception e){
			System.out.println("None parseable object found: " + obj.toString());
			result=false;
		}

		return result;
	}

	private double parseDouble(Object obj){
		return ((Number)obj).doubleValue();
	}

	private String parseString(Object obj){
		return (String)obj;
	}

	private ArrayList<Object> extractData(String detectorName) {
		ArrayList<Object> data = new ArrayList<Object>();
		Object object = extractDetectorObject(detectorName);

		if (object instanceof double[] | object instanceof int[] | object instanceof long[]) {
			data.addAll( Arrays.asList(object) );
		}

		else if (object instanceof String | object instanceof Double | object instanceof Integer | object instanceof Long ) {
			data.add(object);
		}

		else if (object instanceof String[]) {
			String[] sdata = (String[]) object;
			for (int i = 0; i < sdata.length; i++) {
				data.add(Double.valueOf(sdata[i]));
			}
		}

		else if (object instanceof Number[]) {
			Number[] ldata = (Number[]) object;
			for (int i = 0; i < ldata.length; i++) {
				data.add(ldata[i].doubleValue());
			}
		}

		else if (object instanceof PyList) {
			data.addAll( Arrays.asList( ((PyList)object).toArray() ) );
		}

		else{
			logger.error("cannot handle data of type " + object.getClass().getName() + " from detector: "
					+ detectorName + ". NO DATA WILL BE WRITTEN TO NEXUS FILE!");
		}

		return data;
	}

	private INexusTree extractNexusTree(String detectorName) {
		return ((NexusTreeProvider) extractDetectorObject(detectorName)).getNexusTree();
	}

	private Double[] extractDoublePositions(String scannableName) {
		int index = thisPoint.getScannableNames().indexOf(scannableName);

		if (index > -1) {
			Object position = thisPoint.getPositions().get(index);
			if (position != null){
				return ScannableUtils.objectToArray(position);
			}
		}
		return null;
	}

	/**
	 * Perform any tasks that should be done at the end of a scan and close the file.
	 * @throws Exception
	 */
	@Override
	public void completeCollection() throws Exception {
		releaseFile();
		super.completeCollection();
	}

	/**
	 * Releases the file handle.
	 */
	public void releaseFile() {
		try {
			if (file != null) {
				file.flush();
			}
			if (txtfile != null) {
				txtfile.flush();
				txtfile.close();
			}
		} catch (Throwable et) {
			String error = "Error occurred when closing data file(s): ";
			logger.error(error + et.getMessage());
			terminalPrinter.print(error);
			terminalPrinter.print(et.getMessage());
		} finally {
			file = null;
			txtfile = null;
		}
	}

	@Override
	public String getCurrentFileName() {
		return nexusFileUrl;
	}

	public Object getData() {
		return file;
	}

	private void prepareForCollection() throws InstantiationException {
		setupProperties();
		createNextFile();
		makeMetadata();
		makeScannablesAndMonitors();
		makeDetectors();
	}

	private void makeMetadata() {
		try {
			GroupNode group = file.getGroup(NexusUtils.createAugmentPath(entryName, NexusExtractor.NXEntryClassName), false);

			NexusUtils.writeString(file, group, "scan_command", thisPoint.getCommand());
			String scanid = "";
			try {
				scanid = metadata.getMetadataValue(GDAMetadataProvider.SCAN_IDENTIFIER);
			} catch (DeviceException e) {
				// do nothing
			}
			NexusUtils.writeString(file, group, "scan_identifier", scanid.isEmpty() ? thisPoint.getUniqueName() : scanid);
			NexusUtils.writeIntegerArray(file, group, "scan_dimensions", thisPoint.getScanDimensions());
			NexusUtils.writeString(file, group, "title", metadata.getMetadataValue("title"));
			createCustomMetaData();
		} catch (Exception e) {
			logger.info("error writing less important scan information", e);
		}
	}

	/**
	 * Override to provide additional meta data, if required.
	 * Does nothing otherwise.
	 * @throws NexusException
	 */
	@SuppressWarnings("unused")
	protected void createCustomMetaData() throws NexusException {
	}

	private String getGroupNameFor(@SuppressWarnings("unused") Scannable s) {
		// FIXME Either use 's', or replace this method with a String constant
		String groupName = "NXpositioner";
		return groupName;
	}

	/**
	 * Create NXpositioner/NXmonitor class for each Scannable.
	 */
	private void makeScannablesAndMonitors() {
		scannableID = new Vector<SelfCreatingLink>();
		Vector<Scannable> scannablesAndMonitors = new Vector<Scannable>();

		String axislist = "1";
		for (int j = 2; j <= thisPoint.getScanDimensions().length; j++) {
			axislist = axislist + String.format(",%d", j);
		}

		try {
			StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
			NexusUtils.addToAugmentPath(path, "instrument", NexusExtractor.NXInstrumentClassName);

			scannablesAndMonitors.addAll(thisPoint.getScannables());
			// create an NXpositioner for each scannable...
			int[] dataDim = generateDataDim(true, scanDimensions, null);

			int inputnameindex = 0;
			int extranameindex = 0;
			for (Scannable scannable : scannablesAndMonitors) {

				// Get names
				String[] inputNames = scannable.getInputNames();
				String[] extraNames = scannable.getExtraNames();

				// Create (and open) group for the scannable
				String groupName = getGroupNameFor(scannable);
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
					ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset(element, Dataset.FLOAT64, dataDim, null, null);
					DataNode data = file.createData(g, lazy);

					// assign axes
					if (thisPoint.getScanDimensions().length > 0) {
						// TODO
						// in all likelihood this will not give the right axis assignment
						// for scannables with multiple input names
						// this is not solvable given the current data in SDP

						if ((thisPoint.getScanDimensions().length) > inputnameindex) {
							NexusUtils.writeStringAttribute(file, data, "label", String.format("%d",inputnameindex+1));
							NexusUtils.writeStringAttribute(file, data, "primary", "1");
						}
						NexusUtils.writeStringAttribute(file, data, "axis", axislist);
					}

					// Get a link ID to this data set.
					scannableID.add(new SelfCreatingLink(data));
					inputnameindex++;
				}

				for (String element : extraNames) {

					// Create the data array (with an unlimited scan
					// dimension)
					ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset(element, Dataset.FLOAT64, dataDim, null, null);
					DataNode data = file.createData(g, lazy);

					if (thisPoint.getDetectorNames().isEmpty() && extranameindex == 0) {
						NexusUtils.writeStringAttribute(file, data, "signal", "1");
					}

					// Get a link ID to this data set.
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

		// Make links to all scannables.
		for (SelfCreatingLink id : scannableID) {
			try {
				id.create(file, group);
			} catch (NexusException e) {
				logger.warn("Error in makeLink (reported to NX group) for " + id.toString()
						+ "with error" + e.getMessage());
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
					makeDetectorEntry(detector);
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

	/**
	 * @param detector
	 * @throws DeviceException
	 * @throws NexusException
	 */
	private void makeDetectorEntry(Detector detector) throws DeviceException, NexusException {
		logger.debug("Making NXdetector for " + detector.getName() + " in NeXus file.");

		if (detector instanceof NexusDetector) {
			logger.debug("Creating NexusTree entry in NeXus file.");
			INexusTree detTree = extractNexusTree(detector.getName());
			for (INexusTree det : detTree) {
				if (det.getNxClass().equals(NexusExtractor.NXDetectorClassName)) {
					makeGenericDetector(det.getName(), null, Dataset.FLOAT64, detector, det);
				}
			}
		}
		else {
			logger.debug("Creating Generic Detector entry in NeXus file.");
			makeCommonDetector(detector);
		}
	}

	/**
	 * Creates an NXdetector for a generic detector (ie one without a special create routine).
	 *
	 * @param detector
	 * @throws NexusException
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
				NexusUtils.writeString(file, group, "description", detector.getDescription());
				NexusUtils.writeString(file, group, "type", detector.getDetectorType());
				NexusUtils.writeString(file, group, "id", detector.getDetectorID());
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
			// Detectors with multiple extra names can act like countertimers

			int[] dataDim = generateDataDim(true, scanDimensions, null);

			for (int j = 0; j < detector.getExtraNames().length; j++) {
				ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset(detector.getExtraNames()[j], Dataset.FLOAT64, dataDim, null, null);
				DataNode data = file.createData(group, lazy);

				// Get a link ID to this data set
				SelfCreatingLink detectorID = new SelfCreatingLink(data);

				GroupNode g = file.getGroup(group, detector.getName(), NexusExtractor.NXDataClassName, j == 0);
				// If this is the first channel then we need to create (and
				// open) the NXdata item and link to the scannables.
				if (j == 0) {
					// Make links to all scannables.
					for (SelfCreatingLink id : scannableID) {
						id.create(file, g);
					}
				}

				// Make a link to the data array
				detectorID.create(file, g);
			}

		} else {
			// even make data area for detectors that first create their own files
			int[] dataDim = generateDataDim(true, scanDimensions, dataDimensions);

			ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset("data", dtype, dataDim, null, null);
			// make the data array to store the data...
			DataNode data = file.createData(group, lazy);

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

		// Make links to all scannables.
		for (SelfCreatingLink id : scannableID) {
			id.create(file, group);
		}
	}

	/**
	 * Creates an NXdetector for a CounterTimer.
	 *
	 * @param detector
	 * @throws NexusException
	 * @throws DeviceException
	 */
	private void makeCommonDetector(Detector detector) throws NexusException, DeviceException {
		SelfCreatingLink detectorID;

		// Navigate to the relevant section in file...
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, "instrument", NexusExtractor.NXInstrumentClassName);
		NexusUtils.addToAugmentPath(path, detector.getName(), NexusExtractor.NXDetectorClassName);
		// Create NXdetector
		GroupNode group = file.getGroup(path.toString(), true);

		// Metadata items
		NexusUtils.writeString(file, group, "description", detector.getDescription());
		NexusUtils.writeString(file, group, "type", detector.getDetectorType());
		NexusUtils.writeString(file, group, "id", detector.getDetectorID());

		// Check to see if the detector will write its own info into NeXus
		if (detector instanceof INeXusInfoWriteable) {
			((INeXusInfoWriteable) detector).writeNeXusInformation(file, group);
		}

		int[] dataDim = generateDataDim(true, scanDimensions, null);

		ArrayList<String> nameList=new ArrayList<String>();
		nameList.addAll(Arrays.asList(detector.getInputNames()));
		nameList.addAll(Arrays.asList(detector.getExtraNames()));

		ArrayList<Object> dataList = extractData(detector.getName());

		for (int j = 0; j < nameList.size(); j++) {
			//to check the data type:
			if ( !isNumberParsable(dataList.get(j)) ){//Non parsable entry, treat it as file name string
				GroupNode g = file.getGroup(group, "data_file", NexusExtractor.NXNoteClassName, true);
				dataDim = generateDataDim(true, scanDimensions, null);
				ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset("file_name", Dataset.STRING, dataDim, null, null);
				DataNode data = file.createData(g, lazy);
				// Get a link ID to this data set
				detectorID = new SelfCreatingLink(data);
			} else {//Suppose it can be cast into double

				dataDim = generateDataDim(true, scanDimensions, null);

				//this can fail if the list of names contains duplicates
				ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset("data", Dataset.FLOAT64, dataDim, null, null);
				DataNode data = file.createData(group, lazy);
				// Get a link ID to this data set
				detectorID = new SelfCreatingLink(data);
			}

			// If this is the first channel then we need to create (and
			// open) the NXdata item and link to the scannables.
			GroupNode g = file.getGroup(group, detector.getName(), NexusExtractor.NXDataClassName, j == 0);
			if(j== 0){
				// Make links to all scannables.
				for (SelfCreatingLink id : scannableID) {
					id.create(file, g);
				}
			}

			// Make a link to the data array
			detectorID.create(file, g);
		}

	}


	/**
	 * Create the next file. First increment the file number and then try and get a NeXus file from 
	 * {@link NexusFileHDF5#createNexusFile(String)}
	 */
	public void createNextFile() {
		try {
			if (file != null) {
				try {
					file.flush();
				} catch (Throwable et) {
					String error = "Error closing NeXus file.";
					logger.error(error + et.getMessage());
					terminalPrinter.print(error);
					terminalPrinter.print(et.getMessage());
				}
			}

			if (txtfile != null) {
				try {
					txtfile.flush();
					txtfile.close();
				} catch (Throwable et) {
					String error = "Error closing ascii data file.";
					logger.error(error + et.getMessage());
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

			if (txtFileNameTemplate != null) {
				txtFileName = String.format(txtFileNameTemplate, scanNumber);
			} else if (LocalProperties.check(GDA_NEXUS_BEAMLINE_PREFIX)) {
				txtFileName = beamline + "-" + scanNumber + ".dat";
			} else {
				txtFileName = scanNumber + ".dat";
			}
			txtFileUrl = dataDir + txtFileName;

			// Check to see if the file(s) already exists!
			if (new File(nexusFileUrl).exists()) {
				throw new Exception("The file " + nexusFileUrl + " already exists.");
			}

			// create nexus file and return handle
			file = NexusFileHDF5.createNexusFile(nexusFileUrl);
			file.setDebug(LocalProperties.check(GDA_NEXUS_INSTRUMENT_API));
			if (createSrsFile) {
				// Check to see if the file(s) already exists!
				final File textFile = new File(txtFileUrl);
				if (textFile.exists()) {
					throw new Exception("The file " + txtFileUrl + " already exists.");
				}
				textFile.getParentFile().mkdirs();
				if (!textFile.getParentFile().exists()) {
					throw new Exception("Cannot create text file folder: " + textFile.getParentFile());
				}
				txtfile = new FileWriter(txtFileUrl);
				// Now create the SRS header
				txtfile.write(" &SRS\n");
				// Write all the metadata items to the file.
				if (metadata != null) {
					for (IMetadataEntry entry : metadata.getMetadataEntries()) {
						txtfile.write(entry.getName() + "=" + entry.getMetadataValue() + "\n");
					}
				}
				txtfile.write(" &END\n");
				// now write the column headings
				txtfile.write(thisPoint.getHeaderString());
				txtfile.write("\n");
			}

			// If we have been return a null file reference then there was
			// some problem creating the file.
			if (file == null) {
				throw new Exception();
			}

			// Print informational message to console.
			terminalPrinter.print("Writing data to file (NeXus): " + nexusFileUrl);
			if (createSrsFile) {
				terminalPrinter.print("Also creating file (txt): " + txtFileUrl);
			}
		} catch (NexusException ex) {
			String error = "Failed to create file (" + nexusFileUrl + ")";
			logger.error(error, ex);
			terminalPrinter.print(error);
			terminalPrinter.print(ex.getMessage());
		} catch (Exception ex) {
			String error = "Failed to create file (" + nexusFileUrl;
			if (createSrsFile) {
				error += " or " + txtFileUrl;
			}
			error += ")";
			logger.error(error, ex);
			terminalPrinter.print(error);
			terminalPrinter.print(ex.getMessage());
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
	 *
	 * @param header
	 *            String
	 */
	@Override
	public void setHeader(String header) {
	}

	/**
	 * Writes the data for a given scannable to an existing NXpositioner.
	 *
	 * @param scannable
	 * @throws NexusException
	 */
	private void writeScannable(Scannable scannable) throws NexusException {
		int[] startPos = NexusDataWriter.generateDataStartPos(dataStartPosPrefix, null);
		int[] stop = NexusDataWriter.generateDataStop(startPos, null);
		int[] dimArray = generateDataDim(false, dataDimPrefix, null);

		// Get inputNames and positions
		String[] inputNames = scannable.getInputNames();
		String[] extraNames = scannable.getExtraNames();
		Double[] positions = extractDoublePositions(scannable.getName());

		logger.debug("Writing data for scannable (" + scannable.getName() + ") to NeXus file.");

		// Navigate to correct location in the file.
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, "instrument", NexusExtractor.NXInstrumentClassName);
		NexusUtils.addToAugmentPath(path, scannable.getName(), getGroupNameFor(scannable));
		GroupNode group = file.getGroup(path.toString(), true);

		// Loop over inputNames...
		for (int i = 0; i < inputNames.length; i++) {
			// Open data item
			DataNode data = file.getData(group, inputNames[i]);

			ILazyWriteableDataset lazy = data.getWriteableDataset();
			try {
				lazy.setSlice(null, DatasetFactory.createFromObject(positions[i]).reshape(dimArray), SliceND.createSlice(lazy, startPos, stop));
			} catch (Exception e) {
				logger.error("Problem setting slice on data node", e);
				throw new NexusException(e.getMessage());
			}
		}

		// and now over extraNames...
		for (int i = 0; i < extraNames.length; i++) {
			// Open data item
			DataNode data = file.getData(group, extraNames[i]);

			ILazyWriteableDataset lazy = data.getWriteableDataset();
			try {
				lazy.setSlice(null, DatasetFactory.createFromObject(positions[inputNames.length + i]).reshape(dimArray), SliceND.createSlice(lazy, startPos, stop));
			} catch (Exception e) {
				logger.error("Problem setting slice on data node", e);
				throw new NexusException(e.getMessage());
			}
		}
	}

	private void writeCommonDetector(Detector detector) throws NexusException {

		logger.debug("Writing data for Detector (" + detector.getName() + ") to NeXus file.");

		ArrayList<String> nameList=new ArrayList<String>();
		nameList.addAll(Arrays.asList(detector.getInputNames()));
		nameList.addAll(Arrays.asList(detector.getExtraNames()));


		ArrayList<Object> dataList = extractData(detector.getName());

		int[] startPos = NexusDataWriter.generateDataStartPos(dataStartPosPrefix, null);
		int[] stop = NexusDataWriter.generateDataStop(startPos, null);
		int[] dimArray = generateDataDim(false, dataDimPrefix, null);

		// Navigate to correct location in the file.
		StringBuilder path = NexusUtils.addToAugmentPath(new StringBuilder(), entryName, NexusExtractor.NXEntryClassName);
		NexusUtils.addToAugmentPath(path, "instrument", NexusExtractor.NXInstrumentClassName);
		NexusUtils.addToAugmentPath(path, detector.getName(), NexusExtractor.NXDetectorClassName);
		GroupNode group = file.getGroup(path.toString(), true);

		for (int j = 0; j < nameList.size(); j++) {
			Object dataItem=dataList.get(j);
			if (!isNumberParsable(dataItem)) {//treat it as file name
				///////////////////////
				String dataFileName=this.parseString(dataItem);

				if (dataFileName.length() > MAX_DATAFILENAME) {
					logger.error("The detector (" + detector.getName() + ") returned a file name (of length " + dataFileName.length()
							+ ") which is greater than the max allowed length (" + MAX_DATAFILENAME + ").");
				}

				GroupNode g = file.getGroup(group, "data_file", NexusExtractor.NXNoteClassName, false);
				// Open filename array.
				DataNode data = file.getData(g, "file_name");

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
				int[] dataStartPos = NexusDataWriter.generateDataStartPos(dataStartPosPrefix, null);
				int[] dataStop = NexusDataWriter.generateDataStop(dataStartPos, null);

				ILazyWriteableDataset lazy = data.getWriteableDataset();
				try {
					lazy.setSlice(null, DatasetFactory.createFromObject(dataFileName).reshape(dataDim),
							SliceND.createSlice(lazy, dataStartPos, dataStop));
				} catch (Exception e) {
					logger.error("Problem setting slice on data node", e);
					throw new NexusException(e.getMessage());
				}
			} else { // pure data entry
				DataNode data = file.getData(group, nameList.get(j));
				ILazyWriteableDataset lazy = data.getWriteableDataset();
				try {
					lazy.setSlice(null, DatasetFactory.createFromObject(parseDouble(dataItem)).reshape(dimArray),
							SliceND.createSlice(lazy, startPos, stop));
				} catch (Exception e) {
					logger.error("Problem setting slice on data node", e);
					throw new NexusException(e.getMessage());
				}
			}
		}
	}

	@Override
	public int getCurrentScanIdentifier(){
		try {
			return getScanNumber();
		} catch (Exception e) {
			logger.error("Error getting scanIdentifier", e);
		}
		return -1;
	}

	/**
	 * @param txtFileNameTemplate
	 *            the txtFileNameTemplate to set
	 */
	public void setTxtFileNameTemplate(String txtFileNameTemplate) {
		this.txtFileNameTemplate = txtFileNameTemplate;
	}

	/**
	 * @param nexusFileNameTemplate
	 *            the nexusFileNameTemplate to set
	 * @throws Exception
	 */
	public void setNexusFileNameTemplate(String nexusFileNameTemplate) throws Exception {
		this.nexusFileNameTemplate = nexusFileNameTemplate;
		// We calculate some probable paths now so that the probable
		// file name and path are known should the intended file path
		// be queried before being written. (NOTE this is happening in
		// XasAsciiNexusDataWriter currently.
		this.nexusFileName         = String.format(nexusFileNameTemplate, getScanNumber());
		this.nexusFileUrl          = dataDir + nexusFileName;
	}

	@Override
	public void addDataWriterExtender(final IDataWriterExtender e) {
		super.addDataWriterExtender(e);
		this.createSrsFile = false;
	}

	public boolean isFirstData() {
		return firstData;
	}
}

/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.IWritableNexusDevice;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.scan.NexusScanFile;
import org.eclipse.dawnsci.nexus.scan.NexusScanModel;
import org.eclipse.january.dataset.PositionIterator;
import org.eclipse.january.dataset.SliceND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.ServiceHolder;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.nexus.tree.INexusTree;
import gda.device.Scannable;
import gda.jython.InterfaceProvider;
import gda.scan.IScanDataPoint;
import uk.ac.diamond.daq.api.messaging.messages.SwmrStatus;
import uk.ac.gda.api.exception.GDAException;

/**
 * This nexus data writer makes use of the new nexus writing framework in the project
 * {@code org.eclipse.dawnsci.nexus}. Note, a new instance of this class should be
 * created for each scan.
 */
public class NexusScanDataWriter extends DataWriterBase implements INexusDataWriter {

	// TODO: will we ever use extenders with this datawriter? If not we don't need to extend DataWriterBase

	/**
	 * Boolean property specifying whether nxs/dat filenames should be prefixed with the beamline name; if {@code true},
	 * files will be named (e.g.) {@code "i23-999.nxs"} instead of just {@code "999.nxs"}
	 */
	public static final String PROPERTY_NAME_BEAMLINE_PREFIX = "gda.nexus.beamlinePrefix";

	/**
	 * Boolean property specifying whether to enable SWMR mode (Single Write Multiple Read). If enabled,
	 * it is not possible to change the structure of the Nexus file after SWMR mode is enabled - this is
	 * done when the file is created by the first call to {@link #addData(IScanDataPoint)}.
	 */
	public static final String PROPERTY_NAME_WRITE_SWMR = "gda.nexus.writeSwmr";

	/**
	 * The name of the {@link NXentry}. By default the name 'entry' is used. {@link NexusDataWriter}
	 * uses the name 'entry1'. Setting this property to 'entry1' will allow the use of scripts and tools
	 * with that value hard-coded. This should be a temporary measure, the name 'entry' should be considered
	 * to be the Diamond standard.
	 */
	public static final String PROPERTY_NAME_ENTRY_NAME = "gda.nexus.entryName";

	public static final String DEFAULT_ENTRY_NAME = "entry";

	private static final String DEFAULT_FILENAME_TEMPLATE = "%d.nxs";

	private static final String METADATA_ENTRY_NAME_INSTRUMENT = "instrument";
	private static final String DEFAULT_BEAMLINE_NAME = "base";

	private static final Logger logger = LoggerFactory.getLogger(NexusScanDataWriter.class);

	private final String beamlineName;

	/**
	 * The directory to write the nexus file to
	 */
	private final String outputDir;

	/**
	 *  The file name (just the final segment, not the full path)
	 */
	private String fileName = null;

	/**
	 * The string to use as a template file name
	 */
	private String fileNameTemplate = DEFAULT_FILENAME_TEMPLATE;

	/**
	 * The scan number, set when the nexus file is created if not set by {@link #configureScanNumber(int)}
	 */
	private int scanNumber = -1;

	private NexusScanFile nexusScanFile = null;

	private final Map<String, IWritableNexusDevice<? extends NXobject>> nexusDevices = new HashMap<>();

	private int currentPointNumber = -1;

	private PositionIterator scanPositionIter = null;

	private final boolean useSwmr;

	public NexusScanDataWriter() {
		outputDir = InterfaceProvider.getPathConstructor().createFromDefaultProperty();
		beamlineName = GDAMetadataProvider.getInstance().getMetadataValue(METADATA_ENTRY_NAME_INSTRUMENT,
					LocalProperties.GDA_INSTRUMENT, DEFAULT_BEAMLINE_NAME);
		useSwmr = LocalProperties.check(PROPERTY_NAME_WRITE_SWMR, false);
	}

	@Override
	public String getCurrentFileName() {
		return fileName == null ? null : Paths.get(outputDir, fileName).toString();
	}

	@Override
	public int getCurrentScanIdentifier() {
		ensureScanNumberConfigured();
		return scanNumber;
	}

	private void ensureScanNumberConfigured() {
		if (scanNumber == -1) {
			configureScanNumber(getNextScanNumber());
		}
	}

	@Override
	public void configureScanNumber(int scanNumber) {
		this.scanNumber = scanNumber;

		// since we already know the scan number we can update the file name
		calculateFileName();
	}

	@Override
	public void setNexusFileNameTemplate(String fileNameTemplate) {
		this.fileNameTemplate = fileNameTemplate;

		if (scanNumber != -1) {
			// if the scan number has been set we need to update the file name
			calculateFileName();
		}
	}

	private void calculateFileName() {
		// called when the file is created, or when
		String newFileName = String.format(fileNameTemplate, scanNumber);
		if (LocalProperties.check(PROPERTY_NAME_BEAMLINE_PREFIX)) {
			newFileName = beamlineName + "-" + newFileName;
		}

		this.fileName = newFileName;
	}

	@Override
	public String getNexusFileName() {
		return fileName;
	}

	@Override
	public String getDataDir() {
		return outputDir;
	}

	@Override
	public SwmrStatus getSwmrStatus() {
		if (!useSwmr) {
			return SwmrStatus.DISABLED;
		} else if (currentPointNumber > 0) {
			return SwmrStatus.ACTIVE;
		} else {
			return SwmrStatus.ENABLED;
		}
	}

	@Override
	public void setHeader(String header) {
		// not used in this implementation
	}

	@Override
	public void setBeforeScanMetaData(INexusTree beforeScanMetadata) {
		// not used in this implementation
	}

	@Override
	public void addData(IScanDataPoint point) throws Exception {
		currentPointNumber++;
		if (currentPointNumber != point.getCurrentPointNumber()) {
			throw new NexusException("Unexpected point number, expected " + currentPointNumber + " was " + point.getCurrentPointNumber());
		}

		try {
			if (currentPointNumber == 0) {
				try {
					createFile(point);
				} catch (Exception e) {
					throw new GDAException("Could not create nexus file: " + e.getMessage(), e);
				}
			}

			try {
				writePoint(point);
			} catch (Exception e) {
				throw new GDAException("Could not write scan point " + point.getCurrentPointNumber(), e);
			}
		} finally {
			super.addData(point);
		}
	}

	private int getNextScanNumber() {
		try {
			final NumTracker numTracker = new NumTracker(beamlineName);
			return numTracker.incrementNumber();
		} catch (IOException e) {
			throw new IllegalStateException("Could not instantiate NumTracker");
		}
	}

	private void createFile(IScanDataPoint point) throws NexusException {
		ensureScanNumberConfigured();

		logger.debug("Creating new nexus file: {}", getNexusFileName());
		logger.debug("Scan Parameters...");
		logger.debug("Scan Command : {}", point.getCommand());
		logger.debug("Instrument Name : {}", point.getInstrument());
		logger.debug("Number of Points : {}", point.getNumberOfPoints());

		// This is where we create NexusScanModel
		final NexusScanModel nexusScanModel = createNexusScanModel(point);
		nexusScanFile = ServiceHolder.getNexusScanFileService().newNexusScanFile(nexusScanModel);

		nexusScanFile.createNexusFile(false, useSwmr); // TODO, set async to true, see DAQ-3124
	}

	private NexusScanModel createNexusScanModel(IScanDataPoint point) {
		final NexusScanModel nexusScanModel = new NexusScanModel();
		nexusScanModel.setEntryName(LocalProperties.get(PROPERTY_NAME_ENTRY_NAME, DEFAULT_ENTRY_NAME));
		nexusScanModel.setFilePath(getCurrentFileName());
		nexusScanModel.setNexusDevices(getNexusDevices(point));
		nexusScanModel.setDimensionNamesByIndex(getDimensionNamesByIndex(point));
		nexusScanModel.setMetadataWriter(null); // TODO add metadata writer? see DAQ-3151
		nexusScanModel.setNexusMetadataProviders(null); // TODO do we need metadata providers? see DAQ-3151
		nexusScanModel.setTemplateFilePaths(getTemplateFilePaths());
		nexusScanModel.setMultipleNexusDevice(Optional.empty()); // no malcolm device in gda8 scans

		nexusScanModel.setNexusScanInfo(createNexusScanInfo(point, nexusScanModel.getNexusDevices()));
		return nexusScanModel;
	}

	private Map<ScanRole, List<INexusDevice<? extends NXobject>>> getNexusDevices(IScanDataPoint point) {
		final Map<ScanRole, List<INexusDevice<? extends NXobject>>> nexusDevices = new EnumMap<>(ScanRole.class);
		nexusDevices.put(ScanRole.DETECTOR, getNexusDetectors(point));
		nexusDevices.put(ScanRole.SCANNABLE, getScannables(point));
		nexusDevices.put(ScanRole.MONITOR_PER_POINT, getPerPointMonitors(point));
		nexusDevices.put(ScanRole.MONITOR_PER_SCAN, getPerScanMonitors());
		nexusDevices.put(ScanRole.NONE, Collections.emptyList());

		return nexusDevices;
	}

	private static Set<String> getNames(List<INexusDevice<?>> nexusDevices) {
		return nexusDevices.stream().map(INexusDevice::getName).collect(toSet());
	}

	private NexusScanInfo createNexusScanInfo(IScanDataPoint point, Map<ScanRole, List<INexusDevice<?>>> nexusDevices) {
		final NexusScanInfo nexusScanInfo = new NexusScanInfo();
		nexusScanInfo.setFilePath(getCurrentFileName());
		nexusScanInfo.setDetectorNames(getNames(nexusDevices.get(ScanRole.DETECTOR)));
		nexusScanInfo.setScannableNames(nexusDevices.get(ScanRole.SCANNABLE).stream().map(INexusDevice::getName).collect(toList())); // needs list not set
		nexusScanInfo.setPerPointMonitorNames(getNames(nexusDevices.get(ScanRole.MONITOR_PER_POINT)));
		nexusScanInfo.setPerScanMonitorNames(getNames(nexusDevices.get(ScanRole.MONITOR_PER_SCAN)));
		nexusScanInfo.setRank(point.getScanDimensions().length);
		nexusScanInfo.setShape(point.getScanDimensions());

		return nexusScanInfo;
	}

	private List<Set<String>> getDimensionNamesByIndex(IScanDataPoint point) {
		// assume for now that each scannable in the list of scannables corresponds to the same dimension of the scan
		// as its index in the list, and that any scannables left over are monitors
		return point.getScannables().stream()
				.limit(point.getScanDimensions().length)
				.map(Scannable::getName)
				.map(Arrays::asList).map(HashSet::new)
				.collect(toList());
	}

	private List<INexusDevice<?>> getScannables(IScanDataPoint point) {
		// assume for now that each scannable in the list of scannables corresponds to the same dimension of the scan
		// as its index in the list, and that any scannables left over are monitors, see DAQ
		return point.getScannables().stream().limit(point.getScanDimensions().length)
				.map(this::createNexusDevice).collect(toList());
	}

	private List<INexusDevice<?>> getPerPointMonitors(IScanDataPoint point) {
		// assume for now that each scannable in the list of scannables corresponds to the same dimension of the scan
		// as its index in the list, and that any scannables left over are monitors
		return point.getScannables().stream().skip(point.getScanDimensions().length)
				.map(this::createNexusDevice).collect(toList());
	}

	private List<INexusDevice<?>> getNexusDetectors(IScanDataPoint point) {
		return point.getDetectors().stream().map(this::createNexusDevice).collect(toList());
	}

	private INexusDevice<?> createNexusDevice(Scannable device) {
		try {
			final String deviceName = device.getName();
			final INexusDeviceService nexusDeviceService = ServiceHolder.getNexusDeviceService();
			final INexusDevice<?> nexusDevice;
			if (nexusDeviceService.hasNexusDevice(deviceName)) {
				nexusDevice = nexusDeviceService.getNexusDevice(deviceName);
			} else {
				nexusDevice = nexusDeviceService.getNexusDevice(device);
			}

			if (nexusDevice == null) {
				throw new IllegalArgumentException("Could not find or create a nexus device: " + deviceName);
			}

			if (!(nexusDevice instanceof IWritableNexusDevice<?>)) {
				// TODO: might it be useful to let a device implement INexusDevice but not IWritableNexusDevice? If so, it
				// would have be responsible for writing its dataset at the appropriate point in the scan, e.g. in acquireData
				throw new IllegalArgumentException("device must be an IWritableNexusDevice: " + deviceName);
			}

			nexusDevices.put(device.getName(), (IWritableNexusDevice<?>) nexusDevice);
			return nexusDevice;
		} catch (NexusException e) {
			throw new RuntimeException(e);
		}
	}

	private List<INexusDevice<?>> getPerScanMonitors() {
		// TODO get per scan monitors
		return Collections.emptyList();
	}

	private Set<String> getTemplateFilePaths() {
		// TODO get template file paths
		return Collections.emptySet();
	}

	private void writePoint(IScanDataPoint point) throws Exception {
		logger.debug("Writing scan data point: " + point.getCurrentPointNumber());
		// TODO: we need to call write on all the IScannables!!
		// We can't use ScannableNexusWrapper for this as it will also write to the underlying scannable.
		// need a new wrapper(?)
		final int[] scanPosition = getScanPosition(point);
		final SliceND sliceND = createScanSlice(scanPosition);

		writeScannables(point, sliceND);
		writeDetectors(point, sliceND); // TODO just use int[] for slice? or create new class
	}

	private void writeScannables(IScanDataPoint point, final SliceND sliceND) throws Exception {
		// note, this includes scannables that are being scanned and those that aren't (i.e. monitors)
		final List<String> scannableNames = point.getScannableNames();
		final List<Object> scannablePositions = point.getScannablePositions();
		if (scannableNames.size() != scannablePositions.size()) {
			throw new NexusException("Scannables name and position list have different sizes");
		}

		for (int i = 0; i < scannableNames.size(); i++) {
			writeScannablePosition(scannableNames.get(i), scannablePositions.get(i), sliceND);
		}
	}

	private SliceND createScanSlice(final int[] scanPosition) {
		final int[] scanShape = scanPositionIter.getShape();
		final int[] start = scanPosition;
		final int[] stop = Arrays.stream(start).map(pos -> pos + 1).toArray();
		return new SliceND(scanShape, start, stop, null);
	}

	private int[] getScanPosition(IScanDataPoint point) throws NexusException {
		if (currentPointNumber == 0) {
			scanPositionIter = new PositionIterator(point.getScanDimensions());
		}

		if (!scanPositionIter.hasNext()) { // The call to hasNext actually moves the iterator on(!)
			throw new NexusException("Unexpected Point " + currentPointNumber); // scan dimensions and number of points disagree
		}
		return scanPositionIter.getPos();
	}

	private void writeScannablePosition(String scannableName, Object position, SliceND scanSlice) throws Exception {
		logger.debug("Writing scannable {} for point {}", scannableName, currentPointNumber);

		if (!nexusDevices.containsKey(scannableName)) {
			throw new NexusException("No writer found for scannable " + scannableName);
		}

		nexusDevices.get(scannableName).writePosition(position, scanSlice);
	}

	private void writeDetectors(IScanDataPoint point, final SliceND scanSlice) throws Exception {
		final List<String> detectorNames = point.getDetectorNames();
		final List<Object> detectorData = point.getDetectorData();
		if (detectorNames.size() != detectorData.size()) {
			throw new NexusException("Detector name and data lists have different sizes");
		}

		for (int i = 0; i < detectorNames.size(); i++) {
			final IWritableNexusDevice<?> detNexusDevice = nexusDevices.get(detectorNames.get(i));
			if (detNexusDevice == null)
				throw new IllegalArgumentException("No nexus device for detector: " + detectorNames.get(i));
			detNexusDevice.writePosition(detectorData.get(i), scanSlice);
		}
	}

	@Override
	public void completeCollection() throws Exception {
		try {
			if (nexusScanFile != null) {
				nexusScanFile.scanFinished();
			}
			// call scanEnd on all the devices
			Exception exception = null;
			for (IWritableNexusDevice<?> nexusDevice : nexusDevices.values()) {
				try {
					nexusDevice.scanEnd();
				} catch (Exception e) {
					if (exception == null)
						exception = e;
				}
			}
			if (exception != null) {
				throw exception;
			}
		} finally {
			super.completeCollection();
		}
	}
}
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

import static gda.data.scan.datawriter.NexusDataWriter.CREATE_SRS_FILE_BY_DEFAULT;
import static gda.data.scan.datawriter.NexusDataWriter.GDA_NEXUS_CREATE_MEASUREMENT_GROUP;
import static gda.data.scan.datawriter.NexusDataWriter.GDA_NEXUS_CREATE_SRS;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.IWritableNexusDevice;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.builder.NexusMetadataProvider;
import org.eclipse.dawnsci.nexus.builder.impl.MapBasedMetadataProvider;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.scan.NexusScanFile;
import org.eclipse.dawnsci.nexus.scan.NexusScanMetadataWriter;
import org.eclipse.dawnsci.nexus.scan.NexusScanModel;
import org.eclipse.january.dataset.PositionIterator;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.device.CommonBeamlineDevicesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.ServiceHolder;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.nexus.tree.INexusTree;
import gda.data.scan.nexus.device.AbstractDetectorNexusDeviceAdapter;
import gda.data.scan.nexus.device.AbstractScannableNexusDevice;
import gda.device.Detector;
import gda.device.Scannable;
import gda.jython.InterfaceProvider;
import gda.scan.IScanDataPoint;
import gda.util.Version;
import uk.ac.diamond.daq.api.messaging.messages.SwmrStatus;
import uk.ac.gda.api.scan.IScanObject;
import uk.ac.gda.common.exception.GDAException;

/**
 * This nexus data writer makes use of the new nexus writing framework in the project
 * {@code org.eclipse.dawnsci.nexus}. Note, a new instance of this class should be
 * created for each scan.
 */
public class NexusScanDataWriter extends DataWriterBase implements INexusDataWriter {

	// TODO: will we ever use extenders with this datawriter? If not we don't need to extend DataWriterBase

	/**
	 * Call {@link LocalProperties#set(String, String)} with property name
	 * {@link LocalProperties#GDA_DATA_SCAN_DATAWRITER_DATAFORMAT} and this value to use this nexus writer.
	 */
	public static final String PROPERTY_VALUE_DATA_FORMAT_NEXUS_SCAN = NexusScanDataWriter.class.getSimpleName();

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

	public static final String FIELD_NAME_BEAMLINE = "beamline";
	public static final String FIELD_NAME_END_STATION = "end_station";

	public static final String DEFAULT_ENTRY_NAME = "entry";

	private static final String DEFAULT_FILENAME_TEMPLATE = "%d.nxs";

	public static final String METADATA_ENTRY_NAME_INSTRUMENT = "instrument";
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

	private final Map<String, IWritableNexusDevice<? extends NXobject>> writableNexusDevices = new HashMap<>();

	private int currentPointNumber = -1;

	private IScanDataPoint firstPoint = null;

	private PositionIterator scanPositionIter = null;

	private final boolean useSwmr;

	private SrsDataFile srsFile = null;

	private NexusScanMetadataWriter scanMetadataWriter;

	private MeasurementGroupWriter measurementGroupWriter;

	public NexusScanDataWriter() {
		outputDir = InterfaceProvider.getPathConstructor().createFromDefaultProperty();
		beamlineName = GDAMetadataProvider.getInstance().getMetadataValue(METADATA_ENTRY_NAME_INSTRUMENT,
					LocalProperties.GDA_INSTRUMENT, DEFAULT_BEAMLINE_NAME);
		useSwmr = LocalProperties.check(PROPERTY_NAME_WRITE_SWMR, false);

		if (LocalProperties.check(GDA_NEXUS_CREATE_SRS, CREATE_SRS_FILE_BY_DEFAULT)) {
			srsFile = new SrsDataFile();
		}
		if (LocalProperties.check(GDA_NEXUS_CREATE_MEASUREMENT_GROUP)) {
			measurementGroupWriter = new MeasurementGroupWriter();
		}
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

		if (srsFile != null) {
			try {
				srsFile.configureScanNumber(scanNumber);
			} catch (Exception e) {
				throw new IllegalStateException("Error configuring scan number for SRS file");
			}
		}

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
			// if this is the first point, create the nexus file
			if (currentPointNumber == 0) {
				try {
					firstPoint = point;
					createFile();
				} catch (Exception e) {
					throw new GDAException("Could not create nexus file: " + e.getMessage(), e);
				}
			}

			// write to an SRS file (.dat file) if configured
			if (srsFile != null) {
				try {
					srsFile.addData(point);
				} catch (Exception e) {
					logger.error("An error occurred writing to the srs file", e);
				}
			}

			// write the data for this point into the nexus file
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

	private void createFile() throws NexusException {
		ensureScanNumberConfigured();

		logger.debug("Creating new nexus file: {}", getNexusFileName());
		logger.debug("Scan Parameters...");
		logger.debug("Scan Command : {}", firstPoint.getCommand());
		logger.debug("Instrument Name : {}", firstPoint.getInstrument());
		logger.debug("Number of Points : {}", firstPoint.getNumberOfPoints());
		terminalPrinter.print("Writing data to file: " + getCurrentFileName());

		// This is where we create the NexusScanModel describing the scan in nexus terms
		scanMetadataWriter = new NexusScanMetadataWriter();
		final NexusScanModel nexusScanModel = createNexusScanModel();
		if (measurementGroupWriter != null) { // TODO find a better way to do this.
			measurementGroupWriter.setNexusDevices(nexusScanModel.getNexusDevices());
		}

		nexusScanFile = ServiceHolder.getNexusScanFileService().newNexusScanFile(nexusScanModel);
		nexusScanFile.createNexusFile(false, useSwmr); // TODO, set async to true, see DAQ-3124
	}

	private NexusScanModel createNexusScanModel() throws NexusException {
		final NexusScanModel nexusScanModel = new NexusScanModel();
		nexusScanModel.setEntryName(LocalProperties.get(PROPERTY_NAME_ENTRY_NAME, DEFAULT_ENTRY_NAME));
		nexusScanModel.setFilePath(getCurrentFileName());
		nexusScanModel.setNexusDevices(getNexusDevices());
		nexusScanModel.setDimensionNamesByIndex(getDimensionNamesByIndex());
		nexusScanModel.setMetadataWriter(scanMetadataWriter);
		nexusScanModel.setNexusMetadataProviders(createNexusMetadataProviders());
		nexusScanModel.setTemplateFilePaths(getTemplateFilePaths());
		nexusScanModel.setNexusScanInfo(createNexusScanInfo(nexusScanModel.getNexusDevices()));

		return nexusScanModel;
	}

	private Map<ScanRole, List<INexusDevice<? extends NXobject>>> getNexusDevices() throws NexusException {
		final Map<ScanRole, List<INexusDevice<? extends NXobject>>> nexusDevices = new EnumMap<>(ScanRole.class);
		nexusDevices.put(ScanRole.DETECTOR, getNexusDetectors());
		nexusDevices.put(ScanRole.SCANNABLE, getScannables());
		nexusDevices.put(ScanRole.MONITOR_PER_POINT, getPerPointMonitors());
		nexusDevices.put(ScanRole.MONITOR_PER_SCAN, getPerScanMonitors());
		nexusDevices.put(ScanRole.NONE, Collections.emptyList());

		return nexusDevices;
	}

	private static Set<String> getNames(List<INexusDevice<?>> nexusDevices) {
		return nexusDevices.stream().map(INexusDevice::getName).collect(toSet());
	}

	private NexusScanInfo createNexusScanInfo(Map<ScanRole, List<INexusDevice<?>>> nexusDevices) {
		final NexusScanInfo nexusScanInfo = new NexusScanInfo();
		nexusScanInfo.setFilePath(getCurrentFileName());
		nexusScanInfo.setDetectorNames(getNames(nexusDevices.get(ScanRole.DETECTOR)));
		nexusScanInfo.setScannableNames(nexusDevices.get(ScanRole.SCANNABLE).stream().map(INexusDevice::getName).collect(toList())); // needs list not set
		nexusScanInfo.setPerPointMonitorNames(getNames(nexusDevices.get(ScanRole.MONITOR_PER_POINT)));
		nexusScanInfo.setPerScanMonitorNames(getNames(nexusDevices.get(ScanRole.MONITOR_PER_SCAN)));
		nexusScanInfo.setRank(firstPoint.getScanDimensions().length);
		nexusScanInfo.setShape(firstPoint.getScanDimensions());
		nexusScanInfo.setScanCommand(firstPoint.getCommand());
		nexusScanInfo.setScanFieldNames(getScanFieldNames());
		nexusScanInfo.setCurrentScriptName(getCurrentScriptName());

		return nexusScanInfo;
	}

	private List<String> getScanFieldNames() {
		final String[] header = Stream.of(firstPoint.getPositionHeader(), firstPoint.getDetectorHeader())
				.flatMap(Collection::stream)
				.toArray(String[]::new);
		final Double[] pointData = firstPoint.getAllValuesAsDoubles();
		if (header.length != pointData.length) {
			throw new IllegalArgumentException("Point data must be same size and header, was " + pointData.length + ", expected " + header.length);
		}

		return IntStream.range(0, header.length)
				.filter(i -> pointData[i] != null)
				.mapToObj(i -> header[i])
				.collect(toList());
	}

	private String getCurrentScriptName() {
		return InterfaceProvider.getScriptController().getScriptName().orElse(null);
	}

	private List<Set<String>> getDimensionNamesByIndex() {
		// assume for now that each scannable in the list of scannables corresponds to the same dimension of the scan
		// as its index in the list, and that any scannables left over are monitors
		return firstPoint.getScannables().stream()
				.limit(firstPoint.getScanDimensions().length)
				.map(Scannable::getName)
				.map(Arrays::asList).map(HashSet::new)
				.collect(toList());
	}

	private List<INexusDevice<?>> getScannables() {
		// assume for now that each scannable in the list of scannables corresponds to the same dimension of the scan
		// as its index in the list, and that any scannables left over are monitors, see DAQ
		return firstPoint.getScannables().stream().limit(firstPoint.getScanDimensions().length)
				.map(this::createNexusDevice).collect(toList());
	}

	private List<INexusDevice<?>> getPerPointMonitors() {
		// assume for now that each scannable in the list of scannables corresponds to the same dimension of the scan
		// as its index in the list, and that any scannables left over are monitors
		return firstPoint.getScannables().stream().skip(firstPoint.getScanDimensions().length)
				.map(this::createNexusDevice).collect(toList());
	}

	private List<INexusDevice<?>> getNexusDetectors() throws NexusException {
		final List<String> detectorNames = firstPoint.getDetectorNames();
		if (detectorNames.size() != firstPoint.getDetectorData().size()) {
			throw new NexusException("Detector name and data lists have different sizes");
		}

		return firstPoint.getDetectorNames().stream()
				.map(this::createDetectorNexusDevice)
				.collect(toList());
	}

	private INexusDevice<?> createDetectorNexusDevice(String detectorName) {
		final Detector detector = firstPoint.getDetector(detectorName);
		final INexusDevice<?> device = createNexusDevice(detector);
		final Object detectorData = getDetectorData(detector.getName(), firstPoint);
		if (device instanceof AbstractDetectorNexusDeviceAdapter) {
			((AbstractDetectorNexusDeviceAdapter) device).setFirstPointData(detectorData);
		}

		return device;
	}

	private INexusDevice<?> createNexusDevice(Scannable device) {
		final INexusDevice<?> nexusDevice = createNexusDevice(device, true);
		if (nexusDevice instanceof AbstractScannableNexusDevice<?>) {
			((AbstractScannableNexusDevice<?>) nexusDevice).setScanObject(getScanObject(device.getName()));
		}
		return nexusDevice;
	}

	private INexusDevice<?> createNexusDevice(Scannable scannable, boolean writeable) {
		final String deviceName = scannable.getName();
		final INexusDevice<?> nexusDevice;
		if (scannable instanceof INexusDevice) {
			nexusDevice = (INexusDevice<?>) scannable;
		} else {
			final INexusDeviceService nexusDeviceService = ServiceHolder.getNexusDeviceService();
			try {
				if (nexusDeviceService.hasNexusDevice(deviceName)) {
					nexusDevice = nexusDeviceService.getNexusDevice(deviceName);
				} else {
					nexusDevice = nexusDeviceService.getNexusDevice(scannable);
				}

				if (nexusDevice == null) {
					throw new IllegalArgumentException("Could not find or create a nexus device: " + deviceName);
				}
			} catch (NexusException e) {
				throw new RuntimeException(e);
			}
		}

		if (writeable) {
			if (!(nexusDevice instanceof IWritableNexusDevice<?>)) {
				// per-point nexus devices must implement IWritebleNexusDevice
				// TODO: might it be useful to let a device implement INexusDevice but not IWritableNexusDevice? If so, it
				// would have be responsible for writing its dataset at the appropriate point in the scan, e.g. in acquireData
				throw new IllegalArgumentException("device must be an IWritableNexusDevice: " + deviceName);
			}
			writableNexusDevices.put(scannable.getName(), (IWritableNexusDevice<?>) nexusDevice);
		}

		return nexusDevice;
	}

	private INexusDevice<?> createScannableNexusDevice(String scannableName) {
		final Object jythonObject = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(scannableName);
		if (jythonObject instanceof Scannable) {
			return createNexusDevice((Scannable) jythonObject, false);
		} else if (jythonObject != null) {
			// If the object is not a jython scannable, there still might be an INexusDevice with that name, so log and continue
			logger.debug("The object named ''{}'' in the jython namespace is not a Scannable.", scannableName);
		}

		// see if there is a nexus device registered with the nexus device service with the given name. This allows custom
		// metadata to be added without having to create a scannable.
		if (ServiceHolder.getNexusDeviceService().hasNexusDevice(scannableName)) {
			try {
				return ServiceHolder.getNexusDeviceService().getNexusDevice(scannableName);
			} catch (NexusException e) {
				logger.error("An error occurred getting a nexus device with the name '{}'. It will not be written.", scannableName, e);
			}
		} else {
			logger.error("No such scannable or nexus device '{}'. It will not be written", scannableName);
		}
		return null;
	}

	private List<INexusDevice<?>> getPerScanMonitors() {
		final MetadataScannableCalculator calculator = new MetadataScannableCalculator(
				firstPoint.getDetectorNames(), firstPoint.getScannableNames());
		final Set<String> perScanMonitorNames = new HashSet<>();
		perScanMonitorNames.addAll(calculator.calculateMetadataScannableNames());
		perScanMonitorNames.addAll(getCommonBeamlineDeviceNames());

		final List<INexusDevice<?>> perScanMonitors = perScanMonitorNames.stream()
				.map(this::createScannableNexusDevice)
				.filter(Objects::nonNull)
				.collect(toCollection(ArrayList::new));

		if (measurementGroupWriter != null) {
			measurementGroupWriter.setFirstPoint(firstPoint);
			perScanMonitors.add(measurementGroupWriter);
		}

		return perScanMonitors;
	}

	private IScanObject getScanObject(String scannableName) {
		if (firstPoint.getScanObjects() == null) return null;
		return firstPoint.getScanObjects().stream()
				.filter(scanObj -> scannableName.equals(scanObj.getScannable().getName()))
				.findFirst().orElse(null);
	}

	private Set<String> getCommonBeamlineDeviceNames() {
		final CommonBeamlineDevicesConfiguration deviceConfig = ServiceHolder.getCommonBeamlineDevicesConfiguration();

		if (deviceConfig == null) {
			logger.error("Could not find a {} bean.\n"
					+ "It is required to define a bean of this type in your GDA server spring configuration in order to use {}",
					CommonBeamlineDevicesConfiguration.class.getSimpleName(), NexusScanDataWriter.class.getSimpleName());
			return Collections.emptySet();
		}

		return deviceConfig.getCommonDeviceNames();
	}

	private Set<String> getTemplateFilePaths() {
		final List<String> templateFilePaths = ServiceHolder.getNexusDataWriterConfiguration().getNexusTemplateFiles();
		if (templateFilePaths.isEmpty()) return Collections.emptySet();
		return templateFilePaths.stream().map(this::resolveTemplateFilePath).collect(toSet());
	}

	private String resolveTemplateFilePath(String templateFilePath) {
		final Path filePath = Paths.get(templateFilePath);
		if (filePath.isAbsolute()) {
			return filePath.toString();
		}

		// if the file path is relative, resolve it relative to gda.var
		final String gdaVar = InterfaceProvider.getPathConstructor().createFromProperty(LocalProperties.GDA_VAR_DIR);
		return Paths.get(gdaVar).resolve(filePath).toString();
	}

	private List<NexusMetadataProvider> createNexusMetadataProviders() {
		// TODO do we need more metadata providers, or provide a way to populate them from some configuration? see DAQ-3151
		final MapBasedMetadataProvider entryMetadata = new MapBasedMetadataProvider(NexusBaseClass.NX_ENTRY);
		entryMetadata.addMetadataEntry(NXentry.NX_PROGRAM_NAME, "GDA " + Version.getRelease());

		final MapBasedMetadataProvider instrumentMetadata = new MapBasedMetadataProvider(NexusBaseClass.NX_INSTRUMENT);
		instrumentMetadata.addMetadataEntry(FIELD_NAME_BEAMLINE, beamlineName);
		final String endStationName = LocalProperties.get(LocalProperties.GDA_END_STATION_NAME);
		if (endStationName != null) instrumentMetadata.addMetadataEntry(FIELD_NAME_END_STATION, endStationName);
		final String instrumentName = endStationName != null ? endStationName : beamlineName;
		instrumentMetadata.addMetadataEntry(NXinstrument.NX_NAME, instrumentName);

		return List.of(entryMetadata, instrumentMetadata);
	}

	private void writePoint(IScanDataPoint point) throws Exception {
		logger.debug("Writing scan data point: " + point.getCurrentPointNumber());
		// TODO: we need to call write on all the IScannables!!
		// We can't use ScannableNexusWrapper for this as it will also write to the underlying scannable.
		// need a new wrapper(?)
		final int[] scanPosition = getScanPosition(point);
		final SliceND sliceND = createScanSlice(scanPosition);

		writeScannables(point, sliceND);
		writeDetectors(point, sliceND);
		writeScanPointMetadata(point, sliceND);
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

	private void writeScanPointMetadata(IScanDataPoint point, final SliceND scanSlice) throws NexusException {
		// writes unique keys
		scanMetadataWriter.writePosition(scanSlice, point.getCurrentPointNumber());

		// write point start and point end at the same time, as we only get called once per point
		scanMetadataWriter.pointStarted(scanSlice);
		scanMetadataWriter.pointFinished(scanSlice);
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

		if (!writableNexusDevices.containsKey(scannableName)) {
			throw new NexusException("No writer found for scannable " + scannableName);
		}

		writableNexusDevices.get(scannableName).writePosition(position, scanSlice);
	}

	private void writeDetectors(IScanDataPoint point, final SliceND scanSlice) throws Exception {
		final List<String> detectorNames = point.getDetectorNames();
		if (detectorNames.size() != point.getDetectorData().size()) {
			throw new NexusException("Detector name and data lists have different sizes");
		}

		for (String detectorName : detectorNames) {
			writeDetector(point, detectorName, scanSlice);
		}
	}

	private void writeDetector(IScanDataPoint point, String detectorName, final SliceND scanSlice)
			throws NexusException {
		final Object detectorData = getDetectorData(detectorName, point);
		final IWritableNexusDevice<?> detNexusDevice = writableNexusDevices.get(detectorName);
		if (detNexusDevice == null)
			throw new IllegalArgumentException("No nexus device for detector: " + detectorName);
		detNexusDevice.writePosition(detectorData, scanSlice);
	}

	private Object getDetectorData(String detName, IScanDataPoint point) {
		final int detectorIndex = point.getDetectorNames().indexOf(detName);
		return point.getDetectorData().get(detectorIndex);
	}

	@Override
	public void completeCollection() throws Exception {
		try {
			// call scanEnd on all the devices
			Exception exception = null;
			for (IWritableNexusDevice<?> nexusDevice : writableNexusDevices.values()) {
				try {
					nexusDevice.scanEnd();
				} catch (Exception e) {
					if (exception == null)
						exception = e;
				}
			}

			if (nexusScanFile != null) {
				scanMetadataWriter.scanFinished();
				nexusScanFile.scanFinished();
			}
			if (srsFile != null) {
				srsFile.releaseFile();
			}

			if (exception != null) {
				throw exception;
			}
		} finally {
			super.completeCollection();
		}
	}
}

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
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.eclipse.dawnsci.nexus.scan.NexusScanFileService;
import org.eclipse.dawnsci.nexus.scan.NexusScanMetadataWriter;
import org.eclipse.dawnsci.nexus.scan.NexusScanModel;
import org.eclipse.dawnsci.nexus.template.NexusTemplate;
import org.eclipse.january.dataset.PositionIterator;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.device.CommonBeamlineDevicesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.nexus.tree.INexusTree;
import gda.data.scan.nexus.device.AbstractDetectorNexusDeviceAdapter;
import gda.data.scan.nexus.device.AbstractScannableNexusDevice;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.NexusDetector;
import gda.jython.InterfaceProvider;
import gda.scan.IScanDataPoint;
import gda.scan.ScanInformation;
import gda.util.Version;
import uk.ac.diamond.daq.api.messaging.messages.SwmrStatus;
import uk.ac.diamond.osgi.services.ServiceProvider;
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

	/**
	 * Boolean property specifying whether to create the nexus file at the start of the scan rather
	 * than after the first point (i.e. the first call to {@link #addData(IScanDataPoint)}). Detectors in a scan
	 * must implement {@link IWritableNexusDevice} or override {@link Detector#getFileStructure()} to be compatible.
	 */
	public static final String PROPERTY_NAME_CREATE_FILE_AT_SCAN_START = "gda.nexus.createFileAtScanStart";

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

	private final Map<String, INexusDevice<? extends NXobject>> nexusDevices = new HashMap<>();

	private int currentPointNumber = -1;

	private IScanDataPoint firstPoint;

	private ScanInformation scanInfo = null;

	private List<Scannable> scannables = null;

	private List<Detector> detectors = null;

	private PositionIterator scanPositionIter = null;

	private final boolean useSwmr;

	private SrsDataFile srsFile = null;

	private NexusScanMetadataWriter scanMetadataWriter;

	private MeasurementGroupWriter measurementGroupWriter;

	// capture start_time on data writer instance creation.
	private final ZonedDateTime startTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

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
		logger.debug("Configuring file number: {}", this.scanNumber);
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
		logger.debug("Nexus file to be written to: {}", this.fileName);
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
	public void scanStart(ScanInformation scanInfo, List<Scannable> scannables, List<Detector> detectors) throws NexusException {
		this.scanInfo = scanInfo;
		this.scannables = scannables;
		this.detectors = detectors;

		if (LocalProperties.check(PROPERTY_NAME_CREATE_FILE_AT_SCAN_START, false)) {
			createFile();
		}
	}

	@Override
	public void addData(IScanDataPoint point) throws Exception {
		currentPointNumber++;

		if (currentPointNumber != point.getCurrentPointNumber()) {
			throw new NexusException("Unexpected point number, expected " + currentPointNumber + " was " + point.getCurrentPointNumber());
		}

		logger.debug("Adding IScanDataPoint with number: {}, UUID: {}", currentPointNumber, point.getUniqueName());

		try {
			// if this is the first point, create the nexus file
			if (currentPointNumber == 0) {
				try {
					if (nexusScanFile == null) {
						firstPoint = point;
						scannables = point.getScannables();
						detectors = point.getDetectors();
						createFile();
					}
				} catch (Exception e) {
					throw new GDAException("Could not create nexus file: " + e.getMessage(), e);
				}
			}

			// write to an SRS file (.dat file) if configured
			if (srsFile != null) {
				try {
					srsFile.addData(point);
				} catch (Exception e) {
					String message = "An error occurred writing to the srs file";
					logger.error(message, e);
					terminalPrinter.print(message);
				}
			}

			// write the data for this point into the nexus file
			try {
				writePoint(point);
			} catch (Exception e) {
				throw new GDAException("Could not write scan point " + currentPointNumber, e);
			}
		} finally {
			super.addData(point);
			logger.debug("addData completed for point number: {}", currentPointNumber);
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
		logger.debug("Scan Command: {}", scanInfo.getScanCommand());
		logger.debug("Instrument Name: {}", scanInfo.getInstrument());
		logger.debug("Number of Points: {}", scanInfo.getNumberOfPoints());
		terminalPrinter.print("Writing data to file: " + getCurrentFileName());

		// This is where we create the NexusScanModel describing the scan in nexus terms
		scanMetadataWriter = new NexusScanMetadataWriter();
		scanMetadataWriter.setStartTime(startTime); // set start time in metadata writer
		final NexusScanModel nexusScanModel = createNexusScanModel();
		if (measurementGroupWriter != null) { // TODO find a better way to do this.
			measurementGroupWriter.setNexusDevices(nexusScanModel.getNexusDevices());
		}

		nexusScanFile = ServiceProvider.getService(NexusScanFileService.class).newNexusScanFile(nexusScanModel);
		nexusScanFile.createNexusFile(false, useSwmr); // TODO, set async to true, see DAQ-3124

		logger.debug("Nexus file created: {}", getNexusFileName());
	}

	private NexusScanModel createNexusScanModel() throws NexusException {
		final NexusScanModel nexusScanModel = new NexusScanModel();
		nexusScanModel.setEntryName(LocalProperties.get(PROPERTY_NAME_ENTRY_NAME, DEFAULT_ENTRY_NAME));
		nexusScanModel.setFilePath(getCurrentFileName());
		nexusScanModel.setNexusDevices(getNexusDevicesWithScanRoles());
		nexusScanModel.setDimensionNamesByIndex(getDimensionNamesByIndex());
		nexusScanModel.setMetadataWriter(scanMetadataWriter);
		nexusScanModel.setNexusMetadataProviders(createNexusMetadataProviders());
		nexusScanModel.setTemplateFilePaths(getTemplateFilePaths());
		nexusScanModel.setNexusTemplates(getNexusTemplates());
		nexusScanModel.setNexusScanInfo(createNexusScanInfo(nexusScanModel.getNexusDevices()));

		return nexusScanModel;
	}

	private Map<ScanRole, List<INexusDevice<? extends NXobject>>> getNexusDevicesWithScanRoles() throws NexusException {
		final Map<ScanRole, List<INexusDevice<? extends NXobject>>> nexusDevicesWithScanRoles = new EnumMap<>(ScanRole.class);
		nexusDevicesWithScanRoles.put(ScanRole.DETECTOR, getDetectorNexusDevices());
		nexusDevicesWithScanRoles.put(ScanRole.SCANNABLE, getScannableNexusDevices());
		nexusDevicesWithScanRoles.put(ScanRole.MONITOR_PER_POINT, getPerPointMonitorNexusDevices());
		nexusDevicesWithScanRoles.put(ScanRole.MONITOR_PER_SCAN, getPerScanMonitorNexusDevices());
		nexusDevicesWithScanRoles.put(ScanRole.NONE, Collections.emptyList());

		return nexusDevicesWithScanRoles;
	}

	private static Set<String> getNames(List<INexusDevice<?>> nexusDevices) {
		return nexusDevices.stream().map(INexusDevice::getName).collect(toSet());
	}

	private NexusScanInfo createNexusScanInfo(Map<ScanRole, List<INexusDevice<?>>> nexusDevices) {
		final NexusScanInfo nexusScanInfo = new NexusScanInfo();
		nexusScanInfo.setFilePath(getCurrentFileName());
		nexusScanInfo.setDetectorNames(getNames(nexusDevices.get(ScanRole.DETECTOR)));
		nexusScanInfo.setScannableNames(nexusDevices.get(ScanRole.SCANNABLE).stream().map(INexusDevice::getName).toList());
		nexusScanInfo.setPerPointMonitorNames(getNames(nexusDevices.get(ScanRole.MONITOR_PER_POINT)));
		nexusScanInfo.setPerScanMonitorNames(getNames(nexusDevices.get(ScanRole.MONITOR_PER_SCAN)));
		nexusScanInfo.setOverallShape(scanInfo.getDimensions());
		nexusScanInfo.setOuterShape(scanInfo.getDimensions());
		nexusScanInfo.setScanCommand(scanInfo.getScanCommand());
		nexusScanInfo.setScanFieldNames(getScanFieldNames());
		nexusScanInfo.setCurrentScriptName(getCurrentScriptName());
		nexusScanInfo.setEstimatedScanTime(getEstimatedScanTimeMilliSeconds());
		nexusScanInfo.setCurrentScanIdentifier(getCurrentScanIdentifier());
		return nexusScanInfo;
	}

	private long getEstimatedScanTimeMilliSeconds() {
		//Find the maximum detector collection time
		final long timePerPoint = detectors.stream()
			.filter(Objects::nonNull)
			.mapToDouble(this::getDetectorCollectionTime)
			.map(time -> time == -1 ? time : time * 1000) //convert from seconds to milliseconds if valid time
			.mapToLong(Math::round)
			.reduce(0l, Math::max);
		return timePerPoint * scanInfo.getNumberOfPoints();
	}

	private double getDetectorCollectionTime(Detector det) {
		try {
			return det.getCollectionTime();
		} catch (DeviceException e){
			logger.error("Failed to do " + det.getName() +".getCollectionTime()", e);
			return -1;
		}
	}

	private List<String> getScanFieldNames() {
		final List<String> fieldNames = Stream.of(scannables, detectors)
				.flatMap(List::stream)
				.map(Scannable.class::cast)
				.map(this::getPrefixedDeviceFieldNames)
				.flatMap(Function.identity())
				.collect(toCollection(ArrayList::new));

		// check we have the same number of points as values
		if (firstPoint == null || firstPoint.getAllValuesAsDoubles() == null) {
			return fieldNames;
		}

		final Double[] pointData = firstPoint.getAllValuesAsDoubles();
		if (fieldNames.size() != pointData.length) {
			throw new IllegalArgumentException("Point data must be same size as number of field names, "
					+ "was " + pointData.length + ", expected " + header.size());
		}

		// remove null valued fields (indices must be in reverse order)
		IntStream.iterate(pointData.length - 1, i -> i >= 0, i -> i - 1)
			.filter(i -> pointData[i] == null)
			.forEach(fieldNames::remove);

		return fieldNames;
	}

	private Stream<String> getPrefixedDeviceFieldNames(Scannable device) {
		return getDeviceFieldNames(device).map(fieldName -> device.getName() + "." + fieldName);
	}

	private Stream<String> getDeviceFieldNames(Scannable device) {
		if (device instanceof Detector det) {
			if (!ArrayUtils.isEmpty(device.getExtraNames())) {
				return Arrays.stream(det.getExtraNames());
			} else {
				return Stream.of(det.getName());
			}
		}

		return Stream.concat(Arrays.stream(device.getInputNames()), Arrays.stream(device.getExtraNames()));
	}

	private String getCurrentScriptName() {
		return InterfaceProvider.getScriptController().getScriptName().orElse(null);
	}

	private List<List<String>> getDimensionNamesByIndex() {
		// assume for now that each scannable in the list of scannables corresponds to the same dimension of the scan
		// as its index in the list, and that any scannables left over are monitors, see DAQ-3155
		return Arrays.stream(scanInfo.getScannableNames())
				.limit(scanInfo.getDimensions().length)
				.map(List::of).toList();
	}

	private List<INexusDevice<?>> getScannableNexusDevices() {
		// assume for now that each scannable in the list of scannables corresponds to the same dimension of the scan
		// as its index in the list, and that any scannables left over are monitors, see DAQ-3155
		return getScannables().stream().map(scannable -> createNexusDevice(scannable, ScanRole.SCANNABLE)).collect(toList());
	}

	private List<INexusDevice<?>> getPerPointMonitorNexusDevices() {
		// assume for now that each scannable in the list of scannables corresponds to the same dimension of the scan
		// as its index in the list, and that any scannables left over are monitors, see DAQ-3155
		return getPerPointMonitors().stream().map(perPoint -> createNexusDevice(perPoint, ScanRole.MONITOR_PER_POINT)).collect(toList());
	}

	private List<INexusDevice<?>> getDetectorNexusDevices() throws NexusException {
		if (firstPoint != null && firstPoint.getDetectorNames().size() != firstPoint.getDetectorData().size()) {
			throw new NexusException("Detector name and data lists have different sizes");
		}

		return detectors.stream().map(this::createDetectorNexusDevice).collect(toList());
	}

	private INexusDevice<?> createDetectorNexusDevice(Detector detector) {
		final INexusDevice<?> device = createNexusDevice(detector, ScanRole.DETECTOR);
		if (detector instanceof NexusDetector && device instanceof AbstractDetectorNexusDeviceAdapter adapter) {
			// we need the first point data (the INexusTree structure) in order to create the nexus object for a NexusDetector
			if (firstPoint == null) {
				try {
					final Object detectorData = detector.getFileStructure();
					if(detectorData == null) {
						throw new IllegalStateException("Detector " + detector.getName() + " does not support creation of nexus file before first scan point.");
					}
					adapter.setFirstPointData(detectorData);
				} catch (DeviceException e) {
					throw new IllegalStateException("Detector " + detector.getName() + " threw error returning detector data", e);
				}
			}
			else {
				final Object detectorData = getDetectorData(detector.getName(), firstPoint);
				adapter.setFirstPointData(detectorData);
			}
		}
		return device;
	}

	private INexusDevice<?> createNexusDevice(Scannable device, ScanRole scanRole) {
		final INexusDevice<?> nexusDevice = createNexusDevice(device, scanRole, true);
		if (nexusDevice instanceof AbstractScannableNexusDevice<?>) {
			((AbstractScannableNexusDevice<?>) nexusDevice).setScanObject(getScanObject(device.getName()));
		}
		return nexusDevice;
	}

	private INexusDevice<?> createNexusDevice(Scannable scannable, ScanRole scanRole, boolean writeable) {
		final String deviceName = scannable.getName();
		final INexusDevice<?> nexusDevice;
		if (scannable instanceof INexusDevice) {
			nexusDevice = (INexusDevice<?>) scannable;
		} else {
			final INexusDeviceService nexusDeviceService = ServiceProvider.getService(INexusDeviceService.class);
			try {
				if (nexusDeviceService.hasNexusDevice(deviceName)) {
					nexusDevice = nexusDeviceService.getNexusDevice(deviceName);
					//DAQ-5108 - Check if the configured PER_SCAN device has a name conflict with any scannables taking part in scan.
					//Warn user that scannable won't be recorded due to naming conflict with nexus device configuration.
					if (scanInfo != null) {
						final Set<String> scannableNamesInScan = Arrays.stream(scanInfo.getScannableNames()).collect(toSet());
						if (scannableNamesInScan.contains(deviceName)) {
							logger.warn("Ignoing scannable \"{}\" for configured INexusDevice with same name. This scannable data won't be written. Update scannable name to avoid future conflicts.", deviceName);
							final String message1 = "* WARNING: Scannable \"" + deviceName + "\" won't be recorded in the nexus file because it has a name conflict with a reserved nexus device which is recorded per scan. *";
							final String message2 = "* Please rename your scannable by doing \"" + deviceName + ".setName(name)\" to resolve conflict.";
							final String highlight = StringUtils.repeat("*", message1.length());
							InterfaceProvider.getTerminalPrinter().print(highlight);
							InterfaceProvider.getTerminalPrinter().print(message1);
							InterfaceProvider.getTerminalPrinter().print(message2 + " ".repeat(message1.length() - message2.length() -1) + "*");
							InterfaceProvider.getTerminalPrinter().print(highlight);
						}
					}
				} else {
					nexusDevice = nexusDeviceService.getNexusDevice(scannable, scanRole);
				}
				if (nexusDevice == null) {
					throw new IllegalArgumentException("Could not find or create a nexus device: " + deviceName);
				}
			} catch (NexusException e) {
				throw new RuntimeException(e);
			}
		}
		if (writeable) {
			nexusDevices.put(scannable.getName(), nexusDevice);
		}
		return nexusDevice;
	}

	private int[] getScanDimensions() {
		return firstPoint != null ? firstPoint.getScanDimensions() : scanInfo.getDimensions();
	}

	private List<Scannable> getScannables() {
		return scannables.subList(0, getScanDimensions().length);
	}

	private List<Scannable> getPerPointMonitors() {
		return scannables.subList(getScanDimensions().length, scannables.size());
	}

	private Optional<Scannable> getOptionalScannable(String scannableName) {
		final Object jythonObject = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(scannableName);
		if (jythonObject instanceof Scannable scannable) {
			return Optional.of(scannable);
		} else if (jythonObject != null) {
			// If the object is not a jython scannable, there still might be an INexusDevice with that name, so log and continue
			logger.debug("The object named ''{}'' in the jython namespace is not a Scannable.", scannableName);
		}
		return Optional.empty();
	}

	private Optional<INexusDevice<?>> createPerScanNexusDevice(String deviceName) {
		final Optional<Scannable> optScannable = getOptionalScannable(deviceName);
		final Optional<INexusDevice<?>> optScannableNexusDevice = optScannable.map(scannable -> createNexusDevice(scannable, ScanRole.MONITOR_PER_SCAN, false));
		final Optional<INexusDevice<?>> optRegisteredNexusDevice = getRegisteredNexusDevice(deviceName);
		//DAQ-5104 - Make registered device have priority over scannable.
		if(optRegisteredNexusDevice.isPresent()) {
			if (optScannable.isPresent()) {
				logger.warn("Ignoring scannable {} as it has the same name as a registered INexusDevice.", deviceName);
			}
			return optRegisteredNexusDevice;
		}
		if(optScannableNexusDevice.isEmpty()) {
			logger.error("No such scannable or registered nexus device '{}'. It will not be written.", deviceName);
		}
		return optScannableNexusDevice;
	}

	private Optional<INexusDevice<?>> getRegisteredNexusDevice(String deviceName) {
		// see if there is a nexus device registered with the nexus device service with the given name. This allows custom
		// metadata to be added without having to create a scannable.
		if (ServiceProvider.getService(INexusDeviceService.class).hasNexusDevice(deviceName)) {
			try {
				return Optional.ofNullable(ServiceProvider.getService(INexusDeviceService.class).getNexusDevice(deviceName));
			} catch (NexusException e) {
				logger.error("An error occurred getting a nexus device with the name '{}'. It will not be written.", deviceName, e);
			}
		}
		return Optional.empty();
	}

	private List<INexusDevice<?>> getPerScanMonitorNexusDevices() {
		final MetadataScannableCalculator calculator = new MetadataScannableCalculator(
			Arrays.asList(scanInfo.getDetectorNames()),
			Arrays.asList(scanInfo.getScannableNames())
		);
		final Set<String> perScanMonitorNames = new HashSet<>();
		perScanMonitorNames.addAll(calculator.calculateMetadataScannableNames());

		final Set<String> deviceNames = getCommonBeamlineDeviceNames();
		final Set<String> deviceNamesTakingPartInScan = Arrays.stream(ArrayUtils.addAll(scanInfo.getScannableNames(), scanInfo.getDetectorNames())).collect(toSet());
		//DAQ-5228 - Copy MetadataScannableCalculator.calculateMetadataScannableNames(), remove the names of any scannables being scanned over so
		//it doesn't have multiple scan roles.
		deviceNames.removeAll(deviceNamesTakingPartInScan);
		perScanMonitorNames.addAll(deviceNames);

		final List<INexusDevice<?>> perScanMonitors = perScanMonitorNames.stream()
				.map(this::createPerScanNexusDevice)
				.filter(Optional::isPresent)
				.map(Optional::orElseThrow)
				.collect(toCollection(ArrayList::new));

		if (measurementGroupWriter != null && firstPoint != null) {
			measurementGroupWriter.setFirstPoint(firstPoint);
			perScanMonitors.add(measurementGroupWriter);
		}

		return perScanMonitors;
	}

	private IScanObject getScanObject(String scannableName) {
		if (firstPoint == null || firstPoint.getScanObjects() == null)
			return null;

		return firstPoint.getScanObjects().stream()
				.filter(scanObj -> scannableName.equals(scanObj.getScannable().getName()))
				.findFirst().orElse(null);
	}

	private Set<String> getCommonBeamlineDeviceNames() {
		final CommonBeamlineDevicesConfiguration deviceConfig = CommonBeamlineDevicesConfiguration.getInstance();

		if (deviceConfig == null) {
			logger.error("Could not find a {} bean.\n"
					+ "It is required to define a bean of this type in your GDA server spring configuration in order to use {}",
					CommonBeamlineDevicesConfiguration.class.getSimpleName(), NexusScanDataWriter.class.getSimpleName());
			return Collections.emptySet();
		}
		return deviceConfig.getCommonDeviceNames();
	}

	private List<String> getTemplateFilePaths() {
		final List<String> templateFilePaths = NexusDataWriterConfiguration.getInstance().getNexusTemplateFiles();
		if (templateFilePaths.isEmpty()) return Collections.emptyList();
		return templateFilePaths.stream().distinct().map(this::resolveTemplateFilePath).toList();
	}

	private List<NexusTemplate> getNexusTemplates() {
		return NexusDataWriterConfiguration.getInstance().getNexusTemplates().stream().toList();
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
		try {
			entryMetadata.addMetadataEntry(NXentry.NX_EXPERIMENT_IDENTIFIER, ServiceProvider.getService(IFilePathService.class).getVisit());
		} catch (Exception e) {
			logger.warn("Could not get visit id");
		}

		final MapBasedMetadataProvider instrumentMetadata = new MapBasedMetadataProvider(NexusBaseClass.NX_INSTRUMENT);
		instrumentMetadata.addMetadataEntry(FIELD_NAME_BEAMLINE, beamlineName);
		final String endStationName = LocalProperties.get(LocalProperties.GDA_END_STATION_NAME);
		if (endStationName != null) instrumentMetadata.addMetadataEntry(FIELD_NAME_END_STATION, endStationName);
		final String instrumentName = endStationName != null ? endStationName : beamlineName;
		instrumentMetadata.addMetadataEntry(NXinstrument.NX_NAME, instrumentName);

		return List.of(entryMetadata, instrumentMetadata);
	}

	private void writePoint(IScanDataPoint point) throws Exception {
		logger.debug("Writing scan data for point number: {}", currentPointNumber);
		final int[] scanPosition = getScanPosition(point);
		final SliceND sliceND = createScanSlice(scanPosition);

		writeScannables(point, sliceND);
		writeDetectors(point, sliceND);
		writeScanPointMetadata(point, sliceND);

		logger.debug("Finished writing scan data for point number: {}", currentPointNumber);
	}

	private void writeScannables(IScanDataPoint point, final SliceND sliceND) throws Exception {
		// note, this includes scannables that are being scanned and those that aren't (i.e. monitors)
		logger.debug("Writing scannables for point number: {}", currentPointNumber);
		final List<String> scannableNames = point.getScannableNames();
		final List<Object> scannablePositions = point.getScannablePositions();
		if (scannableNames.size() != scannablePositions.size()) {
			throw new NexusException("Scannables name and position list have different sizes");
		}

		for (int i = 0; i < scannableNames.size(); i++) {
			writeScannablePosition(scannableNames.get(i), scannablePositions.get(i), sliceND);
		}
		logger.debug("Finished writing scannables for point number: {}", currentPointNumber);
	}

	private SliceND createScanSlice(final int[] scanPosition) {
		final int[] scanShape = scanPositionIter.getShape();
		final int[] start = scanPosition;
		final int[] stop = Arrays.stream(start).map(pos -> pos + 1).toArray();
		return new SliceND(scanShape, start, stop, null);
	}

	private void writeScanPointMetadata(IScanDataPoint point, final SliceND scanSlice) {
		// writes unique keys
		logger.debug("Writing scan point metadata for point number: {}", currentPointNumber);
		scanMetadataWriter.writePosition(scanSlice, currentPointNumber);

		// write point start and point end at the same time, as we only get called once per point
		scanMetadataWriter.pointStarted(scanSlice);
		scanMetadataWriter.pointFinished(scanSlice);
		logger.debug("Finished writing scan point metadata for point number: {}", currentPointNumber);
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
		logger.debug("Writing scannable: {}", scannableName);

		if (!nexusDevices.containsKey(scannableName)) {
			throw new NexusException("No writer found for scannable " + scannableName);
		}
		if(nexusDevices.get(scannableName) instanceof IWritableNexusDevice<?> detWritableNexusDevice) {
			detWritableNexusDevice.writePosition(position, scanSlice);
		}
		else {
			logger.error("Cannot write position \"{}\" for scannable \"{}\". It isn't an IWritableNexusDevice. Likely due to a naming conflict with a configured INexusDevice.", position, scannableName);
		}
	}

	private void writeDetectors(IScanDataPoint point, final SliceND scanSlice) throws Exception {
		logger.debug("Writing detectors for point number: {}", currentPointNumber);
		final List<String> detectorNames = point.getDetectorNames();
		if (detectorNames.size() != point.getDetectorData().size()) {
			throw new NexusException("Detector name and data lists have different sizes");
		}

		for (String detectorName : detectorNames) {
			writeDetector(point, detectorName, scanSlice);
		}
		logger.debug("Finished writing detectors for point number: {}", currentPointNumber);
	}

	private void writeDetector(IScanDataPoint point, String detectorName, final SliceND scanSlice)
			throws NexusException {
		logger.debug("Writing detector: {}", detectorName);
		final Object detectorData = getDetectorData(detectorName, point);
		final INexusDevice<?> detNexusDevice = nexusDevices.get(detectorName);
		if (detNexusDevice == null)
			throw new IllegalArgumentException("No nexus device for detector: " + detectorName);
		if(detNexusDevice instanceof IWritableNexusDevice<?> detWritableNexusDevice) {
			detWritableNexusDevice.writePosition(detectorData, scanSlice);
		}
	}

	private Object getDetectorData(String detName, IScanDataPoint point) {
		final int detectorIndex = point.getDetectorNames().indexOf(detName);
		return point.getDetectorData().get(detectorIndex);
	}

	@Override
	public void completeCollection() throws Exception {
		try {
			if (nexusScanFile != null) {
				logger.debug("completeCollection() called for file: {}", nexusScanFile.getFilePath());
			}
			// call scanEnd on all the devices
			Exception exception = null;
			for (INexusDevice<?> nexusDevice : nexusDevices.values()) {
				try {
					if (nexusDevice instanceof IWritableNexusDevice writableNexusDevice) {
						writableNexusDevice.scanEnd();
					}
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
			if (nexusScanFile != null) {
				logger.info("Finished writing nexus file: {}", nexusScanFile.getFilePath());
			}
		}
	}
}

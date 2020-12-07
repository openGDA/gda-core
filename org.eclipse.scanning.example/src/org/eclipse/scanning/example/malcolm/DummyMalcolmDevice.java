/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/


package org.eclipse.scanning.example.malcolm;

import static java.util.stream.Collectors.toList;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_DATASETS;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_SIMULTANEOUS_AXES;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_FILENAME;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_NAME;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_PATH;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_RANK;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_TYPE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_UNIQUEID;
import static org.eclipse.scanning.api.malcolm.attributes.MalcolmDatasetType.MONITOR;
import static org.eclipse.scanning.api.malcolm.attributes.MalcolmDatasetType.POSITION_MAX;
import static org.eclipse.scanning.api.malcolm.attributes.MalcolmDatasetType.POSITION_MIN;
import static org.eclipse.scanning.api.malcolm.attributes.MalcolmDatasetType.POSITION_SET;
import static org.eclipse.scanning.api.malcolm.attributes.MalcolmDatasetType.POSITION_VALUE;
import static org.eclipse.scanning.api.malcolm.attributes.MalcolmDatasetType.PRIMARY;
import static org.eclipse.scanning.api.malcolm.attributes.MalcolmDatasetType.SECONDARY;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXmonitor;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.ServiceHolder;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.annotation.scan.PreConfigure;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDetectorInfo;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.malcolm.MalcolmVersion;
import org.eclipse.scanning.api.malcolm.attributes.BooleanAttribute;
import org.eclipse.scanning.api.malcolm.attributes.ChoiceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.IDeviceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.MalcolmAttribute;
import org.eclipse.scanning.api.malcolm.attributes.MalcolmDatasetType;
import org.eclipse.scanning.api.malcolm.attributes.NumberAttribute;
import org.eclipse.scanning.api.malcolm.attributes.StringArrayAttribute;
import org.eclipse.scanning.api.malcolm.attributes.StringAttribute;
import org.eclipse.scanning.api.malcolm.attributes.TableAttribute;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.api.scan.rank.IScanRankService;
import org.eclipse.scanning.api.scan.rank.IScanSlice;
import org.eclipse.scanning.example.Services;
import org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice;
import org.eclipse.scanning.sequencer.SubscanModerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dummy Malcolm device for use in dummy mode or tests.
 */
public class DummyMalcolmDevice extends AbstractMalcolmDevice implements IMalcolmDevice {

	private static interface IDummyMalcolmControlledDevice {

		public void createNexusFile(String dirPath) throws NexusException;

		public void closeNexusFile() throws NexusException;

		public void writePosition(IPosition position) throws Exception;

		public String getName();

	}

	/**
	 * Abstract superclass for a dummy malcolm controlled device which writes nexus.
	 */
	public abstract class DummyMalcolmControlledDevice implements IDummyMalcolmControlledDevice {

		private Map<String, ILazyWriteableDataset> datasets = new HashMap<>();

		protected NexusFile nexusFile = null;

		protected void addDataset(String datasetName, ILazyWriteableDataset dataset, int... datashape) {
			datasets.put(datasetName, dataset);
			if (datashape.length > 0) {
				dataset.setChunking(createChunk(dataset, datashape));
			}
		}

		public int[] createChunk(ILazyWriteableDataset dataset, int... datashape) {
			if (dataset.getRank() == 1) {
				return new int[] { 1 };
			}

			final int[] chunk = new int[getScanRank() + datashape.length];
			Arrays.fill(chunk, 1);
			if (datashape.length > 0) {
				int index = 0;
				for (int i = datashape.length; i > 0; i--) {
					chunk[chunk.length - i] = datashape[index];
					index++;
				}
			} else {
				chunk[chunk.length - 1] = 8;
			}
			return chunk;
		}

		protected void writeData(String datasetName, IPosition position, IDataset data) throws DatasetException {
			ILazyWriteableDataset dataset = datasets.get(datasetName);
			IScanSlice slice = IScanRankService.getScanRankService().createScanSlice(position, data.getShape());
			SliceND sliceND = new SliceND(dataset.getShape(), dataset.getMaxShape(),
					slice.getStart(), slice.getStop(), slice.getStep());
			dataset.setSlice(null, data, sliceND);
		}

		protected void writeDemandData(String datasetName, IPosition position) throws DatasetException {
			double demandValue = ((Double) position.get(datasetName)).doubleValue();
			ILazyWriteableDataset dataset = datasets.get(datasetName);

			int index = position.getIndex(datasetName);
			final int[] startPos = new int[] { index };
			final int[] stopPos = new int[] { index + 1 };
			dataset.setSlice(null, DatasetFactory.createFromObject(demandValue), startPos, stopPos, null);
		}

		@Override
		public void closeNexusFile() throws NexusException {
			if (nexusFile!=null) {
				nexusFile.flush();
				nexusFile.close();
			}
		}

	}

	/**
	 * A dummy malcolm controlled detector which writes nexus.
	 */
	private final class DummyMalcolmDetector extends DummyMalcolmControlledDevice {

		private final DummyMalcolmDetectorModel model;

		public DummyMalcolmDetector(DummyMalcolmDetectorModel model) {
			this.model = model;
		}

		@Override
		public void createNexusFile(String dirPath) throws NexusException {
			int scanRank = getScanRank();

			final String filePath = dirPath + model.getName() + FILE_EXTENSION_HDF5;
			System.out.println("Dummy malcolm device creating nexus file " + filePath);
			TreeFile treeFile = NexusNodeFactory.createTreeFile(filePath);
			NXroot root = NexusNodeFactory.createNXroot();
			treeFile.setGroupNode(root);
			NXentry entry = NexusNodeFactory.createNXentry();
			root.setEntry(entry);

			// add an entry to the unique keys collection
			String[] uniqueKeysDatasetPathSegments = UNIQUE_KEYS_DATASET_PATH.split("/");
			NXcollection ndAttributesCollection = NexusNodeFactory.createNXcollection();
			entry.setCollection(uniqueKeysDatasetPathSegments[2], ndAttributesCollection);
			addDataset(DATASET_NAME_UNIQUE_KEYS, ndAttributesCollection.initializeLazyDataset(
					uniqueKeysDatasetPathSegments[3], scanRank, String.class));

			// create an NXdata
			Map<String, DataNode> axesDemandDataNodes = new HashMap<>();
			for (DummyMalcolmDatasetModel datasetModel : model.getDatasets()) {
				final String datasetName = datasetModel.getName();
				NXdata dataGroup = NexusNodeFactory.createNXdata();
				entry.setData(datasetName, dataGroup);
				// initialize the dataset. The scan rank is added to the dataset rank

				addDataset(datasetName,  dataGroup.initializeLazyDataset(datasetName,
						scanRank + datasetModel.getRank(), datasetModel.getDtype()), getDataShape(datasetModel));
				// add the demand values for the axes
				for (String axisName : axesToMove) {
					DataNode axisDemandDataNode = axesDemandDataNodes.get(axisName);
					String dataNodeName = axisName + "_set";
					if (axisDemandDataNode == null) {
						// create demand dataset (has rank 1)
						addDataset(axisName, dataGroup.initializeLazyDataset(dataNodeName, 1, Double.class));
						axisDemandDataNode = dataGroup.getDataNode(dataNodeName);
						axesDemandDataNodes.put(axisName, axisDemandDataNode);
					} else {
						// create a link to the existing demand dataset in the same file
						dataGroup.addDataNode(dataNodeName, axisDemandDataNode);
					}
				}
			}

			// save the nexus tree to disk
			nexusFile = saveNexusFile(treeFile);
		}

		private int[] getDataShape(DummyMalcolmDatasetModel datasetModel) {
			int[] shape = datasetModel.getShape();
			if (shape == null) {
				shape = new int[datasetModel.getRank()];
				Arrays.fill(shape, 64); // e.g. a 64x64 image if rank is 2
				datasetModel.setShape(shape);
			}

			return shape;
		}

		@Override
		public void writePosition(IPosition position) throws Exception {
			if (nexusFile == null) return;

			for (DummyMalcolmDatasetModel datasetModel : model.getDatasets()) {
				// create the data to write into the dataset
				int[] dataShape = getDataShape(datasetModel);
				IDataset data = Random.rand(dataShape);
				writeData(datasetModel.getName(), position, data);
			}

			// write the demand position for each malcolm controlled axis
			for (String axisName : axesToMove) {
				writeDemandData(axisName, position);
			}

			// write unique key
			final int uniqueKey = position.getStepIndex() + 1;
			final IDataset newPositionData = DatasetFactory.createFromObject(uniqueKey);
			writeData(DATASET_NAME_UNIQUE_KEYS, position, newPositionData);
			nexusFile.flush();
		}

		@Override
		public String getName() {
			return model.getName();
		}
	}

	/**
	 * The panda devices controls the motors and writes nexus for them.
	 */
	private final class DummyPandaDevice extends DummyMalcolmControlledDevice {

		@Override
		public void createNexusFile(String dirPath) throws NexusException {
			final String filePath = dirPath + "panda" + FILE_EXTENSION_HDF5;
			System.out.println("Dummy malcolm device creating nexus file " + filePath);
			TreeFile treeFile = NexusNodeFactory.createTreeFile(filePath);
			NXroot root = NexusNodeFactory.createNXroot();
			treeFile.setGroupNode(root);
			NXentry entry = NexusNodeFactory.createNXentry();
			root.setEntry(entry);
			final DummyMalcolmModel model = getModel();

			// add the positioners to the entry
			for (String positionerName : model.getPositionerNames()) {
				for (String suffix : new String[] {"", ".min", ".max"}) {
					NXpositioner positioner = NexusNodeFactory.createNXpositioner();
					final String nameWithSuffix = positionerName.concat(suffix);
					// The path to positioner datasets written by malcolm is e.g. /entry/x/x
					entry.addGroupNode(nameWithSuffix, positioner);
					addDataset(nameWithSuffix, positioner.initializeLazyDataset(
							nameWithSuffix, getScanRank(), Double.class));
				}
			}

			// add the monitors to the entry
			for (String monitorName : model.getMonitorNames()) {
				NXmonitor monitor = NexusNodeFactory.createNXmonitor();
				entry.addGroupNode(monitorName, monitor);
				// TODO: if we want non-scalar monitors we'll have to change the model
				addDataset(monitorName, monitor.initializeLazyDataset(
						monitorName, getScanRank(), Double.class));
			}

			// add an entry to the unique keys collection
			String[] uniqueKeysDatasetPathSegments = UNIQUE_KEYS_DATASET_PATH.split("/");
			NXcollection ndAttributesCollection = NexusNodeFactory.createNXcollection();
			entry.setCollection(uniqueKeysDatasetPathSegments[2], ndAttributesCollection);
			addDataset(DATASET_NAME_UNIQUE_KEYS, ndAttributesCollection.initializeLazyDataset(
					uniqueKeysDatasetPathSegments[3], getScanRank(), String.class));

			nexusFile = saveNexusFile(treeFile);
		}

		@Override
		public void writePosition(IPosition position) throws Exception {
			if (nexusFile == null) return;

			final DummyMalcolmModel model = getModel();
			for (String positionerName : model.getPositionerNames()) {
				Object posValue = position.get(positionerName);
				if (posValue == null) { // a malcolm controlled positioner which is not a axis (maybe aggregated, e.g. one of a group of jacks)
					posValue = Random.rand();
				}
				IDataset data = DatasetFactory.createFromObject(posValue);
				writeData(positionerName, position, data);
			}
			for (String monitorName : model.getMonitorNames()) {
				writeData(monitorName, position, Random.rand());
			}

			// write unique key
			final int uniqueKey = position.getStepIndex() + 1;
			final IDataset newPositionData = DatasetFactory.createFromObject(uniqueKey);
			writeData(DATASET_NAME_UNIQUE_KEYS, position, newPositionData);
			nexusFile.flush();
		}

		@Override
		public String getName() {
			return "panda";
		}

	}

	public static final String DATASET_NAME_UNIQUE_KEYS = "uniqueKeys";

	public static final String UNIQUE_KEYS_DATASET_PATH = "/entry/NDAttributes/NDArrayUniqueId";

	public static final String FILE_EXTENSION_HDF5 = ".h5";

	private static Logger logger = LoggerFactory.getLogger(DummyMalcolmDevice.class);

	private ChoiceAttribute state;
	private StringAttribute health;
	private BooleanAttribute busy;
	private NumberAttribute completedSteps;
	private NumberAttribute configuredSteps;
	private NumberAttribute totalSteps;
	private StringArrayAttribute availableAxes;
	private TableAttribute datasets;
	private TableAttribute layout;
	private List<String> axesToMove;

	private Map<String, MalcolmAttribute<?>> allAttributes;

	private boolean firstRunCompleted = false;

	private int stepIndex = 0;

	private boolean paused = false;

	private int scanRank;

	private DeviceState deviceState;

	// the dummy devices are responsible for writing the nexus files
	private Map<String, IDummyMalcolmControlledDevice> devices = null;

	public DummyMalcolmDevice() {
		super(Services.getRunnableDeviceService()); // Necessary if you are going to spring it
		this.model = new DummyMalcolmModel();
		setupAttributes();
		setDeviceState(DeviceState.READY);
	}

	private void setupAttributes() {
		allAttributes = new LinkedHashMap<>();

		state = new ChoiceAttribute();
		state.setChoices(Arrays.stream(DeviceState.values()).map(DeviceState::toString).toArray(String[]::new));
		state.setValue(DeviceState.READY.toString());
		state.setName("state");
		state.setLabel("state");
		state.setDescription("State of Block");
		state.setWriteable(false);
		allAttributes.put(state.getName(), state);

		health = new StringAttribute();
		health.setValue("Waiting");
		health.setName("health");
		health.setLabel("health");
		health.setDescription("Health of Block");
		health.setWriteable(false);
		allAttributes.put(health.getName(), health);

		busy = new BooleanAttribute();
		busy.setValue(false);
		busy.setName("busy");
		busy.setLabel("busy");
		busy.setDescription("Whether Block busy or not");
		busy.setWriteable(false);
		allAttributes.put(busy.getName(), busy);

		completedSteps = new NumberAttribute();
		completedSteps.setDtype("int32");
		completedSteps.setValue(0);
		completedSteps.setName("completedSteps");
		completedSteps.setLabel("completedSteps");
		completedSteps.setDescription("Readback of number of scan steps");
		completedSteps.setWriteable(false);
		allAttributes.put(completedSteps.getName(), completedSteps);

		configuredSteps = new NumberAttribute();
		configuredSteps.setDtype("int32");
		configuredSteps.setValue(0);
		configuredSteps.setName("configuredSteps");
		configuredSteps.setLabel("configuredSteps");
		configuredSteps.setDescription("Number of steps currently configured");
		allAttributes.put(configuredSteps.getName(), configuredSteps);

		totalSteps = new NumberAttribute();
		totalSteps.setDtype("int32");
		totalSteps.setValue(0);
		totalSteps.setName("totalSteps");
		totalSteps.setLabel("totalSteps");
		totalSteps.setDescription("Readback of number of scan steps");
		totalSteps.setWriteable(false);
		allAttributes.put(totalSteps.getName(), totalSteps);

		availableAxes = new StringArrayAttribute();
		availableAxes.setValue(model.getAxesToMove().toArray(new String[model.getAxesToMove().size()]));
		availableAxes.setName(ATTRIBUTE_NAME_SIMULTANEOUS_AXES);
		availableAxes.setLabel(ATTRIBUTE_NAME_SIMULTANEOUS_AXES);
		availableAxes.setDescription("Default axis names to scan for configure()");
		availableAxes.setWriteable(false);
		allAttributes.put(availableAxes.getName(), availableAxes);
		axesToMove = new ArrayList<>(model.getAxesToMove());

		// set scanRank to the size of axesToMove initially. this will be overwritten before a scan starts
		scanRank = availableAxes.getValue().length;
	}

	@Override
	public void setModel(IMalcolmModel model) {
		super.setModel(model);
		axesToMove = model.getAxesToMove();
	}

	@Override
	public void validate(IMalcolmModel model) throws ValidationException {
		super.validate(model);

		// validate field: axesToMove
		if (model.getAxesToMove() != null) {
			final List<String> axesToMove = Arrays.asList(this.availableAxes.getValue());
			for (String axisToMove : model.getAxesToMove()) {
				if (!axesToMove.contains(axisToMove)) {
					throw new ModelValidationException("Invalid axis name: " + axisToMove, model, "axesToMove");
				}
			}
		}

		// validate file dir if set
		if (getOutputDir() != null) {
			final File fileDir = new File(getOutputDir());
			if (!fileDir.exists()) {
				throw new ModelValidationException("The output dir for malcolm does not exist: " + getOutputDir(),
						model, "fileDir");
			}
			if (!fileDir.isDirectory()) {
				throw new ModelValidationException("The output dir for malcolm is not a directory: " + getOutputDir(),
						model, "fileDir");
			}
		}
	}

	@Override
	public void configure(IMalcolmModel model) throws ScanningException {
		setDeviceState(DeviceState.CONFIGURING);

		// Note: cannot create dataset attr at this point as we don't know the scan rank,
		// which is required for the datasets for the scannables
		totalSteps.setValue(64);
		configuredSteps.setValue(64);
		stepIndex = 0;

		// check that all the axes in axesToMove are in the set of available axes
		final List<String> availableAxes = getAvailableAxes();
		if (!availableAxes.containsAll(model.getAxesToMove())) {
			throw new MalcolmDeviceException("Unknown axis: " + model.getAxesToMove().stream()
					.filter(axisName -> !availableAxes.contains(axisName)).findFirst().get());
		}
		axesToMove = model.getAxesToMove();

		// super.configure sets device state to ready
		super.configure(model);

		final List<IMalcolmDetectorModel> detectorModels = ((DummyMalcolmModel) model).getDetectorModels();
		devices = detectorModels.stream().collect(Collectors.toMap(
				INameable::getName, detModel -> new DummyMalcolmDetector((DummyMalcolmDetectorModel) detModel)));
		devices.put("panda", new DummyPandaDevice());
	}

	@Override
	@ScanFinally
	public void scanFinally() throws ScanningException {
		super.scanFinally();
		// close all the nexus file
		if (devices!=null) for (Map.Entry<String, IDummyMalcolmControlledDevice> entry : devices.entrySet()) {
			try {
				entry.getValue().closeNexusFile();
			} catch (NexusException e) {
				throw new ScanningException("Unable to create nexus file for device " + entry.getKey());
			}
		}

		// reset device state for next scan.
		devices = null;
		firstRunCompleted = false;
		setDeviceState(DeviceState.READY);
	}

	@Override
	protected void setDeviceState(DeviceState nstate) {
		deviceState = nstate;
		if (state != null) {
			state.setValue(nstate.toString());
		}
	}

	@Override
	public DeviceState getDeviceState() throws ScanningException {
		return deviceState;
	}

	@Override
	@PreConfigure
	public void setPointGenerator(IPointGenerator<? extends IScanPointGeneratorModel> pointGenerator) {
		super.setPointGenerator(pointGenerator);

		if (pointGenerator!=null) { // Some tests end up using the configure call of
			                        // RunnableDeviceService which does not have a pointGenerator
			scanRank = pointGenerator.getRank(); // note, scanRank of a static generator is 1 (i.e. acquire scan)
			axesToMove = calculateAxesToMove(axesToMove, pointGenerator);
		}
	}

	@Override
	public DummyMalcolmModel getModel() {
		return (DummyMalcolmModel) super.getModel();
	}

	@Override
	public IMalcolmModel validateWithReturn(IMalcolmModel model) throws ValidationException {
		// the dummy malcolm device only allows frames per step between 1 and 10
		for (IMalcolmDetectorModel detModel : model.getDetectorModels()) {
			int framesPerStep = detModel.getFramesPerStep();
			framesPerStep = Math.max(1, framesPerStep);
			framesPerStep = Math.min(10, framesPerStep);
			detModel.setFramesPerStep(framesPerStep);

			// round the exposure time to 2 decimal places. This is just an example for testing the EditMalcolmModel dialog
			double exposureTime = detModel.getExposureTime();
			BigDecimal bigD = BigDecimal.valueOf(exposureTime).setScale(2, RoundingMode.HALF_UP);
			exposureTime = bigD.doubleValue();
			detModel.setExposureTime(exposureTime);
		}

		return super.validateWithReturn(model);
	}

	private int getScanRank() {
		return scanRank;
	}

	private TableAttribute createDatasetsAttribute(DummyMalcolmModel model) {
		final LinkedHashMap<String, Class<?>> types = new LinkedHashMap<>();
		types.put(DATASETS_TABLE_COLUMN_NAME, String.class);
		types.put(DATASETS_TABLE_COLUMN_FILENAME, String.class);
		types.put(DATASETS_TABLE_COLUMN_TYPE, String.class);
		types.put(DATASETS_TABLE_COLUMN_PATH, String.class);
		types.put(DATASETS_TABLE_COLUMN_RANK, Integer.class);
		types.put(DATASETS_TABLE_COLUMN_UNIQUEID, String.class);

		// add rows for each DummyMalcolmDatasetModel
		MalcolmTable table = new MalcolmTable(types);

		int scanRank = getScanRank();
		for (IMalcolmDetectorModel detectorModel : model.getDetectorModels()) {
			String deviceName = detectorModel.getName();
			MalcolmDatasetType datasetType = PRIMARY; // the first dataset is the primary dataset
			for (DummyMalcolmDatasetModel datasetModel : ((DummyMalcolmDetectorModel) detectorModel).getDatasets()) {
				final String datasetName = datasetModel.getName();
				final String path = String.format("/entry/%s/%s", datasetName, datasetName);
				// The primary dataset is called det.data, whatever its actual name
				final String linkName = datasetType == PRIMARY ? NXdata.NX_DATA : datasetName;
				final int datasetRank = scanRank + datasetModel.getRank();
				table.addRow(createDatasetRow(deviceName, linkName,
						deviceName + FILE_EXTENSION_HDF5, datasetType, path, datasetRank));
				datasetType = SECONDARY;
			}
		}

		// Add rows for the demand values for the axes controlled by malcolm. Malcolm adds these
		// to the NXdata for each primary and secondary dataset of each detector. As they
		// are all the same, the datasets attribute only returns the first one
		if (!model.getDetectorModels().isEmpty()) {
			final String firstDetectorName = model.getDetectorModels().get(0).getName();
			for (String axisToMove : axesToMove) {
				final String datasetName = "value_set";
				final String path = String.format("/entry/%s/%s_set", firstDetectorName, axisToMove); // e.g. /entry/detector/x_set
				table.addRow(createDatasetRow(axisToMove, datasetName,
						firstDetectorName + FILE_EXTENSION_HDF5, POSITION_SET, path, 1));
			}
		}

		// Add rows for the value datasets of each positioner (i.e. read-back-value)
		for (String positionerName: model.getPositionerNames()) {
			final String path = String.format("/entry/%s/%s", positionerName, positionerName); // e.g. /entry/j1/j1
			table.addRow(createDatasetRow(positionerName, "value",
					"panda" + FILE_EXTENSION_HDF5, POSITION_VALUE, path, scanRank));
			final String path_min = String.format("/entry/%s.min/%s.min", positionerName, positionerName); // e.g. /entry/j1.min/j1.min
			table.addRow(createDatasetRow(positionerName, "min",
					"panda" + FILE_EXTENSION_HDF5, POSITION_MIN, path_min, scanRank));
			final String path_max = String.format("/entry/%s.max/%s.max", positionerName, positionerName); // e.g. /entry/j1.max/j1.max
			table.addRow(createDatasetRow(positionerName, "max",
					"panda" + FILE_EXTENSION_HDF5, POSITION_MAX, path_max, scanRank));
		}

		// Add rows for the value datasets of each monitor
		for (String monitorName : model.getMonitorNames()) {
			final String path = String.format("/entry/%s/%s", monitorName, monitorName); // e.g. /entry/i0/i0
			table.addRow(createDatasetRow(monitorName, "value", "panda" + FILE_EXTENSION_HDF5,
					MONITOR, path, scanRank)); // TODO can currently only handle scalar monitors
		}

		TableAttribute datasets = new TableAttribute();
		datasets.setValue(table);
		datasets.setHeadings(table.getHeadings().toArray(new String[table.getHeadings().size()]));
		datasets.setName("datasets");
		datasets.setLabel("datasets");
		datasets.setDescription("Datasets produced in HDF file");
		datasets.setWriteable(true);

		return datasets;
	}

	private Map<String, Object> createDatasetRow(String deviceName, String datasetName,
			String fileName, MalcolmDatasetType type, String path, int rank) {
		Map<String, Object> datasetRow = new HashMap<>();
		datasetRow.put(DATASETS_TABLE_COLUMN_NAME, deviceName + "." + datasetName);
		datasetRow.put(DATASETS_TABLE_COLUMN_FILENAME, fileName);
		datasetRow.put(DATASETS_TABLE_COLUMN_TYPE, type.name().toLowerCase());
		datasetRow.put(DATASETS_TABLE_COLUMN_PATH, path);
		datasetRow.put(DATASETS_TABLE_COLUMN_RANK, rank);
		datasetRow.put(DATASETS_TABLE_COLUMN_UNIQUEID, UNIQUE_KEYS_DATASET_PATH);
		return datasetRow;
	}

	private TableAttribute createLayoutAttribute() {
		final LinkedHashMap<String, Class<?>> types = new LinkedHashMap<>();
		types.put("name", String.class);
		types.put("mri", String.class);
		types.put("x", Double.class);
		types.put("y", Double.class);
		types.put("visible", Boolean.class);

		// add rows for each DummyMalcolmDatasetModel
		MalcolmTable table = new MalcolmTable(types);

		Map<String, Object> datasetRow1 = new HashMap<>();
		datasetRow1.put("name", "BRICK");
		datasetRow1.put("mri", "P45-BRICK01");
		datasetRow1.put("x", 0);
		datasetRow1.put("y", 0);
		datasetRow1.put("visible", false);

		Map<String, Object> datasetRow2 = new HashMap<>();
		datasetRow2.put("name", "MIC");
		datasetRow2.put("mri", "P45-MIC");
		datasetRow2.put("x", 0);
		datasetRow2.put("y", 0);
		datasetRow2.put("visible", false);

		Map<String, Object> datasetRow3 = new HashMap<>();
		datasetRow3.put("name", "ZEBRA");
		datasetRow3.put("mri", "ZEBRA");
		datasetRow3.put("x", 0);
		datasetRow3.put("y", 0);
		datasetRow3.put("visible", false);

		table.addRow(datasetRow1);
		table.addRow(datasetRow2);
		table.addRow(datasetRow3);

		TableAttribute layout = new TableAttribute();
		layout.setValue(table);
		layout.setHeadings(table.getHeadings().toArray(new String[table.getHeadings().size()]));
		layout.setName("layout");
		layout.setLabel("");
		layout.setDescription("Layout of child blocks");
		layout.setWriteable(false);

		return layout;
	}

	@Override
	public void run(IPosition outerScanPosition) throws ScanningException, InterruptedException {
		paused = false;
		setDeviceState(DeviceState.RUNNING);
		health.setValue("OK");

		if (!firstRunCompleted) {
			createNexusFiles();
			firstRunCompleted = true;
		}

		// get an iterator over the inner scan positions
		final ScanModel scanModel = new ScanModel(pointGenerator, this);
		final SubscanModerator moderator = new SubscanModerator(scanModel);
		IPointGenerator<?> innerScanPositions = moderator.getInnerPointGenerator(); // should never be null

		// get each dummy device to write its position at each inner scan position
		for (IPosition innerScanPosition : innerScanPositions) {
			final long pointStartTime = System.nanoTime();
			final long targetDuration = (long) (model.getExposureTime() * 1000000000.0); // nanoseconds

			while (paused) {
				Thread.sleep(100); // TODO, use Condition/awaitPaused flag, see AcquisitionDevice
			}
			final IPosition overallScanPosition = calculateOverallScanPosition(outerScanPosition, innerScanPosition);
			for (IDummyMalcolmControlledDevice device : devices.values()) {
				try {
					device.writePosition(overallScanPosition);
				} catch (Exception e) {
					logger.error("Couldn't write data for device " + device.getName(), e);
				}
			}

			// If required, sleep until the requested exposure time is over
			long currentTime = System.nanoTime();
			long duration = currentTime - pointStartTime;
			if (duration < targetDuration) {
				long millisToWait = (targetDuration - duration) / 1000000;
				Thread.sleep(millisToWait);
			}

			completedSteps.setValue(stepIndex);
			innerScanPosition.setStepIndex(stepIndex);
			try {
				sendEvent(MalcolmEvent.forStepsCompleted(this, stepIndex, "Completed step " + stepIndex));
			} catch (Exception e) {
				throw new ScanningException(e);
			}
			stepIndex++;
		}

		health.setValue("OK");
		setDeviceState(DeviceState.ARMED);
	}

	private IPosition calculateOverallScanPosition(IPosition outerScanPosition, IPosition innerScanPosition) {
		if (isMultiScan) {
			return innerScanPosition;
		}
		return innerScanPosition.compound(outerScanPosition);
	}

	private void createNexusFiles() throws ScanningException {
		if (model.getDetectorModels().isEmpty()) return;

		String dirPath = getOutputDir();
		if (dirPath == null) return; // we can run without writing Nexus
		if (!dirPath.endsWith("/")) {
			dirPath += "/";
		}

		for (Map.Entry<String, IDummyMalcolmControlledDevice> entry : devices.entrySet()) {
			try {
				entry.getValue().createNexusFile(dirPath);
			} catch (NexusException e) {
				throw new ScanningException("Unable to create nexus file for device " + entry.getKey());
			}
		}
	}

	private NexusFile saveNexusFile(TreeFile nexusTree) throws NexusException {
		INexusFileFactory nff = ServiceHolder.getNexusFileFactory();
		NexusFile file = nff.newNexusFile(nexusTree.getFilename(), true);
		file.createAndOpenToWrite();
		file.addNode("/", nexusTree.getGroupNode());
		file.flush();

		return file;
	}

	@Override
	public void dispose() throws MalcolmDeviceException {
		// nothing to do

	}

	@Override
	public boolean isLocked() throws MalcolmDeviceException {
		// never locked
		return false;
	}

	private <T> T getAttributeValue(String attributeName) throws MalcolmDeviceException {
		try {
			updateAttributesWithLatestValues();
		} catch (ScanningException e) {
			throw new MalcolmDeviceException(e.getMessage());
		}

		@SuppressWarnings("unchecked")
		IDeviceAttribute<T> malcolmAttribute = (IDeviceAttribute<T>) allAttributes.get(attributeName);
		if (malcolmAttribute != null) {
			return malcolmAttribute.getValue();
		}
		return null;
	}

	private <T> IDeviceAttribute<T> getAttribute(String attributeName) throws ScanningException {
		updateAttributesWithLatestValues();

		@SuppressWarnings("unchecked")
		IDeviceAttribute<T> attribute = (IDeviceAttribute<T>) allAttributes.get(attributeName);
		if (attribute == null) {
			throw new MalcolmDeviceException("No such attribute: " + attributeName);
		}
		return attribute;
	}

	public <T> void setAttributeValue(String attributeName, T value) throws ScanningException {
		Object attr = getAttribute(attributeName);
		if (attr==null) throw new ScanningException("There is no attribute called "+attributeName);
		try {
			Method setValue = attr.getClass().getMethod("setValue", value.getClass());
			setValue.invoke(attr, value);
		} catch (NoSuchMethodError | Exception ne) {
			throw new ScanningException(ne);
		}
	}

	@Override
	public void pause() throws ScanningException {
		setDeviceState(DeviceState.PAUSED);
		paused = true;
	}

	@Override
	public void resume() throws ScanningException {
		setDeviceState(DeviceState.RUNNING);
		paused = false;
	}

	private void updateAttributesWithLatestValues() throws ScanningException {
		DeviceState deviceState = getDeviceState();
		if (deviceState == null) deviceState = DeviceState.READY;
		state.setValue(deviceState.toString());
		health.setValue(deviceState == DeviceState.FAULT ? "Fault" : "OK");
		busy.setValue(isDeviceBusy());

		datasets = createDatasetsAttribute(getModel());
		allAttributes.put("datasets", datasets);

		layout = createLayoutAttribute();
		allAttributes.put("layout", layout);
	}

	@Override
	public List<String> getAvailableAxes() throws ScanningException {
		final String[] axesArray = getAttributeValue(ATTRIBUTE_NAME_SIMULTANEOUS_AXES);
		return Arrays.asList(axesArray);
	}

	public void setAvailableAxes(List<String> availableAxes) {
		this.availableAxes.setValue(availableAxes.toArray(new String[availableAxes.size()]));
	}

	@Override
	public MalcolmTable getDatasets() throws MalcolmDeviceException {
		return getAttributeValue(ATTRIBUTE_NAME_DATASETS);
	}

	@Override
	public MalcolmVersion getVersion() throws MalcolmDeviceException {
		return MalcolmVersion.VERSION_4_2;
	}

	@Override
	public List<MalcolmDetectorInfo> getDetectorInfos() throws MalcolmDeviceException {
		// for the dummy malcolm device, the detector models in the malcolm model defines the detectors
		return getModel().getDetectorModels().stream().map(this::detectorModelToInfo).collect(toList());
	}

	private MalcolmDetectorInfo detectorModelToInfo(IMalcolmDetectorModel detectorModel) {
		final MalcolmDetectorInfo info = new MalcolmDetectorInfo();
		info.setEnabled(detectorModel.isEnabled());
		info.setName(detectorModel.getName());
		info.setExposureTime(detectorModel.getExposureTime());
		info.setFramesPerStep(detectorModel.getFramesPerStep());
		info.setId(detectorModel.getName()); // the model doesn't have an id field

		return info;
	}

}

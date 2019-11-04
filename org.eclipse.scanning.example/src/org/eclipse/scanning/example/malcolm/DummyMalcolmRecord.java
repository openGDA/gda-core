package org.eclipse.scanning.example.malcolm;

import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_COMPLETED_STEPS;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_DATASETS;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_HEALTH;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_LAYOUT;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_SIMULTANEOUS_AXES;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_STATE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_TOTAL_STEPS;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_FILENAME;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_NAME;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_PATH;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_RANK;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_TYPE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_UNIQUEID;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_ENABLE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_EXPOSURE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_MRI;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_NAME;
import static org.eclipse.scanning.api.malcolm.connector.MalcolmMethod.ABORT;
import static org.eclipse.scanning.api.malcolm.connector.MalcolmMethod.CONFIGURE;
import static org.eclipse.scanning.api.malcolm.connector.MalcolmMethod.DISABLE;
import static org.eclipse.scanning.api.malcolm.connector.MalcolmMethod.RESET;
import static org.eclipse.scanning.api.malcolm.connector.MalcolmMethod.RUN;
import static org.eclipse.scanning.api.malcolm.connector.MalcolmMethod.VALIDATE;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_AXES_TO_MOVE;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_CHOICES;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_DEFAULTS;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_DESCRIPTION;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_DETECTORS;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_DTYPE;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_FILE_DIR;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_FILE_TEMPLATE;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_LABEL;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_LABELS;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_META;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_MRI;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_NAME;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_PRESENT;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_REQUIRED;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_RETURNED;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_RETURNS;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_TAGS;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_TAKES;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_TOOK;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_VALUE;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_VISIBLE;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.FIELD_NAME_WRITEABLE;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_BLOCK;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_BOOLEAN_META;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_CHOICE_META;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_MAP;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_MAP_META;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_METHOD;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_METHOD_LOG;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_METHOD_META;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_NT_SCALAR;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_NT_SCALAR_ARRAY;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_NT_TABLE;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_NUMBER_ARRAY_META;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_NUMBER_META;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_POINT_GENERATOR;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_POINT_GENERATOR_META;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_STRING_ARRAY_META;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_STRING_META;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_TABLE;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_TABLE_META;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.MalcolmConstants;
import org.eclipse.scanning.api.malcolm.MalcolmDetectorInfo;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMethod;
import org.eclipse.scanning.connector.epics.EpicsConnectionConstants;
import org.epics.pvaccess.server.rpc.RPCResponseCallback;
import org.epics.pvaccess.server.rpc.RPCServiceAsync;
import org.epics.pvaccess.server.rpc.Service;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.pv.FieldBuilder;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVBoolean;
import org.epics.pvdata.pv.PVBooleanArray;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVFloat;
import org.epics.pvdata.pv.PVFloatArray;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Union;
import org.epics.pvdatabase.PVDatabase;
import org.epics.pvdatabase.PVDatabaseFactory;
import org.epics.pvdatabase.PVRecord;

class DummyMalcolmRecord extends PVRecord {

	// Static Data
	private static final FieldCreate FIELDCREATE = FieldFactory.getFieldCreate();
	private static final PVDataCreate PVDATACREATE = PVDataFactory.getPVDataCreate();

	private static final String STRUCTURE_ELEMENTS = "elements";

	private static final String FIELD_NAME_STATE_VALUE = "state.value";
	private static final String ATTRIBUTE_NAME_BUSY = "busy";  // TODO real malcolm device has no such attribute
	private static final String ATTRIBUTE_NAME_A = "A";
	private static final String ATTRIBUTE_NAME_B = "B";

	private static final String FIELD_NAME_STAGE_X = "stage_x";
	private static final String FIELD_NAME_STAGE_Y = "stage_y";
	private static final String FIELD_NAME_GENERATOR = "generator"; // TODO real malcolm has no such attribute

	public static final String FIELD_NAME_NAMES = "names";
	public static final String FIELD_NAME_UNITS = "units";
	public static final String FIELD_NAME_SCALE = "scale";
	public static final String FIELD_NAME_CENTRE = "centre";
	public static final String FIELD_NAME_RADIUS = "radius";

	private static List<MalcolmDetectorInfo> malcolmDetectorInfos = null;
	// Member data
	private boolean underControl = false;
	private Map<String, PVStructure> receivedRPCCalls = new HashMap<>();

	public Map<String, PVStructure> getReceivedRPCCalls() {
		return receivedRPCCalls;
	}

	synchronized boolean takeControl() {
		if (!underControl) {
			underControl = true;
			return true;
		}
		return false;
	}

	synchronized void releaseControl() {
		underControl = false;
	}

	private class RPCServiceAsyncImpl implements RPCServiceAsync {

		private DummyMalcolmRecord pvRecord;
		private final Status statusOk = StatusFactory.getStatusCreate().getStatusOK();
		private String methodName = "";

		RPCServiceAsyncImpl(DummyMalcolmRecord record, String methodName) {
			pvRecord = record;
			this.methodName = methodName;
		}

		@Override
		public void request(PVStructure args, RPCResponseCallback callback) {
			System.out.println("Got Async Request:");
			System.out.println(args.toString());
			receivedRPCCalls.put(methodName, args);

			boolean haveControl = pvRecord.takeControl();
			if (!haveControl) {
				handleError("Device busy", callback, haveControl);
				return;
			}

			Structure mapStructure = FIELDCREATE.createFieldBuilder().setId(TYPE_ID_MAP).createStructure();
			PVStructure returnPvStructure = PVDATACREATE.createPVStructure(mapStructure);

			final MalcolmMethod method = MalcolmMethod.fromString(methodName);
			switch (method) {
				case VALIDATE:
					returnPvStructure = modifyValidationPVStucture(args);
					break;
				case CONFIGURE:
					pvRecord.getPVStructure().getSubField(PVString.class, FIELD_NAME_STATE_VALUE).put(DeviceState.CONFIGURING.name());
					break;
				case RUN:
					pvRecord.getPVStructure().getSubField(PVString.class, FIELD_NAME_STATE_VALUE).put(DeviceState.RUNNING.name());
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					break;
				default:
			}

			pvRecord.getPVStructure().getSubField(PVString.class, FIELD_NAME_STATE_VALUE).put(DeviceState.ARMED.name());

			pvRecord.releaseControl();
			callback.requestDone(statusOk, returnPvStructure);
		}

		private PVStructure modifyValidationPVStucture(PVStructure pvStructure) {
			final PVStructure detectorsStructure = pvStructure.getStructureField(FIELD_NAME_DETECTORS);

			final List<MalcolmDetectorInfo> detectorInfos = getDefaultMalcolmDetectorInfos();
			detectorInfos.get(0).setExposureTime(0.1);
			detectorInfos.get(0).setFramesPerStep(1);
			detectorInfos.get(1).setEnabled(false);
			detectorInfos.get(1).setExposureTime(0.025);
			detectorInfos.get(1).setFramesPerStep(4);
			detectorInfos.get(2).setEnabled(true);

			populateDetectorsPVStructure(detectorsStructure, detectorInfos);

			System.err.println("detectors pvStructure = " + detectorsStructure); // TODO REMOVE, DO NOT COMMIT
			return pvStructure;
		}

		private void handleError(String message, RPCResponseCallback callback, boolean haveControl) {
			if (haveControl)
				pvRecord.releaseControl();
			Status status = StatusFactory.getStatusCreate().createStatus(StatusType.ERROR, message, null);
			callback.requestDone(status, null);
		}
	}

	public static DummyMalcolmRecord create(String recordName) {
		FieldBuilder fb = FIELDCREATE.createFieldBuilder();

		// The structure of the 'meta' field - a field that contains the names of the other fields
		Structure metaStructure = newDefaultField(EpicsConnectionConstants.TYPE_ID_BLOCK_META);
		Structure choiceMetaStructure = newDefaultFieldBuilder().addArray(FIELD_NAME_CHOICES, ScalarType.pvString)
				.setId(TYPE_ID_CHOICE_META).createStructure();
		Structure stringMetaStructure = newDefaultField(TYPE_ID_STRING_META);
		Structure booleanMetaStructure = newDefaultField(TYPE_ID_BOOLEAN_META);
		Structure intNumberMetaStructure = newScalarTypeField(TYPE_ID_NUMBER_META, ScalarType.pvString);
		Structure floatNumberMetaStructure = newScalarTypeField(TYPE_ID_NUMBER_META, ScalarType.pvString);
		Structure stringArrayMetaStructure = newDefaultField(TYPE_ID_STRING_ARRAY_META);
		Structure numberArrayMetaStructure = newScalarTypeField(TYPE_ID_NUMBER_ARRAY_META, ScalarType.pvString);

		Structure simpleMetaMethodArgmentsStructure = FIELDCREATE.createFieldBuilder().addArray(EpicsConnectionConstants.FIELD_NAME_REQUIRED, ScalarType.pvString)
				.setId(TYPE_ID_MAP_META).createStructure();

		Structure pointGeneratorMetaStructure = newDefaultField(TYPE_ID_POINT_GENERATOR_META);
		Structure detectorsTableElementsStructure = newDefaultFieldBuilder()
				.add(DETECTORS_TABLE_COLUMN_ENABLE, booleanMetaStructure)
				.add(DETECTORS_TABLE_COLUMN_NAME, stringArrayMetaStructure)
				.add(DETECTORS_TABLE_COLUMN_MRI, stringArrayMetaStructure)
				.add(DETECTORS_TABLE_COLUMN_EXPOSURE, numberArrayMetaStructure)
				.add(DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP, numberArrayMetaStructure)
				.createStructure();

		Structure detectorsMetaStructure = newDefaultFieldBuilder().add(STRUCTURE_ELEMENTS, detectorsTableElementsStructure)
				.setId(TYPE_ID_TABLE_META).createStructure();

		Structure malcolmModelStructure = FIELDCREATE.createFieldBuilder()
				.add(FIELD_NAME_GENERATOR, pointGeneratorMetaStructure)
				.add(FIELD_NAME_FILE_DIR, stringMetaStructure)
				.add(FIELD_NAME_AXES_TO_MOVE, stringArrayMetaStructure)
				.add(FIELD_NAME_DETECTORS, detectorsMetaStructure)
				.add(FIELD_NAME_FILE_TEMPLATE, stringMetaStructure)
				.createStructure();

		Structure malcolmModelArgumentsStructure = FIELDCREATE.createFieldBuilder()
				.add(STRUCTURE_ELEMENTS, malcolmModelStructure)
				.addArray(FIELD_NAME_REQUIRED, ScalarType.pvString).setId(TYPE_ID_MAP_META).createStructure();

		Structure tableElementsStructure = FIELDCREATE.createFieldBuilder()
				.add(DATASETS_TABLE_COLUMN_NAME, stringArrayMetaStructure)
				.add(DATASETS_TABLE_COLUMN_FILENAME, stringArrayMetaStructure)
				.add(DATASETS_TABLE_COLUMN_TYPE, stringArrayMetaStructure)
				.add(DATASETS_TABLE_COLUMN_RANK, numberArrayMetaStructure)
				.add(DATASETS_TABLE_COLUMN_PATH, stringArrayMetaStructure)
				.add(DATASETS_TABLE_COLUMN_UNIQUEID, stringArrayMetaStructure)
				.createStructure();

		Structure tableMetaStructure = newDefaultFieldBuilder().add(STRUCTURE_ELEMENTS, tableElementsStructure)
				.setId(TYPE_ID_TABLE_META).createStructure();

		// Attributes
		Structure stateStructure = createValueStructure(TYPE_ID_NT_SCALAR, ScalarType.pvString, choiceMetaStructure);
		Structure healthStructure = createValueStructure(TYPE_ID_NT_SCALAR, ScalarType.pvString, stringMetaStructure);
		Structure stringArrayAttributeStructure = FIELDCREATE.createFieldBuilder().add(FIELD_NAME_META, stringArrayMetaStructure)
				.addArray(FIELD_NAME_VALUE, ScalarType.pvString).setId(TYPE_ID_NT_SCALAR_ARRAY).createStructure();
		Structure booleanStructure = createValueStructure(TYPE_ID_NT_SCALAR, ScalarType.pvBoolean, booleanMetaStructure);
		Structure intStructure = createValueStructure(TYPE_ID_NT_SCALAR, ScalarType.pvInt, intNumberMetaStructure);

		Structure datasetValueTableStructure = FIELDCREATE.createFieldBuilder()
				.addArray(DATASETS_TABLE_COLUMN_NAME, ScalarType.pvString)
				.addArray(DATASETS_TABLE_COLUMN_FILENAME, ScalarType.pvString)
				.addArray(DATASETS_TABLE_COLUMN_TYPE, ScalarType.pvString)
				.addArray(DATASETS_TABLE_COLUMN_RANK, ScalarType.pvInt)
				.addArray(DATASETS_TABLE_COLUMN_PATH, ScalarType.pvString)
				.addArray(DATASETS_TABLE_COLUMN_UNIQUEID, ScalarType.pvString)
				.setId(TYPE_ID_TABLE)
				.createStructure();

		Structure datasetTableStructure = FIELDCREATE.createFieldBuilder().add(FIELD_NAME_META, tableMetaStructure)
				.addArray(FIELD_NAME_LABELS, ScalarType.pvString).add(FIELD_NAME_VALUE, datasetValueTableStructure)
				.setId(TYPE_ID_NT_TABLE).createStructure();

		Structure layoutTableValueStructure = FIELDCREATE.createFieldBuilder().addArray(FIELD_NAME_NAME, ScalarType.pvString)
				.addArray(FIELD_NAME_MRI, ScalarType.pvString).addArray(FIELD_NAME_STAGE_X, ScalarType.pvFloat)
				.addArray(FIELD_NAME_STAGE_Y, ScalarType.pvFloat).addArray(FIELD_NAME_VISIBLE, ScalarType.pvBoolean)
				.createStructure();

		// TODO: the layout table incorrectly reuses the same table meta as for the datasets table, fix this
		Structure layoutTableStructure = FIELDCREATE.createFieldBuilder().add(FIELD_NAME_META, tableMetaStructure)
				.addArray(FIELD_NAME_LABELS, ScalarType.pvString).add(FIELD_NAME_VALUE, layoutTableValueStructure)
				.setId(TYPE_ID_NT_TABLE).createStructure();

		Structure simpleMethodStructure = createMethodStructure(simpleMetaMethodArgmentsStructure, simpleMetaMethodArgmentsStructure);

		Structure detectorsDefaultTableStructure = FIELDCREATE.createFieldBuilder()
				.addArray(DETECTORS_TABLE_COLUMN_ENABLE, ScalarType.pvBoolean)
				.addArray(DETECTORS_TABLE_COLUMN_NAME, ScalarType.pvString)
				.addArray(DETECTORS_TABLE_COLUMN_MRI, ScalarType.pvString)
				.addArray(DETECTORS_TABLE_COLUMN_EXPOSURE, ScalarType.pvDouble)
				.addArray(DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP, ScalarType.pvInt)
				.setId(TYPE_ID_TABLE)
				.createStructure();

		Structure malcolmModelDefaultsStructure = FIELDCREATE.createFieldBuilder()
				.add(FIELD_NAME_DETECTORS, detectorsDefaultTableStructure)
				.add(FIELD_NAME_FILE_TEMPLATE, ScalarType.pvString)
				.createStructure();

		Structure configureMethodStructure = createMethodStructure(malcolmModelArgumentsStructure,
				simpleMetaMethodArgmentsStructure, malcolmModelDefaultsStructure);
		Structure validateMethodStructure = createMethodStructure(malcolmModelArgumentsStructure,
				malcolmModelArgumentsStructure, malcolmModelDefaultsStructure);

		Structure floatStructure = FIELDCREATE.createFieldBuilder().add(FIELD_NAME_META, floatNumberMetaStructure)
				.add(FIELD_NAME_VALUE, ScalarType.pvFloat).setId(TYPE_ID_NT_SCALAR).createStructure();

		Union union = FieldFactory.getFieldCreate().createVariantUnion();
		Structure generatorStructure = FieldFactory.getFieldCreate().createFieldBuilder().addArray("mutators", union)
				.addArray("generators", union).addArray("excluders", union)
				.setId("scanpointgenerator:generator/CompoundGenerator:1.0").createStructure();

		Structure spiralGeneratorStructure = FieldFactory.getFieldCreate().createFieldBuilder()
				.addArray(FIELD_NAME_CENTRE, ScalarType.pvDouble).add(FIELD_NAME_SCALE, ScalarType.pvDouble).add(FIELD_NAME_UNITS, ScalarType.pvString)
				.addArray(FIELD_NAME_NAMES, ScalarType.pvString).add("alternate_direction", ScalarType.pvBoolean)
				.add(FIELD_NAME_RADIUS, ScalarType.pvDouble).setId("scanpointgenerator:generator/SpiralGenerator:1.0")
				.createStructure();

		Structure pointGeneratorStructure = FieldFactory.getFieldCreate().createFieldBuilder()
				.add(FIELD_NAME_META, pointGeneratorMetaStructure).add(FIELD_NAME_VALUE, generatorStructure)
				.setId(TYPE_ID_POINT_GENERATOR).createStructure();

		// Device
		Structure deviceStructure = fb.add(FIELD_NAME_META, metaStructure) // a string array of the names of the other fields
				.add(ATTRIBUTE_NAME_STATE, stateStructure) // a choice (enum) of string values
				.add(ATTRIBUTE_NAME_HEALTH, healthStructure) // a string value
				.add(ATTRIBUTE_NAME_BUSY, booleanStructure) // a boolean, Note: a real malcolm device does not have this attribute
				.add(ATTRIBUTE_NAME_TOTAL_STEPS, intStructure)
				.add(ABORT.toString(), simpleMethodStructure)
				.add(CONFIGURE.toString(), configureMethodStructure)
				.add(DISABLE.toString(), simpleMethodStructure)
				.add(RESET.toString(), simpleMethodStructure)
				.add(RUN.toString(), simpleMethodStructure)
				.add(VALIDATE.toString(), validateMethodStructure)
				// Note that this test does not add PAUSE and RESUME methods yet, they could be added if necessary
				.add(ATTRIBUTE_NAME_A, floatStructure) // Attributes 'A' and 'B' are just for test, a real malcolm devices doesn't have them
				.add(ATTRIBUTE_NAME_B, floatStructure)
				.add(ATTRIBUTE_NAME_SIMULTANEOUS_AXES, stringArrayAttributeStructure)
				.add(ATTRIBUTE_NAME_LAYOUT, layoutTableStructure)
				.add(ATTRIBUTE_NAME_DATASETS, datasetTableStructure)
				.add(FIELD_NAME_GENERATOR, pointGeneratorStructure) // TODO this attribute no longer exists, has it been replaced
				.add(ATTRIBUTE_NAME_COMPLETED_STEPS, intStructure)
				.setId(TYPE_ID_BLOCK).createStructure();

		PVStructure blockPVStructure = PVDATACREATE.createPVStructure(deviceStructure);

		// Fill in the values to the fields above
		// State
		String[] choicesArray = new String[] { "Resetting", "Ready", "Armed", "Configuring", "Running", "PostRun",
				"Paused", "Rewinding", "Aborting", "Aborted", "Fault", "Disabling", "Disabled" };

		PVStringArray choices = blockPVStructure.getSubField(PVStringArray.class, "state.meta.choices");
		choices.put(0, choicesArray.length, choicesArray, 0);

		blockPVStructure.getSubField(PVString.class, FIELD_NAME_STATE_VALUE).put("READY");

		// Health
		blockPVStructure.getSubField(PVString.class, "health.value").put("Test Health");

		// Busy
		blockPVStructure.getSubField(PVBoolean.class, "busy.value").put(false);

		// Total Steps
		blockPVStructure.getSubField(PVInt.class, "totalSteps.value").put(123);

		// A
		blockPVStructure.getSubField(PVFloat.class, "A.value").put(0.0f);

		// B
		blockPVStructure.getSubField(PVFloat.class, "B.value").put(5.2f);
		blockPVStructure.getSubField(PVBoolean.class, "B.meta.writeable").put(true);

		// axes
		String[] axesArray = new String[] { FIELD_NAME_STAGE_X, FIELD_NAME_STAGE_Y };

		PVStringArray axes = blockPVStructure.getSubField(PVStringArray.class, "simultaneousAxes.value");
		axes.put(0, axesArray.length, axesArray, 0);

		// datasets
		populateDatasetsAttribute(blockPVStructure);

		// default detectors
		populateConfigureDefault(blockPVStructure);

		// current step
		blockPVStructure.getSubField(PVInt.class, "completedSteps.value").put(1);

		// layout
		populateLayoutAttribute(blockPVStructure);

		PVStructure spiralGeneratorPVStructure = PVDataFactory.getPVDataCreate()
				.createPVStructure(spiralGeneratorStructure);
		double[] acentre = new double[] { 3.5, 4.5 };
		spiralGeneratorPVStructure.getSubField(PVDoubleArray.class, FIELD_NAME_CENTRE).put(0, acentre.length, acentre, 0);
		spiralGeneratorPVStructure.getDoubleField(FIELD_NAME_SCALE).put(1.5);
		spiralGeneratorPVStructure.getStringField(FIELD_NAME_UNITS).put("mm");
		String[] anames = new String[] { FIELD_NAME_STAGE_X, FIELD_NAME_STAGE_Y };
		spiralGeneratorPVStructure.getSubField(PVStringArray.class, FIELD_NAME_NAMES).put(0, anames.length, anames, 0);
		spiralGeneratorPVStructure.getBooleanField("alternate_direction").put(true);
		spiralGeneratorPVStructure.getDoubleField(FIELD_NAME_RADIUS).put(5.5);

		PVUnion pvu1 = PVDataFactory.getPVDataCreate().createPVVariantUnion();
		pvu1.set(spiralGeneratorPVStructure);
		PVUnion[] unionArray = new PVUnion[1];
		unionArray[0] = pvu1;
		blockPVStructure.getUnionArrayField("generator.value.generators").put(0, unionArray.length, unionArray, 0);

		DummyMalcolmRecord pvRecord = new DummyMalcolmRecord(recordName, blockPVStructure);
		PVDatabase master = PVDatabaseFactory.getMaster();
		master.addRecord(pvRecord);
		return pvRecord;
	}

	private static void populateDatasetsAttribute(PVStructure blockPVStructure) {
		PVStructure datasetsPVStructure = blockPVStructure.getStructureField(ATTRIBUTE_NAME_DATASETS);
		String[] namesArray = new String[] { "DET1.data", "DET1.sum", "DET2.data", "DET2.sum", "stagey.value_set", "stagex.value_set", "stagey.value", "stagex.value" };
		String[] filenameArray = new String[] { "DET1.h5", "DET1.h5", "DET2.h5", "DET2.h5", "PANDA-01.h5", "PANDA-01.h5", "PANDA-02.h5", "PANDA-02.h5" };
		String[] typeArray = new String[] { "primary", "second", "primary", "secondary", "position_set", "position_set", "position_value", "position_value" };
		int[] rankArray = new int[] { 4, 2, 4, 2, 1, 1, 2, 2 };
		String[] pathArray = new String[] { "/entry/detector1/detector1", "/entry/sum1/sum1", "/entry/detector2/detector2", "/entry/sum2/sum2",
					"/entry/detector1/stagey_set", "/entry/detector1/stagex_set", "/entry/detector1/stagey", "/entry/detector1/stagex" };
		String uniqueIDPath = "/entry/NDAttributes/NDArrayUniqueId";
		String[] uniqueIdArray = new String[] { uniqueIDPath, uniqueIDPath, uniqueIDPath, uniqueIDPath, "", "", uniqueIDPath, uniqueIDPath };
		PVStructure tableValuePVStructure = datasetsPVStructure.getStructureField(FIELD_NAME_VALUE);
		tableValuePVStructure.getSubField(PVStringArray.class, DATASETS_TABLE_COLUMN_NAME).put(0, namesArray.length, namesArray, 0);
		tableValuePVStructure.getSubField(PVStringArray.class, DATASETS_TABLE_COLUMN_FILENAME).put(0, filenameArray.length, filenameArray, 0);
		tableValuePVStructure.getSubField(PVStringArray.class, DATASETS_TABLE_COLUMN_TYPE).put(0, typeArray.length, typeArray, 0);
		tableValuePVStructure.getSubField(PVIntArray.class, DATASETS_TABLE_COLUMN_RANK).put(0, rankArray.length, rankArray, 0);
		tableValuePVStructure.getSubField(PVStringArray.class, DATASETS_TABLE_COLUMN_PATH).put(0, pathArray.length, pathArray, 0);
		tableValuePVStructure.getSubField(PVStringArray.class, DATASETS_TABLE_COLUMN_UNIQUEID).put(0, uniqueIdArray.length, uniqueIdArray, 0);

		String[] headingsArray = new String[] { DATASETS_TABLE_COLUMN_NAME, DATASETS_TABLE_COLUMN_FILENAME, DATASETS_TABLE_COLUMN_TYPE,
				DATASETS_TABLE_COLUMN_RANK, DATASETS_TABLE_COLUMN_PATH, DATASETS_TABLE_COLUMN_UNIQUEID };
		datasetsPVStructure.getSubField(PVStringArray.class, FIELD_NAME_LABELS).put(0, headingsArray.length, headingsArray, 0);
	}

	private static List<MalcolmDetectorInfo> getDefaultMalcolmDetectorInfos() {
		if (malcolmDetectorInfos == null) {
			malcolmDetectorInfos = new ArrayList<>(4);
			malcolmDetectorInfos.add(new MalcolmDetectorInfo("BL45P-ML-DET-01", "DET", 1, 0, true));
			malcolmDetectorInfos.add(new MalcolmDetectorInfo("BL45P-ML-DIFF-01", "DIFF", 1, 0, true));
			malcolmDetectorInfos.add(new MalcolmDetectorInfo("BL45P-ML-PANDA-01", "PANDA-01", 1, 0, false));
			malcolmDetectorInfos.add(new MalcolmDetectorInfo("BL45P-ML-PANDA-02", "PANDA-02", 1, 0, true));
		}

		return malcolmDetectorInfos;
	}

	private static PVStructure populateConfigureDefault(PVStructure blockPVStructure) {
		final PVStructure defaultsPVStructure = blockPVStructure.getStructureField(CONFIGURE.toString())
				.getStructureField(FIELD_NAME_META).getStructureField(FIELD_NAME_DEFAULTS);
		defaultsPVStructure.getSubField(PVString.class, FIELD_NAME_FILE_TEMPLATE).put("%s.h5");
		final PVStructure defaultDetectorsPVStructure = defaultsPVStructure.getStructureField(FIELD_NAME_DETECTORS);

		return populateDetectorsPVStructure(defaultDetectorsPVStructure, getDefaultMalcolmDetectorInfos());
	}

	private static PVStructure populateDetectorsPVStructure(final PVStructure detectorsPvStructure,
			final List<MalcolmDetectorInfo> detectorInfos) {
		final int numDetectors = detectorInfos.size();
		final boolean[] enabledArray = new boolean[numDetectors];
		final String[] nameArray = new String[numDetectors];
		final String[] mriArray = new String[numDetectors];
		final double[] exposureTimeArray = new double[numDetectors];
		final int[] framesPerStepArray = new int[numDetectors];

		for (int i = 0; i < numDetectors; i++) {
			final MalcolmDetectorInfo detectorInfo = detectorInfos.get(i);
			enabledArray[i] = detectorInfo.isEnabled();
			nameArray[i] = detectorInfo.getName();
			mriArray[i] = detectorInfo.getId();
			exposureTimeArray[i] = detectorInfo.getExposureTime();
			framesPerStepArray[i] = detectorInfo.getFramesPerStep();
		}

		detectorsPvStructure.getSubField(PVBooleanArray.class, DETECTORS_TABLE_COLUMN_ENABLE).put(0, enabledArray.length, enabledArray, 0);
		detectorsPvStructure.getSubField(PVStringArray.class, DETECTORS_TABLE_COLUMN_NAME).put(0, nameArray.length, nameArray, 0);
		detectorsPvStructure.getSubField(PVStringArray.class, DETECTORS_TABLE_COLUMN_MRI).put(0, mriArray.length, mriArray, 0);
		detectorsPvStructure.getSubField(PVDoubleArray.class, DETECTORS_TABLE_COLUMN_EXPOSURE).put(0, exposureTimeArray.length, exposureTimeArray, 0);
		detectorsPvStructure.getSubField(PVIntArray.class, DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP).put(0, framesPerStepArray.length, framesPerStepArray, 0);

		return detectorsPvStructure;
	}

	private static void populateLayoutAttribute(PVStructure blockPVStructure) {
		PVStructure layoutPVStructure = blockPVStructure.getStructureField(MalcolmConstants.ATTRIBUTE_NAME_LAYOUT);
		String[] layoutNameArray = new String[] { "BRICK", "MIC", "ZEBRA" };
		String[] layoutMrifilenameArray = new String[] { "P45-BRICK01", "P45-MIC", "P45-ZEBRA01" };
		float[] layoutXArray = new float[] { 0.0f, 0.0f, 0.0f };
		float[] layoutYArray = new float[] { 0.0f, 0.0f, 0.0f };
		boolean[] layoutVisibleArray = new boolean[] { false, false, false };
		PVStructure layoutTableValuePVStructure = layoutPVStructure.getStructureField(FIELD_NAME_VALUE);
		layoutTableValuePVStructure.getSubField(PVStringArray.class, FIELD_NAME_NAME).put(0, layoutNameArray.length,
				layoutNameArray, 0);
		layoutTableValuePVStructure.getSubField(PVStringArray.class, FIELD_NAME_MRI).put(0, layoutMrifilenameArray.length,
				layoutMrifilenameArray, 0);
		layoutTableValuePVStructure.getSubField(PVFloatArray.class, FIELD_NAME_STAGE_X).put(0, layoutXArray.length, layoutXArray, 0);
		layoutTableValuePVStructure.getSubField(PVFloatArray.class, FIELD_NAME_STAGE_Y).put(0, layoutYArray.length, layoutYArray, 0);
		layoutTableValuePVStructure.getSubField(PVBooleanArray.class, FIELD_NAME_VISIBLE).put(0, layoutVisibleArray.length,
				layoutVisibleArray, 0);
		String[] layoutHeadingsArray = new String[] { FIELD_NAME_NAME, FIELD_NAME_MRI, FIELD_NAME_STAGE_X, FIELD_NAME_STAGE_Y, FIELD_NAME_VISIBLE };
		layoutPVStructure.getSubField(PVStringArray.class, FIELD_NAME_LABELS).put(0, layoutHeadingsArray.length,
				layoutHeadingsArray, 0);
	}

	private static FieldBuilder newDefaultFieldBuilder() {
		return FIELDCREATE.createFieldBuilder()
				.add(FIELD_NAME_DESCRIPTION, ScalarType.pvString)
				.addArray(FIELD_NAME_TAGS, ScalarType.pvString)
				.add(FIELD_NAME_WRITEABLE, ScalarType.pvBoolean)
				.add(FIELD_NAME_LABEL, ScalarType.pvString);
	}

	private static FieldBuilder newDefaultFieldBuilder(String id) {
		return newDefaultFieldBuilder().setId(id);
	}

	private static Structure newDefaultField(String id) {
		return newDefaultFieldBuilder().setId(id).createStructure();
	}

	private static Structure newScalarTypeField(String id, ScalarType type) {
		return newDefaultFieldBuilder(id).add(FIELD_NAME_DTYPE, type).createStructure();
	}

	private static Structure createValueStructure(String typeId, ScalarType type, Structure metaStructure) {
		return FIELDCREATE.createFieldBuilder()
				.add(FIELD_NAME_META, metaStructure)
				.add(FIELD_NAME_VALUE, type)
				.setId(typeId)
				.createStructure();
	}

	private static Structure createMethodStructure(Structure takesStructure, Structure returnsStructure) {
		return createMethodStructure(takesStructure, returnsStructure, null);
	}

	private static Structure createMethodStructure(Structure takesStructure, Structure returnsStructure,
			Structure defaultsStructure) {
		FieldBuilder methodMetaBuilder = newDefaultFieldBuilder()
				.add(FIELD_NAME_TAKES, takesStructure);
		if (defaultsStructure != null) {
			methodMetaBuilder.add(FIELD_NAME_DEFAULTS, defaultsStructure);
		}
		Structure methodMetaStructure = methodMetaBuilder.add(FIELD_NAME_RETURNS, returnsStructure)
				.setId(TYPE_ID_METHOD_META)
				.createStructure();

		// Note: we don't fully populate the method log as it differs between methods and GDA makes no use of it
		Structure methodLogStructure = FIELDCREATE.createFieldBuilder()
				.addArray(FIELD_NAME_PRESENT, ScalarType.pvString)
				.setId(TYPE_ID_METHOD_LOG)
				.createStructure();

		return FIELDCREATE.createFieldBuilder()
				.add(FIELD_NAME_TOOK, methodLogStructure)
				.add(FIELD_NAME_RETURNED, methodLogStructure)
				.add(FIELD_NAME_META, methodMetaStructure)
				.setId(TYPE_ID_METHOD)
				.createStructure();
	}

	public DummyMalcolmRecord(String recordName, PVStructure blockPVStructure) {
		super(recordName, blockPVStructure);

		// process
		process();
	}

	@Override
	public Service getService(PVStructure pvRequest) {
		String methodName = pvRequest.getStringField("method").get();
		return new RPCServiceAsyncImpl(this, methodName);
	}
}

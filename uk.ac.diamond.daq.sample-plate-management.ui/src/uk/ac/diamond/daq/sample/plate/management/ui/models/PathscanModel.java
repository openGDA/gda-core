package uk.ac.diamond.daq.sample.plate.management.ui.models;

import java.util.Arrays;
import java.util.List;

public class PathscanModel {

	private static final String NEW_LINE = "\n";

	private static final String SPACE = " ";

	private String[] motors = new String[]{"dummy_a", "dummy_b"};

	private List<Double[]> motorCoords;

	private List<ParamCollection> paramCollections;

	public PathscanModel(List<Double[]> motorCoords, List<ParamCollection> paramCollections) {
		this.motorCoords = motorCoords;
		this.paramCollections = paramCollections;
	}

	public String getScanScript() {
		String script = "";
		script += getImports() + NEW_LINE + NEW_LINE;
		script += getScannableGroup() + NEW_LINE + NEW_LINE;
		for (ParamCollection paramCollection: paramCollections) {
			script += paramCollection.getPresetParams() + NEW_LINE;
			if (paramCollection.getAnalyser() != null) {
				script += "analyser = " + paramCollection.getAnalyser() + NEW_LINE;
			}
			script += "scan" + SPACE;
			script += "pos_motors (";
			for (int i = 0; i < motorCoords.size(); i++) {
				if (i != 0) {
					script += ", ";
				}
				Double[] coords = motorCoords.get(i);
				script += Arrays.toString(coords);
			}
			script += ") ";
			script += paramCollection.getSetParams() + SPACE;
			if (paramCollection.getAnalyser() != null) {
				script += "analyser ";
			}
			script += paramCollection.getCollectedParams();
			script += NEW_LINE + NEW_LINE;
		}
		return script;
	}

	private String getImports() {
		return "from gda.device.scannable.scannablegroup import ScannableGroup";
	}

	private String getScannableGroup() {
		return "pos_motors = ScannableGroup()\n"
				+ "for member in " + Arrays.toString(motors) + ":\n"
				+ "    pos_motors.addGroupMember(member)\n"
				+ "pos_motors.setName(\"pos_motors\")";
	}
}
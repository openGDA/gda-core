package uk.ac.diamond.daq.beamline.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.diamond.daq.beamline.configuration.api.WorkflowException;

public class RangeLookupService extends AbstractCSVLookupService {
	private String lowColumn;
	private String highColumn;

	public RangeLookupService(File csvFile, String lowColumn, String highColumn) {
		super(csvFile);

		this.lowColumn = lowColumn;
		this.highColumn = highColumn;
	}

	@Override
	public Map<String, Double> getScannablePositions(double value, Set<String> columns) throws WorkflowException {
		List<String> requestedColumns = new ArrayList<>();
		requestedColumns.add(lowColumn);
		requestedColumns.add(highColumn);
		requestedColumns.addAll(columns);

		double[] row = readCSVFile(value, requestedColumns);

		Map<String, Double> result = new HashMap<>();
		for (int i=2;i<requestedColumns.size();i++) {
			result.put(requestedColumns.get(i), row[i]);
		}

		return result;
	}

	@Override
	protected boolean rowMatches(double value, double[] record) {
		return record[0] <= value && value <= record[1];
	}
}

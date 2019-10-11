package uk.ac.diamond.daq.beamline.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;

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
	public Map<String, Object> getScannablePositions(Object value, Set<String> columns) throws WorkflowException {
		List<String> requestedColumns = new ArrayList<>();
		requestedColumns.add(lowColumn);
		requestedColumns.add(highColumn);
		requestedColumns.addAll(columns);

		Object[] row = convertRowValues(readCSVFile(value, requestedColumns));

		Map<String, Object> result = new HashMap<>();
		for (int i=2;i<requestedColumns.size();i++) {
			result.put(requestedColumns.get(i), row[i]);
		}

		return result;
	}

	/**
	 * We take a row of String values from the lookup table.
	 * If they are numeric we convert them to numbers
	 */
	private Object[] convertRowValues(String[] row) {
		return Arrays.stream(row)
				.filter(NumberUtils::isNumber).map(NumberUtils::createNumber)
				.toArray();
	}

	@Override
	protected boolean rowMatches(Object value, String[] record) {
		double lo = NumberUtils.createDouble(record[0]);
		double hi = NumberUtils.createDouble(record[1]);
		double reference = (double) value;
		return lo <= reference && reference <= hi;
	}
}

package uk.ac.diamond.daq.beamline.configuration.test;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.ac.diamond.daq.beamline.configuration.AbstractCSVLookupService;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowException;

public class AbstractCSVLookupServiceTest {

	private class DummyService extends AbstractCSVLookupService {
		private String customLookupColumn;

		protected DummyService(File csvFile, String customLookupColumn) {
			super(csvFile);

			this.customLookupColumn = customLookupColumn;
		}

		@Override
		public Map<String, Double> getScannablePositions(double value, Set<String> columns) throws WorkflowException {
			List<String> requestedColumns = new ArrayList<>();
			requestedColumns.add(customLookupColumn);
			requestedColumns.addAll(columns);

			double[] row = this.readCSVFile(value, requestedColumns);

			Map<String, Double> result = new HashMap<>();
			for (int i=1;i<requestedColumns.size();i++) {
				result.put(requestedColumns.get(i), row[i]);
			}
			return result;
		}

		@Override
		protected boolean rowMatches(double value, double[] row) {
			return value == row[0];
		}

	}

	private static final String CSV_FILE = "AbstractCVSMotoPositionLookupServiceTest.csv";
	private static final String LOOKUP_COLUMN = "lookup";
	private static final String VALUE_COLUMN = "value";

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void createSetupData () throws IOException {
		File csvFile = new File(CSV_FILE);

		if (csvFile.exists()) {
			csvFile.delete();
		}

		FileWriter fileWriter = new FileWriter(csvFile);
		fileWriter.write(LOOKUP_COLUMN + "," + VALUE_COLUMN + "," + ",\r\n");
		fileWriter.write("1,2\n");
		fileWriter.write("3,4\n");
		fileWriter.close();
	}

	@AfterClass
	public static void cleanupTestData () {
		File csvFile = new File(CSV_FILE);
		if (csvFile.exists()) {
			csvFile.delete();
		}
	}

	@Test
	public void getMotorPositions_validLookupAndResultColumns() throws Exception {
		Map<String, Double> position = new DummyService(new File(CSV_FILE), LOOKUP_COLUMN)
				.getScannablePositions(3, singleton(VALUE_COLUMN));
		assertThat(position, is(equalTo(singletonMap(VALUE_COLUMN, 4.0))));
	}

	@Test
	public void invalidLookupColumn() throws Exception {
		String rubbishColumn = "hi";
		expectWorkflowException("No column '" + rubbishColumn + "' found in file '" + CSV_FILE + "'");

		new DummyService(new File(CSV_FILE), rubbishColumn)
			.getScannablePositions(1, singleton(VALUE_COLUMN));
	}

	@Test
	public void columnNotFound() throws Exception {
		String rubbishColumn = "hi";
		expectWorkflowException("No column '" + rubbishColumn + "' found in file '" + CSV_FILE + "'");
		new DummyService(new File(CSV_FILE), LOOKUP_COLUMN)
			.getScannablePositions(1, singleton(rubbishColumn));
	}

	@Test
	public void noRowMatched() throws Exception {
		double argument = 5.0;
		expectWorkflowException("No rows matched for value " + argument);

		new DummyService(new File(CSV_FILE), LOOKUP_COLUMN)
			.getScannablePositions(argument, singleton(VALUE_COLUMN));
	}

	private void expectWorkflowException(String message) {
		exception.expect(WorkflowException.class);
		exception.expectMessage(message);
	}
}

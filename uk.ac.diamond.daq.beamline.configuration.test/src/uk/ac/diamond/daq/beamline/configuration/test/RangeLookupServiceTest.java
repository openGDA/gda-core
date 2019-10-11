package uk.ac.diamond.daq.beamline.configuration.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.daq.beamline.configuration.RangeLookupService;
import uk.ac.diamond.daq.beamline.configuration.api.ScannablePositionLookupService;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowException;

public class RangeLookupServiceTest {

	private static final String CSV_FILE = "./RangeMotorPositionLookupServiceTest.csv";
	private static final String LOW_COLUMN = "lowValue";
	private static final String HIGH_COLUMN = "highValue";
	private static final String POSITION_ONE_COLUMN = "position1";
	private static final String POSITION_TWO_COLUMN = "position2";

	@BeforeClass
	public static void createTestData () throws IOException {
		File csvFile = new File(CSV_FILE);

		if (csvFile.exists()) {
			csvFile.delete();
		}

		FileWriter fileWriter = new FileWriter(csvFile);
		fileWriter.write(LOW_COLUMN + "," + HIGH_COLUMN + "," + POSITION_ONE_COLUMN + "," + POSITION_TWO_COLUMN + ",\r\n");
		fileWriter.write("1,2,1,2,\r\n"); //valid initial line
		fileWriter.write("1,2,3,4,\r\n"); //duplicate line with different positions
		fileWriter.write("2,3,5,6,\r\n"); //valid next range
		fileWriter.close();
	}

	@AfterClass
	public static void cleanupTestData () {
		File csvFile = new File(CSV_FILE);
		if (csvFile.exists()) {
			csvFile.delete();
		}
	}

	private void testRowIsFound(Map<String, Object> results, double expectedPosition1, double expectedPosition2) {

		if (results == null || results.isEmpty()) {
			fail("No matching row found");
		}

		Double position = ((Number) results.get(POSITION_ONE_COLUMN)).doubleValue();
		assertThat(position, is(closeTo(expectedPosition1, 0.01)));

		position = ((Number) results.get(POSITION_TWO_COLUMN)).doubleValue();
		assertThat(position, is(closeTo(expectedPosition2, 0.01)));
	}

	@Test
	public void getMotorPosition_correctValueFound () throws Exception {
		File csvFile = new File(CSV_FILE);

		ScannablePositionLookupService service = new RangeLookupService(csvFile, LOW_COLUMN, HIGH_COLUMN);
		Set<String> columns = new HashSet<>();
		columns.add(POSITION_ONE_COLUMN);
		columns.add(POSITION_TWO_COLUMN);

		testRowIsFound(service.getScannablePositions(1.0, columns), 1.0, 2.0);
		testRowIsFound(service.getScannablePositions(1.2, columns), 1.0, 2.0);
		testRowIsFound(service.getScannablePositions(2.0, columns), 1.0, 2.0);
	}

	@Test
	public void getMotorPosition_lowerFails () throws Exception {
		File csvFile = new File(CSV_FILE);

		ScannablePositionLookupService service = new RangeLookupService(csvFile, LOW_COLUMN, HIGH_COLUMN);
		Set<String> columns = new HashSet<>();
		columns.add(POSITION_ONE_COLUMN);
		columns.add(POSITION_TWO_COLUMN);

		try {
			service.getScannablePositions(0.9, columns);
			fail("Found row before start of test data");
		} catch (WorkflowException e) {
			assertThat(e.getMessage(), containsString("No rows matched"));
		}

		try {
			service.getScannablePositions(6.1, columns);
			fail("Found row after end of test data");
		} catch (WorkflowException e) {
			assertThat(e.getMessage(), containsString("No rows matched"));
		}
	}
}

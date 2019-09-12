package uk.ac.diamond.daq.beamline.configuration.test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import gda.device.Scannable;
import uk.ac.diamond.daq.beamline.configuration.LookupTableBasedItem;
import uk.ac.diamond.daq.beamline.configuration.api.ScannablePositionLookupService;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowException;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowItem;

public class LookupTableBasedItemTest {

	private static final String INPUT_PROPERTY = "input";

	private static final String X_NAME = "M1 X";
	private static final String Y_NAME = "M1 Y";

	@Mock
	private ScannablePositionLookupService motorPositionLookupService;

	@Mock
	private Scannable x;

	@Mock
	private Scannable y;

	private Map<String, Scannable> scannables;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setup () throws Exception {
		MockitoAnnotations.initMocks(this);

		scannables = new HashMap<>();
		scannables.put(X_NAME, x);
		scannables.put(Y_NAME, y);
	}

	@Test
	public void startWithOneMotor () throws Exception {
		LookupTableBasedItem workflowItem = new LookupTableBasedItem(INPUT_PROPERTY, scannables, motorPositionLookupService);

		Map<String, Double> answer = new HashMap<>();
		answer.put(X_NAME, 22.0);
		when(motorPositionLookupService.getScannablePositions(30, scannables.keySet())).thenReturn(answer);

		workflowItem.start(getProperties(30.0));
		verify(x).asynchronousMoveTo(22.0);
	}

	@Test
	public void startWithTwoMotors () throws Exception {
		LookupTableBasedItem workflowItem = new LookupTableBasedItem(INPUT_PROPERTY, scannables, motorPositionLookupService);

		Map<String, Double> answer = new HashMap<>();
		answer.put(X_NAME, 22.0);
		answer.put(Y_NAME, 25.0);
		when(motorPositionLookupService.getScannablePositions(30, scannables.keySet())).thenReturn(answer);

		workflowItem.start(getProperties(30.0));

		verify(x).asynchronousMoveTo(22.0);
		verify(y).asynchronousMoveTo(25.0);
	}

	@Test
	public void abortStopsScannables() throws Exception {
		new LookupTableBasedItem(INPUT_PROPERTY, scannables, motorPositionLookupService).abort();
		verify(x).stop();
		verify(y).stop();
	}

	@Test
	public void propertyNotFound() throws Exception {
		String propertyName = "Alice";
		WorkflowItem item = new LookupTableBasedItem(propertyName, scannables, motorPositionLookupService);

		exception.expect(WorkflowException.class);
		exception.expectMessage("Property '" + propertyName + "' not found");
		item.start(getProperties(0));
	}

	private Properties getProperties(double value) {
		Properties properties = new Properties();
		properties.setProperty(INPUT_PROPERTY, Double.toString(value));
		return properties;
	}

}

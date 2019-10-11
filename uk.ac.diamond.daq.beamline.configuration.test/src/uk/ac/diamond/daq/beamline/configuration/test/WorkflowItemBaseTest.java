package uk.ac.diamond.daq.beamline.configuration.test;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import gda.device.DeviceException;
import gda.device.Scannable;
import uk.ac.diamond.daq.beamline.configuration.WorkflowItemBase;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowException;

public class WorkflowItemBaseTest {

	@Mock
	private Scannable x;

	@Mock
	private Scannable y;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	private static final double X_POS = 2.3;
	private static final double Y_POS = 4.0;

	private static final String Y_NAME = "M1 Y";

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(y.getName()).thenReturn(Y_NAME);
	}

	@Test
	public void testMove() throws Exception {
		new DummyItem().start(null);
		verify(x).asynchronousMoveTo(X_POS);
		verify(y).asynchronousMoveTo(Y_POS);
		verify(x).waitWhileBusy();
		verify(y).waitWhileBusy();
	}

	@Test
	public void testAbort() throws Exception {
		new DummyItem().abort();
		verify(x).stop();
		verify(y).stop();
	}

	@Test
	public void problemWithDevice() throws Exception {
		doThrow(DeviceException.class).when(y).asynchronousMoveTo(Y_POS);

		exception.expect(WorkflowException.class);
		exception.expectMessage("Error moving scannable '" + Y_NAME + "'");

		new DummyItem().start(null);
	}

	@Test
	public void problemWhileMoving() throws Exception {
		doThrow(DeviceException.class).when(y).waitWhileBusy();

		exception.expect(WorkflowException.class);
		exception.expectMessage("Error while scannable '" + Y_NAME + "' is moving");

		new DummyItem().start(null);
	}

	@Test
	public void problemStopping() throws Exception {
		doThrow(DeviceException.class).when(y).stop();

		exception.expect(WorkflowException.class);
		exception.expectMessage("Error stopping scannable '" + Y_NAME + "'");

		new DummyItem().abort();
	}

	private class DummyItem extends WorkflowItemBase {

		private final Map<Scannable, Object> positions;

		public DummyItem() {
			positions = new HashMap<>();
			positions.put(x, X_POS);
			positions.put(y, Y_POS);
		}

		@Override
		public Map<Scannable, Object> getPositions(Properties properties) throws WorkflowException {
			return positions;
		}

		@Override
		public Set<Scannable> getScannables() {
			return positions.keySet();
		}
	}

}

package uk.ac.diamond.daq.beamline.configuration.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

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
	public MockitoRule rule = MockitoJUnit.rule();

	private static final double X_POS = 2.3;
	private static final double Y_POS = 4.0;

	private static final String Y_NAME = "M1 Y";

	@Before
	public void init() {
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
		var e = assertThrows(WorkflowException.class, () -> new DummyItem().start(null));
		assertThat(e.getMessage(), is("Error moving scannable '" + Y_NAME + "'"));
	}

	@Test
	public void problemWhileMoving() throws Exception {
		doThrow(DeviceException.class).when(y).waitWhileBusy();
		var e = assertThrows(WorkflowException.class, () -> new DummyItem().start(null));
		assertThat(e.getMessage(), is("Error while scannable '" + Y_NAME + "' is moving"));
	}

	@Test
	public void problemStopping() throws Exception {
		doThrow(DeviceException.class).when(y).stop();
		var e = assertThrows(WorkflowException.class, () -> new DummyItem().abort());
		assertThat(e.getMessage(), is("Error stopping scannable '" + Y_NAME + "'"));
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

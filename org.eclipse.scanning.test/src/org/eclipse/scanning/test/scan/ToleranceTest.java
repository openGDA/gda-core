package org.eclipse.scanning.test.scan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.example.scannable.MockScannable;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ToleranceTest {

	private static final double DOUBLE_FP_TOLERANCE = 1e-10;
	private static IRunnableDeviceService  dservice;
	private static IScannableDeviceService connector;

	@BeforeClass
	public static void before() {
		ServiceTestHelper.setupServices();
		connector = ServiceTestHelper.getScannableDeviceService();
		dservice  = ServiceTestHelper.getRunnableDeviceService();
	}

    @Before
	public void beforeTest() throws ScanningException {

		// Make a few detectors and models...
		MockScannable bnd = new MockScannable();
		bnd.setName("bnd");
		bnd.setActivated(false);
		bnd.setTolerance(1.0);
		bnd.setPosition(3.14);
		bnd.register();
	}

	@Test
	public void testMoveNoTolerance() throws Exception {

		// Something without
		IScannable<Double> a   = connector.getScannable("a");
		IPositioner     pos    = dservice.createPositioner("test");
        pos.setPosition(new MapPosition("a:0:20"));

        assertThat(20d, is(closeTo(a.getPosition(), DOUBLE_FP_TOLERANCE)));
	}

	@Test
	public void testMoveWithTolerance() throws Exception {

		// Something without
		IScannable<Double> a   = connector.getScannable("a");
		IPositioner     pos    = dservice.createPositioner("test");
        pos.setPosition(new MapPosition("a:0:20"));

		a.setTolerance(1d);
        pos.setPosition(new MapPosition("a:0:20.5"));

        assertThat(20d, is(closeTo(a.getPosition(), DOUBLE_FP_TOLERANCE)));
	}

	@Test
	public void testMoveEdgeOfTolerance() throws Exception {

		// Something without
		IScannable<Double> a   = connector.getScannable("a");
		IPositioner     pos    = dservice.createPositioner("test");
        pos.setPosition(new MapPosition("a:0:20"));

		a.setTolerance(1d);
        pos.setPosition(new MapPosition("a:0:21.0"));

        assertThat(21d, is(closeTo(a.getPosition(), DOUBLE_FP_TOLERANCE)));
	}

	@Test
	public void testOutsideTolerance() throws Exception {

		// Something without
		IScannable<Double> a   = connector.getScannable("a");
		IPositioner     pos    = dservice.createPositioner("test");
        pos.setPosition(new MapPosition("a:0:20"));

		a.setTolerance(1d);
        pos.setPosition(new MapPosition("a:0:22.4"));

        assertThat(22.4, is(closeTo(a.getPosition(), DOUBLE_FP_TOLERANCE)));
	}

	@Test
	public void testChangeTolerance() throws Exception {

		// Something without
		IScannable<Double> a   = connector.getScannable("a");
		IPositioner     pos    = dservice.createPositioner("test");
        pos.setPosition(new MapPosition("a:0:20"));

		a.setTolerance(1d);
        pos.setPosition(new MapPosition("a:0:20.5"));
        assertThat(20d, is(closeTo(a.getPosition(), DOUBLE_FP_TOLERANCE)));
		a.setTolerance(0.5d);
        pos.setPosition(new MapPosition("a:0:20.5"));
        assertThat(20.5, is(closeTo(a.getPosition(), DOUBLE_FP_TOLERANCE)));
	}

	@Test
	public void testSetToleranceNull() throws Exception {

		// Something without
		IScannable<Double> a   = connector.getScannable("a");
		IPositioner     pos    = dservice.createPositioner("test");
        pos.setPosition(new MapPosition("a:0:20"));

		a.setTolerance(1d);
        pos.setPosition(new MapPosition("a:0:20.5"));
        assertThat(20d, is(closeTo(a.getPosition(), DOUBLE_FP_TOLERANCE)));
		a.setTolerance(null);
        pos.setPosition(new MapPosition("a:0:20.5"));
        assertThat(20.5, is(closeTo(a.getPosition(), DOUBLE_FP_TOLERANCE)));
	}

	@Test
	public void testSpringConfigurationValue() throws Exception {

		// Something without
		IScannable<Double> bnd   = connector.getScannable("bnd");
		assertThat(3.14, is(closeTo(bnd.getPosition(), DOUBLE_FP_TOLERANCE)));
	}

	@Test
	public void testSpringConfigurationTolerance() throws Exception {

		// Something without
		IScannable<Double> bnd   = connector.getScannable("bnd");
		assertThat(1d, is(closeTo(bnd.getTolerance(), DOUBLE_FP_TOLERANCE)));
	}

	@Test
	public void testSpringMoveWithTolerance() throws Exception {

		// Something without
		IScannable<Double> bnd   = connector.getScannable("bnd");
		assertThat(1d, is(closeTo(bnd.getTolerance(), DOUBLE_FP_TOLERANCE)));

		IPositioner     pos    = dservice.createPositioner("test");
        pos.setPosition(new MapPosition("bnd:0:3.14"));
        assertThat(3.14, is(closeTo(bnd.getPosition(), DOUBLE_FP_TOLERANCE)));

        pos.setPosition(new MapPosition("bnd:0:3.5"));

        assertThat(3.14, is(closeTo(bnd.getPosition(), DOUBLE_FP_TOLERANCE)));

        pos.setPosition(new MapPosition("bnd:0:4.15"));
        assertThat(4.15, is(closeTo(bnd.getPosition(), DOUBLE_FP_TOLERANCE)));

        pos.setPosition(new MapPosition("bnd:0:3.14"));
        assertThat(3.14, is(closeTo(bnd.getPosition(), DOUBLE_FP_TOLERANCE)));

	}

	@Test
	public void testSpringMoveWithToleranceSetToZero() throws Exception {

		// Something without
		IScannable<Double> bnd   = connector.getScannable("bnd");
		assertThat(1.0, is(closeTo(bnd.getTolerance(), DOUBLE_FP_TOLERANCE)));

        bnd.setTolerance(0d);
		IPositioner     pos    = dservice.createPositioner("test");
        pos.setPosition(new MapPosition("bnd:0:3.14"));
        assertThat(3.14, is(closeTo(bnd.getPosition(), DOUBLE_FP_TOLERANCE)));

        pos.setPosition(new MapPosition("bnd:0:3.5"));

        assertThat(3.5, is(closeTo(bnd.getPosition(), DOUBLE_FP_TOLERANCE)));

        pos.setPosition(new MapPosition("bnd:0:3.14"));
        bnd.setTolerance(1.0d);

	}

}

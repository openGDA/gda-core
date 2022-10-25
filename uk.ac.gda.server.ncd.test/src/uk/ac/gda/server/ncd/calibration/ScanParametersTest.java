package uk.ac.gda.server.ncd.calibration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

class ScanParametersTest {
	@Test
	void stepCount() throws Exception {
		assertThat(new CentroidScanParameters(0, 10, 1).apply(0).size(), is(11));
		assertThat(new CentroidScanParameters(0, 10, 3).apply(0).size(), is(4));
		assertThat(new CentroidScanParameters(0, 10, 3.33333334).apply(0).size(), is(4));
	}

	@Test
	void stepCountDoesntDependOnCentre() throws Exception {
		var sp = new CentroidScanParameters(0.6, 1.2, 0.05);
		assertThat(sp.apply(1.4).size(), is(37));
		assertThat(sp.apply(2.5).size(), is(37));
		assertThat(sp.apply(4.5).size(), is(37));
	}

	@Test
	void startPointCorrent() {
		var sp = new CentroidScanParameters(0.6, 1.2, 0.05);
		var spp = sp.apply(1.4);
		assertThat((Double)spp.get(0), is(closeTo(0.8, 1e-6)));
	}

	@Test
	void endPointCorrent() {
		var sp = new CentroidScanParameters(0.6, 1.2, 0.05);
		var spp = sp.apply(1.4);
		assertThat((Double)spp.get(spp.size()-1), is(closeTo(2.6, 1e-6)));
	}
}

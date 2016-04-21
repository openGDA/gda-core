package uk.ac.diamond.daq.mapping.path;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.junit.Before;
import org.junit.Test;

public class SpiralTest {

	private SpiralGenerator generator;

	@Before
	public void before() throws Exception {
		BoundingBox box = new BoundingBox();
		box.setFastAxisStart(-10);
		box.setSlowAxisStart(5);
		box.setFastAxisLength(3);
		box.setSlowAxisLength(4);

		SpiralModel model = new SpiralModel();
		model.setBoundingBox(box);
		// use default parameters

		generator = new SpiralGenerator();
		generator.setModel(model);
	}

	@Test
	public void testSpiralNoROI() throws Exception {

		// Get the point list
		List<Point> pointList = generator.createPoints();

		assertEquals(20, pointList.size());

		// Test a few points
		assertEquals(new Point(-1, -8.5, -1, 7.0), pointList.get(0));
		assertEquals(new Point(-1, -8.63948222773063, -1, 7.9671992383675), pointList.get(3));
		assertEquals(new Point(-1, -6.494089475201543, -1, 7.866585979150157), pointList.get(15));
	}
}

package uk.ac.diamond.daq.mapping.ui.experiment;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.points.models.OneDStepModel;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.scanning.api.points.models.SinglePointModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.path.LissajousModel;
import uk.ac.diamond.daq.mapping.path.SpiralModel;
import uk.ac.diamond.daq.mapping.region.CircularMappingRegion;
import uk.ac.diamond.daq.mapping.region.LineMappingRegion;
import uk.ac.diamond.daq.mapping.region.PointMappingRegion;
import uk.ac.diamond.daq.mapping.region.PolygonMappingRegion;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;

public class MappingRegionManagerTest {

	private IScanPathModel[] twoDPaths = new IScanPathModel[] { new GridModel(), new RasterModel(), new SpiralModel(),
			new LissajousModel() };
	private IScanPathModel[] oneDPaths = new IScanPathModel[] { new OneDEqualSpacingModel(), new OneDStepModel() };
	private IScanPathModel[] zeroDPaths = new IScanPathModel[] { new SinglePointModel() };

	private MappingRegionManager mappingRegionManager;

	@Before
	public void setUp() {
		mappingRegionManager = new MappingRegionManager();
		twoDPaths = mappingRegionManager.twoDPaths.toArray(new IScanPathModel[0]);
		oneDPaths = mappingRegionManager.oneDPaths.toArray(new IScanPathModel[0]);
		zeroDPaths = mappingRegionManager.zeroDPaths.toArray(new IScanPathModel[0]);
	}

	@After
	public void tearDown() {
		mappingRegionManager = null;
	}

	@Test
	public void getRegionsShouldReturnAllKnownRegionTypes() throws Exception {
		IMappingScanRegionShape[] expectedRegions = new IMappingScanRegionShape[] { new RectangularMappingRegion(), new CircularMappingRegion(),
				new LineMappingRegion(), new PointMappingRegion(), new PolygonMappingRegion() };
		List<IMappingScanRegionShape> regions = mappingRegionManager.getRegions();
		assertThat("All region types should be returned", regions, hasItems(expectedRegions));
		assertEquals("No extra region types should be returned", regions.size(), expectedRegions.length);
	}

	@Test
	public void getValidPathsForRectangleShouldReturnAll2DPaths() throws Exception {
		List<IScanPathModel> paths = mappingRegionManager.getValidPaths(new RectangularMappingRegion());
		assertThat("All 2D paths should be returned", paths, hasItems(twoDPaths));
		assertEquals("No other paths should be returned", paths.size(), (twoDPaths.length));
	}

	@Test
	public void getValidPathsForCircleShouldReturnAll2DPaths() throws Exception {
		List<IScanPathModel> paths = mappingRegionManager.getValidPaths(new CircularMappingRegion());
		assertThat("All 2D paths should be returned", paths, hasItems(twoDPaths));
		assertEquals("No other paths should be returned", paths.size(), twoDPaths.length);
	}

	@Test
	public void getValidPathsForLineShouldReturnAll1DPaths() throws Exception {
		List<IScanPathModel> paths = mappingRegionManager.getValidPaths(new LineMappingRegion());
		assertThat("All 1D paths should be returned", paths, hasItems(oneDPaths));
		assertEquals("No other paths should be returned", paths.size(), oneDPaths.length);
	}

	@Test
	public void getValidPathsForPointShouldReturnSinglePointPath() throws Exception {
		List<IScanPathModel> paths = mappingRegionManager.getValidPaths(new PointMappingRegion());
		assertThat("Only a single point path should be valid for a point region", paths, hasItems(zeroDPaths));
		assertEquals("No other paths should be returned", paths.size(), zeroDPaths.length);
	}
}

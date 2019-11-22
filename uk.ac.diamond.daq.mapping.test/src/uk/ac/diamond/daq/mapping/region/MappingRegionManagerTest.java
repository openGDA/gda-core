package uk.ac.diamond.daq.mapping.region;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.api.points.models.TwoAxisPointSingleModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

public class MappingRegionManagerTest {

	private RectangularMappingRegion rectangle = new RectangularMappingRegion();
	private IMappingScanRegionShape[] regions = new IMappingScanRegionShape[] {
			rectangle, new CentredRectangleMappingRegion(),
			new CircularMappingRegion(), new LineMappingRegion(), new PointMappingRegion(),
			new PolygonMappingRegion() };
	private IScanPathModel[] twoDPaths = new IScanPathModel[] { new TwoAxisGridPointsModel(), new TwoAxisGridStepModel(),
			new TwoAxisSpiralModel(), new TwoAxisLissajousModel() };
	private IScanPathModel[] oneDPaths = new IScanPathModel[] { new TwoAxisLinePointsModel(), new TwoAxisLineStepModel() };
	private IScanPathModel[] zeroDPaths = new IScanPathModel[] { new TwoAxisPointSingleModel() };

	private MappingRegionManager mappingRegionManager;

	@Before
	public void setUp() {
		mappingRegionManager = new MappingRegionManager();
		mappingRegionManager.setRegions(Arrays.asList(regions));
		mappingRegionManager.setTwoDPaths(Arrays.asList(twoDPaths));
		mappingRegionManager.setOneDPaths(Arrays.asList(oneDPaths));
		mappingRegionManager.setZeroDPaths(Arrays.asList(zeroDPaths));
	}

	@After
	public void tearDown() {
		mappingRegionManager = null;
	}

	@Test
	public void getRegionsShouldReturnAllKnownRegionTypes() throws Exception {
		List<IMappingScanRegionShape> actualRegions = mappingRegionManager.getTemplateRegions();
		assertThat("All region types should be returned", actualRegions, hasItems(regions));
		assertEquals("No extra region types should be returned", actualRegions.size(), regions.length);
	}

	@Test
	public void getRegionsShouldReturnCopies() {
		// getRegions should give us a copy of the regions
		List<IMappingScanRegionShape> copyOfRegions = mappingRegionManager.getTemplateRegions();

		// sanity check
		assertThat(copyOfRegions, hasItem(rectangle));

		// tweak rectangle's parameters
		rectangle.setxStop(rectangle.getxStop()+1);

		// tweak reflected in the manager's regions...
		assertThat(mappingRegionManager.getTemplateRegions(), hasItem(rectangle));

		// ... but not in our copy
		assertThat(copyOfRegions, not(hasItem(rectangle)));
	}

	@Test
	public void getValidPathsForRectangleShouldReturnAll2DPaths() throws Exception {
		List<IScanPathModel> paths = mappingRegionManager.getValidPaths(new RectangularMappingRegion());
		assertThat("All 2D paths should be returned", paths, hasItems(twoDPaths));
		assertEquals("No other paths should be returned", paths.size(), (twoDPaths.length));
	}

	@Test
	public void getValidPathsForCentredRectangleShouldReturnAll2DPaths() throws Exception {
		List<IScanPathModel> paths = mappingRegionManager.getValidPaths(new CentredRectangleMappingRegion());
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

	@Test
	public void testGetTemplate() {
		// Create template(s) (through Spring perhaps)
		final CircularMappingRegion templateCircularRegion = new CircularMappingRegion();
		templateCircularRegion.setxCentre(-4.6);
		templateCircularRegion.setyCentre(12.12);
		templateCircularRegion.setRadius(0.004);
		mappingRegionManager.setRegions(Arrays.asList(templateCircularRegion));

		// At some other point we are working with a region of the same type
		CircularMappingRegion region = new CircularMappingRegion();
		region.setRadius(5);
		region.setxCentre(5);
		region.setyCentre(5);

		// But we want to load default parameters
		region = mappingRegionManager.getTemplateRegion(CircularMappingRegion.class);

		assertThat(region.getxCentre(), is(-4.6));
		assertThat(region.getyCentre(), is(12.12));
		assertThat(region.getRadius(), is(0.004));
	}

	@Test
	public void getTemplateReturnsCopy() {
		final RectangularMappingRegion region1 = mappingRegionManager.getTemplateRegion(RectangularMappingRegion.class);
		final RectangularMappingRegion region2 = mappingRegionManager.getTemplateRegion(RectangularMappingRegion.class);

		assertThat(region1, is(equalTo(region2)));
		assertThat(region1, is(not(sameInstance(region2))));
	}

	@Test (expected = IllegalArgumentException.class)
	public void getTemplateThrowsIfRegionNotFound() {
		mappingRegionManager.getTemplateRegion(mock(IMappingScanRegionShape.class).getClass());
	}
}

package org.eclipse.scanning.test.scan;

import static org.eclipse.scanning.api.scan.LevelRole.RUN;
import static org.eclipse.scanning.api.scan.LevelRole.WRITE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scanning.api.AbstractScannable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.scan.FileDeclared;
import org.eclipse.scanning.api.annotation.scan.LevelEnd;
import org.eclipse.scanning.api.annotation.scan.LevelStart;
import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.annotation.scan.PointStart;
import org.eclipse.scanning.api.annotation.scan.PostConfigure;
import org.eclipse.scanning.api.annotation.scan.PreConfigure;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.annotation.scan.WriteComplete;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.SimpleDetectorModel;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.LevelInformation;
import org.eclipse.scanning.api.scan.LevelRole;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.sequencer.SubscanModerator;
import org.eclipse.scanning.test.scan.nexus.NexusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnnotationScanTest extends NexusTest {

	private static interface InjectionDevice {

		public <A extends Annotation> void annotatedMethodCalled(Class<A> annotationClass, Object... objects);

		public <A extends Annotation> Set<Object> getMethodContext(Class<A> annotationClass);

		@ScanStart
		public default void scanStart(IPosition position, ScanInformation scanInfo, SubscanModerator moderator, ScanBean scanBean, ScanModel scanModel) {
			annotatedMethodCalled(ScanStart.class, position, scanInfo, moderator, scanBean, scanModel);
		}

		@PointStart
		public default void pointStart(IPosition position, ScanInformation scanInfo, SubscanModerator moderator, ScanBean scanBean, ScanModel scanModel) {
			annotatedMethodCalled(PointStart.class, position, scanInfo, moderator, scanBean, scanModel);
		}

		@PointEnd
		public default void pointEnd(IPosition position, ScanInformation scanInfo, SubscanModerator moderator, ScanBean scanBean, ScanModel scanModel) {
			annotatedMethodCalled(PointEnd.class, position, scanInfo, moderator, scanBean, scanModel);
		}

		@ScanFinally
		public default void scanFinally(IPosition position, ScanInformation scanInfo, SubscanModerator moderator, ScanBean scanBean, ScanModel scanModel) {
			annotatedMethodCalled(ScanFinally.class, position, scanInfo, moderator, scanBean, scanModel);
		}

		@ScanEnd
		public default void scanEnd(IPosition position, ScanInformation scanInfo, SubscanModerator moderator, ScanBean scanBean, ScanModel scanModel) {
			annotatedMethodCalled(ScanEnd.class, position, scanInfo, moderator, scanBean, scanModel);
		}

		@PreConfigure
		public default void preConfigure() {
			// not called as we use AcquistionDevice directly instead of ScanProcess
			annotatedMethodCalled(PreConfigure.class);
		}

		@PostConfigure
		public default void postConfigure() {
			// not called as we use AcquistionDevice directly instead of ScanProcess
			annotatedMethodCalled(PostConfigure.class);
		}

		@LevelStart
		public default void levelStart(IPosition position, LevelInformation levelInfo, ScanInformation scanInfo, SubscanModerator moderator, ScanBean scanBean, ScanModel scanModel) {
			annotatedMethodCalled(LevelStart.class, position, levelInfo, scanInfo, moderator, scanBean, scanModel);
		}

		@LevelEnd
		public default void levelEnd(IPosition position, LevelInformation levelInfo, ScanInformation scanInfo, SubscanModerator moderator, ScanBean scanBean, ScanModel scanModel) {
			annotatedMethodCalled(LevelEnd.class, position, levelInfo, scanInfo, moderator, scanBean, scanModel);
		}

		@FileDeclared
		public default void fileDeclared(String filePath, IPosition position, ScanInformation scanInfo, SubscanModerator moderator, ScanBean scanBean, ScanModel scanModel) {
			annotatedMethodCalled(FileDeclared.class, filePath, position, scanInfo, moderator, scanBean, scanModel);
		}

		@WriteComplete
		public default void writeComplete(IPosition position, ScanInformation scanInfo, SubscanModerator moderator, ScanBean scanBean, ScanModel scanModel) {
			annotatedMethodCalled(WriteComplete.class, position, scanInfo, moderator, scanBean, scanModel);
		}

	}

	private class InjectionDetector extends AbstractRunnableDevice<IDetectorModel> implements InjectionDevice {

		private final Map<Class<?>, Set<Object>> injectionContexts = new HashMap<>();
		private List<LevelRole> levelRoles;

		protected InjectionDetector() {
			super(null);
			setName("injectionDetector");
			setModel(new SimpleDetectorModel(getName(), 1.0));
		}

		@Override
		public void run(IPosition position) throws ScanningException, InterruptedException {
			// Do nothing
		}

		@Override
		public <A extends Annotation> void annotatedMethodCalled(Class<A> annotationClass, Object... objects) {
			if (!injectionContexts.containsKey(annotationClass)) {
				injectionContexts.put(annotationClass, new HashSet<>(Arrays.asList(objects)));
			}
		}

		@Override
		public <A extends Annotation> Set<Object> getMethodContext(Class<A> annotationClass) {
			return injectionContexts.get(annotationClass);
		}

		@Override
		@ScanStart
		public void scanStart(IPosition position, ScanInformation scanInfo, SubscanModerator moderator, ScanBean scanBean, ScanModel scanModel) {
			levelRoles = new ArrayList<>();
			annotatedMethodCalled(ScanStart.class, position, scanInfo, moderator, scanBean, scanModel);
		}

		public List<LevelRole> getLevelRoles() {
			return levelRoles;
		}

		@Override
		@LevelStart
		public void levelStart(IPosition position, LevelInformation levelInfo, ScanInformation scanInfo, SubscanModerator moderator, ScanBean scanBean, ScanModel scanModel) {
			levelRoles.add(levelInfo.getLevelRole());
			annotatedMethodCalled(LevelStart.class, position, levelInfo, scanInfo, moderator, scanBean, scanModel);
		}

	}

	private class InjectionMonitor extends AbstractScannable<Object> implements InjectionDevice {

		private final Map<Class<?>, Set<Object>> injectionContexts = new HashMap<>();

		public InjectionMonitor() {
			setName("injectionMonitor");
		}

		@Override
		public Object getPosition() throws ScanningException {
			return null;
		}

		@Override
		public Object setPosition(Object value, IPosition position) throws ScanningException {
			return null; // No position set
		}

		@Override
		public <A extends Annotation> void annotatedMethodCalled(Class<A> annotationClass, Object... objects) {
			if (!injectionContexts.containsKey(annotationClass)) {
				injectionContexts.put(annotationClass, new HashSet<>(Arrays.asList(objects)));
			}
		}

		@Override
		public <A extends Annotation> Set<Object> getMethodContext(Class<A> annotationClass) {
			return injectionContexts.get(annotationClass);
		}

		@Override
		public void abort() throws ScanningException, InterruptedException {
			// Mock Scannable, nothing to abort.
		}

	}

	private InjectionMonitor injectionMonitor;
	private InjectionDetector injectionDetector;

	@BeforeEach
	void before() {
		injectionMonitor = new InjectionMonitor();
		injectionDetector = new InjectionDetector();
	}

	@Test
	void testInjectedContext() throws Exception {
		final IRunnableDevice<ScanModel> scanner = createGridScan(injectionDetector, injectionMonitor, null, 2, 2);
		scanner.run(null);

		// check that each annotated method for each device has been invoked with objects
		// of the expected classes. TODO: even better would be to check the objects themselves are correct
		// not just that there is an object of the expected class
		checkInjectedContext(ScanStart.class, true, IPosition.class);
		checkInjectedContext(FileDeclared.class, true, String.class, IPosition.class);
		checkInjectedContext(PointStart.class, true, IPosition.class);
		checkInjectedContext(PointEnd.class, true, IPosition.class);
		checkInjectedContext(LevelStart.class, false, LevelInformation.class, IPosition.class);
		checkInjectedContext(LevelEnd.class, false, LevelInformation.class, IPosition.class);
		checkInjectedContext(WriteComplete.class, true, IPosition.class);
		checkInjectedContext(ScanEnd.class, true, IPosition.class);
		checkInjectedContext(ScanFinally.class, true, IPosition.class);

		assertEquals(Arrays.asList(RUN, WRITE, RUN, WRITE, RUN, WRITE, RUN, WRITE),
				     injectionDetector.getLevelRoles());
	}

	private <A extends Annotation> void checkInjectedContext(Class<A> annotationClass, boolean includeCommonContext, Class<?>... expectedContextClasses) {
		checkInjectedContext(injectionDetector, annotationClass, includeCommonContext, expectedContextClasses);
		checkInjectedContext(injectionMonitor, annotationClass, includeCommonContext, expectedContextClasses);
	}

	private static final Class<?>[] COMMON_CONTEXT_CLASSES = new Class<?>[] {
		ScanModel.class, SubscanModerator.class, ScanBean.class, ScanInformation.class
	};

	private <A extends Annotation> void checkInjectedContext(InjectionDevice device, Class<A> annotationClass, boolean includeCommonContext, Class<?>... expectedContextClasses) {
		final Set<Object> injectedContext = device.getMethodContext(annotationClass);
		assertThat(injectedContext, is(notNullValue()));

		// stream the total set of expected classes, filter out those found in context - if the resulting stream isn't empty we're missing something
		final Predicate<Class<?>> contextHasInstance = klass -> injectedContext.stream().anyMatch(obj -> klass.isInstance(obj));
		Stream<Class<?>> expectedClassStream = Arrays.stream(expectedContextClasses);
		if (includeCommonContext) {
			expectedClassStream = Stream.concat(expectedClassStream, Arrays.stream(COMMON_CONTEXT_CLASSES));
		}
		final Optional<Class<?>> missingExpectedContext = expectedClassStream.filter(klass -> !contextHasInstance.test(klass)).findFirst();
		if (missingExpectedContext.isPresent()) {
			fail("Context missing expected instance of: " + missingExpectedContext.get());
		}
	}

	private IRunnableDevice<ScanModel> createGridScan(IRunnableDevice<IDetectorModel> detector,
			IScannable<?> monitorsPerPoint, IScannable<?> monitorsPerScan, int... size) throws Exception {
		final TwoAxisGridPointsModel gridModel = new TwoAxisGridPointsModel();
		gridModel.setxAxisName("xNex");
		gridModel.setxAxisPoints(size[size.length-1]);
		gridModel.setyAxisName("yNex");
		gridModel.setyAxisPoints(size[size.length-2]);
		gridModel.setBoundingBox(new BoundingBox(0,0,3,3));

		final CompoundModel compoundModel = createNestedStepScans(2, size);
		compoundModel.addModel(gridModel);

		final IPointGenerator<CompoundModel> pointGen = pointGenService.createCompoundGenerator(compoundModel);

		// Create the model for a scan.
		final ScanModel scanModel = new ScanModel();
		scanModel.setPointGenerator(pointGen);
		scanModel.setScanPathModel(compoundModel);
		if (detector != null) {
			scanModel.setDetector(detector);
		}
		if (monitorsPerPoint != null) scanModel.setMonitorsPerPoint(monitorsPerPoint);
		if (monitorsPerScan != null) scanModel.setMonitorsPerScan(monitorsPerScan);

		// Create a file to scan into.
		scanModel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to "+scanModel.getFilePath());

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = scanService.createScanDevice(scanModel);

		final IPointGenerator<?> fgen = pointGen;
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(IRunListener.createRunWillPerformListener(
				evt -> System.out.println("Running acquisition scan of size "+fgen.size())));

		return scanner;
	}

}

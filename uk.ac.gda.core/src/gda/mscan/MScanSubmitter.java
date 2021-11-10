/*-
 * Copyright © 2018 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.mscan;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.sequencer.ScanRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.Monitor;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.Findable;
import gda.jython.JythonServerFacade;
import gda.mscan.element.Action;
import gda.mscan.element.IMScanElementEnum;
import gda.mscan.element.Mutator;
import gda.mscan.element.RegionShape;
import gda.mscan.element.ScanDataConsumer;
import gda.mscan.element.Scanpath;
import gda.mscan.processor.IClauseElementProcessor;
import gda.mscan.processor.IRunnableDeviceDetectorElementProcessor;
import gda.mscan.processor.MutatorElementProcessor;
import gda.mscan.processor.NumberElementProcessor;
import gda.mscan.processor.ReRunFromFileElementProcessor;
import gda.mscan.processor.RegionShapeElementProcessor;
import gda.mscan.processor.ScanDataConsumerElementProcessor;
import gda.mscan.processor.ScannableDetectorElementProcessor;
import gda.mscan.processor.ScannableElementProcessor;
import gda.mscan.processor.ScannableGroupElementProcessor;
import gda.mscan.processor.ScannableMonitorElementProcessor;
import gda.mscan.processor.ScannableReadoutElementProcessor;
import gda.mscan.processor.ScanpathElementProcessor;
import gda.mscan.processor.TokenStringElementProcessor;

/**
 * Validates, parses, builds and submits MScan commands supplied in the form of an array of typed Objects. It does
 * so by breaking the scan command up into Clauses delimited by the detection of a new {@link Scannable} (i.e.scan
 * axis). These clauses are then parsed individually to build up the overall {@link CompoundModel} object that
 * represents the scan.
 *
 * @since GDA 9.10
 */
public class MScanSubmitter extends ValidationUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(MScanSubmitter.class);
	private static final String RERUN = "rerun";
	private static final String EMPTYSTRING = "";

	private interface ProcessorFunction extends Function<Object, IClauseElementProcessor> {}
	/**
	 * A constant map of clause element type to a lambda that will create a corresponding processor
	 */
	@SuppressWarnings("unchecked")
	private static final ImmutableMap<Class<?>, ProcessorFunction> processorBuilders =
		ImmutableMap.<Class<?>, ProcessorFunction>builder()
		.put(Scannable.class, arg -> new ScannableElementProcessor((Scannable)arg))
		.put (ScannableGroup.class, arg -> new ScannableGroupElementProcessor((ScannableGroup)arg))
		.put(Detector.class, arg -> new ScannableDetectorElementProcessor((Detector)arg))
		.put(Monitor.class, arg -> new ScannableMonitorElementProcessor((Monitor)arg))
		.put(Scanpath.class, arg -> new ScanpathElementProcessor((Scanpath)arg))
		.put(RegionShape.class, arg -> new RegionShapeElementProcessor((RegionShape)arg))
		.put(Mutator.class, arg -> new MutatorElementProcessor((Mutator)arg))
		.put(Integer.class, arg -> new NumberElementProcessor((Number)arg))
		.put(Double.class, arg -> new NumberElementProcessor((Number)arg))
		.put(IRunnableDevice.class,
				arg -> new IRunnableDeviceDetectorElementProcessor((IRunnableDevice<IDetectorModel>) arg))
		.put(ScanDataConsumer.class, arg -> new ScanDataConsumerElementProcessor((ScanDataConsumer)arg))
		.put(String.class, arg -> new TokenStringElementProcessor((String)arg))
		.build();

	private final List<IClauseElementProcessor> processors = new ArrayList<>();

	private final IEventService eventService;
	private final IRunnableDeviceService runnableDeviceService;
	private final ResolverFactory resolverFactory;
	private final JythonServerFacade facade = JythonServerFacade.getInstance();

	public MScanSubmitter(final IEventService eventService, final IRunnableDeviceService runnableDeviceService) {
		this(eventService, runnableDeviceService,  new ResolverFactory());
	}

	MScanSubmitter(final IEventService eventService,
			final IRunnableDeviceService runnableDeviceService, final ResolverFactory factory) {
		this.eventService = eventService;
		this.runnableDeviceService = runnableDeviceService;
		this.resolverFactory = factory;
	}

	public void buildAndSubmitBlockingScanRequest(final Object[] args) throws Exception {
		buildAndSubmitScanRequest(args, true);
	}

	/**
	 * Takes the supplied MScan array, validates it and then builds a {@link CompoundModel} that represents
	 * the command. This is then submitted to the scanning queue as a blocking call. Currently supports detectors
	 * based on the {@link Detector} interface with a matching {@link IRunnableDevice} representation. This has always
	 * been the case as for Jython to pick up the type of a detector it must be {@link Findable} and
	 * {@link IRunnableDevice} based object are not yet added to the Jython Namespace.
	 *
	 * @param args	The array of objects constituting the command supplied by jython. This should only contain
	 * 				{@link Scannable}s, {@link Number}s and {@link IMScanElementEnum}s or their subclasses, if any
	 * 				other objects are present, the validation will reject the command.
	 * @param block	If true indicates that the scan submission should be a blocking call.
	 *
	 * @throws	IllegalArgumentException if the validation step fails or if the clause resolution is unsuccessful
	 * 			ScanningException if the {@link IRunnableDevice} corresponding to a {@link Detector} cannot be found
	 * 			or its {@link IDetectorModel} is null
	 */
	public void buildAndSubmitScanRequest(final Object[] args, final boolean block) throws Exception {
		throwIf(args == null, "The scan request array is null");
		LOGGER.info("MScan command received {}", Arrays.toString(args));

		final ScanClausesResolver resolver = validateCommand(args);
		// First check for run from Nexus option
		IClauseElementProcessor initialProc = withNullProcessorCheck(processors.get(0));
		if (initialProc instanceof ReRunFromFileElementProcessor) {
			initialProc.process(null, processors, 0);
			try{
				final String filepath = initialProc.getElementValue();
				printToJython(ImmutableMap.of("Loading scan from ", Paths.get(filepath).getFileName()));
				final Optional<ScanRequest> scanRequest = ScanRequestBuilder.buildFromNexusFile(filepath);
				submitFromFile(scanRequest.orElseThrow(), block);
			} catch(Exception e) {
				printToJython(ImmutableMap.of("Exception: ", e.getMessage()));
			}
			return;
		}

		final CompoundModel scanModel = new CompoundModel();

		// Find the distinct clauses within the MScan command and return a list of the list of
		// typed processors for each clause.

		final List<List<IClauseElementProcessor>> processorsByClause = resolver.resolveScanClauses();
		throwIf(processorsByClause.isEmpty() || processorsByClause.contains(null),
			"clause resolution returned an empty or invalid list of processors by clause");

		final ClausesContext context = new ClausesContext(runnableDeviceService);

		// iterate over the processors for the current clause using them to populate the constituent lists of the
		// Compound Model. The type of the first element determines the required action; only Scannables or
		// ScannableGroups require complex parsing of the rest of the clause. The only other possibilities are
		// {@link Detectors} and {@link Monitors} or {@link IRunnableDevice}s
		for (final List<IClauseElementProcessor> clauseProcessors : processorsByClause) {
			throwIf(clauseProcessors.isEmpty() || clauseProcessors.contains(null),
					"clause resolution returned an empty or invalid processor list for a clause");
			context.wipe();
			initialProc = withNullProcessorCheck(clauseProcessors.get(0));

			// within each clauseProcessors list, the first element is always some type of Scannable for which the
			// appropriate type of processor has already been selected. If it is a pure Scannable in a clause of length
			// 1 after a scan definition path has been seen then that means it is a Scannable being used as a monitor
			// and so that processor must be switched.
			for (int index = 0; index < clauseProcessors.size(); index++) {
				if (context.isClauseProcessed()) {
					break;
				}
				// Handle single element Detector or Monitor clauses
				if (clauseProcessors.size() == 1) {
					throwIf(!context.isScanPathSeen() && !clauseProcessors.get(0).isStatic(), "No scan path defined - SPEC style scans not yet supported");
					if (initialProc instanceof ScannableElementProcessor) {
						clauseProcessors.set(0, new ScannableReadoutElementProcessor(
														(Scannable)initialProc.getElement()));
					}
				}
				clauseProcessors.get(index).process(context, clauseProcessors, index);
			}
			// if no detectors or monitors have been seen yet, then the current clause just defined a scanpath so
			// this needs to be added to the CompoundModel for the entire mscan
			if (!context.isDetectorClauseSeen()) {
				context.addPathDefinitionToCompoundModel(scanModel);
			}
		}

		// Populate the {@link ScanRequest} with the assembled objects
		ScanRequest scanRequest = new ScanRequest();
		scanRequest.setCompoundModel(scanModel);
		scanRequest.setDetectors(context.getDetectorMap());
		scanRequest.setMonitorNamesPerPoint(context.getMonitorsPerPoint());
		scanRequest.setTemplateFilePaths(context.getTemplates());
		scanRequest.setProcessingRequest(context.getProcessorRequest());

		submit(scanRequest, block);
	}

	/**
	 * Shim method to be called when re-running from a file after a ScanDefinition has been successfully loaded from it
	 * in order to submit with appropriate messaging.
	 *
	 * @param scanRequest	The ScanRequest to be submitted
	 * @param block			Whether the request should be submitted in blocking or non-blocking mode
	 * @throws Exception	Any Exception arising from the submission operation
	 */
	private void submitFromFile(final ScanRequest scanRequest, final boolean block) throws Exception {
		printToJython(ImmutableMap.of("Successful, ", EMPTYSTRING, "submitting scan", EMPTYSTRING));
		submit(scanRequest, block);
	}

	/**
	 * Handle submission of the supplied ScanRequest and monitoring and reporting of the progress of the associated scan
	 *
	 * @param scanRequest	The ScanRequest to be submitted
	 * @param block			Whether the request should be submitted in blocking or non-blocking mode
	 * @throws Exception	Any Exception arising from the submission operation
	 */
	private void submit(final ScanRequest scanRequest, final boolean block) throws Exception {
		// validate the scan bean properly here or you get a nullpointer exception
		final ScanBean bean = new ScanBean(scanRequest);
		eventService.getEventConnectorService().marshal(bean);
		final URI uri = new URI(LocalProperties.getActiveMQBrokerURI());

		try (final ISubmitter<ScanBean> submitter = eventService.createSubmitter(uri, EventConstants.SUBMISSION_QUEUE);
				final ISubscriber<IScanListener> subscriber = eventService.createSubscriber(uri, EventConstants.STATUS_TOPIC);
				var queue = eventService.createJobQueueProxy(uri, EventConstants.SUBMISSION_QUEUE);) {

			subscriber.addListener(new IScanListener() {
				@Override
				public void scanEventPerformed(ScanEvent evt) {
					String message = evt.getBean().getMessage();
					if (message != null) {
						printToJython(ImmutableMap.of(EMPTYSTRING, message));
					}
				}

				@Override
				public void scanStateChanged(ScanEvent evt) {
					ScanBean bean = evt.getBean();
					if ((bean.getStatus().equals(Status.PREPARING) || bean.getStatus().equals(Status.COMPLETE)) && bean.getFilePath() != null) {
						printToJython(ImmutableMap.of("Output file: ", bean.getFilePath()));
					} else if (bean.getStatus().equals(Status.FAILED)) {
						facade.print(bean.getMessage());
					}
				}
			});
			if (block) {
				try {
					submitter.blockingSubmit(bean);
				} catch (InterruptedException e) {
					queue.terminateJob(bean);
					facade.print("Scan terminated");
					Thread.currentThread().interrupt();
				}
			} else {
				submitter.submit(bean);
			}
		}
	}

	/**
	 * Utility for outputting text and variables to the Jython terminal. Takes a Guava ImmutableMap of Text keys
	 * to Object values (because iteration order is the same as insertion order) where the keys are the sequential
	 * String elements of the output and the values are the variables to embed between them in the composite
	 * output. So if you had the map m["key1":v1, "key2":v2], the resulting printed output would be the same as
	 * "key1" + v1 + "key2" + v2. If v2 is not required it should be replaced in the map with the empty string.
	 *
	 * @param textToParams	Map of Text strings and Variables to be printed
	 */
	private void printToJython(final ImmutableMap<String, Object> textToParams) {
		facade.runCommand("print \"\"\"" +
				textToParams.entrySet().stream()
					.map(e -> e.getKey() + e.getValue())
					.collect(Collectors.joining())
					+ "\"\"\"");
	}

	/**
	 * Checks that the MScan command has parameters and that the first one is valid- either a Scannable or
	 * the Static {@link RegionShape}. Providing this is the case, the validity of the type of each arg is
	 * checked by confirming that there is a corresponding {@link IClauseElementProcessBuilder} in the
	 * {@link processorBuilders} static map. Assuming this is the case for all args, corresponding processors
	 * are added to the {@link processors} list.
	 *
	 * @param args		The arguments that constitute the elements of the incoming MScan command
	 * @return			A {@link ScanClauseResolver} initialised with processors that correspond to the elements
	 * 					of the command if they are successfully validated.
	 *
	 * @throws	IllegalArgumentException if there is no processor type that matches the current arg in the command or if
	 * 			the {@code args} array is empty.
	 */
	private ScanClausesResolver validateCommand(final Object[] args) {
		if (args.length < 1 ||
				!(args[0].equals(Action.RERUN)
						|| args[0] instanceof Scannable
						|| args[0].equals(Scanpath.STATIC)
						|| onlyDetectorsAndParams(args))) {
			throw new IllegalArgumentException(
			"You must specify at least one argument in your mscan command and your first argument must be a Scannable, the Static Scanpath, a Detector or the 'rerun' keyword");
		}

		// Adjust for point scan syntax which doesn't require both region and scanpath. This loop must run
		// first as it potentially popoulates the list to be used in subsequent checks.
		List<Object> params = new ArrayList<>();
		// Prepend with Static region if only detectors provided
		if (onlyDetectorsAndParams(args)) params.add(0, Scanpath.STATIC);

		for (int i = 0; i < args.length; i++) {
			params.addAll(resolveNumericTuples(args[i]));
			if (args[i].equals(RegionShape.POINT) && args[i + 1] instanceof Number && args[i + 2] instanceof Number) {
				params.add(args[i + 1]);
				params.add(args[i + 2]);
				params.add(Scanpath.SINGLE_POINT);
				if ((args.length >= i + 6) && (args[i + 3].equals(RegionShape.POINT))) {
					i += 3;
				}
			}
		}

		// Adjust so that the more natural two axis linear related terms (step and points) can be used to
		// describe equivalent paths for grid and axial scans. This simplifies typing, improves consistency
		// and paves the way to remove the spurious 'grid' vs 'raster' nomenclature.
		Optional<RegionShape> currentShape = Optional.empty();
		for (int i = 0; i < params.size(); i++) {
			Object element = params.get(i);
			if (element instanceof RegionShape) {
				throwIf(currentShape.isPresent(), "A region shape is already being processed");
				if (element == RegionShape.LINE || element == RegionShape.POINT) {	// unless no change required
					continue;
				}
				currentShape = Optional.of((RegionShape)element);	// cache the shape for the current portion
			}
			// and replace uses of 'step' and 'points' with the dimensionally appropriate form
			if (currentShape.isPresent() && element instanceof Scanpath) {
				if ((Scanpath) element == Scanpath.LINE_STEP) {
					params.set(i, currentShape.get() == RegionShape.AXIAL ? Scanpath.AXIS_STEP : Scanpath.GRID_STEP);
				} else if ((Scanpath) element == Scanpath.LINE_POINTS) {
					params.set(i, currentShape.get() == RegionShape.AXIAL ? Scanpath.AXIS_POINTS : Scanpath.GRID_POINTS);
				}
				currentShape = Optional.empty();
			}
		}

		// Iterate over the command args looking for Scannable and Scannable groups. Can't just use the returned class
		// as many classes implement Scannable
		Class<?>[] hierarchy = {Detector.class, Monitor.class, ScannableGroup.class, Scannable.class};
		for (int i = 0; i < params.size() ; i++) {
			Object obj = params.get(i);
			Class<?> type = obj.getClass();

			if (obj instanceof Scannable) {
				for (Class<?> cl : hierarchy) {
					if (cl.isAssignableFrom(obj.getClass())) {
						type = cl;
						break;
					}
				}
			}
			else if (obj instanceof IRunnableDevice<?>) {
				type = IRunnableDevice.class;
			}

			else if (i == 0 && obj instanceof Action && ((Action)obj).equals(Action.RERUN)) {
				processors.add(new ReRunFromFileElementProcessor((String)params.get(1)));
				break;
			}
			// Check the processorBuilders map can construct a processor for the required type and if so
			// make it and add it to the collection of processors that correspond to the command.
			if (!processorBuilders.containsKey(type)) {
				throw new IllegalArgumentException(String.format(
						"Your command contains an invalid argument at position %d", i));
			}
			processors.add(processorBuilders.get(type).apply(params.get(i)));
		}
		return resolverFactory.getResolver(processors);
	}

	private boolean onlyDetectorsAndParams(Object[] args) {
		if (!(args[0] instanceof Detector)) return false;
		for (Object arg : args) {
			if (!(arg instanceof Number || arg instanceof Detector)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * If mscan numeric command parameters are entered using () or [] brackets e.g.
	 *
	 * mscan scannable, anotherScannable, rect ((2,2) [3,3]) line [10,20] detectorRunnableDevice
	 *
	 * this produces tuples and lists in the passed in args. This method just flattens such entries if they exist so
	 * that we end up with a single list containing all the grouped parameters in sequence
	 *
	 * @param element	The argument to be processed
	 * @return			A flat list of {@link Number}s in the order they were specified in the command
	 * @throws			{@link IllegalAtgumentException} if the element is a {@link List} with non-numeric entries
	 */
	private List<Object> resolveNumericTuples(Object element) {
		if (element instanceof List) {
			return ((List<?>)element).stream()
					.map(e -> e instanceof List ? (List<?>)e : Arrays.asList(e))
					.flatMap(Collection::stream)
					.map(this::throwIfNotNumber)
					.collect(Collectors.toList());
		} else {
			return Arrays.asList(element);
		}
	}

	private Number throwIfNotNumber(Object o) {
		throwIf(!(o instanceof Number), "Only numeric parameters can be enclosed in brackets");
		return (Number)o;
	}

	/**
	 * Very Basic factory to support mocking of {@link ScanClausesResolver}
	 */
	public static class ResolverFactory {
		public ScanClausesResolver getResolver(final List<IClauseElementProcessor> elementProcessors) {
			return new ScanClausesResolver(elementProcessors);
		}
	}
}


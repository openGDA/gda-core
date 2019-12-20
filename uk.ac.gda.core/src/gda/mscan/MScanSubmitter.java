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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.Monitor;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.Findable;
import gda.mscan.element.Scanpath;
import gda.mscan.element.IMScanElementEnum;
import gda.mscan.element.Mutator;
import gda.mscan.element.RegionShape;
import gda.mscan.element.ScanDataConsumer;
import gda.mscan.processor.ScanpathElementProcessor;
import gda.mscan.processor.IClauseElementProcessor;
import gda.mscan.processor.IRunnableDeviceDetectorElementProcessor;
import gda.mscan.processor.MutatorElementProcessor;
import gda.mscan.processor.NumberElementProcessor;
import gda.mscan.processor.RegionShapeElementProcessor;
import gda.mscan.processor.ScanDataConsumerElementProcessor;
import gda.mscan.processor.ScannableDetectorElementProcessor;
import gda.mscan.processor.ScannableElementProcessor;
import gda.mscan.processor.ScannableGroupElementProcessor;
import gda.mscan.processor.ScannableMonitorElementProcessor;
import gda.mscan.processor.ScannableReadoutElementProcessor;
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
		final CompoundModel scanModel = new CompoundModel();
		scanModel.setModels(new ArrayList<>());
		scanModel.setRegions(new ArrayList<>());
		scanModel.setMutators(new ArrayList<>());

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
			IClauseElementProcessor initialScannableProc = withNullProcessorCheck(clauseProcessors.get(0));

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
					throwIf(!context.isScanPathSeen(), "No scan path defined - SPEC style scans not yet supported");
					if (initialScannableProc instanceof ScannableElementProcessor) {
						clauseProcessors.set(0, new ScannableReadoutElementProcessor(
														(Scannable)initialScannableProc.getElement()));
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

		// validate the scan bean properly here or you get a nullpointer exception
		final ScanBean bean = new ScanBean(scanRequest);
		eventService.getEventConnectorService().marshal(bean);
		final URI uri = new URI(LocalProperties.getActiveMQBrokerURI());
		try (final ISubmitter<ScanBean> submitter = eventService.createSubmitter(uri, EventConstants.SUBMISSION_QUEUE)) {
			if (block) {
				submitter.blockingSubmit(bean);
			} else {
				submitter.submit(bean);
			}
		}
	}

	/**
	 * Checks that the MScan command has parameters and that the first one is a Scannable. Providing this is the
	 * case, the validity of the type of each arg is checked by confirming that there is a corresponding
	 * {@link IClauseElementProcessBuilder} in the {@link processorBuilders} static map. Assuming this is the
	 * case for all args, corresponding processors are added to the {@link processors} list.
	 *
	 * @param args		The arguments that constitute the elements of the incoming MScan command
	 * @return			A {@link ScanClauseResolver} initialised with processors that correspond to the elements
	 * 					of the command if they are successfully validated.
	 *
	 * @throws	IllegalArgumentException if there is no processor type that matches the current arg in the command or if
	 * 			the {@code args} array is empty.
	 */
	private ScanClausesResolver validateCommand(final Object[] args) {
		if (args.length < 1 || !(args[0] instanceof Scannable)) {
			throw new IllegalArgumentException(
			"You must specify at least one argument in your mscan command and your first argument must be a Scannable");
		}

		// Adjust for point scan syntax which doesn't require both region and scanpath
		List<Object> params = new ArrayList<>();
		for (int i = 0; i < args.length; i++) {
			params.add(args[i]);
			if (args[i].equals(RegionShape.POINT) && args[i + 1] instanceof Number && args[i + 2] instanceof Number) {
				params.add(args[i + 1]);
				params.add(args[i + 2]);
				params.add(Scanpath.SINGLE_POINT);
				if ((args.length >= i + 6) && (args[i + 3].equals(RegionShape.POINT))) {
					i += 3;
				}
			}
		}

		// Adjust so that the more friendly two axis related terms can be used to describe equivalent 1 axis paths
		List<Object> currentList = params;
		int len = currentList.size();
		while (currentList.contains(RegionShape.AXIAL)) {
			int index = currentList.indexOf(RegionShape.AXIAL);
			if (len > index + 2) {
				if (currentList.get(index + 3).equals(Scanpath.LINE_STEP)) {
					currentList.set(index + 3, Scanpath.AXIS_STEP);
				} else if (currentList.get(index + 3).equals(Scanpath.LINE_POINTS)) {
					currentList.set(index + 3, Scanpath.AXIS_POINTS);
				}
			}
			currentList = currentList.subList(index + 1, len -index);
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

	/**
	 * Very Basic factory to support mocking of {@link ScanClausesResolver}
	 */
	static class ResolverFactory {
		public ScanClausesResolver getResolver(final List<IClauseElementProcessor> elementProcessors) {
			return new ScanClausesResolver(elementProcessors);
		}
	}
}


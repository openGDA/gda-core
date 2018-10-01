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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.Findable;
import gda.mscan.element.AreaScanpath;
import gda.mscan.element.IMScanElementEnum;
import gda.mscan.element.Mutator;
import gda.mscan.element.Roi;
import gda.mscan.processor.AreaScanpathElementProcessor;
import gda.mscan.processor.IClauseElementProcessor;
import gda.mscan.processor.IRunnableDeviceDetectorElementProcessor;
import gda.mscan.processor.MutatorElementProcessor;
import gda.mscan.processor.NumberElementProcessor;
import gda.mscan.processor.RoiElementProcessor;
import gda.mscan.processor.ScannableElementProcessor;
import gda.mscan.processor.ScannableGroupElementProcessor;

/**
 * Validates, parses, builds and submits MScan commands supplied in the form of an array of typed Objects. It does
 * so by breaking the scan command up into Clauses delimited by the detection of a new {@link Scannable} (i.e.scan axis).
 * These clauses are then parsed individually to build up the overall {@link CompoundModel} object that represents the scan.
 *
 * @since GDA 9.10
 */
public class MScanSubmitter {

	private static final Logger logger = LoggerFactory.getLogger(MScanSubmitter.class);

	private interface ProcessorFunction extends Function<Object, IClauseElementProcessor> {}
	/**
	 * A constant map of clause element type to a lambda that will create a corresponding processor
	 */
	@SuppressWarnings("unchecked")
	private static final ImmutableMap<Class<?>, ProcessorFunction> processorBuilders =
		ImmutableMap.<Class<?>, ProcessorFunction>builder()
		.put(Scannable.class, arg -> new ScannableElementProcessor((Scannable)arg))
		.put (ScannableGroup.class, arg -> new ScannableGroupElementProcessor((ScannableGroup)arg))
		.put(AreaScanpath.class, arg -> new AreaScanpathElementProcessor((AreaScanpath)arg))
		.put(Roi.class, arg -> new RoiElementProcessor((Roi)arg))
		.put(Mutator.class, arg -> new MutatorElementProcessor((Mutator)arg))
		.put(Integer.class, arg -> new NumberElementProcessor((Number)arg))
		.put(Double.class, arg -> new NumberElementProcessor((Number)arg))
		.put(IRunnableDevice.class,
				arg -> new IRunnableDeviceDetectorElementProcessor((IRunnableDevice<IDetectorModel>) arg))
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
	 * been the case as for Jython to pick up the type of a detector it must be {@link Findable} and {@link IRunnableDevice}
	 * based object are not yet added to the Jython Namespace.
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
		logger.info("MScan command received {}", Arrays.toString(args));

		final ScanClausesResolver resolver = validateCommand(args);
		final CompoundModel<IROI> scanModel = new CompoundModel<IROI>();
		final Map<String, Object> detectorMap = new HashMap<>();
		final List<String> monitorsPerPoint = new ArrayList<>();
		scanModel.setModels(new ArrayList<>());
		scanModel.setRegions(new ArrayList<>());
		scanModel.setMutators(new ArrayList<>());

		// Find the distinct clauses within the MScan command and return a list of the list of
		// typed processors for each clause.

		final List<List<IClauseElementProcessor>> processorsByClause = resolver.resolveScanClauses();
		throwIf(processorsByClause.isEmpty() || processorsByClause.contains(null),
			"clause resolution returned an empty or invalid list of processors by clause");
		boolean scanPathSeen = false;

		// iterate over the processors for the current clause using them to populate the constituent lists of the
		// Compound Model. The type of the first element determines the required action; only Scannables or
		// ScannableGroups require complex parsing of the rest of the clause. The only other possibilities are
		// {@link Detectors} and {@link Monitors} or {@link IRunnableDevice}s
		for (final List<IClauseElementProcessor> clauseProcessors : processorsByClause) {
			throwIf(clauseProcessors.isEmpty() || clauseProcessors.contains(null),
					"clause resolution returned an empty or invalid processor list for a clause");
			IClauseElementProcessor procOne = withNullCheck(clauseProcessors.get(0));
			if (procOne.hasDetector()) {
				throwIf(!scanPathSeen, "No scan path defined - SPEC style scans not yet supported");
				throwIf(clauseProcessors.size() > 2, "too many elements in Detector clause");

				double exposure = 0;
				if (clauseProcessors.size() == 2) {
					IClauseElementProcessor procTwo = withNullCheck(clauseProcessors.get(1));
					throwIf(!procTwo.hasNumber(), "2nd element of unexpected type in Detector clause");
					exposure = Double.valueOf(procTwo.getElementValue());
				}
				if (procOne.hasScannable()) {
					logger.debug("Detector added for {}", addDetector(
							procOne.getElementValue(), exposure, detectorMap));
				} else {
					logger.debug("Detector added for {}", addDetector(
							(IRunnableDevice<?>) procOne.getElement(), exposure, detectorMap));
				}
			// Next check for a {@link Monitor} clause which can have only 1 element
			} else if (procOne.hasMonitor()) {
				throwIf(!scanPathSeen, "No scan path defined - SPEC style scans not yet supported");
				throwIf(clauseProcessors.size() != 1, "too many elements in Monitor clause");
				logger.debug("Monitor added for {}", addMonitor(procOne, monitorsPerPoint));
			// that only leaves a clause starting with a plain {@link Scannable} or {@link ScannableGroup}. This could
			// either be a readout or a scan path definition so is parsed by a dedicated method
			} else if (clauseProcessors.size() == 1) {
				throwIf(!scanPathSeen, "No scan path defined - SPEC style scans not yet supported");
				logger.debug("Scannable readout added for {}",  addMonitor(procOne, monitorsPerPoint));
			// Handle non mapping scan path definitions (not yet a comprehensive test)
			} else if (clauseProcessors.size() < 7 && withNullCheck(clauseProcessors.get(1)).hasNumber()){
				 throw new IllegalArgumentException("SPEC style scans not yet supported.");
			} else {
				addPathModelAndRegion(clauseProcessors, scanModel);
				scanPathSeen = true;
			}
		}

		// Populate the {@link ScanRequest} with the assembled objects
		ScanRequest<IROI> scanRequest = new ScanRequest<IROI>();
		scanRequest.setCompoundModel(scanModel);
		scanRequest.setDetectors(detectorMap);
		scanRequest.setMonitorNamesPerPoint(monitorsPerPoint);

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
	 * Add the name of a {@link Monitor} to the {@link List} of monitor names for the scan
	 *
	 * @param proc					The {@link IClauseElementProcessor} corresponding to the monitor
	 * @param monitorsPerPoint		The list of monitor names for the scan
	 * @return						The name of the added {@link Monitor}
	 */
	private String addMonitor(final IClauseElementProcessor proc, final List<String> monitorsPerPoint) {
		String name = proc.getElementValue();
		monitorsPerPoint.add(name);
		return name;
	}

	/**
	 * The main 'scan' parsing method; iterates over the list of processors that correspond to the current MScan clause,
	 * calling process on each one which will fill in the ClauseContext object as the loop proceeds. At the end of the
	 * loop, it will contain settings for each of the elements of a {@link ScanModel} (or default values) allowing one
	 * to be constructed along with a corresponding DAWN {@link IROI} object. These are passed in to the overall
	 * CompoundModel to be added as appropriate.
	 *
	 * @param clauseProcessors		The list of processors that correspond to the elements of the current MScan clause
	 * @param scanModel				The {@link ScanModel} to be filled in by the parsing process
	 * @throws IllegalArgumentException if the {@link IROI} fails to validate the supplied parameters on creation
	 */
	private void addPathModelAndRegion(final List<IClauseElementProcessor> clauseProcessors,
															final CompoundModel<IROI> scanModel) {
		final ClauseContext context = new ClauseContext();
		for (int index = 0; index < clauseProcessors.size(); index++){
			clauseProcessors.get(index).process(context, index);
		}

		if (context.validateAndAdjust() && context.getAreaScanpath() != null) {
			logger.debug("Valid scan definition clause detected and added");
			ArrayList<Number> boundingBoxParams = new ArrayList<>();

			IROI roi = context.getRoi().createIROI(context.getRoiParams());
			IRectangularROI boundingRoi = roi.getBounds();
			boundingBoxParams.add(boundingRoi.getPointX());
			boundingBoxParams.add(boundingRoi.getPointY());
			boundingBoxParams.add(boundingRoi.getLength(0));
			boundingBoxParams.add(boundingRoi.getLength(1));
			scanModel.setData(context.getAreaScanpath().createModel(context.getScannables(),
					context.getPathParams(), boundingBoxParams, context.getMutators()), roi);
		}
	}

	/**
	 * Adds an {@link IDetectorModel} corresponding to the supplied {@link Detector} name to the map of detectors
	 * setting the exposure time if specified
	 *
	 * @param detectorName			The name of the {@link Detector}
	 * @param exposure				The exposure time of the {@link Detector} to be set if specified
	 * @param detectorMap			The map of {@link IDetectorModel}s to add the specified one to, keyed by name
	 * @return						The name of the added {@link Detector}
	 * @throws ScanningException	If the {@link IRunnableDevice} for the {@link Detector} cannot be retrieved.
	 */
	private String addDetector(final String detectorName, final double exposure,
					final Map<String, Object> detectorMap) throws ScanningException {
		IRunnableDevice<IDetectorModel> detector = runnableDeviceService.getRunnableDevice(detectorName);
		if (detector == null) {
			throw new ScanningException(String.format("Could not get detector for name %s", detectorName));
		}
		return addDetector(detector, exposure, detectorMap);
	}

	/**
	 * Adds an {@link IDetectorModel} corresponding to the supplied {@link IRunnableDevice} to the map of detectors,
	 * setting the exposure time if specified
	 *
	 * @param detector				The {@link IRunnableDevice} corresponding to the detector
	 * @param exposure				The exposure time of the {@link Detector} to be set if specified
	 * @param detectorMap			The map of {@link IDetectorModel}s to add the specified one to, keyed by name
	 * @return						The name of the added {@link Detector}
	 * @throws ScanningException	If the {@link IDetectorModel} for the {@link IRunnableDevice} is null.
	 */
	private String addDetector(final IRunnableDevice<?> detector, final double exposure,
			final Map<String, Object> detectorMap) throws ScanningException {
		IDetectorModel model = (IDetectorModel)detector.getModel();
		if (model == null) {
			throw new ScanningException(String.format("Could not get model for detector %s", detector.getName()));
		}
		if (exposure > 0) {
			model.setExposureTime(exposure);
		}
		detectorMap.put(detector.getName(), model);
		return detector.getName();
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

		// Iterate over the command args looking for Scannable and Scannable groups. Can't just use the returned class
		// as many classes implement Scannable
		for (int i = 0; i < args.length ; i++) {
			Class<?> type = args[i].getClass();
			if (args[i] instanceof Scannable) {
				if (args[i] instanceof ScannableGroup) {
					type = ScannableGroup.class;
				} else {
					type = Scannable.class;
				}
			}
			else if (args[i] instanceof IRunnableDevice<?>) {
				type = IRunnableDevice.class;
			}
			// Check the processorBuilders map can construct a processor for the required type and if so
			// make it and add it to the collection of processors that correspond to the command.
			if (!processorBuilders.containsKey(type)) {
				throw new IllegalArgumentException(String.format(
						"Your command contains an invalid argument at position %d", i));
			}
			processors.add(processorBuilders.get(type).apply(args[i]));
		}
		return resolverFactory.getResolver(processors);
	}

	// Utility methods to handle parameter checking more concisely

	/**
	 * Throws an [@link IllegalArgumentException} using the supplied message prefixed with a standard header.
	 *
	 * @param message		The message to be appended to the standard header
	 * @throws IllegalArgumentException
	 */
	private void throwOnInvalidCommand(final String message) {
		throw new IllegalArgumentException(String.format("Invalid MScan command - %s", message));
	}

	/**
	 * Throws an [@link IllegalArgumentException} using the supplied message prefixed with a standard header if the
	 * rejection condition is true;.
	 *
	 * @param rejectionCondition	The boolean to be evaluated to determin if an exception should be thrown (if true)
	 * @param failureMessage		The message to be appended to the standard header
	 *
	 * @throws IllegalArgumentException	if the rejection condition is true
	 */
	private void throwIf(final boolean rejectionCondition, final String failureMessage) {
		if (rejectionCondition) {
			throwOnInvalidCommand(failureMessage);
		}
	}

	/**
	 * Provides an in line null check throwing if this fails
	 *
	 * @param toReturn	{@link IClauseElementProcessor} to be evaluated and returned if it is not null
	 * @return			The incoming parameter if it is not null
	 * throws IllegaArgumentException if the incoming parameter is null
	 */
	private IClauseElementProcessor withNullCheck(final IClauseElementProcessor toReturn) {
		if (toReturn == null) {
			throwOnInvalidCommand("retrieved processor was null");
		}
		return toReturn;
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


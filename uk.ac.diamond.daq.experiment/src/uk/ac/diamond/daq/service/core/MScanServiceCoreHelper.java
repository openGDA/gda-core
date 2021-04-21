package uk.ac.diamond.daq.service.core;

import static org.eclipse.scanning.server.servlet.Services.getEventService;
import static org.eclipse.scanning.server.servlet.Services.getRunnableDeviceService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;
import gda.factory.Finder;
import gda.mscan.MScanSubmitter;
import gda.mscan.element.Mutator;
import gda.mscan.element.RegionShape;
import gda.mscan.element.ScanDataConsumer;
import gda.mscan.element.Scanpath;
import uk.ac.diamond.daq.mapping.api.document.exception.ScanningAcquisitionServiceException;
import uk.ac.gda.api.acquisition.request.MscanRequest;

/**
 * Extracts from the {@link AcquisitionServiceCore} the mscan service logic.
 * 
 * @author Maurizio Nagni
 *
 */
class MScanServiceCoreHelper {
	private static final Logger logger = LoggerFactory.getLogger(MScanServiceCoreHelper.class);

	protected void runMScan(MscanRequest request) throws ScanningAcquisitionServiceException {
		try {
			createMScanSubmitter().buildAndSubmitScanRequest(parseMscanString(request.getCommand().split("\\s+")), request.isBlock());
		} catch (Exception e) {
			throw new ScanningAcquisitionServiceException("Cannot submit mscan", e);
		}
	}

	private MScanSubmitter createMScanSubmitter() {
		return new MScanSubmitter(getEventService(), getRunnableDeviceService());
	}

	private Object[] parseMscanString(final String[] args) {
		List<Object> args2 = new ArrayList<>();

		IntStream.range(0, args.length)
			.forEach(i -> {
				Object res = parseElement(args[i]);
				if (List.class.isInstance(res)) { 
					args2.addAll((List)res);
				} else {
					args2.add(res);
				}

			});
		return args2.toArray();
	}

	private Object parseElement(String element) {
		Optional<Scannable> scannable = Finder.findOptionalOfType(element, Scannable.class);
		if (scannable.isPresent()) {
			return scannable.get();
		}
		try {
			IRunnableDevice<?> runnableDevice = getRunnableDeviceService().getRunnableDevice(element);
			if (runnableDevice != null)
				return runnableDevice;
		} catch (ScanningException e) {
			logger.error("Error parsing command string", e);
		}
		if (element.contains(",")) {
			String[] elements = element.split(",");

			try {
				// Are numeric elements?
				Double.parseDouble(elements[0]);
			} catch (Exception e) {
				// No so they are strings
				return Arrays.stream(elements)
						.collect(Collectors.toList());
			}

			return Arrays.stream(elements)
					.map(Double::parseDouble)
					.collect(Collectors.toList());
		}

		try {
			// Is a numeric elements?
			return Double.parseDouble(element);
		} catch (Exception e) {

		}

		if (scanpathMap.containsKey(element)) {
			return scanpathMap.get(element);
		}

		if (regionshapeMap.containsKey(element)) {
			return regionshapeMap.get(element);
		}

		if (mutatorMap.containsKey(element)) {
			return mutatorMap.get(element);
		}

		if (scanDataConsumerMap.containsKey(element)) {
			return scanDataConsumerMap.get(element);
		}

		if (stringMap.containsKey(element)) {
			return stringMap.get(element);
		}

		return element;
	}

	private static Map<String, Scanpath> scanpathMap = new HashMap<>();
	private static Map<String, RegionShape> regionshapeMap = new HashMap<>();
	private static Map<String, Mutator> mutatorMap = new HashMap<>();
	private static Map<String, ScanDataConsumer> scanDataConsumerMap = new HashMap<>();	
	private static Map<String, String> stringMap = new HashMap<>();

	static {
		// The mapping below reproduces the one in mscanHandler.py

		// ------------- scanpath ----------------
		scanpathMap.put("grid", Scanpath.GRID_POINTS);

		scanpathMap.put("rast", Scanpath.GRID_STEP);
		scanpathMap.put("raster", Scanpath.GRID_STEP);

		scanpathMap.put("spir", Scanpath.SPIRAL);
		scanpathMap.put("spiral", Scanpath.SPIRAL);

		scanpathMap.put("liss", Scanpath.LISSAJOUS);
		scanpathMap.put("lissajous", Scanpath.LISSAJOUS);

		scanpathMap.put("step", Scanpath.LINE_STEP);
		scanpathMap.put("angl", Scanpath.LINE_STEP);
		scanpathMap.put("angle", Scanpath.LINE_STEP);

		scanpathMap.put("poin", Scanpath.SINGLE_POINT);

		scanpathMap.put("nopt", Scanpath.LINE_POINTS);
		scanpathMap.put("pts", Scanpath.LINE_POINTS);
		scanpathMap.put("noofpoints", Scanpath.LINE_POINTS);
		scanpathMap.put("points", Scanpath.LINE_POINTS);
		scanpathMap.put("proj", Scanpath.LINE_POINTS);
		scanpathMap.put("projections", Scanpath.LINE_POINTS);

		scanpathMap.put("axst", Scanpath.AXIS_STEP);
		scanpathMap.put("axisstep", Scanpath.AXIS_STEP);

		scanpathMap.put("axno", Scanpath.AXIS_POINTS);
		scanpathMap.put("axispoints", Scanpath.AXIS_POINTS);

		scanpathMap.put("stat", Scanpath.STATIC);
		scanpathMap.put("static", Scanpath.STATIC);
		scanpathMap.put("acq", Scanpath.STATIC);
		scanpathMap.put("acquire", Scanpath.STATIC);

		// ------------- regions ----------------
		regionshapeMap.put("rect", RegionShape.RECTANGLE);
		regionshapeMap.put("rectangle", RegionShape.RECTANGLE);

		regionshapeMap.put("crec", RegionShape.CENTRED_RECTANGLE);
		regionshapeMap.put("centred_rectangle", RegionShape.CENTRED_RECTANGLE);

		regionshapeMap.put("circ", RegionShape.CIRCLE);
		regionshapeMap.put("circle", RegionShape.CIRCLE);

		regionshapeMap.put("poly", RegionShape.POLYGON);
		regionshapeMap.put("polygon", RegionShape.POLYGON);

		regionshapeMap.put("line", RegionShape.LINE);
		regionshapeMap.put("helical", RegionShape.LINE);
		regionshapeMap.put("heli", RegionShape.LINE);
		regionshapeMap.put("htom", RegionShape.LINE);

		regionshapeMap.put("poin", RegionShape.POINT);
		regionshapeMap.put("point", RegionShape.POINT);
		regionshapeMap.put("pt", RegionShape.POINT);

		regionshapeMap.put("axis", RegionShape.AXIAL);
		regionshapeMap.put("rotation", RegionShape.AXIAL);
		regionshapeMap.put("rota", RegionShape.AXIAL);
		regionshapeMap.put("rot", RegionShape.AXIAL);
		regionshapeMap.put("tomography", RegionShape.AXIAL);
		regionshapeMap.put("tomo", RegionShape.AXIAL);

		// ------------- mutators ----------------
		mutatorMap.put("snak", Mutator.ALTERNATING);
		mutatorMap.put("snake", Mutator.ALTERNATING);
		mutatorMap.put("alte", Mutator.ALTERNATING);

		mutatorMap.put("roff", Mutator.RANDOM_OFFSET);
		mutatorMap.put("random_offset", Mutator.RANDOM_OFFSET);

		mutatorMap.put("cont", Mutator.CONTINUOUS);
		mutatorMap.put("continuous", Mutator.CONTINUOUS);


		// ------------- scanData consumer ----------------
		scanDataConsumerMap.put("temp", ScanDataConsumer.TEMPLATE);
		scanDataConsumerMap.put("templates", ScanDataConsumer.TEMPLATE);

		scanDataConsumerMap.put("proc", ScanDataConsumer.PROCESSOR);
		scanDataConsumerMap.put("processors", ScanDataConsumer.PROCESSOR);

		// ------------- strings ----------------
		stringMap.put("rrun", "rerun");
		stringMap.put("rerun", "rerun");
	}
}

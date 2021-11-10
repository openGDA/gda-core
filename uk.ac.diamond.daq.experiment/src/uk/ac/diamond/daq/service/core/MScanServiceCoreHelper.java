package uk.ac.diamond.daq.service.core;

import static org.eclipse.scanning.server.servlet.Services.getEventService;
import static org.eclipse.scanning.server.servlet.Services.getRunnableDeviceService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import gda.mscan.element.Action;
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

		if (Scanpath.termsMap().containsKey(element)) {
			return Scanpath.termsMap().get(element);
		}

		if (RegionShape.termsMap().containsKey(element)) {
			return RegionShape.termsMap().get(element);
		}

		if (Mutator.termsMap().containsKey(element)) {
			return Mutator.termsMap().get(element);
		}

		if (ScanDataConsumer.termsMap().containsKey(element)) {
			return ScanDataConsumer.termsMap().get(element);
		}

		if (Action.termsMap().containsKey(element)) {
			return Action.termsMap().get(element);
		}

		return element;
	}
}

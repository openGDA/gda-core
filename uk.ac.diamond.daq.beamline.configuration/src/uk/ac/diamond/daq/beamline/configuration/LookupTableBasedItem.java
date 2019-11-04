package uk.ac.diamond.daq.beamline.configuration;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import gda.device.Scannable;
import uk.ac.diamond.daq.beamline.configuration.api.ScannablePositionLookupService;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowException;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowItem;

/**
 * {@link WorkflowItem} implementation which fetches target positions for its
 * configured {@link Scannable scannables} from a lookup table.
 */
public class LookupTableBasedItem extends WorkflowItemBase {

	private Map<String, Scannable> scannables;
	private String inputProperty;
	private ScannablePositionLookupService positionLookupService;

	public LookupTableBasedItem(String inputProperty, Map<String, Scannable> scannables,
			ScannablePositionLookupService positionLookupService) {

		this.inputProperty = inputProperty;
		this.scannables = scannables;
		this.positionLookupService = positionLookupService;
	}

	@Override
	public Set<Scannable> getScannables() {
		return scannables.entrySet().stream()
				.map(Map.Entry::getValue).collect(Collectors.toSet());
	}

	@Override
	public Map<Scannable, Object> getPositions(Properties properties) throws WorkflowException {
		double lookupArgument = getLookupArgument(properties);
		Map<String, Object> scannablePositions = positionLookupService.getScannablePositions(lookupArgument, scannables.keySet());

		return createPositionsMap(scannablePositions);
	}

	private double getLookupArgument(Properties properties) throws WorkflowException {
		String rawValue = properties.getProperty(inputProperty);
		if (rawValue == null) {
			throw new WorkflowException("Property '" + inputProperty + "' not found");
		}

		return Double.parseDouble(rawValue);
	}

	private Map<Scannable, Object> createPositionsMap(Map<String, Object> scannablePositions) {
		return scannablePositions.entrySet().stream()
			.collect(Collectors.toMap(entry -> scannables.get(entry.getKey()),	Map.Entry::getValue));
	}

}

package uk.ac.diamond.daq.beamline.configuration.api;

import java.util.Map;
import java.util.Set;

public interface ScannablePositionLookupService {

	Map<String, Object> getScannablePositions(Object value, Set<String> scannables) throws WorkflowException;

}
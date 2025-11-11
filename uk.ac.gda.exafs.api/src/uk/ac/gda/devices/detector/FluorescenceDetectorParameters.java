package uk.ac.gda.devices.detector;

import java.util.List;

import uk.ac.gda.beans.exafs.IDetectorConfigurationParameters;
import uk.ac.gda.beans.vortex.DetectorElement;

public interface FluorescenceDetectorParameters extends IDetectorConfigurationParameters {

	String getDetectorName();

	DetectorElement getDetector(int i);

	List<DetectorElement> getDetectorList();

	void setDetectorList(List<DetectorElement> detectorList);
}

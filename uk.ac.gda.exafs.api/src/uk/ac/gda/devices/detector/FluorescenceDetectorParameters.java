package uk.ac.gda.devices.detector;

import java.util.List;

import uk.ac.gda.beans.exafs.IDetectorConfigurationParameters;
import uk.ac.gda.beans.vortex.DetectorElement;

public interface FluorescenceDetectorParameters extends IDetectorConfigurationParameters{

	DetectorElement getDetector(int i);
	
	public List<DetectorElement> getDetectorList();

	public void setDetectorList(List<DetectorElement> detectorList);
}

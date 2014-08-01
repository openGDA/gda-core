package uk.ac.gda.exafs.ui.detector.vortex;

import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.VortexROI;

public class DetectorViewUtils {
	
	public DetectorElement createElement(int number){
		DetectorElement element1 = new DetectorElement();
		element1.setName("Element " + number);
		element1.setNumber(number);
		element1.setGain(2.0);
		element1.setPeakingTime(1.04);
		element1.setOffset(-21.0);
		element1.setExcluded(false);
		VortexROI vortexROI = new VortexROI();
		vortexROI.setRoiName("ROI 1");
		vortexROI.setWindowStart(690);
		vortexROI.setWindowEnd(890);
		element1.addRegion(vortexROI);
		return element1;
	}
}

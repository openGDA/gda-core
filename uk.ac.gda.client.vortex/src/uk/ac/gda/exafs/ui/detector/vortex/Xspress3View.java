package uk.ac.gda.exafs.ui.detector.vortex;

import gda.device.Timer;
import gda.factory.Finder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.devices.detector.FluorescenceDetector;

public class Xspress3View extends ViewPart{

	@Override
	public void createPartControl(Composite parent) {
		
		String path = null;
		List<DetectorElement> detectorList = new ArrayList<DetectorElement>();
		
		DetectorViewUtils detectorViewUtils = new DetectorViewUtils();
		
		detectorList.add(detectorViewUtils.createElement(1));
		detectorList.add(detectorViewUtils.createElement(2));
		detectorList.add(detectorViewUtils.createElement(3));
		detectorList.add(detectorViewUtils.createElement(4));
		
		String detectorName = "xspress3";
		String tfgName = "tfg";
		FluorescenceDetector xspress3Detector = (FluorescenceDetector) Finder.getInstance().find(detectorName);
		Timer tfg = (Timer) Finder.getInstance().find(tfgName);
		new Vortex(path, this.getSite(), parent, detectorList, xspress3Detector, tfg);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}

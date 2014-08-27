package uk.ac.gda.exafs.ui.detector.xspress;

import gda.device.detector.xspress.XspressDetector;
import gda.factory.Finder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import uk.ac.gda.beans.XspressROI;
import uk.ac.gda.beans.xspress.DetectorElement;

public class XspressView extends ViewPart {
	
	@Override
	public void createPartControl(Composite parent) {
		XspressDetector xspressDetector = Finder.getInstance().find("xspress2system");
		List<DetectorElement> detectorList = new ArrayList<DetectorElement>();
		detectorList.add(createElement(1));
		detectorList.add(createElement(2));
		detectorList.add(createElement(3));
		detectorList.add(createElement(4));
		Xspress xspress = new Xspress("", this.getSite(), parent, xspressDetector, detectorList, null);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	
//	<DetectorElement>
//    <name>Element 3</name>
//    <number>3</number>
//    <windowStart>742</windowStart>
//    <windowEnd>850</windowEnd>
//    <excluded>false</excluded>
//    <Region>
//        <roiName>ROI_1</roiName>
//        <roiStart>760</roiStart>
//        <roiEnd>841</roiEnd>
//    </Region>
//    </DetectorElement>
	
	private DetectorElement createElement(int number){
		DetectorElement element1 = new DetectorElement();
		element1.setName("Element " + number);
		element1.setNumber(number);
		element1.setWindowStart(700);
		element1.setWindowEnd(800);
		element1.setExcluded(false);
		XspressROI xspressROI = new XspressROI();
		xspressROI.setRoiName("ROI 1");
		xspressROI.setRoiStart(650);
		xspressROI.setRoiEnd(750);
		element1.addRegion(xspressROI);
		return element1;
	}
	
}

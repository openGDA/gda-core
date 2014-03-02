/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector.vortex;

import gda.device.Timer;
import gda.device.XmapDetector;
import gda.factory.Finder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.VortexROI;

public class VortexView extends ViewPart {
	public VortexView() {
	}

//	<DetectorElement>
//  <name>Element1</name>
//  <number>1</number>
//  <ROI>
//      <name>ROI 1</name>
//      <windowStart>690</windowStart>
//      <windowEnd>890</windowEnd>
//  </ROI>
//  <gain>2.0</gain>
//  <peakingTime>1.04</peakingTime>
//  <offset>-21.0</offset>
//  <excluded>false</excluded>
//	</DetectorElement>
	
	private DetectorElement createElement(int number){
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
	
	@Override
	public void createPartControl(Composite parent) {
		String path = null;
		List<DetectorElement> detectorList = new ArrayList<DetectorElement>();
		
		detectorList.add(createElement(1));
		detectorList.add(createElement(2));
		detectorList.add(createElement(3));
		detectorList.add(createElement(4));
		
		String detectorName = "xmapMca";
		String tfgName = "tfg";
		XmapDetector xmapDetector = (XmapDetector) Finder.getInstance().find(detectorName);
		Timer tfg = (Timer) Finder.getInstance().find(tfgName);
		new Vortex(path, this.getSite(), parent, detectorList, xmapDetector, tfg);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}

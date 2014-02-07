/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector.xspress;

import gda.device.detector.xspress.ResGrades;
import gda.device.detector.xspress.XspressDetector;

import java.awt.Color;
import java.util.List;

import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPartSite;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.gda.beans.xspress.DetectorElement;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.data.ScanObjectManager;
import uk.ac.gda.exafs.ui.detector.Detector;
import uk.ac.gda.exafs.ui.detector.DetectorElementComposite;
import uk.ac.gda.exafs.ui.detector.DetectorROIComposite;
import uk.ac.gda.exafs.ui.detector.Plot;
import uk.ac.gda.exafs.ui.detector.XspressROIComposite;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.components.selector.BeanSelectionEvent;
import uk.ac.gda.richbeans.components.selector.BeanSelectionListener;
import uk.ac.gda.richbeans.components.selector.ListEditor;
import uk.ac.gda.richbeans.components.selector.ListEditorUIAdapter;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

public class Xspress extends Detector{
	private XspressAcquire xspressAcquire;
	private Label lblRegionBins;
	private ResolutionGrade resolutionGrade;
	private ReadoutMode readoutMode;
	private RegionType regionType;
	private XspressPreferences xspressPreferences;
	private XspressElements xspressElements;
	private RegionSynchronizer regionSynchronizer;
	private XspressParameters xspressParameters;
	
	public Xspress(String path, IWorkbenchPartSite site, Composite parent, XspressParameters xspressParameters, DirtyContainer dirtyContainer) {
		super("xspressConfig", site, parent, path);
		this.xspressParameters = xspressParameters;
		regionSynchronizer = new RegionSynchronizer();
		try {
			sashPlotFormComposite = new SashFormPlotComposite(parent, site.getPart(), regionSynchronizer, createUpLoadAction(path));
		} catch (Exception e) {
		}
		sashPlotFormComposite.getPlottingSystem().setRescale(true);
		plot = new Plot(sashPlotFormComposite);
		sashPlotFormComposite.setWeights(new int[] { 30, 74 });
		Composite left = sashPlotFormComposite.getLeft();
		Composite topComposite = new Composite(left, SWT.NONE);
		topComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		topComposite.setLayout(gridLayout_1);
		resolutionGrade = new ResolutionGrade(topComposite);
		readoutMode = new ReadoutMode(topComposite);
		regionType = new RegionType(topComposite);
		boolean showRoi = readoutMode.getReadoutMode().getValue().toString().equals("Regions Of Interest");
		xspressAcquire = new XspressAcquire(left, sashPlotFormComposite, site.getShell().getDisplay(), readoutMode.getReadoutMode(), resolutionGrade.getResolutionGradeCombo(), plot, dirtyContainer, xspressParameters, counts);
		xspressElements = new XspressElements(left, site.getShell(), dirtyContainer, sashPlotFormComposite, xspressParameters, counts, showRoi);
		if(!ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.DETECTOR_OUTPUT_IN_OUTPUT_PARAMETERS) && !ScanObjectManager.isXESOnlyMode())
			xspressPreferences = new XspressPreferences(left);
		xspressElements.configureUI(xspressAcquire.getMcaData(), xspressElements.getDetectorListComposite().getDetectorList().getSelectedIndex());
		xspressAcquire.addAcquireListener(xspressElements.getDetectorListComposite().getDetectorList().getSelectedIndex(), xspressElements.getDetectorListComposite().getDetectorList(), xspressElements.getDetectorListComposite().getDetectorElementComposite());
		xspressElements.getDetectorListComposite().getDetectorList().addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				int[][][] mcaData = xspressAcquire.getMcaData();
				if(mcaData!=null){
					plot.plot(evt.getSelectionIndex(), mcaData, false, null);
					DetectorElementComposite detectorElementComposite = xspressElements.getDetectorListComposite().getDetectorElementComposite();
					detectorElementComposite.setTotalElementCounts(counts.getTotalElementCounts(evt.getSelectionIndex(), mcaData));
					detectorElementComposite.setTotalCounts(counts.getTotalCounts(mcaData));
					xspressElements.setAllElementsCount(counts.getTotalCounts(mcaData));
					xspressElements.setElementCount(counts.getTotalElementCounts(evt.getSelectionIndex(), mcaData));
					int windowStart = getBeanDetectorList().get(evt.getSelectionIndex()).getWindowStart();
					int windowEnd = getBeanDetectorList().get(evt.getSelectionIndex()).getWindowEnd();
					RectangularROI rectangularROI = (RectangularROI)sashPlotFormComposite.getRegionOnDisplay().getROI();
					double[] point = rectangularROI.getPoint();
					point[0]=windowStart;
					double[] endPoint = rectangularROI.getEndPoint();
					endPoint[0]=windowEnd;
					rectangularROI.setPoint(point);
					rectangularROI.setEndPoint(endPoint);
					sashPlotFormComposite.getRegionOnDisplay().setROI(rectangularROI);
				}
			}
		});
		
		sashPlotFormComposite.getRegionOnDisplay().addROIListener(new IROIListener(){

			@Override
			public void roiDragged(ROIEvent evt) {
				// TODO Auto-generated method stub
			}

			@Override
			public void roiChanged(ROIEvent evt) {
				int start = (int)((RectangularROI) sashPlotFormComposite.getRegionOnDisplay().getROI()).getPoint()[0];
				int end = (int)((RectangularROI) sashPlotFormComposite.getRegionOnDisplay().getROI()).getEndPoint()[0];
				int selectedIndex = xspressElements.getDetectorListComposite().getDetectorList().getSelectedIndex();
				if(xspressElements.getApplyToAllCheckbox().getSelection())
					for(int i=0;i<xspressElements.getDetectorListComposite().getDetectorList().getListSize();i++)
						updateBean(i, start, end);
				else
					updateBean(selectedIndex, start, end);
				int[][][] mcaData = xspressAcquire.getMcaData();
				if(start>=0){
					regionSynchronizer.setStart(start);
					regionSynchronizer.setEnd(end);
				}
				xspressElements.updateWindow(start, end, mcaData);
			}

			@Override
			public void roiSelected(ROIEvent evt) {
				// TODO Auto-generated method stub
			}
			
		});
		
		xspressAcquire.addLoadListener(xspressElements.getDetectorListComposite().getDetectorList(), xspressElements.getDetectorListComposite().getDetectorElementComposite(), xspressParameters.getDetectorList().size());
		
		if(readoutMode.isModeOveride()) {
			GridUtils.setVisibleAndLayout(readoutMode.getReadoutMode(), false);
			GridUtils.setVisibleAndLayout(resolutionGrade.getResolutionGradeCombo(), false);
			GridUtils.setVisibleAndLayout(lblRegionBins, false);
			GridUtils.setVisibleAndLayout(regionType.getRegionType(), false);
		}
		
		addReadoutModeListenerListener();
		updateRegionType();
	}
	
	private void addReadoutModeListenerListener(){
		readoutMode.getReadoutMode().addValueListener(new ValueAdapter("readoutMode") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateRegionType();
			}
		});
	}
	
	private void updateRegionType(){		
		readoutMode.updateOverrideMode();
		String readoutModeValue = readoutMode.getReadoutMode().getValue().toString();
		boolean isRoi = readoutModeValue.equals("Regions Of Interest");
		resolutionGrade.setVisible(isRoi);
		regionType.setVisible(isRoi);
		DetectorROIComposite detectorROIComposite = xspressElements.getDetectorListComposite().getDetectorElementComposite().getDetectorROIComposite();
		if(isRoi && detectorROIComposite==null)
			xspressElements.getDetectorListComposite().getDetectorElementComposite().createRegionList();
		if(detectorROIComposite!=null)
			xspressElements.getDetectorListComposite().getDetectorElementComposite().getDetectorROIComposite().setVisible(isRoi);
		xspressElements.getDetectorListComposite().getDetectorElementComposite().setWindowsEditable(!isRoi);
		GridUtils.startMultiLayout(xspressElements.getDetectorListComposite().getParent());
	}
	
	private String getResGradeAllowingForReadoutMode() {
		String uiReadoutMode = (String) readoutMode.getReadoutMode().getValue();
		return uiReadoutMode.equals(XspressDetector.READOUT_ROIS) ? (String) resolutionGrade.getResolutionGradeCombo().getValue() : ResGrades.NONE;
	}

	private void updateBean(int index, int start, int end){
		xspressParameters.getDetectorList().get(index).setWindow(start, end);
	}
	
	private List<DetectorElement> getBeanDetectorList(){
		return xspressParameters.getDetectorList();
	}
	
	protected String getChannelName(int iChannel) {
		String resGrade = getResGradeAllowingForReadoutMode();
		if (resGrade.equals(ResGrades.ALLGRADES))
			return "Resolution Grade " + (iChannel + 1);
		else if (resGrade.startsWith(ResGrades.THRESHOLD)) {
			switch (iChannel) {
			case 0:
				return "Bad";
			case 1:
				return "Good";
			default:
				return "" + iChannel;
			}
		}
		return "" + iChannel;
	}
	
	private ListEditorUIAdapter listEditorUI = new ListEditorUIAdapter() {
		@Override
		public void notifySelected(ListEditor listEditor) {
			XspressROIComposite xspressROIComposite = (XspressROIComposite) (listEditor.getEditorUI());
			xspressROIComposite.setFitTypeVisibility();
		}
	};
	
	@Override
	protected java.awt.Color getChannelColor(int iChannel) {
		String resGrade = getResGradeAllowingForReadoutMode();
		if (resGrade.startsWith(ResGrades.THRESHOLD)) {
			switch (iChannel) {
			case 0:
				return Color.RED; // BAD
			case 1:
				return Color.BLUE; // GOOD
			default:
				return super.getChannelColor(iChannel);
			}
		}
		return super.getChannelColor(iChannel);
	}

	public void updateVisibility(Composite composite) {
		boolean isRoi = readoutMode.getReadoutMode().getValue().equals(XspressDetector.READOUT_ROIS);
		xspressElements.getDetectorListComposite().getDetectorElementComposite().setWindowsEditable(!isRoi);
		Composite roi = xspressElements.getDetectorListComposite().getDetectorElementComposite().getRegionList();
		GridUtils.setVisibleAndLayout(roi, isRoi);
		GridUtils.startMultiLayout(composite.getParent());
		try {
			if(readoutMode.getReadoutMode().getSelectionIndex() == 2 && !readoutMode.isModeOveride()) {
				GridUtils.setVisibleAndLayout(resolutionGrade.getResolutionGradeCombo(), true);
				GridUtils.setVisibleAndLayout(lblRegionBins, true);
				GridUtils.setVisibleAndLayout(regionType.getRegionType(), true);
			} 
			else {
				GridUtils.setVisibleAndLayout(resolutionGrade.getResolutionGradeCombo(), false);
				GridUtils.setVisibleAndLayout(lblRegionBins, false);
				GridUtils.setVisibleAndLayout(regionType.getRegionType(), false);
			}
		} finally {
			GridUtils.endMultiLayout();
		}
		listEditorUI.notifySelected(xspressElements.getDetectorListComposite().getDetectorElementComposite().getRegionList());
		xspressElements.getDetectorListComposite().getDetectorElementComposite().getRegionList().setListEditorUI(listEditorUI);
	}
	
	public ResolutionGrade getResolutionGrade() {
		return resolutionGrade;
	}

	public ReadoutMode getReadoutMode() {
		return readoutMode;
	}

	public RegionType getRegionType() {
		return regionType;
	}

	public XspressPreferences getXspressPreferences() {
		return xspressPreferences;
	}

	public XspressElements getXspressElements() {
		return xspressElements;
	}
	
	public class RegionSynchronizer implements IROIListener {
		private double start;
		private double end;
		
		public RegionSynchronizer(){
		}

		@Override
		public void roiDragged(ROIEvent evt) {
		}

		public void setStart(double start) {
			this.start = start;
		}

		public void setEnd(double end) {
			this.end = end;
		}

		public double getStart() {
			return start;
		}

		public double getEnd() {
			return end;
		}

		@Override
		public void roiChanged(ROIEvent evt) {
			int start = (int)((RectangularROI) sashPlotFormComposite.getRegionOnDisplay().getROI()).getPoint()[0];
			int end = (int)((RectangularROI) sashPlotFormComposite.getRegionOnDisplay().getROI()).getEndPoint()[0];					
			DetectorElementComposite detectorElementComposite = xspressElements.getDetectorListComposite().getDetectorElementComposite();
			detectorElementComposite.getStart().setValue(start);
			detectorElementComposite.getEnd().setValue(end);
		}

		@Override
		public void roiSelected(ROIEvent evt) {
			// TODO Auto-generated method stub
			
		}
	}
	
}
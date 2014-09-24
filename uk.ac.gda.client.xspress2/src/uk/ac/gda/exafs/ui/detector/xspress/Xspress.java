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

import gda.device.detector.xspress.XspressDetector;
import gda.device.detector.xspress.xspress2data.ResGrades;

import java.awt.Color;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.jreality.util.PlotColorUtility;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.gda.beans.XspressParameters;
import uk.ac.gda.beans.xspress.DetectorElement;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.data.ScanObjectManager;
import uk.ac.gda.exafs.ui.detectorviews.Detector;
import uk.ac.gda.exafs.ui.detectorviews.DetectorElementComposite;
import uk.ac.gda.exafs.ui.detectorviews.DetectorListComposite;
import uk.ac.gda.exafs.ui.detectorviews.DetectorROIComposite;
import uk.ac.gda.exafs.ui.detectorviews.Plot;
import uk.ac.gda.exafs.ui.detectorviews.XspressROIComposite;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.components.selector.BeanSelectionEvent;
import uk.ac.gda.richbeans.components.selector.BeanSelectionListener;
import uk.ac.gda.richbeans.components.selector.GridListEditor;
import uk.ac.gda.richbeans.components.selector.ListEditor;
import uk.ac.gda.richbeans.components.selector.ListEditorUIAdapter;
import uk.ac.gda.richbeans.components.selector.VerticalListEditor;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

public class Xspress extends Detector{
	private XspressAcquire xspressAcquire;
	private ResolutionGrade resolutionGrade;
	private ReadoutMode readoutMode;
	private RegionType regionType;
	private XspressPreferences xspressPreferences;
	private XspressElements xspressElements;
	private RegionSynchronizer regionSynchronizer;
	private XspressParameters xspressParameters;
	private TableViewer tableViewer;
	private GridListEditor gridListEditor;
	private DetectorElementComposite detectorElementComposite;
	private DetectorListComposite detectorListComposite;
	private int selectedElement = 0;
	
	public Xspress(String path, IWorkbenchPartSite site, Composite parent, XspressDetector xspressDetector, List<DetectorElement> detectorList, final XspressParameters xspressParameters) {
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
		
		if(xspressParameters!=null){
			int mode = 0;
			String readoutModeFromBean = xspressParameters.getReadoutMode();
			if(readoutModeFromBean.equals(XspressDetector.READOUT_SCALERONLY )){
				mode=0;
			}
			else if(readoutModeFromBean.equals(XspressDetector.READOUT_MCA )){
				mode=1;
			}
			else if(readoutModeFromBean.equals(XspressDetector.READOUT_ROIS )){
				mode=2;
			}
			readoutMode.getReadoutMode().select(mode);
		}
		
		
		regionType = new RegionType(topComposite);
		xspressAcquire = new XspressAcquire(left, sashPlotFormComposite, site.getShell().getDisplay(), readoutMode.getReadoutMode(), resolutionGrade.getResolutionGradeCombo(), plot, xspressDetector, counts);
		boolean showRoi = readoutMode.getReadoutMode().getValue().toString().equals("Regions Of Interest");
		xspressElements = new XspressElements(left, site.getShell(), sashPlotFormComposite, detectorList, counts, showRoi, xspressParameters);
		detectorListComposite = xspressElements.getDetectorListComposite();
		gridListEditor = detectorListComposite.getDetectorList();
		if(!ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.DETECTOR_OUTPUT_IN_OUTPUT_PARAMETERS) && !ScanObjectManager.isXESOnlyMode())
			xspressPreferences = new XspressPreferences(left);
		xspressElements.configureUI(xspressAcquire.getMcaData(), gridListEditor.getSelectedIndex());
		detectorElementComposite = detectorListComposite.getDetectorElementComposite();
		int size = detectorList.size();
		xspressAcquire.addAcquireListener(gridListEditor, detectorElementComposite);
		xspressAcquire.addLoadListener(gridListEditor, detectorElementComposite, detectorList.size());

		detectorElementComposite.getRegionList().addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				//if (getDetectorList().getSelectedIndex() == lastSelectedElementIndex) {
				//	XspressParameters detBean = (XspressParameters) bean;
				//	detBean.setSelectedRegionNumber(evt.getSelectionIndex());
				//}
				//lastSelectedElementIndex = getDetectorList().getSelectedIndex();
				xspressElements.updateROIAfterElementCompositeChange();
			}
		});

		
		gridListEditor.addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				int[][][] mcaData = xspressAcquire.getMcaData();
				if(mcaData!=null){
					plot.plot(evt.getSelectionIndex(), mcaData, false, null);
					detectorElementComposite.setTotalElementCounts(counts.getTotalElementCounts(evt.getSelectionIndex(), mcaData));
					detectorElementComposite.setTotalCounts(counts.getTotalCounts(mcaData));
					xspressElements.setAllElementsCount(counts.getTotalCounts(mcaData));
					xspressElements.setElementCount(counts.getTotalElementCounts(evt.getSelectionIndex(), mcaData));
					int windowStart = xspressParameters.getDetectorList().get(evt.getSelectionIndex()).getWindowStart();
					int windowEnd = xspressParameters.getDetectorList().get(evt.getSelectionIndex()).getWindowEnd();
					RectangularROI rectangularROI = (RectangularROI)sashPlotFormComposite.getRegionOnDisplay().getROI();
					if(rectangularROI!=null){
						double[] point = rectangularROI.getPoint();
						point[0]=windowStart;
						double[] endPoint = rectangularROI.getEndPoint();
						endPoint[0]=windowEnd;
						rectangularROI.setPoint(point);
						rectangularROI.setEndPoint(endPoint);
						sashPlotFormComposite.getRegionOnDisplay().setROI(rectangularROI);
					}
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
				int selectedIndex = gridListEditor.getSelectedIndex();
				if(xspressElements.getApplyToAllCheckbox().getSelection())
					for(int i=0;i<gridListEditor.getListSize();i++)
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
		
		if(readoutMode.isModeOveride()) {
			readoutMode.getReadoutMode().setVisible(false);
			resolutionGrade.getResolutionGradeCombo().setVisible(false);
			detectorElementComposite.getRegionList().setVisible(false);
			regionType.getRegionType().setVisible(false);
		}
		
		addReadoutModeListenerListener();
		
		tableViewer = gridListEditor.getTableViewer();

		tableViewer.setCellModifier(new ICellModifier() {
			@Override
			public boolean canModify(Object element, String property) {
				int col = Integer.parseInt(property);
				int selectedIndex = gridListEditor.getElementIndex(element, col, gridListEditor.getGridOrder(), gridListEditor.getColumns(), gridListEditor.getRows(), gridListEditor.getGridMap());
				updateElement(selectedIndex);
				return false;
			}
			private void updateElement(int selectedIndex) {
				selectedElement = selectedIndex;
				gridListEditor.setSelectedIndex(selectedIndex);
				tableViewer.refresh();
				int[][][] mcaData = xspressAcquire.getMcaData();
				if(mcaData!=null){
					xspressAcquire.setDetectorElementComposite(detectorElementComposite);
					xspressAcquire.setXspressElements(xspressElements);
					xspressAcquire.dataUpdate(selectedIndex, mcaData);
				}
				boolean excluded = xspressParameters.getDetectorList().get(selectedIndex).isExcluded();
				detectorElementComposite.getExcluded().setValue(excluded);
				int numberOfElements = xspressParameters.getDetectorList().size();
				for(int i=0;i<numberOfElements;i++)
					tableViewer.refresh();
				tableViewer.refresh();
			}
			
			@Override
			public Object getValue(Object element, String property) {
				return null;
			}
			@Override
			public void modify(Object item, String property, Object value) {
			}
		});
		
		xspressAcquire.setTableViewer(tableViewer);
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
		DetectorROIComposite detectorROIComposite = detectorElementComposite.getDetectorROIComposite();
		if(isRoi && detectorROIComposite==null)
			detectorElementComposite.createRegionList();
		if(detectorROIComposite!=null)
			detectorElementComposite.getDetectorROIComposite().setVisible(isRoi);
		detectorElementComposite.setWindowsEditable(!isRoi);
		updateVisibility(detectorROIComposite);
	}
	
	private String getResGradeAllowingForReadoutMode() {
		String uiReadoutMode = (String) readoutMode.getReadoutMode().getValue();
		return uiReadoutMode.equals(XspressDetector.READOUT_ROIS) ? (String) resolutionGrade.getResolutionGradeCombo().getValue() : ResGrades.NONE;
	}

	private void updateBean(int index, int start, int end){
		xspressParameters.getDetectorList().get(index).setWindow(start, end);
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
	
	protected java.awt.Color getChannelColor(int i) {
		String resGrade = getResGradeAllowingForReadoutMode();
		if (resGrade.startsWith(ResGrades.THRESHOLD)) {
			switch (i) {
			case 0:
				return Color.RED; // BAD
			case 1:
				return Color.BLUE; // GOOD
			default:
				return PlotColorUtility.getDefaultColour(i);
			}
		}
		return PlotColorUtility.getDefaultColour(i);
	}

	public void updateVisibility(Composite composite) {
		boolean isRoi = readoutMode.getReadoutMode().getValue().equals(XspressDetector.READOUT_ROIS);
		detectorElementComposite.setWindowsEditable(!isRoi);
		VerticalListEditor roi = detectorElementComposite.getRegionList();
		if(readoutMode.getReadoutMode().getSelectionIndex() == 2 && !readoutMode.isModeOveride()) {//if readout rois
			resolutionGrade.getResolutionGradeCombo().setVisible(true);
			if(roi!=null)
				roi.setVisible(true);
			regionType.getRegionType().setVisible(true);
		} 
		else {
			resolutionGrade.getResolutionGradeCombo().setVisible(false);
			if(roi!=null)
				roi.setVisible(false);
			regionType.getRegionType().setVisible(false);
		}
		sashPlotFormComposite.getLeft().layout();
		if(roi!=null){
			listEditorUI.notifySelected(roi);
			roi.setListEditorUI(listEditorUI);
		}
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
			detectorElementComposite.getStart().setValue(start);
			detectorElementComposite.getEnd().setValue(end);
		}

		@Override
		public void roiSelected(ROIEvent evt) {
			// TODO Auto-generated method stub
			
		}
	}
	
}

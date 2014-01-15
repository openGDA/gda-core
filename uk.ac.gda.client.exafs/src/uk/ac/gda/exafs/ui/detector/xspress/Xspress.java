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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPartSite;

import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.data.ScanObjectManager;
import uk.ac.gda.exafs.ui.detector.Detector;
import uk.ac.gda.exafs.ui.detector.DetectorElementComposite;
import uk.ac.gda.exafs.ui.detector.XspressROIComposite;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.components.selector.BeanSelectionEvent;
import uk.ac.gda.richbeans.components.selector.BeanSelectionListener;
import uk.ac.gda.richbeans.components.selector.ListEditor;
import uk.ac.gda.richbeans.components.selector.ListEditorUIAdapter;
import uk.ac.gda.richbeans.editors.DirtyContainer;

public class Xspress extends Detector{
	private XspressAcquire xspressAcquire;
	private Label lblRegionBins;
	private ResolutionGrade resolutionGrade;
	private ReadoutMode readoutMode;
	private RegionType regionType;
	private XspressPreferences xspressPreferences;
	private XspressElements xspressElements;
	
	public Xspress(String path, IWorkbenchPartSite site, Composite parent, XspressParameters xspressParameters, DirtyContainer dirtyContainer) {
		super("xspressConfig", site, parent, path);
		
		Composite left = sashPlotFormComposite.getLeft();
		
		Composite topComposite = new Composite(left, SWT.NONE);
		topComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		topComposite.setLayout(gridLayout_1);
		
		resolutionGrade = new ResolutionGrade(topComposite);
		
		readoutMode = new ReadoutMode(topComposite, resolutionGrade);
		
		lblRegionBins = new Label(topComposite, SWT.NONE);
		lblRegionBins.setText("Region type");
		
		regionType = new RegionType(topComposite);
		
		xspressAcquire = new XspressAcquire(left, sashPlotFormComposite, site.getShell().getDisplay(), readoutMode.getReadoutMode(), resolutionGrade.getResolutionGradeCombo(), plot, dirtyContainer, xspressParameters, counts);
		
		xspressElements = new XspressElements(left, site.getShell(), dirtyContainer, sashPlotFormComposite, xspressParameters, counts);
		
		regionSynchronizer.setDetectorElementComposite(xspressElements.getDetectorListComposite().getDetectorElementComposite());
		
		if(!ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.DETECTOR_OUTPUT_IN_OUTPUT_PARAMETERS) && !ScanObjectManager.isXESOnlyMode())
			xspressPreferences = new XspressPreferences(left);
		
		sashPlotFormComposite.setWeights(new int[] { 30, 74 });
		
		xspressElements.configureUI(xspressAcquire.getMcaData(), xspressElements.getDetectorListComposite().getDetectorList().getSelectedIndex());
		
		xspressAcquire.addAcquireListener(xspressElements.getDetectorListComposite().getDetectorList().getSelectedIndex(), xspressElements.getDetectorListComposite().getDetectorList(), xspressElements.getDetectorListComposite().getDetectorElementComposite());
		xspressElements.getDetectorListComposite().getDetectorList().addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				int[][][] mcaData = xspressAcquire.getMcaData();
				plot.plot(evt.getSelectionIndex(), mcaData, false, null);
				DetectorElementComposite detectorElementComposite = xspressElements.getDetectorListComposite().getDetectorElementComposite();
				detectorElementComposite.setTotalElementCounts(counts.getTotalElementCounts(evt.getSelectionIndex(), mcaData));
				detectorElementComposite.setTotalCounts(counts.getTotalCounts(mcaData));
				xspressElements.setAllElementsCount(counts.getTotalCounts(mcaData));
				xspressElements.setElementCount(counts.getTotalElementCounts(evt.getSelectionIndex(), mcaData));
				xspressElements.setInWindowCounts(counts.getInWindowsCounts(xspressElements.getShowIndividualElements().getValue(), (int)regionSynchronizer.getStart(), (int)regionSynchronizer.getEnd(), evt.getSelectionIndex(), mcaData));
			}
		});
		
		xspressAcquire.addLoadListener(xspressElements.getDetectorListComposite().getDetectorList(), xspressElements.getDetectorListComposite().getDetectorElementComposite(), xspressParameters.getDetectorList().size());
		
		if(readoutMode.isModeOveride()) {
			GridUtils.setVisibleAndLayout(readoutMode.getReadoutMode(), false);
			GridUtils.setVisibleAndLayout(resolutionGrade.getResGradeLabel(), false);
			GridUtils.setVisibleAndLayout(resolutionGrade.getResolutionGradeCombo(), false);
			GridUtils.setVisibleAndLayout(lblRegionBins, false);
			GridUtils.setVisibleAndLayout(regionType.getRegionType(), false);
		}
	}
	
	private String getResGradeAllowingForReadoutMode() {
		String uiReadoutMode = (String) readoutMode.getReadoutMode().getValue();
		return uiReadoutMode.equals(XspressDetector.READOUT_ROIS) ? (String) resolutionGrade.getResolutionGradeCombo().getValue() : ResGrades.NONE;
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
				GridUtils.setVisibleAndLayout(resolutionGrade.getResGradeLabel(), true);
				GridUtils.setVisibleAndLayout(resolutionGrade.getResolutionGradeCombo(), true);
				GridUtils.setVisibleAndLayout(lblRegionBins, true);
				GridUtils.setVisibleAndLayout(regionType.getRegionType(), true);
			} 
			else {
				GridUtils.setVisibleAndLayout(resolutionGrade.getResGradeLabel(), false);
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
	
}
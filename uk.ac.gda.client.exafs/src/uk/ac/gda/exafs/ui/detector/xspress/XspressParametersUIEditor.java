/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import gda.configuration.properties.LocalProperties;
import gda.device.detector.xspress.ResGrades;
import gda.device.detector.xspress.XspressDetector;

import java.awt.Color;
import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.composites.FluorescenceComposite;
import uk.ac.gda.exafs.ui.data.ScanObjectManager;
import uk.ac.gda.exafs.ui.detector.DetectorEditor;
import uk.ac.gda.exafs.ui.detector.DetectorElementComposite;
import uk.ac.gda.exafs.ui.detector.Plot;
import uk.ac.gda.exafs.ui.detector.RegionSynchronizer;
import uk.ac.gda.exafs.ui.detector.XspressROIComposite;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.selector.BeanSelectionEvent;
import uk.ac.gda.richbeans.components.selector.BeanSelectionListener;
import uk.ac.gda.richbeans.components.selector.GridListEditor;
import uk.ac.gda.richbeans.components.selector.ListEditor;
import uk.ac.gda.richbeans.components.selector.ListEditorUIAdapter;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.editors.DirtyContainer;

public class XspressParametersUIEditor extends DetectorEditor {
	private Composite parentComposite;
	private XspressAcquire xspressAcquire;
	private XspressParameters xspressParameters;
	private Label lblRegionBins;
	private ResolutionGrade resolutionGrade;
	private ReadoutMode readoutMode;
	private RegionType regionType;
	private XspressPreferences xspressPreferences;
	private XspressElements xspressElements;
	
	public XspressParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean, "xspressConfig");
		xspressParameters = (XspressParameters) editingBean;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		Composite left = sashPlotFormComposite.getLeft();
		Composite topComposite = new Composite(left, SWT.NONE);
		topComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		topComposite.setLayout(gridLayout_1);
		resolutionGrade = new ResolutionGrade(topComposite);
		readoutMode = new ReadoutMode(topComposite, resolutionGrade);
		if(readoutMode.isModeOveride()) {
			GridUtils.setVisibleAndLayout(readoutMode.getReadoutMode(), false);
			GridUtils.setVisibleAndLayout(resolutionGrade.getResGradeLabel(), false);
			GridUtils.setVisibleAndLayout(resolutionGrade.getResolutionGradeCombo(), false);
			GridUtils.setVisibleAndLayout(lblRegionBins, false);
			GridUtils.setVisibleAndLayout(regionType.getRegionType(), false);
		}
		lblRegionBins = new Label(topComposite, SWT.NONE);
		lblRegionBins.setText("Region type");
		regionType = new RegionType(topComposite);
		xspressAcquire = new XspressAcquire(left, sashPlotFormComposite, getSite().getShell().getDisplay(), readoutMode.getReadoutMode(), resolutionGrade.getResolutionGradeCombo(), plot, dirtyContainer);
		
		xspressElements = new XspressElements(left, getSite().getShell(), dirtyContainer, sashPlotFormComposite, xspressParameters, counts);
		regionSynchronizer.setDetectorElementComposite(xspressElements.getDetectorListComposite().getDetectorElementComposite());
		
		if(!ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.DETECTOR_OUTPUT_IN_OUTPUT_PARAMETERS) && !ScanObjectManager.isXESOnlyMode())
			xspressPreferences = new XspressPreferences(left);
		sashPlotFormComposite.setWeights(new int[] { 30, 74 });
		xspressAcquire.init(xspressParameters, counts, xspressElements.getShowIndividualElements());
		xspressElements.configureUI(xspressAcquire.getMcaData(), getCurrentSelectedElementIndex());
		xspressAcquire.addAcquireListener(getCurrentSelectedElementIndex(), getDetectorList(), getDetectorElementComposite());
		getDetectorList().addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				int[][][] mcaData = xspressAcquire.getMcaData();
				plot.plot(evt.getSelectionIndex(), mcaData, false, null);
				DetectorElementComposite detectorElementComposite = getDetectorElementComposite();
				detectorElementComposite.setTotalElementCounts(counts.getTotalElementCounts(evt.getSelectionIndex(), mcaData));
				detectorElementComposite.setTotalCounts(counts.getTotalCounts(mcaData));
				xspressElements.setAllElementsCount(counts.getTotalCounts(mcaData));
				xspressElements.setElementCount(counts.getTotalElementCounts(evt.getSelectionIndex(), mcaData));
				xspressElements.setInWindowCounts(counts.getInWindowsCounts(xspressElements.getShowIndividualElements().getValue(), (int)regionSynchronizer.getStart(), (int)regionSynchronizer.getEnd(), evt.getSelectionIndex(), mcaData));
			}
		});
		xspressAcquire.addLoadListener(getDetectorList(), getDetectorElementComposite(), xspressParameters.getDetectorList().size());
	}

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

	private String getResGradeAllowingForReadoutMode() {
		String uiReadoutMode = (String) getReadoutMode().getValue();
		return uiReadoutMode.equals(XspressDetector.READOUT_ROIS) ? (String) getResGrade().getValue() : ResGrades.NONE;
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

	@Override
	public void doSave(IProgressMonitor monitor) {
		//if (!(Boolean) xspressElements.getShowIndividualElements().getValue() || xspressElements.getApplyToAllLabel().getSelection())
		//	applyToAll(false);
		super.doSave(monitor);
	}

	private ListEditorUIAdapter listEditorUI = new ListEditorUIAdapter() {
		@Override
		public void notifySelected(ListEditor listEditor) {
			XspressROIComposite xspressROIComposite = (XspressROIComposite) (listEditor.getEditorUI());
			xspressROIComposite.setFitTypeVisibility();
		}
	};
	
	@Override
	public void linkUI(final boolean isPageChange) {
		GridUtils.startMultiLayout(parentComposite);
		try {
			super.linkUI(isPageChange);
			readoutMode.updateOverrideMode();
			boolean readoutRois = false;
			if(resolutionGrade.getResolutionGradeCombo().getValue().equals(XspressDetector.READOUT_ROIS))
				readoutRois = true;
			resolutionGrade.updateResModeItems(readoutRois);
			//updateRoiVisibility();
			//updateElementsVisibility(parentComposite);
			resolutionGrade.updateResGradeVisibility(parentComposite);
			listEditorUI.notifySelected(getDetectorElementComposite().getRegionList());
			getDetectorElementComposite().getRegionList().setListEditorUI(listEditorUI);
			xspressElements.updateROIAfterElementCompositeChange();
		} finally {
			GridUtils.endMultiLayout();
		}
	}

	public ComboWrapper getResGrade() {
		return resolutionGrade.getResolutionGradeCombo();
	}

	public BooleanWrapper getEditIndividualElements() {
		return xspressElements.getShowIndividualElements();
	}

	public ComboWrapper getReadoutMode() {
		return readoutMode.getReadoutMode();
	}

	@Override
	public void notifyFileSaved(File file) {
		FluorescenceComposite fluorescenceComposite = (FluorescenceComposite) BeanUI.getBeanField("fluorescenceParameters", DetectorParameters.class);
		if (fluorescenceComposite == null || fluorescenceComposite.isDisposed())
			return;
		fluorescenceComposite.getDetectorType().setValue("Germanium");
		fluorescenceComposite.getConfigFileName().setValue(file.getAbsolutePath());
	}

	public BooleanWrapper getOnlyShowFF() {
		return xspressPreferences.getOnlyShowFF();
	}

	public BooleanWrapper getShowDTRawValues() {
		return xspressPreferences.getShowDTRawValues();
	}

	public BooleanWrapper getSaveRawSpectrum() {
		return xspressPreferences.getSaveRawSpectrum();
	}

	@Override
	protected String getDataXMLName() {
		String varDir = LocalProperties.get(LocalProperties.GDA_VAR_DIR);
		return varDir + "/xspress_editor_data.xml";
	}
	
	public ComboWrapper getRegionType() {
		return regionType.getRegionType();
	}
	
	protected String getDetectorName() {
		return xspressParameters.getDetectorName();
	}
	
	public XMLCommandHandler getXMLCommandHandler() {
		return ExperimentBeanManager.INSTANCE.getXmlCommandHandler(XspressParameters.class);
	}
	
	@Override
	protected String getRichEditorTabText() {
		return "Xspress";
	}
	
	public DetectorElementComposite getDetectorElementComposite() {
		return xspressElements.getDetectorListComposite().getDetectorElementComposite();
	}
	
	protected int getCurrentSelectedElementIndex() {
		return xspressElements.getDetectorListComposite().getDetectorList().getSelectedIndex();
	}

	public GridListEditor getDetectorList() {
		return xspressElements.getDetectorListComposite().getDetectorList();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
	
}
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

package uk.ac.gda.exafs.ui.detector;

import java.text.NumberFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.richbeans.beans.IFieldWidget;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.selector.VerticalListEditor;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper.BOOLEAN_MODE;
import uk.ac.gda.richbeans.components.wrappers.LabelWrapper;
import uk.ac.gda.richbeans.components.wrappers.LabelWrapper.TEXT_TYPE;
import uk.ac.gda.richbeans.event.ValueAdapter;

public class DetectorElementComposite extends Composite {
	private BooleanWrapper excluded;
	private ScaleBox offset;
	private LabelWrapper totalCounts;
	private LabelWrapper elementTotalCounts;
	private int allElementTotalCountsValue;
	private int thisElementTotalCountsValue;
	private ScaleBox gain;
	private LabelWrapper name;
	private ScaleBox peakingTime;
	private ExpandableComposite advancedExpandableComposite;
	protected VerticalListEditor regionList;
	private ExpansionAdapter expansionListener;
	private ScaleBox windowStart, roiStart;
	private ScaleBox windowEnd, roiEnd;
	private LabelWrapper windowCounts;
	private Group windowComposite;
	private boolean windowsEditable;
	protected Composite mainComposite;
	private boolean isIndividualElements = false;
	protected DetectorROIComposite detectorROIComposite;
	protected boolean isMultipleElements;
	protected Class<? extends DetectorROI> regionClass;
	
	public DetectorElementComposite(final Composite parent, final int style, boolean isMultipleElements,
			final Class<? extends DetectorROI> regionClass) {
		super(parent, style);
		this.isMultipleElements = isMultipleElements;
		this.regionClass = regionClass;
		GridLayout gridLayout = new GridLayout();
		setLayout(gridLayout);
		mainComposite = new Composite(this, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, true);
		gridData.widthHint = 293;
		mainComposite.setLayoutData(gridData);
		gridLayout = new GridLayout();
		mainComposite.setLayout(gridLayout);
		if (isMultipleElements)
			createWindoControl();
		createRegionList();
		GridUtils.layoutFull(mainComposite);
	}

	private void createWindoControl(){
		Composite topComposite = new Composite(mainComposite, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		topComposite.setLayoutData(gridData);
		topComposite.setLayout(new GridLayout(2, false));
		
		name = new LabelWrapper(topComposite, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		name.setLayoutData(gridData);
		name.setText("Element100");
		name.setTextType(TEXT_TYPE.PLAIN_TEXT);
		
		excluded = new BooleanWrapper(topComposite, SWT.NONE);
		excluded.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		excluded.setBooleanMode(BOOLEAN_MODE.REVERSE);
		excluded.setText("Enabled");
		
		totalCounts = new LabelWrapper(topComposite, SWT.NONE);
		totalCounts.setTextType(TEXT_TYPE.PLAIN_TEXT);
		totalCounts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		totalCounts.setText("");
		elementTotalCounts = new LabelWrapper(topComposite, SWT.NONE);
		elementTotalCounts.setTextType(TEXT_TYPE.PLAIN_TEXT);
		elementTotalCounts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		elementTotalCounts.setText("");
		windowComposite = new Group(mainComposite, SWT.NONE);
		windowComposite.setText("Window");
		gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		windowComposite.setLayoutData(gridData);
		windowComposite.setLayout(new GridLayout(2, false));
		Label windowStartLabel = new Label(windowComposite, SWT.NONE);
		windowStartLabel.setText("Start");
		windowStart = new ScaleBox(windowComposite, SWT.NONE);
		windowStart.setIntegerBox(true);
		windowStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		windowStart.setButtonVisible(true);
		windowStart.setDecimalPlaces(0);
		Label windowEndLabel = new Label(windowComposite, SWT.NONE);
		windowEndLabel.setText("End");
		windowEnd = new ScaleBox(windowComposite, SWT.NONE);
		windowEnd.setIntegerBox(true);
		windowEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		windowEnd.setButtonVisible(true);
		windowEnd.setDecimalPlaces(0);
		windowEnd.setMaximum(4095);
		windowStart.setMaximum(windowEnd);
		windowEnd.setMinimum(windowStart);
		Label windowCountsLabel = new Label(windowComposite, SWT.NONE);
		windowCountsLabel.setText("In window counts");
		windowCounts = new LabelWrapper(windowComposite, SWT.NONE);
		windowCounts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		windowCounts.setDecimalPlaces(0);
	}
	
	public void createRegionList() {
		regionList = new VerticalListEditor(mainComposite, isMultipleElements ? SWT.BORDER : SWT.NONE);
		regionList.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		regionList.setEditorClass(regionClass);
		detectorROIComposite = new XspressROIComposite(regionList, SWT.NONE);
		regionList.setEditorUI(detectorROIComposite);
		detectorROIComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		regionList.setTemplateName("ROI");
		regionList.setNameField("roiName");
		regionList.setListHeight(100);
		regionList.setListWidth(270);
	}

	@Override
	public void dispose() {
		if (advancedExpandableComposite != null)
			advancedExpandableComposite.removeExpansionListener(expansionListener);
		super.dispose();
	}

	public void setWindowsEditable(final boolean isWindows) {
		this.windowsEditable = isWindows;
		if (windowComposite == null)
			return;
		GridUtils.setVisibleAndLayout(windowComposite, isWindows);
	}

	public void setMaximumRegions(final int maxRegions) {
		regionList.setMaxItems(maxRegions);
	}

	public void setMinimumRegions(final int minRegions) {
		regionList.setMinItems(minRegions);
	}

	public void addExpansionListener(IExpansionListener l) {
		if (advancedExpandableComposite != null)
			advancedExpandableComposite.addExpansionListener(l);
	}

	public void removeExpansionListener(IExpansionListener l) {
		if (advancedExpandableComposite != null)
			advancedExpandableComposite.removeExpansionListener(l);
	}

	public LabelWrapper getName() {
		return name;
	}

	public ScaleBox getOffset() {
		return offset;
	}

	public ScaleBox getGain() {
		return gain;
	}

	public BooleanWrapper getExcluded() {
		return excluded;
	}

	public ScaleBox getPeakingTime() {
		return peakingTime;
	}

	public VerticalListEditor getRegionList() {
		return regionList;
	}

	public IFieldWidget getStart() {
		if (windowsEditable)
			return getWindowStart();
		if(detectorROIComposite!=null){
			try {
				if (roiStart == null)
					roiStart = detectorROIComposite.getFieldWidgetsForDetectorElementsComposite().getRoiStart();
				return roiStart;
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	public void addStartListener(ValueAdapter v) {
		if (getWindowStart() != null)
			getWindowStart().addValueListener(v);
		if(detectorROIComposite!=null){
			if (roiStart == null)
				roiStart = detectorROIComposite.getFieldWidgetsForDetectorElementsComposite().getRoiStart();
			roiStart.addValueListener(v);
		}
	}

	public ScaleBox getEnd() {
		if (windowsEditable)
			return getWindowEnd();
		if (roiEnd == null)
			roiEnd = detectorROIComposite.getFieldWidgetsForDetectorElementsComposite().getRoiEnd();
		return roiEnd;
	}

	public void addEndListener(ValueAdapter v) {
		if (getWindowEnd() != null)
			getWindowEnd().addValueListener(v);
		if(detectorROIComposite!=null){
			if (roiEnd == null)
				roiEnd = detectorROIComposite.getFieldWidgetsForDetectorElementsComposite().getRoiEnd();
			roiEnd.addValueListener(v);
		}
	}

	public void setEndMaximum(int length) {
		if (getWindowEnd() != null)
			getWindowEnd().setMaximum(length);
		if(detectorROIComposite!=null){
			if (roiEnd == null)
				roiEnd = detectorROIComposite.getFieldWidgetsForDetectorElementsComposite().getRoiEnd();
			if (roiEnd != null)
				roiEnd.setMaximum(length);
		}
	}

	public void setStartEnabled(boolean isEnabled) {
		if (getWindowStart() != null)
			getWindowStart().setEnabled(isEnabled);
		if(detectorROIComposite!=null){
			if (roiStart == null)
				roiStart = detectorROIComposite.getFieldWidgetsForDetectorElementsComposite().getRoiStart();
			roiStart.setEnabled(isEnabled);
		}
	}

	public void setEndEnabled(boolean isEnabled) {
		if (getWindowEnd() != null)
			getWindowEnd().setEnabled(isEnabled);
		if(detectorROIComposite!=null){
			if (roiEnd == null)
				roiEnd = detectorROIComposite.getFieldWidgetsForDetectorElementsComposite().getRoiEnd();
			roiEnd.setEnabled(isEnabled);
		}
	}
	
	public IFieldWidget getCount() {
		if (windowsEditable)
			return getWindowCounts();
		return detectorROIComposite.getFieldWidgetsForDetectorElementsComposite().getCounts();
	}

	public ScaleBox getWindowEnd() {
		return windowEnd;
	}

	public ScaleBox getWindowStart() {
		return windowStart;
	}

	public LabelWrapper getWindowCounts() {
		return windowCounts;
	}

	private void updateTotalCountsDisplay() {
		if(elementTotalCounts!=null)
			elementTotalCounts.setValue("Element Total Counts " + NumberFormat.getInstance().format(thisElementTotalCountsValue));
		if(totalCounts!=null)
			totalCounts.setValue("All Element Total Counts " + NumberFormat.getInstance().format(allElementTotalCountsValue));
	}

	public void setTotalElementCounts(int total) {
		thisElementTotalCountsValue = total;
		updateTotalCountsDisplay();
	}

	public void setTotalCounts(int totalCounts) {
		allElementTotalCountsValue = totalCounts;
		updateTotalCountsDisplay();
	}

	public void setIndividualElements(boolean selection) {
		this.isIndividualElements = selection;
		updateTotalCountsDisplay();
	}

	public boolean isIndividualElements() {
		return isIndividualElements;
	}

	public DetectorROIComposite getDetectorROIComposite() {
		return detectorROIComposite;
	}
	
}
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
import org.eclipse.ui.forms.events.ExpansionEvent;
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
	private Double allElementTotalCountsValue;
	private Double thisElementTotalCountsValue;
	private ScaleBox gain;
	private LabelWrapper name;
	private ScaleBox peakingTime;
	private ExpandableComposite advancedExpandableComposite;
	private VerticalListEditor regionList;
	private ExpansionAdapter expansionListener;
	private ScaleBox windowStart, roiStart;
	private ScaleBox windowEnd, roiEnd;
	private LabelWrapper windowCounts;
	private Group windowComposite;
	private boolean isWindows;
	private Composite mainComposite;
	private boolean isIndividualElements = false;
	private DetectorROIComposite detectorROIComposite;

	public DetectorElementComposite(final Composite parent, final int style, final boolean isMultipleElements,
			final Class<? extends DetectorROI> regionClass, final IDetectorROICompositeFactory regionEditorFactory,
			Boolean showAdvanced) {

		super(parent, style);

		GridLayout gridLayout = new GridLayout();
		setLayout(gridLayout);

		mainComposite = new Composite(this, SWT.NONE);
		{
			GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, true);
			gridData.widthHint = 293;
			mainComposite.setLayoutData(gridData);
		}
		gridLayout = new GridLayout();
		mainComposite.setLayout(gridLayout);

		if (isMultipleElements) {
			Composite topComposite = new Composite(mainComposite, SWT.NONE);
			{
				GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
				topComposite.setLayoutData(gridData);
			}
			topComposite.setLayout(new GridLayout(2, false));

			name = new LabelWrapper(topComposite, SWT.NONE);
			{
				GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
				name.setLayoutData(gridData);
			}
			name.setText("Element100");
			name.setTextType(TEXT_TYPE.PLAIN_TEXT);

			excluded = new BooleanWrapper(topComposite, SWT.NONE);
			excluded.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
			excluded.setBooleanMode(BOOLEAN_MODE.REVERSE);
			excluded.setText("Enabled");

			this.totalCounts = new LabelWrapper(topComposite, SWT.NONE);
			totalCounts.setTextType(TEXT_TYPE.PLAIN_TEXT);
			totalCounts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			totalCounts.setText("");

			this.elementTotalCounts = new LabelWrapper(topComposite, SWT.NONE);
			elementTotalCounts.setTextType(TEXT_TYPE.PLAIN_TEXT);
			elementTotalCounts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			elementTotalCounts.setText("");

			this.windowComposite = new Group(mainComposite, SWT.NONE);
			windowComposite.setText("Window");
			{
				GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
				windowComposite.setLayoutData(gridData);
			}
			windowComposite.setLayout(new GridLayout(2, false));

			final Label windowStartLabel = new Label(windowComposite, SWT.NONE);
			windowStartLabel.setText("Start");

			windowStart = new ScaleBox(windowComposite, SWT.NONE);
			windowStart.setIntegerBox(true);
			windowStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			windowStart.setButtonVisible(true);
			windowStart.setDecimalPlaces(0);

			final Label windowEndLabel = new Label(windowComposite, SWT.NONE);
			windowEndLabel.setText("End");

			windowEnd = new ScaleBox(windowComposite, SWT.NONE);
			windowEnd.setIntegerBox(true);
			windowEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			windowEnd.setButtonVisible(true);
			windowEnd.setDecimalPlaces(0);
			windowEnd.setMaximum(4095);
			windowStart.setMaximum(windowEnd);
			windowEnd.setMinimum(windowStart);

			final Label windowCountsLabel = new Label(windowComposite, SWT.NONE);
			windowCountsLabel.setText("In window counts");

			windowCounts = new LabelWrapper(windowComposite, SWT.NONE);
			windowCounts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			windowCounts.setDecimalPlaces(0);
		}

		this.regionList = new VerticalListEditor(mainComposite, isMultipleElements ? SWT.BORDER : SWT.NONE);
		regionList.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		regionList.setEditorClass(regionClass);

		detectorROIComposite = regionEditorFactory.createDetectorROIComposite(regionList, SWT.NONE);
		regionList.setEditorUI(detectorROIComposite);

		detectorROIComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		regionList.setTemplateName("ROI");
		regionList.setNameField("roiName");
		regionList.setListHeight(100);

		if (showAdvanced) {

			this.advancedExpandableComposite = new ExpandableComposite(this, SWT.NONE);
			{
				GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
				gridData.minimumWidth = 130;
				advancedExpandableComposite.setLayoutData(gridData);
			}
			advancedExpandableComposite.setText("Advanced");

			final Composite advanced = new Composite(advancedExpandableComposite, SWT.BORDER);
			advanced.setLayout(new GridLayout(2, false));

			final Label gainLabel = new Label(advanced, SWT.NONE);
			gainLabel.setText("Gain");

			gain = new ScaleBox(advanced, SWT.NONE);
			{
				GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
				gridData.minimumWidth = 130;
				gain.setLayoutData(gridData);
			}
			gain.setUnit("eV");
			gain.setMinimum(1);
			gain.setMaximum(1000000.0);
			gain.setDecimalPlaces(0);

			Label lblPeakingTime = new Label(advanced, SWT.NONE);
			lblPeakingTime.setText("Peaking Time");

			peakingTime = new ScaleBox(advanced, SWT.NONE);
			{
				GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
				gridData.minimumWidth = 130;
				peakingTime.setLayoutData(gridData);
			}
			peakingTime.setUnit("\u03BCs");

			final Label offSetLabel = new Label(advanced, SWT.NONE);
			offSetLabel.setText("Offset");

			offset = new ScaleBox(advanced, SWT.NONE);
			{
				GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
				gridData.minimumWidth = 130;
				offset.setLayoutData(gridData);
			}
			offset.setMinimum(-100);
			offset.setMaximum(100);
			offset.setDecimalPlaces(0);

			advancedExpandableComposite.setClient(advanced);
			this.expansionListener = new ExpansionAdapter() {
				@Override
				public void expansionStateChanged(ExpansionEvent e) {
					GridUtils.layoutFull(advanced);
				}
			};
			advancedExpandableComposite.addExpansionListener(expansionListener);
		}
		
		GridUtils.layoutFull(mainComposite);
	}

	@Override
	public void dispose() {
		if (advancedExpandableComposite != null)
			advancedExpandableComposite.removeExpansionListener(expansionListener);
		super.dispose();
	}

	public void setWindowsEditable(final boolean isWindows) {
		this.isWindows = isWindows;
		if (windowComposite == null)
			return;
		// need to display windows tools all the time
		GridUtils.setVisibleAndLayout(windowComposite, isWindows);
	}

	public void setMaximumRegions(final int maxRegions) {
		regionList.setMaxItems(maxRegions);
	}

	public void setMinimumRegions(final int minRegions) {
		regionList.setMinItems(minRegions);
	}

	/**
	 * Notified when the advanced section is expanded.
	 * 
	 * @param l
	 */
	public void addExpansionListener(IExpansionListener l) {
		if (advancedExpandableComposite != null)
			advancedExpandableComposite.addExpansionListener(l);
	}

	public void removeExpansionListener(IExpansionListener l) {
		if (advancedExpandableComposite != null)
			advancedExpandableComposite.removeExpansionListener(l);
	}

	// getName warning due to private definition in super type
	@SuppressWarnings("all")
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

	/**
	 * @return Returns the regions.
	 */
	public VerticalListEditor getRegionList() {
		return regionList;
	}

	public IFieldWidget getStart() {
		if (isWindows)
			return getWindowStart();
		try {
			if (roiStart == null)
				roiStart = detectorROIComposite.getFieldWidgetsForDetectorElementsComposite().getRoiStart();
			return roiStart;
		} catch (Exception e) {
			return null;
		}
	}

	public void addStartListener(ValueAdapter v) {
		if (getWindowStart() != null)
			getWindowStart().addValueListener(v);
		if (roiStart == null)
			roiStart = detectorROIComposite.getFieldWidgetsForDetectorElementsComposite().getRoiStart();
		roiStart.addValueListener(v);
	}

	public ScaleBox getEnd() {
		if (isWindows)
			return getWindowEnd();
		if (roiEnd == null)
			roiEnd = detectorROIComposite.getFieldWidgetsForDetectorElementsComposite().getRoiEnd();
		return roiEnd;

	}

	public void addEndListener(ValueAdapter v) {
		if (getWindowEnd() != null)
			getWindowEnd().addValueListener(v);
		if (roiEnd == null)
			roiEnd = detectorROIComposite.getFieldWidgetsForDetectorElementsComposite().getRoiEnd();
		roiEnd.addValueListener(v);
	}

	public void setEndMaximum(int length) {
		if (getWindowEnd() != null)
			getWindowEnd().setMaximum(length);
		if (roiEnd == null)
			roiEnd = detectorROIComposite.getFieldWidgetsForDetectorElementsComposite().getRoiEnd();
		if (roiEnd != null)
			roiEnd.setMaximum(length);
	}

	public void setStartEnabled(boolean isEnabled) {
		if (getWindowStart() != null)
			getWindowStart().setEnabled(isEnabled);
		if (roiStart == null)
			roiStart = detectorROIComposite.getFieldWidgetsForDetectorElementsComposite().getRoiStart();
		roiStart.setEnabled(isEnabled);
	}

	public void setEndEnabled(boolean isEnabled) {
		if (getWindowEnd() != null)
			getWindowEnd().setEnabled(isEnabled);
		if (roiEnd == null)
			roiEnd = detectorROIComposite.getFieldWidgetsForDetectorElementsComposite().getRoiEnd();
		roiEnd.setEnabled(isEnabled);
	}

	public IFieldWidget getCount() {
		if (isWindows)
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
		// if too early in lifecycle of composite
		if (elementTotalCounts == null || totalCounts == null) {
			return;
		}

		if (isIndividualElements && thisElementTotalCountsValue != null) {
			elementTotalCounts.setValue("Element Total Counts "
					+ NumberFormat.getInstance().format(thisElementTotalCountsValue));
		} else {
			elementTotalCounts.setValue("									");
		}

		if (allElementTotalCountsValue != null) {
			totalCounts.setValue("All Element Total Counts "
					+ NumberFormat.getInstance().format(allElementTotalCountsValue));
		} else {
			totalCounts.setValue("");
		}

		GridUtils.layoutFull(mainComposite);
	}

	public void setTotalElementCounts(final Double total) {
		if (Double.isNaN(total))
			return;
		if (Double.isInfinite(total))
			return;
		if (totalCounts == null)
			return;
		if (isIndividualElements) {
			thisElementTotalCountsValue = total;
		}
		updateTotalCountsDisplay();
	}

	public void setTotalCounts(final Double total) {
		if (Double.isNaN(total))
			return;
		if (Double.isInfinite(total))
			return;
		if (totalCounts == null)
			return;
		allElementTotalCountsValue = total;
		updateTotalCountsDisplay();
	}

	public void setIndividualElements(boolean selection) {
		this.isIndividualElements = selection;
		updateTotalCountsDisplay();
	}

	public boolean isIndividualElements() {
		return isIndividualElements;
	}
}

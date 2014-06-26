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

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.gda.beans.xspress.DetectorElement;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.beans.xspress.XspressROI;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.exafs.ui.detectorviews.Counts;
import uk.ac.gda.exafs.ui.detectorviews.DetectorElementComposite;
import uk.ac.gda.exafs.ui.detectorviews.Elements;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;

public class XspressElements extends Elements{
	private static final Logger logger = LoggerFactory.getLogger(XspressElements.class);
	private BooleanWrapper showIndividualElements;
	private Button applyToAllButton;
	private Button applyToAllCheckbox;
	private ValueListener detectorElementCompositeValueListener;
	private Composite middleComposite;
	private Group detectorElementsGroup;
	private int allElementsCount;
	private int elementCount;
	private int inWindowCounts;
	protected DetectorElementComposite detectorElementComposite;
	
	public XspressElements(final Composite parent, Shell shell, SashFormPlotComposite sashPlotFormComposite, List<DetectorElement> detectorList, final Counts counts, boolean showRoi, XspressParameters xspressParameters) {
		super(shell, sashPlotFormComposite, counts);
		Composite grid = new Composite(parent, SWT.BORDER);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(grid);
		
		showIndividualElements = new BooleanWrapper(grid, SWT.NONE);
		showIndividualElements.setText("Show individual elements");
		showIndividualElements.setEnabled(false);
		//addShowIndividualElementsListener();
		
		Composite middleComposite = new Composite(grid, SWT.BORDER);
		middleComposite.setLayout(new GridLayout(2, false));
		
		applyToAllCheckbox = new Button(middleComposite, SWT.CHECK);
		applyToAllCheckbox.setText("Apply Changes To All Elements ");
		applyToAllCheckbox.setEnabled(true);
		applyToAllCheckbox.setSelection(true);
		applyToAllCheckbox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (applyToAllCheckbox.getSelection()) {
					if (applyToAll(true)) {
						if (detectorElementCompositeValueListener == null)
							createApplyToAllObserver();
					} 
					else
						applyToAllCheckbox.setSelection(false);
				}
				applyToAllButton.setEnabled(!applyToAllCheckbox.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		applyToAllButton = new Button(middleComposite, SWT.NONE);
		applyToAllButton.setEnabled(false);
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gridData.widthHint = 90;
		gridData.minimumWidth = 90;
		applyToAllButton.setLayoutData(gridData);
		applyToAllButton.setText("Apply now");
		applyToAllButton.setToolTipText("Apply current detector regions of interest to all other detector elements.");
		applyToAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				applyToAll(true);
			}
		});
		
		detectorElementsGroup = new Group(grid, SWT.BORDER);
		detectorElementsGroup.setText("Detector Elements");
		GridLayoutFactory.fillDefaults().applyTo(detectorElementsGroup);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(detectorElementsGroup);
		
		try {
			createDetectorList(detectorElementsGroup, DetectorElement.class, detectorList.size(), XspressROI.class);
			NewXspressParametersUIHelper.INSTANCE.setDetectorListGridOrder(detectorListComposite.getDetectorList());
			if(showRoi){
				detectorListComposite.getDetectorElementComposite().setMinimumRegions(NewXspressParametersUIHelper.INSTANCE.getMinimumRegions());
				detectorListComposite.getDetectorElementComposite().setMaximumRegions(NewXspressParametersUIHelper.INSTANCE.getMaximumRegions());
			}
		} catch (Exception e1) {
			logger.error("Cannot create region editor.", e1);
		}
		
		createApplyToAllObserver();
	}
	
	public void setAllElementsCount(int allElementsCount) {
		this.allElementsCount = allElementsCount;
	}

	public void setElementCount(int elementCount) {
		this.elementCount = elementCount;
	}

	public void setInWindowCounts(int inWindowCounts) {
		this.inWindowCounts = inWindowCounts;
	}
	
	public void updateWindow(int start, int end, int[][][] mcaData){
		detectorElementComposite = getDetectorListComposite().getDetectorElementComposite();
		detectorElementComposite.getStart().setValue(start);
		detectorElementComposite.getEnd().setValue(end);
		int selectedIndex = getDetectorListComposite().getDetectorList().getSelectedIndex();
		int inWindowsCounts = counts.getInWindowsCounts(getShowIndividualElements().getValue(), start, end, selectedIndex, mcaData);
		setInWindowCounts(inWindowsCounts);
		getDetectorListComposite().getDetectorElementComposite().getCount().setValue(inWindowsCounts);
	}
	
	public void addShowIndividualElementsListener(){
		showIndividualElements.addValueListener(new ValueAdapter("editIndividualElements") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				if ((Boolean) e.getValue() == false)
					if (!applyToAll(true)) {
						showIndividualElements.setValue(true);
						return;
					}
				
				//updateElementsVisibility();
				detectorListComposite.getDetectorElementComposite().setTotalCounts(allElementsCount);
				detectorListComposite.getDetectorElementComposite().setTotalElementCounts(elementCount);
				detectorListComposite.getDetectorElementComposite().getCount().setValue(inWindowCounts);
			}
		});
	}
	
	protected void createApplyToAllObserver() {
		detectorElementCompositeValueListener = new ValueListener() {
			
			@Override
			public void valueChangePerformed(ValueEvent e) {
				if (applyToAllCheckbox.getSelection())
					applyToAll(false);
			}
			
			@Override
			public String getValueListenerName() {
				return null;
			}
		};
		DetectorElementComposite detectorElementComposite = detectorListComposite.getDetectorElementComposite();
		detectorElementComposite.getWindowStart().addValueListener(detectorElementCompositeValueListener);
		detectorElementComposite.getWindowEnd().addValueListener(detectorElementCompositeValueListener);
		if(detectorElementComposite.getRegionList()!=null)
			detectorElementComposite.getRegionList().addValueListener(detectorElementCompositeValueListener);
	}

	protected void updateElementsVisibility() {
		boolean currentEditIndividual = getShowIndividualElements().getValue();
		if (currentEditIndividual)
			detectorElementsGroup.setText("Detector Elements");
		else
			detectorElementsGroup.setText("All Elements");
		GridUtils.setVisibleAndLayout(middleComposite, currentEditIndividual);
		GridUtils.setVisibleAndLayout(applyToAllCheckbox, currentEditIndividual);
		GridUtils.setVisibleAndLayout(applyToAllButton, currentEditIndividual);
		detectorListComposite.getDetectorElementComposite().setIndividualElements(currentEditIndividual);
		detectorListComposite.getDetectorList().setListVisible(currentEditIndividual);
		autoApplyToAll(!currentEditIndividual);
	}
	
	public BooleanWrapper getShowIndividualElements() {
		return showIndividualElements;
	}

	public Button getApplyToAllCheckbox() {
		return applyToAllCheckbox;
	}

}

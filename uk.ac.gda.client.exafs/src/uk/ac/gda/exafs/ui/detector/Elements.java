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

package uk.ac.gda.exafs.ui.detector;

import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.exafs.IDetectorElement;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.selector.VerticalListEditor;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;

public class Elements {	
	private static final Logger logger = LoggerFactory.getLogger(Elements.class);
	private ValueListener autoApplyToAllListener;
	private Shell shell;
	protected DetectorListComposite detectorListComposite;
	private ExpansionAdapter expansionListener;
	private DirtyContainer dirtyContainer;
	protected SashFormPlotComposite sashPlotFormComposite;
	private volatile Boolean updatingAfterROIDrag = null;
	public Counts counts;
	
	public Elements(Shell shell, DirtyContainer dirtyContainer, SashFormPlotComposite sashPlotFormComposite, Counts counts) {
		this.shell = shell;
		this.dirtyContainer = dirtyContainer;
		this.sashPlotFormComposite = sashPlotFormComposite;
		this.counts = counts;
	}
	
	protected boolean applyToAll(boolean showMessage) {
		if (showMessage) {
			if (!MessageDialog.openConfirm(shell, "Confirm Apply To All",
			"Do you want to apply currently selected elements regions of interest to all detecors?\n\nThis will write new regions for the elements automatically."))
				return false;
		}
		
		int currentIndex = detectorListComposite.getDetectorList().getSelectedIndex();

		// Uses bean utils to clone the region list and reflection to send it
		// to the other elements.
		Object startWindow = detectorListComposite.getDetectorElementComposite().getWindowStart() != null ? detectorListComposite.getDetectorElementComposite().getWindowStart().getValue() : null;
		Object endWindow = detectorListComposite.getDetectorElementComposite().getWindowEnd() != null ? detectorListComposite.getDetectorElementComposite().getWindowEnd().getValue() : null;
		List<?> regions = (List<?>) detectorListComposite.getDetectorElementComposite().getRegionList().getValue();
		List<?> elements = (List<?>) detectorListComposite.getDetectorList().getValue();
		int index = -1;
		try {
			for (Object element : elements) {
				++index;
				if (index == currentIndex)
					continue;
				final List<?> regionClone = BeanUI.cloneBeans(regions);
				final Method setRegionList = element.getClass().getMethod("setRegionList", java.util.List.class);
				setRegionList.invoke(element, regionClone);
				// If there are a window start and end, set them
				try {
					BeansFactory.setBeanValue(element, "windowStart", startWindow);
					BeansFactory.setBeanValue(element, "windowEnd", endWindow);
				} catch (IllegalArgumentException ignored) {
					// The bean may not have windowStart and windowEnd to synchronize
				}
			}
		} catch (Exception e1) {
			logger.error("Error apply current detector regions to all detectors.", e1);
		}
		this.dirtyContainer.setDirty(true);
		return true;
	}
	
	protected void autoApplyToAll(boolean on) {
		if (autoApplyToAllListener == null) {
			autoApplyToAllListener = new ValueAdapter("autoApplyToAllListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					applyToAll(false);
				}
			};
		}

		ScaleBox windowStart = detectorListComposite.getDetectorElementComposite().getWindowStart();
		ScaleBox windowEnd = detectorListComposite.getDetectorElementComposite().getWindowEnd();
		VerticalListEditor regionList = detectorListComposite.getDetectorElementComposite().getRegionList();

		if (on) {
			if (windowStart != null)
				windowStart.addValueListener(autoApplyToAllListener);
			if (windowEnd != null)
				windowEnd.addValueListener(autoApplyToAllListener);
			if (regionList != null)
				regionList.addValueListener(autoApplyToAllListener);
			if (detectorListComposite.getDetectorList() != null)
				detectorListComposite.getDetectorList().addValueListener(autoApplyToAllListener);
		} 
		else {
			if (windowStart != null)
				windowStart.removeValueListener(autoApplyToAllListener);
			if (windowEnd != null)
				windowEnd.removeValueListener(autoApplyToAllListener);
			if (regionList != null)
				regionList.removeValueListener(autoApplyToAllListener);
			if (detectorListComposite.getDetectorList() != null)
				detectorListComposite.getDetectorList().removeValueListener(autoApplyToAllListener);
		}
	}
	
	protected DetectorListComposite createDetectorList(final Composite parent,
	final Class<? extends IDetectorElement> editorClass, final int elementListSize,
	final Class<? extends DetectorROI> regionClass, final IDetectorROICompositeFactory regionEditorFactory,
	final Boolean showAdvanced) {
		
		detectorListComposite = new DetectorListComposite(parent, editorClass, elementListSize, regionClass, regionEditorFactory, showAdvanced);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(detectorListComposite);
		expansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				sashPlotFormComposite.getLeftScroll().setMinSize(sashPlotFormComposite.getLeft().computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		};
		
		expansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				sashPlotFormComposite.getLeftScroll().setMinSize(sashPlotFormComposite.getLeft().computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		};
		detectorListComposite.addExpansionListener(expansionListener);

		ExafsActivator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().compareTo(ExafsPreferenceConstants.DETECTOR_OVERLAY_ENABLED) == 0)
					setRegionEditableFromPreference();
			}
		});
		setRegionEditableFromPreference();
		return detectorListComposite;
	}
	
	private void setRegionEditableFromPreference(){
		sashPlotFormComposite.getRegionOnDisplay().setMobile(ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.DETECTOR_OVERLAY_ENABLED));
	}

	public DetectorListComposite getDetectorListComposite() {
		return detectorListComposite;
	}
	
	public void configureUI(final int[][][] detectorData, final int selectedElementIndex) {
		sashPlotFormComposite.setXAxisLabel("Channel Number");
		sashPlotFormComposite.setYAxisLabel("Counts");
		sashPlotFormComposite.computeSizes();
		try {
			detectorListComposite.getDetectorElementComposite().addStartListener(new ValueAdapter("windowStartListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					updateUIAfterDetectorElementCompositeChange(detectorData, selectedElementIndex);
				}
			});
			detectorListComposite.getDetectorElementComposite().addEndListener(new ValueAdapter("windowEndListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					updateUIAfterDetectorElementCompositeChange(detectorData, selectedElementIndex);
				}
			});
		} catch (Exception ne) {
			logger.error("Cannot add listeners", ne);
		}
	}
	
	private void updateUIAfterDetectorElementCompositeChange(int[][][] detectorData, int selectedElementIndex) {
		if(detectorData!=null)
			if(detectorData.length!=0){
				updatingAfterROIDrag = false;
				detectorListComposite.getDetectorElementComposite().setTotalCounts(counts.getTotalCounts(detectorData));
				detectorListComposite.getDetectorElementComposite().setTotalElementCounts(counts.getTotalElementCounts(selectedElementIndex, detectorData));
				updateROIAfterElementCompositeChange();
			}
	}
	public void updateROIAfterElementCompositeChange() {
		double roiStart = ((Number) detectorListComposite.getDetectorElementComposite().getStart().getValue()).doubleValue();
		double roiEnd = ((Number) detectorListComposite.getDetectorElementComposite().getEnd().getValue()).doubleValue();
		sashPlotFormComposite.getRegionOnDisplay().setROI(new RectangularROI(roiStart, 0, roiEnd - roiStart, 0, 0));
		sashPlotFormComposite.getRegionOnDisplay().repaint();
	}
}

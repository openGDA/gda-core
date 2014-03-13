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

package uk.ac.gda.exafs.ui.detector.vortex;

import gda.device.Timer;
import gda.device.XmapDetector;

import java.util.List;

import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPartSite;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.detector.Counts;
import uk.ac.gda.exafs.ui.detector.Detector;
import uk.ac.gda.exafs.ui.detector.DetectorElementComposite;
import uk.ac.gda.exafs.ui.detector.DetectorListComposite;
import uk.ac.gda.exafs.ui.detector.Plot;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.components.selector.GridListEditor;

public class Vortex extends Detector{
	public Label acquireFileLabel;
	private VortexAcquire vortexAcquire;
	private VortexElements vortexElements;
	private RegionSynchronizer regionSynchronizer;
	private TableViewer tableViewer;
	private GridListEditor gridListEditor;
	private DetectorElementComposite detectorElementComposite;
	
	public Vortex(String path, IWorkbenchPartSite site, Composite parent, List<DetectorElement> detectorList, gda.device.Detector xmapDetector, Timer tfg) {
		super("vortexConfig", site, parent, path);
		regionSynchronizer = new RegionSynchronizer();
		try {
			sashPlotFormComposite = new SashFormPlotComposite(parent, site.getPart(), regionSynchronizer, createUpLoadAction(path));
		} catch (Exception e) {
		}
		sashPlotFormComposite.getPlottingSystem().setRescale(true);
		plot = new Plot(sashPlotFormComposite);
		sashPlotFormComposite.setWeights(new int[] { 35, 74 });
		Composite left = sashPlotFormComposite.getLeft();
		vortexAcquire = new VortexAcquire(sashPlotFormComposite, xmapDetector, tfg, site.getShell().getDisplay(), plot, new Counts());
		vortexAcquire.createAcquire(parent, left);
		vortexElements = new VortexElements(site.getShell(), sashPlotFormComposite, counts);
		vortexElements.createROI(left, detectorList);
		DetectorListComposite detectorListComposite = vortexElements.getDetectorListComposite();
		detectorElementComposite = vortexElements.getDetectorListComposite().getDetectorElementComposite();
		gridListEditor = detectorListComposite.getDetectorList();
		int selectedIndex = gridListEditor.getSelectedIndex();
		vortexElements.configureUI(vortexAcquire.getMcaData(), selectedIndex);
		if (!ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.DETECTOR_OUTPUT_IN_OUTPUT_PARAMETERS))
			vortexElements.addOutputPreferences(left);
		vortexAcquire.addAcquireListener(gridListEditor, detectorElementComposite);
		vortexAcquire.addLoadListener(gridListEditor, detectorElementComposite, detectorList);
		tableViewer = gridListEditor.getTableViewer();
		tableViewer.setCellModifier(new ICellModifier() {
			@Override
			public boolean canModify(Object element, String property) {
				final int col = Integer.parseInt(property);
				int selectedIndex = gridListEditor.getElementIndex(element, col, gridListEditor.getGridOrder(), gridListEditor.getColumns(), gridListEditor.getRows(), gridListEditor.getGridMap());
				gridListEditor.setSelectedIndex(selectedIndex);
				tableViewer.refresh();
				int[][][] mcaData = vortexAcquire.getMcaData();
				if(mcaData!=null){
					plot.plot(selectedIndex,mcaData, false, null);
					detectorElementComposite.setTotalElementCounts(counts.getTotalElementCounts(selectedIndex, mcaData));
					detectorElementComposite.setTotalCounts(counts.getTotalCounts(mcaData));
					vortexElements.configureUI(vortexAcquire.getMcaData(), selectedIndex);
				}
				return false;
			}
			@Override
			public Object getValue(Object element, String property) {
				return null;
			}
			@Override
			public void modify(Object item, String property, Object value) {
			}
		});
	}

	public VortexAcquire getVortexAcquire() {
		return vortexAcquire;
	}

	public VortexElements getVortexElements() {
		return vortexElements;
	}

	public Label getAcquireFileLabel() {
		return acquireFileLabel;
	}
	
	public class RegionSynchronizer implements IROIListener {
		private double start;
		private double end;
		
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
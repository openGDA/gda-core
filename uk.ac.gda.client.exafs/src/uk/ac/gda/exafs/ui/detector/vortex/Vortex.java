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
import gda.factory.Finder;

import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPartSite;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.detector.Counts;
import uk.ac.gda.exafs.ui.detector.Detector;
import uk.ac.gda.exafs.ui.detector.DetectorElementComposite;
import uk.ac.gda.exafs.ui.detector.Plot;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.components.selector.BeanSelectionEvent;
import uk.ac.gda.richbeans.components.selector.BeanSelectionListener;
import uk.ac.gda.richbeans.editors.DirtyContainer;

public class Vortex extends Detector{
	public Label acquireFileLabel;
	private VortexAcquire vortexAcquire;
	private XmapDetector xmapDetector;
	private VortexElements vortexElements;
	private RegionSynchronizer regionSynchronizer;
	
	public Vortex(String path, IWorkbenchPartSite site, Composite parent, VortexParameters vortexParameters, DirtyContainer dirtyContainer) {
		super("vortexConfig", site, parent, path);
		
		regionSynchronizer = new RegionSynchronizer(counts);
		try {
			sashPlotFormComposite = new SashFormPlotComposite(parent, site.getPart(), regionSynchronizer, createUpLoadAction(path));
		} catch (Exception e) {
		}
		sashPlotFormComposite.getPlottingSystem().setRescale(true);
		plot = new Plot(sashPlotFormComposite);
		
		sashPlotFormComposite.setWeights(new int[] { 35, 74 });
		
		Composite left = sashPlotFormComposite.getLeft();
		
		String detectorName = vortexParameters.getDetectorName();
		
		xmapDetector = (XmapDetector) Finder.getInstance().find(detectorName);
		
		String tfgName = vortexParameters.getTfgName();
		Timer tfg = (Timer) Finder.getInstance().find(tfgName);
		
		vortexAcquire = new VortexAcquire(sashPlotFormComposite, xmapDetector, tfg, site.getShell().getDisplay(), plot, new Counts(), dirtyContainer);
		vortexAcquire.createAcquire(parent, left);
		
		vortexElements = new VortexElements(site.getShell(), dirtyContainer, sashPlotFormComposite, vortexParameters, counts);
		
		vortexElements.createROI(left);
		vortexElements.configureUI(vortexAcquire.getMcaData(), vortexElements.getDetectorListComposite().getDetectorList().getSelectedIndex());
		
		if (!ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.DETECTOR_OUTPUT_IN_OUTPUT_PARAMETERS))
			vortexElements.addOutputPreferences(left);
		
		vortexAcquire.addAcquireListener(vortexElements.getDetectorListComposite().getDetectorList().getSelectedIndex(), vortexElements.getDetectorListComposite().getDetectorList(), vortexElements.getDetectorListComposite().getDetectorElementComposite());
		vortexAcquire.addLoadListener(vortexParameters, vortexElements.getDetectorListComposite().getDetectorList(), vortexElements.getDetectorListComposite().getDetectorElementComposite());
		
		vortexElements.getDetectorListComposite().getDetectorList().addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				int[][][] mcaData = vortexAcquire.getMcaData();
				plot.plot(evt.getSelectionIndex(),mcaData, false, null);
				DetectorElementComposite detectorElementComposite = vortexElements.getDetectorListComposite().getDetectorElementComposite();
				detectorElementComposite.setTotalElementCounts(counts.getTotalElementCounts(evt.getSelectionIndex(), mcaData));
				detectorElementComposite.setTotalCounts(counts.getTotalCounts(mcaData));
				vortexElements.configureUI(vortexAcquire.getMcaData(), evt.getSelectionIndex());
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
		protected Counts counts;
		private double start;
		private double end;
		
		public RegionSynchronizer(Counts counts){
			this.counts = counts;
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
			// TODO Auto-generated method stub
			int start = (int)((RectangularROI) sashPlotFormComposite.getRegionOnDisplay().getROI()).getPoint()[0];
			int end = (int)((RectangularROI) sashPlotFormComposite.getRegionOnDisplay().getROI()).getEndPoint()[0];
			DetectorElementComposite detectorElementComposite = vortexElements.getDetectorListComposite().getDetectorElementComposite();
			detectorElementComposite.getStart().setValue(start);
			detectorElementComposite.getEnd().setValue(end);
		}

		@Override
		public void roiSelected(ROIEvent evt) {
			// TODO Auto-generated method stub
			
		}
	}

}
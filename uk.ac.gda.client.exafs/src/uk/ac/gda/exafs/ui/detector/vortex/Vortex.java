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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPartSite;

import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.detector.Counts;
import uk.ac.gda.exafs.ui.detector.Detector;
import uk.ac.gda.exafs.ui.detector.DetectorElementComposite;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.components.selector.BeanSelectionEvent;
import uk.ac.gda.richbeans.components.selector.BeanSelectionListener;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.editors.DirtyContainer;

public class Vortex extends Detector{
	public Label acquireFileLabel;
	private VortexParameters vortexParameters;
	private ComboWrapper countType;
	private VortexAcquire vortexAcquire;
	private XmapDetector xmapDetector;
	private VortexElements vortexElements;
	
	public Vortex(String path, IWorkbenchPartSite site, Composite parent, VortexParameters vortexParameters, DirtyContainer dirtyContainer) {
		super("vortexConfig", site, parent, path);
		
		Composite left = sashPlotFormComposite.getLeft();
		
		String detectorName = vortexParameters.getDetectorName();
		
		xmapDetector = (XmapDetector) Finder.getInstance().find(detectorName);
		
		String tfgName = vortexParameters.getTfgName();
		Timer tfg = (Timer) Finder.getInstance().find(tfgName);
		
		vortexAcquire = new VortexAcquire(sashPlotFormComposite, xmapDetector, tfg, site.getShell().getDisplay(), plot, new Counts(), dirtyContainer);
		vortexAcquire.createAcquire(parent, left);
		
		sashPlotFormComposite.setWeights(new int[] { 35, 74 });
		
		vortexElements = new VortexElements(left, site.getShell(), dirtyContainer, sashPlotFormComposite, vortexParameters, counts);
		
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

	public ComboWrapper getCountType() {
		return countType;
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

}
/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites.detectors;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.common.richbeans.beans.BeanUI;
import org.dawnsci.common.richbeans.components.FieldBeanComposite;
import org.dawnsci.common.richbeans.components.FieldComposite;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.dawnsci.common.richbeans.event.ValueListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.exafs.ui.composites.detectors.internal.FluoDetectorAcquireComposite;
import uk.ac.gda.exafs.ui.composites.detectors.internal.FluoDetectorRegionsComposite;
import uk.ac.gda.exafs.ui.composites.detectors.internal.FluorescenceDetectorCompositeController;

public class FluorescenceDetectorComposite extends FieldBeanComposite implements ValueListener {

	private static final Logger logger = LoggerFactory.getLogger(FluorescenceDetectorComposite.class);

	private SashFormPlotComposite sashFormPlot;
	private FluoDetectorAcquireComposite acquireComposite;
	private FluoDetectorRegionsComposite regionsComposite;
	private FluorescenceDetectorCompositeController controller;

	public FluorescenceDetectorComposite(Composite parent, int style, final IWorkbenchPart part,
			FluorescenceDetector theDetector) {
		super(parent, style);

		this.setLayout(new FillLayout());

		try {
			// this.dataWrapper = readStoredData(); TODO will need to reimplement this

			// TODO problem passing null to SashFormPlotComposite - see DetectorEditor.RegionSynchronizer for possible
			// solution, or better to correct the SashFormPlot so null isn't a problem?
			sashFormPlot = new SashFormPlotComposite(this, part, null, new IAction[] {});
			sashFormPlot.getPlottingSystem().setRescale(false);
			GridLayoutFactory.fillDefaults().applyTo(sashFormPlot.getLeft());
			sashFormPlot.getSashForm().setWeights(new int[] { 35, 70 });

			createController(theDetector, part.getSite());
			createDefaultModel();

			acquireComposite = new FluoDetectorAcquireComposite(sashFormPlot.getLeft(), controller);
			GridDataFactory.fillDefaults().grab(true,false).applyTo(acquireComposite);
			regionsComposite = new FluoDetectorRegionsComposite(sashFormPlot.getLeft(), controller);
			GridDataFactory.fillDefaults().grab(true,true).applyTo(regionsComposite);

			sashFormPlot.computeSizes();

			BeanUI.switchState(getDataModel(), getEditorUI(), false);
			BeanUI.beanToUI(getDataModel(), getEditorUI());
			BeanUI.switchState(getDataModel(), getEditorUI(), true);
			BeanUI.addValueListener(getDataModel(), getEditorUI(), this);

			GridUtils.setVisibleAndLayout(acquireComposite, true);
			GridUtils.setVisibleAndLayout(regionsComposite, true);

		} catch (Exception e) {
			logger.info("Exception when opening FluorescenceDetector view", e);
		}
	}

	private void createController(FluorescenceDetector theDetector, IWorkbenchPartSite site) {
		controller = new FluorescenceDetectorCompositeController(theDetector, site, this);
	}

	private void createDefaultModel() throws DeviceException {

		editorBean = new Xspress3Parameters();

		getDataModel().setCollectionTime(1000.0);
		int numberChannels = controller.getDetector().getNumberOfChannels();
		DetectorROI[] regions = controller.getDetector().getRegionsOfInterest();

		if (regions.length == 0) {
			// create a default
			regions = new DetectorROI[1];
			regions[0] = new DetectorROI();
			regions[0].setRoiName("Roi1");
			regions[0].setRoiStart(700);
			regions[0].setRoiEnd(800);
		}

		List<DetectorElement> detElements = new ArrayList<DetectorElement>();

		for (int index = 0; index < numberChannels; index++) {
			DetectorElement thisElement = new DetectorElement();
			for (DetectorROI region : regions) {
				thisElement.addRegion(region);
			}
			thisElement.setNumber(index);
			thisElement.setName("roi" + index);
			detElements.add(thisElement);
		}
		getDataModel().setDetectorList(detElements);
	}

	public FieldComposite getCollectionTime() {
		return acquireComposite.getCollectionTime();
	}

	public FieldComposite getDetectorList() {
		return regionsComposite.getDetectorList();
	}

	@Override
	public void dispose() {
		super.dispose();
		controller.dispose();
	}

	@Override
	public void valueChangePerformed(ValueEvent e) {
		try {
			BeanUI.uiToBean(getEditorUI(), getDataModel());
		} catch (Exception e1) {
			logger.info("Exception when updating FluorescenceDetector view from UI event", e);
		}
	}

	@Override
	public String getValueListenerName() {
		// TODO Auto-generated method stub
		return null;
	}

	public SashFormPlotComposite getSashFormPlot() {
		return sashFormPlot;
	}

	public FluoDetectorAcquireComposite getAcquireComposite() {
		return acquireComposite;
	}

	public FluoDetectorRegionsComposite getRegionsComposite() {
		return regionsComposite;
	}

	public Xspress3Parameters getDataModel() {
		return (Xspress3Parameters) editorBean;
	}

	public FluorescenceDetectorCompositeController getController() {
		return controller;
	}

	public void updateBeanFromUI() {
		try {
			// TODO this code partially repeats the code in valueChangePerformed. Why are they different?
			BeanUI.switchState(getDataModel(), getEditorUI(), false);
			BeanUI.uiToBean(getEditorUI(), getDataModel());
			BeanUI.switchState(getDataModel(), getEditorUI(), true);
		} catch (Exception e) {
			logger.info("Exception when updating FluorescenceDetector view from UI event", e);
		}
	}

}

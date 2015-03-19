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

import org.dawnsci.common.richbeans.components.scalebox.ScaleBox;
import org.dawnsci.common.richbeans.components.selector.GridListEditor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.exafs.ui.composites.detectors.internal.FluoDetectorAcquireComposite;
import uk.ac.gda.exafs.ui.composites.detectors.internal.FluoDetectorCompositeController;
import uk.ac.gda.exafs.ui.composites.detectors.internal.FluoDetectorRegionsComposite;

/**
 * Composite for enabling a user to set the regions of interest for energy sensitive detectors.
 */
public class FluorescenceDetectorComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(FluorescenceDetectorComposite.class);

	private SashFormPlotComposite sashFormPlot;
	private FluoDetectorAcquireComposite acquireComposite;
	private FluoDetectorRegionsComposite regionsComposite;
	private FluoDetectorCompositeController controller;

	public FluorescenceDetectorComposite(Composite parent, int style, FluoDetectorCompositeController controller, Class<? extends FluorescenceDetectorParameters> detectorParametersClazz) {
		super(parent, style);
		this.setLayout(new FillLayout());

		this.controller = controller;

		try {
			sashFormPlot = new SashFormPlotComposite(this, null);
			sashFormPlot.getPlottingSystem().setRescale(false);
			sashFormPlot.getSashForm().setWeights(new int[] { 35, 65 });

			acquireComposite = new FluoDetectorAcquireComposite(sashFormPlot.getLeft(), controller);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(acquireComposite);
			regionsComposite = new FluoDetectorRegionsComposite(sashFormPlot.getLeft(), controller, detectorParametersClazz);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(regionsComposite);

			sashFormPlot.computeSizes();

			// BeanUI.switchState(getDataModel(), getEditorUI(), false);
			// BeanUI.beanToUI(getDataModel(), getEditorUI());
			// BeanUI.switchState(getDataModel(), getEditorUI(), true);
			//
			// BeanUI.addValueListener(getDataModel(), getEditorUI(), this);

			GridUtils.setVisibleAndLayout(acquireComposite, true);
			GridUtils.setVisibleAndLayout(regionsComposite, true);

//			controller.replot();

		} catch (Exception e) {
			logger.info("Exception creating FluorescenceDetectorComposite", e);
		}
	}

	/*
	 * This name must match the field name in VortexParameters
	 */
	public ScaleBox getCollectionTime() {
		return acquireComposite.getCollectionTime();
	}

	/*
	 * This name must match the field name in VortexParameters
	 */
	public GridListEditor getDetectorList() {
		return regionsComposite.getDetectorList();
	}

	@Override
	public void dispose() {
		super.dispose();
		controller.dispose();
	}

	// @Override
	// public void valueChangePerformed(ValueEvent e) {
	// try {
	// BeanUI.uiToBean(getEditorUI(), getDataModel());
	// } catch (Exception e1) {
	// logger.info("Exception when updating FluorescenceDetectorComposite from UI event", e);
	// }
	// }
	//
	// @Override
	// public String getValueListenerName() {
	// return FluorescenceDetectorComposite.class.getName();
	// }

	public SashFormPlotComposite getSashFormPlot() {
		return sashFormPlot;
	}

	public FluoDetectorAcquireComposite getAcquireComposite() {
		return acquireComposite;
	}

	public FluoDetectorRegionsComposite getRegionsComposite() {
		return regionsComposite;
	}

	public FluoDetectorCompositeController getController() {
		return controller;
	}
	//
	// public void updateBeanFromUI() {
	// try {
	// // TODO this code partially repeats the code in valueChangePerformed. Why are they different?
	// BeanUI.switchState(getDataModel(), getEditorUI(), false);
	// BeanUI.uiToBean(getEditorUI(), getDataModel());
	// BeanUI.switchState(getDataModel(), getEditorUI(), true);
	// } catch (Exception e) {
	// logger.info("Exception when updating FluorescenceDetectorComposite from UI event", e);
	// }
	// }

}

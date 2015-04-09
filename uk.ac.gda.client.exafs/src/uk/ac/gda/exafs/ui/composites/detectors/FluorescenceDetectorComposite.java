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
import org.dawnsci.common.richbeans.event.ValueListener;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.exafs.ui.composites.detectors.internal.FluoDetectorAcquireComposite;
import uk.ac.gda.exafs.ui.composites.detectors.internal.FluoDetectorCompositeController;
import uk.ac.gda.exafs.ui.composites.detectors.internal.FluoDetectorRegionsComposite;

/**
 * Composite for enabling a user to set the regions of interest for energy sensitive detectors.
 */
public abstract class FluorescenceDetectorComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(FluorescenceDetectorComposite.class);

	private SashFormPlotComposite sashFormPlot;
	private FluoDetectorAcquireComposite acquireComposite;
	private FluoDetectorRegionsComposite regionsComposite;
	private FluoDetectorCompositeController controller;
	protected FluorescenceDetector theDetector;

	public FluorescenceDetectorComposite(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());

		try {
			this.theDetector = getDetectorInstance();

			FluorescenceDetectorParameters detectorParameters = theDetector.getConfigurationParameters();

			this.controller = new FluoDetectorCompositeController(this, detectorParameters, theDetector);

			sashFormPlot = new SashFormPlotComposite(this, null);
			sashFormPlot.getPlottingSystem().setRescale(false);
			sashFormPlot.getSashForm().setWeights(new int[] { 35, 65 });

			acquireComposite = new FluoDetectorAcquireComposite(sashFormPlot.getLeft(), controller);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(acquireComposite);
			regionsComposite = new FluoDetectorRegionsComposite(sashFormPlot.getLeft(), controller,
					theDetector.getConfigurationParametersClass());
			GridDataFactory.fillDefaults().grab(true, true).applyTo(regionsComposite);

			sashFormPlot.computeSizes();

			GridUtils.setVisibleAndLayout(acquireComposite, true);
			GridUtils.setVisibleAndLayout(regionsComposite, true);

			controller.start();
			controller.replot();

		} catch (Exception ex) {
			logger.info("Exception creating FluorescenceDetectorComposite", ex);
		}
	}

	/**
	 * Override this method to return the specific FluorescenceDetector that is to be configured with this composite
	 * 
	 * @return the FluorescenceDetector
	 */
	protected abstract FluorescenceDetector getDetectorInstance();

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
		sashFormPlot.dispose();
		super.dispose();
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

	public FluoDetectorCompositeController getController() {
		return controller;
	}

	/**
	 * Add a value listener to all IFieldWidgets in the UI
	 */
	public void addValueListener(ValueListener listener) throws Exception {
		controller.addValueListener(listener);
	}

	/**
	 * Remove a value listener from all IFieldWidgets in the UI
	 */
	public void removeValueListener(ValueListener listener) throws Exception {
		controller.removeValueListener(listener);
	}
}

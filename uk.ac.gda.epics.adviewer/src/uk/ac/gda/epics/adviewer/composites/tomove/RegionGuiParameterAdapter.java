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

package uk.ac.gda.epics.adviewer.composites.tomove;


import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROIList;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.IRegionSystem;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.Observable;
import gda.observable.ObservableUtil;
import gda.observable.Observer;
import gda.observable.Predicate;
import uk.ac.diamond.scisoft.analysis.PlotServer;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;

/**
 * Adapts an {@link IRegionSystem} to an Observable<Map<GuiParameters,
 * Serializable>>, suitable for collecting parameter changes to put into a
 * {@link GuiBean}
 *
 * @author zrb13439
 *
 */
public class RegionGuiParameterAdapter implements
		Observable<Map<GuiParameters, Serializable>> {

	private static final Logger logger = LoggerFactory
			.getLogger(RegionGuiParameterAdapter.class);

	private final IRegionListener regionListener;

	private final IROIListener roiListener;

	ObservableUtil<Map<GuiParameters, Serializable>> observableComponent;

	private IRegionSystem regionSystem;

	/**
	 * Constructs an Observable<Map<GuiParameters, Serializable>> adapter to an
	 * {@link IRegionSystem}. The last changed ROI is returned via
	 * GuiParameters.ROIDATA and the complete list via
	 * GuiParameters.ROIDATALIST.
	 * <p>
	 * It is arguably inefficient to send the entire region list whenever one
	 * region is updated, however the likely target of this class's updates is
	 * the {@link PlotServer}'s GuiBean system which can only move the entire
	 * GUI state about anyway.
	 *
	 * @param regionSystem
	 *            {@link IRegionSystem} to adapt
	 */
	public RegionGuiParameterAdapter(final IRegionSystem regionSystem) {

		this.regionSystem = regionSystem;

		observableComponent = new ObservableUtil<Map<GuiParameters, Serializable>>();

		roiListener = new IROIListener.Stub() {
			@Override
			public void roiChanged(ROIEvent evt) {
				logger.info("ROI changed in '" + evt.getSource() + "': "
						+ evt.getROI().toString());
				fireChange(evt.getROI(), regionSystem.getRegions());
			}
		};

		regionListener = new IRegionListener.Stub() {
			@Override
			public void regionAdded(RegionEvent evt) {
				logger.info("Region added from'" + evt.getSource() + " ': "
						+ evt.getRegion());
				evt.getRegion().addROIListener(roiListener);
				fireChange(evt.getRegion().getROI(), regionSystem.getRegions());
			}

			@Override
			public void regionRemoved(RegionEvent evt) {
				logger.info("Region removed from'" + evt.getSource() + " ': "
						+ evt.getRegion());
				evt.getRegion().removeROIListener(roiListener);
				fireChange(null, regionSystem.getRegions());
			}

		};

		regionSystem.addRegionListener(regionListener);

	}

	@Override
	public void addObserver(Observer<Map<GuiParameters, Serializable>> observer)
			throws Exception {
		observableComponent.addObserver(observer);
	}

	@Override
	public void removeObserver(
			Observer<Map<GuiParameters, Serializable>> observer) {
		observableComponent.removeObserver(observer);
	}

	/**
	 * Fires the current region list. Useful after first creation of a client side view to initialise the server.
	 */
	public void fireCurrentRegionList() {
		fireChange(null, regionSystem.getRegions());
	}

	private void fireChange(IROI changedRoi,
			Collection<IRegion> roiCollection) {
		HashMap<GuiParameters, Serializable> changedParameters = new HashMap<GuiParameters, Serializable>();
		RectangularROIList roiList = new RectangularROIList();
		for (IRegion region : roiCollection) {
			IROI roi = region.getROI();
			roiList.add((RectangularROI) roi); // TODO: Only works for
												// rectangular regions
		}
		if (changedRoi instanceof RectangularROI) {
			roiList.add(changedRoi);
		}
		changedParameters.put(GuiParameters.ROIDATA, changedRoi.getName());
		changedParameters.put(GuiParameters.ROIDATALIST, roiList);

		observableComponent.notifyIObservers(this, changedParameters);
	}

	@Override
	public void addObserver(Observer<Map<GuiParameters, Serializable>> observer,
			Predicate<Map<GuiParameters, Serializable>> predicate) throws Exception {
		observableComponent.addObserver(observer, predicate);

	}

}
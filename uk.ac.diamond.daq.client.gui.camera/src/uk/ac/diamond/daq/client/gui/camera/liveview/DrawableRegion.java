/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera.liveview;

import java.util.UUID;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Creates an {@link Action} into the {@link IPlottingSystem} context menu to draw ROI.
 */
public class DrawableRegion {
	private static final Logger log = LoggerFactory.getLogger(DrawableRegion.class);

	private enum RegionActionMode {
		CREATE, REMOVE
	}

	private final UUID regionID;
	private final Color color;
	private final String name;
	private IROIListener roiListener;
	private IPlottingSystem<Composite> plottingSystem;
	private boolean active;

	private RegionAction regionAction;

	/**
	 * @param plottingSystem the plotting system where append the new action
	 * @param color the
	 * @param name
	 * @param roiListener
	 * @param regionID
	 */
	public DrawableRegion(IPlottingSystem<Composite> plottingSystem, Color color, String name, IROIListener roiListener,
			UUID regionID) {
		this.plottingSystem = plottingSystem;
		this.color = color;
		this.name = name;
		this.roiListener = roiListener;
		this.active = false;
		this.regionID = regionID;

		regionAction = new RegionAction(name, regionID);
	}

	public UUID getRegionID() {
		return regionID;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		if (active == this.active) {
			return;
		}
		if (active) {
			regionAction.create(false);
			regionAction.register();
		} else {
			regionAction.remove(false);
			regionAction.unregister();
		}
		this.active = active;
	}

	public IRegion getIRegion() {
		return plottingSystem.getRegion(getRegionID().toString());
	}

	public String getName() {
		return name;
	}

	private class RegionAction extends Action {
		private RegionActionMode mode;
		private final String name;

		private RegionAction(String name, UUID actionID) {
			this.setId(actionID.toString());
			this.name = name;
			setMode(RegionActionMode.CREATE);
		}

		private void setMode(RegionActionMode mode) {
			if (this.mode == mode) {
				return;
			}
			if (mode == RegionActionMode.CREATE) {
				setText("Create Region " + name);
			}
			if (mode == RegionActionMode.REMOVE) {
				setText("Remove Region " + name);
			}
			this.mode = mode;
		}

		private void register() {
			unregister();
			plottingSystem.getPlotActionSystem().addPopupAction(this);
		}

		private void unregister() {
			plottingSystem.getPlotActionSystem().remove(getId());
		}

		@Override
		public void run() {
			if (mode == RegionActionMode.CREATE) {
				create(true);
			}
			if (mode == RegionActionMode.REMOVE) {
				remove(true);
			}
		}

		private void create(boolean newRegion) {
			try {
				remove(newRegion);
				IRegion region = getIRegion();
				if (region == null && newRegion) {
					region = plottingSystem.createRegion(getRegionID().toString(), RegionType.BOX);
					region.setRegionColor(color);
					if (roiListener != null) {
						region.addROIListener(roiListener);
					}
					log.trace("Creating ROI: {}", getRegionID());
					regionAction.setMode(RegionActionMode.REMOVE);
				} else if (region != null) {
					log.trace("Showing ROI: {}", getRegionID());
					region.setVisible(true);
					regionAction.setMode(RegionActionMode.REMOVE);
				}
			} catch (Exception e) {
				log.error("Unable to create ROI", e);
			}
		}

		private void remove(boolean delete) {
			IRegion region = getIRegion();
			if (region != null) {
				if (delete) {
					log.trace("Deleting ROI: {}", getRegionID());
					plottingSystem.removeRegion(region);
					regionAction.setMode(RegionActionMode.CREATE);
				} else {
					log.trace("Hiding ROI: {}", getRegionID());
					region.setVisible(false);
				}
			} else {
				log.trace("region {} not found", getRegionID());
			}
		}
	}
}

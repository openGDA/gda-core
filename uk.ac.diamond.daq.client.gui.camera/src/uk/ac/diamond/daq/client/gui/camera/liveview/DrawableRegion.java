package uk.ac.diamond.daq.client.gui.camera.liveview;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DrawableRegion {
	private static final Logger log = LoggerFactory.getLogger(DrawableRegion.class);
	
	private enum RegionActionMode { CREATE, REMOVE }
	
	private class RegionAction extends Action {
		private RegionActionMode mode;
		
		private RegionAction (String name) {
			this.setId(name);
			setMode(RegionActionMode.CREATE);
		}
		
		private void setMode (RegionActionMode mode) {
			if (this.mode == mode) {
				return;
			}
			if (mode == RegionActionMode.CREATE) {
				setText ("Create Region " + getId());
			}
			if (mode == RegionActionMode.REMOVE) {
				setText ("Remove Region " + getId());
			}
			this.mode = mode;
		}
		
		private void register () {
			unregister();
			plottingSystem.getPlotActionSystem().addPopupAction(this);
		}
		
		private void unregister () {
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
	}

	private Color color;
	private String name;
	private IROIListener roiListener;
	private IPlottingSystem<Composite> plottingSystem;
	private boolean active;
	
	private RegionAction regionAction;
	
	public DrawableRegion(IPlottingSystem<Composite> plottingSystem, Color color, 
			String name, IROIListener roiListener) {
		this.plottingSystem = plottingSystem;
		this.color = color;
		this.name = name;
		this.roiListener = roiListener;
		this.active = false;
		
		regionAction = new RegionAction(name);
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive (boolean active) {
		if (active == this.active) {
			return;
		}
		if (active) {
			create(false);
			regionAction.register();
		} else {
			remove(false);
			regionAction.unregister();
		}
		this.active = active;
	}
	
	private void create (boolean newRegion) {
		try {
			remove(newRegion);
			IRegion region = getIRegion();
			if (region == null && newRegion) {
				region = plottingSystem.createRegion(name, RegionType.BOX);
				region.setRegionColor(color);
				if (roiListener != null) {
					region.addROIListener(roiListener);
				}
				log.trace("Creating ROI: {}", name);
				regionAction.setMode(RegionActionMode.REMOVE);
			} else if (region != null) {
				log.trace("Showing ROI: {}", name);
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
				log.trace("Deleting ROI: {}", name);
				plottingSystem.removeRegion(region);
				regionAction.setMode(RegionActionMode.CREATE);
			} else {
				log.trace("Hiding ROI: {}", name);
				region.setVisible(false);
			}
		} else {
			log.trace("region {} not found", name);
		}
	}
	
	public void clear () {
		remove (true);
	}
	
	public void setRegion (IRectangularROI roi) {
		IRegion region = getIRegion();
		if (region != null) {
			IROI iroi = region.getROI();
			if (iroi instanceof RectangularROI) {
				RectangularROI rectangularROI = (RectangularROI)iroi;
				rectangularROI.setPoint(roi.getPoint());
				rectangularROI.setLengths(roi.getLength(0), roi.getLength(1));
			}
			regionAction.setMode(RegionActionMode.REMOVE);
		} else {
			regionAction.setMode(RegionActionMode.CREATE);
		}
	}
	
	public String getName() {
		return name;
	}
	
	private IRegion getIRegion () {
		return plottingSystem.getRegion(name);
	}
	
	public IRectangularROI getRegion () {
		IRegion region = getIRegion ();
		if (region == null) {
			return null;
		}
		return region.getROI().getBounds();
	}
}

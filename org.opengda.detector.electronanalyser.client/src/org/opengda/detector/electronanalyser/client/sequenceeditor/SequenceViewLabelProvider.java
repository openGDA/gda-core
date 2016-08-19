package org.opengda.detector.electronanalyser.client.sequenceeditor;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.opengda.detector.electronanalyser.client.Camera;
import org.opengda.detector.electronanalyser.client.ElectronAnalyserClientPlugin;
import org.opengda.detector.electronanalyser.client.ImageConstants;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS;
import org.opengda.detector.electronanalyser.utils.RegionStepsTimeEstimation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class SequenceViewLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider {

	private double xRaySourceEnergyLimit = 2100.0; // must be in eV
	private boolean sourceSelectable = false;
	private Camera camera;
	private static final Logger logger=LoggerFactory.getLogger(SequenceViewLabelProvider.class);
	public SequenceViewLabelProvider() {
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof Region) {
			Region region = (Region) element;
			if (columnIndex == SequenceTableConstants.COL_ENABLED) {
				if (region.isEnabled()) {
					return ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_CHECKED_STATE);
				} else {
					return ElectronAnalyserClientPlugin.getDefault().getImageRegistry().get(ImageConstants.ICON_UNCHECKED_STATE);
				}
			} else if (columnIndex == SequenceTableConstants.COL_STATUS) {
				if (region.isEnabled()) {
					if (region.getStatus()==STATUS.READY) {
						return ElectronAnalyserClientPlugin.getDefault().getImageRegistry()	.get(ImageConstants.ICON_RUN_READY);
					} else if (region.getStatus()==STATUS.RUNNING) {
						return ElectronAnalyserClientPlugin.getDefault().getImageRegistry()	.get(ImageConstants.ICON_RUNNING);
					} else if (region.getStatus()==STATUS.COMPLETED) {
						return ElectronAnalyserClientPlugin.getDefault().getImageRegistry()	.get(ImageConstants.ICON_RUN_COMPLETE);
					} else if (region.getStatus()==STATUS.ABORTED) {
						return ElectronAnalyserClientPlugin.getDefault().getImageRegistry()	.get(ImageConstants.ICON_RUN_FAILURE);
					} else if (region.getStatus()==STATUS.INVALID) {
						return ElectronAnalyserClientPlugin.getDefault().getImageRegistry()	.get(ImageConstants.ICON_INVALID_REGION);
					}
				}
			}
		}
		return null;
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof Region) {
			Region region = (Region) element;
			switch (columnIndex) {
			case SequenceTableConstants.COL_STATUS:
				return "";

			case SequenceTableConstants.COL_ENABLED:
				return "";
			case SequenceTableConstants.COL_REGION_NAME:
				return region.getName();
			case SequenceTableConstants.COL_LENS_MODE:
				return region.getLensMode();
			case SequenceTableConstants.COL_PASS_ENERGY:
				return Integer.toString(region.getPassEnergy());
			case SequenceTableConstants.COL_X_RAY_SOURCE:
				if (isSourceSelectable()) {
					if (region.getExcitationEnergy() < xRaySourceEnergyLimit) {
						logger.debug("excitation energy {}, limit {}", region.getExcitationEnergy(), xRaySourceEnergyLimit);
						return "Soft";
					}
					return "Hard";
				}
				return Double.toString(region.getExcitationEnergy());
			case SequenceTableConstants.COL_ENERGY_MODE:
				return region.getEnergyMode().getLiteral();
			case SequenceTableConstants.COL_LOW_ENERGY:
				return Double.toString(region.getLowEnergy());
			case SequenceTableConstants.COL_HIGH_ENERGY:
				return Double.toString(region.getHighEnergy());
			case SequenceTableConstants.COL_ENERGY_STEP:
				return Double.toString(region.getEnergyStep());
			case SequenceTableConstants.COL_STEP_TIME:
				return Double.toString(region.getStepTime());
			case SequenceTableConstants.COL_STEPS:
				if (region.getAcquisitionMode()==ACQUISITION_MODE.SWEPT) {
					return Long.toString(calculatedSteps(region));
				} else {
					return Integer.toString(1);
				}
			case SequenceTableConstants.COL_TOTAL_TIME:
				if (region.getAcquisitionMode()==ACQUISITION_MODE.SWEPT) {
					return Double.toString(region.getStepTime()*calculatedSteps(region));
				} else {
					return Double.toString(region.getStepTime()*1);
				}
			case SequenceTableConstants.COL_X_CHANNEL_FROM:
				return Integer.toString(region.getFirstXChannel());
			case SequenceTableConstants.COL_X_CHANNEL_TO:
				return Integer.toString(region.getLastXChannel());
			case SequenceTableConstants.COL_Y_CHANNEL_FROM:
				return Integer.toString(region.getFirstYChannel());
			case SequenceTableConstants.COL_Y_CHANNEL_TO:
				return Integer.toString(region.getLastYChannel());
			case SequenceTableConstants.COL_SLICES:
				return Integer.toString(region.getSlices());
			case SequenceTableConstants.COL_MODE:
				return region.getDetectorMode().getLiteral();
			}
		}
		return null;
	}

	private long calculatedSteps(Region region) {
		return RegionStepsTimeEstimation.calculateTotalSteps(
				(region.getHighEnergy() - region.getLowEnergy()),
				region.getEnergyStep(),
				camera.getEnergyResolution()*region.getPassEnergy()
						* (region.getLastXChannel() - region
								.getFirstXChannel()+1));
	}

	public double getXRaySourceEnergyLimit() {
		return xRaySourceEnergyLimit;
	}

	public void setXRaySourceEnergyLimit(double xRaySourceEnergyLimit) {
		this.xRaySourceEnergyLimit = xRaySourceEnergyLimit;
	}

	public boolean isSourceSelectable() {
		return sourceSelectable;
	}

	public void setSourceSelectable(boolean sourceSelectable) {
		this.sourceSelectable = sourceSelectable;
	}

	@Override
	public Color getForeground(Object element, int columnIndex) {
		if (columnIndex == SequenceTableConstants.COL_STATUS) {
			return ColorConstants.red;
		}
		return null;
	}

	@Override
	public Color getBackground(Object element, int columnIndex) {
		if (columnIndex == SequenceTableConstants.COL_STATUS) {
			return ColorConstants.white;
		}
		return null;
	}
	public void setCamera(Camera camera) {
		this.camera = camera;

	}
}

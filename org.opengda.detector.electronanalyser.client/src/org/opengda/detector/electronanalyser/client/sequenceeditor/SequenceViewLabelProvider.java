package org.opengda.detector.electronanalyser.client.sequenceeditor;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.omg.CORBA.INTF_REPOS;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;

public class SequenceViewLabelProvider extends LabelProvider implements
		ITableLabelProvider, ITableColorProvider {

	@Override
	public Color getForeground(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getBackground(Object element, int columnIndex) {
		return null;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {

		return null;
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
				return region.getLensMode().getLiteral();
			case SequenceTableConstants.COL_PASS_ENERGY:
				return region.getPassEnergy().getLiteral();
			case SequenceTableConstants.COL_EXCITATION_ENERGY:
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
				//FIXME - getSetpTime()
				return Double.toString(region.getSetpTime());
			case SequenceTableConstants.COL_STEPS:
				//FIXME
				return Integer.toString(1);
			case SequenceTableConstants.COL_TOTAL_TIME:
				//FIXME
				return Double.toString(100.00);
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
}

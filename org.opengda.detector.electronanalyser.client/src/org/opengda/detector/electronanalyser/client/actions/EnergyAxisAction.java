package org.opengda.detector.electronanalyser.client.actions;

import org.eclipse.jface.action.Action;
import org.opengda.detector.electronanalyser.api.SESRegion;
import org.opengda.detector.electronanalyser.client.IEnergyAxis;

public class EnergyAxisAction extends Action {

	IEnergyAxis energyAxis;
	String mode;

	public EnergyAxisAction(String text, int style,IEnergyAxis plot, String mode) {
		super(text, style);
		this.energyAxis=plot;
		this.mode=mode;
	}
	@Override
	public void run() {
		super.run();
		energyAxis.displayInBindingEnergy(mode.equals(SESRegion.BINDING));
		energyAxis.updatePlot();
	}
}

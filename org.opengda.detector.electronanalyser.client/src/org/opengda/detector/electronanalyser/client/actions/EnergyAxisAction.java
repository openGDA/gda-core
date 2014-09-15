package org.opengda.detector.electronanalyser.client.actions;

import org.eclipse.jface.action.Action;
import org.opengda.detector.electronanalyser.client.IEnergyAxis;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE;

public class EnergyAxisAction extends Action {
	
	IEnergyAxis energyAxis;
	ENERGY_MODE mode;
	
	public EnergyAxisAction(String text, int style,IEnergyAxis plot, ENERGY_MODE mode) {
		super(text, style);
		this.energyAxis=plot;
		this.mode=mode;
	}
	@Override
	public void run() {
		super.run();
		if (mode == ENERGY_MODE.KINETIC) {
			energyAxis.displayInBindingEnergy(false);
		}
		else {
			energyAxis.displayInBindingEnergy(true);
		}
		energyAxis.updatePlot();
	}
}


package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.client.IEnergyAxis;
import org.opengda.detector.electronanalyser.client.IPlotCompositeInitialiser;
import org.opengda.detector.electronanalyser.client.actions.EnergyAxisAction;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;

public abstract class LivePlotView extends ViewPart {

	private IVGScientaAnalyser analyser;
	private String arrayPV;
	Action kinetic;
	Action binding;

	protected void configureAndInitialisePlotComposite(IPlotCompositeInitialiser plotComposite) {
		plotComposite.setAnalyser(getAnalyser());
		plotComposite.setArrayPV(getArrayPV());
		plotComposite.initialise();
	}

	protected void makeActions(IViewSite viewSite, IEnergyAxis plotComposite) {
		kinetic = new EnergyAxisAction("Kinetic", IAction.AS_RADIO_BUTTON, plotComposite,ENERGY_MODE.KINETIC);
		kinetic.setToolTipText("Display data in kinetic energy.");
		kinetic.setEnabled(false);
		kinetic.setChecked(true);
		binding = new EnergyAxisAction("Binding", IAction.AS_RADIO_BUTTON, plotComposite,ENERGY_MODE.BINDING);
		binding.setToolTipText("Display data in binding energy.");
		binding.setEnabled(true);
		binding.setChecked(false);
		IMenuManager menuManager = viewSite.getActionBars().getMenuManager();
		menuManager.add(kinetic);
		menuManager.add(binding);
	}

	protected void updateEnergyAxisActions(IWorkbenchPart part, Object firstElement, IEnergyAxis plotComposite) {
		Region region = (Region) firstElement;
		if (region.getEnergyMode() == ENERGY_MODE.BINDING) {
			plotComposite.displayInBindingEnergy(true);
			if (!part.getSite().getShell().getDisplay().isDisposed()) {
				part.getSite().getShell().getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						kinetic.setEnabled(true);
						kinetic.setChecked(false);
						binding.setEnabled(false);
						kinetic.setChecked(true);
					}
				});
			}
		} else {
			plotComposite.displayInBindingEnergy(false);
			if (!part.getSite().getShell().getDisplay().isDisposed()) {
				part.getSite().getShell().getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						kinetic.setEnabled(false);
						kinetic.setChecked(true);
						binding.setEnabled(true);
						kinetic.setChecked(false);
					}
				});
			}
		}
	}

	public LivePlotView() {
		super();
	}

	@Override
	public void setFocus() {
	
	}

	public IVGScientaAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(IVGScientaAnalyser analyser) {
		this.analyser = analyser;
	}

	public void setViewPartName(String viewPartName) {
		setPartName(viewPartName);
	
	}

	public String getArrayPV() {
		return arrayPV;
	}

	public void setArrayPV(String arrayPV) {
		this.arrayPV = arrayPV;
	}

}
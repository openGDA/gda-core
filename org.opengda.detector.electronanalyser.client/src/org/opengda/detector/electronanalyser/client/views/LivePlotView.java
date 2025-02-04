package org.opengda.detector.electronanalyser.client.views;

import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.api.SESRegion;
import org.opengda.detector.electronanalyser.client.ElectronAnalyserClientPlugin;
import org.opengda.detector.electronanalyser.client.IEnergyAxis;
import org.opengda.detector.electronanalyser.client.IPlotCompositeInitialiser;
import org.opengda.detector.electronanalyser.client.ImageConstants;
import org.opengda.detector.electronanalyser.client.actions.EnergyAxisAction;

import uk.ac.gda.devices.vgscienta.IVGScientaAnalyserRMI;

public abstract class LivePlotView extends ViewPart {

	private IVGScientaAnalyserRMI analyser;
	private String updatePV;
	private Action kinetic;
	private Action binding;
	private double updatesPerSecond;

	protected void configureAndInitialisePlotComposite(IPlotCompositeInitialiser plotComposite) {
		plotComposite.setAnalyser(getAnalyser());
		plotComposite.setUpdatePV(getUpdatePV());
		plotComposite.setUpdatesPerSecond(getUpdatesPerSecond());
		plotComposite.initialise();
	}

	/**
	 * This inner class is the action attached to the swap energy button itself. It handles flipping between KE and BE
	 */
	private class SwitchEnergyAction extends Action {

		private IEnergyAxis energyAxis;

		public SwitchEnergyAction(IEnergyAxis plot) {
			this.energyAxis = plot;
		}

		@Override
		public void run() {
			final Action action = energyAxis.isDisplayInBindingEnergy() ? binding : kinetic;
			action.setChecked(true);
			action.run();
		}
	}

	protected void makeActions(IViewSite viewSite, IEnergyAxis plotComposite) {

		final MenuAction energyDropDown = new MenuAction("Energy mode selection");
		energyDropDown.setId("org.opengda.detector.electronanalyser.client.actions.energymodeselection");
		energyDropDown.setImageDescriptor(ElectronAnalyserClientPlugin.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_ENERGY_SELECTION));
		energyDropDown.setSelectedAction(new SwitchEnergyAction(plotComposite));

		// Create energy actions
		kinetic = new EnergyAxisAction("Kinetic", IAction.AS_RADIO_BUTTON, plotComposite, SESRegion.KINETIC);
		kinetic.setToolTipText("Display data in kinetic energy.");

		binding = new EnergyAxisAction("Binding", IAction.AS_RADIO_BUTTON, plotComposite, SESRegion.BINDING);
		binding.setToolTipText("Display data in binding energy.");

		// Add actions to the drop down
		energyDropDown.add(kinetic);
		energyDropDown.add(binding);
		energyDropDown.setText("Swap Energy");

		// Create a CheckableActionGroup to ensure exactly one option is always selected.
		final CheckableActionGroup energyGroup = new CheckableActionGroup();
		energyGroup.add(kinetic);
		energyGroup.add(binding);

		contributeToActionBars(energyDropDown);
	}

	protected void updateEnergyAxisActions(IWorkbenchPart part, boolean isBindingEnergy, IEnergyAxis plotComposite) {
		if (part.getSite().getShell().getDisplay().isDisposed()) return;
		final Action action = isBindingEnergy ? binding : kinetic;
		plotComposite.displayInBindingEnergy(isBindingEnergy);
		part.getSite().getShell().getDisplay().asyncExec(() -> action.setChecked(true));
	}
	private void contributeToActionBars(Action energyDropDown) {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager(), energyDropDown);
		fillLocalToolBar(bars.getToolBarManager(), energyDropDown);
	}

	private void fillLocalPullDown(IMenuManager manager, Action energyDropDown) {
		manager.add(new Separator());
		manager.add(energyDropDown);
	}

	private void fillLocalToolBar(IToolBarManager manager, Action energyDropDown) {
		manager.add(new Separator());
		manager.add(energyDropDown);

	}

	public LivePlotView() {
		super();
	}

	@Override
	public void setFocus() {

	}

	public IVGScientaAnalyserRMI getAnalyser() {
		return analyser;
	}

	public void setAnalyser(IVGScientaAnalyserRMI analyser) {
		this.analyser = analyser;
	}

	public void setViewPartName(String viewPartName) {
		setPartName(viewPartName);
	}

	public void setUpdatePV(String updatePV) {
		this.updatePV = updatePV;
	}

	public String getUpdatePV() {
		return updatePV;
	}

	public double getUpdatesPerSecond() {
		return updatesPerSecond;
	}

	public void setUpdatesPerSecond(double updatesPerSecond) {
		this.updatesPerSecond = updatesPerSecond;
	}

}
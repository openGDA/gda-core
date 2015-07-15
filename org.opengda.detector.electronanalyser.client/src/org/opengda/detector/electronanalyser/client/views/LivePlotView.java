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
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.client.ElectronAnalyserClientPlugin;
import org.opengda.detector.electronanalyser.client.IEnergyAxis;
import org.opengda.detector.electronanalyser.client.IPlotCompositeInitialiser;
import org.opengda.detector.electronanalyser.client.ImageConstants;
import org.opengda.detector.electronanalyser.client.actions.EnergyAxisAction;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;

public abstract class LivePlotView extends ViewPart {

	private IVGScientaAnalyser analyser;
	private String arrayPV;
	private Action kinetic;
	private Action binding;

	protected void configureAndInitialisePlotComposite(IPlotCompositeInitialiser plotComposite) {
		plotComposite.setAnalyser(getAnalyser());
		plotComposite.setArrayPV(getArrayPV());
		plotComposite.initialise();
	}

	protected void makeActions(IViewSite viewSite, IEnergyAxis plotComposite) {
		CheckableActionGroup energyGroup=new CheckableActionGroup();

        final MenuAction energyDropDown = new MenuAction("Energy mode selection");
        energyDropDown.setId("org.opengda.detector.electronanalyser.client.actions.energymodeselection");
        energyDropDown.setImageDescriptor(ElectronAnalyserClientPlugin.getDefault().getImageRegistry().getDescriptor(ImageConstants.ICON_ENERGY_SELECTION));

        kinetic = new EnergyAxisAction("Kinetic", IAction.AS_CHECK_BOX, plotComposite,ENERGY_MODE.KINETIC);
		kinetic.setToolTipText("Display data in kinetic energy.");
        energyDropDown.add(kinetic);
        energyDropDown.setSelectedAction(kinetic);
        energyDropDown.setText("Swap Energy");
        energyDropDown.setChecked(true);

        binding = new EnergyAxisAction("Binding", IAction.AS_CHECK_BOX, plotComposite,ENERGY_MODE.BINDING);
		binding.setToolTipText("Display data in binding energy.");
		energyDropDown.add(binding);

		energyGroup.add(kinetic);
		energyGroup.add(binding);

		contributeToActionBars(energyDropDown);
	}

	protected void updateEnergyAxisActions(IWorkbenchPart part, Object firstElement, IEnergyAxis plotComposite) {
		Region region = (Region) firstElement;
		if (region.getEnergyMode() == ENERGY_MODE.BINDING) {
			plotComposite.displayInBindingEnergy(true);
			if (!part.getSite().getShell().getDisplay().isDisposed()) {
				part.getSite().getShell().getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
						binding.setChecked(true);
					}
				});
			}
		} else {
			plotComposite.displayInBindingEnergy(false);
			if (!part.getSite().getShell().getDisplay().isDisposed()) {
				part.getSite().getShell().getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
						kinetic.setChecked(true);
					}
				});
			}
		}
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

	private void fillContextMenu(IMenuManager manager, Action energyDropDown) {
		manager.add(energyDropDown);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
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
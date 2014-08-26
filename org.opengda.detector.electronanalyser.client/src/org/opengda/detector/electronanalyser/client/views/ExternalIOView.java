package org.opengda.detector.electronanalyser.client.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.client.selection.RegionRunCompletedSelection;
import org.opengda.detector.electronanalyser.client.viewextensionfactories.SequenceViewExtensionFactory;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalIOView extends ViewPart {

	private static final Logger logger=LoggerFactory .getLogger(ExternalIOView.class);
	private IVGScientaAnalyser analyser;
	private String arrayPV;
	
	public ExternalIOView() {
		setTitleToolTip("live display of external IO data");
		// setContentDescription("A view for external IO data.");
		setPartName("ExternalIO");
	}

	ExtIOPlotComposite externalIOPlotComposite;
	Action energyMode;
	@Override
	public void createPartControl(Composite parent) {
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new FillLayout());

		try {
			externalIOPlotComposite = new ExtIOPlotComposite(this, rootComposite, SWT.None);
			externalIOPlotComposite.setAnalyser(getAnalyser());
			externalIOPlotComposite.setArrayPV(arrayPV);
			externalIOPlotComposite.initialise();
			
			energyMode = new Action("Kinetic", IAction.AS_CHECK_BOX) {
				@Override
				public void run() {
					super.run();
					if (energyMode.isChecked()) {
						externalIOPlotComposite.setDisplayBindingEnergy(false);
						energyMode.setText("Kinetic");
					} else {
						externalIOPlotComposite.setDisplayBindingEnergy(true);
						energyMode.setText("Binding");
					}
					externalIOPlotComposite.updatePlot();
				}
			};
			energyMode.setToolTipText("Toggle energy mode to display data");
			energyMode.setEnabled(true);
			energyMode.setChecked(true);
			IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
			toolBarManager.add(energyMode);

		} catch (Exception e) {
			logger.error("Cannot create external IO plot composite.", e);
		}
		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(SequenceViewExtensionFactory.ID, selectionListener);
	}

	private ISelectionListener selectionListener = new INullSelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof RegionRunCompletedSelection) {
				externalIOPlotComposite.setNewRegion(true);
			} else if (selection instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection) selection;
				Object firstElement = sel.getFirstElement();
				if (firstElement instanceof Region) {
					Region region = (Region) firstElement;
					if (region.getEnergyMode() == ENERGY_MODE.BINDING) {
						externalIOPlotComposite.setDisplayBindingEnergy(true);
						if (!part.getSite().getShell().getDisplay().isDisposed()) {
							part.getSite().getShell().getDisplay().asyncExec(new Runnable() {

								@Override
								public void run() {
									energyMode.setText("Binding");
									energyMode.setChecked(false);
								}
							});
						}
					} else {
						externalIOPlotComposite.setDisplayBindingEnergy(false);
						if (!part.getSite().getShell().getDisplay().isDisposed()) {
							part.getSite().getShell().getDisplay().asyncExec(new Runnable() {

								@Override
								public void run() {
									energyMode.setText("Kinetic");
									energyMode.setChecked(true);
								}
							});
						}
					}
				}
			}
		}
	};


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

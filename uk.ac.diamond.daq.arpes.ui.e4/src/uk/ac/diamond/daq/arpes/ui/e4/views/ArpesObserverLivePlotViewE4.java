package uk.ac.diamond.daq.arpes.ui.e4.views;


import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.dawnsci.plotting.tools.profile.BoxProfileTool;
import org.dawnsci.plotting.tools.profile.ProfileTool;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.arpes.ui.e4.actions.ProfileAction;
import uk.ac.diamond.daq.pes.api.LiveDataPlotUpdate;

/**
 * This class is creating an E4 view that finds and observes a certain data
 * dispatcher if injected plot_name annotation corresponds to data dispatcher'
 * plotName. The view is also adding some DAWN tools using e3 plottingService.
 *
 */
public class ArpesObserverLivePlotViewE4 extends BaseLivePlotViewE4 {
	private static final Logger logger = LoggerFactory.getLogger(ArpesObserverLivePlotViewE4.class);
	private LiveDataPlotUpdate lastDataUpdate;

	@Inject
	public ArpesObserverLivePlotViewE4(IEclipseContext context) {
		this.context = context;
	}

	@PostConstruct
	public void createComposite(Composite parent) {
		// Add DAWN plotting tools - create light weight plotting system
		try {
			validateDependencies();
			createPlottingSystem(parent);
			configureToolbar(parent);
			addProfileAction(parent, BoxProfileTool.class);
			addTransposeAction(parent);
			logger.debug("Successfully created ARPES live plot view");
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}

	@Focus
	public void setFocus() {
		plottingSystem.setFocus();
	}

	private void addProfileAction(Composite parent, Class<? extends ProfileTool> clazz) {
		ProfileAction lineProfileAction =	new ProfileAction(plottingSystem, parent, clazz);
		IActionBars actionBars = plottingSystem.getActionBars();
		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		toolBarManager.add(lineProfileAction);
		ActionContributionItem item = new ActionContributionItem(lineProfileAction);
		item.fill(findToolbarComposite(parent));
		toolBarManager.update(true);
	}


	private void addTransposeAction(Composite parent) {
		Action transposeImage = new Action("Transpose", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				transposePreference = isChecked();
				if (lastDataUpdate!=null) {
					updatePlot(lastDataUpdate);
				}
			}
		};
		transposeImage.setId(PlottingConstants.IMAGE_TRANSPOSE_ID);
		transposeImage.setToolTipText("Swap axes about image origin");
		transposeImage.setChecked(false);
		IActionBars actionBars = plottingSystem.getActionBars();
		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		toolBarManager.add(transposeImage);
		ActionContributionItem item = new ActionContributionItem(transposeImage);
		item.fill(findToolbarComposite(parent));
		toolBarManager.update(true);
	}



	@Override
	protected void updatePlot(LiveDataPlotUpdate arg) {
		cacheLastDataUpdate(arg);
		doUpdate(arg);
	}

	private void cacheLastDataUpdate(LiveDataPlotUpdate arg) {
		this.lastDataUpdate = arg;
	}
}

package uk.ac.diamond.daq.arpes.ui.e4.views;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.diamond.daq.arpes.ui.e4.dispatcher.ArpesLiveDataDispatcherObservable;
import uk.ac.diamond.daq.pes.api.LiveDataPlotUpdate;

/**
 * This class is creating an E4 view that finds and observes a certain data
 * dispatcher if injected plot_name annotation corresponds to data dispatcher'
 * plotName. The view is also adding some DAWN tools using e3 plottingService.
 *
 */
public class ArpesObserverLivePlotViewE4 implements IObserver {
	private static final Logger logger = LoggerFactory.getLogger(ArpesObserverLivePlotViewE4.class);
	private IEclipseContext context;
	private IPlottingSystem<Composite> plottingSystem;
	private List<ArpesLiveDataDispatcherObservable> dataDispatchersList;
	private String plotName;

	@Inject
	public ArpesObserverLivePlotViewE4(IEclipseContext context, @Named("plot_name") @Active @Optional String plotName) {
		this.plotName = (plotName != null) ? plotName : "Image";
		this.context = context;
		logger.debug("Configuring plot view with plot_name {}", this.plotName);
	}

	@PostConstruct
	public void createComposite(Composite parent) {
		IActionBars actionBars;

		// Find all data dispatchers, filter by plotName and subscribe to updates
		dataDispatchersList = Finder.listLocalFindablesOfType(ArpesLiveDataDispatcherObservable.class);
		for (ArpesLiveDataDispatcherObservable dataDispatcher : dataDispatchersList) {
			if (dataDispatcher.getPlotName().contains(plotName)) {
				dataDispatcher.addIObserver(this);
				logger.debug("Found live data dispatcher with name {} for plot view with plot_name {}", plotName,
						plotName);
			}
		}

		// Add DAWN plotting tools - create light weight plotting system
		try {
			plottingSystem = context.get(IPlottingService.class).createPlottingSystem();
			actionBars = plottingSystem.getActionBars();
			plottingSystem.createPlotPart(parent, plotName, actionBars, PlotType.IMAGE, null);
			plottingSystem.setShowLegend(false);
			plottingSystem.setTitle(plotName);
			plottingSystem.setShowLegend(false);
			plottingSystem.getSelectedYAxis().setInverted(true);
			plottingSystem.setKeepAspect(false);
			// Adding DAWN actions to the toolbar as this class does not extend e3 ViewPart
			// Note this works only if Part is inside PartStack in fragment file!
			Composite toolbarComposite = (Composite) parent.getParent().getChildren()[0];
			// here contributions filled from LightWeightPlotting
			IToolBarManager tbm = plottingSystem.getActionBars().getToolBarManager();
			for (IContributionItem item : tbm.getItems()) {
				item.fill(toolbarComposite);
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}

	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof LiveDataPlotUpdate ds) {
			updatePlot(ds);
		}
	}

	public IPlottingSystem<Composite> getPlottingSystem() {
		return plottingSystem;
	}

	private void updatePlot(LiveDataPlotUpdate arg) {
		List<IDataset> axis = Arrays.asList(arg.getxAxis(), arg.getyAxis());
		plottingSystem.updatePlot2D(arg.getData(), axis, null);
		plottingSystem.setKeepAspect(false);
		plottingSystem.repaint();
	}

	@Focus
	public void setFocus() {
		plottingSystem.setFocus();
	}

	@PreDestroy
	public void dispose() {
		plottingSystem.dispose();
		for (ArpesLiveDataDispatcherObservable dataDispatcher : dataDispatchersList) {
			dataDispatcher.deleteIObserver(this);
		}
	}
}

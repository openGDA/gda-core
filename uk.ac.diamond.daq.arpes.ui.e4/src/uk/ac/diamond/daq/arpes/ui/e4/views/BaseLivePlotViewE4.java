package uk.ac.diamond.daq.arpes.ui.e4.views;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.diamond.daq.arpes.ui.e4.constants.ArpesUiConstants;
import uk.ac.diamond.daq.arpes.ui.e4.dispatcher.AbstractBaseArpesLiveDataDispatcher;
import uk.ac.diamond.daq.pes.api.LiveDataPlotUpdate;

public abstract class BaseLivePlotViewE4 implements IObserver{
	private static final Logger logger = LoggerFactory.getLogger(BaseLivePlotViewE4.class);

	protected IPlottingSystem<Composite> plottingSystem;
	protected IEclipseContext context;
	protected IPlottingService plottingService;

	private volatile boolean isDisposed;
	protected boolean transposePreference;

	private Map<String, AbstractBaseArpesLiveDataDispatcher> dispatchers;


	@Inject
	MPart myPart;

	@Inject
	@Named("tag")
	@Active
	@Optional
	String tag;

	@PostConstruct
	private void setDispatcher() {
		dispatchers = Finder.getFindablesOfType(AbstractBaseArpesLiveDataDispatcher.class);
		// Find all data dispatchers, filter by plotName and subscribe to updates
		this.tag = (tag != null) ? ArpesUiConstants.getConstantValue(tag) : ArpesUiConstants.ARPES_LIVE_DATA_UPDATE_TOPIC;
		for (AbstractBaseArpesLiveDataDispatcher dataDispatcher : dispatchers.values()) {
			if (dataDispatcher.getTags().contains(tag)) {
				dataDispatcher.addIObserver(this);
				logger.debug("Found live data dispatcher with name {} for plot view with plot_name {}", tag,
						tag);
			}
		}
	}

	@Override
	public void update(Object source, Object arg) {
		if (plottingSystem == null || isDisposed) {
			return;
		}
		if (arg instanceof LiveDataPlotUpdate dataUpdate) {
			updatePlot(dataUpdate);
		}
	}

	// Implement this method in child classes
	protected abstract void updatePlot(LiveDataPlotUpdate dataUpdate);


	protected void doUpdate(LiveDataPlotUpdate arg) {
		List<IDataset> axes = Arrays.asList(arg.getxAxis(), arg.getyAxis());
		if (plottingSystem != null) {
			if (transposePreference) {
				plottingSystem.updatePlot2D(arg.getData().getTransposedView(1,0), axes.reversed(), null);
			} else {
				plottingSystem.updatePlot2D(arg.getData(), axes, null);
			}
		}
	}

	protected void createPlottingSystem(Composite parent) throws Exception {
		plottingSystem = plottingService.createPlottingSystem();
		if (plottingSystem == null) {
			throw new IllegalStateException("Failed to create plotting system");
		}
		// Configure plotting system
		IActionBars actionBars = plottingSystem.getActionBars();
		String partLabel = getPartLabel();
		plottingSystem.createPlotPart(parent, partLabel, actionBars, PlotType.IMAGE, null);
		plottingSystem.setShowLegend(false);
		plottingSystem.setTitle(partLabel);
		logger.debug("Created plotting system with title: {}", partLabel);
	}


	protected void configureToolbar(Composite parent) {
		// Adding DAWN actions to the toolbar as this class does not extend e3 ViewPart
		// Note this works only if Part is inside PartStack in fragment file!
		try {
			Composite toolbarComposite = findToolbarComposite(parent);
			if (toolbarComposite == null) {
				logger.warn("Could not find toolbar composite - toolbar actions will not be available");
				return;
			}
			IActionBars actionBars = plottingSystem.getActionBars();
			if (actionBars == null) {
				logger.warn("No action bars available from plotting system");
				return;
			}
			// here contributions filled from LightWeightPlotting
			populateToolbar(toolbarComposite, actionBars.getToolBarManager());
		} catch (Exception e) {
			logger.warn("Failed to configure toolbar - continuing without toolbar actions", e);
		}
	}


	protected void populateToolbar(Composite toolbarComposite, IToolBarManager toolBarManager) {
		if (toolBarManager == null) {
			return;
		}
		IContributionItem[] items = toolBarManager.getItems();
		if (items == null || items.length == 0) {
			logger.debug("No toolbar items to populate");
			return;
		}
		for (IContributionItem item : items) {
			try {
				if (item != null) {
					item.fill(toolbarComposite);
				}
			} catch (Exception e) {
				logger.warn("Failed to add toolbar item: {}", item, e);
			}
		}
		logger.debug("Added {} toolbar items", items.length);
	}

	protected Composite findToolbarComposite(Composite parent) {
		Composite parentComposite = parent.getParent();
		if (parentComposite == null) {
			return null;
		}
		Control[] children = parentComposite.getChildren();
		if (children.length == 0) {
			return null;
		}
		// Safely check if first child is a Composite
		Control firstChild = children[0];
		return (firstChild instanceof Composite firstChildComposite) ? firstChildComposite : null;
	}


	protected void validateDependencies() {
		if (myPart == null) {
			throw new IllegalStateException("MPart not injected");
		}
		plottingService = context.get(IPlottingService.class);
		if (plottingService == null) {
			throw new IllegalStateException("IPlottingService not available in context");
		}
	}

	protected String getPartLabel() {
		return (myPart != null && myPart.getLabel() != null) ? myPart.getLabel() : "ARPES Live Plot";
	}


	@PreDestroy
	public void dispose() {
		isDisposed = true;
		if (plottingSystem!=null) {
			try {
				plottingSystem.dispose();
				logger.debug("Disposed plotting system");
			} catch (Exception e) {
				logger.warn("Error disposing plotting system", e);
			} finally {
				plottingSystem = null;
			}
		}
		for (AbstractBaseArpesLiveDataDispatcher dataDispatcher : dispatchers.values()) {
			dataDispatcher.deleteIObserver(this);
		}
	}

}

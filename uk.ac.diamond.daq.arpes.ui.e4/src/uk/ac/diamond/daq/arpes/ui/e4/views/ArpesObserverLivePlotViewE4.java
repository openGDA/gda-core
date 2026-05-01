package uk.ac.diamond.daq.arpes.ui.e4.views;

import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.pes.api.LiveDataPlotUpdate;

/**
 * This class is creating an E4 view that finds and observes a certain data
 * dispatcher if injected plot_name annotation corresponds to data dispatcher'
 * plotName. The view is also adding some DAWN tools using e3 plottingService.
 *
 */
public class ArpesObserverLivePlotViewE4 extends BaseLivePlotViewE4 {
	private static final Logger logger = LoggerFactory.getLogger(ArpesObserverLivePlotViewE4.class);
	private IEclipseContext context;
	private IPlottingService plottingService;

	@Inject
	MPart myPart;

	@Inject
	public ArpesObserverLivePlotViewE4(IEclipseContext context) {
		this.context = context;
		logger.debug("Configuring plot view with tag {}", this.tag);
	}

	@PostConstruct
	public void createComposite(Composite parent) {
		// Add DAWN plotting tools - create light weight plotting system
		try {
			validateDependencies();
			createPlottingSystem(parent);
			configureToolbar(parent);
			logger.debug("Successfully created ARPES live plot view");
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}

	private void validateDependencies() {
		if (myPart == null) {
			throw new IllegalStateException("MPart not injected");
		}
		plottingService = context.get(IPlottingService.class);
		if (plottingService == null) {
			throw new IllegalStateException("IPlottingService not available in context");
		}
	}

	private void createPlottingSystem(Composite parent) throws Exception {
		plottingSystem = plottingService.createPlottingSystem();
		if (plottingSystem == null) {
			throw new IllegalStateException("Failed to create plotting system");
		}
		// Configure plotting system
		IActionBars actionBars = plottingSystem.getActionBars();
		String partLabel = getPartLabel();
		plottingSystem.createPlotPart(parent, partLabel, actionBars, PlotType.IMAGE, null);
		plottingSystem.setShowLegend(false);
		plottingSystem.setTitle(myPart.getLabel());
		plottingSystem.getSelectedYAxis().setInverted(true);
		plottingSystem.setKeepAspect(false);
		logger.debug("Created plotting system with title: {}", partLabel);
	}

	private void configureToolbar(Composite parent) {
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

	private Composite findToolbarComposite(Composite parent) {
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
		return (firstChild instanceof Composite) ? (Composite) firstChild : null;
	}

	private void populateToolbar(Composite toolbarComposite, IToolBarManager toolBarManager) {
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

	private String getPartLabel() {
		return (myPart != null && myPart.getLabel() != null) ? myPart.getLabel() : "ARPES Live Plot";
	}

	public IPlottingSystem<Composite> getPlottingSystem() {
		return plottingSystem;
	}

	@Override
	protected void updatePlot(LiveDataPlotUpdate arg) {
		List<IDataset> axis = Arrays.asList(arg.getxAxis(), arg.getyAxis());
		plottingSystem.updatePlot2D(arg.getData(), axis, null);
		plottingSystem.setKeepAspect(false);
		plottingSystem.repaint();
	}

	@Focus
	public void setFocus() {
		plottingSystem.setFocus();
	}

}

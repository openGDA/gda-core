package uk.ac.diamond.daq.arpes.ui.e4.views;

import java.util.Arrays;

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
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.arpes.ui.e4.constants.ArpesUiConstants;
import uk.ac.diamond.daq.pes.api.LiveDataPlotUpdate;

/**
 * This class is creating an E4 view and subscribes to a configurable topic on
 * event broker.
 *
 */
public class ArpesLivePlotViewE4 {
	@Inject
	MPart myPart;
	@Inject
	IEventBroker broker;
	private static final Logger logger = LoggerFactory.getLogger(ArpesLivePlotViewE4.class);
	private IPlottingSystem<Composite> plottingSystem;
	private IEclipseContext context;
	private String eventTopic;

	@Inject
	public ArpesLivePlotViewE4(IEclipseContext context, @Named("eventTopic") @Active @Optional String eventTopic) {
		this.context = context;
		this.eventTopic = ArpesUiConstants.getConstantValue(eventTopic);
	}

	@PostConstruct
	public void createComposite(Composite parent) {
		// Add DAWN plotting tools - create light weight plotting system
		try {
			plottingSystem = context.get(IPlottingService.class).createPlottingSystem();
			IActionBars actionBars = plottingSystem.getActionBars();
			plottingSystem.createPlotPart(parent, myPart.getLabel(), actionBars, PlotType.IMAGE, null);
			plottingSystem.setShowLegend(false);
			plottingSystem.setTitle(myPart.getLabel());
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
			subscribeToEventBroker();
		} catch (Exception e) {
			logger.error("Failed create composite",e);
		}
	}

	private void subscribeToEventBroker() {
		broker.subscribe(eventTopic, updatePlot);
	}

	private EventHandler updatePlot = event -> {
		LiveDataPlotUpdate data = (LiveDataPlotUpdate) event.getProperty(IEventBroker.DATA);
		plottingSystem.updatePlot2D(data.getData(), Arrays.asList(data.getxAxis(), data.getyAxis()), null);
		plottingSystem.setKeepAspect(false);
		plottingSystem.repaint();
	};

	@Focus
	public void setFocus() {
		if (plottingSystem!=null) plottingSystem.setFocus();
	}

	@PreDestroy
	public void dispose() {
		if (plottingSystem!=null) plottingSystem.dispose();
	}
}

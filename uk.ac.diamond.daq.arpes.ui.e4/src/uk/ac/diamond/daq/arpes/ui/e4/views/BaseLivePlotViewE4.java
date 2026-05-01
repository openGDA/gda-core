package uk.ac.diamond.daq.arpes.ui.e4.views;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.IObserver;
import uk.ac.diamond.daq.arpes.ui.e4.constants.ArpesUiConstants;
import uk.ac.diamond.daq.arpes.ui.e4.dispatcher.AbstractBaseArpesLiveDataDispatcher;
import uk.ac.diamond.daq.pes.api.LiveDataPlotUpdate;

public class BaseLivePlotViewE4 implements IObserver{
	private static final Logger logger = LoggerFactory.getLogger(BaseLivePlotViewE4.class);

	protected IPlottingSystem<Composite> plottingSystem;

	private volatile boolean isDisposed = false;

	@Inject
	@Named("dispatcherMap")
	Map<String, AbstractBaseArpesLiveDataDispatcher> dispatchers;

	@Inject
	@Named("tag")
	@Active
	@Optional
	String tag;

	@PostConstruct
	private void setDispatcher() {
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
	protected void updatePlot(LiveDataPlotUpdate dataUpdate) {

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

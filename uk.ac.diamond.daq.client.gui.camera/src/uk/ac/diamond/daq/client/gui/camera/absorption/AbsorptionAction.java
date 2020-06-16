package uk.ac.diamond.daq.client.gui.camera.absorption;

import java.util.function.Supplier;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.client.gui.camera.absorption.AbsorptionElement.ActionRegionROIListenerBuilder;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * Allows the user to draw a shape on the plotting system
 * @author Maurizio Nagni
 *
 */
class AbsorptionAction extends Action implements IRegionListener {

	private static final Logger logger = LoggerFactory.getLogger(AbsorptionAction.class);

	/**
	 * The state of the action
	 */
	enum ActionState {
		/**
		 * The region has been drawn
		 */
		CREATED,
		/**
		 * The region can be drawn
		 */
		READY 
	}

	/**
	 * The shape drawn by this action
	 */
	private final IRegion.RegionType regionType;
	/**
	 * The action label
	 */
	private final ClientMessages actionLabel;
	private final Color regionColor;
	private final String regionName;
	/**
	 * A listener builder parametrized on the newly created region
	 */
	private final ActionRegionROIListenerBuilder roiListenerBuilder;	
	/**
	 * Using a supplier allows a dynamic link to the plotting system, i.e. the stream still not started
	 */
	private final Supplier<IPlottingSystem<Composite>> plottingSystem;

	
	/**
	 * The region drawn 
	 */
	private IRegion region;
	/**
	 * The instantiated region listener.
	 */
	private IROIListener roiListener;

	/**
	 * Keep the state of the action 
	 */
	private ActionState state = ActionState.READY;



	AbsorptionAction(ClientMessages actionLabel, IRegion.RegionType regionType, Color regionColor, String regionName,
			ActionRegionROIListenerBuilder roiListenerBuilder, Supplier<IPlottingSystem<Composite>> plottingSystem) {
		this.actionLabel = actionLabel;
		this.regionType = regionType;
		this.regionColor = regionColor;
		this.regionName = regionName;
		this.roiListenerBuilder = roiListenerBuilder;
		this.plottingSystem = plottingSystem;
		setText(ClientMessagesUtility.getMessage(actionLabel));
		setEnabled(true);
	}

	@Override
	public void run() {
		try {
			// the action only create the region. Remove action is managed by
			// SelectionRegionFactory
			createRegion();
		} catch (GDAClientException e) {
			UIHelper.showError(e.getMessage(), e, logger);
		}
	}

	private void createRegion() throws GDAClientException {
		try {
			region = plottingSystem.get().createRegion(regionName, regionType);
			region.setRegionColor(regionColor);
			roiListener = roiListenerBuilder.build(region);
			region.addROIListener(roiListener);
			setActionLabel("Remove Region");
			state = ActionState.CREATED;
			firePropertyChange(state.name(), ActionState.READY, ActionState.CREATED);
		} catch (Exception e) {
			throw new GDAClientException("Cannot create region", e);
		}
	}

	private void setActionLabel(String text) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(() -> this.setText(text));
	}

	@Override
	public void regionRemoved(RegionEvent evt) {
		if (evt.getRegion().equals(this.region)) {
			region.removeROIListener(roiListener);
			setActionLabel(ClientMessagesUtility.getMessage(actionLabel));
			state = ActionState.READY;
			firePropertyChange(state.name(),  ActionState.CREATED, ActionState.READY);
		}
	}

	@Override
	public void regionCreated(RegionEvent evt) {
		// Not used
	}

	@Override
	public void regionCancelled(RegionEvent evt) {
		// Not used
	}

	@Override
	public void regionNameChanged(RegionEvent evt, String oldName) {
		// Not used
	}

	@Override
	public void regionAdded(RegionEvent evt) {
		// Not used
	}

	@Override
	public void regionsRemoved(RegionEvent evt) {
		// Not used
	}
}

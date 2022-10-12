package uk.ac.diamond.daq.mapping.ui;

import java.util.Optional;
import java.util.UUID;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.mapping.ui.services.MappingRemoteServices;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.spring.ClientRemoteServices;

/**
 * Creates an {@link Action} into the {@link IPlottingSystem} context menu to shows, or not, the mapping livestream.
 *
 * @author Maurizio Nagni
 */
public class LiveStreamBackgroundAction {

	private LiveBackgroundAction liveBackgroundAction;

	private enum ActionMode {
		SHOW, HIDE
	}

	public LiveStreamBackgroundAction(BackgroundStateHelper helper) {
		initialise(helper);
	}

	// This is still necessary as the spring creation happens before the IPlottingService is available.
	private void initialise(BackgroundStateHelper helper) {
		liveBackgroundAction = new LiveBackgroundAction(helper);
		liveBackgroundAction.run();
		getMappingRemoteServices().getIMapFileController().addListener(mappedDataFile -> getLiveBackgroundAction().updateAction());
	}

	private class LiveBackgroundAction extends Action {
		private ActionMode mode;
		private final BackgroundStateHelper helper;

		private LiveBackgroundAction(BackgroundStateHelper helper) {
			this.setId(UUID.randomUUID().toString());
			this.mode = ActionMode.SHOW;
			this.helper = helper;
		}

		private void updateAction() {
			if (helper.isPlotted()) {
				updateAction(ActionMode.HIDE);
			} else {
				updateAction(ActionMode.SHOW);
			}
		}

		@Override
		public void run() {
			if (mode == ActionMode.SHOW) {
				helper.show(true);
				updateAction(mode);
			} else if (mode == ActionMode.HIDE) {
				helper.show(false);
				updateAction(mode);
			}
		}

		private void updateAction(ActionMode mode) {
			if (mode == ActionMode.SHOW) {
				setText("Hide Imaging Camera Stream");
				this.mode = ActionMode.HIDE;
			} else if (mode == ActionMode.HIDE) {
				setText("Show Imaging Camera Stream");
				this.mode = ActionMode.SHOW;
			}
			Optional.ofNullable(getMap()).ifPresent(this::updateActionGUI);
		}

		private IPlottingSystem<Composite> getMap() {
			return SpringApplicationContextFacade.getBean(ClientRemoteServices.class)
					.getIPlottingService().getPlottingSystem("Map");
		}

		private void updateActionGUI(IPlottingSystem<Composite> plottingSystem) {
			plottingSystem.getPlotActionSystem().remove(getId());
			plottingSystem.getPlotActionSystem().addPopupAction(this);
		}
	}

	private LiveBackgroundAction getLiveBackgroundAction() {
		return liveBackgroundAction;
	}

	private MappingRemoteServices getMappingRemoteServices() {
		return SpringApplicationContextFacade.getBean(MappingRemoteServices.class);
	}
}
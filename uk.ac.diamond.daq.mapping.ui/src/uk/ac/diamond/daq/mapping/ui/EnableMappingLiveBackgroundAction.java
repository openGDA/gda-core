package uk.ac.diamond.daq.mapping.ui;

import java.util.Optional;
import java.util.UUID;

import org.dawnsci.mapping.ui.api.IMapFileController;
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
public class EnableMappingLiveBackgroundAction {

	public static EnableMappingLiveBackgroundAction appendContextMenuAction() {
		return new EnableMappingLiveBackgroundAction();
	}

	private enum ActionMode {
		SHOW, HIDE
	}

	private final BackgroundStateHelper helper = new BackgroundStateHelper();

	private EnableMappingLiveBackgroundAction() {
		Optional.ofNullable(getMappingRemoteServices().getIMapFileController())
			.ifPresent(this::addListerToMapFileController);
	}

	private void addListerToMapFileController(IMapFileController controller) {
		LiveBackgroundAction liveBackgroundAction = new LiveBackgroundAction();
		controller.addListener(df -> {
			if (helper.isPlotted()) {
				liveBackgroundAction.updateAction(ActionMode.HIDE);
			} else {
				liveBackgroundAction.updateAction(ActionMode.SHOW);
			}
		});
	}

	private class LiveBackgroundAction extends Action {
		private ActionMode mode;

		private LiveBackgroundAction() {
			this.setId(UUID.randomUUID().toString());
			this.mode = ActionMode.SHOW;
			run();
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
			getMap().ifPresent(this::updateActionGUI);
		}

		private Optional<IPlottingSystem<Composite>> getMap() {
			return Optional
					.ofNullable(getClientRemoteServices().getIPlottingService().getPlottingSystem("Map"));
		}

		private void updateActionGUI(IPlottingSystem<Composite> plottingSystem) {
			plottingSystem.getPlotActionSystem().remove(getId());
			plottingSystem.getPlotActionSystem().addPopupAction(this);
		}
	}

	private static MappingRemoteServices getMappingRemoteServices() {
		return SpringApplicationContextFacade.getBean(MappingRemoteServices.class);
	}

	private static ClientRemoteServices getClientRemoteServices() {
		return SpringApplicationContextFacade.getBean(ClientRemoteServices.class);
	}
}
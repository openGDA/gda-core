package uk.ac.diamond.daq.mapping.ui;

import java.util.Optional;
import java.util.UUID;

import org.dawnsci.mapping.ui.api.IMapFileController;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

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
		LiveBackgroundAction liveBackgroundAction = new LiveBackgroundAction();
		PlatformUI.getWorkbench().getService(IMapFileController.class).addListener(df -> {
			if (helper.isPlotted()) {
				liveBackgroundAction.updateAction(ActionMode.SHOW);
			} else {
				liveBackgroundAction.updateAction(ActionMode.HIDE);
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
					.ofNullable(PlatformUI.getWorkbench().getService(IPlottingService.class).getPlottingSystem("Map"));
		}

		private void updateActionGUI(IPlottingSystem<Composite> plottingSystem) {
			plottingSystem.getPlotActionSystem().remove(getId());
			plottingSystem.getPlotActionSystem().addPopupAction(this);
		}
	}
}
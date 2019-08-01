package uk.ac.gda.tomography.scan.editor.view;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.tomography.model.ActionLog;

public class LogsViewer {

	public static final ItemsViewer<ActionLog> createLogsViewer(Composite parent, int style, java.util.List<ActionLog> actionLogs,
			ItemViewerController<ActionLog> controller) {
		LogsViewer lv = new LogsViewer(parent, style, actionLogs, controller);
		return lv.getViewer();
	}

	private ItemsViewer<ActionLog> viewer;
	private final java.util.List<ActionLog> actionLogs;

	private LogsViewer(Composite parent, int style, java.util.List<ActionLog> actionLogs, ItemViewerController<ActionLog> controller) {
		this.actionLogs = actionLogs;
		viewer = new ItemsViewer<>(parent, style, getLogs(), controller);
	}

	public ItemsViewer<ActionLog> getViewer() {
		return this.viewer;
	}

	private Map<String, ActionLog> getLogs() {
		Map<String, ActionLog> logsMap = new HashMap<>();

		actionLogs.forEach(a -> {
			String key = String.format("%s: %s", a.getDate(), a.getNote());
			logsMap.put(key, a);
		});

		return logsMap;
	}

	private class LogsViewerController implements ItemViewerController<ActionLog> {

		@Override
		public ActionLog createItem() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ActionLog editItem(ActionLog item) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ActionLog deleteItem(ActionLog item) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getItemName(ActionLog item) {
			// TODO Auto-generated method stub
			return null;
		}
	};
}

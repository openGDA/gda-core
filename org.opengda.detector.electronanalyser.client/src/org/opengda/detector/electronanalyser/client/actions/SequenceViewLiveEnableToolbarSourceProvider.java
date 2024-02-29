package org.opengda.detector.electronanalyser.client.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequenceViewLiveEnableToolbarSourceProvider extends AbstractSourceProvider {

	public static final String SOURCE_NAME = "org.opengda.detector.electronanalyser.client.actions.sequenceviewlive.toolbar.enable";

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(SequenceViewLiveEnableToolbarSourceProvider.class);

	private Map<String, String> currentStates = new HashMap<>();

	private enum State {
		ENABLED, DISABLED
	}

	public SequenceViewLiveEnableToolbarSourceProvider() {
		setEnabled(true);
	}

	@Override
	public Map<String, String> getCurrentState() {
		return currentStates;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] {SOURCE_NAME};
	}

	public void setEnabled(boolean enabled) {
		State state;
		if (enabled) {
			state = State.ENABLED;
		}
		else {
			state = State.DISABLED;
		}
		currentStates.put(SOURCE_NAME, state.toString());
		fireSourceChanged(ISources.WORKBENCH, SOURCE_NAME, state.toString());
	}

	public boolean isEnabled() {
		return currentStates.get(SOURCE_NAME).equals(State.ENABLED.toString());
	}

	@Override
	public void dispose() {
	}
}
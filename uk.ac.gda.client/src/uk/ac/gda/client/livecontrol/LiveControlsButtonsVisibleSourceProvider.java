package uk.ac.gda.client.livecontrol;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveControlsButtonsVisibleSourceProvider extends AbstractSourceProvider {

	public static final String SOURCE_NAME = "uk.ac.gda.client.livecontrol.visible";
	public static final String STOP_BUTTON_VISIBLE = "uk.ac.gda.client.livecontrol.stopbutton.visible";
	public static final String LAYOUT_BUTTON_VISIBLE = "uk.ac.gda.client.livecontrol.layoutbutton.visible";
	public static final String INCREMENT_BUTTON_VISIBLE = "uk.ac.gda.client.livecontrol.incrementbutton.visible";

	private static final Logger logger = LoggerFactory.getLogger(LiveControlsButtonsVisibleSourceProvider.class);
	public static final String VISIBLE   = "VISIBLE";
	public static final String INVISIBLE = "INVISIBLE";

	enum State {
		VISIBLE, INVISIBLE
	}
	private Map<String, String> currentStates = new HashMap<>();

	public LiveControlsButtonsVisibleSourceProvider() {
		setLayoutButtonVisible(true);
		setIncrementButtonVisible(true);
		setStopButtonVisible(true);
	}

	@Override
	public void dispose() {
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] {SOURCE_NAME, STOP_BUTTON_VISIBLE, LAYOUT_BUTTON_VISIBLE, INCREMENT_BUTTON_VISIBLE};
	}

	@Override
	public Map<String, String> getCurrentState() {
		return currentStates;
	}

	private void setVisible(String sourceName, boolean visible) {
		logger.debug("setting {} to {}", sourceName, visible);

		if (visible) {
			currentStates.put(sourceName, VISIBLE);
			fireSourceChanged(ISources.WORKBENCH, sourceName, VISIBLE);
		}
		else {
			currentStates.put(sourceName, INVISIBLE);
			fireSourceChanged(ISources.WORKBENCH, sourceName, INVISIBLE);
		}
	}

	public void setLayoutButtonVisible(boolean value) {
		setVisible(LAYOUT_BUTTON_VISIBLE, value);
	}

	public void setStopButtonVisible(boolean value) {
		setVisible(STOP_BUTTON_VISIBLE, value);
	}

	public void setIncrementButtonVisible(boolean value) {
		setVisible(INCREMENT_BUTTON_VISIBLE, value);
	}
}
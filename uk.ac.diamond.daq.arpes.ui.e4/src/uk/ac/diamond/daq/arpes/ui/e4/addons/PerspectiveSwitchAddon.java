package uk.ac.diamond.daq.arpes.ui.e4.addons;

import java.util.Objects;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.osgi.service.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.JythonStatus;
import uk.ac.diamond.daq.pes.api.IElectronAnalyser;

public class PerspectiveSwitchAddon {
	private static final Logger logger = LoggerFactory.getLogger(PerspectiveSwitchAddon.class);
	final IElectronAnalyser analyser = Finder.find("analyser");

	@Inject
	@Optional
	public void subscribeTopicSelectedElement(
			@EventTopic(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT) Event event) {
		Object element = event.getProperty(EventTags.ELEMENT);
		Object newValue = event.getProperty(EventTags.NEW_VALUE);
		Object oldValue = event.getProperty(EventTags.OLD_VALUE);
		// ensure that the selected element of a perspective stack is changed and that
		// this is a perspective
		if (!(element instanceof MPerspectiveStack) || !(newValue instanceof MPerspective)
				|| !(oldValue instanceof MPerspective) || (Objects.equals(newValue, oldValue))) {
			return;
		}
		// Check if a scan is running if not stop the analyser
		if (InterfaceProvider.getScanStatusHolder().getScanStatus() == JythonStatus.IDLE) {
			try {
				analyser.zeroSupplies(); // Stop the analyser and zero supplies
			} catch (Exception e) {
				logger.error("Failed to zeroSupplies during perspective switch", e);
			}
		}
	}
}

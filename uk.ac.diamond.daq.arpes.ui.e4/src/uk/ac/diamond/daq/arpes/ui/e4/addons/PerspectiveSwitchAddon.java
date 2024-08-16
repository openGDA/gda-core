package uk.ac.diamond.daq.arpes.ui.e4.addons;

import java.util.Objects;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
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
	EModelService modelService;

	@Inject
	@Optional
	public void subscribeTopicSelectedElement(@EventTopic(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT) Event event) {
		// only run this, if the NEW_VALUE is a MPerspective
		if (!(event.getProperty(EventTags.NEW_VALUE) instanceof MPerspective) && (!Objects.equals(EventTags.OLD_VALUE, EventTags.NEW_VALUE))) {
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

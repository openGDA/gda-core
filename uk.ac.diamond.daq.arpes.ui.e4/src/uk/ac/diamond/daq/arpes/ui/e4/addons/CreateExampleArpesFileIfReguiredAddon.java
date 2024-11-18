package uk.ac.diamond.daq.arpes.ui.e4.addons;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import javax.inject.Inject;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.osgi.service.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.arpes.ui.e4.constants.ArpesUiConstants;

public class CreateExampleArpesFileIfReguiredAddon {
	private static final Logger logger = LoggerFactory.getLogger(CreateExampleArpesFileIfReguiredAddon.class);

	@Inject
	EModelService modelService;

	@Inject
	@Optional
	public void subscribeTopicSelectedElement(
			@EventTopic(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT) Event event) {
		Object newValue = event.getProperty(EventTags.NEW_VALUE);
		// only run this, if the NEW_VALUE is a MPerspective
		if (!(newValue instanceof MPerspective) && (!Objects.equals(EventTags.OLD_VALUE, EventTags.NEW_VALUE))) {
			return;
		}
		MPerspective newPerspective = (MPerspective) newValue;
		if (newPerspective.getElementId().contains(ArpesUiConstants.ARPES_EXPERIMENT_PERSPECTIVE_E4_ID)
			|| newPerspective.getElementId().contains(ArpesUiConstants.ARPES_SLICING_PERSPECTIVE_E4_ID)
			|| newPerspective.getElementId().contains(ArpesUiConstants.ARPES_SLICING_PERSPECTIVE_E3_ID)) {
			createExampleArpesFileIfRequired();
		}
	}

	protected void createExampleArpesFileIfRequired() {

		// Find the target location for the example .arpes file
		final String tgtDataRootPath = InterfaceProvider.getPathConstructor()
				.createFromProperty("gda.analyser.sampleConf.dir");
		final String exampleFileName = LocalProperties.get("gda.analyser.sampleConf");
		final File targetFile = new File(tgtDataRootPath, exampleFileName);

		// Find the full path to initialExampleAnalyserConfig.arpes in the config
		String configDir = LocalProperties.getConfigDir();
		File exampleFile = new File(configDir, exampleFileName);

		// Example file doesn't exist so copy it
		if (!targetFile.exists()) {
			try {
				Files.createDirectories(targetFile.toPath().getParent());
				Files.copy(exampleFile.toPath(), targetFile.toPath());
			} catch (IOException e) {
				logger.error("Failed to create directory/copy file", e);
			}
		} else {
		}

		// Open the example in the editor
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(targetFile.toURI());
		try {
			IDE.openEditorOnFileStore(page, fileStore);
		} catch (PartInitException e) {
			logger.error("Failed to open editor on file store", e);
		}

	}
}

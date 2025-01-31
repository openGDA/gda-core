package uk.ac.diamond.daq.arpes.ui.e4.addons;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
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
	private Map<String, Boolean> perspectiveStartupMap = new HashMap<>();
	private static final Set<String> SET_OF_PERSPECTIVES = Set.of(ArpesUiConstants.ARPES_EXPERIMENT_PERSPECTIVE_E4_ID,
			ArpesUiConstants.ARPES_SLICING_PERSPECTIVE_E4_ID, ArpesUiConstants.ARPES_SLICING_PERSPECTIVE_E3_ID);

	public CreateExampleArpesFileIfReguiredAddon() {
		SET_OF_PERSPECTIVES.stream().forEach(id -> perspectiveStartupMap.put(id, true));
	}

	@Inject
	@Optional
	public void subscribeTopicSelectedElement(
			@EventTopic(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT) Event event) {
		Object element = event.getProperty(EventTags.NEW_VALUE);
		if ((element instanceof MPerspective perspective)
				&& (SET_OF_PERSPECTIVES.contains(perspective.getElementId()))) {
			checkCreateExampleArpesFile(perspective);
		}
	}

	@Inject
	@Optional
	public void subscribeTopicPerspectiveOpened(@UIEventTopic(UIEvents.UILifeCycle.PERSPECTIVE_OPENED) Event event) {
		logger.debug("UIEvents.UILifeCycle.PERSPECTIVE_OPENED event for {}",event.getProperty(EventTags.ELEMENT) );
		Object element = event.getProperty(EventTags.ELEMENT);
		if ((element instanceof MPerspective perspective)
				&& (SET_OF_PERSPECTIVES.contains(perspective.getElementId()))) {
			createExampleArpesFileIfRequired();
		}
	}

	@Inject
	@Optional
	public void subscribeTopicPerspectiveReset(@UIEventTopic(UIEvents.UILifeCycle.PERSPECTIVE_RESET) Event event) {
		Object element = event.getProperty(EventTags.ELEMENT);
		if ((element instanceof MPerspective perspective)
				&& (SET_OF_PERSPECTIVES.contains(perspective.getElementId()))) {
			createExampleArpesFileIfRequired();
		}
	}

	private void checkCreateExampleArpesFile(MPerspective perspective) {
		if (SET_OF_PERSPECTIVES.stream().filter(perspectiveStartupMap::get)
				.anyMatch(name -> name.contains(perspective.getElementId()))) {
			logger.debug("CreateExampleArpesFile called");
			createExampleArpesFileIfRequired();
			perspectiveStartupMap.put(perspective.getElementId(), false);
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

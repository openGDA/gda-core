package uk.ac.diamond.daq.mapping.ui.experiment;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.OpenRequest;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.IDetectorModelWrapper;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBeanProvider;
import uk.ac.diamond.daq.mapping.impl.MappingExperimentBean;

/**
 * An E4-style POJO class for the mapping experiment view. This allows all dependencies to be injected (currently by a
 * ViewPart instance until we have annotation-based injection available). Ideally that would make this class
 * unit-testable, but usage of the GuiGeneratorService is currently too extensive to allow easy mocking, and the real
 * service cannot be obtained without breaking encapsulation or running in an OSGi framework.
 */
public class MappingExperimentView implements IAdaptable {

	public static final String PROPERTY_NAME_MAPPING_SCAN = "mappingScan";

	private static final String STATE_KEY_MAPPING_BEAN_JSON = "mappingBean.json";

	/**
	 * These classes will be created on a scrollable composite (not always visible)
	 */
	@SuppressWarnings("unchecked")
	private static final Class<? extends AbstractMappingSection>[] SCROLLED_SECTION_CLASSES = new Class[] {
			// a section for configuring scannables to be moved to a particular position at the start of a scan
			BeamlineConfigurationSection.class,
			// a section for configuring scripts to be run before and after a scan
			ScriptFilesSection.class,
			// a section for configuring outer scannables (i.e. in addition to the inner map)
			OuterScannablesSection.class,
			// a section for choosing the detectors (or malcolm device) to include in the scan
			DetectorsSection.class,
			// a section for configuring the path of the mapping scan
			RegionAndPathSection.class,
			// a section for configuring metadata to add to the scan
			ScanMetadataSection.class,
			// a section for configuring live processing to run
			ProcessingSection.class
	};

	/**
	 * These classes are always visible
	 */
	@SuppressWarnings("unchecked")
	private static final Class<? extends AbstractMappingSection>[] UNSCROLLED_SECTION_CLASSES = new Class[] {
			// a section for submitting the scan to the queue
			SubmitScanSection.class
	};

	private static final Logger logger = LoggerFactory.getLogger(MappingExperimentView.class);

	private IMappingExperimentBean mappingBean = null;

	private StatusPanel statusPanel;

	@Inject
	private PlottingController plotter;
	@Inject
	private BeamPositionPlotter beamPositionPlotter;
	@Inject
	private IEclipseContext injectionContext;
	@Inject
	private ScanRequestConverter scanRequestConverter;

	private ScrolledComposite scrolledComposite;

	private Composite mainComposite;

	private IRunnableDeviceService runnableDeviceService;

	private Map<Class<? extends AbstractMappingSection>, AbstractMappingSection> sections;

	@Inject
	public MappingExperimentView(IMappingExperimentBeanProvider beanProvider) {
		if (beanProvider == null) {
			throw new NullPointerException("beanProvider must not be null");
		} else {
			setMappingBean(beanProvider.getMappingExperimentBean());
		}
	}

	public Shell getShell() {
		return (Shell) injectionContext.get(IServiceConstants.ACTIVE_SHELL);
	}

	@Focus
	public void setFocus() {
		if (sections != null) {
			sections.get(RegionAndPathSection.class).setFocus();
		}
	}

	@PostConstruct
	public void createView(Composite parent, MPart part) {
		// It'd really be better if the beam position plotter could initialise itself when the map plot view was
		// created, but there doesn't seem to be a good way to hook into that, so we use the creation of the GUI
		// elements for this view as a proxy since it happens at around the same time.
		beamPositionPlotter.init();

		loadPreviousState(part);

		logger.trace("Starting to build the mapping experiment view");

		GridLayoutFactory.fillDefaults().applyTo(parent);
		GridDataFactory.fillDefaults().applyTo(parent);
		parent.setBackgroundMode(SWT.INHERIT_FORCE);

		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().applyTo(scrolledComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(scrolledComposite);

		Composite alwaysVisible = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(alwaysVisible);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(alwaysVisible);

		mainComposite = new Composite(scrolledComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().spacing(SWT.DEFAULT, 1).applyTo(mainComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(mainComposite);

		scrolledComposite.setContent(mainComposite);

		// Separator to distinguish between mainComposite and alwaysVisible composites
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(
				new Label(alwaysVisible, SWT.SEPARATOR | SWT.HORIZONTAL));

		// Make the status bar label
		createStatusPanel(alwaysVisible);

		if (mappingBean == null) {
			logger.error("Error getting mapping configuration, no mapping bean set");
		} else {
			// create the controls for sections that should be shown
			createSections(mainComposite, SCROLLED_SECTION_CLASSES, part.getPersistedState());
			createSections(alwaysVisible, UNSCROLLED_SECTION_CLASSES, part.getPersistedState());
		}

		mainComposite.pack();
		logger.trace("Finished building the mapping experiment view");
	}

	private void loadPreviousState(MPart part) {
		String json = part.getPersistedState().get(STATE_KEY_MAPPING_BEAN_JSON);
		if (json != null) {
			logger.trace("Restoring the previous state of the mapping view.");
			IMarshallerService marshaller = injectionContext.get(IMarshallerService.class);
			try {
				mappingBean = marshaller.unmarshal(json, MappingExperimentBean.class);
			} catch (Exception e) {
				logger.error("Failed to restore the previous state of the mapping view", e);
			}
		}
	}

	@PersistState
	public void saveState(MPart part) {
		// serialize the json bean and save it in the preferences
		IMarshallerService marshaller = injectionContext.get(IMarshallerService.class);
		try {
			logger.trace("Saving the current state of the mapping view.");
			String json = marshaller.marshal(mappingBean);
			part.getPersistedState().put(STATE_KEY_MAPPING_BEAN_JSON, json);
		} catch (Exception e) {
			logger.error("Could not save current the state of the mapping view.", e);
		}

		// Now save any other persistent data that is outside the mapping bean
		for (AbstractMappingSection section : sections.values()) {
			section.saveState(part.getPersistedState());
		}
	}

	private void createSections(Composite parent, Class<? extends AbstractMappingSection>[] classes, Map<String, String> persistedState) {
		if (sections==null) sections = new HashMap<>();
		for (Class<? extends AbstractMappingSection> sectionClass : classes) {
			AbstractMappingSection section;
			try {
				section = sectionClass.newInstance();
				section.initialize(this);
				sections.put(sectionClass, section);

				if (section.shouldShow()) {
					// create separator if this section should have one, unless its the first section
					if (section.createSeparator() && sections.size() > 1) {
						GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(
								new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL));
					}
					section.loadState(persistedState);
					section.createControls(parent);
				}
			} catch (InstantiationException | IllegalAccessException e) {
				logger.error("Could not create section " + sectionClass.getSimpleName(), e);
			}
		}
	}

	@PreDestroy
	public void dispose() {
		plotter.dispose();
		beamPositionPlotter.dispose();
	}

	private void createStatusPanel(final Composite mainComposite) {
		statusPanel = new StatusPanel(mainComposite, SWT.NONE, mappingBean, scanRequestConverter);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(statusPanel);

		if (mappingBean == null) {
			statusPanel.setMessage("Error getting mapping experiment definition");
		}
	}

	@Inject
	@Optional
	private void updateUiWithPathInfo(@UIEventTopic(PathInfoCalculatorJob.PATH_CALCULATION_TOPIC) PathInfo pathInfo) {
		statusPanel.setPathInfo(pathInfo);
		plotter.plotPath(pathInfo);
	}

	@Inject
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) IStructuredSelection selection) {
		if (selection != null && selection.getFirstElement() instanceof OpenRequest) {
			handleOpenRequest((OpenRequest) selection.getFirstElement());
		}
	}

	private boolean isMappingScanBean(StatusBean statusBean) {
		return statusBean instanceof ScanBean &&
				Boolean.TRUE.toString().equals(statusBean.getProperty(PROPERTY_NAME_MAPPING_SCAN));
	}

	/**
	 * @param openRequest
	 */
	private void handleOpenRequest(OpenRequest openRequest) {
		if (!isMappingScanBean(openRequest.getStatusBean())) {
			return;
		}

		ScanBean scanBean = (ScanBean) openRequest.getStatusBean();
		String scanName = scanBean.getName();
		logger.info("Open Request", "Received an open request for ScanBean with the name: %s", scanName);

		// Confirm whether this scan should be opened as it will overwrite the contents of the view
		Shell shell = (Shell) injectionContext.get(IServiceConstants.ACTIVE_SHELL);
		boolean confirm = MessageDialog.openConfirm(shell, "Open Mapping Scan",
				MessageFormat.format("Do you want to open the scan ''{0}'' in the Mapping Experiment Setup view?\n"
				+ "This will overwrite the current contents of this view.", scanName));
		if (!confirm) {
			return;
		}

		// Get the scan request and merge it into the mapping bean
		@SuppressWarnings("unchecked")
		ScanRequest<IROI> scanRequest = (ScanRequest<IROI>) scanBean.getScanRequest();
		try {
			scanRequestConverter.mergeIntoMappingBean(scanRequest, (MappingExperimentBean) mappingBean);
			updateControls();
		} catch (Exception e) {
			String errorMessage = MessageFormat.format(
					"Could not open scan {0}. Could not recreate the mapping view from the queued scan. See the error log for more details.", scanName);
			MessageDialog.openError(shell, "Open Results", errorMessage);
			logger.error("Error merging scan request into mapping bean.", e);
		}
	}

	public void setMappingBean(IMappingExperimentBean bean) {
		mappingBean = bean;
	}

	public void updateControls() {
		sections.values().forEach(AbstractMappingSection::updateControls);
		relayout();
	}

	public IEclipseContext getEclipseContext() {
		return injectionContext;
	}

	protected Composite getMainComposite() {
		return mainComposite;
	}

	protected IMappingExperimentBean getBean() {
		return mappingBean;
	}

	protected StatusPanel getStatusPanel() {
		return statusPanel;
	}

	protected void recalculateMinimumSize() {
		scrolledComposite.setMinSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	protected void relayout() {
		mainComposite.layout(true, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == ScanRequest.class) {
			try {
				return (T) scanRequestConverter.convertToScanRequest(mappingBean);
			} catch (ScanningException e) {
				logger.error("Could not create scan request", e);
			}
		}

		return null;
	}

	public IRunnableDeviceService getRunnableDeviceService() throws EventException {
		if (runnableDeviceService == null) {
			IEventService eventService = injectionContext.get(IEventService.class);
			try {
				URI jmsURI = new URI(LocalProperties.getActiveMQBrokerURI());
				return  eventService.createRemoteService(jmsURI, IRunnableDeviceService.class);
			} catch (URISyntaxException e) {
				throw new EventException("Malformed URI for activemq", e);
			}
		}

		return runnableDeviceService;
	}

	public void detectorSelectionChanged(List<IDetectorModelWrapper> selectedDetectors) {
		RegionAndPathSection section = (RegionAndPathSection) sections.get(RegionAndPathSection.class);
		if (Objects.isNull(section)) return;
		section.detectorsChanged(selectedDetectors);
	}

}

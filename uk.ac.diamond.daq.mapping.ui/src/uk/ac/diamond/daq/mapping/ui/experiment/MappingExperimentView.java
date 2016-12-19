package uk.ac.diamond.daq.mapping.ui.experiment;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IDetectorModelWrapper;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBeanProvider;

/**
 * An E4-style POJO class for the mapping experiment view. This allows all dependencies to be injected (currently by a
 * ViewPart instance until we have annotation-based injection available). Ideally that would make this class
 * unit-testable, but usage of the GuiGeneratorService is currently too extensive to allow easy mocking, and the real
 * service cannot be obtained without breaking encapsulation or running in an OSGi framework.
 */
public class MappingExperimentView implements IAdaptable {

	public static final String PROPERTY_NAME_MAPPING_SCAN = "mappingScan";

	@SuppressWarnings("unchecked")
	private static final Class<? extends AbstractMappingSection>[] SECTION_CLASSES = new Class[] {
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
			ProcessingSection.class,
			// a section for submitting the scan to the queue
			SubmitScanSection.class
	};

	private static final Logger logger = LoggerFactory.getLogger(MappingExperimentView.class);

	private IMappingExperimentBean experimentBean;

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

	private Map<Class<? extends AbstractMappingSection>, AbstractMappingSection> sections;

	@Inject
	public MappingExperimentView(IMappingExperimentBeanProvider beanProvider) {
		if (beanProvider != null) {
			experimentBean = beanProvider.getMappingExperimentBean();
		} else {
			experimentBean = null;
			logger.error("A mapping experiment bean provider is required - check Spring and OSGi configuration");
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
	public void createView(Composite parent) {
		// It'd really be better if the beam position plotter could initialise itself when the map plot view was
		// created, but there doesn't seem to be a good way to hook into that, so we use the creation of the GUI
		// elements for this view as a proxy since it happens at around the same time.
		beamPositionPlotter.init();

		logger.trace("Starting to build the mapping experiment view");

		mainComposite = createMainComposite(parent);
		// Make the status bar label
		createStatusPanel(mainComposite);
		if (experimentBean == null) {
			logger.error("Error getting mapping configuration, no mapping bean set");
		} else {
			// create the controls for sections that should be shown
			createSections(mainComposite);
		}

		logger.trace("Finished building the mapping experiment view");
	}

	private void createSections(Composite parent) {
		sections = new HashMap<>();
		for (Class<? extends AbstractMappingSection> sectionClass : SECTION_CLASSES) {
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

	private Composite createMainComposite(Composite parent) {
		parent.setLayout(new FillLayout());
		parent.setBackgroundMode(SWT.INHERIT_FORCE); // stop the ScrolledComposite being grey regardless of theme colour
		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		final Composite mainComposite = new Composite(scrolledComposite, SWT.NONE);
		scrolledComposite.setContent(mainComposite);

		GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 0).applyTo(mainComposite);
		return mainComposite;
	}

	private void createStatusPanel(final Composite mainComposite) {
		statusPanel = new StatusPanel(mainComposite, SWT.NONE, this);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(statusPanel);

		if (experimentBean == null) {
			statusPanel.setMessage("Error getting mapping experiment definition");
		}
	}

	@Inject
	@Optional
	private void updateUiWithPathInfo(@UIEventTopic(PathInfoCalculatorJob.PATH_CALCULATION_TOPIC) PathInfo pathInfo) {
		statusPanel.setPathInfo(pathInfo);
		plotter.plotPath(pathInfo);
	}

	double getPointExposureTime() {
		double exposure = 0.0;
		for (IDetectorModelWrapper detectorParameters : experimentBean.getDetectorParameters()) {
			if (detectorParameters.isIncludeInScan()) {
				exposure = Math.max(exposure, detectorParameters.getModel().getExposureTime());
			}
		}
		return exposure;
	}

	public IEclipseContext getEclipseContext() {
		return injectionContext;
	}

	protected Composite getMainComposite() {
		return mainComposite;
	}

	protected IMappingExperimentBean getBean() {
		return experimentBean;
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
			return (T) scanRequestConverter.convertToScanRequest(experimentBean);
		}

		return null;
	}

}

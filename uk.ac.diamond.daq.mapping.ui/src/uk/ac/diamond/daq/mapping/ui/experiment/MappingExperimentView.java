/*-
 * Copyright © 2018 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.daq.mapping.ui.experiment;

import static java.util.Arrays.asList;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
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
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.OpenRequest;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.points.models.IMapPathModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBeanProvider;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.impl.MappingExperimentBean;

/**
 * An E4-style POJO class for the a view containing several sections view.
 * <p>
 * This allows all dependencies to be injected (currently by a ViewPart instance until we have annotation-based
 * injection available). Ideally that would make this class unit-testable, but usage of the GuiGeneratorService is
 * currently too extensive to allow easy mocking, and the real service cannot be obtained without breaking encapsulation
 * or running in an OSGi framework.
 */
public class MappingExperimentView implements IAdaptable {

	public static final String ID = "uk.ac.diamond.daq.mapping.ui.experiment.mappingExperimentView";

	private static final String STATE_KEY_MAPPING_BEAN_JSON = "mappingBean.json";

	private static final Logger logger = LoggerFactory.getLogger(MappingExperimentView.class);

	private IMappingExperimentBeanProvider mappingBeanProvider = null;

	private StatusPanel statusPanel;

	@Inject
	private IEclipseContext injectionContext;
	@Inject
	private ScanRequestConverter scanRequestConverter;

	private ScrolledComposite scrolledComposite;

	private Composite mainComposite;

	private IRunnableDeviceService runnableDeviceService;

	private final ClassToInstanceMap<IMappingSection> sections = MutableClassToInstanceMap.create();

	private final MappingViewConfiguration mappingViewConfiguration;

	@Inject
	public MappingExperimentView(IMappingExperimentBeanProvider beanProvider) {
		Objects.requireNonNull(beanProvider, "beanProvider must not be null");
		mappingBeanProvider = beanProvider;
		mappingViewConfiguration = PlatformUI.getWorkbench().getService(MappingViewConfiguration.class);
		Objects.requireNonNull(mappingViewConfiguration, "Cannot get MappingViewConfiguration");
	}

	public Shell getShell() {
		return (Shell) injectionContext.get(IServiceConstants.ACTIVE_SHELL);
	}

	@Focus
	public void setFocus() {
		handleSetFocus();
	}

	protected void handleSetFocus() {
		if (sections != null) {
			sections.get(RegionAndPathSection.class).setFocus();
		}
	}

	@PostConstruct
	public void createView(Composite parent, MPart part) {

		loadPreviousState(part);

		final IMappingExperimentBean mappingBean = mappingBeanProvider.getMappingExperimentBean();
		if (mappingBean == null) {
			showError("No mapping bean", "Error getting mapping configuration, no mapping bean set");
			return;
		}

		logger.trace("Starting to build the mapping experiment view");

		GridLayoutFactory.fillDefaults().applyTo(parent);
		GridDataFactory.fillDefaults().applyTo(parent);
		parent.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
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

		// create the controls for sections that should be shown
		createSections(mainComposite, getScrolledSections(), part.getPersistedState());
		createSections(alwaysVisible, getUnscrolledSections(), part.getPersistedState());
		recalculateMinimumSize();

		// Check that there is a status panel
		statusPanel = getSection(StatusPanel.class);
		if (statusPanel == null) {
			showError("No status panel", "No status panel defined for this view");
			return;
		}
		statusPanel.setMappingBean(mappingBean);

		// If there is a SubmitScanSelector section, allow the user to switch between submit sections
		final SubmitScanSelector submitScanSelector = getSection(SubmitScanSelector.class);
		if (submitScanSelector != null && submitScanSelector.getNumberOfSections() > 1) {
			addSubmitSectionSelectionButton(part, submitScanSelector);
		}

		mainComposite.pack();
		logger.trace("Finished building the mapping experiment view");
	}

	private void showError(String title, String message) {
		logger.error(message);
		MessageDialog.openError(getShell(), title, message);
	}

	/**
	 * Create a button on the toolbar to display a dialog that allows the user to choose a submit section
	 *
	 * @param part
	 *            the view part on which to create the button
	 * @param submitScanSelector
	 *            the selector containing the submit sections that the user can choose
	 */
	private void addSubmitSectionSelectionButton(MPart part, SubmitScanSelector submitScanSelector) {
		final String buttonId = "selectionSelectorId";
		final String iconUri = "platform:/plugin/uk.ac.diamond.daq.mapping.ui/icons/map--pencil.png";
		final String dialogDescription = "Choose scan type";

		// Create toolbar if necessary
		MToolBar toolBar = part.getToolbar();
		if (toolBar == null) {
			toolBar = MMenuFactory.INSTANCE.createToolBar();
			part.setToolbar(toolBar);
		}

		// Remove any existing buttons for this function
		final List<MToolBarElement> toolbarChildren = toolBar.getChildren();
		toolbarChildren.removeIf(e -> e.getElementId().equals(buttonId));

		// Create button to pop up a dialog so the user can select a submit section
		final MDirectToolItem element = MMenuFactory.INSTANCE.createDirectToolItem();
		element.setElementId(buttonId);
		element.setIconURI(iconUri);
		element.setLabel(dialogDescription);
		element.setTooltip(dialogDescription);
		element.setObject(new SelectionHandler(submitScanSelector));
		toolbarChildren.add(element);
	}

	/**
	 * Class to show a dialog where the user can select a submit section, and to display the selected section
	 */
	private class SelectionHandler {
		private final SubmitScanSelector submitScanSelector;

		public SelectionHandler(SubmitScanSelector submitScanSelector) {
			this.submitScanSelector = submitScanSelector;
		}

		@Execute
		public void execute(Shell shell) {
			final List<String> descriptions = submitScanSelector.getDescriptions();
			final int currentSelection = submitScanSelector.getCurrentSectionIndex();

			final ListDialog dialog = new ListDialog(shell);
			dialog.setTitle("Choose the scan type");
			dialog.setContentProvider(new ArrayContentProvider());
			dialog.setLabelProvider(new LabelProvider());
			dialog.setInput(submitScanSelector.getDescriptions());
			// Select the name of the section that is currently visible
			dialog.setInitialElementSelections(asList(descriptions.get(currentSelection)));

			if (dialog.open() == Window.OK) {
				final Object[] result = dialog.getResult();
				submitScanSelector.showSection(descriptions.indexOf(result[0]));
			}
		}
	}

	/**
	 * These sections will be created on a scrollable composite (not always visible)
	 */
	protected List<IMappingSection> getScrolledSections() {
		return mappingViewConfiguration.getScrolledSections();
	}

	/**
	 * These sections are always visible
	 */
	protected List<IMappingSection> getUnscrolledSections() {
		return mappingViewConfiguration.getUnscrolledSections();
	}

	private void loadPreviousState(MPart part) {
		// Restore mapping bean unless it has been set by another view
		if (!mappingBeanProvider.isSetByView()) {
			final String json = part.getPersistedState().get(STATE_KEY_MAPPING_BEAN_JSON);
			if (json != null) {
				logger.trace("Restoring the previous state of the mapping view.");
				final IMarshallerService marshaller = injectionContext.get(IMarshallerService.class);
				try {
					setMappingBean(marshaller.unmarshal(json, MappingExperimentBean.class));
				} catch (Exception e) {
					logger.error("Failed to restore the previous state of the mapping view", e);
				}
			}
		}
	}

	@PersistState
	public void saveState(MPart part) {
		// serialize the json bean and save it in the preferences
		final IMarshallerService marshaller = injectionContext.get(IMarshallerService.class);
		try {
			logger.trace("Saving the current state of the mapping view.");
			final String json = marshaller.marshal(mappingBeanProvider.getMappingExperimentBean());
			part.getPersistedState().put(STATE_KEY_MAPPING_BEAN_JSON, json);
		} catch (Exception e) {
			logger.error("Could not save current the state of the mapping view.", e);
		}

		// Now save any other persistent data that is outside the mapping bean
		for (IMappingSection section : sections.values()) {
			section.saveState(part.getPersistedState());
		}
	}

	private void createSections(Composite parent, List<IMappingSection> sectionsToCreate, Map<String, String> persistedState) {
		for (IMappingSection section : sectionsToCreate) {
			section.initialize(this);
			sections.put(section.getClass(), section);

			if (section.shouldShow()) {
				section.loadState(persistedState);
				section.createControls(parent);
			}
		}
	}

	@PreDestroy
	public final void dispose() {
		disposeInternal();
	}

	protected void disposeInternal() {
		for (IMappingSection section : sections.values()) {
			section.dispose();
		}
	}

	@Inject
	@Optional
	private void updateUiWithPathInfo(@UIEventTopic(PathInfoCalculatorJob.PATH_CALCULATION_TOPIC) PathInfo pathInfo) {
		statusPanel.setPathInfo(pathInfo);
	}

	@Inject
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) IStructuredSelection selection) {
		if (selection != null && selection.getFirstElement() instanceof OpenRequest) {
			handleOpenRequest((OpenRequest) selection.getFirstElement());
		}
	}

	private boolean isMappingScanBean(StatusBean statusBean) {
		if (!(statusBean instanceof ScanBean)) return false;
		List<Object> models = ((ScanBean) statusBean).getScanRequest().getCompoundModel().getModels();
		boolean innerPathIs2D = models.get(models.size()-1) instanceof IMapPathModel;
		boolean outerPathsHave1Scannable = models.subList(0, models.size()-1).stream()
											.map(path -> ((IScanPathModel) path).getScannableNames())
											.allMatch(scannables -> scannables.size() == 1);

		return innerPathIs2D && outerPathsHave1Scannable;
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
		logger.info("Open Request, Received an open request for ScanBean with the name: {}", scanName);

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
			scanRequestConverter.mergeIntoMappingBean(scanRequest, mappingBeanProvider.getMappingExperimentBean());
			updateControls();
		} catch (Exception e) {
			logger.error("Error merging scan request into mapping bean.", e);
			final String errorMessage = MessageFormat.format(
					"Could not open scan {0}. Could not recreate the mapping view from the queued scan. See the error log for more details.", scanName);
			MessageDialog.openError(shell, "Open Results", errorMessage);
		}
	}

	public void setMappingBean(IMappingExperimentBean bean) {
		mappingBeanProvider.setMappingExperimentBean(bean);
		mappingBeanProvider.setSetByView(true);
	}

	public void updateControls() {
		for (IMappingSection section : sections.values()) {
			if (section.shouldShow()) {
				section.updateControls();
			}
		}
		relayout();
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractMappingSection> T getSection(Class<T> sectionClass) {
		return (T) sections.get(sectionClass);
	}

	public IEclipseContext getEclipseContext() {
		return injectionContext;
	}

	protected Composite getMainComposite() {
		return mainComposite;
	}

	protected IMappingExperimentBean getBean() {
		return mappingBeanProvider.getMappingExperimentBean();
	}

	public void updateStatusLabel() {
		if (statusPanel != null) {
			statusPanel.updateStatusLabel();
		}
	}

	public void setStatusMessage(String message) {
		if (statusPanel != null) {
			statusPanel.setMessage(message);
		}
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
			return (T) scanRequestConverter.convertToScanRequest(mappingBeanProvider.getMappingExperimentBean());
		}

		return null;
	}

	public IRunnableDeviceService getRunnableDeviceService() throws EventException {
		if (runnableDeviceService == null) {
			return (IRunnableDeviceService) getRemoteService(IRunnableDeviceService.class);
		}
		return runnableDeviceService;
	}

	public IScannableDeviceService getScannableDeviceService() throws EventException {
		return (IScannableDeviceService) getRemoteService(IScannableDeviceService.class);
	}

	private Object getRemoteService(Class<?> klass) throws EventException {
		IEventService eventService = injectionContext.get(IEventService.class);
		try {
			URI jmsURI = new URI(LocalProperties.getActiveMQBrokerURI());
			return eventService.createRemoteService(jmsURI, klass);
		} catch (URISyntaxException e) {
			throw new EventException("Malformed URI for activemq", e);
		}
	}

	public void detectorSelectionChanged(List<IScanModelWrapper<IDetectorModel>> selectedDetectors) {
		RegionAndPathSection section = (RegionAndPathSection) sections.get(RegionAndPathSection.class);
		if (Objects.isNull(section)) return;
		section.detectorsChanged(selectedDetectors);
	}

	protected void redrawRegionAndPathComposites() {
		RegionAndPathSection section = (RegionAndPathSection) sections.get(RegionAndPathSection.class);
		if (Objects.isNull(section)) return;
		section.rebuildMappingSection();

	}

}

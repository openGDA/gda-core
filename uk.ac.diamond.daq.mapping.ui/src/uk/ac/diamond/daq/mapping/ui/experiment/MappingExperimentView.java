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

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
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
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.OpenRequest;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.scanning.api.points.models.IMapPathModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBeanProvider;
import uk.ac.diamond.daq.mapping.api.IScanDefinition;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.api.document.scanpath.MappingPathInfo;
import uk.ac.diamond.daq.mapping.impl.MappingExperimentBean;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;
import uk.ac.diamond.daq.mapping.impl.ScanPathModelWrapper;
import uk.ac.diamond.daq.mapping.ui.AbstractSectionView;
import uk.ac.diamond.daq.mapping.ui.BackgroundStateHelper;
import uk.ac.diamond.daq.mapping.ui.ISectionView;
import uk.ac.diamond.daq.mapping.ui.IViewSection;
import uk.ac.diamond.daq.mapping.ui.LiveStreamBackgroundAction;

/**
 * An E4-style POJO class for the a view containing several sections view.
 * <p>
 * This allows all dependencies to be injected (currently by a ViewPart instance until we have annotation-based
 * injection available). Ideally that would make this class unit-testable, but usage of the GuiGeneratorService is
 * currently too extensive to allow easy mocking, and the real service cannot be obtained without breaking encapsulation
 * or running in an OSGi framework.
 */
public class MappingExperimentView extends AbstractSectionView<IMappingExperimentBean> implements IAdaptable {

	public static final String ID = "uk.ac.diamond.daq.mapping.ui.experiment.mappingExperimentView";

	public static final String SCAN_POINTS_CALCULATION_TOPIC = "uk/ac/diamond/daq/mapping/client/events/ScanPointsCalculationEvent";

	public static final String PATH_CALCULATION_TOPIC = "uk/ac/diamond/daq/mapping/client/events/PathCalculationEvent";

	private static final String STATE_KEY_MAPPING_BEAN_JSON = "mappingBean.json";

	private static final Logger logger = LoggerFactory.getLogger(MappingExperimentView.class);

	private IMappingExperimentBeanProvider mappingBeanProvider = null;

	private StatusPanel statusPanel;

	@Inject
	private ScanRequestConverter scanRequestConverter;

	private ScrolledComposite scrolledComposite;

	private Composite mainComposite;

	private IRunnableDeviceService runnableDeviceService;

	private final MappingViewConfiguration mappingViewConfiguration;

	@Inject
	public MappingExperimentView(IMappingExperimentBeanProvider beanProvider) {
		Objects.requireNonNull(beanProvider, "beanProvider must not be null");
		mappingBeanProvider = beanProvider;
		mappingViewConfiguration = PlatformUI.getWorkbench().getService(MappingViewConfiguration.class);
		Objects.requireNonNull(mappingViewConfiguration, "Cannot get MappingViewConfiguration");
	}

	@Focus
	public void setFocus() {
		handleSetFocus();
	}

	protected void handleSetFocus() {
		final RegionAndPathSection section = getSection(RegionAndPathSection.class);
		if (section != null) section.setFocus();
	}

	@Override
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

		var streamSourceConfig = getService(MappingStageInfo.class).getDefaultStreamSourceConfig();
		if(!streamSourceConfig.isEmpty()) {
			new LiveStreamBackgroundAction(new BackgroundStateHelper());
		}

		mainComposite.pack();
		logger.trace("Finished building the mapping experiment view");
	}

	private void showError(String title, String message) {
		logger.error(message);
		MessageDialog.openError(getShell(), title, message);
	}

	/**
	 * These sections will be created on a scrollable composite (not always visible)
	 */
	protected List<AbstractMappingSection> getScrolledSections() {
		return mappingViewConfiguration.getScrolledSections();
	}

	/**
	 * These sections are always visible
	 */
	protected List<AbstractMappingSection> getUnscrolledSections() {
		return mappingViewConfiguration.getUnscrolledSections();
	}

	private void loadPreviousState(MPart part) {
		// Restore mapping bean unless it has been set by another view
		if (!mappingBeanProvider.isSetByView()) {
			final String json = part.getPersistedState().get(STATE_KEY_MAPPING_BEAN_JSON);
			if (json != null) {
				logger.trace("Restoring the previous state of the mapping view.");
				final IMarshallerService marshaller = getEclipseContext().get(IMarshallerService.class);
				try {
					setMappingBean(marshaller.unmarshal(json, MappingExperimentBean.class));
				} catch (Exception e) {
					logger.error("Failed to restore the previous state of the mapping view", e);
				}
			} else {
				// If there is no state to restore, ensure that any default outer scannables are set
				final IScanDefinition scanDefinition = mappingBeanProvider.getMappingExperimentBean().getScanDefinition();
				if (scanDefinition == null) {
					return;
				}
				final List<String> defaultOuterScannables = scanDefinition.getDefaultOuterScannables();
				if (defaultOuterScannables.isEmpty()) {
					return;
				}
				final List<IScanModelWrapper<IAxialModel>> scanModels = defaultOuterScannables.stream()
						.map(scannable -> new ScanPathModelWrapper<IAxialModel>(scannable, null, false))
						.collect(Collectors.toList());
				scanDefinition.setOuterScannables(scanModels);
			}
		}
	}

	@Override
	@PersistState
	public void saveState(MPart part) {
		// serialize the json bean and save it in the preferences
		final IMarshallerService marshaller = getEclipseContext().get(IMarshallerService.class);
		try {
			logger.trace("Saving the current state of the mapping view.");
			final String json = marshaller.marshal(mappingBeanProvider.getMappingExperimentBean());
			part.getPersistedState().put(STATE_KEY_MAPPING_BEAN_JSON, json);
		} catch (Exception e) {
			logger.error("Could not save current the state of the mapping view.", e);
		}

		// Now save any other persistent data that is outside the mapping bean
		for (IViewSection<IMappingExperimentBean, ISectionView<IMappingExperimentBean>> section : getAllSections()) {
			section.saveState(part.getPersistedState());
		}
	}

	@Inject
	@Optional
	private void updateUiWithPathInfo(@UIEventTopic(PATH_CALCULATION_TOPIC) MappingPathInfo pathInfo) {
		if (ID.equals(pathInfo.getSourceId())) {
			statusPanel.setPathInfo(pathInfo);
		}
	}

	@Inject
	@Optional
	private void updateUiWithPath(@UIEventTopic(SCAN_POINTS_CALCULATION_TOPIC) ScanPointsCalculator scanPointsCalculator) {
		statusPanel.setScanPointsCalculator(scanPointsCalculator);
	}

	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) IStructuredSelection selection) {
		if (selection != null && selection.getFirstElement() instanceof OpenRequest) {
			handleOpenRequest((OpenRequest) selection.getFirstElement());
		}
	}

	private boolean isMappingScanBean(StatusBean statusBean) {
		if (!(statusBean instanceof ScanBean)) return false;
		List<IScanPointGeneratorModel> models = ((ScanBean) statusBean).getScanRequest().getCompoundModel().getModels();
		boolean innerPathIs2D = models.get(models.size()-1) instanceof IMapPathModel;
		boolean outerPathsHave1Scannable = models.subList(0, models.size()-1).stream()
											.map(IScanPathModel::getScannableNames)
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
		boolean confirm = MessageDialog.openConfirm(getShell(), "Open Mapping Scan",
				MessageFormat.format("Do you want to open the scan ''{0}'' in the Mapping Experiment Setup view?\n"
				+ "This will overwrite the current contents of this view.", scanName));
		if (!confirm) {
			return;
		}

		// If the scan bean contains a valid reference to a stored mapping bean, use this
		if (LocalProperties.isPersistenceServiceAvailable()) {
			final ScanManagementController smController = getEclipseContext().get(ScanManagementController.class);
			final java.util.Optional<IMappingExperimentBean> savedMappingBean = smController.loadScanMappingBean(scanBean.getMappingBeanId());
			if (savedMappingBean.isPresent()) {
				mappingBeanProvider.setMappingExperimentBean(savedMappingBean.get());
				updateControls();
				return;
			} else {
				logger.warn("No saved mapping bean found for scan id {}. Loading view from ScanRequest", scanBean.getMappingBeanId());
			}
		}

		// Otherwise, get the scan request and merge it into the mapping bean
		ScanRequest scanRequest = scanBean.getScanRequest();
		try {
			scanRequestConverter.mergeIntoMappingBean(scanRequest, mappingBeanProvider.getMappingExperimentBean());
			updateControls();
		} catch (Exception e) {
			logger.error("Error merging scan request into mapping bean.", e);
			final String errorMessage = MessageFormat.format(
					"Could not open scan {0}. Could not recreate the mapping view from the queued scan. See the error log for more details.", scanName);
			MessageDialog.openError(getShell(), "Open Results", errorMessage);
		}
	}

	public void setMappingBean(IMappingExperimentBean bean) {
		mappingBeanProvider.setMappingExperimentBean(bean);
		mappingBeanProvider.setSetByView(true);
	}

	protected Composite getMainComposite() {
		return mainComposite;
	}

	@Override
	public IMappingExperimentBean getBean() {
		return mappingBeanProvider.getMappingExperimentBean();
	}

	@Override
	public void updateStatusLabel() {
		if (statusPanel != null) {
			statusPanel.updateStatusLabel();
		}
	}

	@Override
	public void setStatusMessage(String message) {
		if (statusPanel != null) {
			statusPanel.setMessage(message);
		}
	}

	protected void recalculateMinimumSize() {
		scrolledComposite.setMinSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	@Override
	public void relayout() {
		mainComposite.layout(true, true);
		recalculateMinimumSize();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == ScanRequest.class) {
			return (T) scanRequestConverter.convertToScanRequest(mappingBeanProvider.getMappingExperimentBean());
		}

		return null;
	}

	public IRunnableDeviceService getRunnableDeviceService() {
		if (runnableDeviceService == null) {
			return getRemoteService(IRunnableDeviceService.class);
		}
		return runnableDeviceService;
	}

	public IScannableDeviceService getScannableDeviceService() {
		return getRemoteService(IScannableDeviceService.class);
	}

	public void detectorSelectionChanged(List<IScanModelWrapper<IDetectorModel>> selectedDetectors) {
		RegionAndPathSection section = getSection(RegionAndPathSection.class);
		if (Objects.isNull(section)) return;
		section.detectorsChanged(selectedDetectors);
	}

	protected void redrawRegionAndPathComposites() {
		RegionAndPathSection section = getSection(RegionAndPathSection.class);
		if (Objects.isNull(section)) return;
		section.rebuildMappingSection();
	}

	public void showControl(Control control) {
		scrolledComposite.showControl(control);
	}

}

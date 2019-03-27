/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.scan.IParserService;
import org.eclipse.scanning.api.scan.ui.MonitorScanUIElement.MonitorScanRole;
import org.eclipse.scanning.device.ui.device.MonitorView;
import org.eclipse.scanning.device.ui.util.PageUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;

public abstract class AbstractMappingSection implements IMappingSection {
	private static final Logger logger = LoggerFactory.getLogger(AbstractMappingSection.class);

	/**
	 * Number of horizontal dialog units per character, value <code>4</code>.
	 */
	private static final int HORIZONTAL_DIALOG_UNIT_PER_CHAR = 4;

	private FontMetrics fontMetrics = null;

	private MappingExperimentView mappingView;

	private Label separator;

	protected DataBindingContext dataBindingContext;

	private boolean createSeparator = true;

	@Override
	public void initialize(MappingExperimentView mappingView) {
		this.mappingView = mappingView;
	}

	@Override
	public void createControls(Composite parent) {
		// Create the separator if required
		if (createSeparator) {
			separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
			GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(separator);
		}
	}

	protected Shell getShell() {
		return mappingView.getShell();
	}

	protected <S> S getService(Class<S> serviceClass) {
		return mappingView.getEclipseContext().get(serviceClass);
	}

	protected IRunnableDeviceService getRunnableDeviceService() throws EventException {
		return mappingView.getRunnableDeviceService();
	}

	protected IMappingExperimentBean getMappingBean() {
		return mappingView.getBean();
	}

	protected MappingExperimentView getMappingView() {
		return mappingView;
	}

	protected void relayoutMappingView() {
		mappingView.relayout();
		mappingView.recalculateMinimumSize();
	}

	protected IEclipseContext getEclipseContext() {
		return mappingView.getEclipseContext();
	}

	@Override
	public boolean shouldShow() {
		return true;
	}

	@Override
	public boolean createSeparator() {
		return createSeparator;
	}

	@Override
	public void setFocus() {
		// do nothing, subclasses may override
	}

	@Override
	public void dispose() {
		// do nothing, subclasses may override
	}

	protected void updateStatusLabel() {
		mappingView.updateStatusLabel();
	}

	protected void setStatusMessage(String message) {
		mappingView.setStatusMessage(message);
	}

	protected void setButtonLayoutData(Button button) {
		// copied from org.eclipse.jface.dialogs.Dialog. Gives buttons a standard minimum width
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = Math.max(widthHint, minSize.x);
		button.setLayoutData(data);
	}

	private int convertHorizontalDLUsToPixels(int dlus) {
		if (fontMetrics == null) {
			GC gc = new GC(mappingView.getMainComposite());
			gc.setFont(JFaceResources.getDialogFont());
			fontMetrics = gc.getFontMetrics();
			gc.dispose();
		}

		// round to the nearest pixel
		return (fontMetrics.getAverageCharWidth() * dlus + HORIZONTAL_DIALOG_UNIT_PER_CHAR / 2)
				/ HORIZONTAL_DIALOG_UNIT_PER_CHAR;
	}

	/**
	 * Updates this section based on the mapping bean.
	 */
	@Override
	public void updateControls() {
		// Default implementation does nothing. Subclasses may override.
	}

	/**
	 * Default implementation does nothing since most data is saved through the mapping bean
	 */
	@Override
	public void saveState(@SuppressWarnings("unused") Map<String, String> persistedState) {

	}

	/**
	 * Default implementation does nothing since most data is loaded through the mapping bean
	 */
	@Override
	public void loadState(@SuppressWarnings("unused") Map<String, String> persistedState) {

	}

	/**
	 * Show or hide the separator (if there is one)
	 * <p>
	 * You may have to call {@link #relayoutMappingView()} after doing this
	 *
	 * @param visible
	 *            true if the separator is to be shown, false if it is to be hidden
	 */
	protected void setSeparatorVisibility(boolean visible) {
		if (separator != null) {
			separator.setVisible(visible);
			((GridData) separator.getLayoutData()).exclude = !visible;
		}
	}

	/**
	 * Remove all existing bindings in {@link #dataBindingContext}
	 */
	protected void removeOldBindings() {
		if (dataBindingContext == null) {
			return;
		}

		// copy the bindings to prevent concurrent modification exception
		@SuppressWarnings("unchecked")
		final List<Binding> bindings = new ArrayList<>(dataBindingContext.getBindings());
		for (Binding binding : bindings) {
			dataBindingContext.removeBinding(binding);
			binding.dispose();
		}
	}

	protected String createScanCommand() {
		final ScanBean scanBean = createScanBean();
		final IParserService parserService = getEclipseContext().get(IParserService.class);
		try {
			return parserService.getCommand(scanBean.getScanRequest(), true);
		} catch (Exception e) {
			final String message = "Error creating scan commmand";
			logger.error(message, e);
			return message;
		}
	}

	protected ScanBean createScanBean() {
		final IMappingExperimentBean mappingBean = getMappingBean();
		addMonitors(mappingBean);

		final ScanBean scanBean = new ScanBean();
		String sampleName = mappingBean.getSampleMetadata().getSampleName();
		if (sampleName == null || sampleName.length() == 0) {
			sampleName = "unknown sample";
		}
		final String pathName = mappingBean.getScanDefinition().getMappingScanRegion().getScanPath().getName();
		scanBean.setName(String.format("%s - %s Scan", sampleName, pathName));
		scanBean.setBeamline(System.getProperty("BEAMLINE"));

		final ScanRequest<IROI> scanRequest = getScanRequest(mappingBean);
		scanBean.setScanRequest(scanRequest);
		return scanBean;
	}

	protected ScanRequest<IROI> getScanRequest(final IMappingExperimentBean mappingBean) {
		final ScanRequestConverter converter = getService(ScanRequestConverter.class);
		return converter.convertToScanRequest(mappingBean);
	}

	private void addMonitors(IMappingExperimentBean mappingBean) {
		final IViewReference viewRef = PageUtil.getPage().findViewReference(MonitorView.ID);
		if (viewRef == null) return;
		final MonitorView monitorView = (MonitorView) viewRef.getView(true); // TODO should we restore the view?

		final BiFunction<Map<String, MonitorScanRole>, MonitorScanRole, Set<String>> getMonitorNamesForRole =
				(map, role) -> map.entrySet().stream()
									.filter(entry -> entry.getValue() == role)
									.map(Map.Entry::getKey).collect(toSet());

		final Map<String, MonitorScanRole> monitors = monitorView.getEnabledMonitors();
		mappingBean.setPerPointMonitorNames(getMonitorNamesForRole.apply(monitors, MonitorScanRole.PER_POINT));
		mappingBean.setPerScanMonitorNames(getMonitorNamesForRole.apply(monitors, MonitorScanRole.PER_SCAN));
	}

	public void setCreateSeparator(boolean createSeparator) {
		this.createSeparator = createSeparator;
	}
}

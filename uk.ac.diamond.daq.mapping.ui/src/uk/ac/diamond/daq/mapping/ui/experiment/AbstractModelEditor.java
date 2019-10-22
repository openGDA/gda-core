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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import javax.inject.Inject;
import javax.measure.quantity.Quantity;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.gda.client.NumberAndUnitsComposite;

/**
 * Base class for model (e.g. region, path) editors. Once the model is set, call createEditorPart.
 * Though it has no abstract methods, this class is abstract because createEditorPart returns a blank composite.
 * @param <T> Type of model handled by the editor
 */
public abstract class AbstractModelEditor<T> {
	private static final Logger logger = LoggerFactory.getLogger(AbstractModelEditor.class);

	protected static final String X_POSITION = "xPosition";
	protected static final String Y_POSITION = "yPosition";

	protected static final String X_START = "xStart";
	protected static final String Y_START = "yStart";
	protected static final String X_STOP = "xStop";
	protected static final String Y_STOP = "yStop";

	protected static final String X_CENTRE = "xCentre";
	protected static final String Y_CENTRE = "yCentre";
	protected static final String X_RANGE = "xRange";
	protected static final String Y_RANGE = "yRange";

	protected static final String RADIUS = "radius";

	protected static final String X_AXIS_STEP = "xAxisStep";
	protected static final String Y_AXIS_STEP = "yAxisStep";

	private T model;
	private Composite composite;

	@Inject
	private IStageScanConfiguration mappingStageInfo;

	@Inject
	private IEventService eventService;
	private IScannableDeviceService scannableDeviceService;

	/**
	 * Get via {@link #getUnitsProvider()} to ensure correct initialisation
	 */
	private UnitsProvider units;

	/**
	 * Apply to a control to make it fill horizontal space
	 */
	protected final GridDataFactory grabHorizontalSpace = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

	/**
	 * For common binding calls
	 */
	protected final DataBinder binder = new DataBinder();

	/**
	 * Create a GUI to edit the model (which must already be set). Default implementation creates a blank 2-column composite.
	 * For proper disposal and style consistency, final implementations should use super's returned composite.
	 * i.e.
	 * <pre>
	 * {@code @Override
	 * public Composite createEditorPart(Composite parent)
	 *     final Composite composite = super.createEditorPart(parent)
	 *     ....
	 *     return composite;}
	 * </pre>
	 *
	 * Final implementations should handle the data binding as well - probably using the protected DataBinder instance.
	 * @param parent composite on which to put this one.
	 * @return Editor composite
	 */
	public Composite createEditorPart(Composite parent) {
		return makeComposite(parent);
	}

	/**
	 * This method should be called before createEditorPart for data binding.
	 * @param model
	 */
	public void setModel(T model) {
		this.model = model;
	}

	/**
	 * @return the model being edited by this editor
	 */
	public T getModel() {
		return model;
	}

	/**
	 * Child classes can override and add their tear down calls,
	 * but they should call super.dispose() as well
	 */
	public void dispose() {
		composite.dispose();
	}

	/**
	 * @return a blank 2-column composite
	 */
	private Composite makeComposite(Composite parentComposite) {
		composite = new Composite(parentComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.TOP).applyTo(composite);
		GridLayoutFactory.swtDefaults().numColumns(2).spacing(10, 5).applyTo(composite);
		return composite;
	}

	/**
	 * @return scannable name if IStageScanConfiguration is configured, otherwise the literal "x axis"
	 */
	protected String getXAxisName() {
		return Objects.nonNull(mappingStageInfo) ? mappingStageInfo.getPlotXAxisName() : "x axis";
	}

	/**
	 * @return scannable name if IStageScanConfiguration is configured, otherwise the literal "y axis"
	 */
	protected String getYAxisName() {
		return Objects.nonNull(mappingStageInfo) ? mappingStageInfo.getPlotYAxisName() : "y axis";
	}

	protected IScannableDeviceService getScannableDeviceService() throws EventException {
		if (scannableDeviceService == null) scannableDeviceService = createScannableDeviceService();
		return scannableDeviceService;
	}

	private IScannableDeviceService createScannableDeviceService() throws EventException {
		try {
			URI jmsURI = new URI(LocalProperties.getActiveMQBrokerURI());
			return eventService.createRemoteService(jmsURI, IScannableDeviceService.class);
		} catch (EventException | URISyntaxException e) {
			throw new EventException("Could not create remote service", e);
		}
	}

	/**
	 * Create a {@link NumberAndUnitsComposite} initialising units according the axis scannable.
	 *
	 * @param parent
	 *            composite
	 *
	 * @param axisName
	 *            name of scannable
	 *
	 * @param modelPropertyName
	 *            if {@link InitialLengthUnits} is configured, initial units for that property are looked up
	 *
	 * @return a {@link NumberAndUnitsComposite} initialised for the scannable
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected NumberAndUnitsComposite<Quantity> createNumberAndUnitsComposite(Composite parent, String axisName, String modelPropertyName) {
		return new NumberAndUnitsComposite(parent, SWT.NONE, getUnitsProvider().getScannableUnit(axisName),
				getUnitsProvider().getCompatibleUnits(axisName), getUnitsProvider().getInitialUnit(axisName, modelPropertyName));
	}

	private UnitsProvider getUnitsProvider() {
		if (units == null) {
			units = new UnitsProvider();
			try {
				units.setScannableService(getScannableDeviceService());
			} catch (EventException e) {
				logger.warn("Could not get the scannable device service; initialising GUI with default units", e);
			}
		}
		return units;
	}

}

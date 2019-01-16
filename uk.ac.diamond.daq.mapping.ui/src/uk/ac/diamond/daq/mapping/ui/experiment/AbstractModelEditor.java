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

import static gda.configuration.properties.LocalProperties.GDA_INITIAL_LENGTH_UNITS;
import static org.jscience.physics.units.SI.METER;
import static org.jscience.physics.units.SI.MICRO;
import static org.jscience.physics.units.SI.MILLI;
import static org.jscience.physics.units.SI.NANO;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import gda.configuration.properties.LocalProperties;
import uk.ac.gda.client.NumberAndUnitsComposite;

/**
 * Base class for model (e.g. region, path) editors. Once the model is set, call createEditorPart.
 * Though it has no abstract methods, this class is abstract because createEditorPart returns a blank composite.
 * @param <T> Type of model handled by the editor
 */
public abstract class AbstractModelEditor<T> {
	private static final Logger logger = LoggerFactory.getLogger(AbstractModelEditor.class);

	private static final Unit<Length> MODEL_LENGTH_UNIT = MILLI(METER);
	private static final List<Unit<Length>> LENGTH_UNITS = ImmutableList.of(MILLI(METER), MICRO(METER), NANO(METER));
	private static final Unit<Length> INITIAL_LENGTH_UNIT = getInitialLengthUnit();

	private T model;
	private Composite composite;

	@Inject
	private IStageScanConfiguration mappingStageInfo;

	@Inject
	private IEventService eventService;
	private IScannableDeviceService scannableDeviceService;

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
	 * @return scannable name if IStageScanConfiguration is configured, otherwise the literal "Fast axis"
	 */
	protected String getFastAxisName() {
		return Objects.nonNull(mappingStageInfo) ? mappingStageInfo.getActiveFastScanAxis() : "Fast axis";
	}

	/**
	 * @return scannable name if IStageScanConfiguration is configured, otherwise the literal "Slow axis"
	 */
	protected String getSlowAxisName() {
		return Objects.nonNull(mappingStageInfo) ? mappingStageInfo.getActiveSlowScanAxis() : "Slow axis";
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
	 * Get the initial units (i.e. the units shown in the combo box when it is first displayed)
	 * <p>
	 * This defaults to millimetres but can be set in a property
	 *
	 * @return the initial units
	 */
	@SuppressWarnings("unchecked")
	public static Unit<Length> getInitialLengthUnit() {
		final String unitString = LocalProperties.get(GDA_INITIAL_LENGTH_UNITS, "mm").toLowerCase();
		try {
			final Unit<?> unit = Unit.valueOf(unitString);
			if (unit.isCompatible(MODEL_LENGTH_UNIT)) {
				return (Unit<Length>) unit;
			}
			logger.warn("Value '{}' of property '{}' is not a valid length unit: assuming millimetres", unitString, GDA_INITIAL_LENGTH_UNITS);
			return MODEL_LENGTH_UNIT;
		} catch (Exception e) {
			logger.warn("Cannot parse value '{}' of property '{}': assuming millimetres", unitString, GDA_INITIAL_LENGTH_UNITS);
			return MODEL_LENGTH_UNIT;
		}
	}

	/**
	 * Create a {@link NumberAndUnitsComposite} for length units, assuming model units are mm
	 *
	 * @param parent
	 *            composite
	 * @return a {@link NumberAndUnitsComposite} initialised for length
	 */
	protected static NumberAndUnitsComposite<Length> createNumberAndUnitsLengthComposite(Composite parent) {
		return new NumberAndUnitsComposite<>(parent, SWT.NONE, MODEL_LENGTH_UNIT, LENGTH_UNITS, INITIAL_LENGTH_UNIT);
	}

}

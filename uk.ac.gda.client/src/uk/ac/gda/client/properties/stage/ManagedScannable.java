/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.client.properties.stage;

import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.publishEvent;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.math.NumberUtils;

import gda.device.DeviceException;
import gda.device.Scannable;
import uk.ac.gda.client.event.ManagedScannableEvent;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.spring.FinderService;

/**
 * Allows the client to drive a scannable using a {@link ScannableProperties}
 *
 * <p>
 * It is assumed that instances of this class are available only through
 * {@link ScannablesPropertiesHelper#getManagedScannable(DefaultManagedScannable)}
 * or {@link ScannablesPropertiesHelper#getManagedScannable(uk.ac.gda.client.properties.stage.position.ScannableKeys)}
 * </p>
 *
 * <p>
 * A client component, i.e. a widget, may parametrise a scannable using a pair of strings (groupID, scananbleID) and
 * assume to know the expected type. Using those parameters in
 * {@code ScannablesPropertiesHelper#getManagedScannable(String, String, Class)} the client component receives an object
 * which can drive the scannable. This class may be expanded in the future when other use cases will appear.
 * </p>
 *
 * <p>
 * 	Any successful {@link #moveTo(Object)} publishes a {@link ManagedScannableEvent}
 * </p>
 *
 * @author Maurizio Nagni
 */
public class ManagedScannable<T> {

	private Scannable scannable;
	private final ScannableProperties scannablePropertiesDocument;

	/**
	 * This constructor is restricted to the package in order to force the use of
	 * {@code ScannablesPropertiesHelper#getManagedScannable(String, String)}
	 *
	 * @param scannablePropertiesDocument
	 */
	ManagedScannable(ScannableProperties scannablePropertiesDocument) {
		this.scannablePropertiesDocument = scannablePropertiesDocument;
	}

	/**
	 * Utility to move a scannable
	 *
	 * @param position
	 *            the new position
	 * @throws GDAClientException
	 *             when the scannable cannot be moved
	 */
	public final void moveTo(T position) throws GDAClientException {
		doMoveTo(position);
	}

	/**
	 * Utility to get a scannable position
	 *
	 * @throws GDAClientException
	 *             when the scannable position is not available
	 */
	public final T getPosition() throws GDAClientException {
		if (isAvailable()) {
			Object pos;
			try {
				pos = getScannable().getPosition();
			} catch (DeviceException e) {
				throw new GDAClientException("Cannot handle device", e);
			}
			if (pos instanceof String) {
				return (T) String.class.cast(pos);
			}
			if (pos instanceof Number) {
				return (T) Number.class.cast(pos);
			}
		}
		throw new GDAClientException("The scannable is not available: " + scannablePropertiesDocument);
	}

	/**
	 * Verifies if the scannable is available.
	 *
	 * @return {@code true} if the scannable is available, otherwise {@code false}
	 */
	public boolean isAvailable() {
		return Objects.nonNull(getScannable());
	}

	private final void doMoveTo(Object position) throws GDAClientException {
		if (!isAvailable()) {
			throw new GDAClientException("The scannable is not available: " + scannablePropertiesDocument);
		}

		try {
			if (NumberUtils.isNumber(position.toString())) {
				getScannable().moveTo(position);
			} else {
				moveToEnumPosition(position.toString());
			}
		} catch (DeviceException e) {
			throw new GDAClientException("Cannot handle device", e);
		}
		publishEvent(new ManagedScannableEvent<>(this, scannablePropertiesDocument, position));
	}

	private final void moveToEnumPosition(String position) throws DeviceException {
		getScannable().moveTo(scannablePropertiesDocument.getEnumsMap().getOrDefault(position, position));
	}

	private Scannable getScannable() {
		return Optional.ofNullable(scannable)
				.orElseGet(this::retrieveScannable);
	}

	private Scannable retrieveScannable() {
		scannable = getFinder().getFindableObject(getDevice(), Scannable.class)
				.filter(Scannable.class::isInstance)
				.map(Scannable.class::cast)
				.orElseGet(() -> null);
		return scannable;
	}

	public ScannableProperties getScannablePropertiesDocument() {
		return scannablePropertiesDocument;
	}

	private String getDevice() {
		return Optional.ofNullable(getScannablePropertiesDocument())
				.map(ScannableProperties::getScannable)
				.orElseGet(() -> null);
	}

	private static FinderService getFinder() {
		return SpringApplicationContextFacade.getBean(FinderService.class);
	}

	@Override
	public String toString() {
		return "ManagedScannable [scannablePropertiesDocument=" + scannablePropertiesDocument + "]";
	}
}

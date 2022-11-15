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

package uk.ac.diamond.daq.scanning;

import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_GDA_FIELD_NAME;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_GDA_SCANNABLE_NAME;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_GDA_SCAN_ROLE;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_LOCAL_NAME;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_UNITS;
import static org.eclipse.dawnsci.nexus.NexusConstants.NXCLASS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import gda.device.DeviceException;
import gda.device.Monitor;
import gda.device.ScannableMotionUnits;
import gda.device.enumpositioner.DummyEnumPositioner;
import gda.device.scannable.DummyScannable;
import gda.device.scannable.DummyScannableMotor;

public class ScannableNexusWrapperTest {

	private IScannable<Object> scannable;

	@BeforeEach
	public void before() throws Exception {
		DummyScannable wrappedScannable = new DummyScannable("sax", 3.7);
		wrappedScannable.setLowerGdaLimits(0.5);
		wrappedScannable.setUpperGdaLimits(9.5);
		scannable = new ScannableNexusWrapper<>(wrappedScannable);
	}

	@Test
	public void testGetLevel() {
		assertThat(scannable.getLevel(), is(5));
	}

	@Test
	public void testSetLevel() {
		assertThat(scannable.getLevel(), is(5));
		scannable.setLevel(2);
		assertThat(scannable.getLevel(), is(2));
	}

	@Test
	public void testGetName() {
		assertThat(scannable.getName(), equalTo("sax"));
	}

	@Test
	public void testSetName() {
		assertThat(scannable.getName(), equalTo("sax"));
		scannable.setName("newName");
		assertThat(scannable.getName(), equalTo("newName"));
	}
	@Test
	public void testGetUnit() {
		assertThat(scannable.getUnit(), is(nullValue()));

		ScannableMotionUnits mockUnitsScannable = mock(ScannableMotionUnits.class);
		when(mockUnitsScannable.getUserUnits()).thenReturn("mm");
		IScannable<?> unitsScannable = new ScannableNexusWrapper<>(mockUnitsScannable);

		assertThat(unitsScannable.getUnit(), is(equalTo("mm")));
	}

	@Test
	public void testGetUnitMonitor() throws DeviceException {
		assertThat(scannable.getUnit(), is(nullValue()));

		Monitor mockUnitsScannable = mock(Monitor.class);
		when(mockUnitsScannable.getUnit()).thenReturn("mm");
		IScannable<?> unitsScannable = new ScannableNexusWrapper<>(mockUnitsScannable);

		assertThat(unitsScannable.getUnit(), is(equalTo("mm")));
	}

	@Test
	public void testGetMinimum() {
		assertThat(scannable.getMinimum(), is(equalTo(0.5)));
	}

	@Test
	public void testGetMaximum() {
		assertThat(scannable.getMaximum(), is(equalTo(9.5)));
	}

	@Test
	public void testGetPermittedValues() throws Exception {
		assertThat(scannable.getPermittedValues(), is(nullValue()));

		DummyEnumPositioner enumPositioner = new DummyEnumPositioner();
		enumPositioner.setPositions(Arrays.asList("One", "Two", "Three"));
		IScannable<?> enumScannable = new ScannableNexusWrapper<>(enumPositioner);

		assertThat(enumScannable.getPermittedValues(),
				equalTo(new String[] { "One", "Two", "Three" }));
	}

	@Test
	public void testGetPosition() throws Exception {
		assertThat(scannable.getPosition(), is(equalTo(3.7)));
	}

	@Test
	public void testSetPosition() throws Exception {
		// Arrange
		final double newPosition = 8.3;
		IPositionListener posListener = mock(IPositionListener.class);
		((IPositionListenable) scannable).addPositionListener(posListener);
		assertThat(scannable.getPosition(), is(equalTo(3.7)));

		// Act
		scannable.setPosition(newPosition);

		// Assert
		assertThat(scannable.getPosition(), is(equalTo(newPosition)));
		((IPositionListenable) scannable).removePositionListener(posListener);

		ArgumentCaptor<PositionEvent> captor = ArgumentCaptor.forClass(PositionEvent.class);
		InOrder order = inOrder(posListener);
		order.verify(posListener).positionWillPerform(captor.capture());
		order.verify(posListener).positionChanged(captor.capture());
		order.verify(posListener).positionPerformed(captor.capture());
		order.verifyNoMoreInteractions();

		List<PositionEvent> posEvents = captor.getAllValues();
		assertThat(posEvents, hasSize(3));
		int i = 0;
		for (PositionEvent posEvent : posEvents) {
			assertThat(posEvent.getLevel(), is(i == 0 ? 0 : 5)); // willPerform event doesn't have level set
			IPosition position = posEvent.getPosition();
			assertThat(position.getNames(), contains(scannable.getName()));
			assertThat(position.getValue(scannable.getName()), is(equalTo(Double.valueOf(newPosition))));
			i++;
		}
	}

	@Test
	public void testSetPosition_withScanPosition() throws Exception {
		// Note: nexus writing tested separately by ScannableNexusWrapperScanTest

		// Arrange
		final double newPosition = 3.8;
		final int scanIndex = 38;
		IPositionListener posListener = mock(IPositionListener.class);
		((IPositionListenable) scannable).addPositionListener(posListener);
		final IPosition scanPosition = new Scalar<Double>("sax", scanIndex, newPosition);
		assertThat(scannable.getPosition(), is(equalTo(3.7)));

		// Act
		scannable.setPosition(newPosition, scanPosition);

		// Assert
		assertThat(scannable.getPosition(), is(equalTo(newPosition)));
		((IPositionListenable) scannable).removePositionListener(posListener);

		ArgumentCaptor<PositionEvent> captor = ArgumentCaptor.forClass(PositionEvent.class);
		InOrder order = inOrder(posListener);
		order.verify(posListener).positionWillPerform(captor.capture());
		order.verify(posListener).positionChanged(captor.capture());
		order.verify(posListener).positionPerformed(captor.capture());
		order.verifyNoMoreInteractions();

		List<PositionEvent> posEvents = captor.getAllValues();
		assertThat(posEvents, hasSize(3));
		int i = 0;
		for (PositionEvent posEvent : posEvents) {
			assertThat(posEvent.getLevel(), is(i == 0 ? 0 : 5)); // willPerform event doesn't have level set
			IPosition position = posEvent.getPosition();
			assertThat(position.getNames(), contains(scannable.getName()));
			assertThat(position.getIndex("sax"), is(i == 1 ? -1 : 38)); // index not set for positionChanged
			assertThat(position.getValue(scannable.getName()), is(equalTo(Double.valueOf(newPosition))));
			i++;
		}
	}

	@Test
	public void testNullSetPosition() throws Exception {
		// Arrange
		IPositionListener posListener = mock(IPositionListener.class);
		((IPositionListenable) scannable).addPositionListener(posListener);
		assertThat(scannable.getPosition(), is(equalTo(3.7)));

		// Act
		scannable.setPosition(null);

		// Assert
		assertThat(scannable.getPosition(), is(equalTo(3.7)));
		((IPositionListenable) scannable).removePositionListener(posListener);
		verifyNoInteractions(posListener);
	}

	@Test
	public void testNullSetPosition_withScanPosition() throws Exception {
		// Note: nexus writing tested separately by ScannableNexusWrapperScanTest

		// Arrange
		final int scanIndex = 38;
		IPositionListener posListener = mock(IPositionListener.class);
		((IPositionListenable) scannable).addPositionListener(posListener);
		final IPosition scanPosition = new Scalar<Double>("sax", scanIndex, null);
		assertThat(scannable.getPosition(), is(equalTo(3.7)));

		// Act
		scannable.setPosition(null, scanPosition);

		// Assert
		assertThat(scannable.getPosition(), is(equalTo(3.7)));
		((IPositionListenable) scannable).removePositionListener(posListener);
		verifyNoInteractions(posListener);
	}

	@Test
	public void testGetNexusProvider() throws Exception {
		// getNexusProvider should no longer be called directly. The nexus writing framework will
		// always call getNexusProviders().
		assertThrows(UnsupportedOperationException.class, () -> ((INexusDevice<?>) scannable).getNexusProvider(null));
	}

	@Test
	public void testGetNexusProviders() throws Exception {
		// NOTE: nexus writing is more fully tested in ScannableNexusWrapperScanTest
		final String scannableName = "meta1";
		final ScannableMotionUnits mockScannable = mock(ScannableMotionUnits.class);
		when(mockScannable.getName()).thenReturn(scannableName);
		final String[] inputNames = { "input1", "input2", "input3" };
		final String[] extraNames = { "extra1", "extra2" };
		when(mockScannable.getInputNames()).thenReturn(inputNames);
		when(mockScannable.getExtraNames()).thenReturn(extraNames);
		final Object[] position = new Object[] { 1.0, 2.0, 3.0, "One", "Two" };
		when(mockScannable.getPosition()).thenReturn(position);
		final String expectedUnits = "nm";
		when(mockScannable.getUserUnits()).thenReturn(expectedUnits);
		final Double[] lowerLimits = new Double[] { -1.0, -2.0, -3.0 };
		when(mockScannable.getLowerGdaLimits()).thenReturn(lowerLimits);
		final Double[] upperLimits = new Double[] { 1.0, 2.0, 3.0 };
		when(mockScannable.getUpperGdaLimits()).thenReturn(upperLimits);

		final ScannableNexusWrapper<?> scannableNexusWrapper = new ScannableNexusWrapper<>(mockScannable);

		final NexusScanInfo scanInfo = new NexusScanInfo();
		scanInfo.setPerScanMonitorNames(Set.of(scannableName));
		final List<NexusObjectProvider<?>> nexusObjectProviders = scannableNexusWrapper.getNexusProviders(scanInfo);
		assertThat(nexusObjectProviders.size(), is(equalTo(inputNames.length + 1)));

		for (int inputFieldIndex = 0; inputFieldIndex < inputNames.length; inputFieldIndex++) {
			final String inputName = inputNames[inputFieldIndex];
			final String positionerName = mockScannable.getName() + "." + inputName;
			@SuppressWarnings("unchecked")
			final NexusObjectProvider<NXpositioner> positionerProvider = (NexusObjectProvider<NXpositioner>) nexusObjectProviders.get(inputFieldIndex + 1);

			assertThat(positionerProvider, is(notNullValue()));
			assertThat(positionerProvider.getName(), is(positionerName));
			assertThat(positionerProvider.getNexusBaseClass(), is(NexusBaseClass.NX_POSITIONER));
			assertThat(positionerProvider.getCategory(), is(nullValue()));
			assertThat(positionerProvider.getCollectionName(), is(nullValue()));
			// for multi-field scannables the NXcollection is used to get the DataNodes to add to the NXdata groups
			assertThat(positionerProvider.getAxisDataFieldNames(), is(empty()));
			assertThat(positionerProvider.getDefaultAxisDataFieldName(), is(nullValue()));

			final NXpositioner positioner = positionerProvider.getNexusObject();
			assertThat(positioner, is(notNullValue()));
			assertThat(positioner.getNexusBaseClass(), is(NexusBaseClass.NX_POSITIONER));

			assertThat(positioner.getAttributeNames(), containsInAnyOrder(NXCLASS, ATTRIBUTE_NAME_GDA_SCANNABLE_NAME,
					ATTRIBUTE_NAME_GDA_SCAN_ROLE));
			assertThat(positioner.getGroupNodeNames(), is(empty()));

			assertThat(positioner.getAttrString(null, ATTRIBUTE_NAME_GDA_SCANNABLE_NAME), is(equalTo(scannableName)));
			assertThat(positioner.getAttrString(null, ATTRIBUTE_NAME_GDA_SCAN_ROLE),
					equalTo(ScanRole.MONITOR_PER_SCAN.toString().toLowerCase()));

			assertThat(positioner.getDataNodeNames(), containsInAnyOrder(NXpositioner.NX_VALUE,
					NXpositioner.NX_NAME, NXpositioner.NX_SOFT_LIMIT_MIN, NXpositioner.NX_SOFT_LIMIT_MAX));
			assertThat(positioner.getNameScalar(), is(equalTo(positionerName)));

			final DataNode valueDataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
			assertThat(valueDataNode, is(notNullValue()));
			assertThat(valueDataNode.getDataset(), is(notNullValue()));

			assertThat(positioner.getSoft_limit_minScalar().doubleValue(), is(closeTo(lowerLimits[inputFieldIndex], 1e-15)));
			assertThat(positioner.getSoft_limit_maxScalar().doubleValue(), is(closeTo(upperLimits[inputFieldIndex], 1e-15)));
		}

		// assert collection
		@SuppressWarnings("unchecked")
		final NexusObjectProvider<NXcollection> collectionProvider = (NexusObjectProvider<NXcollection>) nexusObjectProviders.get(0);
		assertThat(collectionProvider, is(notNullValue()));
		assertThat(collectionProvider.getName(), is(equalTo(scannableName)));
		assertThat(collectionProvider.getNexusBaseClass(), is(NexusBaseClass.NX_COLLECTION));
		assertThat(collectionProvider.getCategory(), is(NexusBaseClass.NX_INSTRUMENT));
		assertThat(collectionProvider.getCollectionName(), is(nullValue()));
		assertThat(collectionProvider.getAxisDataFieldNames(), contains(inputNames));
		assertThat(collectionProvider.getDefaultAxisDataFieldName(), is(inputNames[0]));

		final String[] expectedDataNodeNames = Stream.of(inputNames, extraNames, new String[] { "name" })
				.flatMap(Stream::of).toArray(String[]::new);
		final NXcollection collection = collectionProvider.getNexusObject();
		assertThat(collection, is(notNullValue()));
		assertThat(collection.getNexusBaseClass(), is(NexusBaseClass.NX_COLLECTION));
		assertThat(collection.getAttributeNames(), containsInAnyOrder(NXCLASS, ATTRIBUTE_NAME_GDA_SCANNABLE_NAME, ATTRIBUTE_NAME_GDA_SCAN_ROLE));
		assertThat(collection.getGroupNodeNames(), is(empty()));
		assertThat(collection.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));
		assertThat(collection.getAttrString(null, ATTRIBUTE_NAME_GDA_SCANNABLE_NAME), is(equalTo(scannableName)));
		assertThat(collection.getAttrString(null, ATTRIBUTE_NAME_GDA_SCAN_ROLE), is(equalTo(ScanRole.MONITOR_PER_SCAN.toString().toLowerCase())));

		// check links to input fields
		for (int inputFieldIndex = 0; inputFieldIndex < inputNames.length; inputFieldIndex++) {
			final DataNode inputFieldDataNode = collection.getDataNode(inputNames[inputFieldIndex]);
			assertThat(inputFieldDataNode, is(notNullValue()));
			final NXpositioner positioner = (NXpositioner) nexusObjectProviders.get(inputFieldIndex + 1).getNexusObject();
			assertThat(inputFieldDataNode, is(sameInstance(positioner.getDataNode(NXpositioner.NX_VALUE))));
		}

		for (int i = 0; i < extraNames.length; i++) {
			final String extraName = extraNames[i];
			final DataNode extraFieldDataNode = collection.getDataNode(extraNames[i]);
			assertThat(extraFieldDataNode, is(notNullValue()));
			assertThat(extraFieldDataNode.getAttributeNames(),
					containsInAnyOrder(ATTRIBUTE_NAME_GDA_FIELD_NAME, ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_UNITS));
			assertThat(collection.getAttrString(extraName, ATTRIBUTE_NAME_GDA_FIELD_NAME), is(equalTo(extraName)));
			assertThat(collection.getAttrString(extraName, ATTRIBUTE_NAME_LOCAL_NAME), is(equalTo(scannableName + "." + extraName)));
			assertThat(collection.getAttrString(extraName, ATTRIBUTE_NAME_UNITS), is(equalTo(expectedUnits)));
		}
	}

	@Test
	public void testGetMinimumAndMaximumWhenGDALimitsNotSet() throws Exception {
		DummyScannableMotor motor = new DummyScannableMotor();
		motor.setUpperMotorLimit(9.4);
		motor.setLowerMotorLimit(-0.25);
		Double nullLimit = null;
		motor.setUpperGdaLimits(nullLimit);
		motor.setLowerGdaLimits(nullLimit);
		IScannable<Object> scannableWrapper = new ScannableNexusWrapper<>(motor);

		assertThat(scannableWrapper.getMaximum(), is(9.4));
		assertThat(scannableWrapper.getMinimum(), is(-0.25));
	}

}

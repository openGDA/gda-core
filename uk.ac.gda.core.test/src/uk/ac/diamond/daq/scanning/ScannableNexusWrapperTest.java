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

import static gda.data.scan.nexus.device.ScannableNexusDevice.ATTR_NAME_GDA_FIELD_NAME;
import static gda.data.scan.nexus.device.ScannableNexusDevice.ATTR_NAME_GDA_SCANNABLE_NAME;
import static gda.data.scan.nexus.device.ScannableNexusDevice.ATTR_NAME_GDA_SCAN_ROLE;
import static gda.data.scan.nexus.device.ScannableNexusDevice.ATTR_NAME_LOCAL_NAME;
import static gda.data.scan.nexus.device.ScannableNexusDevice.ATTR_NAME_UNITS;
import static org.eclipse.dawnsci.nexus.NexusBaseClass.NX_POSITIONER;
import static org.eclipse.dawnsci.nexus.NexusConstants.NXCLASS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.google.common.collect.Sets;

import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;
import gda.device.enumpositioner.DummyEnumPositioner;
import gda.device.scannable.DummyScannable;
import gda.device.scannable.DummyScannableMotor;

public class ScannableNexusWrapperTest {

	private static final String FIELD_NAME_VALUE_SET = NXpositioner.NX_VALUE + "_set";

	private IScannable<Object> scannable;

	@Before
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
	public void testGetUnit() throws Exception {
		assertThat(scannable.getUnit(), is(nullValue()));

		ScannableMotionUnits mockUnitsScannable = mock(ScannableMotionUnits.class);
		when(mockUnitsScannable.getUserUnits()).thenReturn("mm");
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
		verifyZeroInteractions(posListener);
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
		verifyZeroInteractions(posListener);
	}

	@Test
	public void testGetFieldNames() throws Exception {
		DummyScannable multiFieldDummyScannable = new DummyScannable("multiField");
		multiFieldDummyScannable.setInputNames(new String[] { "input1", "input2", "input3" });
		multiFieldDummyScannable.setExtraNames(new String[] { "extra1", "extra2" });
		ScannableNexusWrapper<?> multiFieldScannable = new ScannableNexusWrapper<>(multiFieldDummyScannable);

		assertThat(multiFieldScannable.getOutputFieldNames(),
				contains("input1", "input2", "input3", "extra1", "extra2"));

		// test that the first field name is 'value' where the first input name of the
		// wrapped scannable is the same as the name of the scannable itself
		DummyScannable dummyScannable = new DummyScannable("sax");
		assertThat(dummyScannable.getInputNames(), is(equalTo(new String[] { "sax" })));
		ScannableNexusWrapper<?> simpleScannable = new ScannableNexusWrapper<>(dummyScannable);
		assertThat(simpleScannable.getOutputFieldNames(), contains("value"));
	}

	@Test
	public void testGetFieldNamesRecalculated() throws Exception {
		final DummyScannable dummyScannable = new DummyScannable("multiField") {
			@Override
			public Object getPosition() throws DeviceException {
				return new double[getInputNames().length + getExtraNames().length];
			}
		};
		dummyScannable.setInputNames(new String[] { "input1", "input2", "input3" });
		dummyScannable.setExtraNames(new String[] { "extra1", "extra2" });
		dummyScannable.setLowerGdaLimits(new Double[] { 0.0, 0.0, 0.0 });
		dummyScannable.setUpperGdaLimits(new Double[] { 100.0, 100.0, 100.0 });
		final ScannableNexusWrapper<?> scannableWrapper = new ScannableNexusWrapper<>(dummyScannable);

		String[] expectedOutputNames = new String[] { "input1", "input2", "input3", "extra1", "extra2" };
		assertThat(scannableWrapper.getOutputFieldNames(), contains(expectedOutputNames));

		final NexusScanInfo scanInfo = new NexusScanInfo(Arrays.asList("xPos", "yPos"));
		scanInfo.setPerPointMonitorNames(Sets.newHashSet("multiField"));
		NexusObjectProvider<?> nexusObjectProvider = scannableWrapper.getNexusProvider(scanInfo);
		NXpositioner nxPositioner = (NXpositioner) nexusObjectProvider.getNexusObject();

		final String[] additionalNames = new String[] { NXpositioner.NX_NAME,
				NXpositioner.NX_SOFT_LIMIT_MIN, NXpositioner.NX_SOFT_LIMIT_MAX };
		String[] expectedDataNodeNames = ArrayUtils.addAll(expectedOutputNames, additionalNames);
		assertThat(nxPositioner.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));

		// update the names
		dummyScannable.setInputNames(new String[] { "newInput1", "newInput2" });
		dummyScannable.setExtraNames(new String[] { "newExtra" });
		dummyScannable.setLowerGdaLimits(new Double[] { 0.0, 0.0 });
		dummyScannable.setUpperGdaLimits(new Double[] { 100.0, 100.0 });

		// calling getNexusProvider triggers fields to be recalculated
		nexusObjectProvider = scannableWrapper.getNexusProvider(scanInfo);
		nxPositioner = (NXpositioner) nexusObjectProvider.getNexusObject();
		expectedOutputNames = new String[] { "newInput1", "newInput2", "newExtra" };
		expectedDataNodeNames = ArrayUtils.addAll(expectedOutputNames, additionalNames);
		assertThat(nxPositioner.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));

		assertThat(scannableWrapper.getOutputFieldNames(), contains(expectedOutputNames));
	}

	@Test
	public void testGetNexusProvider() throws Exception {
		// NOTE: nexus writing is more fully tested in ScannableNexusWrapperScanTest
		final ScannableMotionUnits mockScannable = mock(ScannableMotionUnits.class);
		when(mockScannable.getName()).thenReturn("xPos");
		final String[] inputNames = { "input1", "input2", "input3" };
		final String[] extraNames = { "extra1", "extra2" };
		when(mockScannable.getInputNames()).thenReturn(inputNames);
		when(mockScannable.getExtraNames()).thenReturn(extraNames);
		final Object[] position = new Object[] { 1.0, 2.0, 3.0, "One", "Two" };
		when(mockScannable.getPosition()).thenReturn(position);
		when(mockScannable.getUserUnits()).thenReturn("nm");
		final Double[] lowerLimits = new Double[] { -1.0, -2.0, -3.0 };
		when(mockScannable.getLowerGdaLimits()).thenReturn(lowerLimits);
		final Double[] upperLimits = new Double[] { 1.0, 2.0, 3.0 };
		when(mockScannable.getUpperGdaLimits()).thenReturn(upperLimits);

		final ScannableNexusWrapper<?> scannableNexusWrapper = new ScannableNexusWrapper<>(mockScannable);

		final NexusScanInfo scanInfo = new NexusScanInfo(Arrays.asList("xPos", "yPos"));
		final NexusObjectProvider<?> nexusObjectProvider = scannableNexusWrapper.getNexusProvider(scanInfo);
		assertThat(nexusObjectProvider, is(notNullValue()));
		assertThat(nexusObjectProvider.getName(), is(equalTo("xPos")));
		assertThat(nexusObjectProvider.getNexusBaseClass(), is(NX_POSITIONER));
		assertThat(nexusObjectProvider.getCategory(), is(nullValue()));
		assertThat(nexusObjectProvider.getCollectionName(), is(nullValue()));
		assertThat(nexusObjectProvider.getAxisDataFieldNames(),
				contains("input1", FIELD_NAME_VALUE_SET));
		assertThat(nexusObjectProvider.getDefaultAxisDataFieldName(), is(equalTo(FIELD_NAME_VALUE_SET)));

		final String[] expectedFieldNames = Stream.of(inputNames, extraNames).flatMap(Stream::of).toArray(String[]::new);
		final String[] otherDataNodeNames = new String[] { NXpositioner.NX_NAME, FIELD_NAME_VALUE_SET,
				NXpositioner.NX_SOFT_LIMIT_MIN, NXpositioner.NX_SOFT_LIMIT_MAX };
		final String[] expectedDataNodeNames = Stream.of(expectedFieldNames, otherDataNodeNames)
				.flatMap(Stream::of).toArray(String[]::new);

		final NXpositioner nexusObject = (NXpositioner) nexusObjectProvider.getNexusObject();
		assertThat(nexusObject, notNullValue());
		assertThat(nexusObject.getNexusBaseClass(), is(NX_POSITIONER));
		assertThat(nexusObject.getAttributeNames(), containsInAnyOrder(NXCLASS, ATTR_NAME_GDA_SCANNABLE_NAME, ATTR_NAME_GDA_SCAN_ROLE));
		assertThat(nexusObject.getNumberOfAttributes(), is(3));
		assertThat(nexusObject.getGroupNodeNames(), is(empty()));
		assertThat(nexusObject.getNumberOfGroupNodes(), is(0));
		assertThat(nexusObject.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));
		assertThat(nexusObject.getNumberOfDataNodes(), is(expectedDataNodeNames.length));
		assertThat(nexusObject.getNameScalar(), is(equalTo("xPos")));
		assertThat(nexusObject.getAttrString(null, ATTR_NAME_GDA_SCANNABLE_NAME), is(equalTo("xPos")));
		assertThat(nexusObject.getAttrString(null, ATTR_NAME_GDA_SCAN_ROLE),
				equalTo(ScanRole.SCANNABLE.toString().toLowerCase()));


		final List<String> fieldNames = scannableNexusWrapper.getOutputFieldNames();
		assertThat(fieldNames, contains(expectedFieldNames));
		for (String fieldName : fieldNames) {
			final DataNode valueDataNode = nexusObject.getDataNode(fieldName);
			assertThat(valueDataNode, notNullValue());
			assertThat(nexusObject.getAttrString(fieldName, ATTR_NAME_LOCAL_NAME), is(equalTo("xPos." + fieldName)));
			assertThat(nexusObject.getAttrString(fieldName, ATTR_NAME_GDA_FIELD_NAME), is(equalTo(fieldName)));
			assertThat(valueDataNode.getDataset(), is(notNullValue()));
			assertThat(nexusObject.getAttrString(fieldName, ATTR_NAME_UNITS), is(equalTo("nm")));
		}

		final DataNode valueDemandDataNode = nexusObject.getDataNode(FIELD_NAME_VALUE_SET);
		assertThat(valueDemandDataNode, is(notNullValue()));
		assertThat(valueDemandDataNode.getDataset(), is(notNullValue()));

		final DataNode softLimitMin = nexusObject.getDataNode(NXpositioner.NX_SOFT_LIMIT_MIN);
		assertThat(softLimitMin, is(notNullValue()));
		final IDataset softLimitMinDataset = softLimitMin.getDataset().getSlice();
		assertThat(softLimitMinDataset, is(equalTo(DatasetFactory.createFromObject(lowerLimits))));

		final DataNode softLimitMax = nexusObject.getDataNode(NXpositioner.NX_SOFT_LIMIT_MAX);
		assertThat(softLimitMax, is(notNullValue()));
		final IDataset softLimitMaxDataset = softLimitMax.getDataset().getSlice();
		assertThat(softLimitMaxDataset, is(equalTo(DatasetFactory.createFromObject(upperLimits))));
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

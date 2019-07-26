/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package org.eclipse.scanning.test.scan;

import static java.util.stream.Collectors.toMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.SimpleDetectorModel;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.points.PointGeneratorService;
import org.junit.Before;
import org.junit.Test;

public class ScanInformationTest {

	private IPointGeneratorService pointGenService;

	@Before
	public void setUp() {
		pointGenService = new PointGeneratorService();
	}

	@Test
	public void testNewScanInformationFromScanRequest() throws Exception {
		CompoundModel compoundModel = createCompoundModel();

		ScanRequest<?> scanRequest = new ScanRequest<>();
		scanRequest.setCompoundModel(compoundModel);
		scanRequest.setDetectors(createDetectorModels());
		String filePath = "/path/to/nexusFile.nxs";
		scanRequest.setFilePath(filePath);

		ScanInformation scanInfo = new ScanInformation(pointGenService, scanRequest);
		checkScanInfo(scanInfo);
	}

	@Test
	public void testNewScanInformationFromPointGenAndDetectors() throws Exception {
		CompoundModel compoundModel = createCompoundModel();
		IPointGenerator<?> pointGen = pointGenService.createCompoundGenerator(compoundModel);

		String filePath = "/path/to/nexusFile.nxs";
		Collection<Object> detModels = createDetectorModels().values();

		ScanInformation scanInfo = new ScanInformation(pointGen, detModels, filePath);
		checkScanInfo(scanInfo);
	}

	private CompoundModel createCompoundModel() {
		GridModel gridModel = new GridModel();
		gridModel.setXAxisName("xNex");
		gridModel.setXAxisPoints(8);
		gridModel.setYAxisName("yNex");
		gridModel.setYAxisPoints(5);
		gridModel.setBoundingBox(new BoundingBox(0, 0, 5, 5));
		StepModel stepModel = new StepModel("T", 0, 25, 5);
		CompoundModel compoundModel = new CompoundModel<>(stepModel, gridModel);
		return compoundModel;
	}

	private Map<String, Object> createDetectorModels() {
		List<IDetectorModel> detModels = new ArrayList<>();
		detModels.add(new SimpleDetectorModel("det1", 0.5));
		detModels.add(new SimpleDetectorModel("det2", 1.5));
		detModels.add(new SimpleDetectorModel("det3", 0.2));
		return detModels.stream().collect(toMap(IDetectorModel::getName, Function.identity()));
	}

	private void checkScanInfo(ScanInformation scanInfo) {
		assertThat(scanInfo.getFilePath(), is(equalTo("/path/to/nexusFile.nxs")));
		assertThat(scanInfo.getRank(), is(3));
		assertThat(scanInfo.getSize(), is(equalTo(240)));
		assertThat(scanInfo.getScannableNames(), contains("T", "yNex", "xNex"));
		assertThat(scanInfo.getTimePerPoint(), is(1500L));
		assertThat(scanInfo.getEstimatedScanTime(), is(equalTo(360000L)));
	}

}

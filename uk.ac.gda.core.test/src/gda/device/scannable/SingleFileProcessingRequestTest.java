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

package gda.device.scannable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collection;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gda.data.ServiceHolder;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.StoredMetadataEntry;
import gda.device.ProcessingRequestProvider;
import uk.ac.diamond.daq.scanning.FilePathService;

public class SingleFileProcessingRequestTest {

	private static final String VISIT_ID = "abc123-1";
	private static final String KEY = "config";
	private SingleFileProcessingRequest pr = new SingleFileProcessingRequest("pr", KEY);

	@BeforeAll
	public static void setUp() {
		// explicitly set the visit id. otherwise on Jenkins it could be picked up from
		// metadata left behind by a previously run test, causing this test to fail
		new ServiceHolder().setFilePathService(new FilePathService());
		GDAMetadataProvider.getInstance().addMetadataEntry(new StoredMetadataEntry("visit", VISIT_ID));
	}

	@AfterAll
	public static void tearDown() {
		GDAMetadataProvider.setInstanceForTesting(null);
		new ServiceHolder().setFilePathService(null);
	}

	@Test
	void absolutePathWithoutBase() {
		pr.setProcessingFile("/config.xml");

		checkRequest(pr, KEY, "/config.xml");
	}

	@Test
	void absolutePathIgnoresBase() {
		pr.setRelativePathBase("/tmp/");
		pr.setProcessingFile("/config.xml");

		checkRequest(pr, KEY, "/config.xml");
	}

	@Test
	void relativePathWithBase() {
		pr.setRelativePathBase("/tmp/");
		pr.setProcessingFile("config.xml");

		checkRequest(pr, KEY, "/tmp/config.xml");
	}

	@Test
	void baseMustBeAbsolute() {
		assertThrows(IllegalArgumentException.class, () -> pr.setRelativePathBase("relative/path"));
	}

	@Test
	void clearingRelativePathRevertsToRoot() {
		pr.setRelativePathBase("/some/relative/base/");
		pr.setRelativePathBase(null);

		pr.setProcessingFile("relative/path/to/config.xml");

		checkRequest(pr, KEY, "/relative/path/to/config.xml");
	}

	@Test
	void requestIsEmptyWhenNoFileSet() {
		pr.setProcessingFile(null);
		Map<String, Collection<Object>> request = pr.getProcessingRequest();
		assertThat(request.keySet(), is(empty()));
	}

	@Test
	void emptyFileIsEmptyRequest() {
		pr.setProcessingFile("");
		Map<String, Collection<Object>> request = pr.getProcessingRequest();
		assertThat(request.keySet(), is(empty()));
	}

	@Test
	void templateExpandedInBase() {
		pr.setRelativePathBase("/tmp/$visit$/config");
		pr.setProcessingFile("config.xml");

		checkRequest(pr, KEY, "/tmp/%s/config/config.xml".formatted(VISIT_ID));
	}

	private static void checkRequest(ProcessingRequestProvider pr, String key, String value) {
		Map<String, Collection<Object>> request = pr.getProcessingRequest();
		assertThat(request.keySet(), contains(key));
		assertThat(request.get(KEY), contains(value));
	}
}

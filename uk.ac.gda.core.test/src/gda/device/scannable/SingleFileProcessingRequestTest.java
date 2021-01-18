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

import java.util.Collection;
import java.util.Map;

import org.junit.Test;

import gda.device.ProcessingRequestProvider;

public class SingleFileProcessingRequestTest {

	private static final String KEY = "config";
	private SingleFileProcessingRequest pr = new SingleFileProcessingRequest("pr", KEY);

	@Test
	public void absolutePathWithoutBase() {
		pr.setProcessingFile("/config.xml");

		checkRequest(pr, KEY, "/config.xml");
	}

	@Test
	public void absolutePathIgnoresBase() {
		pr.setRelativePathBase("/tmp/");
		pr.setProcessingFile("/config.xml");

		checkRequest(pr, KEY, "/config.xml");
	}

	@Test
	public void relativePathWithBase() {
		pr.setRelativePathBase("/tmp/");
		pr.setProcessingFile("config.xml");

		checkRequest(pr, KEY, "/tmp/config.xml");
	}

	@Test(expected = IllegalArgumentException.class)
	public void baseMustBeAbsolute() {
		pr.setRelativePathBase("relative/path");
	}

	@Test
	public void clearingRelativePathRevertsToRoot() {
		pr.setRelativePathBase("/some/relative/base/");
		pr.setRelativePathBase(null);

		pr.setProcessingFile("relative/path/to/config.xml");

		checkRequest(pr, KEY, "/relative/path/to/config.xml");
	}

	@Test
	public void requestIsEmptyWhenNoFileSet() {
		pr.setProcessingFile(null);
		Map<String, Collection<Object>> request = pr.getProcessingRequest();
		assertThat(request.keySet(), is(empty()));
	}

	@Test
	public void emptyFileIsEmptyRequest() {
		pr.setProcessingFile("");
		Map<String, Collection<Object>> request = pr.getProcessingRequest();
		assertThat(request.keySet(), is(empty()));
	}

	@Test
	public void templateExpandedInBase() {
		pr.setRelativePathBase("/tmp/$visit$/config");
		pr.setProcessingFile("config.xml");

		checkRequest(pr, KEY, "/tmp/0-0/config/config.xml");
	}

	private static void checkRequest(ProcessingRequestProvider pr, String key, String value) {
		Map<String, Collection<Object>> request = pr.getProcessingRequest();
		assertThat(request.keySet(), contains(key));
		assertThat(request.get(KEY), contains(value));
	}
}

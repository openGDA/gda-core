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

package uk.ac.diamond.daq.experiment.structure;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;

import org.eclipse.scanning.api.scan.IFilePathService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;

public class ExperimentTreeCacheTest {

	/** the object under test */
	private ExperimentTreeCache cache;

	@Rule
	public TemporaryFolder configDir = new TemporaryFolder();

	@Mock
	private IFilePathService filePathService;

	private static final String EXPERIMENT_NAME = "GaN crystallinity";

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		when(filePathService.getVisitConfigDir()).thenReturn(configDir.getRoot().getAbsolutePath());

		cache = new ExperimentTreeCache();
		cache.setFilePathService(filePathService);
	}

	@Test
	public void noStateFile() throws IOException {
		assertFalse(cache.restore().isPresent());
	}

	@Test
	public void emptyStateFile() throws IOException {
		configDir.newFile(ExperimentTreeCache.STATE_FILE);
		assertFalse(cache.restore().isPresent());
	}

	@Test
	public void singleNodeExperiment() throws IOException {
		ExperimentTree tree = buildTree(EXPERIMENT_NAME, new ExperimentNode(getUrl("data/experiment"), null));
		test(tree);
	}

	private ExperimentTree buildTree(String name, ExperimentNode node) {
		return new ExperimentTree.Builder()
				.withExperimentName(name)
				.withActiveNode(node).build();
	}

	@Test
	public void singleLevelChildren() throws IOException {
		ExperimentTree tree = buildTree(EXPERIMENT_NAME, new ExperimentNode(getUrl("data/experiment"), null));
		tree.addChild(new ExperimentNode(getUrl("data/experiment/acq1"), tree.getActiveNode().getId()));
		tree.addChild(new ExperimentNode(getUrl("data/experiment/acq2"), tree.getActiveNode().getId()));

		test(tree);
	}

	@Test
	public void multiLevelChildren() throws ExperimentControllerException, IOException {
		ExperimentTree tree = buildTree(EXPERIMENT_NAME, new ExperimentNode(getUrl("data/experiment"), null));
		ExperimentNode pointAndShoot = new ExperimentNode(getUrl("data/experiment/pointAndShoot"), tree.getActiveNode().getId());
		tree.addChild(pointAndShoot);
		tree.moveDown(pointAndShoot.getId());
		tree.addChild(new ExperimentNode(getUrl("data/experiment/pointAndShoot/subAcquisition"), pointAndShoot.getId()));

		test(tree);
	}

	private ExperimentTree storeAndRestore(ExperimentTree tree) throws IOException {
		cache.store(tree);
		return cache.restore()
				.orElseThrow(() -> new IOException("Should have been loaded from file, but failed"));
	}

	private void test(ExperimentTree tree) throws IOException {
		ExperimentTree restored = storeAndRestore(tree);
		assertThat(tree, is(equalTo(restored)));
	}


	private URL getUrl(String suffix) throws IOException {
		return new URL("file://" + filePathService.getVisitDir() + suffix);
	}

}

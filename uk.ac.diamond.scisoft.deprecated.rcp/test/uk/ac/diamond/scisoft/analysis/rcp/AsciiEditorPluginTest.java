/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.rcp.editors.AsciiEditor;
import uk.ac.diamond.scisoft.analysis.rcp.editors.DataSetPlotEditor;
import uk.ac.gda.common.rcp.util.EclipseUtils;

// FIXME - make this work!
public class AsciiEditorPluginTest {

	@Before
	public void before() {
		while (Job.getJobManager().currentJob() != null)
			EclipseUtils.delay(1000);
	}

	/**
	 * Test simply plots a dataset, gets it back out and checks if there are differences.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDatasetAccuracy() throws Exception {

		final AsciiEditor editor = createAsciiEditor();
		final DataSetPlotEditor dataEd = (DataSetPlotEditor) editor.getActiveEditor();
		final DoubleDataset energy1 = (DoubleDataset) dataEd.setDatasetSelected("Energy", true);
		
		final ITrace trace = editor.getPlotWindow().getPlottingSystem().getTrace("Energy");
		final DoubleDataset energy2 = (DoubleDataset)trace.getData();

		if (!energy1.equals(energy2))
			throw new Exception("Dataset changed by plotting it!");

	}

	@Test
	public void testSelectingLooping1DDataSets() throws Exception {
		final AsciiEditor editor = createAsciiEditor();
		final DataSetPlotEditor dataEd = (DataSetPlotEditor) editor.getActiveEditor();

		for (int i = 0; i < 100; i++) {
			dataEd.setDatasetSelected("ln(I0/It)", true);
			EclipseUtils.delay(200);
			dataEd.setDatasetSelected("Energy", true);
			EclipseUtils.delay(200);
		}

		while (Job.getJobManager().currentJob() != null)
			EclipseUtils.delay(1000);
		EclipseUtils.delay(2000);
	}

	private AsciiEditor createAsciiEditor() throws PartInitException {
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		AsciiEditor editor = (AsciiEditor) EclipseUtils.openExternalEditor("testfiles/FeKedge_1_15.dat");

		window.getActivePage().activate(editor);
		window.getActivePage().setPartState(window.getActivePage().getActivePartReference(),
				IWorkbenchPage.STATE_MAXIMIZED);
		return editor;
	}
}

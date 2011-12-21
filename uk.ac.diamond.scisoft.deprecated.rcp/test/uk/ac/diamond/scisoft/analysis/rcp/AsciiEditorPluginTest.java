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

import java.io.Serializable;
import java.util.ArrayList;

import junit.framework.Assert;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.fitting.functions.APeak;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.rcp.editors.AsciiEditor;
import uk.ac.diamond.scisoft.analysis.rcp.editors.DataSetPlotEditor;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot.FitData;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot.Fitting1D;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.AreaSelectEvent;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.AreaSelectTool;
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
		final DoubleDataset energy2 = (DoubleDataset) editor.getPlotWindow().getMainPlotter().getCurrentDataSet();

		if (!energy1.equals(energy2))
			throw new Exception("Dataset changed by plotting it!");

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFit1DSidePlot() throws Exception {

		final AsciiEditor editor = createAsciiEditor();
		final DataSetPlotEditor dataEd = (DataSetPlotEditor) editor.getActiveEditor();
		// //final DoubleDataset set = (DoubleDataset)dataEd.setDatasetSelected("ln(I0/It)", true);

		if (editor.getSidePlot() == null)
			editor.setSidePlotSelected(0);
		final Fitting1D sidePlot = (Fitting1D) editor.getSidePlot();
		Assert.assertNotNull(sidePlot);

		sidePlot.areaSelected(new AreaSelectEvent(new AreaSelectTool(), new double[] { 366d, 476d }, '2', 0));

		final FitData fitData = new FitData();
		fitData.setNumberOfPeaks(3);
		fitData.setAlgType(0);
		fitData.setPeakSelection(0);
		fitData.setAccuracy(0.01);
		fitData.setSmoothing(1);

		// Bad use of Dataset API.
		// NOTE slicing for 1D in API is hard to figure out how to use.
		// need to submit defect to this affect some time.
		// //final DoubleDataset x = DoubleDataset.arange(366, 476, 1);
		// //final DoubleDataset y = set.getSlice(new int[] {366}, new int[] {476}, null);

		// Fit the peaks
		sidePlot.fitPeakFromOverlay(366, 476, fitData);
		EclipseUtils.delay(1000);

		GuiBean guiBean = dataEd.getPlotWindow().getDataBean().getGuiParameters();
		EclipseUtils.delay(1000); // This is to allow the fitting to complete
		ArrayList<APeak> peaks = new ArrayList<APeak>();
		if (guiBean.containsKey(GuiParameters.FITTEDPEAKS)) {
			Serializable probablePeaks = guiBean.get(GuiParameters.FITTEDPEAKS);
			if (probablePeaks instanceof ArrayList<?> && ((ArrayList<?>) probablePeaks).get(0) instanceof APeak) {
				peaks = (ArrayList<APeak>) probablePeaks;
			}
		}
		Assert.assertEquals(peaks.size(), 3);
		Assert.assertEquals(peaks.get(0).getPosition(), 416.0);
		Assert.assertEquals(peaks.get(1).getPosition(), 377.0);
		Assert.assertEquals(peaks.get(2).getPosition(), 464.0);

		EclipseUtils.delay(1000);
	}

	@SuppressWarnings({ "null", "unchecked"})
	@Test
	public void testFit1DSidePlotTwoSelected() throws Exception {

		final AsciiEditor editor = createAsciiEditor();
		final DataSetPlotEditor dataEd = (DataSetPlotEditor) editor.getActiveEditor();
		final DoubleDataset x = (DoubleDataset) dataEd.setDatasetSelected("Energy", true);
		// //final DoubleDataset y = (DoubleDataset)dataEd.setDatasetSelected("ln(I0/It)", false);

		if (editor.getSidePlot() == null)
			editor.setSidePlotSelected(0);
		final Fitting1D sidePlot = (Fitting1D) editor.getSidePlot();
		Assert.assertNotNull(sidePlot != null);

		sidePlot.areaSelected(new AreaSelectEvent(new AreaSelectTool(), new double[] { 0, x.get(x.getSize() - 1) },
				'2', 0));

		final FitData fitData = new FitData();
		fitData.setNumberOfPeaks(3);
		fitData.setAlgType(0);
		fitData.setPeakSelection(0);
		fitData.setAccuracy(0.01);
		fitData.setSmoothing(1);

		// Fit the peaks

		sidePlot.fitPeakFromOverlay(x.min().doubleValue(), x.max().doubleValue(), fitData);
		GuiBean guiBean = dataEd.getPlotWindow().getDataBean().getGuiParameters();
		EclipseUtils.delay(1000); // This is to allow the fitting to complete
		ArrayList<APeak> peaks = new ArrayList<APeak>();
		if (guiBean.containsKey(GuiParameters.FITTEDPEAKS)) {
			Serializable probablePeaks = guiBean.get(GuiParameters.FITTEDPEAKS);
			if (probablePeaks instanceof ArrayList<?> && ((ArrayList<?>) probablePeaks).get(0) instanceof APeak) {
				peaks = (ArrayList<APeak>) probablePeaks;
			}
		}
		Assert.assertEquals(peaks.size(), 3);
		Assert.assertEquals(peaks.get(0).getPosition(), 7818.50);
		Assert.assertEquals(peaks.get(1).getPosition(), 7740.50);
		Assert.assertEquals(peaks.get(2).getPosition(), 7914.50);

		EclipseUtils.delay(1000);
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

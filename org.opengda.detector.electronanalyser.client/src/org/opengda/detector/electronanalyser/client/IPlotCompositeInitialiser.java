package org.opengda.detector.electronanalyser.client;

import uk.ac.gda.devices.vgscienta.IVGScientaAnalyserRMI;

public interface IPlotCompositeInitialiser {

	public void setAnalyser(IVGScientaAnalyserRMI analyser);

	public void setUpdatePV(String updatePV);

	public void setUpdatesPerSecond(double updatesPerSecond);

	public void initialise();

}

package org.opengda.detector.electronanalyser.client;

import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;

public interface IPlotCompositeInitialiser {

	public void setAnalyser(IVGScientaAnalyser analyser);

	public void setUpdatePV(String updatePV);

	public void setUpdatesPerSecond(double updatesPerSecond);

	public void initialise();

}

package org.opengda.detector.electronanalyser.client;

import uk.ac.gda.devices.vgscienta.IVGScientaAnalyserRMI;

public interface IPlotCompositeInitialiser {

	void setAnalyser(IVGScientaAnalyserRMI analyser);

	void setUpdatePV(String updatePV);

	void setUpdatesPerSecond(double updatesPerSecond);

	void initialise();
}

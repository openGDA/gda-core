package uk.ac.gda.arpes.calculator;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResolutionCalculatorConfigurationNano extends ResolutionCalculatorConfigurationHR{
	private static final Logger logger = LoggerFactory.getLogger(ResolutionCalculatorConfigurationNano.class);
	private String analyserResolutionParamsFilePath;

	@Override
	public Double calculateAnalyserResolution(double passEnergy, double analyserSlit) {
		// passEnergy [eV] analyserSlit [microns] -> analyserResolution [meV]
		Map<Integer, double[]>analyserResolutionTable = getParametersFromFile(analyserResolutionParamsFilePath);
		// AnalyserResolutionTable for Nano branch just have Pass Energies and Analyser Resolutions columns
		if ((analyserResolutionTable == null) || (analyserResolutionTable.isEmpty())) return 0.0;
		double[] result = analyserResolutionTable.get((int) Math.round(passEnergy));
		if (result==null) return 0.0;
		return result[0];
	}

	public String getAnalyserResolutionParamsFilePath() {
		return analyserResolutionParamsFilePath;
	}

	public void setAnalyserResolutionParamsFilePath(String analyserResolutionParamsFilePath) {
		this.analyserResolutionParamsFilePath = analyserResolutionParamsFilePath;
	}
}

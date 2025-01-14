package uk.ac.gda.arpes.calculator;

import java.util.Map;

import gda.factory.Findable;

public interface IResolutionCalculatorConfiguration extends Findable {

	String getPhotonEnergyName();

	String getGratingName();

	String getExitSlitName();

	String getAnalyserName();

	String getAnalyserEntranceSlitProviderName();

	String getWorkFunctionFilePath();

	String getBlResolutionParamsFilePath();

	Double getWorkFunction(double grating, double energy, Map<Integer, double[]> workFunctionParameters);

	Double calculateResolvingPower(double exitSlit, double grating, Map<Integer, double[]> beamlineResolutionParameters);

	Double calculateBeamlineResolution(Double photonEnergy, Double blResolvingPower);

	Double calculateAnalyserResolution(double passEnergy, double analyserSlit);

	Double calculateTotalResolution(Double blResolution, Double anResolution);

	Double getDoublesPrecision();

	Double getDefaultWorkFunction();

	Map<Integer, double[]> getParametersFromFile(String filepath);

}
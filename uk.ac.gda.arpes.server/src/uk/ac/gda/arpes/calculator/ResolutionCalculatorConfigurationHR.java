package uk.ac.gda.arpes.calculator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(IResolutionCalculatorConfiguration.class)
public class ResolutionCalculatorConfigurationHR implements IResolutionCalculatorConfiguration {
	private static final Logger logger = LoggerFactory.getLogger(ResolutionCalculatorConfigurationHR.class);

	private String name;
	private String photonEnergyName;
	private String gratingName;
	private String exitSlitName;
	private String analyserName;
	private String workFunctionFilePath;
	private String blResolutionParamsFilePath;

	private List<Double> analyserSlits;
	private int defaultSlitPosition;

	private double doublesPrecision = 0.001;
	private double defaultWorkFunction = 4.44;
	@Override
	public Double getDefaultWorkFunction() {
		return defaultWorkFunction;
	}

	public void setDefaultWorkFunction(Double defaultWorkFunction) {
		this.defaultWorkFunction = defaultWorkFunction;
	}
	@Override
	public Map<Integer, double[]> getParametersFromFile(String filepath) {
		HashMap<Integer, double[]> map = new HashMap<>();

		File source = new File(filepath);
		if (!(source.exists() && source.isFile())) {
			logger.error("Parameter file is empty or does not exist - returned empty parameters map");
			return map;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(source))) {
			boolean firstLine = true;
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (firstLine) {
					firstLine = false;
				} else {
					double[] rowsDoubles = Arrays.stream(line.split(",")).filter(x->!x.replaceAll("\\D", "").isEmpty()).
							mapToDouble(Double::parseDouble).toArray();
					map.put((int) Math.round(rowsDoubles[0]),Arrays.copyOfRange(rowsDoubles,1,rowsDoubles.length));
				}
			}
		} catch (IOException e) {
			logger.error("Failed to load parameters table from file {}",filepath);
		}
		if (!map.isEmpty()) logger.debug("Read parameters from file resulted in map size {}", map.size());
		return map;
	}
	@Override
	public Double getWorkFunction(double grating, double energy, Map<Integer, double[]> workFunctionParameters) {
		if (workFunctionParameters.isEmpty()) return defaultWorkFunction ;
		double[] wfParamArray = workFunctionParameters.get((int) Math.round(grating));
		return wfParamArray[0] + energy*wfParamArray[1] + Math.pow(energy, 2.0)*wfParamArray[2]+
				Math.pow(energy, 3.0)*wfParamArray[3] + Math.pow(energy, 4.0)*wfParamArray[4];
	}

	@Override
	public Double calculateResolvingPower(double exitSlit, double grating, Map<Integer, double[]> beamlineResolutionParameters) {
		if (beamlineResolutionParameters.isEmpty()) return 1.0;
	    double[] params = beamlineResolutionParameters.get((int) Math.round(grating));
	    return params[0]*1000/(params[1]+params[2]*exitSlit/1000);
	}

	@Override
	public Double calculateBeamlineResolution(Double photonEnergy, Double blResolvingPower) {
		// photonEnergy [eV] blResolvingPower[] -> beamlineResolution[meV]
		return ((blResolvingPower!=null) && Math.abs(blResolvingPower.doubleValue()-0)>doublesPrecision)?
				photonEnergy/blResolvingPower*1000: 0;
	}

	@Override
	public Double calculateAnalyserResolution(double passEnergy, double analyserSlit) {
		// passEnergy [eV] analyserSlit [microns] -> analyserResolution [meV]
		return 0.5*(analyserSlit/200.0)*passEnergy;
	}

	@Override
	public Double calculateTotalResolution(Double blResolution, Double anResolution) {
		// blResolution [meV] anResolution [meV] -> totalResolution [meV]
		return Math.sqrt(blResolution*blResolution + anResolution*anResolution);
	}

	@Override
	public Double getDoublesPrecision() {
		return doublesPrecision;
	}

	public void setDoublesPrecision(double doublesPrecision) {
		this.doublesPrecision = doublesPrecision;
	}

	@Override
	public String getPhotonEnergyName() {
		return photonEnergyName;
	}

	public void setPhotonEnergyName(String photonEnergyName) {
		this.photonEnergyName = photonEnergyName;
	}

	@Override
	public String getGratingName() {
		return gratingName;
	}

	public void setGratingName(String gratingName) {
		this.gratingName = gratingName;
	}

	@Override
	public String getExitSlitName() {
		return exitSlitName;
	}

	public void setExitSlitName(String exitSlitName) {
		this.exitSlitName = exitSlitName;
	}

	@Override
	public String getAnalyserName() {
		return analyserName;
	}

	public void setAnalyserName(String analyserName) {
		this.analyserName = analyserName;
	}

	@Override
	public Integer getDefaultSlitPosition() {
		return defaultSlitPosition;
	}

	public void setDefaultSlitPosition(int defaultSlitPosition) {
		this.defaultSlitPosition = defaultSlitPosition;
	}

	public void setAnalyserSlits(List<Double> analyserSlits) {
		this.analyserSlits = analyserSlits;
	}

	@Override
	public List<Double> getAnalyserSlits() {
		return analyserSlits;
	}

	@Override
	public String getWorkFunctionFilePath() {
		return workFunctionFilePath;
	}

	public void setWorkFunctionFilePath(String workFunctionFilePath) {
		this.workFunctionFilePath = workFunctionFilePath;
	}

	@Override
	public String getBlResolutionParamsFilePath() {
		return blResolutionParamsFilePath;
	}

	public void setBlResolutionParamsFilePath(String blResolutionParamsFilePath) {
		this.blResolutionParamsFilePath = blResolutionParamsFilePath;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

}

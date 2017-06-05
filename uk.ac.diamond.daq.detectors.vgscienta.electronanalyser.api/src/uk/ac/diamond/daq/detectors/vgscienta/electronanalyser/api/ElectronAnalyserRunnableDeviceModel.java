package uk.ac.diamond.daq.detectors.vgscienta.electronanalyser.api;

import org.eclipse.scanning.api.annotation.UiLookup;
import org.eclipse.scanning.api.annotation.UiRequired;

import uk.ac.diamond.daq.detectors.addetector.api.AreaDetectorRunnableDeviceModel;

/**
 * This is the simplest model possible to use an VG Scienta electron analyser with the new scanning.
 *
 * It is designed to be used on i05-1
 *
 * @author James Mudd
 */
public class ElectronAnalyserRunnableDeviceModel extends AreaDetectorRunnableDeviceModel {

	private DA30LensMode lensMode = DA30LensMode.DA30_08; // Default to a angular 30 mode
	private int passEnergy = 10;
	private double centreEnergy = 50;
	private int iterations = 1;
	private int slices = 1000;

	public int getSlices() {
		return slices;
	}

	public void setSlices(int slices) {
		this.slices = slices;
	}

	@UiRequired
	public DA30LensMode getLensMode() {
		return lensMode;
	}

	public void setLensMode(DA30LensMode lensMode) {
		this.lensMode = lensMode;
	}

	@UiLookup({"1", "2", "5", "10", "20", "40", "50", "75", "100", "200"})
	public int getPassEnergy() {
		return passEnergy;
	}

	public void setPassEnergy(int passEnergy) {
		this.passEnergy = passEnergy;
	}

	public double getCentreEnergy() {
		return centreEnergy;
	}

	public void setCentreEnergy(double centreEnergy) {
		this.centreEnergy = centreEnergy;
	}

	public int getIterations() {
		return iterations;
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(centreEnergy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + iterations;
		result = prime * result + ((lensMode == null) ? 0 : lensMode.hashCode());
		result = prime * result + passEnergy;
		result = prime * result + slices;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ElectronAnalyserRunnableDeviceModel other = (ElectronAnalyserRunnableDeviceModel) obj;
		if (Double.doubleToLongBits(centreEnergy) != Double.doubleToLongBits(other.centreEnergy))
			return false;
		if (iterations != other.iterations)
			return false;
		if (lensMode != other.lensMode)
			return false;
		if (passEnergy != other.passEnergy)
			return false;
		if (slices != other.slices)
			return false;
		return true;
	}

}

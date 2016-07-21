package gda.device.detector.xspress.xspress2data;

import java.util.List;

import uk.ac.gda.beans.vortex.DetectorDeadTimeElement;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.xspress.XspressDeadTimeParameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.util.CorrectionUtils;

/**
 * Provides tools to calculate the deadtime in Xspress2.
 *
 * @author rjw82
 *
 */
public class Xspress2DeadtimeTools {
	/**
	 * Given an array of the hardwareScalerReadings (4 values per element) for a given frame, this calculates the
	 * deadtime correction factor for each element.
	 * <p>
	 * The output includes excluded detectors (elements). These will have a deadtime of 0.0.
	 *
	 * @param hardwareScalerReadings
	 * @return double[]
	 */
	public double[] calculateDeadtimeCorrectionFactors(long[] hardwareScalerReadings, int numberOfDetectors, XspressParameters xspressParameters, XspressDeadTimeParameters xspressDeadTimeParameters, Double deadtimeEnergy) {
		// assumes in order element0all, element0reset, element0counts, element0time, element1all etc.
		double dataout[] = new double[(numberOfDetectors)];
		int k = 0;
		int l = 0;
		long all;
		long reset;
		// long win; // not used
		long time;
		time = 0;
		final List<DetectorElement> detectors = xspressParameters != null ? xspressParameters.getDetectorList() : null;
		final List<DetectorDeadTimeElement> detectorsDte = xspressDeadTimeParameters != null ? xspressDeadTimeParameters
				.getDetectorDeadTimeElementList() : null;
		DetectorDeadTimeElement detectorDte = null;
		int index = 0;
		if (detectors != null && detectorsDte != null)
			for (DetectorElement detector : detectors) {
				detectorDte = detectorsDte.get(index++);
				if (detector.isExcluded()) {
					k += 4;
					dataout[l] = 0.0;
				}
				else {
					all = hardwareScalerReadings[k++]; // total number of events
					reset = hardwareScalerReadings[k++]; // TFG reset counts
					// win = hardwareScalerReadings[k++]; // in window events. Not used in this calculation
					k++; // ignore win but move on k anyway
					time = hardwareScalerReadings[k++]; // TFG clock counts
					Double processDeadTimeAllEvent = calculateDetectorProcessDeadTimeAllEvent(detectorDte, deadtimeEnergy);
					Double processDeadTimeInWindowEvent = calculateDetectorProcessDeadTimeInWindowEvent(detectorDte, deadtimeEnergy);
					Double factor = dtc(all, reset, time, processDeadTimeAllEvent,processDeadTimeInWindowEvent);
					// need a sensible number if there were zeroes in the reading
					if (factor.isNaN() || factor.isInfinite())
						factor = 1.0;
					dataout[l] = factor;
				}
				l++;
			}
		return dataout;
	}

	private double calculateDetectorProcessDeadTimeAllEvent(DetectorDeadTimeElement detectorDte, Double deadtimeEnergy) {
		Double grad = detectorDte.getProcessDeadTimeAllEventGradient();
		if (grad == null || grad == 0.0 || deadtimeEnergy == null || deadtimeEnergy == 0.0)
			return detectorDte.getProcessDeadTimeAllEventOffset();
		return detectorDte.getProcessDeadTimeAllEventOffset() + grad * deadtimeEnergy;
	}

	private double calculateDetectorProcessDeadTimeInWindowEvent(DetectorDeadTimeElement detectorDte, Double deadtimeEnergy) {
		Double grad = detectorDte.getProcessDeadTimeInWindowGradient();
		if (grad == null || grad == 0.0 || deadtimeEnergy == null || deadtimeEnergy == 0.0)
			return detectorDte.getProcessDeadTimeInWindow();
		return detectorDte.getProcessDeadTimeInWindow() + grad * deadtimeEnergy;
	}

	/**
	 * Documentation from William Helsby is available to explain the maths in this method
	 * @param all
	 * @param reset
	 * @param time
	 * @param processDeadTimeAllEvent
	 * @param processDeadTimeInWindow
	 * @return dead time correction factor for non piled up events
	 */
	private double dtc(long all, long reset, long time, double processDeadTimeAllEvent, double processDeadTimeInWindow) {
		final double clockRate = 12.5e-09;
		// Calculate the reset tick corrected measured count rate
		double dt = (time - reset) * clockRate;
		double measuredRate = all / dt;
		// calculate the input corrected count rate
		double corrected = CorrectionUtils.correct(measuredRate, processDeadTimeAllEvent);
		// calculate dead time correction factor to be applied to the in-window scaler
		double factor = time * clockRate / dt;
		factor *= 1 / Math.exp(-corrected * 2 * processDeadTimeInWindow);
		return factor;
	}

}

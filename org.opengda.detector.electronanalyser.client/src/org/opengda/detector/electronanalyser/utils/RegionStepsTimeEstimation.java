package org.opengda.detector.electronanalyser.utils;

public class RegionStepsTimeEstimation {

	public static long calculateTotalSteps(double energywidth, double energystep,
			double energyrangperimage) {
		// get number of steps required for the scan
		long M = (long) Math.ceil(energywidth * 1000 / energystep);
		// calculate image overlapping number per data point
		long N = (long) (Math.ceil(energyrangperimage / energystep));
		return M + N;
	}
	public static double calculateTotalTime(double stepTime, long totalSteps) {
		return stepTime*totalSteps;
	}

}

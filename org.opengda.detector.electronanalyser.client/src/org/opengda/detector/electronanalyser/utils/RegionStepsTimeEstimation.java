package org.opengda.detector.electronanalyser.utils;

public class RegionStepsTimeEstimation {

	private RegionStepsTimeEstimation() {}

	public static long calculateTotalSteps(double energywidth, double energystep,
			double energyrangperimage) {
		// get number of steps required for the scan
		final long m = (long) Math.ceil(energywidth * 1000 / energystep);
		// calculate image overlapping number per data point
		final long n = (long) (Math.ceil(energyrangperimage / energystep));
		return m + n;
	}
	public static double calculateTotalTime(double stepTime, double totalSteps, int numberOfIterations) {
		return stepTime*totalSteps*numberOfIterations;
	}
}
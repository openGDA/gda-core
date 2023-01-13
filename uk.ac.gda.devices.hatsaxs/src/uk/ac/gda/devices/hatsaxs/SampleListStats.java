package uk.ac.gda.devices.hatsaxs;

import java.util.List;
import java.util.function.ToDoubleFunction;

public class SampleListStats<T> {
	/**
	 * Get the total runtime of a list of samples as a HH:MM:SS string
	 * 
	 * @param samples list of samples
	 * @param timeExtractor function to get a time (in seconds) of each sample
	 * @param overhead the extra time (in seconds) that each sample takes
	 * @return Runtime string in the form hh:mm:ss
	 */
	public static <T> String getRuntimeString(List<T> samples, ToDoubleFunction<T> timeExtractor, int overhead) {
		long totalTime = (long)samples.stream().mapToDouble(timeExtractor).sum();
		totalTime += samples.size() * overhead;
		long hours = totalTime / 3600;
		long minutes = (totalTime % 3600) / 60;
		long seconds = totalTime % 60;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
}

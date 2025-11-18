package gda.device.hidenrga;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import gda.device.DeviceException;
import gda.factory.Findable;
import gda.observable.IObservable;

public interface HidenRGA extends Findable, IObservable {

	/**
	 * Start recording RGA data to an ASCII file with a unique name in the current visit data directory.
	 * <P>
	 * The rate at which data is recording is determined by the collection rate attribute.
	 *
	 * @throws IOException
	 */
	void startRecording() throws IOException;

	void stopRecording();

	boolean isBusy() throws DeviceException;

	Set<Integer> getMasses();

	void setMasses(int[] masses);

	/**
	 * Set the time in seconds between writing data to file in recording mode.
	 * <p>
	 * If <= 0 then data will be recorded as fast as the RGA collects it, if >=1
	 * then GDA will wait a minimum of that value in seconds before recording
	 * the next line of data.
	 *
	 * @param collectionRate
	 */
	void setCollectionRate(double rate);

	double getCollectionRate();

	List<Double> getBarChartPressures() throws IOException;

	int getNumBarChartPressures();

	int getNumberOfMassChannels();
}
package gda.device;

import gda.factory.Findable;
import gda.observable.IObservable;

import java.io.IOException;
import java.util.Set;

public interface HidenRGA extends Findable, IObservable{

	/**
	 * Start recording RGA data to an ASCII file with a unique name in the current visit data directory.
	 * <P>
	 * The rate at which data is recording is determined by the collection rate attribute.
	 * 
	 * @throws IOException
	 */
	public void startRecording() throws IOException;

	public void stopRecording();

	public boolean isBusy() throws DeviceException;

	public Set<Integer> getMasses();

	public void setMasses(int[] masses);
	
	/**
	 * Set the time in seconds between writing data to file in recording mode.
	 * <p>
	 * If <= 0 then data will be recorded as fast as the RGA collects it, if >=1
	 * then GDA will wait a minimum of that value in seconds before recording
	 * the next line of data.
	 * 
	 * @param collectionRate
	 */
	public void setCollectionRate(int rate);
	
	public int getCollectionRate();

}
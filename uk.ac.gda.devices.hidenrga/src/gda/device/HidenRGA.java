package gda.device;

import gda.factory.Findable;

import java.io.IOException;
import java.util.Set;

public interface HidenRGA extends Findable{

	public abstract void startRecording() throws IOException;

	public abstract void stopRecording();

	public abstract boolean isBusy() throws DeviceException;

	public abstract Set<Integer> getMasses();

	public abstract void setMasses(int[] masses);

}
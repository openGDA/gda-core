package uk.ac.gda.devices.cirrus;

import gda.factory.Findable;
import gda.observable.IObservable;

public interface Cirrus extends Findable, IObservable{

	public Integer[] getMasses();

	public void setMasses(Integer[] masses);
}
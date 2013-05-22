package org.opengda.detector.electronanalyser;

import gda.device.detector.addetector.triggering.SimpleAcquire;
import gda.device.detector.areadetector.v17.ADBase;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyserAcquireStrategy extends SimpleAcquire implements IObservable {
	static final Logger logger = LoggerFactory.getLogger(AnalyserAcquireStrategy.class);
	private ObservableComponent oc = new ObservableComponent();

	public AnalyserAcquireStrategy(ADBase adBase, double readoutTime) {
		super(adBase, readoutTime);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void collectData() throws Exception {
		// TODO Auto-generated method stub
		super.collectData();
	}
	@Override
	public void addIObserver(IObserver observer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteIObservers() {
		// TODO Auto-generated method stub
		
	}

}

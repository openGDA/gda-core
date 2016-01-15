/*-
 * Copyright © 2016 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.device.detector.addetector.collectionstrategy;

import gda.device.DeviceException;
import gda.device.detector.addetector.triggering.AbstractADTriggeringStrategy;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdetector.CollectionStrategyBeanInterface;
import gda.scan.ScanInformation;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * This class is intended to be the base class (only) for AbstractADCollectionStrategy. It abstracts out just the functions that are delegated to
 * AbstractADTriggeringStrategy, thus leaving AbstractADCollectionStrategy itself with the functions that it implements itself. AbstractADTriggeringStrategy is
 * used by delegation rather than by inheritance in order to keep the inheritance hierarchy of the new composition system collection strategies separate from
 * old-style strategies.
 */
public abstract class AbstractADCollectionStrategyBase implements CollectionStrategyBeanInterface {

	// NXCollectionStrategyPlugin interface

	@Override
	public double getAcquireTime() throws Exception {
		return delegate.getAcquireTime();
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return delegate.getAcquirePeriod();
	}

	@Override
	@Deprecated
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		delegate.configureAcquireAndPeriodTimes(collectionTime);
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		delegate.prepareForCollection(collectionTime, numImages, scanInfo);
	}

	@Override
	public void collectData() throws Exception {
		delegate.collectData();
	}

	@Override
	public int getStatus() throws Exception {
		return delegate.getStatus();
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, Exception {
		delegate.waitWhileBusy();
	}

	@Override
	public void setGenerateCallbacks(boolean b) {
		delegate.setGenerateCallbacks(b);
	}

	@Override
	public boolean isGenerateCallbacks() {
		return delegate.isGenerateCallbacks();
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) throws Exception {
		return delegate.getNumberImagesPerCollection(collectionTime);
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		return delegate.requiresAsynchronousPlugins();
	}

	// NXPluginBase interface

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public boolean willRequireCallbacks() {
		return delegate.willRequireCallbacks();
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		delegate.prepareForCollection(numberImagesPerCollection, scanInfo);
	}

	@Override
	public void prepareForLine() throws Exception {
		delegate.prepareForLine();
	}

	@Override
	public void completeLine() throws Exception {
		delegate.completeLine();
	}

	@Override
	public List<String> getInputStreamNames() {
		return delegate.getInputStreamNames();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return delegate.getInputStreamFormats();
	}

	// PositionInputStream interface

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException {
		return delegate.read(maxToRead);
	}

	// InitializingBean interface

	@Override
	public void afterPropertiesSet() throws Exception {
		delegate.afterPropertiesSet();
	}

	// AbstractADTriggeringStrategy class functions

	public boolean isAccumlationMode() {
		return delegate.isAccumlationMode();
	}

	public void setAccumlationMode(boolean accumlationMode) {
		delegate.setAccumlationMode(accumlationMode);
	}

	public double getAcc_expo_time() {
		return delegate.getAcc_expo_time();
	}

	public void setAcc_expo_time(double acc_expo_time) {
		delegate.setAcc_expo_time(acc_expo_time);
	}

	public void setReadoutTime(double readoutTime) {
		delegate.setReadoutTime(readoutTime);
	}

	public void setReadAcquisitionTime(boolean readAcquisitionTime) {
		delegate.setReadAcquisitionTime(readAcquisitionTime);
	}

	public void setReadAcquisitionPeriod(boolean readAcquisitionPeriod) {
		delegate.setReadAcquisitionPeriod(readAcquisitionPeriod);
	}

	public double getReadoutTime() {
		return delegate.getReadoutTime();
	}

	public ADBase getAdBase() {
		return delegate.getAdBase();
	}

	public void setAdBase(ADBase adBase) {
		delegate.setAdBase(adBase);
	}

	public boolean isReadAcquisitionTime() {
		return delegate.isReadAcquisitionTime();
	}

	public boolean isReadAcquisitionPeriod() {
		return delegate.isReadAcquisitionPeriod();
	}

	public List<String> getInputStreamUnits() {
		return delegate.getInputStreamUnits();
	}

	public String getTimeFormat() {
		return delegate.getTimeFormat();
	}

	public void setTimeFormat(String timeFormat) {
		delegate.setTimeFormat(timeFormat);
	}

	public String getAcquisitionPeriodUnit() {
		return delegate.getAcquisitionPeriodUnit();
	}

	public void setAcquisitionPeriodUnit(String acquisitionPeriodUnit) {
		delegate.setAcquisitionPeriodUnit(acquisitionPeriodUnit);
	}

	public String getAcquisitionTimeUnit() {
		return delegate.getAcquisitionTimeUnit();
	}

	public void setAcquisitionTimeUnit(String acquisitionTimeUnit) {
		delegate.setAcquisitionTimeUnit(acquisitionTimeUnit);
	}

	public void errorIfPropertySetAfterBeanConfigured(String description) {
		delegate.errorIfPropertySetAfterBeanConfigured(description);
	}

	/*
	 * AbstractADTriggeringStrategy delegate. Functions that are needed but are not implemented in AbstractADTriggeringStrategy should be implemented in
	 * AbstractADCollectionStrategy, not in this delegate.
	 */
	private AbstractADTriggeringStrategy delegate = new AbstractADTriggeringStrategy() {
		@Override
		public void atCommandFailure() throws Exception {
			throw new UnsupportedOperationException("Attempt to call unimplemented function atCommandFailure()");
		}

		@Override
		public void collectData() throws Exception {
			throw new UnsupportedOperationException("Attempt to call unimplemented function collectData()");
		}

		@Override
		public int getNumberImagesPerCollection(double collectionTime) throws Exception {
			throw new UnsupportedOperationException("Attempt to call unimplemented function getNumberImagesPerCollection()");
		}

		@Override
		public void completeCollection() throws Exception {
			throw new UnsupportedOperationException("Attempt to call unimplemented function completeCollection()");
		}

		@Override
		public void stop() throws Exception {
			throw new UnsupportedOperationException("Attempt to call unimplemented function stop()");
		}
	};
}

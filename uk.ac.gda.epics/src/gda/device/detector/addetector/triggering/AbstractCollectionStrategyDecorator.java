/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.detector.addetector.triggering;

import java.util.List;
import java.util.NoSuchElementException;

import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdetector.AsyncNXCollectionStrategy;
import gda.scan.ScanInformation;

import org.springframework.beans.factory.InitializingBean;

/** This class provides a base class from which Collection Strategy Decorators can be derived.
 *  it allows decorators to only override the functions they need to override.
 *  
 *  Collection Strategy Decorators can be used to apply the same functionality to multiple
 *  collection strategies without having to create a separate class for every combination of
 *  class additional 
 *  
 *  Note the class is abstract, but all of the methods are concrete, which prevents this 'do nothing'
 *  decorator class from being instantiated, but allows any or all methods to be overwritten.
 */
public abstract class AbstractCollectionStrategyDecorator implements CollectionStrategyBeanInterface {

	/**
	 * 
	 */
	public AbstractCollectionStrategyDecorator() {
	}

	private CollectionStrategyBeanInterface decoratee;

	/* interface NXCollectionStrategyPlugin */

	@Override
	public double getAcquireTime() throws Exception {
		return getDecoratee().getAcquireTime();
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return getDecoratee().getAcquirePeriod();
	}

	@Deprecated
	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		getDecoratee().configureAcquireAndPeriodTimes(collectionTime);
	}

	@Override
	public void prepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception {
		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);

	}

	@Override
	public void collectData() throws Exception {
		getDecoratee().collectData();
	}

	@Override
	public int getStatus() throws Exception {
		return getDecoratee().getStatus();
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, Exception {
		getDecoratee().waitWhileBusy();
	}

	@Override
	public void setGenerateCallbacks(boolean b) {
		getDecoratee().setGenerateCallbacks(b);
	}

	@Override
	public boolean isGenerateCallbacks() {
		return getDecoratee().isGenerateCallbacks();
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) throws Exception {
		return getDecoratee().getNumberImagesPerCollection(collectionTime);
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		return getDecoratee().requiresAsynchronousPlugins();
	}

	/* interface NXPluginBase */

	@Override
	public String getName() {
		return getDecoratee().getName();
	}

	@Override
	public boolean willRequireCallbacks() {
		return getDecoratee().willRequireCallbacks();
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		getDecoratee().prepareForCollection(numberImagesPerCollection, scanInfo);
	}

	@Override
	public void prepareForLine() throws Exception {
		getDecoratee().prepareForLine();

	}

	@Override
	public void completeLine() throws Exception {
		getDecoratee().completeLine();

	}

	@Override
	public void completeCollection() throws Exception {
		getDecoratee().completeCollection();

	}

	@Override
	public void atCommandFailure() throws Exception {
		getDecoratee().atCommandFailure();

	}

	@Override
	public void stop() throws Exception {
		getDecoratee().stop();

	}

	@Override
	public List<String> getInputStreamNames() {
		return getDecoratee().getInputStreamNames();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return getDecoratee().getInputStreamFormats();
	}

	/* interface PositionInputStream<T> */

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		return getDecoratee().read(maxToRead);
	}

	/* interface InitializingBean */

	@Override
	public void afterPropertiesSet() throws Exception {
		getDecoratee().afterPropertiesSet();
	}

	/* Class functions */

	public CollectionStrategyBeanInterface getDecoratee() {
		return decoratee;
	}

	public void setDecoratee(CollectionStrategyBeanInterface decoratee) {
		this.decoratee = decoratee;
	}

}

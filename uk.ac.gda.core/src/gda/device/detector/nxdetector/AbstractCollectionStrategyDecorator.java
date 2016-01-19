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

package gda.device.detector.nxdetector;

import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.scan.ScanInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/** This class provides a base class from which Collection Strategy Decorators can be derived.
 *  it allows decorators to only override the functions they need to override.
 *
 *  Collection Strategy Decorators can be used to apply the same functionality to multiple
 *  collection strategies without having to create a separate class for every combination required.
 *
 *  Note the class is abstract, but all of the methods are concrete, which prevents this 'do nothing'
 *  decorator class from being instantiated, but allows any or all methods to be overwritten.
 */
public abstract class AbstractCollectionStrategyDecorator extends CollectionStrategyDecoratableBase {

	private CollectionStrategyDecoratableInterface decoratee;
	private boolean propertiesSet = false;

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
	public final void prepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		beforePreparation();
		rawPrepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
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
	public final void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		beforePreparation();
		rawPrepareForCollection(numberImagesPerCollection, scanInfo);
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
	public final void completeCollection() throws Exception {
		beforeCompletion();
		rawCompleteCollection();
		afterCompletion();
	}

	@Override
	public final void atCommandFailure() throws Exception {
		beforeCompletion();
		rawAtCommandFailure();
		afterCompletion();
	}

	@Override
	public final void stop() throws Exception {
		beforeCompletion();
		rawStop();
		afterCompletion();
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
		propertiesSet = true;
	}

	// CollectionStrategyDecoratableInterface

	@Override
	public final void setSuppressSave() {
		suppressSave = true;
		getDecoratee().setSuppressSave();
	}

	@Override
	public final void setSuppressRestore() {
		suppressRestore = true;
		getDecoratee().setSuppressRestore();
	}

	/*
	 * Default implementations of save/restore functions. All overrides of these functions MUST call on to the decoratee. For saveState(), the call to the
	 * decoratee should normally be the first statement in the function; for restoreState(), it should normally be the last.
	 */
	@Override
	public void saveState() throws Exception {
		getDecoratee().saveState();
	}

	@Override
	public void restoreState() throws Exception {
		getDecoratee().restoreState();
	}

	/* Class functions */

	public CollectionStrategyDecoratableInterface getDecoratee() {
		return decoratee;
	}

	public void setDecoratee(CollectionStrategyDecoratableInterface decoratee) {
		errorIfPropertySetAfterBeanConfigured("decoratee");
		this.decoratee = decoratee;
	}

	/** This function recurses through each decorator down to the decorated class
	 *  returning all decorators which are an instance of the specified class.
	 *
	 * @param clazz		 should be the Class object which represents type T
	 * @return			 the list of decoratees which are of the specified type
	 */
	public <T> List<T> getDecorateesOfType(Class<T> clazz) {
		if (!propertiesSet) {
			String name = getName() == null ? "<unknown>" : getName();
			throw new IllegalAccessError("Attempt to getDecorateesOfType(" + clazz.getName() + ") before initialisation of " + name + " complete!");
		}

		List<T> decorateesOfT;

		if (decoratee instanceof AbstractCollectionStrategyDecorator) { // Recurse down
			decorateesOfT = ((AbstractCollectionStrategyDecorator) decoratee).getDecorateesOfType(clazz);
		} else { // Otherwise start a new list
			decorateesOfT = new ArrayList<>();
			// and add decoratee if it is one
			if (clazz.isInstance(decoratee)) {
				decorateesOfT.add(clazz.cast(decoratee));
			}
		}
		// Now add self if we are one.
		if (clazz.isInstance(this)) {
			decorateesOfT.add(clazz.cast(this));
		}
		return decorateesOfT;
	}

	/**
	 * This function can be used to enforce the condition that properties are not changed after Bean initialisation completes.
	 *
	 * @param description used in thrown exceptions
	 */
	protected void errorIfPropertySetAfterBeanConfigured(String description) {
		//if (propertiesSet) throw new IllegalAccessError("Attempt to set property " + description  + " in bean "+ getName() + "after Bean configured!");
	}

	/*
	 * Default implementations of the "raw" functions called above. All overrides of these functions MUST call on to the decoratee. For prepareForCollection(),
	 * the call to the decoratee should normally be the last statement in the function; for completeCollection(), atCommandFailure() & stop(), it should
	 * normally be the first.
	 */
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
	}

	protected void rawPrepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		getDecoratee().prepareForCollection(numberImagesPerCollection, scanInfo);
	}

	protected void rawCompleteCollection() throws Exception {
		getDecoratee().completeCollection();
	}

	protected void rawAtCommandFailure() throws Exception {
		getDecoratee().atCommandFailure();
	}

	protected void rawStop() throws Exception {
		getDecoratee().stop();
	}

	/**
	 * If prepareCollection() has been called by another decorator, the calling decorator will already have caused this one to save its state, and will have set
	 * suppressSave. In this case, just reset the flag. Otherwise, cascade saving the state and suppress saving in all decoratees.
	 */
	private void beforePreparation() throws Exception {
		if (suppressSave) {
			suppressSave = false;
		} else {
			saveState();
			getDecoratee().setSuppressSave();
		}
	}

	/**
	 * We want to restore all state after all other closing-down tasks have completed, so suppress restoring of state in all
	 */
	private void beforeCompletion() {
		if (!suppressRestore) {
			getDecoratee().setSuppressRestore();
		}
	}

	/**
	 * Another decorator may be in control of restoring the state, so check the flag before restoring.
	 *
	 * @throws Exception
	 */
	private void afterCompletion() throws Exception {
		if (suppressRestore) {
			suppressRestore = false;
		} else {
			restoreState();
		}
	}
}

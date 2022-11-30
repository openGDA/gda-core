/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.areadetector.v17.ImageMode;
import gda.scan.ScanInformation;

/**
 * An {@link AbstractADCollectionStrategyDecorator} that configures both EPICS Area Detector's Image Mode and Number of Images.
 *
 * This class uses index position for EPICS area detector image mode instead of {@link ImageMode} so it applies to any EPICS
 * area detector which has different enumerated values for its image mode.
 * <p>
 * Example configuration usage:
 * <pre>
 * {@code
 * 	<bean id="medipixMultipleCollectionStrategy" class="gda.device.detector.pco.collectionstrategy.PCOStopDecorator">
		<property name="restoreAcquireState" value="true" />
		<property name="decoratee">
			<bean class="gda.device.detector.addetector.collectionstrategy.ImageModeDecorator">
				<property name="restoreNumImagesAndImageMode" value="true" />
				<property name="imageMode" value="1" /> <!-- multiple image mode -->
				<property name="decoratee">
					<bean id="medipixacquireperiod"	class="gda.device.detector.addetector.collectionstrategy.ConfigureAcquireTimeAcquirePeriodDecorator">
						<property name="restoreAcquireTime" value="true" />
						<property name="restoreAcquirePeriod" value="true" />
						<property name="acquirePeriodExpression" value="#acquireTime + 0.005" />
						<property name="decoratee">
							<bean id="medipixtriggermode_Auto" class="gda.device.detector.addetector.collectionstrategy.TriggerModeDecorator">
								<property name="restoreTriggerMode" value="true" />
								<property name="triggerMode" value="0" /> <!-- Auto trigger mode -->
								<property name="decoratee" ref="softstatrstop" />
							</bean>
						</property>
					</bean>
				</property>
			</bean>
		</property>
	</bean>}
 *</pre>
 *
 * Note: there are specialised decorators for SINGLE mode {@link SingleImageModeDecorator} and MULTIPLE mode {@link MultipleImageModeDecorator}.
 *
 * @since GDA 9.12
 */
public class ImageModeDecorator extends AbstractADCollectionStrategyDecorator {

	private static final Logger logger = LoggerFactory.getLogger(ImageModeDecorator.class);

	private int imageMode;
	private boolean restoreNumImagesAndImageMode = false;
	private int numImagesSaved;
	private short imageModeSaved;

	private int numberOfImagesPerCollection=1;

	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception {
		logger.trace("rawPrepareForCollection({}, {}, {}) called", collectionTime, numberImagesPerCollection, scanInfo);
		if (numberImagesPerCollection != getDecoratee().getNumberImagesPerCollection(collectionTime))
			logger.warn("numberImagesPerCollection {} not equal to getDecoratee().getNumberImagesPerCollection({})", numberImagesPerCollection, collectionTime,
					getDecoratee().getNumberImagesPerCollection(collectionTime));

		getAdBase().setNumImages(getNumberOfImagesPerCollection());
		getAdBase().setImageMode(getImageMode());
		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
	}

	// CollectionStrategyBeanInterface interface

	@Override
	public void saveState() throws Exception {
		logger.trace("saveState() called, restoreNumImagesAndImageMode={}", restoreNumImagesAndImageMode);
		getDecoratee().saveState();
		if (restoreNumImagesAndImageMode) {
			numImagesSaved = getAdBase().getNumImages();
			imageModeSaved = getAdBase().getImageMode();
			existingStateSaved=true;
			logger.debug("Saved State now numImagesSaved={}, imageModeSaved={}", numImagesSaved, imageModeSaved);
		}
	}

	@Override
	public void restoreState() throws Exception {
		logger.trace("restoreState() called, restoreNumImagesAndImageMode={}", restoreNumImagesAndImageMode);
		if (restoreNumImagesAndImageMode && existingStateSaved) {
			getAdBase().setNumImages(numImagesSaved);
			getAdBase().setImageMode(imageModeSaved);
			existingStateSaved=false;
			logger.debug("Restored state to numImagesSaved={}, imageModeSaved={}", numImagesSaved, imageModeSaved);
		}
		getDecoratee().restoreState();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getDecoratee()==null) throw new IllegalStateException("'decoratee' is not set!");
		super.afterPropertiesSet();
	}

	public boolean isRestoreNumImagesAndImageMode() {
		return restoreNumImagesAndImageMode;
	}

	public void setRestoreNumImagesAndImageMode(boolean restoreNumImagesAndImageMode) {
		this.restoreNumImagesAndImageMode = restoreNumImagesAndImageMode;
	}

	public int getImageMode() {
		return imageMode;
	}

	public void setImageMode(int imageMode) {
		this.imageMode = imageMode;
	}

	@Override
	public int getNumberImagesPerCollection(double unusedCollectionTime) throws Exception {
		return getNumberOfImagesPerCollection();
	}

	public int getNumberOfImagesPerCollection() {
		return numberOfImagesPerCollection;
	}

	public void setNumberOfImagesPerCollection(int numberOfImagesPerCollection) {
		this.numberOfImagesPerCollection = numberOfImagesPerCollection;
	}
}

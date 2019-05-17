/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import gda.scan.ScanInformation;

/**
 * An {@link AbstractADCollectionStrategyDecorator} that configures both EPICS Area Detector's acquisition time and acquisition period.
 *
 * It supports a Spring-configurable {@link Expression} describing relationship between acquisition time and acquisition period,
 * for example, <property name="acquirePeriodExpression" value="#acquireTime + 0.005" />
 *
 */
public class ConfigureAcquireTimeAcquirePeriodDecorator extends AbstractADCollectionStrategyDecorator {

	private static final Logger logger = LoggerFactory.getLogger(ConfigureAcquireTimeAcquirePeriodDecorator.class);

	private boolean restoreAcquireTime = false;
	private boolean restoreAcquirePeriod = false;

	private double acquireTimeSaved;
	private double acquirePeriodSaved;

	private String acquirePeriodExpression;
	// NXCollectionStrategyPlugin interface

	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		logger.trace("rawPrepareForCollection({}, {}, {}) called", collectionTime, numberImagesPerCollection, scanInfo);
		getAdBase().setAcquireTime(collectionTime);
		ExpressionParser parser=new SpelExpressionParser();
		StandardEvaluationContext context = new StandardEvaluationContext();
		context.setVariable("acquireTime", collectionTime);
		Expression exp = parser.parseExpression(getAcquirePeriodExpression());
		double newValue = exp.getValue(context, Double.class);
		getAdBase().setAcquirePeriod(newValue);
		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
	}

	// CollectionStrategyDecoratableInterface interface

	@Override
	public void saveState() throws Exception {
		logger.trace("saveState() called, restoreAcquireTime={}, restoreAcquirePeriod={}", restoreAcquireTime, restoreAcquirePeriod);
		getDecoratee().saveState();
		if (restoreAcquireTime) {
			acquireTimeSaved = getAdBase().getAcquireTime();
			logger.debug("Saved State now acquireTimeSaved={}", acquireTimeSaved);
		}
		if (restoreAcquirePeriod) {
			acquirePeriodSaved = getAdBase().getAcquirePeriod();
			logger.debug("Saved State now acquirePeriodSaved={}", acquirePeriodSaved);
		}
	}

	@Override
	public void restoreState() throws Exception {
		logger.trace("restoreState() called, restoreAcquireTime={}, acquireTimeSaved={}", restoreAcquireTime, acquireTimeSaved);
		if (restoreAcquireTime) {
			getAdBase().setAcquireTime(acquireTimeSaved);
			logger.debug("Restored state to acquireTimeSaved={}", acquireTimeSaved);
		}
		if (restoreAcquirePeriod) {
			getAdBase().setAcquirePeriod(acquirePeriodSaved);
			logger.debug("Restored state to acquirePeriodSaved={}", acquirePeriodSaved);
		}
		getDecoratee().restoreState();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getDecoratee()==null) throw new IllegalStateException("'decoratee' is not set!");
		super.afterPropertiesSet();
	}

	public boolean isRestoreAcquireTime() {
		return restoreAcquireTime;
	}

	public void setRestoreAcquireTime(boolean restoreAcquireTime) {
		this.restoreAcquireTime = restoreAcquireTime;
	}

	public boolean isRestoreAcquirePeriod() {
		return restoreAcquirePeriod;
	}

	public void setRestoreAcquirePeriod(boolean restoreAcquirePeriod) {
		this.restoreAcquirePeriod = restoreAcquirePeriod;
	}

	public String getAcquirePeriodExpression() {
		return acquirePeriodExpression;
	}

	public void setAcquirePeriodExpression(String acquirePeriodExpression) {
		this.acquirePeriodExpression = acquirePeriodExpression;
	}
}

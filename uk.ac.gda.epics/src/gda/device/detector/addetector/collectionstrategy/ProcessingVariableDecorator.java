/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import gda.configuration.properties.LocalProperties;
import gda.epics.connection.EpicsController;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

/**
 * An EPICS Processing Variable (PV) decorator to configure any given PV parameter before data collection using scan and
 * restore this PV to its original value after scan.
 *
 *<p>
 * This can be used to configure a PV of different device that does not appear in the scan command.
 * For an example use case see https://jira.diamond.ac.uk/browse/I06-456.
 *<p>
 * It supports Spring <a href="https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/expressions.html">SPEL</a>
 * expression String in bean definition which depends on area detector acquire time.
 * In order to evaluate the expression dynamically, plain String text expression must be used in bean definition,
 * Not the expression template surrounded by #{..}.
 *<br/>
 * This decorator must be inside of {@link ConfigureAcquireTimeDecorator} in collection strategy bean definition
 * if the specified expression depends on acquire time (see example below). Otherwise it can be at any place in the decorator chain before {@link SoftwareStartStop}.
 *<p>
 * When {@link #isEnabled()} returns false, it does nothing but pass through to its decoratee.
 *<p>
 * It has built in support for dummy mode operation off beamline, which simply print message to Jython terminal.
 *<p>
 * Example usage:
 * <pre>
 * {@code
 <bean id="pcocontroller" class="gda.device.detector.pco.DummyPCODriverController" init-method="afterPropertiesSet">
 <bean id="pcoArm" class="gda.device.detector.pco.collectionstrategy.PCOArmDecorator">
		<property name="restoreArm" value="true"/>
		<property name="pcoController" ref="pcocontroller"/>
		<property name="decoratee">
			<bean class="gda.device.detector.addetector.collectionstrategy.SoftwareStartStop">
				<property name="adBase" ref="pco_adbase" />
				<property name="restoreAcquireState" value="true"/>
				<property name="stopAquiringInPreparation" value="false"/>
			</bean>
		</property>
	</bean>
   	<bean id="kbRasteringFreq" class="gda.device.detector.addetector.collectionstrategy.ProcessingVariableDecorator">
   		<property name="pvName" value="BL06I-OP-KBM-01:VFM:FPITCH:FREQ"/>
   		<property name="expression" value="1/#acquireTime lt 10.0 ? 1/#acquireTime : 10.0"/>
   		<property name="enabled" value="true"/>
   		<property name="restorePvValue" value="true"/>
		<property name="decoratee" ref="pcoArm"/>
   	</bean>
	<bean id="pcotriggermode_soft" class="gda.device.detector.pco.collectionstrategy.PCOTriggerModeDecorator">
		<property name="restoreTriggerMode" value="true"/>
		<property name="triggerMode" value="SOFT"/> <!-- possible values: AUTO, SOFT, EXTSOFT, EXTPULSE, EXTONLY -->
		<property name="decoratee" ref="kbRasteringFreq"/>
	</bean>
	<bean id="pcoacquireperiod_soft" class="gda.device.detector.pco.collectionstrategy.PCOConfigureAcquireTimeAcquirePeriodDecorator">
		<property name="restoreAcquireTime" value="true" />
		<property name="restoreAcquirePeriod" value="true"/>
		<property name="acquirePeriod" value="0.1"/>
		<property name="decoratee" ref="pcotriggermode_soft"/>
	</bean>
	<bean id="pcoimagemodesingle" class="gda.device.detector.pco.collectionstrategy.PCOImageModeDecorator">
		<property name="restoreNumImagesAndImageMode" value="true"/>
		<property name="imageMode" value="SINGLE"/> <!-- possible modes: SINGLE, MULTIPLE, CONTINUOUS-->
		<property name="decoratee" ref="pcoacquireperiod_soft"/>
	</bean>
	<bean id="pcoCollectionStrategy" class="gda.device.detector.pco.collectionstrategy.PCOStopDecorator">
		<property name="restoreAcquireState" value="true"/>
		<property name="decoratee">
			<bean class="gda.device.detector.pco.collectionstrategy.PCOADCModeDecorator">
				<property name="restoreADCMode" value="true"/>
				<property name="adcMode" value="OneADC"/> <!-- possible values: OneADC, TwoADC -->
				<property name="pcoController" ref="pcocontroller"/>
				<property name="decoratee" ref="pcoimagemodesingle"/>
			</bean>
		</property>
	</bean>
	}
 *</pre>
 */
public class ProcessingVariableDecorator extends AbstractADCollectionStrategyDecorator {
	private static final Logger logger = LoggerFactory.getLogger(ProcessingVariableDecorator.class);
	/**
	 * EPICS Utility
	 */
	private final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();

	private String pvName;
	private String expression;
	private boolean enabled=false;
	private boolean restorePvValue = false;
	private double pvValueSaved=1.0;

	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {

		if (isEnabled()) {
			double acquireTime=getAdBase().getAcquireTime();
			ExpressionParser parser=new SpelExpressionParser();
			StandardEvaluationContext context = new StandardEvaluationContext();
			context.setVariable("acquireTime", acquireTime);
			Expression exp = parser.parseExpression(getExpression());
			double newValue = exp.getValue(context, Double.class);

			if (LocalProperties.isDummyModeEnabled()) {
				print(String.format("set %s to %f", getPvName(), newValue));
			} else {
				setPvValue(newValue);
			}
		}
		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo); // must be called before detector initialise
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getPvName() == null) {
			throw new IllegalArgumentException("'pvName' needs to be declared");
		}
		if (getExpression() == null) {
			throw new IllegalArgumentException("'expression' needs to be declared");
		}
		if (getDecoratee()==null) throw new IllegalStateException("'decoratee' is not set!");
		super.afterPropertiesSet();
	}

	// CollectionStrategyDecoratableInterface interface

	@Override
	public void saveState() throws Exception {
		getDecoratee().saveState();
		if (isEnabled()) {
			logger.trace("saveState() called, restorePvValue={}", restorePvValue);
			if (isRestorePvValue()) {
				if (LocalProperties.isDummyModeEnabled()) {
					print(String.format("get PV value %f from %s", pvValueSaved, getPvName()));
				} else {
					pvValueSaved=getPvValue();
				}
				logger.debug("Saved PV value now pvValueSaved={}", pvValueSaved);
			}
		}
	}

	@Override
	public void restoreState() throws Exception {
		if (isEnabled()) {
			logger.trace("restoreState() called, restorePvValue={}, pvValueSaved={}", restorePvValue, pvValueSaved);
			if (isRestorePvValue()) {
				if (LocalProperties.isDummyModeEnabled()) {
					print(String.format("set %s to %f", getPvName(), pvValueSaved));
				} else {
					setPvValue(pvValueSaved);
				}
				logger.debug("Restored state to pvValueSaved={}", pvValueSaved);
			}
		}
		getDecoratee().restoreState();
	}

	public double getPvValue() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(getPvName()));
		} catch (Exception ex) {
			logger.error("{}: Cannot getPvValue", getName());
			throw ex;
		}
	}

	public void setPvValue(double value) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(getPvName()), value);
		} catch (Exception ex) {
			logger.error("{}: Cannot setPvValue", getName());
			throw ex;
		}
	}

	private Channel getChannel(String fullPvName) throws CAException, TimeoutException {
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			//only create channel if not already existed!
			try {
				channel = EPICS_CONTROLLER.createChannel(fullPvName);
			} catch (CAException cae) {
				logger.warn("Problem creating channel", cae);
				throw cae;
			} catch (TimeoutException te) {
				logger.warn("Problem creating channel", te);
				throw te;
			}
			channelMap.put(fullPvName, channel);
		}
		return channel;
	}

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	public boolean isRestorePvValue() {
		return restorePvValue;
	}

	public void setRestorePvValue(boolean restorePvValue) {
		this.restorePvValue = restorePvValue;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}
	/**
	 * method to print message to the Jython Terminal console.
	 *
	 * @param msg
	 */
	private void print(String msg) {
		if (InterfaceProvider.getTerminalPrinter() != null) {
			InterfaceProvider.getTerminalPrinter().print(msg);
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}

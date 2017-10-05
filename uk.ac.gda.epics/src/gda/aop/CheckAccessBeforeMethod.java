package gda.aop;


import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;

/**
 * Intercept before method call to enforce access rule defined in an instance of {@link DeviceAccess}:
 * <li> proceed when access is granted;</li>
 * <li> throw {@link IllegalAccessException} if access is not granted</li>
 *
 * <p>
 * The original requirement for this comes from I06 and I10, where access control to shared devices between
 * the two end stations are set in EPICS based on which end station hutch shutter is opening.
 * </p>
 * <p>
 * It is designed to be used with Spring AOP and IoC, see example bean configurations:
 *
 * <pre>
<ol><li>define data source from EPICS controller PV</li>
{@code
<bean id="pgmController" class="gda.device.enumpositioner.EpicsSimpleMbbinary">
	<property name="recordName" value="BL06I-OP-PGM-01:CONTROLLER"/>
</bean>
} <br/>
<li>define Device access control bean </li>
{@code	<bean id="accessControl" class="gda.aop.DeviceAccess" init-method="init">
	<property name="controller" ref="pgmController"/>
	<property name="endStation" value="PEEM"/>
</bean>
} <br/>
<li> define advice before method where access to the device is enforced</li>
{@code
<bean id="accessCheckAdvice" class="gda.aop.CheckAccessBeforeMethod">
	<property name="accessControl" ref="accessControl"/>
</bean>
}<br/>
<li>define point cut advisor for specified method's signatures</li>
{@code
<bean id="accessCheckAdvisor" class="org.springframework.aop.support.NameMatchMethodPointcutAdvisor">
	<property name="mappedNames" value="asynchronousMoveTo, rawAsynchronousMoveTo" />
	<property name="advice" ref="accessCheckAdvice" />
</bean>
} <br/>
<li>apply the advice and advisor to Scannable instance</li>
{@code
<!-- Plane Grating Monochromator Energy is Guarded -->
<bean id="pgmenergy" class="org.springframework.aop.framework.ProxyFactoryBean">
	<property name="target">
		<bean class="gda.device.scannable.ScannableMotor">
			<property name="motor" ref="MotorEnergy_PGM" />
			<property name="protectionLevel" value="0" />
			<property name="initialUserUnits" value="eV" />
			<property name="hardwareUnitString" value="eV" />
			<property name="outputFormat">
				<array>
					<value>%11.7f</value>
				</array>
			</property>
		</bean>
	</property>
	<property name="interceptorNames">
		<list>
			<value>accessCheckAdvisor</value>
		</list>
	</property>
</bean>
} <br/>
 * </pre>
 * </p>
 */
public class CheckAccessBeforeMethod implements MethodInterceptor {
	private DeviceAccess accessControl;
	private static final Logger logger = LoggerFactory.getLogger(CheckAccessBeforeMethod.class);
	@Override
	public Object invoke(MethodInvocation arg0) throws Throwable {
		// user info is stored in metadata entries on user log into GDA.
		final Metadata metadata = GDAMetadataProvider.getInstance();
		final String userid = metadata.getMetadataValue("federalid");
		// at GDA server start up time, there is no user ID available
		if (userid == null || userid.isEmpty())
			//TODO test in shutdown to see if this still required or not after I06-294
			return arg0.proceed();
		logger.debug("Check if user {} has access right to {}", userid, arg0.getThis());
		if (accessControl != null && accessControl.hasAccess()) {
			return arg0.proceed();
		} else {
			logger.info("User {} has NO access right to {}", userid, arg0.getThis());
			throw new IllegalAccessError("You do not have access right to the device requested.");
		}
	}

	public DeviceAccess getAccessControl() {
		return accessControl;
	}

	public void setAccessControl(DeviceAccess accessControl) {
		this.accessControl = accessControl;
	}

}

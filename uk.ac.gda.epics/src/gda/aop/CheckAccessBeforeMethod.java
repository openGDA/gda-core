package gda.aop;


import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.epics.AccessControl;
import gda.jython.authoriser.AuthoriserProvider;

/**
 * This method interceptor provides method execution control based on the status of the {@link AccessControl} instance. Only users having
 * {@link gda.epics.AccessControl.Status} ENABLED can execute the target object's method, otherwise an {@link IllegalAccessError} will be thrown, thus blocking
 * the execution of tartget's method. Beamline staffs are exempted from this restriction.
 * <p>
 * The original requirements for this come from I06 and I10, where access control to shared devices between the two branches are set in EPICS based on which
 * shutter is opened.
 * <p>
 * It is designed to be used with Spring AOP and IoC, for example:
 *
 * <pre>
 * {@code
 * <bean id="doAccessCheck" class="gda.aop.checkAccessBeforeMethod">
 * 	<property name="accessControl" ref="accessControl"/>
 * </bean>
 * } <br>
 * Then being used in a proxy bean by name: <br>
 * {@code
 * <bean id="MotorEnergy_PGM" class="org.springframework.aop.framework.ProxyFactoryBean">
 *	<property name="target">
 * 		<bean class="gda.device.motor.EpicsMotor">
 * 			<property name="deviceName" value="PGM.EN" />
 *			<property name="accessControl" ref="accessControl" />
 *		</bean>
 *	</property>
 *	<property name="interceptorNames">
 *		<list>
 *			<value>doAccessCheck</value>
 *		</list>
 *	</property>
 * </bean>
 * } <br>
 * where <code>accessControl</code> is defined as <br>
 * {@code
 *  <bean id="accessControl" class="gda.epics.AccessControl">
 *  	<property name="accessControlPvName" value="BL06I-OP-PGM-01:CONTROLLER" />
 *  	<property name="enableValue" value="0" />
 *  	<property name="disableValue" value="1" />
 *  </bean>
 *  } <br>
 * Implementation note: checking isLocalStaff require file access, so it is better to do it only when necessary.
 */
public class CheckAccessBeforeMethod implements MethodInterceptor {
	private AccessControl accessControl;
	private static final Logger logger = LoggerFactory.getLogger(CheckAccessBeforeMethod.class);
	@Override
	public Object invoke(MethodInvocation arg0) throws Throwable {
		// user info is stored in metadata entries on user log into GDA.
		final Metadata metadata = GDAMetadataProvider.getInstance();
		final String userid = metadata.getMetadataValue("federalid");
		// at GDA server start up time, there is no user ID available
		if (userid == null || userid.isEmpty())
			return arg0.proceed();
		logger.debug("Check if user {} has access right to {}", userid, arg0.getThis());
		if (accessControl != null && accessControl.getStatus() == AccessControl.Status.ENABLED) {
			logger.debug("User {} has access right to {}", userid, arg0.getThis());
			return arg0.proceed();
		} else if (AuthoriserProvider.getAuthoriser().isLocalStaff(userid)) {
			// beamline staffs always have all access
			logger.debug("User {} has access right to {}", userid, arg0.getThis());
			return arg0.proceed();
		} else {
			logger.debug("User {} has NO access right to {}", userid, arg0.getThis());
			throw new IllegalAccessError("You do not have access right to the device requested.");
		}
	}

	public AccessControl getAccessControl() {
		return accessControl;
	}

	public void setAccessControl(AccessControl accessControl) {
		this.accessControl = accessControl;
	}

}

package uk.ac.gda.test.helpers.springmock;

import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Map;

import org.mockito.Mockito;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A {@link FactoryBean} for creating mocked beans based on Mockito so that they can be {@link Autowired} into Spring
 * test configurations. Allows simple stubbing of methods for when you want the mock to return particular value when
 * particular method is called.
 * 
 * @see org.mockito.Mockito <pre>
 * 	<bean id="pco1_hdf5_Zebra" class="uk.ac.gda.test.helpers.springmock.MockitoFactoryBean">
 * 		<constructor-arg name="classToBeMocked"
 * 			value="gda.device.detector.nxdetector.NXPlugin" />
 * 		<constructor-arg name="properties">
 * 			<map>
 * 				<entry key="getName" value="pco1_hdf5_Zebra" />
 * 			</map>
 * 		</constructor-arg>
 * 	</bean>
 * </pre>
 **/
public class MockitoFactoryBean<T> implements FactoryBean<T> {

	private Class<T> classToBeMocked;

	/**
	 * Creates a Mockito mock instance of the provided class.
	 * 
	 * @param classToBeMocked
	 *            The class to be mocked.
	 */
	public MockitoFactoryBean(Class<T> classToBeMocked, Map<String, Object> properties) {
		this.classToBeMocked = classToBeMocked;
		this.properties = properties;
	}

	public MockitoFactoryBean(Class<T> classToBeMocked) {
		this(classToBeMocked, null);
	}

	final Map<String, Object> properties;

	@Override
	public T getObject() throws Exception {
		T mock = Mockito.mock(classToBeMocked);

		if (properties != null) {
			Class<? extends Object> class1 = mock.getClass();
			for (String methodName : properties.keySet()) {

				Method method = class1.getMethod(methodName, (Class[]) null);
				when(method.invoke(mock, (Object[]) null)).thenReturn(properties.get(methodName));
			}
		}
		return mock;
	}

	@Override
	public Class<?> getObjectType() {
		return classToBeMocked;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
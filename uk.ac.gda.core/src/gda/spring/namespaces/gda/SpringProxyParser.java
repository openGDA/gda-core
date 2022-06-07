/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.spring.namespaces.gda;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;

import java.util.Collection;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gda.factory.Findable;
import gda.factory.Finder;
import gda.util.osgi.OSGiServiceRegister;
import uk.ac.gda.core.GDACoreActivator;
import uk.ac.gda.remoting.client.GdaRmiProxy;

/**
 * Create a proxy for an object exported from the server
 * <br>
 * Optionally also register it as an OSGi service that can be used throughout the client.
 * This combines the existing functionality provided by the {@link GdaRmiProxy} and
 * {@link OSGiServiceRegister} classes but wraps them in a less verbose xml element. This
 * allows previous configurations such as
 * <pre> {@code <bean id="commandQueueProcessor" class="uk.ac.gda.remoting.client.GdaRmiProxy" />
 *
 * <bean class="gda.util.osgi.OSGiServiceRegister">
 *     <property name="class" value="gda.commandqueue.Processor" />
 *     <property name="service" ref="commandQueueProcessor" />
 * </bean>
 *
 * <bean class="gda.util.osgi.OSGiServiceRegister">
 *     <property name="class" value="gda.commandqueue.Queue" />
 *     <property name="service" ref="commandQueueProcessor" />
 * </bean>}</pre>
 * to be rewritten as
 * <pre> {@code <gda:proxy id="commandQueueProcessor" >
 *     <gda:service interface="gda.commandqueue.Processor" />
 *     <gda:service interface="gda.commandqueue.Queue" />
 * </gda:proxy>}</pre>
 *
 * If an imported bean does not need to be registered as a service, the previous configuration
 * <pre> {@code <bean id="command_server" class="uk.ac.gda.remoting.client.GdaRmiProxy" />}</pre>
 * to be replaced with
 * <pre> {@code <gda:proxy id="command_server" />}</pre>
 * where the class used to create the proxy does not need to be known.
 */
public class SpringProxyParser implements BeanDefinitionParser {

	private static final Logger logger = LoggerFactory.getLogger(SpringProxyParser.class);

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		String beanId = element.getAttribute("id");
		if (beanId.isEmpty()) {
			throw new IllegalStateException("Imported bean id cannot be empty");
		}
		Collection<String> interfaces = getServiceInterfaces(element);
		if (!interfaces.isEmpty()) {
			Findable imported = Finder.findOptionalOfType(beanId, Findable.class)
					.orElseThrow(() -> new IllegalStateException("No object called " + beanId + " can be found"));

			BundleContext bundleContext = GDACoreActivator.getBundleContext();
			interfaces.forEach(name -> {
				bundleContext.registerService(name, imported, null);
				logger.debug("Registered '{}' as service {}", imported.getName(), name);
			});
		}

		// Add GdaRmiProxy to context so other beans can reference it if required.
		// This does mean there are two finder calls but the second should not require additional
		// server lookups as the object will already have been exported
		GenericBeanDefinition rmiProxy = new GenericBeanDefinition();
		rmiProxy.setBeanClass(GdaRmiProxy.class);
		parserContext.getRegistry().registerBeanDefinition(beanId, rmiProxy);
		return null;
	}

	/** Extract the set of interface names for this element */
	private Collection<String> getServiceInterfaces(Element element) {
		NodeList services = element.getElementsByTagNameNS(element.getNamespaceURI(), "service");
		return range(0, services.getLength())
				.mapToObj(services::item)
				.map(Node::getAttributes)
				.map(n -> n.getNamedItem("interface"))
				.map(Node::getNodeValue)
				.collect(toSet());
	}
}

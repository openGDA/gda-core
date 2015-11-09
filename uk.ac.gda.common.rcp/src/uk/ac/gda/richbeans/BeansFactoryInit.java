/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.richbeans;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.richbeans.reflection.BeansFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.util.beans.xml.XMLRichBean;

/**
 *
 */
public class BeansFactoryInit {

	private static final Logger logger = LoggerFactory.getLogger(BeansFactoryInit.class);

	/**
	 * Initialises {@link BeansFactory} from the Eclipse extension registry. This function is separated from
	 * {@link BeansFactory} so that uk.ac.gda.common does not depend on Eclipse/RCP.
	 * 
	 * @throws Exception
	 *             if BeansFactory does not initialze
	 */
	@SuppressWarnings("unchecked")
	public static void initBeansFactory() throws Exception {
		try {

			IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
					"uk.ac.common.beans.factory");
			final List<Class<? extends XMLRichBean>> classes = new ArrayList<Class<? extends XMLRichBean>>(7);
			for (IConfigurationElement e : config) {

				final Class<? extends XMLRichBean> beanClass = (Class<? extends XMLRichBean>) e.createExecutableExtension(
						"class").getClass();
				classes.add(beanClass);

				if (e.getAttribute("describer") != null) {
					e.createExecutableExtension("describer");
				}
			}
			BeansFactory.setClasses(classes.toArray(new Class[classes.size()]));
		} catch (Exception ne) {
			logger.error("Cannot initialize BeansFactory", ne);
			throw ne;
		}
	}
}

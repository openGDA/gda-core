/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.data.metadata.icat;

import gda.configuration.properties.LocalProperties;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;

/**
 * Reads an xml file which provides a user to experiment mapping
 */
public class XMLIcat extends IcatBase {

	private DateFormat formatter = new SimpleDateFormat("HH:mm dd-MM-yy");

	@Override
	protected String getExperimentTitleAccessName() {
		return "experimentalTitle";
	}

	@Override
	protected String getValue(String visitIDFilter, String userNameFilter, String accessName) throws Exception {
		
		String filepath = "file:" + LocalProperties.get(URL_PROP);
		Resource xmlfile = new FileSystemResourceLoader().getResource(filepath);
		XmlBeanFactory bf = new XmlBeanFactory(xmlfile);
		
		long tolerance = LocalProperties.getAsInt(SHIFT_TOL_PROP,1440);

		// if not filtering on visit ID
		if (visitIDFilter == null || visitIDFilter.isEmpty()) {
			
			//loop over all the beans
			String values = "";
			Map<String, XMLIcatEntry> beans = bf.getBeansOfType(XMLIcatEntry.class);
			for (XMLIcatEntry bean : beans.values()) {
				// filter on username
				String names[] = bean.getUsernames().split(",");
				if (ArrayUtils.contains(names, userNameFilter)) {

					// filter on date
					Date now;
					if (operatingDate != null) {
						now = operatingDate;
					} else {
						now = new Date();
					}

					Date start = formatter.parse(bean.getExperimentStart());
					Date end = formatter.parse(bean.getExperimentStart());
					start.setTime(start.getTime() - tolerance * 60000);// tolerance is in minutes but getTime returns in
					// ms
					end.setTime(end.getTime() + tolerance * 60000); // tolerance is in minutes but getTime returns in ms

					if (now.after(start) && now.before(end)) {
						
						// add to return string
						try {
							if (values.isEmpty()) {
								values = BeanUtils.getProperty(bean, accessName);
							} else {
								values += "," + BeanUtils.getProperty(bean, accessName);
							}
						} catch (Exception e) {
							logger.warn("Exception trying to get property " + accessName + " from bean.", e);
						}
					}
				}
			}

			// return the values string
			if (values.isEmpty()) {
				return null;
			}
			return values;
		}

		// else find the experiment for that visit and get its property
		XMLIcatEntry visit = bf.getBean(visitIDFilter, XMLIcatEntry.class);
		String names[] = visit.getUsernames().split(",");
		if (ArrayUtils.contains(names, userNameFilter)) {
			try {
				return BeanUtils.getProperty(visit, accessName);
			} catch (Exception e) {
				logger.warn("Exception trying to get property " + accessName + " from bean.", e);
			}
		}
		
		// else 
		return null;

	}

	@Override
	protected String getVisitIDAccessName() {
		return "visitID";
	}
}

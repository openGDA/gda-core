/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.spring;

import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.GdaMetadata;
import gda.data.metadata.Metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * A Spring {@link BeanFactoryPostProcessor} that registers singleton objects.
 */
public class SingletonRegistrationPostProcessor implements BeanFactoryPostProcessor {

	private Logger logger = LoggerFactory.getLogger(SingletonRegistrationPostProcessor.class);
	
	private BeanFactory beanFactory;
	
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
		registerGdaMetadata();
	}
	
	private void registerGdaMetadata() {
		if (beanFactory.containsBean(GDAMetadataProvider.GDAMETADATANAME)) {
			Metadata metadata = beanFactory.getBean(GDAMetadataProvider.GDAMETADATANAME, Metadata.class);
			logger.info("Setting GdaMetadata singleton (in GDAMetadataProvider) to Spring-instantiated instance " + metadata);
			GDAMetadataProvider.setInstance(metadata);
		}
	}

}

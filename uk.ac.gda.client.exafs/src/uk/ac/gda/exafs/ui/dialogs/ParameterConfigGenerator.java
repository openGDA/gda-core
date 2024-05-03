/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.dialogs;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ParameterConfigGenerator {

	private static final Logger logger = LoggerFactory.getLogger(ParameterConfigGenerator.class);

	public abstract List<ParameterConfig> createParameterConfigs(List<ParameterValuesForBean> o);

	/**
	 *
	 *  Find the first ParameterValuesForBean in the list that uses a bean object with given type
	 *
	 * @param paramValuesForBeans
	 * @param clazz
	 *
	 */
	protected Optional<ParameterValuesForBean> getParameterOfType(List<ParameterValuesForBean> paramValuesForBeans, Class<?> clazz) {
		return paramValuesForBeans
				.stream()
				.filter(paramsForBean -> paramsForBean.beanIsAssignableFrom(clazz))
				.findFirst();
	}

	/**
	 *  Find the {@link ParameterValuesForBean} that correspond to parameters for a
	 *  bean object of particular type.
	 *
	 * @param paramValuesForBeans
	 * @param clazz
	 * @return bean object
	 */
	protected <T> T getBeanOfType(List<ParameterValuesForBean> paramValuesForBeans, Class<T> clazz) {
		// Find the ParameterValuesForBean to be applied sample parameters bean
		Optional<ParameterValuesForBean> result = getParameterOfType(paramValuesForBeans, clazz);
		if (result.isEmpty()) {
			logger.warn("No Parameter values object for bean of class {} found", clazz.getSimpleName());
			return null;
		}
		try {
			return clazz.cast(result.get().getBeanObject());
		} catch (Exception e) {
			logger.error("Error when trying to get bean from Parameter object", e);
			return null;
		}
	}
}

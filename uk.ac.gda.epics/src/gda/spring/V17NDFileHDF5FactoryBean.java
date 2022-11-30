/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import gda.device.detector.areadetector.v17.NDFileHDF5;
import gda.device.detector.areadetector.v17.impl.NDFileHDF5Impl;
import gda.device.detector.areadetector.v17.impl.NDFileImpl;
/**
 * FactoryBean to make the creation of an bean that implements NDFileHDF5 easier
 */
public class V17NDFileHDF5FactoryBean extends V17FactoryBeanBase<NDFileHDF5>{

	private NDFileImpl ndFileImpl;
	private String initialCompression = null;
	private Integer initialZCompressLevel = null;

	public NDFileImpl getNdFileImpl() {
		return ndFileImpl;
	}

	public String getInitialCompression() {
		return initialCompression;
	}

	public int getInitialZCompressLevel() {
		return initialZCompressLevel;
	}

	public void setNdFileImpl(NDFileImpl ndFileImpl) {
		this.ndFileImpl = ndFileImpl;
	}

	public void setInitialCompression(String initialCompression) {
		this.initialCompression = initialCompression;
	}

	public void setZCompressLevel(int zCompressLevel) {
		this.initialZCompressLevel = zCompressLevel;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		NDFileHDF5Impl plugin = new NDFileHDF5Impl();
		plugin.setFile(ndFileImpl);
		plugin.setBasePVName(ndFileImpl.getBasePVName());
		plugin.afterPropertiesSet();
		if (initialCompression != null) {
			plugin.setInitialCompression(initialCompression);
		}
		if (initialZCompressLevel != null) {
			plugin.setInitialZCompressLevel(initialZCompressLevel);
		}
		bean  = plugin;
	}

	@Override
	public Class<?> getObjectType() {
		return bean != null ? bean.getClass() : null;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public NDFileHDF5 getObject() throws Exception {
		return bean;
	}

}

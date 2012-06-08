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

package gda.spring.namespaces.corba;

import gda.factory.Findable;
import gda.factory.corba.util.AdapterFactory;
import gda.factory.corba.util.NetService;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;

public class RemoteObjectFactoryBean implements FactoryBean<Findable>, BeanNameAware {

	private String beanName;
	
	@Override
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	private NetService netService;
	
	public void setNetService(NetService netService) {
		this.netService = netService;
	}
	
	private String remoteName;
	
	public void setRemoteName(String remoteName) {
		this.remoteName = remoteName;
	}
	
	@Override
	public Class<?> getObjectType() {
		return Findable.class;
	}

	protected Findable adapter;
	
	@Override
	public Findable getObject() throws Exception {
		synchronized (this) {
			if (adapter == null) {
				adapter = AdapterFactory.createAdapter(netService, remoteName, beanName);
			}
			return adapter;
		}
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}

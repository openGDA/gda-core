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

package uk.ac.gda.remoting.client;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

/**
 * Advice that can be applied to client-side proxies, so that the proxy maintains its own list of observers and doesn't
 * try to make remote calls for {@link IObservable} methods.
 */
@Aspect
public class ClientSideIObservableAdvice {
	
	private ObservableComponent observableComponent = new ObservableComponent();
	
	public ObservableComponent getObservableComponent() {
		return observableComponent;
	}
	
	@Pointcut("execution(* gda.observable.IObservable.addIObserver(..))")
	@SuppressWarnings("unused")
	private void addIObserver() {}
	
	@Around("addIObserver()")
	public Object addIObserver(ProceedingJoinPoint pjp) {
		final IObserver observer = (IObserver) pjp.getArgs()[0];
		observableComponent.addIObserver(observer);
		return null;
	}
	
	@Pointcut("execution(* gda.observable.IObservable.deleteIObserver(..))")
	@SuppressWarnings("unused")
	private void deleteIObserver() {}
	
	@Around("deleteIObserver()")
	public Object deleteIObserver(ProceedingJoinPoint pjp) {
		final IObserver observer = (IObserver) pjp.getArgs()[0];
		observableComponent.deleteIObserver(observer);
		return null;
	}
	
	@Pointcut("execution(* gda.observable.IObservable.deleteIObservers())")
	@SuppressWarnings("unused")
	private void deleteIObservers() {}
	
	@Around("deleteIObservers()")
	public Object deleteIObservers(@SuppressWarnings("unused") ProceedingJoinPoint pjp) {
		observableComponent.deleteIObservers();
		return null;
	}
	
}

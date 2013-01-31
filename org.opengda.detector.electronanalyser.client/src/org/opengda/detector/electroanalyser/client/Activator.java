package org.opengda.detector.electroanalyser.client;

import java.io.File;

import gda.util.SpringObjectServer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		SpringObjectServer s = new SpringObjectServer(
				new File(
						"D:/gda/gda-i09/workspace/org.opengda.detector.electroanalyser.client/client.xml"),
				true);
		s.configure();
//		FileSystemXmlApplicationContext applicationContext = new FileSystemXmlApplicationContext(
//				"file:D:\\gda\\gda-i09\\workspace\\org.opengda.detector.electroanalyser.client\\client.xml");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}

package org.opengda.detector.electronanalyser;

import org.eclipse.emf.edit.domain.EditingDomain;
import org.opengda.detector.electronanalyser.utils.SequenceEditingDomain;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private static Activator plugin;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		plugin=this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		plugin=null;
		Activator.context = null;
	}
	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public EditingDomain getSequenceEditingDomain() throws Exception {
		try {
			// return TransactionalEditingDomain.Registry.INSTANCE
			// .getEditingDomain(EDITING_DOMAIN_ID);
			return SequenceEditingDomain.INSTANCE.getEditingDomain();
		} catch (Exception ex) {
			throw new Exception("Unable to get editing domain:"
					+ ex.getMessage());
		}
	}


}

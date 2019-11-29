package uk.ac.diamond.daq.persistence.manager.serializer;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceActivator extends Plugin {
	private static final Logger log = LoggerFactory.getLogger(PersistenceActivator.class);

	private static PersistenceActivator instance;
	private BundleContext bundleContext;

	@Override
	public void start(BundleContext context) throws Exception {
		log.info("Persistence Class Loader Activator started...");
		instance = this;
		this.bundleContext = context;
		super.start(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		instance = null;
		super.stop(context);
	}

	static <T> T getService(Class<T> serviceClass) {
		BundleContext bundleContext = instance.getBundle().getBundleContext();
		return bundleContext.getService(bundleContext.getServiceReference(serviceClass));
	}

	public static PersistenceActivator getInstance() {
		return instance;
	}

	public BundleContext getBundleContext() {
		return bundleContext;
	}
}

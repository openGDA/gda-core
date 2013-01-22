package uk.ac.gda.epics.adviewer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import uk.ac.gda.common.rcp.NamedServiceProvider;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "uk.ac.gda.epics.adviewer"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		bundleContext = context;
	}


	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	
	private static BundleContext bundleContext;


	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		if(namedServiceProvider != null){
			namedServiceProvider.close();
			namedServiceProvider = null;
		}
		bundleContext = null;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	

	private static NamedServiceProvider namedServiceProvider;
	
	public static Object getNamedService(@SuppressWarnings("rawtypes") Class clzz, final String name) {
		if(namedServiceProvider == null){
			namedServiceProvider = new NamedServiceProvider(bundleContext);
		}
		return namedServiceProvider.getNamedService(clzz, "SERVICE_NAME", name);
		
	}
}

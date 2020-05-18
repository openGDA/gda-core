package uk.ac.gda.sisa;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "uk.ac.gda.sisa"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	private BundleContext context;
	
	public Activator() {

	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		this.context = context;
	}
	
	public static Image getImage(final String path) {
		ImageDescriptor imageDescriptor = getImageDescriptor(path); // loads the ImageDescriptor into the image registry
		if (plugin == null) {
			return imageDescriptor.createImage();
		}
		return getDefault().getImageRegistry().get(path);
	}
	
	public static ImageDescriptor getImageDescriptor(String path) {
		if (plugin==null) {
			// create an image outside of eclipse, used for tests
			final ImageData data = new ImageData("../"+PLUGIN_ID+"/"+path);
			return new ImageDescriptor() {
				@Override
				public ImageData getImageData() {
					return data;
				}
			};
		}

		final ImageRegistry imageRegistry = getDefault().getImageRegistry();
		ImageDescriptor descriptor = imageRegistry.getDescriptor(path);
		if (descriptor == null) {
			descriptor = imageDescriptorFromPlugin(PLUGIN_ID, path);
			imageRegistry.put(path, descriptor);
		}
		return descriptor;
	}
}

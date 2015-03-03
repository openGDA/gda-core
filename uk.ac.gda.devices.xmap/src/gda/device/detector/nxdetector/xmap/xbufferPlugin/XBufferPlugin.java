/**
 * 
 */
package gda.device.detector.nxdetector.xmap.xbufferPlugin;


import org.springframework.beans.factory.InitializingBean;
import gda.device.detector.nxdetector.plugin.NullNXPlugin;
import gda.scan.ScanInformation;

/**
 * This class ensures that the Xmap Xbuf intermediate plugin (EPICs) is set to Enable just before data collection.
 *
 */
public class XBufferPlugin extends NullNXPlugin implements InitializingBean{
	private XBufferPVs xBufferPVs;
	
	public XBufferPlugin(XBufferPVs xBufferPV){
		this.xBufferPVs = xBufferPV;
	}
	
	@Override
	public String getName() {
		return "XBuffer plugin";
	}
	
	@Override
	public void prepareForCollection(int numberImagesPerCollection,
			ScanInformation scanInfo) throws Exception {
		xBufferPVs.setEnableCallbacks(true);
	}
	
	public XBufferPVs getXBufferPVs(){
		return xBufferPVs;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (xBufferPVs == null) {
			throw new RuntimeException("XBufferPVs is not set.");
		}		
	}

}

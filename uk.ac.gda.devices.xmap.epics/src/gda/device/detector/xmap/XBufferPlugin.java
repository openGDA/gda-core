/**
 *
 */
package gda.device.detector.xmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.detector.nxdetector.plugin.NullNXPlugin;
import gda.epics.connection.EpicsController;
import gda.scan.ScanInformation;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

/**
 * @author dfq16044
 *
 */
public class XBufferPlugin extends NullNXPlugin implements InitializingBean  {

	/**
	 *
	 */

	String basePVName;
	String fullPvName;
	String fullPvName_RBV;
	public final String EnableCallbacks = "EnableCallbacks";
	public final String EnableCallbacks_RBV = "EnableCallbacks_RBV";


	private EnableXBuf EnableXbuf;
	public enum EnableXBuf {
		DISABLE, ENABLE,
	}
	protected final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	static final Logger logger = LoggerFactory.getLogger(XBufferPlugin.class);

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	/*public XBufferPlugin() throws Exception {
		//setEnabled();
		// TODO Auto-generated constructor stub
	}*/

	public void setFullPvName() {
		this.fullPvName = basePVName+EnableCallbacks;
	}

	public String getFullPvName() {
		return fullPvName;
	}

	public void setFullPvName_RBV() {
		this.fullPvName_RBV = basePVName+EnableCallbacks_RBV;
	}

	public String getFullPvName_RBV() {
		return fullPvName_RBV;
	}

	public EnableXBuf getEnable() {
		return this.EnableXbuf;
	}

	public void setEnableXBuf(EnableXBuf enable) {
		this.EnableXbuf = enable;
	}


	public Channel createChannel(String fullPvName) throws TimeoutException {
	 	Channel channel = null;

	 	try {
	 		channel = EPICS_CONTROLLER.createChannel(fullPvName);
	 	} catch (CAException cae){
			logger.warn("Problem creating channel",cae);
	 	}
	 	return channel;
	 }


	 public void setEnabled() throws Exception {
		 setFullPvName();
		 try{
			 EPICS_CONTROLLER.caput(createChannel(fullPvName), EnableXbuf.ordinal());
		 } catch (Exception ex){
			 logger.warn("Cannot setEnabled", ex);
				throw ex;
		 }
	}

	 public short getEnabled() throws Exception {
		 setFullPvName_RBV();
		 try{
			 return EPICS_CONTROLLER.cagetEnum(createChannel(fullPvName_RBV));
		 } catch (Exception ex) {
				logger.warn("Cannot getEnabled_RBV", ex);
				throw ex;
		}
	}



	/* (non-Javadoc)
	 * @see gda.device.detector.nxdetector.NXPluginBase#getName()
	 */
	@Override
	public String getName() {
		return "XBuffer plugin";
	}



	/* (non-Javadoc)
	 * @see gda.device.detector.nxdetector.NXPluginBase#willRequireCallbacks()
	 */
	/*@Override
	public boolean willRequireCallbacks() {
		// TODO Auto-generated method stub
		return false;
	}
*/
	@Override
	public void prepareForCollection(int numberImagesPerCollection,
			ScanInformation scanInfo) throws Exception {
		setEnabled();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePVName == null) {
			throw new IllegalArgumentException("'basePVName'needs to be declared");
		}

	}

}

/**
 * 
 */
package gda.device.detector.nxdetector.xmap;

import org.springframework.beans.factory.InitializingBean;

/**
 * @author dfq16044
 *
 */
public abstract class PVBase implements InitializingBean{

	private String basePVName;
	
	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}
	
	public String fullPVname(String PVsuffix){
		return basePVName + PVsuffix;
		
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePVName == null) {
			throw new IllegalArgumentException("'basePVName' needs to be declared");
		}
	}
	
}

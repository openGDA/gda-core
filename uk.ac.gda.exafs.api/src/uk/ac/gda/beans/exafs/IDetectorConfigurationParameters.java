package uk.ac.gda.beans.exafs;

import uk.ac.gda.util.beans.xml.XMLRichBean;

/**
 * Rich Beans which describe the configuration for a detector for a specific experiment.
 * <p>
 * It would be expected that the detector be re-configured for each experiment based on these parameters e.g. set regions of interest in a fluorescence detector
 *
 * @author rjw82
 *
 */
public interface IDetectorConfigurationParameters extends XMLRichBean {

}

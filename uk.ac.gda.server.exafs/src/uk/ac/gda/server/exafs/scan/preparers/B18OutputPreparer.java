package uk.ac.gda.server.exafs.scan.preparers;

import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.scan.ScanPlotSettings;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.server.exafs.scan.OutputPreparer;

public class B18OutputPreparer implements OutputPreparer {

	private AsciiDataWriterConfiguration datawriterconfig;

	public B18OutputPreparer(AsciiDataWriterConfiguration datawriterconfig) {
		this.datawriterconfig = datawriterconfig;

	}

	@Override
	public void prepare(IOutputParameters outputParameters, IScanParameters scanBean) {
//		List<MetadataParameters> metadata = outputParameters.getMetadataList();
//		meta = Metadata(self.datawriterconfig);
//		if (len(metadata) > 0) {
//			meta.add_to_metadata(metadata);
//		}
	}

	// # Determines the AsciiDataWriterConfiguration to use to format the header/footer of the ascii data files
	// # If this returns None, then let the Ascii Data Writer class find the config for itself.
	@Override
	public AsciiDataWriterConfiguration getAsciiDataWriterConfig(IScanParameters scanBean) {
		return datawriterconfig;
	}

//	// # For any specific plotting requirements based on all the options in this experiment
//	public getPlotSettings(IDetectorParameters detectorBean, IOutputParameters outputBean) {
//		return null;
//	}

	// def _resetHeader(self):
	// self.datawriterconfig.setHeader(self.original_header)
	// meta_clear_alldynamical()

	@Override
	public void _resetNexusStaticMetadataList() {
		// do nothing
	}

	@Override
	public ScanPlotSettings getPlotSettings(IDetectorParameters detectorBean, IOutputParameters outputBean) {
		return null;
	}

}
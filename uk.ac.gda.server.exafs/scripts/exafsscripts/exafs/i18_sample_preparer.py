from gdascripts.messages.handle_messages import simpleLog
from gda.data.scan.datawriter import NexusExtraMetadataDataWriter, NexusFileMetadata
from gda.data.scan.datawriter.NexusFileMetadata import EntryTypes, NXinstrumentSubTypes
from time import sleep

class I18SamplePreparer:
	def __init__(self):
		self.logging_enabled = True
	
	def setLoggingEnabled(self, enabled):
		self.logging_enabled = enabled
	
	def log(self, msg):
		if self.logging_enabled == True:
			simpleLog(msg)
		else:
			print msg
	
	def prepare(self, sampleParameters):
		Finder.getInstance().find("RCPController").openPesrpective("org.diamond.exafs.ui.PlottingPerspective")

		stage = sampleParameters.getSampleStageParameters()
		pos([rootnamespace['sc_MicroFocusSampleX'], stage.getX(), rootnamespace['sc_MicroFocusSampleY'], stage.getY(), rootnamespace['sc_sample_z'], stage.getZ()])

		att2 = sampleParameters.getAttenuatorParameter2()
		att1 = sampleParameters.getAttenuatorParameter1()
		pos([rootnamespace['D7A'], att1.getSelectedPosition(), rootnamespace['D7B'], att2.getSelectedPosition()])
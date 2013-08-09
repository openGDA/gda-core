from gdascripts.parameters import beamline_parameters

class OutputPreparer:
    def __init__(self):
        pass
    
    def prepare(self, outputParameters):
        metadataActive = outputParameters.isMetadataActive()
        if metadataActive:
            self.add_to_metadata(outputParameters.getMetadataList())

    # Determines the AsciiDataWriterConfiguration to use to format the header/footer of the ascii data files
    # If this returns None, then let the Ascii Data Writer class find the config for itself.
    def getAsciiDataWriterConfig(self,beanGroup):
        return None

    # For any specific plotting requirements based on all the options in this experiment
    def getPlotSettings(self,beanGroup):
        return None
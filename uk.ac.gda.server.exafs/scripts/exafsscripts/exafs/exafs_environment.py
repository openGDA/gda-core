from gda.data import PathConstructor

class ExafsEnvironment:
    testScriptFolder = None
    
    def getXMLFolder(self):
        if ExafsEnvironment.testScriptFolder != None:
            return ExafsEnvironment.testScriptFolder
        dataDirectory = PathConstructor.createFromDefaultProperty()
        return dataDirectory + "/xml/"

    testScannable = None
    
    def getScannable(self):
        if ExafsEnvironment.testScannable != None:
            return ExafsEnvironment.testScannable
        # The scannable name is defined in the XML when not in testing mode.
        # Therefore the scannable argument is omitted from the bean
        return None
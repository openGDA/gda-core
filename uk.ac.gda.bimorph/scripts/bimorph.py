import Jama.Matrix
import Jama.SingularValueDecomposition
from gdascripts.pd.dummy_pds import DummyPD
import gda.jython.commands.InputCommands as inputCommand
from gdascripts.analysis.io.ScanFileLoader import ScanFileLoader
from gda.data import PathConstructor
from gda.data import NumTracker
from gda.data.metadata import GDAMetadataProvider

class VoltageControllerSim:
    def __init__(self, verbose, numberOfElectrodes):
        self.verbose = verbose
        self.numberOfElectrodes = numberOfElectrodes
        self.electrodeVoltageSim = [0]*numberOfElectrodes

    def getVoltage(self,electrodeNo):
        if (electrodeNo < 0) or (electrodeNo > (self.numberOfElectrodes-1)):
            voltage = None
        else:
            voltage = self.electrodeVoltageSim[electrodeNo]
        return voltage
    
    def setVoltage(self, electrodeNo, voltage):
        self.electrodeVoltageSim[electrodeNo]=voltage

    def getNumberOfElectrodes(self):
        return self.numberOfElectrodes

class CentroidReaderSim:
    def __init__(self, verbose, centroids ,motor, voltageController, slitPos, initialVoltages, voltageIncrement ):
        self.verbose = verbose
        self.centroids = centroids
        self.motor = motor
        self.voltageController = voltageController
        self.slitPos = slitPos
        self.initialVoltages = initialVoltages
        self.voltageIncrement = voltageIncrement

    def getValue(self):
        voltageStep = 0
        for i in range(0, self.voltageController.getNumberOfElectrodes()):
            if self.voltageController.getVoltage(i) == self.initialVoltages[i]+self.voltageIncrement:
                voltageStep = i+1

        motorPos = self.motor()
        slitPosIndex = 0
        for i in range(len(self.slitPos)):
            if self.slitPos[i] == motorPos:
                slitPosIndex =i
                break
        val = self.centroids[voltageStep][slitPosIndex] 
        return val 
    
class RunOptimisation:
    def __init__(self, bimorphScannable=None,
                  mirror_type = None,
                  numberOfElectrodes = None,
                  voltageIncrement = None,
                  files = None,
                  error_file =  None,
                  desiredFocSize = None,
                  user_offset =  None,
                  bm_voltages=None, 
                  beamOffset=None,
                  autoDist=None,
                  scalingFactor=None,
                  scanDir=None, 
                  minSlitPos=None, 
                  maxSlitPos=None,
                  slitPosScannableName=None):
        
        self.bimorphScannable = bimorphScannable
        
        self.mirror_type = mirror_type
        self.numberOfElectrodes = numberOfElectrodes
        self.voltageIncrement = voltageIncrement
        self.files = files
        self.error_file =  error_file
        self.desiredFocSize = desiredFocSize
        self.user_offset =  user_offset
        self.bm_voltages=bm_voltages
        self.auto_offset = True
        self.autoDist=autoDist
        self.beamOffset = beamOffset
        self.voltages = []
        self.scalingFactor=scalingFactor
        self.scanDir = scanDir
        self.minSlitPos = minSlitPos
        self.maxSlitPos = maxSlitPos
        self.slitPosScannableName = slitPosScannableName

    def __call__(self):
        self.requestInputs()
        self.calculateVoltages()

    def requestInputs(self):
        if self.mirror_type == None:
            self.mirror_type = inputCommand.requestInput("Please enter the type of mirror, either vfm or hfm")
        if self.numberOfElectrodes == None:
            self.numberOfElectrodes = int(inputCommand.requestInput("Please enter the number of electrodes"))#24
        if self.voltageIncrement == None:
            self.voltageIncrement = int(inputCommand.requestInput("Please enter the voltage increment"))#-50
        if self.files == None:
            self.files = eval(inputCommand.requestInput("Please enter the .dat input file numbers seperated by commas e.g. 0,1,2,3,4,5,6,7"))
        if self.error_file == None:
            self.error_file =  int(inputCommand.requestInput("Please enter the .dat error file number e.g. 0"))
        if self.desiredFocSize == None:
            self.desiredFocSize = float(inputCommand.requestInput("Please enter the beam size"))#0
        
        user_offset_desired = self.user_offset
        if self.user_offset == None:
            user_offset_desired =  inputCommand.requestInput("Would you like to input a fixed offset? (yes, y)")


        if user_offset_desired in ("yes", "y", "1"):
            self.auto_offset = False
            if self.beamOffset == None:
                self.beamOffset = float(inputCommand.requestInput("Please enter the beam offset"))#0
        else:
            self.auto_offset = True
            self.beamOffset = 0
            
        if self.bimorphScannable == None :
            if self.bm_voltages == None:
                self.bm_voltages = eval(inputCommand.requestInput("Please enter the current voltages separated by commas e.g. 0,1,2,3,4,5,6,7"))

    def calculateVoltages(self):
        
        maxSafeVoltage = 1000
        minSafeVoltage = -1000
        maxSafeVoltDiff = 450
        
        errorData = self.getErrorData()
        
        headings = errorData.getNames()
        columnNames = self.getColumnNames(headings)
        
        slitPos = self.getSlitPos(columnNames, headings, errorData)
        limits = self.getLimitIndices(slitPos)
        if limits is not None:
            slitPos = slitPos[limits[0]:limits[1]]

        centroidMatrix, error_centroids = self.getCentroidMatrix(errorData, columnNames, limits)
        
        initialVoltages = [0] * (self.numberOfElectrodes + int(self.auto_offset))
        
        dummyPD = DummyPD("slitpos")
        vc = VoltageControllerSim(False, self.numberOfElectrodes)
        bm = BimorphMirror(self.numberOfElectrodes, maxSafeVoltage, minSafeVoltage, maxSafeVoltDiff)
        centroidReaderSim = CentroidReaderSim(False, centroidMatrix, dummyPD, vc, slitPos, initialVoltages, self.voltageIncrement)
        optimizer = BimorphOptimiser(False, bm, vc, centroidReaderSim , slitPos, dummyPD, self.beamOffset, self.auto_offset, self.autoDist, self.scalingFactor)
        
        noOfCentroids = len(centroidMatrix[0])
        weights = [1]*noOfCentroids
        self.voltages = optimizer.run(weights, self.desiredFocSize, initialVoltages, self.voltageIncrement, error_centroids, self.autoDist, self.scalingFactor)
        self.voltages = self.voltages.getArray();
        self.printVoltages(self.voltages)

    def getErrorData(self):
        fileNo = self.error_file
        if fileNo == 0:
            nt = NumTracker(GDAMetadataProvider.getInstance().getMetadataValue("instrument", "gda.instrument", None))
            fileNo = nt.getCurrentFileNumber()
            print "Using %d (most recent file) as error file as 0 was specified" %fileNo
        try:
            errorData = ScanFileLoader(fileNo, self.scanDir).getSFH()
        except Exception, e:
            errorData = ScanFileLoader(fileNo, PathConstructor().getDefaultDataDir()).getSFH()
        return errorData

    def getColumnNames(self, headings):
        suffix = self.getColumnSuffix()
        if "peak2d." + suffix in headings:
            return self.nexusColumnNames("peak2d.", suffix)
        elif "pa." + suffix in headings:
            return self.nexusColumnNames("pa.", suffix)
        else:
            return self.nonNexusColumnNames(suffix)

    def nexusColumnNames(self, prefix, suffix):
        names = {}
        names["data_centroid"] = prefix + suffix
        names["err_centroid"] = prefix + suffix
        names["err_slit"] = self.slitPosScannableName + "." + self.slitPosScannableName
        return names
    
    def nonNexusColumnNames(self, suffix):
        names = {}
        names["data_centroid"] = suffix
        names["err_centroid"] = suffix
        names["err_slit"] = "/entry1/instrument/pa/idx"
        return names

    def getColumnSuffix(self):
        name = "peak2d_peak"
        if self.mirror_type in ["vfm", "VFM", "y"]:
            name += "y"
        elif self.mirror_type in ["hfm", "HFM", "x"]:
            name += "x"
        else:
            raise "Mirror not recognised"
        return name

    def getSlitPos(self, names, headings, errorData):
        if names["err_slit"] in headings:
            slitPos = errorData.getLazyDataset(names["err_slit"]).getSlice(None).getBuffer()
        else:
            if headings[0]=="idx":
                slitPos = errorData.getLazyDataset(1).getSlice(None).getBuffer()
            else:
                slitPos = errorData.getLazyDataset(0).getSlice(None).getBuffer()
        return slitPos

    def hasSlitPosLimit(self):
        return self.minSlitPos!=None or self.maxSlitPos!=None

    def getLimitIndices(self, slitPos):
        if self.hasSlitPosLimit():
            startIndex = 0
            endIndex = 0
            startFound=False
            index=0
            for pos in slitPos:
                if pos<=self.maxSlitPos:
                    if pos >=self.minSlitPos:
                        if startFound==False:
                            startIndex=index
                            startFound=True
                    endIndex+=1
                index+=1
            print "startIndex: "+`startIndex` + " endIndex: " + `endIndex`
            return [startIndex, endIndex]
        return None

    def getCentroidMatrix(self, errorData, columnNames, limit):
        try:
            files = [int(f) for f in self.files]
        except ValueError:
            files = self.files
        matrix = []
        for file in files:
            data = ScanFileLoader(file, self.scanDir).getSFH()
            dataHeadings = data.getNames()
            dataColumnNames = self.getColumnNames(dataHeadings)
            print "data:" + str(dataColumnNames)
            print "error:" + str(columnNames)
            # *********** RobW 17 May 2014 7pm : its not finding the column in here. I've added some debig stuff. If had time I would have dived into ScanFileHolder, and/or debugged it on the server.
            print "file: " + str(file)
            print "self.scanDir: " + str(self.scanDir)
            print "scan file holder: " + str(data)
            print "scan file holder (DataHolder) contains names:" + str(data.getNames())
            column_name = dataColumnNames["data_centroid"]
            print "using column for the data_centroid the column named: " + column_name
            ds = data.getLazyDataset(column_name)
            if ds == None:
                print "No dataset found for column '" + column_name + "' trying generic 'peaky'..."
                ds = data.getLazyDataset('peaky')
            print "dataset: " + str(ds)
            centroids = ds.getSlice(None).getBuffer()
            
            print "errorData: " + str(errorData)
            print "errorData (DataHolder) contains names:" + str(data.getNames())
            column_name = columnNames["err_centroid"]
            print "using column for the err_centroid the column named: " + column_name
            ds = errorData.getLazyDataset(column_name)
            if ds == None:
                print "No dataset found for column '" + column_name + "' trying generic 'peaky'..."
                ds = errorData.getLazyDataset('peaky')
            print "dataset: " + str(ds)
            error_centroids = ds.getSlice(None).getBuffer()
            
            if self.hasSlitPosLimit():
                centroids = centroids[limit[0]:limit[1]]
                error_centroids = error_centroids[limit[0]:limit[1]]
                
            matrix.append(centroids)
        return matrix, error_centroids

    def printVoltages(self, voltages):
        print "\n"
        print "__________________________________________________________________________________________________________________"
        print "                                                   voltages"
        print "__________________________________________________________________________________________________________________"
        print "        current voltages         |           calculated voltages           |        summed voltages to be set"
        print "_________________________________|_________________________________________|______________________________________"

        bm_voltage = 0;

        useableOutput = [] #Can be copied from the terminal to where the voltages need to be input
        offset_shown = not self.auto_offset
        for voltage in voltages:
            current = self.bm_voltages[bm_voltage]
            correction = voltage[0]
            voltage_sum = current + correction
            if not offset_shown:
                print "  {:20}  |  {:< 11.1f} - offset  |".format("", correction)
                offset_shown=True
            else:
                print "  {:< 20.1f}  |  {:< 20.1f}  |  {:< 20.1f}".format(current, correction, voltage_sum)
                bm_voltage += 1
                useableOutput.append(voltage_sum)

        print "\nTo be set:"
        print ",".join(["%.1f" % v_sum for v_sum in useableOutput])

class BimorphMirror:
    def __init__(self, numberOfElectrodes, maxSafeVoltage,
        minSafeVoltage, maxSafeVoltDiff):
        self.numberOfElectrodes = numberOfElectrodes
        self.maxSafeVoltage = maxSafeVoltage #V
        self.minSafeVoltage = minSafeVoltage #V
        self.maxSafeVoltDiff = maxSafeVoltDiff #V 
        
    def genSlitPos(self, initSlitPos, finlSlitPos, numScanPtsPerElectrode): 
        """
        return list of slit positions to cover the range
        """
        if initSlitPos != None:
            if finlSlitPos != None:
                numSlitScanPts = int(self.numberOfElectrodes * numScanPtsPerElectrode)
                slitMotorPositions = range(0, int(numSlitScanPts))
                for i in slitMotorPositions:
                    slitMotorPositions[i] = ((finlSlitPos - initSlitPos) * i / (numSlitScanPts - 1)) + initSlitPos
                return slitMotorPositions

class BimorphOptimiser:
    def __init__(self, verbose, bimorphMirror, voltageController, centroidReader, slitPos, slitMotor, beamOffset, auto_offset, autoDist=None, scalingFactor=None):
        self.verbose = verbose
        self.bimorphMirror = bimorphMirror
        self.voltageController = voltageController
        self.centroidReader = centroidReader
        self.slitPos = slitPos
        self.slitMotor = slitMotor
        self.beamOffset = beamOffset
        self.auto_offset = auto_offset
        self.autoDist = autoDist
        self.scalingFactor = scalingFactor 
        
    def getVoltage(self, electrodeNo):
        return self.voltageController.getVoltage(electrodeNo)
    
    def setVoltage(self, electrodeNo, voltage):
        if (voltage > self.bimorphMirror.maxSafeVoltage) or (voltage < self.bimorphMirror.minSafeVoltage):
            raise "Set voltage is outside safe range. " + `voltage` + "min = " + `self.bimorphMirror.minSafeVoltage` + "max = " + `self.bimorphMirror.maxSafeVoltage`
        else:
            leftVoltage = self.getVoltage(electrodeNo - 1)
            rightVoltage = self.getVoltage(electrodeNo + 1)
            if (electrodeNo < (self.bimorphMirror.numberOfElectrodes - 1)):
                rightDiff = abs(rightVoltage - voltage)
            elif (electrodeNo == (self.bimorphMirror.numberOfElectrodes - 1)):
                rightDiff = 0.
            if (electrodeNo > 0):
                leftDiff = abs(leftVoltage - voltage)
            elif (electrodeNo == 0):
                leftDiff = 0.
            if (leftDiff > self.bimorphMirror.maxSafeVoltDiff) or (rightDiff > self.bimorphMirror.maxSafeVoltDiff):
                raise "Voltage difference will go outside safe range."
            else:
                self.voltageController.setVoltage(electrodeNo, voltage)

    def incrementVoltage(self, electrodeNo, voltageIncrement):
        presentVoltage = self.getVoltage(electrodeNo)
        desiredVoltage = presentVoltage + voltageIncrement
        self.setVoltage(electrodeNo, desiredVoltage)
    
    def getDesiredCentroids(self, presentCentroids, weights, desiredFocSize):
        wtsum = 0
        wtmnsum = 0
        noCentroids = len(presentCentroids)
      
        for i in range(noCentroids):
            wtsum = wtsum + weights[i]
            wtmnsum = wtmnsum + (weights[i] * presentCentroids[i])
        weightedMeanPos = wtmnsum / wtsum

        desiredCentroids = []
        for i in range(0, noCentroids):
            desiredCentroids.append(weightedMeanPos + self.beamOffset + desiredFocSize * (-0.5 + (i - 1) * 1. / (noCentroids - 1)))
        
        return desiredCentroids

    def getCentroids(self):
        centroids = []
        for pos in range(len(self.slitPos)):
            self.slitMotor(self.slitPos[pos])
            centroids.append(self.centroidReader.getValue())         
        return centroids
    
    def forceVoltages(self, voltage):
        #we will need to lower the voltages gradually
        for electrodeNo in range (self.bimorphMirror.numberOfElectrodes):
            self.setVoltage(electrodeNo, voltage)

    def buildInteractionMatrix(self, initialVoltages, voltageIncrement):
        """
        Build up matrix of differences in centroid values for different slitPositions betwen different voltages
        imatrix[i][j]  is the difference in centroid values between slitPos[j] for voltage i and slitPos[j] for voltage number i-1
        """
        if self.auto_offset:
            iMatrix=[[1]*(self.bimorphMirror.numberOfElectrodes+1) for i in range(len(self.slitPos))]

        else:
            iMatrix=[[1]*(self.bimorphMirror.numberOfElectrodes) for i in range(len(self.slitPos))]

        prevCentroids = self.getCentroids()
        
        for electrodeNo in range(self.bimorphMirror.numberOfElectrodes):
            self.incrementVoltage(electrodeNo, voltageIncrement)
            newCentroids = self.getCentroids()
            
            for centroidIndex in range(len(newCentroids)):
                if self.auto_offset:
                    iMatrix[centroidIndex][electrodeNo+1] = ((float(newCentroids[centroidIndex]) - float(prevCentroids[centroidIndex])) / voltageIncrement)
                else:
                    iMatrix[centroidIndex][electrodeNo] = ((float(newCentroids[centroidIndex]) - float(prevCentroids[centroidIndex])) / voltageIncrement)
            prevCentroids = newCentroids
        return Jama.Matrix(iMatrix)
    
    def run(self, weights, desiredFocSize, initialVoltages, voltageIncrement, centroids, autoDist=None, scalingFactor=None):
        """
        returns required voltages to give desired focus
        weights = weighting given to the centroids for different slit positions. size must equal number of slit positions
        """

        noOfCentroids = len(centroids)
        
        desiredCentroids = self.getDesiredCentroids(centroids, weights, desiredFocSize)

        diagonalMatrix=[[0]*(noOfCentroids) for i in range(noOfCentroids)]

        for i in range(noOfCentroids):
            diagonalMatrix[i][i] = weights[i] 

        dp = 15#decimal places

        weightsMatrix = Jama.Matrix(diagonalMatrix)

        interactionMatrix = self.buildInteractionMatrix(initialVoltages, voltageIncrement)

        if autoDist!=None and scalingFactor!=None:
            if autoDist==True:
                interactionMatrix.timesEquals(scalingFactor);

        weightedInteractionMatrix = weightsMatrix.times(interactionMatrix) 
                
        svd = Jama.SingularValueDecomposition(weightedInteractionMatrix)
        lsv = svd.getU()
        rsv = svd.getV()
        singularValues = svd.getSingularValues()

        r_orthogonal = True
        r_trans = rsv.transpose().getColumnPackedCopy()
        r_inv = rsv.inverse().getColumnPackedCopy()
        for i in range(len(r_trans) - 1):
            if round(r_trans[i], 4) != round(r_inv[i], 4):
                r_orthogonal = False

        l_orthogonal = True
        l_trans = lsv.transpose().getColumnPackedCopy()
        l_inv = lsv.inverse().getColumnPackedCopy()
        for i in range(len(l_trans) - 1):
            if round(l_trans[i], 4) != round(l_inv[i], 4):
                l_orthogonal = False

        if self.auto_offset:
            diagonalMatrixOfSingularValues=[[0]*(self.bimorphMirror.numberOfElectrodes+1) for i in range(self.bimorphMirror.numberOfElectrodes+1)]
            
            for i in range(self.bimorphMirror.numberOfElectrodes+1):
                diagonalMatrixOfSingularValues[i][i] = singularValues[i]
    
        else:
            diagonalMatrixOfSingularValues=[[0]*(self.bimorphMirror.numberOfElectrodes) for i in range(self.bimorphMirror.numberOfElectrodes)]
            for i in range(self.bimorphMirror.numberOfElectrodes):
                diagonalMatrixOfSingularValues[i][i] = singularValues[i] 
            
        original_interaction_matrix = lsv.times(Jama.Matrix(diagonalMatrixOfSingularValues)).times(rsv.transpose())
        
        interactionIsSame = True
        packed_interaction_matrix = interactionMatrix.getColumnPackedCopy()
        packed_original_interaction_matrix = original_interaction_matrix.getColumnPackedCopy()
        for i in range(len(packed_interaction_matrix) - 1):
            if round(packed_interaction_matrix[i], 4) != round(packed_original_interaction_matrix[i], 4):
                interactionIsSame = False

        tr_diag_sv = Jama.Matrix(diagonalMatrixOfSingularValues).transpose()
        tr_lsv = lsv.transpose()
        param_a = tr_diag_sv.times(Jama.Matrix(diagonalMatrixOfSingularValues))
        
        pseudo_inverse_manual = rsv.times(param_a.inverse()).times(tr_diag_sv).times(tr_lsv)
        
        correction = []
        for i in range(len(centroids)):
            correction.append(desiredCentroids[i] - centroids[i])

        jCorr = Jama.Matrix(correction, 1)
        weightedCorrection = jCorr.times(weightsMatrix).transpose()
        
        return pseudo_inverse_manual.times(weightedCorrection)

def runOptimisation(bimorphScannable=None, mirror_type = None,numberOfElectrodes = None,voltageIncrement = None,files = None,error_file =  None,desiredFocSize = None,user_offset =  None,bm_voltages=None, beamOffset=None, autoDist=None, scalingFactor=None, scanDir=None, minSlitPos=None, maxSlitPos=None, \
                    slitPosScannableName=None):
    ''' 
    >>> runOptimisation(bimorphScannable)
    
    where bimorphScannable() returns an array of voltages of length = inputNames+extraNames and 
    positions[i] = demand voltage for plate i
    positions[len(inputNames)+i]= actual voltage for plate i
    
    '''
    
    ro=RunOptimisation(bimorphScannable, mirror_type,numberOfElectrodes,voltageIncrement,files,error_file,desiredFocSize,user_offset,bm_voltages, beamOffset, autoDist, scalingFactor, scanDir, minSlitPos, maxSlitPos,slitPosScannableName)
    ro()
    return ro

def findSlitRange(data):
    """
    data is 2d array [i][j] where i is 0-1 where 0 = slitPositions and 1 = values at that position
    returns expected edges of the 'step' function and a code to indicate if the step is encompassed in the original scan
    #message codes
    #0 - Scan was successful.
    #1 - Scan has failed.
    #2 - Enlarge scan at beginning and end of scan.
    #3 - Enlarge scan range at beginning of scan.
    #4 - Enlarge scan range at end of scan.
    """

    detPlot = data[1]
    slitPos = data[0]
    firstIndData = detPlot[0]
    lastIndData = detPlot[detPlot.getDimensions()[0] - 1]
    precision = round(slitPos[1] - data[0][0], 2)

    threshold = data[1].max() / 2.0
    
    firstLastInd = data.getInterpolatedX(data[0], detPlot, threshold)
    
    initSlitPos = None
    finlSlitPos = None

    messageCode = 0

    if len(firstLastInd) == 2:
        initSlitPos = firstLastInd[0] - (precision / 2.0)
        finlSlitPos = firstLastInd[1] - (precision / 2.0)
        initSlitPos = round(initSlitPos, 2)
        finlSlitPos = round(finlSlitPos, 2)
        messageCode = 0
        
    elif len(firstLastInd) == 0:
        if data[1].max() == 0:
            messageCode = 1
        else:
            messageCode = 2
            
    elif len(firstLastInd) < 2:
        if firstIndData > threshold:
            messageCode = 3
        if lastIndData > threshold:
            messageCode = 4

    return initSlitPos, finlSlitPos, messageCode

def footprint(slitWidth, incidenceAngleRad): 
    footprnt = (slitWidth) / (incidenceAngleRad)
    return footprnt


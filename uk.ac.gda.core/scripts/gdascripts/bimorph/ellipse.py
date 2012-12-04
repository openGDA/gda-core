import unittest
from math import sin, cos, tan, pow, sqrt, ceil
from gda.jython.commands.ScannableCommands import scan
from gdascripts.scannable.preloadedArray import PreloadedArray

class TestCorrectResults(unittest.TestCase):
    
    def setUp(self):
        self.calc = EllipseCalculator(pixel_size=4.65, p_1=33.115, q_1=6.885, theta_1=.0027, p_2=25., q_2=10., theta_2=.0025, i_sign= -1., detector_distance=6.885, slit_start=-3.89,slit_end=-5.21, slit_step=-.04)
    
    def test_print_results(self):
        self.calc.calcSlopes()
    
    
class EllipseCalculator():
    def __init__(self, pixel_size=None, p_1=None, q_1=None, theta_1=None, p_2=None, q_2=None, theta_2=None, i_sign=None, detector_distance=None, slit_start=None, slit_end=None, slit_step=None, column=None, inv=1, method=0):
        '''
        pixel_size in microns
        detector_distance in metres
        elipse parameters in m or rad
        detector_distance in m defaults to q_2 if not given
        
        '''
        self.pixel_size = pixel_size # microns
        self.p_1 = p_1
        self.q_1 = q_1
        self.theta_1 = theta_1/1000.0
        self.p_2 = p_2
        self.q_2 = q_2
        self.theta_2 = theta_2/1000.0
        self.i_sign = i_sign 
        self.detector_distance = detector_distance or q_2
        self.slit_start = slit_start
        self.slit_end = slit_end
        self.slit_step = slit_step
        self.column = column
        self.s_pos = []
        self.error_list = []
        self.sold = []
        self.snew = []

        self.told = []
        self.tnew = []
        self.inv = inv
        self.method = method


    def calcCamPosNoFile(self):
        self.s_pos = [ i for i in self.frange(self.slit_start, self.slit_end, self.slit_step) ]
        
        s_med = self.median(self.s_pos)
        pos = 0
        
        self.error_list = []
        
        for i in self.s_pos:
            x_val = self.i_sign * 0.001 * ((self.s_pos[pos] - s_med) / sin(self.theta_1))

            y_old_val = self.calcHeight(self.p_1, self.q_1, self.theta_1, x_val)
            y_new_val = self.calcHeight(self.p_2, self.q_2, self.theta_2, x_val)

            A = x_val * sin(self.theta_1) + y_old_val * cos(self.theta_1)
            B = self.detector_distance - self.q_1
            C = self.q_1 + x_val * cos(self.theta_1) - y_old_val * sin(self.theta_1)
            
            t_old_val = (A*B)/C

            D = tan((2*self.theta_2)-(2*self.theta_1))
            E = (x_val * sin(self.theta_2)) + (y_new_val*cos(self.theta_2))
            F = self.detector_distance - self.q_2
            G = self.q_2 + (x_val*cos(self.theta_2))-(y_new_val*sin(self.theta_2))
            
            t_new_val = -self.detector_distance * D +  ((E*F)/G)

            self.told.append(t_old_val)
            self.tnew.append(t_new_val)

            error = self.inv * (t_old_val - t_new_val) * (1000000/self.pixel_size)

            self.error_list.append(error)
            pos += 1
        
        return error
    
    def calcCamPos(self):
        self.s_pos = [ i for i in self.frange(self.slit_start, self.slit_end, self.slit_step) ]
        
        s_med = self.median(self.s_pos)
        pos = 0
        
        self.error_list = []
        
        for i in self.s_pos:
            x_val = self.i_sign * 0.001 * ((self.s_pos[pos] - s_med) / sin(self.theta_1))

            y_old_val = self.calcHeight(self.p_1, self.q_1, self.theta_1, x_val)
            y_new_val = self.calcHeight(self.p_2, self.q_2, self.theta_2, x_val)

            A = x_val * sin(self.theta_1) + y_old_val * cos(self.theta_1)
            B = self.detector_distance - self.q_1
            C = self.q_1 + x_val * cos(self.theta_1) - y_old_val * sin(self.theta_1)
            
            t_old_val = (A*B)/C

            D = tan((2*self.theta_2)-(2*self.theta_1))
            E = (x_val * sin(self.theta_2)) + (y_new_val*cos(self.theta_2))
            F = self.detector_distance - self.q_2
            G = self.q_2 + (x_val*cos(self.theta_2))-(y_new_val*sin(self.theta_2))
            
            t_new_val = -self.detector_distance * D +  ((E*F)/G)

            self.told.append(t_old_val)
            self.tnew.append(t_new_val)

            error = self.inv * (t_old_val - t_new_val) * (1000000/self.pixel_size)

            self.error_list.append(error)
            pos += 1
        
        self.create_error_file(self.s_pos, self.error_list, self.column)
        
        return error

    def calcHeight(self, p, q, theta, x):
        A = (p + q) * sin(theta)
        B = pow((p + q), 2) - pow((p - q), 2) * pow(sin(theta), 2)
        C = (p - q) * cos(theta) * x
        D = 2 * sqrt(p * q)
        E = 2 * p * q
        F = sqrt((p * q) + ((p - q) * cos(theta)) * x - pow(x, 2))
        return (A / B) * (E + C - D * F)

    
    def calcSlopesNoFile(self):

        #
        if self.method==1:
            return self.calcCamPosNoFile();
        #
           
        self.s_pos = [ i for i in self.frange(self.slit_start, self.slit_end, self.slit_step) ]
        
        s_med = self.median(self.s_pos)
        pos = 0
        
        self.error_list = []
        
        for i in self.s_pos:
            x_val = self.i_sign * 0.001 * ((self.s_pos[pos] - s_med) / sin(self.theta_1))
            
            s_old_val = self.calcSlope(self.p_1, self.q_1, self.theta_1, x_val)
            s_new_val = self.calcSlope(self.p_2, self.q_2, self.theta_2, x_val)
            
            self.sold.append(s_old_val)
            self.snew.append(s_new_val)
            
            error = self.inv * 2 * self.detector_distance * (s_old_val - s_new_val) * 1000000. / self.pixel_size
            self.error_list.append(error)
            pos += 1
        
        return error
    
    def calcSlopes(self):

        #
        if self.method==1:
            return self.calcCamPos();
        #
        
        self.s_pos = [ i for i in self.frange(self.slit_start, self.slit_end, self.slit_step) ]
        
        s_med = self.median(self.s_pos)
        pos = 0
        
        self.error_list = []
        
        for i in self.s_pos:
            x_val = self.i_sign * 0.001 * ((self.s_pos[pos] - s_med) / sin(self.theta_1))
            
            s_old_val = self.calcSlope(self.p_1, self.q_1, self.theta_1, x_val)
            s_new_val = self.calcSlope(self.p_2, self.q_2, self.theta_2, x_val)
            
            self.sold.append(s_old_val)
            self.snew.append(s_new_val)
            
            error = self.inv * 2 * self.detector_distance * (s_old_val - s_new_val) * 1000000. / self.pixel_size
            self.error_list.append(error)
            pos += 1
        
        self.create_error_file(self.s_pos, self.error_list, self.column)
        
        return error
            
    def calcSlope(self, p, q, theta, x):
        #print "p="+str(p)+" q="+str(q)+" theta="+str(theta)+" x="+str(x)
        A = (p + q) * sin(theta)
        B = pow((p + q), 2) - pow((p - q), 2) * pow(sin(theta), 2)
        C = (p - q) * cos(theta)
        D = sqrt(p * q)
        E = (C) - (2 * x)
        F = sqrt((p * q) + ((p - q) * cos(theta)) * x - pow(x, 2))
        return (A / B) * (C - D * E / F)
        
    def median(self, s):
        i = len(s)
        if not i % 2:
            return (s[(i / 2) - 1] + s[i / 2]) / 2.0
        return s[i / 2]

    def frange(self, limit1, limit2, increment):
        increment = float(increment)
        count = int(ceil(((limit2 + increment / 100.) - limit1) / increment))
        result = []
        for n in range(count):
            result.append(limit1 + n * increment)
        return result
    
    def getBeamPositions(self):
        return self.s_pos
    
    def getErrors(self):
        return self.error_list
        
    def create_error_file(self, beam_positions, errors, output_column_name): #peak2d_peakx or peak2d_peaky
        pa = PreloadedArray('pa', ['beam_position', output_column_name, 'sold', 'snew'], ['%f', '%f','%f', '%f'])
        for i in range(len(beam_positions)):
            pa.append([beam_positions[i], errors[i], self.sold[i], self.snew[i]])
        scan([pa, 0, pa.getLength()-1, 1])   

if __name__ == '__main__':
    unittest.main()

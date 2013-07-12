import unittest
from math import sin, cos, tan, pow, sqrt, ceil
from gda.jython.commands.ScannableCommands import scan
from gdascripts.scannable.preloadedArray import PreloadedArray

class TestCorrectResults(unittest.TestCase):
    
    def setUp(self):
        self.calc = EllipseCalculator(pixel_size=4.65, p_1=33.115, q_1=6.885, theta_1=.0027, p_2=25., q_2=10., theta_2=.0025, i_sign= -1., detector_distance=6.885, slit_start=-3.89,slit_end=-5.21, slit_step=-.04)
    
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

    def calc(self, method):
        if method==0:
            self.calcSlopes()
        elif method==1:
            self.calcCamPos()
        elif method==2:
            self.calcCamPos2()

    # Method 0
    def calcSlopes(self):
        self.s_pos = self.frange(self.slit_start, self.slit_end, self.slit_step)
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
        return self.error_list
            
    def calcSlope(self, p, q, theta, x):
        A = (p + q) * sin(theta)
        B = pow((p + q), 2) - pow((p - q), 2) * pow(sin(theta), 2)
        C = (p - q) * cos(theta)
        D = sqrt(p * q)
        E = (C) - (2 * x)
        F = sqrt((p * q) + ((p - q) * cos(theta)) * x - pow(x, 2))
        return (A / B) * (C - D * E / F)

    # Method 1
    def calcCamPos(self):
        self.s_pos = self.frange(self.slit_start, self.slit_end, self.slit_step)
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
        self.create_error_file_cam(self.s_pos, self.error_list, self.column)
        return self.error_list

    def calcHeight(self, p, q, theta, x):
        A = (p + q) * sin(theta)
        B = pow((p + q), 2) - pow((p - q), 2) * pow(sin(theta), 2)
        C = (p - q) * cos(theta) * x
        D = 2 * sqrt(p * q)
        E = 2 * p * q
        F = sqrt((p * q) + ((p - q) * cos(theta)) * x - pow(x, 2))
        return (A / B) * (E + C - D * F)
        
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
        #should really output the slit positions
        pa = PreloadedArray('pa', ['beam_position', output_column_name, 'sold', 'snew'], ['%f', '%f','%f', '%f'])
        for i in range(len(beam_positions)):
            pa.append([beam_positions[i], errors[i], self.sold[i], self.snew[i]])
        # scan requires a command server to be present so the file should be created the simple way!
        try:
            scan([pa, 0, pa.getLength()-1, 1])
        except:
            pass
        
    def create_error_file_cam(self, beam_positions, errors, output_column_name): #peak2d_peakx or peak2d_peaky
        #should really output the slit positions
        pa = PreloadedArray('pa', ['beam_position', output_column_name, 'told', 'tnew'], ['%f', '%f','%f', '%f'])
        for i in range(len(beam_positions)):
            pa.append([beam_positions[i], errors[i], self.told[i], self.tnew[i]])
        print "scanning"
        # scan requires a command server to be present so the file should be created the simple way!
        try:
            scan([pa, 0, pa.getLength()-1, 1])
        except:
            pass
        
    # Method 2
    def calcCamPos2(self):
        snthold = sin(self.theta_1)
        #self.s_pos = self.frange(-0.18, 0.15, 0.01)
        self.s_pos = [ i for i in self.frange(self.slit_start, self.slit_end, self.slit_step) ]
        s_medmm = self.median(self.s_pos)
        xbar1m=[]
        pos=0
        for i in self.s_pos:
            xbar1m.append(self.i_sign*0.001*((self.s_pos[pos]-s_medmm)/snthold))
            pos+=1
        ydyel1 = self.EllipseCalc3(self.p_1,self.q_1,self.theta_1,xbar1m)
        ybar1m = ydyel1[0];
        dyb1rd = ydyel1[1];
        ydyel2 = self.EllipseCalc3(self.p_2,self.q_2,self.theta_2,xbar1m);
        ybar2m = ydyel2[0];
        dyb2rd = ydyel2[1];
        utonm = self.new_ellipse_method(xbar1m,ybar1m,dyb1rd,self.p_1,self.q_1,self.theta_1,ybar2m,dyb2rd,self.p_2,self.q_2,self.theta_2,self.detector_distance);
        self.told = utonm[0][1];
        self.tnew = utonm[1][1];
        pos=0
        for i in self.told:
            self.error_list.append(self.inv*(self.told[pos]-self.tnew[pos])*1.0E+06/self.pixel_size)
            pos+=1
        self.create_error_file_cam(self.s_pos, self.error_list, self.column)
        return self.error_list
    
    def EllipseCalc3(self,pm,qm,thrad,xm):
        ppq=pm+qm
        pmq=pm-qm
        pq=pm*qm
        pmqcos=pmq*cos(thrad)
        snth=sin(thrad)
        prf=ppq*snth/(ppq*ppq-pmq*pmq*snth*snth)
        xrad=[]
        yell=[]
        dyell=[]
        pos=0
        for i in xm:
            x=sqrt(pq+pmqcos*xm[pos]-xm[pos]*xm[pos])
            xrad.append(x)
            yell.append(prf*( 2.*pq + pmqcos*xm[pos] - 2.*sqrt(pq)*x))
            dyell.append(prf*( pmqcos - sqrt(pq)*( pmqcos - 2.*xm[pos] )/x))
            pos+=1
        return [yell,dyell]
    
    def new_ellipse_method(self,xbar1m,ybar1m,dyb1rd,p1m,q1m,th1rad,ybar2m,dyb2rd,p2m,q2m,th2rad,detector_distance):
        snth1 = sin(th1rad)
        csth1 = cos(th1rad)
        sntth1 = 2.0*snth1*csth1
        cstth1 = csth1*csth1-snth1*snth1
        f1m = sqrt(p1m*p1m+q1m*q1m+2.0*p1m*q1m*cstth1)
        xc1m = (p1m*p1m-q1m*q1m)/(2.0*f1m)
        yc1m = p1m*q1m*sntth1/f1m
        snbt1 = (p1m-q1m)*snth1/f1m
        csbt1 = (p1m+q1m)*csth1/f1m
        sneta1 = q1m*sntth1/f1m
        cseta1 = (p1m+q1m*cstth1)/f1m
        snth2 = sin(th2rad)
        csth2 = cos(th2rad)
        sntth2 = 2.0*snth2*csth2
        cstth2 = csth2*csth2-snth2*snth2
        f2m = sqrt(p2m*p2m+q2m*q2m + 2.0*p2m*q2m*cstth2)
        xc2m = (p2m*p2m-q2m*q2m)/(2.0*f2m)
        yc2m = p2m*q2m*sntth2/f2m
        snbt2 = (p2m-q2m)*snth2/f2m
        csbt2 = (p2m+q2m)*csth2/f2m
        sneta2 = q2m*sntth2/f2m
        cseta2 = (p2m + q2m*cstth2)/f2m
        sndeta=sneta1*cseta2-cseta1*sneta2
        csdeta=cseta1*cseta2+sneta1*sneta2
        snt1t2=snth1*csth2-csth1*snth2
        cst1t2=csth1*csth2+snth1*snth2
        utoldma=[]
        utoldmb=[]
        utnewma=[]
        utnewmb=[]
        pos=0
        for i in ybar1m:
            x=xc1m-xbar1m[pos]*csbt1-ybar1m[pos]*snbt1
            y=yc1m+xbar1m[pos]*snbt1-ybar1m[pos]*csbt1
            r=sqrt(pow((0.5*f1m-x),2)+pow(y,2))
            a=(0.5*f1m-x)/r
            b=-yc1m/q1m
            c=-y/r
            d=-(0.5*f1m-xc1m)/q1m
            x=(1.0-detector_distance/q1m)*xc1m+0.5*(1.0+detector_distance/q1m)*f1m-(x+0.5*f1m)
            y=(1.0-detector_distance/q1m)*yc1m-y
            det=(a*d)-(b*c)
            AB=[(d/det*x)+(-b/det*y),(-c/det*x)+(a/det*y)]
            utoldma.append(AB[0])
            utoldmb.append(AB[1])
            xp2rtm=(xbar1m[pos]*cst1t2+ybar2m[pos]*snt1t2)
            yp2rtm=(-xbar1m[pos]*snt1t2+ybar2m[pos]*cst1t2)
            spxb2m=(-p2m*csth2+xp2rtm)
            spyb2m=(-p2m*snth2+yp2rtm)
            splngm=(sqrt(spxb2m*spxb2m+spyb2m*spyb2m))
            rpxb2=(spxb2m/splngm)
            rpyb2=(spyb2m/splngm)
            csgam=(-csth2*rpxb2-snth2*rpyb2)
            sign=0.
            xb=xbar1m[pos]
            if xb>0:
                sign=1.
            elif xb<0:
                sign=-1.
            sngam=(-sqrt(1.0-csgam*csgam)*sign)
            csrho=(1.0/sqrt(1.0+dyb2rd[pos]*dyb2rd[pos]))
            snrho=(dyb2rd[pos]/sqrt(1.0+dyb2rd[pos]*dyb2rd[pos]))
            snthgm=(snth1*csgam-csth1*sngam)
            csthgm=(csth1*csgam+snth1*sngam)
            snkapp=(snthgm*csrho-csthgm*snrho)
            cskapp=(csthgm*csrho+snthgm*snrho)
            sn2kp=(2.0*snkapp*cskapp)
            cs2kp=(cskapp*cskapp-snkapp*snkapp)
            rrpxb2=(rpxb2*cs2kp+rpyb2*sn2kp)
            rrpyb2=(-rpxb2*sn2kp+rpyb2*cs2kp)
            xp2m=(xc2m-xp2rtm*csbt2-yp2rtm*snbt2)
            yp2m=(yc2m+xp2rtm*snbt2-yp2rtm*csbt2)
            rrpx2=(-rrpxb2*csbt2-rrpyb2*snbt2)
            rrpy2=(rrpxb2*snbt2-rrpyb2*csbt2)
            xp1m=(( xp2m + 0.5*f2m )*csdeta - yp2m*sndeta - 0.5*f1m)
            yp1m=(( xp2m + 0.5*f2m )*sndeta + yp2m*csdeta)
            rrpx1=(rrpx2*csdeta - rrpy2*sndeta)
            rrpy1=(rrpx2*sndeta + rrpy2*csdeta)
            a = rrpx1
            b = -yc1m/q1m
            c = rrpy1
            d = -(0.5*f1m - xc1m)/q1m
            x = xc1m + detector_distance*(0.5*f1m - xc1m)/q1m - xp1m
            y = (1.0-detector_distance/q1m)*yc1m - yp1m
            det=(a*d)-(b*c)
            AB=[(d/det*x)+(-b/det*y),(-c/det*x)+(a/det*y)]
            utnewma.append(AB[0])
            utnewmb.append(AB[1])
            pos+=1
        utoldm = [utoldma,utoldmb]
        utnewm = [utnewma,utnewmb]
        return [utoldm,utnewm]

if __name__ == '__main__':
    unittest.main()

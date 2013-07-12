from math import pow, sin, cos, sqrt, ceil

def ut_main(xbar1m,ybar1m,dyb1rd,p1m,q1m,th1rad,ybar2m,dyb2rd,p2m,q2m,th2rad,ddetm):
    
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
        x=(1.0-ddetm/q1m)*xc1m+0.5*(1.0+ddetm/q1m)*f1m-(x+0.5*f1m)
        y=(1.0-ddetm/q1m)*yc1m-y
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
        x = xc1m + ddetm*(0.5*f1m - xc1m)/q1m - xp1m
        y = (1.0-ddetm/q1m)*yc1m - yp1m
        det=(a*d)-(b*c)
        AB=[(d/det*x)+(-b/det*y),(-c/det*x)+(a/det*y)]
        utnewma.append(AB[0])
        utnewmb.append(AB[1])
        pos+=1
        
    utoldm = [utoldma,utoldmb]
    utnewm = [utnewma,utnewmb]
    
    return [utoldm,utnewm]


def EllipseCalc3(pm,qm,thrad,xm):
    ppq = pm + qm
    pmq = pm - qm
    pq = pm*qm
    pmqcos = pmq*cos(thrad)
    snth = sin(thrad)
    prf = ppq*snth/( ppq*ppq - pmq*pmq*snth*snth )
    
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


def frange(limit1, limit2, increment):
    increment = float(increment)
    count = int(ceil(((limit2 + increment / 100.) - limit1) / increment))
    result = []
    for n in range(count):
        result.append(limit1 + n * increment)
    return result


def median(mylist):
    sorts = sorted(mylist)
    length = len(sorts)
    if not length % 2:
        return (sorts[length / 2] + sorts[length / 2 - 1]) / 2.0
    return sorts[length / 2]


isign = -1
iinv = +1
poldm = 215.0
qoldm = 0.403
toldmr = 2.91
pnewm = 215.0
qnewm = 0.270
tnewmr = 2.40
ddetm = 0.403
pixum = 0.19
tholdr = toldmr*1.0E-03
thnewr = tnewmr*1.0E-03
snthold = sin(tholdr)
s_posmm = frange(-0.18, 0.15, 0.01)
s_medmm = median(s_posmm)

xbar1m=[]
pos=0
for i in s_posmm:
    xbar1m.append(isign*0.001*((s_posmm[pos]-s_medmm)/snthold))
    pos+=1
    
ydyel1 = EllipseCalc3(poldm,qoldm,tholdr,xbar1m)
ybar1m = ydyel1[0];
dyb1rd = ydyel1[1];

ydyel2 = EllipseCalc3(pnewm,qnewm,thnewr,xbar1m);
ybar2m = ydyel2[0];
dyb2rd = ydyel2[1];

utonm = ut_main(xbar1m,ybar1m,dyb1rd,poldm,qoldm,tholdr,ybar2m,dyb2rd,pnewm,qnewm,thnewr,ddetm);
uoldm = utonm[0][0];
toldm = utonm[0][1];
unewm = utonm[1][0];
tnewm = utonm[1][1];

error=[]
pos=0
for i in toldm:
    error.append(iinv*(toldm[pos]-tnewm[pos])*1.0E+06/pixum)
    pos+=1
    
print error
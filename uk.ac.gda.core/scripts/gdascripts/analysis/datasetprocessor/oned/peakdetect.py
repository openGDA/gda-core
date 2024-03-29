import scisoftpy as dnp
import sys

def peakdet(v, delta, x = None):
    """
Converted from MATLAB script at http://billauer.co.il/peakdet.html
Currently returns two lists of tuples, but maybe arrays would be better
function [maxtab, mintab]=peakdet(v, delta, x)
%PEAKDET Detect peaks in a vector
% [MAXTAB, MINTAB] = PEAKDET(V, DELTA) finds the local
% maxima and minima ("peaks") in the vector V.
% MAXTAB and MINTAB consists of two columns. Column 1
% contains indices in V, and column 2 the found values.
%
% With [MAXTAB, MINTAB] = PEAKDET(V, DELTA, X) the indices
% in MAXTAB and MINTAB are replaced with the corresponding
% X-values.
%
% A point is considered a maximum peak if it has the maximal
% value, and was preceded (to the left) by a value lower by
% DELTA.
% Eli Billauer, 3.4.05 (Explicitly not copyrighted).
% This function is released to the public domain; Any use is allowed.
"""
    maxtab = []
    mintab = []
       
    if x is None:
        x = dnp.arange(len(v))
    
    v = dnp.asarray(v)
    
    if len(v) != len(x):
        sys.exit('Input vectors v and x must have same length')
    
    if not isinstance(delta, (type(None),int,float,str,bool)):
        sys.exit('Input argument delta must be a number')
    
    if delta <= 0:
        sys.exit('Input argument delta must be positive')
        

    mn, mx = float('inf'), float('-inf')
    mnpos, mxpos = float('NaN'), float('NaN')
    
    lookformax = True
    
    for i in dnp.arange(len(v)):
        this = v[i]
        if this > mx:
            mx = this
            mxpos = x[i]
        if this < mn:
            mn = this
            mnpos = x[i]
        
        if lookformax:
            if this < mx-delta:
                maxtab.append((mxpos, mx))
                mn = this
                mnpos = x[i]
                lookformax = False
        else:
            if this > mn+delta:
                mintab.append((mnpos, mn))
                mx = this
                mxpos = x[i]
                lookformax = True

    return maxtab, mintab

if __name__=="__main__":
    series = [0.0,0.7,0.4,2.0,0.8,0.4,0.0,-2.0,0.0,0.0,0.0,2.0,0.0,0.0,0.0,-2.0,0.0]
    print peakdet(series,1.0)
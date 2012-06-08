#We must redefine it in Py3k if it's not already there
def execfile(file, glob=None, loc=None):
    if glob is None:
        glob = globals()
    if loc is None:
        loc = glob
    stream = open(file, 'rb')
    try:
        encoding = None
        #Get encoding!
        for _i in range(2):
            line = stream.readline() #Should not raise an exception even if there are no more contents
            #Must be a comment line
            # b prefix for Python 3 is unsupported in Jython 2, so as GDA only uses Jython, remote b
            if line.strip().startswith('#'):
                #Don't import re if there's no chance that there's an encoding in the line
                if 'coding' in line:
                    import re
                    p = re.search(r"coding[:=]\s*([-\w.]+)", line)
                    if p:
                        try:
                            encoding = p.group(1).decode('ascii')
                            break
                        except:
                            encoding = None
    finally:
        stream.close()

    if encoding:
        stream = open(file, encoding=encoding)
    else:
        stream = open(file)
    try:
        contents = stream.read()
    finally:
        stream.close()
        
    exec(compile(contents+"\n", file, 'exec'), glob, loc) #execute the script
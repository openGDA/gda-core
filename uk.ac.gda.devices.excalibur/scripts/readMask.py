import csv
def get_mask_from_file(file_path):
    d=[]
    csvfile =  open(file_path)
    rdr=csv.reader(csvfile)
    for r in rdr:
        d=d+r
    return d
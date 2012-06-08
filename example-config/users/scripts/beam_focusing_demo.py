# fwhm = full width half maximum
# minimise fwhmarea (area of beam spot on detector)

# to display the images only (on the "Data Vector" panel):
scan f 430 600 20 pil 20

# to focus on region of interest:
peak2d.setRoi(50, 50, 150, 150)

# do a wide scan:
scan f 430 600 20 pil 20 peak2d
# data is plotted as it's collected

# then do a finer scan:
go minval
rscan f -20 20 2.5 pil 20 peak2d

# show result:
minval

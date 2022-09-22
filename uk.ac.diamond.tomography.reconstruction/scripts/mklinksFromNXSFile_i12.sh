echo "Hostname: "
echo $HOSTNAME

. /etc/profile.d/modules.sh
cd /dls_sw/i12/software/gda/workspace_git/gda-tomography.git/uk.ac.diamond.tomography.reconstruction/scripts/
module load numpy
echo "Python interpreter: "
which python
echo "Python command: "
echo python mklinksFromNXSFile_i12.py $@
python mklinksFromNXSFile_i12.py $@
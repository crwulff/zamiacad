setMode -bs
setCable -port auto
Identify 
identifyMPM 
assignFile -p 4 -file "xilinx-ml501-xc5vlx50.bit"
Program -p 4 -v -defaultVersion 0 
quit

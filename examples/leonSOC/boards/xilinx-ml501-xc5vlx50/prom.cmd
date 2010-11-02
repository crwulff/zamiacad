setMode -bs
setCable -port auto
Identify 
identifyMPM 
setAttribute -position 1 -attr devicePartName -value "xcf32p"
assignFile -p 1 -file "xilinx-ml501-xc5vlx50.mcs"
setAttribute -position 1 -attr packageName -value "(null)"
Program -p 1 -v -defaultVersion 0 
quit

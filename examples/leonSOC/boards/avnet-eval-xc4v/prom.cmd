setMode -bscan
setCable -port auto
Identify 
identifyMPM 
assignFile -p 1 -file "avnet-eval-xc4v.mcs"
Program -p 1 -e -v -defaultVersion 0 
quit

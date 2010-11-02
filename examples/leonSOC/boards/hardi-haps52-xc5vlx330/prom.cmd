setMode -bs
setMode -bs
setCable -port auto
Identify
identifyMPM
assignFile -p 1 -file "hardi-haps52-xc5vlx330.bit"
attachflash -position 1 -spi "M25P128"
assignfiletoattachedflash -position 1 -file "hardi-haps52-xc5vlx330.mcs"
attachflash -position 1 -spi "M25P128"
Program -p 1 -e -v -defaultVersion 0 -spionly

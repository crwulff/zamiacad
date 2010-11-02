setPreference -pref AutoSignature:FALSE
setPreference -pref KeepSVF:FALSE
setPreference -pref ConcurrentMode:FALSE
setPreference -pref UseHighz:FALSE
setPreference -pref UserLevel:NOVICE
setPreference -pref svfUseTime:FALSE
setMode -bs
setCable -port auto
Identify 
identifyMPM
assignFile -p 2 -file "leon3mp.bit"
Program -p 2 -s -defaultVersion 0 
quit

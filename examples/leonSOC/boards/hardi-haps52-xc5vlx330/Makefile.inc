TECHNOLOGY=virtex5
PART=xc5vlx330
PACKAGE=ff1760
SPEED=-1
SYNFREQ=100
#PROMGENPAR=-x xcf32p -u 0 $(TOP).bit -p mcs -w -o hardi-haps52-xc5vlx330
PROMGENPAR=-spi -w -o hardi-haps52-xc5vlx330 -p mcs -u 0 $(TOP).bit -s 16384

MANUFACTURER=Xilinx
MGCPART=5vlx330$(PACKAGE)
MGCTECHNOLOGY=VIRTEX-5
MGCPACKAGE=$(PACKAGE)

# Note: HAPSMAP must point to hapsmap executable
# Note: HAPSMAP_LIB must point 'cardlib' path

BOARDPERIOD = 10

HAPSMAP = hapsmap

hapsmap : $(TOP).ucf

$(TOP).ucf : $(TOP).con $(TOP).pas
	@if test $(OS) != "Linux"; then \
	   if test $(OS) != "SunOs"; then \
	      $(HAPSMAP) $(TOP).con -c 0 -n $(TOP); \
	   else \
	      echo "HapsMap does not support SunOS"; \
	   fi; \
   else \
      $(HAPSMAP) $(TOP).con -c 0 -n $(TOP); \
   fi;
	@mv $(TOP)A.ucf $(TOP).ucf
	@echo " " >> $(TOP).ucf
	@echo " NET clk period =" $(BOARDPERIOD) " ; " >> $(TOP).ucf

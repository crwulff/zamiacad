TECHNOLOGY=virtex4
PART=xc4vlx80
PACKAGE=ff1148
SPEED=-10
SYNFREQ=50
PROMGENPAR=-x xcf32p -u 0 $(TOP).bit -p mcs -w -o hardi-haps31-xc4vlx80

MANUFACTURER=Xilinx
MGCPART=4vlx80$(PACKAGE)
MGCTECHNOLOGY=VIRTEX-4
MGCPACKAGE=$(PACKAGE)

# Note: HAPSMAP must point to hapsmap executable
# Note: HAPSMAP_LIB must point 'cardlib' path

BOARDPERIOD=25

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
	@echo " NET clk period = " $(BOARDPERIOD) ";" >> $(TOP).ucf

TECHNOLOGY=virtex5
PART=xc5vlx330
PACKAGE=ff1760
SPEED=-1
SYNFREQ=100
#PROMGENPAR=-x xcf32p -u 0 $(TOP).bit -p mcs -w -o hardi-haps51-xc5vlx330
PROMGENPAR=-spi -w -o hardi-haps51-xc5vlx330 -p mcs -u 0 $(TOP).bit -s 16384

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
	@echo " NET clk_100 period =" $(BOARDPERIOD) " ; " >> $(TOP).ucf
	@echo " " >> $(TOP).ucf
	@echo " INST \"ddrsp0.ddrc0/ddr_phy0/ddr_phy0/xc4v.ddr_phy0/idelctrl.0.u\" LOC = \"IDELAYCTRL_X2Y0\"; " >> $(TOP).ucf
	@echo " INST \"ddrsp0.ddrc0/ddr_phy0/ddr_phy0/xc4v.ddr_phy0/idelctrl.1.u\" LOC = \"IDELAYCTRL_X2Y1\"; " >> $(TOP).ucf
	@echo " INST \"ddrsp0.ddrc0/ddr_phy0/ddr_phy0/xc4v.ddr_phy0/idelctrl.2.u\" LOC = \"IDELAYCTRL_X2Y3\"; " >> $(TOP).ucf
	@echo " INST \"ddrsp0.ddrc0/ddr_phy0/ddr_phy0/xc4v.ddr_phy0/idelctrl[0].u\" LOC = \"IDELAYCTRL_X2Y0\"; " >> $(TOP).ucf
	@echo " INST \"ddrsp0.ddrc0/ddr_phy0/ddr_phy0/xc4v.ddr_phy0/idelctrl[1].u\" LOC = \"IDELAYCTRL_X2Y1\"; " >> $(TOP).ucf
	@echo " INST \"ddrsp0.ddrc0/ddr_phy0/ddr_phy0/xc4v.ddr_phy0/idelctrl[2].u\" LOC = \"IDELAYCTRL_X2Y3\"; " >> $(TOP).ucf
	@echo " " >> $(TOP).ucf
	@echo " NET \"clkm\" 		TNM_NET = \"clkm\"; " >> $(TOP).ucf
	@echo " NET \"clkml\" 		TNM_NET = \"clkml\"; " >> $(TOP).ucf
	@echo " TIMESPEC \"TS_clkm_clkml\" = FROM \"clkm\" TO \"clkml\" TIG; " >> $(TOP).ucf
	@echo " TIMESPEC \"TS_clkml_clkm\" = FROM \"clkml\" TO \"clkm\" TIG; " >> $(TOP).ucf

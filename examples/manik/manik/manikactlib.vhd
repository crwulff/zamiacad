-------------------------------------------------------------------------------
-- Title      : manikactlib
-- Project    : MANIK-2
-------------------------------------------------------------------------------
-- File       : maniklib.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2002-10-28
-- Last update: 2005-11-03
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Contains ACTEL Specific optimized Library
-------------------------------------------------------------------------------
-- Copyright (c) 2002 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2002-10-28  1.0      sandeep	Created
-------------------------------------------------------------------------------


library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

library UNISIM;
use UNISIM.vcomponents.all;

entity adsu is 
    port( DataA : in std_logic_vector(31 downto 0); DataB : in 
        std_logic_vector(31 downto 0);Cin, Addsub : in std_logic; 
        Sum : out std_logic_vector(31 downto 0); Cout : out 
        std_logic) ;
end adsu;


architecture DEF_ARCH of  adsu is

    component INV
        port(A : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component AND2
        port(A, B : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component XNOR2
        port(A, B : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component AO21
        port(A, B, C : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component XOR2
        port(A, B : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component BFR
        port(A : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component OR3
        port(A, B, C : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    signal AddsubAux_0_net, AddsubAux_6_net, AddsubAux_12_net, 
        AddsubAux_18_net, AddsubAux_24_net, AddsubAux_29_net, 
        DataBXnor2_0_net, DataBXnor2_1_net, DataBXnor2_2_net, 
        DataBXnor2_3_net, DataBXnor2_4_net, DataBXnor2_5_net, 
        DataBXnor2_6_net, DataBXnor2_7_net, DataBXnor2_8_net, 
        DataBXnor2_9_net, DataBXnor2_10_net, DataBXnor2_11_net, 
        DataBXnor2_12_net, DataBXnor2_13_net, DataBXnor2_14_net, 
        DataBXnor2_15_net, DataBXnor2_16_net, DataBXnor2_17_net, 
        DataBXnor2_18_net, DataBXnor2_19_net, DataBXnor2_20_net, 
        DataBXnor2_21_net, DataBXnor2_22_net, DataBXnor2_23_net, 
        DataBXnor2_24_net, DataBXnor2_25_net, DataBXnor2_26_net, 
        DataBXnor2_27_net, DataBXnor2_28_net, DataBXnor2_29_net, 
        DataBXnor2_30_net, DataBXnor2_31_net, addci, addco, 
        INV_0_Y, AND2_6_Y, AND2_112_Y, AND2_148_Y, AND2_51_Y, 
        AND2_117_Y, AND2_106_Y, AND2_126_Y, AND2_105_Y, 
        AND2_135_Y, AND2_58_Y, AND2_47_Y, AND2_95_Y, AND2_122_Y, 
        AND2_83_Y, AND2_74_Y, AND2_28_Y, AND2_27_Y, AND2_96_Y, 
        AND2_76_Y, AND2_1_Y, AND2_81_Y, AND2_145_Y, AND2_159_Y, 
        AND2_128_Y, AND2_118_Y, AND2_63_Y, AND2_62_Y, AND2_146_Y, 
        AND2_119_Y, AND2_43_Y, AND2_98_Y, AND2_158_Y, AND2_11_Y, 
        AND2_150_Y, XOR2_15_Y, XOR2_37_Y, XOR2_22_Y, XOR2_47_Y, 
        XOR2_29_Y, XOR2_23_Y, XOR2_34_Y, XOR2_17_Y, XOR2_1_Y, 
        XOR2_42_Y, XOR2_50_Y, XOR2_63_Y, XOR2_51_Y, XOR2_41_Y, 
        XOR2_45_Y, XOR2_46_Y, XOR2_56_Y, XOR2_59_Y, XOR2_40_Y, 
        XOR2_21_Y, XOR2_31_Y, XOR2_38_Y, XOR2_32_Y, XOR2_14_Y, 
        XOR2_28_Y, XOR2_30_Y, XOR2_35_Y, XOR2_36_Y, XOR2_12_Y, 
        XOR2_54_Y, XOR2_3_Y, XOR2_18_Y, AND2_114_Y, AO21_62_Y, 
        AND2_39_Y, AO21_104_Y, AND2_124_Y, AO21_105_Y, AND2_52_Y, 
        AO21_85_Y, AND2_162_Y, AO21_56_Y, AND2_102_Y, AO21_36_Y, 
        AND2_68_Y, AO21_110_Y, AND2_144_Y, AO21_121_Y, AND2_31_Y, 
        AO21_25_Y, AND2_137_Y, AO21_24_Y, AND2_37_Y, AO21_91_Y, 
        AND2_123_Y, AO21_70_Y, AND2_41_Y, AO21_80_Y, AND2_140_Y, 
        AO21_126_Y, AND2_80_Y, AO21_39_Y, AND2_24_Y, AO21_111_Y, 
        AND2_67_Y, AO21_102_Y, AND2_143_Y, AO21_46_Y, AND2_29_Y, 
        AO21_10_Y, AND2_136_Y, AO21_94_Y, AND2_36_Y, AO21_33_Y, 
        AND2_120_Y, AO21_5_Y, AND2_40_Y, AO21_21_Y, AND2_139_Y, 
        AO21_72_Y, AND2_79_Y, AO21_108_Y, AND2_23_Y, AO21_57_Y, 
        AND2_116_Y, AO21_48_Y, AND2_19_Y, AO21_114_Y, AND2_70_Y, 
        AO21_81_Y, AND2_14_Y, AO21_74_Y, AND2_77_Y, AND2_0_Y, 
        AND2_82_Y, AO21_2_Y, AND2_18_Y, AO21_53_Y, AND2_132_Y, 
        AO21_83_Y, AND2_66_Y, AO21_31_Y, AND2_49_Y, AO21_23_Y, 
        AND2_113_Y, AO21_93_Y, AND2_5_Y, AO21_65_Y, AND2_108_Y, 
        AO21_73_Y, AND2_8_Y, AO21_6_Y, AND2_93_Y, AO21_116_Y, 
        AND2_15_Y, AO21_127_Y, AND2_110_Y, AO21_50_Y, AND2_56_Y, 
        AO21_82_Y, AND2_2_Y, AO21_30_Y, AND2_59_Y, AO21_19_Y, 
        AND2_133_Y, AO21_92_Y, AND2_20_Y, AO21_63_Y, AND2_129_Y, 
        AO21_101_Y, AND2_25_Y, AO21_43_Y, AND2_107_Y, AO21_17_Y, 
        AND2_33_Y, AO21_29_Y, AND2_131_Y, AO21_75_Y, AND2_72_Y, 
        AO21_115_Y, AND2_17_Y, AO21_61_Y, AND2_32_Y, AO21_55_Y, 
        AND2_94_Y, AO21_122_Y, AND2_153_Y, AO21_90_Y, AND2_88_Y, 
        AO21_22_Y, AND2_157_Y, AND2_75_Y, AND2_161_Y, AND2_90_Y, 
        AND2_45_Y, AO21_38_Y, AND2_151_Y, AO21_109_Y, AND2_54_Y, 
        AO21_100_Y, AND2_138_Y, AO21_45_Y, AND2_134_Y, AO21_8_Y, 
        AND2_30_Y, AO21_86_Y, AND2_142_Y, AO21_28_Y, AND2_10_Y, 
        AO21_4_Y, AND2_84_Y, AO21_16_Y, AND2_55_Y, AO21_66_Y, 
        AND2_53_Y, AO21_103_Y, AND2_100_Y, AO21_51_Y, AND2_89_Y, 
        AO21_42_Y, AND2_7_Y, AO21_107_Y, AND2_3_Y, AO21_77_Y, 
        AND2_61_Y, AO21_76_Y, AND2_9_Y, AO21_12_Y, AND2_50_Y, 
        AO21_123_Y, AND2_130_Y, AO21_3_Y, AND2_92_Y, AO21_54_Y, 
        AND2_87_Y, AO21_89_Y, AND2_147_Y, AO21_35_Y, AND2_35_Y, 
        AO21_27_Y, AND2_109_Y, AO21_96_Y, AND2_104_Y, AND2_4_Y, 
        AND2_111_Y, AND2_155_Y, AND2_60_Y, AND2_38_Y, AND2_34_Y, 
        AND2_78_Y, AND2_44_Y, AO21_124_Y, AND2_125_Y, AO21_64_Y, 
        AND2_115_Y, AO21_32_Y, AND2_12_Y, AO21_99_Y, AND2_127_Y, 
        AO21_119_Y, AND2_160_Y, AO21_95_Y, AND2_71_Y, AO21_113_Y, 
        AND2_48_Y, AO21_79_Y, AND2_42_Y, AO21_97_Y, AND2_85_Y, 
        AO21_87_Y, AND2_101_Y, AO21_71_Y, AND2_21_Y, AO21_59_Y, 
        AND2_13_Y, AO21_117_Y, AND2_73_Y, AO21_20_Y, AND2_22_Y, 
        AO21_41_Y, AND2_57_Y, AO21_13_Y, AND2_141_Y, AND2_103_Y, 
        AND2_99_Y, AND2_156_Y, AND2_65_Y, AND2_152_Y, AND2_149_Y, 
        AND2_46_Y, AND2_154_Y, AND2_26_Y, AND2_97_Y, AND2_69_Y, 
        AND2_64_Y, AND2_121_Y, AND2_16_Y, AND2_91_Y, AND2_86_Y, 
        OR3_0_Y, AO21_14_Y, AO21_9_Y, AO21_118_Y, AO21_88_Y, 
        AO21_69_Y, AO21_78_Y, AO21_125_Y, AO21_68_Y, AO21_47_Y, 
        AO21_112_Y, AO21_84_Y, AO21_98_Y, AO21_15_Y, AO21_58_Y, 
        AO21_0_Y, AO21_34_Y, AO21_1_Y, AO21_18_Y, AO21_7_Y, 
        AO21_120_Y, AO21_106_Y, AO21_40_Y, AO21_52_Y, AO21_67_Y, 
        AO21_44_Y, AO21_60_Y, AO21_26_Y, AO21_49_Y, AO21_37_Y, 
        AO21_11_Y, XOR2_4_Y, XOR2_0_Y, XOR2_11_Y, XOR2_58_Y, 
        XOR2_6_Y, XOR2_7_Y, XOR2_2_Y, XOR2_13_Y, XOR2_60_Y, 
        XOR2_26_Y, XOR2_27_Y, XOR2_20_Y, XOR2_33_Y, XOR2_8_Y, 
        XOR2_55_Y, XOR2_57_Y, XOR2_52_Y, XOR2_61_Y, XOR2_43_Y, 
        XOR2_48_Y, XOR2_49_Y, XOR2_44_Y, XOR2_53_Y, XOR2_39_Y, 
        XOR2_9_Y, XOR2_10_Y, XOR2_5_Y, XOR2_16_Y, XOR2_62_Y, 
        XOR2_24_Y, XOR2_25_Y, XOR2_19_Y : std_logic ;
    begin   

    INV_0 : INV
      port map(A => addci, Y => INV_0_Y);
    AND2_132 : AND2
      port map(A => AND2_124_Y, B => AND2_162_Y, Y => AND2_132_Y);
    XNOR2_29_inst : XNOR2
      port map(A => DataB(29), B => AddsubAux_29_net, Y => 
        DataBXnor2_29_net);
    AO21_121 : AO21
      port map(A => XOR2_42_Y, B => AND2_47_Y, C => AND2_95_Y, 
        Y => AO21_121_Y);
    AND2_2 : AND2
      port map(A => AND2_140_Y, B => AND2_24_Y, Y => AND2_2_Y);
    AO21_52 : AO21
      port map(A => AND2_42_Y, B => AO21_125_Y, C => AO21_79_Y, 
        Y => AO21_52_Y);
    XNOR2_26_inst : XNOR2
      port map(A => DataB(26), B => AddsubAux_24_net, Y => 
        DataBXnor2_26_net);
    AO21_44 : AO21
      port map(A => AND2_101_Y, B => AO21_47_Y, C => AO21_87_Y, 
        Y => AO21_44_Y);
    AND2_20 : AND2
      port map(A => AND2_67_Y, B => AND2_29_Y, Y => AND2_20_Y);
    AO21_125 : AO21
      port map(A => AND2_49_Y, B => AO21_118_Y, C => AO21_31_Y, 
        Y => AO21_125_Y);
    XOR2_Sum_30_inst : XOR2
      port map(A => XOR2_25_Y, B => AO21_37_Y, Y => Sum(30));
    AO21_72 : AO21
      port map(A => XOR2_30_Y, B => AND2_62_Y, C => AND2_146_Y, 
        Y => AO21_72_Y);
    AND2_11 : AND2
      port map(A => DataA(30), B => DataBXnor2_30_net, Y => 
        AND2_11_Y);
    AND2_154 : AND2
      port map(A => AND2_45_Y, B => AND2_42_Y, Y => AND2_154_Y);
    AND2_22 : AND2
      port map(A => AND2_3_Y, B => AND2_35_Y, Y => AND2_22_Y);
    AND2_71 : AND2
      port map(A => AND2_142_Y, B => AND2_3_Y, Y => AND2_71_Y);
    AO21_0 : AO21
      port map(A => AND2_84_Y, B => AO21_125_Y, C => AO21_4_Y, 
        Y => AO21_0_Y);
    AO21_12 : AO21
      port map(A => AND2_131_Y, B => AO21_63_Y, C => AO21_29_Y, 
        Y => AO21_12_Y);
    AO21_124 : AO21
      port map(A => AND2_55_Y, B => AO21_38_Y, C => AO21_16_Y, 
        Y => AO21_124_Y);
    XOR2_19 : XOR2
      port map(A => DataA(31), B => DataBXnor2_31_net, Y => 
        XOR2_19_Y);
    AND2_44 : AND2
      port map(A => AND2_45_Y, B => AND2_84_Y, Y => AND2_44_Y);
    XOR2_23 : XOR2
      port map(A => DataA(5), B => DataBXnor2_5_net, Y => 
        XOR2_23_Y);
    XOR2_1 : XOR2
      port map(A => DataA(8), B => DataBXnor2_8_net, Y => 
        XOR2_1_Y);
    AO21_53 : AO21
      port map(A => AND2_162_Y, B => AO21_104_Y, C => AO21_85_Y, 
        Y => AO21_53_Y);
    AND2_104 : AND2
      port map(A => AND2_32_Y, B => AND2_157_Y, Y => AND2_104_Y);
    AO21_73 : AO21
      port map(A => AND2_37_Y, B => AO21_121_Y, C => AO21_24_Y, 
        Y => AO21_73_Y);
    XNOR2_13_inst : XNOR2
      port map(A => DataB(13), B => AddsubAux_12_net, Y => 
        DataBXnor2_13_net);
    XOR2_47 : XOR2
      port map(A => DataA(3), B => DataBXnor2_3_net, Y => 
        XOR2_47_Y);
    XOR2_38 : XOR2
      port map(A => DataA(21), B => DataBXnor2_21_net, Y => 
        XOR2_38_Y);
    XNOR2_8_inst : XNOR2
      port map(A => DataB(8), B => AddsubAux_6_net, Y => 
        DataBXnor2_8_net);
    XOR2_Sum_21_inst : XOR2
      port map(A => XOR2_44_Y, B => AO21_120_Y, Y => Sum(21));
    AO21_95 : AO21
      port map(A => AND2_3_Y, B => AO21_86_Y, C => AO21_107_Y, 
        Y => AO21_95_Y);
    AND2_124 : AND2
      port map(A => XOR2_22_Y, B => XOR2_47_Y, Y => AND2_124_Y);
    AO21_84 : AO21
      port map(A => AND2_134_Y, B => AO21_118_Y, C => AO21_45_Y, 
        Y => AO21_84_Y);
    XOR2_Sum_8_inst : XOR2
      port map(A => XOR2_60_Y, B => AO21_125_Y, Y => Sum(8));
    AO21_13 : AO21
      port map(A => AND2_104_Y, B => AO21_76_Y, C => AO21_96_Y, 
        Y => AO21_13_Y);
    XOR2_Sum_25_inst : XOR2
      port map(A => XOR2_10_Y, B => AO21_67_Y, Y => Sum(25));
    AND2_135 : AND2
      port map(A => DataA(6), B => DataBXnor2_6_net, Y => 
        AND2_135_Y);
    AND2_18 : AND2
      port map(A => AND2_39_Y, B => AND2_52_Y, Y => AND2_18_Y);
    AND2_15 : AND2
      port map(A => AND2_37_Y, B => AND2_41_Y, Y => AND2_15_Y);
    AO21_25 : AO21
      port map(A => XOR2_50_Y, B => AND2_95_Y, C => AND2_122_Y, 
        Y => AO21_25_Y);
    AND2_84 : AND2
      port map(A => AND2_8_Y, B => AND2_56_Y, Y => AND2_84_Y);
    AO21_62 : AO21
      port map(A => XOR2_22_Y, B => AND2_51_Y, C => AND2_117_Y, 
        Y => AO21_62_Y);
    BFR_AddsubAux_24_inst : BFR
      port map(A => Addsub, Y => AddsubAux_24_net);
    AND2_78 : AND2
      port map(A => AND2_90_Y, B => AND2_10_Y, Y => AND2_78_Y);
    AND2_75 : AND2
      port map(A => XOR2_15_Y, B => AND2_18_Y, Y => AND2_75_Y);
    XOR2_45 : XOR2
      port map(A => DataA(14), B => DataBXnor2_14_net, Y => 
        XOR2_45_Y);
    XNOR2_31_inst : XNOR2
      port map(A => DataB(31), B => AddsubAux_29_net, Y => 
        DataBXnor2_31_net);
    AND2_1 : AND2
      port map(A => DataA(17), B => DataBXnor2_17_net, Y => 
        AND2_1_Y);
    XOR2_Sum_5_inst : XOR2
      port map(A => XOR2_7_Y, B => AO21_88_Y, Y => Sum(5));
    AND2_49 : AND2
      port map(A => AND2_162_Y, B => AND2_68_Y, Y => AND2_49_Y);
    XNOR2_11_inst : XNOR2
      port map(A => DataB(11), B => AddsubAux_6_net, Y => 
        DataBXnor2_11_net);
    AND2_10 : AND2
      port map(A => AND2_108_Y, B => AND2_110_Y, Y => AND2_10_Y);
    AO21_59 : AO21
      port map(A => AND2_87_Y, B => AO21_51_Y, C => AO21_54_Y, 
        Y => AO21_59_Y);
    AND2_7 : AND2
      port map(A => AND2_2_Y, B => AND2_129_Y, Y => AND2_7_Y);
    XOR2_20 : XOR2
      port map(A => DataA(11), B => DataBXnor2_11_net, Y => 
        XOR2_20_Y);
    XOR2_63 : XOR2
      port map(A => DataA(11), B => DataBXnor2_11_net, Y => 
        XOR2_63_Y);
    AO21_63 : AO21
      port map(A => AND2_136_Y, B => AO21_102_Y, C => AO21_10_Y, 
        Y => AO21_63_Y);
    AND2_70 : AND2
      port map(A => XOR2_12_Y, B => XOR2_54_Y, Y => AND2_70_Y);
    AO21_9 : AO21
      port map(A => AND2_39_Y, B => OR3_0_Y, C => AO21_62_Y, Y => 
        AO21_9_Y);
    XNOR2_9_inst : XNOR2
      port map(A => DataB(9), B => AddsubAux_6_net, Y => 
        DataBXnor2_9_net);
    AO21_79 : AO21
      port map(A => AND2_9_Y, B => AO21_4_Y, C => AO21_76_Y, Y => 
        AO21_79_Y);
    AND2_12 : AND2
      port map(A => AND2_138_Y, B => AND2_100_Y, Y => AND2_12_Y);
    XOR2_52 : XOR2
      port map(A => DataA(16), B => DataBXnor2_16_net, Y => 
        XOR2_52_Y);
    AO21_48 : AO21
      port map(A => XOR2_12_Y, B => AND2_43_Y, C => AND2_98_Y, 
        Y => AO21_48_Y);
    AO21_2 : AO21
      port map(A => AND2_52_Y, B => AO21_62_Y, C => AO21_105_Y, 
        Y => AO21_2_Y);
    AND2_72 : AND2
      port map(A => AND2_40_Y, B => AND2_79_Y, Y => AND2_72_Y);
    AND2_61 : AND2
      port map(A => AND2_133_Y, B => AND2_107_Y, Y => AND2_61_Y);
    AO21_91 : AO21
      port map(A => XOR2_51_Y, B => AND2_83_Y, C => AND2_74_Y, 
        Y => AO21_91_Y);
    AO21_19 : AO21
      port map(A => AND2_143_Y, B => AO21_39_Y, C => AO21_102_Y, 
        Y => AO21_19_Y);
    AO21_32 : AO21
      port map(A => AND2_100_Y, B => AO21_100_Y, C => AO21_103_Y, 
        Y => AO21_32_Y);
    AO21_127 : AO21
      port map(A => AND2_140_Y, B => AO21_91_Y, C => AO21_80_Y, 
        Y => AO21_127_Y);
    AND2_89 : AND2
      port map(A => AND2_56_Y, B => AND2_20_Y, Y => AND2_89_Y);
    XOR2_24 : XOR2
      port map(A => DataA(29), B => DataBXnor2_29_net, Y => 
        XOR2_24_Y);
    AO21_21 : AO21
      port map(A => XOR2_28_Y, B => AND2_63_Y, C => AND2_62_Y, 
        Y => AO21_21_Y);
    AND2_148 : AND2
      port map(A => DataBXnor2_0_net, B => INV_0_Y, Y => 
        AND2_148_Y);
    AND2_57 : AND2
      port map(A => AND2_61_Y, B => AND2_109_Y, Y => AND2_57_Y);
    XOR2_21 : XOR2
      port map(A => DataA(19), B => DataBXnor2_19_net, Y => 
        XOR2_21_Y);
    XNOR2_14_inst : XNOR2
      port map(A => DataB(14), B => AddsubAux_12_net, Y => 
        DataBXnor2_14_net);
    AO21_90 : AO21
      port map(A => AND2_14_Y, B => AO21_48_Y, C => AO21_81_Y, 
        Y => AO21_90_Y);
    AND2_46 : AND2
      port map(A => AND2_90_Y, B => AND2_48_Y, Y => AND2_46_Y);
    AO21_33 : AO21
      port map(A => XOR2_32_Y, B => AND2_128_Y, C => AND2_118_Y, 
        Y => AO21_33_Y);
    AO21_56 : AO21
      port map(A => XOR2_34_Y, B => AND2_105_Y, C => AND2_135_Y, 
        Y => AO21_56_Y);
    AO21_20 : AO21
      port map(A => AND2_35_Y, B => AO21_107_Y, C => AO21_35_Y, 
        Y => AO21_20_Y);
    XOR2_16 : XOR2
      port map(A => DataA(27), B => DataBXnor2_27_net, Y => 
        XOR2_16_Y);
    XOR2_60 : XOR2
      port map(A => DataA(8), B => DataBXnor2_8_net, Y => 
        XOR2_60_Y);
    AO21_69 : AO21
      port map(A => AND2_132_Y, B => AO21_14_Y, C => AO21_53_Y, 
        Y => AO21_69_Y);
    AO21_88 : AO21
      port map(A => AND2_18_Y, B => OR3_0_Y, C => AO21_2_Y, Y => 
        AO21_88_Y);
    AND2_68 : AND2
      port map(A => XOR2_34_Y, B => XOR2_17_Y, Y => AND2_68_Y);
    AND2_65 : AND2
      port map(A => AND2_82_Y, B => AND2_127_Y, Y => AND2_65_Y);
    AO21_76 : AO21
      port map(A => AND2_33_Y, B => AO21_92_Y, C => AO21_17_Y, 
        Y => AO21_76_Y);
    AND2_43 : AND2
      port map(A => DataA(27), B => DataBXnor2_27_net, Y => 
        AND2_43_Y);
    XNOR2_17_inst : XNOR2
      port map(A => DataB(17), B => AddsubAux_12_net, Y => 
        DataBXnor2_17_net);
    XOR2_Sum_17_inst : XOR2
      port map(A => XOR2_61_Y, B => AO21_34_Y, Y => Sum(17));
    AO21_16 : AO21
      port map(A => AND2_2_Y, B => AO21_6_Y, C => AO21_82_Y, Y => 
        AO21_16_Y);
    AND2_86 : AND2
      port map(A => AND2_44_Y, B => AND2_141_Y, Y => AND2_86_Y);
    AND2_6 : AND2
      port map(A => DataA(0), B => DataBXnor2_0_net, Y => 
        AND2_6_Y);
    AND2_60 : AND2
      port map(A => AND2_82_Y, B => AND2_134_Y, Y => AND2_60_Y);
    XOR2_61 : XOR2
      port map(A => DataA(17), B => DataBXnor2_17_net, Y => 
        XOR2_61_Y);
    AND2_83 : AND2
      port map(A => DataA(11), B => DataBXnor2_11_net, Y => 
        AND2_83_Y);
    XOR2_57 : XOR2
      port map(A => DataA(15), B => DataBXnor2_15_net, Y => 
        XOR2_57_Y);
    XOR2_33 : XOR2
      port map(A => DataA(12), B => DataBXnor2_12_net, Y => 
        XOR2_33_Y);
    AND2_62 : AND2
      port map(A => DataA(24), B => DataBXnor2_24_net, Y => 
        AND2_62_Y);
    AND2_97 : AND2
      port map(A => AND2_111_Y, B => AND2_101_Y, Y => AND2_97_Y);
    XOR2_49 : XOR2
      port map(A => DataA(20), B => DataBXnor2_20_net, Y => 
        XOR2_49_Y);
    AO21_39 : AO21
      port map(A => XOR2_56_Y, B => AND2_96_Y, C => AND2_76_Y, 
        Y => AO21_39_Y);
    XOR2_4 : XOR2
      port map(A => DataA(0), B => DataBXnor2_0_net, Y => 
        XOR2_4_Y);
    AO21_66 : AO21
      port map(A => AND2_59_Y, B => AO21_116_Y, C => AO21_30_Y, 
        Y => AO21_66_Y);
    XNOR2_23_inst : XNOR2
      port map(A => DataB(23), B => AddsubAux_18_net, Y => 
        DataBXnor2_23_net);
    XOR2_55 : XOR2
      port map(A => DataA(14), B => DataBXnor2_14_net, Y => 
        XOR2_55_Y);
    AND2_24 : AND2
      port map(A => XOR2_46_Y, B => XOR2_56_Y, Y => AND2_24_Y);
    AND2_31 : AND2
      port map(A => XOR2_1_Y, B => XOR2_42_Y, Y => AND2_31_Y);
    AND2_113 : AND2
      port map(A => AND2_102_Y, B => AND2_144_Y, Y => AND2_113_Y);
    XOR2_18 : XOR2
      port map(A => DataA(31), B => DataBXnor2_31_net, Y => 
        XOR2_18_Y);
    XOR2_Sum_4_inst : XOR2
      port map(A => XOR2_6_Y, B => AO21_118_Y, Y => Sum(4));
    ADDERCI : XOR2
      port map(A => Cin, B => AddsubAux_29_net, Y => addci);
    AO21_57 : AO21
      port map(A => XOR2_36_Y, B => AND2_119_Y, C => AND2_43_Y, 
        Y => AO21_57_Y);
    AND2_138 : AND2
      port map(A => AND2_66_Y, B => AND2_108_Y, Y => AND2_138_Y);
    XOR2_8 : XOR2
      port map(A => DataA(13), B => DataBXnor2_13_net, Y => 
        XOR2_8_Y);
    AND2_116 : AND2
      port map(A => XOR2_35_Y, B => XOR2_36_Y, Y => AND2_116_Y);
    AND2_110 : AND2
      port map(A => AND2_123_Y, B => AND2_140_Y, Y => AND2_110_Y);
    XNOR2_21_inst : XNOR2
      port map(A => DataB(21), B => AddsubAux_18_net, Y => 
        DataBXnor2_21_net);
    AND2_144 : AND2
      port map(A => XOR2_17_Y, B => XOR2_1_Y, Y => AND2_144_Y);
    XOR2_30 : XOR2
      port map(A => DataA(25), B => DataBXnor2_25_net, Y => 
        XOR2_30_Y);
    AO21_77 : AO21
      port map(A => AND2_107_Y, B => AO21_19_Y, C => AO21_43_Y, 
        Y => AO21_77_Y);
    AO21_36 : AO21
      port map(A => XOR2_17_Y, B => AND2_135_Y, C => AND2_58_Y, 
        Y => AO21_36_Y);
    AO21_42 : AO21
      port map(A => AND2_129_Y, B => AO21_82_Y, C => AO21_63_Y, 
        Y => AO21_42_Y);
    AND2_38 : AND2
      port map(A => AND2_75_Y, B => AND2_30_Y, Y => AND2_38_Y);
    AND2_35 : AND2
      port map(A => AND2_72_Y, B => AND2_153_Y, Y => AND2_35_Y);
    AO21_17 : AO21
      port map(A => AND2_40_Y, B => AO21_94_Y, C => AO21_5_Y, 
        Y => AO21_17_Y);
    AND2_29 : AND2
      port map(A => XOR2_40_Y, B => XOR2_21_Y, Y => AND2_29_Y);
    AND2_153 : AND2
      port map(A => AND2_116_Y, B => AND2_70_Y, Y => AND2_153_Y);
    AO21_116 : AO21
      port map(A => AND2_41_Y, B => AO21_24_Y, C => AO21_70_Y, 
        Y => AO21_116_Y);
    XNOR2_2_inst : XNOR2
      port map(A => DataB(2), B => AddsubAux_0_net, Y => 
        DataBXnor2_2_net);
    AND2_117 : AND2
      port map(A => DataA(2), B => DataBXnor2_2_net, Y => 
        AND2_117_Y);
    XOR2_34 : XOR2
      port map(A => DataA(6), B => DataBXnor2_6_net, Y => 
        XOR2_34_Y);
    XOR2_31 : XOR2
      port map(A => DataA(20), B => DataBXnor2_20_net, Y => 
        XOR2_31_Y);
    AND2_3 : AND2
      port map(A => AND2_59_Y, B => AND2_25_Y, Y => AND2_3_Y);
    AO21_43 : AO21
      port map(A => AND2_120_Y, B => AO21_10_Y, C => AO21_33_Y, 
        Y => AO21_43_Y);
    AND2_30 : AND2
      port map(A => AND2_113_Y, B => AND2_93_Y, Y => AND2_30_Y);
    XNOR2_0_inst : XNOR2
      port map(A => DataB(0), B => AddsubAux_0_net, Y => 
        DataBXnor2_0_net);
    XNOR2_24_inst : XNOR2
      port map(A => DataB(24), B => AddsubAux_24_net, Y => 
        DataBXnor2_24_net);
    AND2_156 : AND2
      port map(A => AND2_0_Y, B => AND2_12_Y, Y => AND2_156_Y);
    AND2_150 : AND2
      port map(A => DataA(31), B => DataBXnor2_31_net, Y => 
        AND2_150_Y);
    AND2_14 : AND2
      port map(A => XOR2_54_Y, B => XOR2_3_Y, Y => AND2_14_Y);
    AND2_103 : AND2
      port map(A => XOR2_15_Y, B => AND2_125_Y, Y => AND2_103_Y);
    AO21_106 : AO21
      port map(A => AND2_71_Y, B => AO21_69_Y, C => AO21_95_Y, 
        Y => AO21_106_Y);
    XOR2_Sum_2_inst : XOR2
      port map(A => XOR2_11_Y, B => AO21_14_Y, Y => Sum(2));
    AND2_74 : AND2
      port map(A => DataA(12), B => DataBXnor2_12_net, Y => 
        AND2_74_Y);
    AND2_32 : AND2
      port map(A => AND2_79_Y, B => AND2_116_Y, Y => AND2_32_Y);
    AO21_120 : AO21
      port map(A => AND2_160_Y, B => AO21_88_Y, C => AO21_119_Y, 
        Y => AO21_120_Y);
    AO21_67 : AO21
      port map(A => AND2_85_Y, B => AO21_68_Y, C => AO21_97_Y, 
        Y => AO21_67_Y);
    XOR2_Sum_23_inst : XOR2
      port map(A => XOR2_39_Y, B => AO21_40_Y, Y => Sum(23));
    AND2_123 : AND2
      port map(A => XOR2_63_Y, B => XOR2_51_Y, Y => AND2_123_Y);
    AO21_82 : AO21
      port map(A => AND2_24_Y, B => AO21_80_Y, C => AO21_39_Y, 
        Y => AO21_82_Y);
    AO21_123 : AO21
      port map(A => AND2_72_Y, B => AO21_101_Y, C => AO21_75_Y, 
        Y => AO21_123_Y);
    XOR2_Sum_1_inst : XOR2
      port map(A => XOR2_0_Y, B => OR3_0_Y, Y => Sum(1));
    XOR2_46 : XOR2
      port map(A => DataA(15), B => DataBXnor2_15_net, Y => 
        XOR2_46_Y);
    XOR2_Sum_16_inst : XOR2
      port map(A => XOR2_52_Y, B => AO21_0_Y, Y => Sum(16));
    XNOR2_27_inst : XNOR2
      port map(A => DataB(27), B => AddsubAux_24_net, Y => 
        DataBXnor2_27_net);
    XOR2_Sum_7_inst : XOR2
      port map(A => XOR2_13_Y, B => AO21_78_Y, Y => Sum(7));
    AND2_157 : AND2
      port map(A => AND2_70_Y, B => AND2_77_Y, Y => AND2_157_Y);
    AND2_106 : AND2
      port map(A => DataA(3), B => DataBXnor2_3_net, Y => 
        AND2_106_Y);
    AND2_100 : AND2
      port map(A => AND2_110_Y, B => AND2_133_Y, Y => AND2_100_Y);
    AND2_26 : AND2
      port map(A => AND2_4_Y, B => AND2_85_Y, Y => AND2_26_Y);
    AND2_111 : AND2
      port map(A => AND2_114_Y, B => AND2_54_Y, Y => AND2_111_Y);
    XOR2_9 : XOR2
      port map(A => DataA(24), B => DataBXnor2_24_net, Y => 
        XOR2_9_Y);
    AND2_126 : AND2
      port map(A => DataA(4), B => DataBXnor2_4_net, Y => 
        AND2_126_Y);
    AND2_120 : AND2
      port map(A => XOR2_38_Y, B => XOR2_32_Y, Y => AND2_120_Y);
    AO21_55 : AO21
      port map(A => AND2_19_Y, B => AO21_108_Y, C => AO21_48_Y, 
        Y => AO21_55_Y);
    XOR2_59 : XOR2
      port map(A => DataA(17), B => DataBXnor2_17_net, Y => 
        XOR2_59_Y);
    AND2_23 : AND2
      port map(A => XOR2_30_Y, B => XOR2_35_Y, Y => AND2_23_Y);
    BFR_AddsubAux_0_inst : BFR
      port map(A => Addsub, Y => AddsubAux_0_net);
    AO21_83 : AO21
      port map(A => AND2_102_Y, B => AO21_105_Y, C => AO21_56_Y, 
        Y => AO21_83_Y);
    XOR2_5 : XOR2
      port map(A => DataA(26), B => DataBXnor2_26_net, Y => 
        XOR2_5_Y);
    AO21_75 : AO21
      port map(A => AND2_79_Y, B => AO21_5_Y, C => AO21_72_Y, 
        Y => AO21_75_Y);
    XNOR2_12_inst : XNOR2
      port map(A => DataB(12), B => AddsubAux_12_net, Y => 
        DataBXnor2_12_net);
    AO21_49 : AO21
      port map(A => AND2_73_Y, B => AO21_98_Y, C => AO21_117_Y, 
        Y => AO21_49_Y);
    AND2_107 : AND2
      port map(A => AND2_136_Y, B => AND2_120_Y, Y => AND2_107_Y);
    AND2_19 : AND2
      port map(A => XOR2_36_Y, B => XOR2_12_Y, Y => AND2_19_Y);
    AO21_37 : AO21
      port map(A => AND2_22_Y, B => AO21_15_Y, C => AO21_20_Y, 
        Y => AO21_37_Y);
    AO21_15 : AO21
      port map(A => AND2_142_Y, B => AO21_69_Y, C => AO21_86_Y, 
        Y => AO21_15_Y);
    AND2_79 : AND2
      port map(A => XOR2_28_Y, B => XOR2_30_Y, Y => AND2_79_Y);
    AND2_119 : AND2
      port map(A => DataA(26), B => DataBXnor2_26_net, Y => 
        AND2_119_Y);
    XOR2_22 : XOR2
      port map(A => DataA(2), B => DataBXnor2_2_net, Y => 
        XOR2_22_Y);
    AND2_127 : AND2
      port map(A => AND2_134_Y, B => AND2_89_Y, Y => AND2_127_Y);
    XOR2_13 : XOR2
      port map(A => DataA(7), B => DataBXnor2_7_net, Y => 
        XOR2_13_Y);
    AO21_94 : AO21
      port map(A => XOR2_38_Y, B => AND2_159_Y, C => AND2_128_Y, 
        Y => AO21_94_Y);
    XNOR2_6_inst : XNOR2
      port map(A => DataB(6), B => AddsubAux_6_net, Y => 
        DataBXnor2_6_net);
    AND2_151 : AND2
      port map(A => AND2_18_Y, B => AND2_113_Y, Y => AND2_151_Y);
    AND2_134 : AND2
      port map(A => AND2_49_Y, B => AND2_8_Y, Y => AND2_134_Y);
    AO21_5 : AO21
      port map(A => XOR2_14_Y, B => AND2_118_Y, C => AND2_63_Y, 
        Y => AO21_5_Y);
    XNOR2_18_inst : XNOR2
      port map(A => DataB(18), B => AddsubAux_18_net, Y => 
        DataBXnor2_18_net);
    AO21_24 : AO21
      port map(A => XOR2_63_Y, B => AND2_122_Y, C => AND2_83_Y, 
        Y => AO21_24_Y);
    AND2_51 : AND2
      port map(A => DataA(1), B => DataBXnor2_1_net, Y => 
        AND2_51_Y);
    XOR2_Sum_29_inst : XOR2
      port map(A => XOR2_24_Y, B => AO21_49_Y, Y => Sum(29));
    AO21_7 : AO21
      port map(A => AND2_127_Y, B => AO21_118_Y, C => AO21_99_Y, 
        Y => AO21_7_Y);
    AND2_64 : AND2
      port map(A => AND2_60_Y, B => AND2_13_Y, Y => AND2_64_Y);
    AO21_111 : AO21
      port map(A => XOR2_59_Y, B => AND2_76_Y, C => AND2_1_Y, 
        Y => AO21_111_Y);
    AND2_47 : AND2
      port map(A => DataA(8), B => DataBXnor2_8_net, Y => 
        AND2_47_Y);
    AO21_51 : AO21
      port map(A => AND2_20_Y, B => AO21_50_Y, C => AO21_92_Y, 
        Y => AO21_51_Y);
    AO21_65 : AO21
      port map(A => AND2_137_Y, B => AO21_110_Y, C => AO21_25_Y, 
        Y => AO21_65_Y);
    AO21_115 : AO21
      port map(A => AND2_23_Y, B => AO21_21_Y, C => AO21_108_Y, 
        Y => AO21_115_Y);
    XOR2_48 : XOR2
      port map(A => DataA(19), B => DataBXnor2_19_net, Y => 
        XOR2_48_Y);
    AO21_89 : AO21
      port map(A => AND2_94_Y, B => AO21_29_Y, C => AO21_55_Y, 
        Y => AO21_89_Y);
    AO21_1 : AO21
      port map(A => AND2_115_Y, B => AO21_14_Y, C => AO21_64_Y, 
        Y => AO21_1_Y);
    XOR2_Sum_14_inst : XOR2
      port map(A => XOR2_55_Y, B => AO21_15_Y, Y => Sum(14));
    AO21_71 : AO21
      port map(A => AND2_92_Y, B => AO21_103_Y, C => AO21_3_Y, 
        Y => AO21_71_Y);
    AO21_46 : AO21
      port map(A => XOR2_21_Y, B => AND2_81_Y, C => AND2_145_Y, 
        Y => AO21_46_Y);
    AND2_101 : AND2
      port map(A => AND2_53_Y, B => AND2_130_Y, Y => AND2_101_Y);
    AND2_159 : AND2
      port map(A => DataA(20), B => DataBXnor2_20_net, Y => 
        AND2_159_Y);
    AND2_16 : AND2
      port map(A => AND2_34_Y, B => AND2_22_Y, Y => AND2_16_Y);
    XOR2_Sum_18_inst : XOR2
      port map(A => XOR2_43_Y, B => AO21_1_Y, Y => Sum(18));
    XOR2_Sum_20_inst : XOR2
      port map(A => XOR2_49_Y, B => AO21_7_Y, Y => Sum(20));
    AND2_76 : AND2
      port map(A => DataA(16), B => DataBXnor2_16_net, Y => 
        AND2_76_Y);
    AO21_11 : AO21
      port map(A => AND2_57_Y, B => AO21_58_Y, C => AO21_41_Y, 
        Y => AO21_11_Y);
    AO21_114 : AO21
      port map(A => XOR2_54_Y, B => AND2_98_Y, C => AND2_158_Y, 
        Y => AO21_114_Y);
    AND2_121 : AND2
      port map(A => AND2_38_Y, B => AND2_73_Y, Y => AND2_121_Y);
    AO21_122 : AO21
      port map(A => AND2_70_Y, B => AO21_57_Y, C => AO21_114_Y, 
        Y => AO21_122_Y);
    AO21_101 : AO21
      port map(A => AND2_36_Y, B => AO21_46_Y, C => AO21_94_Y, 
        Y => AO21_101_Y);
    XNOR2_4_inst : XNOR2
      port map(A => DataB(4), B => AddsubAux_0_net, Y => 
        DataBXnor2_4_net);
    XOR2_62 : XOR2
      port map(A => DataA(28), B => DataBXnor2_28_net, Y => 
        XOR2_62_Y);
    AO21_50 : AO21
      port map(A => AND2_80_Y, B => AO21_70_Y, C => AO21_126_Y, 
        Y => AO21_50_Y);
    AND2_13 : AND2
      port map(A => AND2_89_Y, B => AND2_87_Y, Y => AND2_13_Y);
    AND2_87 : AND2
      port map(A => AND2_33_Y, B => AND2_32_Y, Y => AND2_87_Y);
    AO21_105 : AO21
      port map(A => XOR2_29_Y, B => AND2_106_Y, C => AND2_126_Y, 
        Y => AO21_105_Y);
    ADDERCO : XNOR2
      port map(A => addco, B => AddsubAux_29_net, Y => Cout);
    AND2_73 : AND2
      port map(A => AND2_7_Y, B => AND2_147_Y, Y => AND2_73_Y);
    XOR2_10 : XOR2
      port map(A => DataA(25), B => DataBXnor2_25_net, Y => 
        XOR2_10_Y);
    AO21_70 : AO21
      port map(A => XOR2_41_Y, B => AND2_74_Y, C => AND2_28_Y, 
        Y => AO21_70_Y);
    AND2_58 : AND2
      port map(A => DataA(7), B => DataBXnor2_7_net, Y => 
        AND2_58_Y);
    AND2_55 : AND2
      port map(A => AND2_93_Y, B => AND2_2_Y, Y => AND2_55_Y);
    AND2_109 : AND2
      port map(A => AND2_17_Y, B => AND2_88_Y, Y => AND2_109_Y);
    AO21_6 : AO21
      port map(A => AND2_123_Y, B => AO21_25_Y, C => AO21_91_Y, 
        Y => AO21_6_Y);
    AO21_10 : AO21
      port map(A => XOR2_31_Y, B => AND2_145_Y, C => AND2_159_Y, 
        Y => AO21_10_Y);
    AO21_104 : AO21
      port map(A => XOR2_47_Y, B => AND2_117_Y, C => AND2_106_Y, 
        Y => AO21_104_Y);
    AND2_69 : AND2
      port map(A => AND2_155_Y, B => AND2_21_Y, Y => AND2_69_Y);
    AO21_35 : AO21
      port map(A => AND2_153_Y, B => AO21_75_Y, C => AO21_122_Y, 
        Y => AO21_35_Y);
    AND2_129 : AND2
      port map(A => AND2_143_Y, B => AND2_136_Y, Y => AND2_129_Y);
    XOR2_27 : XOR2
      port map(A => DataA(10), B => DataBXnor2_10_net, Y => 
        XOR2_27_Y);
    AO21_119 : AO21
      port map(A => AND2_7_Y, B => AO21_8_Y, C => AO21_42_Y, Y => 
        AO21_119_Y);
    AO21_86 : AO21
      port map(A => AND2_15_Y, B => AO21_93_Y, C => AO21_116_Y, 
        Y => AO21_86_Y);
    AO21_61 : AO21
      port map(A => AND2_116_Y, B => AO21_72_Y, C => AO21_57_Y, 
        Y => AO21_61_Y);
    XOR2_7 : XOR2
      port map(A => DataA(5), B => DataBXnor2_5_net, Y => 
        XOR2_7_Y);
    AND2_5 : AND2
      port map(A => AND2_68_Y, B => AND2_31_Y, Y => AND2_5_Y);
    XOR2_56 : XOR2
      port map(A => DataA(16), B => DataBXnor2_16_net, Y => 
        XOR2_56_Y);
    XOR2_14 : XOR2
      port map(A => DataA(23), B => DataBXnor2_23_net, Y => 
        XOR2_14_Y);
    AND2_91 : AND2
      port map(A => AND2_78_Y, B => AND2_57_Y, Y => AND2_91_Y);
    AND2_50 : AND2
      port map(A => AND2_129_Y, B => AND2_131_Y, Y => AND2_50_Y);
    XOR2_11 : XOR2
      port map(A => DataA(2), B => DataBXnor2_2_net, Y => 
        XOR2_11_Y);
    XNOR2_Sum_0_inst : XNOR2
      port map(A => XOR2_4_Y, B => addci, Y => Sum(0));
    XOR2_Sum_22_inst : XOR2
      port map(A => XOR2_53_Y, B => AO21_106_Y, Y => Sum(22));
    XNOR2_15_inst : XNOR2
      port map(A => DataB(15), B => AddsubAux_12_net, Y => 
        DataBXnor2_15_net);
    AO21_98 : AO21
      port map(A => AND2_30_Y, B => AO21_88_Y, C => AO21_8_Y, 
        Y => AO21_98_Y);
    XOR2_25 : XOR2
      port map(A => DataA(30), B => DataBXnor2_30_net, Y => 
        XOR2_25_Y);
    AND2_52 : AND2
      port map(A => XOR2_47_Y, B => XOR2_29_Y, Y => AND2_52_Y);
    XOR2_Sum_11_inst : XOR2
      port map(A => XOR2_20_Y, B => AO21_112_Y, Y => Sum(11));
    XOR2_Sum_15_inst : XOR2
      port map(A => XOR2_57_Y, B => AO21_58_Y, Y => Sum(15));
    AO21_addco : AO21
      port map(A => AND2_141_Y, B => AO21_0_Y, C => AO21_13_Y, 
        Y => addco);
    AO21_28 : AO21
      port map(A => AND2_110_Y, B => AO21_65_Y, C => AO21_127_Y, 
        Y => AO21_28_Y);
    AO21_109 : AO21
      port map(A => AND2_5_Y, B => AO21_53_Y, C => AO21_93_Y, 
        Y => AO21_109_Y);
    AO21_60 : AO21
      port map(A => AND2_21_Y, B => AO21_112_Y, C => AO21_71_Y, 
        Y => AO21_60_Y);
    AO21_3 : AO21
      port map(A => AND2_17_Y, B => AO21_43_Y, C => AO21_115_Y, 
        Y => AO21_3_Y);
    AND2_112 : AND2
      port map(A => DataA(0), B => INV_0_Y, Y => AND2_112_Y);
    AND2_34 : AND2
      port map(A => AND2_161_Y, B => AND2_142_Y, Y => AND2_34_Y);
    AND2_66 : AND2
      port map(A => AND2_52_Y, B => AND2_102_Y, Y => AND2_66_Y);
    AO21_47 : AO21
      port map(A => AND2_54_Y, B => AO21_14_Y, C => AO21_109_Y, 
        Y => AO21_47_Y);
    AO21_31 : AO21
      port map(A => AND2_68_Y, B => AO21_85_Y, C => AO21_36_Y, 
        Y => AO21_31_Y);
    AND2_143 : AND2
      port map(A => XOR2_59_Y, B => XOR2_40_Y, Y => AND2_143_Y);
    AND2_98 : AND2
      port map(A => DataA(28), B => DataBXnor2_28_net, Y => 
        AND2_98_Y);
    AND2_95 : AND2
      port map(A => DataA(9), B => DataBXnor2_9_net, Y => 
        AND2_95_Y);
    XNOR2_1_inst : XNOR2
      port map(A => DataB(1), B => AddsubAux_0_net, Y => 
        DataBXnor2_1_net);
    XNOR2_22_inst : XNOR2
      port map(A => DataB(22), B => AddsubAux_18_net, Y => 
        DataBXnor2_22_net);
    AO21_117 : AO21
      port map(A => AND2_147_Y, B => AO21_42_Y, C => AO21_89_Y, 
        Y => AO21_117_Y);
    AND2_63 : AND2
      port map(A => DataA(23), B => DataBXnor2_23_net, Y => 
        AND2_63_Y);
    XOR2_32 : XOR2
      port map(A => DataA(22), B => DataBXnor2_22_net, Y => 
        XOR2_32_Y);
    OR3_0 : OR3
      port map(A => AND2_6_Y, B => AND2_112_Y, C => AND2_148_Y, 
        Y => OR3_0_Y);
    AND2_9 : AND2
      port map(A => AND2_20_Y, B => AND2_33_Y, Y => AND2_9_Y);
    AND2_146 : AND2
      port map(A => DataA(25), B => DataBXnor2_25_net, Y => 
        AND2_146_Y);
    AND2_140 : AND2
      port map(A => XOR2_41_Y, B => XOR2_45_Y, Y => AND2_140_Y);
    AO21_30 : AO21
      port map(A => AND2_67_Y, B => AO21_126_Y, C => AO21_111_Y, 
        Y => AO21_30_Y);
    AND2_90 : AND2
      port map(A => AND2_0_Y, B => AND2_66_Y, Y => AND2_90_Y);
    XOR2_58 : XOR2
      port map(A => DataA(3), B => DataBXnor2_3_net, Y => 
        XOR2_58_Y);
    AO21_107 : AO21
      port map(A => AND2_25_Y, B => AO21_30_Y, C => AO21_101_Y, 
        Y => AO21_107_Y);
    AND2_152 : AND2
      port map(A => AND2_75_Y, B => AND2_160_Y, Y => AND2_152_Y);
    XOR2_43 : XOR2
      port map(A => DataA(18), B => DataBXnor2_18_net, Y => 
        XOR2_43_Y);
    XNOR2_28_inst : XNOR2
      port map(A => DataB(28), B => AddsubAux_24_net, Y => 
        DataBXnor2_28_net);
    XNOR2_3_inst : XNOR2
      port map(A => DataB(3), B => AddsubAux_0_net, Y => 
        DataBXnor2_3_net);
    AND2_92 : AND2
      port map(A => AND2_107_Y, B => AND2_17_Y, Y => AND2_92_Y);
    AO21_87 : AO21
      port map(A => AND2_130_Y, B => AO21_66_Y, C => AO21_123_Y, 
        Y => AO21_87_Y);
    AND2_39 : AND2
      port map(A => XOR2_37_Y, B => XOR2_22_Y, Y => AND2_39_Y);
    XOR2_Sum_31_inst : XOR2
      port map(A => XOR2_19_Y, B => AO21_11_Y, Y => Sum(31));
    AND2_147 : AND2
      port map(A => AND2_131_Y, B => AND2_94_Y, Y => AND2_147_Y);
    AND2_115 : AND2
      port map(A => AND2_54_Y, B => AND2_53_Y, Y => AND2_115_Y);
    AND2_102 : AND2
      port map(A => XOR2_23_Y, B => XOR2_34_Y, Y => AND2_102_Y);
    AND2_27 : AND2
      port map(A => DataA(14), B => DataBXnor2_14_net, Y => 
        AND2_27_Y);
    AND2_122 : AND2
      port map(A => DataA(10), B => DataBXnor2_10_net, Y => 
        AND2_122_Y);
    AO21_45 : AO21
      port map(A => AND2_8_Y, B => AO21_31_Y, C => AO21_73_Y, 
        Y => AO21_45_Y);
    XOR2_29 : XOR2
      port map(A => DataA(4), B => DataBXnor2_4_net, Y => 
        XOR2_29_Y);
    XOR2_40 : XOR2
      port map(A => DataA(18), B => DataBXnor2_18_net, Y => 
        XOR2_40_Y);
    XOR2_2 : XOR2
      port map(A => DataA(6), B => DataBXnor2_6_net, Y => 
        XOR2_2_Y);
    AND2_155 : AND2
      port map(A => AND2_0_Y, B => AND2_138_Y, Y => AND2_155_Y);
    XOR2_37 : XOR2
      port map(A => DataA(1), B => DataBXnor2_1_net, Y => 
        XOR2_37_Y);
    AND2_36 : AND2
      port map(A => XOR2_31_Y, B => XOR2_38_Y, Y => AND2_36_Y);
    AND2_141 : AND2
      port map(A => AND2_9_Y, B => AND2_104_Y, Y => AND2_141_Y);
    XOR2_Sum_6_inst : XOR2
      port map(A => XOR2_2_Y, B => AO21_69_Y, Y => Sum(6));
    AND2_33 : AND2
      port map(A => AND2_36_Y, B => AND2_40_Y, Y => AND2_33_Y);
    XOR2_44 : XOR2
      port map(A => DataA(21), B => DataBXnor2_21_net, Y => 
        XOR2_44_Y);
    AO21_92 : AO21
      port map(A => AND2_29_Y, B => AO21_111_Y, C => AO21_46_Y, 
        Y => AO21_92_Y);
    XNOR2_25_inst : XNOR2
      port map(A => DataB(25), B => AddsubAux_24_net, Y => 
        DataBXnor2_25_net);
    AND2_133 : AND2
      port map(A => AND2_24_Y, B => AND2_143_Y, Y => AND2_133_Y);
    XOR2_41 : XOR2
      port map(A => DataA(13), B => DataBXnor2_13_net, Y => 
        XOR2_41_Y);
    XOR2_35 : XOR2
      port map(A => DataA(26), B => DataBXnor2_26_net, Y => 
        XOR2_35_Y);
    AND2_105 : AND2
      port map(A => DataA(5), B => DataBXnor2_5_net, Y => 
        AND2_105_Y);
    AO21_22 : AO21
      port map(A => AND2_77_Y, B => AO21_114_Y, C => AO21_74_Y, 
        Y => AO21_22_Y);
    AO21_4 : AO21
      port map(A => AND2_56_Y, B => AO21_73_Y, C => AO21_50_Y, 
        Y => AO21_4_Y);
    AND2_160 : AND2
      port map(A => AND2_30_Y, B => AND2_7_Y, Y => AND2_160_Y);
    AND2_149 : AND2
      port map(A => AND2_161_Y, B => AND2_71_Y, Y => AND2_149_Y);
    AO21_85 : AO21
      port map(A => XOR2_23_Y, B => AND2_126_Y, C => AND2_105_Y, 
        Y => AO21_85_Y);
    AND2_125 : AND2
      port map(A => AND2_151_Y, B => AND2_55_Y, Y => AND2_125_Y);
    AO21_41 : AO21
      port map(A => AND2_109_Y, B => AO21_77_Y, C => AO21_27_Y, 
        Y => AO21_41_Y);
    AO21_54 : AO21
      port map(A => AND2_32_Y, B => AO21_17_Y, C => AO21_61_Y, 
        Y => AO21_54_Y);
    AND2_41 : AND2
      port map(A => XOR2_51_Y, B => XOR2_41_Y, Y => AND2_41_Y);
    AND2_0 : AND2
      port map(A => XOR2_15_Y, B => AND2_39_Y, Y => AND2_0_Y);
    XOR2_Sum_27_inst : XOR2
      port map(A => XOR2_16_Y, B => AO21_60_Y, Y => Sum(27));
    AND2_136 : AND2
      port map(A => XOR2_21_Y, B => XOR2_31_Y, Y => AND2_136_Y);
    AND2_130 : AND2
      port map(A => AND2_25_Y, B => AND2_72_Y, Y => AND2_130_Y);
    AND2_17 : AND2
      port map(A => AND2_139_Y, B => AND2_23_Y, Y => AND2_17_Y);
    AO21_93 : AO21
      port map(A => AND2_31_Y, B => AO21_36_Y, C => AO21_121_Y, 
        Y => AO21_93_Y);
    XOR2_6 : XOR2
      port map(A => DataA(4), B => DataBXnor2_4_net, Y => 
        XOR2_6_Y);
    AO21_74 : AO21
      port map(A => XOR2_18_Y, B => AND2_11_Y, C => AND2_150_Y, 
        Y => AO21_74_Y);
    AND2_77 : AND2
      port map(A => XOR2_3_Y, B => XOR2_18_Y, Y => AND2_77_Y);
    AND2_54 : AND2
      port map(A => AND2_132_Y, B => AND2_5_Y, Y => AND2_54_Y);
    AO21_23 : AO21
      port map(A => AND2_144_Y, B => AO21_56_Y, C => AO21_110_Y, 
        Y => AO21_23_Y);
    AO21_14 : AO21
      port map(A => XOR2_37_Y, B => OR3_0_Y, C => AND2_51_Y, Y => 
        AO21_14_Y);
    AO21_118 : AO21
      port map(A => AND2_124_Y, B => AO21_14_Y, C => AO21_104_Y, 
        Y => AO21_118_Y);
    AO21_40 : AO21
      port map(A => AND2_48_Y, B => AO21_78_Y, C => AO21_113_Y, 
        Y => AO21_40_Y);
    XOR2_53 : XOR2
      port map(A => DataA(22), B => DataBXnor2_22_net, Y => 
        XOR2_53_Y);
    AND2_137 : AND2
      port map(A => XOR2_42_Y, B => XOR2_50_Y, Y => AND2_137_Y);
    AO21_8 : AO21
      port map(A => AND2_93_Y, B => AO21_23_Y, C => AO21_6_Y, 
        Y => AO21_8_Y);
    AND2_81 : AND2
      port map(A => DataA(18), B => DataBXnor2_18_net, Y => 
        AND2_81_Y);
    AO21_110 : AO21
      port map(A => XOR2_1_Y, B => AND2_58_Y, C => AND2_47_Y, 
        Y => AO21_110_Y);
    XOR2_12 : XOR2
      port map(A => DataA(28), B => DataBXnor2_28_net, Y => 
        XOR2_12_Y);
    AO21_81 : AO21
      port map(A => XOR2_3_Y, B => AND2_158_Y, C => AND2_11_Y, 
        Y => AO21_81_Y);
    AND2_48 : AND2
      port map(A => AND2_10_Y, B => AND2_61_Y, Y => AND2_48_Y);
    AND2_45 : AND2
      port map(A => AND2_82_Y, B => AND2_49_Y, Y => AND2_45_Y);
    AO21_113 : AO21
      port map(A => AND2_61_Y, B => AO21_28_Y, C => AO21_77_Y, 
        Y => AO21_113_Y);
    AO21_108 : AO21
      port map(A => XOR2_35_Y, B => AND2_146_Y, C => AND2_119_Y, 
        Y => AO21_108_Y);
    XNOR2_30_inst : XNOR2
      port map(A => DataB(30), B => AddsubAux_29_net, Y => 
        DataBXnor2_30_net);
    AO21_99 : AO21
      port map(A => AND2_89_Y, B => AO21_45_Y, C => AO21_51_Y, 
        Y => AO21_99_Y);
    AO21_64 : AO21
      port map(A => AND2_53_Y, B => AO21_109_Y, C => AO21_66_Y, 
        Y => AO21_64_Y);
    XOR2_26 : XOR2
      port map(A => DataA(9), B => DataBXnor2_9_net, Y => 
        XOR2_26_Y);
    XNOR2_10_inst : XNOR2
      port map(A => DataB(10), B => AddsubAux_6_net, Y => 
        DataBXnor2_10_net);
    AND2_59 : AND2
      port map(A => AND2_80_Y, B => AND2_67_Y, Y => AND2_59_Y);
    AO21_29 : AO21
      port map(A => AND2_139_Y, B => AO21_33_Y, C => AO21_21_Y, 
        Y => AO21_29_Y);
    AND2_161 : AND2
      port map(A => AND2_114_Y, B => AND2_132_Y, Y => AND2_161_Y);
    XOR2_Sum_13_inst : XOR2
      port map(A => XOR2_8_Y, B => AO21_98_Y, Y => Sum(13));
    AO21_100 : AO21
      port map(A => AND2_108_Y, B => AO21_83_Y, C => AO21_65_Y, 
        Y => AO21_100_Y);
    AND2_4 : AND2
      port map(A => XOR2_15_Y, B => AND2_151_Y, Y => AND2_4_Y);
    AO21_80 : AO21
      port map(A => XOR2_45_Y, B => AND2_28_Y, C => AND2_27_Y, 
        Y => AO21_80_Y);
    AND2_40 : AND2
      port map(A => XOR2_32_Y, B => XOR2_14_Y, Y => AND2_40_Y);
    AO21_103 : AO21
      port map(A => AND2_133_Y, B => AO21_127_Y, C => AO21_19_Y, 
        Y => AO21_103_Y);
    AND2_88 : AND2
      port map(A => AND2_19_Y, B => AND2_14_Y, Y => AND2_88_Y);
    AND2_85 : AND2
      port map(A => AND2_55_Y, B => AND2_50_Y, Y => AND2_85_Y);
    XOR2_Sum_9_inst : XOR2
      port map(A => XOR2_26_Y, B => AO21_68_Y, Y => Sum(9));
    XOR2_50 : XOR2
      port map(A => DataA(10), B => DataBXnor2_10_net, Y => 
        XOR2_50_Y);
    AND2_131 : AND2
      port map(A => AND2_120_Y, B => AND2_139_Y, Y => AND2_131_Y);
    BFR_AddsubAux_29_inst : BFR
      port map(A => Addsub, Y => AddsubAux_29_net);
    AND2_94 : AND2
      port map(A => AND2_23_Y, B => AND2_19_Y, Y => AND2_94_Y);
    AND2_42 : AND2
      port map(A => AND2_84_Y, B => AND2_9_Y, Y => AND2_42_Y);
    XNOR2_19_inst : XNOR2
      port map(A => DataB(19), B => AddsubAux_18_net, Y => 
        DataBXnor2_19_net);
    AND2_118 : AND2
      port map(A => DataA(22), B => DataBXnor2_22_net, Y => 
        AND2_118_Y);
    XNOR2_16_inst : XNOR2
      port map(A => DataB(16), B => AddsubAux_12_net, Y => 
        DataBXnor2_16_net);
    AND2_67 : AND2
      port map(A => XOR2_56_Y, B => XOR2_59_Y, Y => AND2_67_Y);
    AO21_58 : AO21
      port map(A => AND2_10_Y, B => AO21_78_Y, C => AO21_28_Y, 
        Y => AO21_58_Y);
    BFR_AddsubAux_18_inst : BFR
      port map(A => Addsub, Y => AddsubAux_18_net);
    XOR2_39 : XOR2
      port map(A => DataA(23), B => DataBXnor2_23_net, Y => 
        XOR2_39_Y);
    AND2_8 : AND2
      port map(A => AND2_31_Y, B => AND2_37_Y, Y => AND2_8_Y);
    AO21_78 : AO21
      port map(A => AND2_66_Y, B => AO21_9_Y, C => AO21_83_Y, 
        Y => AO21_78_Y);
    AND2_80 : AND2
      port map(A => XOR2_45_Y, B => XOR2_46_Y, Y => AND2_80_Y);
    XOR2_3 : XOR2
      port map(A => DataA(30), B => DataBXnor2_30_net, Y => 
        XOR2_3_Y);
    AO21_34 : AO21
      port map(A => AND2_125_Y, B => OR3_0_Y, C => AO21_124_Y, 
        Y => AO21_34_Y);
    XOR2_54 : XOR2
      port map(A => DataA(29), B => DataBXnor2_29_net, Y => 
        XOR2_54_Y);
    AND2_139 : AND2
      port map(A => XOR2_14_Y, B => XOR2_28_Y, Y => AND2_139_Y);
    XOR2_51 : XOR2
      port map(A => DataA(12), B => DataBXnor2_12_net, Y => 
        XOR2_51_Y);
    AO21_96 : AO21
      port map(A => AND2_157_Y, B => AO21_61_Y, C => AO21_22_Y, 
        Y => AO21_96_Y);
    AND2_82 : AND2
      port map(A => AND2_114_Y, B => AND2_124_Y, Y => AND2_82_Y);
    AO21_18 : AO21
      port map(A => AND2_12_Y, B => AO21_9_Y, C => AO21_32_Y, 
        Y => AO21_18_Y);
    AND2_142 : AND2
      port map(A => AND2_5_Y, B => AND2_15_Y, Y => AND2_142_Y);
    XNOR2_7_inst : XNOR2
      port map(A => DataB(7), B => AddsubAux_6_net, Y => 
        DataBXnor2_7_net);
    AND2_56 : AND2
      port map(A => AND2_41_Y, B => AND2_80_Y, Y => AND2_56_Y);
    AO21_26 : AO21
      port map(A => AND2_13_Y, B => AO21_84_Y, C => AO21_59_Y, 
        Y => AO21_26_Y);
    AO21_126 : AO21
      port map(A => XOR2_46_Y, B => AND2_27_Y, C => AND2_96_Y, 
        Y => AO21_126_Y);
    XOR2_17 : XOR2
      port map(A => DataA(7), B => DataBXnor2_7_net, Y => 
        XOR2_17_Y);
    AND2_158 : AND2
      port map(A => DataA(29), B => DataBXnor2_29_net, Y => 
        AND2_158_Y);
    AND2_53 : AND2
      port map(A => AND2_15_Y, B => AND2_59_Y, Y => AND2_53_Y);
    AND2_99 : AND2
      port map(A => AND2_114_Y, B => AND2_115_Y, Y => AND2_99_Y);
    XOR2_28 : XOR2
      port map(A => DataA(24), B => DataBXnor2_24_net, Y => 
        XOR2_28_Y);
    XOR2_Sum_19_inst : XOR2
      port map(A => XOR2_48_Y, B => AO21_18_Y, Y => Sum(19));
    BFR_AddsubAux_12_inst : BFR
      port map(A => Addsub, Y => AddsubAux_12_net);
    XOR2_Sum_26_inst : XOR2
      port map(A => XOR2_5_Y, B => AO21_44_Y, Y => Sum(26));
    XOR2_15 : XOR2
      port map(A => DataA(0), B => DataBXnor2_0_net, Y => 
        XOR2_15_Y);
    AO21_112 : AO21
      port map(A => AND2_138_Y, B => AO21_9_Y, C => AO21_100_Y, 
        Y => AO21_112_Y);
    AO21_68 : AO21
      port map(A => AND2_151_Y, B => OR3_0_Y, C => AO21_38_Y, 
        Y => AO21_68_Y);
    AND2_108 : AND2
      port map(A => AND2_144_Y, B => AND2_137_Y, Y => AND2_108_Y);
    XNOR2_5_inst : XNOR2
      port map(A => DataB(5), B => AddsubAux_0_net, Y => 
        DataBXnor2_5_net);
    XOR2_Sum_3_inst : XOR2
      port map(A => XOR2_58_Y, B => AO21_9_Y, Y => Sum(3));
    BFR_AddsubAux_6_inst : BFR
      port map(A => Addsub, Y => AddsubAux_6_net);
    AND2_128 : AND2
      port map(A => DataA(21), B => DataBXnor2_21_net, Y => 
        AND2_128_Y);
    XOR2_Sum_10_inst : XOR2
      port map(A => XOR2_27_Y, B => AO21_47_Y, Y => Sum(10));
    AND2_21 : AND2
      port map(A => AND2_100_Y, B => AND2_92_Y, Y => AND2_21_Y);
    AO21_102 : AO21
      port map(A => XOR2_40_Y, B => AND2_1_Y, C => AND2_81_Y, 
        Y => AO21_102_Y);
    AND2_145 : AND2
      port map(A => DataA(19), B => DataBXnor2_19_net, Y => 
        AND2_145_Y);
    XOR2_0 : XOR2
      port map(A => DataA(1), B => DataBXnor2_1_net, Y => 
        XOR2_0_Y);
    AND2_96 : AND2
      port map(A => DataA(15), B => DataBXnor2_15_net, Y => 
        AND2_96_Y);
    AND2_37 : AND2
      port map(A => XOR2_50_Y, B => XOR2_63_Y, Y => AND2_37_Y);
    AO21_38 : AO21
      port map(A => AND2_113_Y, B => AO21_2_Y, C => AO21_23_Y, 
        Y => AO21_38_Y);
    AND2_93 : AND2
      port map(A => AND2_137_Y, B => AND2_123_Y, Y => AND2_93_Y);
    AO21_97 : AO21
      port map(A => AND2_50_Y, B => AO21_16_Y, C => AO21_12_Y, 
        Y => AO21_97_Y);
    XOR2_42 : XOR2
      port map(A => DataA(9), B => DataBXnor2_9_net, Y => 
        XOR2_42_Y);
    AND2_114 : AND2
      port map(A => XOR2_15_Y, B => XOR2_37_Y, Y => AND2_114_Y);
    XOR2_36 : XOR2
      port map(A => DataA(27), B => DataBXnor2_27_net, Y => 
        XOR2_36_Y);
    AO21_27 : AO21
      port map(A => AND2_88_Y, B => AO21_115_Y, C => AO21_90_Y, 
        Y => AO21_27_Y);
    XNOR2_20_inst : XNOR2
      port map(A => DataB(20), B => AddsubAux_18_net, Y => 
        DataBXnor2_20_net);
    AND2_28 : AND2
      port map(A => DataA(13), B => DataBXnor2_13_net, Y => 
        AND2_28_Y);
    AND2_25 : AND2
      port map(A => AND2_29_Y, B => AND2_36_Y, Y => AND2_25_Y);
    AND2_162 : AND2
      port map(A => XOR2_29_Y, B => XOR2_23_Y, Y => AND2_162_Y);
    XOR2_Sum_12_inst : XOR2
      port map(A => XOR2_33_Y, B => AO21_84_Y, Y => Sum(12));
    XOR2_Sum_24_inst : XOR2
      port map(A => XOR2_9_Y, B => AO21_52_Y, Y => Sum(24));
    XOR2_Sum_28_inst : XOR2
      port map(A => XOR2_62_Y, B => AO21_26_Y, Y => Sum(28));
end DEF_ARCH;

library IEEE;
use IEEE.std_logic_1164.all;

entity ramdp is

   port(DO : out std_logic_vector (31 downto 0);
      WCLOCK : in std_logic;
      DI : in std_logic_vector (31 downto 0);
      PO : out std_logic_vector (3 downto 0);
      PI : in std_logic_vector (3 downto 0);
      WRB : in std_logic;
      RDB : in std_logic;
      WADDR : in std_logic_vector (3 downto 0);
      RADDR : in std_logic_vector (3 downto 0);
      WPE : out std_logic;
      RPE : out std_logic);

end ramdp;

architecture STRUCT_ramdp of ramdp is
component PWR
   port(Y : out std_logic);
end component;

component GND
   port(Y : out std_logic);
end component;

component RAM256x9SA
   port(WCLKS : in std_logic;
      DO8 : out std_logic;
      DO7 : out std_logic;
      DO6 : out std_logic;
      DO5 : out std_logic;
      DO4 : out std_logic;
      DO3 : out std_logic;
      DO2 : out std_logic;
      DO1 : out std_logic;
      DO0 : out std_logic;
      DOS : out std_logic;
      WPE : out std_logic;
      RPE : out std_logic;
      WADDR7 : in std_logic;
      WADDR6 : in std_logic;
      WADDR5 : in std_logic;
      WADDR4 : in std_logic;
      WADDR3 : in std_logic;
      WADDR2 : in std_logic;
      WADDR1 : in std_logic;
      WADDR0 : in std_logic;
      RADDR7 : in std_logic;
      RADDR6 : in std_logic;
      RADDR5 : in std_logic;
      RADDR4 : in std_logic;
      RADDR3 : in std_logic;
      RADDR2 : in std_logic;
      RADDR1 : in std_logic;
      RADDR0 : in std_logic;
      DI8 : in std_logic;
      DI7 : in std_logic;
      DI6 : in std_logic;
      DI5 : in std_logic;
      DI4 : in std_logic;
      DI3 : in std_logic;
      DI2 : in std_logic;
      DI1 : in std_logic;
      DI0 : in std_logic;
      WRB : in std_logic;
      RDB : in std_logic;
      WBLKB : in std_logic;
      RBLKB : in std_logic;
      PARODD : in std_logic;
      DIS : in std_logic);
end component;

component OR3
   port(Y : out std_logic;
      A : in std_logic;
      B : in std_logic;
      C : in std_logic);
end component;

component OR2
   port(Y : out std_logic;
      A : in std_logic;
      B : in std_logic);
end component;

signal net00000, net00001, net00002, net00003, net00004, net00005, net00006, net00007, 
      net00008, net00009, net00010, net00011, net00012, net00013, net00014, net00015, 
      net00016, net00017, net00018, net00019, net00020, net00021, net00022, net00023, 
      net00024, net00025, net00026, net00027, net00028, net00029, net00030, net00031, 
      net00032, net00033, net00034, net00035, net00036, net00037, net00038, net00039, 
      net00040, net00041, net00042, net00043, net00044, net00045, net00046, net00047, 
      net00048 : std_logic;

begin

   U1 : GND port map(Y => net00001);
   M0 : RAM256x9SA port map(WCLKS => WCLOCK, DO8 => PO(0), DO7 => DO(7), DO6 => DO(6), DO5 => DO(5), 
      DO4 => DO(4), DO3 => DO(3), DO2 => DO(2), DO1 => DO(1), DO0 => DO(0), 
      DOS => net00045, WPE => net00035, RPE => net00040, WADDR7 => net00001, WADDR6 => net00001, WADDR5 => net00001, 
      WADDR4 => net00001, WADDR3 => WADDR(3), WADDR2 => WADDR(2), WADDR1 => WADDR(1), 
      WADDR0 => WADDR(0), RADDR7 => net00001, RADDR6 => net00001, RADDR5 => net00001, RADDR4 => net00001, 
      RADDR3 => RADDR(3), RADDR2 => RADDR(2), RADDR1 => RADDR(1), RADDR0 => RADDR(0), 
      DI8 => PI(0), DI7 => DI(7), DI6 => DI(6), DI5 => DI(5), DI4 => DI(4), 
      DI3 => DI(3), DI2 => DI(2), DI1 => DI(1), DI0 => DI(0), WRB => WRB, RDB => RDB, 
      WBLKB => net00001, RBLKB => net00001, PARODD => net00001, DIS => net00001);
   M1 : RAM256x9SA port map(WCLKS => WCLOCK, DO8 => PO(1), DO7 => DO(15), DO6 => DO(14), DO5 => DO(13), 
      DO4 => DO(12), DO3 => DO(11), DO2 => DO(10), DO1 => DO(9), DO0 => DO(8), 
      DOS => net00046, WPE => net00036, RPE => net00041, WADDR7 => net00001, WADDR6 => net00001, WADDR5 => net00001, 
      WADDR4 => net00001, WADDR3 => WADDR(3), WADDR2 => WADDR(2), WADDR1 => WADDR(1), 
      WADDR0 => WADDR(0), RADDR7 => net00001, RADDR6 => net00001, RADDR5 => net00001, RADDR4 => net00001, 
      RADDR3 => RADDR(3), RADDR2 => RADDR(2), RADDR1 => RADDR(1), RADDR0 => RADDR(0), 
      DI8 => PI(1), DI7 => DI(15), DI6 => DI(14), DI5 => DI(13), DI4 => DI(12), 
      DI3 => DI(11), DI2 => DI(10), DI1 => DI(9), DI0 => DI(8), WRB => WRB, RDB => RDB, 
      WBLKB => net00001, RBLKB => net00001, PARODD => net00001, DIS => net00001);
   M2 : RAM256x9SA port map(WCLKS => WCLOCK, DO8 => PO(2), DO7 => DO(23), DO6 => DO(22), DO5 => DO(21), 
      DO4 => DO(20), DO3 => DO(19), DO2 => DO(18), DO1 => DO(17), DO0 => DO(16), 
      DOS => net00047, WPE => net00037, RPE => net00042, WADDR7 => net00001, WADDR6 => net00001, WADDR5 => net00001, 
      WADDR4 => net00001, WADDR3 => WADDR(3), WADDR2 => WADDR(2), WADDR1 => WADDR(1), 
      WADDR0 => WADDR(0), RADDR7 => net00001, RADDR6 => net00001, RADDR5 => net00001, RADDR4 => net00001, 
      RADDR3 => RADDR(3), RADDR2 => RADDR(2), RADDR1 => RADDR(1), RADDR0 => RADDR(0), 
      DI8 => PI(2), DI7 => DI(23), DI6 => DI(22), DI5 => DI(21), DI4 => DI(20), 
      DI3 => DI(19), DI2 => DI(18), DI1 => DI(17), DI0 => DI(16), WRB => WRB, RDB => RDB, 
      WBLKB => net00001, RBLKB => net00001, PARODD => net00001, DIS => net00001);
   M3 : RAM256x9SA port map(WCLKS => WCLOCK, DO8 => PO(3), DO7 => DO(31), DO6 => DO(30), DO5 => DO(29), 
      DO4 => DO(28), DO3 => DO(27), DO2 => DO(26), DO1 => DO(25), DO0 => DO(24), 
      DOS => net00048, WPE => net00038, RPE => net00043, WADDR7 => net00001, WADDR6 => net00001, WADDR5 => net00001, 
      WADDR4 => net00001, WADDR3 => WADDR(3), WADDR2 => WADDR(2), WADDR1 => WADDR(1), 
      WADDR0 => WADDR(0), RADDR7 => net00001, RADDR6 => net00001, RADDR5 => net00001, RADDR4 => net00001, 
      RADDR3 => RADDR(3), RADDR2 => RADDR(2), RADDR1 => RADDR(1), RADDR0 => RADDR(0), 
      DI8 => PI(3), DI7 => DI(31), DI6 => DI(30), DI5 => DI(29), DI4 => DI(28), 
      DI3 => DI(27), DI2 => DI(26), DI1 => DI(25), DI0 => DI(24), WRB => WRB, RDB => RDB, 
      WBLKB => net00001, RBLKB => net00001, PARODD => net00001, DIS => net00001);
   U2 : OR3 port map(Y => net00039, A => net00037, B => net00036, C => net00035);
   U3 : OR2 port map(Y => WPE, A => net00038, B => net00039);
   U4 : OR3 port map(Y => net00044, A => net00042, B => net00041, C => net00040);
   U5 : OR2 port map(Y => RPE, A => net00043, B => net00044);

end STRUCT_ramdp;

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

entity syncram8 is

   port(DO : out std_logic_vector (7 downto 0);
      RCLOCK : in std_logic;
      WCLOCK : in std_logic;
      DI : in std_logic_vector (7 downto 0);
      PO : out std_logic;
      PI : in std_logic;
      WRB : in std_logic;
      RDB : in std_logic;
      WADDR : in std_logic_vector (7 downto 0);
      RADDR : in std_logic_vector (7 downto 0);
      WPE : out std_logic;
      RPE : out std_logic);

end syncram8;

architecture STRUCT_syncram8 of syncram8 is
component PWR
   port(Y : out std_logic);
end component;

component GND
   port(Y : out std_logic);
end component;

component RAM256x9SST
   port(RCLKS : in std_logic;
      WCLKS : in std_logic;
      DO8 : out std_logic;
      DO7 : out std_logic;
      DO6 : out std_logic;
      DO5 : out std_logic;
      DO4 : out std_logic;
      DO3 : out std_logic;
      DO2 : out std_logic;
      DO1 : out std_logic;
      DO0 : out std_logic;
      DOS : out std_logic;
      WPE : out std_logic;
      RPE : out std_logic;
      WADDR7 : in std_logic;
      WADDR6 : in std_logic;
      WADDR5 : in std_logic;
      WADDR4 : in std_logic;
      WADDR3 : in std_logic;
      WADDR2 : in std_logic;
      WADDR1 : in std_logic;
      WADDR0 : in std_logic;
      RADDR7 : in std_logic;
      RADDR6 : in std_logic;
      RADDR5 : in std_logic;
      RADDR4 : in std_logic;
      RADDR3 : in std_logic;
      RADDR2 : in std_logic;
      RADDR1 : in std_logic;
      RADDR0 : in std_logic;
      DI8 : in std_logic;
      DI7 : in std_logic;
      DI6 : in std_logic;
      DI5 : in std_logic;
      DI4 : in std_logic;
      DI3 : in std_logic;
      DI2 : in std_logic;
      DI1 : in std_logic;
      DI0 : in std_logic;
      WRB : in std_logic;
      RDB : in std_logic;
      WBLKB : in std_logic;
      RBLKB : in std_logic;
      PARODD : in std_logic;
      DIS : in std_logic);
end component;

signal net00000, net00001, net00002, net00003, net00004, net00005, net00006, net00007, 
      net00008, net00009, net00010, net00011 : std_logic;

begin

   U1 : GND port map(Y => net00001);
   M0 : RAM256x9SST port map(RCLKS => RCLOCK, WCLKS => WCLOCK, DO8 => PO, DO7 => DO(7), DO6 => DO(6), 
      DO5 => DO(5), DO4 => DO(4), DO3 => DO(3), DO2 => DO(2), DO1 => DO(1), 
      DO0 => DO(0), DOS => net00011, WPE => WPE, RPE => RPE, WADDR7 => WADDR(7), WADDR6 => WADDR(6), 
      WADDR5 => WADDR(5), WADDR4 => WADDR(4), WADDR3 => WADDR(3), WADDR2 => WADDR(2), 
      WADDR1 => WADDR(1), WADDR0 => WADDR(0), RADDR7 => RADDR(7), RADDR6 => RADDR(6), 
      RADDR5 => RADDR(5), RADDR4 => RADDR(4), RADDR3 => RADDR(3), RADDR2 => RADDR(2), 
      RADDR1 => RADDR(1), RADDR0 => RADDR(0), DI8 => PI, DI7 => DI(7), DI6 => DI(6), 
      DI5 => DI(5), DI4 => DI(4), DI3 => DI(3), DI2 => DI(2), DI1 => DI(1), 
      DI0 => DI(0), WRB => WRB, RDB => RDB, WBLKB => net00001, RBLKB => net00001, PARODD => net00001, 
      DIS => net00001);

end STRUCT_syncram8;

library ieee;
use ieee.std_logic_1164.all;
library APA;

entity mux41_actel is 
    port( Data0_port : in std_logic_vector(31 downto 0); 
        Data1_port : in std_logic_vector(31 downto 0); Data2_port : 
        in std_logic_vector(31 downto 0); Data3_port : in 
        std_logic_vector(31 downto 0);Sel0, Sel1 : in std_logic; 
        Result : out std_logic_vector(31 downto 0)) ;
end mux41_actel;


architecture DEF_ARCH of  mux41_actel is

    component MUX2H
        port(A, B, S : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component BFR
        port(A : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    signal SelAux_0_0_net, SelAux_0_6_net, SelAux_0_12_net, 
        SelAux_0_17_net, SelAux_0_22_net, SelAux_0_27_net, 
        SelAux_0_32_net, SelAux_0_38_net, SelAux_0_44_net, 
        SelAux_0_49_net, SelAux_0_54_net, SelAux_0_59_net, 
        SelAux_1_0_net, SelAux_1_6_net, SelAux_1_12_net, 
        SelAux_1_17_net, SelAux_1_22_net, SelAux_1_27_net, 
        MUX2H_25_Y, MUX2H_31_Y, MUX2H_30_Y, MUX2H_46_Y, MUX2H_5_Y, 
        MUX2H_29_Y, MUX2H_44_Y, MUX2H_42_Y, MUX2H_50_Y, 
        MUX2H_24_Y, MUX2H_48_Y, MUX2H_53_Y, MUX2H_14_Y, 
        MUX2H_40_Y, MUX2H_27_Y, MUX2H_60_Y, MUX2H_41_Y, MUX2H_7_Y, 
        MUX2H_33_Y, MUX2H_15_Y, BFR_0_Y, BFR_1_Y, MUX2H_6_Y, 
        MUX2H_13_Y, MUX2H_8_Y, MUX2H_11_Y, MUX2H_32_Y, MUX2H_12_Y, 
        MUX2H_36_Y, MUX2H_45_Y, MUX2H_54_Y, MUX2H_20_Y, MUX2H_2_Y, 
        MUX2H_34_Y, MUX2H_21_Y, MUX2H_16_Y, MUX2H_22_Y, 
        MUX2H_35_Y, MUX2H_39_Y, MUX2H_63_Y, MUX2H_51_Y, 
        MUX2H_28_Y, MUX2H_58_Y, MUX2H_37_Y, MUX2H_1_Y, MUX2H_4_Y, 
        MUX2H_3_Y, MUX2H_19_Y, MUX2H_26_Y, MUX2H_9_Y, MUX2H_23_Y, 
        MUX2H_0_Y, MUX2H_38_Y, MUX2H_52_Y, MUX2H_43_Y, MUX2H_61_Y, 
        MUX2H_49_Y, MUX2H_59_Y, MUX2H_62_Y, MUX2H_18_Y, 
        MUX2H_10_Y, MUX2H_57_Y, MUX2H_56_Y, MUX2H_17_Y, 
        MUX2H_47_Y, MUX2H_55_Y : std_logic ;
    begin   

    MUX2H_Result_10_inst : MUX2H
      port map(A => MUX2H_2_Y, B => MUX2H_34_Y, S => 
        SelAux_1_6_net, Y => Result(10));
    MUX2H_37 : MUX2H
      port map(A => Data2_port(15), B => Data3_port(15), S => 
        SelAux_0_27_net, Y => MUX2H_37_Y);
    MUX2H_12 : MUX2H
      port map(A => Data2_port(25), B => Data3_port(25), S => 
        SelAux_0_49_net, Y => MUX2H_12_Y);
    MUX2H_35 : MUX2H
      port map(A => Data2_port(21), B => Data3_port(21), S => 
        SelAux_0_38_net, Y => MUX2H_35_Y);
    MUX2H_14 : MUX2H
      port map(A => Data0_port(7), B => Data1_port(7), S => 
        SelAux_0_12_net, Y => MUX2H_14_Y);
    MUX2H_Result_3_inst : MUX2H
      port map(A => MUX2H_43_Y, B => MUX2H_61_Y, S => 
        SelAux_1_0_net, Y => Result(3));
    MUX2H_4 : MUX2H
      port map(A => Data2_port(23), B => Data3_port(23), S => 
        SelAux_0_44_net, Y => MUX2H_4_Y);
    MUX2H_Result_17_inst : MUX2H
      port map(A => MUX2H_6_Y, B => MUX2H_13_Y, S => 
        SelAux_1_17_net, Y => Result(17));
    BFR_SelAux_1_0_inst : BFR
      port map(A => Sel1, Y => SelAux_1_0_net);
    MUX2H_33 : MUX2H
      port map(A => Data0_port(0), B => Data1_port(0), S => 
        SelAux_0_0_net, Y => MUX2H_33_Y);
    MUX2H_Result_26_inst : MUX2H
      port map(A => MUX2H_48_Y, B => MUX2H_53_Y, S => 
        SelAux_1_22_net, Y => Result(26));
    MUX2H_Result_22_inst : MUX2H
      port map(A => MUX2H_23_Y, B => MUX2H_0_Y, S => 
        SelAux_1_22_net, Y => Result(22));
    MUX2H_60 : MUX2H
      port map(A => Data2_port(24), B => Data3_port(24), S => 
        SelAux_0_49_net, Y => MUX2H_60_Y);
    MUX2H_Result_8_inst : MUX2H
      port map(A => MUX2H_21_Y, B => MUX2H_16_Y, S => 
        SelAux_1_6_net, Y => Result(8));
    MUX2H_28 : MUX2H
      port map(A => Data2_port(28), B => Data3_port(28), S => 
        SelAux_0_54_net, Y => MUX2H_28_Y);
    MUX2H_51 : MUX2H
      port map(A => Data0_port(28), B => Data1_port(28), S => 
        SelAux_0_54_net, Y => MUX2H_51_Y);
    MUX2H_Result_15_inst : MUX2H
      port map(A => MUX2H_58_Y, B => MUX2H_37_Y, S => 
        SelAux_1_12_net, Y => Result(15));
    MUX2H_62 : MUX2H
      port map(A => Data0_port(2), B => Data1_port(2), S => 
        SelAux_0_0_net, Y => MUX2H_62_Y);
    MUX2H_Result_28_inst : MUX2H
      port map(A => MUX2H_51_Y, B => MUX2H_28_Y, S => 
        SelAux_1_27_net, Y => Result(28));
    MUX2H_26 : MUX2H
      port map(A => Data0_port(4), B => Data1_port(4), S => 
        SelAux_0_6_net, Y => MUX2H_26_Y);
    BFR_SelAux_1_6_inst : BFR
      port map(A => Sel1, Y => SelAux_1_6_net);
    BFR_0 : BFR
      port map(A => Sel0, Y => BFR_0_Y);
    BFR_SelAux_0_6_inst : BFR
      port map(A => BFR_0_Y, Y => SelAux_0_6_net);
    MUX2H_18 : MUX2H
      port map(A => Data2_port(2), B => Data3_port(2), S => 
        SelAux_0_0_net, Y => MUX2H_18_Y);
    MUX2H_Result_29_inst : MUX2H
      port map(A => MUX2H_3_Y, B => MUX2H_19_Y, S => 
        SelAux_1_27_net, Y => Result(29));
    MUX2H_39 : MUX2H
      port map(A => Data0_port(1), B => Data1_port(1), S => 
        SelAux_0_0_net, Y => MUX2H_39_Y);
    MUX2H_Result_7_inst : MUX2H
      port map(A => MUX2H_14_Y, B => MUX2H_40_Y, S => 
        SelAux_1_6_net, Y => Result(7));
    MUX2H_2 : MUX2H
      port map(A => Data0_port(10), B => Data1_port(10), S => 
        SelAux_0_17_net, Y => MUX2H_2_Y);
    MUX2H_57 : MUX2H
      port map(A => Data2_port(18), B => Data3_port(18), S => 
        SelAux_0_32_net, Y => MUX2H_57_Y);
    MUX2H_41 : MUX2H
      port map(A => Data0_port(20), B => Data1_port(20), S => 
        SelAux_0_38_net, Y => MUX2H_41_Y);
    MUX2H_55 : MUX2H
      port map(A => Data2_port(27), B => Data3_port(27), S => 
        SelAux_0_54_net, Y => MUX2H_55_Y);
    BFR_SelAux_0_27_inst : BFR
      port map(A => BFR_0_Y, Y => SelAux_0_27_net);
    MUX2H_16 : MUX2H
      port map(A => Data2_port(8), B => Data3_port(8), S => 
        SelAux_0_17_net, Y => MUX2H_16_Y);
    BFR_SelAux_0_32_inst : BFR
      port map(A => BFR_1_Y, Y => SelAux_0_32_net);
    MUX2H_53 : MUX2H
      port map(A => Data2_port(26), B => Data3_port(26), S => 
        SelAux_0_49_net, Y => MUX2H_53_Y);
    BFR_SelAux_0_22_inst : BFR
      port map(A => BFR_0_Y, Y => SelAux_0_22_net);
    MUX2H_Result_4_inst : MUX2H
      port map(A => MUX2H_26_Y, B => MUX2H_9_Y, S => 
        SelAux_1_0_net, Y => Result(4));
    MUX2H_Result_2_inst : MUX2H
      port map(A => MUX2H_62_Y, B => MUX2H_18_Y, S => 
        SelAux_1_0_net, Y => Result(2));
    MUX2H_1 : MUX2H
      port map(A => Data0_port(23), B => Data1_port(23), S => 
        SelAux_0_44_net, Y => MUX2H_1_Y);
    MUX2H_30 : MUX2H
      port map(A => Data0_port(19), B => Data1_port(19), S => 
        SelAux_0_38_net, Y => MUX2H_30_Y);
    MUX2H_Result_16_inst : MUX2H
      port map(A => MUX2H_8_Y, B => MUX2H_11_Y, S => 
        SelAux_1_12_net, Y => Result(16));
    MUX2H_47 : MUX2H
      port map(A => Data0_port(27), B => Data1_port(27), S => 
        SelAux_0_54_net, Y => MUX2H_47_Y);
    MUX2H_45 : MUX2H
      port map(A => Data2_port(31), B => Data3_port(31), S => 
        SelAux_0_59_net, Y => MUX2H_45_Y);
    BFR_SelAux_0_59_inst : BFR
      port map(A => BFR_1_Y, Y => SelAux_0_59_net);
    MUX2H_Result_12_inst : MUX2H
      port map(A => MUX2H_50_Y, B => MUX2H_24_Y, S => 
        SelAux_1_12_net, Y => Result(12));
    MUX2H_43 : MUX2H
      port map(A => Data0_port(3), B => Data1_port(3), S => 
        SelAux_0_6_net, Y => MUX2H_43_Y);
    MUX2H_Result_1_inst : MUX2H
      port map(A => MUX2H_39_Y, B => MUX2H_63_Y, S => 
        SelAux_1_0_net, Y => Result(1));
    MUX2H_32 : MUX2H
      port map(A => Data0_port(25), B => Data1_port(25), S => 
        SelAux_0_49_net, Y => MUX2H_32_Y);
    MUX2H_34 : MUX2H
      port map(A => Data2_port(10), B => Data3_port(10), S => 
        SelAux_0_17_net, Y => MUX2H_34_Y);
    BFR_SelAux_0_0_inst : BFR
      port map(A => BFR_0_Y, Y => SelAux_0_0_net);
    MUX2H_21 : MUX2H
      port map(A => Data0_port(8), B => Data1_port(8), S => 
        SelAux_0_12_net, Y => MUX2H_21_Y);
    MUX2H_Result_24_inst : MUX2H
      port map(A => MUX2H_27_Y, B => MUX2H_60_Y, S => 
        SelAux_1_22_net, Y => Result(24));
    MUX2H_59 : MUX2H
      port map(A => Data2_port(11), B => Data3_port(11), S => 
        SelAux_0_22_net, Y => MUX2H_59_Y);
    MUX2H_Result_23_inst : MUX2H
      port map(A => MUX2H_1_Y, B => MUX2H_4_Y, S => 
        SelAux_1_22_net, Y => Result(23));
    MUX2H_Result_18_inst : MUX2H
      port map(A => MUX2H_10_Y, B => MUX2H_57_Y, S => 
        SelAux_1_17_net, Y => Result(18));
    MUX2H_6 : MUX2H
      port map(A => Data0_port(17), B => Data1_port(17), S => 
        SelAux_0_32_net, Y => MUX2H_6_Y);
    MUX2H_8 : MUX2H
      port map(A => Data0_port(16), B => Data1_port(16), S => 
        SelAux_0_32_net, Y => MUX2H_8_Y);
    BFR_SelAux_0_38_inst : BFR
      port map(A => BFR_1_Y, Y => SelAux_0_38_net);
    MUX2H_Result_19_inst : MUX2H
      port map(A => MUX2H_30_Y, B => MUX2H_46_Y, S => 
        SelAux_1_17_net, Y => Result(19));
    MUX2H_11 : MUX2H
      port map(A => Data2_port(16), B => Data3_port(16), S => 
        SelAux_0_32_net, Y => MUX2H_11_Y);
    MUX2H_27 : MUX2H
      port map(A => Data0_port(24), B => Data1_port(24), S => 
        SelAux_0_44_net, Y => MUX2H_27_Y);
    MUX2H_25 : MUX2H
      port map(A => Data0_port(13), B => Data1_port(13), S => 
        SelAux_0_22_net, Y => MUX2H_25_Y);
    MUX2H_49 : MUX2H
      port map(A => Data0_port(11), B => Data1_port(11), S => 
        SelAux_0_22_net, Y => MUX2H_49_Y);
    MUX2H_50 : MUX2H
      port map(A => Data0_port(12), B => Data1_port(12), S => 
        SelAux_0_22_net, Y => MUX2H_50_Y);
    MUX2H_23 : MUX2H
      port map(A => Data0_port(22), B => Data1_port(22), S => 
        SelAux_0_44_net, Y => MUX2H_23_Y);
    MUX2H_Result_31_inst : MUX2H
      port map(A => MUX2H_36_Y, B => MUX2H_45_Y, S => 
        SelAux_1_27_net, Y => Result(31));
    BFR_SelAux_1_17_inst : BFR
      port map(A => Sel1, Y => SelAux_1_17_net);
    MUX2H_Result_0_inst : MUX2H
      port map(A => MUX2H_33_Y, B => MUX2H_15_Y, S => 
        SelAux_1_0_net, Y => Result(0));
    MUX2H_38 : MUX2H
      port map(A => Data0_port(9), B => Data1_port(9), S => 
        SelAux_0_17_net, Y => MUX2H_38_Y);
    BFR_SelAux_0_54_inst : BFR
      port map(A => BFR_1_Y, Y => SelAux_0_54_net);
    BFR_SelAux_1_27_inst : BFR
      port map(A => Sel1, Y => SelAux_1_27_net);
    BFR_SelAux_0_49_inst : BFR
      port map(A => BFR_1_Y, Y => SelAux_0_49_net);
    BFR_SelAux_1_12_inst : BFR
      port map(A => Sel1, Y => SelAux_1_12_net);
    MUX2H_17 : MUX2H
      port map(A => Data2_port(30), B => Data3_port(30), S => 
        SelAux_0_59_net, Y => MUX2H_17_Y);
    MUX2H_Result_21_inst : MUX2H
      port map(A => MUX2H_22_Y, B => MUX2H_35_Y, S => 
        SelAux_1_17_net, Y => Result(21));
    MUX2H_52 : MUX2H
      port map(A => Data2_port(9), B => Data3_port(9), S => 
        SelAux_0_17_net, Y => MUX2H_52_Y);
    MUX2H_15 : MUX2H
      port map(A => Data2_port(0), B => Data3_port(0), S => 
        SelAux_0_0_net, Y => MUX2H_15_Y);
    BFR_SelAux_1_22_inst : BFR
      port map(A => Sel1, Y => SelAux_1_22_net);
    MUX2H_54 : MUX2H
      port map(A => Data0_port(14), B => Data1_port(14), S => 
        SelAux_0_27_net, Y => MUX2H_54_Y);
    MUX2H_Result_5_inst : MUX2H
      port map(A => MUX2H_44_Y, B => MUX2H_42_Y, S => 
        SelAux_1_0_net, Y => Result(5));
    MUX2H_61 : MUX2H
      port map(A => Data2_port(3), B => Data3_port(3), S => 
        SelAux_0_6_net, Y => MUX2H_61_Y);
    MUX2H_40 : MUX2H
      port map(A => Data2_port(7), B => Data3_port(7), S => 
        SelAux_0_12_net, Y => MUX2H_40_Y);
    MUX2H_36 : MUX2H
      port map(A => Data0_port(31), B => Data1_port(31), S => 
        SelAux_0_59_net, Y => MUX2H_36_Y);
    MUX2H_Result_30_inst : MUX2H
      port map(A => MUX2H_56_Y, B => MUX2H_17_Y, S => 
        SelAux_1_27_net, Y => Result(30));
    MUX2H_13 : MUX2H
      port map(A => Data2_port(17), B => Data3_port(17), S => 
        SelAux_0_32_net, Y => MUX2H_13_Y);
    MUX2H_3 : MUX2H
      port map(A => Data0_port(29), B => Data1_port(29), S => 
        SelAux_0_54_net, Y => MUX2H_3_Y);
    BFR_1 : BFR
      port map(A => Sel0, Y => BFR_1_Y);
    MUX2H_0 : MUX2H
      port map(A => Data2_port(22), B => Data3_port(22), S => 
        SelAux_0_44_net, Y => MUX2H_0_Y);
    MUX2H_Result_14_inst : MUX2H
      port map(A => MUX2H_54_Y, B => MUX2H_20_Y, S => 
        SelAux_1_12_net, Y => Result(14));
    MUX2H_Result_20_inst : MUX2H
      port map(A => MUX2H_41_Y, B => MUX2H_7_Y, S => 
        SelAux_1_17_net, Y => Result(20));
    MUX2H_42 : MUX2H
      port map(A => Data2_port(5), B => Data3_port(5), S => 
        SelAux_0_6_net, Y => MUX2H_42_Y);
    MUX2H_Result_13_inst : MUX2H
      port map(A => MUX2H_25_Y, B => MUX2H_31_Y, S => 
        SelAux_1_12_net, Y => Result(13));
    MUX2H_29 : MUX2H
      port map(A => Data2_port(6), B => Data3_port(6), S => 
        SelAux_0_12_net, Y => MUX2H_29_Y);
    MUX2H_44 : MUX2H
      port map(A => Data0_port(5), B => Data1_port(5), S => 
        SelAux_0_6_net, Y => MUX2H_44_Y);
    MUX2H_Result_27_inst : MUX2H
      port map(A => MUX2H_47_Y, B => MUX2H_55_Y, S => 
        SelAux_1_27_net, Y => Result(27));
    MUX2H_9 : MUX2H
      port map(A => Data2_port(4), B => Data3_port(4), S => 
        SelAux_0_6_net, Y => MUX2H_9_Y);
    BFR_SelAux_0_17_inst : BFR
      port map(A => BFR_0_Y, Y => SelAux_0_17_net);
    MUX2H_Result_6_inst : MUX2H
      port map(A => MUX2H_5_Y, B => MUX2H_29_Y, S => 
        SelAux_1_6_net, Y => Result(6));
    MUX2H_63 : MUX2H
      port map(A => Data2_port(1), B => Data3_port(1), S => 
        SelAux_0_0_net, Y => MUX2H_63_Y);
    MUX2H_58 : MUX2H
      port map(A => Data0_port(15), B => Data1_port(15), S => 
        SelAux_0_27_net, Y => MUX2H_58_Y);
    BFR_SelAux_0_44_inst : BFR
      port map(A => BFR_1_Y, Y => SelAux_0_44_net);
    MUX2H_7 : MUX2H
      port map(A => Data2_port(20), B => Data3_port(20), S => 
        SelAux_0_38_net, Y => MUX2H_7_Y);
    BFR_SelAux_0_12_inst : BFR
      port map(A => BFR_0_Y, Y => SelAux_0_12_net);
    MUX2H_19 : MUX2H
      port map(A => Data2_port(29), B => Data3_port(29), S => 
        SelAux_0_59_net, Y => MUX2H_19_Y);
    MUX2H_20 : MUX2H
      port map(A => Data2_port(14), B => Data3_port(14), S => 
        SelAux_0_27_net, Y => MUX2H_20_Y);
    MUX2H_Result_25_inst : MUX2H
      port map(A => MUX2H_32_Y, B => MUX2H_12_Y, S => 
        SelAux_1_22_net, Y => Result(25));
    MUX2H_56 : MUX2H
      port map(A => Data0_port(30), B => Data1_port(30), S => 
        SelAux_0_59_net, Y => MUX2H_56_Y);
    MUX2H_48 : MUX2H
      port map(A => Data0_port(26), B => Data1_port(26), S => 
        SelAux_0_49_net, Y => MUX2H_48_Y);
    MUX2H_31 : MUX2H
      port map(A => Data2_port(13), B => Data3_port(13), S => 
        SelAux_0_27_net, Y => MUX2H_31_Y);
    MUX2H_Result_11_inst : MUX2H
      port map(A => MUX2H_49_Y, B => MUX2H_59_Y, S => 
        SelAux_1_6_net, Y => Result(11));
    MUX2H_22 : MUX2H
      port map(A => Data0_port(21), B => Data1_port(21), S => 
        SelAux_0_38_net, Y => MUX2H_22_Y);
    MUX2H_24 : MUX2H
      port map(A => Data2_port(12), B => Data3_port(12), S => 
        SelAux_0_22_net, Y => MUX2H_24_Y);
    MUX2H_5 : MUX2H
      port map(A => Data0_port(6), B => Data1_port(6), S => 
        SelAux_0_12_net, Y => MUX2H_5_Y);
    MUX2H_10 : MUX2H
      port map(A => Data0_port(18), B => Data1_port(18), S => 
        SelAux_0_32_net, Y => MUX2H_10_Y);
    MUX2H_Result_9_inst : MUX2H
      port map(A => MUX2H_38_Y, B => MUX2H_52_Y, S => 
        SelAux_1_6_net, Y => Result(9));
    MUX2H_46 : MUX2H
      port map(A => Data2_port(19), B => Data3_port(19), S => 
        SelAux_0_38_net, Y => MUX2H_46_Y);
end DEF_ARCH;



library ieee;
use ieee.std_logic_1164.all;
library proasic3;

entity ramdp_apa3 is 
    port( DATAA : in std_logic_vector(31 downto 0); QA : out 
        std_logic_vector(31 downto 0); DATAB : in 
        std_logic_vector(31 downto 0); QB : out std_logic_vector(
        31 downto 0); ADDRESSA : in std_logic_vector(3 downto 0); 
        ADDRESSB : in std_logic_vector(3 downto 0);RWA, RWB, BLKA, 
        BLKB, CLKA, CLKB : in std_logic) ;
end ramdp_apa3;


architecture DEF_ARCH of  ramdp_apa3 is

    component RAM4K9
    generic (MEMORYFILE:string := "");

        port(ADDRA11, ADDRA10, ADDRA9, ADDRA8, ADDRA7, ADDRA6, 
        ADDRA5, ADDRA4, ADDRA3, ADDRA2, ADDRA1, ADDRA0, ADDRB11, 
        ADDRB10, ADDRB9, ADDRB8, ADDRB7, ADDRB6, ADDRB5, ADDRB4, 
        ADDRB3, ADDRB2, ADDRB1, ADDRB0, DINA8, DINA7, DINA6, 
        DINA5, DINA4, DINA3, DINA2, DINA1, DINA0, DINB8, DINB7, 
        DINB6, DINB5, DINB4, DINB3, DINB2, DINB1, DINB0, WIDTHA0, 
        WIDTHA1, WIDTHB0, WIDTHB1, PIPEA, PIPEB, WMODEA, WMODEB, 
        BLKA, BLKB, WENA, WENB, CLKA, CLKB, RESET : in std_logic := 
        'U'; DOUTA8, DOUTA7, DOUTA6, DOUTA5, DOUTA4, DOUTA3, 
        DOUTA2, DOUTA1, DOUTA0, DOUTB8, DOUTB7, DOUTB6, DOUTB5, 
        DOUTB4, DOUTB3, DOUTB2, DOUTB1, DOUTB0 : out std_logic) ;
    end component;

    component INV
        port(A : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component VCC
        port( Y : out std_logic);
    end component;

    component GND
        port( Y : out std_logic);
    end component;

    signal CLKBP, VCC_1_net, GND_1_net : std_logic ;
    begin   

    VCC_2_net : VCC port map(Y => VCC_1_net);
    GND_2_net : GND port map(Y => GND_1_net);
    ramdp_apa3_R0C1 : RAM4K9
      port map(ADDRA11 => GND_1_net, ADDRA10 => GND_1_net, 
        ADDRA9 => GND_1_net, ADDRA8 => GND_1_net, ADDRA7 => 
        GND_1_net, ADDRA6 => GND_1_net, ADDRA5 => GND_1_net, 
        ADDRA4 => GND_1_net, ADDRA3 => ADDRESSA(3), ADDRA2 => 
        ADDRESSA(2), ADDRA1 => ADDRESSA(1), ADDRA0 => ADDRESSA(0), 
        ADDRB11 => GND_1_net, ADDRB10 => GND_1_net, ADDRB9 => 
        GND_1_net, ADDRB8 => GND_1_net, ADDRB7 => GND_1_net, 
        ADDRB6 => GND_1_net, ADDRB5 => GND_1_net, ADDRB4 => 
        GND_1_net, ADDRB3 => ADDRESSB(3), ADDRB2 => ADDRESSB(2), 
        ADDRB1 => ADDRESSB(1), ADDRB0 => ADDRESSB(0), DINA8 => 
        GND_1_net, DINA7 => DATAA(15), DINA6 => DATAA(14), 
        DINA5 => DATAA(13), DINA4 => DATAA(12), DINA3 => 
        DATAA(11), DINA2 => DATAA(10), DINA1 => DATAA(9), 
        DINA0 => DATAA(8), DINB8 => GND_1_net, DINB7 => DATAB(15), 
        DINB6 => DATAB(14), DINB5 => DATAB(13), DINB4 => 
        DATAB(12), DINB3 => DATAB(11), DINB2 => DATAB(10), 
        DINB1 => DATAB(9), DINB0 => DATAB(8), WIDTHA0 => 
        VCC_1_net, WIDTHA1 => VCC_1_net, WIDTHB0 => VCC_1_net, 
        WIDTHB1 => VCC_1_net, PIPEA => GND_1_net, PIPEB => 
        GND_1_net, WMODEA => VCC_1_net, WMODEB => VCC_1_net, 
        BLKA => BLKA, BLKB => BLKB, WENA => RWA, WENB => RWB, 
        CLKA => CLKA, CLKB => CLKBP, RESET => VCC_1_net, 
        DOUTA8 => OPEN , DOUTA7 => QA(15), DOUTA6 => QA(14), 
        DOUTA5 => QA(13), DOUTA4 => QA(12), DOUTA3 => QA(11), 
        DOUTA2 => QA(10), DOUTA1 => QA(9), DOUTA0 => QA(8), 
        DOUTB8 => OPEN , DOUTB7 => QB(15), DOUTB6 => QB(14), 
        DOUTB5 => QB(13), DOUTB4 => QB(12), DOUTB3 => QB(11), 
        DOUTB2 => QB(10), DOUTB1 => QB(9), DOUTB0 => QB(8));
    ramdp_apa3_R0C0 : RAM4K9
      port map(ADDRA11 => GND_1_net, ADDRA10 => GND_1_net, 
        ADDRA9 => GND_1_net, ADDRA8 => GND_1_net, ADDRA7 => 
        GND_1_net, ADDRA6 => GND_1_net, ADDRA5 => GND_1_net, 
        ADDRA4 => GND_1_net, ADDRA3 => ADDRESSA(3), ADDRA2 => 
        ADDRESSA(2), ADDRA1 => ADDRESSA(1), ADDRA0 => ADDRESSA(0), 
        ADDRB11 => GND_1_net, ADDRB10 => GND_1_net, ADDRB9 => 
        GND_1_net, ADDRB8 => GND_1_net, ADDRB7 => GND_1_net, 
        ADDRB6 => GND_1_net, ADDRB5 => GND_1_net, ADDRB4 => 
        GND_1_net, ADDRB3 => ADDRESSB(3), ADDRB2 => ADDRESSB(2), 
        ADDRB1 => ADDRESSB(1), ADDRB0 => ADDRESSB(0), DINA8 => 
        GND_1_net, DINA7 => DATAA(7), DINA6 => DATAA(6), DINA5 => 
        DATAA(5), DINA4 => DATAA(4), DINA3 => DATAA(3), DINA2 => 
        DATAA(2), DINA1 => DATAA(1), DINA0 => DATAA(0), DINB8 => 
        GND_1_net, DINB7 => DATAB(7), DINB6 => DATAB(6), DINB5 => 
        DATAB(5), DINB4 => DATAB(4), DINB3 => DATAB(3), DINB2 => 
        DATAB(2), DINB1 => DATAB(1), DINB0 => DATAB(0), 
        WIDTHA0 => VCC_1_net, WIDTHA1 => VCC_1_net, WIDTHB0 => 
        VCC_1_net, WIDTHB1 => VCC_1_net, PIPEA => GND_1_net, 
        PIPEB => GND_1_net, WMODEA => VCC_1_net, WMODEB => 
        VCC_1_net, BLKA => BLKA, BLKB => BLKB, WENA => RWA, 
        WENB => RWB, CLKA => CLKA, CLKB => CLKBP, RESET => 
        VCC_1_net, DOUTA8 => OPEN , DOUTA7 => QA(7), DOUTA6 => 
        QA(6), DOUTA5 => QA(5), DOUTA4 => QA(4), DOUTA3 => QA(3), 
        DOUTA2 => QA(2), DOUTA1 => QA(1), DOUTA0 => QA(0), 
        DOUTB8 => OPEN , DOUTB7 => QB(7), DOUTB6 => QB(6), 
        DOUTB5 => QB(5), DOUTB4 => QB(4), DOUTB3 => QB(3), 
        DOUTB2 => QB(2), DOUTB1 => QB(1), DOUTB0 => QB(0));
    ramdp_apa3_R0C2 : RAM4K9
      port map(ADDRA11 => GND_1_net, ADDRA10 => GND_1_net, 
        ADDRA9 => GND_1_net, ADDRA8 => GND_1_net, ADDRA7 => 
        GND_1_net, ADDRA6 => GND_1_net, ADDRA5 => GND_1_net, 
        ADDRA4 => GND_1_net, ADDRA3 => ADDRESSA(3), ADDRA2 => 
        ADDRESSA(2), ADDRA1 => ADDRESSA(1), ADDRA0 => ADDRESSA(0), 
        ADDRB11 => GND_1_net, ADDRB10 => GND_1_net, ADDRB9 => 
        GND_1_net, ADDRB8 => GND_1_net, ADDRB7 => GND_1_net, 
        ADDRB6 => GND_1_net, ADDRB5 => GND_1_net, ADDRB4 => 
        GND_1_net, ADDRB3 => ADDRESSB(3), ADDRB2 => ADDRESSB(2), 
        ADDRB1 => ADDRESSB(1), ADDRB0 => ADDRESSB(0), DINA8 => 
        GND_1_net, DINA7 => DATAA(23), DINA6 => DATAA(22), 
        DINA5 => DATAA(21), DINA4 => DATAA(20), DINA3 => 
        DATAA(19), DINA2 => DATAA(18), DINA1 => DATAA(17), 
        DINA0 => DATAA(16), DINB8 => GND_1_net, DINB7 => 
        DATAB(23), DINB6 => DATAB(22), DINB5 => DATAB(21), 
        DINB4 => DATAB(20), DINB3 => DATAB(19), DINB2 => 
        DATAB(18), DINB1 => DATAB(17), DINB0 => DATAB(16), 
        WIDTHA0 => VCC_1_net, WIDTHA1 => VCC_1_net, WIDTHB0 => 
        VCC_1_net, WIDTHB1 => VCC_1_net, PIPEA => GND_1_net, 
        PIPEB => GND_1_net, WMODEA => VCC_1_net, WMODEB => 
        VCC_1_net, BLKA => BLKA, BLKB => BLKB, WENA => RWA, 
        WENB => RWB, CLKA => CLKA, CLKB => CLKBP, RESET => 
        VCC_1_net, DOUTA8 => OPEN , DOUTA7 => QA(23), DOUTA6 => 
        QA(22), DOUTA5 => QA(21), DOUTA4 => QA(20), DOUTA3 => 
        QA(19), DOUTA2 => QA(18), DOUTA1 => QA(17), DOUTA0 => 
        QA(16), DOUTB8 => OPEN , DOUTB7 => QB(23), DOUTB6 => 
        QB(22), DOUTB5 => QB(21), DOUTB4 => QB(20), DOUTB3 => 
        QB(19), DOUTB2 => QB(18), DOUTB1 => QB(17), DOUTB0 => 
        QB(16));
    CLKBBUBBLE : INV
      port map(A => CLKB, Y => CLKBP);
    ramdp_apa3_R0C3 : RAM4K9
      port map(ADDRA11 => GND_1_net, ADDRA10 => GND_1_net, 
        ADDRA9 => GND_1_net, ADDRA8 => GND_1_net, ADDRA7 => 
        GND_1_net, ADDRA6 => GND_1_net, ADDRA5 => GND_1_net, 
        ADDRA4 => GND_1_net, ADDRA3 => ADDRESSA(3), ADDRA2 => 
        ADDRESSA(2), ADDRA1 => ADDRESSA(1), ADDRA0 => ADDRESSA(0), 
        ADDRB11 => GND_1_net, ADDRB10 => GND_1_net, ADDRB9 => 
        GND_1_net, ADDRB8 => GND_1_net, ADDRB7 => GND_1_net, 
        ADDRB6 => GND_1_net, ADDRB5 => GND_1_net, ADDRB4 => 
        GND_1_net, ADDRB3 => ADDRESSB(3), ADDRB2 => ADDRESSB(2), 
        ADDRB1 => ADDRESSB(1), ADDRB0 => ADDRESSB(0), DINA8 => 
        GND_1_net, DINA7 => DATAA(31), DINA6 => DATAA(30), 
        DINA5 => DATAA(29), DINA4 => DATAA(28), DINA3 => 
        DATAA(27), DINA2 => DATAA(26), DINA1 => DATAA(25), 
        DINA0 => DATAA(24), DINB8 => GND_1_net, DINB7 => 
        DATAB(31), DINB6 => DATAB(30), DINB5 => DATAB(29), 
        DINB4 => DATAB(28), DINB3 => DATAB(27), DINB2 => 
        DATAB(26), DINB1 => DATAB(25), DINB0 => DATAB(24), 
        WIDTHA0 => VCC_1_net, WIDTHA1 => VCC_1_net, WIDTHB0 => 
        VCC_1_net, WIDTHB1 => VCC_1_net, PIPEA => GND_1_net, 
        PIPEB => GND_1_net, WMODEA => VCC_1_net, WMODEB => 
        VCC_1_net, BLKA => BLKA, BLKB => BLKB, WENA => RWA, 
        WENB => RWB, CLKA => CLKA, CLKB => CLKBP, RESET => 
        VCC_1_net, DOUTA8 => OPEN , DOUTA7 => QA(31), DOUTA6 => 
        QA(30), DOUTA5 => QA(29), DOUTA4 => QA(28), DOUTA3 => 
        QA(27), DOUTA2 => QA(26), DOUTA1 => QA(25), DOUTA0 => 
        QA(24), DOUTB8 => OPEN , DOUTB7 => QB(31), DOUTB6 => 
        QB(30), DOUTB5 => QB(29), DOUTB4 => QB(28), DOUTB3 => 
        QB(27), DOUTB2 => QB(26), DOUTB1 => QB(25), DOUTB0 => 
        QB(24));
end DEF_ARCH;

-- Version: 6.2 6.2.50.1

library ieee;
use ieee.std_logic_1164.all;
library proasic3;

entity mux41_actel_apa3 is 
    port( Data0_port : in std_logic_vector(31 downto 0); 
        Data1_port : in std_logic_vector(31 downto 0); Data2_port : 
        in std_logic_vector(31 downto 0); Data3_port : in 
        std_logic_vector(31 downto 0);Sel0, Sel1 : in std_logic; 
        Result : out std_logic_vector(31 downto 0)) ;
end mux41_actel_apa3;


architecture DEF_ARCH of  mux41_actel_apa3 is

    component MX2
        port(A, B, S : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component BUFF
        port(A : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    signal SelAux_0_0_net, SelAux_0_6_net, SelAux_0_12_net, 
        SelAux_0_17_net, SelAux_0_22_net, SelAux_0_27_net, 
        SelAux_0_32_net, SelAux_0_38_net, SelAux_0_44_net, 
        SelAux_0_49_net, SelAux_0_54_net, SelAux_0_59_net, 
        SelAux_1_0_net, SelAux_1_6_net, SelAux_1_12_net, 
        SelAux_1_17_net, SelAux_1_22_net, SelAux_1_27_net, 
        MX2_25_Y, MX2_31_Y, MX2_30_Y, MX2_46_Y, MX2_5_Y, MX2_29_Y, 
        MX2_44_Y, MX2_42_Y, MX2_50_Y, MX2_24_Y, MX2_48_Y, 
        MX2_53_Y, MX2_14_Y, MX2_40_Y, MX2_27_Y, MX2_60_Y, 
        MX2_41_Y, MX2_7_Y, MX2_33_Y, MX2_15_Y, BUFF_0_Y, BUFF_1_Y, 
        MX2_6_Y, MX2_13_Y, MX2_8_Y, MX2_11_Y, MX2_32_Y, MX2_12_Y, 
        MX2_36_Y, MX2_45_Y, MX2_54_Y, MX2_20_Y, MX2_2_Y, MX2_34_Y, 
        MX2_21_Y, MX2_16_Y, MX2_22_Y, MX2_35_Y, MX2_39_Y, 
        MX2_63_Y, MX2_51_Y, MX2_28_Y, MX2_58_Y, MX2_37_Y, MX2_1_Y, 
        MX2_4_Y, MX2_3_Y, MX2_19_Y, MX2_26_Y, MX2_9_Y, MX2_23_Y, 
        MX2_0_Y, MX2_38_Y, MX2_52_Y, MX2_43_Y, MX2_61_Y, MX2_49_Y, 
        MX2_59_Y, MX2_62_Y, MX2_18_Y, MX2_10_Y, MX2_57_Y, 
        MX2_56_Y, MX2_17_Y, MX2_47_Y, MX2_55_Y : std_logic ;
    begin   

    MX2_18 : MX2
      port map(A => Data2_port(2), B => Data3_port(2), S => 
        SelAux_0_0_net, Y => MX2_18_Y);
    BUFF_SelAux_1_12_inst : BUFF
      port map(A => Sel1, Y => SelAux_1_12_net);
    MX2_Result_1_inst : MX2
      port map(A => MX2_39_Y, B => MX2_63_Y, S => SelAux_1_0_net, 
        Y => Result(1));
    MX2_12 : MX2
      port map(A => Data2_port(25), B => Data3_port(25), S => 
        SelAux_0_49_net, Y => MX2_12_Y);
    MX2_Result_18_inst : MX2
      port map(A => MX2_10_Y, B => MX2_57_Y, S => SelAux_1_17_net, 
        Y => Result(18));
    MX2_10 : MX2
      port map(A => Data0_port(18), B => Data1_port(18), S => 
        SelAux_0_32_net, Y => MX2_10_Y);
    BUFF_SelAux_0_12_inst : BUFF
      port map(A => BUFF_0_Y, Y => SelAux_0_12_net);
    MX2_Result_31_inst : MX2
      port map(A => MX2_36_Y, B => MX2_45_Y, S => SelAux_1_27_net, 
        Y => Result(31));
    MX2_7 : MX2
      port map(A => Data2_port(20), B => Data3_port(20), S => 
        SelAux_0_38_net, Y => MX2_7_Y);
    MX2_53 : MX2
      port map(A => Data2_port(26), B => Data3_port(26), S => 
        SelAux_0_49_net, Y => MX2_53_Y);
    MX2_15 : MX2
      port map(A => Data2_port(0), B => Data3_port(0), S => 
        SelAux_0_0_net, Y => MX2_15_Y);
    MX2_Result_8_inst : MX2
      port map(A => MX2_21_Y, B => MX2_16_Y, S => SelAux_1_6_net, 
        Y => Result(8));
    BUFF_SelAux_0_38_inst : BUFF
      port map(A => BUFF_1_Y, Y => SelAux_0_38_net);
    MX2_57 : MX2
      port map(A => Data2_port(18), B => Data3_port(18), S => 
        SelAux_0_32_net, Y => MX2_57_Y);
    MX2_Result_24_inst : MX2
      port map(A => MX2_27_Y, B => MX2_60_Y, S => SelAux_1_22_net, 
        Y => Result(24));
    BUFF_0 : BUFF
      port map(A => Sel0, Y => BUFF_0_Y);
    MX2_26 : MX2
      port map(A => Data0_port(4), B => Data1_port(4), S => 
        SelAux_0_6_net, Y => MX2_26_Y);
    MX2_Result_2_inst : MX2
      port map(A => MX2_62_Y, B => MX2_18_Y, S => SelAux_1_0_net, 
        Y => Result(2));
    MX2_43 : MX2
      port map(A => Data0_port(3), B => Data1_port(3), S => 
        SelAux_0_6_net, Y => MX2_43_Y);
    MX2_Result_27_inst : MX2
      port map(A => MX2_47_Y, B => MX2_55_Y, S => SelAux_1_27_net, 
        Y => Result(27));
    MX2_Result_5_inst : MX2
      port map(A => MX2_44_Y, B => MX2_42_Y, S => SelAux_1_0_net, 
        Y => Result(5));
    MX2_Result_0_inst : MX2
      port map(A => MX2_33_Y, B => MX2_15_Y, S => SelAux_1_0_net, 
        Y => Result(0));
    MX2_47 : MX2
      port map(A => Data0_port(27), B => Data1_port(27), S => 
        SelAux_0_54_net, Y => MX2_47_Y);
    MX2_59 : MX2
      port map(A => Data2_port(11), B => Data3_port(11), S => 
        SelAux_0_22_net, Y => MX2_59_Y);
    MX2_Result_29_inst : MX2
      port map(A => MX2_3_Y, B => MX2_19_Y, S => SelAux_1_27_net, 
        Y => Result(29));
    MX2_Result_14_inst : MX2
      port map(A => MX2_54_Y, B => MX2_20_Y, S => SelAux_1_12_net, 
        Y => Result(14));
    MX2_16 : MX2
      port map(A => Data2_port(8), B => Data3_port(8), S => 
        SelAux_0_17_net, Y => MX2_16_Y);
    MX2_51 : MX2
      port map(A => Data0_port(28), B => Data1_port(28), S => 
        SelAux_0_54_net, Y => MX2_51_Y);
    MX2_34 : MX2
      port map(A => Data2_port(10), B => Data3_port(10), S => 
        SelAux_0_17_net, Y => MX2_34_Y);
    MX2_58 : MX2
      port map(A => Data0_port(15), B => Data1_port(15), S => 
        SelAux_0_27_net, Y => MX2_58_Y);
    MX2_52 : MX2
      port map(A => Data2_port(9), B => Data3_port(9), S => 
        SelAux_0_17_net, Y => MX2_52_Y);
    MX2_2 : MX2
      port map(A => Data0_port(10), B => Data1_port(10), S => 
        SelAux_0_17_net, Y => MX2_2_Y);
    MX2_50 : MX2
      port map(A => Data0_port(12), B => Data1_port(12), S => 
        SelAux_0_22_net, Y => MX2_50_Y);
    MX2_Result_17_inst : MX2
      port map(A => MX2_6_Y, B => MX2_13_Y, S => SelAux_1_17_net, 
        Y => Result(17));
    MX2_49 : MX2
      port map(A => Data0_port(11), B => Data1_port(11), S => 
        SelAux_0_22_net, Y => MX2_49_Y);
    MX2_Result_19_inst : MX2
      port map(A => MX2_30_Y, B => MX2_46_Y, S => SelAux_1_17_net, 
        Y => Result(19));
    MX2_41 : MX2
      port map(A => Data0_port(20), B => Data1_port(20), S => 
        SelAux_0_38_net, Y => MX2_41_Y);
    MX2_55 : MX2
      port map(A => Data2_port(27), B => Data3_port(27), S => 
        SelAux_0_54_net, Y => MX2_55_Y);
    MX2_48 : MX2
      port map(A => Data0_port(26), B => Data1_port(26), S => 
        SelAux_0_49_net, Y => MX2_48_Y);
    MX2_42 : MX2
      port map(A => Data2_port(5), B => Data3_port(5), S => 
        SelAux_0_6_net, Y => MX2_42_Y);
    MX2_1 : MX2
      port map(A => Data0_port(23), B => Data1_port(23), S => 
        SelAux_0_44_net, Y => MX2_1_Y);
    MX2_40 : MX2
      port map(A => Data2_port(7), B => Data3_port(7), S => 
        SelAux_0_12_net, Y => MX2_40_Y);
    BUFF_SelAux_0_59_inst : BUFF
      port map(A => BUFF_1_Y, Y => SelAux_0_59_net);
    MX2_Result_30_inst : MX2
      port map(A => MX2_56_Y, B => MX2_17_Y, S => SelAux_1_27_net, 
        Y => Result(30));
    MX2_63 : MX2
      port map(A => Data2_port(1), B => Data3_port(1), S => 
        SelAux_0_0_net, Y => MX2_63_Y);
    MX2_45 : MX2
      port map(A => Data2_port(31), B => Data3_port(31), S => 
        SelAux_0_59_net, Y => MX2_45_Y);
    MX2_Result_3_inst : MX2
      port map(A => MX2_43_Y, B => MX2_61_Y, S => SelAux_1_0_net, 
        Y => Result(3));
    MX2_0 : MX2
      port map(A => Data2_port(22), B => Data3_port(22), S => 
        SelAux_0_44_net, Y => MX2_0_Y);
    MX2_56 : MX2
      port map(A => Data0_port(30), B => Data1_port(30), S => 
        SelAux_0_59_net, Y => MX2_56_Y);
    BUFF_SelAux_1_27_inst : BUFF
      port map(A => Sel1, Y => SelAux_1_27_net);
    BUFF_SelAux_0_27_inst : BUFF
      port map(A => BUFF_0_Y, Y => SelAux_0_27_net);
    MX2_Result_26_inst : MX2
      port map(A => MX2_48_Y, B => MX2_53_Y, S => SelAux_1_22_net, 
        Y => Result(26));
    BUFF_SelAux_1_6_inst : BUFF
      port map(A => Sel1, Y => SelAux_1_6_net);
    BUFF_1 : BUFF
      port map(A => Sel0, Y => BUFF_1_Y);
    MX2_24 : MX2
      port map(A => Data2_port(12), B => Data3_port(12), S => 
        SelAux_0_22_net, Y => MX2_24_Y);
    MX2_Result_23_inst : MX2
      port map(A => MX2_1_Y, B => MX2_4_Y, S => SelAux_1_22_net, 
        Y => Result(23));
    MX2_46 : MX2
      port map(A => Data2_port(19), B => Data3_port(19), S => 
        SelAux_0_38_net, Y => MX2_46_Y);
    MX2_61 : MX2
      port map(A => Data2_port(3), B => Data3_port(3), S => 
        SelAux_0_6_net, Y => MX2_61_Y);
    BUFF_SelAux_1_22_inst : BUFF
      port map(A => Sel1, Y => SelAux_1_22_net);
    BUFF_SelAux_0_22_inst : BUFF
      port map(A => BUFF_0_Y, Y => SelAux_0_22_net);
    MX2_Result_16_inst : MX2
      port map(A => MX2_8_Y, B => MX2_11_Y, S => SelAux_1_12_net, 
        Y => Result(16));
    MX2_62 : MX2
      port map(A => Data0_port(2), B => Data1_port(2), S => 
        SelAux_0_0_net, Y => MX2_62_Y);
    MX2_5 : MX2
      port map(A => Data0_port(6), B => Data1_port(6), S => 
        SelAux_0_12_net, Y => MX2_5_Y);
    MX2_60 : MX2
      port map(A => Data2_port(24), B => Data3_port(24), S => 
        SelAux_0_49_net, Y => MX2_60_Y);
    BUFF_SelAux_0_49_inst : BUFF
      port map(A => BUFF_1_Y, Y => SelAux_0_49_net);
    MX2_14 : MX2
      port map(A => Data0_port(7), B => Data1_port(7), S => 
        SelAux_0_12_net, Y => MX2_14_Y);
    BUFF_SelAux_0_0_inst : BUFF
      port map(A => BUFF_0_Y, Y => SelAux_0_0_net);
    MX2_Result_13_inst : MX2
      port map(A => MX2_25_Y, B => MX2_31_Y, S => SelAux_1_12_net, 
        Y => Result(13));
    MX2_33 : MX2
      port map(A => Data0_port(0), B => Data1_port(0), S => 
        SelAux_0_0_net, Y => MX2_33_Y);
    MX2_9 : MX2
      port map(A => Data2_port(4), B => Data3_port(4), S => 
        SelAux_0_6_net, Y => MX2_9_Y);
    MX2_37 : MX2
      port map(A => Data2_port(15), B => Data3_port(15), S => 
        SelAux_0_27_net, Y => MX2_37_Y);
    MX2_4 : MX2
      port map(A => Data2_port(23), B => Data3_port(23), S => 
        SelAux_0_44_net, Y => MX2_4_Y);
    BUFF_SelAux_0_32_inst : BUFF
      port map(A => BUFF_1_Y, Y => SelAux_0_32_net);
    MX2_Result_22_inst : MX2
      port map(A => MX2_23_Y, B => MX2_0_Y, S => SelAux_1_22_net, 
        Y => Result(22));
    MX2_Result_21_inst : MX2
      port map(A => MX2_22_Y, B => MX2_35_Y, S => SelAux_1_17_net, 
        Y => Result(21));
    MX2_39 : MX2
      port map(A => Data0_port(1), B => Data1_port(1), S => 
        SelAux_0_0_net, Y => MX2_39_Y);
    MX2_31 : MX2
      port map(A => Data2_port(13), B => Data3_port(13), S => 
        SelAux_0_27_net, Y => MX2_31_Y);
    BUFF_SelAux_0_54_inst : BUFF
      port map(A => BUFF_1_Y, Y => SelAux_0_54_net);
    MX2_38 : MX2
      port map(A => Data0_port(9), B => Data1_port(9), S => 
        SelAux_0_17_net, Y => MX2_38_Y);
    MX2_Result_12_inst : MX2
      port map(A => MX2_50_Y, B => MX2_24_Y, S => SelAux_1_12_net, 
        Y => Result(12));
    MX2_Result_25_inst : MX2
      port map(A => MX2_32_Y, B => MX2_12_Y, S => SelAux_1_22_net, 
        Y => Result(25));
    MX2_32 : MX2
      port map(A => Data0_port(25), B => Data1_port(25), S => 
        SelAux_0_49_net, Y => MX2_32_Y);
    MX2_Result_11_inst : MX2
      port map(A => MX2_49_Y, B => MX2_59_Y, S => SelAux_1_6_net, 
        Y => Result(11));
    BUFF_SelAux_1_0_inst : BUFF
      port map(A => Sel1, Y => SelAux_1_0_net);
    BUFF_SelAux_0_6_inst : BUFF
      port map(A => BUFF_0_Y, Y => SelAux_0_6_net);
    MX2_30 : MX2
      port map(A => Data0_port(19), B => Data1_port(19), S => 
        SelAux_0_38_net, Y => MX2_30_Y);
    MX2_54 : MX2
      port map(A => Data0_port(14), B => Data1_port(14), S => 
        SelAux_0_27_net, Y => MX2_54_Y);
    MX2_35 : MX2
      port map(A => Data2_port(21), B => Data3_port(21), S => 
        SelAux_0_38_net, Y => MX2_35_Y);
    MX2_Result_15_inst : MX2
      port map(A => MX2_58_Y, B => MX2_37_Y, S => SelAux_1_12_net, 
        Y => Result(15));
    MX2_6 : MX2
      port map(A => Data0_port(17), B => Data1_port(17), S => 
        SelAux_0_32_net, Y => MX2_6_Y);
    MX2_23 : MX2
      port map(A => Data0_port(22), B => Data1_port(22), S => 
        SelAux_0_44_net, Y => MX2_23_Y);
    MX2_44 : MX2
      port map(A => Data0_port(5), B => Data1_port(5), S => 
        SelAux_0_6_net, Y => MX2_44_Y);
    MX2_27 : MX2
      port map(A => Data0_port(24), B => Data1_port(24), S => 
        SelAux_0_44_net, Y => MX2_27_Y);
    MX2_Result_9_inst : MX2
      port map(A => MX2_38_Y, B => MX2_52_Y, S => SelAux_1_6_net, 
        Y => Result(9));
    MX2_Result_7_inst : MX2
      port map(A => MX2_14_Y, B => MX2_40_Y, S => SelAux_1_6_net, 
        Y => Result(7));
    BUFF_SelAux_0_44_inst : BUFF
      port map(A => BUFF_1_Y, Y => SelAux_0_44_net);
    MX2_36 : MX2
      port map(A => Data0_port(31), B => Data1_port(31), S => 
        SelAux_0_59_net, Y => MX2_36_Y);
    MX2_13 : MX2
      port map(A => Data2_port(17), B => Data3_port(17), S => 
        SelAux_0_32_net, Y => MX2_13_Y);
    MX2_29 : MX2
      port map(A => Data2_port(6), B => Data3_port(6), S => 
        SelAux_0_12_net, Y => MX2_29_Y);
    MX2_17 : MX2
      port map(A => Data2_port(30), B => Data3_port(30), S => 
        SelAux_0_59_net, Y => MX2_17_Y);
    MX2_Result_20_inst : MX2
      port map(A => MX2_41_Y, B => MX2_7_Y, S => SelAux_1_17_net, 
        Y => Result(20));
    MX2_21 : MX2
      port map(A => Data0_port(8), B => Data1_port(8), S => 
        SelAux_0_12_net, Y => MX2_21_Y);
    MX2_Result_6_inst : MX2
      port map(A => MX2_5_Y, B => MX2_29_Y, S => SelAux_1_6_net, 
        Y => Result(6));
    MX2_28 : MX2
      port map(A => Data2_port(28), B => Data3_port(28), S => 
        SelAux_0_54_net, Y => MX2_28_Y);
    BUFF_SelAux_1_17_inst : BUFF
      port map(A => Sel1, Y => SelAux_1_17_net);
    MX2_22 : MX2
      port map(A => Data0_port(21), B => Data1_port(21), S => 
        SelAux_0_38_net, Y => MX2_22_Y);
    MX2_20 : MX2
      port map(A => Data2_port(14), B => Data3_port(14), S => 
        SelAux_0_27_net, Y => MX2_20_Y);
    MX2_Result_28_inst : MX2
      port map(A => MX2_51_Y, B => MX2_28_Y, S => SelAux_1_27_net, 
        Y => Result(28));
    BUFF_SelAux_0_17_inst : BUFF
      port map(A => BUFF_0_Y, Y => SelAux_0_17_net);
    MX2_19 : MX2
      port map(A => Data2_port(29), B => Data3_port(29), S => 
        SelAux_0_59_net, Y => MX2_19_Y);
    MX2_Result_10_inst : MX2
      port map(A => MX2_2_Y, B => MX2_34_Y, S => SelAux_1_6_net, 
        Y => Result(10));
    MX2_3 : MX2
      port map(A => Data0_port(29), B => Data1_port(29), S => 
        SelAux_0_54_net, Y => MX2_3_Y);
    MX2_8 : MX2
      port map(A => Data0_port(16), B => Data1_port(16), S => 
        SelAux_0_32_net, Y => MX2_8_Y);
    MX2_Result_4_inst : MX2
      port map(A => MX2_26_Y, B => MX2_9_Y, S => SelAux_1_0_net, 
        Y => Result(4));
    MX2_11 : MX2
      port map(A => Data2_port(16), B => Data3_port(16), S => 
        SelAux_0_32_net, Y => MX2_11_Y);
    MX2_25 : MX2
      port map(A => Data0_port(13), B => Data1_port(13), S => 
        SelAux_0_22_net, Y => MX2_25_Y);
end DEF_ARCH;


library ieee;
use ieee.std_logic_1164.all;
library proasic3;

entity adsu_apa3 is 
    port( DataA : in std_logic_vector(31 downto 0); DataB : in 
        std_logic_vector(31 downto 0);Cin, Addsub : in std_logic; 
        Sum : out std_logic_vector(31 downto 0); Cout : out 
        std_logic) ;
end adsu_apa3;


architecture DEF_ARCH of  adsu_apa3 is

    component INV
        port(A : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component AO1
        port(A, B, C : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component XNOR2
        port(A, B : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component AND2
        port(A, B : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component XOR2
        port(A, B : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component BUFF
        port(A : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component OR3
        port(A, B, C : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    signal AddsubAux_0_net, AddsubAux_6_net, AddsubAux_12_net, 
        AddsubAux_18_net, AddsubAux_24_net, AddsubAux_29_net, 
        DataBXnor2_0_net, DataBXnor2_1_net, DataBXnor2_2_net, 
        DataBXnor2_3_net, DataBXnor2_4_net, DataBXnor2_5_net, 
        DataBXnor2_6_net, DataBXnor2_7_net, DataBXnor2_8_net, 
        DataBXnor2_9_net, DataBXnor2_10_net, DataBXnor2_11_net, 
        DataBXnor2_12_net, DataBXnor2_13_net, DataBXnor2_14_net, 
        DataBXnor2_15_net, DataBXnor2_16_net, DataBXnor2_17_net, 
        DataBXnor2_18_net, DataBXnor2_19_net, DataBXnor2_20_net, 
        DataBXnor2_21_net, DataBXnor2_22_net, DataBXnor2_23_net, 
        DataBXnor2_24_net, DataBXnor2_25_net, DataBXnor2_26_net, 
        DataBXnor2_27_net, DataBXnor2_28_net, DataBXnor2_29_net, 
        DataBXnor2_30_net, DataBXnor2_31_net, addci, addco, 
        INV_0_Y, AND2_4_Y, AND2_60_Y, AND2_86_Y, AND2_30_Y, 
        AND2_64_Y, AND2_56_Y, AND2_71_Y, AND2_55_Y, AND2_77_Y, 
        AND2_33_Y, AND2_28_Y, AND2_51_Y, AND2_68_Y, AND2_49_Y, 
        AND2_42_Y, AND2_17_Y, AND2_16_Y, AND2_52_Y, AND2_43_Y, 
        AND2_1_Y, AND2_47_Y, AND2_84_Y, AND2_89_Y, AND2_72_Y, 
        AND2_65_Y, AND2_36_Y, AND2_35_Y, AND2_85_Y, AND2_66_Y, 
        AND2_27_Y, AND2_53_Y, AND2_88_Y, AND2_6_Y, AND2_87_Y, 
        XOR2_15_Y, XOR2_37_Y, XOR2_22_Y, XOR2_47_Y, XOR2_29_Y, 
        XOR2_23_Y, XOR2_34_Y, XOR2_17_Y, XOR2_1_Y, XOR2_42_Y, 
        XOR2_50_Y, XOR2_63_Y, XOR2_51_Y, XOR2_41_Y, XOR2_45_Y, 
        XOR2_46_Y, XOR2_56_Y, XOR2_59_Y, XOR2_40_Y, XOR2_21_Y, 
        XOR2_31_Y, XOR2_38_Y, XOR2_32_Y, XOR2_14_Y, XOR2_28_Y, 
        XOR2_30_Y, XOR2_35_Y, XOR2_36_Y, XOR2_12_Y, XOR2_54_Y, 
        XOR2_3_Y, XOR2_18_Y, AND2_62_Y, AO1_26_Y, AND2_24_Y, 
        AO1_44_Y, AND2_70_Y, AO1_45_Y, AND2_31_Y, AO1_37_Y, 
        AND2_90_Y, AO1_23_Y, AND2_54_Y, AO1_16_Y, AND2_39_Y, 
        AO1_47_Y, AND2_83_Y, AO1_53_Y, AND2_19_Y, AO1_11_Y, 
        AND2_79_Y, AO1_10_Y, AND2_23_Y, AO1_38_Y, AND2_69_Y, 
        AO1_29_Y, AND2_26_Y, AO1_33_Y, AND2_81_Y, AO1_54_Y, 
        AND2_46_Y, AO1_17_Y, AND2_14_Y, AND2_38_Y, AO1_43_Y, 
        AND2_82_Y, AO1_19_Y, AND2_18_Y, AO1_4_Y, AND2_78_Y, 
        AO1_41_Y, AND2_22_Y, AO1_15_Y, AND2_67_Y, AO1_1_Y, 
        AND2_25_Y, AO1_8_Y, AND2_80_Y, AND2_45_Y, AO1_46_Y, 
        AND2_13_Y, AO1_24_Y, AND2_63_Y, AO1_20_Y, AND2_11_Y, 
        AND2_40_Y, AO1_34_Y, AND2_7_Y, AND2_44_Y, AND2_0_Y, 
        AND2_48_Y, AND2_10_Y, AND2_75_Y, AND2_37_Y, AND2_29_Y, 
        AND2_61_Y, AND2_3_Y, AND2_58_Y, AND2_5_Y, AND2_50_Y, 
        AND2_8_Y, AND2_59_Y, AND2_32_Y, AND2_2_Y, AND2_34_Y, 
        AND2_76_Y, AND2_12_Y, AND2_73_Y, AND2_15_Y, AND2_57_Y, 
        AND2_21_Y, AND2_74_Y, AND2_41_Y, AND2_9_Y, AND2_20_Y, 
        OR3_0_Y, AO1_5_Y, AO1_51_Y, AO1_48_Y, AO1_55_Y, AO1_36_Y, 
        AO1_21_Y, AO1_30_Y, AO1_35_Y, AO1_14_Y, AO1_13_Y, 
        AO1_52_Y, AO1_7_Y, AO1_9_Y, AO1_39_Y, AO1_49_Y, AO1_27_Y, 
        AO1_40_Y, AO1_42_Y, AO1_0_Y, AO1_18_Y, AO1_28_Y, AO1_6_Y, 
        AO1_3_Y, AO1_12_Y, AO1_31_Y, AO1_32_Y, AO1_22_Y, AO1_50_Y, 
        AO1_2_Y, AO1_25_Y, XOR2_4_Y, XOR2_0_Y, XOR2_11_Y, 
        XOR2_58_Y, XOR2_6_Y, XOR2_7_Y, XOR2_2_Y, XOR2_13_Y, 
        XOR2_60_Y, XOR2_26_Y, XOR2_27_Y, XOR2_20_Y, XOR2_33_Y, 
        XOR2_8_Y, XOR2_55_Y, XOR2_57_Y, XOR2_52_Y, XOR2_61_Y, 
        XOR2_43_Y, XOR2_48_Y, XOR2_49_Y, XOR2_44_Y, XOR2_53_Y, 
        XOR2_39_Y, XOR2_9_Y, XOR2_10_Y, XOR2_5_Y, XOR2_16_Y, 
        XOR2_62_Y, XOR2_24_Y, XOR2_25_Y, XOR2_19_Y : std_logic ;
    begin   

    INV_0 : INV
      port map(A => addci, Y => INV_0_Y);
    AO1_52 : AO1
      port map(A => AND2_18_Y, B => AO1_30_Y, C => AO1_19_Y, Y => 
        AO1_52_Y);
    XNOR2_29_inst : XNOR2
      port map(A => DataB(29), B => AddsubAux_29_net, Y => 
        DataBXnor2_29_net);
    AND2_2 : AND2
      port map(A => AND2_45_Y, B => XOR2_1_Y, Y => AND2_2_Y);
    XNOR2_26_inst : XNOR2
      port map(A => DataB(26), B => AddsubAux_24_net, Y => 
        DataBXnor2_26_net);
    AND2_20 : AND2
      port map(A => AND2_50_Y, B => XOR2_3_Y, Y => AND2_20_Y);
    XOR2_Sum_30_inst : XOR2
      port map(A => XOR2_25_Y, B => AO1_2_Y, Y => Sum(30));
    AO1_11 : AO1
      port map(A => XOR2_21_Y, B => AND2_47_Y, C => AND2_84_Y, 
        Y => AO1_11_Y);
    AND2_11 : AND2
      port map(A => AND2_25_Y, B => AND2_80_Y, Y => AND2_11_Y);
    AND2_22 : AND2
      port map(A => AND2_19_Y, B => AND2_79_Y, Y => AND2_22_Y);
    AND2_71 : AND2
      port map(A => DataA(4), B => DataBXnor2_4_net, Y => 
        AND2_71_Y);
    XOR2_19 : XOR2
      port map(A => DataA(31), B => DataBXnor2_31_net, Y => 
        XOR2_19_Y);
    AND2_44 : AND2
      port map(A => AND2_40_Y, B => AND2_7_Y, Y => AND2_44_Y);
    AO1_31 : AO1
      port map(A => AND2_26_Y, B => AO1_3_Y, C => AO1_29_Y, Y => 
        AO1_31_Y);
    XOR2_1 : XOR2
      port map(A => DataA(8), B => DataBXnor2_8_net, Y => 
        XOR2_1_Y);
    XOR2_23 : XOR2
      port map(A => DataA(5), B => DataBXnor2_5_net, Y => 
        XOR2_23_Y);
    XNOR2_13_inst : XNOR2
      port map(A => DataB(13), B => AddsubAux_12_net, Y => 
        DataBXnor2_13_net);
    XOR2_47 : XOR2
      port map(A => DataA(3), B => DataBXnor2_3_net, Y => 
        XOR2_47_Y);
    XOR2_38 : XOR2
      port map(A => DataA(21), B => DataBXnor2_21_net, Y => 
        XOR2_38_Y);
    XNOR2_8_inst : XNOR2
      port map(A => DataB(8), B => AddsubAux_6_net, Y => 
        DataBXnor2_8_net);
    XOR2_Sum_21_inst : XOR2
      port map(A => XOR2_44_Y, B => AO1_18_Y, Y => Sum(21));
    XOR2_Sum_8_inst : XOR2
      port map(A => XOR2_60_Y, B => AO1_30_Y, Y => Sum(8));
    AO1_7 : AO1
      port map(A => XOR2_51_Y, B => AO1_52_Y, C => AND2_42_Y, 
        Y => AO1_7_Y);
    XOR2_Sum_25_inst : XOR2
      port map(A => XOR2_10_Y, B => AO1_12_Y, Y => Sum(25));
    AND2_18 : AND2
      port map(A => AND2_90_Y, B => AND2_54_Y, Y => AND2_18_Y);
    AND2_15 : AND2
      port map(A => AND2_3_Y, B => XOR2_40_Y, Y => AND2_15_Y);
    AND2_84 : AND2
      port map(A => DataA(19), B => DataBXnor2_19_net, Y => 
        AND2_84_Y);
    AO1_25 : AO1
      port map(A => XOR2_3_Y, B => AO1_2_Y, C => AND2_6_Y, Y => 
        AO1_25_Y);
    AND2_78 : AND2
      port map(A => AND2_39_Y, B => AND2_83_Y, Y => AND2_78_Y);
    AND2_75 : AND2
      port map(A => AND2_0_Y, B => AND2_25_Y, Y => AND2_75_Y);
    XOR2_45 : XOR2
      port map(A => DataA(14), B => DataBXnor2_14_net, Y => 
        XOR2_45_Y);
    XNOR2_31_inst : XNOR2
      port map(A => DataB(31), B => AddsubAux_29_net, Y => 
        DataBXnor2_31_net);
    AND2_1 : AND2
      port map(A => DataA(17), B => DataBXnor2_17_net, Y => 
        AND2_1_Y);
    XOR2_Sum_5_inst : XOR2
      port map(A => XOR2_7_Y, B => AO1_55_Y, Y => Sum(5));
    AND2_49 : AND2
      port map(A => DataA(11), B => DataBXnor2_11_net, Y => 
        AND2_49_Y);
    AO1_8 : AO1
      port map(A => AND2_14_Y, B => AO1_54_Y, C => AO1_17_Y, Y => 
        AO1_8_Y);
    XNOR2_11_inst : XNOR2
      port map(A => DataB(11), B => AddsubAux_6_net, Y => 
        DataBXnor2_11_net);
    AND2_10 : AND2
      port map(A => AND2_40_Y, B => AND2_22_Y, Y => AND2_10_Y);
    AND2_7 : AND2
      port map(A => AND2_63_Y, B => AND2_11_Y, Y => AND2_7_Y);
    XOR2_20 : XOR2
      port map(A => DataA(11), B => DataBXnor2_11_net, Y => 
        XOR2_20_Y);
    XOR2_63 : XOR2
      port map(A => DataA(11), B => DataBXnor2_11_net, Y => 
        XOR2_63_Y);
    AND2_70 : AND2
      port map(A => XOR2_29_Y, B => XOR2_23_Y, Y => AND2_70_Y);
    XNOR2_9_inst : XNOR2
      port map(A => DataB(9), B => AddsubAux_6_net, Y => 
        DataBXnor2_9_net);
    AND2_12 : AND2
      port map(A => AND2_61_Y, B => XOR2_45_Y, Y => AND2_12_Y);
    XOR2_52 : XOR2
      port map(A => DataA(16), B => DataBXnor2_16_net, Y => 
        XOR2_52_Y);
    AO1_42 : AO1
      port map(A => XOR2_40_Y, B => AO1_40_Y, C => AND2_47_Y, 
        Y => AO1_42_Y);
    AO1_50 : AO1
      port map(A => XOR2_12_Y, B => AO1_22_Y, C => AND2_53_Y, 
        Y => AO1_50_Y);
    AND2_72 : AND2
      port map(A => DataA(21), B => DataBXnor2_21_net, Y => 
        AND2_72_Y);
    AND2_61 : AND2
      port map(A => AND2_48_Y, B => AND2_39_Y, Y => AND2_61_Y);
    AO1_15 : AO1
      port map(A => AND2_69_Y, B => AO1_10_Y, C => AO1_38_Y, Y => 
        AO1_15_Y);
    BUFF_AddsubAux_6_inst : BUFF
      port map(A => Addsub, Y => AddsubAux_6_net);
    AND2_89 : AND2
      port map(A => DataA(20), B => DataBXnor2_20_net, Y => 
        AND2_89_Y);
    XOR2_24 : XOR2
      port map(A => DataA(29), B => DataBXnor2_29_net, Y => 
        XOR2_24_Y);
    AND2_57 : AND2
      port map(A => AND2_10_Y, B => XOR2_31_Y, Y => AND2_57_Y);
    XOR2_21 : XOR2
      port map(A => DataA(19), B => DataBXnor2_19_net, Y => 
        XOR2_21_Y);
    AO1_35 : AO1
      port map(A => XOR2_1_Y, B => AO1_30_Y, C => AND2_28_Y, Y => 
        AO1_35_Y);
    XNOR2_14_inst : XNOR2
      port map(A => DataB(14), B => AddsubAux_12_net, Y => 
        DataBXnor2_14_net);
    AND2_46 : AND2
      port map(A => XOR2_12_Y, B => XOR2_54_Y, Y => AND2_46_Y);
    AO1_53 : AO1
      port map(A => XOR2_59_Y, B => AND2_43_Y, C => AND2_1_Y, 
        Y => AO1_53_Y);
    XOR2_16 : XOR2
      port map(A => DataA(27), B => DataBXnor2_27_net, Y => 
        XOR2_16_Y);
    XOR2_60 : XOR2
      port map(A => DataA(8), B => DataBXnor2_8_net, Y => 
        XOR2_60_Y);
    AND2_68 : AND2
      port map(A => DataA(10), B => DataBXnor2_10_net, Y => 
        AND2_68_Y);
    AND2_65 : AND2
      port map(A => DataA(22), B => DataBXnor2_22_net, Y => 
        AND2_65_Y);
    AND2_43 : AND2
      port map(A => DataA(16), B => DataBXnor2_16_net, Y => 
        AND2_43_Y);
    AO1_24 : AO1
      port map(A => AND2_67_Y, B => AO1_41_Y, C => AO1_15_Y, Y => 
        AO1_24_Y);
    XNOR2_17_inst : XNOR2
      port map(A => DataB(17), B => AddsubAux_12_net, Y => 
        DataBXnor2_17_net);
    BUFF_AddsubAux_12_inst : BUFF
      port map(A => Addsub, Y => AddsubAux_12_net);
    XOR2_Sum_17_inst : XOR2
      port map(A => XOR2_61_Y, B => AO1_27_Y, Y => Sum(17));
    AND2_86 : AND2
      port map(A => DataBXnor2_0_net, B => INV_0_Y, Y => 
        AND2_86_Y);
    AND2_6 : AND2
      port map(A => DataA(30), B => DataBXnor2_30_net, Y => 
        AND2_6_Y);
    AND2_60 : AND2
      port map(A => DataA(0), B => INV_0_Y, Y => AND2_60_Y);
    XOR2_61 : XOR2
      port map(A => DataA(17), B => DataBXnor2_17_net, Y => 
        XOR2_61_Y);
    AND2_83 : AND2
      port map(A => XOR2_45_Y, B => XOR2_46_Y, Y => AND2_83_Y);
    XOR2_57 : XOR2
      port map(A => DataA(15), B => DataBXnor2_15_net, Y => 
        XOR2_57_Y);
    AND2_62 : AND2
      port map(A => XOR2_15_Y, B => XOR2_37_Y, Y => AND2_62_Y);
    XOR2_33 : XOR2
      port map(A => DataA(12), B => DataBXnor2_12_net, Y => 
        XOR2_33_Y);
    BUFF_AddsubAux_24_inst : BUFF
      port map(A => Addsub, Y => AddsubAux_24_net);
    XOR2_49 : XOR2
      port map(A => DataA(20), B => DataBXnor2_20_net, Y => 
        XOR2_49_Y);
    AO1_14 : AO1
      port map(A => AND2_90_Y, B => AO1_30_Y, C => AO1_37_Y, Y => 
        AO1_14_Y);
    XOR2_4 : XOR2
      port map(A => DataA(0), B => DataBXnor2_0_net, Y => 
        XOR2_4_Y);
    XNOR2_23_inst : XNOR2
      port map(A => DataB(23), B => AddsubAux_18_net, Y => 
        DataBXnor2_23_net);
    XOR2_55 : XOR2
      port map(A => DataA(14), B => DataBXnor2_14_net, Y => 
        XOR2_55_Y);
    AND2_24 : AND2
      port map(A => XOR2_22_Y, B => XOR2_47_Y, Y => AND2_24_Y);
    AO1_40 : AO1
      port map(A => AND2_19_Y, B => AO1_49_Y, C => AO1_53_Y, Y => 
        AO1_40_Y);
    AND2_31 : AND2
      port map(A => XOR2_34_Y, B => XOR2_17_Y, Y => AND2_31_Y);
    AO1_34 : AO1
      port map(A => AND2_11_Y, B => AO1_24_Y, C => AO1_20_Y, Y => 
        AO1_34_Y);
    XOR2_18 : XOR2
      port map(A => DataA(31), B => DataBXnor2_31_net, Y => 
        XOR2_18_Y);
    ADDERCI : XOR2
      port map(A => Cin, B => AddsubAux_29_net, Y => addci);
    XOR2_Sum_4_inst : XOR2
      port map(A => XOR2_6_Y, B => AO1_48_Y, Y => Sum(4));
    AO1_46 : AO1
      port map(A => AND2_78_Y, B => AO1_19_Y, C => AO1_4_Y, Y => 
        AO1_46_Y);
    XOR2_8 : XOR2
      port map(A => DataA(13), B => DataBXnor2_13_net, Y => 
        XOR2_8_Y);
    XNOR2_21_inst : XNOR2
      port map(A => DataB(21), B => AddsubAux_18_net, Y => 
        DataBXnor2_21_net);
    AO1_43 : AO1
      port map(A => AND2_31_Y, B => AO1_44_Y, C => AO1_45_Y, Y => 
        AO1_43_Y);
    XOR2_30 : XOR2
      port map(A => DataA(25), B => DataBXnor2_25_net, Y => 
        XOR2_30_Y);
    AND2_38 : AND2
      port map(A => AND2_62_Y, B => AND2_24_Y, Y => AND2_38_Y);
    AND2_35 : AND2
      port map(A => DataA(24), B => DataBXnor2_24_net, Y => 
        AND2_35_Y);
    AND2_29 : AND2
      port map(A => AND2_45_Y, B => AND2_90_Y, Y => AND2_29_Y);
    XNOR2_2_inst : XNOR2
      port map(A => DataB(2), B => AddsubAux_0_net, Y => 
        DataBXnor2_2_net);
    XOR2_34 : XOR2
      port map(A => DataA(6), B => DataBXnor2_6_net, Y => 
        XOR2_34_Y);
    XOR2_31 : XOR2
      port map(A => DataA(20), B => DataBXnor2_20_net, Y => 
        XOR2_31_Y);
    BUFF_AddsubAux_0_inst : BUFF
      port map(A => Addsub, Y => AddsubAux_0_net);
    AND2_3 : AND2
      port map(A => AND2_40_Y, B => AND2_19_Y, Y => AND2_3_Y);
    AO1_49 : AO1
      port map(A => AND2_13_Y, B => AO1_30_Y, C => AO1_46_Y, Y => 
        AO1_49_Y);
    AND2_30 : AND2
      port map(A => DataA(1), B => DataBXnor2_1_net, Y => 
        AND2_30_Y);
    XNOR2_0_inst : XNOR2
      port map(A => DataB(0), B => AddsubAux_0_net, Y => 
        DataBXnor2_0_net);
    XNOR2_24_inst : XNOR2
      port map(A => DataB(24), B => AddsubAux_24_net, Y => 
        DataBXnor2_24_net);
    AND2_14 : AND2
      port map(A => XOR2_3_Y, B => XOR2_18_Y, Y => AND2_14_Y);
    BUFF_AddsubAux_18_inst : BUFF
      port map(A => Addsub, Y => AddsubAux_18_net);
    XOR2_Sum_2_inst : XOR2
      port map(A => XOR2_11_Y, B => AO1_5_Y, Y => Sum(2));
    AND2_74 : AND2
      port map(A => AND2_0_Y, B => XOR2_28_Y, Y => AND2_74_Y);
    AND2_32 : AND2
      port map(A => AND2_37_Y, B => XOR2_34_Y, Y => AND2_32_Y);
    XOR2_Sum_23_inst : XOR2
      port map(A => XOR2_39_Y, B => AO1_6_Y, Y => Sum(23));
    XOR2_Sum_1_inst : XOR2
      port map(A => XOR2_0_Y, B => OR3_0_Y, Y => Sum(1));
    XOR2_46 : XOR2
      port map(A => DataA(15), B => DataBXnor2_15_net, Y => 
        XOR2_46_Y);
    XOR2_Sum_16_inst : XOR2
      port map(A => XOR2_52_Y, B => AO1_49_Y, Y => Sum(16));
    AO1_2 : AO1
      port map(A => AND2_46_Y, B => AO1_22_Y, C => AO1_54_Y, Y => 
        AO1_2_Y);
    XNOR2_27_inst : XNOR2
      port map(A => DataB(27), B => AddsubAux_24_net, Y => 
        DataBXnor2_27_net);
    XOR2_Sum_7_inst : XOR2
      port map(A => XOR2_13_Y, B => AO1_21_Y, Y => Sum(7));
    AND2_26 : AND2
      port map(A => XOR2_28_Y, B => XOR2_30_Y, Y => AND2_26_Y);
    XOR2_9 : XOR2
      port map(A => DataA(24), B => DataBXnor2_24_net, Y => 
        XOR2_9_Y);
    XOR2_59 : XOR2
      port map(A => DataA(17), B => DataBXnor2_17_net, Y => 
        XOR2_59_Y);
    AND2_23 : AND2
      port map(A => XOR2_31_Y, B => XOR2_38_Y, Y => AND2_23_Y);
    XOR2_5 : XOR2
      port map(A => DataA(26), B => DataBXnor2_26_net, Y => 
        XOR2_5_Y);
    XNOR2_12_inst : XNOR2
      port map(A => DataB(12), B => AddsubAux_12_net, Y => 
        DataBXnor2_12_net);
    AO1_28 : AO1
      port map(A => AND2_23_Y, B => AO1_0_Y, C => AO1_10_Y, Y => 
        AO1_28_Y);
    AND2_19 : AND2
      port map(A => XOR2_56_Y, B => XOR2_59_Y, Y => AND2_19_Y);
    AND2_79 : AND2
      port map(A => XOR2_40_Y, B => XOR2_21_Y, Y => AND2_79_Y);
    XOR2_22 : XOR2
      port map(A => DataA(2), B => DataBXnor2_2_net, Y => 
        XOR2_22_Y);
    AO1_1 : AO1
      port map(A => AND2_81_Y, B => AO1_29_Y, C => AO1_33_Y, Y => 
        AO1_1_Y);
    XOR2_13 : XOR2
      port map(A => DataA(7), B => DataBXnor2_7_net, Y => 
        XOR2_13_Y);
    XNOR2_6_inst : XNOR2
      port map(A => DataB(6), B => AddsubAux_6_net, Y => 
        DataBXnor2_6_net);
    XNOR2_18_inst : XNOR2
      port map(A => DataB(18), B => AddsubAux_18_net, Y => 
        DataBXnor2_18_net);
    AND2_51 : AND2
      port map(A => DataA(9), B => DataBXnor2_9_net, Y => 
        AND2_51_Y);
    XOR2_Sum_29_inst : XOR2
      port map(A => XOR2_24_Y, B => AO1_50_Y, Y => Sum(29));
    AO1_3 : AO1
      port map(A => AND2_63_Y, B => AO1_49_Y, C => AO1_24_Y, Y => 
        AO1_3_Y);
    AND2_64 : AND2
      port map(A => DataA(2), B => DataBXnor2_2_net, Y => 
        AND2_64_Y);
    AND2_47 : AND2
      port map(A => DataA(18), B => DataBXnor2_18_net, Y => 
        AND2_47_Y);
    AO1_51 : AO1
      port map(A => XOR2_22_Y, B => AO1_5_Y, C => AND2_64_Y, Y => 
        AO1_51_Y);
    AO1_18 : AO1
      port map(A => XOR2_31_Y, B => AO1_0_Y, C => AND2_89_Y, Y => 
        AO1_18_Y);
    XOR2_48 : XOR2
      port map(A => DataA(19), B => DataBXnor2_19_net, Y => 
        XOR2_48_Y);
    AO1_47 : AO1
      port map(A => XOR2_46_Y, B => AND2_16_Y, C => AND2_52_Y, 
        Y => AO1_47_Y);
    XOR2_Sum_14_inst : XOR2
      port map(A => XOR2_55_Y, B => AO1_9_Y, Y => Sum(14));
    AND2_16 : AND2
      port map(A => DataA(14), B => DataBXnor2_14_net, Y => 
        AND2_16_Y);
    XOR2_Sum_18_inst : XOR2
      port map(A => XOR2_43_Y, B => AO1_40_Y, Y => Sum(18));
    XOR2_Sum_20_inst : XOR2
      port map(A => XOR2_49_Y, B => AO1_0_Y, Y => Sum(20));
    AND2_76 : AND2
      port map(A => AND2_48_Y, B => XOR2_51_Y, Y => AND2_76_Y);
    AO1_38 : AO1
      port map(A => XOR2_14_Y, B => AND2_65_Y, C => AND2_36_Y, 
        Y => AO1_38_Y);
    XNOR2_4_inst : XNOR2
      port map(A => DataB(4), B => AddsubAux_0_net, Y => 
        DataBXnor2_4_net);
    XOR2_62 : XOR2
      port map(A => DataA(28), B => DataBXnor2_28_net, Y => 
        XOR2_62_Y);
    AND2_13 : AND2
      port map(A => AND2_18_Y, B => AND2_78_Y, Y => AND2_13_Y);
    AND2_87 : AND2
      port map(A => DataA(31), B => DataBXnor2_31_net, Y => 
        AND2_87_Y);
    ADDERCO : XNOR2
      port map(A => addco, B => AddsubAux_29_net, Y => Cout);
    AND2_73 : AND2
      port map(A => AND2_40_Y, B => XOR2_56_Y, Y => AND2_73_Y);
    XOR2_10 : XOR2
      port map(A => DataA(25), B => DataBXnor2_25_net, Y => 
        XOR2_10_Y);
    AND2_58 : AND2
      port map(A => AND2_10_Y, B => AND2_23_Y, Y => AND2_58_Y);
    AND2_55 : AND2
      port map(A => DataA(5), B => DataBXnor2_5_net, Y => 
        AND2_55_Y);
    AND2_69 : AND2
      port map(A => XOR2_32_Y, B => XOR2_14_Y, Y => AND2_69_Y);
    XOR2_27 : XOR2
      port map(A => DataA(10), B => DataBXnor2_10_net, Y => 
        XOR2_27_Y);
    XOR2_7 : XOR2
      port map(A => DataA(5), B => DataBXnor2_5_net, Y => 
        XOR2_7_Y);
    AND2_5 : AND2
      port map(A => AND2_0_Y, B => AND2_26_Y, Y => AND2_5_Y);
    XOR2_56 : XOR2
      port map(A => DataA(16), B => DataBXnor2_16_net, Y => 
        XOR2_56_Y);
    XOR2_14 : XOR2
      port map(A => DataA(23), B => DataBXnor2_23_net, Y => 
        XOR2_14_Y);
    AND2_50 : AND2
      port map(A => AND2_75_Y, B => AND2_46_Y, Y => AND2_50_Y);
    XOR2_11 : XOR2
      port map(A => DataA(2), B => DataBXnor2_2_net, Y => 
        XOR2_11_Y);
    XNOR2_Sum_0_inst : XNOR2
      port map(A => XOR2_4_Y, B => addci, Y => Sum(0));
    XOR2_Sum_22_inst : XOR2
      port map(A => XOR2_53_Y, B => AO1_28_Y, Y => Sum(22));
    XNOR2_15_inst : XNOR2
      port map(A => DataB(15), B => AddsubAux_12_net, Y => 
        DataBXnor2_15_net);
    AND2_52 : AND2
      port map(A => DataA(15), B => DataBXnor2_15_net, Y => 
        AND2_52_Y);
    XOR2_25 : XOR2
      port map(A => DataA(30), B => DataBXnor2_30_net, Y => 
        XOR2_25_Y);
    AO1_22 : AO1
      port map(A => AND2_25_Y, B => AO1_3_Y, C => AO1_1_Y, Y => 
        AO1_22_Y);
    XOR2_Sum_11_inst : XOR2
      port map(A => XOR2_20_Y, B => AO1_13_Y, Y => Sum(11));
    XOR2_Sum_15_inst : XOR2
      port map(A => XOR2_57_Y, B => AO1_39_Y, Y => Sum(15));
    AO1_addco : AO1
      port map(A => AND2_7_Y, B => AO1_49_Y, C => AO1_34_Y, Y => 
        addco);
    AO1_55 : AO1
      port map(A => XOR2_29_Y, B => AO1_48_Y, C => AND2_71_Y, 
        Y => AO1_55_Y);
    AND2_34 : AND2
      port map(A => AND2_29_Y, B => XOR2_50_Y, Y => AND2_34_Y);
    AO1_41 : AO1
      port map(A => AND2_79_Y, B => AO1_53_Y, C => AO1_11_Y, Y => 
        AO1_41_Y);
    AND2_66 : AND2
      port map(A => DataA(26), B => DataBXnor2_26_net, Y => 
        AND2_66_Y);
    AO1_6 : AO1
      port map(A => XOR2_32_Y, B => AO1_28_Y, C => AND2_65_Y, 
        Y => AO1_6_Y);
    XNOR2_1_inst : XNOR2
      port map(A => DataB(1), B => AddsubAux_0_net, Y => 
        DataBXnor2_1_net);
    XNOR2_22_inst : XNOR2
      port map(A => DataB(22), B => AddsubAux_18_net, Y => 
        DataBXnor2_22_net);
    AND2_63 : AND2
      port map(A => AND2_22_Y, B => AND2_67_Y, Y => AND2_63_Y);
    BUFF_AddsubAux_29_inst : BUFF
      port map(A => Addsub, Y => AddsubAux_29_net);
    AO1_12 : AO1
      port map(A => XOR2_28_Y, B => AO1_3_Y, C => AND2_35_Y, Y => 
        AO1_12_Y);
    XOR2_32 : XOR2
      port map(A => DataA(22), B => DataBXnor2_22_net, Y => 
        XOR2_32_Y);
    OR3_0 : OR3
      port map(A => AND2_4_Y, B => AND2_60_Y, C => AND2_86_Y, 
        Y => OR3_0_Y);
    AND2_9 : AND2
      port map(A => AND2_75_Y, B => XOR2_12_Y, Y => AND2_9_Y);
    AND2_90 : AND2
      port map(A => XOR2_1_Y, B => XOR2_42_Y, Y => AND2_90_Y);
    XOR2_58 : XOR2
      port map(A => DataA(3), B => DataBXnor2_3_net, Y => 
        XOR2_58_Y);
    XOR2_43 : XOR2
      port map(A => DataA(18), B => DataBXnor2_18_net, Y => 
        XOR2_43_Y);
    XNOR2_28_inst : XNOR2
      port map(A => DataB(28), B => AddsubAux_24_net, Y => 
        DataBXnor2_28_net);
    XNOR2_3_inst : XNOR2
      port map(A => DataB(3), B => AddsubAux_0_net, Y => 
        DataBXnor2_3_net);
    AO1_32 : AO1
      port map(A => XOR2_35_Y, B => AO1_31_Y, C => AND2_66_Y, 
        Y => AO1_32_Y);
    AO1_9 : AO1
      port map(A => AND2_39_Y, B => AO1_52_Y, C => AO1_16_Y, Y => 
        AO1_9_Y);
    AND2_39 : AND2
      port map(A => XOR2_51_Y, B => XOR2_41_Y, Y => AND2_39_Y);
    XOR2_Sum_31_inst : XOR2
      port map(A => XOR2_19_Y, B => AO1_25_Y, Y => Sum(31));
    AND2_27 : AND2
      port map(A => DataA(27), B => DataBXnor2_27_net, Y => 
        AND2_27_Y);
    AO1_54 : AO1
      port map(A => XOR2_54_Y, B => AND2_53_Y, C => AND2_88_Y, 
        Y => AO1_54_Y);
    AO1_20 : AO1
      port map(A => AND2_80_Y, B => AO1_1_Y, C => AO1_8_Y, Y => 
        AO1_20_Y);
    AO1_0 : AO1
      port map(A => AND2_22_Y, B => AO1_49_Y, C => AO1_41_Y, Y => 
        AO1_0_Y);
    XOR2_29 : XOR2
      port map(A => DataA(4), B => DataBXnor2_4_net, Y => 
        XOR2_29_Y);
    AO1_45 : AO1
      port map(A => XOR2_17_Y, B => AND2_77_Y, C => AND2_33_Y, 
        Y => AO1_45_Y);
    XOR2_40 : XOR2
      port map(A => DataA(18), B => DataBXnor2_18_net, Y => 
        XOR2_40_Y);
    XOR2_2 : XOR2
      port map(A => DataA(6), B => DataBXnor2_6_net, Y => 
        XOR2_2_Y);
    XOR2_37 : XOR2
      port map(A => DataA(1), B => DataBXnor2_1_net, Y => 
        XOR2_37_Y);
    AND2_36 : AND2
      port map(A => DataA(23), B => DataBXnor2_23_net, Y => 
        AND2_36_Y);
    AO1_26 : AO1
      port map(A => XOR2_47_Y, B => AND2_64_Y, C => AND2_56_Y, 
        Y => AO1_26_Y);
    AO1_23 : AO1
      port map(A => XOR2_63_Y, B => AND2_68_Y, C => AND2_49_Y, 
        Y => AO1_23_Y);
    XOR2_Sum_6_inst : XOR2
      port map(A => XOR2_2_Y, B => AO1_36_Y, Y => Sum(6));
    AND2_33 : AND2
      port map(A => DataA(7), B => DataBXnor2_7_net, Y => 
        AND2_33_Y);
    XOR2_44 : XOR2
      port map(A => DataA(21), B => DataBXnor2_21_net, Y => 
        XOR2_44_Y);
    XNOR2_25_inst : XNOR2
      port map(A => DataB(25), B => AddsubAux_24_net, Y => 
        DataBXnor2_25_net);
    AO1_10 : AO1
      port map(A => XOR2_38_Y, B => AND2_89_Y, C => AND2_72_Y, 
        Y => AO1_10_Y);
    XOR2_41 : XOR2
      port map(A => DataA(13), B => DataBXnor2_13_net, Y => 
        XOR2_41_Y);
    XOR2_35 : XOR2
      port map(A => DataA(26), B => DataBXnor2_26_net, Y => 
        XOR2_35_Y);
    AO1_30 : AO1
      port map(A => AND2_82_Y, B => AO1_48_Y, C => AO1_43_Y, Y => 
        AO1_30_Y);
    AND2_41 : AND2
      port map(A => AND2_5_Y, B => XOR2_35_Y, Y => AND2_41_Y);
    AND2_0 : AND2
      port map(A => AND2_40_Y, B => AND2_63_Y, Y => AND2_0_Y);
    XOR2_Sum_27_inst : XOR2
      port map(A => XOR2_16_Y, B => AO1_32_Y, Y => Sum(27));
    AO1_16 : AO1
      port map(A => XOR2_41_Y, B => AND2_42_Y, C => AND2_17_Y, 
        Y => AO1_16_Y);
    AND2_17 : AND2
      port map(A => DataA(13), B => DataBXnor2_13_net, Y => 
        AND2_17_Y);
    AO1_29 : AO1
      port map(A => XOR2_30_Y, B => AND2_35_Y, C => AND2_85_Y, 
        Y => AO1_29_Y);
    XOR2_6 : XOR2
      port map(A => DataA(4), B => DataBXnor2_4_net, Y => 
        XOR2_6_Y);
    AND2_77 : AND2
      port map(A => DataA(6), B => DataBXnor2_6_net, Y => 
        AND2_77_Y);
    AND2_54 : AND2
      port map(A => XOR2_50_Y, B => XOR2_63_Y, Y => AND2_54_Y);
    AO1_13 : AO1
      port map(A => XOR2_50_Y, B => AO1_14_Y, C => AND2_68_Y, 
        Y => AO1_13_Y);
    XOR2_53 : XOR2
      port map(A => DataA(22), B => DataBXnor2_22_net, Y => 
        XOR2_53_Y);
    AO1_36 : AO1
      port map(A => AND2_70_Y, B => AO1_48_Y, C => AO1_44_Y, Y => 
        AO1_36_Y);
    AND2_81 : AND2
      port map(A => XOR2_35_Y, B => XOR2_36_Y, Y => AND2_81_Y);
    AO1_33 : AO1
      port map(A => XOR2_36_Y, B => AND2_66_Y, C => AND2_27_Y, 
        Y => AO1_33_Y);
    AO1_44 : AO1
      port map(A => XOR2_23_Y, B => AND2_71_Y, C => AND2_55_Y, 
        Y => AO1_44_Y);
    XOR2_12 : XOR2
      port map(A => DataA(28), B => DataBXnor2_28_net, Y => 
        XOR2_12_Y);
    AND2_48 : AND2
      port map(A => AND2_45_Y, B => AND2_18_Y, Y => AND2_48_Y);
    AND2_45 : AND2
      port map(A => AND2_38_Y, B => AND2_82_Y, Y => AND2_45_Y);
    AO1_19 : AO1
      port map(A => AND2_54_Y, B => AO1_37_Y, C => AO1_23_Y, Y => 
        AO1_19_Y);
    XNOR2_30_inst : XNOR2
      port map(A => DataB(30), B => AddsubAux_29_net, Y => 
        DataBXnor2_30_net);
    XOR2_26 : XOR2
      port map(A => DataA(9), B => DataBXnor2_9_net, Y => 
        XOR2_26_Y);
    XNOR2_10_inst : XNOR2
      port map(A => DataB(10), B => AddsubAux_6_net, Y => 
        DataBXnor2_10_net);
    AND2_59 : AND2
      port map(A => AND2_38_Y, B => XOR2_29_Y, Y => AND2_59_Y);
    XOR2_Sum_13_inst : XOR2
      port map(A => XOR2_8_Y, B => AO1_7_Y, Y => Sum(13));
    AND2_4 : AND2
      port map(A => DataA(0), B => DataBXnor2_0_net, Y => 
        AND2_4_Y);
    AO1_39 : AO1
      port map(A => XOR2_45_Y, B => AO1_9_Y, C => AND2_16_Y, Y => 
        AO1_39_Y);
    AND2_40 : AND2
      port map(A => AND2_45_Y, B => AND2_13_Y, Y => AND2_40_Y);
    AND2_88 : AND2
      port map(A => DataA(29), B => DataBXnor2_29_net, Y => 
        AND2_88_Y);
    AND2_85 : AND2
      port map(A => DataA(25), B => DataBXnor2_25_net, Y => 
        AND2_85_Y);
    XOR2_Sum_9_inst : XOR2
      port map(A => XOR2_26_Y, B => AO1_35_Y, Y => Sum(9));
    XOR2_50 : XOR2
      port map(A => DataA(10), B => DataBXnor2_10_net, Y => 
        XOR2_50_Y);
    AND2_42 : AND2
      port map(A => DataA(12), B => DataBXnor2_12_net, Y => 
        AND2_42_Y);
    XNOR2_19_inst : XNOR2
      port map(A => DataB(19), B => AddsubAux_18_net, Y => 
        DataBXnor2_19_net);
    XNOR2_16_inst : XNOR2
      port map(A => DataB(16), B => AddsubAux_12_net, Y => 
        DataBXnor2_16_net);
    AO1_5 : AO1
      port map(A => XOR2_37_Y, B => OR3_0_Y, C => AND2_30_Y, Y => 
        AO1_5_Y);
    AND2_67 : AND2
      port map(A => AND2_23_Y, B => AND2_69_Y, Y => AND2_67_Y);
    XOR2_39 : XOR2
      port map(A => DataA(23), B => DataBXnor2_23_net, Y => 
        XOR2_39_Y);
    AND2_8 : AND2
      port map(A => AND2_62_Y, B => XOR2_22_Y, Y => AND2_8_Y);
    AND2_80 : AND2
      port map(A => AND2_46_Y, B => AND2_14_Y, Y => AND2_80_Y);
    XOR2_3 : XOR2
      port map(A => DataA(30), B => DataBXnor2_30_net, Y => 
        XOR2_3_Y);
    XOR2_54 : XOR2
      port map(A => DataA(29), B => DataBXnor2_29_net, Y => 
        XOR2_54_Y);
    XOR2_51 : XOR2
      port map(A => DataA(12), B => DataBXnor2_12_net, Y => 
        XOR2_51_Y);
    AO1_27 : AO1
      port map(A => XOR2_56_Y, B => AO1_49_Y, C => AND2_43_Y, 
        Y => AO1_27_Y);
    AND2_82 : AND2
      port map(A => AND2_70_Y, B => AND2_31_Y, Y => AND2_82_Y);
    XNOR2_7_inst : XNOR2
      port map(A => DataB(7), B => AddsubAux_6_net, Y => 
        DataBXnor2_7_net);
    AND2_56 : AND2
      port map(A => DataA(3), B => DataBXnor2_3_net, Y => 
        AND2_56_Y);
    XOR2_17 : XOR2
      port map(A => DataA(7), B => DataBXnor2_7_net, Y => 
        XOR2_17_Y);
    AND2_53 : AND2
      port map(A => DataA(28), B => DataBXnor2_28_net, Y => 
        AND2_53_Y);
    XOR2_28 : XOR2
      port map(A => DataA(24), B => DataBXnor2_24_net, Y => 
        XOR2_28_Y);
    XOR2_Sum_19_inst : XOR2
      port map(A => XOR2_48_Y, B => AO1_42_Y, Y => Sum(19));
    XOR2_Sum_26_inst : XOR2
      port map(A => XOR2_5_Y, B => AO1_31_Y, Y => Sum(26));
    XOR2_15 : XOR2
      port map(A => DataA(0), B => DataBXnor2_0_net, Y => 
        XOR2_15_Y);
    AO1_17 : AO1
      port map(A => XOR2_18_Y, B => AND2_6_Y, C => AND2_87_Y, 
        Y => AO1_17_Y);
    XNOR2_5_inst : XNOR2
      port map(A => DataB(5), B => AddsubAux_0_net, Y => 
        DataBXnor2_5_net);
    XOR2_Sum_3_inst : XOR2
      port map(A => XOR2_58_Y, B => AO1_51_Y, Y => Sum(3));
    XOR2_Sum_10_inst : XOR2
      port map(A => XOR2_27_Y, B => AO1_14_Y, Y => Sum(10));
    AND2_21 : AND2
      port map(A => AND2_58_Y, B => XOR2_32_Y, Y => AND2_21_Y);
    AO1_37 : AO1
      port map(A => XOR2_42_Y, B => AND2_28_Y, C => AND2_51_Y, 
        Y => AO1_37_Y);
    XOR2_0 : XOR2
      port map(A => DataA(1), B => DataBXnor2_1_net, Y => 
        XOR2_0_Y);
    AO1_4 : AO1
      port map(A => AND2_83_Y, B => AO1_16_Y, C => AO1_47_Y, Y => 
        AO1_4_Y);
    AND2_37 : AND2
      port map(A => AND2_38_Y, B => AND2_70_Y, Y => AND2_37_Y);
    XOR2_42 : XOR2
      port map(A => DataA(9), B => DataBXnor2_9_net, Y => 
        XOR2_42_Y);
    AO1_21 : AO1
      port map(A => XOR2_34_Y, B => AO1_36_Y, C => AND2_77_Y, 
        Y => AO1_21_Y);
    XOR2_36 : XOR2
      port map(A => DataA(27), B => DataBXnor2_27_net, Y => 
        XOR2_36_Y);
    XNOR2_20_inst : XNOR2
      port map(A => DataB(20), B => AddsubAux_18_net, Y => 
        DataBXnor2_20_net);
    AND2_28 : AND2
      port map(A => DataA(8), B => DataBXnor2_8_net, Y => 
        AND2_28_Y);
    AND2_25 : AND2
      port map(A => AND2_26_Y, B => AND2_81_Y, Y => AND2_25_Y);
    AO1_48 : AO1
      port map(A => AND2_24_Y, B => AO1_5_Y, C => AO1_26_Y, Y => 
        AO1_48_Y);
    XOR2_Sum_12_inst : XOR2
      port map(A => XOR2_33_Y, B => AO1_52_Y, Y => Sum(12));
    XOR2_Sum_24_inst : XOR2
      port map(A => XOR2_9_Y, B => AO1_3_Y, Y => Sum(24));
    XOR2_Sum_28_inst : XOR2
      port map(A => XOR2_62_Y, B => AO1_22_Y, Y => Sum(28));
end DEF_ARCH;


library ieee;
use ieee.std_logic_1164.all;
library proasic3;

entity mult_apa3 is 
    port( DataA : in std_logic_vector(31 downto 0);
          DataB : in std_logic_vector(1 downto 0);
          Mult : out std_logic_vector(33 downto 0)) ;
end mult_apa3;


architecture DEF_ARCH of  mult_apa3 is

    component XOR2
        port(A, B : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component AND2
        port(A, B : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component AO1
        port(A, B, C : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component MX2
        port(A, B, S : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component AND2A
        port(A, B : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component BUFF
        port(A : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component AND3
        port(A, B, C : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component OR3
        port(A, B, C : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component GND
        port( Y : out std_logic);
    end component;

    signal S_0_net, EBAR, PP0_1_net, PP0_2_net, PP0_3_net, 
        PP0_4_net, PP0_5_net, PP0_6_net, PP0_7_net, PP0_8_net, 
        PP0_9_net, PP0_10_net, PP0_11_net, PP0_12_net, PP0_13_net, 
        PP0_14_net, PP0_15_net, PP0_16_net, PP0_17_net, 
        PP0_18_net, PP0_19_net, PP0_20_net, PP0_21_net, 
        PP0_22_net, PP0_23_net, PP0_24_net, PP0_25_net, 
        PP0_26_net, PP0_27_net, PP0_28_net, PP0_29_net, 
        PP0_30_net, PP0_31_net, PP0_32_net, BUFF_0_Y, BUFF_1_Y, 
        BUFF_2_Y, XOR2_27_Y, XOR2_37_Y, AND2A_1_Y, AND2_121_Y, 
        MX2_23_Y, AND2_76_Y, MX2_4_Y, AND2_117_Y, MX2_27_Y, 
        AND2_56_Y, MX2_11_Y, AND2_125_Y, MX2_22_Y, AND2_27_Y, 
        MX2_10_Y, AND2_35_Y, MX2_12_Y, AND2_75_Y, MX2_8_Y, 
        AND2_80_Y, MX2_16_Y, AND2_34_Y, MX2_0_Y, AND2_30_Y, 
        MX2_19_Y, AND2A_0_Y, AND2_131_Y, MX2_7_Y, AND2_102_Y, 
        MX2_28_Y, AND2_143_Y, AND2_14_Y, MX2_9_Y, AND2_1_Y, 
        MX2_24_Y, AND2_114_Y, MX2_20_Y, AND2_153_Y, MX2_13_Y, 
        AND2_59_Y, MX2_15_Y, AND2_120_Y, MX2_17_Y, AND2_154_Y, 
        MX2_5_Y, AND2_113_Y, MX2_30_Y, AND2A_2_Y, AND2_6_Y, 
        MX2_6_Y, AND2_123_Y, MX2_18_Y, AND2_4_Y, MX2_26_Y, 
        AND2_148_Y, MX2_31_Y, MX2_1_Y, AND2_116_Y, MX2_25_Y, 
        AND2_42_Y, MX2_14_Y, AND2_118_Y, MX2_3_Y, AND2_106_Y, 
        MX2_21_Y, AND2_108_Y, MX2_29_Y, AND2_115_Y, MX2_2_Y, 
        OR3_0_Y, AND3_0_Y, AND2_28_Y, AND2_64_Y, AND2_134_Y, 
        AND2_25_Y, AND2_41_Y, AND2_79_Y, AND2_139_Y, AND2_46_Y, 
        AND2_128_Y, AND2_95_Y, AND2_67_Y, AND2_22_Y, AND2_157_Y, 
        AND2_2_Y, AND2_19_Y, AND2_54_Y, AND2_26_Y, AND2_136_Y, 
        AND2_49_Y, AND2_66_Y, AND2_39_Y, AND2_138_Y, AND2_110_Y, 
        AND2_127_Y, AND2_137_Y, AND2_20_Y, AND2_141_Y, AND2_88_Y, 
        AND2_9_Y, AND2_47_Y, AND2_13_Y, AND2_107_Y, XOR2_50_Y, 
        XOR2_65_Y, XOR2_28_Y, XOR2_26_Y, XOR2_51_Y, XOR2_40_Y, 
        XOR2_3_Y, XOR2_60_Y, XOR2_42_Y, XOR2_18_Y, XOR2_38_Y, 
        XOR2_1_Y, XOR2_21_Y, XOR2_17_Y, XOR2_61_Y, XOR2_6_Y, 
        XOR2_5_Y, XOR2_54_Y, XOR2_20_Y, XOR2_43_Y, XOR2_58_Y, 
        XOR2_25_Y, XOR2_48_Y, XOR2_41_Y, XOR2_19_Y, XOR2_35_Y, 
        XOR2_34_Y, XOR2_11_Y, XOR2_47_Y, XOR2_63_Y, XOR2_39_Y, 
        XOR2_2_Y, XOR2_23_Y, AND2_112_Y, AO1_32_Y, AND2_135_Y, 
        AO1_87_Y, AND2_53_Y, AO1_36_Y, AND2_119_Y, AO1_5_Y, 
        AND2_86_Y, AO1_46_Y, AND2_8_Y, AO1_57_Y, AND2_87_Y, 
        AO1_30_Y, AND2_62_Y, AO1_21_Y, AND2_158_Y, AO1_37_Y, 
        AND2_72_Y, AO1_14_Y, AND2_147_Y, AO1_85_Y, AND2_5_Y, 
        AO1_63_Y, AND2_74_Y, AO1_11_Y, AND2_149_Y, AO1_79_Y, 
        AND2_124_Y, AO1_39_Y, AND2_43_Y, AND2_126_Y, AND2_15_Y, 
        AO1_54_Y, AND2_98_Y, AO1_61_Y, AND2_38_Y, AO1_2_Y, 
        AND2_91_Y, AO1_73_Y, AND2_111_Y, AO1_51_Y, AND2_40_Y, 
        AO1_0_Y, AND2_93_Y, AO1_69_Y, AND2_68_Y, AO1_26_Y, 
        AND2_151_Y, AO1_65_Y, AND2_70_Y, AO1_27_Y, AND2_99_Y, 
        AO1_41_Y, AND2_48_Y, AO1_50_Y, AND2_130_Y, AO1_19_Y, 
        AND2_44_Y, AO1_91_Y, AND2_57_Y, AO1_67_Y, AND2_132_Y, 
        AO1_16_Y, AND2_45_Y, AO1_84_Y, AND2_16_Y, AO1_42_Y, 
        AND2_78_Y, AND2_18_Y, AND2_77_Y, AND2_29_Y, AND2_96_Y, 
        AO1_48_Y, AND2_17_Y, AO1_22_Y, AND2_37_Y, AO1_1_Y, 
        AND2_97_Y, AO1_43_Y, AND2_21_Y, AO1_18_Y, AND2_150_Y, 
        AO1_72_Y, AND2_63_Y, AO1_13_Y, AND2_152_Y, AO1_75_Y, 
        AND2_71_Y, AO1_89_Y, AND2_24_Y, AO1_93_Y, AND2_90_Y, 
        AO1_20_Y, AND2_7_Y, AO1_92_Y, AND2_32_Y, AO1_70_Y, 
        AND2_92_Y, AO1_17_Y, AND2_12_Y, AO1_86_Y, AND2_142_Y, 
        AO1_47_Y, AND2_60_Y, AO1_81_Y, AND2_144_Y, AND2_33_Y, 
        AND2_122_Y, AND2_50_Y, AND2_105_Y, AND2_133_Y, AND2_51_Y, 
        AND2_109_Y, AND2_83_Y, AO1_82_Y, AND2_3_Y, AO1_24_Y, 
        AND2_85_Y, AO1_83_Y, AND2_10_Y, AO1_3_Y, AND2_100_Y, 
        AO1_6_Y, AND2_0_Y, AO1_78_Y, AND2_52_Y, AO1_56_Y, 
        AND2_104_Y, AO1_33_Y, AND2_73_Y, AO1_77_Y, AND2_11_Y, 
        AO1_52_Y, AND2_89_Y, AO1_8_Y, AND2_23_Y, AO1_45_Y, 
        AND2_82_Y, AND2_145_Y, AND2_81_Y, AND2_140_Y, AND2_36_Y, 
        AND2_84_Y, AND2_61_Y, AND2_146_Y, AND2_69_Y, AND2_155_Y, 
        AND2_65_Y, AND2_101_Y, AND2_55_Y, AND2_94_Y, AND2_156_Y, 
        AND2_58_Y, AND2_31_Y, AO1_71_Y, AND2_103_Y, AND2_129_Y, 
        AO1_7_Y, AO1_76_Y, AO1_40_Y, AO1_80_Y, AO1_44_Y, AO1_59_Y, 
        AO1_66_Y, AO1_49_Y, AO1_62_Y, AO1_68_Y, AO1_58_Y, 
        AO1_35_Y, AO1_9_Y, AO1_55_Y, AO1_28_Y, AO1_10_Y, AO1_23_Y, 
        AO1_31_Y, AO1_64_Y, AO1_38_Y, AO1_15_Y, AO1_60_Y, 
        AO1_34_Y, AO1_88_Y, AO1_29_Y, AO1_90_Y, AO1_4_Y, AO1_12_Y, 
        AO1_74_Y, AO1_53_Y, AO1_25_Y, XOR2_62_Y, XOR2_7_Y, 
        XOR2_22_Y, XOR2_31_Y, XOR2_14_Y, XOR2_52_Y, XOR2_64_Y, 
        XOR2_13_Y, XOR2_4_Y, XOR2_56_Y, XOR2_33_Y, XOR2_44_Y, 
        XOR2_55_Y, XOR2_32_Y, XOR2_16_Y, XOR2_53_Y, XOR2_66_Y, 
        XOR2_15_Y, XOR2_45_Y, XOR2_30_Y, XOR2_0_Y, XOR2_12_Y, 
        XOR2_29_Y, XOR2_24_Y, XOR2_10_Y, XOR2_49_Y, XOR2_59_Y, 
        XOR2_9_Y, XOR2_8_Y, XOR2_57_Y, XOR2_36_Y, XOR2_46_Y, 
        GND_1_net : std_logic ;
    begin   

    GND_2_net : GND port map(Y => GND_1_net);
    XOR2_Mult_19_inst : XOR2
      port map(A => XOR2_15_Y, B => AO1_23_Y, Y => Mult(19));
    AND2_132 : AND2
      port map(A => AND2_74_Y, B => AND2_149_Y, Y => AND2_132_Y);
    AO1_52 : AO1
      port map(A => AND2_142_Y, B => AO1_70_Y, C => AO1_86_Y, 
        Y => AO1_52_Y);
    AND2_2 : AND2
      port map(A => PP0_15_net, B => GND_1_net, Y => AND2_2_Y);
    AND2_20 : AND2
      port map(A => PP0_27_net, B => GND_1_net, Y => AND2_20_Y);
    AO1_11 : AO1
      port map(A => XOR2_11_Y, B => AND2_20_Y, C => AND2_141_Y, 
        Y => AO1_11_Y);
    AND2_S_0_inst : AND2
      port map(A => XOR2_27_Y, B => DataB(1), Y => S_0_net);
    AND2_11 : AND2
      port map(A => AND2_32_Y, B => AND2_12_Y, Y => AND2_11_Y);
    AND2_154 : AND2
      port map(A => DataB(0), B => DataA(2), Y => AND2_154_Y);
    AND2_22 : AND2
      port map(A => PP0_13_net, B => GND_1_net, Y => AND2_22_Y);
    AND2_71 : AND2
      port map(A => AND2_99_Y, B => XOR2_58_Y, Y => AND2_71_Y);
    XOR2_19 : XOR2
      port map(A => PP0_25_net, B => GND_1_net, Y => XOR2_19_Y);
    AND2_44 : AND2
      port map(A => AND2_147_Y, B => AND2_5_Y, Y => AND2_44_Y);
    XOR2_23 : XOR2
      port map(A => EBAR, B => GND_1_net, Y => XOR2_23_Y);
    AO1_31 : AO1
      port map(A => AND2_70_Y, B => AO1_82_Y, C => AO1_65_Y, Y => 
        AO1_31_Y);
    XOR2_1 : XOR2
      port map(A => PP0_12_net, B => GND_1_net, Y => XOR2_1_Y);
    XOR2_PP0_22_inst : XOR2
      port map(A => MX2_26_Y, B => BUFF_2_Y, Y => PP0_22_net);
    MX2_17 : MX2
      port map(A => AND2_120_Y, B => DataA(4), S => AND2A_0_Y, 
        Y => MX2_17_Y);
    XOR2_PP0_17_inst : XOR2
      port map(A => MX2_10_Y, B => BUFF_1_Y, Y => PP0_17_net);
    AO1_EBAR : AO1
      port map(A => XOR2_37_Y, B => OR3_0_Y, C => AND3_0_Y, Y => 
        EBAR);
    AND2_104 : AND2
      port map(A => AND2_32_Y, B => AND2_57_Y, Y => AND2_104_Y);
    AO1_67 : AO1
      port map(A => AND2_149_Y, B => AO1_63_Y, C => AO1_11_Y, 
        Y => AO1_67_Y);
    XOR2_Mult_18_inst : XOR2
      port map(A => XOR2_66_Y, B => AO1_10_Y, Y => Mult(18));
    XOR2_47 : XOR2
      port map(A => PP0_29_net, B => GND_1_net, Y => XOR2_47_Y);
    XOR2_38 : XOR2
      port map(A => PP0_11_net, B => GND_1_net, Y => XOR2_38_Y);
    MX2_21 : MX2
      port map(A => AND2_106_Y, B => DataA(26), S => AND2A_2_Y, 
        Y => MX2_21_Y);
    AND2_124 : AND2
      port map(A => XOR2_47_Y, B => XOR2_63_Y, Y => AND2_124_Y);
    AO1_7 : AO1
      port map(A => XOR2_65_Y, B => AND2_129_Y, C => AND2_28_Y, 
        Y => AO1_7_Y);
    AND2_135 : AND2
      port map(A => XOR2_28_Y, B => XOR2_26_Y, Y => AND2_135_Y);
    AND2_18 : AND2
      port map(A => AND2_15_Y, B => XOR2_51_Y, Y => AND2_18_Y);
    AND2_15 : AND2
      port map(A => AND2_112_Y, B => AND2_135_Y, Y => AND2_15_Y);
    AND2_84 : AND2
      port map(A => AND2_3_Y, B => AND2_71_Y, Y => AND2_84_Y);
    AO1_25 : AO1
      port map(A => AND2_82_Y, B => AO1_3_Y, C => AO1_45_Y, Y => 
        AO1_25_Y);
    XOR2_Mult_22_inst : XOR2
      port map(A => XOR2_0_Y, B => AO1_38_Y, Y => Mult(22));
    AND2_78 : AND2
      port map(A => AND2_124_Y, B => AND2_43_Y, Y => AND2_78_Y);
    AND2_75 : AND2
      port map(A => DataB(0), B => DataA(19), Y => AND2_75_Y);
    XOR2_45 : XOR2
      port map(A => PP0_20_net, B => GND_1_net, Y => XOR2_45_Y);
    XOR2_PP0_8_inst : XOR2
      port map(A => MX2_15_Y, B => BUFF_0_Y, Y => PP0_8_net);
    XOR2_PP0_10_inst : XOR2
      port map(A => MX2_24_Y, B => BUFF_0_Y, Y => PP0_10_net);
    XOR2_Mult_16_inst : XOR2
      port map(A => XOR2_16_Y, B => AO1_55_Y, Y => Mult(16));
    AND2_1 : AND2
      port map(A => DataB(0), B => DataA(10), Y => AND2_1_Y);
    AND2_49 : AND2
      port map(A => PP0_20_net, B => GND_1_net, Y => AND2_49_Y);
    AO1_8 : AO1
      port map(A => AND2_60_Y, B => AO1_70_Y, C => AO1_47_Y, Y => 
        AO1_8_Y);
    AO1_78 : AO1
      port map(A => AND2_74_Y, B => AO1_92_Y, C => AO1_63_Y, Y => 
        AO1_78_Y);
    AND2_10 : AND2
      port map(A => AND2_97_Y, B => AND2_152_Y, Y => AND2_10_Y);
    AND2_7 : AND2
      port map(A => AND2_48_Y, B => AND2_44_Y, Y => AND2_7_Y);
    XOR2_20 : XOR2
      port map(A => PP0_19_net, B => GND_1_net, Y => XOR2_20_Y);
    XOR2_63 : XOR2
      port map(A => PP0_30_net, B => GND_1_net, Y => XOR2_63_Y);
    AND2_70 : AND2
      port map(A => AND2_158_Y, B => XOR2_20_Y, Y => AND2_70_Y);
    AND2A_1 : AND2A
      port map(A => DataB(0), B => BUFF_1_Y, Y => AND2A_1_Y);
    AND2_12 : AND2
      port map(A => AND2_132_Y, B => XOR2_47_Y, Y => AND2_12_Y);
    XOR2_52 : XOR2
      port map(A => PP0_7_net, B => GND_1_net, Y => XOR2_52_Y);
    AO1_42 : AO1
      port map(A => AND2_43_Y, B => AO1_79_Y, C => AO1_39_Y, Y => 
        AO1_42_Y);
    XOR2_Mult_5_inst : XOR2
      port map(A => XOR2_31_Y, B => AO1_40_Y, Y => Mult(5));
    AO1_50 : AO1
      port map(A => XOR2_48_Y, B => AO1_14_Y, C => AND2_138_Y, 
        Y => AO1_50_Y);
    AND2_72 : AND2
      port map(A => XOR2_20_Y, B => XOR2_43_Y, Y => AND2_72_Y);
    AND2_61 : AND2
      port map(A => AND2_85_Y, B => AND2_24_Y, Y => AND2_61_Y);
    AO1_15 : AO1
      port map(A => AND2_24_Y, B => AO1_24_Y, C => AO1_89_Y, Y => 
        AO1_15_Y);
    MX2_5 : MX2
      port map(A => AND2_154_Y, B => DataA(1), S => AND2A_0_Y, 
        Y => MX2_5_Y);
    MX2_25 : MX2
      port map(A => AND2_116_Y, B => DataA(27), S => AND2A_2_Y, 
        Y => MX2_25_Y);
    XOR2_PP0_9_inst : XOR2
      port map(A => MX2_28_Y, B => BUFF_0_Y, Y => PP0_9_net);
    AO1_88 : AO1
      port map(A => AND2_0_Y, B => AO1_24_Y, C => AO1_6_Y, Y => 
        AO1_88_Y);
    AND2_89 : AND2
      port map(A => AND2_92_Y, B => AND2_142_Y, Y => AND2_89_Y);
    XOR2_24 : XOR2
      port map(A => PP0_25_net, B => GND_1_net, Y => XOR2_24_Y);
    AND2_148 : AND2
      port map(A => DataB(0), B => DataA(23), Y => AND2_148_Y);
    AND2_57 : AND2
      port map(A => AND2_74_Y, B => XOR2_34_Y, Y => AND2_57_Y);
    XOR2_21 : XOR2
      port map(A => PP0_13_net, B => GND_1_net, Y => XOR2_21_Y);
    XOR2_Mult_7_inst : XOR2
      port map(A => XOR2_52_Y, B => AO1_44_Y, Y => Mult(7));
    BUFF_2 : BUFF
      port map(A => DataB(1), Y => BUFF_2_Y);
    AO1_35 : AO1
      port map(A => AND2_21_Y, B => AO1_48_Y, C => AO1_43_Y, Y => 
        AO1_35_Y);
    AO1_56 : AO1
      port map(A => AND2_57_Y, B => AO1_92_Y, C => AO1_91_Y, Y => 
        AO1_56_Y);
    AND2_46 : AND2
      port map(A => PP0_9_net, B => GND_1_net, Y => AND2_46_Y);
    AO1_53 : AO1
      port map(A => AND2_23_Y, B => AO1_3_Y, C => AO1_8_Y, Y => 
        AO1_53_Y);
    AO1_61 : AO1
      port map(A => XOR2_3_Y, B => AO1_87_Y, C => AND2_79_Y, Y => 
        AO1_61_Y);
    XOR2_16 : XOR2
      port map(A => PP0_16_net, B => GND_1_net, Y => XOR2_16_Y);
    XOR2_60 : XOR2
      port map(A => PP0_8_net, B => GND_1_net, Y => XOR2_60_Y);
    AND2_68 : AND2
      port map(A => AND2_87_Y, B => XOR2_61_Y, Y => AND2_68_Y);
    AND2_65 : AND2
      port map(A => AND2_10_Y, B => AND2_52_Y, Y => AND2_65_Y);
    XOR2_PP0_1_inst : XOR2
      port map(A => MX2_9_Y, B => BUFF_0_Y, Y => PP0_1_net);
    AND2_43 : AND2
      port map(A => XOR2_39_Y, B => XOR2_2_Y, Y => AND2_43_Y);
    AO1_24 : AO1
      port map(A => AND2_152_Y, B => AO1_1_Y, C => AO1_13_Y, Y => 
        AO1_24_Y);
    XOR2_Mult_25_inst : XOR2
      port map(A => XOR2_24_Y, B => AO1_34_Y, Y => Mult(25));
    MX2_20 : MX2
      port map(A => AND2_114_Y, B => DataA(5), S => AND2A_0_Y, 
        Y => MX2_20_Y);
    AND2_86 : AND2
      port map(A => XOR2_42_Y, B => XOR2_18_Y, Y => AND2_86_Y);
    MX2_19 : MX2
      port map(A => AND2_30_Y, B => DataA(17), S => AND2A_1_Y, 
        Y => MX2_19_Y);
    XOR2_Mult_10_inst : XOR2
      port map(A => XOR2_4_Y, B => AO1_49_Y, Y => Mult(10));
    AND2_6 : AND2
      port map(A => DataB(0), B => DataA(25), Y => AND2_6_Y);
    XOR2_64 : XOR2
      port map(A => PP0_8_net, B => GND_1_net, Y => XOR2_64_Y);
    AND3_0 : AND3
      port map(A => GND_1_net, B => DataB(0), C => DataB(1), Y => 
        AND3_0_Y);
    AND2_60 : AND2
      port map(A => AND2_45_Y, B => AND2_16_Y, Y => AND2_60_Y);
    XOR2_61 : XOR2
      port map(A => PP0_15_net, B => GND_1_net, Y => XOR2_61_Y);
    AO1_59 : AO1
      port map(A => AND2_38_Y, B => AO1_40_Y, C => AO1_61_Y, Y => 
        AO1_59_Y);
    AND2_83 : AND2
      port map(A => AND2_37_Y, B => AND2_152_Y, Y => AND2_83_Y);
    XOR2_57 : XOR2
      port map(A => PP0_31_net, B => GND_1_net, Y => XOR2_57_Y);
    XOR2_33 : XOR2
      port map(A => PP0_12_net, B => GND_1_net, Y => XOR2_33_Y);
    XOR2_Mult_29_inst : XOR2
      port map(A => XOR2_9_Y, B => AO1_4_Y, Y => Mult(29));
    AND2_62 : AND2
      port map(A => XOR2_61_Y, B => XOR2_6_Y, Y => AND2_62_Y);
    AND2_97 : AND2
      port map(A => AND2_98_Y, B => AND2_91_Y, Y => AND2_97_Y);
    XOR2_PP0_24_inst : XOR2
      port map(A => MX2_29_Y, B => BUFF_2_Y, Y => PP0_24_net);
    MX2_31 : MX2
      port map(A => AND2_148_Y, B => DataA(22), S => AND2A_2_Y, 
        Y => MX2_31_Y);
    AO1_91 : AO1
      port map(A => XOR2_34_Y, B => AO1_63_Y, C => AND2_20_Y, 
        Y => AO1_91_Y);
    XOR2_49 : XOR2
      port map(A => PP0_27_net, B => GND_1_net, Y => XOR2_49_Y);
    AO1_14 : AO1
      port map(A => XOR2_25_Y, B => AND2_66_Y, C => AND2_39_Y, 
        Y => AO1_14_Y);
    XOR2_4 : XOR2
      port map(A => PP0_10_net, B => GND_1_net, Y => XOR2_4_Y);
    XOR2_PP0_29_inst : XOR2
      port map(A => MX2_2_Y, B => BUFF_2_Y, Y => PP0_29_net);
    MX2_14 : MX2
      port map(A => AND2_42_Y, B => DataA(25), S => AND2A_2_Y, 
        Y => MX2_14_Y);
    AO1_72 : AO1
      port map(A => AND2_68_Y, B => AO1_0_Y, C => AO1_69_Y, Y => 
        AO1_72_Y);
    XOR2_55 : XOR2
      port map(A => PP0_14_net, B => GND_1_net, Y => XOR2_55_Y);
    AND2_24 : AND2
      port map(A => AND2_99_Y, B => AND2_147_Y, Y => AND2_24_Y);
    XOR2_PP0_25_inst : XOR2
      port map(A => MX2_6_Y, B => BUFF_2_Y, Y => PP0_25_net);
    AO1_40 : AO1
      port map(A => AND2_135_Y, B => AO1_7_Y, C => AO1_32_Y, Y => 
        AO1_40_Y);
    AND2_31 : AND2
      port map(A => AND2_100_Y, B => AND2_82_Y, Y => AND2_31_Y);
    XOR2_Mult_13_inst : XOR2
      port map(A => XOR2_44_Y, B => AO1_58_Y, Y => Mult(13));
    XOR2_PP0_28_inst : XOR2
      port map(A => MX2_25_Y, B => BUFF_2_Y, Y => PP0_28_net);
    AND2_113 : AND2
      port map(A => DataB(0), B => DataA(7), Y => AND2_113_Y);
    AO1_34 : AO1
      port map(A => AND2_7_Y, B => AO1_24_Y, C => AO1_20_Y, Y => 
        AO1_34_Y);
    XOR2_PP0_11_inst : XOR2
      port map(A => MX2_27_Y, B => BUFF_1_Y, Y => PP0_11_net);
    XOR2_18 : XOR2
      port map(A => PP0_10_net, B => GND_1_net, Y => XOR2_18_Y);
    AO1_82 : AO1
      port map(A => AND2_152_Y, B => AO1_22_Y, C => AO1_13_Y, 
        Y => AO1_82_Y);
    AO1_46 : AO1
      port map(A => XOR2_1_Y, B => AND2_95_Y, C => AND2_67_Y, 
        Y => AO1_46_Y);
    AO1_65 : AO1
      port map(A => XOR2_20_Y, B => AO1_21_Y, C => AND2_136_Y, 
        Y => AO1_65_Y);
    AND2_138 : AND2
      port map(A => PP0_23_net, B => GND_1_net, Y => AND2_138_Y);
    XOR2_Mult_28_inst : XOR2
      port map(A => XOR2_59_Y, B => AO1_90_Y, Y => Mult(28));
    XOR2_8 : XOR2
      port map(A => PP0_30_net, B => GND_1_net, Y => XOR2_8_Y);
    AND2_116 : AND2
      port map(A => DataB(0), B => DataA(28), Y => AND2_116_Y);
    AND2_110 : AND2
      port map(A => PP0_24_net, B => GND_1_net, Y => AND2_110_Y);
    AND2_144 : AND2
      port map(A => AND2_45_Y, B => AND2_78_Y, Y => AND2_144_Y);
    AO1_43 : AO1
      port map(A => XOR2_21_Y, B => AO1_51_Y, C => AND2_22_Y, 
        Y => AO1_43_Y);
    XOR2_30 : XOR2
      port map(A => PP0_21_net, B => GND_1_net, Y => XOR2_30_Y);
    AND2_38 : AND2
      port map(A => AND2_53_Y, B => XOR2_3_Y, Y => AND2_38_Y);
    AND2_35 : AND2
      port map(A => DataB(0), B => DataA(15), Y => AND2_35_Y);
    MX2_8 : MX2
      port map(A => AND2_75_Y, B => DataA(18), S => AND2A_1_Y, 
        Y => MX2_8_Y);
    AND2_29 : AND2
      port map(A => AND2_15_Y, B => AND2_38_Y, Y => AND2_29_Y);
    AND2_153 : AND2
      port map(A => DataB(0), B => DataA(4), Y => AND2_153_Y);
    XOR2_PP0_4_inst : XOR2
      port map(A => MX2_13_Y, B => BUFF_0_Y, Y => PP0_4_net);
    AND2_117 : AND2
      port map(A => DataB(0), B => DataA(11), Y => AND2_117_Y);
    XOR2_34 : XOR2
      port map(A => PP0_27_net, B => GND_1_net, Y => XOR2_34_Y);
    XOR2_Mult_26_inst : XOR2
      port map(A => XOR2_10_Y, B => AO1_88_Y, Y => Mult(26));
    XOR2_31 : XOR2
      port map(A => PP0_5_net, B => GND_1_net, Y => XOR2_31_Y);
    AND2_3 : AND2
      port map(A => AND2_37_Y, B => AND2_152_Y, Y => AND2_3_Y);
    MX2_PP0_32_inst : MX2
      port map(A => MX2_1_Y, B => EBAR, S => AND2A_2_Y, Y => 
        PP0_32_net);
    AO1_49 : AO1
      port map(A => XOR2_42_Y, B => AO1_66_Y, C => AND2_46_Y, 
        Y => AO1_49_Y);
    AND2_30 : AND2
      port map(A => DataB(0), B => DataA(18), Y => AND2_30_Y);
    XOR2_PP0_16_inst : XOR2
      port map(A => MX2_16_Y, B => BUFF_1_Y, Y => PP0_16_net);
    AO1_57 : AO1
      port map(A => XOR2_17_Y, B => AND2_22_Y, C => AND2_157_Y, 
        Y => AO1_57_Y);
    AND2_156 : AND2
      port map(A => AND2_100_Y, B => AND2_89_Y, Y => AND2_156_Y);
    AND2_150 : AND2
      port map(A => AND2_93_Y, B => AND2_87_Y, Y => AND2_150_Y);
    AND2_14 : AND2
      port map(A => DataB(0), B => DataA(1), Y => AND2_14_Y);
    AND2_103 : AND2
      port map(A => AND2_31_Y, B => XOR2_23_Y, Y => AND2_103_Y);
    AND2_74 : AND2
      port map(A => XOR2_19_Y, B => XOR2_35_Y, Y => AND2_74_Y);
    AND2_32 : AND2
      port map(A => AND2_48_Y, B => AND2_44_Y, Y => AND2_32_Y);
    AND2_123 : AND2
      port map(A => DataB(0), B => DataA(31), Y => AND2_123_Y);
    MX2_30 : MX2
      port map(A => AND2_113_Y, B => DataA(6), S => AND2A_0_Y, 
        Y => MX2_30_Y);
    XOR2_46 : XOR2
      port map(A => EBAR, B => GND_1_net, Y => XOR2_46_Y);
    MX2_22 : MX2
      port map(A => AND2_125_Y, B => DataA(20), S => AND2A_1_Y, 
        Y => MX2_22_Y);
    AO1_2 : AO1
      port map(A => AND2_119_Y, B => AO1_87_Y, C => AO1_36_Y, 
        Y => AO1_2_Y);
    AO1_70 : AO1
      port map(A => AND2_44_Y, B => AO1_41_Y, C => AO1_19_Y, Y => 
        AO1_70_Y);
    AND2_157 : AND2
      port map(A => PP0_14_net, B => GND_1_net, Y => AND2_157_Y);
    AND2_106 : AND2
      port map(A => DataB(0), B => DataA(27), Y => AND2_106_Y);
    AND2_100 : AND2
      port map(A => AND2_97_Y, B => AND2_152_Y, Y => AND2_100_Y);
    AND2_26 : AND2
      port map(A => PP0_18_net, B => GND_1_net, Y => AND2_26_Y);
    AND2_111 : AND2
      port map(A => AND2_86_Y, B => XOR2_38_Y, Y => AND2_111_Y);
    XOR2_9 : XOR2
      port map(A => PP0_29_net, B => GND_1_net, Y => XOR2_9_Y);
    AND2_126 : AND2
      port map(A => AND2_112_Y, B => XOR2_28_Y, Y => AND2_126_Y);
    AND2_120 : AND2
      port map(A => DataB(0), B => DataA(5), Y => AND2_120_Y);
    XOR2_PP0_2_inst : XOR2
      port map(A => MX2_5_Y, B => BUFF_0_Y, Y => PP0_2_net);
    XOR2_Mult_32_inst : XOR2
      port map(A => XOR2_36_Y, B => AO1_53_Y, Y => Mult(32));
    XOR2_59 : XOR2
      port map(A => PP0_28_net, B => GND_1_net, Y => XOR2_59_Y);
    AO1_64 : AO1
      port map(A => AND2_99_Y, B => AO1_82_Y, C => AO1_27_Y, Y => 
        AO1_64_Y);
    AND2_23 : AND2
      port map(A => AND2_92_Y, B => AND2_60_Y, Y => AND2_23_Y);
    XOR2_5 : XOR2
      port map(A => PP0_17_net, B => GND_1_net, Y => XOR2_5_Y);
    XOR2_PP0_27_inst : XOR2
      port map(A => MX2_21_Y, B => BUFF_2_Y, Y => PP0_27_net);
    MX2_0 : MX2
      port map(A => AND2_34_Y, B => DataA(12), S => AND2A_1_Y, 
        Y => MX2_0_Y);
    XOR2_PP0_13_inst : XOR2
      port map(A => MX2_0_Y, B => BUFF_1_Y, Y => PP0_13_net);
    AO1_76 : AO1
      port map(A => XOR2_28_Y, B => AO1_7_Y, C => AND2_64_Y, Y => 
        AO1_76_Y);
    AO1_80 : AO1
      port map(A => XOR2_51_Y, B => AO1_40_Y, C => AND2_25_Y, 
        Y => AO1_80_Y);
    AND2_107 : AND2
      port map(A => EBAR, B => GND_1_net, Y => AND2_107_Y);
    AND2_19 : AND2
      port map(A => PP0_16_net, B => GND_1_net, Y => AND2_19_Y);
    AO1_28 : AO1
      port map(A => AND2_152_Y, B => AO1_22_Y, C => AO1_13_Y, 
        Y => AO1_28_Y);
    AO1_73 : AO1
      port map(A => XOR2_38_Y, B => AO1_5_Y, C => AND2_95_Y, Y => 
        AO1_73_Y);
    AND2_79 : AND2
      port map(A => PP0_7_net, B => GND_1_net, Y => AND2_79_Y);
    AND2_119 : AND2
      port map(A => XOR2_3_Y, B => XOR2_60_Y, Y => AND2_119_Y);
    XOR2_22 : XOR2
      port map(A => PP0_4_net, B => GND_1_net, Y => XOR2_22_Y);
    AND2_127 : AND2
      port map(A => PP0_25_net, B => GND_1_net, Y => AND2_127_Y);
    AO1_1 : AO1
      port map(A => AND2_91_Y, B => AO1_54_Y, C => AO1_2_Y, Y => 
        AO1_1_Y);
    MX2_28 : MX2
      port map(A => AND2_102_Y, B => DataA(8), S => AND2A_0_Y, 
        Y => MX2_28_Y);
    XOR2_13 : XOR2
      port map(A => PP0_9_net, B => GND_1_net, Y => XOR2_13_Y);
    XOR2_Mult_3_inst : XOR2
      port map(A => XOR2_7_Y, B => AO1_7_Y, Y => Mult(3));
    AND2_151 : AND2
      port map(A => AND2_87_Y, B => AND2_62_Y, Y => AND2_151_Y);
    AND2_134 : AND2
      port map(A => PP0_4_net, B => GND_1_net, Y => AND2_134_Y);
    XOR2_Mult_8_inst : XOR2
      port map(A => XOR2_64_Y, B => AO1_59_Y, Y => Mult(8));
    XOR2_Mult_20_inst : XOR2
      port map(A => XOR2_45_Y, B => AO1_31_Y, Y => Mult(20));
    AO1_86 : AO1
      port map(A => AND2_124_Y, B => AO1_16_Y, C => AO1_79_Y, 
        Y => AO1_86_Y);
    XOR2_PP0_20_inst : XOR2
      port map(A => MX2_4_Y, B => BUFF_1_Y, Y => PP0_20_net);
    AND2_51 : AND2
      port map(A => AND2_37_Y, B => AND2_150_Y, Y => AND2_51_Y);
    AO1_3 : AO1
      port map(A => AND2_152_Y, B => AO1_1_Y, C => AO1_13_Y, Y => 
        AO1_3_Y);
    AO1_83 : AO1
      port map(A => AND2_152_Y, B => AO1_1_Y, C => AO1_13_Y, Y => 
        AO1_83_Y);
    AND2_64 : AND2
      port map(A => PP0_3_net, B => GND_1_net, Y => AND2_64_Y);
    AND2_47 : AND2
      port map(A => PP0_31_net, B => GND_1_net, Y => AND2_47_Y);
    AO1_51 : AO1
      port map(A => AND2_8_Y, B => AO1_5_Y, C => AO1_46_Y, Y => 
        AO1_51_Y);
    AO1_18 : AO1
      port map(A => AND2_87_Y, B => AO1_0_Y, C => AO1_57_Y, Y => 
        AO1_18_Y);
    AO1_79 : AO1
      port map(A => XOR2_63_Y, B => AND2_88_Y, C => AND2_9_Y, 
        Y => AO1_79_Y);
    XOR2_48 : XOR2
      port map(A => PP0_23_net, B => GND_1_net, Y => XOR2_48_Y);
    MX2_26 : MX2
      port map(A => AND2_4_Y, B => DataA(21), S => AND2A_2_Y, 
        Y => MX2_26_Y);
    AO1_47 : AO1
      port map(A => AND2_16_Y, B => AO1_16_Y, C => AO1_84_Y, Y => 
        AO1_47_Y);
    AND2A_2 : AND2A
      port map(A => DataB(0), B => BUFF_2_Y, Y => AND2A_2_Y);
    AND2_101 : AND2
      port map(A => AND2_10_Y, B => AND2_104_Y, Y => AND2_101_Y);
    AND2_16 : AND2
      port map(A => AND2_124_Y, B => XOR2_39_Y, Y => AND2_16_Y);
    XOR2_Mult_11_inst : XOR2
      port map(A => XOR2_56_Y, B => AO1_62_Y, Y => Mult(11));
    AND2_76 : AND2
      port map(A => DataB(0), B => DataA(20), Y => AND2_76_Y);
    XOR2_Mult_23_inst : XOR2
      port map(A => XOR2_12_Y, B => AO1_15_Y, Y => Mult(23));
    AND2_121 : AND2
      port map(A => DataB(0), B => DataA(14), Y => AND2_121_Y);
    XOR2_Mult_4_inst : XOR2
      port map(A => XOR2_22_Y, B => AO1_76_Y, Y => Mult(4));
    AO1_38 : AO1
      port map(A => AND2_71_Y, B => AO1_82_Y, C => AO1_75_Y, Y => 
        AO1_38_Y);
    XOR2_62 : XOR2
      port map(A => PP0_2_net, B => GND_1_net, Y => XOR2_62_Y);
    AND2_13 : AND2
      port map(A => PP0_32_net, B => GND_1_net, Y => AND2_13_Y);
    AND2_87 : AND2
      port map(A => XOR2_21_Y, B => XOR2_17_Y, Y => AND2_87_Y);
    AO1_89 : AO1
      port map(A => AND2_147_Y, B => AO1_27_Y, C => AO1_14_Y, 
        Y => AO1_89_Y);
    AND2_73 : AND2
      port map(A => AND2_32_Y, B => AND2_132_Y, Y => AND2_73_Y);
    XOR2_10 : XOR2
      port map(A => PP0_26_net, B => GND_1_net, Y => XOR2_10_Y);
    XOR2_Mult_1_inst : XOR2
      port map(A => PP0_1_net, B => S_0_net, Y => Mult(1));
    AND2_58 : AND2
      port map(A => AND2_100_Y, B => AND2_23_Y, Y => AND2_58_Y);
    AND2_55 : AND2
      port map(A => AND2_10_Y, B => AND2_73_Y, Y => AND2_55_Y);
    MX2_2 : MX2
      port map(A => AND2_115_Y, B => DataA(28), S => AND2A_2_Y, 
        Y => MX2_2_Y);
    AND2_109 : AND2
      port map(A => AND2_37_Y, B => AND2_63_Y, Y => AND2_109_Y);
    MX2_23 : MX2
      port map(A => AND2_121_Y, B => DataA(13), S => AND2A_1_Y, 
        Y => MX2_23_Y);
    XOR2_Mult_2_inst : XOR2
      port map(A => XOR2_62_Y, B => AND2_129_Y, Y => Mult(2));
    AND2_69 : AND2
      port map(A => AND2_85_Y, B => AND2_7_Y, Y => AND2_69_Y);
    AND2_129 : AND2
      port map(A => PP0_1_net, B => S_0_net, Y => AND2_129_Y);
    XOR2_27 : XOR2
      port map(A => AND2_143_Y, B => BUFF_0_Y, Y => XOR2_27_Y);
    XOR2_7 : XOR2
      port map(A => PP0_3_net, B => GND_1_net, Y => XOR2_7_Y);
    AND2_5 : AND2
      port map(A => XOR2_48_Y, B => XOR2_41_Y, Y => AND2_5_Y);
    XOR2_56 : XOR2
      port map(A => PP0_11_net, B => GND_1_net, Y => XOR2_56_Y);
    XOR2_14 : XOR2
      port map(A => PP0_6_net, B => GND_1_net, Y => XOR2_14_Y);
    AND2_91 : AND2
      port map(A => AND2_53_Y, B => AND2_119_Y, Y => AND2_91_Y);
    AND2_50 : AND2
      port map(A => AND2_17_Y, B => AND2_111_Y, Y => AND2_50_Y);
    XOR2_11 : XOR2
      port map(A => PP0_28_net, B => GND_1_net, Y => XOR2_11_Y);
    XOR2_25 : XOR2
      port map(A => PP0_22_net, B => GND_1_net, Y => XOR2_25_Y);
    AND2_52 : AND2
      port map(A => AND2_32_Y, B => AND2_74_Y, Y => AND2_52_Y);
    AO1_22 : AO1
      port map(A => AND2_91_Y, B => AO1_54_Y, C => AO1_2_Y, Y => 
        AO1_22_Y);
    XOR2_Mult_6_inst : XOR2
      port map(A => XOR2_14_Y, B => AO1_80_Y, Y => Mult(6));
    BUFF_0 : BUFF
      port map(A => DataB(1), Y => BUFF_0_Y);
    AO1_55 : AO1
      port map(A => AND2_63_Y, B => AO1_22_Y, C => AO1_72_Y, Y => 
        AO1_55_Y);
    AND2_112 : AND2
      port map(A => XOR2_50_Y, B => XOR2_65_Y, Y => AND2_112_Y);
    MX2_27 : MX2
      port map(A => AND2_117_Y, B => DataA(10), S => AND2A_1_Y, 
        Y => MX2_27_Y);
    AND2_34 : AND2
      port map(A => DataB(0), B => DataA(13), Y => AND2_34_Y);
    AO1_41 : AO1
      port map(A => AND2_72_Y, B => AO1_21_Y, C => AO1_37_Y, Y => 
        AO1_41_Y);
    AND2_66 : AND2
      port map(A => PP0_21_net, B => GND_1_net, Y => AND2_66_Y);
    AO1_6 : AO1
      port map(A => XOR2_19_Y, B => AO1_20_Y, C => AND2_127_Y, 
        Y => AO1_6_Y);
    MX2_11 : MX2
      port map(A => AND2_56_Y, B => DataA(11), S => AND2A_1_Y, 
        Y => MX2_11_Y);
    AND2_143 : AND2
      port map(A => DataB(0), B => DataA(0), Y => AND2_143_Y);
    AND2_98 : AND2
      port map(A => AND2_112_Y, B => AND2_135_Y, Y => AND2_98_Y);
    AND2_95 : AND2
      port map(A => PP0_11_net, B => GND_1_net, Y => AND2_95_Y);
    AO1_77 : AO1
      port map(A => AND2_12_Y, B => AO1_92_Y, C => AO1_17_Y, Y => 
        AO1_77_Y);
    AND2_63 : AND2
      port map(A => AND2_93_Y, B => AND2_68_Y, Y => AND2_63_Y);
    AO1_12 : AO1
      port map(A => AND2_11_Y, B => AO1_83_Y, C => AO1_77_Y, Y => 
        AO1_12_Y);
    XOR2_32 : XOR2
      port map(A => PP0_15_net, B => GND_1_net, Y => XOR2_32_Y);
    OR3_0 : OR3
      port map(A => GND_1_net, B => DataB(0), C => DataB(1), Y => 
        OR3_0_Y);
    XOR2_PP0_21_inst : XOR2
      port map(A => MX2_22_Y, B => BUFF_1_Y, Y => PP0_21_net);
    AND2_9 : AND2
      port map(A => PP0_30_net, B => GND_1_net, Y => AND2_9_Y);
    MX2_6 : MX2
      port map(A => AND2_6_Y, B => DataA(24), S => AND2A_2_Y, 
        Y => MX2_6_Y);
    XOR2_65 : XOR2
      port map(A => PP0_2_net, B => GND_1_net, Y => XOR2_65_Y);
    AND2_146 : AND2
      port map(A => AND2_85_Y, B => AND2_90_Y, Y => AND2_146_Y);
    AND2_140 : AND2
      port map(A => AND2_3_Y, B => AND2_70_Y, Y => AND2_140_Y);
    AO1_68 : AO1
      port map(A => AND2_111_Y, B => AO1_48_Y, C => AO1_73_Y, 
        Y => AO1_68_Y);
    AND2_90 : AND2
      port map(A => AND2_99_Y, B => AND2_130_Y, Y => AND2_90_Y);
    AO1_87 : AO1
      port map(A => XOR2_40_Y, B => AND2_25_Y, C => AND2_41_Y, 
        Y => AO1_87_Y);
    XOR2_58 : XOR2
      port map(A => PP0_21_net, B => GND_1_net, Y => XOR2_58_Y);
    AND2_152 : AND2
      port map(A => AND2_93_Y, B => AND2_151_Y, Y => AND2_152_Y);
    XOR2_43 : XOR2
      port map(A => PP0_20_net, B => GND_1_net, Y => XOR2_43_Y);
    AO1_32 : AO1
      port map(A => XOR2_26_Y, B => AND2_64_Y, C => AND2_134_Y, 
        Y => AO1_32_Y);
    AND2_92 : AND2
      port map(A => AND2_48_Y, B => AND2_44_Y, Y => AND2_92_Y);
    XOR2_Mult_14_inst : XOR2
      port map(A => XOR2_55_Y, B => AO1_35_Y, Y => Mult(14));
    MX2_7 : MX2
      port map(A => AND2_131_Y, B => DataA(2), S => AND2A_0_Y, 
        Y => MX2_7_Y);
    AO1_9 : AO1
      port map(A => AND2_150_Y, B => AO1_22_Y, C => AO1_18_Y, 
        Y => AO1_9_Y);
    AND2_39 : AND2
      port map(A => PP0_22_net, B => GND_1_net, Y => AND2_39_Y);
    AND2_147 : AND2
      port map(A => XOR2_58_Y, B => XOR2_25_Y, Y => AND2_147_Y);
    AND2_115 : AND2
      port map(A => DataB(0), B => DataA(29), Y => AND2_115_Y);
    MX2_15 : MX2
      port map(A => AND2_59_Y, B => DataA(7), S => AND2A_0_Y, 
        Y => MX2_15_Y);
    AND2_102 : AND2
      port map(A => DataB(0), B => DataA(9), Y => AND2_102_Y);
    AND2_27 : AND2
      port map(A => DataB(0), B => DataA(17), Y => AND2_27_Y);
    MX2_3 : MX2
      port map(A => AND2_118_Y, B => DataA(29), S => AND2A_2_Y, 
        Y => MX2_3_Y);
    AND2_122 : AND2
      port map(A => AND2_17_Y, B => AND2_86_Y, Y => AND2_122_Y);
    AO1_54 : AO1
      port map(A => AND2_135_Y, B => AO1_7_Y, C => AO1_32_Y, Y => 
        AO1_54_Y);
    AO1_20 : AO1
      port map(A => AND2_44_Y, B => AO1_41_Y, C => AO1_19_Y, Y => 
        AO1_20_Y);
    AO1_0 : AO1
      port map(A => AND2_8_Y, B => AO1_5_Y, C => AO1_46_Y, Y => 
        AO1_0_Y);
    XOR2_29 : XOR2
      port map(A => PP0_24_net, B => GND_1_net, Y => XOR2_29_Y);
    XOR2_PP0_26_inst : XOR2
      port map(A => MX2_14_Y, B => BUFF_2_Y, Y => PP0_26_net);
    XOR2_40 : XOR2
      port map(A => PP0_6_net, B => GND_1_net, Y => XOR2_40_Y);
    AO1_45 : AO1
      port map(A => AND2_144_Y, B => AO1_70_Y, C => AO1_81_Y, 
        Y => AO1_45_Y);
    MX2_9 : MX2
      port map(A => AND2_14_Y, B => DataA(0), S => AND2A_0_Y, 
        Y => MX2_9_Y);
    XOR2_2 : XOR2
      port map(A => PP0_32_net, B => GND_1_net, Y => XOR2_2_Y);
    XOR2_Mult_9_inst : XOR2
      port map(A => XOR2_13_Y, B => AO1_66_Y, Y => Mult(9));
    AND2_155 : AND2
      port map(A => AND2_85_Y, B => AND2_0_Y, Y => AND2_155_Y);
    AO1_71 : AO1
      port map(A => XOR2_23_Y, B => AO1_25_Y, C => AND2_107_Y, 
        Y => AO1_71_Y);
    XOR2_37 : XOR2
      port map(A => DataA(31), B => DataB(1), Y => XOR2_37_Y);
    AND2A_0 : AND2A
      port map(A => DataB(0), B => BUFF_0_Y, Y => AND2A_0_Y);
    AND2_36 : AND2
      port map(A => AND2_3_Y, B => AND2_99_Y, Y => AND2_36_Y);
    AND2_141 : AND2
      port map(A => PP0_28_net, B => GND_1_net, Y => AND2_141_Y);
    AO1_26 : AO1
      port map(A => AND2_62_Y, B => AO1_57_Y, C => AO1_30_Y, Y => 
        AO1_26_Y);
    XOR2_Mult_21_inst : XOR2
      port map(A => XOR2_30_Y, B => AO1_64_Y, Y => Mult(21));
    XOR2_Mult_17_inst : XOR2
      port map(A => XOR2_53_Y, B => AO1_28_Y, Y => Mult(17));
    MX2_10 : MX2
      port map(A => AND2_27_Y, B => DataA(16), S => AND2A_1_Y, 
        Y => MX2_10_Y);
    AO1_23 : AO1
      port map(A => AND2_158_Y, B => AO1_82_Y, C => AO1_21_Y, 
        Y => AO1_23_Y);
    AND2_33 : AND2
      port map(A => AND2_96_Y, B => XOR2_42_Y, Y => AND2_33_Y);
    XOR2_44 : XOR2
      port map(A => PP0_13_net, B => GND_1_net, Y => XOR2_44_Y);
    AO1_10 : AO1
      port map(A => XOR2_5_Y, B => AO1_28_Y, C => AND2_54_Y, Y => 
        AO1_10_Y);
    AND2_133 : AND2
      port map(A => AND2_17_Y, B => AND2_21_Y, Y => AND2_133_Y);
    XOR2_41 : XOR2
      port map(A => PP0_24_net, B => GND_1_net, Y => XOR2_41_Y);
    XOR2_35 : XOR2
      port map(A => PP0_26_net, B => GND_1_net, Y => XOR2_35_Y);
    MX2_29 : MX2
      port map(A => AND2_108_Y, B => DataA(23), S => AND2A_2_Y, 
        Y => MX2_29_Y);
    AND2_105 : AND2
      port map(A => AND2_17_Y, B => AND2_40_Y, Y => AND2_105_Y);
    AO1_81 : AO1
      port map(A => AND2_78_Y, B => AO1_16_Y, C => AO1_42_Y, Y => 
        AO1_81_Y);
    XOR2_PP0_12_inst : XOR2
      port map(A => MX2_11_Y, B => BUFF_1_Y, Y => PP0_12_net);
    AND2_149 : AND2
      port map(A => XOR2_34_Y, B => XOR2_11_Y, Y => AND2_149_Y);
    XOR2_PP0_3_inst : XOR2
      port map(A => MX2_7_Y, B => BUFF_0_Y, Y => PP0_3_net);
    AND2_125 : AND2
      port map(A => DataB(0), B => DataA(21), Y => AND2_125_Y);
    XOR2_Mult_30_inst : XOR2
      port map(A => XOR2_8_Y, B => AO1_12_Y, Y => Mult(30));
    XOR2_PP0_23_inst : XOR2
      port map(A => MX2_31_Y, B => BUFF_2_Y, Y => PP0_23_net);
    AO1_30 : AO1
      port map(A => XOR2_6_Y, B => AND2_2_Y, C => AND2_19_Y, Y => 
        AO1_30_Y);
    AO1_62 : AO1
      port map(A => AND2_86_Y, B => AO1_48_Y, C => AO1_5_Y, Y => 
        AO1_62_Y);
    AND2_41 : AND2
      port map(A => PP0_6_net, B => GND_1_net, Y => AND2_41_Y);
    AND2_0 : AND2
      port map(A => AND2_7_Y, B => XOR2_19_Y, Y => AND2_0_Y);
    AND2_136 : AND2
      port map(A => PP0_19_net, B => GND_1_net, Y => AND2_136_Y);
    AND2_130 : AND2
      port map(A => AND2_147_Y, B => XOR2_48_Y, Y => AND2_130_Y);
    AO1_16 : AO1
      port map(A => AND2_149_Y, B => AO1_63_Y, C => AO1_11_Y, 
        Y => AO1_16_Y);
    AND2_17 : AND2
      port map(A => AND2_98_Y, B => AND2_91_Y, Y => AND2_17_Y);
    AO1_29 : AO1
      port map(A => AND2_52_Y, B => AO1_83_Y, C => AO1_78_Y, Y => 
        AO1_29_Y);
    XOR2_6 : XOR2
      port map(A => PP0_16_net, B => GND_1_net, Y => XOR2_6_Y);
    AND2_77 : AND2
      port map(A => AND2_15_Y, B => AND2_53_Y, Y => AND2_77_Y);
    MX2_24 : MX2
      port map(A => AND2_1_Y, B => DataA(9), S => AND2A_0_Y, Y => 
        MX2_24_Y);
    AND2_54 : AND2
      port map(A => PP0_17_net, B => GND_1_net, Y => AND2_54_Y);
    AO1_13 : AO1
      port map(A => AND2_151_Y, B => AO1_0_Y, C => AO1_26_Y, Y => 
        AO1_13_Y);
    XOR2_53 : XOR2
      port map(A => PP0_17_net, B => GND_1_net, Y => XOR2_53_Y);
    AO1_36 : AO1
      port map(A => XOR2_60_Y, B => AND2_79_Y, C => AND2_139_Y, 
        Y => AO1_36_Y);
    AND2_137 : AND2
      port map(A => PP0_26_net, B => GND_1_net, Y => AND2_137_Y);
    AND2_81 : AND2
      port map(A => AND2_3_Y, B => AND2_158_Y, Y => AND2_81_Y);
    XOR2_Mult_33_inst : XOR2
      port map(A => XOR2_46_Y, B => AO1_25_Y, Y => Mult(33));
    AO1_33 : AO1
      port map(A => AND2_132_Y, B => AO1_92_Y, C => AO1_67_Y, 
        Y => AO1_33_Y);
    AO1_44 : AO1
      port map(A => AND2_53_Y, B => AO1_40_Y, C => AO1_87_Y, Y => 
        AO1_44_Y);
    XOR2_12 : XOR2
      port map(A => PP0_23_net, B => GND_1_net, Y => XOR2_12_Y);
    AND2_48 : AND2
      port map(A => AND2_158_Y, B => AND2_72_Y, Y => AND2_48_Y);
    AND2_45 : AND2
      port map(A => AND2_74_Y, B => AND2_149_Y, Y => AND2_45_Y);
    AO1_92 : AO1
      port map(A => AND2_44_Y, B => AO1_41_Y, C => AO1_19_Y, Y => 
        AO1_92_Y);
    AO1_75 : AO1
      port map(A => XOR2_58_Y, B => AO1_27_Y, C => AND2_66_Y, 
        Y => AO1_75_Y);
    AO1_19 : AO1
      port map(A => AND2_5_Y, B => AO1_14_Y, C => AO1_85_Y, Y => 
        AO1_19_Y);
    XOR2_PP0_30_inst : XOR2
      port map(A => MX2_3_Y, B => BUFF_2_Y, Y => PP0_30_net);
    XOR2_26 : XOR2
      port map(A => PP0_4_net, B => GND_1_net, Y => XOR2_26_Y);
    AND2_59 : AND2
      port map(A => DataB(0), B => DataA(8), Y => AND2_59_Y);
    AND2_4 : AND2
      port map(A => DataB(0), B => DataA(22), Y => AND2_4_Y);
    XOR2_PP0_6_inst : XOR2
      port map(A => MX2_20_Y, B => BUFF_0_Y, Y => PP0_6_net);
    AO1_39 : AO1
      port map(A => XOR2_2_Y, B => AND2_47_Y, C => AND2_13_Y, 
        Y => AO1_39_Y);
    AND2_40 : AND2
      port map(A => AND2_86_Y, B => AND2_8_Y, Y => AND2_40_Y);
    AND2_88 : AND2
      port map(A => PP0_29_net, B => GND_1_net, Y => AND2_88_Y);
    AND2_85 : AND2
      port map(A => AND2_97_Y, B => AND2_152_Y, Y => AND2_85_Y);
    AO1_85 : AO1
      port map(A => XOR2_41_Y, B => AND2_138_Y, C => AND2_110_Y, 
        Y => AO1_85_Y);
    AND2_131 : AND2
      port map(A => DataB(0), B => DataA(3), Y => AND2_131_Y);
    XOR2_50 : XOR2
      port map(A => PP0_1_net, B => S_0_net, Y => XOR2_50_Y);
    AND2_94 : AND2
      port map(A => AND2_10_Y, B => AND2_11_Y, Y => AND2_94_Y);
    AND2_42 : AND2
      port map(A => DataB(0), B => DataA(26), Y => AND2_42_Y);
    XOR2_PP0_5_inst : XOR2
      port map(A => MX2_17_Y, B => BUFF_0_Y, Y => PP0_5_net);
    AND2_118 : AND2
      port map(A => DataB(0), B => DataA(30), Y => AND2_118_Y);
    XOR2_Mult_24_inst : XOR2
      port map(A => XOR2_29_Y, B => AO1_60_Y, Y => Mult(24));
    MX2_4 : MX2
      port map(A => AND2_76_Y, B => DataA(19), S => AND2A_1_Y, 
        Y => MX2_4_Y);
    AO1_5 : AO1
      port map(A => XOR2_18_Y, B => AND2_46_Y, C => AND2_128_Y, 
        Y => AO1_5_Y);
    AND2_67 : AND2
      port map(A => PP0_12_net, B => GND_1_net, Y => AND2_67_Y);
    XOR2_39 : XOR2
      port map(A => PP0_31_net, B => GND_1_net, Y => XOR2_39_Y);
    AND2_8 : AND2
      port map(A => XOR2_38_Y, B => XOR2_1_Y, Y => AND2_8_Y);
    AND2_80 : AND2
      port map(A => DataB(0), B => DataA(16), Y => AND2_80_Y);
    XOR2_3 : XOR2
      port map(A => PP0_7_net, B => GND_1_net, Y => XOR2_3_Y);
    XOR2_54 : XOR2
      port map(A => PP0_18_net, B => GND_1_net, Y => XOR2_54_Y);
    AO1_60 : AO1
      port map(A => AND2_90_Y, B => AO1_24_Y, C => AO1_93_Y, Y => 
        AO1_60_Y);
    MX2_12 : MX2
      port map(A => AND2_35_Y, B => DataA(14), S => AND2A_1_Y, 
        Y => MX2_12_Y);
    AND2_139 : AND2
      port map(A => PP0_8_net, B => GND_1_net, Y => AND2_139_Y);
    XOR2_Mult_12_inst : XOR2
      port map(A => XOR2_33_Y, B => AO1_68_Y, Y => Mult(12));
    XOR2_51 : XOR2
      port map(A => PP0_5_net, B => GND_1_net, Y => XOR2_51_Y);
    AO1_27 : AO1
      port map(A => AND2_72_Y, B => AO1_21_Y, C => AO1_37_Y, Y => 
        AO1_27_Y);
    AND2_82 : AND2
      port map(A => AND2_92_Y, B => AND2_144_Y, Y => AND2_82_Y);
    XOR2_66 : XOR2
      port map(A => PP0_18_net, B => GND_1_net, Y => XOR2_66_Y);
    AND2_142 : AND2
      port map(A => AND2_45_Y, B => AND2_124_Y, Y => AND2_142_Y);
    AND2_56 : AND2
      port map(A => DataB(0), B => DataA(12), Y => AND2_56_Y);
    AO1_58 : AO1
      port map(A => AND2_40_Y, B => AO1_48_Y, C => AO1_51_Y, Y => 
        AO1_58_Y);
    XOR2_17 : XOR2
      port map(A => PP0_14_net, B => GND_1_net, Y => XOR2_17_Y);
    AND2_158 : AND2
      port map(A => XOR2_5_Y, B => XOR2_54_Y, Y => AND2_158_Y);
    AND2_53 : AND2
      port map(A => XOR2_51_Y, B => XOR2_40_Y, Y => AND2_53_Y);
    AO1_66 : AO1
      port map(A => AND2_91_Y, B => AO1_40_Y, C => AO1_2_Y, Y => 
        AO1_66_Y);
    AND2_99 : AND2
      port map(A => AND2_158_Y, B => AND2_72_Y, Y => AND2_99_Y);
    XOR2_28 : XOR2
      port map(A => PP0_3_net, B => GND_1_net, Y => XOR2_28_Y);
    AO1_74 : AO1
      port map(A => AND2_89_Y, B => AO1_3_Y, C => AO1_52_Y, Y => 
        AO1_74_Y);
    XOR2_PP0_14_inst : XOR2
      port map(A => MX2_23_Y, B => BUFF_1_Y, Y => PP0_14_net);
    AO1_63 : AO1
      port map(A => XOR2_35_Y, B => AND2_127_Y, C => AND2_137_Y, 
        Y => AO1_63_Y);
    XOR2_15 : XOR2
      port map(A => PP0_19_net, B => GND_1_net, Y => XOR2_15_Y);
    XOR2_PP0_19_inst : XOR2
      port map(A => MX2_8_Y, B => BUFF_1_Y, Y => PP0_19_net);
    MX2_18 : MX2
      port map(A => AND2_123_Y, B => DataA(30), S => AND2A_2_Y, 
        Y => MX2_18_Y);
    AO1_17 : AO1
      port map(A => XOR2_47_Y, B => AO1_67_Y, C => AND2_88_Y, 
        Y => AO1_17_Y);
    AO1_90 : AO1
      port map(A => AND2_104_Y, B => AO1_83_Y, C => AO1_56_Y, 
        Y => AO1_90_Y);
    XOR2_PP0_15_inst : XOR2
      port map(A => MX2_12_Y, B => BUFF_1_Y, Y => PP0_15_net);
    XOR2_Mult_27_inst : XOR2
      port map(A => XOR2_49_Y, B => AO1_29_Y, Y => Mult(27));
    AND2_108 : AND2
      port map(A => DataB(0), B => DataA(24), Y => AND2_108_Y);
    XOR2_PP0_18_inst : XOR2
      port map(A => MX2_19_Y, B => BUFF_1_Y, Y => PP0_18_net);
    AO1_84 : AO1
      port map(A => XOR2_39_Y, B => AO1_79_Y, C => AND2_47_Y, 
        Y => AO1_84_Y);
    AND2_128 : AND2
      port map(A => PP0_10_net, B => GND_1_net, Y => AND2_128_Y);
    AND2_21 : AND2
      port map(A => AND2_40_Y, B => XOR2_21_Y, Y => AND2_21_Y);
    AO1_37 : AO1
      port map(A => XOR2_43_Y, B => AND2_136_Y, C => AND2_49_Y, 
        Y => AO1_37_Y);
    AND2_145 : AND2
      port map(A => AND2_83_Y, B => XOR2_5_Y, Y => AND2_145_Y);
    AO1_69 : AO1
      port map(A => XOR2_61_Y, B => AO1_57_Y, C => AND2_2_Y, Y => 
        AO1_69_Y);
    MX2_16 : MX2
      port map(A => AND2_80_Y, B => DataA(15), S => AND2A_1_Y, 
        Y => MX2_16_Y);
    XOR2_0 : XOR2
      port map(A => PP0_22_net, B => GND_1_net, Y => XOR2_0_Y);
    AND2_96 : AND2
      port map(A => AND2_15_Y, B => AND2_91_Y, Y => AND2_96_Y);
    AO1_93 : AO1
      port map(A => AND2_130_Y, B => AO1_27_Y, C => AO1_50_Y, 
        Y => AO1_93_Y);
    XOR2_PP0_7_inst : XOR2
      port map(A => MX2_30_Y, B => BUFF_0_Y, Y => PP0_7_net);
    AO1_4 : AO1
      port map(A => AND2_73_Y, B => AO1_83_Y, C => AO1_33_Y, Y => 
        AO1_4_Y);
    AND2_37 : AND2
      port map(A => AND2_98_Y, B => AND2_91_Y, Y => AND2_37_Y);
    XOR2_PP0_31_inst : XOR2
      port map(A => MX2_18_Y, B => BUFF_2_Y, Y => PP0_31_net);
    AND2_93 : AND2
      port map(A => AND2_86_Y, B => AND2_8_Y, Y => AND2_93_Y);
    XOR2_42 : XOR2
      port map(A => PP0_9_net, B => GND_1_net, Y => XOR2_42_Y);
    XOR2_Mult_15_inst : XOR2
      port map(A => XOR2_32_Y, B => AO1_9_Y, Y => Mult(15));
    AO1_21 : AO1
      port map(A => XOR2_54_Y, B => AND2_54_Y, C => AND2_26_Y, 
        Y => AO1_21_Y);
    XOR2_Mult_0_inst : XOR2
      port map(A => XOR2_27_Y, B => DataB(1), Y => Mult(0));
    AND2_114 : AND2
      port map(A => DataB(0), B => DataA(6), Y => AND2_114_Y);
    BUFF_1 : BUFF
      port map(A => DataB(1), Y => BUFF_1_Y);
    XOR2_36 : XOR2
      port map(A => PP0_32_net, B => GND_1_net, Y => XOR2_36_Y);
    MX2_1 : MX2
      port map(A => BUFF_2_Y, B => XOR2_37_Y, S => DataB(0), Y => 
        MX2_1_Y);
    AND2_28 : AND2
      port map(A => PP0_2_net, B => GND_1_net, Y => AND2_28_Y);
    AND2_25 : AND2
      port map(A => PP0_5_net, B => GND_1_net, Y => AND2_25_Y);
    MX2_13 : MX2
      port map(A => AND2_153_Y, B => DataA(3), S => AND2A_0_Y, 
        Y => MX2_13_Y);
    XOR2_Mult_31_inst : XOR2
      port map(A => XOR2_57_Y, B => AO1_74_Y, Y => Mult(31));
    AO1_48 : AO1
      port map(A => AND2_91_Y, B => AO1_54_Y, C => AO1_2_Y, Y => 
        AO1_48_Y);
end DEF_ARCH;

-- Version: 6.2 6.2.50.1

library ieee;
use ieee.std_logic_1164.all;
library proasic3;

entity actpll is 
    port(POWERDOWN, CLKA : in std_logic;  LOCK, GLA : out 
        std_logic) ;
end actpll;


architecture DEF_ARCH of  actpll is

    component PLL
    generic (VCOFREQUENCY:real := 0.0);

        port(CLKA, EXTFB, POWERDOWN : in std_logic := 'U'; GLA, 
        LOCK, GLB, YB, GLC, YC : out std_logic;  OADIV0, OADIV1, 
        OADIV2, OADIV3, OADIV4, OAMUX0, OAMUX1, OAMUX2, DLYGLA0, 
        DLYGLA1, DLYGLA2, DLYGLA3, DLYGLA4, OBDIV0, OBDIV1, 
        OBDIV2, OBDIV3, OBDIV4, OBMUX0, OBMUX1, OBMUX2, DLYYB0, 
        DLYYB1, DLYYB2, DLYYB3, DLYYB4, DLYGLB0, DLYGLB1, DLYGLB2, 
        DLYGLB3, DLYGLB4, OCDIV0, OCDIV1, OCDIV2, OCDIV3, OCDIV4, 
        OCMUX0, OCMUX1, OCMUX2, DLYYC0, DLYYC1, DLYYC2, DLYYC3, 
        DLYYC4, DLYGLC0, DLYGLC1, DLYGLC2, DLYGLC3, DLYGLC4, 
        FINDIV0, FINDIV1, FINDIV2, FINDIV3, FINDIV4, FINDIV5, 
        FINDIV6, FBDIV0, FBDIV1, FBDIV2, FBDIV3, FBDIV4, FBDIV5, 
        FBDIV6, FBDLY0, FBDLY1, FBDLY2, FBDLY3, FBDLY4, FBSEL0, 
        FBSEL1, XDLYSEL, VCOSEL0, VCOSEL1, VCOSEL2 : in std_logic := 
        'U') ;
    end component;

    component VCC
        port( Y : out std_logic);
    end component;

    component GND
        port( Y : out std_logic);
    end component;

    signal VCC_1_net, GND_1_net : std_logic ;
    begin   

    VCC_2_net : VCC port map(Y => VCC_1_net);
    GND_2_net : GND port map(Y => GND_1_net);
    Core : PLL
      generic map(VCOFREQUENCY => 50.000)

      port map(CLKA => CLKA, EXTFB => GND_1_net, POWERDOWN => 
        POWERDOWN, GLA => GLA, LOCK => LOCK, GLB => OPEN , YB => 
        OPEN , GLC => OPEN , YC => OPEN , OADIV0 => GND_1_net, 
        OADIV1 => GND_1_net, OADIV2 => GND_1_net, OADIV3 => 
        GND_1_net, OADIV4 => GND_1_net, OAMUX0 => GND_1_net, 
        OAMUX1 => VCC_1_net, OAMUX2 => GND_1_net, DLYGLA0 => 
        GND_1_net, DLYGLA1 => GND_1_net, DLYGLA2 => GND_1_net, 
        DLYGLA3 => GND_1_net, DLYGLA4 => GND_1_net, OBDIV0 => 
        GND_1_net, OBDIV1 => GND_1_net, OBDIV2 => GND_1_net, 
        OBDIV3 => GND_1_net, OBDIV4 => GND_1_net, OBMUX0 => 
        GND_1_net, OBMUX1 => GND_1_net, OBMUX2 => GND_1_net, 
        DLYYB0 => GND_1_net, DLYYB1 => GND_1_net, DLYYB2 => 
        GND_1_net, DLYYB3 => GND_1_net, DLYYB4 => GND_1_net, 
        DLYGLB0 => GND_1_net, DLYGLB1 => GND_1_net, DLYGLB2 => 
        GND_1_net, DLYGLB3 => GND_1_net, DLYGLB4 => GND_1_net, 
        OCDIV0 => GND_1_net, OCDIV1 => GND_1_net, OCDIV2 => 
        GND_1_net, OCDIV3 => GND_1_net, OCDIV4 => GND_1_net, 
        OCMUX0 => GND_1_net, OCMUX1 => GND_1_net, OCMUX2 => 
        GND_1_net, DLYYC0 => GND_1_net, DLYYC1 => GND_1_net, 
        DLYYC2 => GND_1_net, DLYYC3 => GND_1_net, DLYYC4 => 
        GND_1_net, DLYGLC0 => GND_1_net, DLYGLC1 => GND_1_net, 
        DLYGLC2 => GND_1_net, DLYGLC3 => GND_1_net, DLYGLC4 => 
        GND_1_net, FINDIV0 => GND_1_net, FINDIV1 => GND_1_net, 
        FINDIV2 => GND_1_net, FINDIV3 => VCC_1_net, FINDIV4 => 
        GND_1_net, FINDIV5 => GND_1_net, FINDIV6 => GND_1_net, 
        FBDIV0 => GND_1_net, FBDIV1 => GND_1_net, FBDIV2 => 
        GND_1_net, FBDIV3 => VCC_1_net, FBDIV4 => GND_1_net, 
        FBDIV5 => GND_1_net, FBDIV6 => GND_1_net, FBDLY0 => 
        GND_1_net, FBDLY1 => GND_1_net, FBDLY2 => GND_1_net, 
        FBDLY3 => GND_1_net, FBDLY4 => GND_1_net, FBSEL0 => 
        VCC_1_net, FBSEL1 => GND_1_net, XDLYSEL => GND_1_net, 
        VCOSEL0 => GND_1_net, VCOSEL1 => VCC_1_net, VCOSEL2 => 
        GND_1_net);
end DEF_ARCH;

library ieee;
use ieee.std_logic_1164.all;
library proasic3;

entity adsu_fast_apa3 is 
    port( DataA : in std_logic_vector(31 downto 0); DataB : in 
        std_logic_vector(31 downto 0);Cin, Addsub : in std_logic; 
        Sum : out std_logic_vector(31 downto 0); Cout : out 
        std_logic) ;
end adsu_fast_apa3;


architecture DEF_ARCH of  adsu_fast_apa3 is

    component INV
        port(A : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component AO1
        port(A, B, C : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component XNOR2
        port(A, B : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component AND2
        port(A, B : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component XOR2
        port(A, B : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component BUFF
        port(A : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component OR3
        port(A, B, C : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    signal AddsubAux_0_net, AddsubAux_6_net, AddsubAux_12_net, 
        AddsubAux_18_net, AddsubAux_24_net, AddsubAux_29_net, 
        DataBXnor2_0_net, DataBXnor2_1_net, DataBXnor2_2_net, 
        DataBXnor2_3_net, DataBXnor2_4_net, DataBXnor2_5_net, 
        DataBXnor2_6_net, DataBXnor2_7_net, DataBXnor2_8_net, 
        DataBXnor2_9_net, DataBXnor2_10_net, DataBXnor2_11_net, 
        DataBXnor2_12_net, DataBXnor2_13_net, DataBXnor2_14_net, 
        DataBXnor2_15_net, DataBXnor2_16_net, DataBXnor2_17_net, 
        DataBXnor2_18_net, DataBXnor2_19_net, DataBXnor2_20_net, 
        DataBXnor2_21_net, DataBXnor2_22_net, DataBXnor2_23_net, 
        DataBXnor2_24_net, DataBXnor2_25_net, DataBXnor2_26_net, 
        DataBXnor2_27_net, DataBXnor2_28_net, DataBXnor2_29_net, 
        DataBXnor2_30_net, DataBXnor2_31_net, addci, addco, 
        INV_0_Y, AND2_6_Y, AND2_86_Y, AND2_117_Y, AND2_39_Y, 
        AND2_90_Y, AND2_80_Y, AND2_97_Y, AND2_79_Y, AND2_105_Y, 
        AND2_45_Y, AND2_36_Y, AND2_73_Y, AND2_94_Y, AND2_64_Y, 
        AND2_56_Y, AND2_22_Y, AND2_21_Y, AND2_74_Y, AND2_58_Y, 
        AND2_1_Y, AND2_62_Y, AND2_114_Y, AND2_124_Y, AND2_98_Y, 
        AND2_91_Y, AND2_50_Y, AND2_49_Y, AND2_115_Y, AND2_92_Y, 
        AND2_34_Y, AND2_75_Y, AND2_123_Y, AND2_11_Y, AND2_118_Y, 
        XOR2_15_Y, XOR2_37_Y, XOR2_22_Y, XOR2_47_Y, XOR2_29_Y, 
        XOR2_23_Y, XOR2_34_Y, XOR2_17_Y, XOR2_1_Y, XOR2_42_Y, 
        XOR2_50_Y, XOR2_63_Y, XOR2_51_Y, XOR2_41_Y, XOR2_45_Y, 
        XOR2_46_Y, XOR2_56_Y, XOR2_59_Y, XOR2_40_Y, XOR2_21_Y, 
        XOR2_31_Y, XOR2_38_Y, XOR2_32_Y, XOR2_14_Y, XOR2_28_Y, 
        XOR2_30_Y, XOR2_35_Y, XOR2_36_Y, XOR2_12_Y, XOR2_54_Y, 
        XOR2_3_Y, XOR2_18_Y, AND2_88_Y, AO1_42_Y, AND2_31_Y, 
        AO1_74_Y, AND2_96_Y, AO1_75_Y, AND2_40_Y, AO1_60_Y, 
        AND2_126_Y, AO1_39_Y, AND2_77_Y, AO1_25_Y, AND2_53_Y, 
        AO1_79_Y, AND2_113_Y, AO1_86_Y, AND2_25_Y, AO1_17_Y, 
        AND2_107_Y, AO1_16_Y, AND2_30_Y, AO1_65_Y, AND2_95_Y, 
        AO1_48_Y, AND2_33_Y, AO1_56_Y, AND2_110_Y, AO1_90_Y, 
        AND2_61_Y, AO1_27_Y, AND2_19_Y, AND2_52_Y, AND2_112_Y, 
        AO1_31_Y, AND2_23_Y, AO1_7_Y, AND2_106_Y, AO1_68_Y, 
        AND2_29_Y, AO1_23_Y, AND2_93_Y, AO1_3_Y, AND2_32_Y, 
        AO1_13_Y, AND2_109_Y, AO1_49_Y, AND2_60_Y, AO1_77_Y, 
        AND2_18_Y, AO1_40_Y, AND2_89_Y, AO1_33_Y, AND2_16_Y, 
        AO1_82_Y, AND2_54_Y, AO1_57_Y, AND2_12_Y, AO1_51_Y, 
        AND2_59_Y, AO1_6_Y, AND2_0_Y, AO1_85_Y, AND2_63_Y, 
        AO1_0_Y, AND2_15_Y, AO1_36_Y, AND2_102_Y, AO1_59_Y, 
        AND2_51_Y, AND2_37_Y, AND2_87_Y, AND2_5_Y, AND2_82_Y, 
        AO1_50_Y, AND2_8_Y, AO1_4_Y, AND2_71_Y, AO1_84_Y, 
        AND2_13_Y, AO1_91_Y, AND2_84_Y, AO1_34_Y, AND2_44_Y, 
        AO1_58_Y, AND2_2_Y, AO1_21_Y, AND2_46_Y, AO1_12_Y, 
        AND2_103_Y, AO1_66_Y, AND2_17_Y, AO1_43_Y, AND2_99_Y, 
        AO1_71_Y, AND2_20_Y, AO1_29_Y, AND2_81_Y, AO1_11_Y, 
        AND2_27_Y, AO1_20_Y, AND2_101_Y, AO1_52_Y, AND2_55_Y, 
        AO1_83_Y, AND2_14_Y, AO1_41_Y, AND2_26_Y, AND2_72_Y, 
        AND2_120_Y, AND2_67_Y, AND2_122_Y, AND2_57_Y, AND2_125_Y, 
        AND2_69_Y, AND2_35_Y, AO1_26_Y, AND2_119_Y, AO1_78_Y, 
        AND2_42_Y, AO1_70_Y, AND2_108_Y, AO1_30_Y, AND2_104_Y, 
        AO1_5_Y, AND2_24_Y, AO1_61_Y, AND2_111_Y, AO1_19_Y, 
        AND2_10_Y, AO1_2_Y, AND2_65_Y, AO1_10_Y, AND2_43_Y, 
        AO1_45_Y, AND2_41_Y, AO1_73_Y, AND2_76_Y, AO1_35_Y, 
        AND2_68_Y, AND2_7_Y, AND2_3_Y, AND2_48_Y, AND2_9_Y, 
        AND2_38_Y, AND2_100_Y, AND2_70_Y, AND2_66_Y, AND2_116_Y, 
        AND2_28_Y, AND2_83_Y, AND2_78_Y, AND2_4_Y, AND2_85_Y, 
        AND2_121_Y, AND2_47_Y, OR3_0_Y, AO1_9_Y, AO1_80_Y, 
        AO1_72_Y, AO1_22_Y, AO1_15_Y, AO1_67_Y, AO1_44_Y, 
        AO1_38_Y, AO1_87_Y, AO1_64_Y, AO1_14_Y, AO1_62_Y, 
        AO1_47_Y, AO1_55_Y, AO1_89_Y, AO1_28_Y, AO1_76_Y, 
        AO1_54_Y, AO1_53_Y, AO1_8_Y, AO1_88_Y, AO1_1_Y, AO1_37_Y, 
        AO1_63_Y, AO1_24_Y, AO1_18_Y, AO1_69_Y, AO1_46_Y, 
        AO1_32_Y, AO1_81_Y, XOR2_4_Y, XOR2_0_Y, XOR2_11_Y, 
        XOR2_58_Y, XOR2_6_Y, XOR2_7_Y, XOR2_2_Y, XOR2_13_Y, 
        XOR2_60_Y, XOR2_26_Y, XOR2_27_Y, XOR2_20_Y, XOR2_33_Y, 
        XOR2_8_Y, XOR2_55_Y, XOR2_57_Y, XOR2_52_Y, XOR2_61_Y, 
        XOR2_43_Y, XOR2_48_Y, XOR2_49_Y, XOR2_44_Y, XOR2_53_Y, 
        XOR2_39_Y, XOR2_9_Y, XOR2_10_Y, XOR2_5_Y, XOR2_16_Y, 
        XOR2_62_Y, XOR2_24_Y, XOR2_25_Y, XOR2_19_Y : std_logic ;
    begin   

    INV_0 : INV
      port map(A => addci, Y => INV_0_Y);
    AO1_52 : AO1
      port map(A => AND2_61_Y, B => AO1_0_Y, C => AO1_90_Y, Y => 
        AO1_52_Y);
    XNOR2_29_inst : XNOR2
      port map(A => DataB(29), B => AddsubAux_29_net, Y => 
        DataBXnor2_29_net);
    AND2_2 : AND2
      port map(A => AND2_109_Y, B => AND2_60_Y, Y => AND2_2_Y);
    XNOR2_26_inst : XNOR2
      port map(A => DataB(26), B => AddsubAux_24_net, Y => 
        DataBXnor2_26_net);
    AND2_20 : AND2
      port map(A => AND2_54_Y, B => AND2_59_Y, Y => AND2_20_Y);
    XOR2_Sum_30_inst : XOR2
      port map(A => XOR2_25_Y, B => AO1_32_Y, Y => Sum(30));
    AO1_11 : AO1
      port map(A => AND2_59_Y, B => AO1_82_Y, C => AO1_51_Y, Y => 
        AO1_11_Y);
    AND2_11 : AND2
      port map(A => DataA(30), B => DataBXnor2_30_net, Y => 
        AND2_11_Y);
    AND2_22 : AND2
      port map(A => DataA(13), B => DataBXnor2_13_net, Y => 
        AND2_22_Y);
    AND2_71 : AND2
      port map(A => AND2_23_Y, B => AND2_29_Y, Y => AND2_71_Y);
    XOR2_19 : XOR2
      port map(A => DataA(31), B => DataBXnor2_31_net, Y => 
        XOR2_19_Y);
    AND2_44 : AND2
      port map(A => AND2_109_Y, B => AND2_53_Y, Y => AND2_44_Y);
    XOR2_1 : XOR2
      port map(A => DataA(8), B => DataBXnor2_8_net, Y => 
        XOR2_1_Y);
    XOR2_23 : XOR2
      port map(A => DataA(5), B => DataBXnor2_5_net, Y => 
        XOR2_23_Y);
    AO1_31 : AO1
      port map(A => AND2_31_Y, B => AO1_9_Y, C => AO1_42_Y, Y => 
        AO1_31_Y);
    AND2_104 : AND2
      port map(A => AND2_13_Y, B => AND2_46_Y, Y => AND2_104_Y);
    AO1_67 : AO1
      port map(A => AND2_106_Y, B => AO1_72_Y, C => AO1_7_Y, Y => 
        AO1_67_Y);
    XNOR2_13_inst : XNOR2
      port map(A => DataB(13), B => AddsubAux_12_net, Y => 
        DataBXnor2_13_net);
    XOR2_47 : XOR2
      port map(A => DataA(3), B => DataBXnor2_3_net, Y => 
        XOR2_47_Y);
    XOR2_38 : XOR2
      port map(A => DataA(21), B => DataBXnor2_21_net, Y => 
        XOR2_38_Y);
    XNOR2_8_inst : XNOR2
      port map(A => DataB(8), B => AddsubAux_6_net, Y => 
        DataBXnor2_8_net);
    XOR2_Sum_21_inst : XOR2
      port map(A => XOR2_44_Y, B => AO1_8_Y, Y => Sum(21));
    AND2_124 : AND2
      port map(A => DataA(20), B => DataBXnor2_20_net, Y => 
        AND2_124_Y);
    XOR2_Sum_8_inst : XOR2
      port map(A => XOR2_60_Y, B => AO1_44_Y, Y => Sum(8));
    AO1_7 : AO1
      port map(A => XOR2_34_Y, B => AO1_74_Y, C => AND2_105_Y, 
        Y => AO1_7_Y);
    XOR2_Sum_25_inst : XOR2
      port map(A => XOR2_10_Y, B => AO1_63_Y, Y => Sum(25));
    AND2_18 : AND2
      port map(A => AND2_53_Y, B => AND2_113_Y, Y => AND2_18_Y);
    AND2_15 : AND2
      port map(A => AND2_33_Y, B => AND2_110_Y, Y => AND2_15_Y);
    AND2_84 : AND2
      port map(A => AND2_32_Y, B => XOR2_51_Y, Y => AND2_84_Y);
    AO1_25 : AO1
      port map(A => XOR2_41_Y, B => AND2_56_Y, C => AND2_22_Y, 
        Y => AO1_25_Y);
    AND2_78 : AND2
      port map(A => AND2_108_Y, B => AND2_65_Y, Y => AND2_78_Y);
    AND2_75 : AND2
      port map(A => DataA(28), B => DataBXnor2_28_net, Y => 
        AND2_75_Y);
    XOR2_45 : XOR2
      port map(A => DataA(14), B => DataBXnor2_14_net, Y => 
        XOR2_45_Y);
    XNOR2_31_inst : XNOR2
      port map(A => DataB(31), B => AddsubAux_29_net, Y => 
        DataBXnor2_31_net);
    AND2_1 : AND2
      port map(A => DataA(17), B => DataBXnor2_17_net, Y => 
        AND2_1_Y);
    XOR2_Sum_5_inst : XOR2
      port map(A => XOR2_7_Y, B => AO1_22_Y, Y => Sum(5));
    AND2_49 : AND2
      port map(A => DataA(24), B => DataBXnor2_24_net, Y => 
        AND2_49_Y);
    AO1_8 : AO1
      port map(A => AND2_103_Y, B => AO1_26_Y, C => AO1_12_Y, 
        Y => AO1_8_Y);
    AO1_78 : AO1
      port map(A => AND2_46_Y, B => AO1_84_Y, C => AO1_21_Y, Y => 
        AO1_78_Y);
    XNOR2_11_inst : XNOR2
      port map(A => DataB(11), B => AddsubAux_6_net, Y => 
        DataBXnor2_11_net);
    AND2_10 : AND2
      port map(A => AND2_81_Y, B => AND2_0_Y, Y => AND2_10_Y);
    AND2_7 : AND2
      port map(A => AND2_35_Y, B => XOR2_56_Y, Y => AND2_7_Y);
    XOR2_20 : XOR2
      port map(A => DataA(11), B => DataBXnor2_11_net, Y => 
        XOR2_20_Y);
    XOR2_63 : XOR2
      port map(A => DataA(11), B => DataBXnor2_11_net, Y => 
        XOR2_63_Y);
    AND2_70 : AND2
      port map(A => AND2_42_Y, B => AND2_99_Y, Y => AND2_70_Y);
    XNOR2_9_inst : XNOR2
      port map(A => DataB(9), B => AddsubAux_6_net, Y => 
        DataBXnor2_9_net);
    AND2_12 : AND2
      port map(A => AND2_30_Y, B => XOR2_32_Y, Y => AND2_12_Y);
    XOR2_52 : XOR2
      port map(A => DataA(16), B => DataBXnor2_16_net, Y => 
        XOR2_52_Y);
    AO1_42 : AO1
      port map(A => XOR2_47_Y, B => AND2_90_Y, C => AND2_80_Y, 
        Y => AO1_42_Y);
    AND2_72 : AND2
      port map(A => AND2_82_Y, B => XOR2_1_Y, Y => AND2_72_Y);
    AO1_50 : AO1
      port map(A => AND2_29_Y, B => AO1_31_Y, C => AO1_68_Y, Y => 
        AO1_50_Y);
    AND2_61 : AND2
      port map(A => XOR2_12_Y, B => XOR2_54_Y, Y => AND2_61_Y);
    AO1_15 : AO1
      port map(A => AND2_96_Y, B => AO1_72_Y, C => AO1_74_Y, Y => 
        AO1_15_Y);
    BUFF_AddsubAux_6_inst : BUFF
      port map(A => Addsub, Y => AddsubAux_6_net);
    AO1_88 : AO1
      port map(A => AND2_17_Y, B => AO1_78_Y, C => AO1_66_Y, Y => 
        AO1_88_Y);
    AND2_89 : AND2
      port map(A => AND2_25_Y, B => XOR2_40_Y, Y => AND2_89_Y);
    XOR2_24 : XOR2
      port map(A => DataA(29), B => DataBXnor2_29_net, Y => 
        XOR2_24_Y);
    AND2_57 : AND2
      port map(A => AND2_8_Y, B => AND2_84_Y, Y => AND2_57_Y);
    XOR2_21 : XOR2
      port map(A => DataA(19), B => DataBXnor2_19_net, Y => 
        XOR2_21_Y);
    AO1_35 : AO1
      port map(A => AND2_26_Y, B => AO1_11_Y, C => AO1_41_Y, Y => 
        AO1_35_Y);
    XNOR2_14_inst : XNOR2
      port map(A => DataB(14), B => AddsubAux_12_net, Y => 
        DataBXnor2_14_net);
    AO1_56 : AO1
      port map(A => XOR2_36_Y, B => AND2_92_Y, C => AND2_34_Y, 
        Y => AO1_56_Y);
    AND2_46 : AND2
      port map(A => AND2_109_Y, B => AND2_18_Y, Y => AND2_46_Y);
    AO1_53 : AO1
      port map(A => AND2_16_Y, B => AO1_26_Y, C => AO1_33_Y, Y => 
        AO1_53_Y);
    AO1_61 : AO1
      port map(A => AND2_33_Y, B => AO1_29_Y, C => AO1_48_Y, Y => 
        AO1_61_Y);
    XOR2_16 : XOR2
      port map(A => DataA(27), B => DataBXnor2_27_net, Y => 
        XOR2_16_Y);
    XOR2_60 : XOR2
      port map(A => DataA(8), B => DataBXnor2_8_net, Y => 
        XOR2_60_Y);
    AND2_68 : AND2
      port map(A => AND2_27_Y, B => AND2_26_Y, Y => AND2_68_Y);
    AND2_65 : AND2
      port map(A => AND2_81_Y, B => AND2_63_Y, Y => AND2_65_Y);
    AND2_43 : AND2
      port map(A => AND2_81_Y, B => AND2_101_Y, Y => AND2_43_Y);
    AO1_24 : AO1
      port map(A => AND2_111_Y, B => AO1_70_Y, C => AO1_61_Y, 
        Y => AO1_24_Y);
    XNOR2_17_inst : XNOR2
      port map(A => DataB(17), B => AddsubAux_12_net, Y => 
        DataBXnor2_17_net);
    BUFF_AddsubAux_12_inst : BUFF
      port map(A => Addsub, Y => AddsubAux_12_net);
    XOR2_Sum_17_inst : XOR2
      port map(A => XOR2_61_Y, B => AO1_28_Y, Y => Sum(17));
    AND2_86 : AND2
      port map(A => DataA(0), B => INV_0_Y, Y => AND2_86_Y);
    AND2_6 : AND2
      port map(A => DataA(0), B => DataBXnor2_0_net, Y => 
        AND2_6_Y);
    AND2_60 : AND2
      port map(A => AND2_53_Y, B => XOR2_45_Y, Y => AND2_60_Y);
    XOR2_61 : XOR2
      port map(A => DataA(17), B => DataBXnor2_17_net, Y => 
        XOR2_61_Y);
    AO1_59 : AO1
      port map(A => AND2_19_Y, B => AO1_90_Y, C => AO1_27_Y, Y => 
        AO1_59_Y);
    AND2_83 : AND2
      port map(A => AND2_108_Y, B => AND2_10_Y, Y => AND2_83_Y);
    XOR2_57 : XOR2
      port map(A => DataA(15), B => DataBXnor2_15_net, Y => 
        XOR2_57_Y);
    AND2_62 : AND2
      port map(A => DataA(18), B => DataBXnor2_18_net, Y => 
        AND2_62_Y);
    XOR2_33 : XOR2
      port map(A => DataA(12), B => DataBXnor2_12_net, Y => 
        XOR2_33_Y);
    BUFF_AddsubAux_24_inst : BUFF
      port map(A => Addsub, Y => AddsubAux_24_net);
    AND2_97 : AND2
      port map(A => DataA(4), B => DataBXnor2_4_net, Y => 
        AND2_97_Y);
    AO1_91 : AO1
      port map(A => XOR2_51_Y, B => AO1_3_Y, C => AND2_56_Y, Y => 
        AO1_91_Y);
    XOR2_49 : XOR2
      port map(A => DataA(20), B => DataBXnor2_20_net, Y => 
        XOR2_49_Y);
    AO1_14 : AO1
      port map(A => AND2_32_Y, B => AO1_50_Y, C => AO1_3_Y, Y => 
        AO1_14_Y);
    XOR2_4 : XOR2
      port map(A => DataA(0), B => DataBXnor2_0_net, Y => 
        XOR2_4_Y);
    AO1_72 : AO1
      port map(A => AND2_31_Y, B => AO1_9_Y, C => AO1_42_Y, Y => 
        AO1_72_Y);
    XNOR2_23_inst : XNOR2
      port map(A => DataB(23), B => AddsubAux_18_net, Y => 
        DataBXnor2_23_net);
    XOR2_55 : XOR2
      port map(A => DataA(14), B => DataBXnor2_14_net, Y => 
        XOR2_55_Y);
    AND2_24 : AND2
      port map(A => AND2_20_Y, B => XOR2_28_Y, Y => AND2_24_Y);
    AO1_40 : AO1
      port map(A => XOR2_40_Y, B => AO1_86_Y, C => AND2_62_Y, 
        Y => AO1_40_Y);
    AND2_31 : AND2
      port map(A => XOR2_22_Y, B => XOR2_47_Y, Y => AND2_31_Y);
    AND2_113 : AND2
      port map(A => XOR2_45_Y, B => XOR2_46_Y, Y => AND2_113_Y);
    AO1_34 : AO1
      port map(A => AND2_53_Y, B => AO1_13_Y, C => AO1_25_Y, Y => 
        AO1_34_Y);
    XOR2_18 : XOR2
      port map(A => DataA(31), B => DataBXnor2_31_net, Y => 
        XOR2_18_Y);
    ADDERCI : XOR2
      port map(A => Cin, B => AddsubAux_29_net, Y => addci);
    XOR2_Sum_4_inst : XOR2
      port map(A => XOR2_6_Y, B => AO1_72_Y, Y => Sum(4));
    AO1_82 : AO1
      port map(A => AND2_107_Y, B => AO1_86_Y, C => AO1_17_Y, 
        Y => AO1_82_Y);
    AO1_46 : AO1
      port map(A => AND2_43_Y, B => AO1_70_Y, C => AO1_10_Y, Y => 
        AO1_46_Y);
    AO1_65 : AO1
      port map(A => XOR2_14_Y, B => AND2_91_Y, C => AND2_50_Y, 
        Y => AO1_65_Y);
    XOR2_8 : XOR2
      port map(A => DataA(13), B => DataBXnor2_13_net, Y => 
        XOR2_8_Y);
    AND2_116 : AND2
      port map(A => AND2_42_Y, B => AND2_24_Y, Y => AND2_116_Y);
    AND2_110 : AND2
      port map(A => XOR2_35_Y, B => XOR2_36_Y, Y => AND2_110_Y);
    XNOR2_21_inst : XNOR2
      port map(A => DataB(21), B => AddsubAux_18_net, Y => 
        DataBXnor2_21_net);
    AO1_43 : AO1
      port map(A => AND2_12_Y, B => AO1_33_Y, C => AO1_57_Y, Y => 
        AO1_43_Y);
    XOR2_30 : XOR2
      port map(A => DataA(25), B => DataBXnor2_25_net, Y => 
        XOR2_30_Y);
    AND2_38 : AND2
      port map(A => AND2_119_Y, B => AND2_103_Y, Y => AND2_38_Y);
    AND2_35 : AND2
      port map(A => AND2_71_Y, B => AND2_46_Y, Y => AND2_35_Y);
    AND2_29 : AND2
      port map(A => AND2_96_Y, B => AND2_40_Y, Y => AND2_29_Y);
    XNOR2_2_inst : XNOR2
      port map(A => DataB(2), B => AddsubAux_0_net, Y => 
        DataBXnor2_2_net);
    AND2_117 : AND2
      port map(A => DataBXnor2_0_net, B => INV_0_Y, Y => 
        AND2_117_Y);
    XOR2_34 : XOR2
      port map(A => DataA(6), B => DataBXnor2_6_net, Y => 
        XOR2_34_Y);
    XOR2_31 : XOR2
      port map(A => DataA(20), B => DataBXnor2_20_net, Y => 
        XOR2_31_Y);
    BUFF_AddsubAux_0_inst : BUFF
      port map(A => Addsub, Y => AddsubAux_0_net);
    AND2_3 : AND2
      port map(A => AND2_119_Y, B => AND2_25_Y, Y => AND2_3_Y);
    AO1_49 : AO1
      port map(A => XOR2_45_Y, B => AO1_25_Y, C => AND2_21_Y, 
        Y => AO1_49_Y);
    AND2_30 : AND2
      port map(A => XOR2_31_Y, B => XOR2_38_Y, Y => AND2_30_Y);
    AO1_57 : AO1
      port map(A => XOR2_32_Y, B => AO1_16_Y, C => AND2_91_Y, 
        Y => AO1_57_Y);
    XNOR2_0_inst : XNOR2
      port map(A => DataB(0), B => AddsubAux_0_net, Y => 
        DataBXnor2_0_net);
    XNOR2_24_inst : XNOR2
      port map(A => DataB(24), B => AddsubAux_24_net, Y => 
        DataBXnor2_24_net);
    AND2_14 : AND2
      port map(A => AND2_15_Y, B => AND2_102_Y, Y => AND2_14_Y);
    AND2_103 : AND2
      port map(A => AND2_16_Y, B => XOR2_31_Y, Y => AND2_103_Y);
    BUFF_AddsubAux_18_inst : BUFF
      port map(A => Addsub, Y => AddsubAux_18_net);
    XOR2_Sum_2_inst : XOR2
      port map(A => XOR2_11_Y, B => AO1_9_Y, Y => Sum(2));
    AND2_74 : AND2
      port map(A => DataA(15), B => DataBXnor2_15_net, Y => 
        AND2_74_Y);
    AND2_32 : AND2
      port map(A => AND2_126_Y, B => AND2_77_Y, Y => AND2_32_Y);
    XOR2_Sum_23_inst : XOR2
      port map(A => XOR2_39_Y, B => AO1_1_Y, Y => Sum(23));
    AND2_123 : AND2
      port map(A => DataA(29), B => DataBXnor2_29_net, Y => 
        AND2_123_Y);
    XOR2_Sum_1_inst : XOR2
      port map(A => XOR2_0_Y, B => OR3_0_Y, Y => Sum(1));
    XOR2_46 : XOR2
      port map(A => DataA(15), B => DataBXnor2_15_net, Y => 
        XOR2_46_Y);
    XOR2_Sum_16_inst : XOR2
      port map(A => XOR2_52_Y, B => AO1_89_Y, Y => Sum(16));
    XNOR2_27_inst : XNOR2
      port map(A => DataB(27), B => AddsubAux_24_net, Y => 
        DataBXnor2_27_net);
    AO1_2 : AO1
      port map(A => AND2_63_Y, B => AO1_29_Y, C => AO1_85_Y, Y => 
        AO1_2_Y);
    XOR2_Sum_7_inst : XOR2
      port map(A => XOR2_13_Y, B => AO1_67_Y, Y => Sum(7));
    AO1_70 : AO1
      port map(A => AND2_46_Y, B => AO1_84_Y, C => AO1_21_Y, Y => 
        AO1_70_Y);
    AND2_106 : AND2
      port map(A => AND2_96_Y, B => XOR2_34_Y, Y => AND2_106_Y);
    AND2_100 : AND2
      port map(A => AND2_42_Y, B => AND2_17_Y, Y => AND2_100_Y);
    AND2_26 : AND2
      port map(A => AND2_15_Y, B => AND2_51_Y, Y => AND2_26_Y);
    AND2_111 : AND2
      port map(A => AND2_81_Y, B => AND2_33_Y, Y => AND2_111_Y);
    XOR2_9 : XOR2
      port map(A => DataA(24), B => DataBXnor2_24_net, Y => 
        XOR2_9_Y);
    AND2_126 : AND2
      port map(A => XOR2_1_Y, B => XOR2_42_Y, Y => AND2_126_Y);
    AND2_120 : AND2
      port map(A => AND2_8_Y, B => AND2_126_Y, Y => AND2_120_Y);
    XOR2_59 : XOR2
      port map(A => DataA(17), B => DataBXnor2_17_net, Y => 
        XOR2_59_Y);
    AO1_64 : AO1
      port map(A => AND2_93_Y, B => AO1_50_Y, C => AO1_23_Y, Y => 
        AO1_64_Y);
    AND2_23 : AND2
      port map(A => AND2_88_Y, B => AND2_31_Y, Y => AND2_23_Y);
    XOR2_5 : XOR2
      port map(A => DataA(26), B => DataBXnor2_26_net, Y => 
        XOR2_5_Y);
    XNOR2_12_inst : XNOR2
      port map(A => DataB(12), B => AddsubAux_12_net, Y => 
        DataBXnor2_12_net);
    AO1_76 : AO1
      port map(A => AND2_25_Y, B => AO1_26_Y, C => AO1_86_Y, Y => 
        AO1_76_Y);
    AO1_80 : AO1
      port map(A => XOR2_22_Y, B => AO1_9_Y, C => AND2_90_Y, Y => 
        AO1_80_Y);
    AND2_107 : AND2
      port map(A => XOR2_40_Y, B => XOR2_21_Y, Y => AND2_107_Y);
    AND2_19 : AND2
      port map(A => XOR2_3_Y, B => XOR2_18_Y, Y => AND2_19_Y);
    AO1_28 : AO1
      port map(A => XOR2_56_Y, B => AO1_89_Y, C => AND2_58_Y, 
        Y => AO1_28_Y);
    AO1_73 : AO1
      port map(A => AND2_14_Y, B => AO1_11_Y, C => AO1_83_Y, Y => 
        AO1_73_Y);
    AND2_79 : AND2
      port map(A => DataA(5), B => DataBXnor2_5_net, Y => 
        AND2_79_Y);
    AND2_119 : AND2
      port map(A => AND2_71_Y, B => AND2_46_Y, Y => AND2_119_Y);
    XOR2_22 : XOR2
      port map(A => DataA(2), B => DataBXnor2_2_net, Y => 
        XOR2_22_Y);
    AO1_1 : AO1
      port map(A => AND2_99_Y, B => AO1_78_Y, C => AO1_43_Y, Y => 
        AO1_1_Y);
    XOR2_13 : XOR2
      port map(A => DataA(7), B => DataBXnor2_7_net, Y => 
        XOR2_13_Y);
    XNOR2_6_inst : XNOR2
      port map(A => DataB(6), B => AddsubAux_6_net, Y => 
        DataBXnor2_6_net);
    AO1_86 : AO1
      port map(A => XOR2_59_Y, B => AND2_58_Y, C => AND2_1_Y, 
        Y => AO1_86_Y);
    XNOR2_18_inst : XNOR2
      port map(A => DataB(18), B => AddsubAux_18_net, Y => 
        DataBXnor2_18_net);
    AND2_51 : AND2
      port map(A => AND2_61_Y, B => AND2_19_Y, Y => AND2_51_Y);
    XOR2_Sum_29_inst : XOR2
      port map(A => XOR2_24_Y, B => AO1_46_Y, Y => Sum(29));
    AO1_3 : AO1
      port map(A => AND2_77_Y, B => AO1_60_Y, C => AO1_39_Y, Y => 
        AO1_3_Y);
    AO1_83 : AO1
      port map(A => AND2_102_Y, B => AO1_0_Y, C => AO1_36_Y, Y => 
        AO1_83_Y);
    AND2_64 : AND2
      port map(A => DataA(11), B => DataBXnor2_11_net, Y => 
        AND2_64_Y);
    AND2_47 : AND2
      port map(A => AND2_104_Y, B => AND2_68_Y, Y => AND2_47_Y);
    AO1_51 : AO1
      port map(A => AND2_95_Y, B => AO1_16_Y, C => AO1_65_Y, Y => 
        AO1_51_Y);
    AO1_18 : AO1
      port map(A => AND2_10_Y, B => AO1_70_Y, C => AO1_19_Y, Y => 
        AO1_18_Y);
    AO1_79 : AO1
      port map(A => XOR2_46_Y, B => AND2_21_Y, C => AND2_74_Y, 
        Y => AO1_79_Y);
    XOR2_48 : XOR2
      port map(A => DataA(19), B => DataBXnor2_19_net, Y => 
        XOR2_48_Y);
    AO1_47 : AO1
      port map(A => AND2_44_Y, B => AO1_4_Y, C => AO1_34_Y, Y => 
        AO1_47_Y);
    XOR2_Sum_14_inst : XOR2
      port map(A => XOR2_55_Y, B => AO1_47_Y, Y => Sum(14));
    AND2_101 : AND2
      port map(A => AND2_63_Y, B => XOR2_12_Y, Y => AND2_101_Y);
    AND2_16 : AND2
      port map(A => AND2_25_Y, B => AND2_107_Y, Y => AND2_16_Y);
    XOR2_Sum_18_inst : XOR2
      port map(A => XOR2_43_Y, B => AO1_76_Y, Y => Sum(18));
    XOR2_Sum_20_inst : XOR2
      port map(A => XOR2_49_Y, B => AO1_53_Y, Y => Sum(20));
    AND2_76 : AND2
      port map(A => AND2_27_Y, B => AND2_14_Y, Y => AND2_76_Y);
    AND2_121 : AND2
      port map(A => AND2_104_Y, B => AND2_76_Y, Y => AND2_121_Y);
    XNOR2_4_inst : XNOR2
      port map(A => DataB(4), B => AddsubAux_0_net, Y => 
        DataBXnor2_4_net);
    AO1_38 : AO1
      port map(A => XOR2_1_Y, B => AO1_44_Y, C => AND2_36_Y, Y => 
        AO1_38_Y);
    XOR2_62 : XOR2
      port map(A => DataA(28), B => DataBXnor2_28_net, Y => 
        XOR2_62_Y);
    AND2_13 : AND2
      port map(A => AND2_23_Y, B => AND2_29_Y, Y => AND2_13_Y);
    AND2_87 : AND2
      port map(A => AND2_112_Y, B => AND2_96_Y, Y => AND2_87_Y);
    ADDERCO : XNOR2
      port map(A => addco, B => AddsubAux_29_net, Y => Cout);
    AO1_89 : AO1
      port map(A => AND2_46_Y, B => AO1_4_Y, C => AO1_21_Y, Y => 
        AO1_89_Y);
    AND2_73 : AND2
      port map(A => DataA(9), B => DataBXnor2_9_net, Y => 
        AND2_73_Y);
    XOR2_10 : XOR2
      port map(A => DataA(25), B => DataBXnor2_25_net, Y => 
        XOR2_10_Y);
    AND2_58 : AND2
      port map(A => DataA(16), B => DataBXnor2_16_net, Y => 
        AND2_58_Y);
    AND2_55 : AND2
      port map(A => AND2_15_Y, B => AND2_61_Y, Y => AND2_55_Y);
    AND2_109 : AND2
      port map(A => AND2_126_Y, B => AND2_77_Y, Y => AND2_109_Y);
    AND2_69 : AND2
      port map(A => AND2_71_Y, B => AND2_2_Y, Y => AND2_69_Y);
    XOR2_27 : XOR2
      port map(A => DataA(10), B => DataBXnor2_10_net, Y => 
        XOR2_27_Y);
    XOR2_7 : XOR2
      port map(A => DataA(5), B => DataBXnor2_5_net, Y => 
        XOR2_7_Y);
    AND2_5 : AND2
      port map(A => AND2_112_Y, B => AND2_106_Y, Y => AND2_5_Y);
    XOR2_56 : XOR2
      port map(A => DataA(16), B => DataBXnor2_16_net, Y => 
        XOR2_56_Y);
    XOR2_14 : XOR2
      port map(A => DataA(23), B => DataBXnor2_23_net, Y => 
        XOR2_14_Y);
    AND2_91 : AND2
      port map(A => DataA(22), B => DataBXnor2_22_net, Y => 
        AND2_91_Y);
    AND2_50 : AND2
      port map(A => DataA(23), B => DataBXnor2_23_net, Y => 
        AND2_50_Y);
    XOR2_11 : XOR2
      port map(A => DataA(2), B => DataBXnor2_2_net, Y => 
        XOR2_11_Y);
    XNOR2_Sum_0_inst : XNOR2
      port map(A => XOR2_4_Y, B => addci, Y => Sum(0));
    XOR2_Sum_22_inst : XOR2
      port map(A => XOR2_53_Y, B => AO1_88_Y, Y => Sum(22));
    XNOR2_15_inst : XNOR2
      port map(A => DataB(15), B => AddsubAux_12_net, Y => 
        DataBXnor2_15_net);
    AND2_52 : AND2
      port map(A => AND2_88_Y, B => XOR2_22_Y, Y => AND2_52_Y);
    XOR2_25 : XOR2
      port map(A => DataA(30), B => DataBXnor2_30_net, Y => 
        XOR2_25_Y);
    AO1_22 : AO1
      port map(A => XOR2_29_Y, B => AO1_72_Y, C => AND2_97_Y, 
        Y => AO1_22_Y);
    XOR2_Sum_11_inst : XOR2
      port map(A => XOR2_20_Y, B => AO1_64_Y, Y => Sum(11));
    XOR2_Sum_15_inst : XOR2
      port map(A => XOR2_57_Y, B => AO1_55_Y, Y => Sum(15));
    AO1_addco : AO1
      port map(A => AND2_68_Y, B => AO1_30_Y, C => AO1_35_Y, Y => 
        addco);
    AO1_55 : AO1
      port map(A => AND2_2_Y, B => AO1_4_Y, C => AO1_58_Y, Y => 
        AO1_55_Y);
    AND2_112 : AND2
      port map(A => AND2_88_Y, B => AND2_31_Y, Y => AND2_112_Y);
    AND2_34 : AND2
      port map(A => DataA(27), B => DataBXnor2_27_net, Y => 
        AND2_34_Y);
    AND2_66 : AND2
      port map(A => AND2_42_Y, B => AND2_20_Y, Y => AND2_66_Y);
    AO1_41 : AO1
      port map(A => AND2_51_Y, B => AO1_0_Y, C => AO1_59_Y, Y => 
        AO1_41_Y);
    AO1_6 : AO1
      port map(A => XOR2_35_Y, B => AO1_48_Y, C => AND2_92_Y, 
        Y => AO1_6_Y);
    AND2_98 : AND2
      port map(A => DataA(21), B => DataBXnor2_21_net, Y => 
        AND2_98_Y);
    AND2_95 : AND2
      port map(A => XOR2_32_Y, B => XOR2_14_Y, Y => AND2_95_Y);
    AO1_77 : AO1
      port map(A => AND2_113_Y, B => AO1_25_Y, C => AO1_79_Y, 
        Y => AO1_77_Y);
    XNOR2_1_inst : XNOR2
      port map(A => DataB(1), B => AddsubAux_0_net, Y => 
        DataBXnor2_1_net);
    XNOR2_22_inst : XNOR2
      port map(A => DataB(22), B => AddsubAux_18_net, Y => 
        DataBXnor2_22_net);
    AND2_63 : AND2
      port map(A => AND2_33_Y, B => AND2_110_Y, Y => AND2_63_Y);
    BUFF_AddsubAux_29_inst : BUFF
      port map(A => Addsub, Y => AddsubAux_29_net);
    AO1_12 : AO1
      port map(A => XOR2_31_Y, B => AO1_33_Y, C => AND2_124_Y, 
        Y => AO1_12_Y);
    XOR2_32 : XOR2
      port map(A => DataA(22), B => DataBXnor2_22_net, Y => 
        XOR2_32_Y);
    OR3_0 : OR3
      port map(A => AND2_6_Y, B => AND2_86_Y, C => AND2_117_Y, 
        Y => OR3_0_Y);
    AND2_9 : AND2
      port map(A => AND2_119_Y, B => AND2_16_Y, Y => AND2_9_Y);
    AO1_68 : AO1
      port map(A => AND2_40_Y, B => AO1_74_Y, C => AO1_75_Y, Y => 
        AO1_68_Y);
    AND2_90 : AND2
      port map(A => DataA(2), B => DataBXnor2_2_net, Y => 
        AND2_90_Y);
    AO1_87 : AO1
      port map(A => AND2_126_Y, B => AO1_50_Y, C => AO1_60_Y, 
        Y => AO1_87_Y);
    XOR2_58 : XOR2
      port map(A => DataA(3), B => DataBXnor2_3_net, Y => 
        XOR2_58_Y);
    XOR2_43 : XOR2
      port map(A => DataA(18), B => DataBXnor2_18_net, Y => 
        XOR2_43_Y);
    XNOR2_28_inst : XNOR2
      port map(A => DataB(28), B => AddsubAux_24_net, Y => 
        DataBXnor2_28_net);
    XNOR2_3_inst : XNOR2
      port map(A => DataB(3), B => AddsubAux_0_net, Y => 
        DataBXnor2_3_net);
    AO1_32 : AO1
      port map(A => AND2_41_Y, B => AO1_30_Y, C => AO1_45_Y, Y => 
        AO1_32_Y);
    AND2_92 : AND2
      port map(A => DataA(26), B => DataBXnor2_26_net, Y => 
        AND2_92_Y);
    AO1_9 : AO1
      port map(A => XOR2_37_Y, B => OR3_0_Y, C => AND2_39_Y, Y => 
        AO1_9_Y);
    AND2_39 : AND2
      port map(A => DataA(1), B => DataBXnor2_1_net, Y => 
        AND2_39_Y);
    XOR2_Sum_31_inst : XOR2
      port map(A => XOR2_19_Y, B => AO1_81_Y, Y => Sum(31));
    AND2_115 : AND2
      port map(A => DataA(25), B => DataBXnor2_25_net, Y => 
        AND2_115_Y);
    AND2_102 : AND2
      port map(A => AND2_61_Y, B => XOR2_3_Y, Y => AND2_102_Y);
    AND2_27 : AND2
      port map(A => AND2_54_Y, B => AND2_59_Y, Y => AND2_27_Y);
    AND2_122 : AND2
      port map(A => AND2_8_Y, B => AND2_32_Y, Y => AND2_122_Y);
    AO1_54 : AO1
      port map(A => AND2_89_Y, B => AO1_26_Y, C => AO1_40_Y, Y => 
        AO1_54_Y);
    AO1_20 : AO1
      port map(A => XOR2_12_Y, B => AO1_85_Y, C => AND2_75_Y, 
        Y => AO1_20_Y);
    AO1_0 : AO1
      port map(A => AND2_110_Y, B => AO1_48_Y, C => AO1_56_Y, 
        Y => AO1_0_Y);
    XOR2_29 : XOR2
      port map(A => DataA(4), B => DataBXnor2_4_net, Y => 
        XOR2_29_Y);
    XOR2_40 : XOR2
      port map(A => DataA(18), B => DataBXnor2_18_net, Y => 
        XOR2_40_Y);
    AO1_45 : AO1
      port map(A => AND2_55_Y, B => AO1_11_Y, C => AO1_52_Y, Y => 
        AO1_45_Y);
    XOR2_2 : XOR2
      port map(A => DataA(6), B => DataBXnor2_6_net, Y => 
        XOR2_2_Y);
    AO1_71 : AO1
      port map(A => AND2_59_Y, B => AO1_82_Y, C => AO1_51_Y, Y => 
        AO1_71_Y);
    XOR2_37 : XOR2
      port map(A => DataA(1), B => DataBXnor2_1_net, Y => 
        XOR2_37_Y);
    AND2_36 : AND2
      port map(A => DataA(8), B => DataBXnor2_8_net, Y => 
        AND2_36_Y);
    AO1_26 : AO1
      port map(A => AND2_46_Y, B => AO1_4_Y, C => AO1_21_Y, Y => 
        AO1_26_Y);
    AO1_23 : AO1
      port map(A => XOR2_50_Y, B => AO1_60_Y, C => AND2_94_Y, 
        Y => AO1_23_Y);
    XOR2_Sum_6_inst : XOR2
      port map(A => XOR2_2_Y, B => AO1_15_Y, Y => Sum(6));
    AND2_33 : AND2
      port map(A => XOR2_28_Y, B => XOR2_30_Y, Y => AND2_33_Y);
    XOR2_44 : XOR2
      port map(A => DataA(21), B => DataBXnor2_21_net, Y => 
        XOR2_44_Y);
    XNOR2_25_inst : XNOR2
      port map(A => DataB(25), B => AddsubAux_24_net, Y => 
        DataBXnor2_25_net);
    AO1_10 : AO1
      port map(A => AND2_101_Y, B => AO1_29_Y, C => AO1_20_Y, 
        Y => AO1_10_Y);
    XOR2_41 : XOR2
      port map(A => DataA(13), B => DataBXnor2_13_net, Y => 
        XOR2_41_Y);
    XOR2_35 : XOR2
      port map(A => DataA(26), B => DataBXnor2_26_net, Y => 
        XOR2_35_Y);
    AND2_105 : AND2
      port map(A => DataA(6), B => DataBXnor2_6_net, Y => 
        AND2_105_Y);
    AO1_81 : AO1
      port map(A => AND2_76_Y, B => AO1_30_Y, C => AO1_73_Y, Y => 
        AO1_81_Y);
    AND2_125 : AND2
      port map(A => AND2_71_Y, B => AND2_44_Y, Y => AND2_125_Y);
    AO1_30 : AO1
      port map(A => AND2_46_Y, B => AO1_84_Y, C => AO1_21_Y, Y => 
        AO1_30_Y);
    AO1_62 : AO1
      port map(A => AND2_84_Y, B => AO1_50_Y, C => AO1_91_Y, Y => 
        AO1_62_Y);
    AND2_41 : AND2
      port map(A => AND2_27_Y, B => AND2_55_Y, Y => AND2_41_Y);
    AND2_0 : AND2
      port map(A => AND2_33_Y, B => XOR2_35_Y, Y => AND2_0_Y);
    XOR2_Sum_27_inst : XOR2
      port map(A => XOR2_16_Y, B => AO1_18_Y, Y => Sum(27));
    AO1_16 : AO1
      port map(A => XOR2_38_Y, B => AND2_124_Y, C => AND2_98_Y, 
        Y => AO1_16_Y);
    AND2_17 : AND2
      port map(A => AND2_16_Y, B => AND2_30_Y, Y => AND2_17_Y);
    AO1_29 : AO1
      port map(A => AND2_59_Y, B => AO1_82_Y, C => AO1_51_Y, Y => 
        AO1_29_Y);
    XOR2_6 : XOR2
      port map(A => DataA(4), B => DataBXnor2_4_net, Y => 
        XOR2_6_Y);
    AND2_77 : AND2
      port map(A => XOR2_50_Y, B => XOR2_63_Y, Y => AND2_77_Y);
    AND2_54 : AND2
      port map(A => AND2_25_Y, B => AND2_107_Y, Y => AND2_54_Y);
    AO1_13 : AO1
      port map(A => AND2_77_Y, B => AO1_60_Y, C => AO1_39_Y, Y => 
        AO1_13_Y);
    XOR2_53 : XOR2
      port map(A => DataA(22), B => DataBXnor2_22_net, Y => 
        XOR2_53_Y);
    AO1_36 : AO1
      port map(A => XOR2_3_Y, B => AO1_90_Y, C => AND2_11_Y, Y => 
        AO1_36_Y);
    AND2_81 : AND2
      port map(A => AND2_54_Y, B => AND2_59_Y, Y => AND2_81_Y);
    AO1_33 : AO1
      port map(A => AND2_107_Y, B => AO1_86_Y, C => AO1_17_Y, 
        Y => AO1_33_Y);
    AO1_44 : AO1
      port map(A => AND2_29_Y, B => AO1_72_Y, C => AO1_68_Y, Y => 
        AO1_44_Y);
    XOR2_12 : XOR2
      port map(A => DataA(28), B => DataBXnor2_28_net, Y => 
        XOR2_12_Y);
    AND2_48 : AND2
      port map(A => AND2_119_Y, B => AND2_89_Y, Y => AND2_48_Y);
    AND2_45 : AND2
      port map(A => DataA(7), B => DataBXnor2_7_net, Y => 
        AND2_45_Y);
    AO1_75 : AO1
      port map(A => XOR2_17_Y, B => AND2_105_Y, C => AND2_45_Y, 
        Y => AO1_75_Y);
    AO1_19 : AO1
      port map(A => AND2_0_Y, B => AO1_29_Y, C => AO1_6_Y, Y => 
        AO1_19_Y);
    XNOR2_30_inst : XNOR2
      port map(A => DataB(30), B => AddsubAux_29_net, Y => 
        DataBXnor2_30_net);
    XOR2_26 : XOR2
      port map(A => DataA(9), B => DataBXnor2_9_net, Y => 
        XOR2_26_Y);
    XNOR2_10_inst : XNOR2
      port map(A => DataB(10), B => AddsubAux_6_net, Y => 
        DataBXnor2_10_net);
    AND2_59 : AND2
      port map(A => AND2_30_Y, B => AND2_95_Y, Y => AND2_59_Y);
    XOR2_Sum_13_inst : XOR2
      port map(A => XOR2_8_Y, B => AO1_62_Y, Y => Sum(13));
    AND2_4 : AND2
      port map(A => AND2_108_Y, B => AND2_43_Y, Y => AND2_4_Y);
    AO1_39 : AO1
      port map(A => XOR2_63_Y, B => AND2_94_Y, C => AND2_64_Y, 
        Y => AO1_39_Y);
    AND2_40 : AND2
      port map(A => XOR2_34_Y, B => XOR2_17_Y, Y => AND2_40_Y);
    AND2_88 : AND2
      port map(A => XOR2_15_Y, B => XOR2_37_Y, Y => AND2_88_Y);
    AND2_85 : AND2
      port map(A => AND2_104_Y, B => AND2_41_Y, Y => AND2_85_Y);
    XOR2_Sum_9_inst : XOR2
      port map(A => XOR2_26_Y, B => AO1_38_Y, Y => Sum(9));
    AO1_85 : AO1
      port map(A => AND2_110_Y, B => AO1_48_Y, C => AO1_56_Y, 
        Y => AO1_85_Y);
    XOR2_50 : XOR2
      port map(A => DataA(10), B => DataBXnor2_10_net, Y => 
        XOR2_50_Y);
    AND2_94 : AND2
      port map(A => DataA(10), B => DataBXnor2_10_net, Y => 
        AND2_94_Y);
    AND2_42 : AND2
      port map(A => AND2_13_Y, B => AND2_46_Y, Y => AND2_42_Y);
    XNOR2_19_inst : XNOR2
      port map(A => DataB(19), B => AddsubAux_18_net, Y => 
        DataBXnor2_19_net);
    AND2_118 : AND2
      port map(A => DataA(31), B => DataBXnor2_31_net, Y => 
        AND2_118_Y);
    XNOR2_16_inst : XNOR2
      port map(A => DataB(16), B => AddsubAux_12_net, Y => 
        DataBXnor2_16_net);
    AO1_5 : AO1
      port map(A => XOR2_28_Y, B => AO1_71_Y, C => AND2_49_Y, 
        Y => AO1_5_Y);
    AND2_67 : AND2
      port map(A => AND2_8_Y, B => AND2_93_Y, Y => AND2_67_Y);
    XOR2_39 : XOR2
      port map(A => DataA(23), B => DataBXnor2_23_net, Y => 
        XOR2_39_Y);
    AND2_8 : AND2
      port map(A => AND2_23_Y, B => AND2_29_Y, Y => AND2_8_Y);
    AND2_80 : AND2
      port map(A => DataA(3), B => DataBXnor2_3_net, Y => 
        AND2_80_Y);
    XOR2_3 : XOR2
      port map(A => DataA(30), B => DataBXnor2_30_net, Y => 
        XOR2_3_Y);
    XOR2_54 : XOR2
      port map(A => DataA(29), B => DataBXnor2_29_net, Y => 
        XOR2_54_Y);
    AO1_60 : AO1
      port map(A => XOR2_42_Y, B => AND2_36_Y, C => AND2_73_Y, 
        Y => AO1_60_Y);
    XOR2_51 : XOR2
      port map(A => DataA(12), B => DataBXnor2_12_net, Y => 
        XOR2_51_Y);
    AO1_27 : AO1
      port map(A => XOR2_18_Y, B => AND2_11_Y, C => AND2_118_Y, 
        Y => AO1_27_Y);
    AND2_82 : AND2
      port map(A => AND2_112_Y, B => AND2_29_Y, Y => AND2_82_Y);
    XNOR2_7_inst : XNOR2
      port map(A => DataB(7), B => AddsubAux_6_net, Y => 
        DataBXnor2_7_net);
    AND2_56 : AND2
      port map(A => DataA(12), B => DataBXnor2_12_net, Y => 
        AND2_56_Y);
    AO1_58 : AO1
      port map(A => AND2_60_Y, B => AO1_13_Y, C => AO1_49_Y, Y => 
        AO1_58_Y);
    XOR2_17 : XOR2
      port map(A => DataA(7), B => DataBXnor2_7_net, Y => 
        XOR2_17_Y);
    AND2_53 : AND2
      port map(A => XOR2_51_Y, B => XOR2_41_Y, Y => AND2_53_Y);
    AO1_66 : AO1
      port map(A => AND2_30_Y, B => AO1_33_Y, C => AO1_16_Y, Y => 
        AO1_66_Y);
    AND2_99 : AND2
      port map(A => AND2_16_Y, B => AND2_12_Y, Y => AND2_99_Y);
    XOR2_28 : XOR2
      port map(A => DataA(24), B => DataBXnor2_24_net, Y => 
        XOR2_28_Y);
    AO1_74 : AO1
      port map(A => XOR2_23_Y, B => AND2_97_Y, C => AND2_79_Y, 
        Y => AO1_74_Y);
    AO1_63 : AO1
      port map(A => AND2_24_Y, B => AO1_78_Y, C => AO1_5_Y, Y => 
        AO1_63_Y);
    XOR2_Sum_19_inst : XOR2
      port map(A => XOR2_48_Y, B => AO1_54_Y, Y => Sum(19));
    XOR2_Sum_26_inst : XOR2
      port map(A => XOR2_5_Y, B => AO1_24_Y, Y => Sum(26));
    XOR2_15 : XOR2
      port map(A => DataA(0), B => DataBXnor2_0_net, Y => 
        XOR2_15_Y);
    AO1_17 : AO1
      port map(A => XOR2_21_Y, B => AND2_62_Y, C => AND2_114_Y, 
        Y => AO1_17_Y);
    AO1_90 : AO1
      port map(A => XOR2_54_Y, B => AND2_75_Y, C => AND2_123_Y, 
        Y => AO1_90_Y);
    AND2_108 : AND2
      port map(A => AND2_13_Y, B => AND2_46_Y, Y => AND2_108_Y);
    XNOR2_5_inst : XNOR2
      port map(A => DataB(5), B => AddsubAux_0_net, Y => 
        DataBXnor2_5_net);
    XOR2_Sum_3_inst : XOR2
      port map(A => XOR2_58_Y, B => AO1_80_Y, Y => Sum(3));
    AO1_84 : AO1
      port map(A => AND2_29_Y, B => AO1_31_Y, C => AO1_68_Y, Y => 
        AO1_84_Y);
    XOR2_Sum_10_inst : XOR2
      port map(A => XOR2_27_Y, B => AO1_87_Y, Y => Sum(10));
    AND2_21 : AND2
      port map(A => DataA(14), B => DataBXnor2_14_net, Y => 
        AND2_21_Y);
    AO1_37 : AO1
      port map(A => AND2_20_Y, B => AO1_78_Y, C => AO1_71_Y, Y => 
        AO1_37_Y);
    AO1_69 : AO1
      port map(A => AND2_65_Y, B => AO1_70_Y, C => AO1_2_Y, Y => 
        AO1_69_Y);
    XOR2_0 : XOR2
      port map(A => DataA(1), B => DataBXnor2_1_net, Y => 
        XOR2_0_Y);
    AND2_96 : AND2
      port map(A => XOR2_29_Y, B => XOR2_23_Y, Y => AND2_96_Y);
    AO1_4 : AO1
      port map(A => AND2_29_Y, B => AO1_31_Y, C => AO1_68_Y, Y => 
        AO1_4_Y);
    AND2_37 : AND2
      port map(A => AND2_112_Y, B => XOR2_29_Y, Y => AND2_37_Y);
    AND2_93 : AND2
      port map(A => AND2_126_Y, B => XOR2_50_Y, Y => AND2_93_Y);
    XOR2_42 : XOR2
      port map(A => DataA(9), B => DataBXnor2_9_net, Y => 
        XOR2_42_Y);
    AO1_21 : AO1
      port map(A => AND2_18_Y, B => AO1_13_Y, C => AO1_77_Y, Y => 
        AO1_21_Y);
    AND2_114 : AND2
      port map(A => DataA(19), B => DataBXnor2_19_net, Y => 
        AND2_114_Y);
    XOR2_36 : XOR2
      port map(A => DataA(27), B => DataBXnor2_27_net, Y => 
        XOR2_36_Y);
    XNOR2_20_inst : XNOR2
      port map(A => DataB(20), B => AddsubAux_18_net, Y => 
        DataBXnor2_20_net);
    AND2_28 : AND2
      port map(A => AND2_108_Y, B => AND2_111_Y, Y => AND2_28_Y);
    AND2_25 : AND2
      port map(A => XOR2_56_Y, B => XOR2_59_Y, Y => AND2_25_Y);
    AO1_48 : AO1
      port map(A => XOR2_30_Y, B => AND2_49_Y, C => AND2_115_Y, 
        Y => AO1_48_Y);
    XOR2_Sum_12_inst : XOR2
      port map(A => XOR2_33_Y, B => AO1_14_Y, Y => Sum(12));
    XOR2_Sum_24_inst : XOR2
      port map(A => XOR2_9_Y, B => AO1_37_Y, Y => Sum(24));
    XOR2_Sum_28_inst : XOR2
      port map(A => XOR2_62_Y, B => AO1_69_Y, Y => Sum(28));
end DEF_ARCH;

library ieee;
use ieee.std_logic_1164.all;
library proasic3;

entity mux31_apa3 is 
    port( Data0_port : in std_logic_vector(31 downto 0); 
        Data1_port : in std_logic_vector(31 downto 0); Data2_port : 
        in std_logic_vector(31 downto 0);Sel0, Sel1 : in 
        std_logic; Result : out std_logic_vector(31 downto 0)) ;
end mux31_apa3;


architecture DEF_ARCH of  mux31_apa3 is

    component MX2
        port(A, B, S : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    component BUFF
        port(A : in std_logic := 'U'; Y : out std_logic) ;
    end component;

    signal SelAux_0_0_net, SelAux_0_6_net, SelAux_0_12_net, 
        SelAux_0_17_net, SelAux_0_22_net, SelAux_0_27_net, 
        SelAux_1_0_net, SelAux_1_6_net, SelAux_1_12_net, 
        SelAux_1_17_net, SelAux_1_22_net, SelAux_1_27_net, 
        MX2_11_Y, MX2_14_Y, MX2_3_Y, MX2_22_Y, MX2_26_Y, MX2_24_Y, 
        MX2_7_Y, MX2_13_Y, MX2_20_Y, MX2_16_Y, MX2_4_Y, MX2_5_Y, 
        MX2_15_Y, MX2_17_Y, MX2_28_Y, MX2_1_Y, MX2_8_Y, MX2_9_Y, 
        MX2_19_Y, MX2_27_Y, MX2_30_Y, MX2_0_Y, MX2_2_Y, MX2_12_Y, 
        MX2_10_Y, MX2_18_Y, MX2_21_Y, MX2_25_Y, MX2_31_Y, MX2_6_Y, 
        MX2_29_Y, MX2_23_Y : std_logic ;
    begin   

    MX2_18 : MX2
      port map(A => Data0_port(9), B => Data1_port(9), S => 
        SelAux_0_6_net, Y => MX2_18_Y);
    BUFF_SelAux_1_12_inst : BUFF
      port map(A => Sel1, Y => SelAux_1_12_net);
    MX2_Result_1_inst : MX2
      port map(A => MX2_19_Y, B => Data2_port(1), S => 
        SelAux_1_0_net, Y => Result(1));
    MX2_12 : MX2
      port map(A => Data0_port(4), B => Data1_port(4), S => 
        SelAux_0_0_net, Y => MX2_12_Y);
    MX2_Result_18_inst : MX2
      port map(A => MX2_6_Y, B => Data2_port(18), S => 
        SelAux_1_17_net, Y => Result(18));
    MX2_10 : MX2
      port map(A => Data0_port(22), B => Data1_port(22), S => 
        SelAux_0_22_net, Y => MX2_10_Y);
    BUFF_SelAux_0_12_inst : BUFF
      port map(A => Sel0, Y => SelAux_0_12_net);
    MX2_Result_31_inst : MX2
      port map(A => MX2_17_Y, B => Data2_port(31), S => 
        SelAux_1_27_net, Y => Result(31));
    MX2_7 : MX2
      port map(A => Data0_port(7), B => Data1_port(7), S => 
        SelAux_0_6_net, Y => MX2_7_Y);
    MX2_15 : MX2
      port map(A => Data0_port(25), B => Data1_port(25), S => 
        SelAux_0_22_net, Y => MX2_15_Y);
    MX2_Result_8_inst : MX2
      port map(A => MX2_8_Y, B => Data2_port(8), S => 
        SelAux_1_6_net, Y => Result(8));
    MX2_Result_24_inst : MX2
      port map(A => MX2_13_Y, B => Data2_port(24), S => 
        SelAux_1_22_net, Y => Result(24));
    MX2_26 : MX2
      port map(A => Data0_port(12), B => Data1_port(12), S => 
        SelAux_0_12_net, Y => MX2_26_Y);
    MX2_Result_2_inst : MX2
      port map(A => MX2_31_Y, B => Data2_port(2), S => 
        SelAux_1_0_net, Y => Result(2));
    MX2_Result_5_inst : MX2
      port map(A => MX2_22_Y, B => Data2_port(5), S => 
        SelAux_1_0_net, Y => Result(5));
    MX2_Result_27_inst : MX2
      port map(A => MX2_23_Y, B => Data2_port(27), S => 
        SelAux_1_27_net, Y => Result(27));
    MX2_Result_0_inst : MX2
      port map(A => MX2_16_Y, B => Data2_port(0), S => 
        SelAux_1_0_net, Y => Result(0));
    MX2_Result_29_inst : MX2
      port map(A => MX2_2_Y, B => Data2_port(29), S => 
        SelAux_1_27_net, Y => Result(29));
    MX2_Result_14_inst : MX2
      port map(A => MX2_28_Y, B => Data2_port(14), S => 
        SelAux_1_12_net, Y => Result(14));
    MX2_16 : MX2
      port map(A => Data0_port(0), B => Data1_port(0), S => 
        SelAux_0_0_net, Y => MX2_16_Y);
    MX2_2 : MX2
      port map(A => Data0_port(29), B => Data1_port(29), S => 
        SelAux_0_27_net, Y => MX2_2_Y);
    MX2_Result_17_inst : MX2
      port map(A => MX2_4_Y, B => Data2_port(17), S => 
        SelAux_1_17_net, Y => Result(17));
    MX2_Result_19_inst : MX2
      port map(A => MX2_14_Y, B => Data2_port(19), S => 
        SelAux_1_17_net, Y => Result(19));
    MX2_1 : MX2
      port map(A => Data0_port(10), B => Data1_port(10), S => 
        SelAux_0_6_net, Y => MX2_1_Y);
    MX2_Result_30_inst : MX2
      port map(A => MX2_29_Y, B => Data2_port(30), S => 
        SelAux_1_27_net, Y => Result(30));
    MX2_Result_3_inst : MX2
      port map(A => MX2_21_Y, B => Data2_port(3), S => 
        SelAux_1_0_net, Y => Result(3));
    MX2_0 : MX2
      port map(A => Data0_port(23), B => Data1_port(23), S => 
        SelAux_0_22_net, Y => MX2_0_Y);
    BUFF_SelAux_0_27_inst : BUFF
      port map(A => Sel0, Y => SelAux_0_27_net);
    BUFF_SelAux_1_27_inst : BUFF
      port map(A => Sel1, Y => SelAux_1_27_net);
    MX2_Result_26_inst : MX2
      port map(A => MX2_24_Y, B => Data2_port(26), S => 
        SelAux_1_22_net, Y => Result(26));
    BUFF_SelAux_1_6_inst : BUFF
      port map(A => Sel1, Y => SelAux_1_6_net);
    MX2_24 : MX2
      port map(A => Data0_port(26), B => Data1_port(26), S => 
        SelAux_0_22_net, Y => MX2_24_Y);
    MX2_Result_23_inst : MX2
      port map(A => MX2_0_Y, B => Data2_port(23), S => 
        SelAux_1_22_net, Y => Result(23));
    BUFF_SelAux_1_22_inst : BUFF
      port map(A => Sel1, Y => SelAux_1_22_net);
    BUFF_SelAux_0_22_inst : BUFF
      port map(A => Sel0, Y => SelAux_0_22_net);
    MX2_Result_16_inst : MX2
      port map(A => MX2_5_Y, B => Data2_port(16), S => 
        SelAux_1_12_net, Y => Result(16));
    MX2_5 : MX2
      port map(A => Data0_port(16), B => Data1_port(16), S => 
        SelAux_0_12_net, Y => MX2_5_Y);
    MX2_14 : MX2
      port map(A => Data0_port(19), B => Data1_port(19), S => 
        SelAux_0_17_net, Y => MX2_14_Y);
    BUFF_SelAux_0_0_inst : BUFF
      port map(A => Sel0, Y => SelAux_0_0_net);
    MX2_Result_13_inst : MX2
      port map(A => MX2_11_Y, B => Data2_port(13), S => 
        SelAux_1_12_net, Y => Result(13));
    MX2_9 : MX2
      port map(A => Data0_port(21), B => Data1_port(21), S => 
        SelAux_0_17_net, Y => MX2_9_Y);
    MX2_4 : MX2
      port map(A => Data0_port(17), B => Data1_port(17), S => 
        SelAux_0_17_net, Y => MX2_4_Y);
    MX2_Result_22_inst : MX2
      port map(A => MX2_10_Y, B => Data2_port(22), S => 
        SelAux_1_22_net, Y => Result(22));
    MX2_Result_21_inst : MX2
      port map(A => MX2_9_Y, B => Data2_port(21), S => 
        SelAux_1_17_net, Y => Result(21));
    MX2_31 : MX2
      port map(A => Data0_port(2), B => Data1_port(2), S => 
        SelAux_0_0_net, Y => MX2_31_Y);
    MX2_Result_12_inst : MX2
      port map(A => MX2_26_Y, B => Data2_port(12), S => 
        SelAux_1_12_net, Y => Result(12));
    MX2_Result_25_inst : MX2
      port map(A => MX2_15_Y, B => Data2_port(25), S => 
        SelAux_1_22_net, Y => Result(25));
    MX2_Result_11_inst : MX2
      port map(A => MX2_25_Y, B => Data2_port(11), S => 
        SelAux_1_6_net, Y => Result(11));
    BUFF_SelAux_0_6_inst : BUFF
      port map(A => Sel0, Y => SelAux_0_6_net);
    BUFF_SelAux_1_0_inst : BUFF
      port map(A => Sel1, Y => SelAux_1_0_net);
    MX2_30 : MX2
      port map(A => Data0_port(15), B => Data1_port(15), S => 
        SelAux_0_12_net, Y => MX2_30_Y);
    MX2_Result_15_inst : MX2
      port map(A => MX2_30_Y, B => Data2_port(15), S => 
        SelAux_1_12_net, Y => Result(15));
    MX2_6 : MX2
      port map(A => Data0_port(18), B => Data1_port(18), S => 
        SelAux_0_17_net, Y => MX2_6_Y);
    MX2_23 : MX2
      port map(A => Data0_port(27), B => Data1_port(27), S => 
        SelAux_0_27_net, Y => MX2_23_Y);
    MX2_27 : MX2
      port map(A => Data0_port(28), B => Data1_port(28), S => 
        SelAux_0_27_net, Y => MX2_27_Y);
    MX2_Result_9_inst : MX2
      port map(A => MX2_18_Y, B => Data2_port(9), S => 
        SelAux_1_6_net, Y => Result(9));
    MX2_Result_7_inst : MX2
      port map(A => MX2_7_Y, B => Data2_port(7), S => 
        SelAux_1_6_net, Y => Result(7));
    MX2_13 : MX2
      port map(A => Data0_port(24), B => Data1_port(24), S => 
        SelAux_0_22_net, Y => MX2_13_Y);
    MX2_29 : MX2
      port map(A => Data0_port(30), B => Data1_port(30), S => 
        SelAux_0_27_net, Y => MX2_29_Y);
    MX2_17 : MX2
      port map(A => Data0_port(31), B => Data1_port(31), S => 
        SelAux_0_27_net, Y => MX2_17_Y);
    MX2_Result_20_inst : MX2
      port map(A => MX2_20_Y, B => Data2_port(20), S => 
        SelAux_1_17_net, Y => Result(20));
    MX2_21 : MX2
      port map(A => Data0_port(3), B => Data1_port(3), S => 
        SelAux_0_0_net, Y => MX2_21_Y);
    MX2_Result_6_inst : MX2
      port map(A => MX2_3_Y, B => Data2_port(6), S => 
        SelAux_1_6_net, Y => Result(6));
    MX2_28 : MX2
      port map(A => Data0_port(14), B => Data1_port(14), S => 
        SelAux_0_12_net, Y => MX2_28_Y);
    BUFF_SelAux_1_17_inst : BUFF
      port map(A => Sel1, Y => SelAux_1_17_net);
    MX2_22 : MX2
      port map(A => Data0_port(5), B => Data1_port(5), S => 
        SelAux_0_0_net, Y => MX2_22_Y);
    MX2_20 : MX2
      port map(A => Data0_port(20), B => Data1_port(20), S => 
        SelAux_0_17_net, Y => MX2_20_Y);
    MX2_Result_28_inst : MX2
      port map(A => MX2_27_Y, B => Data2_port(28), S => 
        SelAux_1_27_net, Y => Result(28));
    BUFF_SelAux_0_17_inst : BUFF
      port map(A => Sel0, Y => SelAux_0_17_net);
    MX2_19 : MX2
      port map(A => Data0_port(1), B => Data1_port(1), S => 
        SelAux_0_0_net, Y => MX2_19_Y);
    MX2_3 : MX2
      port map(A => Data0_port(6), B => Data1_port(6), S => 
        SelAux_0_6_net, Y => MX2_3_Y);
    MX2_Result_10_inst : MX2
      port map(A => MX2_1_Y, B => Data2_port(10), S => 
        SelAux_1_6_net, Y => Result(10));
    MX2_8 : MX2
      port map(A => Data0_port(8), B => Data1_port(8), S => 
        SelAux_0_6_net, Y => MX2_8_Y);
    MX2_Result_4_inst : MX2
      port map(A => MX2_12_Y, B => Data2_port(4), S => 
        SelAux_1_0_net, Y => Result(4));
    MX2_25 : MX2
      port map(A => Data0_port(11), B => Data1_port(11), S => 
        SelAux_0_6_net, Y => MX2_25_Y);
    MX2_11 : MX2
      port map(A => Data0_port(13), B => Data1_port(13), S => 
        SelAux_0_12_net, Y => MX2_11_Y);
end DEF_ARCH;

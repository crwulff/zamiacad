package org.zamia.verilog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.TestCase;

import org.zamia.SourceFile;
import org.zamia.verilog.lexer.Lexer;
import org.zamia.verilog.lexer.LexerException;
import org.zamia.verilog.node.Start;
import org.zamia.verilog.parser.Parser;
import org.zamia.verilog.parser.ParserException;
import org.zamia.verilog.pre.IPreprocessor;
import org.zamia.verilog.pre.VerilogPreprocessor;

/**
 * 
 * @author Guenter Bartsch
 *
 */

public class VerilogParserTest extends TestCase {

	public VerilogParserTest() {

	}

	private Start parseFile(String aFileName) throws ParserException, LexerException, IOException {

		SourceFile sf = new SourceFile(new File(aFileName));

		IPreprocessor pre = new VerilogPreprocessor(sf, new BufferedReader(new FileReader(aFileName)), false);

		//Parser p = new Parser(new Lexer(new PushbackReader(new FileReader(aFileName), 1024)));
		Parser p = new Parser(new Lexer(pre));

		return p.parse();
	}

	private void compile(String aFileName) throws ParserException, LexerException, IOException {

		Start tree = parseFile(aFileName);

		System.out.printf("%s: Got tree: %s\n", aFileName, tree);

		tree.apply(new IGGenerator());

	}

	public void testLeonVerilog() throws ParserException, LexerException, IOException {
		compile("test/verilog/misc/ahb2wb.v");
		compile("test/verilog/misc/ata_device_oc.v");
//		compile("test/verilog/misc/i2c_slave_model.v");
//		compile("test/verilog/misc/postponer.v");
//		compile("test/verilog/misc/simple_spi_top.v");
//		compile("test/verilog/misc/spi_slave_model.v");
	}

	public void testDDR2() throws ParserException, LexerException, IOException {
		compile("test/verilog/misc/ddr2.v");
	}

	public void testPositions() throws ParserException, LexerException, IOException {
		compile("test/verilog/misc/foo.v");
	}

	public void testPreprocessor() throws Exception {

		String fileName = "test/verilog/or1200-rel1/or1200_cpu.v";

		SourceFile sf = new SourceFile(new File(fileName));

		VerilogPreprocessor pre = new VerilogPreprocessor(sf, new BufferedReader(new FileReader(fileName)), false);

		while (true) {

			int ic = pre.read();

			if (ic < 0) {
				break;
			} else {
				char c = (char) ic;

				System.out.printf("%c", c);
			}

		}

	}

	public void testLVALUE() throws ParserException, LexerException, IOException {
		compile("test/verilog/misc/lvalue.v");
	}

	public void testNAND() throws ParserException, LexerException, IOException {
		compile("test/verilog/nand/nand.v");
	}

	public void testNANDSim() throws ParserException, LexerException, IOException {
		compile("test/verilog/nand/nandsim.v");
	}

	public void testCounterHier() throws ParserException, LexerException, IOException {
		compile("test/verilog/counter_hier/m16.v");
		compile("test/verilog/counter_hier/dedgeff.v");
		compile("test/verilog/counter_hier/m555.v");
		compile("test/verilog/counter_hier/board.v");
	}

	public void testOR1200() throws ParserException, LexerException, IOException {
		compile("test/verilog/or1200-rel1/or1200_cpu.v");
		compile("test/verilog/or1200-rel1/or1200_alu.v");
		compile("test/verilog/or1200-rel1/or1200_amultp2_32x32.v");
		compile("test/verilog/or1200-rel1/or1200_cfgr.v");
		compile("test/verilog/or1200-rel1/or1200_ctrl.v");
		compile("test/verilog/or1200-rel1/or1200_dc_fsm.v");
		compile("test/verilog/or1200-rel1/or1200_dc_ram.v");
		compile("test/verilog/or1200-rel1/or1200_dc_tag.v");
		compile("test/verilog/or1200-rel1/or1200_dc_top.v");
		compile("test/verilog/or1200-rel1/or1200_dmmu_tlb.v");
		compile("test/verilog/or1200-rel1/or1200_dmmu_top.v");
		compile("test/verilog/or1200-rel1/or1200_dpram_256x32.v");
		compile("test/verilog/or1200-rel1/or1200_dpram_32x32.v");
		compile("test/verilog/or1200-rel1/or1200_du.v");
		compile("test/verilog/or1200-rel1/or1200_except.v");
		compile("test/verilog/or1200-rel1/or1200_freeze.v");
		compile("test/verilog/or1200-rel1/or1200_genpc.v");
		compile("test/verilog/or1200-rel1/or1200_gmultp2_32x32.v");
		compile("test/verilog/or1200-rel1/or1200_ic_fsm.v");
		compile("test/verilog/or1200-rel1/or1200_ic_ram.v");
		compile("test/verilog/or1200-rel1/or1200_ic_tag.v");
		compile("test/verilog/or1200-rel1/or1200_ic_top.v");
		compile("test/verilog/or1200-rel1/or1200_if.v");
		compile("test/verilog/or1200-rel1/or1200_immu_tlb.v");
		compile("test/verilog/or1200-rel1/or1200_immu_top.v");
		compile("test/verilog/or1200-rel1/or1200_iwb_biu.v");
		compile("test/verilog/or1200-rel1/or1200_lsu.v");
		compile("test/verilog/or1200-rel1/or1200_mem2reg.v");
		compile("test/verilog/or1200-rel1/or1200_mult_mac.v");
		compile("test/verilog/or1200-rel1/or1200_operandmuxes.v");
		compile("test/verilog/or1200-rel1/or1200_pic.v");
		compile("test/verilog/or1200-rel1/or1200_pm.v");
		compile("test/verilog/or1200-rel1/or1200_qmem_top.v");
		compile("test/verilog/or1200-rel1/or1200_reg2mem.v");
		compile("test/verilog/or1200-rel1/or1200_rfram_generic.v");
		compile("test/verilog/or1200-rel1/or1200_rf.v");
		compile("test/verilog/or1200-rel1/or1200_sb_fifo.v");
		compile("test/verilog/or1200-rel1/or1200_sb.v");
		compile("test/verilog/or1200-rel1/or1200_spram_1024x32_bw.v");
		compile("test/verilog/or1200-rel1/or1200_spram_1024x32.v");
		compile("test/verilog/or1200-rel1/or1200_spram_1024x8.v");
		compile("test/verilog/or1200-rel1/or1200_spram_128x32.v");
		compile("test/verilog/or1200-rel1/or1200_spram_2048x32_bw.v");
		compile("test/verilog/or1200-rel1/or1200_spram_2048x32.v");
		compile("test/verilog/or1200-rel1/or1200_spram_2048x8.v");
		compile("test/verilog/or1200-rel1/or1200_spram_256x21.v");
		compile("test/verilog/or1200-rel1/or1200_spram_32x24.v");
		compile("test/verilog/or1200-rel1/or1200_spram_512x20.v");
		compile("test/verilog/or1200-rel1/or1200_spram_64x14.v");
		compile("test/verilog/or1200-rel1/or1200_spram_64x22.v");
		compile("test/verilog/or1200-rel1/or1200_spram_64x24.v");
		compile("test/verilog/or1200-rel1/or1200_sprs.v");
		compile("test/verilog/or1200-rel1/or1200_top.v");
		compile("test/verilog/or1200-rel1/or1200_tpram_32x32.v");
		compile("test/verilog/or1200-rel1/or1200_tt.v");
		compile("test/verilog/or1200-rel1/or1200_wb_biu.v");
		compile("test/verilog/or1200-rel1/or1200_wbmux.v");
		compile("test/verilog/or1200-rel1/or1200_xcv_ram32x8d.v");
	}
}
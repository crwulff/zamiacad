package org.zamia.verilog;

import org.junit.Test;
import org.zamia.SourceFile;
import org.zamia.verilog.lexer.Lexer;
import org.zamia.verilog.lexer.LexerException;
import org.zamia.verilog.node.Start;
import org.zamia.verilog.parser.Parser;
import org.zamia.verilog.parser.ParserException;
import org.zamia.verilog.pre.IPreprocessor;
import org.zamia.verilog.pre.VerilogPreprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Guenter Bartsch
 */

public class VerilogParserTest {

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

	@Test
	public void testLeonVerilog() throws ParserException, LexerException, IOException {
		compile("examples/verilog/misc/ahb2wb.v");
		compile("examples/verilog/misc/ata_device_oc.v");
		compile("examples/verilog/misc/i2c_slave_model.v");
		compile("examples/verilog/misc/postponer.v");
		compile("examples/verilog/misc/simple_spi_top.v");
		compile("examples/verilog/misc/spi_slave_model.v");
	}

	@Test
	public void testDDR2() throws ParserException, LexerException, IOException {
		compile("examples/verilog/misc/ddr2.v");
	}

	@Test
	public void testPositions() throws ParserException, LexerException, IOException {
		compile("examples/verilog/misc/foo.v");
	}

	@Test
	public void testPreprocessor() throws Exception {

		String fileName = "examples/verilog/or1200-rel1/or1200_cpu.v";

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

	@Test
	public void testLVALUE() throws ParserException, LexerException, IOException {
		compile("examples/verilog/misc/lvalue.v");
	}

	@Test
	public void testNAND() throws ParserException, LexerException, IOException {
		compile("examples/verilog/nand/nand.v");
	}

	@Test
	public void testNANDSim() throws ParserException, LexerException, IOException {
		compile("examples/verilog/nand/nandsim.v");
	}

	@Test
	public void testCounterHier() throws ParserException, LexerException, IOException {
		compile("examples/verilog/counter_hier/m16.v");
		compile("examples/verilog/counter_hier/dedgeff.v");
		compile("examples/verilog/counter_hier/m555.v");
		compile("examples/verilog/counter_hier/board.v");
	}

	@Test
	public void testOR1200() throws ParserException, LexerException, IOException {
		compile("examples/verilog/or1200-rel1/or1200_cpu.v");
		compile("examples/verilog/or1200-rel1/or1200_alu.v");
		compile("examples/verilog/or1200-rel1/or1200_amultp2_32x32.v");
		compile("examples/verilog/or1200-rel1/or1200_cfgr.v");
		compile("examples/verilog/or1200-rel1/or1200_ctrl.v");
		compile("examples/verilog/or1200-rel1/or1200_dc_fsm.v");
		compile("examples/verilog/or1200-rel1/or1200_dc_ram.v");
		compile("examples/verilog/or1200-rel1/or1200_dc_tag.v");
		compile("examples/verilog/or1200-rel1/or1200_dc_top.v");
		compile("examples/verilog/or1200-rel1/or1200_dmmu_tlb.v");
		compile("examples/verilog/or1200-rel1/or1200_dmmu_top.v");
		compile("examples/verilog/or1200-rel1/or1200_dpram_256x32.v");
		compile("examples/verilog/or1200-rel1/or1200_dpram_32x32.v");
		compile("examples/verilog/or1200-rel1/or1200_du.v");
		compile("examples/verilog/or1200-rel1/or1200_except.v");
		compile("examples/verilog/or1200-rel1/or1200_freeze.v");
		compile("examples/verilog/or1200-rel1/or1200_genpc.v");
		compile("examples/verilog/or1200-rel1/or1200_gmultp2_32x32.v");
		compile("examples/verilog/or1200-rel1/or1200_ic_fsm.v");
		compile("examples/verilog/or1200-rel1/or1200_ic_ram.v");
		compile("examples/verilog/or1200-rel1/or1200_ic_tag.v");
		compile("examples/verilog/or1200-rel1/or1200_ic_top.v");
		compile("examples/verilog/or1200-rel1/or1200_if.v");
		compile("examples/verilog/or1200-rel1/or1200_immu_tlb.v");
		compile("examples/verilog/or1200-rel1/or1200_immu_top.v");
		compile("examples/verilog/or1200-rel1/or1200_iwb_biu.v");
		compile("examples/verilog/or1200-rel1/or1200_lsu.v");
		compile("examples/verilog/or1200-rel1/or1200_mem2reg.v");
		compile("examples/verilog/or1200-rel1/or1200_mult_mac.v");
		compile("examples/verilog/or1200-rel1/or1200_operandmuxes.v");
		compile("examples/verilog/or1200-rel1/or1200_pic.v");
		compile("examples/verilog/or1200-rel1/or1200_pm.v");
		compile("examples/verilog/or1200-rel1/or1200_qmem_top.v");
		compile("examples/verilog/or1200-rel1/or1200_reg2mem.v");
		compile("examples/verilog/or1200-rel1/or1200_rfram_generic.v");
		compile("examples/verilog/or1200-rel1/or1200_rf.v");
		compile("examples/verilog/or1200-rel1/or1200_sb_fifo.v");
		compile("examples/verilog/or1200-rel1/or1200_sb.v");
		compile("examples/verilog/or1200-rel1/or1200_spram_1024x32_bw.v");
		compile("examples/verilog/or1200-rel1/or1200_spram_1024x32.v");
		compile("examples/verilog/or1200-rel1/or1200_spram_1024x8.v");
		compile("examples/verilog/or1200-rel1/or1200_spram_128x32.v");
		compile("examples/verilog/or1200-rel1/or1200_spram_2048x32_bw.v");
		compile("examples/verilog/or1200-rel1/or1200_spram_2048x32.v");
		compile("examples/verilog/or1200-rel1/or1200_spram_2048x8.v");
		compile("examples/verilog/or1200-rel1/or1200_spram_256x21.v");
		compile("examples/verilog/or1200-rel1/or1200_spram_32x24.v");
		compile("examples/verilog/or1200-rel1/or1200_spram_512x20.v");
		compile("examples/verilog/or1200-rel1/or1200_spram_64x14.v");
		compile("examples/verilog/or1200-rel1/or1200_spram_64x22.v");
		compile("examples/verilog/or1200-rel1/or1200_spram_64x24.v");
		compile("examples/verilog/or1200-rel1/or1200_sprs.v");
		compile("examples/verilog/or1200-rel1/or1200_top.v");
		compile("examples/verilog/or1200-rel1/or1200_tpram_32x32.v");
		compile("examples/verilog/or1200-rel1/or1200_tt.v");
		compile("examples/verilog/or1200-rel1/or1200_wb_biu.v");
		compile("examples/verilog/or1200-rel1/or1200_wbmux.v");
		compile("examples/verilog/or1200-rel1/or1200_xcv_ram32x8d.v");
	}
}
module ffNand;

  wire	q, qBar;
  reg	preset, clear;

  nand #1
    g1 (q, qBar, preset),
    g2 (qBar, q, clear);

endmodule


/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.zamia.verilog.node;

import org.zamia.verilog.analysis.*;
import org.zamia.SourceFile;

@SuppressWarnings("nls")
public final class TKEdge0x extends Token
{
    public TKEdge0x(int line, int pos, SourceFile sf)
    {
        super ("0x", line, pos, sf);
    }

    @Override
    public Object clone()
    {
      return new TKEdge0x(getLine(), getPos(), getSource());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTKEdge0x(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TKEdge0x text.");
    }
}

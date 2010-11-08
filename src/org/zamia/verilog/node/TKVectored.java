/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.zamia.verilog.node;

import org.zamia.verilog.analysis.*;
import org.zamia.SourceFile;

@SuppressWarnings("nls")
public final class TKVectored extends Token
{
    public TKVectored(int line, int pos, SourceFile sf)
    {
        super ("vectored", line, pos, sf);
    }

    @Override
    public Object clone()
    {
      return new TKVectored(getLine(), getPos(), getSource());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTKVectored(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TKVectored text.");
    }
}
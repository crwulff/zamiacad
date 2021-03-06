/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.zamia.verilog.node;

import org.zamia.verilog.analysis.*;
import org.zamia.SourceFile;

@SuppressWarnings("nls")
public final class TKDdtNature extends Token
{
    public TKDdtNature(int line, int pos, SourceFile sf)
    {
        super ("ddt_nature", line, pos, sf);
    }

    @Override
    public Object clone()
    {
      return new TKDdtNature(getLine(), getPos(), getSource());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTKDdtNature(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TKDdtNature text.");
    }
}

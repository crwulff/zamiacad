/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.zamia.verilog.node;

import org.zamia.verilog.analysis.*;
import org.zamia.SourceFile;

@SuppressWarnings("nls")
public final class TKExp extends Token
{
    public TKExp(int line, int pos, SourceFile sf)
    {
        super ("exp", line, pos, sf);
    }

    @Override
    public Object clone()
    {
      return new TKExp(getLine(), getPos(), getSource());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTKExp(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TKExp text.");
    }
}
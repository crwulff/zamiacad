/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.zamia.verilog.node;

import org.zamia.verilog.analysis.*;
import org.zamia.SourceFile;

@SuppressWarnings("nls")
public final class TKParameter extends Token
{
    public TKParameter(int line, int pos, SourceFile sf)
    {
        super ("parameter", line, pos, sf);
    }

    @Override
    public Object clone()
    {
      return new TKParameter(getLine(), getPos(), getSource());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTKParameter(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TKParameter text.");
    }
}
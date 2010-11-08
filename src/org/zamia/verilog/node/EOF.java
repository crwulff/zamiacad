/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.zamia.verilog.node;

import org.zamia.verilog.analysis.*;

import org.zamia.SourceFile;

@SuppressWarnings("nls")
public final class EOF extends Token
{
    public EOF(int line, int pos, SourceFile sf)
    {
        super("", line, pos, sf);
    }

    @Override
    public Object clone()
    {
        return new EOF(getLine(), getPos(), getSource());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseEOF(this);
    }
}
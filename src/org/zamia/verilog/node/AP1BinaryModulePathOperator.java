/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.zamia.verilog.node;

import org.zamia.verilog.analysis.*;

@SuppressWarnings("nls")
public final class AP1BinaryModulePathOperator extends PBinaryModulePathOperator
{
    private TTNe _tNe_;

    public AP1BinaryModulePathOperator()
    {
        // Constructor
    }

    public AP1BinaryModulePathOperator(
        @SuppressWarnings("hiding") TTNe _tNe_)
    {
        // Constructor
        setTNe(_tNe_);

    }

    @Override
    public Object clone()
    {
        return new AP1BinaryModulePathOperator(
            cloneNode(this._tNe_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAP1BinaryModulePathOperator(this);
    }

    public TTNe getTNe()
    {
        return this._tNe_;
    }

    public void setTNe(TTNe node)
    {
        if(this._tNe_ != null)
        {
            this._tNe_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._tNe_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._tNe_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._tNe_ == child)
        {
            this._tNe_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._tNe_ == oldChild)
        {
            setTNe((TTNe) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}

/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.zamia.verilog.node;

import org.zamia.verilog.analysis.*;

@SuppressWarnings("nls")
public final class ASimpleIdentifier extends PIdentifier
{
    private TSimpleIdentifier _simpleIdentifier_;

    public ASimpleIdentifier()
    {
        // Constructor
    }

    public ASimpleIdentifier(
        @SuppressWarnings("hiding") TSimpleIdentifier _simpleIdentifier_)
    {
        // Constructor
        setSimpleIdentifier(_simpleIdentifier_);

    }

    @Override
    public Object clone()
    {
        return new ASimpleIdentifier(
            cloneNode(this._simpleIdentifier_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseASimpleIdentifier(this);
    }

    public TSimpleIdentifier getSimpleIdentifier()
    {
        return this._simpleIdentifier_;
    }

    public void setSimpleIdentifier(TSimpleIdentifier node)
    {
        if(this._simpleIdentifier_ != null)
        {
            this._simpleIdentifier_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._simpleIdentifier_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._simpleIdentifier_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._simpleIdentifier_ == child)
        {
            this._simpleIdentifier_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._simpleIdentifier_ == oldChild)
        {
            setSimpleIdentifier((TSimpleIdentifier) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
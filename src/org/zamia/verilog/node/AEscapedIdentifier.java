/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.zamia.verilog.node;

import org.zamia.verilog.analysis.*;

@SuppressWarnings("nls")
public final class AEscapedIdentifier extends PIdentifier
{
    private TEscapedIdentifier _escapedIdentifier_;

    public AEscapedIdentifier()
    {
        // Constructor
    }

    public AEscapedIdentifier(
        @SuppressWarnings("hiding") TEscapedIdentifier _escapedIdentifier_)
    {
        // Constructor
        setEscapedIdentifier(_escapedIdentifier_);

    }

    @Override
    public Object clone()
    {
        return new AEscapedIdentifier(
            cloneNode(this._escapedIdentifier_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAEscapedIdentifier(this);
    }

    public TEscapedIdentifier getEscapedIdentifier()
    {
        return this._escapedIdentifier_;
    }

    public void setEscapedIdentifier(TEscapedIdentifier node)
    {
        if(this._escapedIdentifier_ != null)
        {
            this._escapedIdentifier_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._escapedIdentifier_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._escapedIdentifier_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._escapedIdentifier_ == child)
        {
            this._escapedIdentifier_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._escapedIdentifier_ == oldChild)
        {
            setEscapedIdentifier((TEscapedIdentifier) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
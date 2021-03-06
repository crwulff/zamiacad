/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.zamia.verilog.node;

import org.zamia.verilog.analysis.*;

@SuppressWarnings("nls")
public final class AP1PortDeclarations extends PPortDeclarations
{
    private PPortDeclarationFoo _portDeclarationFoo_;
    private TTComma _tComma_;
    private PPortDeclarations _portDeclarations_;

    public AP1PortDeclarations()
    {
        // Constructor
    }

    public AP1PortDeclarations(
        @SuppressWarnings("hiding") PPortDeclarationFoo _portDeclarationFoo_,
        @SuppressWarnings("hiding") TTComma _tComma_,
        @SuppressWarnings("hiding") PPortDeclarations _portDeclarations_)
    {
        // Constructor
        setPortDeclarationFoo(_portDeclarationFoo_);

        setTComma(_tComma_);

        setPortDeclarations(_portDeclarations_);

    }

    @Override
    public Object clone()
    {
        return new AP1PortDeclarations(
            cloneNode(this._portDeclarationFoo_),
            cloneNode(this._tComma_),
            cloneNode(this._portDeclarations_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAP1PortDeclarations(this);
    }

    public PPortDeclarationFoo getPortDeclarationFoo()
    {
        return this._portDeclarationFoo_;
    }

    public void setPortDeclarationFoo(PPortDeclarationFoo node)
    {
        if(this._portDeclarationFoo_ != null)
        {
            this._portDeclarationFoo_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._portDeclarationFoo_ = node;
    }

    public TTComma getTComma()
    {
        return this._tComma_;
    }

    public void setTComma(TTComma node)
    {
        if(this._tComma_ != null)
        {
            this._tComma_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._tComma_ = node;
    }

    public PPortDeclarations getPortDeclarations()
    {
        return this._portDeclarations_;
    }

    public void setPortDeclarations(PPortDeclarations node)
    {
        if(this._portDeclarations_ != null)
        {
            this._portDeclarations_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._portDeclarations_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._portDeclarationFoo_)
            + toString(this._tComma_)
            + toString(this._portDeclarations_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._portDeclarationFoo_ == child)
        {
            this._portDeclarationFoo_ = null;
            return;
        }

        if(this._tComma_ == child)
        {
            this._tComma_ = null;
            return;
        }

        if(this._portDeclarations_ == child)
        {
            this._portDeclarations_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._portDeclarationFoo_ == oldChild)
        {
            setPortDeclarationFoo((PPortDeclarationFoo) newChild);
            return;
        }

        if(this._tComma_ == oldChild)
        {
            setTComma((TTComma) newChild);
            return;
        }

        if(this._portDeclarations_ == oldChild)
        {
            setPortDeclarations((PPortDeclarations) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}

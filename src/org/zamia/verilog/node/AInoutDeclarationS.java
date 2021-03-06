/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.zamia.verilog.node;

import org.zamia.verilog.analysis.*;

@SuppressWarnings("nls")
public final class AInoutDeclarationS extends PInoutDeclarationS
{
    private TKInout _kInout_;
    private PNetType _netType_;
    private TKSigned _kSigned_;
    private PRange _range_;
    private PIdentifier _identifier_;

    public AInoutDeclarationS()
    {
        // Constructor
    }

    public AInoutDeclarationS(
        @SuppressWarnings("hiding") TKInout _kInout_,
        @SuppressWarnings("hiding") PNetType _netType_,
        @SuppressWarnings("hiding") TKSigned _kSigned_,
        @SuppressWarnings("hiding") PRange _range_,
        @SuppressWarnings("hiding") PIdentifier _identifier_)
    {
        // Constructor
        setKInout(_kInout_);

        setNetType(_netType_);

        setKSigned(_kSigned_);

        setRange(_range_);

        setIdentifier(_identifier_);

    }

    @Override
    public Object clone()
    {
        return new AInoutDeclarationS(
            cloneNode(this._kInout_),
            cloneNode(this._netType_),
            cloneNode(this._kSigned_),
            cloneNode(this._range_),
            cloneNode(this._identifier_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAInoutDeclarationS(this);
    }

    public TKInout getKInout()
    {
        return this._kInout_;
    }

    public void setKInout(TKInout node)
    {
        if(this._kInout_ != null)
        {
            this._kInout_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._kInout_ = node;
    }

    public PNetType getNetType()
    {
        return this._netType_;
    }

    public void setNetType(PNetType node)
    {
        if(this._netType_ != null)
        {
            this._netType_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._netType_ = node;
    }

    public TKSigned getKSigned()
    {
        return this._kSigned_;
    }

    public void setKSigned(TKSigned node)
    {
        if(this._kSigned_ != null)
        {
            this._kSigned_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._kSigned_ = node;
    }

    public PRange getRange()
    {
        return this._range_;
    }

    public void setRange(PRange node)
    {
        if(this._range_ != null)
        {
            this._range_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._range_ = node;
    }

    public PIdentifier getIdentifier()
    {
        return this._identifier_;
    }

    public void setIdentifier(PIdentifier node)
    {
        if(this._identifier_ != null)
        {
            this._identifier_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._identifier_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._kInout_)
            + toString(this._netType_)
            + toString(this._kSigned_)
            + toString(this._range_)
            + toString(this._identifier_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._kInout_ == child)
        {
            this._kInout_ = null;
            return;
        }

        if(this._netType_ == child)
        {
            this._netType_ = null;
            return;
        }

        if(this._kSigned_ == child)
        {
            this._kSigned_ = null;
            return;
        }

        if(this._range_ == child)
        {
            this._range_ = null;
            return;
        }

        if(this._identifier_ == child)
        {
            this._identifier_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._kInout_ == oldChild)
        {
            setKInout((TKInout) newChild);
            return;
        }

        if(this._netType_ == oldChild)
        {
            setNetType((PNetType) newChild);
            return;
        }

        if(this._kSigned_ == oldChild)
        {
            setKSigned((TKSigned) newChild);
            return;
        }

        if(this._range_ == oldChild)
        {
            setRange((PRange) newChild);
            return;
        }

        if(this._identifier_ == oldChild)
        {
            setIdentifier((PIdentifier) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}

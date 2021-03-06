/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.zamia.verilog.node;

import org.zamia.verilog.analysis.*;

@SuppressWarnings("nls")
public final class AModuleInstantiation extends PModuleInstantiation
{
    private PIdentifier _identifier_;
    private PParameterValueAssignment _parameterValueAssignment_;
    private PModuleInstances _moduleInstances_;
    private TTSemicolon _tSemicolon_;

    public AModuleInstantiation()
    {
        // Constructor
    }

    public AModuleInstantiation(
        @SuppressWarnings("hiding") PIdentifier _identifier_,
        @SuppressWarnings("hiding") PParameterValueAssignment _parameterValueAssignment_,
        @SuppressWarnings("hiding") PModuleInstances _moduleInstances_,
        @SuppressWarnings("hiding") TTSemicolon _tSemicolon_)
    {
        // Constructor
        setIdentifier(_identifier_);

        setParameterValueAssignment(_parameterValueAssignment_);

        setModuleInstances(_moduleInstances_);

        setTSemicolon(_tSemicolon_);

    }

    @Override
    public Object clone()
    {
        return new AModuleInstantiation(
            cloneNode(this._identifier_),
            cloneNode(this._parameterValueAssignment_),
            cloneNode(this._moduleInstances_),
            cloneNode(this._tSemicolon_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAModuleInstantiation(this);
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

    public PParameterValueAssignment getParameterValueAssignment()
    {
        return this._parameterValueAssignment_;
    }

    public void setParameterValueAssignment(PParameterValueAssignment node)
    {
        if(this._parameterValueAssignment_ != null)
        {
            this._parameterValueAssignment_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._parameterValueAssignment_ = node;
    }

    public PModuleInstances getModuleInstances()
    {
        return this._moduleInstances_;
    }

    public void setModuleInstances(PModuleInstances node)
    {
        if(this._moduleInstances_ != null)
        {
            this._moduleInstances_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._moduleInstances_ = node;
    }

    public TTSemicolon getTSemicolon()
    {
        return this._tSemicolon_;
    }

    public void setTSemicolon(TTSemicolon node)
    {
        if(this._tSemicolon_ != null)
        {
            this._tSemicolon_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._tSemicolon_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._identifier_)
            + toString(this._parameterValueAssignment_)
            + toString(this._moduleInstances_)
            + toString(this._tSemicolon_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._identifier_ == child)
        {
            this._identifier_ = null;
            return;
        }

        if(this._parameterValueAssignment_ == child)
        {
            this._parameterValueAssignment_ = null;
            return;
        }

        if(this._moduleInstances_ == child)
        {
            this._moduleInstances_ = null;
            return;
        }

        if(this._tSemicolon_ == child)
        {
            this._tSemicolon_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._identifier_ == oldChild)
        {
            setIdentifier((PIdentifier) newChild);
            return;
        }

        if(this._parameterValueAssignment_ == oldChild)
        {
            setParameterValueAssignment((PParameterValueAssignment) newChild);
            return;
        }

        if(this._moduleInstances_ == oldChild)
        {
            setModuleInstances((PModuleInstances) newChild);
            return;
        }

        if(this._tSemicolon_ == oldChild)
        {
            setTSemicolon((TTSemicolon) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}

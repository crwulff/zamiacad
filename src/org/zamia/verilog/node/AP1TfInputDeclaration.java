/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.zamia.verilog.node;

import org.zamia.verilog.analysis.*;

@SuppressWarnings("nls")
public final class AP1TfInputDeclaration extends PTfInputDeclaration
{
    private TKInput _kInput_;
    private PTaskPortType _taskPortType_;
    private PListOfPortIdentifiers _listOfPortIdentifiers_;

    public AP1TfInputDeclaration()
    {
        // Constructor
    }

    public AP1TfInputDeclaration(
        @SuppressWarnings("hiding") TKInput _kInput_,
        @SuppressWarnings("hiding") PTaskPortType _taskPortType_,
        @SuppressWarnings("hiding") PListOfPortIdentifiers _listOfPortIdentifiers_)
    {
        // Constructor
        setKInput(_kInput_);

        setTaskPortType(_taskPortType_);

        setListOfPortIdentifiers(_listOfPortIdentifiers_);

    }

    @Override
    public Object clone()
    {
        return new AP1TfInputDeclaration(
            cloneNode(this._kInput_),
            cloneNode(this._taskPortType_),
            cloneNode(this._listOfPortIdentifiers_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAP1TfInputDeclaration(this);
    }

    public TKInput getKInput()
    {
        return this._kInput_;
    }

    public void setKInput(TKInput node)
    {
        if(this._kInput_ != null)
        {
            this._kInput_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._kInput_ = node;
    }

    public PTaskPortType getTaskPortType()
    {
        return this._taskPortType_;
    }

    public void setTaskPortType(PTaskPortType node)
    {
        if(this._taskPortType_ != null)
        {
            this._taskPortType_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._taskPortType_ = node;
    }

    public PListOfPortIdentifiers getListOfPortIdentifiers()
    {
        return this._listOfPortIdentifiers_;
    }

    public void setListOfPortIdentifiers(PListOfPortIdentifiers node)
    {
        if(this._listOfPortIdentifiers_ != null)
        {
            this._listOfPortIdentifiers_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._listOfPortIdentifiers_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._kInput_)
            + toString(this._taskPortType_)
            + toString(this._listOfPortIdentifiers_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._kInput_ == child)
        {
            this._kInput_ = null;
            return;
        }

        if(this._taskPortType_ == child)
        {
            this._taskPortType_ = null;
            return;
        }

        if(this._listOfPortIdentifiers_ == child)
        {
            this._listOfPortIdentifiers_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._kInput_ == oldChild)
        {
            setKInput((TKInput) newChild);
            return;
        }

        if(this._taskPortType_ == oldChild)
        {
            setTaskPortType((PTaskPortType) newChild);
            return;
        }

        if(this._listOfPortIdentifiers_ == oldChild)
        {
            setListOfPortIdentifiers((PListOfPortIdentifiers) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
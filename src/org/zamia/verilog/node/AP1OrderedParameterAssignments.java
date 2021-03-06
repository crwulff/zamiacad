/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.zamia.verilog.node;

import org.zamia.verilog.analysis.*;

@SuppressWarnings("nls")
public final class AP1OrderedParameterAssignments extends POrderedParameterAssignments
{
    private PExpression _expression_;
    private TTComma _tComma_;
    private POrderedParameterAssignments _orderedParameterAssignments_;

    public AP1OrderedParameterAssignments()
    {
        // Constructor
    }

    public AP1OrderedParameterAssignments(
        @SuppressWarnings("hiding") PExpression _expression_,
        @SuppressWarnings("hiding") TTComma _tComma_,
        @SuppressWarnings("hiding") POrderedParameterAssignments _orderedParameterAssignments_)
    {
        // Constructor
        setExpression(_expression_);

        setTComma(_tComma_);

        setOrderedParameterAssignments(_orderedParameterAssignments_);

    }

    @Override
    public Object clone()
    {
        return new AP1OrderedParameterAssignments(
            cloneNode(this._expression_),
            cloneNode(this._tComma_),
            cloneNode(this._orderedParameterAssignments_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAP1OrderedParameterAssignments(this);
    }

    public PExpression getExpression()
    {
        return this._expression_;
    }

    public void setExpression(PExpression node)
    {
        if(this._expression_ != null)
        {
            this._expression_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._expression_ = node;
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

    public POrderedParameterAssignments getOrderedParameterAssignments()
    {
        return this._orderedParameterAssignments_;
    }

    public void setOrderedParameterAssignments(POrderedParameterAssignments node)
    {
        if(this._orderedParameterAssignments_ != null)
        {
            this._orderedParameterAssignments_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._orderedParameterAssignments_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._expression_)
            + toString(this._tComma_)
            + toString(this._orderedParameterAssignments_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._expression_ == child)
        {
            this._expression_ = null;
            return;
        }

        if(this._tComma_ == child)
        {
            this._tComma_ = null;
            return;
        }

        if(this._orderedParameterAssignments_ == child)
        {
            this._orderedParameterAssignments_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._expression_ == oldChild)
        {
            setExpression((PExpression) newChild);
            return;
        }

        if(this._tComma_ == oldChild)
        {
            setTComma((TTComma) newChild);
            return;
        }

        if(this._orderedParameterAssignments_ == oldChild)
        {
            setOrderedParameterAssignments((POrderedParameterAssignments) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}

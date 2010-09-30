/*
 * Copyright 2007 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 * 
 */
package org.zamia.plugin.ui;

/**
 * @author guenter bartsch
 */
public interface ITreeNode
{
    public static final Object[] NO_CHILDREN = new Object[0];

    String getName();

    Object getParent();

    boolean hasChildren();

    Object[] getChildren();

    String getUniqueID();

    int getStartLine();

    int getEndLine();

    /**
     * Visitor design pattern.
     * @see ITreeVisitor#visit(ITreeNodeInfo)
     */
    //boolean accept(ITreeVisitor aVisitor);
}

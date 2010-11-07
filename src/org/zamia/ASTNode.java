/*
 * Copyright 2006-2010 by the authors indicated in the @author tags.
 * All rights reserved.
 *
 * See the LICENSE file for details.
 *
 * Created by guenter on Feb 27, 2006
 * 
 */
package org.zamia;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.zamia.util.HashMapArray;
import org.zamia.util.HashSetArray;
import org.zamia.util.ZStack;
import org.zamia.vhdl.ast.Architecture;
import org.zamia.vhdl.ast.ConcurrentStatement;
import org.zamia.zdb.ZDB;
import org.zamia.zdb.ZDBIIDSaver;

/**
 * 
 * everything in the syntax tree is supposed to be derived from this base class
 * 
 * @author Guenter Bartsch
 *
 */

@SuppressWarnings("serial")
public abstract class ASTNode implements Serializable, ZDBIIDSaver {

	public static final ZamiaLogger logger = ZamiaLogger.getInstance();

	public static final ExceptionLogger el = ExceptionLogger.getInstance();

	protected SourceFile fSource; // file name or URI

	protected int fStartCol, fStartLine;

	protected int fEndCol, fEndLine;

	protected ASTNode fParent;

	private static int counter = 0;

	protected final int fCnt;

	private transient long fDBID;

	private transient ZDB fZDB;

	public ASTNode(ASTNode aParent) {
		fParent = aParent;
		fCnt = counter++;
	}

	public ASTNode() {
		this(null);
	}

	public SourceLocation getLocation() {
		return new SourceLocation(fSource, fStartLine, fStartCol);
	}

	public void setParent(ASTNode aParent) {
		setParent(aParent, false);
	}

	public void setParent(ASTNode aParent, boolean aForce) {

		if (!aForce && fParent != null)
			return;

		fParent = aParent;
	}

	public ASTNode getParent() {
		return fParent;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void visit(IASTNodeVisitor aVisitor) {

		HashSet<ASTNode> done = new HashSet<ASTNode>();

		ZStack<ASTNode> stack = new ZStack<ASTNode>();
		ZStack<ASTNode> postStack = new ZStack<ASTNode>();

		stack.push(this);

		while (!stack.isEmpty()) {

			ASTNode node = stack.pop();

			if (done.contains(node)) {
				continue;
			}

			done.add(node);

			aVisitor.visitPre(node);
			
			if (node instanceof Architecture) {
				
				Architecture arch = (Architecture) node;
				
				int n = arch.getNumConcurrentStatements();
				
				for (int i = 0; i<n; i++) {
					ConcurrentStatement cs = arch.getConcurrentStatement(i);
					stack.push(cs);
				}
				
			}

			Class cls = node.getClass();

			while (!Object.class.equals(cls)) {
				Field[] fields = cls.getDeclaredFields();
				int n = fields.length;
				for (int i = 0; i < n; i++) {
					Field field = fields[i];
					field.setAccessible(true);

					try {
						Object value = field.get(node);

						if (value instanceof ASTNode) {

							ASTNode io = (ASTNode) value;

							if (io != node.getParent()) {
								stack.push(io);
							}

						} else if (value instanceof HashMap) {
							HashMap hm = (HashMap) value;
							for (Iterator it = hm.keySet().iterator(); it.hasNext();) {
								Object key = it.next();
								Object v = hm.get(key);
								if (v instanceof ASTNode) {
									ASTNode io = (ASTNode) v;
									stack.push(io);
								}
								if (key instanceof ASTNode) {
									ASTNode io = (ASTNode) key;
									stack.push(io);
								}
							}

						} else if (value instanceof HashMapArray) {
							HashMapArray hm = (HashMapArray) value;
							for (Iterator it = hm.keySet().iterator(); it.hasNext();) {
								Object key = it.next();
								Object v = hm.get(key);
								if (v instanceof ASTNode) {
									ASTNode io = (ASTNode) v;
									stack.push(io);
								}
								if (key instanceof ASTNode) {
									ASTNode io = (ASTNode) key;
									stack.push(io);
								}
							}

						} else if (value instanceof Array) {
							Array a = (Array) value;
							int m = Array.getLength(a);
							for (int j = 0; j < m; j++) {
								Object o2 = Array.get(a, j);
								if (o2 instanceof ASTNode) {
									ASTNode io = (ASTNode) o2;
									stack.push(io);
								}
							}

						} else if (value instanceof ArrayList) {
							ArrayList al = (ArrayList) value;
							int m = al.size();
							for (int j = 0; j < m; j++) {
								Object o2 = al.get(j);
								if (o2 instanceof ASTNode) {
									ASTNode io = (ASTNode) o2;
									stack.push(io);
								}
							}

						} else if (value instanceof HashSetArray) {
							HashSetArray hma = (HashSetArray) value;

							int m = hma.size();
							for (int j = 0; j < m; j++) {
								Object o2 = hma.get(j);
								if (o2 instanceof ASTNode) {
									ASTNode io = (ASTNode) o2;
									stack.push(io);
								}
							}

						} else if (value instanceof List) {
							List list = (List) value;

							int m = list.size();
							for (int j = 0; j < m; j++) {
								Object o = list.get(j);

								if (o instanceof ASTNode) {
									ASTNode io = (ASTNode) o;

									stack.push(io);
								}
							}
						}

					} catch (IllegalArgumentException e) {
						el.logException(e);
					} catch (IllegalAccessException e) {
						el.logException(e);
					}
				}
				cls = cls.getSuperclass();
			}

			postStack.push(node);
		}

		while (!postStack.isEmpty()) {
			ASTNode node = postStack.pop();
			aVisitor.visitPost(node);
		}

	}

	public ZDB getZDB() {
		if (fZDB != null) {
			return fZDB;
		}
		if (fParent != null) {
			return fParent.getZDB();
		}
		return null;
	}

	public ZamiaProject getZPrj() {
		fZDB = getZDB();
		if (fZDB == null)
			return null;
		return (ZamiaProject) fZDB.getOwner();
	}

	public int getStartCol() {
		return fStartCol;
	}

	public void setStartCol(int aStartCol) {
		fStartCol = aStartCol;
	}

	public int getStartLine() {
		return fStartLine;
	}

	public void setStartLine(int aStartLine) {
		fStartLine = aStartLine;
	}

	public int getEndCol() {
		return fEndCol;
	}

	public void setEndCol(int aEndCol) {
		fEndCol = aEndCol;
	}

	public int getEndLine() {
		return fEndLine;
	}

	public void setEndLine(int aEndLine) {
		fEndLine = aEndLine;
	}

	public SourceFile getSource() {
		return fSource;
	}

	public void setSource(SourceFile aSource) {
		fSource = aSource;
	}

	public int getCnt() {
		return fCnt;
	}

	/*
	 * ZDB ID Saving
	 */

	public void setDBID(long aId) {
		fDBID = aId;
	}

	public long getDBID() {
		return fDBID;
	}

	public void setZDB(ZDB aZDB) {
		fZDB = aZDB;
	}

}

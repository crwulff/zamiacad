/* 
 * Copyright 2009 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 * Created by Guenter Bartsch on Apr 11, 2009
 */
package org.zamia.instgraph.interpreter;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGStaticValueBuilder;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.IGOperationAttribute.AttrOp;
import org.zamia.vhdl.ast.VHDLNode.ASTErrorMode;
import org.zamia.zdb.ZDB;


/**
 * 
 * @author Guenter Bartsch
 * 
 */
@SuppressWarnings("serial")
public class IGAttributeStmt extends IGStmt {

	private boolean fHaveArgument;

	private AttrOp fOp;

	private long fResTypeDBID;

	public IGAttributeStmt(IGType aResType, AttrOp aAttrOp, boolean aHaveArgument, SourceLocation aLocation, ZDB aZDB) {
		super(aLocation, aZDB);
		fHaveArgument = aHaveArgument;
		fOp = aAttrOp;
		fResTypeDBID = save(aResType);
	}

	private IGType getResType() {
		return (IGType) getZDB().load(fResTypeDBID);
	}

	@Override
	public ReturnStatus execute(IGInterpreterRuntimeEnv aRuntime, ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		IGStackFrame sf = aRuntime.pop();

		IGStaticValue argument = null;
		if (fHaveArgument) {
			IGStackFrame sf1 = aRuntime.pop();
			argument = sf1.getValue();
		}

		IGStaticValue resValue = null;

		IGTypeStatic type = sf.getType();
		if (type != null) {

			if (argument != null) {

				if (type.isDiscrete()) {

					switch (fOp) {
					case POS:

						String id = argument.getId();

						resValue = type.findEnumLiteral(id);
						if (resValue == null) {
							throw new ZamiaException("Enum literal not found :" + id, computeSourceLocation());
						}

						IGTypeStatic resType = getResType().computeStaticType(aRuntime, aErrorMode, aReport);
						if (resType == null) {
							return ReturnStatus.ERROR;
						}
						resValue = new IGStaticValueBuilder(resType, null, computeSourceLocation()).setNum(resValue.getOrd()).buildConstant();

						break;

					default:
						throw new ZamiaException("Sorry. not implemented yet: Attribute " + fOp + " for discrete types.", computeSourceLocation());
					}

				} else {
					if (!type.isArray()) {
						throw new ZamiaException("Not an array type: " + type, computeSourceLocation());
					}

					// FIXME
					throw new ZamiaException("Sorry. not implemented yet.", computeSourceLocation());
				}
			} else {

				switch (fOp) {
				case HIGH:
					if (type.isArray()) {
						resValue = type.getStaticIndexType(computeSourceLocation()).getStaticHigh(computeSourceLocation());
					} else {
						resValue = type.getStaticHigh(computeSourceLocation());
					}
					break;
				case LOW:
					if (type.isArray()) {
						resValue = type.getStaticIndexType(computeSourceLocation()).getStaticLow(computeSourceLocation());
					} else {
						resValue = type.getStaticLow(computeSourceLocation());
					}
					break;
				case LEFT:
					if (type.isArray()) {
						resValue = type.getStaticIndexType(computeSourceLocation()).getStaticLeft(computeSourceLocation());
					} else {
						resValue = type.getStaticLeft(computeSourceLocation());
					}
					break;
				case RIGHT:
					if (type.isArray()) {
						resValue = type.getStaticIndexType(computeSourceLocation()).getStaticRight(computeSourceLocation());
					} else {
						resValue = type.getStaticRight(computeSourceLocation());
					}
					break;
				case ASCENDING:
					if (type.isArray()) {
						resValue = type.getStaticIndexType(computeSourceLocation()).getStaticAscending(computeSourceLocation());
					} else {
						resValue = type.getStaticAscending(computeSourceLocation());
					}
					break;
				case RANGE:
					resValue = type.isArray() ? type.getStaticIndexType(null).getStaticRange() : type.getStaticRange();
					break;
				case REVERSE_RANGE:
					if (type.isArray()) {
						resValue = type.getStaticIndexType(computeSourceLocation()).getStaticRange();
					} else {
						resValue = type.getStaticRange();
					}
					
					IGStaticValueBuilder b = new IGStaticValueBuilder(resValue.getStaticType(), null, computeSourceLocation());

					// reverse direction
					
					IGStaticValue asc = resValue.getAscending();
					IGTypeStatic ascT = asc.getStaticType();
					
					boolean ascB = !asc.isTrue();
					
					asc = ascB ? ascT.getEnumLiteral(1, computeSourceLocation(), ASTErrorMode.EXCEPTION, null) : ascT.getEnumLiteral(0, computeSourceLocation(), ASTErrorMode.EXCEPTION, null);

					b.setAscending(asc);
					
					// left becomes right, right becomes left
					
					b.setRight(resValue.getLeft());
					b.setLeft(resValue.getRight());
					
					resValue = b.buildConstant();
					
					break;
				case LENGTH:
					if (!type.isArray()) {
						throw new ZamiaException ("Attribute "+fOp+" is not defined for non-array types.", computeSourceLocation());
					}
					IGTypeStatic idxType = type.getStaticIndexType(computeSourceLocation());
					long card = idxType.computeCardinality(computeSourceLocation());
					resValue = new IGStaticValueBuilder(idxType, null, computeSourceLocation()).setNum(card).buildConstant();
					break;
				default:
					throw new ZamiaException("Internal error: attribute " + fOp + " not implemented for types.", computeSourceLocation());
				}
			}
		} else {

			IGStaticValue v = sf.getLiteral();
			type = v.getStaticType();

			switch (fOp) {
			case LENGTH:

				if (type.isArray()) {
					type = type.getStaticIndexType(computeSourceLocation());
				}

				IGTypeStatic srType = getResType().computeStaticType(aRuntime, aErrorMode, aReport);
				
				resValue = new IGStaticValueBuilder(srType, null, computeSourceLocation()).setNum(
						type.computeCardinality(computeSourceLocation())).buildConstant();
				break;
			case LOW:
				if (type.isArray()) {
					resValue = type.getStaticIndexType(computeSourceLocation()).getStaticLow(computeSourceLocation());
				} else {
					resValue = v.getAscending().isTrue() ? v.getLeft() : v.getRight();
				}
				break;
			case HIGH:
				if (type.isArray()) {
					resValue = type.getStaticIndexType(computeSourceLocation()).getStaticHigh(computeSourceLocation());
				} else {
					resValue = v.getAscending().isTrue() ? v.getRight() : v.getLeft();
				}
				break;
			case LEFT:
				if (type.isArray()) {
					resValue = type.getStaticIndexType(computeSourceLocation()).getStaticLeft(computeSourceLocation());
				} else {
					resValue = v.getLeft();
				}
				break;
			case RIGHT:
				if (type.isArray()) {
					resValue = type.getStaticIndexType(computeSourceLocation()).getStaticRight(computeSourceLocation());
				} else {
					resValue = v.getRight();
				}
				break;
			case ASCENDING:
				if (type.isArray()) {
					resValue = type.getStaticIndexType(computeSourceLocation()).getStaticAscending(computeSourceLocation());
				} else {
					resValue = v.getAscending();
				}
				break;
			case RANGE:
				if (type.isArray()) {
					resValue = type.getStaticIndexType(computeSourceLocation()).getStaticRange();
				} else {
					resValue = type.getStaticRange();
				}
				break;
			case REVERSE_RANGE:
				if (type.isArray()) {
					resValue = type.getStaticIndexType(computeSourceLocation()).getStaticRange();
				} else {
					resValue = type.getStaticRange();
				}
				
				IGStaticValueBuilder b = new IGStaticValueBuilder(resValue.getStaticType(), null, computeSourceLocation());

				// reverse direction
				
				IGStaticValue asc = resValue.getAscending();
				IGTypeStatic ascT = asc.getStaticType();
				
				boolean ascB = !asc.isTrue();
				
				asc = ascB ? ascT.getEnumLiteral(1, computeSourceLocation(), ASTErrorMode.EXCEPTION, null) : ascT.getEnumLiteral(0, computeSourceLocation(), ASTErrorMode.EXCEPTION, null);

				b.setAscending(asc);
				
				// left becomes right, right becomes left
				
				b.setRight(resValue.getLeft());
				b.setLeft(resValue.getRight());
				
				resValue = b.buildConstant();
				
				break;
			default:
				throw new ZamiaException("Internal error: attribute " + fOp + " not implemented for values.", computeSourceLocation());
			}

		}

		aRuntime.push(resValue);

		return ReturnStatus.CONTINUE;
	}

	@Override
	public String toString() {
		return "ATTRIBUTE OP " + fOp;
	}
}

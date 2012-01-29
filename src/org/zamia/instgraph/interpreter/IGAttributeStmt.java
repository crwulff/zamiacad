/* 
 * Copyright 2009,2010 by the authors indicated in the @author tags. 
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
import org.zamia.instgraph.IGOperationAttribute.AttrOp;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGStaticValueBuilder;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.sim.ref.IGFileDriver;
import org.zamia.instgraph.sim.ref.IGSignalDriver;
import org.zamia.instgraph.sim.ref.IGSimProcess;
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

					IGTypeStatic resType = getResType().computeStaticType(aRuntime, aErrorMode, aReport);
					if (resType == null) {
						return ReturnStatus.ERROR;
					}

					switch (fOp) {
					case POS:

						String id = argument.getId();

						resValue = type.findEnumLiteral(id);
						if (resValue == null) {
							throw new ZamiaException("Enum literal not found :" + id, computeSourceLocation());
						}

						resValue = new IGStaticValueBuilder(resType, null, computeSourceLocation()).setNum(resValue.getOrd()).buildConstant();

						break;

					case IMAGE:

						String val = null;

						if (type.isInteger()) {

							val = argument.toDecString();

						} else if (type.isEnum()) {

							val = argument.toString().toLowerCase();

							if (val.startsWith("\\") && val.endsWith("\\")) {
								// extended identifier => double backslashes
								val = val.substring(1, val.length() - 1).replace("\\", "\\\\");
								val = "\\" + val + "\\";
							} else {
								val = "'" + val + "'";
							}
						}

						if (val == null) {
							throw new ZamiaException("Sorry, not implemented yet: Attribute IMAGE is only supported for NUMERIC and ENUM types atm...", computeSourceLocation());
						}
						resValue = IGFileDriver.line2IG(val, resType, aRuntime, computeSourceLocation(), aErrorMode, aReport, aRuntime.getZDB());

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

					asc = ascB ? ascT.getEnumLiteral(1, computeSourceLocation(), ASTErrorMode.EXCEPTION, null) : ascT.getEnumLiteral(0, computeSourceLocation(),
							ASTErrorMode.EXCEPTION, null);

					b.setAscending(asc);

					// left becomes right, right becomes left

					b.setRight(resValue.getLeft());
					b.setLeft(resValue.getRight());

					resValue = b.buildConstant();

					break;
				case LENGTH:
					if (!type.isArray()) {
						throw new ZamiaException("Attribute " + fOp + " is not defined for non-array types.", computeSourceLocation());
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

			IGStaticValue v = sf.getValue();

			if (v == null) {

				IGObjectDriver driver = sf.getObjectDriver();

				if (driver != null) {

					type = driver.getCurrentType();
					
				} else {
					if (aErrorMode == ASTErrorMode.RETURN_NULL) {
						return ReturnStatus.ERROR;
					} else {
						throw new ZamiaException("Internal error: value expected for attribute computation.", computeSourceLocation());
					}
				}

			} else {

				type = v.getStaticType();
			}

			switch (fOp) {
			case LENGTH:

				if (type.isArray()) {
					if (!checkConstrained(type, aErrorMode, aReport, computeSourceLocation())) {
						return ReturnStatus.ERROR;
					}
					type = type.getStaticIndexType(computeSourceLocation());
				}

				IGTypeStatic srType = getResType().computeStaticType(aRuntime, aErrorMode, aReport);

				resValue = new IGStaticValueBuilder(srType, null, computeSourceLocation()).setNum(type.computeCardinality(computeSourceLocation())).buildConstant();
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

				asc = ascB ? ascT.getEnumLiteral(1, computeSourceLocation(), ASTErrorMode.EXCEPTION, null) : ascT.getEnumLiteral(0, computeSourceLocation(),
						ASTErrorMode.EXCEPTION, null);

				b.setAscending(asc);

				// left becomes right, right becomes left

				b.setRight(resValue.getLeft());
				b.setLeft(resValue.getRight());

				resValue = b.buildConstant();

				break;
			case EVENT:

				IGObjectDriver driver = sf.getObjectDriver();

				IGType rt = getResType().computeStaticType(aRuntime, aErrorMode, aReport);
				if (rt == null) {
					return ReturnStatus.ERROR;
				}

				resValue = rt.getEnumLiteral(driver.isEvent() ? 1 : 0, computeSourceLocation(), ASTErrorMode.EXCEPTION, null);

				break;
			case LAST_VALUE:

				driver = sf.getObjectDriver().getTargetDriver();
				if (!(driver instanceof IGSignalDriver)) {
					throw new ZamiaException("Internal error: attribute " + fOp + " is only supported for signals.", computeSourceLocation());
				}
				IGSignalDriver signalDriver = (IGSignalDriver) driver;

				if (!(aRuntime instanceof IGSimProcess)) {
					throw new ZamiaException("Internal error: attribute " + fOp + " is only supported for runtime with signals' history.", computeSourceLocation());
				}
				IGSimProcess simProcess = (IGSimProcess) aRuntime;

				resValue = simProcess.getObjectLastValue(signalDriver.getPath());

				break;
			default:
				throw new ZamiaException("Internal error: attribute " + fOp + " not implemented for values.", computeSourceLocation());
			}

		}

		aRuntime.push(resValue);

		return ReturnStatus.CONTINUE;
	}

	private boolean checkConstrained(IGTypeStatic aType, ASTErrorMode aErrorMode, ErrorReport aReport, SourceLocation aLocation) throws ZamiaException {
		
		if (!aType.isUnconstrained())
			return true;
		
		ZamiaException e = new ZamiaException("Unconstrained array detected in attribute computation.", aLocation);
		if (aErrorMode == ASTErrorMode.EXCEPTION) {
			throw e;
		}
		if (aReport != null) {
			aReport.append(e);
		}
		
		return false;
	}

	@Override
	public String toString() {
		return "ATTRIBUTE OP " + fOp;
	}
}

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
import org.zamia.instgraph.IGOperation;
import org.zamia.instgraph.IGOperationAttribute.AttrOp;
import org.zamia.instgraph.IGOperationLiteral;
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
		SourceLocation sourceLocation = computeSourceLocation();
		
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
							throw new ZamiaException("Enum literal not found :" + id, sourceLocation);
						}

						resValue = new IGStaticValueBuilder(resType, null, sourceLocation).setNum(resValue.getOrd()).buildConstant();

						break;

					case IMAGE:

						String val = null;

						if (type.isInteger()) {

							val = argument.toDecString();

						} else if (type.isEnum()) {

							if (type.isCharEnum())
								val = (argument.isCharLiteral() ? "'" + argument.getId() + "'" : argument.getId()).toLowerCase();
							
							else {
								val = argument.toString().toLowerCase();

								if (val.startsWith("\\") && val.endsWith("\\")) {
									// extended identifier => double backslashes
									val = val.substring(1, val.length() - 1).replace("\\", "\\\\");
									val = "\\" + val + "\\";
								} else {
									val = "'" + val + "'";
								}
							}
							
						}

						if (val == null) {
							throw new ZamiaException("Sorry, not implemented yet: Attribute IMAGE is only supported for NUMERIC and ENUM types atm...", sourceLocation);
						}
						
						resValue = IGFileDriver.line2IG(val, resType, aRuntime, sourceLocation, aErrorMode, aReport, aRuntime.getZDB());
						
						break;

					case VAL:
						resValue = type.getDiscreteValue(argument.getOrd(), sourceLocation, aErrorMode, aReport).computeStaticValue(aRuntime, aErrorMode, aReport);
						break;
					default:
						throw new ZamiaException("Sorry. not implemented yet: Attribute " + fOp + " for discrete types.", sourceLocation);
					}

				} else {
					if (!type.isArray()) {
						throw new ZamiaException("Not an array type: " + type, sourceLocation);
					}

					// FIXME
					throw new ZamiaException("Sorry. not implemented yet.", sourceLocation);
				}
			} else {

				switch (fOp) {
				case HIGH:
					if (type.isArray()) {
						resValue = type.getStaticIndexType(sourceLocation).getStaticHigh(sourceLocation);
					} else {
						resValue = type.getStaticHigh(sourceLocation);
					}
					break;
				case LOW:
					if (type.isArray()) {
						resValue = type.getStaticIndexType(sourceLocation).getStaticLow(sourceLocation);
					} else {
						resValue = type.getStaticLow(sourceLocation);
					}
					break;
				case LEFT:
					if (type.isArray()) {
						resValue = type.getStaticIndexType(sourceLocation).getStaticLeft(sourceLocation);
					} else {
						resValue = type.getStaticLeft(sourceLocation);
					}
					break;
				case RIGHT:
					if (type.isArray()) {
						resValue = type.getStaticIndexType(sourceLocation).getStaticRight(sourceLocation);
					} else {
						resValue = type.getStaticRight(sourceLocation);
					}
					break;
				case ASCENDING:
					if (type.isArray()) {
						resValue = type.getStaticIndexType(sourceLocation).getStaticAscending(sourceLocation);
					} else {
						resValue = type.getStaticAscending(sourceLocation);
					}
					break;
				case RANGE:
					resValue = type.isArray() ? type.getStaticIndexType(null).getStaticRange() : type.getStaticRange();
					break;
				case REVERSE_RANGE:
					if (type.isArray()) {
						resValue = type.getStaticIndexType(sourceLocation).getStaticRange();
					} else {
						resValue = type.getStaticRange();
					}

					IGStaticValueBuilder b = new IGStaticValueBuilder(resValue.getStaticType(), null, sourceLocation);

					// reverse direction

					IGStaticValue asc = resValue.getAscending();
					IGTypeStatic ascT = asc.getStaticType();

					boolean ascB = !asc.isTrue();

					asc = ascB ? ascT.getEnumLiteral(1, sourceLocation, ASTErrorMode.EXCEPTION, null) : ascT.getEnumLiteral(0, sourceLocation,
							ASTErrorMode.EXCEPTION, null);

					b.setAscending(asc);

					// left becomes right, right becomes left

					b.setRight(resValue.getLeft());
					b.setLeft(resValue.getRight());

					resValue = b.buildConstant();

					break;
				case LENGTH:
					if (!type.isArray()) {
						throw new ZamiaException("Attribute " + fOp + " is not defined for non-array types.", sourceLocation);
					}
					IGTypeStatic idxType = type.getStaticIndexType(sourceLocation);
					long card = idxType.computeCardinality(sourceLocation);
					resValue = new IGStaticValueBuilder(idxType, null, sourceLocation).setNum(card).buildConstant();
					break;
				default:
					throw new ZamiaException("Internal error: attribute " + fOp + " not implemented for types.", sourceLocation);
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
						throw new ZamiaException("Internal error: value expected for attribute computation.", sourceLocation);
					}
				}

			} else {

				type = v.getStaticType();
			}

			switch (fOp) {
			case LENGTH:

				if (type.isArray()) {
					if (!checkConstrained(type, aErrorMode, aReport, sourceLocation)) {
						return ReturnStatus.ERROR;
					}
					type = type.getStaticIndexType(sourceLocation);
				}

				IGTypeStatic srType = getResType().computeStaticType(aRuntime, aErrorMode, aReport);

				resValue = new IGStaticValueBuilder(srType, null, sourceLocation).setNum(type.computeCardinality(sourceLocation)).buildConstant();
				break;
			case LOW:
				if (type.isArray()) {
					resValue = type.getStaticIndexType(sourceLocation).getStaticLow(sourceLocation);
				} else {
					resValue = v.getAscending().isTrue() ? v.getLeft() : v.getRight();
				}
				break;
			case HIGH:
				if (type.isArray()) {
					resValue = type.getStaticIndexType(sourceLocation).getStaticHigh(sourceLocation);
				} else {
					resValue = v.getAscending().isTrue() ? v.getRight() : v.getLeft();
				}
				break;
			case LEFT:
				if (type.isArray()) {
					resValue = type.getStaticIndexType(sourceLocation).getStaticLeft(sourceLocation);
				} else {
					resValue = v.getLeft();
				}
				break;
			case RIGHT:
				if (type.isArray()) {
					resValue = type.getStaticIndexType(sourceLocation).getStaticRight(sourceLocation);
				} else {
					resValue = v.getRight();
				}
				break;
			case ASCENDING:
				if (type.isArray()) {
					resValue = type.getStaticIndexType(sourceLocation).getStaticAscending(sourceLocation);
				} else {
					resValue = v.getAscending();
				}
				break;
			case RANGE:
				if (type.isArray()) {
					resValue = type.getStaticIndexType(sourceLocation).getStaticRange();
				} else {
					resValue = type.getStaticRange();
				}
				break;
			case REVERSE_RANGE:
				if (type.isArray()) {
					resValue = type.getStaticIndexType(sourceLocation).getStaticRange();
				} else {
					resValue = type.getStaticRange();
				}

				IGStaticValueBuilder b = new IGStaticValueBuilder(resValue.getStaticType(), null, sourceLocation);

				// reverse direction

				IGStaticValue asc = resValue.getAscending();
				IGTypeStatic ascT = asc.getStaticType();

				boolean ascB = !asc.isTrue();

				asc = ascB ? ascT.getEnumLiteral(1, sourceLocation, ASTErrorMode.EXCEPTION, null) : ascT.getEnumLiteral(0, sourceLocation,
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

				resValue = rt.getEnumLiteral(driver.isEvent() ? 1 : 0, sourceLocation, ASTErrorMode.EXCEPTION, null);

				break;
			case LAST_VALUE:

				driver = sf.getObjectDriver().getTargetDriver();
				if (!(driver instanceof IGSignalDriver)) {
					throw new ZamiaException("Internal error: attribute " + fOp + " is only supported for signals.", sourceLocation);
				}
				IGSignalDriver signalDriver = (IGSignalDriver) driver;

				if (!(aRuntime instanceof IGSimProcess)) {
					throw new ZamiaException("Internal error: attribute " + fOp + " is only supported for runtime with signals' history.", sourceLocation);
				}
				IGSimProcess simProcess = (IGSimProcess) aRuntime;

				resValue = simProcess.getObjectLastValue(signalDriver.getPath());

				break;
			default:
				throw new ZamiaException("Internal error: attribute " + fOp + " not implemented for values.", sourceLocation);
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

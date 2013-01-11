package org.zamia.instgraph;

import org.zamia.ZamiaLogger;

/**
 * This class is intended to be inherited.
 * <p/>
 * Subclasses are responsible for deciding which operations to step in.
 *
 * @author Anton Chepurov
 */
@SuppressWarnings("UnusedParameters")
public class IGDefaultOperationVisitor {

    protected final static ZamiaLogger logger = ZamiaLogger.getInstance();

    protected IGDefaultSequentialStatementVisitor fSeqStmtVisitor;

    void setSequentialStatementVisitor(IGDefaultSequentialStatementVisitor aSequentialStatementVisitor) {
        fSeqStmtVisitor = aSequentialStatementVisitor;
    }

    protected void visitOperation(IGOperation aOperation) {

        if (aOperation instanceof IGOperationBinary) {
            visitBinary((IGOperationBinary) aOperation);
        } else if (aOperation instanceof IGOperationInvokeSubprogram) {
            visitSubprogram((IGOperationInvokeSubprogram) aOperation);
        } else if (aOperation instanceof IGOperationAlias) {
            visitAlias((IGOperationAlias) aOperation);
        } else if (aOperation instanceof IGOperationAllocate) {
            visitAllocate((IGOperationAllocate) aOperation);
        } else if (aOperation instanceof IGOperationArrayAggregate) {
            visitAggregate((IGOperationArrayAggregate) aOperation);
        } else if (aOperation instanceof IGOperationAttribute) {
            visitAttribute((IGOperationAttribute) aOperation);
        } else if (aOperation instanceof IGOperationDeref) {
            visitDeref((IGOperationDeref) aOperation);
        } else if (aOperation instanceof IGOperationError) {
            visitError((IGOperationError) aOperation);
        } else if (aOperation instanceof IGOperationIndex) {
            visitIndex((IGOperationIndex) aOperation);
        } else if (aOperation instanceof IGOperationLiteral) {
            visitLiteral((IGOperationLiteral) aOperation);
        } else if (aOperation instanceof IGOperationObject) {
            visitObject((IGOperationObject) aOperation);
        } else if (aOperation instanceof IGOperationPhi) {
            visitPhi((IGOperationPhi) aOperation);
        } else if (aOperation instanceof IGOperationRange) {
            visitOpRange((IGOperationRange) aOperation);
        } else if (aOperation instanceof IGOperationRecordAggregate) {
            visitRecordAggregate((IGOperationRecordAggregate) aOperation);
        } else if (aOperation instanceof IGOperationRecordField) {
            visitRecordField((IGOperationRecordField) aOperation);
        } else if (aOperation instanceof IGOperationTypeConversion) {
            visitTypeConversion((IGOperationTypeConversion) aOperation);
        } else if (aOperation instanceof IGOperationTypeQualification) {
            visitTypeQualification((IGOperationTypeQualification) aOperation);
        } else if (aOperation instanceof IGOperationUnary) {
            visitUnary((IGOperationUnary) aOperation);
        } else if (aOperation instanceof IGRange) {
            visitRange((IGRange) aOperation);
        } else if (aOperation instanceof IGStaticValue) {
            visitStaticValue((IGStaticValue) aOperation);
        } else {
            logger.debug("IGDefaultOperationVisitor: unknown operation: %s", aOperation);
        }
    }

    protected void visitStaticValue(IGStaticValue aValue) {
    }

    protected void visitRange(IGRange aRange) {
    }

    protected void visitUnary(IGOperationUnary aUnary) {
    }

    protected void visitTypeQualification(IGOperationTypeQualification aTypeQualification) {
    }

    protected void visitTypeConversion(IGOperationTypeConversion aTypeConversion) {
    }

    protected void visitRecordField(IGOperationRecordField aRecordField) {
    }

    protected void visitRecordAggregate(IGOperationRecordAggregate aRecordAggregate) {
    }

    protected void visitOpRange(IGOperationRange aOpRange) {
    }

    protected void visitPhi(IGOperationPhi aPhi) {
    }

    protected void visitObject(IGOperationObject aObject) {
    }

    protected void visitLiteral(IGOperationLiteral aLiteral) {
    }

    protected void visitIndex(IGOperationIndex aIndex) {
    }

    protected void visitError(IGOperationError aError) {
    }

    protected void visitDeref(IGOperationDeref aDeref) {
    }

    protected void visitAttribute(IGOperationAttribute aAttribute) {
    }

    protected void visitAggregate(IGOperationArrayAggregate aAggregate) {
    }

    protected void visitAllocate(IGOperationAllocate aAllocate) {
    }

    protected void visitAlias(IGOperationAlias aAlias) {
    }

    protected void visitSubprogram(IGOperationInvokeSubprogram aSubprogram) {
    }

    protected void visitBinary(IGOperationBinary aBinary) {
    }

}

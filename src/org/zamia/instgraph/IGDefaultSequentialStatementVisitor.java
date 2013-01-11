package org.zamia.instgraph;

import org.zamia.ZamiaLogger;
import org.zamia.util.PathName;

/**
 * This class is intended to be inherited.
 * <p/>
 * The class can traverse all sequential statements of the design.
 * Subclasses are responsible for deciding which statements to step in.
 * <p/>
 * Uses {@link IGDefaultOperationVisitor} to traverse operations.
 * <p/>
 * todo: add objects traversal (with path as a method's parameter)
 * todo: add AllStatementsVisitor which traverses absolutely every statement. Not sure if it is useful, though.
 *
 * @author Anton Chepurov
 */
@SuppressWarnings("UnusedDeclaration")
public class IGDefaultSequentialStatementVisitor implements IGStructureVisitor {

    protected final static ZamiaLogger logger = ZamiaLogger.getInstance();

    protected final IGDefaultOperationVisitor fOperationVisitor;

    public IGDefaultSequentialStatementVisitor(IGDefaultOperationVisitor aOperationVisitor) {
        fOperationVisitor = aOperationVisitor;
        aOperationVisitor.setSequentialStatementVisitor(this);
    }

    @Override
    public void visit(IGStructure aStructure, PathName aPath) {

        int n = aStructure.getNumStatements();
        for (int i = 0; i < n; i++) {
            IGConcurrentStatement stmt = aStructure.getStatement(i);
            if (stmt instanceof IGProcess) {
                IGProcess process = (IGProcess) stmt;

                IGSequenceOfStatements sos = process.getSequenceOfStatements();

                visit(sos);
            }
        }
    }

    protected void visit(IGSequentialStatement aStatement) {

        if (aStatement instanceof IGSequenceOfStatements) {
            IGSequenceOfStatements sos = (IGSequenceOfStatements) aStatement;
            int numStatements = sos.getNumStatements();
            for (int j = 0; j < numStatements; j++) {
                IGSequentialStatement statement = sos.getStatement(j);
                visit(statement);
            }
        } else if (aStatement instanceof IGSequentialAssignment) {
            visitAssignment((IGSequentialAssignment) aStatement);
        } else if (aStatement instanceof IGSequentialAssert) {
            visitAssert((IGSequentialAssert) aStatement);
        } else if (aStatement instanceof IGSequentialExit) {
            visitExit((IGSequentialExit) aStatement);
        } else if (aStatement instanceof IGSequentialIf) {
            visitIf((IGSequentialIf) aStatement);
        } else if (aStatement instanceof IGSequentialLoop) {
            visitLoop((IGSequentialLoop) aStatement);
        } else if (aStatement instanceof IGSequentialNext) {
            visitNext((IGSequentialNext) aStatement);
        } else if (aStatement instanceof IGSequentialProcedureCall) {
            visitCall((IGSequentialProcedureCall) aStatement);
        } else if (aStatement instanceof IGSequentialReport) {
            visitReport((IGSequentialReport) aStatement);
        } else if (aStatement instanceof IGSequentialRestart) {
            visitRestart((IGSequentialRestart) aStatement);
        } else if (aStatement instanceof IGSequentialReturn) {
            visitReturn((IGSequentialReturn) aStatement);
        } else if (aStatement instanceof IGSequentialWait) {
            visitWait((IGSequentialWait) aStatement);
        } else {
            logger.debug("IGDefaultSequentialStatementVisitor: unknown sequential statement: %s", aStatement);
        }

    }

    protected void visitWait(IGSequentialWait aWait) {
    }

    protected void visitReturn(IGSequentialReturn aReturn) {
    }

    protected void visitRestart(IGSequentialRestart aRestart) {
    }

    protected void visitReport(IGSequentialReport aReport) {
    }

    protected void visitCall(IGSequentialProcedureCall aCall) {
    }

    protected void visitNext(IGSequentialNext aNext) {
    }

    protected void visitLoop(IGSequentialLoop aLoop) {
    }

    protected void visitIf(IGSequentialIf aIf) {
    }

    protected void visitExit(IGSequentialExit aExit) {
    }

    protected void visitAssert(IGSequentialAssert aAssert) {
    }

    protected void visitAssignment(IGSequentialAssignment aAssignment) {
    }


    protected void visitOperation(IGOperation aOperation) {
        fOperationVisitor.visitOperation(aOperation);
    }
}

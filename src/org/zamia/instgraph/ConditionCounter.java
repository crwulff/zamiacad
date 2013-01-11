package org.zamia.instgraph;

import org.zamia.SourceFile;
import org.zamia.SourceLocation;
import org.zamia.instgraph.interpreter.IGInterpreterCode;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import static org.zamia.instgraph.IGOperationBinary.BinOp;
import static org.zamia.instgraph.IGSubProgram.IGBuiltin;

/**
 * @author Anton Chepurov
 */
public class ConditionCounter extends IGDefaultSequentialStatementVisitor {

    public ConditionCounter() {
        super(new ConditionVisitor());
    }

    public int getNumConditions() {
        return getOpVisitor().fConditions.getNumConditions();
    }

    public int getNumConditionsInRange(String aPath, int aStart, int aEnd) {
        return getOpVisitor().fConditions.getNumConditionsInRange(aPath, aStart, aEnd);
    }

    private ConditionVisitor getOpVisitor() {
        return (ConditionVisitor) fOperationVisitor;
    }

    @Override
    protected void visitIf(IGSequentialIf aIf) {

        IGOperation cond = aIf.getCond();

        visitOperation(cond);

        visit(aIf.getThenSOS());
        if (aIf.getElseSOS() != null) {
            visit(aIf.getElseSOS());
        }
    }

    public static class Conditions {

        private final TreeMap<SourceFile, TreeMap<SourceLocation, IGOperation>> fConditionsBySF = new TreeMap<SourceFile, TreeMap<SourceLocation, IGOperation>>();

        private void add(IGOperation aConditionOperation, SourceLocation aLoc) {

            TreeMap<SourceLocation, IGOperation> map = fConditionsBySF.get(aLoc.fSF);
            if (map == null) {
                map = new TreeMap<SourceLocation, IGOperation>();
                fConditionsBySF.put(aLoc.fSF, map);
            }
            map.put(aLoc, aConditionOperation);
        }

        public int getNumConditions() {
            int count = 0;
            for (TreeMap<SourceLocation, IGOperation> maps : fConditionsBySF.values()) {
                count += maps.size();
            }
            return count;
        }

        public int getNumConditionsInRange(String aPath, int aStart, int aEnd) {

            SourceFile sf = new SourceFile(new File(aPath), aPath);

            TreeMap<SourceLocation, IGOperation> map = fConditionsBySF.get(sf);
            if (map == null) {
                return 0;
            }

            int count = 0;
            for (SourceLocation loc : map.keySet()) {
                if (loc.fLine >= aStart && loc.fLine <= aEnd) {
                    count++;
                }
                if (loc.fLine > aEnd) {
                    break;
                }
            }

            return count;
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();

            for (Map.Entry<SourceFile, TreeMap<SourceLocation, IGOperation>> entry : fConditionsBySF.entrySet()) {
                SourceFile file = entry.getKey();
                entry.getValue();

                b.append(file).append(":\n");

                for (Map.Entry<SourceLocation, IGOperation> entry2 : entry.getValue().entrySet()) {
                    SourceLocation loc = entry2.getKey();
                    IGOperation operation = entry2.getValue();

                    b.append(String.format("%5s:%3s   %s\n", loc.fLine, loc.fCol, operation.toHRString()));
                }
            }

            return b.toString();
        }
    }

    private static class ConditionVisitor extends IGDefaultOperationVisitor {

        private final Conditions fConditions = new Conditions();

        @Override
        protected void visitBinary(IGOperationBinary aBinary) {
            IGOperation a = aBinary.getA();
            IGOperation b = aBinary.getB();
            BinOp binOp = aBinary.getBinOp();

            if (isRelational(binOp)) {

                fConditions.add(aBinary, aBinary.computeSourceLocation());
            }

            visitOperation(a);
            visitOperation(b);
        }


        @Override
        protected void visitSubprogram(IGOperationInvokeSubprogram aSubprogram) {

            IGSubProgram sub = aSubprogram.getSub();

            if (isRelational(sub)) {

                fConditions.add(aSubprogram, aSubprogram.computeOpSourceLocation());
            }

            int numMappings = aSubprogram.getNumMappings();
            for (int i = 0; i < numMappings; i++) {

                IGMapping mapping = aSubprogram.getMapping(i);

                visitOperation(mapping.getActual());
            }

            IGSequenceOfStatements code = sub.getCode();
            if (code != null) {
                fSeqStmtVisitor.visit(code);
            }
        }

    }

    public static boolean isRelational(BinOp binOp) {
        switch (binOp) {
            case EQUAL:
            case LESSEQ:
            case LESS:
            case GREATER:
            case GREATEREQ:
            case NEQUAL:
            case AND:
            case NAND:
            case OR:
            case NOR:
            case XOR:
            case XNOR:
                return true;
            default:
                return false;
        }
    }

    public static boolean isRelational(IGBuiltin builtin) {
        switch (builtin) {
            case SCALAR_EQUALS:
            case SCALAR_GREATER:
            case SCALAR_GREATEREQ:
            case SCALAR_LESS:
            case SCALAR_LESSEQ:
            case SCALAR_NEQUALS:
            case BOOL_AND:
            case BOOL_NAND:
            case BOOL_NOR:
            case BOOL_OR:
            case BOOL_XNOR:
            case BOOL_XOR:
            case BOOL_NOT:
            case BIT_NOT:
            case BIT_AND:
            case BIT_NAND:
            case BIT_NOR:
            case BIT_OR:
            case BIT_XNOR:
            case BIT_XOR:
            case ARRAY_NOT:
            case ARRAY_EQUALS:
            case ARRAY_NEQUALS:
            case ARRAY_GREATER:
            case ARRAY_GREATEREQ:
            case ARRAY_LESS:
            case ARRAY_LESSEQ:
                return true;
            default:
                return false;
        }
    }

    public static boolean isRelational(IGSubProgram aSub) {

        IGInterpreterCode code = aSub.getInterpreterCode();
        if (code == null) {
            IGBuiltin bi = aSub.getBuiltin();
            return bi != null && isRelational(bi);
        } else {
            return aSub.isFunction() && aSub.getReturnType().isBool();
        }
    }
}

package org.zamia.instgraph.sim.ref;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.math.BigInteger;

import org.zamia.ErrorReport;
import org.zamia.SourceLocation;
import org.zamia.ZamiaException;
import org.zamia.instgraph.IGContainer;
import org.zamia.instgraph.IGObject;
import org.zamia.instgraph.IGOperationLiteral;
import org.zamia.instgraph.IGRange;
import org.zamia.instgraph.IGStaticValue;
import org.zamia.instgraph.IGStaticValueBuilder;
import org.zamia.instgraph.IGSubProgram;
import org.zamia.instgraph.IGType;
import org.zamia.instgraph.IGTypeStatic;
import org.zamia.instgraph.interpreter.IGInterpreterRuntimeEnv;
import org.zamia.instgraph.interpreter.IGObjectDriver;
import org.zamia.vhdl.ast.VHDLNode;

/**
 * @author Anton Chepurov
 */
public class IGFileDriver extends IGObjectDriver {

	private IGStaticValue fLineNr;

	private String fBasePath;

	public IGFileDriver(String aId, IGObject.OIDir aDir, IGObject.IGObjectCat aCat, IGObjectDriver aParent, IGTypeStatic aType, SourceLocation aLocation) throws ZamiaException {
		super(aId, aDir, aCat, aParent, aType, aLocation);
	}

	public void setBasePath(String aBasePath) {
		fBasePath = aBasePath;
	}

	public IGStaticValue readLine(IGSubProgram aSub, IGInterpreterRuntimeEnv aRuntime, SourceLocation aLocation, VHDLNode.ASTErrorMode aErrorMode, ErrorReport aReport) throws ZamiaException {

		File file = getFile(aLocation);

		String line = readNextLine(file, aSub, aLocation);

		if (line == null) {
			throw new ZamiaException("TEXTIO : Read past end of file \"" + file.getName() + "\".", aLocation);
		}

		// store line nr to read next time
		IGContainer container = aSub.getContainer();
		IGTypeStatic intType = container.findIntType().computeStaticType(aRuntime, aErrorMode, aReport);
		BigInteger lineNr = fLineNr == null ? BigInteger.ONE : fLineNr.getNum().add(BigInteger.ONE);
		fLineNr = new IGStaticValueBuilder(intType, null, aLocation).setNum(lineNr).buildConstant();

		// construct OperationLiteral
		IGType stringType = container.findStringType();
		IGTypeStatic idxType = stringType.getIndexType().computeStaticType(aRuntime, aErrorMode, aReport);
		IGStaticValue left = idxType.getStaticLeft(aLocation);
		IGStaticValue right = new IGStaticValueBuilder(left, aLocation).setNum(new BigInteger("" + line.length())).buildConstant();
		IGStaticValue ascending = container.findTrueValue();

		IGRange range = new IGRange(left, right, ascending, aLocation, aSub.getZDB());
		stringType = stringType.createSubtype(range, aRuntime, aLocation);

		IGOperationLiteral lineOL = new IGOperationLiteral(line.toUpperCase(), stringType, aLocation);

		return lineOL.computeStaticValue(aRuntime, aErrorMode, aReport);
	}

	public boolean isEOF(IGSubProgram aSub, SourceLocation aLocation) throws ZamiaException {

		File file = getFile(aLocation);

		String line = readNextLine(file, aSub, aLocation);

		return line == null;
	}

	public void writeLine(IGStaticValue aValue, SourceLocation aLocation) throws ZamiaException {

		File file = getFile(aLocation);

		BufferedWriter writer = null;
		try {

			writer = new BufferedWriter(new FileWriter(file, true));

			writer.append(aValue.toString()); // todo: toString() ???
			writer.newLine();

		} catch (FileNotFoundException e) {
			throw new ZamiaException("File to write to is not found: " + file.getAbsolutePath(), aLocation);
		} catch (IOException e) {
			throw new ZamiaException("Error while writing to file " + file.getAbsolutePath() + ":\n" + e.getMessage(), aLocation);
		} finally {
			try {
				if (writer != null) { // always close the writer
					writer.close();
				}
			} catch (IOException e) {/* do nothing */}
		}

	}

	private String readNextLine(File aFile, IGSubProgram aSub, SourceLocation aLocation) throws ZamiaException {

		String line;
		LineNumberReader reader = null;
		try {

			reader = createReader(aFile);

			line = reader.readLine();

		} catch (FileNotFoundException e) {
			throw new ZamiaException("File " + aFile.getAbsolutePath() + " not found while executing " + aSub, aLocation);
		} catch (IOException e) {
			throw new ZamiaException("Error while reading file " + aFile.getAbsolutePath() + ":\n" + e.getMessage(), aLocation);
		} finally {
			close(reader);
		}

		return line;
	}

	private File getFile(SourceLocation aLocation) throws ZamiaException {

		IGStaticValue v = getValue(aLocation);

		String filePath = v.getId();

		return new File(fBasePath + File.separator + filePath);
	}

	private LineNumberReader createReader(File aFile) throws IOException {

		LineNumberReader reader = new LineNumberReader(new FileReader(aFile));

		int lineToRead = fLineNr == null ? 0 : fLineNr.getInt();

		for (int i = 0; i < lineToRead; i++) {
			reader.readLine();
		}

		return reader;
	}

	private static void close(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException e) {/* do nothing */}
	}

}

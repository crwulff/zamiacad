package org.zamia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.zamia.util.HashSetArray;


/** 
 * These tests must succeed but fail, unfortunately.
 * 
 * I have grouped the tests but HierarchicalContextRunner support is still not perfect in Eclipse
 * http://github.com/bechte/junit-hierarchicalcontextrunner/issues/6#issuecomment-32127032
 * You can run only all tests at once and to re-run, you must Run/Debug a @Test method in the top class. 
 * 
 * */

@RunWith(de.bechte.junit.runners.context.HierarchicalContextRunner.class)
public class FailedTestsToPass extends BasicTest {
	
	private String computePath(String path) {
		return "examples/failedByZamia/" + path;
	}
	
	protected void runFailure(String path, int arg) throws Exception {
		runTest(computePath(path), arg);
	}
	
	protected void runFailure(String path, int arg, int arg2) throws Exception {
		runTest(computePath(path), arg, arg2);
	}
	
	protected void runFailure(String path, int arg, ErrorChecker errorChecker) throws Exception {
		runTest(computePath(path), "BuildPath.txt", arg, errorChecker);
	}
	
	@Test
	public void t1_zOverrides1Sim() throws Exception {

		runFailure("1.Zoverrides1Sim", 1, 40);
	}

	// The class had to be static but HierarchicalContextRunner does not support statics, 
	// https://github.com/bechte/junit-hierarchicalcontextrunner/issues/4 
	//
	public class t2_Names { 
		@Test
		public void t2_selectedPrefix() throws Exception {
			runFailure("2.names/1.prefix", 1, 1);
		}
		
		@Test
		public void t2_indexedSuffix() throws Exception {
			runFailure("2.names/2.suffix", 1, 1);
		}
		
		
	}
	@Test 
	public void t3_charToBitVector() throws Exception {
		runFailure("3.charToBitVector", 1);
	}
	
	
	public class t4_Instantiation {
		@Test
		public void t4_instActualConstrainedPort() throws Exception {
			runFailure("4.inst/1.actual-constrainedPort", 2);
		}
		
		@Test
		public void t4_instUnmatchedPortWidth() throws Exception {
			runFailure("4.inst/2.unmatchedPortWidth", 2, 100);
		}
		
		@Test
		public void t4_instSelfInstantiation() throws Exception {
			runFailure("4.inst/3.self-instantiation", 2, new ErrorChecker() {
				public void handle() {
					int nErr = fZPrj.getERM().getNumErrors();
					assertEquals("Got wrong number of errors", 2, nErr);

					assertContains(0, "EntityInstantiation: Couldn't find 'UNIMPLEMENTED_ENTITY'");
					assertContains(1, "Architecture not found for WORK.UNIMPLEMENTED_ENTITY");
				}
			});
		}

		@Test
		public void t6_compInstGeneric() throws Exception {
			runFailure("6.componentInstGeneric", 2, new ErrorChecker() {
				public void handle() {
					int nErr = fZPrj.getERM().getNumErrors();
					assertEquals("Got wrong number of errors", 1, nErr);
					assertContains(0, "u1 : entity ENTITY_INSTANCE_MANDATORY: generic 'actual' is not specified");
				}
			});
			
		}
	}
	
	@Test 
	//Fails to sustain Std type overriding because they are
	// not "semantically resolved" and looked up by string name
	// instead.
	public void t7_buildinTypes() throws Exception {
		runFailure("7.overriding_builtin_types", 1);
	}
	
	@Test 
	public void t8_attributes() throws Exception {
		runFailure("8.attributes", 1);
	}

	@Test 
	public void t9_events() throws Exception {
		runFailure("9.events", 7, 100);
	}

	@Test 
	public void t10_operations() throws Exception {
		runFailure("10.operations", 2);
	}

	@Test 
	public void t11_aggregates() throws Exception {
		runFailure("11.aggregates", 2);
	}

	public class t12_IO {
		@Test
		public void t12_std() throws Exception {
			runFailure("12.io/std", 1);
		}
		
		@Test
		public void t12_binary() throws Exception {
			runFailure("12.io/binary", 1);
		}
		
		@Test
		public void t12_fileName() throws Exception {
			runFailure("12.io/fname", 1);
		}
		
	}
	
	@Test
	public void t13_alias() throws Exception {
		runFailure("13.alias", 1, new ErrorChecker() {
			public void handle() {
				int nErr = fZPrj.getERM().getNumErrors();
				assertEquals("Got wrong number of errors", 1, nErr);
				assertContains(0, "alias YY range mismatches that of aliased object XX");
			}
		});
	}
	
	@Test
	public void t14_accessType() throws Exception {
		runFailure("14.access_type", 1);
	}
	
	@Test
	public void t15_incrementalBuild() throws Exception {
		// Demonstrates that incremental works only for 
		// entities/architecutres but not for packages.
		
		
		String path = "15.incremental_build";
		runFailure(path, 1);
		ZamiaProjectBuilder builder = fZPrj.getBuilder();
		HashSetArray<SourceFile> vhdl = new HashSetArray<>();
		File incremental = new File(computePath(path + "/incremental.vhdl-"));
		File original = new File(computePath(path + "/incremental.vhdl")); 
		File temp = new File(computePath(path + "/temp"));
		
		// Rename 'incremantal' -> 'original' because Zamia won't compile
		// re-elaborate known design units declared in another source file.
		// This is kind of opposite of VHDL ideology where last analyzed is 
		// valid.
		original.renameTo(temp);
		try {
			incremental.renameTo(original);
			try {
				
				String[] toCaputre = {"HELLO FROM INCREMENTAL PKG", "HELLO FROM INCREMENTAL ARCH"};
				CaputreLogMessagesContaining appender = new CaputreLogMessagesContaining(toCaputre);
				ZamiaLogger.getInstance().getLogger().addAppender(appender);
				
				try {
					vhdl.add(new SourceFile(original));
					builder.build(false, false, vhdl);
				} finally {
					ZamiaLogger.getInstance().getLogger().removeAppender(appender);
				}
				
				for (String str : appender.toCaputre)
					assertTrue("log fails to contain \"" + str + "\"", false);
				
				
			} finally {
				original.renameTo(incremental);
			}
		} finally {
			temp.renameTo(original);
		}
		
	}
	
	@Test
	public void t16_resolution_function() throws Exception {
		runFailure("16.resolution_func", 1);
	}
	
}

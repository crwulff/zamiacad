package org.zamia;

import java.io.File;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Anton Chepurov
 */
public class SourceRangesTest {
	@Test
	public void testSubtract() throws Exception {

		SourceRanges ranges = SourceRanges.createRanges();

		SourceFile sf1 = new SourceFile(new File("asdf1"));
		SourceLocation sl1 = new SourceLocation(sf1, 10, 11);
		SourceFile sf2 = new SourceFile(new File("asdf2"));
		SourceLocation sl2 = new SourceLocation(sf2, 1, 2);
		SourceFile sf3 = new SourceFile(new File("asdf3"));
		SourceLocation sl3 = new SourceLocation(sf3, 13, 12);
		SourceFile sf4 = new SourceFile(new File("asdf4"));
		SourceLocation sl4 = new SourceLocation(sf4, 132, 13);
		SourceFile sf5 = new SourceFile(new File("asdf5"));
		SourceLocation sl5 = new SourceLocation(sf5, 198, 14);
		ranges.add(sl1, 123);
		ranges.add(sl2, 122);
		ranges.add(sl3, 121);
		ranges.add(sl4, 120);
		ranges.add(sl5, 119);

		SourceRanges subtr = SourceRanges.createRanges();

		subtr.add(sl4, 11);
		subtr.add(sl5, 11);


		SourceRanges result = ranges.subtract(subtr);

		assertNotNull(result);

		assertTrue(result.hasFile(sf1));
		assertTrue(result.hasFile(sf2));
		assertTrue(result.hasFile(sf3));
		assertTrue(!result.hasFile(sf4));
		assertTrue(!result.hasFile(sf5));

		assertTrue(result.getSourceRanges(sf1).hasLine(9));
		assertTrue(result.getSourceRanges(sf2).hasLine(0));
		assertTrue(result.getSourceRanges(sf3).hasLine(12));

		assertEquals(123, result.getSourceRanges(sf1).getCount(9));
		assertEquals(122, result.getSourceRanges(sf2).getCount(0));
		assertEquals(121, result.getSourceRanges(sf3).getCount(12));

	}
}

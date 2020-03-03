package com.graham.passvaultplus.model.core;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.Date;

public class PvpRecordComparatorTest {

	PvpField dateField1 = new PvpField("Test1", PvpField.TYPE_DATE);

	@Test
	public void testCompareDates() {
		PvpRecordComparator recCmp = new PvpRecordComparator(dateField1, true);

		assertEquals(0, recCmp.compareDates((Date)null, (Date)null));
		assertEquals(0, recCmp.compareDates("", ""));
		assertEquals(0, recCmp.compareDates("Aug 1, 2020", "Aug 1, 2020"));
		assertEquals(0, recCmp.compareDates("ert", "ert"));

		assertEquals(-1, recCmp.compareDates("Mar 2, 2020", "Aug 1, 2020"));
		assertEquals(1, recCmp.compareDates("Aug 1, 2020", "Mar 2, 2020"));
		assertEquals(-1, recCmp.compareDates("ert", "fgh"));
		assertEquals(1, recCmp.compareDates("fgh", "ert"));

		assertEquals(-1, recCmp.compareDates("Mar 2, 2020", "ert"));
		assertEquals(-1, recCmp.compareDates("Mar 2, 2020", "abc"));

		assertEquals(-1, recCmp.compareDates("Mar 2, 2020bad", "Aug 1, 2020bad"));
		assertEquals(1, recCmp.compareDates("Aug 1, 2020bad", "Mar 2, 2020bad"));
	}
}

/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class AppUtilTest {

		@Test
		public void testParseHourlyTimeStamp_1() {
				Date d;
				d = AppUtil.parseHourlyTimeStamp("-2019-8-28-7");
				assertEquals("Wed Aug 28 07:00:00 CDT 2019", d.toString());

				d = AppUtil.parseHourlyTimeStamp("a-2019-8-28-7a");
				assertEquals("Wed Aug 28 07:00:00 CDT 2019", d.toString());

				d = AppUtil.parseHourlyTimeStamp("asdf-asdf-2019-8-28-7-asdf-asdf");
				assertEquals("Wed Aug 28 07:00:00 CDT 2019", d.toString());

				//d = AppUtil.parseHourlyTimeStamp("-2019-8-28-71");
				//assertEquals("Wed Aug 28 07:00:00 CDT 2019", d.toString());
		}

		@Test
		public void testParseHourlyTimeStamp_2() {
				Date d = AppUtil.parseHourlyTimeStamp("-2019-8-28-19");
				assertEquals("Wed Aug 28 19:00:00 CDT 2019", d.toString());
		}

		@Test
		public void testParseHourlyTimeStamp_3() {
				Date d = AppUtil.parseHourlyTimeStamp("pvp-data-2019-1-15-14.bmn");
				assertEquals("Tue Jan 15 14:00:00 CST 2019", d.toString());
		}

		@Test
		public void testGetHourlyTimeStamp() {
				Calendar c = Calendar.getInstance();
				c.set(2019, Calendar.AUGUST, 28, 7, 0, 0);
				String ts = AppUtil.getHourlyTimeStamp(c.getTime());
				assertEquals("-2019-8-28-7", ts);
		}

}
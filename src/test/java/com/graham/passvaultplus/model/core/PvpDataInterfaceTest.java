/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.graham.passvaultplus.PvpContext;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PvpDataInterfaceTest {
	
	interface RecGetter {
		List<PvpRecord> get();
	}
	
	PvpContext context;
	
	PvpType type1;
	PvpRecord rec1;
	PvpRecord rec2;
	PvpRecord rec1modified;
	
	private List<PvpType> createTestTypes() {
		List<PvpType> types = new ArrayList<>();
		types.add(type1);
		return types;
	}
	
	private List<PvpRecord> createTestRecords() {
		List<PvpRecord> records = new ArrayList<>();
		records.add(rec1);
		return records;
	}
	
	private List<PvpRecord> createTestRecords2() {
		List<PvpRecord> records = new ArrayList<>();
		records.add(rec2);
		return records;
	}
	
	private List<PvpRecord> createTestRecordsModified() {
		List<PvpRecord> records = new ArrayList<>();
		records.add(rec1modified);
		return records;
	}
	
	@Before
	public void setUpStuff() {
		context = mock(PvpContext.class);
		
		type1 = new PvpType();
		type1.setName("Account");
		type1.addField(new PvpField("Account Name", PvpField.TYPE_STRING));
		type1.addField(new PvpField("Username", PvpField.TYPE_STRING));
		type1.addField(new PvpField("Password", PvpField.TYPE_STRING));
		type1.setToStringCode("Account Name");
		
		rec1 = new PvpRecord(type1);
		rec1.setId(8);
		rec1.setCreationDate(new Date());
		rec1.setModificationDate(new Date());
		rec1.setCustomField("Account Name", "Email");
		rec1.setCustomField("Username", "joe123");
		rec1.setCustomField("Password", "secret99");
		
		rec2 = new PvpRecord(type1);
		rec2.setId(6);
		rec2.setCreationDate(new Date());
		rec2.setModificationDate(new Date());
		rec2.setCustomField("Account Name", "Work Email");
		rec2.setCustomField("Username", "joe-work");
		rec2.setCustomField("Password", "password123");
		
		rec1modified = new PvpRecord(type1);
		rec1modified.setId(8);
		rec1modified.setCreationDate(rec1.getCreationDate());
		rec1modified.setModificationDate(new Date(new Date().getTime() + 1000)); // make the modification 1 second later
		rec1modified.setCustomField("Account Name", "Email");
		rec1modified.setCustomField("Username", "joe123");
		rec1modified.setCustomField("Password", "newsecret99"); // updated password
	}
	
	private void testMergeBasic1Help(RecGetter rg2) {
		List<PvpType> types1 = createTestTypes();
		List<PvpRecord> records1 = createTestRecords();
		int maxID1 = 9;
		PvpDataInterface di1 = new PvpDataInterface(context, types1, records1, maxID1);
		
		List<PvpType> types2 = createTestTypes();
		List<PvpRecord> records2 = rg2.get();
		int maxID2 = 9;
		PvpDataInterface di2 = new PvpDataInterface(context, types2, records2, maxID2);
		
		boolean result = di1.mergeData(di2);
		assertFalse(result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(9, di1.getMaxId());
		assertEquals(1, di1.getRecordCount());
		{
			PvpRecord r = di1.getRecord(8);
			assertEquals("Email", r.getCustomField("Account Name"));
			assertEquals("joe123", r.getCustomField("Username"));
			assertEquals("secret99", r.getCustomField("Password"));
		}
	}
	
	/**
	 * Test where the two databases are identical
	 */
	@Test
	public void testMergeBasic1() {
		RecGetter rg = () -> {
			return createTestRecords();
		};
		testMergeBasic1Help(rg);
	}
	
	/**
	 * Test where the two databases are identical - using copy
	 */
	@Test
	public void testMergeBasic1b() {
		RecGetter rg = () -> {
			List<PvpRecord> records2 = new ArrayList<>();
			PvpRecord rec1Copy = new PvpRecord(type1);
			rec1.copyTo(rec1Copy);
			rec1Copy.setId(8);
			records2.add(rec1Copy);
			return records2;
		};
		testMergeBasic1Help(rg);
	}
	
	/**
	 * same except using copy, and changing creation date
	 */
	@Test
	public void testMergeBasic1c() {
		RecGetter rg = () -> {
			List<PvpRecord> records2 = new ArrayList<>();
			PvpRecord rec1Copy = new PvpRecord(type1);
			rec1.copyTo(rec1Copy);
			rec1Copy.setId(8);
			rec1Copy.setCreationDate(new Date(new Date().getTime() + 60000));
			records2.add(rec1Copy);
			return records2;
		};
		testMergeBasic1Help(rg);
	}
	
	/**
	 * same except using copy, and changing title - use new title
	 */
	@Test
	public void testMerge_newTitle() {
		RecGetter rg2 = () -> {
			List<PvpRecord> records2 = new ArrayList<>();
			PvpRecord rec1Copy = new PvpRecord(type1);
			rec1.copyTo(rec1Copy);
			rec1Copy.setId(8);
			rec1Copy.setCustomField("Account Name", "Email 2");
			rec1Copy.setModificationDate(new Date(new Date().getTime() + 60000));
			records2.add(rec1Copy);
			return records2;
		};
		
		
		List<PvpType> types1 = createTestTypes();
		List<PvpRecord> records1 = createTestRecords();
		int maxID1 = 9;
		PvpDataInterface di1 = new PvpDataInterface(context, types1, records1, maxID1);
		
		List<PvpType> types2 = createTestTypes();
		List<PvpRecord> records2 = rg2.get();
		int maxID2 = 9;
		PvpDataInterface di2 = new PvpDataInterface(context, types2, records2, maxID2);
		
		boolean result = di1.mergeData(di2);
		assertTrue(result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(9, di1.getMaxId());
		assertEquals(1, di1.getRecordCount());
		{
			PvpRecord r = di1.getRecord(8);
			assertEquals("Email 2", r.getCustomField("Account Name"));
			assertEquals("joe123", r.getCustomField("Username"));
			assertEquals("secret99", r.getCustomField("Password"));
		}
	}
	
	/**
	 * same except using copy, and changing title - keep main title
	 */
	@Test
	public void testMergeBasic_keepMainTitle() {
		RecGetter rg = () -> {
			List<PvpRecord> records2 = new ArrayList<>();
			PvpRecord rec1Copy = new PvpRecord(type1);
			rec1.copyTo(rec1Copy);
			rec1Copy.setId(8);
			rec1Copy.setCustomField("Account Name", "Email 2");
			rec1Copy.setModificationDate(new Date(new Date().getTime() - 60000));
			records2.add(rec1Copy);
			return records2;
		};
		testMergeBasic1Help(rg);
	}
	
	/**
	 * same except using copy, changing id, same title
	 */
	@Test
	public void testMerge_diffId() {
		RecGetter rg = () -> {
			List<PvpRecord> records2 = new ArrayList<>();
			PvpRecord rec1Copy = new PvpRecord(type1);
			rec1.copyTo(rec1Copy);
			rec1Copy.setId(3);
			records2.add(rec1Copy);
			return records2;
		};
		testMergeBasic1Help(rg);
	}
	
	/**
	 * Test where the main has a higher maxID
	 */
	@Test
	public void testMergeBasic2() {
		List<PvpType> types1 = createTestTypes();
		List<PvpRecord> records1 = createTestRecords();
		int maxID1 = 9;
		PvpDataInterface di1 = new PvpDataInterface(context, types1, records1, maxID1);
		
		List<PvpType> types2 = createTestTypes();
		List<PvpRecord> records2 = createTestRecords();
		int maxID2 = 15;
		PvpDataInterface di2 = new PvpDataInterface(context, types2, records2, maxID2);
		
		boolean result = di1.mergeData(di2);
		assertFalse(result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(9, di1.getMaxId());
		assertEquals(1, di1.getRecordCount());
	}
	
	/**
	 * Test where the import has a higher maxID
	 */
	@Test
	public void testMergeBasic3() {
		List<PvpType> types1 = createTestTypes();
		List<PvpRecord> records1 = createTestRecords();
		int maxID1 = 15;
		PvpDataInterface di1 = new PvpDataInterface(context, types1, records1, maxID1);
		
		List<PvpType> types2 = createTestTypes();
		List<PvpRecord> records2 = createTestRecords();
		int maxID2 = 9;
		PvpDataInterface di2 = new PvpDataInterface(context, types2, records2, maxID2);
		
		boolean result = di1.mergeData(di2);
		assertFalse(result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(15, di1.getMaxId());
		assertEquals(1, di1.getRecordCount());
	}
	
	/**
	 * Test where the import has a different record
	 */
	@Test
	public void testMergeBasic4() {
		List<PvpType> types1 = createTestTypes();
		List<PvpRecord> records1 = createTestRecords();
		int maxID1 = 9;
		PvpDataInterface di1 = new PvpDataInterface(context, types1, records1, maxID1);
		
		List<PvpType> types2 = createTestTypes();
		List<PvpRecord> records2 = createTestRecords2();
		int maxID2 = 15;
		PvpDataInterface di2 = new PvpDataInterface(context, types2, records2, maxID2);
		
		boolean result = di1.mergeData(di2);
		assertTrue(result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(10, di1.getMaxId());
		assertEquals(2, di1.getRecordCount());
		{
			PvpRecord r = di1.getRecord(8);
			assertEquals("Email", r.getCustomField("Account Name"));
			assertEquals("joe123", r.getCustomField("Username"));
			assertEquals("secret99", r.getCustomField("Password"));
		}
		{
			PvpRecord r2 = di1.getRecord(10);
			assertEquals("Work Email", r2.getCustomField("Account Name"));
			assertEquals("joe-work", r2.getCustomField("Username"));
			assertEquals("password123", r2.getCustomField("Password"));
		}
	}
	
	/**
	 * Test where the import record was modified
	 */
	@Test
	public void testMergeBasic5() {
		List<PvpType> types1 = createTestTypes();
		List<PvpRecord> records1 = createTestRecords();
		int maxID1 = 15;
		PvpDataInterface di1 = new PvpDataInterface(context, types1, records1, maxID1);
		
		List<PvpType> types2 = createTestTypes();
		List<PvpRecord> records2 = createTestRecordsModified();
		int maxID2 = 9;
		PvpDataInterface di2 = new PvpDataInterface(context, types2, records2, maxID2);
		
		boolean result = di1.mergeData(di2);
		assertTrue(result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(15, di1.getMaxId());
		assertEquals(1, di1.getRecordCount());
		{
			PvpRecord r = di1.getRecord(8);
			assertEquals("Email", r.getCustomField("Account Name"));
			assertEquals("joe123", r.getCustomField("Username"));
			assertEquals("newsecret99", r.getCustomField("Password"));
		}
	}
	
	/**
	 * Test where the main record was modified
	 */
	@Test
	public void testMergeBasic6() {
		List<PvpType> types1 = createTestTypes();
		List<PvpRecord> records1 = createTestRecordsModified();
		int maxID1 = 15;
		PvpDataInterface di1 = new PvpDataInterface(context, types1, records1, maxID1);
		
		List<PvpType> types2 = createTestTypes();
		List<PvpRecord> records2 = createTestRecords();
		int maxID2 = 9;
		PvpDataInterface di2 = new PvpDataInterface(context, types2, records2, maxID2);
		
		boolean result = di1.mergeData(di2);
		assertFalse(result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(15, di1.getMaxId());
		assertEquals(1, di1.getRecordCount());
		{
			PvpRecord r = di1.getRecord(8);
			assertEquals("Email", r.getCustomField("Account Name"));
			assertEquals("joe123", r.getCustomField("Username"));
			assertEquals("newsecret99", r.getCustomField("Password"));
		}
	}
	
	/**
	 * Test where the main record was modified, and title was changed
	 */
	/*
	@Test
	public void testMergeBasic7() {
		List<PvpType> types1 = createTestTypes();
		List<PvpRecord> records1 = createTestRecordsModified();
		records1.get(0).setCustomField("Account Name", "Email 2");
		int maxID1 = 15;
		PvpDataInterface di1 = new PvpDataInterface(context, types1, records1, maxID1);
		
		List<PvpType> types2 = createTestTypes();
		List<PvpRecord> records2 = createTestRecords();
		int maxID2 = 9;
		PvpDataInterface di2 = new PvpDataInterface(context, types2, records2, maxID2);
		
		boolean result = di1.mergeData(di2);
		assertTrue(result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(16, di1.getMaxId());
		assertEquals(2, di1.getRecordCount());
		{
			PvpRecord r = di1.getRecord(8);
			assertEquals("Email 2", r.getCustomField("Account Name"));
			assertEquals("joe123", r.getCustomField("Username"));
			assertEquals("newsecret99", r.getCustomField("Password"));
		}
		{
			PvpRecord r2 = di1.getRecord(16);
			assertEquals("Email", r2.getCustomField("Account Name"));
			assertEquals("joe123", r2.getCustomField("Username"));
			assertEquals("secret99", r2.getCustomField("Password"));
		}
	}
	*/
	
	/**
	 * test where merged has different records - but dont delete because maxId is 1
	 */
	@Test
	public void testMergeBasic_delete1() {
		PvpDataInterface di1;
		{
			List<PvpType> types1 = createTestTypes();
			rec1.setId(1);
			rec2.setId(2);
			List<PvpRecord> records1 = new ArrayList<>();
			records1.add(rec1);
			records1.add(rec2);
			int maxID1 = 2;
			di1 = new PvpDataInterface(context, types1, records1, maxID1);
		}
		
		PvpDataInterface di2;
		{
			List<PvpType> types2 = createTestTypes();
			List<PvpRecord> records2 = new ArrayList<>();
			records2.add(rec1);
			int maxID2 = 1;
			di2 = new PvpDataInterface(context, types2, records2, maxID2);
		}
		
		boolean result = di1.mergeData(di2);
		assertFalse(result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(2, di1.getMaxId());
		assertEquals(2, di1.getRecordCount());
	}
	

	/**
	 * test where merged has different records - but dont delete because maxId is 1
	 */
	@Test
	public void testMergeBasic_delete2() {
		PvpDataInterface di1;
		{
			List<PvpType> types1 = createTestTypes();
			rec1.setId(1);
			rec2.setId(2);
			List<PvpRecord> records1 = new ArrayList<>();
			records1.add(rec1);
			records1.add(rec2);
			int maxID1 = 2;
			di1 = new PvpDataInterface(context, types1, records1, maxID1);
		}
		
		PvpDataInterface di2;
		{
			List<PvpType> types2 = createTestTypes();
			List<PvpRecord> records2 = new ArrayList<>();
			records2.add(rec1);
			int maxID2 = 2; // the maxId is 2
			di2 = new PvpDataInterface(context, types2, records2, maxID2);
		}
		
		boolean result = di1.mergeData(di2);
		assertFalse(result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(2, di1.getMaxId());
		assertEquals(2, di1.getRecordCount());
	}

	

	/**
	 * test delete
	 */
	@Test
	public void testMergeBasic_delete3() {
		PvpDataInterface di1;
		{
			List<PvpType> types1 = createTestTypes();
			rec1.setId(1);
			rec2.setId(2);
			List<PvpRecord> records1 = new ArrayList<>();
			records1.add(rec1);
			records1.add(rec2);
			int maxID1 = 2;
			di1 = new PvpDataInterface(context, types1, records1, maxID1);
		}
		
		PvpDataInterface di2;
		{
			List<PvpType> types2 = createTestTypes();
			List<PvpRecord> records2 = new ArrayList<>();
			records2.add(rec2);
			int maxID2 = 2; // the maxId is 2
			di2 = new PvpDataInterface(context, types2, records2, maxID2);
		}
		
		boolean result = di1.mergeData(di2);
		assertFalse(result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(2, di1.getMaxId());
		assertEquals(1, di1.getRecordCount());
		{
			PvpRecord r2 = di1.getRecord(2);
			assertEquals("Work Email", r2.getCustomField("Account Name"));
			assertEquals("joe-work", r2.getCustomField("Username"));
			assertEquals("password123", r2.getCustomField("Password"));
		}
	}
}

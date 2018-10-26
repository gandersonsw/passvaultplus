/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.graham.passvaultplus.PvpContext;

import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PvpDataMergerTest {

	PvpContext context;

	PvpType type1;
	PvpRecord rec1; // in di1
	PvpRecord rec2; // in di1
	PvpRecord rec1Copy; // in di2
	PvpRecord rec2Copy; // in di2

	PvpDataInterface di1;
	PvpDataInterface di2;

	@Before
	public void setUpStuff() {
		context = mock(PvpContext.class);

		type1 = new PvpType();
		type1.setName("Account");
		type1.addField(new PvpField("Account Name", PvpField.TYPE_STRING));
		type1.addField(new PvpField("Username", PvpField.TYPE_STRING));
		type1.addField(new PvpField("Password", PvpField.TYPE_STRING));
		type1.setToStringCode("Account Name");
	}

	private List<PvpType> createTestTypes() {
		List<PvpType> types = new ArrayList<>();
		types.add(type1);
		return types;
	}

	private PvpRecord createRec1() {
		PvpRecord r = new PvpRecord(type1);
		r.setId(11);
		r.setCreationDate(new Date());
		r.setModificationDate(new Date());
		r.setCustomField("Account Name", "Email");
		r.setCustomField("Username", "joe123");
		r.setCustomField("Password", "secret99");
		return r;
	}

	private PvpRecord createRec2() {
		PvpRecord r = new PvpRecord(type1);
		r.setId(12);
		r.setCreationDate(new Date());
		r.setModificationDate(new Date());
		r.setCustomField("Account Name", "Work Email");
		r.setCustomField("Username", "joe-work");
		r.setCustomField("Password", "password123");
		return r;
	}

  static class DISetupParams {
		int maxId = 12;
		boolean addRec1 = true;
		boolean addRec2 = true;
	}
  static class SetupParams {
		DISetupParams di1 = new DISetupParams();
		DISetupParams di2 = new DISetupParams();
	}

	private void setUpBothDI(SetupParams p) {
		PvpRecord r1 = createRec1();
		PvpRecord r2 = createRec2();
		{
			List<PvpType> types1 = createTestTypes();
			List<PvpRecord> records1 = new ArrayList<>();
			if (p.di1.addRec1) { rec1 = r1; records1.add(rec1); }
			if (p.di1.addRec2) { rec2 = r2; records1.add(rec2); }
			di1 = new PvpDataInterface(context, types1, records1, p.di1.maxId);
		}
		{
			List<PvpType> types2 = createTestTypes();
			List<PvpRecord> records2 = new ArrayList<>();
			if (p.di2.addRec1) {
				rec1Copy = new PvpRecord(type1);
				r1.copyTo(rec1Copy);
				rec1Copy.setId(r1.getId());
				records2.add(rec1Copy);
			}
			if (p.di2.addRec2) {
				rec2Copy = new PvpRecord(type1);
				r2.copyTo(rec2Copy);
				rec2Copy.setId(r2.getId());
				records2.add(rec2Copy);
			}
			di2 = new PvpDataInterface(context, types2, records2, p.di2.maxId);
		}
	}

	private void defaultValidate(PvpDataMerger.MergeResultState result) {
		assertEquals(PvpDataMerger.MergeResultState.NO_CHANGE, result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(12, di1.getMaxId());
		assertEquals(2, di1.getRecordCount());
		{
			PvpRecord r = di1.getRecord(11);
			assertEquals("Email", r.getCustomField("Account Name"));
			assertEquals("joe123", r.getCustomField("Username"));
			assertEquals("secret99", r.getCustomField("Password"));
		}
		{
			PvpRecord r = di1.getRecord(12);
			assertEquals("Work Email", r.getCustomField("Account Name"));
			assertEquals("joe-work", r.getCustomField("Username"));
			assertEquals("password123", r.getCustomField("Password"));
		}
	}

	private void printContextInfo() {
		ArgumentCaptor<String> contextInfoCaptor = ArgumentCaptor.forClass(String.class);
		verify(context, atLeastOnce()).notifyInfo(contextInfoCaptor.capture());
		for (String s : contextInfoCaptor.getAllValues()) {
			System.out.println(s);
		}
	}

	/**
	 * Test where the two databases are identical
	 */
	@Test
	public void testMerge_same() {
		setUpBothDI(new SetupParams());

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context).mergeData(di1, di2);

		defaultValidate(result);
	}

	/**
	 * same except changing creation date
	 */
	@Test
	public void testMerge_creationDate() {
		setUpBothDI(new SetupParams());
		rec1Copy.setCreationDate(new Date(new Date().getTime() + 60000));

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context).mergeData(di1, di2);

		defaultValidate(result);
	}

	/**
	 * same except using copy, and changing title - use new title
	 * di1 has the newest data
	 */
	@Test
	public void testMerge_newTitle_1() {
		setUpBothDI(new SetupParams());
		rec1.setModificationDate(new Date(new Date().getTime() + 60000));
		rec1Copy.setCustomField("Account Name", "Email 2");

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context).mergeData(di1, di2);

		assertEquals(PvpDataMerger.MergeResultState.FROM_CHANGED, result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(12, di1.getMaxId());
		assertEquals(2, di1.getRecordCount());
		{
			PvpRecord r = di1.getRecord(11);
			assertEquals("Email", r.getCustomField("Account Name"));
			assertEquals("joe123", r.getCustomField("Username"));
			assertEquals("secret99", r.getCustomField("Password"));
		}
		{
			PvpRecord r = di1.getRecord(12);
			assertEquals("Work Email", r.getCustomField("Account Name"));
			assertEquals("joe-work", r.getCustomField("Username"));
			assertEquals("password123", r.getCustomField("Password"));
		}
	}

	/**
	 * same except using copy, and changing title - use new title
	 * di2 has the newest data
	 */
	@Test
	public void testMerge_newTitle_2() {
		setUpBothDI(new SetupParams());
		rec1Copy.setModificationDate(new Date(new Date().getTime() + 60000));
		rec1Copy.setCustomField("Account Name", "Email 2");

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context).mergeData(di1, di2);

		assertEquals(PvpDataMerger.MergeResultState.TO_CHANGED, result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(12, di1.getMaxId());
		assertEquals(2, di1.getRecordCount());
		{
			PvpRecord r = di1.getRecord(11);
			assertEquals("Email 2", r.getCustomField("Account Name"));
			assertEquals("joe123", r.getCustomField("Username"));
			assertEquals("secret99", r.getCustomField("Password"));
		}
		{
			PvpRecord r = di1.getRecord(12);
			assertEquals("Work Email", r.getCustomField("Account Name"));
			assertEquals("joe-work", r.getCustomField("Username"));
			assertEquals("password123", r.getCustomField("Password"));
		}
	}

	/**
	 * changing id
	 */
	@Test
	public void testMerge_diffId() {
		setUpBothDI(new SetupParams());
		rec1Copy.setId(3);

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context).mergeData(di1, di2);

		defaultValidate(result);
	}

	/**
	 * Test where the "FROM" has a higher maxID
	 */
	 /*
	@Test
	public void testMerge_highId_from() {
		SetupParams p = new SetupParams();
		p.di2.maxId = 18;
		setUpBothDI(p);
		rec1Copy.setId(3);

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context).mergeData(di1, di2);

		assertEquals(PvpDataMerger.MergeResultState.NO_CHANGE, result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(18, di1.getMaxId());
		assertEquals(1, di1.getRecordCount());
	}
	*/

	/**
	 * Test where the "TO" has a higher maxID
	 */
	@Test
	public void testMerge_highId_to() {
		SetupParams p = new SetupParams();
		p.di1.maxId = 18;
		setUpBothDI(p);

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context).mergeData(di1, di2);

		assertEquals(PvpDataMerger.MergeResultState.NO_CHANGE, result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(18, di1.getMaxId());
		assertEquals(2, di1.getRecordCount());
	}

	/**
	 * Test where there is a new record in both datasets
	 */
	@Test
	public void testMerge_new_record_in_each() {
		SetupParams p = new SetupParams();
		p.di1.addRec1 = false;
		p.di2.addRec2 = false;
		p.di2.maxId = 11;
		setUpBothDI(p);

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context).mergeData(di1, di2);
		printContextInfo();
		assertEquals(PvpDataMerger.MergeResultState.BOTH_CHANGED, result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(13, di1.getMaxId());
		assertEquals(2, di1.getRecordCount());
		{
			PvpRecord r = di1.getRecord(13);
			assertEquals("Email", r.getCustomField("Account Name"));
			assertEquals("joe123", r.getCustomField("Username"));
			assertEquals("secret99", r.getCustomField("Password"));
		}
		{
			PvpRecord r2 = di1.getRecord(12);
			assertEquals("Work Email", r2.getCustomField("Account Name"));
			assertEquals("joe-work", r2.getCustomField("Username"));
			assertEquals("password123", r2.getCustomField("Password"));
		}
	}

	/**
	 * test where merged has different records - but dont delete because maxId is 1
	 */
	@Test
	public void testMergeBasic_delete1() {
		SetupParams p = new SetupParams();
		p.di2.maxId = 11;
		p.di2.addRec2 = false;
		setUpBothDI(p);

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context).mergeData(di1, di2);

		assertEquals(PvpDataMerger.MergeResultState.FROM_CHANGED, result); // this is true, because the di2 needs to updated with rec2
		assertEquals(1, di1.getTypes().size());
		assertEquals(12, di1.getMaxId());
		assertEquals(2, di1.getRecordCount());
	}

	/**
	 * test where merged has different records - delete because maxId is 2
	 */
	@Test
	public void testMergeBasic_delete2() {
		SetupParams p = new SetupParams();
		p.di2.addRec2 = false;
		setUpBothDI(p);

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context).mergeData(di1, di2);

		assertEquals(PvpDataMerger.MergeResultState.FROM_CHANGED, result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(12, di1.getMaxId());
		assertEquals(2, di1.getRecordCount()); // TODO this should be 1
	}

	/**
	 * test delete
	 */
	@Test
	public void testMergeBasic_delete3() {
		SetupParams p = new SetupParams();
		p.di2.addRec1 = false;
		setUpBothDI(p);

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context).mergeData(di1, di2);

		// TODO should not be BOTH_CHANGED
		assertEquals(PvpDataMerger.MergeResultState.BOTH_CHANGED, result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(12, di1.getMaxId());
		assertEquals(1, di1.getRecordCount());
		{
			PvpRecord r2 = di1.getRecord(12);
			assertEquals("Work Email", r2.getCustomField("Account Name"));
			assertEquals("joe-work", r2.getCustomField("Username"));
			assertEquals("password123", r2.getCustomField("Password"));
		}
	}

	/**
	 * Test were the record was edited in the TO dataset
	 */
	@Test
	public void testMergeBasic_edit_to() {
		SetupParams p = new SetupParams();
		setUpBothDI(p);
		rec1.setModificationDate(new Date(new Date().getTime() + 1000)); // make the modification 1 second later
		rec1.setCustomField("Password", "newsecret99"); // updated password

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context).mergeData(di1, di2);

		assertEquals(PvpDataMerger.MergeResultState.FROM_CHANGED, result); // the "FROM"" dataset is updated already, just need to update the "TO" dataset
		assertEquals(1, di1.getTypes().size());
		assertEquals(12, di1.getMaxId());
		assertEquals(2, di1.getRecordCount()); // keep both records because the dataset without the record is modified earlier
		{
			PvpRecord r2 = di1.getRecord(11);
			assertEquals("Email", r2.getCustomField("Account Name")); // get the record from di1 because its modification date is later
			assertEquals("joe123", r2.getCustomField("Username"));
			assertEquals("newsecret99", r2.getCustomField("Password"));
		}
	}

  /**
	 * Test were the record was edited in the FROM dataset
	 */
	@Test
	public void testMergeBasic_edit_from() {
		SetupParams p = new SetupParams();
		setUpBothDI(p);
		rec1Copy.setModificationDate(new Date(new Date().getTime() + 1000)); // make the modification 1 second later
		rec1Copy.setCustomField("Password", "newsecret99"); // updated password

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context).mergeData(di1, di2);

		assertEquals(PvpDataMerger.MergeResultState.TO_CHANGED, result); // the "FROM"" dataset is updated already, just need to update the "TO" dataset
		assertEquals(1, di1.getTypes().size());
		assertEquals(12, di1.getMaxId());
		assertEquals(2, di1.getRecordCount()); // keep both records because the dataset without the record is modified earlier
		{
			PvpRecord r2 = di1.getRecord(11);
			assertEquals("Email", r2.getCustomField("Account Name")); // get the record from di2 because its modification date is later
			assertEquals("joe123", r2.getCustomField("Username"));
			assertEquals("newsecret99", r2.getCustomField("Password"));
		}
	}

}

/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpContextUI;

import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PvpDataMergerTest {

	PvpContext context;

	PvpType type1;
	PvpType type2;

	PvpDataInterface di1;
	PvpDataInterface di2;
	PvpRecord di1rec1;
	PvpRecord di1rec2;
	PvpRecord di1rec3; // type 2
	PvpRecord di2rec1;
	PvpRecord di2rec2;
	PvpRecord di2rec3; // type 2

	@Before
	public void setUpStuff() {
		context = new PvpContext(null, null, mock(PvpContextUI.class));

		type1 = new PvpType();
		type1.setName("Account");
		type1.addField(new PvpField("Account Name", PvpField.TYPE_STRING));
		type1.addField(new PvpField("Username", PvpField.TYPE_STRING));
		type1.addField(new PvpField("Password", PvpField.TYPE_STRING));
		type1.setToStringCode("Account Name");
		
		type2 = new PvpType();
		type2.setName("Address");
		type2.addField(new PvpField("Name", PvpField.TYPE_STRING));
		type2.addField(new PvpField("Street", PvpField.TYPE_STRING));
		type2.addField(new PvpField("City", PvpField.TYPE_STRING));
		type2.setToStringCode("Name");

		PvpDataMerger.USE_DELETE_UI = false;
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
	
	private PvpRecord createRec3() {
		PvpRecord r = new PvpRecord(type2);
		r.setId(13);
		r.setCreationDate(new Date());
		r.setModificationDate(new Date());
		r.setCustomField("Name", "Sara");
		r.setCustomField("Street", "4637 Park Ave");
		r.setCustomField("City", "New York");
		return r;
	}

	static class DISetupParams {
		int maxId = 12;
		boolean addRec1 = true;
		boolean addRec2 = true;
		boolean addRec3 = false; // this is of type2
		boolean addType2 = false;
		void doAddType2() {
			addType2 = true;
			//maxId =13;
		}
		void doAddRec3() {
			addType2 = true;
			addRec3 = true;
			maxId =13;
		}
	}
	static class SetupParams {
		DISetupParams di1 = new DISetupParams();
		DISetupParams di2 = new DISetupParams();
	}

	private void setUpBothDI(SetupParams p) {
		PvpRecord r1 = createRec1();
		PvpRecord r2 = createRec2();
		PvpRecord r3 = createRec3();
		{
			List<PvpType> types1 = createTestTypes();
			List<PvpRecord> records1 = new ArrayList<>();
			if (p.di1.addRec1) { di1rec1 = r1; records1.add(di1rec1); }
			if (p.di1.addRec2) { di1rec2 = r2; records1.add(di1rec2); }
			if (p.di1.addType2) {
				types1.add(type2);
				if (p.di1.addRec3) { di1rec3 = r3; records1.add(di1rec3); }
				//records1.add(r3);
				//di1rec3 = r3;
			}
			di1 = new PvpDataInterface(context, types1, records1, p.di1.maxId, null);
		}
		{
			List<PvpType> types2 = createTestTypes();
			List<PvpRecord> records2 = new ArrayList<>();
			if (p.di2.addRec1) {
				di2rec1 = new PvpRecord(type1);
				r1.copyTo(di2rec1);
				di2rec1.setId(r1.getId());
				records2.add(di2rec1);
			}
			if (p.di2.addRec2) {
				di2rec2 = new PvpRecord(type1);
				r2.copyTo(di2rec2);
				di2rec2.setId(r2.getId());
				records2.add(di2rec2);
			}
			if (p.di2.addType2) {
				types2.add(type2);
				if (p.di2.addRec3) {
					di2rec3 = new PvpRecord(type2);
					r3.copyTo(di2rec3);
					di2rec3.setId(r3.getId());
					records2.add(di2rec3);
					//di1rec3 = r3;
				}
			}
			di2 = new PvpDataInterface(context, types2, records2, p.di2.maxId, null);
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
		verify(context, atLeastOnce()).ui.notifyInfo(contextInfoCaptor.capture());
		for (String s : contextInfoCaptor.getAllValues()) {
			System.out.println("PvpDataMergerTest.printContextInfo - " + s);
		}
	}

	/**
	 * Test where the two databases are identical
	 */
	@Test
	public void testMerge_same() {
		setUpBothDI(new SetupParams());

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context, di1, di2).mergeData();

		defaultValidate(result);
	}

	/**
	 * same except changing creation date
	 */
	@Test
	public void testMerge_creationDate() {
		setUpBothDI(new SetupParams());
		di2rec1.setCreationDate(new Date(new Date().getTime() + 60000));

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context, di1, di2).mergeData();

		defaultValidate(result);
	}

	/**
	 * same except using copy, and changing title - use new title
	 * di1 has the newest data
	 */
	@Test
	public void testMerge_newTitle_1() {
		setUpBothDI(new SetupParams());
		di1rec1.setModificationDate(new Date(new Date().getTime() + 60000));
		di2rec1.setCustomField("Account Name", "Email 2");

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context, di1, di2).mergeData();

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
		di2rec1.setModificationDate(new Date(new Date().getTime() + 60000));
		di2rec1.setCustomField("Account Name", "Email 2");

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context, di1, di2).mergeData();

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
		di2rec1.setId(3);

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context, di1, di2).mergeData();

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

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context, di1, di2).mergeData();

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

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context, di1, di2).mergeData();
		//printContextInfo();
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

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context, di1, di2).mergeData();

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

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context, di1, di2).mergeData();

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

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context, di1, di2).mergeData();

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
		di1rec1.setModificationDate(new Date(new Date().getTime() + 1000)); // make the modification 1 second later
		di1rec1.setCustomField("Password", "newsecret99"); // updated password

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context, di1, di2).mergeData();

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
		di2rec1.setModificationDate(new Date(new Date().getTime() + 1000)); // make the modification 1 second later
		di2rec1.setCustomField("Password", "newsecret99"); // updated password

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context, di1, di2).mergeData();

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
	
	/**
	 * A delete where the other rec has all fields changed. In FROM dataset
	 */
	@Test
	public void testMerge_deleteWithAllFieldsChanged_from() {
		SetupParams p = new SetupParams();
		p.di2.addRec1 = false;
		setUpBothDI(p);
		di2rec2.setCustomField("Account Name", "Work Email 2");
		di2rec2.setCustomField("Username", "joe-work 2");
		di2rec2.setCustomField("Password", "password123_2");

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context, di1, di2).mergeData();
		//printContextInfo();

		// TODO not sure this should be BOTH_CHANGED
		assertEquals(PvpDataMerger.MergeResultState.BOTH_CHANGED, result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(12, di1.getMaxId());
		assertEquals(1, di1.getRecordCount());
		{
			PvpRecord r = di1.getRecord(12);
			assertEquals("Work Email", r.getCustomField("Account Name"));
			assertEquals("joe-work", r.getCustomField("Username"));
			assertEquals("password123", r.getCustomField("Password"));
		}
	}
	
	/**
	 * A delete where the other rec has all fields changed. In FROM dataset
	 */
	@Test
	public void testMerge_deleteWithAllFieldsChanged_from_modDate() {
		SetupParams p = new SetupParams();
		p.di2.addRec1 = false;
		setUpBothDI(p);
		di2rec2.setCustomField("Account Name", "Work Email 2");
		di2rec2.setCustomField("Username", "joe-work 2");
		di2rec2.setCustomField("Password", "password123_2");
		di2rec2.setModificationDate(new Date(new Date().getTime() + 1000)); // make the modification 1 second later

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context, di1, di2).mergeData();
		//printContextInfo();

		// TODO not sure this should be BOTH_CHANGED
		assertEquals(PvpDataMerger.MergeResultState.BOTH_CHANGED, result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(12, di1.getMaxId());
		assertEquals(1, di1.getRecordCount());
		{
			PvpRecord r = di1.getRecord(12);
			assertEquals("Work Email 2", r.getCustomField("Account Name"));
			assertEquals("joe-work 2", r.getCustomField("Username"));
			assertEquals("password123_2", r.getCustomField("Password"));
		}
	}
	
	/**
	 * A delete where the other rec has all fields changed. In TO dataset
	 */
	@Test
	public void testMerge_deleteWithAllFieldsChanged_to() {
		SetupParams p = new SetupParams();
		p.di1.addRec1 = false;
		setUpBothDI(p);
		di1rec2.setCustomField("Account Name", "Work Email 2");
		di1rec2.setCustomField("Username", "joe-work 2");
		di1rec2.setCustomField("Password", "password123_2");

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context, di1, di2).mergeData();
		//printContextInfo();

		assertEquals(PvpDataMerger.MergeResultState.FROM_CHANGED, result);
		assertEquals(1, di1.getTypes().size());
		assertEquals(12, di1.getMaxId());
		assertEquals(1, di1.getRecordCount());
		{
			PvpRecord r = di1.getRecord(12);
			assertEquals("Work Email 2", r.getCustomField("Account Name"));
			assertEquals("joe-work 2", r.getCustomField("Username"));
			assertEquals("password123_2", r.getCustomField("Password"));
		}
	}
	
	/**
	 * test with a new datatype in TO
	 */
	@Test
	public void testMerge_type_new_to() {
		SetupParams p = new SetupParams();
		p.di1.doAddType2();
		setUpBothDI(p);

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context, di1, di2).mergeData();
		//printContextInfo();

		// TODO what about the case were they create a type in the older datafile? Should be rare, but not handled right now
		assertEquals(PvpDataMerger.MergeResultState.NO_CHANGE, result);
		assertEquals(2, di1.getTypes().size());
		assertEquals(12, di1.getMaxId());
		assertEquals(2, di1.getRecordCount());
//		{
//			PvpRecord r = di1.getRecord(13);
//			assertEquals("Sara", r.getCustomField("Name"));
//			assertEquals("4637 Park Ave", r.getCustomField("Street"));
//			assertEquals("New York", r.getCustomField("City"));
//		}
	}
	
	/**
	 * test with a new datatype in FROM
	 */
	@Test
	public void testMerge_type_new_from() {
		SetupParams p = new SetupParams();
		p.di2.doAddType2();
		setUpBothDI(p);

		PvpDataMerger.MergeResultState result = new PvpDataMerger(context, di1, di2).mergeData();
		//printContextInfo();

		
		assertEquals(PvpDataMerger.MergeResultState.TO_CHANGED, result);
		assertEquals(2, di1.getTypes().size());
		assertEquals(12, di1.getMaxId());
		assertEquals(2, di1.getRecordCount());
//		{
//			PvpRecord r = di1.getRecord(13);
//			assertEquals("Sara", r.getCustomField("Name"));
//			assertEquals("4637 Park Ave", r.getCustomField("Street"));
//			assertEquals("New York", r.getCustomField("City"));
//		}
	}

}

package com.ict.twitter.CrawlerSchedul;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class KeyWordsSchedulerTest {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetAllWords() {
		KeyWordsScheduler kws=new KeyWordsScheduler(null);
		System.out.println(kws.getAllWords().size());
	}

}

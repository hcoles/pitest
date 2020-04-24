package tests;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import sources.DiscoveredClass;

public class DiscoveredTest {

	@Test
	public void testAdd() {
		assertEquals(2, new DiscoveredClass().add(1, 1));
	}

}

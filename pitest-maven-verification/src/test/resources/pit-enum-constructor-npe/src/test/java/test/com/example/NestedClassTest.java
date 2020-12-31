package com.example;

import org.junit.Test;

public class NestedClassTest {
	public static enum MyEnum {
		A { }
	}

	@Test
	public void test() throws ClassNotFoundException {
		Class.forName("com.example.NestedClassTest$MyEnum$1");
	}
}


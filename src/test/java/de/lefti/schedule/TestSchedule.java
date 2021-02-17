package de.lefti.schedule;

import org.junit.Ignore;
import org.junit.Test;

public class TestSchedule {

	@Test
	public void test10SecondsSchedule() {
		Schedule.every(10).seconds().run(() -> System.out.println("Hello every 10 seconds"));
	}

	@Test(expected = AssertionError.class)
	public void testExpectNotInitialized() {
		Schedule.every(10).second().run(() -> System.out.println("This should never work"));
	}

	@Test(expected = AssertionError.class)
	public void testExpectSyntaxAssertionFail() {
		Schedule.every(10).second().run(() -> System.out.println("This should never work"));
	}

	@Ignore
	@Test(expected = AssertionError.class)
	public void testExpectSyntaxAssertNotIdeal() {
		Schedule.every().seconds().run(() -> System.out.println("This should never work"));
	}

	@Test(expected = AssertionError.class)
	public void testExpectTimeFormatAssertionFail() {
		Schedule.every().day().at("25:90").run(() -> System.out.println("This should never work"));
	}

	@Test
	public void testClockTimeSchedule() {
		Schedule.every().day().at("21:35").run(() -> System.out.println("Hello every day"));
	}
}

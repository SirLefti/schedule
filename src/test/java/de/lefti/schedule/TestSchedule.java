package de.lefti.schedule;

import org.junit.Ignore;
import org.junit.Test;

/**
 * It is pretty hard to actually test scheduling, especially the long-term scheduling. This tests are focussing mainly
 * on compilation errors.
 */
public class TestSchedule {

	@Test
	public void test10SecondsSchedule() {
		Schedule.every(10).seconds().run(() -> System.out.println("Hello every 10 seconds"));
	}

	@Test(expected = IntervalException.class)
	public void testExpectSyntaxAssertionFail() {
		Schedule.every(10).second().run(() -> System.out.println("This should never work"));
	}

	/**
	 * Currently ignored. Using {@link Schedule#every()} implies an interval of 1, which means a plural as unit does
	 * not make any sense. However, we have to allow this in the current implementation, because singular unit method
	 * are calling their plural form.
	 */
	@Ignore
	@Test(expected = IntervalException.class)
	public void testExpectSyntaxAssertNotIdeal() {
		Schedule.every().seconds().run(() -> System.out.println("This should never work"));
	}

	@Test(expected = TimeFormatException.class)
	public void testExpectTimeFormatAssertionFail() {
		Schedule.every().day().at("25:90").run(() -> System.out.println("This should never work"));
	}

	@Test
	public void testClockTimeSchedule() {
		Schedule.every().day().at("21:35").run(() -> System.out.println("Hello every day"));
	}

	@Test(expected = ScheduleException.class)
	public void testWeirdSyntax() {
		Schedule.every().second().monday().run(() -> System.out.println("This should never work"));
	}

	@Test(expected = ScheduleException.class)
	public void testMoreWeirdSyntax() {
		Schedule.every().week().monday().run(() -> System.out.println("This should never work"));
	}

	@Test
	public void testOnceTaskSyntax() {
		Schedule.once().at("11-01 08:00").run(() -> System.out.println("Auto-choose year as unit to use month and day as timestamp"));
		Schedule.once().month().at("-05 09:00").run(() -> System.out.println("Execute at next 5th of a month"));
	}
}

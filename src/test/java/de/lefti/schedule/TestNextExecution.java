package de.lefti.schedule;

import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.*;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertEquals;

/**
 * This test class focuses on the actual scheduling by using reflections to access and manipulate the inner parts.
 */
public class TestNextExecution {

	Field nextExecution;
	Method nextExecutionTimestamp;

	public TestNextExecution() throws NoSuchFieldException, NoSuchMethodException {
		// retrieve field and method to test and make them accessible once
		nextExecution = Schedule.class.getDeclaredField("_nextExecution");
		nextExecution.setAccessible(true);
		nextExecutionTimestamp = Schedule.class.getDeclaredMethod("nextExecutionTimestamp");
		nextExecutionTimestamp.setAccessible(true);
	}

	@Test
	public void testSecondDelayForOneMinute() throws IllegalAccessException, InvocationTargetException {
		Schedule task = Schedule.every().second();
		final long SECOND = ChronoUnit.SECONDS.getDuration().toMillis();
		long now = System.currentTimeMillis();

		for (int i = 0; i < 60; i++) {
			nextExecution.setLong(task, now + i * SECOND);
			long next = (long) nextExecutionTimestamp.invoke(task);
			assertEquals(now + (i + 1) * SECOND, next);
		}
	}

	@Test
	public void testFirstOfMonth() throws IllegalAccessException, InvocationTargetException {
		Schedule task = Schedule.every().month().at("-01");
		ZonedDateTime firstOfJan2020 = ZonedDateTime.of(LocalDate.of(2020, Month.JANUARY, 1), LocalTime.of(0, 0, 0), ZoneId.systemDefault());

		long next = firstOfJan2020.toInstant().toEpochMilli();

		for (int i = 2; i <= 12; i++) {
			nextExecution.setLong(task, next);
			next = (long) nextExecutionTimestamp.invoke(task);
			assertEquals(firstOfJan2020.withMonth(i).toInstant().toEpochMilli(), next);
		}
	}

	/**
	 * Currently ignored due to the way nextExecutionTimestamp works. If you start with a monthly task e.g. on 31-Jan,
	 * the day must be reduced to 28 in Feb, or 29 if it is a leap year. Based upon this, in Mar the day if 28 or 29 as
	 * well.
	 * Evaluate if this behaviour is as expected or the day should be aligned back to the initial value.
	 * @throws IllegalAccessException if field is not available
	 * @throws InvocationTargetException fi method could not be invoked
	 */
	@Ignore
	@Test
	public void testLastOfMonth() throws IllegalAccessException, InvocationTargetException {
		Schedule task = Schedule.every().month().at("-31");
		ZonedDateTime lastOfJan2020 = ZonedDateTime.of(LocalDate.of(2020, Month.JANUARY, 31), LocalTime.of(0, 0, 0), ZoneId.systemDefault());

		long next = lastOfJan2020.toInstant().toEpochMilli();

		for (int i = 2; i <= 12; i++) {
			nextExecution.setLong(task, next);
			next = (long) nextExecutionTimestamp.invoke(task);
			assertEquals(lastOfJan2020.withMonth(i).toInstant().toEpochMilli(), next);
		}
	}

	@Test
	public void testEveryFirstApril() throws IllegalAccessException, InvocationTargetException {
		Schedule task = Schedule.every().april().at("-01");
		int startYear = 2020;
		ZonedDateTime firstOfApr2020 = ZonedDateTime.of(LocalDate.of(startYear, Month.APRIL, 1), LocalTime.of(0, 0, 0), ZoneId.systemDefault());

		long next = firstOfApr2020.toInstant().toEpochMilli();

		for (int i = 1; i <= 4; i++) {
			nextExecution.setLong(task, next);
			next = (long) nextExecutionTimestamp.invoke(task);
			assertEquals(firstOfApr2020.plusYears(i).toInstant().toEpochMilli(), next);
		}
	}

	@Test
	public void testEveryTalkLikeAPirateDay() throws IllegalAccessException, InvocationTargetException {
		Schedule task = Schedule.every().september().at("-19");
		int startYear = 2020;
		ZonedDateTime talkLikeAPirateDay2020 = ZonedDateTime.of(LocalDate.of(startYear, Month.APRIL, 1), LocalTime.of(0, 0, 0), ZoneId.systemDefault());

		long next = talkLikeAPirateDay2020.toInstant().toEpochMilli();

		for (int i = 1; i <= 4; i++) {
			nextExecution.setLong(task, next);
			next = (long) nextExecutionTimestamp.invoke(task);
			assertEquals(talkLikeAPirateDay2020.plusYears(i).toInstant().toEpochMilli(), next);
		}
	}
}

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

		ZonedDateTime firstOfMonth = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1);
		if (firstOfMonth.isBefore(ZonedDateTime.now())) {
			firstOfMonth = firstOfMonth.plusMonths(1);
		}

		for (int i = 0; i <= 12; i++) {
			long next = (long) nextExecutionTimestamp.invoke(task);
			assertEquals(firstOfMonth.plusMonths(i).toInstant().toEpochMilli(), next);
			nextExecution.setLong(task, next);
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

		ZonedDateTime lastOfJan = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).withMonth(1).withDayOfMonth(31);
		if (lastOfJan.isBefore(ZonedDateTime.now())) {
			lastOfJan = lastOfJan.plusYears(1);
		}

		for (int i = 2; i <= 12; i++) {
			long next = (long) nextExecutionTimestamp.invoke(task);
			assertEquals(lastOfJan.withMonth(i).toInstant().toEpochMilli(), next);
			nextExecution.setLong(task, next);
		}
	}

	@Test
	public void testEveryFirstApril() throws IllegalAccessException, InvocationTargetException {
		Schedule task = Schedule.every().april().at("-01");

		ZonedDateTime firstOfApr = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).withMonth(4).withDayOfMonth(1);
		if (firstOfApr.isBefore(ZonedDateTime.now())) {
			firstOfApr = firstOfApr.plusYears(1);
		}

		for (int i = 0; i <= 4; i++) {
			long next = (long) nextExecutionTimestamp.invoke(task);
			assertEquals(firstOfApr.plusYears(i).toInstant().toEpochMilli(), next);
			nextExecution.setLong(task, next);
		}
	}

	@Test
	public void testEveryTalkLikeAPirateDay() throws IllegalAccessException, InvocationTargetException {
		Schedule task = Schedule.every().september().at("-19");

		ZonedDateTime talkLikeAPirateDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).withMonth(9).withDayOfMonth(19);
		if (talkLikeAPirateDay.isBefore(ZonedDateTime.now())) {
			talkLikeAPirateDay = talkLikeAPirateDay.plusYears(1);
		}

		for (int i = 0; i <= 4; i++) {
			long next = (long) nextExecutionTimestamp.invoke(task);
			assertEquals(talkLikeAPirateDay.plusYears(i).toInstant().toEpochMilli(), next);
			nextExecution.setLong(task, next);
		}
	}

	@Test
	public void testEveryFirstAprilUsingAt() throws IllegalAccessException {
		Schedule task = Schedule.every().year().at("04-01").run(() -> System.out.println("Hello world"));

		ZonedDateTime firstOfApril = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).withMonth(4).withDayOfMonth(1);
		if (firstOfApril.isBefore(ZonedDateTime.now())) {
			firstOfApril = firstOfApril.plusYears(1);
		}

		assertEquals(firstOfApril.toInstant().toEpochMilli(), nextExecution.getLong(task));
	}

	@Test
	public void testOnceAtSpecificDate() throws IllegalAccessException {
		Schedule task = Schedule.once().september().at("-19").run(() -> System.out.println("Hello world"));

		ZonedDateTime talkLikeAPirateDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).withMonth(9).withDayOfMonth(19);
		if (talkLikeAPirateDay.isBefore(ZonedDateTime.now())) {
			talkLikeAPirateDay = talkLikeAPirateDay.plusYears(1);
		}

		assertEquals(talkLikeAPirateDay.toInstant().toEpochMilli(), nextExecution.getLong(task));
	}

	@Test
	public void testOnceAtNextMonday() throws IllegalAccessException {
		Schedule task = Schedule.once().monday().at("08:00").run(() -> System.out.println("Hello world"));
		final int DAYS_PER_WEEK = 7;

		ZonedDateTime nextMonday8AM = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).withHour(8);
		if (nextMonday8AM.isBefore(ZonedDateTime.now())) {
			nextMonday8AM = nextMonday8AM.plusDays((DayOfWeek.MONDAY.compareTo(nextMonday8AM.getDayOfWeek()) + DAYS_PER_WEEK) % DAYS_PER_WEEK);
		}

		assertEquals(nextMonday8AM.toInstant().toEpochMilli(), nextExecution.getLong(task));
	}
}

package de.lefti.schedule;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Set;

public class TestCoreScheduler {

	Set<Schedule> taskSet;
	boolean flag = false;
	final static int MILLIS = 1000;

	@SuppressWarnings("unchecked")
	public TestCoreScheduler() throws NoSuchFieldException, IllegalAccessException {
		var scheduler = Schedule.class.getDeclaredField("_coreScheduler");
		scheduler.setAccessible(true);
		var optionalClass = Arrays.stream(Schedule.class.getDeclaredClasses())
				.filter(c -> c.getSimpleName().equals("CoreScheduler")).findAny();

		Assert.assertTrue(optionalClass.isPresent());
		var tasks = optionalClass.get().getDeclaredField("_scheduledTasks");
		tasks.setAccessible(true);
		var instance = scheduler.get(Schedule.class);
		taskSet = (Set<Schedule>) tasks.get(instance);
		// clear it in case other tests have been running beforehand
		taskSet.clear();
	}

	@Test
	public synchronized void testIdleBehaviour() throws InterruptedException {
		var timeToKill = 2;
		var timeToWait = timeToKill * 2;

		Assert.assertFalse(flag);
		Assert.assertEquals(0, taskSet.size());
		Schedule toCancel = Schedule.every().second().run(() -> System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "] " + "This will run only a few times"));

		Assert.assertEquals(1, taskSet.size());
		Schedule.once().minute().at(String.format(":%02d", LocalDateTime.now().plusSeconds(timeToKill).getSecond())).run(() -> {
			toCancel.cancel();
			delayedNotify(MILLIS);
		});
		Assert.assertEquals(2, taskSet.size());
		this.wait(timeToWait * MILLIS);
		Assert.assertEquals(0, taskSet.size());
		System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "] " + "Notified to proceed, no tasks scheduled");

		Schedule.once().minute().at(String.format(":%02d", LocalDateTime.now().plusSeconds(timeToKill).getSecond())).run(() -> {
			System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "] " + "This is a delayed task that flags the task as true");
			flag = true;
			delayedNotify(MILLIS);
		});
		this.wait(timeToWait * MILLIS);
		Assert.assertTrue(flag);
		Assert.assertEquals(0, taskSet.size());
		System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "] " + "Notified to proceed, no tasks scheduled");
	}

	private synchronized void delayedNotify(int millis) {
		try {
			this.wait(millis);
		} catch (InterruptedException ignored) {

		}
		this.notify();
	}
}

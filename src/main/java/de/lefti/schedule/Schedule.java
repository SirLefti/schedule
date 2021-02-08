package de.lefti.schedule;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Schedule {

	private final int _interval;
	private ChronoUnit _unit;
	private DayOfWeek _startDay = DayOfWeek.MONDAY;
	private int targetHour = 0;
	private int targetMinute = 0;
	private int targetSecond = 0;

	private static ScheduledExecutorService _scheduler;

	/**
	 * Private accessed constructor.
	 *
	 * @param interval base interval for execution.
	 */
	private Schedule(int interval) {
		_interval = interval;
	}

	/**
	 * Initializes the scheduler. Greater pool sizes might improve performance when executing multiple task
	 * simultaneously.
	 *
	 * @param poolSize pool size to be used for internal scheduler.
	 */
	public static void init(int poolSize) {
		_scheduler = new ScheduledThreadPoolExecutor(poolSize);
	}

	/**
	 * Creates a scheduled task with the default interval.
	 *
	 * @return Schedule object
	 */
	public static Schedule every() {
		assert _scheduler != null : "scheduler not initialized";
		return new Schedule(1);
	}

	/**
	 * Creates a scheduled task with given interval.
	 *
	 * @param interval interval to be used.
	 * @return Schedule object
	 */
	public static Schedule every(int interval) {
		assert _scheduler != null : "scheduler not initialized";
		assert interval != 1 : "use every() instead";
		assert interval > 1 : "use positive interval values only";
		return new Schedule(interval);
	}

	/**
	 * Sets the scheduled task to be run every second.
	 *
	 * @return Schedule object
	 */
	public Schedule second() {
		assert _interval == 1 : "use seconds() instead";
		return seconds();
	}

	/**
	 * Sets the scheduled task to be run by an interval of seconds.
	 *
	 * @return Schedule object
	 */
	public Schedule seconds() {
		_unit = ChronoUnit.SECONDS;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every minute.
	 *
	 * @return Schedule object
	 */
	public Schedule minute() {
		assert _interval == 1 : "use minutes() instead";
		return minutes();
	}

	/**
	 * Sets the scheduled task to be run by an interval of minutes.
	 *
	 * @return Schedule object
	 */
	public Schedule minutes() {
		_unit = ChronoUnit.MINUTES;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every hour.
	 *
	 * @return Schedule object
	 */
	public Schedule hour() {
		assert _interval == 1 : "use hours() instead";
		return hours();
	}

	/**
	 * Sets the scheduled task to be run by an interval of hours.
	 *
	 * @return Schedule object
	 */
	public Schedule hours() {
		_unit = ChronoUnit.HOURS;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every day.
	 *
	 * @return Schedule object
	 */
	public Schedule day() {
		assert _interval == 1 : "use days() instead";
		return days();
	}

	/**
	 * Sets the scheduled task to be run by an interval of days.
	 *
	 * @return Schedule object
	 */
	public Schedule days() {
		_unit = ChronoUnit.DAYS;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every week.
	 *
	 * @return Schedule object
	 */
	public Schedule week() {
		assert _interval == 1 : "use weeks() instead";
		return weeks();
	}

	/**
	 * Sets the scheduled task to be run by an interval of weeks.
	 *
	 * @return Schedule object
	 */
	public Schedule weeks() {
		_unit = ChronoUnit.WEEKS;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every monday.
	 *
	 * @return Schedule object
	 */
	public Schedule monday() {
		_unit = ChronoUnit.WEEKS;
		_startDay = DayOfWeek.MONDAY;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every tuesday.
	 *
	 * @return Schedule object
	 */
	public Schedule tuesday() {
		_unit = ChronoUnit.WEEKS;
		_startDay = DayOfWeek.TUESDAY;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every wednesday.
	 *
	 * @return Schedule object
	 */
	public Schedule wednesday() {
		_unit = ChronoUnit.WEEKS;
		_startDay = DayOfWeek.WEDNESDAY;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every thursday.
	 *
	 * @return Schedule object
	 */
	public Schedule thursday() {
		_unit = ChronoUnit.WEEKS;
		_startDay = DayOfWeek.THURSDAY;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every friday.
	 *
	 * @return Schedule object
	 */
	public Schedule friday() {
		_unit = ChronoUnit.WEEKS;
		_startDay = DayOfWeek.FRIDAY;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every saturday.
	 *
	 * @return Schedule object
	 */
	public Schedule saturday() {
		_unit = ChronoUnit.WEEKS;
		_startDay = DayOfWeek.SATURDAY;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every sunday.
	 *
	 * @return Schedule object
	 */
	public Schedule sunday() {
		_unit = ChronoUnit.WEEKS;
		_startDay = DayOfWeek.SUNDAY;
		return this;
	}

	/**
	 * Sets the scheduled task to be run at a given time.
	 * 
	 * For daily or weekly tasks --> "HH:MM:SS" or "HH:MM"
	 * For hourly tasks --> "MM:SS" or ":MM"
	 * For minute tasks --> ":SS"
	 *
	 * @param time time as string.
	 * @return Schedule object
	 */
	public Schedule at(String time) {
		if (_unit == ChronoUnit.MINUTES) {
			assert time.matches("^:[0-5]\\d$") : "invalid time format";
		} else if (_unit == ChronoUnit.HOURS) {
			assert time.matches("^([0-5]\\d)?:[0-5]\\d$") : "invalid time format";
		} else if (_unit == ChronoUnit.DAYS || _unit == ChronoUnit.WEEKS) {
			assert time.matches("^([0-2]\\d:)?[0-5]\\d:[0-5]\\d$") : "invalid time format";
		} else {
			throw new AssertionError("invalid time unit");
		}
		String[] values = time.split(":");
		if (values.length == 3) {
			targetHour = Integer.parseInt(values[0]);
			targetMinute = Integer.parseInt(values[1]);
			targetSecond = Integer.parseInt(values[2]);
		} else if (values.length == 2 && _unit == ChronoUnit.MINUTES) {
			targetSecond = Integer.parseInt(values[1]);
		} else if (values.length == 2 && _unit == ChronoUnit.HOURS) {
			if (values[0].isEmpty()) {
				targetMinute = Integer.parseInt(values[1]);
			} else {
				targetMinute = Integer.parseInt(values[0]);
				targetSecond = Integer.parseInt(values[1]);
			}
		} else if (values.length == 2) {
			// implies that unit is days or weeks
			targetHour = Integer.parseInt(values[0]);
			targetMinute = Integer.parseInt(values[1]);
		}
		return this;
	}

	/**
	 * Schedules the actual task.
	 * 
	 * @param task task to be run.
	 */
	public void run(Runnable task) {
		_scheduler.scheduleAtFixedRate(task, initialDelay(), delay(), TimeUnit.MILLISECONDS);
	}

	private long initialDelay() {
		final int DAYS_PER_WEEK = 7;
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime firstExecution = now.truncatedTo(ChronoUnit.SECONDS);
		TemporalUnit temporal = ChronoUnit.SECONDS;
		if (_unit == ChronoUnit.SECONDS) {
			firstExecution = firstExecution.plusSeconds(1);
			temporal = ChronoUnit.SECONDS;
		} else if (_unit == ChronoUnit.MINUTES) {
			firstExecution = firstExecution.withSecond(targetSecond);
			temporal = ChronoUnit.MINUTES;
		} else if (_unit == ChronoUnit.HOURS) {
			firstExecution = firstExecution.withMinute(targetMinute).withSecond(targetSecond);
			temporal = ChronoUnit.HOURS;
		} else if (_unit == ChronoUnit.DAYS) {
			firstExecution = firstExecution.withHour(targetHour).withMinute(targetMinute).withSecond(targetSecond);
			temporal = ChronoUnit.DAYS;
		} else if (_unit == ChronoUnit.WEEKS) {
			firstExecution = firstExecution.plusDays((_startDay.compareTo(firstExecution.getDayOfWeek()) + DAYS_PER_WEEK) % DAYS_PER_WEEK);
			temporal = ChronoUnit.WEEKS;
		}
		if (firstExecution.isBefore(now)) {
			firstExecution = firstExecution.plus(1, temporal);
		}
		return Duration.between(LocalDateTime.now(), firstExecution).toMillis();
	}

	private long delay() {
		return _unit.getDuration().toMillis() * _interval;
	}
}

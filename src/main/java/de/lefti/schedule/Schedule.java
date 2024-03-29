package de.lefti.schedule;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

public class Schedule {

	private final int _interval;
	private ChronoUnit _unit;
	private Runnable _task;
	private DayOfWeek _targetDayOfWeek;

	private boolean _alive = true;
	private boolean _repeat = true;
	private boolean _usingTargetDate = false;
	private boolean _usingTargetTime = false;
	private long _nextExecution = 0;
	private int _targetMonth = 1;
	private int _targetDay = 1;
	private int _targetHour = 0;
	private int _targetMinute = 0;
	private int _targetSecond = 0;

	private static final CoreScheduler _coreScheduler = new CoreScheduler();

	private static final class CoreScheduler implements Runnable {
		private final Set<Schedule> _scheduledTasks = new HashSet<>();
		private boolean _alive = true;

		public void kill() {
			_alive = false;
		}

		public synchronized void wakeUp() {
			this.notify();
		}

		public synchronized boolean addTask(Schedule task) {
			_scheduledTasks.add(task);
			return _scheduledTasks.size() == 1;
		}

		@Override
		public synchronized void run() {
			Set<Schedule> toDelete = new HashSet<>();
			while (_alive) {
				try {
					long nextExecution = Long.MAX_VALUE;
					for (Schedule scheduledTask : _scheduledTasks) {
						if (scheduledTask._alive) {
							nextExecution = Math.min(scheduledTask._nextExecution, nextExecution);
						} else {
							toDelete.add(scheduledTask);
						}
					}
					_scheduledTasks.removeAll(toDelete);
					toDelete.clear();
					long now = System.currentTimeMillis();
					if (nextExecution > now) {
						this.wait(nextExecution - now);
					}
					for (Schedule scheduledTask : _scheduledTasks) {
						if (scheduledTask.shouldRun()) {
							new Thread(scheduledTask._task).start();
							if (scheduledTask._repeat) {
								// now reschedule
								scheduledTask._nextExecution = scheduledTask.nextExecutionTimestamp();
							} else {
								scheduledTask.cancel();
							}
						}
					}

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// clear when not alive any more
			_scheduledTasks.clear();
		}
	}

	/**
	 * Private accessed constructor.
	 *
	 * @param interval base interval for execution.
	 */
	private Schedule(int interval) {
		_interval = interval;
	}

	/**
	 * Private accessed constructor.
	 *
	 * @param interval base interval for execution
	 * @param repeat {@code true} for continuous repeated tasks; {@code false} else
	 */
	private Schedule(int interval, boolean repeat) {
		_interval = interval;
		_repeat = repeat;
	}

	/**
	 * Creates a scheduled task with the default interval.
	 *
	 * @return Schedule object
	 */
	public static Schedule every() {
		return new Schedule(1);
	}

	/**
	 * Creates a scheduled task with given interval.
	 *
	 * @param interval interval to be used.
	 * @return Schedule object
	 */
	public static Schedule every(int interval) {
		if (interval == 1) {
			throw new IntervalException("use every() instead");
		}
		if (interval < 1) {
			throw new IntervalException("use positive interval values only");
		}
		return new Schedule(interval);
	}

	/**
	 * Creates a scheduled task that is executed only once.
	 * Use with {@link #at(String)} to specify the timestamp.
	 *
	 * @return Schedule object
	 */
	public static Schedule once() {
		return new Schedule(1, false);
	}

	/**
	 * Sets the scheduled task to be run every second.
	 *
	 * @return Schedule object
	 */
	public Schedule second() {
		if (_interval != 1) {
			throw new IntervalException("use seconds() instead");
		}
		return seconds();
	}

	/**
	 * Sets the scheduled task to be run by an interval of seconds.
	 *
	 * @return Schedule object
	 */
	public Schedule seconds() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.SECONDS;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every minute.
	 *
	 * @return Schedule object
	 */
	public Schedule minute() {
		if (_interval != 1) {
			throw new IntervalException("use minutes() instead");
		}
		return minutes();
	}

	/**
	 * Sets the scheduled task to be run by an interval of minutes.
	 *
	 * @return Schedule object
	 */
	public Schedule minutes() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.MINUTES;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every hour.
	 *
	 * @return Schedule object
	 */
	public Schedule hour() {
		if (_interval != 1) {
			throw new IntervalException("use hours() instead");
		}
		return hours();
	}

	/**
	 * Sets the scheduled task to be run by an interval of hours.
	 *
	 * @return Schedule object
	 */
	public Schedule hours() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.HOURS;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every day.
	 *
	 * @return Schedule object
	 */
	public Schedule day() {
		if (_interval != 1) {
			throw new IntervalException("use days() instead");
		}
		return days();
	}

	/**
	 * Sets the scheduled task to be run by an interval of days.
	 *
	 * @return Schedule object
	 */
	public Schedule days() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.DAYS;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every week.
	 *
	 * @return Schedule object
	 */
	public Schedule week() {
		if (_interval != 1) {
			throw new IntervalException("use weeks() instead");
		}
		return weeks();
	}

	/**
	 * Sets the scheduled task to be run by an interval of weeks.
	 *
	 * @return Schedule object
	 */
	public Schedule weeks() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.WEEKS;
		_targetDayOfWeek = ZonedDateTime.now().getDayOfWeek();
		return this;
	}

	/**
	 * Sets the scheduled task to be run every monday.
	 *
	 * @return Schedule object
	 */
	public Schedule monday() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.WEEKS;
		_targetDayOfWeek = DayOfWeek.MONDAY;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every tuesday.
	 *
	 * @return Schedule object
	 */
	public Schedule tuesday() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.WEEKS;
		_targetDayOfWeek = DayOfWeek.TUESDAY;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every wednesday.
	 *
	 * @return Schedule object
	 */
	public Schedule wednesday() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.WEEKS;
		_targetDayOfWeek = DayOfWeek.WEDNESDAY;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every thursday.
	 *
	 * @return Schedule object
	 */
	public Schedule thursday() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.WEEKS;
		_targetDayOfWeek = DayOfWeek.THURSDAY;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every friday.
	 *
	 * @return Schedule object
	 */
	public Schedule friday() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.WEEKS;
		_targetDayOfWeek = DayOfWeek.FRIDAY;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every saturday.
	 *
	 * @return Schedule object
	 */
	public Schedule saturday() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.WEEKS;
		_targetDayOfWeek = DayOfWeek.SATURDAY;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every sunday.
	 *
	 * @return Schedule object
	 */
	public Schedule sunday() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.WEEKS;
		_targetDayOfWeek = DayOfWeek.SUNDAY;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every month.
	 *
	 * @return Schedule object
	 */
	public Schedule month() {
		if (_interval != 1) {
			throw new IntervalException("use months() instead");
		}
		return months();
	}

	/**
	 * Sets the scheduled task to be run by an interval of months.
	 *
	 * @return Schedule object
	 */
	public Schedule months() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.MONTHS;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every january.
	 *
	 * @return Schedule object
	 */
	public Schedule january() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.YEARS;
		_targetMonth = 1;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every february.
	 *
	 * @return Schedule object
	 */
	public Schedule february() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.YEARS;
		_targetMonth = 2;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every march.
	 *
	 * @return Schedule object
	 */
	public Schedule march() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.YEARS;
		_targetMonth = 3;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every april.
	 *
	 * @return Schedule object
	 */
	public Schedule april() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.YEARS;
		_targetMonth = 4;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every may.
	 *
	 * @return Schedule object
	 */
	public Schedule may() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.YEARS;
		_targetMonth = 5;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every june.
	 *
	 * @return Schedule object
	 */
	public Schedule june() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.YEARS;
		_targetMonth = 6;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every july.
	 *
	 * @return Schedule object
	 */
	public Schedule july() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.YEARS;
		_targetMonth = 7;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every august.
	 *
	 * @return Schedule object
	 */
	public Schedule august() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.YEARS;
		_targetMonth = 8;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every september.
	 *
	 * @return Schedule object
	 */
	public Schedule september() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.YEARS;
		_targetMonth = 9;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every october.
	 *
	 * @return Schedule object
	 */
	public Schedule october() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.YEARS;
		_targetMonth = 10;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every november.
	 *
	 * @return Schedule object
	 */
	public Schedule november() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.YEARS;
		_targetMonth = 11;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every december.
	 *
	 * @return Schedule object
	 */
	public Schedule december() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.YEARS;
		_targetMonth = 12;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every year.
	 *
	 * @return Schedule object
	 */
	public Schedule year() {
		if (_interval != 1) {
			throw new IntervalException("use years() instead");
		}
		return years();
	}

	/**
	 * Sets the scheduled task to be run by an interval of years.
	 *
	 * @return Schedule object
	 */
	public Schedule years() {
		if (_unit != null) {
			throw new ScheduleException("schedule unit already set");
		}
		_unit = ChronoUnit.YEARS;
		return this;
	}

	/**
	 * Sets the scheduled task to be run at a given time.
	 * <p>
	 * For daily or weekly tasks --> "HH:MM:SS" or "HH:MM"<br>
	 * For hourly tasks --> "MM:SS" or ":MM"<br>
	 * For minute tasks --> ":SS"<br>
	 * <p>
	 * For monthly tasks --> "-dd HH:MM:SS", "-dd HH:MM" or "-dd"<br>
	 * For yearly tasks --> "mm-dd HH:MM:SS", "mm-dd HH:MM", "mm-dd", "-dd HH:MM:SS", "-dd HH:MM" or "-dd"<br>
	 *
	 * @param timestamp timestamp as string.
	 * @return Schedule object
	 */
	public Schedule at(String timestamp) {
		_usingTargetTime = true;
		if (!_repeat && _unit == null) {
			_unit = ChronoUnit.YEARS;
		}
		if (_unit == ChronoUnit.MINUTES) {
			if (!timestamp.matches("^:[0-5]\\d$")) {
				throw new TimeFormatException("invalid time format");
			}
		} else if (_unit == ChronoUnit.HOURS) {
			if (!timestamp.matches("^([0-5]\\d)?:[0-5]\\d$")) {
				throw new TimeFormatException("invalid time format");
			}
		} else if (_unit == ChronoUnit.DAYS || _unit == ChronoUnit.WEEKS) {
			if (!timestamp.matches("^([0-2]\\d:)?[0-5]\\d:[0-5]\\d$")) {
				throw new TimeFormatException("invalid time format");
			}
		} else if (_unit == ChronoUnit.MONTHS) {
			if (!timestamp.matches("^(-[0-3]\\d)((\\s[0-2]\\d):([0-5]\\d)(:[0-5]\\d)?)?$")) {
				throw new TimeFormatException("invalid time format");
			}
			_usingTargetDate = true;
		} else if (_unit == ChronoUnit.YEARS) {
			if (!timestamp.matches("^([0-1]\\d)?(-[0-3]\\d)((\\s[0-2]\\d):([0-5]\\d)(:[0-5]\\d)?)?$")) {
				throw new TimeFormatException("invalid time format");
			}
			_usingTargetDate = true;
		} else {
			throw new ScheduleException("invalid time unit");
		}
		// Split on colon, whitespace and hyphen
		String[] values = timestamp.split(":|\\s|-");
		if (values.length == 5) {
			if (!values[0].isEmpty()) {
				_targetMonth = Integer.parseInt(values[0]);
			}
			_targetDay = Integer.parseInt(values[1]);
			_targetHour = Integer.parseInt(values[2]);
			_targetMinute = Integer.parseInt(values[3]);
			_targetSecond = Integer.parseInt(values[4]);
		} else if (values.length == 4) {
			if (!values[0].isEmpty()) {
				_targetMonth = Integer.parseInt(values[0]);
			}
			_targetDay = Integer.parseInt(values[1]);
			_targetHour = Integer.parseInt(values[2]);
			_targetMinute = Integer.parseInt(values[3]);
		} else if (values.length == 3) {
			_targetHour = Integer.parseInt(values[0]);
			_targetMinute = Integer.parseInt(values[1]);
			_targetSecond = Integer.parseInt(values[2]);
		} else if (values.length == 2 && _unit == ChronoUnit.MINUTES) {
			_targetSecond = Integer.parseInt(values[1]);
		} else if (values.length == 2 && _unit == ChronoUnit.HOURS) {
			if (values[0].isEmpty()) {
				// implies that default hour is used
				_targetMinute = Integer.parseInt(values[1]);
			} else {
				_targetMinute = Integer.parseInt(values[0]);
				_targetSecond = Integer.parseInt(values[1]);
			}
		} else if (values.length == 2 && (_unit == ChronoUnit.DAYS || _unit == ChronoUnit.WEEKS)) {
			_targetHour = Integer.parseInt(values[0]);
			_targetMinute = Integer.parseInt(values[1]);
		} else if (values.length == 2 && (_unit == ChronoUnit.MONTHS || _unit == ChronoUnit.YEARS)) {
			if (!values[0].isEmpty()) {
				_targetMonth = Integer.parseInt(values[0]);
			}
			_targetDay = Integer.parseInt(values[1]);
		}
		return this;
	}

	/**
	 * Schedules the actual task.
	 *
	 * @param task task to be run.
	 * @return Schedule object
	 */
	public Schedule run(Runnable task) {
		_task = task;
		_nextExecution = nextExecutionTimestamp();
		if (_coreScheduler.addTask(this)) {
			// This means, we added the first task, so we start the core scheduler
			Thread t = new Thread(_coreScheduler);
			t.setName("CoreScheduler");
			t.start();
		} else {
			_coreScheduler.wakeUp();
		}
		return this;
	}

	/**
	 * Cancels the scheduled task. If the task is currently running, it will be finished. If not, the task is cancelled
	 * immediately.
	 */
	public void cancel() {
		_alive = false;
	}

	/**
	 * Shuts down the scheduler. All scheduled tasks will be removed. A new scheduler will be created when adding new
	 * tasks.
	 */
	public static void shutdown() {
		_coreScheduler.kill();
	}

	private long nextExecutionTimestamp() {
		if (_nextExecution == 0 && !_usingTargetTime && !_usingTargetDate) {
			// first execution, but no specific date or time, choose current time
			return System.currentTimeMillis();
		} else if (_nextExecution == 0) {
			// first execution, date or time is set
			final int DAYS_PER_WEEK = 7;
			// align to next full second
			ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(1);
			ZonedDateTime next = now;
			if (_unit == ChronoUnit.SECONDS) {
				// nothing to do
			} else if (_unit == ChronoUnit.MINUTES) {
				next = next.withSecond(_targetSecond);
			} else if (_unit == ChronoUnit.HOURS) {
				next = next.withMinute(_targetMinute).withSecond(_targetSecond);
			} else if (_unit == ChronoUnit.DAYS) {
				next = next.withHour(_targetHour).withMinute(_targetMinute).withSecond(_targetSecond);
			} else if (_unit == ChronoUnit.WEEKS) {
				next = next.plusDays((_targetDayOfWeek.compareTo(next.getDayOfWeek()) + DAYS_PER_WEEK) % DAYS_PER_WEEK)
						.withHour(_targetHour).withMinute(_targetMinute).withSecond(_targetSecond);
			} else if (_unit == ChronoUnit.MONTHS) {
				next = next.withMonth(_targetMonth).withDayOfMonth(_targetDay)
						.withHour(_targetHour).withMinute(_targetMinute).withSecond(_targetSecond);
			} else if (_unit == ChronoUnit.YEARS) {
				next = next.withMonth(_targetMonth).withDayOfMonth(_targetDay)
						.withHour(_targetHour).withMinute(_targetMinute).withSecond(_targetSecond);
			}
			while (!next.isAfter(now)) {
				next = next.plus(1, _unit);
			}
			return next.toInstant().toEpochMilli();
		} else {
			// next execution, just add interval
			return ZonedDateTime.ofInstant(Instant.ofEpochMilli(_nextExecution), ZoneId.systemDefault()).plus(_interval, _unit).toInstant().toEpochMilli();
		}
	}

	private boolean shouldRun() {
		return _nextExecution < System.currentTimeMillis() && _alive;
	}
}

package de.lefti.schedule;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

public class Schedule {

	private final int _interval;
	private ChronoUnit _unit;
	private DayOfWeek _startDay;
	private Month _startMonth;
	private Runnable _task;

	private boolean _alive = true;
	private boolean _usingTargetDate = false;
	private boolean _usingTargetTime = false;
	private long _lastExecution = 0;
	private long _nextExecution = 0;
	private int _targetMonth;
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
							// now reschedule
							scheduledTask._lastExecution = scheduledTask._nextExecution;
							scheduledTask._nextExecution = scheduledTask.nextExecutionTimestamp();
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
		assert _unit == null : "schedule unit already set";
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
		assert _unit == null : "schedule unit already set";
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
		assert _unit == null : "schedule unit already set";
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
		assert _unit == null : "schedule unit already set";
		_unit = ChronoUnit.DAYS;
		_startDay = ZonedDateTime.now().getDayOfWeek();
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
		assert _unit == null : "schedule unit already set";
		_unit = ChronoUnit.WEEKS;
		_startDay = ZonedDateTime.now().getDayOfWeek();
		return this;
	}

	/**
	 * Sets the scheduled task to be run every monday.
	 *
	 * @return Schedule object
	 */
	public Schedule monday() {
		assert _unit == null : "schedule unit already set";
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
		assert _unit == null : "schedule unit already set";
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
		assert _unit == null : "schedule unit already set";
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
		assert _unit == null : "schedule unit already set";
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
		assert _unit == null : "schedule unit already set";
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
		assert _unit == null : "schedule unit already set";
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
		assert _unit == null : "schedule unit already set";
		_unit = ChronoUnit.WEEKS;
		_startDay = DayOfWeek.SUNDAY;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every month.
	 *
	 * @return Schedule object
	 */
	public Schedule month() {
		assert _interval == 1 : "use months() instead";
		return weeks();
	}

	/**
	 * Sets the scheduled task to be run by an interval of months.
	 *
	 * @return Schedule object
	 */
	public Schedule months() {
		assert _unit == null : "schedule unit already set";
		_unit = ChronoUnit.MONTHS;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every january.
	 *
	 * @return Schedule object
	 */
	public Schedule january() {
		assert _unit == null : "schedule unit already set";
		_unit = ChronoUnit.YEARS;
		_startMonth = Month.JANUARY;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every february.
	 *
	 * @return Schedule object
	 */
	public Schedule february() {
		assert _unit == null : "schedule unit already set";
		_unit = ChronoUnit.YEARS;
		_startMonth = Month.FEBRUARY;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every march.
	 *
	 * @return Schedule object
	 */
	public Schedule march() {
		assert _unit == null : "schedule unit already set";
		_unit = ChronoUnit.YEARS;
		_startMonth = Month.MARCH;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every april.
	 *
	 * @return Schedule object
	 */
	public Schedule april() {
		assert _unit == null : "schedule unit already set";
		_unit = ChronoUnit.YEARS;
		_startMonth = Month.APRIL;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every may.
	 *
	 * @return Schedule object
	 */
	public Schedule may() {
		assert _unit == null : "schedule unit already set";
		_unit = ChronoUnit.YEARS;
		_startMonth = Month.MAY;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every june.
	 *
	 * @return Schedule object
	 */
	public Schedule june() {
		assert _unit == null : "schedule unit already set";
		_unit = ChronoUnit.YEARS;
		_startMonth = Month.JUNE;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every july.
	 *
	 * @return Schedule object
	 */
	public Schedule july() {
		assert _unit == null : "schedule unit already set";
		_unit = ChronoUnit.YEARS;
		_startMonth = Month.JULY;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every august.
	 *
	 * @return Schedule object
	 */
	public Schedule august() {
		assert _unit == null : "schedule unit already set";
		_unit = ChronoUnit.YEARS;
		_startMonth = Month.AUGUST;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every september.
	 *
	 * @return Schedule object
	 */
	public Schedule september() {
		assert _unit == null : "schedule unit already set";
		_unit = ChronoUnit.YEARS;
		_startMonth = Month.SEPTEMBER;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every october.
	 *
	 * @return Schedule object
	 */
	public Schedule october() {
		assert _unit == null : "schedule unit already set";
		_unit = ChronoUnit.YEARS;
		_startMonth = Month.OCTOBER;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every november.
	 *
	 * @return Schedule object
	 */
	public Schedule november() {
		assert _unit == null : "schedule unit already set";
		_unit = ChronoUnit.YEARS;
		_startMonth = Month.NOVEMBER;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every december.
	 *
	 * @return Schedule object
	 */
	public Schedule december() {
		assert _unit == null : "schedule unit already set";
		_unit = ChronoUnit.YEARS;
		_startMonth = Month.DECEMBER;
		return this;
	}

	/**
	 * Sets the scheduled task to be run every year.
	 *
	 * @return Schedule object
	 */
	public Schedule year() {
		assert _interval == 1 : "use years() instead";
		return weeks();
	}

	/**
	 * Sets the scheduled task to be run by an interval of years.
	 *
	 * @return Schedule object
	 */
	public Schedule years() {
		assert _unit == null : "schedule unit already set";
		_unit = ChronoUnit.YEARS;
		return this;
	}

	/**
	 * Sets the scheduled task to be run at a given time.
	 * <p>
	 * For daily or weekly tasks --> "HH:MM:SS" or "HH:MM"
	 * For hourly tasks --> "MM:SS" or ":MM"
	 * For minute tasks --> ":SS"
	 * <p>
	 * For monthly tasks --> "-dd HH:MM:SS" or "-dd HH:MM" or "-dd"
	 * For yearly tasks --> "mm-dd HH:MM:SS" or "mm-dd HH:MM" or "mm-dd"
	 *
	 * @param time time as string.
	 * @return Schedule object
	 */
	public Schedule at(String time) {
		_usingTargetTime = true;
		if (_unit == ChronoUnit.MINUTES) {
			assert time.matches("^:[0-5]\\d$") : "invalid time format";
		} else if (_unit == ChronoUnit.HOURS) {
			assert time.matches("^([0-5]\\d)?:[0-5]\\d$") : "invalid time format";
		} else if (_unit == ChronoUnit.DAYS || _unit == ChronoUnit.WEEKS) {
			assert time.matches("^([0-2]\\d:)?[0-5]\\d:[0-5]\\d$") : "invalid time format";
		} else if (_unit == ChronoUnit.MONTHS) {
			assert time.matches("^(-[0-2]\\d)((\\s[0-2]\\d):([0-5]\\d)(:[0-5]\\d)?)?$") : "invalid time format";
			_usingTargetDate = true;
		} else if (_unit == ChronoUnit.YEARS) {
			assert time.matches("^([0-2]\\d)(-[0-2]\\d)((\\s[0-2]\\d):([0-5]\\d)(:[0-5]\\d)?)?$") : "invalid time format";
			_usingTargetDate = true;
		} else {
			throw new AssertionError("invalid time unit");
		}
		// Split on colon, whitespace and hyphen
		String[] values = time.split(":|\\s|-");
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
		} else if (values.length == 2) {
			// implies that unit is days or weeks
			_targetHour = Integer.parseInt(values[0]);
			_targetMinute = Integer.parseInt(values[1]);
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
			new Thread(_coreScheduler).start();
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
				next = next.plusDays((_startDay.compareTo(next.getDayOfWeek()) + DAYS_PER_WEEK) % DAYS_PER_WEEK)
						.withHour(_targetHour).withMinute(_targetMinute).withSecond(_targetSecond);
			} else if (_unit == ChronoUnit.MONTHS) {
				next = next.withMonth(_startMonth.ordinal() + 1).withDayOfMonth(_targetDay)
						.withHour(_targetHour).withMinute(_targetMinute).withSecond(_targetSecond);
			} else if (_unit == ChronoUnit.YEARS) {
				next = next.withMonth(_startMonth.ordinal() + 1).withDayOfMonth(_targetDay)
						.withHour(_targetHour).withMinute(_targetMinute).withSecond(_targetSecond);
			}
			if (!next.isAfter(now)) {
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

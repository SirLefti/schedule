package de.lefti.schedule;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashSet;
import java.util.Set;

public class Schedule {

	private final int _interval;
	private ChronoUnit _unit;
	private DayOfWeek _startDay = DayOfWeek.MONDAY;
	private Runnable _task;

	private boolean _alive = true;
	private boolean _targetTime = false;
	private long _lastExecution = 0;
	private long _nextExecution = 0;
	private int _targetHour = 0;
	private int _targetMinute = 0;
	private int _targetSecond = 0;

	private static final Set<Schedule> _scheduledTasks = new HashSet<>();
	private static final CoreScheduler _coreScheduler = new CoreScheduler();

	private static final class CoreScheduler implements Runnable {
		private boolean _alive = true;

		public void kill() {
			_alive = false;
		}

		public synchronized void wakeUp() {
			this.notify();
		}

		@Override
		public synchronized void run() {
			Set<Schedule> toDelete = new HashSet<>();
			while (_alive) {
				System.out.println("Running core scheduler");
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
		_targetTime = true;
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
			_targetHour = Integer.parseInt(values[0]);
			_targetMinute = Integer.parseInt(values[1]);
			_targetSecond = Integer.parseInt(values[2]);
		} else if (values.length == 2 && _unit == ChronoUnit.MINUTES) {
			_targetSecond = Integer.parseInt(values[1]);
		} else if (values.length == 2 && _unit == ChronoUnit.HOURS) {
			if (values[0].isEmpty()) {
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
	 */
	public void run(Runnable task) {
		_task = task;
		_scheduledTasks.add(this);
		_nextExecution = nextExecutionTimestamp();
		if (_scheduledTasks.size() == 1) {
			// This means, we added the first task, so we start the core scheduler
			new Thread(_coreScheduler).start();
		} else {
			_coreScheduler.wakeUp();
		}
	}

	/**
	 * Cancels the scheduled task. If the task is currently running, it will be finished. If not, the task is cancelled
	 * immediately.
	 */
	public void cancel() {
		_alive = false;
	}

	public static void shutdown() {
		_coreScheduler.kill();
	}

	private long nextExecutionTimestamp() {
		if (_lastExecution == 0 && !_targetTime) {
			return System.currentTimeMillis();
		} else if (!_targetTime) {
			return _lastExecution + _unit.getDuration().toMillis() * _interval;
		} else {
			final int DAYS_PER_WEEK = 7;
			ZonedDateTime now = ZonedDateTime.now();
			ZonedDateTime next = now.truncatedTo(ChronoUnit.SECONDS);
			TemporalUnit temporal = ChronoUnit.SECONDS;
			if (_unit == ChronoUnit.SECONDS) {
				next = next.plusSeconds(_interval);
				temporal = ChronoUnit.SECONDS;
			} else if (_unit == ChronoUnit.MINUTES) {
				next = next.withSecond(_targetSecond);
				temporal = ChronoUnit.MINUTES;
			} else if (_unit == ChronoUnit.HOURS) {
				next = next.withMinute(_targetMinute).withSecond(_targetSecond);
				temporal = ChronoUnit.HOURS;
			} else if (_unit == ChronoUnit.DAYS) {
				next = next.withHour(_targetHour).withMinute(_targetMinute).withSecond(_targetSecond);
				temporal = ChronoUnit.DAYS;
			} else if (_unit == ChronoUnit.WEEKS) {
				next = next.plusDays((_startDay.compareTo(next.getDayOfWeek()) + DAYS_PER_WEEK) % DAYS_PER_WEEK)
						.withHour(_targetHour).withMinute(_targetMinute).withSecond(_targetSecond);
				temporal = ChronoUnit.WEEKS;
			}
			if (next.isBefore(now)) {
				next = next.plus(1, temporal);
			}
			return next.toEpochSecond() * 1000;
		}
	}

	private boolean shouldRun() {
		return _nextExecution < System.currentTimeMillis();
	}
}

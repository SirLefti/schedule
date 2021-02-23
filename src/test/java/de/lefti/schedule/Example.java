package de.lefti.schedule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Example {

	public static void main(String[] args) {
		Schedule.every(10).seconds().run(() -> System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "] " + "Hello from scheduler every 10 seconds"));
		Schedule.every().minute().run(() -> System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "] " + "Hello from scheduler every minute"));
		Schedule.every().minute().at(":15").run(() -> System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "] " + "Hello from scheduler every minute at :15"));
		Schedule toCancel = Schedule.every().second().run(() -> System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "] " + "This will run only a few times"));
		Schedule.every().minute().at(":00").run(() -> {
			System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "] " + "Killing cancel task");
			toCancel.cancel();
		});
		Schedule.every().friday().run(() -> System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "] " + "Hello from scheduler on fridays"));
		Schedule.every().year().at("02-19 18:03:30").run(() -> System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "] " + "Hello from scheduler yearly on specific date"));
		Schedule.every().months().at("-01").run(() -> System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "] " + "Hello every month"));
	}
}

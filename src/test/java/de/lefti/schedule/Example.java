package de.lefti.schedule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Example {

	public static void main(String[] args) {
		Schedule.init(1);
		Schedule.every(10).seconds().run(() -> System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "] " + "Hello from scheduler every 10 seconds"));
		Schedule.every().minute().run(() -> System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "] " + "Hello from scheduler every minute"));
	}
}

# Schedule

Java task scheduling for humans. Execute runnable tasks periodically using a friendly syntax. Basically a port of the
Python lib [schedule](https://pypi.org/project/schedule/) by D. Bader to Java.

* chained syntax made for humans
* built-in scheduler
* no external dependencies

***

## Usage

```java
public class Example {
	public static void main(String[] args) {
		Runnable task = () -> System.out.println("Hello World!");

		Schedule.every(10).seconds().run(task);
		Schedule.every().hour().at(":30").run(task);
		Schedule.every().monday().at("00:30").run(task);
	}
}
```

## Installation
Replace **VERSION** key with the latest version available.

**Maven**
```xml
<dependency>
    <groupId>de.lefti</groupId>
    <artifactId>schedule</artifactId>
    <version>VERSION</version>
</dependency>
```

**Gradle**
```gradle
dependencies {
    compile 'de.lefti:schedule:VERSION'
}
```


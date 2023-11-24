package com.dutch.parking.misc;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

public class Utilities {
	public static LocalTime isBetweenNineAndEight(LocalTime time, String ps){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalTime startLocalTime = LocalTime.parse("21:00:00", formatter);
		LocalTime endLocalTime = LocalTime.parse("08:00:00", formatter);

		if (endLocalTime.isAfter(startLocalTime)) {
			if (startLocalTime.isBefore(time) && endLocalTime.isAfter(time)) {
				return ps.equals(ParkingStatusEnum.PARKING_REGISTERED.getValue())?LocalTime.of(8, 0, 0):
						LocalTime.of(21, 0, 0);
			}
		} else if (time.isAfter(startLocalTime) || time.isBefore(endLocalTime)) {
			return ps.equals(ParkingStatusEnum.PARKING_REGISTERED.getValue())?LocalTime.of(8, 0, 0):
					LocalTime.of(21, 0, 0);
		}
		return LocalTime.parse(time.toString(),formatter);
	}

	public static boolean isSameDay(LocalDateTime startDay, LocalDateTime endDay){
		return startDay.toLocalDate().equals(endDay.toLocalDate())?true:false;
	}

	public static long noOfSundayBetweenDates(LocalDateTime startDate, LocalDateTime endDate){
		return startDate.toLocalDate().datesUntil(endDate.toLocalDate())
				.filter(day->day.getDayOfWeek().equals(DayOfWeek.SUNDAY)).toList().size();
	}
}

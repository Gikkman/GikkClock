package com.gikk.clock.util;

/**
 *
 * @author Gikkman
 */
public class TimeFormatter {

    /**Parses the parameter amount of seconds into hours:minutes:seconds
	 *
	 * @param seconds The amount of secnds you want in timer-format
	 * @return A string formated hh:mm:ss
	 */
	public static String getHoursMinutesSeconds(Long seconds){
        if( seconds == null || seconds < 0L ){
            seconds = 0L;
        }
		Long h = seconds  / (60 * 60);
		Long m = (seconds / 60) % 60;
		Long s = seconds  % 60 % 60;

		return h + ":" + String.format("%02d:%02d", m, s);
	}

    /**Calculates how many seconds the parameter hours, minutes and seconds add upp to
	 *
	 * @param hours
	 * @param minutes
	 * @param seconds
	 * @return
	 */
	public static long getSeconds(Long hours, Long minutes, Long seconds){
		return hours * 60 * 60 + minutes * 60 + seconds;
	}
}

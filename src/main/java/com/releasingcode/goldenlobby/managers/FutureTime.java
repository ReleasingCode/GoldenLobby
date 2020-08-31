package com.releasingcode.goldenlobby.managers;

import com.releasingcode.goldenlobby.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FutureTime {
    /*
    (\d+)(mth(\d|\b)) => Month
    (\d+)(w(\d|\b)) => Week
    (\d+)(d(\d|\b)) => Day
    (\d+)(h(\d|\b)) => Hour
    (\d+)(\m(\d|\b)) => Minute
    (\d+)(s(\d|\b)) => Second

     */
    Integer month;
    Integer week;
    Integer day;
    Integer hour;
    Integer minute;
    Integer second;

    public static FutureTime parseByString(String text) {
        FutureTime futuretime = new FutureTime();
        for (TIME time : TIME.values()) {
            Pattern p = Pattern.compile("(\\d+)(" + time.getPrefix() + "(\\d|\\b))");
            Matcher matcher = p.matcher(text.toLowerCase());
            if (matcher.find()) {
                try {
                    Integer longer = Integer.parseInt(matcher.group(1));
                    switch (time) {
                        case MONTH: {
                            futuretime.setMonth(longer);
                            break;
                        }
                        case WEEK: {
                            futuretime.setWeek(longer);
                            break;
                        }
                        case DAY: {
                            futuretime.setDay(longer);
                            break;
                        }
                        case HOUR: {
                            futuretime.setHour(longer);
                            break;
                        }
                        case MINUTE: {
                            futuretime.setMinute(longer);
                            break;
                        }
                        case SECOND: {
                            futuretime.setSecond(longer);
                            break;
                        }
                    }
                } catch (NumberFormatException e) {
                    Utils.log("Formato incorrecto: " + e.getMessage() + " -> " + matcher.group(1));
                }
            }
        }
        return futuretime;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getWeek() {
        return week;
    }

    public void setWeek(Integer week) {
        this.week = week;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }

    public Integer getSecond() {
        return second;
    }

    public void setSecond(Integer second) {
        this.second = second;
    }

    public boolean isValid() {
        return getMonth() != null && getMonth() > 0
                || getWeek() != null && getWeek() > 0
                || getDay() != null && getDay() > 0
                || getHour() != null && getHour() > 0
                || getMinute() != null && getMinute() > 0
                || getSecond() != null && getSecond() > 0;
    }

    public String toString() {
        String builder = "";
        if (getMonth() != null && getMonth() > 0) {
            builder += "" + getMonth() + " " + (getMonth() == 1 ? "Mes" : "Meses") + " ";
        }
        if (getWeek() != null && getWeek() > 0) {
            builder += "" + getWeek() + " " + (getWeek() == 1 ? "Semana" : "Semanas") + " ";
        }
        if (getDay() != null && getDay() > 0) {
            builder += "" + getDay() + " " + (getDay() == 1 ? "Día" : "Dias") + " ";
        }
        if (getHour() != null && getHour() > 0) {
            builder += "" + getHour() + " " + (getHour() == 1 ? "Hora" : "Horas") + " ";
        }
        if (getMinute() != null && getMinute() > 0) {
            builder += "" + getMinute() + " " + (getMinute() == 1 ? "Minuto" : "Minutos") + " ";
        }
        if (getSecond() != null && getSecond() > 0) {
            builder += "" + getSecond() + " " + (getSecond() == 1 ? "Segundo" : "Segundos") + " ";
        }
        return builder.trim().isEmpty() ? "No hay un tiempo especificado" : builder.trim();
    }

    public enum TIME {
        MONTH("mth"),
        WEEK("w"),
        DAY("d"),
        HOUR("h"),
        MINUTE("m"),
        SECOND("s");
        //1h20m
        //20m1h
        //1mth
        //1mth1w1d20m3h1s
        //2w10h20m
        private final String prefix;

        TIME(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.bukkit.ferryban.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author RevvyDevvyDoo
 */


public class TimeConverter {

    public enum TimeUnit {
        MILISECONDS(1),
        SECONDS(1000, MILISECONDS, 1000, "s", "sec", "second", "seconds"),
        MINUTES(60, SECONDS, 60000, "min", "minute", "minutes"),
        HOURS(60, MINUTES, 3600000, "h", "hour", "hours"),
        DAYS(24, HOURS, 86400000, "d", "day", "days"),
        WEEKS(7, DAYS, 604800000, "w", "week", "weeks"),
        MONTHS(4, WEEKS, 2419200000L, "m", "month", "months"),
        YEARS(12, MONTHS, 29030400000L, "y", "year", "years");
        
        private final String plural;
        private final String singular;
        private final long time;
        private final TimeUnit parent;
        private final String[] keywords;
        private final long timestamp;
        
        private TimeUnit(long timestamp) {
            StringBuilder builder = new StringBuilder();
            builder.append(this.toString().toLowerCase());
            builder.setLength(builder.length() -1);
            this.singular = builder.toString();
            this.plural = this.toString().toLowerCase();
            this.time = 1;
            this.parent = null;
            this.timestamp = timestamp;
            this.keywords = null;
        }
        
        private TimeUnit(int time, TimeUnit parent, long timestamp, String... keywords){
            StringBuilder builder = new StringBuilder();
            builder.append(this.toString().toLowerCase());
            builder.setLength(builder.length() -1);
            this.keywords = keywords;
            this.singular = builder.toString();
            this.timestamp = timestamp;
            this.plural = this.toString().toLowerCase();
            this.time = time;
            this.parent = parent;
        }
        
        public String getPlural(){
            return plural;
        }
        
        public String getSingular(){
            return singular;
        }
        
        public String[] getKeyWords(){
            return keywords;
        }
        
        public long getTimeStamp(){
            return timestamp;
        }
        
        public long getTime(){
            if(parent == null){
                return time;
            } else {
                return parent.getTime() * this.time;
            }
        }
        
        public TimeUnit getParent(){
            return parent;
        }
    }
    
    public static String getMessage(long time, int maxUnits){
        long temptime = time;
        StringBuilder builder = new StringBuilder();
        TimeUnit[] values = TimeUnit.values();
        for(int i = 0; i < maxUnits; i++){
            for(int t = TimeUnit.values().length -1; t >= 0; t--){
                if(temptime > TimeUnit.values()[t].getTime()){
                    long amount = temptime / values[t].getTime();
                    if(amount != 0){
                        temptime = temptime - (amount * values[t].getTime());
                        builder.append(amount).append(" ").append(amount == 1 ? values[t].getSingular() : values[t].getPlural());
                        builder.append(" ");
                        break;
                    }
                }
            }
        }
        return builder.toString();
    }
    
    public static long getLong(String message){
        long l = 0;
        Pattern p = Pattern.compile("(\\d+)([a-z]+)",Pattern.CASE_INSENSITIVE);
        Matcher matcher =  p.matcher(message);
        while (matcher.find()) {
            for(TimeUnit unit : TimeUnit.values()){
                if(unit.keywords == null){
                    continue;
                }
                for(String s : unit.getKeyWords()){
                    if(s.equalsIgnoreCase(matcher.group(2))){
                        l = l + (unit.getTimeStamp() * Integer.parseInt(matcher.group(1)));
                    }
                }
            }
        }
        return l;
    }
}

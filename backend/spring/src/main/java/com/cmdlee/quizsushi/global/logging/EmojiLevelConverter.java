
package com.cmdlee.quizsushi.global.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;


public class EmojiLevelConverter extends ClassicConverter {

    @Override
    public String convert(ILoggingEvent event) {
        Level level = event.getLevel();
        return switch (level.toInt()) {
            case Level.ERROR_INT -> "❌ ERROR";
            case Level.WARN_INT  -> "⚠️ WARN";
            case Level.INFO_INT  -> "ℹ️ INFO";
            case Level.DEBUG_INT -> "🐛 DEBUG";
            case Level.TRACE_INT -> "🔍 TRACE";
            default              -> "🔸" + level.levelStr;
        };
    }
}
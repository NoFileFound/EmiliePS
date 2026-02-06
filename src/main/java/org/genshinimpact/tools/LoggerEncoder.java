package org.genshinimpact.tools;

// Imports
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.fusesource.jansi.Ansi;

public class LoggerEncoder extends PatternLayoutEncoder {
    @Override
    public byte[] encode(ILoggingEvent event) {
        return colorize(event).getBytes(StandardCharsets.UTF_8);
    }

    private String colorize(ILoggingEvent event) {
        String timestamp = Ansi.ansi().fg(Ansi.Color.WHITE).a("[" + DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault()).format(Instant.ofEpochSecond(event.getTimeStamp())) + "]").reset().toString();
        String levelColor = switch (event.getLevel().toInt()) {
            case Level.INFO_INT -> Ansi.ansi().fg(Ansi.Color.GREEN).toString();
            case Level.WARN_INT -> Ansi.ansi().fg(Ansi.Color.YELLOW).toString();
            case Level.ERROR_INT -> Ansi.ansi().fg(Ansi.Color.RED).toString();
            case Level.DEBUG_INT -> Ansi.ansi().fg(Ansi.Color.MAGENTA).toString();
            default -> Ansi.ansi().fg(Ansi.Color.DEFAULT).toString();
        };

        String level = levelColor + "[" + event.getLevel() + "]" + Ansi.ansi().reset();
        String msg = Ansi.ansi().fg(Ansi.Color.DEFAULT).a(" " + event.getFormattedMessage()).reset().toString();
        return timestamp + level + msg + System.lineSeparator();
    }
}
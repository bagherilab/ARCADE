package arcade.core;

import java.util.logging.*;
import java.util.Date;

public final class Main {
	public static void main(String[] args) { }
	
	public static Logger updateLogger() {
		// Setup logger.
		Logger logger = Logger.getLogger("arcade");
		logger.setUseParentHandlers(false);
		
		// Change logger display format.
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new SimpleFormatter() {
			private static final String FORMAT = "%1$tF %1$tT %2$-7s %3$-30s : %4$s %n";
			public synchronized String format(LogRecord lr) {
				return String.format(FORMAT,
					new Date(lr.getMillis()),
					lr.getLevel().getLocalizedName(),
					lr.getSourceClassName(),
					lr.getMessage()
				);
			}
		});
		
		logger.addHandler(handler);
		return Logger.getLogger(Main.class.getName());
	}
}
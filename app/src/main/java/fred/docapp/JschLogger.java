package fred.docapp;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by fred on 17/11/15.
 *
 */
public class JschLogger implements com.jcraft.jsch.Logger {
        private Map<Integer, Level> levels = new HashMap<Integer, Level>();

        private Logger LOGGER = null;


        public JschLogger() {
            // Mapping between JSch levels and our own levels
            levels.put(DEBUG, Level.FINE);
            levels.put(INFO, Level.INFO);
            levels.put(WARN, Level.WARNING);
            levels.put(ERROR, Level.SEVERE);
            levels.put(FATAL, Level.SEVERE);

            LOGGER = Logger.getLogger("fred.docapp"); // Anything you want here, depending on your logging framework
        }

        void setLevel(int newLevel) {
            Level level = levels.get(newLevel);
            LOGGER.setLevel(level);
        }

        @Override
        public boolean isEnabled(int pLevel) {
            return true; // here, all levels enabled
        }

        @Override
        public void log(int pLevel, String pMessage) {
            Level level = levels.get(pLevel);
            if (level == null) {
                level = Level.SEVERE;
            }
            LOGGER.log(level, pMessage); // logging-framework dependent...
        }
    }


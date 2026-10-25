package ca.dauqui.spacecolonygame.util;

import ca.dauqui.spacecolonygame.gamedata.starsystem.StarDataColumn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

/**
 * this small program will generate all the localization files for the stars with name set at the star name and DESCRIPTION at a generic text
 */
public class LocalisationTextStarGenerator {
    private static final Logger LOG = Logger.getLogger(LocalisationTextStarGenerator.class.getName());

    private static final String STAR_LOCALISATION_PATH = "src/main/resources/ca/dauqui/spacecolonygame/localization/data/";
    private static final String STAR_DATA_PATH = "src/main/resources/ca/dauqui/spacecolonygame/data/starsystems/";
    private static final  double MAX_DISTANCE = 25.0;


    static void main() {
        for (java.util.logging.Handler handler : LOG.getHandlers()) {
            LOG.removeHandler(handler);
        }

        // Create a new StreamHandler to write to System.out
        StreamHandler sh = new StreamHandler(System.out, new SimpleFormatter());

        // Set the logging level for the handler to ALL to ensure all messages are processed
        sh.setLevel(Level.ALL);

        // Add the new handler to the logger
        LOG.addHandler(sh);

        // Set the logger's level to ALL to ensure all messages are passed to the handler
        LOG.setLevel(Level.ALL);



        Path path = new File(STAR_DATA_PATH + "StarData.txt").toPath();
        try {
            Path file = Files.createFile(new File(STAR_LOCALISATION_PATH + "star_data_en.properties").toPath());

            createFile(path, file);
        } catch (FileAlreadyExistsException _) {
            LOG.warning(() -> "localization file already exists");
        } catch (IOException e){
            LOG.severe(e::getLocalizedMessage);
        }
    }

    private static void createFile(Path path, Path file) {
        String line = "";
        try (BufferedReader br = Files.newBufferedReader(path);
             BufferedWriter bw = Files.newBufferedWriter(file)) {
            line = br.readLine();

            while (line != null) {
                if (!line.isBlank() && !line.startsWith("#")) {
                    String[] text = line.split(",");

                    if (Double.parseDouble(text[StarDataColumn.DIST.ordinal()]) > MAX_DISTANCE){
                        break;
                    }

                    String type = text[StarDataColumn.TY.ordinal()].trim().replace("?", "");

                    String name = text[StarDataColumn.NAME.ordinal()].trim();
                    switch (type) {
                        case "S":
                            String prefix = name.replace(" ", "_") + ".";
                            bw.write("# String data for " + name + "\n\n");
                            bw.write(prefix + "NAME " + name + "\n");
                            bw.write(prefix + "DESCRIPTION A temporary description for " + name + "\n");
                            bw.write("\n");
                            bw.flush();
                            break;
                        case "PJ", "PN", "PT":
                            LOG.info(() -> "Skipping " + name + " of planet type " + type);
                            break;
                        default:
                            LOG.info(() -> "Skipping " + name + " of type " + type);
                    }
                }

                line = br.readLine();
            }

        } catch (IOException _) {
            LOG.severe(() -> "Error loading localization file : " + path);
        } catch (Exception _) {
            String finalLine = line;
            LOG.warning(() -> "tries to do something with " + finalLine);
        }
    }
}
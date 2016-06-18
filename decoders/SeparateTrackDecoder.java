package decoders;

import javax.sound.midi.Sequence;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ra on 18.06.16.
 * Part of midiParser, in package PACKAGE_NAME.
 */
public class SeparateTrackDecoder implements Decoder {
    private Path outputDir = Paths.get("assets/midi/csv/tracks/");

    @Override
    public void decode(Sequence sequence, String name) {
        for (int i = 0; i < sequence.getTracks().length; i++) {
            Path file = Paths.get(outputDir + "track" + i + ".csv");
            List<String> lines = new ArrayList<>();

            try {
                Files.write(file, lines, Charset.forName("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setOutputDir(Path path) {
        this.outputDir = path;
    }
}

package decoders;

import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static decoders.midiUtil.getKeyName;

/**
 * Created by ra on 18.06.16.
 * Part of midiParser, in package decoders.
 */
public class SeparateTrackDecoder implements Decoder<ShortMessage> {
    private Path outputDir = Paths.get("assets/midi/csv/tracks/");

    public SeparateTrackDecoder(Path outputDir) {
        this.outputDir = Paths.get(outputDir.endsWith("/") ? outputDir + "tracks/" : outputDir + "/tracks/");
    }

    @Override
    public void decode(Sequence sequence, String name) {
        for (int i = 0; i < sequence.getTracks().length; i++) {
            Path file = Paths.get(outputDir + "track_" + i + ".csv");
            List<String> lines = new ArrayList<>();

            try {
                Files.write(file, lines, Charset.forName("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String decodeMessage(ShortMessage message) {
        String strMessage;
        switch (message.getCommand()) {
            case 0x80:
                strMessage = "note Off " + getKeyName(message.getData1()) + ", velocity " + message.getData2();
                break;

            case 0x90:
                strMessage = "note On " + getKeyName(message.getData1()) + ", velocity " + message.getData2();
                break;
           default:
                strMessage = "unknown message: status = " + message.getStatus() + "; byte1 = " + message.getData1() + "; byte2 = " + message.getData2();
                break;
        }
        return strMessage;
    }

    @Override
    public void setOutputDir(Path path) {
        this.outputDir = path;
    }
}

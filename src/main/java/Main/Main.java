package Main;

import decoders.MetaInfoDecoder;
import decoders.ShortmessageDecoder;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by ra on 18.06.16.
 * Part of midiParser, in package Main.
 */
class Main {

    public static void main(String[] args) throws InvalidMidiDataException, IOException {
        String inFilePath;
        String assetPath = "assets/midi/";
        String filename = "Bach_BrichAn.mid";
        String outputPath = assetPath + "csv/" + filename + "/";
        try {
            Files.createDirectories(Paths.get(outputPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (args.length != 1) {
            inFilePath = assetPath + filename;
        } else {
            inFilePath = args[0];
        }

        File midiFile = new File(inFilePath);

        Sequence sequence = MidiSystem.getSequence(midiFile);

        try {
            MetaInfoDecoder.toFile(MetaInfoDecoder.decode(sequence), Paths.get(outputPath), filename);
            ShortmessageDecoder.toFile(ShortmessageDecoder.decode(sequence), Paths.get(outputPath), filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

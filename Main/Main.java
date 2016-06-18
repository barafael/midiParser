package Main;

import decoders.MetaInfoDecoder;
import decoders.SeparateTrackDecoder;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by ra on 18.06.16.
 * Part of midiParser, in package Main.
 */
class Main {

    public static void main(String[] args) {
        String inFilePath;
        String assetPath = "assets/midi/";
        String filename = "giordani_caro.mid";
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

        Sequence sequence = null;
        try {
            sequence = MidiSystem.getSequence(midiFile);
        } catch (InvalidMidiDataException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        decoders.Decoder<MetaMessage> metaDecoder = new MetaInfoDecoder(Paths.get(outputPath), filename, sequence);
        decoders.Decoder<ShortMessage> shortMessageDecoder = new SeparateTrackDecoder(Paths.get(outputPath), sequence);
        metaDecoder.decode();
        shortMessageDecoder.decode();
    }
}

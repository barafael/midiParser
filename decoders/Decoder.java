package decoders;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import java.nio.file.Path;

/**
 * Part of midiParser, in package decoders.
 */
interface Decoder<T extends MidiMessage> {
    void decode(Sequence sequence, String filename);

    String decodeMessage(T message);

    void setOutputDir(Path path);
}

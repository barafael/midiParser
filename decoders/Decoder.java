package decoders;

import javax.sound.midi.MidiMessage;

/**
 * Part of midiParser, in package decoders.
 */
public interface Decoder<T extends MidiMessage> {
    void decode();

    String decodeMessage(T message, long tick);

}

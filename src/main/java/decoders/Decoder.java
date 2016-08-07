package decoders;

import javax.sound.midi.MidiMessage;

/**
 * Pointless interface
 */
public interface Decoder<T extends MidiMessage> {
    void decode();

    String decodeMessage(T message, long tick);

}

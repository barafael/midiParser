package decoders;

import midiUtil.Event;
import midiUtil.MetaEvent;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import java.util.List;

/**
 * Pointless interface
 */
public interface Decoder<T extends MidiMessage, U extends Event> {
    List<U> decode(Sequence sequence);

    void decodeMessage(T message, long tick);
}

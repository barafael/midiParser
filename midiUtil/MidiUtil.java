package midiUtil;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;

/**
 * Created by ra on 18.06.16.
 * Part of midiParser, in package midiUtil.
 */
public class MidiUtil {

    enum keyNamesSharp {C, Cs, D, Ds, E, F, Fs, G, Gs, A, As, B}

    enum keyNamesFlat {C, Db, D, Eb, E, F, Gb, G, Ab, A, Bb, Cb}

    public enum KeySig {Cb, Gb, Db, Ab, Eb, Bb, F, C, G, D, A, E, B, Fs, Cs}

    // convert from microseconds per quarter note to beats per minute and vice versa
    public static float convertTempo(float value) {
        if (value <= 0) {
            value = 0.1f;
        }
        return 60000000.0f / value;
    }

    public static String getKeyName(int nKeyNumber, boolean sharp) {
        if (nKeyNumber > 127) {
            return "illegal value";
        } else {
            int nNote = nKeyNumber % 12;
            int nOctave = nKeyNumber / 12;
            return sharp ? keyNamesSharp.values()[nNote].toString() + (nOctave - 1)
                    : keyNamesFlat.values()[nNote].toString() + (nOctave - 1);
        }
    }

    private static final char[] hexDigits =
            {'0', '1', '2', '3',
                    '4', '5', '6', '7',
                    '8', '9', 'A', 'B',
                    'C', 'D', 'E', 'F'
            };

    public static String getHexString(byte[] bytes) {
        StringBuffer buffer = new StringBuffer(bytes.length * 3 + 2);
        for (byte anAByte : bytes) {
            buffer.append(' ');
            buffer.append(hexDigits[(anAByte & 0xF0) >> 4]);
            buffer.append(hexDigits[anAByte & 0x0F]);
        }
        return new String(buffer);
    }

    public static boolean determineKey(Sequence sequence) {
        for (int trackIndex = 0; trackIndex < sequence.getTracks().length; trackIndex++) {
            for (int eventIndex = 0; eventIndex < sequence.getTracks()[trackIndex].size(); eventIndex++) {
                MidiEvent event = sequence.getTracks()[trackIndex].get(eventIndex);
                if (event.getMessage() instanceof MetaMessage) {
                    if (((MetaMessage) event.getMessage()).getType() == 0x59) { // key signature
                        byte[] data = ((MetaMessage) event.getMessage()).getData();
                        return KeySig.values()[data[0] + 7].toString().contains("#");
                    }
                }
            }
        }
        return true;
    }
}

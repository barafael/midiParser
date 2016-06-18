package decoders;

/**
 * Created by ra on 18.06.16.
 * Part of midiParser, in package PACKAGE_NAME.
 */
class midiUtil {

    static final String[] strKeyNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    static final String[] strKeySignatures = {"Cb", "Gb", "Db", "Ab", "Eb", "Bb", "F", "C", "G", "D", "A", "E", "B", "F#", "C#"};
    static final String[] SYSTEM_MESSAGE_TEXT =
            {
                    "System Exclusive (should not be in ShortMessage!)",
                    "MTC Quarter Frame: ",
                    "Song Position: ",
                    "Song Select: ",
                    "Undefined",
                    "Undefined",
                    "Tune Request",
                    "End of SysEx (should not be in ShortMessage!)",
                    "Timing clock",
                    "Undefined",
                    "Start",
                    "Continue",
                    "Stop",
                    "Undefined",
                    "Active Sensing",
                    "System Reset"
            };

    static final String[] QUARTER_FRAME_MESSAGE_TEXT =
            {
                    "frame count LS: ",
                    "frame count MS: ",
                    "seconds count LS: ",
                    "seconds count MS: ",
                    "minutes count LS: ",
                    "minutes count MS: ",
                    "hours count LS: ",
                    "hours count MS: "
            };

    static final String[] FRAME_TYPE_TEXT =
            {
                    "24 frames/second",
                    "25 frames/second",
                    "30 frames/second (drop)",
                    "30 frames/second (non-drop)",
            };

    // convert from microseconds per quarter note to beats per minute and vice versa
    static float convertTempo(float value) {
        if (value <= 0) {
            value = 0.1f;
        }
        return 60000000.0f / value;
    }


    private static final char[] hexDigits =
            {'0', '1', '2', '3',
                    '4', '5', '6', '7',
                    '8', '9', 'A', 'B',
                    'C', 'D', 'E', 'F'
            };

    static String getHexString(byte[] bytes) {
        StringBuffer buffer = new StringBuffer(bytes.length * 3 + 2);
        for (byte anAByte : bytes) {
            buffer.append(' ');
            buffer.append(hexDigits[(anAByte & 0xF0) >> 4]);
            buffer.append(hexDigits[anAByte & 0x0F]);
        }
        return new String(buffer);
    }
}

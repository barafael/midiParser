package midiUtil;

/**
 * Created by ra on 18.06.16.
 * Part of midiParser, in package midiUtil.
 */
public class MetaEvent extends Event {

    public enum EventType {
        SEQ_NUM("Sequence Number: "),
        TEXT("Text Event: "),
        COPYRIGHT_NOTICE("Copyright Notice: "),
        SEQ_TRACK_NAME("Sequence/Track Name: "),
        INSTRUMENT_CHANGE("Instrument Name: "),
        LYRICS("Lyric: "),
        MARKER_TEXT("Marker: "),
        CUE_POINT("Cue Point: "),
        CHANNEL_PREFIX("MIDI Channel Prefix: "),
        TRACK_END("End of Track"),
        TEMPO_CHANGE("New tempo in bpm: "),
        SMPTE_OFFSET("SMTPE Offset: "),
        TIMESIG_CHANGE("New Time signature: "),
        KEYSIG_CHANGE("Key Signature: "),
        UNKNOWN("Unknown Meta event: "),
        COMMENT("-- "),
        TRACK_CHANGE("New track: "),
        ;

        private final String message;

        EventType(String message) {
            this.message = message;
        }
    }

    private final EventType type;

    private final String message;

    public final long tick;

    public MetaEvent(String message, long tick, EventType type) {
        this.message = type.message + message;
        this.tick = tick;
        this.type = type;
    }

    /**
     * Change this to change csv output of notes; TODO generalize this
     */
    @Override
    public String toString() {
        return tick + ", " + message;
    }

    @Override
    public String toCSV() {
        return toString();
    }
}

package midiUtil;

/**
 * Created by ra on 18.06.16.
 * Part of midiParser, in package midiUtil.
 */
public class NoteEvent extends Event {
    private final static int DEFAULT_OCTAVE = 1;
    private static final int DEFAULT_VELOCITY = 60;
    private static final long FALLBACK_DURATION = 200;

    private final String name;
    private final int octave;

    public final long startTick;

    private long duration;

    private final int velocity;

    public NoteEvent(String name, long tick) {
        this(name, tick, DEFAULT_VELOCITY);
    }

    public NoteEvent(String name, long startTick, int velocity) {
        if (name.matches(".*\\d")) { // Note name includes octave number
            this.octave = Integer.parseInt(name.substring(name.length() - 1));
            name = name.replaceAll("\\d", "");
        } else {
            this.octave = DEFAULT_OCTAVE;
        }
        this.name = name;
        this.startTick = startTick;
        this.velocity = velocity;
    }

    public void setDefaultDuration() {
        this.duration = FALLBACK_DURATION;
    }

    public void setDuration(long endTick) {
        if (startTick <= endTick) {
            this.duration = endTick - startTick;
        } else {
            this.setDuration(startTick + FALLBACK_DURATION);
            throw new IllegalArgumentException("Note ended before it began! " +
                    "This program is not compatible with MIDI files generated while travelling faster than light.");
        }
    }

    /**
     * Change this to change csv output of notes; TODO generalize this
     */
    @Override
    public String toString() {
        return startTick + ", " + name + ", " + octave + ", " +
                duration + ", " + velocity;
    }

    public String getName() {
        return name + octave;
    }

    @Override
    public String toCSV() {
        return toString();
    }
}

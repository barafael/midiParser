package midiUtil;

/**
 * Created by ra on 18.06.16.
 * Part of midiParser, in package midiUtil.
 */
public class Note {
    private final String name;
    private final int octave;

    public long getStartTick() {
        return startTick;
    }

    private final long startTick;
    private final int velocity;
    private long endTick = -1;

    private long duration = -1;

    public Note(String name, long startTick, int velocity) {
        if (name.matches(".*\\d")) { // Notename included octave
            this.octave = Integer.parseInt(name.substring(name.length() - 1));
            name = name.replaceAll("\\d", "");
        } else {
            this.octave = -1;
        }
        this.name = name;
        this.startTick = startTick;
        this.velocity = velocity;
    }

    public void setEndTickAndDuration(long endTick) {
        if (!isFinal()) {
            this.endTick = endTick;
            this.duration = endTick - startTick;
        }
    }

    @Override
    public String toString() {
        if (isFinal()) {
            return startTick + ", " + name + ", " + octave + ", " +
                    duration + ", " + velocity;
        } else {
            System.err.println("Warning: non-finalised note toString() called!");
            return startTick + ", " + name + ", " + velocity;
        }
    }

    private boolean isFinal() {
        return endTick != -1 && duration != -1;
    }

    public String getName() {
        return octave == -1 ? name : name + octave;
    }
}

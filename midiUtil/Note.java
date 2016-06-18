package midiUtil;

/**
 * Created by ra on 18.06.16.
 * Part of midiParser, in package midiUtil.
 */
public class Note {
    private final String name;

    public long getStartTick() {
        return startTick;
    }

    private final long startTick;
    private final int velocity;
    private long endTick = -1;

    private long duration = -1;

    private Note(String name, long startTick, long endTick, int velocity) {
        this.name = name;
        this.startTick = startTick;
        this.endTick = endTick;
        this.duration = endTick - startTick;
        this.velocity = velocity;
    }

    public Note(String name, long startTick, int velocity) {
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
            return startTick + ", " + name + ", " +
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
        return name;
    }
}

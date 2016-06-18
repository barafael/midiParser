package decoders;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static decoders.midiUtil.getKeyName;

/**
 * Created by ra on 18.06.16.
 * Part of midiParser, in package decoders.
 */
public class SeparateTrackDecoder implements Decoder<ShortMessage> {
    private Path outputDir = Paths.get("assets/midi/csv/tracks/");

    public SeparateTrackDecoder(Path outputDir) {
        this.outputDir = Paths.get(outputDir.endsWith("/") ? outputDir + "tracks/" : outputDir + "/tracks/");
    }

    List<Note> currentlyPlayingNotes = new ArrayList<>();
    Map<Long, List<Note>> notes = new TreeMap<>(); // traversal in natural order of Long

    @Override
    public void decode(Sequence sequence, String name) {
        for (int trackIndex = 0; trackIndex < sequence.getTracks().length; trackIndex++) {
            currentlyPlayingNotes.clear();
            notes.clear();
            Path file = Paths.get(outputDir + "track_" + trackIndex + ".csv");
            Track track = sequence.getTracks()[trackIndex];
            for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
                MidiEvent event = track.get(eventIndex);
                if (event.getMessage() instanceof ShortMessage) {
                    long tick = event.getTick();
                    decodeMessage((ShortMessage) event.getMessage(), tick);
                }
            }
            List<String> lines = new ArrayList<>();
            notes.entrySet().stream().forEach(entry -> entry.getValue().forEach(note -> lines.add(note.toString())));
            try {
                Files.write(file, lines, Charset.forName("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String decodeMessage(ShortMessage message, long tick) {
        String strMessage;
        String notename;
        Note currentNote;
        switch (message.getCommand()) {
            case 0x80:
                notename = getKeyName(message.getData1());
                System.out.println("note on parsed: " + notename);
                currentNote = new Note(notename, tick, message.getData2());
                currentlyPlayingNotes.add(currentNote);
                strMessage = "note On " + notename + ", velocity " + message.getData2();
                break;

            case 0x90:
                notename = getKeyName(message.getData1());
                System.out.println("note off parsed: " + notename);
                int noteIndex = -1;
                for (int i = 0; i < currentlyPlayingNotes.size(); i++) {
                    if (currentlyPlayingNotes.get(i).name.equals(notename)) {
                        noteIndex = i;
                        break;
                    }
                }
                if (noteIndex >= 0) {
                    currentNote = currentlyPlayingNotes.get(noteIndex);
                    currentNote.setEndTickAndDuration(tick);
                    currentlyPlayingNotes.remove(currentNote);
                    if (notes.containsKey(currentNote.startTick)) {
                        notes.get(currentNote.startTick).add(currentNote);
                    } else {
                        List<Note> chord = new ArrayList<>();
                        chord.add(currentNote);
                        notes.put(currentNote.startTick, chord);
                    }
                } else {
                    System.err.println("corresponding note on event not found!");
                }

                strMessage = "note Off " + notename + ", velocity " + message.getData2();
                break;
            default:
                strMessage = "unknown message: status = " + message.getStatus() + "; byte1 = " + message.getData1() + "; byte2 = " + message.getData2();
                break;
        }
        return strMessage;
    }

    @Override
    public void setOutputDir(Path path) {
        this.outputDir = path;
    }

    private class Note {
        final String name;
        final long startTick;
        final int velocity;
        private long endTick = -1;

        private long duration = -1;

        private Note(String name, long startTick, long endTick, int velocity) {
            this.name = name;
            this.startTick = startTick;
            this.endTick = endTick;
            this.duration = endTick - startTick;
            this.velocity = velocity;
        }

        private Note(String name, long startTick, int velocity) {
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
                StringBuilder buddy = new StringBuilder();
                buddy.append(startTick).append(", ").append(name).append(", ")
                        .append(duration).append(", ").append(velocity);
                return buddy.toString();
            } else {
                System.err.println("Warning: non-finalised note toString() called!");
                return startTick + ", " + name + ", " + velocity;
            }
        }

        public long getDuration() {
            return duration;
        }

        public boolean isFinal() {
            return endTick == -1 || duration == -1;
        }
    }
}

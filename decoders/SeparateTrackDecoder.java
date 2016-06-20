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
import java.util.*;

import static midiUtil.MidiUtil.determineKey;
import static midiUtil.MidiUtil.getKeyName;

/**
 * Created by ra on 18.06.16.
 * Part of midiParser, in package decoders.
 */
public class SeparateTrackDecoder implements Decoder<ShortMessage> {
    private static final short FALLBACK_LENGTH = 250;
    private final Path outputDir;
    private final Sequence sequence;
    private final boolean isSharp;

    public SeparateTrackDecoder(Path outputDir, Sequence sequence) {
        this.sequence = sequence;
        isSharp = determineKey(sequence);
        this.outputDir = outputDir.resolve(Paths.get("tracks"));
        try {
            Files.createDirectories(this.outputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final List<midiUtil.Note> currentlyPlayingNotes = new ArrayList<>();
    private final Map<Long, List<midiUtil.Note>> notes = new TreeMap<>(); // traversal in natural order of Long(i.e. chronological, since the tick is the key

    @Override
    public void decode() {
        for (int trackIndex = 0; trackIndex < sequence.getTracks().length; trackIndex++) {
            currentlyPlayingNotes.clear();
            notes.clear();
            Path file = outputDir.resolve(Paths.get("track" + trackIndex + ".csv"));
            Track track = sequence.getTracks()[trackIndex];
            for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
                MidiEvent event = track.get(eventIndex);
                if (event.getMessage() instanceof ShortMessage) {
                    long tick = event.getTick();
                    decodeMessage((ShortMessage) event.getMessage(), tick);
                }
            }
            if (notes.isEmpty()) { // no notes or note offs parsed
                if (currentlyPlayingNotes.size() > 10) { // but there are still notes which weren't ended(missing note off...)
                    currentlyPlayingNotes.stream().forEach(note -> note.setEndTickAndDuration(note.getStartTick() + FALLBACK_LENGTH));
                } else {
                    continue;
                }
            }
            List<String> lines = new ArrayList<>();
            notes.entrySet().stream().forEach(entry -> entry.getValue().forEach(note -> lines.add(note.toString())));
            try {
                if (!Files.exists(file)) {
                    Files.createFile(file);
                }
                Files.write(file, lines, Charset.forName("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String decodeMessage(ShortMessage message, long tick) {
        String strMessage;
        switch (message.getCommand()) {
            case 0x80: // note off
                strMessage = parseNoteOff(message, tick);
                break;

            case 0x90: // note on
                strMessage = parseNoteOn(message, tick);
                break;
            default:
                strMessage = "unknown message: status = " + message.getStatus() + "; byte1 = " + message.getData1() + "; byte2 = " + message.getData2();
                break;
        }
        return strMessage;
    }

    private String parseNoteOn(ShortMessage message, long tick) {
        if (message.getData2() == 0) { // Fake note off signal
            return parseNoteOff(message, tick);
        }
        String notename = getKeyName(message.getData1(), isSharp);
        System.out.println(tick + " note on parsed: " + notename + ", " + message.getData2());
        midiUtil.Note currentNote = new midiUtil.Note(notename, tick, message.getData2());
        currentlyPlayingNotes.add(currentNote);
        return "note On " + notename + ", velocity " + message.getData2();
    }

    private String parseNoteOff(ShortMessage message, long tick) {
        String notename = getKeyName(message.getData1(), isSharp);
        System.out.println(tick + " note off parsed: " + notename);
        int noteIndex = -1;
        for (int i = 0; i < currentlyPlayingNotes.size(); i++) {
            if (currentlyPlayingNotes.get(i).getName().equals(notename)) {
                noteIndex = i;
                break;
            }
        }
        if (noteIndex >= 0) {
            midiUtil.Note currentNote = currentlyPlayingNotes.get(noteIndex);
            currentNote.setEndTickAndDuration(tick);
            currentlyPlayingNotes.remove(currentNote);
            if (notes.containsKey(currentNote.getStartTick())) {
                notes.get(currentNote.getStartTick()).add(currentNote);
            } else {
                List<midiUtil.Note> chord = new ArrayList<>();
                chord.add(currentNote);
                notes.put(currentNote.getStartTick(), chord);
            }
        } else {
            System.err.println("corresponding note on event not found!");
        }

        return "note Off " + notename + ", velocity " + message.getData2();
    }
}

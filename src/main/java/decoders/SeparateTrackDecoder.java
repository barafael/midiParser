package decoders;

import midiUtil.Note;

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
import java.util.stream.Collectors;

import static midiUtil.MidiUtil.isMajor;
import static midiUtil.MidiUtil.getKeyName;

/**
 * Saves all the shortmessages(note on/off) in tracks to one file per track in outputDir/trackN.csv.
 */
public class SeparateTrackDecoder /*implements Decoder<ShortMessage, Note>*/ {

    /*@Override*/
    public List<List<Note>> decode(Sequence sequence) {
        boolean isMajor = isMajor(sequence);

        List<List<Note>> trackList = new ArrayList<>(sequence.getTracks().length);

        for (int trackIndex = 0; trackIndex < sequence.getTracks().length; trackIndex++) {
            Set<Note> currentlyPlayingNotes = new HashSet<>();
            List<Note> notes = new ArrayList<>();

            Track track = sequence.getTracks()[trackIndex];

            for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
                MidiEvent event = track.get(eventIndex);
                if (event.getMessage() instanceof ShortMessage) { // only interested in shortmessages
                    long tick = event.getTick();
                    decodeMessage((ShortMessage) event.getMessage(), tick, isMajor, currentlyPlayingNotes, notes);
                }
            }
            if (notes.isEmpty()) { // no notes or note offs parsed, or more note on's than note off's
                if (currentlyPlayingNotes.size() > 0) { // there are still notes which weren't ended(missing note off...)
                    currentlyPlayingNotes.forEach(Note::setDefaultDuration);
                } else {
                    continue;
                }
            }
            trackList.add(trackIndex, notes);
        }
        return trackList;
    }

    private void decodeMessage(ShortMessage message, long tick, boolean isMajor, Set<Note> currentlyPlayingNotes,
                               List<Note> notes) {
        switch (message.getCommand()) {
            case 0x80: // note off
                parseNoteOff(message, tick, isMajor, currentlyPlayingNotes, notes);
                break;

            case 0x90: // note on or note 'onff' (note on with velocity 0, effectively note off)
                parseNoteOn(message, tick, isMajor, currentlyPlayingNotes, notes);
                break;
            default:
                throw new IllegalArgumentException("unknown shortmessage: status = " + message.getStatus() +
                        "; byte1 = " + message.getData1() + "; byte2 = " + message.getData2());
        }
    }

    private void parseNoteOff(ShortMessage message, long tick, boolean isMajor, Set<Note> currentlyPlayingNotes,
                              List<Note> notes) {
        String notename = getKeyName(message.getData1(), isMajor);
        Set<Note> stoppedNotes = currentlyPlayingNotes.stream().filter(note -> note.getName().equals(notename))
                .collect(Collectors.toSet());
        for (Note note : stoppedNotes) {
            note.setDuration(tick);
            notes.add(note);
            currentlyPlayingNotes.remove(note); // note stopped playing
        }
    }

    private void parseNoteOn(ShortMessage message, long tick, boolean isMajor, Set<Note> currentlyPlayingNotes,
                             List<Note> notes) {
        if (message.getData2() == 0) { // Fake note off signal (velocity = 0)
            parseNoteOff(message, tick, isMajor, currentlyPlayingNotes, notes);
        }
        String notename = getKeyName(message.getData1(), isMajor);
        int velocity = message.getData2();
        Note currentNote = new Note(notename, tick, velocity);
        currentlyPlayingNotes.add(currentNote);
    }

    // TODO this method should write each track in a separate file
    public void toFile(List<List<Note>> trackList, Path outputDir) throws IOException {
        outputDir = outputDir.resolve(Paths.get("tracks"));
        Files.createDirectories(outputDir);

        List<String> lines = new ArrayList<>();
        Path file = outputDir.resolve(Paths.get("track" + 7 + ".csv"));
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
        Files.write(file, lines, Charset.forName("UTF-8"));
    }
}

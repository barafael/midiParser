package decoders;

import midiUtil.NoteEvent;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static midiUtil.MidiUtil.getKeyName;
import static midiUtil.MidiUtil.isMajor;

/**
 * Decodes all the shortmessages(note on/off) in any track to one list
 */
public class ShortmessageDecoder /*implements Decoder<ShortMessage, NoteEvent>*/ {

    //TODO s/decode/parse/g
    //TODO oake private
    /*@Override*/
    private static List<NoteEvent> decode(final Sequence sequence) {
        boolean isMajor = isMajor(sequence);

        Set<NoteEvent> currentlyPlayingNotes = new HashSet<>(); // Notes for which no note off has been encountered

        // traversal in natural order of Long(i.e. chronological, since the tick is the key.
        // Several notes can occur at same tick. (Or can they?)
        Map<Long, List<NoteEvent>> allNotes = new TreeMap<>();

        for (int trackIndex = 0; trackIndex < sequence.getTracks().length; trackIndex++) {
            currentlyPlayingNotes.clear();
            Track track = sequence.getTracks()[trackIndex];
            for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
                MidiEvent event = track.get(eventIndex);
                if (event.getMessage() instanceof ShortMessage) { // only interested in shortmessages
                    long tick = event.getTick();
                    decodeMessage((ShortMessage) event.getMessage(), tick, isMajor, currentlyPlayingNotes, allNotes);
                }
            }
            if (allNotes.isEmpty()) { // no notes or note offs parsed
                if (currentlyPlayingNotes.size() > 0) { // there are still notes which weren't ended(missing note off...)
                    currentlyPlayingNotes.forEach(NoteEvent::setDefaultDuration);
                } else {
                    continue;
                }
            }
            List<String> lines = new ArrayList<>();
            allNotes.entrySet().forEach(entry -> entry.getValue().forEach(note -> lines.add(note.toString())));
        }
        List<NoteEvent> result = new ArrayList<>();
        allNotes.values().forEach(result::addAll);
        return result;
    }

    /*@Override*/
    private static void decodeMessage(ShortMessage message, long tick, boolean isMajor, Set<NoteEvent> currentlyPlayingNotes,
                               Map<Long, List<NoteEvent>> allNotes) {
        switch (message.getCommand()) {
            case 0x80: // note off
                parseNoteOff(message, tick, isMajor, currentlyPlayingNotes, allNotes);
                break;

            case 0x90: // note on or note 'onff' (note on with velocity 0, effectively note off)
                parseNoteOn(message, tick, isMajor, currentlyPlayingNotes, allNotes);
                break;
            default:
                System.err.println("unknown shortmessage: status = " + message.getStatus() +
                        "; byte1 = " + message.getData1() + "; byte2 = " + message.getData2());
        }
    }

    private static void parseNoteOff(ShortMessage message, long tick, boolean isMajor, Set<NoteEvent> currentlyPlayingNotes,
                              Map<Long, List<NoteEvent>> allNotes) {
        String notename = getKeyName(message.getData1(), isMajor);
        Set<NoteEvent> stoppedNotes = currentlyPlayingNotes.stream().filter(note -> note.getName().equals(notename))
                .collect(Collectors.toSet());
        for (NoteEvent note : stoppedNotes) {
            note.setDuration(tick);
            currentlyPlayingNotes.remove(note);
            if (allNotes.containsKey(note.startTick)) { // TODO: maybe use computeIfAbsent
                allNotes.get(note.startTick).add(note);
            } else {
                allNotes.put(note.startTick, new ArrayList<>());
                allNotes.get(note.startTick).add(note);
            }
        }
    }

    private static void parseNoteOn(ShortMessage message, long tick, boolean isMajor, Set<NoteEvent> currentlyPlayingNotes,
                             Map<Long, List<NoteEvent>> allNotes) {
        if (message.getData2() == 0) { // Fake note off signal (velocity = 0)
            parseNoteOff(message, tick, isMajor, currentlyPlayingNotes, allNotes);
        }
        String notename = getKeyName(message.getData1(), isMajor);
        int velocity = message.getData2();
        NoteEvent currentNote = new NoteEvent(notename, tick, velocity);
        currentlyPlayingNotes.add(currentNote);
    }

    public static void toFile(List<NoteEvent> noteList, Path outputDir, String filename) throws IOException {
        Path outputPath = outputDir.resolve(filename + "_trackdump.csv");

        if (!Files.exists(outputPath)) {
            Files.createFile(outputPath);
        }
        Files.write(outputPath, noteList.stream().map(NoteEvent::toCSV).collect(Collectors.toList()), Charset.forName("UTF-8"));
    }
}

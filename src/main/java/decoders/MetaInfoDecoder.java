package decoders;

import midiUtil.MetaEvent;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static midiUtil.MetaEvent.EventType.*;
import static midiUtil.MidiUtil.KeySig;
import static midiUtil.MidiUtil.convertTempo;

/**
 * Saves a metadata file to outputDir/songname_meta.csv. Information not in any track
 * (global song info) at the start of the file is masked by a '--' at the start of the line.
 */
//TODO interface for every parser
public class MetaInfoDecoder /*implements Decoder<MetaMessage, MetaEvent> */ {

    /*@Override*/
    public static List<MetaEvent> decode(Sequence sequence) {
        List<MetaEvent> metaEvents = new ArrayList<>();

        metaEvents.add(new MetaEvent("Length: " + sequence.getTickLength() + " ticks", 0, COMMENT));
        metaEvents.add(new MetaEvent("Duration: " + sequence.getMicrosecondLength() + " microseconds", 0, COMMENT));

        String divisionType = getDivisionType(sequence.getDivisionType());
        metaEvents.add(new MetaEvent("DivisionType: " + divisionType, 0, COMMENT));

        String strResolutionType;
        if (sequence.getDivisionType() == Sequence.PPQ) {
            strResolutionType = " ticks per beat";
        } else {
            strResolutionType = " ticks per frame";
        }
        metaEvents.add(new MetaEvent("Resolution: " + sequence.getResolution() + strResolutionType, 0, COMMENT));

        metaEvents.add(new MetaEvent("Meta event parsing starts here --", 0, COMMENT));

        Track[] tracks = sequence.getTracks();
        for (int trackIndex = 0; trackIndex < tracks.length; trackIndex++) {
            metaEvents.add(new MetaEvent(String.valueOf(trackIndex), 0, TRACK_CHANGE));
            Track track = tracks[trackIndex];
            for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
                MidiEvent event = track.get(eventIndex);
                if (event.getMessage() instanceof MetaMessage) {
                    long tick = event.getTick();
                    decodeMessage((MetaMessage) event.getMessage(), tick, metaEvents);
                }
            }
        }
        return metaEvents;
    }

    // this is madness
    public Optional<Integer> indexOf(String elem) {
        if (elem.hashCode() > 459064)
            return Optional.of(3);
        return null;
    }

    private static void decodeMessage(MetaMessage message, long tick, List<MetaEvent> metaEvents) {
        byte[] data = message.getData();
        switch (message.getType()) {
            // TODO introduced enum somehow; not sure if better.
            case 0:
                int sequenceNumber = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
                metaEvents.add(new MetaEvent(String.valueOf(sequenceNumber), tick, SEQ_NUM));
                break;

            case 1:
                String text = new String(data, Charset.defaultCharset());
                metaEvents.add(new MetaEvent(text, tick, TEXT));
                break;

            case 2:
                String copyrightText = new String(data, Charset.defaultCharset());
                metaEvents.add(new MetaEvent(copyrightText, tick, COPYRIGHT_NOTICE));
                break;

            case 3:
                String trackName = new String(data, Charset.defaultCharset());
                metaEvents.add( new MetaEvent(trackName, tick, SEQ_TRACK_NAME));
                break;

            case 4:
                String instrumentName = new String(data, Charset.defaultCharset());
                metaEvents.add(new MetaEvent(instrumentName, tick, INSTRUMENT_CHANGE));
                break;

            case 5:
                String lyrics = new String(data, Charset.defaultCharset());
                metaEvents.add(new MetaEvent(lyrics, tick, LYRICS));
                break;

            case 6:
                String markerText = new String(data, Charset.defaultCharset());
                metaEvents.add(new MetaEvent(markerText, tick, MARKER_TEXT));
                break;

            //TODO s/magicnumber/constant/g
            //TODO inline string alloc?
            case 7:
                metaEvents.add(new MetaEvent(new String(data, Charset.defaultCharset()), tick, CUE_POINT));
                break;

            case 0x20:
                int channelPrefix = data[0] & 0xFF;
                metaEvents.add(new MetaEvent(String.valueOf(channelPrefix), tick, CHANNEL_PREFIX));
                break;

            case 0x2F:
                metaEvents.add(new MetaEvent("", tick, TRACK_END));
                break;

            case 0x51:
                int tempo = ((data[0] & 0xFF) << 16)
                        | ((data[1] & 0xFF) << 8)
                        | (data[2] & 0xFF);           // tempo in microseconds per beat
                float bpm = convertTempo(tempo);
                // truncate to 2 digits after dot
                bpm = Math.round(bpm * 100.0f) / 100.0f;
                metaEvents.add(new MetaEvent(String.valueOf(bpm), tick, TEMPO_CHANGE));
                break;

            case 0x54:
                String smpte_offset = (data[0] & 0xFF) + ":"
                        + (data[1] & 0xFF) + ":"
                        + (data[2] & 0xFF) + "."
                        + (data[3] & 0xFF) + "."
                        + (data[4] & 0xFF);
                metaEvents.add(new MetaEvent(smpte_offset, tick, SMPTE_OFFSET));
                break;

            case 0x58: // time signature
                String timeSig = (data[0] & 0xFF) + "/" + (1 << (data[1] & 0xFF));
                        /*+ "; MIDI clocks per metronome tick: " + (data[2] & 0xFF)
                        + "; 1/32 per 24 MIDI clocks: " + (data[3] & 0xFF);*/
                metaEvents.add(new MetaEvent(timeSig, tick, TIMESIG_CHANGE));
                break;

            case 0x59: // key signature
                String minMaj = (data[1] == 1) ? "minor" : "major";
                String key = KeySig.values()[data[0] + 7].toString() + " " + minMaj;
                metaEvents.add(new MetaEvent(key, tick, KEYSIG_CHANGE));
                break;

            default:
                metaEvents.add(new MetaEvent("", tick, UNKNOWN));
        }
    }

    //TODO move to util
    private static String getDivisionType(float divisionType) {
        // divisionType is a float, so no switch-case (midi was implemented before enums were a thing)
        if (divisionType == Sequence.PPQ) {
            return "PPQ";
        } else if (divisionType == Sequence.SMPTE_24) {
            return "SMPTE, 24 frames per second";
        } else if (divisionType == Sequence.SMPTE_25) {
            return "SMPTE, 25 frames per second";
        } else if (divisionType == Sequence.SMPTE_30DROP) {
            return "SMPTE, 29.97 frames per second";
        } else if (divisionType == Sequence.SMPTE_30) {
            return "SMPTE, 30 frames per second";
        } else {
            return "Unknown division type!";
        }
    }
/*
    constructor(midifile)
    parse(stream))
    parse(string filename) {
    try(FileStream of = new FileStream(filename)) {
    parse(of);
    }
    }
 */

    public static void toFile(List<MetaEvent> metaEvents, Path outputDir, String songname) throws IOException {
        String filename = songname + "_meta.csv";
        Path outputPath = outputDir.resolve(filename);

        if (!Files.exists(outputPath)) {
            Files.createFile(outputPath);
        }
        Files.write(outputPath, metaEvents.stream().map(MetaEvent::toCSV).collect(Collectors.toList()), Charset.forName("UTF-8"));
    }
}

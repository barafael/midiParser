package decoders;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static decoders.midiUtil.*;

/**
 * Created by ra on 18.06.16.
 * Part of midiParser, in package decoders.
 */
public class MetaInfoDecoder implements Decoder<MetaMessage> {
    private Path outputFile = Paths.get("assets/midi/csv/");
    private final String filename;

    public MetaInfoDecoder(Path outputDir, String songname) {
        filename = songname + "_meta.csv";
        this.outputFile = Paths.get(outputDir + filename);
    }

    @Override
    public void decode(Sequence sequence, String name) {
        List<String> lines = new ArrayList<>();

        lines.add("-- File: " + filename);
        lines.add("-- Length: " + sequence.getTickLength() + " ticks");
        lines.add("-- Duration: " + sequence.getMicrosecondLength() + " microseconds");
        float divisionType = sequence.getDivisionType();
        String strDivisionType = null;
        if (divisionType == Sequence.PPQ) { // divisionType is a float, so no switch-case
            strDivisionType = "PPQ";
        } else if (divisionType == Sequence.SMPTE_24) {
            strDivisionType = "SMPTE, 24 frames per second";
        } else if (divisionType == Sequence.SMPTE_25) {
            strDivisionType = "SMPTE, 25 frames per second";
        } else if (divisionType == Sequence.SMPTE_30DROP) {
            strDivisionType = "SMPTE, 29.97 frames per second";
        } else if (divisionType == Sequence.SMPTE_30) {
            strDivisionType = "SMPTE, 30 frames per second";
        }

        lines.add("-- DivisionType: " + strDivisionType);

        String strResolutionType;
        if (sequence.getDivisionType() == Sequence.PPQ) {
            strResolutionType = " ticks per beat";
        } else {
            strResolutionType = " ticks per frame";
        }
        lines.add("-- Resolution: " + sequence.getResolution() + strResolutionType);

        lines.add("-- Event parsing starts here --");

        Track[] tracks = sequence.getTracks();
        for (int trackIndex = 0; trackIndex < tracks.length; trackIndex++) {
            lines.add("Track " + trackIndex);
            Track track = tracks[trackIndex];
            for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
                MidiEvent event = track.get(eventIndex);
                if (event.getMessage() instanceof MetaMessage) {
                    long tick = event.getTick();
                    String strMessage = decodeMessage((MetaMessage) event.getMessage(), tick);
                    lines.add(tick + ", " + strMessage);
                }
            }

            try {
                Files.write(outputFile, lines, Charset.forName("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String decodeMessage(MetaMessage message, long tick) {
        byte[] abData = message.getData();
        String strMessage;
        switch (message.getType()) {
            case 0:
                int sequenceNumber = ((abData[0] & 0xFF) << 8) | (abData[1] & 0xFF);
                strMessage = "Sequence Number: " + sequenceNumber;
                break;

            case 1:
                String text = new String(abData, Charset.defaultCharset());
                strMessage = "Text Event: " + text;
                break;

            case 2:
                String strCopyrightText = new String(abData, Charset.defaultCharset());
                strMessage = "Copyright Notice: " + strCopyrightText;
                break;

            case 3:
                String strTrackName = new String(abData, Charset.defaultCharset());
                strMessage = "Sequence/Track Name: " + strTrackName;
                break;

            case 4:
                String strInstrumentName = new String(abData, Charset.defaultCharset());
                strMessage = "Instrument Name: " + strInstrumentName;
                break;

            case 5:
                String strLyrics = new String(abData, Charset.defaultCharset());
                strMessage = "Lyric: " + strLyrics;
                break;

            case 6:
                String strMarkerText = new String(abData, Charset.defaultCharset());
                strMessage = "Marker: " + strMarkerText;
                break;

            case 7:
                String strCuePointText = new String(abData, Charset.defaultCharset());
                strMessage = "Cue Point: " + strCuePointText;
                break;

            case 0x20:
                int nChannelPrefix = abData[0] & 0xFF;
                strMessage = "MIDI Channel Prefix: " + nChannelPrefix;
                break;

            case 0x2F:
                strMessage = "End of Track";
                break;

            case 0x51:
                int tempo = ((abData[0] & 0xFF) << 16)
                        | ((abData[1] & 0xFF) << 8)
                        | (abData[2] & 0xFF);           // tempo in microseconds per beat
                float bpm = convertTempo(tempo);
                // truncate it to 2 digits after dot
                bpm = Math.round(bpm * 100.0f) / 100.0f;
                strMessage = "Set Tempo: " + bpm + " bpm";
                break;

            case 0x54:
                // System.out.println("data array length: " + abData.length);
                strMessage = "SMTPE Offset: "
                        + (abData[0] & 0xFF) + ":"
                        + (abData[1] & 0xFF) + ":"
                        + (abData[2] & 0xFF) + "."
                        + (abData[3] & 0xFF) + "."
                        + (abData[4] & 0xFF);
                break;

            case 0x58:
                strMessage = "Time Signature: "
                        + (abData[0] & 0xFF) + "/" + (1 << (abData[1] & 0xFF))
                        + "; MIDI clocks per metronome tick: " + (abData[2] & 0xFF)
                        + "; 1/32 per 24 MIDI clocks: " + (abData[3] & 0xFF);
                break;

            case 0x59: // key signature
                String strGender = (abData[1] == 1) ? "minor" : "major";
                strMessage = "Key Signature: " + strKeySignatures[abData[0] + 7] + " " + strGender;
                break;

            case 0x7F:
                String strDataDump = getHexString(abData);
                strMessage = "Sequencer-Specific Meta event: " + strDataDump;
                break;

            default:
                String strUnknownDump = getHexString(abData);
                strMessage = "unknown Meta event: " + strUnknownDump;
                break;

        }
        return strMessage;
    }

    @Override
    public void setOutputDir(Path path) {
        if (path.endsWith("/")) {
            outputFile = Paths.get(path + filename);
        } else {
            outputFile = Paths.get(path + "/" + filename);
        }
    }
}

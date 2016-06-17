/*
 *	DumpSequence.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 1999, 2000 by Matthias Pfisterer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */


import javax.sound.midi.*;
import java.io.*;

/**
 * Display content of a MIDI file
 */
class DumpSequence {
    private static String[] strKeyNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    private static Receiver sm_receiver = new DumpReceiver(System.out, true); // standard option


    public static void main(String[] args) {
        String inFilePath;
        String assetPath = "assets/midi/";
        String filename = "giordani_caro.mid";

        if (args.length != 1) {
            inFilePath = assetPath + filename;
            /*
            System.out.println("DumpSequence: usage:");
            System.out.println("\tjava DumpSequence ");
            System.exit(1);
            */
        } else {
            inFilePath = args[0];
        }

        File midiFile = new File(inFilePath);

        try {
            OutputStream outputStream = new FileOutputStream(assetPath + "csv/" + filename + ".csv");
            PrintStream printStream = new PrintStream(outputStream);
            sm_receiver = new DumpReceiver(printStream, true, false); // also closes, consumes the stream
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        /*
         *	try to get a Sequence object, representing the MIDI content
		 */
        Sequence sequence = null;
        try {
            sequence = MidiSystem.getSequence(midiFile);
        } catch (InvalidMidiDataException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

		/*
		 *	And now, output the data.
		 */
        System.out.println("File: " + inFilePath);
        System.out.println("Length: " + sequence.getTickLength() + " ticks");
        System.out.println("Duration: " + sequence.getMicrosecondLength() + " microseconds");
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

        System.out.println("DivisionType: " + strDivisionType);

        String strResolutionType;
        if (sequence.getDivisionType() == Sequence.PPQ) {
            strResolutionType = " ticks per beat";
        } else {
            strResolutionType = " ticks per frame";
        }
        System.out.println("Resolution: " + sequence.getResolution() + strResolutionType);
        Track[] tracks = sequence.getTracks();
        for (int nTrack = 0; nTrack < tracks.length; nTrack++) {
            ((DumpReceiver) sm_receiver).getPrintstream().println("-- Track " + nTrack);
            Track track = tracks[nTrack];
            for (int nEvent = 0; nEvent < track.size(); nEvent++) {
                MidiEvent event = track.get(nEvent);
                output(event);
            }
        }
        // TODO: getPatchList()
    }

    private static void output(MidiEvent event) {
        MidiMessage message = event.getMessage();
        long lTicks = event.getTick();
        sm_receiver.send(message, lTicks);
    }
}

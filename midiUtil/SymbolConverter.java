package midiUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ra on 20.06.16.
 * Part of midiParser, in package midiUtil.
 */
public class SymbolConverter {
    private static final Map<String, Character> note2symbol;
    private static final Map<Character, String> symbol2note;

    static {
        note2symbol = new HashMap<>();
        note2symbol.put("Cb", '¢');
        note2symbol.put("Db", 'ð');
        note2symbol.put("Eb", '€');
        note2symbol.put("Fb", 'đ');
        note2symbol.put("Gb", 'ŋ');
        note2symbol.put("Ab", 'æ');
        note2symbol.put("Bb", '“');

        note2symbol.put("Cs", '©');
        note2symbol.put("Ds", 'Ð');
        note2symbol.put("Es", 'Þ');
        note2symbol.put("Fs", 'ª');
        note2symbol.put("Gs", 'Ŋ');
        note2symbol.put("As", 'Æ');
        note2symbol.put("Bs", '‘');
    }

    static {
        symbol2note = new HashMap<>();
        note2symbol.entrySet().stream().forEach(entry -> {
            String noteName = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);
            symbol2note.put(entry.getValue(), noteName);
        });
    }

    /**
     * collapses flat and sharp signs in the input string so they are only one symbol and can be processed further
     *
     * @param data containing Bb, Ab, Cs ...
     * @return collapsed containing only single symbols for each note
     */
    public static String flatSharpCollapser(String data) {
        for (Map.Entry<String, Character> entry : note2symbol.entrySet()) {
            data = data.replaceAll(entry.getKey(), entry.getValue().toString());
        }
        return data;
    }

    /**
     * collapses flat and sharp signs in the input string so they are only one symbol and can be processed further
     *
     * @param data containing Bb, Ab, Cs ...
     * @return collapsed containing only single symbols for each note
     */
    public static String flatSharpInflater(String data) {
        for (Map.Entry<Character, String> entry : symbol2note.entrySet()) {
            data = data.replaceAll(entry.getKey().toString(), entry.getValue());
        }
        return data;
    }
}

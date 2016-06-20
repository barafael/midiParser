package midiUtil;

import java.util.Collections;
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
        Map<String, Character> tempMap = new HashMap<>();
        tempMap.put("Cb", '¢');
        tempMap.put("Db", 'ð');
        tempMap.put("Eb", '€');
        tempMap.put("Fb", 'đ');
        tempMap.put("Gb", 'ŋ');
        tempMap.put("Ab", 'æ');
        tempMap.put("Bb", '“');

        tempMap.put("Cs", '©');
        tempMap.put("Ds", 'Ð');
        tempMap.put("Es", 'Þ');
        tempMap.put("Fs", 'ª');
        tempMap.put("Gs", 'Ŋ');
        tempMap.put("As", 'Æ');
        tempMap.put("Bs", '‘');
        note2symbol = Collections.unmodifiableMap(tempMap);
    }

    static {
        Map<Character, String> tempMap = new HashMap<>();
        note2symbol.entrySet().stream().forEach(entry -> {
            String noteName = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);
            tempMap.put(entry.getValue(), noteName);
        });
        symbol2note = Collections.unmodifiableMap(tempMap);
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

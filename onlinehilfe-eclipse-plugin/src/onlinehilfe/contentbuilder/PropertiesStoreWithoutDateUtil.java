package onlinehilfe.contentbuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.codec.Charsets;

/**
 * Ein Util basierend auf {@link Properties}.
 * Die Methode {@link #store(Properties, OutputStream)} speichert Properties auf {@link OutputStream},
 * dabei wird jedoch auf Datum und Kommentar verzichtet. Zusätzlich werden die Keys jedoch sortiert, 
 * um eine stärkere Änlichkeit nach Änderungen zu erhalten.
 * 
 * @author pmuenzne
 *
 */
public class PropertiesStoreWithoutDateUtil {

	private static final long serialVersionUID = 9083679962769926732L;

	public static void store(Properties properties, OutputStream out) throws IOException {
		store0(properties, new BufferedWriter(new OutputStreamWriter(out, Charsets.ISO_8859_1)), true);
	}

    private static void store0(Properties properties, BufferedWriter bw, boolean escUnicode) throws IOException {
        synchronized (properties) {
        	List<Entry<Object, Object>> properyEntriesSortedByKeys = properties.entrySet().stream()
        			.sorted((o1,o2) -> o1.getKey().toString().compareTo(o2.getKey().toString()))
        			.collect(Collectors.toList());
            for (Map.Entry<Object, Object> e : properyEntriesSortedByKeys) {
                String key = (String)e.getKey();
                String val = (String)e.getValue();
                key = saveConvert(key, true, escUnicode);
                /* No need to escape embedded and trailing spaces for value, hence
                 * pass false to flag.
                 */
                val = saveConvert(val, false, escUnicode);
                bw.write(key + "=" + val);
                bw.newLine();
            }
        }
        bw.flush();
    }
    
    /*
     * Converts unicodes to encoded &#92;uxxxx and escapes
     * special characters with a preceding slash
     */
    private static String saveConvert(String theString,
                               boolean escapeSpace,
                               boolean escapeUnicode) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuilder outBuffer = new StringBuilder(bufLen);

        for(int x=0; x<len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\'); outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch(aChar) {
                case ' ':
                    if (x == 0 || escapeSpace)
                        outBuffer.append('\\');
                    outBuffer.append(' ');
                    break;
                case '\t':outBuffer.append('\\'); outBuffer.append('t');
                          break;
                case '\n':outBuffer.append('\\'); outBuffer.append('n');
                          break;
                case '\r':outBuffer.append('\\'); outBuffer.append('r');
                          break;
                case '\f':outBuffer.append('\\'); outBuffer.append('f');
                          break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    outBuffer.append('\\'); outBuffer.append(aChar);
                    break;
                default:
                    if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode ) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >>  8) & 0xF));
                        outBuffer.append(toHex((aChar >>  4) & 0xF));
                        outBuffer.append(toHex( aChar        & 0xF));
                    } else {
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }
    
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }

    /** A table of hex digits */
    private static final char[] hexDigit = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

}

package ru.bsc.test.at.mock.filter.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


public class ServletUtils {


    public static String readText(BufferedReader reader) throws IOException {

        StringBuilder sb = new StringBuilder(1024);
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }


    public static byte[] readBytes(InputStream is) throws IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int bytesRead;
        byte[] data = new byte[4096];
        while ((bytesRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }


    public static String[] decodeUrlParam(String param, String enc) {

        if (param == null || param.isEmpty())
            return new String[]{"", ""};

        String key = "", val = "";
        int posEq = param.indexOf('=');

        if (posEq == -1) {

            key = param;
        } else if (posEq > 0) {

            key = param.substring(0, posEq);
            if (posEq < param.length() - 1) {
                val = param.substring(posEq + 1);
            }
        }

        try {

            key = URLDecoder.decode(key, enc);
            val = URLDecoder.decode(val, enc);
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        return new String[]{key, val};
    }


    public static MultiMap decodeUrlString(String input, String enc) {

        MultiMap result = new MultiMap();

        if (input == null || input.isEmpty())
            return result;

        String substr;
        int length = input.length();
        int sss = 0;                    // substringStart
        int sse = input.indexOf('&');   // substringEnd

        while (sse <= length) {

            if (sse == -1)
                substr = input.substring(sss);
            else
                substr = input.substring(sss, sse);

            String[] keyval = decodeUrlParam(substr, enc);
            if (!keyval[0].isEmpty())
                result.add(keyval[0], keyval[1]);

            if (sse == -1)
                break;

            sss = sse + 1;
            sse = input.indexOf('&', sss);
        }

        return result;
    }
}
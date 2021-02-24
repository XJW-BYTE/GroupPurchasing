package com.share.GroupPurchasing.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtil {

    public static String inputStream2String(InputStream is) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i;
        while ((i = is.read()) != -1) {
            byteArrayOutputStream.write(i);
        }
        return byteArrayOutputStream.toString();


//        InputStreamReader reader = null;
//        try {
//            reader = new InputStreamReader(in, "UTF-8");
//        } catch (UnsupportedEncodingException e1) {
//            e1.printStackTrace();
//        }
//        BufferedReader br = new BufferedReader(reader);
//        StringBuilder sb = new StringBuilder();
//        String line = "";
//        try {
//            while ((line = br.readLine()) != null) {
//                sb.append(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return sb.toString();

    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailserver;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * @author carloalberto
 */
public class Utils {
    public static String readFile(String path, Charset encoding) throws IOException {
        byte encodedContent[] = Files.readAllBytes(Paths.get(path));
        return new String(encodedContent, encoding);  
    }
}

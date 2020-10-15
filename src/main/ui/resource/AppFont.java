/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.ui.resource;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import main.App;

/**
 *
 * @author Bunnyspa
 */
public class AppFont {

    public static final Font FONT_DIGIT = get("mohave/Mohave-Light.otf");

    private static Font get(String s) {
        try {
            InputStream is = App.getResourceAsStream("font/" + s);
            return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14.0F);
        } catch (FontFormatException | IOException ex) {
        }
        return null;
    }

    public static Font getDefault() {
        return new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    }

}

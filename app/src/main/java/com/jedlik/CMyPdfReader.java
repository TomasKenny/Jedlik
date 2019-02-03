package com.jedlik;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

/**
 * Created by Tom on 6.7.2016.
 */
public class CMyPdfReader {

    public static String Read(File f){
        String str = new String();
        try {
            PDDocument pdf = new PDDocument().load(f);
            PDFTextStripper stripper;
            stripper = new PDFTextStripper();
            str = stripper.getText(pdf);
            pdf.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return str;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.buhalovie.reportgenerator;

import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import java.io.File;
import java.util.List;

/**
 *
 * @author gasoil
 */
public class ReportGenerator {

    private static void printTSV() {

        File file = new File("source-data.tsv");

        TsvParserSettings settings = new TsvParserSettings();

        TsvParser parser = new TsvParser(settings);

        List<String[]> allRows = parser.parseAll(file, "Unicode");

        for (String[] row : allRows) {
            System.out.println("");
            for (String col : row) {
                System.out.print(col);
                System.out.print("\t");
            }
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ReportGenerator.printTSV();
    }

}

/*
 * Copyright 2016 byhalovIE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.byhalovie.reportgenerator;

import com.github.byhalovie.reportgenerator.exceptions.ReportGeneratorException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Класс, содержащий метод main.
 *
 * @author byhalovIE
 */
public class Main {

    /**
     * По заданным параметрам создает ReportGenerator, получает от него отчет и
     * записывает его в файл.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        PrintWriter printWriter = null;

        try {
            if (args.length != 3) {
                throw new ReportGeneratorException("Incorrect number of arguments");
            }

            String settingsPath = args[0];
            String sourceDataPath = args[1];
            String reportPath = args[2];

            ReportGenerator reportGenerator = new ReportGenerator(settingsPath, sourceDataPath);

            File reportFile = new File(reportPath);
            if (!reportFile.exists()) {
                reportFile.createNewFile();
            }
            printWriter = new PrintWriter(reportFile, "UTF-16");

            System.out.println(reportGenerator.getGeneratedReport());
            printWriter.write(reportGenerator.getGeneratedReport());

        } catch (ReportGeneratorException | IOException ex) {

            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);

        } finally {

            if (printWriter != null) {
                printWriter.close();
            }
        }
    }

}

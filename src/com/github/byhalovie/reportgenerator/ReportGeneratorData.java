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
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import java.io.File;
import java.util.List;

/**
 * Класс предназначен для извлечение из файла и хранения данных, подлежащих
 * форматированию
 *
 * @author byhalovIE
 */
public final class ReportGeneratorData {

    private List<String[]> allRows;
    private final String encoding = "UTF-16";

    public ReportGeneratorData(String tsvPath) throws ReportGeneratorException {

        try {
            File file = new File(tsvPath);

            TsvParserSettings settings = new TsvParserSettings();

            TsvParser parser = new TsvParser(settings);

            allRows = parser.parseAll(file, encoding);

        } catch (Exception exception) {

            throw new ReportGeneratorException(
                    "Error reading data tsv file\n" + tsvPath,
                    exception
            );
        }
    }

    public List<String[]> getData() {
        return allRows;
    }
}

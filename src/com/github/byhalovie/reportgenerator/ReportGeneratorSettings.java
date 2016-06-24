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
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Класс предназначен для извлечения из файла и хранения настроек генератора
 * отчетов
 *
 * @author gasoil
 */
public class ReportGeneratorSettings {

    int pageWidth;
    int pageHeight;

    List<Column> columnsList;

    void readXmlFile(String pathname) throws ReportGeneratorException {

        try {

            //  Готовим файл к открытию
            File inputFile = new File(pathname);

            //  Инициализируемм Dom parser
            DocumentBuilder dBuilder = DocumentBuilderFactory
                    .newInstance().newDocumentBuilder();
            Document document = dBuilder.parse(inputFile);

            document.getDocumentElement().normalize();

            // В этом блоке читаем настройки страницы
            Element pageSettings = (Element) document.getElementsByTagName("page").item(0);
            pageWidth = Integer.parseInt(
                    pageSettings.getElementsByTagName("width").item(0)
                    .getTextContent()
            );
            pageHeight = Integer.parseInt(
                    pageSettings.getElementsByTagName("height").item(0)
                    .getTextContent()
            );

            // В этом блоке читаем настройки колонок
            columnsList = new ArrayList<>();
            NodeList columnNodesList = document.getElementsByTagName("column");
            for (int i = 0; i < columnNodesList.getLength(); i++) {
                Element column = (Element) columnNodesList.item(i);

                String title = column.getElementsByTagName("title").item(0).getTextContent();
                int width = Integer.parseInt(column.getElementsByTagName("width").item(0).getTextContent());

                columnsList.add(new Column(title, width));
            }

        } catch (ParserConfigurationException | SAXException | IOException |
                DOMException | NumberFormatException exception) {
            throw new ReportGeneratorException(
                    "Error reading settings XML file:\n" + pathname,
                    exception
            );
        }
    }

    class Column {

        String title;
        int width;

        public Column(String title, int width) {
            this.title = title;
            this.width = width;
        }

        public Column() {
        }

    }
}

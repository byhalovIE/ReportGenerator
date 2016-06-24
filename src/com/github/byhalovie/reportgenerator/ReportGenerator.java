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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Класс, формирующий отчеты. Открыты только конструкторы и метод
 * getGeneratedReport(). Методы, работа которых зависит только от параметров,
 * помечены ключевым словом static.
 *
 * @author byhalovIE
 */
public class ReportGenerator {

    //  Значения по умолчанию
    private String settingsPathName = "settings.xml";
    private String sourceDataPathName = "source-data.tsv";

    private final String newLineCode = "\r\n";
    private final String verticalSeparator = "|";
    private final String horizontalSeparator = "-";
    private final String pageSeparator = "~";
    private String separator;

    private ReportGeneratorSettings settings;
    private ReportGeneratorData data;

    /**
     * Возвращает список токенов. Токен := "слово из цифр и/или букв" |
     * "символ-разделитель"
     *
     * @param field
     * @return
     */
    private static List<String> getDelimitedTokens(String field) {

        // Создадим список токенов, состоящий из слов и разделителей
        List<String> delimitedTokensList = new ArrayList<>();

        int currentWordStartPosition = 0;
        boolean readingWord = false;
        int i = 0;
        for (; i < field.length(); i++) {

            char currentChar = field.charAt(i);

            if (Character.isAlphabetic(currentChar) || Character.isDigit(currentChar)) {

                //  Если текущий символ относится к слову и слово только началось
                if (!readingWord) {
                    readingWord = true;
                    currentWordStartPosition = i;
                }
            } else {

                //  Если текущий символ не принадлежит слову 
                //  т.е. является разделителем
                if (readingWord) {

                    // Если чистали слово, то записать все слово целиком
                    delimitedTokensList.add(field.substring(currentWordStartPosition, i));
                    // Записать разделитель
                    delimitedTokensList.add("" + currentChar);
                    // И поменять флаг слова
                    readingWord = false;

                } else {

                    // А если нет, то просто записать разделитель
                    delimitedTokensList.add("" + currentChar);
                }
            }
        }

        if (readingWord) {
            delimitedTokensList.add(field.substring(currentWordStartPosition, i));
        }

        return delimitedTokensList;
    }

    /**
     * Форматирует поле по заданной ширине. Разбиение проводится по возможности
     * по разделителям слов. Выравниевание по левому краю. При необходимости
     * строка дополняется пробелами до нужной ширины.
     *
     * @param field Поле, подлежащее форматированию
     * @param lenght Ширина, к которой привести поле.
     * @return Список строк длиной lenght
     */
    private static List<String> formatFieldToWidth(String field, int lenght) {

        List<String> formattedString = new ArrayList<>();

        // Создадим список токенов, состоящий из слов и разделителей
        List<String> splitedTokensList = getDelimitedTokens(field);

        String currentString = "";

        //<editor-fold defaultstate="collapsed" desc="Форматирование списка токенов по ширине">
        Iterator<String> iterator = splitedTokensList.iterator();
        String token = iterator.next();
        do {
            if (currentString.length() + token.length() <= lenght) {

                //  Если токен помещается в строку до добавляем его в строку
                //  и берем следующий
                currentString = currentString + token;
                if (iterator.hasNext()) {

                    token = iterator.next();
                } else {

                    // Если токены закончились то выходим из цикла
                    formattedString.add(String.format("%1$-" + lenght + "s", currentString));
                    break;
                }

            } else if (!currentString.isEmpty()) {

                //  Если не помещается то смотрим на токен
                if (token.length() <= lenght) {

                    //  Если он поместится в новой строке то завершаем текущую 
                    //  строку и рассматриваем ситуацию заново
                    formattedString.add(String.format("%1$-" + lenght + "s", currentString));
                    currentString = "";
                } else {

                    //  А если он все равно не поместится начинаем дробить на месте
                    formattedString.add(
                            currentString
                            + token.substring(0, lenght - currentString.length())
                    );
                    token = token.substring(lenght - currentString.length());
                    currentString = "";
                }

            } else /* currentString.isEmpty() */ {

                //  Если не помещается и текущая строка пуста 
                //  то дробим токен на части и рассмотрим ситуацию с остатком
                formattedString.add(token.substring(0, lenght));
                token = token.substring(lenght);

            }
        } while (true);
        //</editor-fold>

        return formattedString;
    }

    /**
     * Расширяет список строк до заданного размера добавлением строк из пробелов
     *
     * @param formattedField
     * @param fieldHeight
     * @return
     */
    private static List<String> trailFieldToHeight(List<String> formattedField, int fieldHeight) {
        for (int i = formattedField.size(); i < fieldHeight; i++) {
            formattedField.add(String.format("%1$-" + formattedField.get(0).length() + "s", ""));
        }
        return formattedField;
    }

    /**
     * Метод формирует форматированную строку типа ReportRow.
     *
     * @param fields Список полей в строке
     * @return
     * @throws ReportGeneratorException
     */
    private ReportRow generateRow(List<String> fields) throws ReportGeneratorException {

        if (fields.size() != settings.columnsList.size()) {
            throw new ReportGeneratorException(
                    "The number of fields in a row doesn't match to the number of columns."
            );
        }

        List<List<String>> row = new ArrayList<>();

        //  Форматируем поле по ширине столбца
        Iterator<String> fieldsIterator = fields.iterator();
        for (ReportGeneratorSettings.Column column : settings.columnsList) {
            String field = fieldsIterator.next();
            row.add(ReportGenerator.formatFieldToWidth(field, column.width));
        }

        //  Вычисляем максимальную высоту поля в строке
        int maxHeight = 0;
        for (List<String> field : row) {
            if (field.size() > maxHeight) {
                maxHeight = field.size();
            }
        }

        //  Форматируем поля по максимальной высоте поля в строке
        for (List<String> field : row) {
            ReportGenerator.trailFieldToHeight(field, maxHeight);
        }

        //  Формируем строку таблицы по полученным полям с использованием 
        //  разделителей
        String rowValue = "";
        for (int i = 0; i < row.get(0).size(); i++) {
            rowValue = rowValue + verticalSeparator;
            for (List<String> headColumn : row) {
                rowValue = rowValue + " " + headColumn.get(i) + " " + verticalSeparator;
            }
            rowValue = rowValue + newLineCode;
        }

        return new ReportRow(rowValue, maxHeight);
    }

    private ReportRow generateRow(String[] fields) throws ReportGeneratorException {
        return generateRow(Arrays.asList(fields));
    }

    /**
     * Метод формирует заголовок отчета типа ReportRow.
     *
     * @return
     * @throws ReportGeneratorException
     */
    private ReportRow generateHead() throws ReportGeneratorException {

        List<String> headFields = new ArrayList<>();
        for (ReportGeneratorSettings.Column column : settings.columnsList) {
            headFields.add(column.title);
        }

        ReportRow headRow = generateRow(headFields);
        headRow.text = headRow.text + separator;
        headRow.height = headRow.height + 1;

        return headRow;
    }

    /**
     * Возвращает список отформатированных страниц
     *
     * @throws
     * com.github.byhalovie.reportgenerator.exceptions.ReportGeneratorException
     */
    private List<String> getPagesList() throws ReportGeneratorException {

        ReportRow head = generateHead();

        int realPageHeight = settings.pageHeight - head.height;

        if (realPageHeight <= 0) {
            throw new ReportGeneratorException("Page height too small.");
        }

        //  Сформируем список отформатированных строк
        List<ReportRow> rowsList = new ArrayList<>();

        for (String[] row : data.getData()) {
            rowsList.add(generateRow(row));
        }

        List<String> pagesList = new ArrayList<>();

        ReportRow currentPage = new ReportRow(head.text, head.height);

        //<editor-fold defaultstate="collapsed" desc="Формирование страниц из строк">
        Iterator<ReportRow> rowsIterator = rowsList.iterator();
        ReportRow row = rowsIterator.next();
        do {
            if (currentPage.height + row.height + 1 <= settings.pageHeight) {

                //  Если строка помещается на страницу
                //  то пишем ее на страницу и 
                currentPage.text = currentPage.text + row.text + separator;
                currentPage.height = currentPage.height + row.height + 1;
                if (rowsIterator.hasNext()) {

                    // Если строки для обработки остались берем следующую
                    row = rowsIterator.next();
                } else {

                    // Если строки закончились то выходим из цикла
                    pagesList.add(currentPage.text);
                    break;
                }

            } else if (currentPage.height > head.height) {

                //  Если не помещается и на текущей странице что-то есть
                //  то закрываем страницу и создаем новую
                pagesList.add(currentPage.text);
                currentPage = new ReportRow(head.text, head.height);

            } else /* currentString.isEmpty() */ {

                //  Если не помещается и текущая страница пуста 
                //  то дробим строку на части и рассмотрим ситуацию с остатком
                ReportRow separatableRow
                        = new ReportRow(row.text + separator, row.height + 1);

                String[] splitedRow = separatableRow.text.split("\n");

                for (int i = 0; i < realPageHeight; i++) {
                    currentPage.text = currentPage.text + splitedRow[i] + newLineCode;
                }
                pagesList.add(currentPage.text);

                currentPage = new ReportRow(head.text, head.height);
                row = new ReportRow("", separatableRow.height - realPageHeight);
                for (int i = realPageHeight; i < separatableRow.height; i++) {
                    row.text = row.text + splitedRow[i] + newLineCode;
                }

            }
        } while (true);
        //</editor-fold>

        return pagesList;

    }

    /**
     * Возвращает сформированный отчет
     *
     * @return
     * @throws ReportGeneratorException
     */
    public String getGeneratedReport() throws ReportGeneratorException {

        String report;
        List<String> pagesList = getPagesList();
        //  Заполняем итоговый отчет страницами попутно проставляя разделители 
        //  страниц
        Iterator<String> pagesIterator = pagesList.iterator();
        report = pagesIterator.next();
        while (pagesIterator.hasNext()) {

            report = report + pageSeparator + newLineCode + pagesIterator.next();
        }

        return report;
    }

    public ReportGenerator() {
        init();
    }

    public ReportGenerator(String settingsPathName, String sourceDataPathName) {
        this.settingsPathName = settingsPathName;
        this.sourceDataPathName = sourceDataPathName;

        init();
    }

    private void init() {
        try {
            settings = new ReportGeneratorSettings();
            settings.readXmlFile(settingsPathName);

            data = new ReportGeneratorData();
            data.readTsvFile(sourceDataPathName);

            separator = String.format("%1$-" + settings.pageWidth + "s", "")
                    .replaceAll(" ", horizontalSeparator) + newLineCode;

        } catch (Exception ex) {
            Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

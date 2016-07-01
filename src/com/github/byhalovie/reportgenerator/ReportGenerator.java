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
public final class ReportGenerator {

    //<editor-fold defaultstate="collapsed" desc="Private Static Methods">
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

        int lastIteration = 0;

        for (int i = 0; i < field.length(); i++) {
            lastIteration = i;
            char currentChar = field.charAt(i);

            if (Character.isAlphabetic(currentChar)
                    || Character.isDigit(currentChar)) {

                //  Если текущий символ относится к слову и слово только началось
                if (readingWord == false) {
                    readingWord = true;
                    currentWordStartPosition = i;
                }
            } else {
                String stringCurrentChar = Character.toString(currentChar);

                //  Если текущий символ не принадлежит слову 
                //  т.е. является разделителем
                if (readingWord) {

                    String completedWord = field.substring(currentWordStartPosition, i);
                    // Если чистали слово, то записать все слово целиком
                    delimitedTokensList.add(completedWord);
                    // Записать разделитель
                    delimitedTokensList.add(stringCurrentChar);
                    // И поменять флаг слова
                    readingWord = false;

                } else {

                    // А если нет, то просто записать разделитель
                    delimitedTokensList.add(stringCurrentChar);
                }
            }
        }

        if (readingWord) {
            delimitedTokensList.add(field.substring(currentWordStartPosition, lastIteration + 1));
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
    private static ReportRow generateRow(List<String> fields, ReportGeneratorSettings settings) throws ReportGeneratorException {

        if (fields.size() != settings.getColumnsList().size()) {
            throw new ReportGeneratorException(
                    "The number of fields in a row doesn't match to the number of columns."
            );
        }

        List<List<String>> row = new ArrayList<>();

        //  Форматируем поле по ширине столбца
        Iterator<String> fieldsIterator = fields.iterator();
        for (ReportGeneratorSettings.Column column : settings.getColumnsList()) {
            String field = fieldsIterator.next();
            row.add(ReportGenerator.formatFieldToWidth(field, column.getWidth()));
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
            rowValue = rowValue + settings.getVerticalSeparator();
            for (List<String> headColumn : row) {
                rowValue = rowValue + " " + headColumn.get(i) + " " + settings.getVerticalSeparator();
            }
            rowValue = rowValue + settings.getNewLineCode();
        }

        return new ReportRow(rowValue, maxHeight);
    }

    private static ReportRow generateRow(String[] fields, ReportGeneratorSettings settings) throws ReportGeneratorException {
        return generateRow(Arrays.asList(fields), settings);
    }

    /**
     * Метод формирует заголовок отчета типа ReportRow.
     *
     * @throws ReportGeneratorException
     */
    private static ReportRow generateHead(ReportGeneratorSettings settings) throws ReportGeneratorException {

        List<String> headFields = new ArrayList<>();
        for (ReportGeneratorSettings.Column column : settings.getColumnsList()) {
            headFields.add(column.getTitle());
        }

        ReportRow headRow = generateRow(headFields, settings);
        headRow.text = headRow.text + settings.getSeparator();
        headRow.height = headRow.height + 1;

        return headRow;
    }

    /**
     * Возвращает список отформатированных страниц
     *
     * @throws
     * com.github.byhalovie.reportgenerator.exceptions.ReportGeneratorException
     */
    private static List<String> getPagesList(ReportGeneratorSettings settings, ReportGeneratorData data) throws ReportGeneratorException {

        //  Формируем заголовок
        ReportRow head = generateHead(settings);

        int realPageHeight = settings.getPageHeight() - head.height;

        if (realPageHeight <= 0) {
            throw new ReportGeneratorException("Page height too small.");
        }

        //  Сформируем список отформатированных строк
        List<ReportRow> rowsList = new ArrayList<>();

        for (String[] row : data.getData()) {
            rowsList.add(generateRow(row, settings));
        }

        //<editor-fold defaultstate="collapsed" desc="Формирование страниц из строк">
        List<String> pagesList = new ArrayList<>();

        ReportRow currentPage = new ReportRow(head.text, head.height);

        Iterator<ReportRow> rowsIterator = rowsList.iterator();
        ReportRow row = rowsIterator.next();
        do {
            if (currentPage.height + row.height + 1 <= settings.getPageHeight()) {

                //  Если строка помещается на страницу
                //  то пишем ее на страницу и 
                currentPage.text = currentPage.text + row.text + settings.getSeparator();
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
                        = new ReportRow(row.text + settings.getSeparator(), row.height + 1);

                String[] splitedRow = separatableRow.text.split("\n");

                for (int i = 0; i < realPageHeight; i++) {
                    currentPage.text = currentPage.text + splitedRow[i] + settings.getNewLineCode();
                }
                pagesList.add(currentPage.text);

                currentPage = new ReportRow(head.text, head.height);
                row = new ReportRow("", separatableRow.height - realPageHeight);
                for (int i = realPageHeight; i < separatableRow.height; i++) {
                    row.text = row.text + splitedRow[i] + settings.getNewLineCode();
                }

            }
        } while (true);
        //</editor-fold>

        return pagesList;

    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="public static methods">
    /**
     * Возвращает сформированный отчет
     *
     * @param sourcePath путь к файлу с данными
     * @param settingsPath путь к файлу с настройками
     * @return сформированный отчет
     * @throws ReportGeneratorException
     */
    public static String generateReport(String sourcePath, String settingsPath)
            throws ReportGeneratorException {

        ReportGeneratorSettings settings = new ReportGeneratorSettings(settingsPath);

        ReportGeneratorData data = new ReportGeneratorData(sourcePath);

        String report;
        List<String> pagesList = getPagesList(settings, data);

        //  Заполняем итоговый отчет страницами попутно проставляя разделители 
        //  страниц
        Iterator<String> pagesIterator = pagesList.iterator();
        report = pagesIterator.next();
        while (pagesIterator.hasNext()) {
            report = report + settings.getPageSeparator()
                    + settings.getNewLineCode() + pagesIterator.next();
        }

        return report;
    }
    //</editor-fold>
}

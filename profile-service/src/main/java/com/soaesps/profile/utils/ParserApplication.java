package com.soaesps.profile.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Application that loads records from CSV and XML files.
 * Recreated to satisfy the JMH benchmark API after migration from the legacy TestTask project.
 * NOTE: javax.xml.parsers and org.w3c.dom are part of Java SE (not Jakarta EE), so they stay unchanged.
 */
public class ParserApplication {

    private static final Logger logger = LoggerFactory.getLogger(ParserApplication.class);

    /**
     * Loads records from a CSV file.
     * Assumes the first line contains column headers.
     */
    public List<Map<String, String>> loadRecordsFromCSVFile(final String filePath) {
        final List<Map<String, String>> records = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            final String headerLine = reader.readLine();
            if (headerLine == null) {
                return records;
            }
            final String[] headers = headerLine.split(",");

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                final String[] values = line.split(",");
                final Map<String, String> record = new HashMap<>();
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    record.put(headers[i].trim(), values[i].trim());
                }
                records.add(record);
            }
        } catch (IOException ex) {
            logger.error("Failed to load CSV file: {}", filePath, ex);
        }

        return records;
    }

    /**
     * Loads records from an XML file.
     * Expected structure: <records><record><field>value</field>...</record></records>
     */
    public List<Map<String, String>> loadRecordsFromXMLFile(final String filePath) {
        final List<Map<String, String>> records = new ArrayList<>();

        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // SECURITY: Disable DOCTYPE declarations to prevent XXE attacks
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            final Document document = factory.newDocumentBuilder().parse(new File(filePath));
            document.getDocumentElement().normalize();

            final NodeList recordNodes = document.getElementsByTagName("record");
            for (int i = 0; i < recordNodes.getLength(); i++) {
                final Element recordElement = (Element) recordNodes.item(i);
                final Map<String, String> record = new HashMap<>();

                final NodeList fields = recordElement.getChildNodes();
                for (int j = 0; j < fields.getLength(); j++) {
                    if (fields.item(j) instanceof Element field) {
                        record.put(field.getTagName(), field.getTextContent().trim());
                    }
                }
                records.add(record);
            }
        } catch (Exception ex) {
            logger.error("Failed to load XML file: {}", filePath, ex);
        }

        return records;
    }
}
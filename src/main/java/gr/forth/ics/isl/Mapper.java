/*
 * Copyright 2015 Institute of Computer Science,
 * Foundation for Research and Technology - Hellas
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 *
 * Contact:  POBox 1385, Heraklio Crete, GR-700 13 GREECE
 * Tel:+30-2810-391632
 * Fax: +30-2810-391638
 * E-mail: isl@ics.forth.gr
 * http://www.ics.forth.gr/isl
 *
 * Authors :  Georgios Samaritakis.
 *
 * This file is part of the x3mlMapper webapp.
 */
package gr.forth.ics.isl;

import gr.forth.ics.isl.x3ml.X3MLEngine;
import static gr.forth.ics.isl.x3ml.X3MLEngine.exception;
import gr.forth.ics.isl.x3ml.X3MLGeneratorPolicy;
import gr.forth.ics.isl.x3ml.engine.Generator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.jena.riot.Lang;
import org.w3c.dom.Element;

/**
 *
 * @author samarita
 */
public class Mapper {

    List<String> errors = new ArrayList<String>();

    public X3MLEngine engine(String x3mlURL) {
        errors = X3MLEngine.validate(urlResource(x3mlURL));
        if (errors.size() > 0) {
            return null;
        }

        return X3MLEngine.load(urlResource(x3mlURL));

    }

    public X3MLEngine engine(String x3mlURL, String thesaurus) {
        errors = X3MLEngine.validate(urlResource(x3mlURL));
        if (errors.size() > 0) {
            return null;
        }
        return X3MLEngine.load(urlResource(x3mlURL), stringResource(thesaurus), getThesaurusFileFormat(thesaurus));
    }

    public List<String> getEngineErrors() {
        return errors;
    }

    public InputStream urlResource(String url) {
        try {
            return new URL(url).openStream();
        } catch (IOException ex) {
            return null;
        }

    }

    public Lang getThesaurusFileFormat(String content) {
        if (content.startsWith("@prefix")) {
            return Lang.TTL;
        } else if (content.startsWith("<rdf:RDF")) {
            return Lang.RDFXML;
        } else {
            return Lang.NT;
        }
    }

    public InputStream stringResource(String content) {
        String encoding = getEncodingFromXML(content);
        InputStream stream = null;
        if (encoding == null) {
            stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        } else {
            if (encoding.equals("ASCII")) {
                stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.US_ASCII));
            } else if (encoding.equals("ISO-8859-1")) {
                stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.ISO_8859_1));
            } else {
                stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            }
        }
        return stream;
    }

    public Generator policy(String content, int uuidSize) {
        return X3MLGeneratorPolicy.load(stringResource(content), X3MLGeneratorPolicy.createUUIDSource(uuidSize));
    }

    public Element documentFromString(String content) {
        try {
            return documentBuilderFactory().newDocumentBuilder().parse(stringResource(content)).getDocumentElement();
        } catch (Exception e) {
            throw exception("Unable to parse " + content);
        }
    }

    public Element documentFromUrl(String url) {
        try {
            return documentBuilderFactory().newDocumentBuilder().parse(urlResource(url)).getDocumentElement();
        } catch (Exception e) {
            throw exception("Unable to parse " + url);
        }
    }

    public DocumentBuilderFactory documentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory;
    }

    private String getEncodingFromXML(String content) {
        String ResultString = "";
        try {
            Pattern regex = Pattern.compile("(?<=encoding=\")[^\"]*(?=\"\\?)", Pattern.MULTILINE);
            Matcher regexMatcher = regex.matcher(content);
            if (regexMatcher.find()) {
                ResultString = regexMatcher.group();
            }
            return ResultString;
        } catch (PatternSyntaxException ex) {
            return null;
        }
    }
}

package com.example.android.wifidirect;

import android.os.Environment;

import com.example.android.wifidirect.services.WiFiTransferService;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CreateXMLFileJava {

    public static final String xmlFilePath = Environment.getExternalStorageDirectory() + "/" + WiFiTransferService.FileServerAsyncTask.context.getPackageName();

    public static void main(String argv[]) {

        try {

            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document xmlVariables = documentBuilder.newDocument();

            // root element
            Element root = xmlVariables.createElement("resources");
            xmlVariables.appendChild(root);

            // variables element
            Element variables = xmlVariables.createElement("variables");
            root.appendChild(variables);

            Element lewyMargines = xmlVariables.createElement("lewy_margines");
            lewyMargines.appendChild(xmlVariables.createTextNode(String.valueOf(Constants.lewy)));
            variables.appendChild(lewyMargines);


            Element prawyMargines = xmlVariables.createElement("prawy_margines");
            prawyMargines.appendChild(xmlVariables.createTextNode(String.valueOf(Constants.prawy)));
            variables.appendChild(prawyMargines);


            Element gornyMargines = xmlVariables.createElement("gorny_margines");
            gornyMargines.appendChild(xmlVariables.createTextNode(String.valueOf(Constants.gorny)));
            variables.appendChild(gornyMargines);


            Element dolnyMargines = xmlVariables.createElement("dolny_margines");
            dolnyMargines.appendChild(xmlVariables.createTextNode(String.valueOf(Constants.dolny)));
            variables.appendChild(dolnyMargines);

            // create the xml file
            // transform the DOM Object to an XML File

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(xmlVariables);
            StreamResult streamResult = new StreamResult(new File(xmlFilePath));


            // If you use
            // StreamResult result = new StreamResult(System.out);
            // the output will be pushed to the standard output ...
            // You can use that for debugging
            transformer.transform(domSource, streamResult);


            System.out.println("Stworzono XML");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();

        } catch (TransformerException tfe) {
            tfe.printStackTrace();

        }

    }


}


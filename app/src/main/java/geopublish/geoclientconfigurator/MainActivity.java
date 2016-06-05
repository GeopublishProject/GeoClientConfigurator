package geopublish.geoclientconfigurator;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class MainActivity extends AppCompatActivity {

    private String configFilePath;
    private int networkEnvironment;
    private String serverIP;
    private String serverPort;
    private static RadioGroup rgNetworkEnv;
    private static EditText txtServerIP;
    private static EditText txtServerPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rgNetworkEnv= (RadioGroup) findViewById(R.id.rgNetworkOptions);
        txtServerIP=(EditText) findViewById(R.id.txtServerIP);
        txtServerPort=(EditText) findViewById(R.id.txtServerPort);

        File root = Environment.getExternalStorageDirectory();

        configFilePath= root.getAbsolutePath()  + "/GeoPublishSettings/Client.xml";

        File file = new File(configFilePath);

        if(file.exists())
        {
            readXML(configFilePath);

            txtServerIP.setEnabled(false);
            if (networkEnvironment==1) txtServerIP.setEnabled(true);

            ((RadioButton)rgNetworkEnv.getChildAt(networkEnvironment)).setChecked(true);
            txtServerIP.setText(serverIP);
            txtServerPort.setText(serverPort);
        }

        Button btnSave=(Button) findViewById(R.id.btnSave);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int index = rgNetworkEnv.indexOfChild(findViewById(rgNetworkEnv.getCheckedRadioButtonId()));

                File dir = new File(Environment.getExternalStorageDirectory() + "/GeoPublishSettings");
                if(dir.exists() && dir.isDirectory()) {
                }
                else
                {
                    dir.mkdirs();
                }

                saveToXML(configFilePath, index,txtServerIP.getText().toString(),txtServerPort.getText().toString() );
                Toast.makeText(MainActivity.this, "Datos guardados", Toast.LENGTH_SHORT).show();
            }
        });

        rgNetworkEnv.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.optNativeAndroidEmulator:
                        txtServerIP.setText("10.0.2.2");
                        txtServerIP.setEnabled(false);

                        break;
                    case R.id.optLAN:
                        txtServerIP.setText("192.168.");
                        txtServerIP.setEnabled(true);

                        break;
                    case R.id.optHotspot:
                        txtServerIP.setText("192.168.43.1");
                        txtServerIP.setEnabled(false);
                        break;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void saveToXML(String xml, int networkEnvironment, String serverIP, String serverPort) {
        Document dom;
        Element e = null;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            dom = db.newDocument();

            // create the root element
            Element rootEle = dom.createElement("settings");

            // create data elements and place them under root
            e = dom.createElement("NetworkEnvironment");
            e.appendChild(dom.createTextNode(String.valueOf(networkEnvironment)));
            rootEle.appendChild(e);

            e = dom.createElement("ServerIP");
            e.appendChild(dom.createTextNode(serverIP));
            rootEle.appendChild(e);

            e = dom.createElement("ServerPort");
            e.appendChild(dom.createTextNode(serverPort));
            rootEle.appendChild(e);

            dom.appendChild(rootEle);

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                // send DOM to file
                tr.transform(new DOMSource(dom),
                        new StreamResult(new FileOutputStream(xml)));

            } catch (TransformerException te) {
                System.out.println(te.getMessage());
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }
    }

    private String getTextValue( Element doc, String tag) {
        String value=null;
        NodeList nl;
        nl = doc.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = nl.item(0).getFirstChild().getNodeValue();
        }
        return value;
    }


    public void readXML(String fileName) {
        Document dom;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            File file = new File(fileName);
            InputStream is = new FileInputStream(file.getPath());
            //DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            //DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(is));
            doc.getDocumentElement().normalize();


            // parse using the builder to get the DOM mapping of the
            // XML file
            //dom = db.parse(xml);

            Element docElement = doc.getDocumentElement();

            networkEnvironment = Integer.parseInt(getTextValue(docElement, "NetworkEnvironment"));
            serverIP = getTextValue( docElement, "ServerIP");
            serverPort = getTextValue( docElement, "ServerPort");

        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

    }
}

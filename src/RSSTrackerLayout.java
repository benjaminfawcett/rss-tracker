import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.net.URL;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

public class RSSTrackerLayout {
    private JList rssList;
    private JButton addButton;
    private JButton removeButton;
    private JComboBox companyBox;
    private JButton addCompanyButton;
    private JSpinner daySelector;
    private JButton compileListButton;
    private JPanel mainPanel;
    private JPanel topLeft;
    private JPanel bottomLeft;
    private JPanel topRight;
    private JPanel bottomRight;
    private JPanel activityPanel;
    private JPanel rssAddRemovePanel;
    private JTextField newURLField;
    private JTextField newCompanyField;
    private JLabel statusText;
    private JScrollPane inactiveList;
    private JLabel addRemoveStatus;
    private HashMap<String, ArrayList<String>> rssMap = new HashMap<>();
    private ArrayList<String> companies = new ArrayList<>();
    private DefaultListModel<String> rssListModel = new DefaultListModel<>();

    // resets the JList with most recent company RSS feeds
    public void setRSSList() {
        try {
            String currentCompany = (String) companyBox.getSelectedItem();
            // alphabetize
            Collections.sort(rssMap.get(currentCompany));
            ArrayList<String> newList = rssMap.get(currentCompany);
            // clear JList model
            rssListModel.clear();

            // add all RSS feeds to cleared list
            for (int i = 0; i < newList.size(); i++) {
                rssListModel.addElement(newList.get(i));
            }
            // null pointer exception was being thrown before RSS feed were added so this catches that
        } catch (NullPointerException e) {

        }

    }

    public RSSTrackerLayout() {
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentCompany = (String) companyBox.getSelectedItem();
                rssList.setModel(rssListModel);
                String newRSS = newURLField.getText();
                // check if RSS feed already exists and add if not

                if (validateRSS(newRSS)) {
                    if (!rssMap.get(currentCompany).contains(newRSS)) {
                        rssMap.get(currentCompany).add(newRSS);
                        setRSSList();
                        // status indicator below add/remove
                        addRemoveStatus.setForeground(Color.GREEN);
                        addRemoveStatus.setText("RSS feed successfully added.");
                    } else {
                        // status indicator if RSS feed already exists (do nothing else)
                        addRemoveStatus.setForeground(Color.RED);
                        addRemoveStatus.setText("RSS feed already exists for " + currentCompany + ".");
                    }
                } else {
                    addRemoveStatus.setForeground(Color.RED);
                    addRemoveStatus.setText("RSS feed invalid.");
                }

                // reset text field
                newURLField.setText("");
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // removes selected RSS feed from list
                String currentCompany = (String) companyBox.getSelectedItem();
                String rssToRemove = (String) rssList.getSelectedValue();
                rssMap.get(currentCompany).remove(rssToRemove);

                // reset RSS list after removal
                setRSSList();

                // sets status indicator
                addRemoveStatus.setForeground(Color.GREEN);
                addRemoveStatus.setText("RSS feed successfully removed.");
            }
        });

        compileListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // will be implemented once function exists to do this
            }
        });

        addCompanyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //set up to add new company
                String newCompany = newCompanyField.getText();
                // check to see if company already exists
                if (rssMap.keySet().contains(newCompany)) {
                    companyBox.setSelectedIndex(companies.indexOf(newCompany));
                    // status update for most recently added company (if it already has been added)
                    statusText.setForeground(Color.RED);
                    statusText.setText("Company has already been added.");
                } else {
                    rssMap.put(newCompany, new ArrayList<String>());
                    // allows for sorting of combo box
                    companyBox.removeAllItems();
                    companies.add(newCompany);
                    Collections.sort(companies);
                    for (int i = 0; i < companies.size(); i++) {
                        companyBox.addItem(companies.get(i));
                    }
                    // sets combo box to the most recently added company
                    companyBox.setSelectedIndex(companies.indexOf(newCompany));
                    // status update for most recently added company
                    statusText.setForeground(Color.GREEN);
                    statusText.setText("Company successfully added.");
                }
                newCompanyField.setText("");
            }
        });

        companyBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // resets RSS list after selecting new company
                setRSSList();
            }
        });
    }

    public boolean validateRSS(String url) {
        // uses W3 RSS validator to confirm the provided link is an RSS feed
        SOAPMessage soapResponse = null;
        String msg = "";
        // connects to the W3 API with the requested link
        try {
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();
            soapResponse = soapConnection.get("http://validator.w3.org/feed/check.cgi?output=soap12&url=" + url);

            // converts SOAP message to string
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            soapResponse.writeTo(out);
            msg = new String(out.toByteArray());

            soapConnection.close();
        } catch (Exception e) {
            System.out.println("Exception : " + e);
        }

        // checks to see if the SOAP request indicates valid RSS feed
        if (soapResponse == null) {
            return false;
        } else if (msg.contains("<m:validity>true</m:validity>")) {
            // the above string is the confirmation that a link is a valid RSS feed in SOAP message
            return true;
        }

        return false;
    }

    public static void main(String[] args) {
        // starts frame to run application
        JFrame frame = new JFrame("RSS Tracker");
        frame.setContentPane(new RSSTrackerLayout().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}

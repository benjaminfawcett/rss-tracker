import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPMessage;

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
    private JTextArea inactiveListText;
    private HashMap<String, ArrayList<String>> rssMap = new HashMap<>();
    private ArrayList<String> companies = new ArrayList<>();
    private DefaultListModel<String> rssListModel = new DefaultListModel<>();
    private HashMap<String, Integer> inactiveFor = new HashMap<>();

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
                if (companies.size() > 0) {
                    // validates provided RSS feed is real
                    if (validateRSS(newRSS)) {
                        // checks to make sure RSS feed doesn't already exist
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
                        // status indicator if passed RSS feed is invalid
                        addRemoveStatus.setForeground(Color.RED);
                        addRemoveStatus.setText("RSS feed invalid.");
                    }
                } else {
                    // status indicator if no company is currently input
                    addRemoveStatus.setForeground(Color.RED);
                    addRemoveStatus.setText("No company currently selected.");
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
                // creates hashmap of company keys with values of the number of days since last update of any RSS feed
                HashMap<String, Integer> daysSinceUpdateMap = getInactiveCompanies(rssMap);
                // clears the list of companies that fit criteria
                inactiveFor.clear();

                // list of companies that have valid RSS feeds
                ArrayList<String> companiesWithRSS = new ArrayList<>(daysSinceUpdateMap.keySet());

                // gets number of days you want to check activity for
                int dayNumber = (int) daySelector.getValue();

                // loops through to see if companies have updated within timeframe - if not, they are added to the inactive list
                for (int i = 0; i < companiesWithRSS.size(); i++) {
                    if (daysSinceUpdateMap.get(companiesWithRSS.get(i)) > dayNumber) {
                        inactiveFor.put(companiesWithRSS.get(i), daysSinceUpdateMap.get(companiesWithRSS.get(i)));
                    }
                }

                // creates list of qualifying companies for posting and sorts
                ArrayList<String> qualifyingCompanies = new ArrayList<>(inactiveFor.keySet());
                Collections.sort(qualifyingCompanies);

                // checks to make sure there are qualifying companies and posts them to the text field
                if (qualifyingCompanies.size() > 0) {
                    inactiveListText.setText(qualifyingCompanies.get(0) + " - inactive for " + inactiveFor.get(qualifyingCompanies.get(0)).toString() + " days.");
                    for (int i = 1; i < qualifyingCompanies.size(); i++) {
                        inactiveListText.append("\n" + qualifyingCompanies.get(i) + " - inactive for " + inactiveFor.get(qualifyingCompanies.get(i)).toString() + " days.");
                    }
                } else {
                    // clears list if no companies fit criteria
                    inactiveListText.setText("N/A");
                }
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


    public String lastUpdated(String url) throws IOException {
        // checks last time a RSS feed was updated using API I created and hosted on Amazon, using a python library
        URL urlForGetRequest = new URL("https://djxnbqqmpe.execute-api.us-east-1.amazonaws.com/working/?url=" + url);
        String readLine = null;
        // sets up get request
        HttpURLConnection conection = (HttpURLConnection) urlForGetRequest.openConnection();
        conection.setRequestMethod("GET");
        int responseCode = conection.getResponseCode();
        // parses get request into program
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conection.getInputStream()));
            StringBuffer response = new StringBuffer();
            while ((readLine = in.readLine()) != null) {
                response.append(readLine);
            }
            in.close();

            // parses month, day, year of last update from get request
            String message = response.toString();
            String day = message.substring(message.indexOf("day") + 6, message.indexOf("day") + 8);
            String month = message.substring(message.indexOf("month") + 8, message.indexOf("month") + 10);
            String year = message.substring(message.indexOf("year") + 7, message.indexOf("year") + 11);

            // returns formatted date for use in daysBetween
            return month + " " + day + " " + year;
        } else {
            return "failed";
        }

    }

    public int daysBetween(String date) {
        // sets up new date format
        SimpleDateFormat myFormat = new SimpleDateFormat("MM dd yyyy");
        int daysBetween = -1;

        try {
            // parses passed date
            Date date1 = myFormat.parse(date);
            // gets current date
            Date date2 = new Date();

            // finds difference between the two dates and returns as int
            long diff = date2.getTime() - date1.getTime();
            daysBetween = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return daysBetween;
    }

    public HashMap<String, Integer> getInactiveCompanies(HashMap<String, ArrayList<String>> rssList) {
        // creates hashmap with all companies as keys and values as the number of days since last post
        HashMap<String, Integer> inactiveList = new HashMap<>();
        // loops through companies to check last post
        for (int i = 0; i < companies.size(); i++) {
            String currentCompany = companies.get(i);

            // loops through rss feeds to get latest information
            ArrayList<String> rssFeeds = rssMap.get(currentCompany);
            if (rssFeeds.size() > 0) {
                for (int j = 0; j < rssFeeds.size(); j++) {
                    String date = "";
                    try {
                        date = lastUpdated(rssFeeds.get(j));
                    } catch (Exception e) {

                    }
                    // gets days since last post for specified RSS feed
                    int daysSince = daysBetween(date);

                    // sets value in previously established hashmap if updated sooner
                    if (daysSince != -1) {
                        if (inactiveList.keySet().contains(currentCompany)) {
                            if (daysSince < inactiveList.get(currentCompany)) {
                                inactiveList.replace(currentCompany, daysSince);
                            }
                        } else {
                            inactiveList.put(currentCompany, daysSince);
                        }
                    }
                }
            }

        }

        return inactiveList;
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

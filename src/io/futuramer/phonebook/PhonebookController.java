package io.futuramer.phonebook;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Controller class for FX workaround : Phonebook.fxml
 */
public class PhonebookController {

    /**
     * An instance of the phonebook class. Instantiating it upon the class-loading workaround
     */
    private RecordList recordList = new RecordList();

    /**
     * Default view index of the phonebook entry, always starting with the very first entry.
     */
    private int currentRecordViewIndex = 0;

    /**
     * Flag of the event that user is going to add new entry and just pushed "+" button.
     * This flag is used to determine the necessary actions in event of navigation workaround, possible validation of input fields and saving of the phonebook
     */
    private boolean isNewRecordAdded;

    /*
     * all @FXML annotated values are injected by FXMLLoader upon the start of application.
     * No setters required, JavaFX workaround can handle it without setters.
     * All fx:id must be equal to field name, for example fx:id="nameTextField" will be injected to TextField nameTextField
     */

    @FXML
    private Label fileNameLabel;

    @FXML
    private Label recordNumbersLabel;

    @FXML
    private TextField nameTextField;

    @FXML
    private TextField stateTextField;

    @FXML
    private TextField phoneTextField;

    @FXML
    private Button loadButton; // always enabled

    @FXML
    private Button serializeButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button addButton;

    @FXML
    private Button previousButton;

    @FXML
    private Button nextButton;

    @FXML
    private Button exitButton; // always enabled

    /*
     * Static String values.
     * Normally kept in .properties file | CMS | DB
     */
    private static final String VALIDATION_ERROR_HEADER = "Invalid value";
    private static final String SERIALIZATION_ERROR_HEADER = "Phonebook serialization failure";
    private static final String EXIT_HEADER = "Exit";
    private static final String INVALID_NAME_MESSAGE = "Invalid Name. Names should start with an uppercase letter followed by at least two characters";
    private static final String INVALID_STATE_MESSAGE = "Invalid State. States should consist of one or two words";
    private static final String INVALID_PHONE_MESSAGE = "Invalid Phone number. Ex (212) 555 - 1234";
    private static final String EXIT_MESSAGE = "Are you sure you want to exit?";
    private static final String LOAD_PHONEBOOK_HEADER = "Open Phonebook";
    private static final String SERIALIZE_PHONEBOOK_HEADER = "Serialize Phonebook";
    private static final String NAME_PATTERN = "^[A-Z][a-zA-Z]{2,}([\\s][A-Z][a-zA-Z]{2,})*$";
    private static final String STATE_PATTERN = "^[A-Z][a-zA-Z]{2,}([\\s][A-Z][a-zA-Z]{2,})?$";
    private static final String PHONE_PATTERN = "^[(][1-9][0-9]{2}[)][\\s][1-9][0-9]{2}[\\s][-][\\s][0-9]{4}$";

    /**
     * Event listener for loadButton action, this method loads XML phonebook from disk
     * @param event An Event representing some type of action, such as when a Button has been fired. Not used in method, just for the correct method signature
     */
    @FXML
    public void loadButtonAction(ActionEvent event) {
        /*
         * instantiating new fileChooser, setting title for its window, setting filter to make sure that user will see *.xml only during browsing, opening the file browser to open xml
         */
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(LOAD_PHONEBOOK_HEADER);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml"));
        File file = fileChooser.showOpenDialog(null);
        /*
         * it could be null if user closed browser without choosing the file. Performing loading if not null.
         */
        if (file != null) {
            fileNameLabel.setText("File: " + file.getName()); // setting label on UI
            try {
                /*
                 * getting new document builder factory to handle XML file, instantiating new document builder from its factory, parsing the file
                 * and creating its structure in Document object. It will throw exception in case of IO issues | unknown structure of document
                 */
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(file);
                doc.getDocumentElement().normalize(); // it is recommended to normalize the contents of the parsed XML structure

                /*
                 * instantiating new collection of type Record in order to fill it with parsed data of XML,
                 * getting collection of XML "record" entries from parsed structure
                 */
                ArrayList<Record> loadedRecordsList = new ArrayList<>();
                NodeList nList = doc.getElementsByTagName("record");

                /*
                 * handling each record from collection of XML entries, getting a single XML record element,
                 * checking if the element is a correct "record" node, creating new instance of Record class,
                 * setting its fields accordingly and adding new Record item to the collection of Records
                 */
                for (int index = 0; index < nList.getLength(); index++) {

                    Node nNode = nList.item(index);

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;

                        Record record = new Record();
                        record.setName(eElement.getElementsByTagName("name").item(0).getTextContent());
                        record.setState(eElement.getElementsByTagName("state").item(0).getTextContent());
                        record.setPhone(eElement.getElementsByTagName("phone").item(0).getTextContent());

                        loadedRecordsList.add(record);
                    }
                }

                /*
                 * saving the collection to Phonebook holder and logging
                 */
                recordList.setRecords(loadedRecordsList);
                System.out.println("File " + file.getName() + " loaded!");
            }
            /*
             * exception can happen due to IO issues, incorrect structure of XML. According to demand, first run of the application should be done with an empty xml file.
             */
            catch (ParserConfigurationException | SAXException | IOException e) {
                System.out.println("File " + file.getName() + " is damaged. Creating new Phonebook!");
                /*
                 * according to System Design, we creating new empty phonebook in case if the file was unable to be read
                 */
                recordList.setRecords(new ArrayList<>());

                /*
                 * in case of file load error informing user with popup window.
                 * Creating Alert object, setting its title and the content message, showing alert and waiting till user presses "Ok"
                 */
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(SERIALIZATION_ERROR_HEADER);
                alert.setHeaderText("File " + file.getName() + " is damaged. Creating new Phonebook");

                alert.showAndWait();
            }
            /*
             * whenever the loading of the XML was successful or not - performing some actions:
             * - resetting the view index to 0 (for example - if we have opened new phonebook - we need to start from the first record)
             * - resetting the flag indicating that user works with new record
             * - enabling "+" button, now user is able to add records from UI
             * - enabling "Serialize" button, so user is able to save records to file
             * - performing the handling of navigation workaround and the handling of evaluation of content to show
             */
            finally {
                currentRecordViewIndex = 0;
                isNewRecordAdded = false;
                addButton.setDisable(false);
                serializeButton.setDisable(false);
                handleNavigation();
                showCurrentRecord();
            }
        }
    }

    /**
     * Event listener for serializeButton action, this method saves phonebook to disk as an XML
     * @param event An Event representing some type of action, such as when a Button has been fired. Not used in method, just for the correct method signature
     */
    @FXML
    public void serializeButtonAction(ActionEvent event){
        /*
         * performing additional handling before saving the phonebook on disk:
         * - skipping validation if there are no new or old records - that means that user wants to save an empty phonebook (why not?)
         * - validating fields of current record
         * - if current record is new - saving it in phonebook
         */
        if (!recordList.getRecords().isEmpty() || isNewRecordAdded) {
            if (!isAllInputFieldsValidatedAndUpdated()) { // validating current record. It might happen that user just added or edited current record - and then wants to save the phonebook
                return; // exiting the saving process if validation fails
            }
            if (isNewRecordAdded) { // checking if user just added new record - we need to add it to collection before saving on disk
                saveNewRecord(); // saving new record to collection
            }
        }

        /*
         * saving (serializing) phonebook to XML file.
         * Instantiating new fileChooser, setting its header title and setting the filter to browse XML files only
         */
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(SERIALIZE_PHONEBOOK_HEADER);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML file (*.xml)", "*.xml"));
            File file = fileChooser.showSaveDialog(null);

            /*
             * it could be null if user closed browser without choosing the file. Performing serialization if not null.
             */
            if (file != null) {
                /*
                 * setting label with fileName on UI
                 */
                fileNameLabel.setText("File: " + file.getName());

                /*
                 * getting the instance of document builder factory to handle XML structure, getting new document builder from its factory
                 */
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

                /*
                 * creating new structure of the XML document, creating its root element and adding it to XML structure
                 */
                Document doc = docBuilder.newDocument();
                Element rootElement = doc.createElement("records");
                doc.appendChild(rootElement);

                /*
                 * handling each record in the collection of records, creating new sub-nodes "record", creating its elements, adding accordingly
                 */
                for (Record record : recordList.getRecords()) {

                    Element recordElement = doc.createElement("record");
                    rootElement.appendChild(recordElement);

                    // name element
                    Element name = doc.createElement("name");
                    name.appendChild(doc.createTextNode(record.getName()));
                    recordElement.appendChild(name);

                    // state element
                    Element state = doc.createElement("state");
                    state.appendChild(doc.createTextNode(record.getState()));
                    recordElement.appendChild(state);

                    // phone element
                    Element phone = doc.createElement("phone");
                    phone.appendChild(doc.createTextNode(record.getPhone()));
                    recordElement.appendChild(phone);
                }

                /*
                 * writing the content into xml file:
                 * getting an instance of the factory that will produce the XML writer, creating new writer of XML using the factory
                 * creating DOM structure of XML, preparing DOM structure to be written to file, saving (serializing) XML on disk.
                 */
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(file);

                transformer.transform(source, result);

                System.out.println("File " + file.getName() + " saved!");
            }
        }
        catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace(); // it should not happen
        }
    }

    /**
     * Event listener for deleteButton "-" action, this method deletes current record form phonebook
     * @param event An Event representing some type of action, such as when a Button has been fired. Not used in method, just for the correct method signature
     */
    @FXML
    private void deleteButtonAction(ActionEvent event) {
        /*
         * checking the case if user has just created new record using "+" button and then changed his mind and wants to delete it without saving:
         * disabling the flag. Nothing to delete from collection since it was not yet saved.
         */
        if (isNewRecordAdded) {
            isNewRecordAdded = false;
        }
        /*
         * in case if it is not a new record - deleting the an existing record from a collection of records according to its index
         */
        else {
            recordList.getRecords().remove(currentRecordViewIndex);
        }
        /*
         * we need to decrease the view index only in case if it is not a first entry.
         * According to SD, in case of delete action we moving navigation state to the beginning of the phonebook.
         */
        if (currentRecordViewIndex > 0) {
            currentRecordViewIndex -= 1;
        }
        handleNavigation();
        showCurrentRecord();
    }

    /**
     * Event listener for addButton "+" action, this method adds an empty phonebook record for further edit
     * @param event An Event representing some type of action, such as when a Button has been fired. Not used in method, just for the correct method signature
     */
    @FXML
    private void addButtonAction(ActionEvent event) {
        /*
         * in case if add button was already pressed before we firstly need to check if all fields values are valid
         * and if so - we must save the record in phonebook and add a new record to edit
         */
        if (isNewRecordAdded && isAllInputFieldsValidatedAndUpdated()) {
            saveNewRecord();

            /*
             * adjusting view index, making it equal to the phonebooks` size. According to SD we have to add new records to the end.
             * Setting isNewRecordAdded to true, indicating the fact that current record was just added by user
             */
            currentRecordViewIndex = recordList.getRecords().size();
            isNewRecordAdded = true;

            handleNavigation();
            showCurrentRecord();
        }
        /*
         * in case if add button was not pressed before:
         * - if the list of records was empty (first run with empty file) we can skip validation of fields
         * - otherwise we validating fields first, to be sure that user did not change existing values incorrectly
         * Adjusting view index, making it equal to the phonebooks` size. According to SD we have to add new records to the end.
         * Setting isNewRecordAdded to true, indicating the fact that current record was just added by user
         */
        else if (!isNewRecordAdded && (recordList.getRecords().isEmpty() || isAllInputFieldsValidatedAndUpdated())) {
            currentRecordViewIndex = recordList.getRecords().size();
            isNewRecordAdded = true;
            handleNavigation();
            showCurrentRecord();
        }
        // if validation failed - do not refresh the current view (do not invoke handleNavigation and showCurrentRecord)
    }

    /**
     * Event listener for previousButton "<<" action, this method performs backward navigation.
     * The validation of possibly existing new record is performed.
     * @param event An Event representing some type of action, such as when a Button has been fired. Not used in method, just for the correct method signature
     */
    @FXML
    private void previousButtonAction(ActionEvent event) {
        /*
         * validating current record. It might happen that user just added or edited current record - and then wants to navigate back to the beginning of the phonebook
         * If new record was just added and it was successfully validated - saving it.
         * Decreasing view index, it will cause the navigation to load new record with further handling
         */
        if (isNewRecordAdded && isAllInputFieldsValidatedAndUpdated()) {
            saveNewRecord();

            currentRecordViewIndex -= 1;
            handleNavigation();
            showCurrentRecord();
        }
        /*
         * if no new record was just added - just decreasing view index, it will cause the navigation to load new record with further handling
         */
        else if (!isNewRecordAdded && isAllInputFieldsValidatedAndUpdated()) {
            currentRecordViewIndex -= 1;
            handleNavigation();
            showCurrentRecord();
        }
        // no action required if validation failed
    }

    /**
     * Event listener for nextButton ">>" action, this method performs forward navigation
     * Here we do not need to check if it is necessary to save new record since this button is disabled in case of existing new record.
     * @param event An Event representing some type of action, such as when a Button has been fired. Not used in method, just for the correct method signature
     */
    @FXML
    private void nextButtonAction(ActionEvent event) {
        /*
         * checking if user did not change anything on current record incorrectly and switching to next record
         */
        if (isAllInputFieldsValidatedAndUpdated()) {
            this.currentRecordViewIndex += 1;
            handleNavigation();
            showCurrentRecord();
        }
        // no action required if validation failed
    }

    /**
     * Event listener for exitButton action, this method shows popup message and exits application upon user confirmation.
     * @param event An Event representing some type of action, such as when a Button has been fired. Not used in method, just for the correct method signature
     */
    @FXML
    private void exitButtonAction(ActionEvent event) {
        /*
         * Creating confirmation popup, setting its header and content message,
         */
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(EXIT_HEADER);
        alert.setHeaderText(EXIT_MESSAGE);

        /*
         * showing the popup and waiting for users reaction. The result is saved in Option object. It might contain button response type or null.
         * Validating the user response. isPresent method check if user has pushed any button and did not just closed the popup. Exiting application if confirmed.
         */
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK){
            System.exit(0);
        }
        // no action required is user chose "Cancel" or closed the dialog
    }

    /**
     * Method to handle the handling of navigation. Each time user navigating between records - currentRecordViewIndex changes
     * and we need to refresh the state of "<<" and ">>" buttons and the content of recordNumbersLabel (example "1 of 3") on UI
     */
    private void handleNavigation() {
        /*
         * checking current view index (if there are any unseen records left) and disabling ">>" button accordingly
         */
        nextButton.setDisable((recordList.getRecords().size() - 1) <= currentRecordViewIndex);
        /*
         * disabling "<<" button if user is now sees very first record
         */
        previousButton.setDisable(currentRecordViewIndex == 0);
        /*
         * if the phonebook is empty and there is no new record - disabling delete button. There is nothing to delete.
         */
        deleteButton.setDisable(recordList.getRecords().isEmpty() && !isNewRecordAdded);

        /*
         * declaring the number of "current" record to evaluate it below
         */
        int currentRecordNumberToShow;

        /*
         * no records in collection and no new records. currentRecordViewIndex is 0 in this case, setting it accordingly
         */
        if (recordList.getRecords().isEmpty() && !isNewRecordAdded) {
            currentRecordNumberToShow = currentRecordViewIndex;
        }
        /*
         * in case if the collection is empty but user has just pushed "+" setting it to 1 (0 + 1) since there is the first (and only) record
         */
        else if (recordList.getRecords().isEmpty() && isNewRecordAdded) {
            currentRecordNumberToShow = currentRecordViewIndex + 1;
        }
        /*
         * else regular case - many records, showing the index + 1
         */
        else {
            currentRecordNumberToShow = currentRecordViewIndex + 1;
        }

        /*
         * evaluating the number of total records.
         */
        int totalRecordsNumberToShow = isNewRecordAdded ? // is user just pushed "+" ?
                recordList.getRecords().size() + 1 : // if so - the total number of records is the size of collection + 1
                recordList.getRecords().size(); // otherwise it is the size of the phonebook

        /*
         * showing results on recordNumbersLabel on UI (example "1 of 3")
         */
        String recordNumbersLabelString = currentRecordNumberToShow + " of " + totalRecordsNumberToShow;
        recordNumbersLabel.setText(recordNumbersLabelString);
    }

    /**
     * Method to evaluate what exactly we need to show in edit fields and if these fields must be editable
     */
    private void showCurrentRecord() {
        /*
         * regular case when current view index is less that the phonebook`s size. That means that
         * - there are existing records if phonebook
         * - user sees already existing records at this moment, no new record added
         */
        if (currentRecordViewIndex < recordList.getRecords().size()) { //
            Record currentRecord = recordList.getRecords().get(currentRecordViewIndex); // getting the record from collecting according to its index
            nameTextField.setText(currentRecord.getName()); // setting fields
            stateTextField.setText(currentRecord.getState());
            phoneTextField.setText(currentRecord.getPhone());
            nameTextField.setDisable(false); // enabling the fields to be editable
            stateTextField.setDisable(false);
            phoneTextField.setDisable(false);
        }
        /*
         * case when user has just pushed "+", handling new record
         */
        else if (isNewRecordAdded) {
            nameTextField.setText(""); // new empty fields to edit
            stateTextField.setText("");
            phoneTextField.setText("");
            nameTextField.setDisable(false); // enabling the fields to be editable
            stateTextField.setDisable(false);
            phoneTextField.setDisable(false);
        }
        /*
         * no existing records in phonebook at all and no new record
         */
        else {
            nameTextField.setText(""); // no result to show
            stateTextField.setText("");
            phoneTextField.setText("");
            nameTextField.setDisable(true); // fields must NOT be editable
            stateTextField.setDisable(true);
            phoneTextField.setDisable(true);
        }
    }

    /**
     * Method to validate
     *  - if all fields are valid according to the specification
     *  - if just successfully validated record is updated (synchronized) in phonebook
     * @return true if all fields are valid, otherwise false
     */
    private boolean isAllInputFieldsValidatedAndUpdated() {
        /*
         * getting contents of edit text fileds from UI
         */
        String name = nameTextField.getText();
        String state = stateTextField.getText();
        String phone = phoneTextField.getText();

        /*
         * validating name and if it is not valid - showing an alert and waiting for users` reaction. Then returning false.
         */
        boolean isNameValid = isNameValid(name);
        if (!isNameValid) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(VALIDATION_ERROR_HEADER);
            alert.setHeaderText(INVALID_NAME_MESSAGE);

            alert.showAndWait();
            return false;
        }

        /*
         * validating state and if it is not valid - showing an alert and waiting for users` reaction. Then returning false.
         */
        boolean isStateValid = isStateValid(state);
        if (!isStateValid) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(VALIDATION_ERROR_HEADER);
            alert.setHeaderText(INVALID_STATE_MESSAGE);

            alert.showAndWait();
            return false;
        }

        /*
         * validating phone and if it is not valid - showing an alert and waiting for users` reaction. Then returning false.
         */
        boolean isPhoneValid = isPhoneValid(phone);
        if (!isPhoneValid) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(VALIDATION_ERROR_HEADER);
            alert.setHeaderText(INVALID_PHONE_MESSAGE);

            alert.showAndWait();
            return false;
        }

        /*
         * updating (synchronizing) possibly changed existing record in phonebook.
         * The handling of new (just added) records is performed with other usecases` workaround handling.
         */
        if (!isNewRecordAdded) {
            Record currentRecord = recordList.getRecords().get(currentRecordViewIndex);
            currentRecord.setName(name);
            currentRecord.setState(state);
            currentRecord.setPhone(phone);
        }

        return true; // and finally - validation Ok
    }

    /**
     * Method to validate name
     *  Name: must start with an uppercase letter followed by at least two letters (upper or lower). For simplicity, we disallow numbers and special characters.
     * @param name String value of name from UI to validate
     * @return true if validation passed, otherwise false
     */
    private boolean isNameValid(String name) {
        return name.matches(NAME_PATTERN);
    }

    /**
     * Method to validate state
     *  State: must consist of one OR two words. Each word should start with an upper case letter followed by at least two letters (upper or lower).
     * @param state String value of state from UI to validate
     * @return true if validation passed, otherwise false
     */
    private boolean isStateValid(String state) {
        return state.matches(STATE_PATTERN);
    }

    /**
     * Method to validate phone
     *  Phone: must be in the form (###) ### - #### Where # is a number between 0 and 9 except the first and fourth digits, they must be between 1 and 9. Notice the spaces after the ')' and around the '-'.
     * @param phone String value of phone from UI to validate
     * @return true if validation passed, otherwise false
     */
    private boolean isPhoneValid(String phone) {
        return phone.matches(PHONE_PATTERN);
    }

    /**
     * Method to save new record. It is not necessary to validate fields since they were already validated.
     * Creating new Record object, setting fields with values from UI fields, adding it to the phonebook.
     * Disabling flag isNewRecordAdded since we have just saved new record and no further handling required.
     */
    private void saveNewRecord() {
        Record newRecord = new Record();
        newRecord.setName(nameTextField.getText());
        newRecord.setState(stateTextField.getText());
        newRecord.setPhone(phoneTextField.getText());
        recordList.getRecords().add(newRecord);

        isNewRecordAdded = false;
    }

}

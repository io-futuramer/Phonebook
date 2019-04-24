package io.futuramer.phonebook;

import java.util.ArrayList;

/**
 * Class containing Phonebook - collection of Records
 */
class RecordList {

    /**
     * Collection of Records of phonebook
     */
    private ArrayList<Record> records;

    /**
     * Getter of Phonebook - collection of Records
     * @return ArrayList<Record> of Records
     */
    ArrayList<Record> getRecords() {
        return records;
    }

    /**
     * Setter for Phonebook
     * @param records collection of phone records
     */
    void setRecords(ArrayList<Record> records) {
        this.records = records;
    }
}

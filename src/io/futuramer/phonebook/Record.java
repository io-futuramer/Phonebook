package io.futuramer.phonebook;

/**
 * Class representing one single entry of the phonebook
 */
class Record {

    /**
     * Name
     */
    private String name;

    /**
     * State
     */
    private String state;

    /**
     * Phone number
     */
    private String phone;

    /**
     * Getter of name
     * @return name
     */
    String getName() {
        return name;
    }

    /**
     * Setter for name
     * @param name name
     */
    void setName(String name) {
        this.name = name;
    }

    /**
     * Getter of state
     * @return state
     */
    String getState() {
        return state;
    }

    /**
     * Sesser for state
     * @param state state
     */
    void setState(String state) {
        this.state = state;
    }

    /**
     * Getter of phone
     * @return phone
     */
    String getPhone() {
        return phone;
    }

    /**
     * Setter for phone
     * @param phone phone
     */
    void setPhone(String phone) {
        this.phone = phone;
    }

}

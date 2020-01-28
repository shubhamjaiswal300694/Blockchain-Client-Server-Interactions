package blockchaintask0;

// imports
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import org.json.simple.JSONObject;

// This is the Class that is used by the BlockChain class when adding a new block or performing any other function
// that needs to use the methods of the class. It overrides the toString() method to return a JSON object as a string
public class Block {

    // class variables
    private int index;
    private Timestamp timestamp;
    private String data;
    private String previousHash;
    private BigInteger nonce = BigInteger.ZERO;
    private int difficulty;

    // the main method does not do anything in this block class
    public static void main(String[] args) {

    }

    // Constructor to initialize index, timestamp, data, and difficulty
    public Block(int index, Timestamp timestamp, String data, int difficulty) {
        this.index = index;
        this.timestamp = timestamp;
        this.data = data;
        this.difficulty = difficulty;
    }

    // this method calculates the hash of the concatenated string of index, timestamp, data, difficulty,
    // previousHash, and Nonce
    // the method is taken from the CalculateHash methods in project2
    public String calculateHash() throws NoSuchAlgorithmException {
        // concatenate the string
        String text = String.valueOf(this.index)+this.timestamp+this.data+this.previousHash+this.nonce+this.difficulty;
        // Create a SHA256 digest
        MessageDigest digest;
        digest = MessageDigest.getInstance("SHA-256");
        // allocate room for the result of the hash
        byte[] hashBytes;
        // perform the hash
        digest.update(text.getBytes(StandardCharsets.UTF_8), 0, text.length());
        // collect result
        hashBytes = digest.digest();
        StringBuilder buf = new StringBuilder();
        for (byte datum : hashBytes) {
            int halfbyte = (datum >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if (halfbyte <= 9)
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = datum & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    // the method computes proof of work for the block. It calls calculateHash and checks if the
    // hash has the same number of leading zeroes as the difficulty of the block
    // if it does, the method returns the hash, else it increments the Nonce by 1
    public String proofOfWork() throws NoSuchAlgorithmException {
        // create an empty string aptHash
        String aptHash = "";
        // flag for the while loop
        boolean flag = true;
        // loop until the flag is set to false
        while (flag) {
            // set the value of calculateHash in the field
            aptHash = calculateHash();
            // get the first characters from the hash
            String firstFourChars = aptHash.substring(0, difficulty);
            // int counter to check number of leading 0
            int check = 0;
            // check if all the characters in the first characters are 0
            for (int i = 0; i < firstFourChars.length(); i++) {
                // if the ith character is 0 increment check counter
                if (firstFourChars.charAt(i) == '0') {
                    check++;
                }
            }
            // check if the counter equals the difficulty value
            if (check == difficulty) {
                // set flag to false
                flag = false;
            // if any of the first characters are not 0 then increment nonce by 1
            } else {
                nonce = nonce.add(BigInteger.ONE);
            }
        }
        // return the value of aptHash
        return aptHash;
    }

    // This method overrides the toString() method. It creates a JSON message using
    // index, timestamp, Transaction, PrevHash, Nonce, difficulty, and gets there values by calling appropriate methods
    public String toString() {
        // create a JSON object
        JSONObject blockJSON = new JSONObject();
        // put values in the JSON object
        blockJSON.put("index", String.valueOf(this.getIndex()));
        blockJSON.put("time stamp", this.getTimestamp().toString());
        blockJSON.put("Tx", this.getData());
        blockJSON.put("PrevHash", this.getPreviousHash());
        blockJSON.put("nonce", this.getNonce().toString());
        blockJSON.put("difficulty", String.valueOf(this.getDifficulty()));
        return blockJSON.toString();
    }

    // getter method for Nonce
    public BigInteger getNonce() {
        return nonce;
    }

    // getter method for index
    public int getIndex() {
        return index;
    }

    // setter method for index
    public void setIndex(int index) {
        this.index = index;
    }

    // getter method for timestamp
    public Timestamp getTimestamp() {
        return timestamp;
    }

    // setter method for timestamp
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    // getter method for data
    public String getData() {
        return data;
    }

    // setter method for data
    public void setData(String data) {
        this.data = data;
    }

    // getter method for previousHash
    public String getPreviousHash() {
        return previousHash;
    }

    // setter method for previousHash
    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    // getter method for difficulty
    public int getDifficulty() {
        return difficulty;
    }

    // setter method for difficulty
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
}
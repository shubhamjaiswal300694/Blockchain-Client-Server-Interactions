package blockChainTask1;

// imports
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;

// This is the class that is used for all the purpose of addition, corruption, verification, repair and display of the
// contents of the block chain. Once taken, the input is used to select task from the list of tasks and the functionality
// is performed by the Class in the main() routine. The BlockChain will continue to accept unless the user quits.
// It uses the Block class and its methods to perform these functions.

// When the chain is added with blocks of difficulty 4 and blocks with difficulty 5. The blocks take on an average
// 5000 milliseconds for a difficulty of block 4
// 20000 milliseconds for a difficulty of block 5
// 80000 milliseconds for a difficulty of block 6
// The block takes on an average 30000 milliseconds to verify a chain that has a block of 4 & 5 difficulty
// The block takes on an average 100000-130000 milliseconds to verify a chain that has a blocks of 4, 5, & 5 difficulty
public class BlockChain {

    // class variables
    ArrayList<Block> lst;
    String chainHash;
    int falseId;
    String beginWith;

    // the main method does nothing in this blockChain class. All its code form Task0 has been moved to the Client main()
    public static void main(String[] args) throws NoSuchAlgorithmException {

    }

    // Constructor to initialize the block chain list and chainHash to an empty string
    public BlockChain() {
        lst = new ArrayList<>();
        chainHash = "";
    }

    // getter method for current time
    public Timestamp getTime() {
        return new Timestamp(System.currentTimeMillis());
    }

    // getter method for latest block that was added in the chain
    public Block getLatestBlock() {
        return lst.get(lst.size() - 1);
    }

    // getter method for getting chain size of the block chain
    public int getChainSize() {
        return lst.size();
    }

    // this method computes the number of hashes that can be done in one second or 1000 milliseconds
    public int hashesPerSecond() throws NoSuchAlgorithmException {
        // initialize i to 0
        int i = 0;
        // get start time
        long start = System.currentTimeMillis();
        // get end time
        long end = System.currentTimeMillis();

        // run the loop until 1000 milliseconds are completed
        while (end - start < 1000) {
            // call the calculate hash method in the class
            calculateHash();
            // increment the count by 1
            i++;
            // update the end time to be used again the while loop
            end = System.currentTimeMillis();
        }
        // return the number of hashes computed in 1 second
        return i;
    }

    // this method add a block to the block chain
    public void addBlock(Block newBlock) throws NoSuchAlgorithmException {
        // adding the block to the chain
        lst.add(newBlock);
        // check if the size of the chain is 0
        if (lst.size() == 0) {
            // set previousHash to an empty string
            newBlock.setPreviousHash("");
        // if the chain size is greater than 0
        } else {
            // set the previousHash to the chainHash computed by the proofOFWork method
            newBlock.setPreviousHash(chainHash);
        }
        // set the previousHash to the chainHash computed by the proofOFWork method
        chainHash = newBlock.proofOfWork();
    }

    // this method overrides the toString method and returns a JSON string of the chain and the chain hash of the chain
    public String toString() {
        // Create new JSON object
        JSONObject blockJSON = new JSONObject();
        // Create new JSON array
        JSONArray blockArray = new JSONArray();
        // add all the blocks in the blockchain to the block array
        for (Block block : lst) {
            blockArray.add(block.toString());
        }
        // put the chain and chainHash in the JSON Object
        blockJSON.put("ds_chain", blockArray);
        blockJSON.put("chainHash", chainHash);
        // return the JSON object as String
        return blockJSON.toString();
    }

    // Check if the block chain is valid
    public boolean isChainValid() throws NoSuchAlgorithmException {
        // check for the size of the chain = 1
        if (lst.size() == 1) {
            // get the block if size is 1
            Block b = lst.get(0);
            // check if proof of work has appropriate number of leading zeroes and the chainHash of the chain equals
            // the proofOfWork of the block
            if (b.proofOfWork().substring(0,b.getDifficulty()).trim().length() == b.getDifficulty()
                    && chainHash.equals(b.proofOfWork())) {
                // return true if both conditions satisfied
                return true;
            // if both the conditions are false
            } else {
                // set falseId to 0 and beginWith to 0 of appropriate difficulty
                falseId = 0;
                beginWith = "0".repeat(b.getDifficulty());
                // return false
                return false;
            }
        // check if the size is other than 1
        } else {
            // run a for loop over the chain
            for (int i = 0; i < lst.size(); i++) {
                // store the block in a variable
                Block b = lst.get(i);
                // check if proof of work of the ith block equals the previous hash for the i+1th block
                // and proof of work has right amount of leading zeroes and the block isn't the last block
                if (i<lst.size()-1 && b.proofOfWork().substring(0,b.getDifficulty()).trim().length() == b.getDifficulty()
                        && b.proofOfWork().equals(lst.get(i+1).getPreviousHash())) {
                    // do nothing if all conditions satisfied
                // check if proof of work of the ith block equals the previous hash for the i+1th block
                // and proof of work has right amount of leading zeroes and the block is the last block
                } else if (i==lst.size()-1 && b.proofOfWork().substring(0,b.getDifficulty()).trim().length() == b.getDifficulty()
                        && chainHash.equals(b.proofOfWork())) {
                    // return true if all conditions satisfied
                    return true;
                // if the conditions aren't satisfied
                } else {
                    // set falseId to 0 and beginWith to 0 of appropriate difficulty
                    falseId = i;
                    beginWith = "0".repeat(b.getDifficulty());
                    // return false
                    return false;
                }
            }
        }
        // return true if there are no blocks in the chain
        return true;
    }

    // this block repairs the chain by computing the proof of work for all the blocks in the chain
    // and sets the chainHash to the proofOfWork of the last block
    public void repairChain() throws NoSuchAlgorithmException {
        // check for all the blocks in the chain
        for (int i = 0; i < lst.size(); i++) {
            // if the block isn't the last one, proof of work of the ith block equals the prevHash of the next block
            if (i<lst.size()-1 && !lst.get(i).proofOfWork().equals(lst.get(i+1).getPreviousHash())) {
                // set chainHash to proofOfWork of the block iteratively
                chainHash = lst.get(i).proofOfWork();
                // set previousHash using setPreviousHash method
                lst.get(i+1).setPreviousHash(chainHash);
            // if it is the last block and the chainHash equals the proof of work
            } else if (i==lst.size()-1 && !lst.get(i).proofOfWork().equals(chainHash)) {
                // set chainHash to the proofOfWork
                chainHash = lst.get(i).proofOfWork();
            }
        }
    }

    // this method is called by the Hashes per second method to compute the number of hashes per second
    // the method uses a string "00000000" and computes its hash.
    // this method is take from the Calculate Hash method in the Project2
    private void calculateHash() throws NoSuchAlgorithmException {
        String text = "00000000";
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
    }
}
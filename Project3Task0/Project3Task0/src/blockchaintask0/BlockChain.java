package blockchaintask0;

// imports
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Scanner;

// This is the class that is used for all the purpose of addition, corruption, verification, repair and display of the
// contents of the block chain. Once taken, the input is used to select task from the list of tasks and the functionality
// is performed by the Class in the main() routine. The BlockChain will continue to accept unless the user quits.
// It uses the Block class and its methods to perform these functions.
// When the chain is added with blocks of difficulty 4 and blocks with difficulty 5. The blocks take on an average
// 5000 milliseconds for a difficulty of block 4
// 15000 milliseconds for a difficulty of block 5
// 50000 milliseconds for a difficulty of block 6
// The block takes on an average 20000 milliseconds to verify a chain that has a block of 4 & 5 difficulty
// The block takes on an average 50000-60000 milliseconds to verify a chain that has a blocks of 4, 5, & 5 difficulty
public class BlockChain {

    // class variables
    private ArrayList<Block> lst;
    private String chainHash;
    private int falseId;
    private String beginWith;

    // The main method is used to form the chain. It takes the input from the user on the terminal and performs the
    // appropriate function when a number associated with a task is entered. The results are displayed on the terminal.
    // The blockchain is always accepting until the user types 6 to quit. Once the User has quit. The blockChain is reset
    public static void main(String[] args) throws NoSuchAlgorithmException {

        // Create an object of BlockChain
        BlockChain bc = new BlockChain();
        // initiate index to 0
        int index = 0;
        // add Genesis Block in the chain
        bc.addBlock(new Block(index, bc.getTime(), "Genesis", 2));
        // Keep the loop running until quit by the user
        while (true) {
            // General instructions for the user
            System.out.println("\n0. View basic blockchain status.");
            System.out.println("1. Add a transaction to the blockchain.");
            System.out.println("2. Verify the blockchain.");
            System.out.println("3. View the blockchain.");
            System.out.println("4. Corrupt the chain.");
            System.out.println("5. Hide the Corruption by repairing the chain.");
            System.out.println("6. Exit");
            System.out.println("Please enter a number corresponding to the tasks listed above");

            // ask the user to enter a number associated with the task
            Scanner keyboard = new Scanner(System.in);
            int option = Integer.parseInt(keyboard.nextLine().trim());

            // check if the option entered is 0
            if (option == 0) {
                // Display all the necessary outputs
                // chain length
                System.out.println("Current size of chain: " + bc.getChainSize());
                // hashes computed per second
                System.out.println("Current hashes per second by this machine: " + bc.hashesPerSecond());
                // most recent difficulty
                System.out.println("Difficulty of most recent block: " + bc.getLatestBlock().getDifficulty());
                // Nonce for the most recent block
                System.out.println("Nonce for most recent block: " + bc.getLatestBlock().getNonce());
                // hash of the chain
                System.out.println("Chain hash: " + bc.chainHash);
            // check if the option entered is 1
            } else if (option == 1) {
                // increment the index by 1
                index++;
                // get start time
                long start = System.currentTimeMillis();
                // ask the user to enter difficulty
                System.out.println("Enter difficulty > 0");
                Scanner difficulty = new Scanner(System.in);
                int diff = Integer.parseInt(difficulty.nextLine().trim());
                // ask the user to enter a transaction
                System.out.println("Enter transaction");
                Scanner transaction = new Scanner(System.in);
                String tran = transaction.nextLine().trim();
                // add block to the chain
                bc.addBlock(new Block(index, bc.getTime(), tran, diff));
                // get end time
                long end = System.currentTimeMillis();
                // print the time taken to execute the addition
                System.out.println("Total execution time to add this block was "+ (end - start) +" milliseconds");
            // check if the option entered is 2
            } else if (option == 2) {
                // get start time
                long start = System.currentTimeMillis();
                // get appropriate statement on screen
                System.out.println("Verifying entire chain");
                // check if chain is valid
                if (!bc.isChainValid()) {
                    // if chain in valid, print message
                    System.out.println("..Improper hash on node " + bc.falseId + " Does not begin with " + bc.beginWith);
                }
                // print final message showing the result of validation
                System.out.println("Chain Verification: " + bc.isChainValid());
                // get end time
                long end = System.currentTimeMillis();
                // print the time taken to execute the verification
                System.out.println("Total execution time to verify this chain was "+ (end - start) +" milliseconds");
            // check if the option entered is 3
            } else if (option == 3) {
                // display the chain as a JSON message using .toString message that is over ridden in the class
                System.out.println("View the Blockchain");
                System.out.println(bc.toString());
            // check if the option entered is 4
            } else if (option == 4) {
                // Chain corruption
                System.out.println("Corrupt the Blockchain");
                // ask the user the enter the id of the block to be corrupted
                System.out.println("Enter block ID of block to Corrupt");
                Scanner bId = new Scanner(System.in);
                int id = Integer.parseInt(bId.nextLine().trim());
                // ask the user to enter data for the block
                System.out.println("Enter new data for block " + id);
                Scanner nData = new Scanner(System.in);
                String nTran = nData.nextLine().trim();
                // update the data in the block to the new data
                bc.lst.get(id).setData(nTran);
                // print message showing that the message in the block has been changed
                System.out.println("Block " + id + " now holds " + nTran);
            // check if the option entered is 5
            } else if (option == 5) {
                // get start time
                long start = System.currentTimeMillis();
                // message to show chain repair in progress
                System.out.println("Repairing the entire chain");
                // repair the chain by calling .repairChain()
                bc.repairChain();
                // get end time
                long end = System.currentTimeMillis();
                // print the time taken to execute the repairing
                System.out.println("Total execution time to repair this chain was "+ (end - start) +" milliseconds");
            // break if option is 6
            } else if (option == 6) {
                break;
            // if any other number entered
            } else {
                // print appropriate response
                System.out.println("Please enter numbers between 0 and 6, inclusive");
            }
        }
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
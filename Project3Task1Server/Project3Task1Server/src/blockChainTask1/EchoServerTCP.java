package blockChainTask1;

// imports
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;
import java.util.Scanner;

@SuppressWarnings("InfiniteLoopStatement")
// This is the Server class that is usd to interact with the Client. It takes input from the user via the Client.
// It then performs the appropriate functionality based on the task chosen and send the message back to the Client.
// The Server is kept running at all times even when the Client decides to quit by pressing 6. When the Client is
// restarted the Server provides the Client to resume the chain from where it had left. The server's responses and
// displayed by the Client on the terminal/console. All the messages sent to the Client and received from the Client
// are in JSON format. The Server uses the Block and BlockChain classes made in the Task0 of this Project3.

// When the chain is added with blocks of difficulty 4 and blocks with difficulty 5. The blocks take on an average
// 5000 milliseconds for a difficulty of block 4
// 20000 milliseconds for a difficulty of block 5
// 80000 milliseconds for a difficulty of block 6
// The block takes on an average 30000 milliseconds to verify a chain that has a block of 4 & 5 difficulty
// The block takes on an average 100000-130000 milliseconds to verify a chain that has a blocks of 4, 5, & 5 difficulty
public class EchoServerTCP {

    // Creating and Initializing sockets to null.
    private Socket clientSocket = null;
    private ServerSocket listenSocket = null;

    /*The main method does all the work of calling all the appropriate methods
    in the class. It initializes the socket at the server, and is kept running.
    It receives the messages from client and does appropriate function as entered.
    It then send appropriate response to the client*/
    public static void main(String[] args) throws NoSuchAlgorithmException {
        // args give message contents and server hostname
        // Printing an appropriate print statement showing the client is Running
        System.out.println("Server Running");
        // Creating an object of the EchoServerTCP class
        EchoServerTCP server = new EchoServerTCP();
        try {
            // assign serverport a vale of 7777
            int serverPort = 7777;
            // initialize listen socket with the serverport
            server.listenSocket = new ServerSocket(serverPort);
        // Appropriate message to show if an SocketException is caught
        } catch (SocketException e) {
            System.out.println("Socket error while initialization " + e.getMessage());
        // Appropriate message to show if an IOException is caught
        } catch (IOException e) {
            System.out.println("IO error while initialization " + e.getMessage());
        }

        // create an instance of the Blockchain class
        BlockChain bc = new BlockChain();
        // initialize index to 0
        int index = 0;
        // add Genesis block
        bc.addBlock(new Block(index, bc.getTime(), "Genesis", 2));

        // keep the server running all the time
        while (true) {
            try {
                // assign the listen socket to client socket and
                // make it to accept coming responses all the time
                server.clientSocket = null;
                server.clientSocket = server.listenSocket.accept();
                // receiving the message from the client
                String data = server.receive();
                // create a JSON object from the received data after parsing the received string
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(data);
                // store the size of the json in an integer value
                int sizeJSON = json.keySet().size();
                // create a JSON object for sending back to the server
                JSONObject sendJSON = new JSONObject();
                // check if the size of the json is 4
                if (sizeJSON == 4) {
                    // Store the 1st element of the array in the variable named option
                    String option = json.get("option").toString();
                    // Store the 2nd element of the array in the variable named Ccipher
                    BigInteger Ccipher = new BigInteger(json.get("hash").toString());
                    // Store the 3rd element of the array in the variable named Ce
                    BigInteger Ce = new BigInteger(json.get("e").toString());
                    // Store the 4th element of the array in the variable named Cn
                    BigInteger Cn = new BigInteger(json.get("n").toString());
                    // decrypt the signed cipher received from the client
                    BigInteger c = Ccipher.modPow(Ce,Cn);
                    // store the string value of biginteger in to cHash
                    String cHash = c.toString().trim();
                    /*Compute the hash  of the values in the server using the values received by the client*/
                    // concatenate the values
                    String toSend = option+","+Ce.toString().trim()+","+Cn.toString().trim();
                    // create a byte array using the concatenetad string
                    byte[] bS = toSend.toLowerCase().getBytes();
                    // create a messageDigest object using the instance of SHA-256
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    // create a byte array using the digest() method
                    byte[] bigDigest = md.digest(bS);
                    // create a new array with length 1 greater than the byte array
                    byte[] newbS = new byte[bigDigest.length+1];
                    // set the first element of the byte array to 0
                    newbS[0] = 0;
                    // copy the old byte array to the new array from position 1
                    System.arraycopy(bigDigest, 0, newbS, 1, bigDigest.length + 1 - 1);
                    // convert the new bye array to a big integer
                    BigInteger m = new BigInteger(newbS);
                    // store the string value of biginteger in to sHash
                    String sHash = m.toString();
                    // Check if the hash computed by the server equals the one sent by the client
                    if (sHash.equals(cHash)) {
                        // Check if the entered task is 0
                        if (option.equals("0")) {
                            // fill json object with values
                            sendJSON.put("msg1", "Current size of chain: " + bc.getChainSize());
                            sendJSON.put("msg2", "Current hashes per second by this machine: " + bc.hashesPerSecond());
                            sendJSON.put("msg3", "Difficulty of most recent block: " + bc.getLatestBlock().getDifficulty());
                            sendJSON.put("msg4", "Nonce for most recent block: " + bc.getLatestBlock().getNonce());
                            sendJSON.put("msg5", "Chain hash: " + bc.chainHash);
                            // convert JSON to string
                            String sentJSON = sendJSON.toString();
                            // send JSON back to the client
                            server.send(sentJSON);
                        // Check if the entered task is 2
                        } else if (option.equals("2")) {
                            // get start time of task
                            long start = System.currentTimeMillis();
                            // check if the chain is valid
                            if (!bc.isChainValid()) {
                                // get end time if the chain is invalid
                                long end = System.currentTimeMillis();
                                // fill json object with values
                                sendJSON.put("msg1", "Verifying entire chain");
                                sendJSON.put("msg2", "..Improper hash on node " + bc.falseId + " Does not begin with " + bc.beginWith);
                                sendJSON.put("msg3", "Chain Verification: " + bc.isChainValid());
                                sendJSON.put("msg4", "Total execution time to add this block was "+ (end - start) +" milliseconds");
                                // convert JSON to string
                                String sentJSON = sendJSON.toString();
                                // send JSON back to the client
                                server.send(sentJSON);
                            } else {
                                // get end time if the chain is valid
                                long end = System.currentTimeMillis();
                                // fill json object with values
                                sendJSON.put("msg1", "Verifying entire chain");
                                sendJSON.put("msg2", "Chain Verification: " + bc.isChainValid());
                                sendJSON.put("msg3", "Total execution time to verify this chain was "+ (end - start) +" milliseconds");
                                // convert JSON to string
                                String sentJSON = sendJSON.toString();
                                // send JSON back to the client
                                server.send(sentJSON);
                            }
                        // Check if the entered task is 3
                        } else if (option.equals("3")) {
                            // fill json object with values
                            sendJSON.put("msg1", "View the Blockchain");
                            sendJSON.put("msg2", bc.toString());
                            // convert JSON to string
                            String sentJSON = sendJSON.toString();
                            // send JSON back to the client
                            server.send(sentJSON);
                        // Check if the entered task is 5
                        } else if (option.equals("5")) {
                            // get start time
                            long start = System.currentTimeMillis();
                            // repair the chain
                            bc.repairChain();
                            // get end time
                            long end = System.currentTimeMillis();
                            // fill json object with values
                            sendJSON.put("msg1", "Repairing the entire chain");
                            sendJSON.put("msg2", "Total execution time to repair the chain was "+ (end - start) +" milliseconds");
                            // convert JSON to string
                            String sentJSON = sendJSON.toString();
                            // send JSON back to the client
                            server.send(sentJSON);
                        }
                    } else {
                        // fill json object with values
                        sendJSON.put("msg1", "Error in request");
                        // convert JSON to string
                        String sentJSON = sendJSON.toString();
                        // send JSON back to the client
                        server.send(sentJSON);
                    }
                // check if the size of the json is 6
                } else if (sizeJSON == 6) {
                    // Store the 1st element of the array in the variable named option
                    String option = json.get("option").toString();
                    // Store the 2nd element of the array in the variable named item1
                    int item1 = Integer.parseInt(json.get("difficulty").toString());
                    // Store the 3rd element of the array in the variable named item2
                    String item2 = json.get("transaction").toString();
                    // Store the 4th element of the array in the variable named Ccipher
                    BigInteger Ccipher = new BigInteger(json.get("hash").toString());
                    // Store the 5th element of the array in the variable named Ce
                    BigInteger Ce = new BigInteger(json.get("e").toString());
                    // Store the 6th element of the array in the variable named Cn
                    BigInteger Cn = new BigInteger(json.get("n").toString());
                    // decrypt the signed cipher received from the client
                    BigInteger c = Ccipher.modPow(Ce,Cn);
                    // store the string value of biginteger in to cHash
                    String cHash = c.toString().trim();
                    /*Compute the hash  of the values in the server using the values received by the client*/
                    // concatenate the values
                    String toSend = option+","+item1+","+item2+","+Ce.toString().trim()+","+Cn.toString().trim();
                    // create a byte array using the concatenetad string
                    byte[] bS = toSend.toLowerCase().getBytes();
                    // create a messageDigest object using the instance of SHA-256
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    // create a byte array using the digest() method
                    byte[] bigDigest = md.digest(bS);
                    // create a new array with length 1 greater than the byte array
                    byte[] newbS = new byte[bigDigest.length+1];
                    // set the first element of the byte array to 0
                    newbS[0] = 0;
                    // copy the old byte array to the new array from position 1
                    System.arraycopy(bigDigest, 0, newbS, 1, bigDigest.length + 1 - 1);
                    // convert the new bye array to a big integer
                    BigInteger m = new BigInteger(newbS);
                    // store the string value of biginteger in to sHash
                    String sHash = m.toString();
                    // check if the hash computed by the server is same as the one sent by the client
                    if (sHash.equals(cHash)) {
                        // Check if the entered task is 1
                        if (option.equals("1")) {
                            // increment the index by 1 during every block addition
                            index++;
                            // get start time
                            long start = System.currentTimeMillis();
                            // add block to the block chain
                            bc.addBlock(new Block(index, bc.getTime(), item2, item1));
                            // get end time
                            long end = System.currentTimeMillis();
                            // fill json object with values
                            sendJSON.put("msg1", "Total execution time to add this block was "+ (end - start) +" milliseconds");
                            // convert JSON to string
                            String sentJSON = sendJSON.toString();
                            // send JSON back to the client
                            server.send(sentJSON);
                        // Check if the entered task is 4
                        } else if (option.equals("4")) {
                            // update the transaction in the block chosen to be corrupted
                            bc.lst.get(item1).setData(item2);
                            // fill json object with values
                            sendJSON.put("msg1", "Corrupt the Blockchain");
                            sendJSON.put("msg2", "Block " + item1 + " now holds " + item2);
                            // convert JSON to string
                            String sentJSON = sendJSON.toString();
                            // send JSON back to the client
                            server.send(sentJSON);
                        }
                    } else {
                        // fill json object with values
                        sendJSON.put("msg1", "Error in request");
                        // convert JSON to string
                        String sentJSON = sendJSON.toString();
                        // send JSON back to the client
                        server.send(sentJSON);
                    }
                } else {
                    System.out.println();
                }
            // Appropriate message to show if an IOException or NoSuchAlgorithmException is caught
            } catch (IOException | NoSuchAlgorithmException e) {
                System.out.println();
            // Appropriate message to show if an NumberFormatException is caught
            } catch (NumberFormatException e) {
                // create a JSON object for sending back to the server
                JSONObject sendJSON = new JSONObject();
                // fill json object with values
                sendJSON.put("msg1", "Error in request. Please enter only numeric values for the Value field.");
                // convert JSON to string
                String sentJSON = sendJSON.toString();
                // send JSON back to the client
                server.send(sentJSON);
            } catch (ParseException e) {
            }
        }
    }

    /*The receive method is used to receive the response from the client and returns 
    a string of the response in the main method. The method prepares a datagram packet*/
    private String receive() {
        try {
            // create a scanner object
            Scanner in;
            // get the input stream from the clientsocket
            in = new Scanner(clientSocket.getInputStream());
            // store the nextline from the input stream in a new string variable
            String data = in.nextLine();
            // check if the input stream is empty
            if (data.equals("")) {
                // leave a line for better appearance
                System.out.println();
                // print appropriate response on the server terminal
                System.out.println("The client has quit");
            }
            // return data as received from the client
            return data;
        // Appropriate message to show if an IOException is caught
        } catch (IOException e) {
            System.out.println("IO error while receiving " + e.getMessage());
        // Appropriate message to show if an NoSuchElementException is caught
        } catch (NoSuchElementException f) {
            System.out.println();
        }
        // return empty string if code fails
        return "";
    }

    /*The send method is used to send the messages to the client. It creates a datagram packet 
    using the String message before sending it back to the client.*/
    private void send(String msg) {
        try {
            // Set up "out" to write to the client socket
            PrintWriter out;
            // store the output stream from the client stream in the out variable
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
            // send the out stream to the client using flush
            out.println(msg);
            out.flush();
        // Appropriate message to show if an SocketException is caught
        } catch (SocketException e) {
            System.out.println("Socket error while sending " + e.getMessage());
        // Appropriate message to show if an IOException is caught
        } catch (IOException e) {
            System.out.println("IO error while sending " + e.getMessage());
        }
    }
}
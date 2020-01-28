package blockChainTask1;

// Imports
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.util.Random;
import java.util.Scanner;

// This is the Client class that is usd to interact with the user and the server. It takes input from the user in the
// form of an integer that is associated to the tasks shown on the terminal. It then interacts with the server to get
// the appropriate response and displays it on the terminal/console. All the messages sent to the server and received
// from the server are in JSON format. The Server uses the Block and BlockChain classes made in the Task0 of this Project3.

// When the chain is added with blocks of difficulty 4 and blocks with difficulty 5. The blocks take on an average
// 5000 milliseconds for a difficulty of block 4
// 20000 milliseconds for a difficulty of block 5
// 80000 milliseconds for a difficulty of block 6
// The block takes on an average 30000 milliseconds to verify a chain that has a block of 4 & 5 difficulty
// The block takes on an average 100000-130000 milliseconds to verify a chain that has a blocks of 4, 5, & 5 difficulty
public class EchoClientTCP {
    //create a client socket, and initialize it to null
    private Socket clientSocket = null;

    /*The main method does all the work for this Java class. Creates an object of the class,
    initializes the client socket, and keeps it running until the client calls quit. keeps sending
    the messages to the server and receives back the response from the server in the form of a 
    string. The socket closes if the user enters quit*/
    public static void main(String[] args) {
        try {
            // args give message contents and server hostname
            // Printing an appropriate print statement showing the client is Running
            System.out.println("Client Running");
            // Create an object of the EchoClientTCP class
            EchoClientTCP Client = new EchoClientTCP();
            // get the private and public keys from the RSA() method.
            // the RSA method is taken from the code already given to us on schedule
            String keys = Client.RSA();
            // split the keys on comma
            String[] msgs = keys.split(",");
            // Store the 1st element from the keys array in e
            // e is the exponent of the public key
            BigInteger e = new BigInteger(msgs[0].trim());
            // Store the 2nd element from the keys array in d
            // d is the exponent of the private key
            BigInteger d = new BigInteger(msgs[1].trim());
            // Store the 3rd element from the keys array in n
            // n is the modulus for both the private and public keys
            BigInteger n = new BigInteger(msgs[2].trim());
            // Initialize the Socket using the init() method
            Client.init();
            // keep the client running unless it receives a command to quit
            while (true) {
                // check if the client socket is closed
                if (!Client.clientSocket.isClosed()) {
                    // Initialize the Socket using the init() method
                    Client.init();
                    // send the messages to the server using the send() method
                    Client.send(e, d, n);
                    // if the socket is not closed, receive the response from the server using receive() method
                    String receivedStuff = Client.receive();

                    // create a JSON object from the received data after parsing the received string
                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject) parser.parse(receivedStuff);
                    // print every response sent by the server, which is stored in the json object above
                    for (Object s : json.values()) {
                        // display the response from the server on the client console
                        System.out.println(s.toString());
                    }

                } else {
                    // if the client socket is closed, break the loop.
                    // the socket will be closed only if the client types quit.
                    break;
                }
            }
        } catch (ParseException e) {
        }
    }

    /*The init method is used to initialize the socket on the client end.
    It uses the "localhost" as the host name, port 7272 as the serverport
    to initialize the socket*/
    private void init() {
        try {
            // create a new socket using localhost as address and 7777 as serverport
            clientSocket = new Socket("localhost", 7777);
        // Appropriate message to show if an IOException is caught
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*The add method is used to send the messages to the server. It creates a datagram 
    packet using the String message before sending it to the server.*/
    private void send(BigInteger e, BigInteger d, BigInteger n) {
        try {
            // Set up "out" to write to the client socket
            // store the output stream from the client stream in the out variable
            PrintWriter out = new PrintWriter(new BufferedWriter
                    (new OutputStreamWriter(clientSocket.getOutputStream())));
            // Basic instructions for the client
            System.out.println("\n0. View basic blockchain status.");
            System.out.println("1. Add a transaction to the blockchain.");
            System.out.println("2. Verify the blockchain.");
            System.out.println("3. View the blockchain.");
            System.out.println("4. Corrupt the chain.");
            System.out.println("5. Hide the Corruption by repairing the chain.");
            System.out.println("6. Exit");
            System.out.println("Please enter a number corresponding to the tasks listed above");

            // ask the user to select one task by entering a number
            Scanner keyboard = new Scanner(System.in);
            int option = Integer.parseInt(keyboard.nextLine().trim());

            // check if the option entered is 0,2,3, or 5
            if (option == 0 || option == 2 || option == 3 || option == 5) {
                // concatenate the strings to be hashed
                String toSend = option+","+ e.toString().trim()+","+ n.toString().trim();
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
                // encrypt the big integer using the private key (d,n)
                BigInteger c = m.modPow(d, n);

                // Create new JSON object
                JSONObject sendJSON = new JSONObject();
                // fill json object with values
                sendJSON.put("e", e.toString().trim());
                sendJSON.put("n", n.toString().trim());
                sendJSON.put("option", option);
                sendJSON.put("hash", c.toString());
                // convert JSON to string
                String sentJSON = sendJSON.toString();
                // send to the server using .flush()
                out.println(sentJSON);
                out.flush();
            // check if the option entered is 1
            } else if (option == 1) {
                // ask the client to enter a difficulty
                System.out.println("Enter difficulty > 0");
                Scanner difficulty = new Scanner(System.in);
                int diff = Integer.parseInt(difficulty.nextLine().trim());
                // ask the client to enter a transaction
                System.out.println("Enter transaction");
                Scanner transaction = new Scanner(System.in);
                String tran = transaction.nextLine().trim();
                // concatenate the strings to be hashed
                String toSend = option+","+diff+","+tran+","+e.toString().trim()+","+ n.toString().trim();
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
                // encrypt the big integer using the private key (d,n)
                BigInteger c = m.modPow(d, n);

                // Create new JSON object
                JSONObject sendJSON = new JSONObject();
                // fill json object with values
                sendJSON.put("e", e.toString().trim());
                sendJSON.put("n", n.toString().trim());
                sendJSON.put("option", option);
                sendJSON.put("hash", c.toString());
                sendJSON.put("difficulty", diff);
                sendJSON.put("transaction", tran);
                // convert JSON to string
                String sentJSON = sendJSON.toString();
                // send to the server using .flush()
                out.println(sentJSON);
                out.flush();
            // check if the option entered is 4
            } else if (option == 4) {
                // ask the user for a block to be corrupted
                System.out.println("Enter block ID of block to Corrupt");
                Scanner bId = new Scanner(System.in);
                int id = Integer.parseInt(bId.nextLine().trim());
                // ask the user for the data to be updated in the block
                System.out.println("Enter new data for block " + id);
                Scanner nData = new Scanner(System.in);
                String nTran = nData.nextLine().trim();
                // concatenate the strings to be hashed
                String toSend = option+","+id+","+nTran+","+ e.toString().trim()+","+ n.toString().trim();
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
                // encrypt the big integer using the private key (d,n)
                BigInteger c = m.modPow(d, n);

                // Create new JSON object
                JSONObject sendJSON = new JSONObject();
                // fill json object with values
                sendJSON.put("e", e.toString().trim());
                sendJSON.put("n", n.toString().trim());
                sendJSON.put("option", option);
                sendJSON.put("hash", c.toString());
                sendJSON.put("difficulty", id);
                sendJSON.put("transaction", nTran);
                // convert JSON to string
                String sentJSON = sendJSON.toString();
                // send to the server using .flush()
                out.println(sentJSON);
                out.flush();
            // check if the option entered is 6
            } else if (option == 6) {
                try {
                    // close the client
                    clientSocket.close();
                } catch (IOException x) {
                    x.printStackTrace();
                }
            } else {
                // msg if number entered is other than 0 to 6
                System.out.println("You had one job! *FacePalm*. Enter only numbers between 0 and 6, inclusive.");
            }
        // Appropriate message to show if an SocketException is caught
        } catch (SocketException x) {
            System.out.println("Socket error while sending " + x.getMessage());
        // Appropriate message to show if an IOException is caught
        } catch (IOException x) {
            System.out.println("IO error while sending " + x.getMessage());
        // Appropriate message to show if an Exception is caught
        } catch (Exception ex) {
        }
    }

    /*The receive method is used to receive the response from the server and returns 
    a string of the response in the main method. The method prepares a datagram packet*/
    private String receive() {
        try {
            // check if the client socket is closed
            if (!clientSocket.isClosed()) {
                // Set up "in" to read from the server socket
                // store the input stream from the server stream in the "in" variable
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                return in.readLine();
            // if the socket is closed, print appropriate message on the client console
            } else {
                System.out.println("Socket has been closed!");
            }
        // Appropriate message to show if an IOException is caught
        } catch (IOException e) {
            System.out.println("IO error while receiving " + e.getMessage());
        }
        // return "" if code fails
        return "";
    }

    /*This method is taken from the code already given on the schedules page.
    The method returns a concatenated string of e,d,n. 
    e is the exponent of the public key 
    d is the exponent of the private key
    n is the modulus for both the private and public keys
    These are calculated using big integer operation in the method*/
    private String RSA() {
        // Each public and private key consists of an exponent and a modulus
        BigInteger e; // e is the exponent of the public key
        BigInteger d; // d is the exponent of the private key
        BigInteger n; // n is the modulus for both the private and public keys
        Random rnd = new Random();
        // Step 1: Generate two large random primes.
        // We use 400 bits here, but best practice for security is 2048 bits.
        // Change 400 to 2048, recompile, and run the program again and you will
        // notice it takes much longer to do the math with that many bits.
        BigInteger p = new BigInteger(400,100,rnd);
        BigInteger q = new BigInteger(400,100,rnd);
        // Step 2: Compute n by the equation n = p * q.
        n = p.multiply(q);
        // Step 3: Compute phi(n) = (p-1) * (q-1)
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        // Step 4: Select a small odd integer e that is relatively prime to phi(n).
        // By convention the prime 65537 is used as the public exponent.
        e = new BigInteger ("65537");
        // Step 5: Compute d as the multiplicative inverse of e modulo phi(n).
        d = e.modInverse(phi);
        return e.toString().trim()+","+d.toString().trim()+","+n.toString().trim();
    }
}
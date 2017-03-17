import ChatApp.*;          // The package containing our stubs. 
import org.omg.CosNaming.*; // HelloServer will use the naming service. 
import org.omg.CosNaming.NamingContextPackage.*; // ..for exceptions. 
import org.omg.CORBA.*;     // All CORBA applications need these classes. 
import org.omg.PortableServer.*;   
import org.omg.PortableServer.POA;
import java.util.ArrayList;
import java.util.*;
import java.lang.Integer;
 
class ChatImpl extends ChatPOA
{
    private ORB orb;
    private Map<String, ChatCallback> players = new HashMap<String, ChatCallback>();
    private static final int BOARD_SIZE = 8;
    private static final int EMPTY = 0;
    private static final int CROSS = 1;
    private static final int CIRCLE = 2;
    private int[][] board = new int[BOARD_SIZE][BOARD_SIZE];

    public void setORB(ORB orb_val) {
        orb = orb_val;
    }

    public String say(ChatCallback callobj, String msg)
    {
        return (msg);
    }
    public String join(ChatCallback callobj, String msg)
    {
        //callobj.callback(msg);
	
	if (players.containsKey(msg) || players.containsValue(callobj) || msg.equals(""))  {
	    return ("Name taken or session already has an active player");
	}
	players.put(msg, callobj);

        return ("Welcome " + msg);
    }
    public String list(ChatCallback callobj) {
	StringBuilder sb = new StringBuilder();

	Set<String> keys = players.keySet();
        for(String key: keys){
            sb.append(key + "\n");
        }
	
	return sb.toString();
    }
    public void leave(ChatCallback callobj) {
	String sender = "";
	for (Map.Entry<String, ChatCallback> e: players.entrySet()) {
	    if (e.getValue().equals(callobj)) {
		sender = e.getKey();
	    }
	}

	players.remove(sender);
	Set<String> keys = players.keySet();
	
	for(String key: keys) {
	    players.get(key).callback(sender + " has left"); 
	}
    }
    public void post(ChatCallback callobj, String msg) {
	String sender = "";

	for (Map.Entry<String, ChatCallback> e: players.entrySet()) {
	    if (e.getValue().equals(callobj)) {
		sender = e.getKey();
	    }
	}
	if(!sender.equals("")){
	    Set<String> keys = players.keySet();
	
	    for(String key: keys) {
		players.get(key).callback(sender + ": " + msg); 
	    }
	}
	else {
	    callobj.callback("You must join chat before posting");
	}
    }
    public void init(ChatCallback callobj) {
	for(int y = 0; y < BOARD_SIZE; y++) {
	    for(int x = 0; x < BOARD_SIZE; x++) {
		board[y][x] = EMPTY;
	    }
	}
	callobj.callback("BOARD INITIATED");
    }
    public void printBoard(ChatCallback callobj) {
	String row = "";
	for(int y = 0; y < BOARD_SIZE; y++) {
	    for(int x = 0; x < BOARD_SIZE; x++) {
		switch (board[y][x]) {
		case EMPTY :
		    row += "| ";
		    break;
		case CROSS : 
		    row += "|x";
		    break;
		case CIRCLE :
		    row += "|o";
		}
	    }
	    callobj.callback(row + "|");
	    row = "";
	}
	callobj.callback("");
    }
    public void put(ChatCallback callobj, String msg) {
	String[] move = new String[4];
	move = msg.replace(" ", "").split("");
	int row = Integer.parseInt(move[1]);
	int col = Integer.parseInt(move[2]);
	int type = Integer.parseInt(move[3]);

	if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE && board[row][col] == EMPTY) {
            board[row][col] = type;
	} 
	else {
	    callobj.callback("Invalid move");
         }
	if(hasWon(row, col, type)) {
	    callobj.callback("YOU WON!!!!!!!!!");
	}
    }
    public boolean hasWon(int row, int col, int type) {
	int points = 0;
	int startrow = row;
	int startcol = col;
	for(int i = 0; i < BOARD_SIZE; i++) {
	    if(board[row][i] == type) {
		points++;
	    }
	    else {
		points = 0;
	    }
	    if(points == 5) {
		return true;
	    }
	}

	for(int i = 0; i < BOARD_SIZE; i++) {
	    if(board[i][col] == type) {
		points++;
	    }
	    else {
		points = 0;
	    }
	    if(points == 5) {
		return true;
	    }
	}

	for(int i = 0; i < BOARD_SIZE; i++) {
	    if(startcol == 0 || startrow == 0) {
		break;
	    }
	    startrow--;
	    startcol--;
	    
	}

	for(int i = 0; i < BOARD_SIZE; i++) {
	    if((startrow + i) >= 0 && (startrow + i) < BOARD_SIZE 
	       && (startcol + i) >= 0 && (startcol + i) < BOARD_SIZE) {
		if(board[startrow + i][startcol + i] == type) {
		    points++;
		}
		else {
		    points = 0;
		}
		if(points == 5) {
		    return true;
		}
	    }
	}
	
	startrow = row;
	startcol = col;
	for(int i = 0; i < BOARD_SIZE; i++) {
	    if(startcol == (BOARD_SIZE - 1) || startrow == 0) {
		break;
	    }
	    startrow--;
	    startcol++;
	    
	}

	for(int i = 0; i < BOARD_SIZE; i++) {
	    if((startrow + i) >= 0 && (startrow + i) < BOARD_SIZE 
	       && (startcol - i) >= 0 && (startcol - i) < BOARD_SIZE) {
		if(board[startrow + i][startcol - i] == type) {
		    points++;
		}
		else {
		    points = 0;
		}
		if(points == 5) {
		    return true;
		}
	    }
	}
	return false;
    }
}

public class ChatServer 
{
    public static void main(String args[]) 
    {
	try { 
	    // create and initialize the ORB
	    ORB orb = ORB.init(args, null); 

	    // create servant (impl) and register it with the ORB
	    ChatImpl chatImpl = new ChatImpl();
	    chatImpl.setORB(orb); 

	    // get reference to rootpoa & activate the POAManager
	    POA rootpoa = 
		POAHelper.narrow(orb.resolve_initial_references("RootPOA"));  
	    rootpoa.the_POAManager().activate(); 

	    // get the root naming context
	    org.omg.CORBA.Object objRef = 
		           orb.resolve_initial_references("NameService");
	    NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

	    // obtain object reference from the servant (impl)
	    org.omg.CORBA.Object ref = 
		rootpoa.servant_to_reference(chatImpl);
	    Chat cref = ChatHelper.narrow(ref);

	    // bind the object reference in naming
	    String name = "Chat";
	    NameComponent path[] = ncRef.to_name(name);
	    ncRef.rebind(path, cref);

	    // Application code goes below
	    System.out.println("ChatServer ready and waiting ...");
	    
	    // wait for invocations from clients
	    orb.run();
	}
	    
	catch(Exception e) {
	    System.err.println("ERROR : " + e);
	    e.printStackTrace(System.out);
	}

	System.out.println("ChatServer Exiting ...");
    }

}

//https://github.com/planetes00/SimpleChat
import java.net.*;
import java.io.*;
import java.util.*;
/*
    1. 접속 사용자 목록보기(/userlist)
    send_userlist()
    2. 내가 타이핑 한거 다시 안나오게 만들기
    3. 금지어 경고기능. 5개 이상 목록 만들어서 차단 못 때림.
*/


public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);//10001
			System.out.println("Waiting connection...");
			HashMap <String, PrintWriter> hm = new HashMap();//얘는 왜 밖에서 선언하
			while(true){
				Socket sock = server.accept();//얘는 왜 안에서 선언하는걸까?? 서버 소켓 accept
				ChatThread chatthread = new ChatThread(sock, hm);//객체를 날려주면 레퍼런스가 날아갑니다.(포인터 던지기)
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	//금지어 목록.
    private ArrayList<String> Forbidden= new ArrayList<String>(Arrays.asList("lab", "quiz", "middle", "last", "exam" ));
	
	
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered.");
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				if(line.equals("/quit"))
					break;
                //유저리스트 출력하라고 하면 출력해준다.
                if (line.equals("/userlist")) send_userlist();
                //만약에 여기서 금지어가 걸리면 안된다고 알려준다.
                else if(isForbidden(line)) youNo();
				else if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}else
					broadcast(id + " : " + line);
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
        Object obj;
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
            synchronized(hm){
			obj = hm.get(to);
            }
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	public void broadcast(String msg){
		synchronized(hm){
            Set set = hm.entrySet();
            Iterator iterator = set.iterator();
			while(iterator.hasNext()){
                Map.Entry entry = (Map.Entry)iterator.next();
                String key = (String)entry.getKey();
				PrintWriter pw = (PrintWriter)entry.getValue();
                //pw에 메세지를 보내기 전에 key 값을 확인하여 본인에게 메세지를 보내지 않는다.
				if(!this.id.equals(key))pw.println(msg);
				pw.flush();
			}
		}
	} // broadcast
    //유저 리스트 뽑는 함수입니다. 해쉬맵 한바퀴 돌아서 유저 이름을 어레이 리스트에 저장한뒤
    //유저 스레드에 저장된 this.id를 이용해 유저pw에 어레이리스트를 모두 출력합니다.
    public void send_userlist(){
        ArrayList<String> names=new ArrayList<>();
        Object obj;
        int num=0;
        //해쉬맵 싱크로!!
        synchronized(hm){
            Set set = hm.entrySet();
            Iterator iterator = set.iterator();
            //돌립니다.
            while(iterator.hasNext()){
                Map.Entry entry = (Map.Entry)iterator.next();
                //키를 받아옵니다.
                String key = (String)entry.getKey();
                //저장한다.
                names.add(key);
                num++;
            }
            //해쉬맵에서 이 유저의 pw를 건저온다.
            obj = hm.get(this.id);
        }
    //출력
    PrintWriter pw = (PrintWriter)obj;
    for(String msg: names) pw.println(msg);
    pw.println("number of chatter: "+num);
    pw.flush();
        
    }
    //유저가 입력한 line에 금지어가 있는지 검사하여 있으면 true, 없으면 flase를 출력합니다.
    public boolean isForbidden(String line){
        //senduserlist에 들어있는 단어가 line에 들어있는지 검사합니다.
        for(String nono: this.Forbidden){
            if(line.contains(nono)) return true;
        }
        return false;
    }
    //해당 유저에게 경고메세지를 출력
    public void youNo(){
        //해당 유저의 pw를 건진다.
        Object obj;
        synchronized(hm){
            obj = hm.get(this.id);
        }
        PrintWriter pw = (PrintWriter)obj;
        //경고메세지를 출력한다.
        pw.println("WARNING: You have used banned words.");
        pw.flush();
    }
}

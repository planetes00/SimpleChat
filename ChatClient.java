//https://github.com/planetes00/SimpleChat
import java.net.*;
import java.io.*;

public class ChatClient {

	public static void main(String[] args) {
		if(args.length != 2){
			System.out.println("Usage : java ChatClient <username> <server-ip>");//commandline argument
			System.exit(1);
		}
		Socket sock = null;
		BufferedReader br = null;
		PrintWriter pw = null;
		boolean endflag = false;
		try{
			sock = new Socket(args[1], 10001);
			//socket in/out, input from keyboard
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			//키보드->버퍼리더에서 일어들어욤 인풋 스트림리더-키보드로 부터 읽어들어오는 
			//systemin-read a byte//buffered reader-read a line-set String
			//inputStrieamReader is executed like a input changer->hdmi/rgb 
			//InputSt... r1=new InputSt...();was on here,but go short
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
			
			// send username.
			pw.println(args[0]);
			pw.flush();
			InputThread it = new InputThread(sock, br);
			it.start();
            String line = null;//라인은 키보드로부터 오네용??
			while((line = keyboard.readLine()) != null){//무한반복하는 while이 핵심인데...
				pw.println(line);
				pw.flush();
				if(line.equals("/quit")){//파개조건 
					endflag = true;
					break;
				}
			}
			System.out.println("Connection closed.");
		}catch(Exception ex){
			if(!endflag)
				System.out.println(ex);
		}finally{
			try{
				if(pw != null)
					pw.close();
			}catch(Exception ex){}
			try{
				if(br != null)
					br.close();
			}catch(Exception ex){}
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		} // finally
	} // main
} // class

class InputThread extends Thread{
	private Socket sock = null;
	private BufferedReader br = null;
	public InputThread(Socket sock, BufferedReader br){
		this.sock = sock;
		this.br = br;
	}
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				System.out.println(line);
			}
		}catch(Exception ex){
		}finally{
			try{
				if(br != null)
					br.close();
			}catch(Exception ex){}
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // InputThread
}

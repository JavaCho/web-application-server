package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.User;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	
	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run(){
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());
		
		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.in에 요청한 정보가 담김 라인단위로 끊어 읽음, 서버가 클라이언트에 응답을 보낼땐 out
			 BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			 
			 String line = reader.readLine();
				byte[] body = line.getBytes();
				String[] tokens = line.split(" ");
				String url=tokens[1];
				if(url.startsWith("/login"))
				{
					
				}
				
				else if(url.startsWith("/create")) {
					int index = url.indexOf("?");
					String requestPath = url.substring(0, index);
					Map<String, String> params = HttpRequestUtils.parseQueryString(url.substring(index+1));
					User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
					System.out.println("User : " + user);
					DataOutputStream dos = new DataOutputStream(out);
					response302Header(dos);
				} else {
					if(url.equals("/")){
						url="/index.html";
					}
					DataOutputStream dos = new DataOutputStream(out);
					body = Files.readAllBytes(new File("./webapp" + url).toPath());
					response200Header(dos, body.length);
					responseBody(dos, body);					
				}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		
		}
	

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	private void response302Header(DataOutputStream dos) {
		try {
			dos.writeBytes("HTTP/1.1 302 OK \r\n");
			dos.writeBytes("Location: /index.html\r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.writeBytes("\r\n");
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}

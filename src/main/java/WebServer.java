import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;

public class WebServer
{
	private static final int webServerPort = 10124;
	private static HttpServer obj_HttpServer;
	
	static void init()
	{
		new Thread(() ->
		{
			try
			{
				System.out.println("在端口 [ " + webServerPort + " ] 上启动 Web API 接口." + Now());
				obj_HttpServer = HttpServer.create(new InetSocketAddress(webServerPort), 0);
/*
				obj_HttpServer.createContext("/", exchange ->
				{
					// localhost:10124/console/HeartBeat?serverID=123
					
					System.out.println(exchange.getRequestURI().toString()); // /console/HeartBeat?serverID=123
					System.out.println(exchange.getRequestURI().getPath()); // /console/HeartBeat
					System.out.println(exchange.getRequestURI().getQuery()); // serverID=123

					// localhost:10124/console/HeartBeat?serverID=123&serverOpera=heartbeat
					
					System.out.println(queryToMap(exchange.getRequestURI().getQuery())); // {serverOpera=heartbeat, serverID=123}
					System.out.println(queryToMap(exchange.getRequestURI().getQuery()).get("serverID")); // 123
					
					exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
					byte[] httpReply = new byte[0];
					exchange.sendResponseHeaders(200, httpReply.length);
					exchange.getResponseBody().write(httpReply);
					exchange.close();
				});
*/
				// WebServer::HTTPMethod_HeartBeat 等价于 exchange -> { HTTPMethod_HeartBeat(exchange) }
				obj_HttpServer.createContext("/heartbeat", WebServer::HTTPMethod_HeartBeat);
				obj_HttpServer.createContext("/control", WebServer::HTTPMethod_control);
				obj_HttpServer.createContext("/", WebServer::HTTPMethod_Null);
				
				obj_HttpServer.setExecutor(Executors.newCachedThreadPool());
				obj_HttpServer.start();
			} catch (IOException e)
			{
				System.out.println("Web Server Services 端口被占用,请检查!" + Now());
				System.exit(2);
			}
		}).start();
	}
	
	public static Map<String, String> queryToMap(String query)
	{
		Map<String, String> result = new HashMap<>();
		if (query != null)
		{
			for (String param : query.split("&"))
			{
				String[] entry = param.split("=");
				if (entry.length > 1)
				{
					result.put(entry[0], entry[1]);
				} else
				{
					result.put(entry[0], "");
				}
			}
		}
		return result;
	}
	
	private static void HTTPMethod_Null(HttpExchange exchange)
	{
		HttpSend(exchange, "");
	}
	
	private static void HTTPMethod_HeartBeat(HttpExchange exchange)
	{
		int serverID;
		try
		{
			serverID = Integer.parseInt(queryToMap(exchange.getRequestURI()
					.getQuery()).get("serverID"));
		} catch (NumberFormatException e)
		{
			serverID = 0;
		}
		if (serverID > 0 && serverID < 65536)
		{
			System.out.println("[ 上报心跳 ] 端口:" + serverID + "" + Now());
			SetServerHeartbeat(serverID);
			HttpSend(exchange, "上报了端口为" + serverID + "的心跳.");
		} else
		{
			System.out.println("[ 上报心跳错误 ] 服务器端口" + serverID + "不存在." + Now());
			HttpSend(exchange, "服务器端口" + serverID + "不存在.");
			
		}
	}
	
	private static void HTTPMethod_control(HttpExchange exchange)
	{
		// 修复直接访问 /control/ 显示无内容
		if (exchange.getRequestURI()
				.toString()
				.equals(exchange.getRequestURI()
						.getPath()))
		{
			HttpSend(exchange, "");
		}
		// 修复找不到Key时的400报错
		if (! queryToMap(exchange.getRequestURI()
				.getQuery()).containsKey("serverID") || ! queryToMap(exchange.getRequestURI()
				.getQuery()).containsKey("serverCommand") || queryToMap(exchange.getRequestURI()
				.getQuery()).get("serverID")
				    .isBlank() || queryToMap(exchange.getRequestURI()
				.getQuery()).get("serverCommand")
				    .isBlank())
		{
			HttpSend(exchange, "");
		}
		int serverID = Integer.parseInt(queryToMap(exchange.getRequestURI()
				.getQuery()).get("serverID"));
		String serverCommand = queryToMap(exchange.getRequestURI()
				.getQuery()).get("serverCommand");
		if (serverID > 0 && serverID < 65536)
		{
			switch (serverCommand)
			{
				case "start":
					System.out.println("[ 执行指令 ] " + serverCommand + " -> " + serverID + "" + Now());
					HttpSend(exchange, "[ 执行指令 ] " + serverCommand + " @ " + serverID);
					System.out.println("启动所有服务器端." + WebServer.Now());
					break;
				case "stop":
					System.out.println("[ 执行指令 ] " + serverCommand + " -> " + serverID + "" + Now());
					HttpSend(exchange, "[ 执行指令 ] " + serverCommand + " @ " + serverID);
					System.out.println("关闭所有服务器端." + WebServer.Now());
					break;
				case "reload":
					System.out.println("[ 执行指令 ] " + serverCommand + " -> " + serverID + "" + Now());
					HttpSend(exchange, "[ 执行指令 ] " + serverCommand + " @ " + serverID);
					System.out.println("重载服务器配置列表." + WebServer.Now());
					Core.loadServerJson();
					break;
				case "exit":
					System.out.println("[ 执行指令 ] " + serverCommand + " -> " + serverID + "" + Now());
					HttpSend(exchange, "[ 执行指令 ] " + serverCommand + " @ " + serverID);
					System.out.println("退出程序." + WebServer.Now());
					System.exit(0);
					break;
				default:
					System.out.println("[ 执行指令错误 ] 指令" + serverCommand + "不存在." + Now());
					HttpSend(exchange, "指令" + serverCommand + "不存在.");
			}
		} else
		{
			System.out.println("[ 执行指令错误 ] 服务器端口" + serverID + "不存在." + Now());
			HttpSend(exchange, "服务器端口" + serverID + "不存在.");
		}
	}
	
	public static void HttpSend(HttpExchange exchange, String sendText)
	{
		exchange.getResponseHeaders()
				.add("Content-Type", "text/html; charset=UTF-8");
		byte[] httpReply = sendText.getBytes(StandardCharsets.UTF_8);
		try
		{
			exchange.sendResponseHeaders(200, httpReply.length);
			exchange.getResponseBody()
					.write(httpReply);
		} catch (IOException e)
		{
			System.out.println("[ I/O异常 ] 发送数据失败! HTTPMethod_Null@WebServer.java" + Now());
			e.printStackTrace();
		} finally
		{
			exchange.close();
		}
	}
	
	public static String Now()
	{
		SimpleDateFormat timeFormat = new SimpleDateFormat();
		timeFormat.applyPattern("yyyy/MM/dd HH:mm:ss");
		return " @ " + timeFormat.format(Calendar.getInstance()
				.getTime());
	}
	
	static TreeMap<String, Long> ServerLastHeartbeat = new TreeMap<>();
	
	public static void SetServerHeartbeat(int port)
	{
		if (Core.serverInfoList.containsKey(String.valueOf(port)))
		{
			String ProgramName = Core.serverInfoList.get(String.valueOf(port))
					.get(1);
			ServerLastHeartbeat.put(ProgramName, System.currentTimeMillis());
			System.out.println("心跳匹配文件名称：" + ProgramName + "\t时间：" + ServerLastHeartbeat.get(ProgramName));
		} else
		{
			System.out.println("心跳未匹配到配置文件.");
		}
	}
	
	public static void CheckServerHeartbeat()
	{
		long nowTimeMillis = System.currentTimeMillis();
		TreeMap<String, Long> temp_ServerLastHeartbeat = new TreeMap<>();
		
		ServerLastHeartbeat.forEach((ProgramName, TimeMillis) ->
		{
			if (nowTimeMillis - TimeMillis > 1000 * 30)
			{
				if (ProcessManager.Tasklists.get(ProgramName) != null)
				{
					Long pid = ProcessManager.FoundPidByProgramName(ProgramName);
					System.out.println(ProcessManager.KillPid(pid) ? "杀死了" + ProgramName + WebServer.Now() : "未杀死" + ProgramName + WebServer.Now());
				} else
				{
					System.out.println(ProgramName + "未启动，从心跳列表移除." + WebServer.Now());
				}
			} else
			{
				temp_ServerLastHeartbeat.put(ProgramName, TimeMillis);
				System.out.println(ProgramName + "存活中." + WebServer.Now());
			}
		});
		
		ServerLastHeartbeat.clear();
		ServerLastHeartbeat = temp_ServerLastHeartbeat;
	}
}

/*
程序启动 初始化json 解析json
启动控制台线程 用于控制台命令处理
启动http服务器线程 显示连接 维护进程表
启动程序管理线程 用于控制程序启动和退出
 */

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

// @SuppressWarnings("unchecked")
public class Core
{
	static HashMap<String, List<String>> serverInfoList = new HashMap<>();
	static HashMap<String, Integer> watchdogConfig = new HashMap<>();
	
	public static void main(String[] args)
	{
		loadWatchdogJson();
		loadServerJson();
		WebServer.init();
		Console.init();
		ProcessManager.initTasklists();
		ProcessManager.initHeartbeat();
	}
	
	public static void loadWatchdogJson()
	{
		System.out.println("读取看门狗配置文件..." + WebServer.Now());
		String jsonSource = null;
		while (jsonSource == null)
		{
			// 读取Json文件到String
			try
			{
				jsonSource = Files.readString(Path.of("./Watchdog.json"));
				// System.out.println(jsonSource);
				if (jsonSource.isEmpty())
				{
					System.out.println("空看门狗配置文件,正在重新生成配置文件示例." + WebServer.Now());
					initWatchdogJson();
				}
			} catch (IOException e)
			{
				System.out.println("看门狗配置文件文件读取错误." + WebServer.Now());
				initWatchdogJson();
			}
		}
		
		// 解析Json
		try
		{
			Gson gson = new Gson();
			watchdogConfig = gson.fromJson(jsonSource, new TypeToken<HashMap<String, Integer>>()
			{
			}.getType());
		} catch (JsonSyntaxException e)
		{
			e.printStackTrace();
		}
		
/*
		if (watchdogConfig.isEmpty())
		{
			watchdogConfig.put("FlushProcessTableInterval", "30");
			watchdogConfig.put("HeartbeatCheckInterval", "60");
		}
*/
		
		// watchdogConfig.forEach((k, v) -> System.out.println(k + "=" + v));
	}
	
	public static void initWatchdogJson()
	{
		try
		{
			Files.writeString(Path.of("./Watchdog.json"), """
					{
					    "FlushProcessTableInterval" : 30,
					    "HeartbeatCheckInterval" : 60
					}""");
		} catch (IOException e)
		{
			System.out.println("生成看门狗配置文件失败!" + WebServer.Now());
			e.printStackTrace();
			System.exit(- 1);
		}
		System.out.println("生成看门狗配置文件完毕,将使用看门狗配置默认值启动." + WebServer.Now());
	}
	
	public static void loadServerJson()
	{
		System.out.println("读取服务器配置文件..." + WebServer.Now());
		String jsonSource = null;
		// 读取Json文件到String
		try
		{
			jsonSource = Files.readString(Path.of("./ServerList.json"));
			// System.out.println(jsonSource);
			if (jsonSource.isEmpty())
			{
				System.out.println("空服务器配置文件,正在重新生成配置文件示例." + WebServer.Now());
				initServerJson();
			}
		} catch (IOException e)
		{
			System.out.println("服务器配置文件读取错误." + WebServer.Now());
			initServerJson();
		}
		
		// 解析Json
		try
		{
			Gson gson = new Gson();
			// @SuppressWarnings("unchecked")
			HashMap<String, List<String>> dirt_ServerInfoList = gson.fromJson(jsonSource, new TypeToken<HashMap<String, List<String>>>()
			{
			}.getType());
			
			// 删除配置示例
			// serverInfoList.remove("端口号");
			
			// 删除非数字Key
			assert dirt_ServerInfoList != null;
			dirt_ServerInfoList.keySet()
					.removeIf(key -> ! (key.matches("^\\d*$")));
			
			serverInfoList = dirt_ServerInfoList;
		} catch (JsonSyntaxException e)
		{
			e.printStackTrace();
		}
		
		System.out.println("读取到 [ " + serverInfoList.size() + " ] 条有效服务器配置." + WebServer.Now());
	}
	
	public static void initServerJson()
	{
		try
		{
			Files.writeString(Path.of("./ServerList.json"), """
					{
					  "端口号": [
					    "服务器端主目录",
					    "可执行文件名",
					    "运行参数"
					  ],
					  "27015": [
					    "c:\\\\Left 4 Dead 2 Server\\\\",
					    "srcds.exe",
					    "-game left4dead2 +map c2m1_highway coop -port 27015"
					  ]
					}""");
		} catch (IOException e)
		{
			System.out.println("生成服务器配置文件失败!" + WebServer.Now());
			e.printStackTrace();
			System.exit(- 1);
		}
		System.out.println("生成服务器配置文件完毕,请修改配置文件后重新启动程序." + WebServer.Now());
		System.exit(1);
	}
}

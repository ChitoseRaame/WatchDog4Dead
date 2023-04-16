/*
 * 1.常驻一个轮询 如果存在服务器配置 每N秒检测一次进程 并与服务器配置比对 储存比对通过的PID
 * 2.控制进程的启动 销毁
 */

import java.nio.file.Path;
import java.util.HashMap;

import static java.lang.ProcessHandle.allProcesses;
import static java.lang.ProcessHandle.of;

public class ProcessManager
{
	// static TreeMap<String, Long> ServerTasks = new TreeMap<>();
	static HashMap<String, Long> Tasklists = new HashMap<>();
	
	static void initTasklists()
	{
		System.out.println("初始化进程列表." + WebServer.Now());
		new Thread(() ->
		{
			while (true)
			{
				allProcesses().forEach(pid ->
				{
					if (pid.info()
							    .command()
							    .isPresent() && ! pid.info()
							.command()
							.get()
							.isEmpty())
					{
						Tasklists.put(Path.of(pid.info()
										.command()
										.get())
								.getFileName()
								.toString(), pid.pid());
					}
				});
				System.out.println("更新进程列表...共 " + Tasklists.size() + " 个进程." + WebServer.Now());
				// Tasklists.forEach((temp_fileName, temp_pid) -> System.out.println(temp_fileName + " " + temp_pid));
				try
				{
					int time = Core.watchdogConfig.get("FlushProcessTableInterval");
					//noinspection BusyWait
					Thread.sleep(1000L * time);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				Tasklists.clear();
			}
		}).start();
	}
	
	static void initHeartbeat()
	{
		System.out.println("初始化心跳检测." + WebServer.Now());
		new Thread(() ->
		{
			while (true)
			{
				try
				{
					int time = Core.watchdogConfig.get("HeartbeatCheckInterval");
					//noinspection BusyWait
					Thread.sleep(1000L * time);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				System.out.println("开始心跳检查." + WebServer.Now());
				WebServer.CheckServerHeartbeat();
			}
		}).start();
	}
	
	static Long FoundPidByProgramName(String ProgramName)
	{
		return Tasklists.get(ProgramName);
	}
	
	static boolean KillPid(long pid)
	{
		try
		{
			if (of(pid).isPresent()) of(pid).get()
					.destroy();
			return true;
		} catch (IllegalStateException e)
		{
			e.printStackTrace();
			return false;
		}
	}
}
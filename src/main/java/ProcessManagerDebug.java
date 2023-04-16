/*
 * 初始化 获取配置文件端口和文件名
 * 列出进程 单独维护一个进程列表 pid -> last heartbeat timestamp 同时启动未启动的进程 并加入pid列表内
 * 关闭全部 = 杀死列表中的全部pid 同时停止监听线程
 * 开启全部 = 启动全部进程 同时启动监听线程
 * 重载配置文件 = 与原列表对比 去除相同的配置后 对剩余的旧列表进程进行强杀
 * */

import java.nio.file.Path;
import java.util.TreeMap;

import static java.lang.ProcessHandle.allProcesses;
import static java.lang.ProcessHandle.of;

public class ProcessManagerDebug
{
	static void printallProcesses()
	{
		allProcesses().forEach(pid -> System.out.println(pid + " " + (pid.info()
				.user()
				.isPresent() ? pid.info()
				.user()
				.get() : "") + " " + (pid.info()
				.command()
				.isPresent() ? pid.info()
				.command()
				.get() : "") + " " + " " + (pid.info()
				.commandLine()
				.isPresent() ? pid.info()
				.commandLine()
				.get() : "")));
		System.out.println("Count:" + allProcesses().count());
	}
	
	static TreeMap<Long, String> pidTasks = new TreeMap<>();
	static TreeMap<String, Long> programTasks = new TreeMap<>();
	
	static void printCacheProgramTasks()
	{
		// allProcesses().forEach(pid -> System.out.println(pid.info().command().isPresent() ? pid.info().command().get() : ""));
		// Pattern executableFilename = Pattern.compile("[^\\f\\r\\n\\\\/]+\\.exe");
		allProcesses().forEach(pid ->
		{
			if (pid.info()
					    .command()
					    .isPresent() && ! pid.info()
					.command()
					.get()
					.isEmpty())
			{
				programTasks.put(Path.of(pid.info()
								.command()
								.get())
						.getFileName()
						.toString(), pid.pid());
			}
		});
		programTasks.forEach((fileName, pid) -> System.out.println(fileName + " " + pid));
	}
	
	static void printCachePidTasks()
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
				pidTasks.put(pid.pid(), Path.of(pid.info()
								.command()
								.get())
						.getFileName()
						.toString());
			}
		});
		pidTasks.forEach((pid, fileName) -> System.out.println(pid + " " + fileName));
	}
	
	static void printof(long pid)
	{
		System.out.println(of(pid).isPresent() ? of(pid).get()
				                                         .info() + " " + of(pid).get()
				                                         .info()
				                                         .command() + " " + of(pid).get()
				                                         .info()
				                                         .commandLine() : "未找到进程!");
	}
	
	static void printcommandequals(long pid, String command)
	{
		System.out.println(of(pid).isPresent() ? of(pid).get()
				.info()
				.command()
				.orElse("")
				.equals(command) ? "可执行文件路径一致. [ " + of(pid).get()
				.info()
				.command()
				.orElse("") + " ]" : of(pid).get()
						                     .info()
						                     .command() + " " + command : "未找到进程!");
	}
	
	static void printdestroy(long pid)
	{
		if (ProcessHandle.current()
				    .pid() != pid)
		{
			try
			{
				System.out.println(of(pid).isPresent() ? of(pid).get()
						.destroy() ? "已杀死进程 [ " + pid + " ]" : "未杀死进程 [ " + pid + " ]" : "未找到进程!");
			} catch (IllegalStateException e)
			{
				e.printStackTrace();
				System.out.println("未杀死进程 [ " + pid + " ]");
			}
		} else
		{
			System.out.println("不能杀死自身.");
		}
	}
	
	static void printdestroyForcibly(long pid)
	{
		if (ProcessHandle.current()
				    .pid() != pid)
		{
			try
			{
				System.out.println(of(pid).isPresent() ? of(pid).get()
						.destroyForcibly() ? "已杀死进程 [ " + pid + " ]" : "未杀死进程 [ " + pid + " ]" : "未找到进程!");
			} catch (IllegalStateException e)
			{
				e.printStackTrace();
				System.out.println("未杀死进程 [ " + pid + " ]");
			}
		} else
		{
			System.out.println("不能杀死自身.");
		}
	}
}
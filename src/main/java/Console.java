import java.util.Scanner;

public class Console
{
	static Scanner userInputStream = new Scanner(System.in);
	
	static void init()
	{
		new Thread(() ->
		{
			System.out.println("输入/help获得帮助列表." + WebServer.Now());
			while (true)
			{
				switch (userInputStream.nextLine())
				{
					case "/start":
						System.out.println("启动所有服务器端." + WebServer.Now());
						break;
					case "/stop":
						System.out.println("关闭所有服务器端." + WebServer.Now());
						break;
					case "/reload":
						System.out.println("重载服务器配置列表." + WebServer.Now());
						Core.loadServerJson();
						break;
					case "/exit":
						System.out.println("退出程序." + WebServer.Now());
						System.exit(0);
						break;
					case "/list":
						System.out.println("端口号\t\t启动文件\t\t\t\t\t\t\t\t\t\t参数");
						Core.serverInfoList.forEach((portID, config) -> System.out.println(portID + "\t\t" + config.get(0) + config.get(1) + "\t\t" + config.get(2)));
						break;
					case "/help":
					case "/?":
					case "?":
						System.out.println("命令列表:\n/start 启动配置中的所有服务器\n/stop 关闭配置中的所有服务器\n/exit 退出程序\n/reload 重载配置文件\n/help 显示此帮助列表");
						break;
					case "/debug":
						System.out.println("Debug命令列表:\n/showall 显示全部进程\n/showof 显示指定PID的进程\n/showcommand 比对制定PID的可执行文件路径\n/showkill 杀死指定PID的进程\n/showpidcache 显示PID为主键的进程列表\n/showprogramcache 显示可执行文件名为主键的进程列表");
						break;
					case "/showall":
						ProcessManagerDebug.printallProcesses();
						break;
					case "/showpidcache":
						ProcessManagerDebug.printCachePidTasks();
						break;
					case "/showprogramcache":
						ProcessManagerDebug.printCacheProgramTasks();
						break;
					case "/showof":
						System.out.print("请输入需要查看的PID:");
						try
						{
							long pid = Long.parseLong(userInputStream.nextLine());
							ProcessManagerDebug.printof(pid);
						} catch (NumberFormatException e)
						{
							System.out.println("PID输入错误!");
						}
						break;
					case "/showcommand":
						System.out.print("请输入需要比对的PID:");
						try
						{
							long pid = Long.parseLong(userInputStream.nextLine());
							System.out.print("请输入需要比对的路径:");
							String command = userInputStream.nextLine();
							ProcessManagerDebug.printcommandequals(pid, command);
						} catch (NumberFormatException e)
						{
							System.out.println("PID输入错误!");
						}
						break;
					case "/showkill":
						System.out.print("请输入需要鲨掉的PID:");
						try
						{
							long pid = Long.parseLong(userInputStream.nextLine());
							ProcessManagerDebug.printdestroy(pid);
						} catch (NumberFormatException e)
						{
							System.out.println("PID输入错误!");
						}
						break;
					case "/showkillforce":
						System.out.print("请输入需要强制鲨掉的PID:");
						try
						{
							long pid = Long.parseLong(userInputStream.nextLine());
							ProcessManagerDebug.printdestroyForcibly(pid);
						} catch (NumberFormatException e)
						{
							System.out.println("PID输入错误!");
						}
						break;
					default:
						break;
				}
			}
		}).start();
	}
}

package core.engine.hadoop.sshclient.connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.sun.org.apache.bcel.internal.generic.LCONST;

import core.engine.hadoop.sshclient.utils.environment.EnvironmentSession;

public class ResourcesManager {

	private static final String lock_command="(flock -x  200 || exit -1; echo 0; while true;do sleep 1; done;) 200>lock2";
	private static final String unlock_command="exec 200>.lock; flock -u 200 && echo 0 || echo -1";
	private static EnvironmentSession session;

	private ResourcesManager(EnvironmentSession session) {
		this.session = session;
	}

	public boolean newlock(long wait_time) throws IOException, JSchException
	{
		try{
			
			ChannelExec channel = (ChannelExec) session.getSession().openChannel("exec");
			((ChannelExec) channel).setPty(true);
		//	((ChannelExec) channel).setCommand(String.format(lock_command, wait_time));
			((ChannelExec) channel).setCommand(lock_command);
			channel.setInputStream(null);
			InputStream in = channel.getInputStream();
			channel.connect();
			String output="";
			byte[] b = new byte[1024];
			while (channel.isConnected()) {

				while (in.available() > 0) {
					int i = in.read(b, 0, 1024);
					if (i < 0)
						break;
					output += new String(b, 0, i).trim();
					
					if (output.contains("0") )
					{
						
//						channel.disconnect();
//						return true;
						System.out.println("Thread lock");
					}
//					else if (output.contains("-1")) {
//						channel.disconnect();
//						
//						return false;
//					}
				}
				if (channel.isClosed()) {
					if (in.available() > 0)
						continue;
					break;
				}

			}
			channel.disconnect();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
		
	}
	public boolean newunlock()throws IOException, JSchException
	{
		try{
			ChannelExec channel = (ChannelExec) session.getSession().openChannel("exec");
			((ChannelExec) channel).setPty(true);
			((ChannelExec) channel).setCommand(unlock_command);
			channel.setInputStream(null);
			InputStream in = channel.getInputStream();
			channel.connect();
			String output="";
			byte[] b = new byte[1024];
			while (channel.isConnected()) {

				while (in.available() > 0) {
					int i = in.read(b, 0, 1024);
					if (i < 0)
						break;
					output += new String(b, 0, i).trim();

				}
				if (channel.isClosed()) {
					if (in.available() > 0)
						continue;
					break;
				}

				if (output.contains("0") )
				{
					channel.disconnect();
					return true;
				}
				else if (output.contains("-1")) {
					channel.disconnect();
					return false;
				}

			}
			channel.disconnect();
			return false;
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public static void main(String args[])
	{

		class Thread_lock extends Thread
		{
			private int ID;
			public Thread_lock(int i)
			{
				ID=i;
			}
			public void run()
			{
				try {
				//	while(true)
					{
						Random rand=new Random();
						EnvironmentSession s=new EnvironmentSession("flavio", "127.0.0.1", "flavio", "", 22);
						ResourcesManager r=new ResourcesManager(s);
						System.out.println("Try to acquire lock by thread "+ID);
						int acquire_time=rand.nextInt(10000);
						
						if(r.newlock(10))
						System.out.println("Thread "+ID+" lock for michel1e.lock for "+acquire_time
								+" milliseconds");
						Thread.sleep(acquire_time);
						
						//if(ID==0) 
//							{
//								System.out.println("Thread "+ID+" release  lock for  michel1e.lock");
//								r.newunlock();
//							}
					}

				} catch (JSchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 

			}
		}


		//	new Thread1().start();
		new Thread_lock(0).start();
		new Thread_lock(1).start();
	}

}

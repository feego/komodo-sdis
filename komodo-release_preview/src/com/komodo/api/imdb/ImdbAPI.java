package com.komodo.api.imdb;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;

import java.io.*;  
import java.net.*;  
import java.util.*;

public class ImdbAPI
{
	static URL url = null;  
	static Scanner sc = null;  
	static String apiurl="http://www.deanclatworthy.com/imdb/";  
	static String moviename=" ";    
	static String dataurl=null;  
	static String retdata=null;  
	static InputStream is = null;  
	static DataInputStream dis = null; 
	static String details[];
	public String info[] = new String[256];
	static int ninfo = 0;
	
	public ImdbAPI()
	{
		
	}
	
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public boolean imdbApi(final Bundle savedInstanceState, String nome, AlertDialog alertDialog) {
		
		ThreadPolicy tp = ThreadPolicy.LAX;
		StrictMode.setThreadPolicy(tp);
		moviename=nome;
		int fail = 0;
		try{  
			

			//Getting movie name from user    

			//Check if user has inputted nothing or blank  
			if(moviename==null || moviename.equals("")){  
				System.out.println("No movie found");  
				System.exit(1);  
			}  

			//Remove unwanted space from moviename yb trimming it  
			moviename=moviename.trim();  

			//Replacing white spaces with + sign as white spaces are not allowed in IMDB api  
			moviename=moviename.replace(" ","+");  

			//Forming a complete url ready to send (type parameter can be JSON also)  
			dataurl=apiurl+"?q="+moviename + "&type=text";  

			System.out.println("Getting data from service");  
			System.out.println("########################################");  

			
			url = new URL(dataurl);
			is = url.openStream();
			dis  = new DataInputStream(is);  

			
			//Reading data from url  
			while((retdata = dis.readLine())!=null){  
				//Indicates that movie does not exist in IMDB databse  
				if(retdata.equals("error|Film not found")){  
					System.out.println("No such movie found");
					fail = -1;
					alertDialog.setTitle("There's a problem");
					alertDialog.setMessage("No such movie found!");
					alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which)
					{
					}
					});
					alertDialog.show(); 
				}  
				
				if(fail==-1)
				{
					return false;
				}
				//Replacing | character with # character for spliting  
				retdata=retdata.replace("|","#");

				//Splitting up string by # character and storing output in details array  
				details=retdata.split("#");
				
				info[ninfo]=details[0].toUpperCase() + " -> ";
				ninfo++;
				info[ninfo]=details[1];
				ninfo++;
			}    

		}    
		catch(Exception e){  
			System.out.println(e);  
		}  
		finally{  
			try{  

				if(dis!=null){  
					dis.close();  
				}  

				if(is!=null){  
					is.close();  
				}  

				if(sc!=null){  
					sc.close();  
				}  
			}  
			catch(Exception e2){  
				;  
			}  
		}
		return true;  


	}
}  

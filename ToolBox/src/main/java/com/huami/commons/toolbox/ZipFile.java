package com.huami.android.commons.toolbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFile {
	
	public static boolean zipCompress(File[] files,FileOutputStream fos){
		ZipOutputStream zos = null;
		try{
            zos = new ZipOutputStream(fos); 
            for(File file:files){
            	InputStream is = null;
            	try {
            		ZipEntry zipEntry = new ZipEntry(file.getName());
					zos.putNextEntry(zipEntry);				
					is = new FileInputStream(file); 
					byte[] buffer = new byte[8192];  
					int count = 0;
					while ((count = is.read(buffer)) >= 0) {  
						zos.write(buffer, 0, count);  
					}                
					zos.flush(); 
            	} catch (Exception e) {
            		e.printStackTrace();
            		return false;
				}finally{
					try{
						zos.closeEntry();  
		                is.close();
					}catch (Exception e){
						e.printStackTrace();
						return false;
					}finally{						
						is = null;
					}					 
				}                 
            }
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			try{
				if(zos != null){
					zos.finish();
		            zos.close();
				}				
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}finally{
				zos = null;
			}			
		}		
		return true;
	}
}

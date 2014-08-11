/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.extension.function;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author ningyuan 
 * 
 * Aug 6, 2014
 *
 */
public class ExFunctionClassLoader extends ClassLoader {
	
	private File pathDir;
	
	public ExFunctionClassLoader(String path) throws FileNotFoundException{
		pathDir = new File(path);
		
		if(pathDir == null){
			throw new FileNotFoundException("Could not find the path of external functions: "+path);
		}
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {  
		/*	
		 *  version problem???
		 * 
		    if(this.findLoadedClass(name) != null){
				throw new ClassNotFoundException("Class "+name+" has been loaded before.");
			}
		 */
		
		 byte[] classData = getClassData(name);   
         if (classData == null) {  
             throw new ClassNotFoundException();  
         }  
         
         Class clazz = defineClass(name, classData, 0, classData.length);   
         	System.out.println("ExFunctionClassLoader: load "+name);
         return clazz;  
		
	}
	
	private byte[] getClassData(String name) throws ClassNotFoundException {  
        InputStream in = null;
        String path = classNameToPath(name);  
        
        try {  
            
            byte[] buff = new byte[4096];  
            int len = -1;  
            in = new FileInputStream(path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();  
            
            while((len = in.read(buff)) != -1) {  
                baos.write(buff,0,len);  
            }  
            
            return baos.toByteArray();  
            
        } catch (IOException e) {  
        	e.printStackTrace();
            throw new ClassNotFoundException("Bineray file of the class "+path+" could not be readed.");  
        } finally {  
            if (in != null) {  
               try {  
                  in.close();  
               } catch(IOException e) {  
                  e.printStackTrace();  
               }  
            }  
        }  
    }  
	
	private String classNameToPath(String name) {  
        return pathDir + "/" + name.replace(".", "/") + ".class";  
    }
}

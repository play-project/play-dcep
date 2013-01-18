package eu.play_project.dcep.distributedetalis;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slf4j.LoggerFactory;

import com.jtalis.core.config.BasicConfig;

public class PrologMethods extends com.jtalis.core.config.BasicConfig{
	Logger logger = LoggerFactory.getLogger(PrologMethods.class);
	
	private static final String ETALIS_SOURCE_FILE = "etalis.P";

	private File tempDir;
	private File etalisSourceFile;
	
	/**
	 * Load PrologMethods. Inspierd by com.jtalis.core.config.BasicConfig.
	 */
	public void loadPrologMethods(){
		
	}
	


}

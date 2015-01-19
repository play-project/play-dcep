/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import eu.play_project.platformservices.querydispatcher.query.extension.function.FunctionManager;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.FunctionParameterTypeException;

/**
 * @author ningyuan 
 * 
 * Jul 30, 2014
 *
 */
public class BDPLCompiler {
	
	/*
	 * the factory to build the compiler
	 */
	private final BDPLCompilerFactory factory;
	
	/**
	 * default BDPL compiler constructor with predefined phases.
	 */
	public BDPLCompiler(String aggregator){
		factory = new BDPLCompilerFactory(aggregator);
	}
	
	/**
	 * thread safe compile bdpl query
	 * 
	 * @param queryStr
	 * @param baseURI
	 * @return
	 * @throws BDPLCompilerException
	 */
	public IBDPLQuery compile(String queryStr, String baseURI) throws BDPLCompilerException {
		
		BDPLCompilerData data = factory.getCompilerData(baseURI, queryStr);
		factory.getPhaseChain().handle(data);
		
		return data.getCompiledQuery();
	}
	
	public static void main(String[] args) throws IOException{
		FunctionManager fManager = FunctionManager.getInstance();
		
		try {
			fManager.initiateTable();
		} catch (FunctionParameterTypeException e) {
			System.out.println(e.getMessage());
		}
		
		BDPLCompiler compiler = new BDPLCompiler("default");
		
		System.out.println("Your BDPL query:");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		StringBuilder buf = new StringBuilder();
		String line = null;
		
		int emptyLineCount = 0;
		while ((line = in.readLine()) != null) {
			if (line.length() > 0) {
				emptyLineCount = 0;
				buf.append(' ').append(line).append('\n');
			}
			else {
				emptyLineCount++;
			}

			if (emptyLineCount == 2) {
				emptyLineCount = 0;
				String queryStr = buf.toString().trim();
				if (queryStr.length() > 0) {
					try {
						
						compiler.compile(queryStr, null);
						
						System.out.println();

					}
					catch (Exception e) {
						System.err.println(e.getMessage());
						e.printStackTrace();
					}
				}
				buf.setLength(0);
			}
		}
	}
}

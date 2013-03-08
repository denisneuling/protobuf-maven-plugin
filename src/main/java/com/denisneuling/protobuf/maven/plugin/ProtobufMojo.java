/*
 * Copyright 2012-2013 Denis Neuling 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package com.denisneuling.protobuf.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;


/**
 * @author Denis Neuling (denisneuling@gmail.com)
 * 
 * @phase protobuf
 * @goal generate-sources
 */
public class ProtobufMojo extends AbstractMojo{

	private final static String SUFFIX = ".proto";
	/**
	 * @parameter expression="${executable}" 
	 * @required
	 */
	private File executable;
	
	/**
	 * @parameter expression="${source}" default-value="${basedir}/src/main/protobuf/"
	 * @required
	 */
	private File inputDirectory;
	
	/**
	 * @parameter expression="${target}" default-value="${basedir}/src/main/java/"
	 * @required
	 */
	private File outputDirectory;
	
	/** {@inheritDoc} */
	public void execute() throws MojoExecutionException, MojoFailureException {
		if(!inputDirectory.exists() || !inputDirectory.isDirectory()){
			throw new MojoExecutionException("Inputdirectory \""+inputDirectory.getAbsolutePath()+"\" does not exist.");
		}
		if(!outputDirectory.exists()){
			outputDirectory.mkdirs();
		}
		
		List<String> files = resolveProtoFiles(inputDirectory);
		
		if(!files.isEmpty()){
			getLog().info("Generating sources...");
		}
		try {
			generateSources(files.toArray(new String[files.size()]));
		} catch (IOException e) {
			getLog().error(e);
			throw new MojoFailureException("Generating protcol buffers java sources failed.");
		} catch (InterruptedException e) {
			getLog().error(e);
			throw new MojoExecutionException("Generating protcol buffers java sources failed.");
		}		
	}
	
	private void generateSources(String[] protocs) throws IOException, InterruptedException{
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < protocs.length; i++){
			buffer.append(protocs[i]);
			if(i<protocs.length-1){
				buffer.append(" ");
			}
		}
		if(!executable.exists()){
			throw new IOException("Protoc executable does not exists: "+executable.getAbsolutePath());
		}
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(executable.getAbsolutePath()+" "+outputDirectory.getAbsolutePath()+buffer.toString());
		int exitCode = pr.waitFor();
		if(exitCode!=0){
			throw new IOException("Protoc exit code was >0");
		}
	}
	
	private List<String> resolveProtoFiles(File sourceDirectory){
		List<String> paths = new LinkedList<String>();
		Set<File> files = resolveFiles(sourceDirectory);
		for(File file : files){
			if(file.getAbsolutePath().endsWith(SUFFIX)){
				getLog().debug("Found: "+file.getAbsolutePath());
				paths.add(file.getAbsolutePath());
			}
		}
		return paths;
	}
	
	private Set<File> resolveFiles(File directory){
		Set<File> filesSet = new HashSet<File>();
		if(directory.isDirectory()){
			File[] files = directory.listFiles();
			for(File file : files){
				if(file.isDirectory()){
					filesSet.addAll(resolveFiles(file));
					continue;
				}
				filesSet.add(file);
			}
		}
		return filesSet;
	}

}

/**
 * Copyright 2014 Universit?? degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   @author Michele Carillo, Serrapica Flavio, Raia Francesco
 */
package it.isislab.scud.core.engine.hadoop.sshclient.utils.selection;


import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.jcraft.jsch.JSchException;

import it.isislab.scud.core.engine.hadoop.sshclient.connection.FileSystemSupport;
import it.isislab.scud.core.engine.hadoop.sshclient.connection.ScudManager;
import it.isislab.scud.core.engine.hadoop.sshclient.connection.HadoopFileSystemManager;
import it.isislab.scud.core.engine.hadoop.sshclient.utils.environment.EnvironmentSession;
import it.isislab.scud.core.engine.hadoop.sshclient.utils.simulation.executor.SCUDRUNNER;
import it.isislab.scud.core.engine.hadoop.sshclient.utils.simulation.executor.ScudRunnerUtils;

/**
 * Class for selection function    
 * 
 * @author Michele Carillo, Serrapica Flavio, Raia Francesco
 *
 */
public class SelectionFunction {
	private String hdfs_domain_xml_file;
	private String hdfs_selection_function_fileName;
	private String hdfs_input_fileName;
	private String hdfs_simulation_rating_folder;
	private String currentExecutionInputLoopPath;
	private String execBinPath;


	/*public SelectionFunction(EnvironmentSession session,
			String hdfs_domain_xml_file,
			String hdfs_input_fileName,
			String hdfs_selection_function_fileName,
			String hdfs_simulation_rating_folder,
			String hdsf_simulation_new_input_folder,
			String execBinPath) {

		this.session = session;
		this.execBinPath = execBinPath;
		this.hdfs_domain_xml_file = hdfs_domain_xml_file;
		this.hdfs_selection_function_fileName = hdfs_selection_function_fileName;
		this.hdfs_simulation_rating_folder = hdfs_simulation_rating_folder;
		this.hdsf_simulation_new_input_folder = hdsf_simulation_new_input_folder;
		this.hdfs_input_fileName = hdfs_input_fileName;
	}*/


	public SelectionFunction(String hdfs_domain_xml_file,
			String hdfs_simulation_loop_input_xml, String hdfs_selection_function_fileName,
			String hdfs_simulation_rating_folder, String currentExecutionInputLoopPath,
			String bashCommandForRunnableFunction) {
				
		this.hdfs_domain_xml_file = hdfs_domain_xml_file;
		this.hdfs_input_fileName = hdfs_simulation_loop_input_xml;
		this.hdfs_selection_function_fileName = hdfs_selection_function_fileName;
		this.hdfs_simulation_rating_folder = hdfs_simulation_rating_folder;
		this.currentExecutionInputLoopPath = currentExecutionInputLoopPath;
		this.execBinPath = bashCommandForRunnableFunction.endsWith("java")?bashCommandForRunnableFunction+" -jar":bashCommandForRunnableFunction;
		
	}


	public boolean generateNewInput(FileSystemSupport fs) throws JSchException, IOException {

		//String tmpFold = HadoopFileSystemManager.makeRemoteTempFolder(session);
		String tmpFold = fs.getRemotePathForTmpFolderForUser();
		
		if(ScudRunnerUtils.mkdir(tmpFold))
			SCUDRUNNER.log.info("Created "+tmpFold+" folder");

		String tmpFolderName = tmpFold.substring(tmpFold.lastIndexOf("/")+1, tmpFold.length());
		
		String xml_domain_fileName = fs.getRemotePathForTmpFileForUser(tmpFolderName);

		if(ScudRunnerUtils.copyFileFromHdfs(fs,hdfs_domain_xml_file, xml_domain_fileName))
			SCUDRUNNER.log.info("Copied "+hdfs_domain_xml_file+" to "+xml_domain_fileName);

		String xml_input_fileName = fs.getRemotePathForTmpFileForUser(tmpFolderName);

		if(ScudRunnerUtils.copyFileFromHdfs(fs,  hdfs_input_fileName, xml_input_fileName))
			SCUDRUNNER.log.info("Copied "+hdfs_input_fileName+" to "+xml_input_fileName);

		String rating_folder_name = fs.getRemotePathForTmpFolderForUser();

		
		if(ScudRunnerUtils.copyFilesFromHdfs(fs, hdfs_simulation_rating_folder, rating_folder_name))
			SCUDRUNNER.log.info("Copied "+hdfs_simulation_rating_folder+" to "+rating_folder_name);

		String selection_function_fileName = fs.getRemotePathForTmpFileForUser(tmpFolderName);
		
		
		if(ScudRunnerUtils.copyFileFromHdfs(fs, hdfs_selection_function_fileName, selection_function_fileName))
			SCUDRUNNER.log.info("Copied "+hdfs_selection_function_fileName+" to "+selection_function_fileName);

		String tmpSelection_Input_folder = fs.getRemotePathForTmpFolderForUser();
		
		if(ScudRunnerUtils.mkdir(tmpSelection_Input_folder))
			SCUDRUNNER.log.info("Created folder "+tmpSelection_Input_folder);


		/*String makeExecutableFilecmd="chmod +x "+selection_function_fileName;
		if(Integer.parseInt(HadoopFileSystemManager.exec(session,makeExecutableFilecmd))<0?false:true)
			SCUDRUNNER.log.info("Make executable "+selection_function_fileName);*/
		if(ScudRunnerUtils.chmodX(selection_function_fileName))
			SCUDRUNNER.log.info("Make executable "+selection_function_fileName);


		boolean result= false;
		/*String prefix = "if ";
		String postFix = "; then echo 0; else echo -1; fi";*/
		String tmpRedirectInputXmlFile=fs.getRemotePathForTmpFileForUser(tmpFolderName);
		File f = new File(tmpRedirectInputXmlFile);
		f.createNewFile();
		String cmd =execBinPath+" "+selection_function_fileName+
				" "+xml_domain_fileName+
				" "+xml_input_fileName+
				" "+rating_folder_name;
		
		SCUDRUNNER.log.info("Launch selection function. \n"+cmd);
		
		if(ScudRunnerUtils.execGenericCommand(cmd.split(" "),tmpRedirectInputXmlFile)){
			if(f.length()>0){
				if(ScudRunnerUtils.copyFileInHdfs(fs, tmpRedirectInputXmlFile, currentExecutionInputLoopPath)){
					SCUDRUNNER.log.info("Generated successfully a new input");
					result=true;
				}
					
			}else
				SCUDRUNNER.log.info("Selection function terminated.");
		}else{
			SCUDRUNNER.log.severe("Unexpected selection function terminated.");
		}
		ScudRunnerUtils.rmr(rating_folder_name);
		ScudRunnerUtils.rmr(tmpSelection_Input_folder);
		ScudRunnerUtils.rmr(tmpFold);
		return result;
	}
	
}

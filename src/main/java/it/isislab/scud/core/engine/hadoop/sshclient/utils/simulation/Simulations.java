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
package it.isislab.scud.core.engine.hadoop.sshclient.utils.simulation;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "simulations")
public class Simulations {
	
	@XmlElement(name= "simulation")
	private ArrayList<Simulation> list;
	
	public Simulations() {
		 list = new ArrayList<Simulation>();
	}
	
	public boolean addSimulation(Simulation s){
		return list.add(s);
	}
	
	public ArrayList<Simulation> getSimulations(){
		return list;
	}

}

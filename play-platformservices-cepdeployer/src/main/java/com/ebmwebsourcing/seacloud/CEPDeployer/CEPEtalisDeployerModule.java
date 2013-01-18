package com.ebmwebsourcing.seacloud.CEPDeployer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import seacloud.petalslink.com.service.management.cloud._1_0.CloudManagementException;

import com.ebmwebsourcing.seacloud.model.AbstractModule;
import com.ebmwebsourcing.seacloud.model.CEPModule;

import engine.cep.admin.api.AddStatementResponseWithActions;
import engine.cep.admin.api.AddStatementWithActions;
import engine.cep.admin.api.ListAllStatements;
import engine.cep.admin.api.ListAllStatementsResponse;
import eu.play_project.play_commons.constants.Constants;
import eu.play_project.play_platformservices.api.QueryDispatchApi;


public class CEPEtalisDeployerModule extends AbstractModule implements CEPModule {


	public CEPEtalisDeployerModule(List<URL> adminAdresses) throws CloudManagementException {
		super(adminAdresses);
	}

	private String seaCloudAddress;
	
	public String addStatement(String statementId, String statement) throws CloudManagementException {
		String subscriptionID;
		 URL wsdl =null;
	        try {
	            wsdl = new URL(Constants.getProperties().getProperty("platfomservices.querydispatchapi.endpoint") + "?wsdl");
	        } catch (MalformedURLException e) {
	            e.printStackTrace();
	        }

	        QName serviceName = new QName("http://play_platformservices.play_project.eu/", "QueryDispatchApi");
	       
	        Service service = Service.create(wsdl, serviceName);
	        QueryDispatchApi queryDispatchApi = service.getPort(QueryDispatchApi.class);
	        
	        subscriptionID = queryDispatchApi.registerQuery(statementId, statement);
		return subscriptionID;
	}



	@Override
	public String deleteStatement(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStatementById(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String updateStatement(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public ListAllStatementsResponse listAllStatements(ListAllStatements parameters) throws CloudManagementException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AddStatementResponseWithActions addStatementWithActions(AddStatementWithActions parameters) throws CloudManagementException {
		// TODO Auto-generated method stub
		return null;
	}


}

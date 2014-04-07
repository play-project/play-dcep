package eu.play_project.dcep.distributedetalis.join;

import java.util.List;

public interface SelectResults {

	public List<String> getVariables();

	public List<List> getResult();

	void setVariables(List<String> variables);

	void setResult(List<List> result);

	int getSize();

}
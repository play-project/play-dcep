package eu.play_project.play_platformservices_querydispatcher.api;

public interface QueryDispatechElement {
	
	public void accept(QueryDispatechElementVisitor visitor);
}

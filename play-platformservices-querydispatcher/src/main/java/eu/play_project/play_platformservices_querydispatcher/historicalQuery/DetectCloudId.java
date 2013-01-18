package eu.play_project.play_platformservices_querydispatcher.historicalQuery;



public class DetectCloudId {
	
	//States
	enum State{ START, COLLECT }
	static State state;

	static String cloudId;
	
	public DetectCloudId(){
	}
	
	public static void startGraph(){
		state=State.COLLECT;
		cloudId = null;
	}
	
	public static void endGraph(){
		state=State.START;
	}
	
	public static void newTriple(String prdicate, String object){

		//Detect event cloud id.
		if(prdicate.equals("http://events.event-processing.org/types/stream")){
			cloudId = object;
		}	
	}
	
	public static String getCloudId(){
		if(cloudId==null){
			throw new RuntimeException("No cloud Id found.");
		}
		
		return cloudId;
	}

}

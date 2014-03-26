package eu.play_project.play_platformservices_querydispatcher.historicalQuery;


/**
 * Every query contains source and destination cloud ids.
 * This data structure will store it till it is needed.
 * @author sobermeier
 *
 */
public class DetectCloudId {
	
	//States
	public enum State{ START, FOUND }
	static State state;

	static String cloudId;
	
	public static void setCloudId(String cloudId) {
		DetectCloudId.cloudId = cloudId;
	}

	public DetectCloudId(){
	}
	
	public static void startServiceGraph() {
		state = State.FOUND;
	}
	
	public static State getState() {
		return state;
	}

	public static void newTriple(String prdicate, String object){

		//Detect event cloud id.
		if(prdicate.equals("http://events.event-processing.org/types/stream")){
			state = State.FOUND;
			cloudId = object;
		}	
	}
	
	public static String getCloudId(){
		if( state == State.FOUND && cloudId == null){
			throw new RuntimeException("No cloud Id found.");
		}
		
		return cloudId;
	}

	public static void resetBuffer() {
		state = State.START;
		cloudId = null;
	}

}

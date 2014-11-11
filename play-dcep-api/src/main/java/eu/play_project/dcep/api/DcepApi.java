package eu.play_project.dcep.api;



public interface DcepApi<EventType> extends DcepMonitoringApi, DcepManagmentApi, DcepListenerApi<EventType>, SimplePublishApi<EventType> {

}

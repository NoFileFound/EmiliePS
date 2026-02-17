package org.genshinimpact.webserver.models;

// Imports
import com.fasterxml.jackson.databind.JsonNode;

@SuppressWarnings("unused")
public class ABTestExperimentsListModel {
    public String app_id;
    public String app_sign;
    public String uid;
    public String scene_id;
    public String experiment_id = "";
    public JsonNode params;
}
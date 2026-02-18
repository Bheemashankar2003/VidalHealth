package org.proj.vidal.model;

public class SubmitSolutionRequest {

    private String finalQuery;

    public SubmitSolutionRequest(String finalQuery){
        this.finalQuery = finalQuery;
    }

    public String getFinalQuery(){
        return finalQuery;
    }
}

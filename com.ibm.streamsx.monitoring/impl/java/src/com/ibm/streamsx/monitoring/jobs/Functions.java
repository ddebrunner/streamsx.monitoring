//
// ****************************************************************************
// * Copyright (C) 2018, International Business Machines Corporation          *
// * All rights reserved.                                                     *
// ****************************************************************************
//
//
package com.ibm.streamsx.monitoring.jobs;

import static com.ibm.streamsx.monitoring.jmx.JmxOperation.LISTENER;
import static com.ibm.streamsx.monitoring.jmx.JmxOperation.OBJECT_NAME;
import static com.ibm.streamsx.monitoring.jmx.JmxOperation.OPERATION;
import static com.ibm.streamsx.monitoring.jmx.JmxOperation.PARAMETERS;
import static com.ibm.streamsx.monitoring.jmx.JmxOperation.SIGNATURE;

import java.io.IOException;
import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.streams.function.model.Function;

public class Functions {

    @Function(description="Create canonical JMX object name for a PE.")
    public static String peObjectName(String domainId, String instanceId, long peId)
            throws MalformedObjectNameException {
        Hashtable<String, String> props = new Hashtable<>();
        props.put("type", "domain.instance.pe");
        props.put("domain", ObjectName.quote(domainId));
        props.put("instance", ObjectName.quote(instanceId));
        props.put("id", Long.toString(peId));
        return ObjectName.getInstance("com.ibm.streams.management", props).getCanonicalName();
    }

    @Function(description="Start PE JMX operation represented as JSON.")
    public static String startPeOperation(String domainId, String instanceId, long peId)
            throws IOException, MalformedObjectNameException {
        JSONObject action = new JSONObject();

        action.put(OBJECT_NAME, peObjectName(domainId, instanceId, peId));
        action.put(OPERATION, "start");

        JSONArray parameters = new JSONArray();
        parameters.add(null); // resource
        parameters.add(null); // listernerId
        action.put(PARAMETERS, parameters);

        JSONArray signature = new JSONArray();
        signature.add(String.class.getName()); // resource
        signature.add(String.class.getName()); // listernerId
        action.put(SIGNATURE, signature);
        
        action.put(LISTENER, true);

        return action.serialize();
    }

    @Function(description="Stop PE JMX operation represented as JSON.")
    public static String stopPeOperation(String domainId, String instanceId, long peId)
            throws IOException, MalformedObjectNameException {
        JSONObject action = new JSONObject();

        action.put(OBJECT_NAME, peObjectName(domainId, instanceId, peId));
        action.put(OPERATION, "stop");

        JSONArray parameters = new JSONArray();
        parameters.add(null); // listenerId
        action.put(PARAMETERS, parameters);

        JSONArray signature = new JSONArray();
        signature.add(String.class.getName()); // listenerId
        action.put(SIGNATURE, signature);
        
        action.put(LISTENER, true);

        return action.serialize();
    }
}

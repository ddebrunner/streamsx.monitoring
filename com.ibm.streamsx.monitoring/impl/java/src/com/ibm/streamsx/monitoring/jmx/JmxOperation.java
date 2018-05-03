//
// ****************************************************************************
// * Copyright (C) 2018, International Business Machines Corporation          *
// * All rights reserved.                                                     *
// ****************************************************************************
//
//
package com.ibm.streamsx.monitoring.jmx;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.StreamingInput;
import com.ibm.streams.operator.Tuple;
import com.ibm.streams.operator.metrics.Metric;
import com.ibm.streams.operator.metrics.Metric.Kind;
import com.ibm.streams.operator.model.CustomMetric;
import com.ibm.streams.operator.model.InputPortSet;
import com.ibm.streams.operator.model.InputPorts;
import com.ibm.streams.operator.model.PrimitiveOperator;

@PrimitiveOperator(description = JmxOperation.OP_DESC)
@InputPorts(@InputPortSet(cardinality = 1, description = JmxOperation.IP0_DESC))
public class JmxOperation extends AbstractJmxOperator {

    public static final String OP_DESC = "Invoke a JMX operator against a domain." + "\\n"
            + "For each input tuple a JMX operation is invoked against the configured"
            + "IBM Streams JMX domain."
            + "\\n"
            + "The operation to invoke is represented as a JSON object with these fields:\\n"
            + " * `objectName` - Name of MBean to invoke the operation against.\\n"
            + " * `operation` - Name of operation to invoke.\\n"
            + " * `parameters` (optional) - Array of parameters to pass to operation.\\n"
            + " * `signature` (optional) - Array representing signature of operation.\\n"
            + " * `listener` (optional) - True if last parameter is a listener.\\n"
            ;

    public static final String IP0_DESC = "Each tuple represents a JMX operation to invoke in JSON form.";

    public static final String OBJECT_NAME = "objectName";
    public static final String OPERATION = "operation";
    public static final String PARAMETERS = "parameters";
    public static final String SIGNATURE = "signature";
    public static final String LISTENER = "listener";
    
    public static final Logger trace = Logger.getLogger(JmxOperation.class.getName());
    
    private Metric nInstanceNotFound;

    @Override
    public void initialize(OperatorContext context) throws Exception {
        super.initialize(context);

        setupJMXConnection();
    }
    

    public Metric getnInstanceNotFound() {
        return nInstanceNotFound;
    }

    @CustomMetric(kind=Kind.COUNTER, description="Number of JMX operations against non-existent MBeans.")
    public void setnInstanceNotFound(Metric nInstanceNotFound) {
        this.nInstanceNotFound = nInstanceNotFound;
    }

    @Override
    public void process(StreamingInput<Tuple> stream, Tuple tuple) throws Exception {
        JSONObject operation = JSONObject.parse(tuple.getString(0));
        invokeOperation(operation);
    }

    protected void invokeOperation(JSONObject action) throws Exception {
        String name = (String) action.get(OBJECT_NAME);
        String operation = (String) action.get(OPERATION);
        JSONArray parameters = (JSONArray) action.get(PARAMETERS);
        JSONArray signature = (JSONArray) action.get(SIGNATURE);

        ObjectName beanName = ObjectName.getInstance(name);

        Object[] params = new Object[parameters == null ? 0 : parameters.size()];
        for (int i = 0; i < params.length; i++)
            params[i] = parameters.get(i);

        String[] sigs = new String[signature == null ? 0 : signature.size()];
        for (int i = 0; i < sigs.length; i++)
            sigs[i] = signature.get(i).toString();
        
        
        if (trace.isLoggable(Level.FINE))
            trace.fine("JMXOperation: About to invoke operation:" + operation + " against:" + name);

        MBeanServerConnection mbs = _operatorConfiguration.get_mbeanServerConnection();
        try {
            mbs.invoke(beanName, operation, params, sigs);
            if (trace.isLoggable(Level.FINE))
                trace.fine("JMXOperation: Completed operation:" + operation + " against:" + name);
        } catch (InstanceNotFoundException e) {
            if (trace.isLoggable(Level.FINE))
                trace.fine("JMXOperation: Instance not found:" + e.getMessage());
        } catch (MBeanException e) {
            System.err.println("MBeanException:" + e);
        } catch (Exception e) {
            System.err.println("Exception:" + e);
        }
    }
}

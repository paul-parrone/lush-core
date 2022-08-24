package com.px3j.lush.core.model;

import com.px3j.lush.core.exception.LushException;

import java.util.HashMap;
import java.util.Map;

/**
 * A glorified Map with factory method to instantiate from a list of key/value pairs.  This class can be used when there
 * is no desire to create a POJO for some data.
 *
 * @author Paul Parrone
 */
public class AnyModel extends HashMap<String, Object> {
    public AnyModel() {
        super();
    }

    public AnyModel(Map<String, Object> m) {
        super(m);
    }

    /**
     * Create a populated instance of AnyModel with the passed in list of key/value pairs
     *
     * @param values List of key value pairs - key1, val1, key2, val2, ...
     * @return A poplated AnyModel instance.
     */
    public static AnyModel from( Object ...values) {
        Map<String,Object> m = new HashMap<>(values.length/2);

        int index = 0;
        boolean expectKey = true;
        String key = null;

        for( Object v : values) {
            if(expectKey) {
                if( !(v instanceof String) ) {
                    throw new LushException("Expected a String for param number: " + index );
                }
                key = (String)v;
                expectKey = false;
            }
            else {
                m.put( key, v );
                expectKey = true;
            }
        }

        return new AnyModel(m);
    }
}

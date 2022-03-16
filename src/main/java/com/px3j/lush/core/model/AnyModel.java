package com.px3j.lush.core.model;

import com.px3j.lush.core.exception.LushException;

import java.util.HashMap;
import java.util.Map;

/**
 * Glorified Map with factory method to create from a list of key/value pairs.  Can be used when there is no desire to
 * create a POJO for some data.
 *
 * @author paul
 */
public class AnyModel extends HashMap<String, Object> {
    public AnyModel(Map<String, Object> m) {
        super(m);
    }

    public static AnyModel from( Object ...values) {
        Map<String,Object> m = new HashMap<>(values.length/2);

        int index = 0;
        boolean atKey = true;
        String key = null;

        for( Object v : values) {
            if(atKey) {
                if( !(v instanceof String) ) {
                    throw new LushException("Expected a String for param number: " + index );
                }
                atKey = false;
                key = (String)v;
            }
            else {
                m.put( key, v );
            }
        }

        return new AnyModel(m);
    }
}

/* 
 * Copyright 2010 by the authors indicated in the @author tags. 
 * All rights reserved. 
 * 
 * See the LICENSE file for details.
 * 
 */
package org.zamia.rtl.sim.behaviors;

import org.zamia.ZamiaException;
import org.zamia.rtl.RTLCE;
import org.zamia.rtl.RTLModule;
import org.zamia.rtl.RTLPort;
import org.zamia.rtl.RTLSignal;
import org.zamia.rtl.sim.IRTLModuleBehavior;
import org.zamia.rtl.sim.Simulator;
import org.zamia.zil.ZILType;
import org.zamia.zil.ZILTypeArray;
import org.zamia.zil.ZILTypeEnum;
import org.zamia.zil.ZILTypeInteger;
import org.zamia.zil.ZILTypePhysical;
import org.zamia.zil.ZILTypeReal;
import org.zamia.zil.ZILValue;


/**
 * @author Anton Chepurov
 * <br>Date: 13.05.2009
 * <br>Time: 18:21:11
 */
public class CEBehavior implements IRTLModuleBehavior {
    
    public void init(RTLModule aModule, Simulator aSimulator) throws ZamiaException {
    	
    	RTLCE module = (RTLCE) aModule;
    	
    	RTLPort e = module.getE();
    	RTLPort ze = module.getZE();
    	
    	RTLSignal signal = e.getSignal();
    	ZILValue ve = aSimulator.getValue(signal);
    	
    	aSimulator.setDelta(ze, ve);
    	
    }

    public void setPort(RTLPort aPort, ZILValue aValue, Simulator aSimulator) throws ZamiaException {

        RTLCE ce = ((RTLCE) aPort.getModule());

        RTLPort ze = ce.getZE();

        if (aPort == ze) {
            return;
        }

        ZILValue res = calcZE(ze, aValue);

        
        aSimulator.setDelta(ze, res);

    }

    private ZILValue calcZE(RTLPort ze, ZILValue aValue) throws ZamiaException {
        ZILType type = ze.getType();

        if ((type instanceof ZILTypeInteger) || (type instanceof ZILTypeReal) || (type instanceof ZILTypePhysical) || (type instanceof ZILTypeEnum)) {

            return aValue;

        } else if (type instanceof ZILTypeArray) {
            ZILTypeArray arrayType = (ZILTypeArray) type;

            ZILValue res = new ZILValue(arrayType, null, null);

            int cardinality = arrayType.getIndexType().getCardinality();
            for (int i = 0; i < cardinality; i++) {
                res.setValue(i, aValue);
            }
            return res;

        } else throw new ZamiaException("Internal error: Don't know how to compute value for " + type);


    }
}

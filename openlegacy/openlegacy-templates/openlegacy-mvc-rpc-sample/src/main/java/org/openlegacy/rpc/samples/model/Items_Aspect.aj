// WARNING: DO NOT EDIT THIS FILE.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.openlegacy.rpc.samples.model;

import java.util.*;
import org.openlegacy.rpc.RpcEntity;

privileged @SuppressWarnings("unused") aspect Items_Aspect {
    declare parents: Items implements RpcEntity;
    
    public List<InnerRecord> Items.getInnerRecord(){
    	return this.innerRecord;
    }
    public void Items.setInnerRecord(List<InnerRecord> innerRecord){
    	this.innerRecord = innerRecord;
    }
    
}

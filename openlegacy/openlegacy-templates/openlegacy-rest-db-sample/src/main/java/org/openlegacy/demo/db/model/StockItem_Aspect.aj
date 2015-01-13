// WARNING: DO NOT EDIT THIS FILE.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.openlegacy.demo.db.model;

import java.io.Serializable;

import java.util.*;
import javax.persistence.Entity;
import javax.persistence.Id;
//	size:  1


privileged @SuppressWarnings("unused") aspect StockItem_Aspect {

	declare parents: StockItem implements Serializable;
	
	private static final long StockItem.serialVersionUID = 1L;
	
	public Integer StockItem.getItemId(){
		return this.itemId;
	}

	public void StockItem.setItemId(Integer itemId){
		this.itemId = itemId;
	}
	public String StockItem.getDescription(){
		return this.description;
	}

	public void StockItem.setDescription(String description){
		this.description = description;
	}
	public String StockItem.getVideoUrl(){
		return this.videoUrl;
	}

	public void StockItem.setVideoUrl(String videoUrl){
		this.videoUrl = videoUrl;
	}
	public Map<String, StockItemNote> StockItem.getNotes(){
		return this.notes;
	}

	public void StockItem.setNotes(Map<String, StockItemNote> notes){
		this.notes = notes;
	}
	public Map<String, StockItemNote> StockItem.getNotes2(){
		return this.notes2;
	}

	public void StockItem.setNotes2(Map<String, StockItemNote> notes2){
		this.notes2 = notes2;
	}
	public List<StockItemImage> StockItem.getImages(){
		return this.images;
	}

	public void StockItem.setImages(List<StockItemImage> images){
		this.images = images;
	}

}
